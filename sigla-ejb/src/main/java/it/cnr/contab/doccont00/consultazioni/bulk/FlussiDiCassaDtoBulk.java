package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.OrderedHashtable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Dictionary;


public class FlussiDiCassaDtoBulk extends OggettoBulk {

    private final static Dictionary tipo_EstrazioneKeys;
    public final static String MANDATI = "MAN";
    public final static String REVERSALI = "REV";

    private String cdCds;
    protected CdsBulk cds = new CdsBulk();
    private Integer esercizio;

    private Timestamp dtEmissioneDa;
    private Timestamp dtEmissioneA;
    private String tipoFlusso;

    static {
        tipo_EstrazioneKeys = new OrderedHashtable();
        tipo_EstrazioneKeys.put(MANDATI, "Mandati");
        tipo_EstrazioneKeys.put(REVERSALI, "Reversali");
    }
    private boolean roFindCds;

    private String classificazione;
    private String dsClassificazione;

    private BigDecimal impPrimoTrimestre;
    private BigDecimal impSecondoTrimestre;
    private BigDecimal impTerzoTrimestre;
    private BigDecimal impQuartoTrimestre;


    public FlussiDiCassaDtoBulk() {
        cds=new CdsBulk();
    }


    public String getCdCds() {
        return this.cds.getCd_unita_organizzativa();
    }

    public void setCdCds(String cdCds) {
        this.cds.setCd_unita_organizzativa(cdCds);
    }

    public CdsBulk getCds() {
        return cds;
    }

    public void setCds(CdsBulk cds) {
        this.cds = cds;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Timestamp getDtEmissioneDa() {
        return dtEmissioneDa;
    }

    public void setDtEmissioneDa(Timestamp dtEmissioneDa) {
        this.dtEmissioneDa = dtEmissioneDa;
    }

    public Timestamp getDtEmissioneA() {
        return dtEmissioneA;
    }

    public void setDtEmissioneA(Timestamp dtEmissioneA) {
        this.dtEmissioneA = dtEmissioneA;
    }

    public String getTipoFlusso() {
        return tipoFlusso;
    }

    public void setTipoFlusso(String tipoFlusso) {
        this.tipoFlusso = tipoFlusso;
    }

    public String getClassificazione() {
        return classificazione;
    }

    public void setClassificazione(String classificazione) {
        this.classificazione = classificazione;
    }

    public String getDsClassificazione() {
        return dsClassificazione;
    }

    public void setDsClassificazione(String dsClassificazione) {
        this.dsClassificazione = dsClassificazione;
    }

    public BigDecimal getImpPrimoTrimestre() {
        return impPrimoTrimestre;
    }

    public void setImpPrimoTrimestre(BigDecimal impPrimoTrimestre) {
        this.impPrimoTrimestre = impPrimoTrimestre;
    }

    public BigDecimal getImpSecondoTrimestre() {
        return impSecondoTrimestre;
    }

    public void setImpSecondoTrimestre(BigDecimal impSecondoTrimestre) {
        this.impSecondoTrimestre = impSecondoTrimestre;
    }

    public BigDecimal getImpTerzoTrimestre() {
        return impTerzoTrimestre;
    }

    public void setImpTerzoTrimestre(BigDecimal impTerzoTrimestre) {
        this.impTerzoTrimestre = impTerzoTrimestre;
    }

    public BigDecimal getImpQuartoTrimestre() {
        return impQuartoTrimestre;
    }

    public void setImpQuartoTrimestre(BigDecimal impQuartoTrimestre) {
        this.impQuartoTrimestre = impQuartoTrimestre;
    }

    public boolean isROFindCds() {
        return roFindCds;
    }

    public void setROFindCds(boolean roFindCds) {
        this.roFindCds = roFindCds;
    }
}
