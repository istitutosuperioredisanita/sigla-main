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

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

/**
 * Home per la consultazione dei saldi residui di spesa con aggregazioni.
 * Costruisce una query con GROUP BY dalla view V_CONS_SALDI_RESIDUI_SPE_DET
 */
public class V_cons_dett_res_speHome extends BulkHome {

    public V_cons_dett_res_speHome(Connection conn) {
        super(V_cons_dett_res_speBulk.class, conn);
    }

    public V_cons_dett_res_speHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_dett_res_speBulk.class, conn, persistentCache);
    }

    /**
     * Override per costruire query con GROUP BY e aggregazioni
     */
    @Override
    public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
            throws PersistencyException {

        // Usa il metodo della classe base per creare il SQLBuilder
        SQLBuilder sql = createSQLBuilder();

        // Reset colonne per costruzione custom
        sql.resetColumns();

        // Costruisci SELECT con aggregazioni e GROUP BY
        buildAggregatedSelect(sql);

        // Aggiungo il filtro per esercizio
        esercizioWhereClause(sql, usercontext);

        // Applica clausole WHERE se presenti
        if (compoundfindclause != null) {
            sql.addClause(compoundfindclause);
        }

        return sql;
    }

    /**
     * Applica il filtro su esercizio del contesto
     */
    private void esercizioWhereClause(SQLBuilder sql, UserContext usercontext)
            throws PersistencyException {

        // Filtro per esercizio corrente
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS,
                CNRUserContext.getEsercizio(usercontext));
    }

    /**
     * Costruisce la SELECT con colonne aggregate e GROUP BY
     */
    private void buildAggregatedSelect(SQLBuilder sql) {
        // Colonne con GROUP BY
        addColumnWithGroupBy(sql, "ESERCIZIO");
        addColumnWithGroupBy(sql, "ESERCIZIO_RES");
        addColumnWithGroupBy(sql, "CD_CENTRO_RESPONSABILITA");
        addColumnWithGroupBy(sql, "TI_APPARTENENZA");
        addColumnWithGroupBy(sql, "TI_GESTIONE");
        addColumnWithGroupBy(sql, "CD_ELEMENTO_VOCE");
        addColumnWithGroupBy(sql, "CDS_OBB");
        addColumnWithGroupBy(sql, "PG_OBB");
        addColumnWithGroupBy(sql, "PG_OBB_SCAD");
        addColumnWithGroupBy(sql, "DS_SCADENZA");
        addColumnWithGroupBy(sql, "CD_TIPO_DOCUMENTO_CONT");

        // Colonne aggregate con MAX
        sql.addColumn("MAX(DS_ELEMENTO_VOCE)", "DESCRIZIONE_VOCE");
        sql.addColumn("MAX(DS_OBBLIGAZIONE)", "DS_OBBLIGAZIONE");

        // Colonna con LISTAGG
        sql.addColumn(
                "NVL(LISTAGG(DS_VAR_RES_PRO, ',') WITHIN GROUP (ORDER BY DS_VAR_RES_PRO), '')",
                "DESCRMODIFICA"
        );

        // Colonne aggregate con SUM
        sql.addColumn("SUM(VAR_PIU_OBB_RES_PRO)", "VAR_PIU_OBB_RES_PRO");
        sql.addColumn("SUM(VAR_MENO_OBB_RES_PRO)", "VAR_MENO_OBB_RES_PRO");
        sql.addColumn("SUM(IM_OBBL_RES_IMP)", "IM_IMPROPRIO");
        sql.addColumn("SUM(IM_OBBL_RES_PRO)", "IM_PROPRIO");
        sql.addColumn("SUM(PAGATO_RES_PROPRIO)", "PAGATO_RES_PROPRIO");
        sql.addColumn("SUM(PAGATO_RES_IMPROPRIO)", "PAGATO_RES_IMPROPRIO");
        sql.addColumn("SUM(IMP_DOC_RES_PROPRIO)", "IMP_DOC_RES_PROPRIO");
        sql.addColumn("SUM(IMP_DOC_RES_IMPROPRIO)", "IMP_DOC_RES_IMPROPRIO");
    }

    /**
     * Aggiunge una colonna e la relativa clausola GROUP BY
     */
    private void addColumnWithGroupBy(SQLBuilder sql, String columnName) {
        sql.addColumn(columnName);
        sql.addSQLGroupBy(columnName.toLowerCase());
    }
}