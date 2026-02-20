package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class DocTrasportoRientroDTOBulk implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Campi testata DOC_TRASPORTO_RIENTRO ---
    private Long pgInventario;
    private String tiDocumento;
    private Integer esercizio;
    private Long pgDocTrasportoRientro;
    private String dsDocTrasportoRientro;
    private Timestamp dataRegistrazione;
    private String cdTipoTrasportoRientro;
    private String stato;
    private String destinazione;
    private String indirizzo;
    private Boolean flIncaricato;
    private Boolean flVettore;
    private Long cdTerzoIncaricato;
    private String nominativoVettore;
    private String noteRitiro;
    private String note;

    // Colonna CD_TERZO_RESPONSABILE della testata DOC_TRASPORTO_RIENTRO
    private Long cdTerzoResponsabile;

    // --- Campi extra per test ---
    private String cdAnagIncaricato;
    private String cdAnagSmartworking;

    // --- Allegati ---
    private List<AttachmentDocTrasportoRientro> attachments;

    // --- Dettagli beni ---
    private List<DocTrasportoRientroDettDTOBulk> dettagli;

    // --- Getters / Setters ---

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

    public String getDsDocTrasportoRientro() {
        return dsDocTrasportoRientro;
    }

    public void setDsDocTrasportoRientro(String dsDocTrasportoRientro) {
        this.dsDocTrasportoRientro = dsDocTrasportoRientro;
    }

    public Timestamp getDataRegistrazione() {
        return dataRegistrazione;
    }

    public void setDataRegistrazione(Timestamp dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }

    public String getCdTipoTrasportoRientro() {
        return cdTipoTrasportoRientro;
    }

    public void setCdTipoTrasportoRientro(String cdTipoTrasportoRientro) {
        this.cdTipoTrasportoRientro = cdTipoTrasportoRientro;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getDestinazione() {
        return destinazione;
    }

    public void setDestinazione(String destinazione) {
        this.destinazione = destinazione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public Boolean getFlIncaricato() {
        return flIncaricato;
    }

    public void setFlIncaricato(Boolean flIncaricato) {
        this.flIncaricato = flIncaricato;
    }

    public Boolean getFlVettore() {
        return flVettore;
    }

    public void setFlVettore(Boolean flVettore) {
        this.flVettore = flVettore;
    }

    public Long getCdTerzoIncaricato() {
        return cdTerzoIncaricato;
    }

    public void setCdTerzoIncaricato(Long cdTerzoIncaricato) {
        this.cdTerzoIncaricato = cdTerzoIncaricato;
    }

    public String getNominativoVettore() {
        return nominativoVettore;
    }

    public void setNominativoVettore(String nominativoVettore) {
        this.nominativoVettore = nominativoVettore;
    }

    public String getNoteRitiro() {
        return noteRitiro;
    }

    public void setNoteRitiro(String noteRitiro) {
        this.noteRitiro = noteRitiro;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCdTerzoResponsabile() {
        return cdTerzoResponsabile;
    }

    public void setCdTerzoResponsabile(Long cdTerzoResponsabile) {
        this.cdTerzoResponsabile = cdTerzoResponsabile;
    }

    public String getCdAnagIncaricato() {
        return cdAnagIncaricato;
    }

    public void setCdAnagIncaricato(String cdAnagIncaricato) {
        this.cdAnagIncaricato = cdAnagIncaricato;
    }

    public String getCdAnagSmartworking() {
        return cdAnagSmartworking;
    }

    public void setCdAnagSmartworking(String cdAnagSmartworking) {
        this.cdAnagSmartworking = cdAnagSmartworking;
    }

    public List<AttachmentDocTrasportoRientro> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDocTrasportoRientro> attachments) {
        this.attachments = attachments;
    }

    public List<DocTrasportoRientroDettDTOBulk> getDettagli() {
        return dettagli;
    }

    public void setDettagli(List<DocTrasportoRientroDettDTOBulk> dettagli) {
        this.dettagli = dettagli;
    }
}