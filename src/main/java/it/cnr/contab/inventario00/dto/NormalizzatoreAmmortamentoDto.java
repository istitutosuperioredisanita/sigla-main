package it.cnr.contab.inventario00.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class NormalizzatoreAmmortamentoDto implements Serializable {

    private Integer esercizioCaricoBene;
    private Integer pgInventario;
    private Integer nrInventario;
    private Integer progressivo;
    private String etichetta;
    private String cdCatGruppo;

    // valore_iniziale + variazioni_piu - variazioni_meno
    private BigDecimal valoreBene;
    // somma degli incrementi successivi alla fine dell'esercizio di contesto
    private BigDecimal incrementiSuccessivi;
    // somma decrementi successivi alla fine dell'esercizio di contesto
    private BigDecimal decrementiSuccessivi;
    // somma di im_movimento_ammort di AMMORTAMENTO_BENE_INV - quota ammortizzata per ogni
    // esercizio precendete a quello di contesto
    private BigDecimal quotaAmmortamentiPrecedenti;
    // valore ammortizzato sul bene
    private BigDecimal valoreAmmortizzato;
    // imponibile ammortamento del bene (tiene conto delle variazioni)
    private BigDecimal imponibileAmmortamento;
    // somma delle quote degli storni successivi alla fine dell'esercizio di contesto
    private BigDecimal quotaStorniSuccessivi;



    public Integer getEsercizioCaricoBene() {
        return esercizioCaricoBene;
    }

    public void setEsercizioCaricoBene(Integer esercizioCaricoBene) {
        this.esercizioCaricoBene = esercizioCaricoBene;
    }

    public Integer getPgInventario() {
        return pgInventario;
    }

    public void setPgInventario(Integer pgInventario) {
        this.pgInventario = pgInventario;
    }

    public Integer getNrInventario() {
        return nrInventario;
    }

    public void setNrInventario(Integer nrInventario) {
        this.nrInventario = nrInventario;
    }

    public Integer getProgressivo() {
        return progressivo;
    }

    public void setProgressivo(Integer progressivo) {
        this.progressivo = progressivo;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public void setEtichetta(String etichetta) {
        this.etichetta = etichetta;
    }

    public String getCdCatGruppo() {
        return cdCatGruppo;
    }

    public void setCdCatGruppo(String cdCatGruppo) {
        this.cdCatGruppo = cdCatGruppo;
    }

    public BigDecimal getValoreBene() {
        return valoreBene;
    }

    public void setValoreBene(BigDecimal valoreBene) {
        this.valoreBene = valoreBene;
    }

    public BigDecimal getIncrementiSuccessivi() {
        return incrementiSuccessivi;
    }

    public void setIncrementiSuccessivi(BigDecimal incrementiSuccessivi) {
        this.incrementiSuccessivi = incrementiSuccessivi;
    }

    public BigDecimal getDecrementiSuccessivi() {
        return decrementiSuccessivi;
    }

    public void setDecrementiSuccessivi(BigDecimal decrementiSuccessivi) {
        this.decrementiSuccessivi = decrementiSuccessivi;
    }

    public BigDecimal getQuotaAmmortamentiPrecedenti() {
        return quotaAmmortamentiPrecedenti;
    }

    public void setQuotaAmmortamentiPrecedenti(BigDecimal quotaAmmortamentiPrecedenti) {
        this.quotaAmmortamentiPrecedenti = quotaAmmortamentiPrecedenti;
    }

    public BigDecimal getValoreAmmortizzato() {
        return valoreAmmortizzato;
    }

    public void setValoreAmmortizzato(BigDecimal valoreAmmortizzato) {
        this.valoreAmmortizzato = valoreAmmortizzato;
    }

    public BigDecimal getImponibileAmmortamento() {
        return imponibileAmmortamento;
    }

    public void setImponibileAmmortamento(BigDecimal imponibileAmmortamento) {
        this.imponibileAmmortamento = imponibileAmmortamento;
    }

    public BigDecimal getQuotaStorniSuccessivi() {
        return quotaStorniSuccessivi;
    }

    public void setQuotaStorniSuccessivi(BigDecimal quotaStorniSuccessivi) {
        this.quotaStorniSuccessivi = quotaStorniSuccessivi;
    }
}
