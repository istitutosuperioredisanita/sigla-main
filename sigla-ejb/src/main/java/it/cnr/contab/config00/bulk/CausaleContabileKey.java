/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/12/2024
 */
package it.cnr.contab.config00.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class CausaleContabileKey extends OggettoBulk implements KeyedPersistent {
	private java.lang.String cdCausale;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CAUSALE_CONTABILE
	 **/
	public CausaleContabileKey() {
		super();
	}
	public CausaleContabileKey(java.lang.String cdCausale) {
		super();
		this.cdCausale=cdCausale;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof CausaleContabileKey)) return false;
		CausaleContabileKey k = (CausaleContabileKey) o;
		if (!compareKey(getCdCausale(), k.getCdCausale())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdCausale());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice]
	 **/
	public void setCdCausale(java.lang.String cdCausale)  {
		this.cdCausale=cdCausale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice]
	 **/
	public java.lang.String getCdCausale() {
		return cdCausale;
	}
}