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

/*
* Created by Generator 1.0
* Date 30/08/2005
*/
package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.rmi.RemoteException;

public class V_ass_inv_bene_fatturaHome extends BulkHome {
	public V_ass_inv_bene_fatturaHome(java.sql.Connection conn) {
		super(V_ass_inv_bene_fatturaBulk.class, conn);
	}
	public V_ass_inv_bene_fatturaHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(V_ass_inv_bene_fatturaBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql =super.selectByClause(usercontext, compoundfindclause);
		String cds = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(usercontext);
		String uo = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(usercontext);
		Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(usercontext);
		it.cnr.contab.inventario00.ejb.IdInventarioComponentSession h = (it.cnr.contab.inventario00.ejb.IdInventarioComponentSession)
				it.cnr.jada.util.ejb.EJBCommonServices.createEJB(
						"CNRINVENTARIO00_EJB_IdInventarioComponentSession",
						it.cnr.contab.inventario00.ejb.IdInventarioComponentSession.class);

		try {
			sql.addSQLClause("AND","esercizio",sql.EQUALS,esercizio);
			if(!Utility.createCdrComponentSession().isEnte(usercontext)){
				h.findInventarioFor(usercontext,cds,uo,false);
				sql.addSQLClause("AND", "pg_inventario", sql.EQUALS, h.findInventarioFor(usercontext,cds,uo,false).getPg_inventario());
				sql.openParenthesis( FindClause.AND.concat(" ("));
					sql.addSQLClause("AND", "esercizio_fatt_pass", sql.EQUALS, esercizio);
					sql.addSQLClause("AND", "cd_cds_fatt_pass", sql.EQUALS, cds);
				sql.closeParenthesis();
				sql.addSQLClause("OR", "esercizio_fatt_pass", sql.ISNULL,null);
				sql.closeParenthesis();
			}
			return sql;
		} catch (ComponentException e) {
			throw new RuntimeException(e);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
}