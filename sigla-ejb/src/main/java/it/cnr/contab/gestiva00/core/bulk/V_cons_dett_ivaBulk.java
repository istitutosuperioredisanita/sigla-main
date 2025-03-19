package it.cnr.contab.gestiva00.core.bulk;

import com.sun.org.apache.xpath.internal.operations.Bool;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.Optional;

public class V_cons_dett_ivaBulk extends OggettoBulk implements Persistent  {
    private Integer esercizio;
    private String mese;
    private String cd_tipo_sezionale;
    private BigDecimal sp_n;
    private BigDecimal sp_y;
    private BigDecimal tot_iva;

    // Riferimento a tipo_sezionale solo per la fase di ricerca
    private Tipo_sezionaleBulk tipo_sezionale;
    private java.util.Collection tipi_sezionali;

    static String AISPP = "a/ispp";
    static String ACOM_VCOM = "a/com - v/com";
    static String AIUE_AISNR = "a/iue - a/isnr";

    public final static Dictionary TIPI_SEZIONALI;

    static {
        TIPI_SEZIONALI = new it.cnr.jada.util.OrderedHashtable();
        TIPI_SEZIONALI.put(AISPP, AISPP);
        TIPI_SEZIONALI.put(ACOM_VCOM, ACOM_VCOM);
        TIPI_SEZIONALI.put(AIUE_AISNR, AIUE_AISNR);

    }


    // Costruttori
    public V_cons_dett_ivaBulk() {
        super();
    }

    // Getters e Setters
    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public String getMese() {
        return mese;
    }

    public void setMese(String mese) {
        this.mese = mese;
    }

    public String getCd_tipo_sezionale() {
        return cd_tipo_sezionale;
    }

    public void setCd_tipo_sezionale(String cd_tipo_sezionale) {
        this.cd_tipo_sezionale = cd_tipo_sezionale;
    }

    public BigDecimal getSp_n() {
        return sp_n;
    }

    public void setSp_n(BigDecimal sp_n) {
        this.sp_n = sp_n;
    }

    public BigDecimal getSp_y() {
        return sp_y;
    }

    public void setSp_y(BigDecimal sp_y) {
        this.sp_y = sp_y;
    }

    public BigDecimal getTot_iva() {
        return tot_iva;
    }

    public void setTot_iva(BigDecimal tot_iva) {
        this.tot_iva = tot_iva;
    }

    public Tipo_sezionaleBulk getTipo_sezionale() {
        return tipo_sezionale;
    }

    public void setTipo_sezionale(Tipo_sezionaleBulk tipo_sezionale) {
        this.tipo_sezionale = tipo_sezionale;
        if (tipo_sezionale != null) {
            this.cd_tipo_sezionale = tipo_sezionale.getCd_tipo_sezionale();
        }
    }

    public java.util.Collection getTipi_sezionali() {
        return tipi_sezionali;
    }

    public void setTipi_sezionali(java.util.Collection tipi_sezionali) {
        this.tipi_sezionali = tipi_sezionali;
    }

    public Dictionary getTipo_sezionaleKeys() {
        return TIPI_SEZIONALI;
    }

    public boolean isROCd_tipo_sezionale() {
        return Boolean.FALSE;
    }

    public boolean isTipoSezionaleAispp() {
        return Optional.ofNullable(getCd_tipo_sezionale())
                .map(s -> s.equalsIgnoreCase(AISPP))
                .orElse(Boolean.FALSE);
    }

    public boolean isTipoSezionaleAcomVcom() {
        return Optional.ofNullable(getCd_tipo_sezionale())
                .map(s -> s.equalsIgnoreCase(ACOM_VCOM))
                .orElse(Boolean.FALSE);
    }

    public boolean isTipoSezionaleAiueAisnr() {
        return Optional.ofNullable(getCd_tipo_sezionale())
                .map(s -> s.equalsIgnoreCase(AIUE_AISNR))
                .orElse(Boolean.FALSE);
    }
}