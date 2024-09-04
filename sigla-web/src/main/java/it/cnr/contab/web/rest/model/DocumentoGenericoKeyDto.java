package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.CdsKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammKey;

import java.io.Serializable;

public class DocumentoGenericoKeyDto implements Serializable {
    private CdsKey cdsKey;
    private Unita_organizzativaKey unitaOrganizzativaKey;
    private Integer esercizio;
    private Tipo_documento_ammKey tipoDocumentoAmmKey;
    private Long pg_documento_generico;

    public CdsKey getCdsKey() {
        return cdsKey;
    }

    public void setCdsKey(CdsKey cdsKey) {
        this.cdsKey = cdsKey;
    }

    public Unita_organizzativaKey getUnitaOrganizzativaKey() {
        return unitaOrganizzativaKey;
    }

    public void setUnitaOrganizzativaKey(Unita_organizzativaKey unitaOrganizzativaKey) {
        this.unitaOrganizzativaKey = unitaOrganizzativaKey;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Tipo_documento_ammKey getTipoDocumentoAmmKey() {
        return tipoDocumentoAmmKey;
    }

    public void setTipoDocumentoAmmKey(Tipo_documento_ammKey tipoDocumentoAmmKey) {
        this.tipoDocumentoAmmKey = tipoDocumentoAmmKey;
    }

    public Long getPg_documento_generico() {
        return pg_documento_generico;
    }

    public void setPg_documento_generico(Long pg_documento_generico) {
        this.pg_documento_generico = pg_documento_generico;
    }
}
