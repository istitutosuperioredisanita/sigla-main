package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.latt.bulk.CofogKey;
import it.cnr.contab.config00.latt.bulk.Gruppo_linea_attivitaKey;
import it.cnr.contab.config00.latt.bulk.Insieme_laKey;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneKey;
import it.cnr.contab.config00.pdcfin.bulk.NaturaKey;
import it.cnr.contab.prevent01.bulk.Pdg_missioneKey;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class LineaAttivitaDto extends LineaAttivitaKeyDto implements Serializable {
    @NotNull
    private Integer esercizio_inizio;
    private Integer esercizio_fine;
    private String denominazione;
    private String ds_linea_attivita;
    private EnumTiGestioneLineaAttivita ti_gestione;
    private Gruppo_linea_attivitaKey gruppoLineaAttivitaKey;
    private TerzoKey responsabileKey;
    private FunzioneKey funzioneKey;
    private NaturaKey naturaKey;

    private ProgettoDto progettoKey;
    private Insieme_laKey insieme_laKey;
    private CofogKey cofogKey;
    private Pdg_missioneKey pdgMissioneKey;
    public Integer getEsercizio_inizio() {
        return esercizio_inizio;
    }

    public void setEsercizio_inizio(Integer esercizio_inizio) {
        this.esercizio_inizio = esercizio_inizio;
    }

    public Integer getEsercizio_fine() {
        return esercizio_fine;
    }

    public void setEsercizio_fine(Integer esercizio_fine) {
        this.esercizio_fine = esercizio_fine;
    }


    public String getDenominazione() {
        return denominazione;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    public String getDs_linea_attivita() {
        return ds_linea_attivita;
    }

    public void setDs_linea_attivita(String ds_linea_attivita) {
        this.ds_linea_attivita = ds_linea_attivita;
    }

    public EnumTiGestioneLineaAttivita getTi_gestione() {
        return ti_gestione;
    }

    public void setTi_gestione(EnumTiGestioneLineaAttivita ti_gestione) {
        this.ti_gestione = ti_gestione;
    }



    public Gruppo_linea_attivitaKey getGruppoLineaAttivitaKey() {
        return gruppoLineaAttivitaKey;
    }

    public void setGruppoLineaAttivitaKey(Gruppo_linea_attivitaKey gruppoLineaAttivitaKey) {
        this.gruppoLineaAttivitaKey = gruppoLineaAttivitaKey;
    }

    public TerzoKey getResponsabileKey() {
        return responsabileKey;
    }

    public void setResponsabileKey(TerzoKey responsabileKey) {
        this.responsabileKey = responsabileKey;
    }

    public FunzioneKey getFunzioneKey() {
        return funzioneKey;
    }

    public void setFunzioneKey(FunzioneKey funzioneKey) {
        this.funzioneKey = funzioneKey;
    }

    public NaturaKey getNaturaKey() {
        return naturaKey;
    }

    public void setNaturaKey(NaturaKey naturaKey) {
        this.naturaKey = naturaKey;
    }


    public ProgettoDto getProgettoKey() {
        return progettoKey;
    }

    public void setProgettoKey(ProgettoDto progettoKey) {
        this.progettoKey = progettoKey;
    }

    public Insieme_laKey getInsieme_laKey() {
        return insieme_laKey;
    }

    public void setInsieme_laKey(Insieme_laKey insieme_laKey) {
        this.insieme_laKey = insieme_laKey;
    }

    public CofogKey getCofogKey() {
        return cofogKey;
    }

    public void setCofogKey(CofogKey cofogKey) {
        this.cofogKey = cofogKey;
    }

    public Pdg_missioneKey getPdgMissioneKey() {
        return pdgMissioneKey;
    }

    public void setPdgMissioneKey(Pdg_missioneKey pdgMissioneKey) {
        this.pdgMissioneKey = pdgMissioneKey;
    }
}
