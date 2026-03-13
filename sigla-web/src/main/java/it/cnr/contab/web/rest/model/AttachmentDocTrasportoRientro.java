package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import it.cnr.si.spring.storage.MimeTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Rappresenta un allegato binario usato nei processi REST relativi
 * ai documenti di Trasporto e Rientro.
 *
 * Contiene i metadati del file, il contenuto binario e la validazione
 * dei campi ricevuti dal client. Gestisce inoltre la conversione del
 * tipo MIME, accettando sia enum che stringhe JSON.
 *
 * Estende AttachmentFile e sincronizza i campi ereditati con quelli
 * destinati alla Bean Validation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentDocTrasportoRientro extends AttachmentFile {

    @NotBlank(message = "Attenzione: selezionare la tipologia di File!")
    private String typeAttachment;

    @Size(max = 255, message = "La descrizione non può superare 255 caratteri")
    private String descrizione;

    @JsonIgnore
    @NotBlank(message = "Attenzione: inserire il nome del file!")
    private String nomeFileValidation;

    @JsonIgnore
    @NotNull(message = "Attenzione: selezionare il tipo MIME!")
    private MimeTypes mimeTypesValidation;

    @JsonIgnore
    @NotNull(message = "Attenzione: inserire il contenuto del file!")
    @Size(min = 1, message = "Il file non può essere vuoto!")
    private byte[] bytesValidation;

    @Override
    public void setNomeFile(String nomeFile) {
        super.setNomeFile(nomeFile);
        this.nomeFileValidation = nomeFile;
    }

    @Override
    public void setMimeTypes(MimeTypes mimeTypes) {
        super.setMimeTypes(mimeTypes);
        this.mimeTypesValidation = mimeTypes;
    }

    @JsonSetter("mimeTypes")
    public void setMimeTypesFromString(String mimeTypeName) {
        if (mimeTypeName == null) {
            setMimeTypes(null);
            return;
        }
        try {
            setMimeTypes(MimeTypes.valueOf(mimeTypeName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valore mimeTypes non valido: '" + mimeTypeName +
                            "'. Valori ammessi: " + java.util.Arrays.toString(MimeTypes.values()), e);
        }
    }

    @Override
    public void setBytes(byte[] bytes) {
        super.setBytes(bytes);
        this.bytesValidation = bytes;
    }

    public String getTypeAttachment() {
        return typeAttachment;
    }

    public void setTypeAttachment(String typeAttachment) {
        this.typeAttachment = typeAttachment;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}