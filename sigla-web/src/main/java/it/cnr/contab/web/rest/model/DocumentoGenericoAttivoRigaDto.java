package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoAttivoRigaDto extends DocumentoGenericoRigaDto implements Serializable {

    private Accertamento_scadenzarioKey accertamentoScadenzarioKey;

    public Accertamento_scadenzarioKey getAccertamentoScadenzarioKey() {
        return accertamentoScadenzarioKey;
    }

    public void setAccertamentoScadenzarioKey(Accertamento_scadenzarioKey accertamentoScadenzarioKey) {
        this.accertamentoScadenzarioKey = accertamentoScadenzarioKey;
    }
}
