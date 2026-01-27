/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/11/2025
 */
package it.cnr.contab.inventario01.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Doc_trasporto_rientro_respintoKey extends OggettoBulk implements KeyedPersistent {
	private Long id;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: DOC_TRASPORTO_RIENTRO_RESPINTO
	 **/
	public Doc_trasporto_rientro_respintoKey() {
		super();
	}
	public Doc_trasporto_rientro_respintoKey(Long id) {
		super();
		this.id=id;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Doc_trasporto_rientro_respintoKey)) return false;
		Doc_trasporto_rientro_respintoKey k = (Doc_trasporto_rientro_respintoKey) o;
		if (!compareKey(getId(), k.getId())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getId());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Identificativo univoco del record]
	 **/
	public void setId(Long id)  {
		this.id=id;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Identificativo univoco del record]
	 **/
	public Long getId() {
		return id;
	}
}