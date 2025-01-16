/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/01/2025
 */
package it.cnr.contab.config00.bulk;
import it.cnr.jada.persistency.Keyed;
public class AssCausaleVoceEPBase extends AssCausaleVoceEPKey implements Keyed {
//    TI_SEZIONE CHAR(1) NOT NULL
	private String tiSezione;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_CAUSALE_VOCE_EP
	 **/
	public AssCausaleVoceEPBase() {
		super();
	}
	public AssCausaleVoceEPBase(String cdCausale, Integer esercizio, String cdVoceEp) {
		super(cdCausale, esercizio, cdVoceEp);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Sezione del conto.]
	 **/
	public String getTiSezione() {
		return tiSezione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Sezione del conto.]
	 **/
	public void setTiSezione(String tiSezione)  {
		this.tiSezione=tiSezione;
	}
}