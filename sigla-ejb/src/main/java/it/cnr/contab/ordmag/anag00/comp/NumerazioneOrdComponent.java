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

package it.cnr.contab.ordmag.anag00.comp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.ordmag.anag00.NumerazioneOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.persistency.sql.SQLBuilder;

import javax.ejb.EJBException;
import java.io.Serializable;
import java.rmi.RemoteException;

public class NumerazioneOrdComponent extends CRUDComponent implements ICRUDMgr,Cloneable,Serializable{

	public SQLBuilder selectUnitaOperativaOrdByClause(it.cnr.jada.UserContext userContext, NumerazioneOrdBulk numerazioneOrdBulk,  UnitaOperativaOrdBulk unitaOperativaOrdBulk, CompoundFindClause clause)  throws ComponentException, EJBException, RemoteException {
		try {
			Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHome(userContext, Unita_organizzativaBulk.class).
					findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));
			SQLBuilder sql = getHome(userContext, UnitaOperativaOrdBulk.class).createSQLBuilder();
			if (!uo.isUoEnte())
				sql.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));
			sql.addClause(clause);
			return sql;
		}catch( Exception e )
		{
			throw handleException( e );
		}
	}
	protected Query select(UserContext userContext, CompoundFindClause clauses, OggettoBulk bulk)
			throws ComponentException, it.cnr.jada.persistency.PersistencyException {
		try {
			Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHome(userContext, Unita_organizzativaBulk.class).
					findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));
			SQLBuilder sql = (SQLBuilder)super.select(userContext,clauses, bulk);
			if (!uo.isUoEnte()) {
				sql.addTableToHeader("UNITA_OPERATIVA_ORD");
				sql.addSQLJoin("NUMERAZIONE_ORD.CD_UNITA_OPERATIVA", "UNITA_OPERATIVA_ORD.CD_UNITA_OPERATIVA");
				sql.addSQLClause(FindClause.AND, "UNITA_OPERATIVA_ORD.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));
			}
			sql.addClause(clauses);
			return sql;
		}catch( Exception e )
		{
			throw handleException( e );
		}

	}


}
