/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class V_ammortamento_beni_detKey extends OggettoBulk implements KeyedPersistent {

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_AMMORTAMENTO_BENI_DET
	 **/
	public V_ammortamento_beni_detKey() {
		super();
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof V_ammortamento_beni_detKey)) return false;
		V_ammortamento_beni_detKey k = (V_ammortamento_beni_detKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}
}