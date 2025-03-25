/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 14/03/2025
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.persistency.Keyed;
public class TipoBilancioBase extends TipoBilancioKey implements Keyed {
//    DS_TIPO_BILANCIO VARCHAR(250) NOT NULL
	private String dsTipoBilancio;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_BILANCIO
	 **/
	public TipoBilancioBase() {
		super();
	}
	public TipoBilancioBase(String cdTipoBilancio) {
		super(cdTipoBilancio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione del bilancio]
	 **/
	public String getDsTipoBilancio() {
		return dsTipoBilancio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione del bilancio]
	 **/
	public void setDsTipoBilancio(String dsTipoBilancio)  {
		this.dsTipoBilancio=dsTipoBilancio;
	}
}