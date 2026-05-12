/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Servizio che gestisce le transizioni di stato dei documenti di
 * trasporto/rientro nel contesto del flusso di firma HappySign.
 *
 * <p>Si occupa esclusivamente della logica di business (aggiornamento
 * stato, recupero documenti predisposti): non interagisce direttamente
 * con le API HappySign (competenza di {@link HappysignDocService}) né con
 * lo storage CMIS (competenza di {@link DocTraspRientCMISService}).</p>
 *
 * <p>Non ha dipendenze da {@code happysign-client}.</p>
 */
public class DocTraspRientFlowService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientFlowService.class);

    private static final String DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION =
            "java:global/sigla/CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession!"
                    + "it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession";

    // -----------------------------------------------------------------------
    // API pubblica
    // -----------------------------------------------------------------------

    /**
     * Recupera la lista dei documenti di trasporto/rientro nel stato
     * "predisposto alla firma" (pronti per l'invio a HappySign).
     *
     * @param userContext contesto utente corrente
     * @return lista di documenti; mai {@code null}, al più lista vuota
     */
    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma(
            UserContext userContext) {

        try {
            List<Doc_trasporto_rientroBulk> documenti =
                    getComponent().getDocumentiPredispostiAllaFirma(userContext);

            return documenti != null ? documenti : Collections.emptyList();

        } catch (ComponentException | RemoteException e) {
            log.error("Errore recupero documenti predisposti alla firma", e);
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Errore runtime recupero documenti predisposti alla firma", e);
            return Collections.emptyList();
        }
    }

    /**
     * Aggiorna il documento portandolo allo stato {@code DEFINITIVO} con
     * stato flusso {@code FIR} (firmato).
     *
     * <p>Chiamare questo metodo dopo aver verificato tramite
     * {@link HappysignDocService#getStato(String)} che il documento
     * è stato effettivamente firmato.</p>
     *
     * @param userContext contesto utente corrente
     * @param documento   documento da aggiornare
     * @throws RuntimeException se l'aggiornamento fallisce
     */
    public void aggiornaDocumentoFirmato(
            UserContext userContext,
            Doc_trasporto_rientroBulk documento) {

        try {
            documento.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            documento.setStatoFlusso("FIR");
            documento.setDataFirma(new Timestamp(System.currentTimeMillis()));
            documento.setNoteRifiuto(null);
            documento.setToBeUpdated();

            getComponent().modificaConBulk(userContext, documento);

            log.info(
                    "Documento T/R firmato aggiornato: esercizio={}, inventario={}, tipo={}, pg={}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro()
            );

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento firmato", e);
        }
    }

    /**
     * Riporta il documento allo stato {@code INSERITO} con stato flusso
     * {@code RIF} (rifiutato) e salva il motivo del rifiuto.
     *
     * <p>Chiamare questo metodo dopo aver verificato tramite
     * {@link HappysignDocService#getStato(String)} che la firma è stata
     * rifiutata.</p>
     *
     * @param userContext    contesto utente corrente
     * @param documento      documento da riportare in stato INSERITO
     * @param motivoRifiuto  testo del motivo di rifiuto (troncato a 4000 caratteri)
     * @throws RuntimeException se l'aggiornamento fallisce
     */
    public void aggiornaDocumentoRifiutato(
            UserContext userContext,
            Doc_trasporto_rientroBulk documento,
            String motivoRifiuto) {

        try {
            // Tronca il motivo rifiuto per non eccedere la colonna DB
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

            log.info(
                    "Documento T/R rifiutato riportato a INSERITO: "
                            + "esercizio={}, inventario={}, tipo={}, pg={}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro()
            );

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento rifiutato", e);
        }
    }

    // -----------------------------------------------------------------------
    // Metodi privati
    // -----------------------------------------------------------------------

    private DocTrasportoRientroComponentSession getComponent()
            throws ComponentException {

        try {
            return (DocTrasportoRientroComponentSession)
                    new InitialContext().lookup(DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION);
        } catch (NamingException e) {
            throw new ComponentException(e);
        }
    }
}