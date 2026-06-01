package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.comp.DocTrasportoRientroComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("iss")
public class DocTraspRientFlowService {

    @Autowired
    private DocTrasportoRientroComponent docTrasportoRientroComponent;

    /**
     * Recupera i documenti di trasporto/rientro inviati a HappySign
     * ma non ancora completati.
     */
    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException {

        return docTrasportoRientroComponent.getDocumentiPredispostiAllaFirma(userContext);
    }

    /**
     * Aggiorna il documento quando HappySign lo restituisce firmato.
     */
    public Doc_trasporto_rientroBulk aggiornaDocumentoFirmato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato) {

        try {
            return docTrasportoRientroComponent.aggiornaDocumentoFirmato(
                    userContext,
                    doc,
                    pdfFirmato
            );

        } catch (ComponentException e) {
            throw new RuntimeException(
                    "Errore aggiornamento documento firmato HappySign",
                    e
            );
        }
    }

    /**
     * Aggiorna il documento quando HappySign segnala un rifiuto firma.
     */
    public Doc_trasporto_rientroBulk aggiornaDocumentoRifiutato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            String motivoRifiuto) {

        try {
            return docTrasportoRientroComponent.aggiornaDocumentoRifiutato(
                    userContext,
                    doc,
                    motivoRifiuto
            );

        } catch (ComponentException e) {
            throw new RuntimeException(
                    "Errore aggiornamento documento rifiutato HappySign",
                    e
            );
        }
    }
}