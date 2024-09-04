package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammKey;

import java.io.Serializable;

public class DocumentoGenericoPassivoDto extends DocumentoGenericoDto<DocumentoGenericoPassRigaDto> implements Serializable {

    private EnumStatoFondoEcomale enumStatoFondoEcomale;

    private EnumStatoLiqDocumentoGen stato_liquidazione;

    private EnumCausaleDocumentoGen causale;

    public EnumStatoFondoEcomale getEnumStatoFondoEcomale() {
        return enumStatoFondoEcomale;
    }

    public void setEnumStatoFondoEcomale(EnumStatoFondoEcomale enumStatoFondoEcomale) {
        this.enumStatoFondoEcomale = enumStatoFondoEcomale;
    }

    public DocumentoGenericoPassivoDto() {
        setTipoDocumentoAmmKey( new Tipo_documento_ammKey("GENERICO_S"));
    }

    public EnumStatoLiqDocumentoGen getStato_liquidazione() {
        return stato_liquidazione;
    }

    public void setStato_liquidazione(EnumStatoLiqDocumentoGen stato_liquidazione) {
        this.stato_liquidazione = stato_liquidazione;
    }

    public EnumCausaleDocumentoGen getCausale() {
        return causale;
    }

    public void setCausale(EnumCausaleDocumentoGen causale) {
        this.causale = causale;
    }
}
