/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 16/06/2025
 */
package it.cnr.contab.docamm00.consultazioni.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;

import java.sql.Connection;
public class V_confronta_costi_coge_cofiHome extends BulkHome {
	public V_confronta_costi_coge_cofiHome(Connection conn) {
		super(V_confronta_costi_coge_cofiBulk.class, conn);
	}
	public V_confronta_costi_coge_cofiHome(Connection conn, PersistentCache persistentCache) {
		super(V_confronta_costi_coge_cofiBulk.class, conn, persistentCache);
	}
}