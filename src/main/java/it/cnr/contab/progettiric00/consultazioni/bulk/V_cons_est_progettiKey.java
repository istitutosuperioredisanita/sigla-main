/*
 * Created by BulkGenerator 2.0
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
import java.util.Objects;

public class V_cons_est_progettiKey extends OggettoBulk implements KeyedPersistent {

    //    PG_PROGETTO DECIMAL(38,0)
    private java.lang.Long pgProgetto;

    //    ESERCIZIO DECIMAL(5,0)
    private java.lang.Integer esercizio;

    //    TIPO_FASE VARCHAR(1)
    private java.lang.String tipoFase;

    public V_cons_est_progettiKey() {
        super();
    }

    public V_cons_est_progettiKey(java.lang.Integer esercizio, java.lang.Long pgProgetto, java.lang.String tipoFase) {
        super();
        this.esercizio = esercizio;
        this.pgProgetto = pgProgetto;
        this.tipoFase = tipoFase;
    }

    public java.lang.Long getPgProgetto() {
        return pgProgetto;
    }

    public void setPgProgetto(java.lang.Long pgProgetto) {
        this.pgProgetto = pgProgetto;
    }

    public java.lang.Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(java.lang.Integer esercizio) {
        this.esercizio = esercizio;
    }

    public java.lang.String getTipoFase() {
        return tipoFase;
    }

    public void setTipoFase(java.lang.String tipoFase) {
        this.tipoFase = tipoFase;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this == o) return true;
        if (!(o instanceof V_cons_est_progettiKey)) return false;
        V_cons_est_progettiKey k = (V_cons_est_progettiKey) o;
        return Objects.equals(this.pgProgetto, k.pgProgetto)
                && Objects.equals(this.esercizio, k.esercizio)
                && Objects.equals(this.tipoFase, k.tipoFase);
    }

    public int primaryKeyHashCode() {
        return Objects.hash(pgProgetto, esercizio, tipoFase);
    }
}