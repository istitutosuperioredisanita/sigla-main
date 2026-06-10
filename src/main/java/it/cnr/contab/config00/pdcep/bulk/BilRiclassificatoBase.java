/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2026
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.persistency.Keyed;
public class BilRiclassificatoBase extends BilRiclassificatoKey implements Keyed {
//    SEZIONE CHAR(1) NOT NULL
	private String sezione;

//    SALDO_CONTO DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal saldoConto;
 
//    RETTIFICA DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal rettifica;
 
//    IMPORTO_FINALE DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal importoFinale;
	//    CD_PIANO_GRUPPI CHAR(5) NOT NULL
	private String cdPianoGruppi;
	//    SEZIONE VARCHAR2(2000) NOT NULL
	private String note;

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: BIL_RICLASSIFICATO
	 **/
	public BilRiclassificatoBase() {
		super();
	}
	public BilRiclassificatoBase(Integer esercizio, String cdUnitaOrganizzativa, String cdTipoBilancio, String cdVoceEp) {
		super(esercizio, cdUnitaOrganizzativa, cdTipoBilancio, cdVoceEp);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Saldo totale del conto]
	 **/
	public java.math.BigDecimal getSaldoConto() {
		return saldoConto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Saldo totale del conto]
	 **/
	public void setSaldoConto(java.math.BigDecimal saldoConto)  {
		this.saldoConto=saldoConto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo di rettifica]
	 **/
	public java.math.BigDecimal getRettifica() {
		return rettifica;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo di rettifica]
	 **/
	public void setRettifica(java.math.BigDecimal rettifica)  {
		this.rettifica=rettifica;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo finale]
	 **/
	public java.math.BigDecimal getImportoFinale() {
		return importoFinale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo finale]
	 **/
	public void setImportoFinale(java.math.BigDecimal importoFinale)  {
		this.importoFinale=importoFinale;
	}

	public String getSezione() {
		return sezione;
	}

	public void setSezione(String sezione) {
		this.sezione = sezione;
	}

	public String getCdPianoGruppi() {
		return cdPianoGruppi;
	}

	public void setCdPianoGruppi(String cdPianoGruppi) {
		this.cdPianoGruppi = cdPianoGruppi;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
