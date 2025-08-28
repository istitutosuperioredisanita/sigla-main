package it.cnr.contab.doccont00.dto;

import java.math.BigDecimal;

public class ObligAgrrDTO {
    String cds;
    Integer esObb;
    Integer esOri;
    Long pgObb;
    BigDecimal totalAmount;

    public ObligAgrrDTO(String cds, Integer esObb, Integer esOri, Long pgObb, BigDecimal amount) {
        this.cds = cds;
        this.esObb = esObb;
        this.esOri = esOri;
        this.pgObb = pgObb;
        this.totalAmount = amount;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static ObligAgrrDTO merge(ObligAgrrDTO existing, ObligAgrrDTO newItem) {
        existing.setTotalAmount(
                existing.getTotalAmount().add(newItem.getTotalAmount())
        );
        return existing;
    }

}