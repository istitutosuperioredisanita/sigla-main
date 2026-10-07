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
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/09/2018
 */
package it.cnr.contab.progettiric00.core.bulk;

import it.cnr.contab.config00.bulk.Configurazione_cnrBase;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.ColumnMapping;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.*;

public class TipoFinanziamentoHome extends BulkHome {
    public TipoFinanziamentoHome(Connection conn) {
        super(TipoFinanziamentoBulk.class, conn);
    }

    public TipoFinanziamentoHome(Connection conn, PersistentCache persistentCache) {
        super(TipoFinanziamentoBulk.class, conn, persistentCache);
    }

    public void initializePrimaryKeyForInsert(UserContext usercontext, OggettoBulk oggettobulk)
            throws PersistencyException {
        try {
            Optional.ofNullable(oggettobulk)
                    .filter(TipoFinanziamentoBulk.class::isInstance)
                    .map(TipoFinanziamentoBulk.class::cast)
                    .ifPresent(tipoFinanziamentoBulk -> {
                        try {
                            tipoFinanziamentoBulk.setId(
                                    Optional.ofNullable(findAndLockMax(tipoFinanziamentoBulk, "id", 0))
                                        .filter(Long.class::isInstance)
                                        .map(Long.class::cast).map(aLong -> aLong + 1)
                                        .orElse(null)
                            );
                        } catch (PersistencyException|BusyResourceException e) {
                           throw new DetailedRuntimeException(e);
                        }
                    });
        } catch (DetailedRuntimeException detailedRuntimeException) {
            throw new PersistencyException(detailedRuntimeException);
        }
    }

    public List<TipoFinanziamentoBulk> findFondiFunzionamento(UserContext userContext, Integer esercizio, String uo) throws ComponentException, PersistencyException {
        final Configurazione_cnrBulk configurazioneCnrBulk = new Configurazione_cnrBulk(
                "FONDI_FUNZIONAMENTO",
                "PARAMETRI",
                "*",
                esercizio);
        Configurazione_cnrHome home = (it.cnr.contab.config00.bulk.Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
        Configurazione_cnrBulk config = Optional.ofNullable(home.findByPrimaryKey(configurazioneCnrBulk))
                .map(Configurazione_cnrBulk.class::cast)
                .orElseGet(() -> {
                    configurazioneCnrBulk.setEsercizio(0);
                    try {
                        return (Configurazione_cnrBulk)home.findByPrimaryKey(configurazioneCnrBulk);
                    } catch (PersistencyException e) {
                        throw new RuntimeException(e);
                    }
                });

        setColumnMap("FONDI_FUNZIONAMENTO");
        SQLBuilder sqlBuilder = super.createSQLBuilder();
        sqlBuilder.addTableToHeader("V_PROGETTO_PADRE");
        sqlBuilder.addTableToHeader("V_SALDI_PIANO_ECONOM_PROGETTO");
        sqlBuilder.addSQLJoin("TIPO_FINANZIAMENTO.ID", "V_PROGETTO_PADRE.ID_TIPO_FINANZIAMENTO");
        sqlBuilder.addSQLJoin("V_PROGETTO_PADRE.ESERCIZIO", "V_SALDI_PIANO_ECONOM_PROGETTO.ESERCIZIO");
        sqlBuilder.addSQLJoin("V_PROGETTO_PADRE.PG_PROGETTO", "V_SALDI_PIANO_ECONOM_PROGETTO.PG_PROGETTO");

        sqlBuilder.addSQLClause(FindClause.AND, "V_PROGETTO_PADRE.ESERCIZIO", SQLBuilder.EQUALS, esercizio);
        sqlBuilder.addSQLClause(FindClause.AND, "V_PROGETTO_PADRE.TIPO_FASE", SQLBuilder.EQUALS, ProgettoGestUoBulk.TIPO_FASE_NON_DEFINITA);

        Optional.ofNullable(config)
                .map(Configurazione_cnrBase::getVal03)
                .map(s -> s.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .forEach(s -> {
                    sqlBuilder.addSQLClause(FindClause.AND, "V_PROGETTO_PADRE.CD_TIPO_PROGETTO", SQLBuilder.NOT_EQUALS, s);
                });
        Optional.ofNullable(uo)
                .ifPresent(s -> {
                    sqlBuilder.addSQLClause(FindClause.AND, "V_PROGETTO_PADRE.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, s);
                });

        Collection<ColumnMapping> columnMappings = getColumnMap().getColumnMappings();
        columnMappings
                .stream()
                .filter(columnMapping -> !columnMapping.isCount())
                .map(ColumnMapping::getColumnName)
                .map(s -> "TIPO_FINANZIAMENTO.".concat(s))
                .forEach(sqlBuilder::addSQLGroupBy);
        return fetchAll(sqlBuilder);
    }

}
