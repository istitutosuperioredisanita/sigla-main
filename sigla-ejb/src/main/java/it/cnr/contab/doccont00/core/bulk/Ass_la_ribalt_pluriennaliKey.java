/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/12/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Ass_la_ribalt_pluriennaliKey extends OggettoBulk implements KeyedPersistent {
	private Integer esercizio;
	private String cdCentroResponsabilita;
	private String cdLineaAttivita;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_LA_RIBALT_PLURIENNALI
	 **/
	public Ass_la_ribalt_pluriennaliKey() {
		super();
	}
	public Ass_la_ribalt_pluriennaliKey(Integer esercizio, String cdCentroResponsabilita, String cdLineaAttivita) {
		super();
		this.esercizio=esercizio;
		this.cdCentroResponsabilita=cdCentroResponsabilita;
		this.cdLineaAttivita=cdLineaAttivita;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Ass_la_ribalt_pluriennaliKey)) return false;
		Ass_la_ribalt_pluriennaliKey k = (Ass_la_ribalt_pluriennaliKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdCentroResponsabilita(), k.getCdCentroResponsabilita())) return false;
		if (!compareKey(getCdLineaAttivita(), k.getCdLineaAttivita())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdCentroResponsabilita());
		i = i + calculateKeyHashCode(getCdLineaAttivita());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio di ribaltamento]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio di ribaltamento]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public void setCdCentroResponsabilita(String cdCentroResponsabilita)  {
		this.cdCentroResponsabilita=cdCentroResponsabilita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public String getCdCentroResponsabilita() {
		return cdCentroResponsabilita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public void setCdLineaAttivita(String cdLineaAttivita)  {
		this.cdLineaAttivita=cdLineaAttivita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public String getCdLineaAttivita() {
		return cdLineaAttivita;
	}
}