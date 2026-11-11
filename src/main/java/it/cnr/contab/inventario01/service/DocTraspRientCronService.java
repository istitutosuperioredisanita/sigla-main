package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.inventario01.util.DocTraspRientUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.iss.si.signature.flow.model.SignatureStatus;
import it.iss.si.signature.flow.model.SignatureStatusDetail;
import it.iss.si.signature.flow.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job che verifica su signature-flow lo stato dei documenti T/R inviati in firma
 * e notifica l'esito via email. Attivo solo con profilo Spring "iss".
 * Come SituazioneAmministrativaService: EJB creato in afterPropertiesSet, flag di abilitazione,
 * lavoro e invio mail (SendMail) nel component via EJB.
 */
@Service
@Profile("iss")
public class DocTraspRientCronService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DocTraspRientCronService.class);

    private static final int RETRY_MAX = 3;
    private static final int BATCH_SIZE = 50;
    private static final String MOTIVO_RIFIUTO_DEFAULT = "Respinta da firma su HappySign";
    /** Utente usato se signature-flow.driver non è valorizzato. */
    private static final String UTENTE_SIGNATUREFLOW_DEFAULT = "SIGNATUREFLOW";

    private DocTrasportoRientroComponentSession docTrasportoRientroComponentSession;

    // Presente solo con signature-flow.driver configurato
    @Autowired(required = false)
    private SignatureService signatureService;

    @Value("${doc.trasp.rient.happysign.timer.enabled:false}")
    private Boolean verificaFirmeCronEnable;

    @Value("${signature-flow.driver:}")
    private String signatureFlowDriver;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.docTrasportoRientroComponentSession = Optional.ofNullable(EJBCommonServices.createEJB("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession"))
                .filter(DocTrasportoRientroComponentSession.class::isInstance)
                .map(DocTrasportoRientroComponentSession.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("cannot find ejb CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession"));
    }

    @Scheduled(cron = "${doc.trasp.rient.happysign.timer.cron.expression}")
    public void executeVerificaFirmeSignatureflow() {
        try {
            if (verificaFirmeCronEnable) {
                if (signatureService == null) {
                    logger.warn("Doc T/R Service: SignatureService non disponibile (signature-flow.driver non configurato), job non eseguito");
                    return;
                }
                logger.info("Doc T/R Service: verificaFirmeSignatureflow started at: {}", LocalDateTime.now());

                UserContext userContext = new CNRUserContext(getUtenteSignatureflow(), null, LocalDate.now().getYear(), null, null, null);
                verificaFirmeSignatureflow(userContext);

                logger.info("Doc T/R Service: verificaFirmeSignatureflow end at: {}", LocalDateTime.now());
            } else {
                logger.info("Doc T/R Service: verificaFirmeSignatureflow disabled");
            }
        } catch (Throwable _ex) {
            logger.error("Doc T/R Service: verificaFirmeSignatureflow ScheduleExecutor error", _ex);
        }
    }

    /** Utente del job = nome del driver signature-flow così come letto dalla configurazione. */
    private String getUtenteSignatureflow() {
        return Optional.ofNullable(signatureFlowDriver)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(UTENTE_SIGNATUREFLOW_DEFAULT);
    }

    /** Recupera i documenti in attesa di firma (uno per UUID) e li elabora a batch. */
    @SuppressWarnings("unchecked")
    private void verificaFirmeSignatureflow(UserContext userContext) throws Exception {
        List<Doc_trasporto_rientroBulk> tuttiDocumenti =
                docTrasportoRientroComponentSession.getDocumentiPredispostiAllaFirma(userContext);

        if (tuttiDocumenti == null || tuttiDocumenti.isEmpty()) {
            logger.info("Doc T/R Service: nessun documento da verificare");
            return;
        }

        Set<String> uuidVisti = new HashSet<>();
        List<Doc_trasporto_rientroBulk> documentiUnici = tuttiDocumenti.stream()
                .filter(doc -> doc.getUuidFlussoAutorizzativo() != null
                        && !doc.getUuidFlussoAutorizzativo().trim().isEmpty())
                .filter(doc -> uuidVisti.add(doc.getUuidFlussoAutorizzativo()))
                .collect(Collectors.toList());

        if (documentiUnici.isEmpty()) {
            logger.info("Doc T/R Service: nessun documento con UUID valido da verificare");
            return;
        }

        logger.info("Doc T/R Service: documenti trovati: {}", documentiUnici.size());

        for (int i = 0; i < documentiUnici.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, documentiUnici.size());
            logger.info("Doc T/R Service: processing batch {}-{}", i, end);
            for (Doc_trasporto_rientroBulk doc : documentiUnici.subList(i, end)) {
                processaDocumentoConRetry(userContext, doc);
            }
        }
    }

    /** Ritenta fino a RETRY_MAX volte in caso di errori transitori. */
    private void processaDocumentoConRetry(UserContext userContext, Doc_trasporto_rientroBulk doc) {
        for (int tentativi = 1; tentativi <= RETRY_MAX; tentativi++) {
            try {
                verificaDocumento(userContext, doc);
                return;
            } catch (Exception e) {
                if (tentativi < RETRY_MAX)
                    logger.warn("Doc T/R Service: retry {} per documento pg={} errore={}", tentativi, doc.getPgDocTrasportoRientro(), e.getMessage(), e);
                else
                    logger.error("Doc T/R Service: errore definitivo documento pg={}", doc.getPgDocTrasportoRientro(), e);
            }
        }
    }

    /** FIRMATO: aggiorna e notifica; RIFIUTATO: registra il rifiuto e notifica; INVIATO: attende. */
    private void verificaDocumento(UserContext userContext, Doc_trasporto_rientroBulk doc) throws Exception {
        String uuid = doc.getUuidFlussoAutorizzativo();

        SignatureStatusDetail dettaglio = signatureService.getSignatureStatusDetail(uuid);
        if (dettaglio == null || dettaglio.status() == null) {
            logger.warn("Doc T/R Service: stato nullo per UUID={}", uuid);
            return;
        }

        logger.info("Doc T/R Service: doc pg={} stato={} uuid={}", doc.getPgDocTrasportoRientro(), dettaglio.status(), uuid);

        if (SignatureStatus.FIRMATO == dettaglio.status()) {
            byte[] pdf = signatureService.getSignedDocument(uuid);
            docTrasportoRientroComponentSession.aggiornaDocumentoFirmato(userContext, doc, pdf);
            logger.info("Doc T/R Service: documento pg={} aggiornato come FIRMATO", doc.getPgDocTrasportoRientro());

            sendMail(userContext, doc,
                    DocTraspRientUtil.oggettoNotificaFirmato(doc),
                    DocTraspRientUtil.testoNotificaFirmato(doc));
            return;
        }

        if (SignatureStatus.RIFIUTATO == dettaglio.status()) {
            String motivo = Optional.ofNullable(dettaglio.note())
                    .filter(note -> !note.isEmpty())
                    .orElse(MOTIVO_RIFIUTO_DEFAULT);
            docTrasportoRientroComponentSession.aggiornaDocumentoRifiutato(userContext, doc, motivo);
            logger.info("Doc T/R Service: documento pg={} RIFIUTATO motivo={}", doc.getPgDocTrasportoRientro(), motivo);

            sendMail(userContext, doc,
                    DocTraspRientUtil.oggettoNotificaRifiutato(doc),
                    DocTraspRientUtil.testoNotificaRifiutato(doc, motivo));
        }
    }

    /**
     * Invio nel component (SendMail.sendMail, come sendAlertMail).
     * Un errore di invio non deve far ripetere l'elaborazione: l'esito firma è già salvato.
     */
    private void sendMail(UserContext userContext, Doc_trasporto_rientroBulk doc, String subject, String msg) {
        try {
            docTrasportoRientroComponentSession.inviaNotifica(userContext, doc, subject, msg);
        } catch (Exception e) {
            logger.error("Doc T/R Service: sendMail error pg={}", doc.getPgDocTrasportoRientro(), e);
        }
    }
}
