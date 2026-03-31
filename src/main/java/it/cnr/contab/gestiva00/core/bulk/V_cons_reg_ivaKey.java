/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/02/2025
 */
package it.cnr.contab.gestiva00.core.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class V_cons_reg_ivaKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONS_REG_IVA
	 **/
	public V_cons_reg_ivaKey() {
		super();
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof V_cons_reg_ivaKey)) return false;
		V_cons_reg_ivaKey k = (V_cons_reg_ivaKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}
}