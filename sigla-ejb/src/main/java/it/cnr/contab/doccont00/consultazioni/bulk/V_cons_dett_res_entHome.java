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

import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

/**
 * Home per la consultazione dei saldi residui di entrata con aggregazioni.
 * Costruisce una query con GROUP BY dalla view V_CONS_SALDI_RESIDUI_ENT
 */
public class V_cons_dett_res_entHome extends AbstractConsAggregatedHome {

    public V_cons_dett_res_entHome(Connection conn) {
        super(V_cons_dett_res_entBulk.class, conn);
    }

    public V_cons_dett_res_entHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_dett_res_entBulk.class, conn, persistentCache);
    }

    /**
     * Costruisce la SELECT con colonne aggregate e GROUP BY specifiche per gli accertamenti
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
        addColumnWithGroupBy(sql, "CDS_ACCERTAMENTO");
        addColumnWithGroupBy(sql, "PG_ACCERTAMENTO");
        addColumnWithGroupBy(sql, "CD_TIPO_DOCUMENTO_CONT");

        // Colonne aggregate con MIN
        sql.addColumn("MIN(DS_ELEMENTO_VOCE)", "DESCRIZIONE_VOCE");
        sql.addColumn("MIN(DS_ACCERTAMENTO)", "DESCRIZIONE_ACCERTAMENTO");

        // Colonna con LISTAGG
        sql.addColumn(
                "NVL(LISTAGG(DS_VAR_RES_PRO, ',') WITHIN GROUP (ORDER BY DS_VAR_RES_PRO), '')",
                "DESCRMODIFICA"
        );

        // Colonne aggregate con SUM
        sql.addColumn("SUM(IM_OBBL_RES_PRO)", "IM_ACCERTAMENTO");
        sql.addColumn("SUM(VAR_PIU_ACC_RES_PRO)", "VARIAZIONI_PIU");
        sql.addColumn("SUM(VAR_MENO_ACC_RES_PRO)", "VARIAZIONI_MENO");
        sql.addColumn("SUM(RISCOSSO_RES_PROPRIO)", "IMPORTO_RISCOSSO");
    }
}