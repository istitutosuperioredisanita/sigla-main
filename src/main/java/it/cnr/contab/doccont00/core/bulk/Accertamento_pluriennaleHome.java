/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/09/2021
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class Accertamento_pluriennaleHome extends BulkHome {
	public Accertamento_pluriennaleHome(Connection conn) {
		super(Accertamento_pluriennaleBulk.class, conn);
	}
	public Accertamento_pluriennaleHome(Connection conn, PersistentCache persistentCache) {
		super(Accertamento_pluriennaleBulk.class, conn, persistentCache);
	}

	public List<Accertamento_pluriennale_voceBulk> findAccertamentiPluriennaliVoce(it.cnr.jada.UserContext userContext, Accertamento_pluriennaleBulk bulk) throws PersistencyException {
		PersistentHome pluriennaleVoceHome = getHomeCache().getHome(Accertamento_pluriennale_voceBulk.class);
		SQLBuilder sql = pluriennaleVoceHome.createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_ACCERTAMENTO", SQLBuilder.EQUALS, bulk.getPgAccertamento());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());

		return pluriennaleVoceHome.fetchAll(sql);
	}
}