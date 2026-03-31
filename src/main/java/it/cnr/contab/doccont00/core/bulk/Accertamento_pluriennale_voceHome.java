/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 03/01/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import java.sql.Connection;
import java.util.List;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderConstants;

public class Accertamento_pluriennale_voceHome extends BulkHome {
	public Accertamento_pluriennale_voceHome(Connection conn) {
		super(Accertamento_pluriennale_voceBulk.class, conn);
	}
	public Accertamento_pluriennale_voceHome(Connection conn, PersistentCache persistentCache) {
		super(Accertamento_pluriennale_voceBulk.class, conn, persistentCache);
	}

	public List<Accertamento_pluriennale_voceBulk> findAccertamentiPluriennaliVoce(it.cnr.jada.UserContext userContext, Accertamento_pluriennaleBulk bulk) throws IntrospectionException, PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", sql.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", sql.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_ACCERTAMENTO", sql.EQUALS, bulk.getPgAccertamento());
		sql.addSQLClause("AND", "ANNO", sql.EQUALS, bulk.getAnno());

		return this.fetchAll(sql);
	}
}