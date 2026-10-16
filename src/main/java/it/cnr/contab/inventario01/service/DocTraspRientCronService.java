package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.utenze00.bp.WSUserContext;
import it.cnr.jada.UserContext;
import it.iss.si.workflow.service.SignatureFlowService;
import it.iss.si.workflow.service.SignatureFlowState;
import it.iss.si.workflow.service.SignatureFlowStatus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("iss")
public class DocTraspRientCronService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientCronService.class);

    private static final int RETRY_MAX = 3;
    private static final int BATCH_SIZE = 50;

    @Autowired
    private DocTraspRientFlowService flowService;

    @Autowired
    private DocTraspRientNotificaService notificaService;

    @Autowired
    private SignatureFlowService signatureFlowService;

    private UserContext userContext;

    @PostConstruct
    public void init() {
        userContext = new WSUserContext(
                "SIGN_WORKFLOW",
                null,
                Calendar.getInstance().get(Calendar.YEAR),
                null,
                null,
                null
        );
    }

    /**
     * Job schedulato: recupera i documenti in attesa di firma, rimuove i duplicati
     * per UUID e avvia l'elaborazione a batch.
     */
    @Scheduled(cron = "${doc.trasp.rient.happysign.timer.cron.expression}")
    public void executeVerificaFirmeWorkflow() {

        long start = System.currentTimeMillis();

        try {
            log.info("START SIGN_WORKFLOW T/R - user={}, esercizio={}",
                    userContext.getUser(),
                    Calendar.getInstance().get(Calendar.YEAR));

            List<Doc_trasporto_rientroBulk> tuttiDocumenti =
                    flowService.getDocumentiPredispostiAllaFirma(userContext);

            if (tuttiDocumenti == null || tuttiDocumenti.isEmpty()) {
                log.info("Nessun documento da verificare");
                return;
            }

            // Un solo record per UUID (gestisce le righe duplicate a monte)
            Set<String> uuidVisti = new HashSet<>();
            List<Doc_trasporto_rientroBulk> documentiUnici = tuttiDocumenti.stream()
                    .filter(doc -> doc.getUuidFlussoAutorizzativo() != null
                            && !doc.getUuidFlussoAutorizzativo().trim().isEmpty())
                    .filter(doc -> uuidVisti.add(doc.getUuidFlussoAutorizzativo()))
                    .collect(Collectors.toList());

            if (documentiUnici.isEmpty()) {
                log.info("Nessun documento con UUID valido da verificare");
                return;
            }

            log.info("Documenti trovati: {}", documentiUnici.size());

            processaBatch(documentiUnici);

        } catch (Exception e) {

            log.error("Errore globale job SIGN_WORKFLOW T/R", e);
        }

        long end = System.currentTimeMillis();
        log.info("END SIGN_WORKFLOW T/R - durata={} ms", (end - start));
    }

    /** Divide i documenti in blocchi per non sovraccaricare sistema e servizi esterni. */
    private void processaBatch(List<Doc_trasporto_rientroBulk> documenti) {

        int totale = documenti.size();

        for (int i = 0; i < totale; i += BATCH_SIZE) {

            int end = Math.min(i + BATCH_SIZE, totale);
            List<Doc_trasporto_rientroBulk> batch = documenti.subList(i, end);

            log.info("Processing batch {}-{}", i, end);

            for (Doc_trasporto_rientroBulk doc : batch) {
                processaDocumentoConRetry(doc);
            }
        }
    }

    /**
     * Retry automatico per errori transitori (es. timeout SIGN_WORKFLOW),
     * fino a RETRY_MAX tentativi.
     */
    private void processaDocumentoConRetry(Doc_trasporto_rientroBulk doc) {

        int tentativi = 0;

        while (tentativi < RETRY_MAX) {
            try {
                verificaDocumento(doc);
                return;

            } catch (Exception e) {
                tentativi++;

                log.warn("Retry {} per documento pg={} errore={}",
                        tentativi, safePg(doc), e.getMessage());

                if (tentativi >= RETRY_MAX) {
                    log.error("Errore definitivo documento pg={}", safePg(doc), e);
                }
            }
        }
    }

    /**
     * Controlla lo stato su SIGN_WORKFLOW e instrada:
     * FIRMATO → aggiornamento documento + notifica firmato,
     * RIFIUTATO/ANNULLATO → aggiornamento rifiuto + notifica rifiuto,
     * INVIATO → in attesa.
     */
    private void verificaDocumento(Doc_trasporto_rientroBulk doc) throws Exception {

        if (doc == null) {
            return;
        }

        String uuid = doc.getUuidFlussoAutorizzativo();

        if (uuid == null || uuid.trim().isEmpty()) {
            return;
        }

        SignatureFlowStatus stato = signatureFlowService.getSignatureFlowStatus(uuid);

        if (stato == null || stato.state() == null) {
            log.warn("Stato nullo per UUID={}", uuid);
            return;
        }

        log.info("Doc pg={} stato={} uuid={}", safePg(doc), stato.state(), uuid);

        if (SignatureFlowState.FIRMATO.equals(stato.state())) {
            byte[] pdf = signatureFlowService.getSignedDocument(uuid);

            flowService.aggiornaDocumentoFirmato(userContext, doc, pdf);

            // Dopo l'aggiornamento riuscito. La notifica gestisce internamente le proprie eccezioni.
            notificaService.notificaFirmato(userContext, doc);

            log.info("Documento pg={} aggiornato come FIRMATO", safePg(doc));
            return;
        }

        if (SignatureFlowState.RIFIUTATO.equals(stato.state())
                || SignatureFlowState.ANNULLATO.equals(stato.state())) {

            flowService.aggiornaDocumentoRifiutato(userContext, doc, stato.message());

            notificaService.notificaRifiutato(userContext, doc, stato.message());

            log.info("Documento pg={} RIFIUTATO/ANNULLATO motivo={}",
                    safePg(doc), stato.message());
            return;
        }

        if (SignatureFlowState.INVIATO.equals(stato.state())) {
            log.debug("Doc pg={} in attesa firma", safePg(doc));
        }
    }

    /** Log sicuro: evita NullPointerException se il documento è nullo. */
    private Object safePg(Doc_trasporto_rientroBulk doc) {
        return doc != null ? doc.getPgDocTrasportoRientro() : "null";
    }
}