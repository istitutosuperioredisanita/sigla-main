/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class ChiusuraAnnoKey extends OggettoBulk implements KeyedPersistent {
	private Integer pgChiusura;
	private Integer anno;
	private String tipoChiusura;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO
	 **/
	public ChiusuraAnnoKey() {
		super();
	}
	public ChiusuraAnnoKey(Integer pgChiusura, Integer anno, String tipoChiusura) {
		super();
		this.pgChiusura=pgChiusura;
		this.anno=anno;
		this.tipoChiusura=tipoChiusura;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof ChiusuraAnnoKey)) return false;
		ChiusuraAnnoKey k = (ChiusuraAnnoKey) o;
		if (!compareKey(getPgChiusura(), k.getPgChiusura())) return false;
		if (!compareKey(getAnno(), k.getAnno())) return false;
		if (!compareKey(getTipoChiusura(), k.getTipoChiusura())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgChiusura());
		i = i + calculateKeyHashCode(getAnno());
		i = i + calculateKeyHashCode(getTipoChiusura());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo record chiusura]
	 **/
	public void setPgChiusura(Integer pgChiusura)  {
		this.pgChiusura=pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo record chiusura]
	 **/
	public Integer getPgChiusura() {
		return pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di chiusura]
	 **/
	public void setAnno(Integer anno)  {
		this.anno=anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di chiusura]
	 **/
	public Integer getAnno() {
		return anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [M=Magazzino; I=Inventario]
	 **/
	public void setTipoChiusura(String tipoChiusura)  {
		this.tipoChiusura=tipoChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [M=Magazzino; I=Inventario]
	 **/
	public String getTipoChiusura() {
		return tipoChiusura;
	}
}