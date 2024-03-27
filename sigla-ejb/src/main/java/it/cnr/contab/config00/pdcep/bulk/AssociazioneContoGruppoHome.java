/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.config00.pdcep.bulk;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import javax.ejb.EJBException;

public class AssociazioneContoGruppoHome extends BulkHome {

	public static final String CD_VOCE_EP = "CD_VOCE_EP";

	public AssociazioneContoGruppoHome(Connection conn) {
		super(AssociazioneContoGruppoBulk.class, conn);
	}
	public AssociazioneContoGruppoHome(Connection conn, PersistentCache persistentCache) {
		super(AssociazioneContoGruppoBulk.class, conn, persistentCache);
	}

	@Override
	public Persistent findByPrimaryKey(Object obj) throws PersistencyException {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		Optional.ofNullable(obj)
				.filter(AssociazioneContoGruppoBulk.class::isInstance)
				.map(AssociazioneContoGruppoBulk.class::cast)
				.ifPresent(acg -> {
					sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, acg.getEsercizio());
					sqlBuilder.addClause(FindClause.AND, "cdPianoGruppi", SQLBuilder.EQUALS, acg.getCdPianoGruppi());
					sqlBuilder.addClause(FindClause.AND, "cdGruppoEp", SQLBuilder.EQUALS, acg.getCdGruppoEp());
					sqlBuilder.addClause(FindClause.AND, "cdVoceEp", SQLBuilder.EQUALS, acg.getCdVoceEp());
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

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		final CompoundFindClause compoundFindClause = Optional.ofNullable(compoundfindclause)
				.orElseGet(() -> new CompoundFindClause());
		compoundFindClause.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		return super.selectByClause(usercontext, compoundFindClause);
	}

	public SQLBuilder selectAssociazioniMultiple(UserContext usercontext, AssociazioneContoGruppoBulk associazioneContoGruppoBulk, CompoundFindClause compoundfindclause) throws PersistencyException {
		final SQLBuilder sqlBuilder = selectByClause(usercontext, compoundfindclause);
		final SQLBuilder sqlBuilderIN = createSQLBuilder();
		sqlBuilderIN.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		sqlBuilderIN.resetColumns();
		sqlBuilderIN.addColumn(CD_VOCE_EP);
		sqlBuilderIN.addSQLGroupBy(CD_VOCE_EP);
		sqlBuilderIN.addSQLHaving("COUNT(1) > 1");
		sqlBuilder.addSQLINClause(FindClause.AND, CD_VOCE_EP, sqlBuilderIN);
		sqlBuilder.addOrderBy(CD_VOCE_EP);
		return sqlBuilder;
	}

	public SQLBuilder selectVoceEpByClause(it.cnr.jada.UserContext userContext, AssociazioneContoGruppoBulk associazioneContoGruppoBulk, Voce_epHome voceEpHome, Voce_epBulk voceEpBulk, CompoundFindClause clause) throws ComponentException, EJBException, RemoteException, PersistencyException {
		return voceEpHome.selectByClause(userContext, clause);
	}

	public java.util.Collection findGruppoEp(AssociazioneContoGruppoBulk associazioneContoGruppoBulk,
			GruppoEPHome gruppoEPHome,
			GruppoEPBulk gruppoEPBulk
	) throws PersistencyException {
		gruppoEPHome = Optional.ofNullable(gruppoEPHome)
				.orElseGet(() -> (GruppoEPHome)getHomeCache().getHome(GruppoEPBulk.class));
		final SQLBuilder sqlBuilder = gruppoEPHome.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "cdPianoGruppi", SQLBuilder.EQUALS, associazioneContoGruppoBulk.getCdPianoGruppi());
		final List<GruppoEPBulk> result = gruppoEPHome.fetchAll(sqlBuilder);
		return result.stream().map(GruppoEPBase::getCdGruppoEp).collect(Collectors.toList());
	}

}