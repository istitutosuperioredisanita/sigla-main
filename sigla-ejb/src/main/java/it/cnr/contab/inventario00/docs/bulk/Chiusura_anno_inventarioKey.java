/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Chiusura_anno_inventarioKey extends OggettoBulk implements KeyedPersistent {
	private Integer pgChiusura;
	private Integer anno;
	private String tipoChiusura;
	private String cdCategoriaGruppo;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_INVENTARIO
	 **/
	public Chiusura_anno_inventarioKey() {
		super();
	}
	public Chiusura_anno_inventarioKey(Integer pgChiusura, Integer anno, String tipoChiusura, String cdCategoriaGruppo) {
		super();
		this.pgChiusura=pgChiusura;
		this.anno=anno;
		this.tipoChiusura=tipoChiusura;
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Chiusura_anno_inventarioKey)) return false;
		Chiusura_anno_inventarioKey k = (Chiusura_anno_inventarioKey) o;
		if (!compareKey(getPgChiusura(), k.getPgChiusura())) return false;
		if (!compareKey(getAnno(), k.getAnno())) return false;
		if (!compareKey(getTipoChiusura(), k.getTipoChiusura())) return false;
		if (!compareKey(getCdCategoriaGruppo(), k.getCdCategoriaGruppo())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgChiusura());
		i = i + calculateKeyHashCode(getAnno());
		i = i + calculateKeyHashCode(getTipoChiusura());
		i = i + calculateKeyHashCode(getCdCategoriaGruppo());
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
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice categoria/gruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice categoria/gruppo]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
}