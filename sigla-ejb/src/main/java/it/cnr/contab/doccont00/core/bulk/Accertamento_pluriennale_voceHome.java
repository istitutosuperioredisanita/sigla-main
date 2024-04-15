/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 03/01/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class Accertamento_pluriennale_voceHome extends BulkHome {
	public Accertamento_pluriennale_voceHome(Connection conn) {
		super(Accertamento_pluriennale_voceBulk.class, conn);
	}
	public Accertamento_pluriennale_voceHome(Connection conn, PersistentCache persistentCache) {
		super(Accertamento_pluriennale_voceBulk.class, conn, persistentCache);
	}
}