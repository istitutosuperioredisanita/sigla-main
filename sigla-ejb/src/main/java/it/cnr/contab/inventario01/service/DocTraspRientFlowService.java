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

import it.cnr.contab.inventario01.bulk.DocTraspRientFlowResult;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service per la gestione del flusso di firma dei documenti di Trasporto/Rientro
 * Gestisce l'aggiornamento degli stati in base ai risultati di HappySign
 */
@Service
public class DocTraspRientFlowService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientFlowService.class);

    @PersistenceContext
    private EntityManager entityManager;

    //TODO CREA SERVICE
//    @Autowired
//    private DocTrasportoRientroRespintoService docTrasportoRientroRespintoService;

    /**
     * Recupera i documenti predisposti alla firma
     * Stato = PREDISPOSTO_FIRMA (PAF) con statoFlusso = INV e idFlussoHappysign valorizzato
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma() {
        try {
            //todo FAI QUERY DAL COMPONENET

        } catch (Exception e) {
            log.error("Errore nel recupero documenti predisposti alla firma", e);
            return null;
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Aggiorna il documento dopo la firma su HappySign
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aggiornaDocumentoFirmato(Doc_trasporto_rientroBulk documento) {
        try {
            log.info("Aggiornamento documento firmato - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

            // Aggiorna lo stato del documento
            documento.setStato(Doc_trasporto_rientroBulk.STATO_FIRMATO);
            documento.setStatoFlusso("FIR");
            documento.setDataFirma(new Timestamp(System.currentTimeMillis()));
            documento.setNoteRifiuto(null);

            // Merge nel database
            entityManager.merge(documento);
            entityManager.flush();

            log.info("Documento aggiornato con successo a stato FIRMATO");

        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento del documento firmato", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Aggiorna il documento quando rifiutato su HappySign
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aggiornaDocumentoRifiutato(Doc_trasporto_rientroBulk documento, String motivoRifiuto) {
        try {
            log.info("Aggiornamento documento rifiutato - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                    documento.getEsercizio(),
                    documento.getPgInventario(),
                    documento.getTiDocumento(),
                    documento.getPgDocTrasportoRientro());

            // Crea il FlowResult per tracciare il rifiuto
            DocTraspRientFlowResult flowResult = new DocTraspRientFlowResult();
            flowResult.setEsercizio(documento.getEsercizio());
            flowResult.setPgInventario(documento.getPgInventario());
            flowResult.setTipoDocumento(documento.getTiDocumento());
            flowResult.setPgDocTrasportoRientro(documento.getPgDocTrasportoRientro());
            flowResult.setProcessInstanceId(documento.getIdFlussoHappysign());
            flowResult.setStato(DocTraspRientFlowResult.ESITO_FLUSSO_RIFIUTATO);
            flowResult.setCommento(motivoRifiuto != null && motivoRifiuto.length() > 1000 ?
                    motivoRifiuto.substring(0, 1000) : motivoRifiuto);
            flowResult.setUser("SYSTEM_HAPPYSIGN");

            //TODO da decommentare
//            // Registra il rifiuto nella tabella di storico
//            docTrasportoRientroRespintoService.inserisciDocumentoRespinto(flowResult);

            // Aggiorna lo stato del documento
            documento.setStato(Doc_trasporto_rientroBulk.STATO_INSERITO);
            documento.setStatoFlusso("RIF");
            documento.setNoteRifiuto(motivoRifiuto);
            documento.setIdFlussoHappysign(null);
            documento.setDataInvioFirma(null);
            documento.setDataFirma(null);

            // Merge nel database
            entityManager.merge(documento);
            entityManager.flush();

            log.info("Documento aggiornato con successo a stato INSERITO (rifiutato)");

        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento del documento rifiutato", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gestisce l'aggiornamento del documento in base allo stato del flusso
     * Simile alla logica di OrdineMissioneService.aggiornaOrdineMissione
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aggiornaDocumento(Doc_trasporto_rientroBulk documento, DocTraspRientFlowResult flowResult) {
        try {
            log.info("Aggiornamento documento da flusso - Stato: {}", flowResult.getStato());

            if (documento == null) {
                log.error("Documento null, impossibile aggiornare");
                return;
            }

            // Verifica che il documento sia in stato corretto
            if (!Doc_trasporto_rientroBulk.STATO_INVIATO.equals(documento.getStato()) ||
                    !"INV".equals(documento.getStatoFlusso())) {
                log.error("Esito flusso non corrispondente con lo stato del documento. " +
                                "Stato documento: {}, Stato flusso: {}, Esito ricevuto: {}",
                        documento.getStato(), documento.getStatoFlusso(), flowResult.getStato());
                return;
            }

            // Switch sullo stato del flusso
            switch (flowResult.getStato()) {
                case DocTraspRientFlowResult.ESITO_FLUSSO_FIRMATO:
                    aggiornaDocumentoFirmato(documento);
                    break;

                case DocTraspRientFlowResult.ESITO_FLUSSO_RIFIUTATO:
                    aggiornaDocumentoRifiutato(documento, flowResult.getCommento());
                    break;

                default:
                    log.warn("Stato flusso non riconosciuto: {}", flowResult.getStato());
                    break;
            }

        } catch (Exception e) {
            log.error("Errore in aggiornaDocumento", e);
            throw new RuntimeException("Errore in aggiornaDocumento: " + e.getMessage(), e);
        }
    }
}
