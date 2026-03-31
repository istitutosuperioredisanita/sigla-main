/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 07/02/2023
 */
package it.cnr.contab.config00.consultazioni.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Keyed;
import it.cnr.jada.persistency.Persistent;

public class VControlloDispObblScadBase extends OggettoBulk implements Persistent {
//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private String cdUnitaOrganizzativa;
 
//    ESERCIZIO_OBBLIGAZIONE DECIMAL(5,0)
	private Integer esercizioObbligazione;
 
//    ESERCIZIO_ORI_OBBLIGAZIONE DECIMAL(5,0)
	private Integer esercizioOriObbligazione;
 
//    CD_CDS_OBBLIGAZIONE VARCHAR(30)
	private String cdCdsObbligazione;
 
//    PG_OBBLIGAZIONE DECIMAL(38,0)
	private Long pgObbligazione;
 
//    PG_OBBLIGAZIONE_SCADENZARIO DECIMAL(38,0)
	private Long pgObbligazioneScadenzario;
 
//    TOT_ORDINE_IMPEGNO_STESSA_UO DECIMAL(0,-127)
	private java.math.BigDecimal totOrdineImpegnoStessaUo;
 
//    TOT_ORDINE_IMPEGNO_ALTRA_UO DECIMAL(0,-127)
	private java.math.BigDecimal totOrdineImpegnoAltraUo;
 
//    TOT_FATTURE_ORDINI DECIMAL(0,-127)
	private java.math.BigDecimal totFattureOrdini;
 
//    TOT_FATTURE_NO_ORDINI DECIMAL(0,-127)
	private java.math.BigDecimal totFattureNoOrdini;
 
//    TOT_DOCUMENTI_GENERICI DECIMAL(0,-127)
	private java.math.BigDecimal totDocumentiGenerici;
 
//    TOT_IMPEGNO_ASSOCIATO_CALCOLATO DECIMAL(0,-127)
	private java.math.BigDecimal totImpegnoAssociatoCalcolato;
 
//    TOT_IMPEGNO_ASSOCIATO DECIMAL(0,-127)
	private java.math.BigDecimal totImpegnoAssociato;
 
//    TOT_IMPEGNO_DA_ASSOCIARE DECIMAL(0,-127)
	private java.math.BigDecimal totImpegnoDaAssociare;
 

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOrganizzativa]
	 **/
	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioObbligazione]
	 **/
	public Integer getEsercizioObbligazione() {
		return esercizioObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioObbligazione]
	 **/
	public void setEsercizioObbligazione(Integer esercizioObbligazione)  {
		this.esercizioObbligazione=esercizioObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioOriObbligazione]
	 **/
	public Integer getEsercizioOriObbligazione() {
		return esercizioOriObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioOriObbligazione]
	 **/
	public void setEsercizioOriObbligazione(Integer esercizioOriObbligazione)  {
		this.esercizioOriObbligazione=esercizioOriObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCdsObbligazione]
	 **/
	public String getCdCdsObbligazione() {
		return cdCdsObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCdsObbligazione]
	 **/
	public void setCdCdsObbligazione(String cdCdsObbligazione)  {
		this.cdCdsObbligazione=cdCdsObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgObbligazione]
	 **/
	public Long getPgObbligazione() {
		return pgObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgObbligazione]
	 **/
	public void setPgObbligazione(Long pgObbligazione)  {
		this.pgObbligazione=pgObbligazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgObbligazioneScadenzario]
	 **/
	public Long getPgObbligazioneScadenzario() {
		return pgObbligazioneScadenzario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgObbligazioneScadenzario]
	 **/
	public void setPgObbligazioneScadenzario(Long pgObbligazioneScadenzario)  {
		this.pgObbligazioneScadenzario=pgObbligazioneScadenzario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totOrdineImpegnoStessaUo]
	 **/
	public java.math.BigDecimal getTotOrdineImpegnoStessaUo() {
		return totOrdineImpegnoStessaUo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totOrdineImpegnoStessaUo]
	 **/
	public void setTotOrdineImpegnoStessaUo(java.math.BigDecimal totOrdineImpegnoStessaUo)  {
		this.totOrdineImpegnoStessaUo=totOrdineImpegnoStessaUo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totOrdineImpegnoAltraUo]
	 **/
	public java.math.BigDecimal getTotOrdineImpegnoAltraUo() {
		return totOrdineImpegnoAltraUo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totOrdineImpegnoAltraUo]
	 **/
	public void setTotOrdineImpegnoAltraUo(java.math.BigDecimal totOrdineImpegnoAltraUo)  {
		this.totOrdineImpegnoAltraUo=totOrdineImpegnoAltraUo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totFattureOrdini]
	 **/
	public java.math.BigDecimal getTotFattureOrdini() {
		return totFattureOrdini;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totFattureOrdini]
	 **/
	public void setTotFattureOrdini(java.math.BigDecimal totFattureOrdini)  {
		this.totFattureOrdini=totFattureOrdini;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totFattureNoOrdini]
	 **/
	public java.math.BigDecimal getTotFattureNoOrdini() {
		return totFattureNoOrdini;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totFattureNoOrdini]
	 **/
	public void setTotFattureNoOrdini(java.math.BigDecimal totFattureNoOrdini)  {
		this.totFattureNoOrdini=totFattureNoOrdini;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totDocumentiGenerici]
	 **/
	public java.math.BigDecimal getTotDocumentiGenerici() {
		return totDocumentiGenerici;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totDocumentiGenerici]
	 **/
	public void setTotDocumentiGenerici(java.math.BigDecimal totDocumentiGenerici)  {
		this.totDocumentiGenerici=totDocumentiGenerici;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totImpegnoAssociatoCalcolato]
	 **/
	public java.math.BigDecimal getTotImpegnoAssociatoCalcolato() {
		return totImpegnoAssociatoCalcolato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totImpegnoAssociatoCalcolato]
	 **/
	public void setTotImpegnoAssociatoCalcolato(java.math.BigDecimal totImpegnoAssociatoCalcolato)  {
		this.totImpegnoAssociatoCalcolato=totImpegnoAssociatoCalcolato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totImpegnoAssociato]
	 **/
	public java.math.BigDecimal getTotImpegnoAssociato() {
		return totImpegnoAssociato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totImpegnoAssociato]
	 **/
	public void setTotImpegnoAssociato(java.math.BigDecimal totImpegnoAssociato)  {
		this.totImpegnoAssociato=totImpegnoAssociato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totImpegnoDaAssociare]
	 **/
	public java.math.BigDecimal getTotImpegnoDaAssociare() {
		return totImpegnoDaAssociare;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totImpegnoDaAssociare]
	 **/
	public void setTotImpegnoDaAssociare(java.math.BigDecimal totImpegnoDaAssociare)  {
		this.totImpegnoDaAssociare=totImpegnoDaAssociare;
	}
}