package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class DocTrasportoRientroDettDTOBulk implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Campi DOC_TRASPORTO_RIENTRO_DETT ---
    private Long pgInventario;
    private String tiDocumento;
    private Integer esercizio;
    private Long pgDocTrasportoRientro;
    private Long nrInventario;
    private Integer progressivo;
    private Long cdTerzoAssegnatario;

    private Long pgInventarioRif;
    private String tiDocumentoRif;
    private Integer esercizioRif;
    private Long pgDocTrasportoRientroRif;
    private Long nrInventarioRif;
    private Integer progressivoRif;

    private Timestamp dacr;
    private String utcr;
    private Timestamp duva;
    private String utuv;
    private Long pgVerRec;

    // --- campi extra per test ---
    private String codBene;
    private String dsBene;
    private String collocazione;

    // --- Getters / Setters ---
    // ... generare tutti i getter e setter


    public Long getPgInventario() {
        return pgInventario;
    }

    public void setPgInventario(Long pgInventario) {
        this.pgInventario = pgInventario;
    }

    public String getTiDocumento() {
        return tiDocumento;
    }

    public void setTiDocumento(String tiDocumento) {
        this.tiDocumento = tiDocumento;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Long getPgDocTrasportoRientro() {
        return pgDocTrasportoRientro;
    }

    public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro) {
        this.pgDocTrasportoRientro = pgDocTrasportoRientro;
    }

    public Long getNrInventario() {
        return nrInventario;
    }

    public void setNrInventario(Long nrInventario) {
        this.nrInventario = nrInventario;
    }

    public Integer getProgressivo() {
        return progressivo;
    }

    public void setProgressivo(Integer progressivo) {
        this.progressivo = progressivo;
    }

    public Long getCdTerzoAssegnatario() {
        return cdTerzoAssegnatario;
    }

    public void setCdTerzoAssegnatario(Long cdTerzoAssegnatario) {
        this.cdTerzoAssegnatario = cdTerzoAssegnatario;
    }

    public Long getPgInventarioRif() {
        return pgInventarioRif;
    }

    public void setPgInventarioRif(Long pgInventarioRif) {
        this.pgInventarioRif = pgInventarioRif;
    }

    public String getTiDocumentoRif() {
        return tiDocumentoRif;
    }

    public void setTiDocumentoRif(String tiDocumentoRif) {
        this.tiDocumentoRif = tiDocumentoRif;
    }

    public Integer getEsercizioRif() {
        return esercizioRif;
    }

    public void setEsercizioRif(Integer esercizioRif) {
        this.esercizioRif = esercizioRif;
    }

    public Long getPgDocTrasportoRientroRif() {
        return pgDocTrasportoRientroRif;
    }

    public void setPgDocTrasportoRientroRif(Long pgDocTrasportoRientroRif) {
        this.pgDocTrasportoRientroRif = pgDocTrasportoRientroRif;
    }

    public Long getNrInventarioRif() {
        return nrInventarioRif;
    }

    public void setNrInventarioRif(Long nrInventarioRif) {
        this.nrInventarioRif = nrInventarioRif;
    }

    public Integer getProgressivoRif() {
        return progressivoRif;
    }

    public void setProgressivoRif(Integer progressivoRif) {
        this.progressivoRif = progressivoRif;
    }

    public Timestamp getDacr() {
        return dacr;
    }

    public void setDacr(Timestamp dacr) {
        this.dacr = dacr;
    }

    public String getUtcr() {
        return utcr;
    }

    public void setUtcr(String utcr) {
        this.utcr = utcr;
    }

    public Timestamp getDuva() {
        return duva;
    }

    public void setDuva(Timestamp duva) {
        this.duva = duva;
    }

    public String getUtuv() {
        return utuv;
    }

    public void setUtuv(String utuv) {
        this.utuv = utuv;
    }

    public Long getPgVerRec() {
        return pgVerRec;
    }

    public void setPgVerRec(Long pgVerRec) {
        this.pgVerRec = pgVerRec;
    }

    public String getCodBene() {
        return codBene;
    }

    public void setCodBene(String codBene) {
        this.codBene = codBene;
    }

    public String getDsBene() {
        return dsBene;
    }

    public void setDsBene(String dsBene) {
        this.dsBene = dsBene;
    }

    public String getCollocazione() {
        return collocazione;
    }

    public void setCollocazione(String collocazione) {
        this.collocazione = collocazione;
    }
}
