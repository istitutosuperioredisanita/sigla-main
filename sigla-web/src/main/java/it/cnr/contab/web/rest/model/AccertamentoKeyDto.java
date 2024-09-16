package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.CdsKey;

import java.io.Serializable;

public class AccertamentoKeyDto implements Serializable {

    private CdsKey cdsKey;

    private  Integer esercizio;

    private  Integer esercizio_originale;

    private Long pg_accertamento;

    public CdsKey getCdsKey() {
        return cdsKey;
    }

    public void setCdsKey(CdsKey cdsKey) {
        this.cdsKey = cdsKey;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Integer getEsercizio_originale() {
        return esercizio_originale;
    }

    public void setEsercizio_originale(Integer esercizio_originale) {
        this.esercizio_originale = esercizio_originale;
    }

    public Long getPg_accertamento() {
        return pg_accertamento;
    }

    public void setPg_accertamento(Long pg_accertamento) {
        this.pg_accertamento = pg_accertamento;
    }
}
