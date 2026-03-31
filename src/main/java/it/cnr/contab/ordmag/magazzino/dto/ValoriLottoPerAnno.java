package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ValoriLottoPerAnno implements Serializable{

    private String cdCdsLotto;
    private String cdMagazzinoLotto;
    private Integer esercizioLotto;
    private String cdNumeratoreMagLotto;
    private Integer pgLotto;
    private String magazzinoDesc;
    private String cdsRaggrMag;
    private String cdRaggrMag;
    private String cdBeneServizio;
    private String beneServizioDesc;
    private String cdCatGruppo;
    private String cdCatPadre;
    private String cdGruppo;
    private String unitaMisura;
    private BigDecimal valoreUnitarioChi;
    private BigDecimal valoreUnitarioLotto;
    private BigDecimal giacenza;
    private BigDecimal qtyInizioAnno;
    private BigDecimal qtyCaricoAnno;
    private BigDecimal qtyScaricoAnno;
    private BigDecimal giacenzaCalcolata; // (inizio anno + carichi) - scarichi


    public String getCdCdsLotto() {
        return cdCdsLotto;
    }

    public void setCdCdsLotto(String cdCdsLotto) {
        this.cdCdsLotto = cdCdsLotto;
    }

    public String getCdMagazzinoLotto() {
        return cdMagazzinoLotto;
    }

    public void setCdMagazzinoLotto(String cdMagazzinoLotto) {
        this.cdMagazzinoLotto = cdMagazzinoLotto;
    }

    public Integer getEsercizioLotto() {
        return esercizioLotto;
    }

    public void setEsercizioLotto(Integer esercizioLotto) {
        this.esercizioLotto = esercizioLotto;
    }

    public String getCdNumeratoreMagLotto() {
        return cdNumeratoreMagLotto;
    }

    public void setCdNumeratoreMagLotto(String cdNumeratoreMagLotto) {
        this.cdNumeratoreMagLotto = cdNumeratoreMagLotto;
    }

    public Integer getPgLotto() {
        return pgLotto;
    }

    public void setPgLotto(Integer pgLotto) {
        this.pgLotto = pgLotto;
    }

    public String getMagazzinoDesc() {
        return magazzinoDesc;
    }

    public void setMagazzinoDesc(String magazzinoDesc) {
        this.magazzinoDesc = magazzinoDesc;
    }

    public String getCdBeneServizio() {
        return cdBeneServizio;
    }

    public void setCdBeneServizio(String cdBeneServizio) {
        this.cdBeneServizio = cdBeneServizio;
    }

    public String getCdCatPadre() {
        return cdCatPadre;
    }

    public void setCdCatPadre(String cdCatPadre) {
        this.cdCatPadre = cdCatPadre;
    }

    public String getCdGruppo() {
        return cdGruppo;
    }

    public void setCdGruppo(String cdGruppo) {
        this.cdGruppo = cdGruppo;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public BigDecimal getValoreUnitarioChi() {
        return valoreUnitarioChi;
    }

    public void setValoreUnitarioChi(BigDecimal valoreUnitarioChi) {
        this.valoreUnitarioChi = valoreUnitarioChi;
    }

    public BigDecimal getValoreUnitarioLotto() {
        return valoreUnitarioLotto;
    }

    public void setValoreUnitarioLotto(BigDecimal valoreUnitarioLotto) {
        this.valoreUnitarioLotto = valoreUnitarioLotto;
    }

    public BigDecimal getGiacenza() {
        return giacenza;
    }

    public void setGiacenza(BigDecimal giacenza) {
        this.giacenza = giacenza;
    }

    public BigDecimal getQtyInizioAnno() {
        return qtyInizioAnno;
    }

    public void setQtyInizioAnno(BigDecimal qtyInizioAnno) {
        this.qtyInizioAnno = qtyInizioAnno;
    }

    public BigDecimal getQtyCaricoAnno() {
        return qtyCaricoAnno;
    }

    public void setQtyCaricoAnno(BigDecimal qtyCaricoAnno) {
        this.qtyCaricoAnno = qtyCaricoAnno;
    }

    public BigDecimal getQtyScaricoAnno() {
        return qtyScaricoAnno;
    }

    public void setQtyScaricoAnno(BigDecimal qtyScaricoAnno) {
        this.qtyScaricoAnno = qtyScaricoAnno;
    }

    public BigDecimal getGiacenzaCalcolata() {
        return giacenzaCalcolata;
    }

    public void setGiacenzaCalcolata(BigDecimal giacenzaCalcolata) {
        this.giacenzaCalcolata = giacenzaCalcolata;
    }

    public String getBeneServizioDesc() {
        return beneServizioDesc;
    }

    public void setBeneServizioDesc(String beneServizioDesc) {
        this.beneServizioDesc = beneServizioDesc;
    }

    public String getCdCatGruppo() {
        return cdCatGruppo;
    }

    public void setCdCatGruppo(String cdCatGruppo) {
        this.cdCatGruppo = cdCatGruppo;
    }

    public String getCdsRaggrMag() {
        return cdsRaggrMag;
    }

    public void setCdsRaggrMag(String cdsRaggrMag) {
        this.cdsRaggrMag = cdsRaggrMag;
    }

    public String getCdRaggrMag() {
        return cdRaggrMag;
    }

    public void setCdRaggrMag(String cdRaggrMag) {
        this.cdRaggrMag = cdRaggrMag;
    }
}
