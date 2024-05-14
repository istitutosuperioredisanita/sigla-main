/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/09/2021
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class Obbligazione_pluriennaleHome extends BulkHome {
	public Obbligazione_pluriennaleHome(Connection conn) {
		super(Obbligazione_pluriennaleBulk.class, conn);
	}
	public Obbligazione_pluriennaleHome(Connection conn, PersistentCache persistentCache) {
		super(Obbligazione_pluriennaleBulk.class, conn, persistentCache);
	}

	public List<Obbligazione_pluriennale_voceBulk> findObbligazioniPluriennaliVoce(it.cnr.jada.UserContext userContext, Obbligazione_pluriennaleBulk bulk) throws  PersistencyException {
		PersistentHome pluriennaleVoceHome = getHomeCache().getHome(Obbligazione_pluriennale_voceBulk.class);
		SQLBuilder sql = pluriennaleVoceHome.createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, bulk.getPgObbligazione());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());

		return pluriennaleVoceHome.fetchAll(sql);
	}
	public Obbligazione_pluriennaleBulk findObbligazioniPluriennale(it.cnr.jada.UserContext userContext, Obbligazione_pluriennaleBulk bulk) throws  PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, bulk.getPgObbligazione());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());

		return (Obbligazione_pluriennaleBulk) fetchAll(sql).get(0);
	}
}