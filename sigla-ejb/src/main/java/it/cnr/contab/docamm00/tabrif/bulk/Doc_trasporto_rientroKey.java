/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.docamm00.tabrif.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Doc_trasporto_rientroKey extends OggettoBulk implements KeyedPersistent {
	private Long pgInventario;
	private String tiDocumento;
	private Integer esercizio;
	private Long pgDocTrasportoRientro;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO
	 **/
	public Doc_trasporto_rientroKey() {
		super();
	}
	public Doc_trasporto_rientroKey(Long pgInventario, String tiDocumento, Integer esercizio, Long pgDocTrasportoRientro) {
		super();
		this.pgInventario=pgInventario;
		this.tiDocumento=tiDocumento;
		this.esercizio=esercizio;
		this.pgDocTrasportoRientro=pgDocTrasportoRientro;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Doc_trasporto_rientroKey)) return false;
		Doc_trasporto_rientroKey k = (Doc_trasporto_rientroKey) o;
		if (!compareKey(getPgInventario(), k.getPgInventario())) return false;
		if (!compareKey(getTiDocumento(), k.getTiDocumento())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getPgDocTrasportoRientro(), k.getPgDocTrasportoRientro())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgInventario());
		i = i + calculateKeyHashCode(getTiDocumento());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getPgDocTrasportoRientro());
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
	 * Restituisce il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro (T=Trasporto, R=Rientro)]
	 **/
	public void setTiDocumento(String tiDocumento)  {
		this.tiDocumento=tiDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag per stabilire se il documento risulta essere un trasporto o un rientro (T=Trasporto, R=Rientro)]
	 **/
	public String getTiDocumento() {
		return tiDocumento;
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
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il documento di trasporto/rientro una volta noto inventario e esercizio]
	 **/
	public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro)  {
		this.pgDocTrasportoRientro=pgDocTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il documento di trasporto/rientro una volta noto inventario e esercizio]
	 **/
	public Long getPgDocTrasportoRientro() {
		return pgDocTrasportoRientro;
	}
}