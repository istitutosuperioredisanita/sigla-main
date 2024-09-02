package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoPassRigaDto extends DocumentoGenericoRigaDto implements Serializable {

    private TerzoPagamentoIncasso terzoDebitore;
    private Obbligazione_scadenzarioKey obbligazioneScadenzarioKey;

    public TerzoPagamentoIncasso getTerzoDebitore() {
        return terzoDebitore;
    }

    public void setTerzoDebitore(TerzoPagamentoIncasso terzoDebitore) {
        this.terzoDebitore = terzoDebitore;
    }

    public Obbligazione_scadenzarioKey getObbligazioneScadenzarioKey() {
        return obbligazioneScadenzarioKey;
    }

    public void setObbligazioneScadenzarioKey(Obbligazione_scadenzarioKey obbligazioneScadenzarioKey) {
        this.obbligazioneScadenzarioKey = obbligazioneScadenzarioKey;
    }
}
