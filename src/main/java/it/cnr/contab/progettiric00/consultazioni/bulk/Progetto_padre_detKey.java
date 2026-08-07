/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 02/07/2026
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Progetto_padre_detKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_PROGETTO_PADRE_DET
	 **/

	public Progetto_padre_detKey() {
		super();
	}
	public Progetto_padre_detKey(java.lang.Integer esercizio, java.lang.String cdProgetto, java.lang.String cdProgettoPadre, java.lang.String cdUnitaOrganizzativa) {
		super();
		this.esercizio=esercizio;
		this.cdProgetto=cdProgetto;
		this.cdProgettoPadre=cdProgettoPadre;
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}

	private java.lang.Integer esercizio;

	//    CD_PROGETTO VARCHAR(30)
	private java.lang.String cdProgetto;

	//    CD_PROGETTO_PADRE VARCHAR(30)
	private java.lang.String cdProgettoPadre;

	//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private java.lang.String cdUnitaOrganizzativa;


	public java.lang.Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(java.lang.Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdProgetto]
	 **/
	public java.lang.String getCdProgetto() {
		return cdProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdProgetto]
	 **/
	public void setCdProgetto(java.lang.String cdProgetto)  {
		this.cdProgetto=cdProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdProgettoPadre]
	 **/
	public java.lang.String getCdProgettoPadre() {
		return cdProgettoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdProgettoPadre]
	 **/
	public void setCdProgettoPadre(java.lang.String cdProgettoPadre)  {
		this.cdProgettoPadre=cdProgettoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOrganizzativa]
	 **/
	public java.lang.String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(java.lang.String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}


	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Progetto_padre_detKey)) return false;
		Progetto_padre_detKey k = (Progetto_padre_detKey) o;

		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdProgetto(), k.getCdProgetto())) return false;
		if (!compareKey(getCdProgettoPadre(), k.getCdProgettoPadre())) return false;
		if (!compareKey(getCdUnitaOrganizzativa(), k.getCdUnitaOrganizzativa())) return false;

		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdProgetto());
		i = i + calculateKeyHashCode(getCdProgettoPadre());
		i = i + calculateKeyHashCode(getCdUnitaOrganizzativa());
		return i;
	}
}