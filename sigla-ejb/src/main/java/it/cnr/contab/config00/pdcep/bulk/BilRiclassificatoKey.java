/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2026
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class BilRiclassificatoKey extends OggettoBulk implements KeyedPersistent {
	private Integer esercizio;
	private String cdUnitaOrganizzativa;
	private String cdTipoBilancio;
	private String cdVoceEp;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: BIL_RICLASSIFICATO
	 **/
	public BilRiclassificatoKey() {
		super();
	}
	public BilRiclassificatoKey(Integer esercizio, String cdUnitaOrganizzativa, String cdTipoBilancio, String cdVoceEp) {
		super();
		this.esercizio=esercizio;
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
		this.cdTipoBilancio=cdTipoBilancio;
		this.cdVoceEp=cdVoceEp;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof BilRiclassificatoKey)) return false;
		BilRiclassificatoKey k = (BilRiclassificatoKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdUnitaOrganizzativa(), k.getCdUnitaOrganizzativa())) return false;
		if (!compareKey(getCdTipoBilancio(), k.getCdTipoBilancio())) return false;
		if (!compareKey(getCdVoceEp(), k.getCdVoceEp())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdUnitaOrganizzativa());
		i = i + calculateKeyHashCode(getCdTipoBilancio());
		i = i + calculateKeyHashCode(getCdVoceEp());
		return i;
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
	 * Restituisce il valore di: [Unità Organizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Unità Organizzativa]
	 **/
	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del Bilancio]
	 **/
	public void setCdTipoBilancio(String cdTipoBilancio)  {
		this.cdTipoBilancio=cdTipoBilancio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del Bilancio]
	 **/
	public String getCdTipoBilancio() {
		return cdTipoBilancio;
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