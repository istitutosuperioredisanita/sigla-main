package it.cnr.contab.doccont00.dto;

import java.math.BigDecimal;

public class ObligAgrrDTO {
    String cds;
    Integer esObb;
    Integer esOri;
    Long pgObb;
    BigDecimal imTotObbAggr;

    public ObligAgrrDTO(String cds, Integer esObb, Integer esOri, Long pgObb, BigDecimal amount) {
        this.cds = cds;
        this.esObb = esObb;
        this.esOri = esOri;
        this.pgObb = pgObb;
        this.imTotObbAggr = amount;
    }

    public String getCds() {
        return cds;
    }

    public void setCds(String cds) {
        this.cds = cds;
    }

    public Integer getEsObb() {
        return esObb;
    }

    public void setEsObb(Integer esObb) {
        this.esObb = esObb;
    }

    public Integer getEsOri() {
        return esOri;
    }

    public void setEsOri(Integer esOri) {
        this.esOri = esOri;
    }

    public Long getPgObb() {
        return pgObb;
    }

    public void setPgObb(Long pgObb) {
        this.pgObb = pgObb;
    }

    public BigDecimal getImTotObbAggr() {
        return imTotObbAggr;
    }

    public void setImTotObbAggr(BigDecimal imTotObbAggr) {
        this.imTotObbAggr = imTotObbAggr;
    }

    public static ObligAgrrDTO updateImpTot(ObligAgrrDTO existing, ObligAgrrDTO newItem) {
        existing.setImTotObbAggr(
                existing.getImTotObbAggr().add(newItem.getImTotObbAggr())
        );
        return existing;
    }

}