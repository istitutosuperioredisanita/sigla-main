/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.docamm00.tabrif.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Doc_trasporto_rientro_dettKey extends OggettoBulk implements KeyedPersistent {
	private Long pgInventario;
	private String tiDocumento;
	private Integer esercizio;
	private Long pgDocTrasportoRientro;
	private Long nrInventario;
	private Integer progressivo;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO_DETT
	 **/
	public Doc_trasporto_rientro_dettKey() {
		super();
	}
	public Doc_trasporto_rientro_dettKey(Long pgInventario, String tiDocumento, Integer esercizio, Long pgDocTrasportoRientro, Long nrInventario, Integer progressivo) {
		super();
		this.pgInventario=pgInventario;
		this.tiDocumento=tiDocumento;
		this.esercizio=esercizio;
		this.pgDocTrasportoRientro=pgDocTrasportoRientro;
		this.nrInventario=nrInventario;
		this.progressivo=progressivo;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Doc_trasporto_rientro_dettKey)) return false;
		Doc_trasporto_rientro_dettKey k = (Doc_trasporto_rientro_dettKey) o;
		if (!compareKey(getPgInventario(), k.getPgInventario())) return false;
		if (!compareKey(getTiDocumento(), k.getTiDocumento())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getPgDocTrasportoRientro(), k.getPgDocTrasportoRientro())) return false;
		if (!compareKey(getNrInventario(), k.getNrInventario())) return false;
		if (!compareKey(getProgressivo(), k.getProgressivo())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgInventario());
		i = i + calculateKeyHashCode(getTiDocumento());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getPgDocTrasportoRientro());
		i = i + calculateKeyHashCode(getNrInventario());
		i = i + calculateKeyHashCode(getProgressivo());
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
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public void setNrInventario(Long nrInventario)  {
		this.nrInventario=nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public Long getNrInventario() {
		return nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene accessorio all'interno di un inventario e una volta noto il bene primario]
	 **/
	public void setProgressivo(Integer progressivo)  {
		this.progressivo=progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene accessorio all'interno di un inventario e una volta noto il bene primario]
	 **/
	public Integer getProgressivo() {
		return progressivo;
	}
}