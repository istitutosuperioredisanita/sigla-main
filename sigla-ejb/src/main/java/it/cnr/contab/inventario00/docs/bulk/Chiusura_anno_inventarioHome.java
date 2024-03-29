/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class Chiusura_anno_inventarioHome extends BulkHome {
	public Chiusura_anno_inventarioHome(Connection conn) {
		super(Chiusura_anno_inventarioBulk.class, conn);
	}
	public Chiusura_anno_inventarioHome(Connection conn, PersistentCache persistentCache) {
		super(Chiusura_anno_inventarioBulk.class, conn, persistentCache);
	}
}