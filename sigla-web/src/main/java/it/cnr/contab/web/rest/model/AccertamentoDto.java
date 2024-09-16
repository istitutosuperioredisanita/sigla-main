package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.contratto.bulk.ContrattoKey;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceKey;
import it.cnr.contab.config00.sto.bulk.CdsKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;

import java.math.BigDecimal;
import java.util.List;

public class AccertamentoDto extends AccertamentoKeyDto {

    private Unita_organizzativaKey unitaOrganizzativaKey;
    private ContrattoKey contrattoKey;
    private TerzoKey terzoKey;
    private Elemento_voceKey elemento_voceKey;
    private String ds_accertamento;
    private BigDecimal im_accertamento;
    private CdsKey cdsOrgineKey;
    private CdsKey unitaOrganizzativaOrigineKey;

    //private LineaAttivitaDto accertamentoLineaAttivitaDto;
    private List<AccertamentoScadenzarioDto> scadenze;

    public Unita_organizzativaKey getUnitaOrganizzativaKey() {
        return unitaOrganizzativaKey;
    }

    public void setUnitaOrganizzativaKey(Unita_organizzativaKey unitaOrganizzativaKey) {
        this.unitaOrganizzativaKey = unitaOrganizzativaKey;
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

    public String getDs_accertamento() {
        return ds_accertamento;
    }

    public void setDs_accertamento(String ds_accertamento) {
        this.ds_accertamento = ds_accertamento;
    }

    public BigDecimal getIm_accertamento() {
        return im_accertamento;
    }

    public void setIm_accertamento(BigDecimal im_accertamento) {
        this.im_accertamento = im_accertamento;
    }

    public List<AccertamentoScadenzarioDto> getScadenze() {
        return scadenze;
    }

    public void setScadenze(List<AccertamentoScadenzarioDto> scadenze) {
        this.scadenze = scadenze;
    }

    public CdsKey getCdsOrgineKey() {
        return cdsOrgineKey;
    }

    public void setCdsOrgineKey(CdsKey cdsOrgineKey) {
        this.cdsOrgineKey = cdsOrgineKey;
    }

    public CdsKey getUnitaOrganizzativaOrigineKey() {
        return unitaOrganizzativaOrigineKey;
    }

    public void setUnitaOrganizzativaOrigineKey(CdsKey unitaOrganizzativaOrigineKey) {
        this.unitaOrganizzativaOrigineKey = unitaOrganizzativaOrigineKey;
    }
}
