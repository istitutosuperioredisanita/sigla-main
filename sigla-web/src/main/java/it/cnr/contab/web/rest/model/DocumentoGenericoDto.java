package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.sql.Timestamp;

abstract  public class DocumentoGenericoDto extends DocumentoGenericoKeyDto implements Serializable {
    private java.sql.Timestamp data_registrazione;
    private java.lang.String ds_documento_generico;
    private java.sql.Timestamp dt_a_competenza_coge;
    private java.sql.Timestamp dt_da_competenza_coge;
    private java.sql.Timestamp dt_scadenza;
    private java.sql.Timestamp dt_cancellazione;

    //stato
    //Associzione man/rev
    //stato Liquidazione
    //causale
    //tipo documento ( Ist/Comm)


    public Timestamp getData_registrazione() {
        return data_registrazione;
    }

    public void setData_registrazione(Timestamp data_registrazione) {
        this.data_registrazione = data_registrazione;
    }
}
