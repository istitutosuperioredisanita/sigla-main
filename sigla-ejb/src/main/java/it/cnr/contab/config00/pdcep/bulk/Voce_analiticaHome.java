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

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.*;

/**
 * Home che gestisce i capoconti e i conti.
 */
public class Voce_analiticaHome extends BulkHome {

    protected Voce_analiticaHome(Class clazz, java.sql.Connection connection) {
        super(clazz, connection);
    }

    protected Voce_analiticaHome(Class clazz, java.sql.Connection connection, PersistentCache persistentCache) {
        super(clazz, connection, persistentCache);
    }

    public Voce_analiticaHome(java.sql.Connection conn) {
        super(Voce_analiticaBulk.class, conn);
    }

    public Voce_analiticaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Voce_analiticaBulk.class, conn, persistentCache);
    }

    public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
        SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
        sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
        return sql;
    }

    public java.util.List findVoceAnaliticaList(ContoBulk contoBulk) throws PersistencyException {
        SQLBuilder sql = super.createSQLBuilder();
        sql.addClause(FindClause.AND, "esercizio_voce_ep", SQLBuilder.EQUALS, contoBulk.getEsercizio());
        sql.addClause(FindClause.AND, "cd_voce_ep", SQLBuilder.EQUALS, contoBulk.getCd_voce_ep());
        sql.addOrderBy("cd_voce_ep");
        return this.fetchAll(sql);
    }
}
