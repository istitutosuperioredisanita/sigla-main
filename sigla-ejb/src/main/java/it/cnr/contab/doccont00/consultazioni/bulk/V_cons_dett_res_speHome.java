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

import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

/**
 * Home per la consultazione dei saldi residui di spesa con aggregazioni.
 * Costruisce una query con GROUP BY dalla view V_CONS_SALDI_RESIDUI_SPE_DET
 */
public class V_cons_dett_res_speHome extends AbstractConsAggregatedHome {

    public V_cons_dett_res_speHome(Connection conn) {
        super(V_cons_dett_res_speBulk.class, conn);
    }

    public V_cons_dett_res_speHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_dett_res_speBulk.class, conn, persistentCache);
    }

    /**
     * Costruisce la SELECT con colonne aggregate e GROUP BY specifiche per le obbligazioni
     */
    @Override
    protected void buildAggregatedSelect(SQLBuilder sql) {
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
}