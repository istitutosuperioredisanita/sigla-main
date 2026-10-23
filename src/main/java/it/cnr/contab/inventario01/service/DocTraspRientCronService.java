    package it.cnr.contab.inventario01.service;

    import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
    import it.cnr.contab.utenze00.bp.WSUserContext;
    import it.cnr.jada.UserContext;
    import it.iss.si.signature.flow.model.SignatureStatus;
    import it.iss.si.signature.flow.model.SignatureStatusDetail;
    import it.iss.si.signature.flow.service.SignatureService;
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

    /** Job che verifica su signature-flow lo stato dei documenti T/R inviati in firma. */
    @Service
    @Profile("iss")
    public class DocTraspRientCronService {

        private static final Logger log =
                LoggerFactory.getLogger(DocTraspRientCronService.class);

        private static final int RETRY_MAX = 3;
        private static final int BATCH_SIZE = 50;
        private static final String MOTIVO_RIFIUTO_DEFAULT = "Respinta da firma su HappySign";

        @Autowired
        private DocTraspRientFlowService flowService;

        @Autowired
        private DocTraspRientNotificaService notificaService;

        // Presente solo con signature-flow.driver=HAPPYSIGN
        @Autowired
        private SignatureService signatureService;

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

        /** Recupera i documenti in attesa di firma (uno per UUID) e li elabora a batch. */
        @Scheduled(cron = "${doc.trasp.rient.happysign.timer.cron.expression}")
        public void executeVerificaFirmeWorkflow() {

            if (signatureService == null) {
                log.warn("SignatureService non disponibile (signature-flow.driver non attivo o driver non nel pacchetto): job T/R non eseguito");
                return;
            }

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

        /** Ritenta fino a RETRY_MAX volte in caso di errori transitori. */
        private void processaDocumentoConRetry(Doc_trasporto_rientroBulk doc) {

            int tentativi = 0;

            while (tentativi < RETRY_MAX) {
                try {
                    verificaDocumento(doc);
                    return;

                } catch (Exception e) {
                    tentativi++;

                    log.warn("Retry {} per documento pg={} errore={}",
                            tentativi, safePg(doc), e.getMessage(), e);

                    if (tentativi >= RETRY_MAX) {
                        log.error("Errore definitivo documento pg={}", safePg(doc), e);
                    }
                }
            }
        }

        /** FIRMATO → aggiorna e notifica; RIFIUTATO → registra il rifiuto e notifica; INVIATO → attende. */
        private void verificaDocumento(Doc_trasporto_rientroBulk doc) throws Exception {

            if (doc == null) {
                return;
            }

            String uuid = doc.getUuidFlussoAutorizzativo();

            if (uuid == null || uuid.trim().isEmpty()) {
                return;
            }

            SignatureStatusDetail dettaglio = signatureService.getSignatureStatusDetail(uuid);

            if (dettaglio == null || dettaglio.status() == null) {
                log.warn("Stato nullo per UUID={}", uuid);
                return;
            }

            log.info("Doc pg={} stato={} uuid={}", safePg(doc), dettaglio.status(), uuid);

            if (SignatureStatus.FIRMATO == dettaglio.status()) {
                byte[] pdf = signatureService.getSignedDocument(uuid);

                flowService.aggiornaDocumentoFirmato(userContext, doc, pdf);

                // La notifica gestisce internamente le proprie eccezioni
                notificaService.notificaFirmato(userContext, doc);

                log.info("Documento pg={} aggiornato come FIRMATO", safePg(doc));
                return;
            }

            if (SignatureStatus.RIFIUTATO == dettaglio.status()) {
                String motivo = motivoRifiuto(dettaglio);

                flowService.aggiornaDocumentoRifiutato(userContext, doc, motivo);

                notificaService.notificaRifiutato(userContext, doc, motivo);

                log.info("Documento pg={} RIFIUTATO motivo={}", safePg(doc), motivo);
                return;
            }

            if (SignatureStatus.INVIATO == dettaglio.status()) {
                log.debug("Doc pg={} in attesa firma", safePg(doc));
            }
        }

        private String motivoRifiuto(SignatureStatusDetail dettaglio) {
            return dettaglio.note() != null && !dettaglio.note().isEmpty()
                    ? dettaglio.note()
                    : MOTIVO_RIFIUTO_DEFAULT;
        }

        private Object safePg(Doc_trasporto_rientroBulk doc) {
            return doc != null ? doc.getPgDocTrasportoRientro() : "null";
        }
    }
