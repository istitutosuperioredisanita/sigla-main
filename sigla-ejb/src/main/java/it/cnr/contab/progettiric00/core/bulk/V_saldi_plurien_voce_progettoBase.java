/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/07/2024
 */
package it.cnr.contab.progettiric00.core.bulk;
import it.cnr.jada.persistency.Keyed;
public class V_saldi_plurien_voce_progettoBase extends V_saldi_plurien_voce_progettoKey implements Keyed {
//    PG_PROGETTO DECIMAL(0,-127)
	private java.math.BigDecimal pgProgetto;
 
//    ESERCIZIO_VOCE DECIMAL(5,0) NOT NULL
	private Integer esercizioVoce;
 
//    TI_APPARTENENZA CHAR(1) NOT NULL
	private String tiAppartenenza;
 
//    TI_GESTIONE CHAR(1) NOT NULL
	private String tiGestione;
 
//    CD_ELEMENTO_VOCE VARCHAR(50) NOT NULL
	private String cdElementoVoce;
 
//    ESERCIZIO_PIANO DECIMAL(5,0) NOT NULL
	private Integer esercizioPiano;
 
//    IMPORTO_PLURIENNALE DECIMAL(0,-127)
	private java.math.BigDecimal importoPluriennale;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_SALDI_PLURIENNALI_VOCE_PROGETTO
	 **/
	public V_saldi_plurien_voce_progettoBase() {
		super();
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgProgetto]
	 **/
	public java.math.BigDecimal getPgProgetto() {
		return pgProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgProgetto]
	 **/
	public void setPgProgetto(java.math.BigDecimal pgProgetto)  {
		this.pgProgetto=pgProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioVoce]
	 **/
	public Integer getEsercizioVoce() {
		return esercizioVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioVoce]
	 **/
	public void setEsercizioVoce(Integer esercizioVoce)  {
		this.esercizioVoce=esercizioVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tiAppartenenza]
	 **/
	public String getTiAppartenenza() {
		return tiAppartenenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiAppartenenza]
	 **/
	public void setTiAppartenenza(String tiAppartenenza)  {
		this.tiAppartenenza=tiAppartenenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tiGestione]
	 **/
	public String getTiGestione() {
		return tiGestione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiGestione]
	 **/
	public void setTiGestione(String tiGestione)  {
		this.tiGestione=tiGestione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdElementoVoce]
	 **/
	public String getCdElementoVoce() {
		return cdElementoVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdElementoVoce]
	 **/
	public void setCdElementoVoce(String cdElementoVoce)  {
		this.cdElementoVoce=cdElementoVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioPiano]
	 **/
	public Integer getEsercizioPiano() {
		return esercizioPiano;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioPiano]
	 **/
	public void setEsercizioPiano(Integer esercizioPiano)  {
		this.esercizioPiano=esercizioPiano;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [importoPluriennale]
	 **/
	public java.math.BigDecimal getImportoPluriennale() {
		return importoPluriennale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [importoPluriennale]
	 **/
	public void setImportoPluriennale(java.math.BigDecimal importoPluriennale)  {
		this.importoPluriennale=importoPluriennale;
	}
}