package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoAttivoRigaDto extends DocumentoGenericoRigaDto implements Serializable {
    private TerzoPagamentoIncasso terzoCreditore;
    private Accertamento_scadenzarioKey accertamentoScadenzarioKey;

    public TerzoPagamentoIncasso getTerzoCreditore() {
        return terzoCreditore;
    }

    public void setTerzoCreditore(TerzoPagamentoIncasso terzoCreditore) {
        this.terzoCreditore = terzoCreditore;
    }

    public Accertamento_scadenzarioKey getAccertamentoScadenzarioKey() {
        return accertamentoScadenzarioKey;
    }

    public void setAccertamentoScadenzarioKey(Accertamento_scadenzarioKey accertamentoScadenzarioKey) {
        this.accertamentoScadenzarioKey = accertamentoScadenzarioKey;
    }
}
