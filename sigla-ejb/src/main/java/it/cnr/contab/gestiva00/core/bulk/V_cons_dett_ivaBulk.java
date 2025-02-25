package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;

public class V_cons_dett_ivaBulk extends OggettoBulk implements Persistent {
    private Integer esercizio;
    private String mese;
    private String cd_tipo_sezionale;
    private String ds_tipo_sezionale;
    private BigDecimal sp_n;
    private BigDecimal sp_y;
    private BigDecimal tot_iva;

    private Tipo_sezionaleBulk tipo_sezionale;
    private java.util.Collection tipi_sezionali;

    // Costruttori
    public V_cons_dett_ivaBulk() {
        super();
    }

    // Getters e Setters
    public Integer getEsercizio() { return esercizio; }
    public void setEsercizio(Integer esercizio) { this.esercizio = esercizio; }

    public String getMese() { return mese; }
    public void setMese(String mese) { this.mese = mese; }

    public BigDecimal getSp_n() { return sp_n; }
    public void setSp_n(BigDecimal sp_n) { this.sp_n = sp_n; }

    public BigDecimal getSp_y() { return sp_y; }
    public void setSp_y(BigDecimal sp_y) { this.sp_y = sp_y; }

    public BigDecimal getTot_iva() { return tot_iva; }
    public void setTot_iva(BigDecimal tot_iva) { this.tot_iva = tot_iva; }

    public Tipo_sezionaleBulk getTipo_sezionale() {
        return tipo_sezionale;
    }

    public void setTipo_sezionale(Tipo_sezionaleBulk tipo_sezionale) {
        this.tipo_sezionale = tipo_sezionale;
    }

    public java.util.Collection getTipi_sezionali() {
        return tipi_sezionali;
    }

    public void setTipi_sezionali(java.util.Collection tipi_sezionali) {
        this.tipi_sezionali = tipi_sezionali;
    }

//    public String getCd_ds_tipo_sezionale() {
//        return tipo_sezionale == null ? null : tipo_sezionale.getCd_tipo_sezionale() + " - " + tipo_sezionale.getDs_tipo_sezionale();
//    }
//
//    public String getCd_tipo_sezionale() {
//        return tipo_sezionale == null ? null : tipo_sezionale.getCd_tipo_sezionale();
//    }
//
//    public void setCd_tipo_sezionale(String cd_tipo_sezionale) {
//        this.tipo_sezionale.setCd_tipo_sezionale(cd_tipo_sezionale);
//    }


    public String getCd_tipo_sezionale() {
        return tipo_sezionale == null ? null : tipo_sezionale.getCd_tipo_sezionale();
    }

    public void setCd_tipo_sezionale(String cd_tipo_sezionale) {
        this.cd_tipo_sezionale = cd_tipo_sezionale;
    }

    public String getDs_tipo_sezionale() {
        return tipo_sezionale == null ? null : tipo_sezionale.getCd_tipo_sezionale() + " - " + tipo_sezionale.getDs_tipo_sezionale();
    }

    public void setDs_tipo_sezionale(String ds_tipo_sezionale) {
        this.ds_tipo_sezionale = ds_tipo_sezionale;
    }


//    public CompoundFindClause buildFindClauses(Boolean filter) {
//        CompoundFindClause clause = new CompoundFindClause();
//
//        if (getEsercizio() != null) {
//            clause.addClause("AND", "esercizio", SQLBuilder.EQUALS, getEsercizio());
//        }
//
//        if (tipo_sezionale != null && tipo_sezionale.getCd_tipo_sezionale() != null) {
//            clause.addClause("AND", "cd_tipo_sezionale", SQLBuilder.EQUALS, tipo_sezionale.getCd_tipo_sezionale());
//        }
//
//        if (getMese() != null) {
//            clause.addClause("AND", "mese", SQLBuilder.EQUALS, getMese());
//        }
//
//        return clause;
//    }
}