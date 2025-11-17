/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/11/2025
 */
package it.cnr.contab.inventario01.bulk;
import it.cnr.jada.persistency.Keyed;
public class Doc_trasporto_rientro_respintoBase extends Doc_trasporto_rientro_respintoKey implements Keyed {
//    PG_INVENTARIO DECIMAL(38,0) NOT NULL
	private Long pgInventario;
 
//    TI_DOCUMENTO CHAR(1) NOT NULL
	private String tiDocumento;
 
//    ESERCIZIO DECIMAL(38,0) NOT NULL
	private Integer esercizio;
 
//    PG_DOC_TRASPORTO_RIENTRO DECIMAL(38,0) NOT NULL
	private Long pgDocTrasportoRientro;
 
//    DATA_INSERIMENTO TIMESTAMP(7) NOT NULL
	private java.sql.Timestamp dataInserimento;
 
//    UID_INSERT VARCHAR(256) NOT NULL
	private String uidInsert;
 
//    TIPO_OPERAZIONE_DOC VARCHAR(2) NOT NULL
	private String tipoOperazioneDoc;
 
//    TIPO_FASE_RESPINGI VARCHAR(3) NOT NULL
	private String tipoFaseRespingi;
 
//    MOTIVO_RESPINGI VARCHAR(2000)
	private String motivoRespingi;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO_RESPINTO
	 **/
	public Doc_trasporto_rientro_respintoBase() {
		super();
	}
	public Doc_trasporto_rientro_respintoBase(Long id) {
		super(id);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice dell'inventario]
	 **/
	public Long getPgInventario() {
		return pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice dell'inventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.pgInventario=pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro (T=Trasporto, R=Rientro)]
	 **/
	public String getTiDocumento() {
		return tiDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro (T=Trasporto, R=Rientro)]
	 **/
	public void setTiDocumento(String tiDocumento)  {
		this.tiDocumento=tiDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio di riferimento]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio di riferimento]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il documento di trasporto/rientro]
	 **/
	public Long getPgDocTrasportoRientro() {
		return pgDocTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il documento di trasporto/rientro]
	 **/
	public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro)  {
		this.pgDocTrasportoRientro=pgDocTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data e ora di inserimento del record di rifiuto]
	 **/
	public java.sql.Timestamp getDataInserimento() {
		return dataInserimento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data e ora di inserimento del record di rifiuto]
	 **/
	public void setDataInserimento(java.sql.Timestamp dataInserimento)  {
		this.dataInserimento=dataInserimento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Utente che ha rifiutato il documento]
	 **/
	public String getUidInsert() {
		return uidInsert;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Utente che ha rifiutato il documento]
	 **/
	public void setUidInsert(String uidInsert)  {
		this.uidInsert=uidInsert;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipo di operazione: TR=Trasporto, RI=Rientro]
	 **/
	public String getTipoOperazioneDoc() {
		return tipoOperazioneDoc;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipo di operazione: TR=Trasporto, RI=Rientro]
	 **/
	public void setTipoOperazioneDoc(String tipoOperazioneDoc)  {
		this.tipoOperazioneDoc=tipoOperazioneDoc;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Fase in cui è avvenuto il rifiuto: RFI=Rifiuto Firma]
	 **/
	public String getTipoFaseRespingi() {
		return tipoFaseRespingi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Fase in cui è avvenuto il rifiuto: RFI=Rifiuto Firma]
	 **/
	public void setTipoFaseRespingi(String tipoFaseRespingi)  {
		this.tipoFaseRespingi=tipoFaseRespingi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Motivazione del rifiuto]
	 **/
	public String getMotivoRespingi() {
		return motivoRespingi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Motivazione del rifiuto]
	 **/
	public void setMotivoRespingi(String motivoRespingi)  {
		this.motivoRespingi=motivoRespingi;
	}
}