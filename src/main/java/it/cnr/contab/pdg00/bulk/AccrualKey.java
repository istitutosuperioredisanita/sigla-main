/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class AccrualKey extends OggettoBulk implements KeyedPersistent {
	private Integer esercizio;

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCRUAL
	 **/
	public AccrualKey() {
		super();
	}
	public AccrualKey(Integer esercizio) {
		super();
		this.esercizio=esercizio;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof AccrualKey)) return false;
		AccrualKey k = (AccrualKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;

		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio di riferimento]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio di riferimento]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}

}