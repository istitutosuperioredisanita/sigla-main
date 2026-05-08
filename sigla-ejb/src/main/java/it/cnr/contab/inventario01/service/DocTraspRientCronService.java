package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.jada.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cron service per verifica periodica stati HappySign.
 */
@Service
public class DocTraspRientCronService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientCronService.class);

    @Autowired
    private DocTraspRientFlowService flowService;

    @Autowired
    private UtilHappySignDocTraspRient utilHappySignDocTraspRient;

    @Autowired(required = false)
    private DocTraspRientCMISService cmisService;

    public void verificaFirmeDocumentiTrasportoRientro(UserContext userContext) {
        List<Doc_trasporto_rientroBulk> documenti =
                flowService.getDocumentiPredispostiAllaFirma(userContext);

        log.info("Documenti T/R da verificare su HappySign: {}", documenti.size());

        for (Doc_trasporto_rientroBulk doc : documenti) {
            try {
                verificaDocumento(userContext, doc);
            } catch (Exception e) {
                log.error("Errore verifica stato HappySign documento T/R: esercizio={}, tipo={}, pg={}",
                        doc.getEsercizio(),
                        doc.getTiDocumento(),
                        doc.getPgDocTrasportoRientro(),
                        e);
            }
        }
    }

    private void verificaDocumento(UserContext userContext,
                                   Doc_trasporto_rientroBulk doc) throws Exception {

        if (doc.getIdFlussoHappysign() == null || doc.getIdFlussoHappysign().trim().isEmpty()) {
            return;
        }

        StatoHappySignDto stato =
                utilHappySignDocTraspRient.getStatoFlusso(doc.getIdFlussoHappysign());

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

    private void salvaPdfFirmatoSePossibile(UserContext userContext,
                                            Doc_trasporto_rientroBulk doc) throws Exception {
        if (cmisService == null) {
            log.warn("DocTraspRientCMISService non disponibile: salto salvataggio PDF firmato su CMIS");
            return;
        }

        byte[] pdfFirmato =
                utilHappySignDocTraspRient.getDocumentoFirmato(doc.getIdFlussoHappysign());

        if (pdfFirmato == null || pdfFirmato.length == 0) {
            log.warn("PDF firmato non disponibile per uuid HappySign {}", doc.getIdFlussoHappysign());
            return;
        }

        cmisService.salvaStampaDocumentoFirmatoSuCMIS(
                pdfFirmato,
                doc,
                userContext
        );
    }
}