/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 02/07/2026
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Progetto_padre_detKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_PROGETTO_PADRE_DET
	 **/
	public Progetto_padre_detKey() {
		super();
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Progetto_padre_detKey)) return false;
		Progetto_padre_detKey k = (Progetto_padre_detKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}
}