package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO per allegati dei documenti di Trasporto o Rientro.
 * Contiene il tipo di allegato (aspectName CMIS) oltre ai campi di AttachmentFile.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentDocTrasportoRientro extends AttachmentFile {

    /**
     * aspectName CMIS che identifica la tipologia dell'allegato.
     */
    private String typeAttachment;

    public String getTypeAttachment() {
        return typeAttachment;
    }

    public void setTypeAttachment(String typeAttachment) {
        this.typeAttachment = typeAttachment;
    }
}