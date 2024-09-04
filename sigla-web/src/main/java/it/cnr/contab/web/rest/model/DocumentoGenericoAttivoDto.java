package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammKey;

import java.io.Serializable;

public class DocumentoGenericoAttivoDto extends DocumentoGenericoDto<DocumentoGenericoAttivoRigaDto> implements Serializable {

    public DocumentoGenericoAttivoDto() {
        setTipoDocumentoAmmKey( new Tipo_documento_ammKey("GENERICO_E"));
    }
}
