package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoAttivoRigaDto extends DocumentoGenericoRigaDto implements Serializable {
    private TerzoKey terzoDebitore;

    private TerzoPagamentoIncasso terzoUo;

    public TerzoPagamentoIncasso getTerzoUo() {
        return terzoUo;
    }

    public void setTerzoUo(TerzoPagamentoIncasso terzoUo) {
        this.terzoUo = terzoUo;
    }

    private Accertamento_scadenzarioKey accertamentoScadenzarioKey;

    public TerzoKey getTerzoDebitore() {
        return terzoDebitore;
    }

    public void setTerzoDebitore(TerzoKey terzoDebitore) {
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
        return terzoUo;
    }
}
