package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoKey;

import java.io.Serializable;

public class TerzoPagamentoIncasso implements Serializable {
    private TerzoKey terzoKey;
    private Long  pg_banca;
    private Rif_modalita_pagamentoKey rifModalitaPagamentoKey;

    public TerzoKey getTerzoKey() {
        return terzoKey;
    }

    public void setTerzoKey(TerzoKey terzoKey) {
        this.terzoKey = terzoKey;
    }

    public Long getPg_banca() {
        return pg_banca;
    }

    public void setPg_banca(Long pg_banca) {
        this.pg_banca = pg_banca;
    }

    public Rif_modalita_pagamentoKey getRifModalitaPagamentoKey() {
        return rifModalitaPagamentoKey;
    }

    public void setRifModalitaPagamentoKey(Rif_modalita_pagamentoKey rifModalitaPagamentoKey) {
        this.rifModalitaPagamentoKey = rifModalitaPagamentoKey;
    }
}
