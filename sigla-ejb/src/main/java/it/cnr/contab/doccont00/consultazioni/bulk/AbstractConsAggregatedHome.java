/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

/**
 * Classe base astratta per le consultazioni con aggregazioni.
 */
public abstract class AbstractConsAggregatedHome extends BulkHome {

    public AbstractConsAggregatedHome(Class bulkClass, Connection conn) {
        super(bulkClass, conn);
    }

    public AbstractConsAggregatedHome(Class bulkClass, Connection conn, PersistentCache persistentCache) {
        super(bulkClass, conn, persistentCache);
    }

    /**
     * Override per costruire query con GROUP BY e aggregazioni
     */
    @Override
    public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
            throws PersistencyException {

        SQLBuilder sql = createSQLBuilder();

        // Reset colonne
        sql.resetColumns();

        // Costruisci SELECT con aggregazioni e GROUP BY (metodo astratto)
        buildAggregatedSelect(sql);

        // Filtro per esercizio corrente
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));

        // Applica clausole WHERE se presenti
        if (compoundfindclause != null) {
            sql.addClause(compoundfindclause);
        }

        return sql;
    }

    /**
     * Metodo astratto da implementare nelle sottoclassi home (acc/obb) per definire
     * le colonne specifiche da aggregare
     */
    protected abstract void buildAggregatedSelect(SQLBuilder sql);

    /**
     * aggiunge una colonna e la relativa clausola GROUP BY
     */
    protected void addColumnWithGroupBy(SQLBuilder sql, String columnName) {
        sql.addColumn(columnName);
        sql.addSQLGroupBy(columnName.toLowerCase());
    }
}