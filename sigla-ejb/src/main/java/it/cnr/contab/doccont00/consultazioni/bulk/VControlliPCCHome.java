/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/05/2024
 */
package it.cnr.contab.doccont00.consultazioni.bulk;
import java.sql.Connection;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class VControlliPCCHome extends BulkHome {
	public VControlliPCCHome(Connection conn) {
		super(VControlliPCCBulk.class, conn);
	}
	public VControlliPCCHome(Connection conn, PersistentCache persistentCache) {
		super(VControlliPCCBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sqlBuilder = super.selectByClause(usercontext, compoundfindclause);
		//	Se uo 999.000 in scrivania: visualizza tutto l'elenco
		Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHomeCache().getHome(Unita_organizzativa_enteBulk.class).findAll().get(0);
		if (!((CNRUserContext) usercontext).getCd_unita_organizzativa().equals( ente.getCd_unita_organizzativa())){
			sqlBuilder.openParenthesis(FindClause.AND);
				sqlBuilder.openParenthesis(FindClause.AND);
					sqlBuilder.addClause(FindClause.AND,"cdUoCUU",SQLBuilder.ISNULL,null);
					sqlBuilder.addClause(FindClause.AND,"cdUnitaOrganizzativa",SQLBuilder.EQUALS,CNRUserContext.getCd_unita_organizzativa(usercontext));
				sqlBuilder.closeParenthesis();
				sqlBuilder.addClause(FindClause.OR,"cdUoCUU",SQLBuilder.EQUALS,CNRUserContext.getCd_unita_organizzativa(usercontext));
			sqlBuilder.closeParenthesis();
		}
		return sqlBuilder;
	}
}