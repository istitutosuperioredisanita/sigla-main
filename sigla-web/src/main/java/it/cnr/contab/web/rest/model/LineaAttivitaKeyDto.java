package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.CdrKey;

import java.io.Serializable;

public class LineaAttivitaKeyDto implements Serializable {

    private String cd_linea_attivita;

    private CdrKey centro_responsabilitaKey;
    public LineaAttivitaKeyDto(){
        super();
    }
    public LineaAttivitaKeyDto(String cd_linea_attivita, String centro_responsabilitaKey) {
        this.cd_linea_attivita = cd_linea_attivita;
        this.centro_responsabilitaKey = new CdrKey(centro_responsabilitaKey);
    }

    public String getCd_linea_attivita() {
        return cd_linea_attivita;
    }

    public void setCd_linea_attivita(String cd_linea_attivita) {
        this.cd_linea_attivita = cd_linea_attivita;
    }

    public CdrKey getCentro_responsabilitaKey() {
        return centro_responsabilitaKey;
    }

    public void setCentro_responsabilitaKey(CdrKey centro_responsabilitaKey) {
        this.centro_responsabilitaKey = centro_responsabilitaKey;
    }
}
