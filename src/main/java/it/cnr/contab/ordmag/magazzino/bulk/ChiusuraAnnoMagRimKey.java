/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class ChiusuraAnnoMagRimKey extends OggettoBulk implements KeyedPersistent {
	private String cdCdsLotto;
	private String cdMagazzinoLotto;
	private Integer esercizioLotto;
	private String cdNumeratoreLotto;
	private Integer pgLotto;
	private Integer anno;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_MAG_RIM
	 **/
	public ChiusuraAnnoMagRimKey() {
		super();
	}
	public ChiusuraAnnoMagRimKey(String cdCdsLotto, String cdMagazzinoLotto, Integer esercizioLotto, String cdNumeratoreLotto, Integer pgLotto,Integer anno) {
		super();
		this.cdCdsLotto=cdCdsLotto;
		this.cdMagazzinoLotto=cdMagazzinoLotto;
		this.esercizioLotto=esercizioLotto;
		this.cdNumeratoreLotto=cdNumeratoreLotto;
		this.pgLotto=pgLotto;
		this.anno=anno;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof ChiusuraAnnoMagRimKey)) return false;
		ChiusuraAnnoMagRimKey k = (ChiusuraAnnoMagRimKey) o;
		if (!compareKey(getCdCdsLotto(), k.getCdCdsLotto())) return false;
		if (!compareKey(getCdMagazzinoLotto(), k.getCdMagazzinoLotto())) return false;
		if (!compareKey(getEsercizioLotto(), k.getEsercizioLotto())) return false;
		if (!compareKey(getCdNumeratoreLotto(), k.getCdNumeratoreLotto())) return false;
		if (!compareKey(getPgLotto(), k.getPgLotto())) return false;
		if (!compareKey(getAnno(), k.getAnno())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdCdsLotto());
		i = i + calculateKeyHashCode(getCdMagazzinoLotto());
		i = i + calculateKeyHashCode(getEsercizioLotto());
		i = i + calculateKeyHashCode(getCdNumeratoreLotto());
		i = i + calculateKeyHashCode(getPgLotto());
		i = i + calculateKeyHashCode(getAnno());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds lotto]
	 **/
	public void setCdCdsLotto(String cdCdsLotto)  {
		this.cdCdsLotto=cdCdsLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds lotto]
	 **/
	public String getCdCdsLotto() {
		return cdCdsLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cd magazzino lotto]
	 **/
	public void setCdMagazzinoLotto(String cdMagazzinoLotto)  {
		this.cdMagazzinoLotto=cdMagazzinoLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cd magazzino lotto]
	 **/
	public String getCdMagazzinoLotto() {
		return cdMagazzinoLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio del Lotto]
	 **/
	public void setEsercizioLotto(Integer esercizioLotto)  {
		this.esercizioLotto=esercizioLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio del Lotto]
	 **/
	public Integer getEsercizioLotto() {
		return esercizioLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numeratore del Lotto]
	 **/
	public void setCdNumeratoreLotto(String cdNumeratoreLotto)  {
		this.cdNumeratoreLotto=cdNumeratoreLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeratore del Lotto]
	 **/
	public String getCdNumeratoreLotto() {
		return cdNumeratoreLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [progressivo del Lotto]
	 **/
	public void setPgLotto(Integer pgLotto)  {
		this.pgLotto=pgLotto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [progressivo del Lotto]
	 **/
	public Integer getPgLotto() {
		return pgLotto;
	}

	public Integer getAnno() {
		return anno;
	}

	public void setAnno(Integer anno) {
		this.anno = anno;
	}
}