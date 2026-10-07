/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 02/07/2026
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class Progetto_padre_detHome extends BulkHome {
	public Progetto_padre_detHome(Connection conn) {
		super(Progetto_padre_detBulk.class, conn);
	}
	public Progetto_padre_detHome(Connection conn, PersistentCache persistentCache) {
		super(Progetto_padre_detBulk.class, conn, persistentCache);
	}
}