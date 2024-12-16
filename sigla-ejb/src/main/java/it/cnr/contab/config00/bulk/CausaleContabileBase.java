/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/12/2024
 */
package it.cnr.contab.config00.bulk;
import it.cnr.jada.persistency.Keyed;
public class CausaleContabileBase extends CausaleContabileKey implements Keyed {
//    DS_CAUSALE VARCHAR(250) NOT NULL
	private java.lang.String dsCausale;
 
//    CD_TIPO_DOCUMENTO_AMM VARCHAR(10) NOT NULL
	private java.lang.String cdTipoDocumentoAmm;
 
//    FL_STORNO VARCHAR(1) NOT NULL
	private java.lang.Boolean flStorno;
 
//    DT_INIZIO_VALIDITA TIMESTAMP(7) NOT NULL
	private java.sql.Timestamp dtInizioValidita;
 
//    DT_FINE_VALIDITA TIMESTAMP(7)
	private java.sql.Timestamp dtFineValidita;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CAUSALE_CONTABILE
	 **/
	public CausaleContabileBase() {
		super();
	}
	public CausaleContabileBase(java.lang.String cdCausale) {
		super(cdCausale);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione]
	 **/
	public java.lang.String getDsCausale() {
		return dsCausale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione]
	 **/
	public void setDsCausale(java.lang.String dsCausale)  {
		this.dsCausale=dsCausale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Identificativo delle tipologie di  documenti amministrativi gestiti.]
	 **/
	public java.lang.String getCdTipoDocumentoAmm() {
		return cdTipoDocumentoAmm;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Identificativo delle tipologie di  documenti amministrativi gestiti.]
	 **/
	public void setCdTipoDocumentoAmm(java.lang.String cdTipoDocumentoAmm)  {
		this.cdTipoDocumentoAmm=cdTipoDocumentoAmm;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Documento di Storno]
	 **/
	public java.lang.Boolean getFlStorno() {
		return flStorno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Documento di Storno]
	 **/
	public void setFlStorno(java.lang.Boolean flStorno)  {
		this.flStorno=flStorno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data inizio validità]
	 **/
	public java.sql.Timestamp getDtInizioValidita() {
		return dtInizioValidita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data inizio validità]
	 **/
	public void setDtInizioValidita(java.sql.Timestamp dtInizioValidita)  {
		this.dtInizioValidita=dtInizioValidita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data fine validità]
	 **/
	public java.sql.Timestamp getDtFineValidita() {
		return dtFineValidita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data fine validità]
	 **/
	public void setDtFineValidita(java.sql.Timestamp dtFineValidita)  {
		this.dtFineValidita=dtFineValidita;
	}
}