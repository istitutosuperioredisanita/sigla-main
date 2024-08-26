package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.CdsKey;

import java.io.Serializable;

public class ObbligazioneKeyDto implements Serializable {


    private CdsKey cdsKey;

    private  Integer esercizio;

    private  Integer esercizio_originale;

    private Long pg_obbligazione;

    public CdsKey getCdsKey() {
        return cdsKey;
    }

    public void setCdsKey(CdsKey cdsKey) {
        this.cdsKey = cdsKey;
    }
}
