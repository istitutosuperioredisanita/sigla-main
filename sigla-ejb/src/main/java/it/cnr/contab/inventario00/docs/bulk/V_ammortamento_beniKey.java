/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class V_ammortamento_beniKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_AMMORTAMENTO_BENI
	 **/
	public V_ammortamento_beniKey() {
		super();
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof V_ammortamento_beniKey)) return false;
		V_ammortamento_beniKey k = (V_ammortamento_beniKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}
}