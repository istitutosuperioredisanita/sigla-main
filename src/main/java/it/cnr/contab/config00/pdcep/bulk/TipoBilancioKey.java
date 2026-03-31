/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 14/03/2025
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class TipoBilancioKey extends OggettoBulk implements KeyedPersistent {
	private String cdTipoBilancio;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_BILANCIO
	 **/
	public TipoBilancioKey() {
		super();
	}
	public TipoBilancioKey(String cdTipoBilancio) {
		super();
		this.cdTipoBilancio=cdTipoBilancio;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof TipoBilancioKey)) return false;
		TipoBilancioKey k = (TipoBilancioKey) o;
		if (!compareKey(getCdTipoBilancio(), k.getCdTipoBilancio())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdTipoBilancio());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del bilancio]
	 **/
	public void setCdTipoBilancio(String cdTipoBilancio)  {
		this.cdTipoBilancio=cdTipoBilancio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del bilancio]
	 **/
	public String getCdTipoBilancio() {
		return cdTipoBilancio;
	}
}