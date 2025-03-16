/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.config00.pdcep.bulk;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class GruppoEPHome extends BulkHome {
	public GruppoEPHome(Connection conn) {
		super(GruppoEPBulk.class, conn);
	}
	public GruppoEPHome(Connection conn, PersistentCache persistentCache) {
		super(GruppoEPBulk.class, conn, persistentCache);
	}

	@Override
	public Persistent findByPrimaryKey(Object obj) throws PersistencyException {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		Optional.ofNullable(obj)
				.filter(GruppoEPBulk.class::isInstance)
				.map(GruppoEPBulk.class::cast)
				.ifPresent(acg -> {
					sqlBuilder.addClause(FindClause.AND, "cdTipoBilancio", SQLBuilder.EQUALS, acg.getCdTipoBilancio());
					sqlBuilder.addClause(FindClause.AND, "cdPianoGruppi", SQLBuilder.EQUALS, acg.getCdPianoGruppi());
					sqlBuilder.addClause(FindClause.AND, "cdGruppoEp", SQLBuilder.EQUALS, acg.getCdGruppoEp());
				});
		final Optional<Persistent> persistent = fetchAll(sqlBuilder)
				.stream()
				.filter(Persistent.class::isInstance)
				.map(Persistent.class::cast)
				.findAny();
		return persistent.orElse(null);
	}

	@Override
	public void insert(Persistent persistent, UserContext userContext) throws PersistencyException {
		setColumnMap("INSERT");
		super.insert(persistent, userContext);
	}

	public List<GruppoEPBulk> findChildren(UserContext userContext, GruppoEPBulk gruppoEPBulk) throws PersistencyException {
		return fetchAll(findChildren(userContext, gruppoEPBulk, null));
	}

	public SQLBuilder findChildren(UserContext userContext, GruppoEPBulk gruppoEPBulk, CompoundFindClause compoundfindclause) {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		Optional.ofNullable(gruppoEPBulk).ifPresent(gruppoEPBulk1 -> {
			sqlBuilder.addClause(FindClause.AND, "tipoBilancio", SQLBuilder.EQUALS, gruppoEPBulk1.getTipoBilancio());
		});
        if (Optional.ofNullable(gruppoEPBulk).filter(gruppoEPBulk1 -> Optional.ofNullable(gruppoEPBulk1.getCdGruppoEp()).isPresent()).isPresent()) {
			sqlBuilder.addClause(FindClause.AND, "cdPianoPadre", SQLBuilder.EQUALS, gruppoEPBulk.getCdPianoGruppi());
			sqlBuilder.addClause(FindClause.AND, "cdGruppoPadre", SQLBuilder.EQUALS, gruppoEPBulk.getCdGruppoEp());
		} else {
			sqlBuilder.addClause(FindClause.AND, "cdPianoPadre", SQLBuilder.ISNULL, null);
			sqlBuilder.addClause(FindClause.AND, "cdGruppoPadre", SQLBuilder.ISNULL, null);
		}
		return sqlBuilder;
	}

	public List<GruppoEPBulk> findParents(UserContext userContext, GruppoEPBulk gruppoEPBulk) throws PersistencyException {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "cdPianoGruppi", SQLBuilder.EQUALS, gruppoEPBulk.getCdPianoPadre());
		sqlBuilder.addClause(FindClause.AND, "cdGruppoEp", SQLBuilder.EQUALS, gruppoEPBulk.getCdGruppoPadre());
		return fetchAll(sqlBuilder);
	}
 }