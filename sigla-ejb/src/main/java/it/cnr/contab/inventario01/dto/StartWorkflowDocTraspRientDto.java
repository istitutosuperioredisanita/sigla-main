package it.cnr.contab.inventario01.dto;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.iss.si.dto.happysign.base.File;

import java.util.ArrayList;
import java.util.List;

public class StartWorkflowDocTraspRientDto {

    private Doc_trasporto_rientroBulk documento;
    private String templateName;
    private List<String> signers = new ArrayList<>();
    private List<String> approvers = new ArrayList<>();
    private File fileToSign;

    public Doc_trasporto_rientroBulk getDocumento() {
        return documento;
    }

    public void setDocumento(Doc_trasporto_rientroBulk documento) {
        this.documento = documento;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public List<String> getSigners() {
        return signers;
    }

    public void setSigners(List<String> signers) {
        this.signers = signers;
    }

    public List<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(List<String> approvers) {
        this.approvers = approvers;
    }

    public File getFileToSign() {
        return fileToSign;
    }

    public void setFileToSign(File fileToSign) {
        this.fileToSign = fileToSign;
    }

    public void addSigner(String signer) {
        if (signer != null && !signer.trim().isEmpty()) {
            signers.add(signer);
        }
    }

    public void addApprover(String approver) {
        if (approver != null && !approver.trim().isEmpty()) {
            approvers.add(approver);
        }
    }
}