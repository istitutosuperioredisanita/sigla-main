package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.utenze00.bp.WSUserContext;
import it.cnr.jada.UserContext;
import it.iss.si.workflow.service.SignatureFlowService;
import it.iss.si.workflow.service.SignatureFlowState;
import it.iss.si.workflow.service.SignatureFlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

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
    private SignatureFlowService signatureFlowService;

    private UserContext userContext;

    @PostConstruct
    public void init() {
        userContext = new WSUserContext(
                "HAPPYSIGN",
                null,
                Calendar.getInstance().get(Calendar.YEAR),
                null,
                null,
                null
        );
    }

    /**
     * Metodo principale del job schedulato.
     * Recupera tutti i documenti da verificare su HappySign e avvia l’elaborazione a batch,
     * gestendo log iniziale/finale e eventuali errori globali.
     */
    @Scheduled(
            cron = "${doc.trasp.rient.happysign.timer.cron.expression}",
            scheduler = "siglaScheduler")
    public void executeVerificaFirmeHappySign() {

        long start = System.currentTimeMillis();

        try {
            log.info("START HappySign T/R - user={}, esercizio={}",
                    userContext.getUser(),
                    Calendar.getInstance().get(Calendar.YEAR));

            List<Doc_trasporto_rientroBulk> documenti =
                    flowService.getDocumentiPredispostiAllaFirma(userContext);

            if (documenti == null || documenti.isEmpty()) {
                log.info("Nessun documento da verificare");
                return;
            }

            log.info("Documenti trovati: {}", documenti.size());

            processaBatch(documenti);

        } catch (Exception e) {
            log.error("Errore globale job HappySign T/R", e);
        }

        long end = System.currentTimeMillis();
        log.info("END HappySign T/R - durata={} ms", (end - start));
    }

    /**
     * Divide i documenti in blocchi (batch) per evitare sovraccarico su sistema e servizi esterni.
     * Ogni batch viene processato sequenzialmente, mantenendo il controllo su memoria e performance.
     */
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
     * Gestisce il retry automatico per ogni documento.
     * In caso di errore transient, ad esempio timeout HappySign, riprova fino a RETRY_MAX volte
     * prima di fallire definitivamente.
     */
    private void processaDocumentoConRetry(Doc_trasporto_rientroBulk doc) {

        int tentativi = 0;

        while (tentativi < RETRY_MAX) {
            try {
                verificaDocumento(doc);
                return;

            } catch (Exception e) {
                tentativi++;

                log.warn(
                        "Retry {} per documento pg={} errore={}",
                        tentativi,
                        safePg(doc),
                        e.getMessage()
                );

                if (tentativi >= RETRY_MAX) {
                    log.error(
                            "Errore definitivo documento pg={}",
                            safePg(doc),
                            e
                    );
                }
            }
        }
    }

    /**
     * Controlla lo stato del documento su HappySign e instrada verso le azioni corrette:
     * firmato -> aggiornamento documento,
     * rifiutato -> gestione rifiuto,
     * inviato -> in attesa.
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

        log.info(
                "Doc pg={} stato={} uuid={}",
                doc.getPgDocTrasportoRientro(),
                stato.state(),
                uuid
        );

        if (SignatureFlowState.FIRMATO.equals(stato.state())) {
            byte[] pdf = signatureFlowService.getSignedDocument(uuid);

            flowService.aggiornaDocumentoFirmato(
                    userContext,
                    doc,
                    pdf
            );

            log.info("Documento pg={} aggiornato come FIRMATO", safePg(doc));
            return;
        }

        if (SignatureFlowState.RIFIUTATO.equals(stato.state())
                || SignatureFlowState.ANNULLATO.equals(stato.state())) {

            flowService.aggiornaDocumentoRifiutato(
                    userContext,
                    doc,
                    stato.message()
            );

            log.info(
                    "Documento pg={} RIFIUTATO/ANNULLATO motivo={}",
                    safePg(doc),
                    stato.message()
            );
            return;
        }

        if (SignatureFlowState.INVIATO.equals(stato.state())) {
            log.debug("Doc pg={} in attesa firma", safePg(doc));
        }
    }


    /**
     * Metodo di utilità per log safe.
     * Evita NullPointerException quando il documento è nullo durante il logging.
     */
    private Object safePg(Doc_trasporto_rientroBulk doc) {
        return doc != null ? doc.getPgDocTrasportoRientro() : "null";
    }
}