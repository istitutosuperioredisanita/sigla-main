package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class StampaInventarioDTO extends StampaInventarioDTOKey implements Serializable {

    private Timestamp dataMovimentoChiusuraLotto;
    private Timestamp dataCaricoLotto;
    private String desc_magazzino;
    private String descArticolo;
    private String descCatGrp;
    private String cod_categoria;
    private String cod_gruppo;
    private BigDecimal giacenza;
    private String um;
    private BigDecimal importoUnitario;
    private String cdMagRaggr;
    private String descMagRaggr;



    public Timestamp getDataMovimentoChiusuraLotto() {
        return dataMovimentoChiusuraLotto;
    }

    public void setDataMovimentoChiusuraLotto(Timestamp dataMovimentoChiusuraLotto) {
        this.dataMovimentoChiusuraLotto = dataMovimentoChiusuraLotto;
    }

    public Timestamp getDataCaricoLotto() {
        return dataCaricoLotto;
    }

    public void setDataCaricoLotto(Timestamp dataCaricoLotto) {
        this.dataCaricoLotto = dataCaricoLotto;
    }

    public String getDesc_magazzino() {
        return desc_magazzino;
    }

    public void setDesc_magazzino(String desc_magazzino) {
        this.desc_magazzino = desc_magazzino;
    }

    public String getDescArticolo() {
        return descArticolo;
    }

    public void setDescArticolo(String descArticolo) {
        this.descArticolo = descArticolo;
    }

    public String getDescCatGrp() {
        return descCatGrp;
    }

    public void setDescCatGrp(String descCatGrp) {
        this.descCatGrp = descCatGrp;
    }

    public String getCod_categoria() {
        return cod_categoria;
    }

    public void setCod_categoria(String cod_categoria) {
        this.cod_categoria = cod_categoria;
    }

    public String getCod_gruppo() {
        return cod_gruppo;
    }

    public void setCod_gruppo(String cod_gruppo) {
        this.cod_gruppo = cod_gruppo;
    }

    public BigDecimal getGiacenza() {
        if(this.giacenza==null)
            return new BigDecimal(0);
        return giacenza;
    }

    public void setGiacenza(BigDecimal giacenza) {
        this.giacenza = giacenza;
    }

    public String getUm() {
        return um;
    }

    public void setUm(String um) {
        this.um = um;
    }

    public BigDecimal getImportoUnitario() {
        return importoUnitario;
    }

    public void setImportoUnitario(BigDecimal importoUnitario) {
        this.importoUnitario = importoUnitario;
    }

    public String getCdMagRaggr() {
        return cdMagRaggr;
    }

    public void setCdMagRaggr(String cdMagRaggr) {
        this.cdMagRaggr = cdMagRaggr;
    }

    public String getDescMagRaggr() {
        return descMagRaggr;
    }

    public void setDescMagRaggr(String descMagRaggr) {
        this.descMagRaggr = descMagRaggr;
    }




}
