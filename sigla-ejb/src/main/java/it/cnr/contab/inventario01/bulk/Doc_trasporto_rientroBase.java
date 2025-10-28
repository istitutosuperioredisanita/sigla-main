/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario01.bulk;
import it.cnr.jada.persistency.Keyed;

public class Doc_trasporto_rientroBase extends Doc_trasporto_rientroKey implements Keyed {

	// DS_DOC_TRASPORTO_RIENTRO VARCHAR(100) NOT NULL
	private String dsDocTrasportoRientro;

	// DATA_REGISTRAZIONE TIMESTAMP(7) NOT NULL
	private java.sql.Timestamp dataRegistrazione;

	// CD_TIPO_TRASPORTO_RIENTRO VARCHAR(10) NOT NULL
	private String cdTipoTrasportoRientro;

	// STATO VARCHAR(3) NOT NULL
	private String stato;

	// DESTINAZIONE VARCHAR(100)
	private String destinazione;

	// INDIRIZZO VARCHAR(200)
	private String indirizzo;

	// FL_INCARICATO CHAR(1) NOT NULL
	private Boolean flIncaricato;

	// FL_VETTORE CHAR(1) NOT NULL
	private Boolean flVettore;

	// CD_TERZO_ASSEGNATARIO NUMBER(8,0) - Codice del dipendente incaricato del ritiro
	private Integer cdTerzoAssegnatario;

	// NOTE_RITIRO VARCHAR(4000)
	private String noteRitiro;

	// NOTE VARCHAR(4000)
	private String note;

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO
	 **/
	public Doc_trasporto_rientroBase() {
		super();
	}

	public Doc_trasporto_rientroBase(Long pgInventario, String tiDocumento, Integer esercizio, Long pgDocTrasportoRientro) {
		super(pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro);
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione del documento di trasporto o rientro]
	 **/
	public String getDsDocTrasportoRientro() {
		return dsDocTrasportoRientro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione del documento di trasporto o rientro]
	 **/
	public void setDsDocTrasportoRientro(String dsDocTrasportoRientro) {
		this.dsDocTrasportoRientro = dsDocTrasportoRientro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data registrazione del documento]
	 **/
	public java.sql.Timestamp getDataRegistrazione() {
		return dataRegistrazione;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data registrazione del documento]
	 **/
	public void setDataRegistrazione(java.sql.Timestamp dataRegistrazione) {
		this.dataRegistrazione = dataRegistrazione;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del tipo di movimento di trasporto/rientro di inventario]
	 **/
	public String getCdTipoTrasportoRientro() {
		return cdTipoTrasportoRientro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del tipo di movimento di trasporto/rientro di inventario]
	 **/
	public void setCdTipoTrasportoRientro(String cdTipoTrasportoRientro) {
		this.cdTipoTrasportoRientro = cdTipoTrasportoRientro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Stato del documento. Dominio: PRE = Predisposto, TRA = Trasportato, RIE = Rientrato, ANN = Annullato]
	 **/
	public String getStato() {
		return stato;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Stato del documento. Dominio: PRE = Predisposto, TRA = Trasportato, RIE = Rientrato, ANN = Annullato]
	 **/
	public void setStato(String stato) {
		this.stato = stato;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione della destinazione del trasporto]
	 **/
	public String getDestinazione() {
		return destinazione;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione della destinazione del trasporto]
	 **/
	public void setDestinazione(String destinazione) {
		this.destinazione = destinazione;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Indirizzo completo della destinazione]
	 **/
	public String getIndirizzo() {
		return indirizzo;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Indirizzo completo della destinazione]
	 **/
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag che indica se il ritiro e' effettuato da un incaricato. Dominio: Y = Si, N = No. Esclusivo con FL_VETTORE. Alla selezione abilita campo NOTE_RITIRO]
	 **/
	public Boolean getFlIncaricato() {
		return flIncaricato;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag che indica se il ritiro e' effettuato da un incaricato. Dominio: Y = Si, N = No. Esclusivo con FL_VETTORE. Alla selezione abilita campo NOTE_RITIRO]
	 **/
	public void setFlIncaricato(Boolean flIncaricato) {
		this.flIncaricato = flIncaricato;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag che indica se il ritiro e' effettuato da un vettore. Dominio: Y = Si, N = No. Esclusivo con FL_INCARICATO. Alla selezione abilita campo NOTE_RITIRO]
	 **/
	public Boolean getFlVettore() {
		return flVettore;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag che indica se il ritiro e' effettuato da un vettore. Dominio: Y = Si, N = No. Esclusivo con FL_INCARICATO. Alla selezione abilita campo NOTE_RITIRO]
	 **/
	public void setFlVettore(Boolean flVettore) {
		this.flVettore = flVettore;
	}

	/**
	 * Restituisce il codice del terzo assegnatario (dipendente incaricato del ritiro)
	 * Obbligatorio quando FL_INCARICATO = Y
	 **/
	public Integer getCdTerzoAssegnatario() {
		return cdTerzoAssegnatario;
	}

	/**
	 * Setta il codice del terzo assegnatario (dipendente incaricato del ritiro)
	 * Obbligatorio quando FL_INCARICATO = Y
	 **/
	public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
		this.cdTerzoAssegnatario = cdTerzoAssegnatario;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Note relative al ritiro del documento. Campo abilitato quando FL_INCARICATO o FL_VETTORE sono valorizzati a Y]
	 **/
	public String getNoteRitiro() {
		return noteRitiro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Note relative al ritiro del documento. Campo abilitato quando FL_INCARICATO o FL_VETTORE sono valorizzati a Y]
	 **/
	public void setNoteRitiro(String noteRitiro) {
		this.noteRitiro = noteRitiro;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Note relative al documento di trasporto/rientro. Campo abilitato in base al flag FL_ABILITA_NOTE della tipologia]
	 **/
	public String getNote() {
		return note;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Note relative al documento di trasporto/rientro. Campo abilitato in base al flag FL_ABILITA_NOTE della tipologia]
	 **/
	public void setNote(String note) {
		this.note = note;
	}
}