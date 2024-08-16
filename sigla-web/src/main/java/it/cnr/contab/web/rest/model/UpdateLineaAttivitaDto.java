package it.cnr.contab.web.rest.model;

import java.io.Serializable;

public class UpdateLineaAttivitaDto implements Serializable {

    private Integer esercizio_fine;

    private String denominazione;
    private String ds_linea_attivita;

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


}
