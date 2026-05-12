package it.cnr.contab.inventario00.dto;

import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class TipoAmmCatGruppoDto implements Serializable {


    private String cdCatGruppo;
    private String cdTipoAmm;
    private String tipoAmm;
    private String tipoAmmDesc;
    private BigDecimal perPrimoAnno;
    private BigDecimal perAnniSucc;
    private Integer numAnni;
    private java.sql.Timestamp dataCanc;

    public String getCdCatGruppo() {
        return cdCatGruppo;
    }

    public void setCdCatGruppo(String cdCatGruppo) {
        this.cdCatGruppo = cdCatGruppo;
    }

    public String getCdTipoAmm() {
        return cdTipoAmm;
    }

    public void setCdTipoAmm(String cdTipoAmm) {
        this.cdTipoAmm = cdTipoAmm;
    }

    public String getTipoAmm() {
        return tipoAmm;
    }

    public void setTipoAmm(String tipoAmm) {
        this.tipoAmm = tipoAmm;
    }

    public String getTipoAmmDesc() {
        return tipoAmmDesc;
    }

    public void setTipoAmmDesc(String tipoAmmDesc) {
        this.tipoAmmDesc = tipoAmmDesc;
    }

    public BigDecimal getPerPrimoAnno() {
        return perPrimoAnno;
    }

    public void setPerPrimoAnno(BigDecimal perPrimoAnno) {
        this.perPrimoAnno = perPrimoAnno;
    }

    public BigDecimal getPerAnniSucc() {
        return perAnniSucc;
    }

    public void setPerAnniSucc(BigDecimal perAnniSucc) {
        this.perAnniSucc = perAnniSucc;
    }

    public Integer getNumAnni() {
        return numAnni;
    }

    public void setNumAnni(Integer numAnni) {
        this.numAnni = numAnni;
    }

    public Timestamp getDataCanc() {
        return dataCanc;
    }

    public void setDataCanc(Timestamp dataCanc) {
        this.dataCanc = dataCanc;
    }
}
