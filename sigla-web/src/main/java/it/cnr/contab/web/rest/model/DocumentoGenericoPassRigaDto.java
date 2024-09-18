package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoPassRigaDto extends DocumentoGenericoRigaDto implements Serializable {

    private TerzoPagamentoIncasso terzoCreditore;
    private Obbligazione_scadenzarioKey obbligazioneScadenzarioKey;

    public TerzoPagamentoIncasso getTerzoCreditore() {
        return terzoCreditore;
    }

    public void setTerzoCreditore(TerzoPagamentoIncasso terzoCreditore) {
        this.terzoCreditore = terzoCreditore;
    }

    public Obbligazione_scadenzarioKey getObbligazioneScadenzarioKey() {
        return obbligazioneScadenzarioKey;
    }

    public void setObbligazioneScadenzarioKey(Obbligazione_scadenzarioKey obbligazioneScadenzarioKey) {
        this.obbligazioneScadenzarioKey = obbligazioneScadenzarioKey;
    }

    @Override
    public TerzoPagamentoIncasso getTerzo() {
        return terzoCreditore;
    }
}
