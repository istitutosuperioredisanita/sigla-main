/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/01/2025
 */
package it.cnr.contab.config00.bulk;
import java.sql.Connection;
import java.util.Optional;

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class AssCausaleVoceEPHome extends BulkHome {
	public AssCausaleVoceEPHome(Connection conn) {
		super(AssCausaleVoceEPBulk.class, conn);
	}
	public AssCausaleVoceEPHome(Connection conn, PersistentCache persistentCache) {
		super(AssCausaleVoceEPBulk.class, conn, persistentCache);
	}

	public SQLBuilder selectVoceEpByClause(
			CNRUserContext cnrUserContext,
			AssCausaleVoceEPBulk assCausaleVoceEPBulk,
			Voce_epHome voceEpHome,
			Voce_epBulk voceEpBulk,
			CompoundFindClause compoundFindClause) {
		SQLBuilder sqlBuilder = voceEpHome.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, cnrUserContext.getEsercizio());
		Optional.ofNullable(compoundFindClause)
				.ifPresent(sqlBuilder::addClause);
		return sqlBuilder;
	}
}