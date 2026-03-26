/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2026
 */
package it.cnr.contab.config00.pdcep.bulk;
import java.sql.Connection;
import java.util.Optional;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class BilRiclassificatoHome extends BulkHome {
	public BilRiclassificatoHome(Connection conn) {
		super(BilRiclassificatoBulk.class, conn);
	}
	public BilRiclassificatoHome(Connection conn, PersistentCache persistentCache) {
		super(BilRiclassificatoBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		CompoundFindClause compoundFindClause = Optional.ofNullable(compoundfindclause).orElseGet(CompoundFindClause::new);
		compoundFindClause.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		compoundFindClause.addClause(FindClause.AND, "cdUnitaOrganizzativa", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(usercontext));
		return super.selectByClause(usercontext, compoundFindClause);
	}
}