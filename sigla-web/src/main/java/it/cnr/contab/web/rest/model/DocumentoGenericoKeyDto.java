package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.CdsKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;

import java.io.Serializable;

public class DocumentoGenericoKeyDto implements Serializable {
    private CdsKey cdsKey;
    private Unita_organizzativaKey unitaOrganizzativaKey;
    private Integer esercizio;
    private String cd_tipo_documento_amm;
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

    public String getCd_tipo_documento_amm() {
        return cd_tipo_documento_amm;
    }

    public void setCd_tipo_documento_amm(String cd_tipo_documento_amm) {
        this.cd_tipo_documento_amm = cd_tipo_documento_amm;
    }

    public Long getPg_documento_generico() {
        return pg_documento_generico;
    }

    public void setPg_documento_generico(Long pg_documento_generico) {
        this.pg_documento_generico = pg_documento_generico;
    }
}
