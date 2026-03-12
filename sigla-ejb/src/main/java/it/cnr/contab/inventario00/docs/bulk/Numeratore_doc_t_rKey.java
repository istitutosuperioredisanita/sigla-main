/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Numeratore_doc_t_rKey extends OggettoBulk implements KeyedPersistent {
	private Long pgInventario;
	private String tiTrasportoRientro;
	private Integer esercizio;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: NUMERATORE_DOC_T_R
	 **/
	public Numeratore_doc_t_rKey() {
		super();
	}
	public Numeratore_doc_t_rKey(Long pgInventario, String tiTrasportoRientro, Integer esercizio) {
		super();
		this.pgInventario=pgInventario;
		this.tiTrasportoRientro=tiTrasportoRientro;
		this.esercizio=esercizio;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Numeratore_doc_t_rKey)) return false;
		Numeratore_doc_t_rKey k = (Numeratore_doc_t_rKey) o;
		if (!compareKey(getPgInventario(), k.getPgInventario())) return false;
		if (!compareKey(getTiTrasportoRientro(), k.getTiTrasportoRientro())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgInventario());
		i = i + calculateKeyHashCode(getTiTrasportoRientro());
		i = i + calculateKeyHashCode(getEsercizio());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice dell'inventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.pgInventario=pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice dell'inventario]
	 **/
	public Long getPgInventario() {
		return pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro]
	 **/
	public void setTiTrasportoRientro(String tiTrasportoRientro)  {
		this.tiTrasportoRientro=tiTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro]
	 **/
	public String getTiTrasportoRientro() {
		return tiTrasportoRientro;
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