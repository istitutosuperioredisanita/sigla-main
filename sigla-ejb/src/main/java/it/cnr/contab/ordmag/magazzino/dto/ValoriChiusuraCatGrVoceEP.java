package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ValoriChiusuraCatGrVoceEP implements Serializable{

    private String cdCategoriaGruppo;
    private String cdVoceEp;
    private Integer esercizio;
    private Integer pgChiusura;
    private String tipoChiusura;
    private BigDecimal impTotCatGrVoceEP;

    public String getCdCategoriaGruppo() {
        return cdCategoriaGruppo;
    }

    public void setCdCategoriaGruppo(String cdCategoriaGruppo) {
        this.cdCategoriaGruppo = cdCategoriaGruppo;
    }

    public String getCdVoceEp() {
        return cdVoceEp;
    }

    public void setCdVoceEp(String cdVoceEp) {
        this.cdVoceEp = cdVoceEp;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Integer getPgChiusura() {
        return pgChiusura;
    }

    public void setPgChiusura(Integer pgChiusura) {
        this.pgChiusura = pgChiusura;
    }

    public String getTipoChiusura() {
        return tipoChiusura;
    }

    public void setTipoChiusura(String tipoChiusura) {
        this.tipoChiusura = tipoChiusura;
    }

    public BigDecimal getImpTotCatGrVoceEP() {
        return impTotCatGrVoceEP;
    }

    public void setImpTotCatGrVoceEP(BigDecimal impTotCatGrVoceEP) {
        this.impTotCatGrVoceEP = impTotCatGrVoceEP;
    }
}
