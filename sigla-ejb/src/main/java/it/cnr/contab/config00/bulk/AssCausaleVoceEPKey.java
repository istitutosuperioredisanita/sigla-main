/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/01/2025
 */
package it.cnr.contab.config00.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class AssCausaleVoceEPKey extends OggettoBulk implements KeyedPersistent {
	private String cdCausale;
	private Integer esercizio;
	private String cdVoceEp;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_CAUSALE_VOCE_EP
	 **/
	public AssCausaleVoceEPKey() {
		super();
	}
	public AssCausaleVoceEPKey(String cdCausale, Integer esercizio, String cdVoceEp) {
		super();
		this.cdCausale=cdCausale;
		this.esercizio=esercizio;
		this.cdVoceEp=cdVoceEp;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof AssCausaleVoceEPKey)) return false;
		AssCausaleVoceEPKey k = (AssCausaleVoceEPKey) o;
		if (!compareKey(getCdCausale(), k.getCdCausale())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdVoceEp(), k.getCdVoceEp())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdCausale());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdVoceEp());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice causale]
	 **/
	public void setCdCausale(String cdCausale)  {
		this.cdCausale=cdCausale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice causale]
	 **/
	public String getCdCausale() {
		return cdCausale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.cdVoceEp=cdVoceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public String getCdVoceEp() {
		return cdVoceEp;
	}
}