package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.si.spring.storage.StorageObject;
import it.iss.si.dto.happysign.request.GetDocumentDetailsRequest;
import it.iss.si.dto.happysign.request.GetDocumentRequest;
import it.iss.si.dto.happysign.request.GetStatusRequest;
import it.iss.si.dto.happysign.response.GetDocumentDetailResponse;
import it.iss.si.dto.happysign.response.GetDocumentResponse;
import it.iss.si.dto.happysign.response.GetStatusResponse;
import it.iss.si.service.HappySign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione dello scheduler di verifica firma HappySign
 * per i documenti di Trasporto/Rientro
 */
@Service
public class DocTraspRientCronService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientCronService.class);

    // Stati documento HappySign (docstatus)
    private static final int STATUS_SIGNED = 3;        // Documento firmato
    private static final int STATUS_REFUSED = 4;       // Documento rifiutato
    private static final int STATUS_PENDING = 1;       // In attesa di firma
    private static final int STATUS_IN_PROGRESS = 2;   // Firma in corso

    @Autowired
    private HappySign happySignClient;

    @Autowired
    private DocTraspRientCMISService docTrasportoRientroCMISService;

//    @Autowired
//    private DocTraspRientFlowService docTraspRientFlowService;

    /**
     * Metodo principale chiamato dallo scheduler
     * Verifica lo stato dei documenti predisposti alla firma su HappySign
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void verificaFirmeDocumentiTrasportoRientro() {

        try {
            log.debug("Inizio verifica documenti trasporto/rientro su HappySign");

            List<Doc_trasporto_rientroBulk> listaDocumenti = new ArrayList<>();
            //TODO da decommentare

//            // Recupera i documenti in stato PREDISPOSTO_FIRMA con idFlusso valorizzato
//            List<Doc_trasporto_rientroBulk> listaDocumenti =
//                    docTraspRientFlowService.getDocumentiPredispostiAllaFirma();

            if (Optional.ofNullable(listaDocumenti).isPresent() && !listaDocumenti.isEmpty()) {

                log.info("Trovati {} documenti da verificare su HappySign", listaDocumenti.size());

                for (Doc_trasporto_rientroBulk documento : listaDocumenti) {
                    try {
                        processaDocumentoHappySign(documento);
                    } catch (Exception e) {
                        log.error("Errore elaborazione documento - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}, idFlusso: {}",
                                documento.getEsercizio(),
                                documento.getPgInventario(),
                                documento.getTiDocumento(),
                                documento.getPgDocTrasportoRientro(),
                                documento.getIdFlusso(), e);
                    }
                }

            } else {
                log.debug("Nessun documento da verificare");
            }

            log.debug("Fine verifica documenti trasporto/rientro su HappySign");

        } catch (Exception e) {
            log.error("Errore generale durante la verifica firme", e);
        }
    }

    /**
     * Processa un singolo documento verificandone lo stato su HappySign
     */
    private void processaDocumentoHappySign(Doc_trasporto_rientroBulk documento) throws Exception {

        if (documento.getIdFlusso() == null || documento.getIdFlusso().isEmpty()) {
            log.warn("Documento senza idFlusso - Progressivo: {}, skip",
                    documento.getPgDocTrasportoRientro());
            return;
        }

        log.debug("Verifico stato documento - idFlusso: {}, Progressivo: {}",
                documento.getIdFlusso(), documento.getPgDocTrasportoRientro());

        // Prepara la request per verificare lo stato
        GetStatusRequest getStatusRequest = new GetStatusRequest();
        getStatusRequest.setUuid(documento.getIdFlusso());

        // Verifica lo stato del documento su HappySign
        GetStatusResponse statusResponse = happySignClient.getDocumentStatus(getStatusRequest);

        if (statusResponse == null) {
            log.error("Risposta nulla da HappySign per documento: {}", documento.getIdFlusso());
            return;
        }

        Integer docStatus = statusResponse.getDocstatus();
        String docReason = statusResponse.getDocreason();

        log.debug("Stato documento {} su HappySign: docstatus={}, docreason={}",
                documento.getIdFlusso(), docStatus, docReason);

        if (docStatus != null) {
            if (docStatus == STATUS_SIGNED) {
                // DOCUMENTO FIRMATO
                gestioneDocumentoFirmato(documento);

            } else if (docStatus == STATUS_REFUSED) {
                // DOCUMENTO RIFIUTATO
                gestioneDocumentoRifiutato(documento);

            } else {
                log.debug("Documento {} ancora in attesa di firma - stato: {}",
                        documento.getPgDocTrasportoRientro(), docStatus);
            }
        }
    }

    /**
     * Gestisce un documento firmato su HappySign
     */
    private void gestioneDocumentoFirmato(Doc_trasporto_rientroBulk documento) throws Exception {

        log.info("Documento FIRMATO su HappySign - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            // Prepara la request per scaricare il documento firmato
            GetDocumentRequest getDocumentRequest = new GetDocumentRequest();
            getDocumentRequest.setUuid(documento.getIdFlusso());

            // Scarica il documento firmato da HappySign
            GetDocumentResponse getDocumentResponse =
                    happySignClient.getDocument(getDocumentRequest);

            if (getDocumentResponse != null && getDocumentResponse.getDocument() != null) {

                // Crea un UserContext di sistema per lo scheduler
                CNRUserContext systemUserContext = createSystemUserContext();

                // Salva il documento firmato su CMIS/Azure
                StorageObject so = docTrasportoRientroCMISService.salvaStampaDocumentoFirmatoSuCMIS(
                        getDocumentResponse.getDocument(),
                        documento,
                        systemUserContext);

                log.info("Documento firmato salvato su CMIS - Key: {}", so.getKey());
                //TODO da decommentare

//                // Aggiorna lo stato del documento a FIRMATO
//                docTraspRientFlowService.aggiornaDocumentoFirmato(documento);

                log.info("Documento aggiornato a stato FIRMATO");

            } else {
                log.error("HappySign non ha restituito il PDF per il documento {}",
                        documento.getPgDocTrasportoRientro());
            }

        } catch (Exception e) {
            log.error("Errore durante il salvataggio del documento firmato", e);
            throw e;
        }
    }

    /**
     * Gestisce un documento rifiutato su HappySign
     */
    private void gestioneDocumentoRifiutato(Doc_trasporto_rientroBulk documento) throws Exception {

        log.info("Documento RIFIUTATO su HappySign - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            // Prepara la request per recuperare i dettagli del rifiuto
            GetDocumentDetailsRequest getDetailsRequest = new GetDocumentDetailsRequest();
            getDetailsRequest.setUuid(documento.getIdFlusso());

            // Recupera i dettagli del rifiuto
            GetDocumentDetailResponse documentDetails =
                    happySignClient.getDocumentDetails(getDetailsRequest);

            String motivoRifiuto = estraiMotivoRifiuto(documentDetails);

            log.info("Motivo rifiuto: {}", motivoRifiuto);
            //TODO da decommentare

//            // Aggiorna lo stato del documento ad ANNULLATO
//            docTraspRientFlowService.aggiornaDocumentoRifiutato(documento, motivoRifiuto);

            log.info("Documento aggiornato a stato ANNULLATO per rifiuto firma");

        } catch (Exception e) {
            log.error("Errore durante la gestione del documento rifiutato", e);
            throw e;
        }
    }

    /**
     * Estrae il motivo del rifiuto dai dettagli del documento HappySign
     */
    private String estraiMotivoRifiuto(GetDocumentDetailResponse documentDetails) {
        if (documentDetails == null) {
            return "Documento rifiutato durante la firma digitale";
        }

        StringBuilder motivo = new StringBuilder("Firma rifiutata");

        // Aggiungi il motivo generale se presente
        if (documentDetails.getDocreason() != null && !documentDetails.getDocreason().isEmpty()) {
            motivo.append(" - ").append(documentDetails.getDocreason());
        }

        // Estrai dettagli dai firmatari
        if (documentDetails.getSigners() != null && documentDetails.getSigners().length > 0) {
            for (it.iss.si.dto.happysign.base.SignersDocumentDetails signer : documentDetails.getSigners()) {

                if (signer.getOperation() != null &&
                        (signer.getOperation().equalsIgnoreCase("REFUSE") ||
                                signer.getOperation().equalsIgnoreCase("REJECT"))) {

                    motivo.append(" - Operazione rifiutata da: ")
                            .append(Optional.ofNullable(signer.getPerformedby()).orElse("N/D"));

                    if (signer.getNote() != null && !signer.getNote().isEmpty()) {
                        motivo.append(" - Motivo: ").append(signer.getNote());
                    }

                    if (signer.getPerformedon() != null) {
                        motivo.append(" - Data: ").append(signer.getPerformedon());
                    }
                }
            }
        }

        // Note di cancellazione
        if (documentDetails.getCancelnote() != null && !documentDetails.getCancelnote().isEmpty()) {
            motivo.append(" - Note cancellazione: ").append(documentDetails.getCancelnote());
        }

        return motivo.toString();
    }

    /**
     * Crea un CNRUserContext di sistema per lo scheduler
     * Questo metodo crea un contesto minimale per operazioni automatiche
     */
    private CNRUserContext createSystemUserContext() {
        // Crea un CNRUserContext vuoto (implementazione base)
        CNRUserContext systemUserContext = new CNRUserContext() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isTransactional() {
                return false;
            }

            @Override
            public void setTransactional(boolean flag) {
                // Non necessario per lo scheduler
            }

            @Override
            public void writeTo(java.io.PrintWriter printwriter) {
                // Non necessario per lo scheduler
            }

            @Override
            public java.util.Dictionary getHiddenColumns() {
                return new java.util.Hashtable();
            }

            @Override
            public java.util.Hashtable<String, java.io.Serializable> getAttributes() {
                return new java.util.Hashtable<>();
            }
        };

        return systemUserContext;
    }
}