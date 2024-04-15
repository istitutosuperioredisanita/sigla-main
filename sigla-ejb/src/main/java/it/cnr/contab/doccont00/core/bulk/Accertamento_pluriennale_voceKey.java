/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 03/01/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Accertamento_pluriennale_voceKey extends OggettoBulk implements KeyedPersistent {
	private String cdCds;
	private Integer esercizio;
	private Integer esercizioOriginale;
	private Long pgAccertamento;
	private Integer anno;
	private String cdCentroResponsabilita;
	private String cdLineaAttivita;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCERTAMENTO_PLURIENNALE_VOCE
	 **/
	public Accertamento_pluriennale_voceKey() {
		super();
	}
	public Accertamento_pluriennale_voceKey(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgAccertamento, Integer anno, String cdCentroResponsabilita, String cdLineaAttivita) {
		super();
		this.cdCds=cdCds;
		this.esercizio=esercizio;
		this.esercizioOriginale=esercizioOriginale;
		this.pgAccertamento=pgAccertamento;
		this.anno=anno;
		this.cdCentroResponsabilita=cdCentroResponsabilita;
		this.cdLineaAttivita=cdLineaAttivita;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Accertamento_pluriennale_voceKey)) return false;
		Accertamento_pluriennale_voceKey k = (Accertamento_pluriennale_voceKey) o;
		if (!compareKey(getCdCds(), k.getCdCds())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getEsercizioOriginale(), k.getEsercizioOriginale())) return false;
		if (!compareKey(getPgAccertamento(), k.getPgAccertamento())) return false;
		if (!compareKey(getAnno(), k.getAnno())) return false;
		if (!compareKey(getCdCentroResponsabilita(), k.getCdCentroResponsabilita())) return false;
		if (!compareKey(getCdLineaAttivita(), k.getCdLineaAttivita())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdCds());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getEsercizioOriginale());
		i = i + calculateKeyHashCode(getPgAccertamento());
		i = i + calculateKeyHashCode(getAnno());
		i = i + calculateKeyHashCode(getCdCentroResponsabilita());
		i = i + calculateKeyHashCode(getCdLineaAttivita());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'accertamento]
	 **/
	public void setCdCds(String cdCds)  {
		this.cdCds=cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'accertamento]
	 **/
	public String getCdCds() {
		return cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'accertamento]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'accertamento]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'accertamento]
	 **/
	public void setEsercizioOriginale(Integer esercizioOriginale)  {
		this.esercizioOriginale=esercizioOriginale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'accertamento]
	 **/
	public Integer getEsercizioOriginale() {
		return esercizioOriginale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'accertamento]
	 **/
	public void setPgAccertamento(Long pgAccertamento)  {
		this.pgAccertamento=pgAccertamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'accertamento]
	 **/
	public Long getPgAccertamento() {
		return pgAccertamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno Accertamento Pluriennale]
	 **/
	public void setAnno(Integer anno)  {
		this.anno=anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno Accertamento Pluriennale]
	 **/
	public Integer getAnno() {
		return anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità]
	 **/
	public void setCdCentroResponsabilita(String cdCentroResponsabilita)  {
		this.cdCentroResponsabilita=cdCentroResponsabilita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità]
	 **/
	public String getCdCentroResponsabilita() {
		return cdCentroResponsabilita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita]
	 **/
	public void setCdLineaAttivita(String cdLineaAttivita)  {
		this.cdLineaAttivita=cdLineaAttivita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita]
	 **/
	public String getCdLineaAttivita() {
		return cdLineaAttivita;
	}
}