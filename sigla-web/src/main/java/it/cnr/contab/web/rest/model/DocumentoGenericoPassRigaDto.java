package it.cnr.contab.web.rest.model;

import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioKey;

import java.io.Serializable;

public class DocumentoGenericoPassRigaDto extends DocumentoGenericoRigaDto implements Serializable {

    private Obbligazione_scadenzarioKey obbligazioneScadenzarioKey;

    public Obbligazione_scadenzarioKey getObbligazioneScadenzarioKey() {
        return obbligazioneScadenzarioKey;
    }

    public void setObbligazioneScadenzarioKey(Obbligazione_scadenzarioKey obbligazioneScadenzarioKey) {
        this.obbligazioneScadenzarioKey = obbligazioneScadenzarioKey;
    }
}
