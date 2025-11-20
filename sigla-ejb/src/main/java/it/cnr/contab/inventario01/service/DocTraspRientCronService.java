package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.si.spring.storage.StorageObject;
import it.iss.si.dto.happysign.base.EnumEsitoFlowDocumentStatus;
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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;

/**
 * Service per la verifica schedulata delle firme digitali
 * dei documenti di trasporto/rientro su HappySign
 */
@Service
public class DocTraspRientCronService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientCronService.class);

    @Autowired(required = false)
    private it.iss.si.service.HappySignService happySignService;

    @Autowired
    private DocTraspRientCMISService docTrasportoRientroCMISService;

    @Autowired
    private DocTraspRientFlowService docTraspRientFlowService;

    @Context
    SecurityContext securityContext;

    /**
     * Metodo principale chiamato dallo scheduler
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void verificaFirmeDocumentiTrasportoRientro() {
        try {
            log.info("Inizio verifica documenti trasporto/rientro su HappySign");

            List<Doc_trasporto_rientroBulk> listaDocumenti =
                    docTraspRientFlowService.getDocumentiPredispostiAllaFirma();

            if (listaDocumenti == null || listaDocumenti.isEmpty()) {
                log.info("Nessun documento da verificare");
                return;
            }

            log.info("Trovati {} documenti da verificare", listaDocumenti.size());

            int firmati = 0;
            int rifiutati = 0;
            int inAttesa = 0;
            int cancellati = 0;
            int errori = 0;

            for (Doc_trasporto_rientroBulk documento : listaDocumenti) {
                try {
                    EnumEsitoFlowDocumentStatus esito = processaDocumentoHappySign(documento);

                    if (esito == null) {
                        errori++;
                        continue;
                    }

                    switch (esito) {
                        case SIGNED:
                            firmati++;
                            break;
                        case REFUSED:
                            rifiutati++;
                            break;
                        case TOSIGN:
                            inAttesa++;
                            break;
                        case CANCELED:
                        case SEGNAD_AND_CANCELED:
                            cancellati++;
                            break;
                        default:
                            errori++;
                    }

                } catch (Exception e) {
                    errori++;
                    log.error("Errore elaborazione doc {}/{}/{}/{}",
                            documento.getEsercizio(),
                            documento.getPgInventario(),
                            documento.getTiDocumento(),
                            documento.getPgDocTrasportoRientro(), e);
                }
            }

            log.info("Verifica completata - Firmati: {}, Rifiutati: {}, In attesa: {}, Cancellati: {}, Errori: {}",
                    firmati, rifiutati, inAttesa, cancellati, errori);

        } catch (Exception e) {
            log.error("Errore critico durante verifica firme", e);
        }
    }

    /**
     * Processa un singolo documento verificandone lo stato su HappySign
     */
    private EnumEsitoFlowDocumentStatus processaDocumentoHappySign(Doc_trasporto_rientroBulk documento) throws Exception {

        if (documento.getIdFlussoHappysign() == null || documento.getIdFlussoHappysign().isEmpty()) {
            log.warn("Documento {} senza idFlusso - skip", documento.getPgDocTrasportoRientro());
            return null;
        }

        GetStatusRequest getStatusRequest = new GetStatusRequest();
        getStatusRequest.setUuid(documento.getIdFlussoHappysign());

        EnumEsitoFlowDocumentStatus statusResponse = happySignService.getDocumentStatus(getStatusRequest.getUuid());

        if (statusResponse == null || statusResponse.docstatus() == null) {
            log.error("Risposta nulla da HappySign per doc {}", documento.getIdFlussoHappysign());
            return null;
        }

        Integer docStatus = statusResponse.docstatus();
        EnumEsitoFlowDocumentStatus esito = EnumEsitoFlowDocumentStatus.esitoForDocStatus(docStatus);

        log.debug("Documento {} - stato HappySign: {} ({})",
                documento.getIdFlussoHappysign(), esito, docStatus);

        if (esito == null) {
            log.warn("Stato sconosciuto {} per doc {}", docStatus, documento.getIdFlussoHappysign());
            return null;
        }

        switch (esito) {
            case SIGNED:
                gestioneDocumentoFirmato(documento);
                break;

            case REFUSED:
                gestioneDocumentoRifiutato(documento);
                break;

            case TOSIGN:
                log.debug("Documento {} in attesa di firma", documento.getPgDocTrasportoRientro());
                break;

            case CANCELED:
            case SEGNAD_AND_CANCELED:
                log.info("Documento {} annullato su HappySign", documento.getPgDocTrasportoRientro());
                docTraspRientFlowService.aggiornaDocumentoRifiutato(documento, "Documento annullato dal flusso firma");
                break;

            default:
                log.warn("Stato non gestito per doc {}", documento.getIdFlussoHappysign());
        }

        return esito;
    }

    /**
     * Gestisce un documento firmato su HappySign
     */
    private void gestioneDocumentoFirmato(Doc_trasporto_rientroBulk documento) throws Exception {

        log.info("Documento FIRMATO - {}/{}/{}/{}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            GetDocumentRequest getDocumentRequest = new GetDocumentRequest();
            getDocumentRequest.setUuid(documento.getIdFlussoHappysign());

            GetDocumentResponse getDocumentResponse = happySignService.getDocument(getDocumentRequest.getUuid());

            if (getDocumentResponse == null || getDocumentResponse.getDocument() == null) {
                throw new Exception("PDF firmato non disponibile da HappySign");
            }

            CNRUserContext systemUserContext = getUserContext();

            StorageObject so = docTrasportoRientroCMISService.salvaStampaDocumentoFirmatoSuCMIS(
                    getDocumentResponse.getDocument(),
                    documento,
                    systemUserContext);

            log.info("PDF firmato salvato su CMIS - Key: {}", so.getKey());

            docTraspRientFlowService.aggiornaDocumentoFirmato(documento);

            log.info("Documento {} aggiornato a stato FIRMATO", documento.getPgDocTrasportoRientro());

        } catch (Exception e) {
            log.error("Errore salvataggio documento firmato", e);
            throw e;
        }
    }

    /**
     * Gestisce un documento rifiutato su HappySign
     */
    private void gestioneDocumentoRifiutato(Doc_trasporto_rientroBulk documento) throws Exception {

        log.info("Documento RIFIUTATO - {}/{}/{}/{}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            GetDocumentDetailsRequest getDetailsRequest = new GetDocumentDetailsRequest();
            getDetailsRequest.setUuid(documento.getIdFlussoHappysign());

            GetDocumentDetailResponse documentDetails = happySignService.getDocumentDetails(getDetailsRequest.getUuid());

            String motivoRifiuto = estraiMotivoRifiuto(documentDetails);

            log.info("Motivo rifiuto: {}", motivoRifiuto);

            docTraspRientFlowService.aggiornaDocumentoRifiutato(documento, motivoRifiuto);

            log.info("Documento {} aggiornato a stato ANNULLATO", documento.getPgDocTrasportoRientro());

        } catch (Exception e) {
            log.error("Errore gestione documento rifiutato", e);
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

        if (documentDetails.getDocreason() != null && !documentDetails.getDocreason().isEmpty()) {
            motivo.append(" - ").append(documentDetails.getDocreason());
        }

        if (documentDetails.getSigners() != null && documentDetails.getSigners().length > 0) {
            for (it.iss.si.dto.happysign.base.SignersDocumentDetails signer : documentDetails.getSigners()) {
                if ("REFUSE".equalsIgnoreCase(signer.getOperation()) ||
                        "REJECT".equalsIgnoreCase(signer.getOperation())) {

                    motivo.append(" - Rifiutato da: ")
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

        if (documentDetails.getCancelnote() != null && !documentDetails.getCancelnote().isEmpty()) {
            motivo.append(" - Note: ").append(documentDetails.getCancelnote());
        }

        return motivo.toString();
    }

    /**
     * Ottiene il CNRUserContext dal SecurityContext
     */
    private CNRUserContext getUserContext() {
        if (securityContext != null && securityContext.getUserPrincipal() instanceof CNRUserContext) {
            return (CNRUserContext) securityContext.getUserPrincipal();
        }

        log.warn("SecurityContext non disponibile, uso CNRUserContext vuoto");
        return new CNRUserContext();
    }
}
