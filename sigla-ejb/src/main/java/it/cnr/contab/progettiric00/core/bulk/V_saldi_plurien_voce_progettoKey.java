/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/07/2024
 */
package it.cnr.contab.progettiric00.core.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class V_saldi_plurien_voce_progettoKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_SALDI_PLURIENNALI_VOCE_PROGETTO
	 **/
	public V_saldi_plurien_voce_progettoKey() {
		super();
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof V_saldi_plurien_voce_progettoKey)) return false;
		V_saldi_plurien_voce_progettoKey k = (V_saldi_plurien_voce_progettoKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}
}