/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class Ass_tipo_amm_cat_grup_invHome extends BulkHome {
	public Ass_tipo_amm_cat_grup_invHome(Connection conn) {
		super(Ass_tipo_amm_cat_grup_invBulk.class, conn);
	}
	public Ass_tipo_amm_cat_grup_invHome(Connection conn, PersistentCache persistentCache) {
		super(Ass_tipo_amm_cat_grup_invBulk.class, conn, persistentCache);
	}
}