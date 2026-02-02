/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/05/2024
 */
package it.cnr.contab.doccont00.consultazioni.bulk;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.doccont00.comp.DateServices;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.*;

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

	public List<VControlliPCCBulk> findRiepilogoPerStruttura(UserContext userContext, VControlliPCCBulk vControlliPCCBulk, Integer esercizio) throws ComponentException, PersistencyException {
		setColumnMap("RIEPILOGO_STRUTTURA");
		SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.addTableToHeader("UNITA_ORGANIZZATIVA");
		sqlBuilder.addTableToHeader("UNITA_ORGANIZZATIVA CDS");
		sqlBuilder.addSQLJoin("NVL(V_CONTROLLI_PCC.CD_UNITA_ORGANIZZATIVA, V_CONTROLLI_PCC.CD_UO_CUU)", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");
		sqlBuilder.addSQLJoin("UNITA_ORGANIZZATIVA.CD_UNITA_PADRE", "CDS.CD_UNITA_ORGANIZZATIVA");
		sqlBuilder.addSQLBetweenClause(FindClause.AND, "V_CONTROLLI_PCC.DATA_RICEZIONE", DateServices.getFirstDayOfYear(esercizio), DateServices.getLastDayOfYear(esercizio));
		Collection<ColumnMapping> columnMappings = getColumnMap().getColumnMappings();
		columnMappings
				.stream()
				.filter(columnMapping -> !columnMapping.isCount())
				.map(ColumnMapping::getColumnName)
				.map(s -> s.substring(0, s.lastIndexOf(" ")))
				.forEach(sqlBuilder::addSQLGroupBy);
		return fetchAll(sqlBuilder);
	}

	public List<VControlliPCCBulk> findRiepilogoPerStato(UserContext userContext, VControlliPCCBulk vControlliPCCBulk, String uo) throws ComponentException, PersistencyException {
		setColumnMap("RIEPILOGO_STATO");
		SQLBuilder sqlBuilder = createSQLBuilder();
		Optional.ofNullable(uo).ifPresent(codice -> {
			sqlBuilder.openParenthesis(FindClause.AND);
			sqlBuilder.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, codice);
			sqlBuilder.addSQLClause(FindClause.OR, "CD_UO_CUU", SQLBuilder.EQUALS, codice);
			sqlBuilder.closeParenthesis();

		});
		Collection<ColumnMapping> columnMappings = getColumnMap().getColumnMappings();
		columnMappings
				.stream()
				.filter(columnMapping -> !columnMapping.isCount())
				.map(ColumnMapping::getColumnName)
				.map(s -> s.substring(0, s.lastIndexOf(" ")))
				.forEach(sqlBuilder::addSQLGroupBy);
		return fetchAll(sqlBuilder);
	}

}