package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.contratto.bulk.ContrattoKey;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceKey;
import it.cnr.contab.config00.sto.bulk.CdsKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;

import java.math.BigDecimal;
import java.util.List;

public class ObbligazioneDto {

    private Integer esercizio;
    private CdsKey cdsKey;

    private Unita_organizzativaKey unitaOrganizzativaKey;
    private Boolean fl_gara_in_corso;
    private ContrattoKey contrattoKey;
    private TerzoDto terzoDto;
    private Elemento_voceKey elemento_voceKey;
    private String ds_obbligazione;
    private BigDecimal im_obbligazione;

    private ObbligazioneLineaAttivitaDto obbligazioneLineaAttivitaDto;
    private List<ObbligazioneScadenzarioDto> scadenze;

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }


    public CdsKey getCdsKey() {
        return cdsKey;
    }

    public void setCdsKey(CdsKey cdsKey) {
        this.cdsKey = cdsKey;
    }

    public Unita_organizzativaKey getUnitaOrganizzativaKey() {
        return unitaOrganizzativaKey;
    }

    public void setUnitaOrganizzativaKey(Unita_organizzativaKey unitaOrganizzativaKey) {
        this.unitaOrganizzativaKey = unitaOrganizzativaKey;
    }

    public Boolean getFl_gara_in_corso() {
        return fl_gara_in_corso;
    }

    public void setFl_gara_in_corso(Boolean fl_gara_in_corso) {
        this.fl_gara_in_corso = fl_gara_in_corso;
    }

    public ContrattoKey getContrattoKey() {
        return contrattoKey;
    }

    public void setContrattoKey(ContrattoKey contrattoKey) {
        this.contrattoKey = contrattoKey;
    }

    public TerzoDto getTerzoDto() {
        return terzoDto;
    }

    public void setTerzoDto(TerzoDto terzoDto) {
        this.terzoDto = terzoDto;
    }

    public Elemento_voceKey getElemento_voceKey() {
        return elemento_voceKey;
    }

    public void setElemento_voceKey(Elemento_voceKey elemento_voceKey) {
        this.elemento_voceKey = elemento_voceKey;
    }

    public String getDs_obbligazione() {
        return ds_obbligazione;
    }

    public void setDs_obbligazione(String ds_obbligazione) {
        this.ds_obbligazione = ds_obbligazione;
    }

    public BigDecimal getIm_obbligazione() {
        return im_obbligazione;
    }

    public void setIm_obbligazione(BigDecimal im_obbligazione) {
        this.im_obbligazione = im_obbligazione;
    }

    public ObbligazioneLineaAttivitaDto getObbligazioneLineaAttivitaDto() {
        return obbligazioneLineaAttivitaDto;
    }

    public void setObbligazioneLineaAttivitaDto(ObbligazioneLineaAttivitaDto obbligazioneLineaAttivitaDto) {
        this.obbligazioneLineaAttivitaDto = obbligazioneLineaAttivitaDto;
    }

    public List<ObbligazioneScadenzarioDto> getScadenze() {
        return scadenze;
    }

    public void setScadenze(List<ObbligazioneScadenzarioDto> scadenze) {
        this.scadenze = scadenze;
    }
}
