package it.cnr.contab.web.rest.model;

public class ProgettoDto {
    public ProgettoDto(){
        super();
    }
    public ProgettoDto(Integer esercizio, Integer pg_progetto) {
        this.esercizio = esercizio;
        this.pg_progetto = pg_progetto;
    }

    private Integer esercizio;

    private Integer pg_progetto;

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Integer getPg_progetto() {
        return pg_progetto;
    }

    public void setPg_progetto(Integer pg_progetto) {
        this.pg_progetto = pg_progetto;
    }
}
