/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/04/2024
 */
package it.cnr.contab.pdg00.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderConstants;

public class VpgBilRiclassificatoHome extends BulkHome {
	public VpgBilRiclassificatoHome(Connection conn) {
		super(VpgBilRiclassificatoBulk.class, conn);
	}
	public VpgBilRiclassificatoHome(Connection conn, PersistentCache persistentCache) {
		super(VpgBilRiclassificatoBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder createSQLBuilder() {
		final SQLBuilder sqlBuilder = super.createSQLBuilder();
		sqlBuilder.setOrderBy("SEQUENZA", OrderConstants.ORDER_ASC);
		return sqlBuilder;
	}
}