/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 14/03/2025
 */
package it.cnr.contab.config00.pdcep.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class TipoBilancioHome extends BulkHome {
	public TipoBilancioHome(Connection conn) {
		super(TipoBilancioBulk.class, conn);
	}
	public TipoBilancioHome(Connection conn, PersistentCache persistentCache) {
		super(TipoBilancioBulk.class, conn, persistentCache);
	}

}