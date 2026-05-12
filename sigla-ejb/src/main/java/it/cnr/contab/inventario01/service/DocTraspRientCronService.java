package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class DocTraspRientCronService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientCronService.class);

    @Autowired
    private DocTraspRientFlowService flowService;

    @Autowired
    private HappysignDocService happySignDocService;

    @Autowired(required = false)
    private DocTraspRientCMISService cmisService;

    @Value("${doc.trasp.rient.happysign.timer.enabled:false}")
    private boolean timerEnabled;

    @Value("${doc.trasp.rient.happysign.timer.user:JOB_HAPPYSIGN}")
    private String timerUser;

    @Value("${doc.trasp.rient.happysign.timer.esercizio:2026}")
    private Integer timerEsercizio;

    @Value("${doc.trasp.rient.happysign.timer.cds:999}")
    private String timerCds;

    @Value("${doc.trasp.rient.happysign.timer.uo:999.000}")
    private String timerUo;

    @Value("${doc.trasp.rient.happysign.timer.cdr:999.000.000}")
    private String timerCdr;

    public void executeVerificaFirmeHappySign() {
        if (!timerEnabled) {
            log.debug("Timer HappySign Doc T/R disabilitato");
            return;
        }

        try {
            UserContext userContext = creaUserContextJob();

            log.info(
                    "Avvio verifica firme HappySign Doc T/R - user={}, esercizio={}, cds={}, uo={}, cdr={}",
                    userContext.getUser(),
                    CNRUserContext.getEsercizio(userContext),
                    CNRUserContext.getCd_cds(userContext),
                    CNRUserContext.getCd_unita_organizzativa(userContext),
                    CNRUserContext.getCd_cdr(userContext)
            );

            verificaFirmeDocumentiTrasportoRientro(userContext);

            log.info("Fine verifica firme HappySign Doc T/R");

        } catch (Exception e) {
            log.error("Errore timer Spring HappySign documenti Trasporto/Rientro", e);
        }
    }

    private UserContext creaUserContextJob() {
        return new CNRUserContext(
                timerUser,
                timerUser,
                timerEsercizio,
                timerCds,
                timerUo,
                timerCdr
        );
    }

    public void verificaFirmeDocumentiTrasportoRientro(UserContext userContext) {
        List<Doc_trasporto_rientroBulk> documenti =
                flowService.getDocumentiPredispostiAllaFirma(userContext);

        log.info("Documenti T/R da verificare su HappySign: {}", documenti.size());

        for (Doc_trasporto_rientroBulk doc : documenti) {
            try {
                verificaDocumento(userContext, doc);
            } catch (Exception e) {
                log.error(
                        "Errore verifica stato HappySign documento T/R: esercizio={}, inventario={}, tipo={}, pg={}",
                        doc.getEsercizio(),
                        doc.getPgInventario(),
                        doc.getTiDocumento(),
                        doc.getPgDocTrasportoRientro(),
                        e
                );
            }
        }
    }

    private void verificaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws Exception {

        if (doc.getIdFlussoHappysign() == null
                || doc.getIdFlussoHappysign().trim().isEmpty()) {
            return;
        }

        StatoHappySignDto stato =
                happySignDocService.getStatoFlusso(
                        doc.getIdFlussoHappysign()
                );

        if (stato == null || stato.isInviato()) {
            return;
        }

        if (stato.isFirmato()) {
            salvaPdfFirmatoSePossibile(userContext, doc);
            flowService.aggiornaDocumentoFirmato(userContext, doc);
            return;
        }

        if (stato.isRifiutato()) {
            flowService.aggiornaDocumentoRifiutato(
                    userContext,
                    doc,
                    stato.getMotivoRifiuto()
            );
        }
    }

    private void salvaPdfFirmatoSePossibile(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws Exception {

        if (cmisService == null) {
            log.warn(
                    "DocTraspRientCMISService non disponibile: salto salvataggio PDF firmato su CMIS"
            );
            return;
        }

        byte[] pdfFirmato =
                happySignDocService.getDocumentoFirmato(
                        doc.getIdFlussoHappysign()
                );

        if (pdfFirmato == null || pdfFirmato.length == 0) {
            log.warn(
                    "PDF firmato non disponibile per uuid HappySign {}",
                    doc.getIdFlussoHappysign()
            );
            return;
        }

        cmisService.salvaStampaDocumentoFirmatoSuCMIS(
                pdfFirmato,
                doc,
                userContext
        );
    }
}