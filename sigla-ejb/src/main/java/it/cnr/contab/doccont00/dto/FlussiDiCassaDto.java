package it.cnr.contab.doccont00.dto;

import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.jada.util.OrderedHashtable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Dictionary;

public class FlussiDiCassaDto  implements Serializable  {

    private final static Dictionary tipo_EstrazioneKeys;
    public final static String MANDATI = "MAN";
    public final static String REVERSALI = "REV";

    private String cdCds;
    protected CdsBulk cds = new CdsBulk();
    private Integer esercizio;

    private Timestamp dtEmissioneDa;
    private Timestamp dtEmissioneA;
    private String tipoPagamento;

    static {
        tipo_EstrazioneKeys = new OrderedHashtable();
        tipo_EstrazioneKeys.put(MANDATI, "Mandati");
        tipo_EstrazioneKeys.put(REVERSALI, "Reversali");
    }


    private Integer idClassificazione;
    private String dsClassificazione;

    private BigDecimal impPrimoTrimestre;
    private BigDecimal impSecondoTrimestre;
    private BigDecimal impTerzoTrimestre;
    private BigDecimal impQuartoTrimestre;


    public String getCdCds() {
        return cdCds;
    }

    public void setCdCds(String cdCds) {
        this.cdCds = cdCds;
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

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Integer getIdClassificazione() {
        return idClassificazione;
    }

    public void setIdClassificazione(Integer idClassificazione) {
        this.idClassificazione = idClassificazione;
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
}
