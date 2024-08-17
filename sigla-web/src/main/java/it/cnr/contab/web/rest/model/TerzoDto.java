package it.cnr.contab.web.rest.model;

public class TerzoDto {
    private Integer cd_terzo;

    private String denominazione_sede;

    private String codiceFiscale;

    private String partita_iva;

    private String ragione_sociale;

    private String nome;

    private String cognome;

    public Integer getCd_terzo() {
        return cd_terzo;
    }

    public void setCd_terzo(Integer cd_terzo) {
        this.cd_terzo = cd_terzo;
    }

    public String getDenominazione_sede() {
        return denominazione_sede;
    }

    public void setDenominazione_sede(String denominazione_sede) {
        this.denominazione_sede = denominazione_sede;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getPartita_iva() {
        return partita_iva;
    }

    public void setPartita_iva(String partita_iva) {
        this.partita_iva = partita_iva;
    }

    public String getRagione_sociale() {
        return ragione_sociale;
    }

    public void setRagione_sociale(String ragione_sociale) {
        this.ragione_sociale = ragione_sociale;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
}
