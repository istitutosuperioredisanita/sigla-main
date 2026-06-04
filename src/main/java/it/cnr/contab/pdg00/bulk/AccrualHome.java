/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class AccrualHome extends BulkHome {
	public AccrualHome(Connection conn) {
		super(AccrualBulk.class, conn);
	}
	public AccrualHome(Connection conn, PersistentCache persistentCache) {
		super(AccrualBulk.class, conn, persistentCache);
	}
}