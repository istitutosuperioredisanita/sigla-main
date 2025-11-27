package it.cnr.contab.inventario01.bulk;
import it.cnr.jada.persistency.Keyed;

public class Doc_trasporto_rientroBase extends Doc_trasporto_rientroKey implements Keyed {

	// ========================================
	// CAMPI ESISTENTI
	// ========================================

	private String dsDocTrasportoRientro;
	private java.sql.Timestamp dataRegistrazione;
	private String cdTipoTrasportoRientro;
	private String stato;
	private String destinazione;
	private String indirizzo;
	private Boolean flIncaricato=Boolean.FALSE;
	private Boolean flVettore=Boolean.FALSE;
	private Integer cdTerzoAssegnatario;
	private String noteRitiro;
	private String note;
	private String nominativoVettore;

	// ========================================
	// NUOVI CAMPI PER FIRMA DIGITALE HAPPYSIGN
	// ========================================

	/**
	 * ID del flusso HappySign per la firma digitale
	 */
	private String idFlussoHappysign;

	/**
	 * Stato del flusso di firma
	 * Valori: INV = Inviato, FIR = Firmato, RIF = Rifiutato
	 */
	private String statoFlusso;

	/**
	 * Data invio al flusso di firma
	 */
	private java.sql.Timestamp dataInvioFirma;

	/**
	 * Data completamento firma
	 */
	private java.sql.Timestamp dataFirma;

	/**
	 * Note in caso di rifiuto firma
	 */
	private String noteRifiuto;

	/**
	 * Codice terzo responsabile struttura (firmatario obbligatorio)
	 */
	private Integer cdTerzoResponsabile;

	/**
	 * Tipo documento di riferimento (per collegamento tra trasporti e rientri)
	 */
	private String tiDocumentoRif;

	/**
	 * Esercizio del documento di riferimento
	 */
	private Integer esercizioRif;

	/**
	 * Progressivo del documento di riferimento
	 */
	private Long pgDocTrasportoRientroRif;

	// ========================================
	// COSTRUTTORI
	// ========================================

	public Doc_trasporto_rientroBase() {
		super();
	}

	public Doc_trasporto_rientroBase(Long pgInventario, String tiDocumento, Integer esercizio, Long pgDocTrasportoRientro) {
		super(pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro);
	}

	// ========================================
	// GETTER E SETTER - CAMPI ESISTENTI
	// ========================================

	public String getDsDocTrasportoRientro() {
		return dsDocTrasportoRientro;
	}

	public void setDsDocTrasportoRientro(String dsDocTrasportoRientro) {
		this.dsDocTrasportoRientro = dsDocTrasportoRientro;
	}

	public java.sql.Timestamp getDataRegistrazione() {
		return dataRegistrazione;
	}

	public void setDataRegistrazione(java.sql.Timestamp dataRegistrazione) {
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

	public Integer getCdTerzoAssegnatario() {
		return cdTerzoAssegnatario;
	}

	public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
		this.cdTerzoAssegnatario = cdTerzoAssegnatario;
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

	public String getNominativoVettore() {
		return nominativoVettore;
	}

	public void setNominativoVettore(String nominativoVettore) {
		this.nominativoVettore = nominativoVettore;
	}

	// ========================================
	// GETTER E SETTER - FIRMA DIGITALE HAPPYSIGN
	// ========================================

	public String getIdFlussoHappysign() {
		return idFlussoHappysign;
	}

	public void setIdFlussoHappysign(String idFlussoHappysign) {
		this.idFlussoHappysign = idFlussoHappysign;
	}

	public String getStatoFlusso() {
		return statoFlusso;
	}

	public void setStatoFlusso(String statoFlusso) {
		this.statoFlusso = statoFlusso;
	}

	public java.sql.Timestamp getDataInvioFirma() {
		return dataInvioFirma;
	}

	public void setDataInvioFirma(java.sql.Timestamp dataInvioFirma) {
		this.dataInvioFirma = dataInvioFirma;
	}

	public java.sql.Timestamp getDataFirma() {
		return dataFirma;
	}

	public void setDataFirma(java.sql.Timestamp dataFirma) {
		this.dataFirma = dataFirma;
	}

	public String getNoteRifiuto() {
		return noteRifiuto;
	}

	public void setNoteRifiuto(String noteRifiuto) {
		this.noteRifiuto = noteRifiuto;
	}

	public Integer getCdTerzoResponsabile() {
		return cdTerzoResponsabile;
	}

	public void setCdTerzoResponsabile(Integer cdTerzoResponsabile) {
		this.cdTerzoResponsabile = cdTerzoResponsabile;
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
}
