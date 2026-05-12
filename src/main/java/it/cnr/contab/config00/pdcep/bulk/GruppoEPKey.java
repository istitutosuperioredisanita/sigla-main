/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class GruppoEPKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_GRUPPO_EP
	 **/
	private String rowid;
	public GruppoEPKey() {
		super();
	}
	public GruppoEPKey(String rowid) {
		super();
		this.rowid=rowid;
	}

	public String getRowid() {
		return rowid;
	}

	public void setRowid(String rowid) {
		this.rowid = rowid;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof GruppoEPKey)) return false;
		GruppoEPKey k = (GruppoEPKey) o;
		if (!compareKey(getRowid(), k.getRowid())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getRowid());
		return i;
	}


}