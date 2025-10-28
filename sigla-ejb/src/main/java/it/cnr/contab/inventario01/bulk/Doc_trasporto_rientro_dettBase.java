/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario01.bulk;
import it.cnr.jada.persistency.Keyed;
public class Doc_trasporto_rientro_dettBase extends Doc_trasporto_rientro_dettKey implements Keyed {
//    INTERVALLO VARCHAR(20) NOT NULL
	private String intervallo;
 
//    QUANTITA DECIMAL(38,0) NOT NULL
	private Long quantita;
 
//    DATA_EFFETTIVA_MOVIMENTAZIONE TIMESTAMP(7)
	private java.sql.Timestamp dataEffettivaMovimentazione;
 
//    PG_INVENTARIO_RIF DECIMAL(38,0)
	private Long pgInventarioRif;

//    TI_DOCUMENTO_RIF CHAR(1)
	private String tiDocumentoRif;
 
//    ESERCIZIO_RIF DECIMAL(5,0)
	private Integer esercizioRif;
 
//    PG_DOC_TRASPORTO_RIENTRO_RIF DECIMAL(38,0)
	private Long pgDocTrasportoRientroRif;
 
//    NR_INVENTARIO_RIF DECIMAL(38,0)
	private Long nrInventarioRif;
 
//    PROGRESSIVO_RIF DECIMAL(5,0)
	private Integer progressivoRif;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO_DETT
	 **/
	public Doc_trasporto_rientro_dettBase() {
		super();
	}
	public Doc_trasporto_rientro_dettBase(Long pgInventario, String tiDocumento, Integer esercizio, Long pgDocTrasportoRientro, Long nrInventario, Integer progressivo) {
		super(pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro, nrInventario, progressivo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Stringa che identifica il gruppo di appartenenza del bene in esame, rispetto agli altri beni dello stesso documento di trasporto]
	 **/
	public String getIntervallo() {
		return intervallo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Stringa che identifica il gruppo di appartenenza del bene in esame, rispetto agli altri beni dello stesso documento di trasporto]
	 **/
	public void setIntervallo(String intervallo)  {
		this.intervallo=intervallo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Quantita di beni trasportati/rientrati]
	 **/
	public Long getQuantita() {
		return quantita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Quantita di beni trasportati/rientrati]
	 **/
	public void setQuantita(Long quantita)  {
		this.quantita=quantita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data effettiva di movimentazione del bene (trasporto o rientro)]
	 **/
	public java.sql.Timestamp getDataEffettivaMovimentazione() {
		return dataEffettivaMovimentazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data effettiva di movimentazione del bene (trasporto o rientro)]
	 **/
	public void setDataEffettivaMovimentazione(java.sql.Timestamp dataEffettivaMovimentazione)  {
		this.dataEffettivaMovimentazione=dataEffettivaMovimentazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento PG_INVENTARIO del documento di trasporto originale (per i rientri)]
	 **/
	public Long getPgInventarioRif() {
		return pgInventarioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento PG_INVENTARIO del documento di trasporto originale (per i rientri)]
	 **/
	public void setPgInventarioRif(Long pgInventarioRif)  {
		this.pgInventarioRif=pgInventarioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento TI_DOCUMENTO del documento di trasporto originale (per i rientri)]
	 **/
	public String getTiDocumentoRif() {
		return tiDocumentoRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento TI_DOCUMENTO del documento di trasporto originale (per i rientri)]
	 **/
	public void setTiDocumentoRif(String tiDocumentoRif)  {
		this.tiDocumentoRif=tiDocumentoRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento ESERCIZIO del documento di trasporto originale (per i rientri)]
	 **/
	public Integer getEsercizioRif() {
		return esercizioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento ESERCIZIO del documento di trasporto originale (per i rientri)]
	 **/
	public void setEsercizioRif(Integer esercizioRif)  {
		this.esercizioRif=esercizioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento PG_DOC_TRASPORTO_RIENTRO del documento di trasporto originale (per i rientri)]
	 **/
	public Long getPgDocTrasportoRientroRif() {
		return pgDocTrasportoRientroRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento PG_DOC_TRASPORTO_RIENTRO del documento di trasporto originale (per i rientri)]
	 **/
	public void setPgDocTrasportoRientroRif(Long pgDocTrasportoRientroRif)  {
		this.pgDocTrasportoRientroRif=pgDocTrasportoRientroRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento NR_INVENTARIO del documento di trasporto originale (per i rientri)]
	 **/
	public Long getNrInventarioRif() {
		return nrInventarioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento NR_INVENTARIO del documento di trasporto originale (per i rientri)]
	 **/
	public void setNrInventarioRif(Long nrInventarioRif)  {
		this.nrInventarioRif=nrInventarioRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Riferimento PROGRESSIVO del documento di trasporto originale (per i rientri)]
	 **/
	public Integer getProgressivoRif() {
		return progressivoRif;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Riferimento PROGRESSIVO del documento di trasporto originale (per i rientri)]
	 **/
	public void setProgressivoRif(Integer progressivoRif)  {
		this.progressivoRif=progressivoRif;
	}
}