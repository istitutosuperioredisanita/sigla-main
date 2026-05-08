package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.dto.StartWorkflowDocTraspRientDto;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.iss.si.dto.happysign.base.EnumEsitoFlowDocumentStatus;
import it.iss.si.dto.happysign.base.File;
import it.iss.si.dto.happysign.response.GetDocumentResponse;
import it.iss.si.service.HappySignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class UtilHappySignDocTraspRient {

    @Value("${flows.templateFirme.1Firma:#{null}}")
    private String template1Firma;

    @Value("${flows.templateFirme.2Firme:#{null}}")
    private String template2Firme;

    @Value("${flows.templateFirme.3Firme:#{null}}")
    private String template3Firme;

    @Value("${flows.templateFirme.4Firme:#{null}}")
    private String template4Firme;

    @Value("${flows.templateFirme.5Firme:#{null}}")
    private String template5Firme;

    @Autowired
    private HappySignService happySignService;

    public List<String> getNoDoubleSigners(List<String> signers) {
        if (signers == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(new LinkedHashSet<>(signers));
    }

    public void setTemplateFirme(StartWorkflowDocTraspRientDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO HappySign non valorizzato.");
        }

        if (dto.getSigners() == null || dto.getSigners().isEmpty()) {
            throw new IllegalArgumentException("Nessun firmatario presente per il flusso HappySign.");
        }

        String templateName;

        switch (dto.getSigners().size()) {
            case 1:
                templateName = template1Firma;
                break;
            case 2:
                templateName = template2Firme;
                break;
            case 3:
                templateName = template3Firme;
                break;
            case 4:
                templateName = template4Firme;
                break;
            case 5:
                templateName = template5Firme;
                break;
            default:
                throw new IllegalArgumentException(
                        "Numero firmatari non gestito da HappySign: " + dto.getSigners().size()
                );
        }

        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Template HappySign non configurato per " + dto.getSigners().size()
                            + " firmatari. Verificare flows.templateFirme."
                            + dto.getSigners().size()
                            + (dto.getSigners().size() == 1 ? "Firma" : "Firme")
            );
        }

        dto.setTemplateName(templateName);
    }

    public String send(String templateName,
                       List<String> signers,
                       List<String> approvers,
                       File fileToSign) throws Exception {

        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template HappySign non presente.");
        }

        if (fileToSign == null) {
            throw new IllegalArgumentException("File da firmare non presente.");
        }

        return happySignService.startFlowToSignSingleDocument(
                templateName,
                getNoDoubleSigners(signers),
                approvers,
                fileToSign
        );
    }

    public StatoHappySignDto getStatoFlusso(String uuidDocumento) throws Exception {
        EnumEsitoFlowDocumentStatus stato =
                happySignService.getDocumentStatus(uuidDocumento);

        if (EnumEsitoFlowDocumentStatus.SIGNED.equals(stato)) {
            return new StatoHappySignDto(StatoHappySignDto.STATO_FIRMATO, null);
        }

        if (EnumEsitoFlowDocumentStatus.REFUSED.equals(stato)) {
            return new StatoHappySignDto(
                    StatoHappySignDto.STATO_RIFIUTATO,
                    "Documento rifiutato su HappySign"
            );
        }

        return new StatoHappySignDto(StatoHappySignDto.STATO_INVIATO, null);
    }

    public byte[] getDocumentoFirmato(String uuidDocumento) throws Exception {
        GetDocumentResponse response = happySignService.getDocument(uuidDocumento);

        if (response == null) {
            throw new IllegalStateException("Risposta HappySign getDocument nulla");
        }

        if (response.getStatus() != 0) {
            throw new IllegalStateException(
                    "Errore HappySign getDocument: " + response.getReason()
            );
        }

        if (response.getDocument() == null || response.getDocument().length == 0) {
            throw new IllegalStateException(
                    "Documento firmato non presente nella risposta HappySign"
            );
        }

        return response.getDocument();
    }

    public File toHappySignFile(String fileName, byte[] content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Contenuto PDF non presente.");
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "documento.pdf";
        }

        File file = new File();
        file.setFilename(fileName);
        file.setPdf(content);
        return file;
    }
}