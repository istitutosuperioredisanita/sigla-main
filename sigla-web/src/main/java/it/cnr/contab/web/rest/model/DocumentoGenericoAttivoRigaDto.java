package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoAttivoRigaDto extends DocumentoGenericoRigaDto implements Serializable {
    private TerzoPagamentoIncasso terzoDebitore;
    private Accertamento_scadenzarioKey accertamentoScadenzarioKey;

    public TerzoPagamentoIncasso getTerzoDebitore() {
        return terzoDebitore;
    }

    public void setTerzoDebitore(TerzoPagamentoIncasso terzoDebitore) {
        this.terzoDebitore = terzoDebitore;
    }

    public Accertamento_scadenzarioKey getAccertamentoScadenzarioKey() {
        return accertamentoScadenzarioKey;
    }

    public void setAccertamentoScadenzarioKey(Accertamento_scadenzarioKey accertamentoScadenzarioKey) {
        this.accertamentoScadenzarioKey = accertamentoScadenzarioKey;
    }

    @Override
    public TerzoPagamentoIncasso getTerzo() {
        return terzoDebitore;
    }
}
