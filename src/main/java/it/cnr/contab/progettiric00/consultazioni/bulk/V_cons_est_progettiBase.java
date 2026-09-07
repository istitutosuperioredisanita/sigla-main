/*
 * Created by BulkGenerator 2.0
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;

import it.cnr.jada.persistency.Keyed;

public class V_cons_est_progettiBase extends V_cons_est_progettiKey implements Keyed {

    private java.lang.String cdProgetto;
    private java.lang.String areaProgettuale;
    private java.lang.String cdUnitaOrganizzativa;
    private java.lang.String dsProgetto;
    private java.math.BigDecimal imFinanziato;
    private java.math.BigDecimal imCofinanziato;
    private java.sql.Timestamp dtInizio;
    private java.sql.Timestamp dtFine;
    private java.sql.Timestamp dtProroga;
    private java.lang.String stato;
    private java.lang.Long livello;
    private java.lang.Long cdResponsabileTerzo;
    private java.lang.String responsabileScientifico;
    private java.lang.String enteFinanziatore;
    private java.lang.String missione;
    private java.lang.String tipoProgetto;
    private java.lang.String tipoFinanziamento;
    private java.lang.String note;
    private java.math.BigDecimal quotaAssegnata;
    private java.math.BigDecimal quotaStanziata;
    private java.math.BigDecimal quotaUtilizzata;
    private java.math.BigDecimal quotaPagata;
    private java.lang.Integer esercizioPiano;

    public V_cons_est_progettiBase() {
        super();
    }

    public V_cons_est_progettiBase(java.lang.Integer esercizio, java.lang.Long pgProgetto, java.lang.String tipoFase) {
        super(esercizio, pgProgetto, tipoFase);
    }

    public java.lang.String getCdProgetto() { return cdProgetto; }
    public void setCdProgetto(java.lang.String cdProgetto) { this.cdProgetto = cdProgetto; }

    public java.lang.String getAreaProgettuale() { return areaProgettuale; }
    public void setAreaProgettuale(java.lang.String areaProgettuale) { this.areaProgettuale = areaProgettuale; }

    public java.lang.String getCdUnitaOrganizzativa() { return cdUnitaOrganizzativa; }
    public void setCdUnitaOrganizzativa(java.lang.String cdUnitaOrganizzativa) { this.cdUnitaOrganizzativa = cdUnitaOrganizzativa; }

    public java.lang.String getDsProgetto() { return dsProgetto; }
    public void setDsProgetto(java.lang.String dsProgetto) { this.dsProgetto = dsProgetto; }

    public java.math.BigDecimal getImFinanziato() { return imFinanziato; }
    public void setImFinanziato(java.math.BigDecimal imFinanziato) { this.imFinanziato = imFinanziato; }

    public java.math.BigDecimal getImCofinanziato() { return imCofinanziato; }
    public void setImCofinanziato(java.math.BigDecimal imCofinanziato) { this.imCofinanziato = imCofinanziato; }

    public java.sql.Timestamp getDtInizio() { return dtInizio; }
    public void setDtInizio(java.sql.Timestamp dtInizio) { this.dtInizio = dtInizio; }

    public java.sql.Timestamp getDtFine() { return dtFine; }
    public void setDtFine(java.sql.Timestamp dtFine) { this.dtFine = dtFine; }

    public java.sql.Timestamp getDtProroga() { return dtProroga; }
    public void setDtProroga(java.sql.Timestamp dtProroga) { this.dtProroga = dtProroga; }

    public java.lang.String getStato() { return stato; }
    public void setStato(java.lang.String stato) { this.stato = stato; }

    public java.lang.Long getLivello() { return livello; }
    public void setLivello(java.lang.Long livello) { this.livello = livello; }

    public java.lang.Long getCdResponsabileTerzo() { return cdResponsabileTerzo; }
    public void setCdResponsabileTerzo(java.lang.Long cdResponsabileTerzo) { this.cdResponsabileTerzo = cdResponsabileTerzo; }

    public java.lang.String getResponsabileScientifico() { return responsabileScientifico; }
    public void setResponsabileScientifico(java.lang.String responsabileScientifico) { this.responsabileScientifico = responsabileScientifico; }

    public java.lang.String getEnteFinanziatore() { return enteFinanziatore; }
    public void setEnteFinanziatore(java.lang.String enteFinanziatore) { this.enteFinanziatore = enteFinanziatore; }

    public java.lang.String getMissione() { return missione; }
    public void setMissione(java.lang.String missione) { this.missione = missione; }

    public java.lang.String getTipoProgetto() { return tipoProgetto; }
    public void setTipoProgetto(java.lang.String tipoProgetto) { this.tipoProgetto = tipoProgetto; }

    public java.lang.String getTipoFinanziamento() { return tipoFinanziamento; }
    public void setTipoFinanziamento(java.lang.String tipoFinanziamento) { this.tipoFinanziamento = tipoFinanziamento; }

    public java.lang.String getNote() { return note; }
    public void setNote(java.lang.String note) { this.note = note; }

    public java.math.BigDecimal getQuotaAssegnata() { return quotaAssegnata; }
    public void setQuotaAssegnata(java.math.BigDecimal quotaAssegnata) { this.quotaAssegnata = quotaAssegnata; }

    public java.math.BigDecimal getQuotaStanziata() { return quotaStanziata; }
    public void setQuotaStanziata(java.math.BigDecimal quotaStanziata) { this.quotaStanziata = quotaStanziata; }

    public java.math.BigDecimal getQuotaUtilizzata() { return quotaUtilizzata; }
    public void setQuotaUtilizzata(java.math.BigDecimal quotaUtilizzata) { this.quotaUtilizzata = quotaUtilizzata; }

    public java.math.BigDecimal getQuotaPagata() { return quotaPagata; }
    public void setQuotaPagata(java.math.BigDecimal quotaPagata) { this.quotaPagata = quotaPagata; }

    public Integer getEsercizioPiano() {return esercizioPiano;}

    public void setEsercizioPiano(Integer esercizioPiano) {this.esercizioPiano = esercizioPiano;}
}