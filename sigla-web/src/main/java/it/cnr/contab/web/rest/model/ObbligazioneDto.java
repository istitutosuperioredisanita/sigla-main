package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.contratto.bulk.ContrattoKey;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;

import java.math.BigDecimal;
import java.util.List;

public class ObbligazioneDto extends ObbligazioneKeyDto {

    private Unita_organizzativaKey unitaOrganizzativaKey;
    private Boolean fl_gara_in_corso;
    private ContrattoKey contrattoKey;
    private TerzoKey terzoKey;
    private Elemento_voceKey elemento_voceKey;
    private String ds_obbligazione;
    private BigDecimal im_obbligazione;
    private EnumStatoObbligazione statoObbligazione;

   //private ScadenzarioVoceLineaAttivitaDto obbligazioneLineaAttivitaDto;
    private List<ObbligazioneScadenzarioDto> scadenze;



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

    public TerzoKey getTerzoKey() {
        return terzoKey;
    }

    public void setTerzoKey(TerzoKey terzoKey) {
        this.terzoKey = terzoKey;
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

    /*public LineaAttivitaDto getObbligazioneLineaAttivitaDto() {
        return obbligazioneLineaAttivitaDto;
    }*/

    public EnumStatoObbligazione getStatoObbligazione() {
        return statoObbligazione;
    }

    public void setStatoObbligazione(EnumStatoObbligazione statoObbligazione) {
        this.statoObbligazione = statoObbligazione;
    }

    /*
    public void setObbligazioneLineaAttivitaDto(LineaAttivitaDto obbligazioneLineaAttivitaDto) {
        this.obbligazioneLineaAttivitaDto = obbligazioneLineaAttivitaDto;
    }*/

    public List<ObbligazioneScadenzarioDto> getScadenze() {
        return scadenze;
    }

    public void setScadenze(List<ObbligazioneScadenzarioDto> scadenze) {
        this.scadenze = scadenze;
    }
}
