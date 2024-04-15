package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class StampaInventarioDTOKey implements Serializable {

    Integer annoLotto;
    Integer numeroLotto;
    String tipoLotto;
    String cd_magazzino;
    String cdCds;
    String categoriaGruppo;
    String cod_articolo;


    public StampaInventarioDTOKey( String cdCds,Integer annoLotto, Integer numeroLotto, String cd_magazzino, String categoriaGruppo, String cod_articolo,String tipoLotto ) {
        this.annoLotto = annoLotto;
        this.numeroLotto = numeroLotto;
        this.tipoLotto = tipoLotto;
        this.cd_magazzino = cd_magazzino;
        this.cdCds = cdCds;
        this.categoriaGruppo = categoriaGruppo;
        this.cod_articolo = cod_articolo;
    }
    public StampaInventarioDTOKey() {
        super();
    }


    public Integer getAnnoLotto() {
        return annoLotto;
    }

    public void setAnnoLotto(Integer annoLotto) {
        this.annoLotto = annoLotto;
    }

    public Integer getNumeroLotto() {
        return numeroLotto;
    }

    public void setNumeroLotto(Integer numeroLotto) {
        this.numeroLotto = numeroLotto;
    }

    public String getTipoLotto() {
        return tipoLotto;
    }

    public void setTipoLotto(String tipoLotto) {
        this.tipoLotto = tipoLotto;
    }

    public String getCd_magazzino() {
        return cd_magazzino;
    }

    public void setCd_magazzino(String cd_magazzino) {
        this.cd_magazzino = cd_magazzino;
    }

    public String getCdCds() {
        return cdCds;
    }

    public void setCdCds(String cdCds) {
        this.cdCds = cdCds;
    }

    public String getCategoriaGruppo() {
        return categoriaGruppo;
    }

    public void setCategoriaGruppo(String categoriaGruppo) {
        this.categoriaGruppo = categoriaGruppo;
    }

    public String getCod_articolo() {
        return cod_articolo;
    }

    public void setCod_articolo(String cod_articolo) {
        this.cod_articolo = cod_articolo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StampaInventarioDTOKey that = (StampaInventarioDTOKey) o;
        return Objects.equals(annoLotto, that.annoLotto) && Objects.equals(numeroLotto, that.numeroLotto) && Objects.equals(tipoLotto, that.tipoLotto) && Objects.equals(cd_magazzino, that.cd_magazzino) && Objects.equals(cdCds, that.cdCds) && Objects.equals(categoriaGruppo, that.categoriaGruppo) && Objects.equals(cod_articolo, that.cod_articolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annoLotto, numeroLotto, tipoLotto, cd_magazzino, cdCds, categoriaGruppo, cod_articolo);
    }
}
