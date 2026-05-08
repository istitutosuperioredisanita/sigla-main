package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Service per gestione aggiornamento stati HappySign dei documenti T/R.
 */
@Service
public class DocTraspRientFlowService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientFlowService.class);

    private static final String DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION =
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession";

    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma(UserContext userContext) {
        try {
            DocTrasportoRientroComponentSession component = getComponent();

            List<Doc_trasporto_rientroBulk> documenti =
                    component.getDocumentiPredispostiAllaFirma(userContext);

            return documenti != null ? documenti : Collections.emptyList();

        } catch (ComponentException | RemoteException e) {
            log.error("Errore recupero documenti predisposti alla firma", e);
            return Collections.emptyList();
        }
    }

    public void aggiornaDocumentoFirmato(UserContext userContext,
                                         Doc_trasporto_rientroBulk documento) {
        try {
            documento.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            documento.setStatoFlusso("FIR");
            documento.setDataFirma(new Timestamp(System.currentTimeMillis()));
            documento.setNoteRifiuto(null);
            documento.setToBeUpdated();

            getComponent().modificaConBulk(userContext, documento);

            log.info("Documento T/R firmato aggiornato: esercizio={}, inventario={}, tipo={}, pg={}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento firmato", e);
        }
    }

    public void aggiornaDocumentoRifiutato(UserContext userContext,
                                           Doc_trasporto_rientroBulk documento,
                                           String motivoRifiuto) {
        try {
            String motivoTroncato = motivoRifiuto;
            if (motivoTroncato != null && motivoTroncato.length() > 4000) {
                motivoTroncato = motivoTroncato.substring(0, 4000);
            }

            documento.setStato(Doc_trasporto_rientroBulk.STATO_INSERITO);
            documento.setStatoFlusso("RIF");
            documento.setNoteRifiuto(motivoTroncato);
            documento.setIdFlussoHappysign(null);
            documento.setDataInvioFirma(null);
            documento.setDataFirma(null);
            documento.setToBeUpdated();

            getComponent().modificaConBulk(userContext, documento);

            log.info("Documento T/R rifiutato riportato a INSERITO: esercizio={}, inventario={}, tipo={}, pg={}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento rifiutato", e);
        }
    }

    private DocTrasportoRientroComponentSession getComponent() throws ComponentException {
        return (DocTrasportoRientroComponentSession) EJBCommonServices.createEJB(
                DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                DocTrasportoRientroComponentSession.class
        );
    }
}