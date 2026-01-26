/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 13/01/2026
 */
package it.cnr.contab.doccont00.core.bulk;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class ConsIndicatorePagamentiHome extends BulkHome {
	public ConsIndicatorePagamentiHome(Connection conn) {
		super(ConsIndicatorePagamentiBulk.class, conn);
	}
	public ConsIndicatorePagamentiHome(Connection conn, PersistentCache persistentCache) {
		super(ConsIndicatorePagamentiBulk.class, conn, persistentCache);
	}

	public List<ConsIndicatorePagamentiBulk> findRiepilogo(UserContext userContext, ConsIndicatorePagamentiBulk consIndicatorePagamentiBulk, Integer esercizio, String uo) throws ComponentException, PersistencyException {
		SQLBuilder sql = createSQLBuilder();
		Optional.ofNullable(consIndicatorePagamentiBulk)
				.ifPresent(bulk -> sql.addClause(bulk.buildFindClauses(true)));
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
		if (uo != null)
			sql.addClause(FindClause.AND, "uoDocumento", SQLBuilder.EQUALS, uo);
		else {
			sql.addClause(FindClause.AND, "uoDocumento", SQLBuilder.ISNULL, null);
		}
		sql.addClause(FindClause.AND, "tipoRiga", SQLBuilder.NOT_EQUALS, "DETAIL");
		return fetchAll(sql);
	}
}
