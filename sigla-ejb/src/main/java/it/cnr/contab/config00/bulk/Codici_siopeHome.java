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
 * Created by Aurelio's BulkGenerator 1.0
 * Date 23/04/2007
 */
package it.cnr.contab.config00.bulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
import it.cnr.contab.config00.pdcep.cla.bulk.V_classificazione_voci_epBulk;
import it.cnr.contab.config00.pdcep.cla.bulk.V_classificazione_voci_epHome;
import it.cnr.contab.config00.pdcfin.cla.bulk.V_classificazione_vociHome;
import it.cnr.contab.ordmag.anag00.AssUnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
public class Codici_siopeHome extends BulkHome {
	public Codici_siopeHome(Connection conn) {
		super(Codici_siopeBulk.class, conn);
	}
	public Codici_siopeHome(Connection conn, PersistentCache persistentCache) {
		super(Codici_siopeBulk.class, conn, persistentCache);
	} 

	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
	    sql.addClause("AND","esercizio",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		return sql;
	}

    public SQLBuilder selectV_classificazione_voci_ep_siopeByClause(CNRUserContext usercontext, Codici_siopeBulk codiciSiopeBulk, V_classificazione_voci_epHome classHome,
                                                                    V_classificazione_voci_epBulk classBulk, CompoundFindClause compoundfindclause) throws PersistencyException{
        SQLBuilder sql = classHome.createSQLBuilder();
        sql.addClause(FindClause.OR, "tipo", SQLBuilder.EQUALS, Voce_epHome.SIOPE);
        sql.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(usercontext));

        sql.addClause(FindClause.AND, "fl_mastrino", SQLBuilder.EQUALS, Boolean.TRUE);

        if (compoundfindclause != null)
            sql.addClause(compoundfindclause);
        return sql;
    }

    public SQLBuilder selectV_classificazione_voci_ep_siope_rendByClause(CNRUserContext usercontext, Codici_siopeBulk codiciSiopeBulk, V_classificazione_voci_epHome classHome,
                                                                    V_classificazione_voci_epBulk classBulk, CompoundFindClause compoundfindclause) throws PersistencyException{
        SQLBuilder sql = classHome.createSQLBuilder();
        sql.addClause(FindClause.OR, "tipo", SQLBuilder.EQUALS, Voce_epHome.SIOPE_RENDICONTO);
        sql.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(usercontext));

        sql.addClause(FindClause.AND, "fl_mastrino", SQLBuilder.EQUALS, Boolean.TRUE);

        if (compoundfindclause != null)
            sql.addClause(compoundfindclause);
        return sql;
    }
}