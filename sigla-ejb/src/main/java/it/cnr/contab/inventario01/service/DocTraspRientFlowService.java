/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Service per la gestione del flusso di firma dei documenti di Trasporto/Rientro
 * Gestisce l'aggiornamento degli stati in base ai risultati di HappySign
 */
@Service
public class DocTraspRientFlowService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientFlowService.class);

    // Nomi JNDI dei component session
    private static final String DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION =
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession";

    @Autowired
    private DocTraspRientRespintoService docTRRespService;

    @Context
    SecurityContext securityContext;

    /**
     * Recupera i documenti predisposti alla firma
     * Stato = INVIATO con statoFlusso = INV e idFlussoHappysign valorizzato
     */
    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma() {
        try {
            CNRUserContext userContext = getUserContext();

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Recupera i documenti tramite il component
            List<Doc_trasporto_rientroBulk> documenti =
                    component.getDocumentiPredispostiAllaFirma(userContext);

            log.debug("Recuperati {} documenti predisposti alla firma",
                    documenti != null ? documenti.size() : 0);

            return documenti != null ? documenti : Collections.emptyList();

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel recupero documenti predisposti alla firma", e);
            return Collections.emptyList();
        }
    }

    /**
     * Aggiorna il documento dopo la firma su HappySign
     */
    public void aggiornaDocumentoFirmato(Doc_trasporto_rientroBulk documento) {
        try {
            CNRUserContext userContext = getUserContext();

            log.info("Aggiornamento documento firmato - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Aggiorna lo stato del documento
            documento.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            documento.setStatoFlusso("FIR");
            documento.setDataFirma(new Timestamp(System.currentTimeMillis()));
            documento.setNoteRifiuto(null);

            // Salva tramite component
            component.modificaConBulk(userContext, documento);

            log.info("Documento aggiornato con successo a stato FIRMATO");

        } catch (ComponentException | RemoteException e) {
            log.error("Errore durante l'aggiornamento del documento firmato", e);
            throw new RuntimeException("Errore aggiornamento documento firmato", e);
        }
    }

    /**
     * Aggiorna il documento quando rifiutato su HappySign
     */
    public void aggiornaDocumentoRifiutato(Doc_trasporto_rientroBulk documento, String motivoRifiuto) {
        try {
            CNRUserContext userContext = getUserContext();

            log.info("Aggiornamento documento rifiutato - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

            // Tronca il motivo se troppo lungo
            String motivoTroncato = motivoRifiuto;
            if (motivoRifiuto != null && motivoRifiuto.length() > 2000) {
                motivoTroncato = motivoRifiuto.substring(0, 2000);
                log.warn("Motivo rifiuto troncato da {} a 2000 caratteri", motivoRifiuto.length());
            }

            // Inserisci record nella tabella dei respinti
            docTRRespService.inserisciDocumentoRespinto(documento, motivoTroncato);

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Aggiorna lo stato del documento
            documento.setStato(Doc_trasporto_rientroBulk.STATO_INSERITO);
            documento.setStatoFlusso("RIF");
            documento.setNoteRifiuto(motivoTroncato);
            documento.setIdFlussoHappysign(null);
            documento.setDataInvioFirma(null);
            documento.setDataFirma(null);

            // Salva tramite component
            component.modificaConBulk(userContext, documento);

            log.info("Documento aggiornato con successo a stato INSERITO (rifiutato)");

        } catch (ComponentException | RemoteException e) {
            log.error("Errore durante l'aggiornamento del documento rifiutato", e);
            throw new RuntimeException("Errore aggiornamento documento rifiutato", e);
        }
    }

    /**
     * Recupera un documento dalla chiave primaria

    public Doc_trasporto_rientroBulk recuperaDocumento(
            Long pgInventario,
            String tiDocumento,
            Integer esercizio,
            Long pgDocTrasportoRientro) throws ComponentException {

        try {
            CNRUserContext userContext = getUserContext();

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Crea il bulk con la chiave
            Doc_trasporto_rientroBulk documento = new Doc_trasporto_rientroBulk(
                    pgInventario,
                    tiDocumento,
                    esercizio,
                    pgDocTrasportoRientro
            );

            // Recupera il documento completo
            documento = (Doc_trasporto_rientroBulk)
                    component.inizializzaBulkPerModifica(userContext, documento);

            if (documento == null) {
                throw new ComponentException(
                        "Documento non trovato: " + esercizio + "/" + tiDocumento + "/" + pgDocTrasportoRientro
                );
            }

            return documento;

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel recupero documento", e);
            throw new ComponentException("Errore nel recupero documento: " + e.getMessage(), e);
        }
    }
     */

    /**
     * Salva un documento (inserimento o modifica)
     */
    public Doc_trasporto_rientroBulk salvaDocumento(Doc_trasporto_rientroBulk documento)
            throws ComponentException {

        try {
            CNRUserContext userContext = getUserContext();

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Salva (il component capisce se è insert o update)
            if (documento.isTemporaneo()) {
                // Nuovo documento
                documento = (Doc_trasporto_rientroBulk)
                        component.creaConBulk(userContext, documento);
                log.info("Documento inserito con progressivo: {}",
                        documento.getPgDocTrasportoRientro());
            } else {
                // Documento esistente
                documento = (Doc_trasporto_rientroBulk)
                        component.modificaConBulk(userContext, documento);
                log.info("Documento aggiornato: {}",
                        documento.getPgDocTrasportoRientro());
            }

            return documento;

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel salvataggio documento", e);
            throw new ComponentException("Errore nel salvataggio documento: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un documento
     */
    public void eliminaDocumento(Doc_trasporto_rientroBulk documento)
            throws ComponentException {

        try {
            CNRUserContext userContext = getUserContext();

            // Crea il component session
            DocTrasportoRientroComponentSession component = (DocTrasportoRientroComponentSession)
                    EJBCommonServices.createEJB(
                            DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION,
                            DocTrasportoRientroComponentSession.class
                    );

            // Elimina
            component.eliminaConBulk(userContext, documento);

            log.info("Documento eliminato: {}/{}/{}/{}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nell'eliminazione documento", e);
            throw new ComponentException("Errore nell'eliminazione documento: " + e.getMessage(), e);
        }
    }

    /**
     * Ottiene il CNRUserContext dal SecurityContext
     */
    private CNRUserContext getUserContext() {
        if (securityContext != null && securityContext.getUserPrincipal() instanceof CNRUserContext) {
            return (CNRUserContext) securityContext.getUserPrincipal();
        }
        throw new IllegalStateException("CNRUserContext non disponibile nel SecurityContext");
    }
}