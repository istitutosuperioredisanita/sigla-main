/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/09/2021
 */
package it.cnr.contab.doccont00.core.bulk;
import java.sql.Connection;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderConstants;

public class Obbligazione_pluriennaleHome extends BulkHome {
	public Obbligazione_pluriennaleHome(Connection conn) {
		super(Obbligazione_pluriennaleBulk.class, conn);
	}
	public Obbligazione_pluriennaleHome(Connection conn, PersistentCache persistentCache) {
		super(Obbligazione_pluriennaleBulk.class, conn, persistentCache);
	}


	public java.util.Collection findObbligazioniPluriennaliVoce(it.cnr.jada.UserContext userContext, Obbligazione_pluriennaleBulk bulk) throws IntrospectionException, PersistencyException {
		PersistentHome dettHome = getHomeCache().getHome(Obbligazione_pluriennale_voceBulk.class);
		SQLBuilder sql = dettHome.createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, bulk.getPgObbligazione());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());

		sql.setOrderBy("ANNO", OrderConstants.ORDER_DESC);
		return dettHome.fetchAll(sql);
	}
}