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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazioneMastrinoContoBP;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.Optional;

public class MastrinoContoHome extends Movimento_cogeHome {

    public MastrinoContoHome(Connection conn) {
        super(MastrinoContoBulk.class, conn);
    }

    public MastrinoContoHome(Connection conn, PersistentCache persistentCache) {
        super(MastrinoContoBulk.class, conn, persistentCache);
    }

    public SQLBuilder selectByClauseForMastrinoConto(
            UserContext usercontext,
            MastrinoContoBulk mastrinoContoBulk,
            CompoundFindClause compoundfindclause,
            Object... objects) {
        setColumnMap("MASTRINO_CONTO");

        Optional<ConsultazioneMastrinoContoBP.MapFilter>[] mapFilter = new Optional[]{Optional.empty()};
        Optional<ContoBulk>[] contiSelected = new Optional[]{Optional.empty()};
        Optional<ContoBulk>[] mastriniSelected = new Optional[]{Optional.empty()};

        for (Object obj : objects)
            Optional.ofNullable(obj)
                    .filter(ConsultazioneMastrinoContoBP.MapFilter.class::isInstance)
                    .map(ConsultazioneMastrinoContoBP.MapFilter.class::cast)
                    .ifPresent(el -> mapFilter[0] = Optional.of(el));

        SQLBuilder sqlBuilderDet = this.createSQLBuilder(usercontext, compoundfindclause, mapFilter[0]);
        SQLBuilder sqlBuilderTotUo = this.createSQLBuilder(usercontext, compoundfindclause, mapFilter[0]);
        SQLBuilder sqlBuilderTotAll = this.createSQLBuilder(usercontext, compoundfindclause, mapFilter[0]);

        sqlBuilderDet.setHeader("SELECT V_MASTRINO_CONTO.PG_SCRITTURA, " +
                "V_MASTRINO_CONTO.DT_CONTABILIZZAZIONE, " +
                "V_MASTRINO_CONTO.DS_SCRITTURA, " +
                "V_MASTRINO_CONTO.ESERCIZIO, " +
                "V_MASTRINO_CONTO.PG_MOVIMENTO, " +
                "V_MASTRINO_CONTO.CD_UNITA_ORGANIZZATIVA, " +
                "V_MASTRINO_CONTO.CD_CDS, " +
                "'D' CD_RIGA, " +
                "V_MASTRINO_CONTO.SEZIONE, " +
                "V_MASTRINO_CONTO.CD_VOCE_EP, " +
                "V_MASTRINO_CONTO.CD_TERZO, " +
                "V_MASTRINO_CONTO.TI_RIGA, " +
                "NVL(V_MASTRINO_CONTO.CD_CONTRIBUTO_RITENUTA, ' ') CD_CONTRIBUTO_RITENUTA, " +
                "V_MASTRINO_CONTO.ESERCIZIO_DOCUMENTO, " +
                "V_MASTRINO_CONTO.CD_TIPO_DOCUMENTO, " +
                "V_MASTRINO_CONTO.CD_CDS_DOCUMENTO, " +
                "V_MASTRINO_CONTO.CD_UO_DOCUMENTO, " +
                "V_MASTRINO_CONTO.PG_NUMERO_DOCUMENTO, " +
                "Decode(V_MASTRINO_CONTO.SEZIONE,'D',V_MASTRINO_CONTO.IM_MOVIMENTO,0) IM_MOVIMENTO_DARE, " +
                "Decode(V_MASTRINO_CONTO.SEZIONE,'A',V_MASTRINO_CONTO.IM_MOVIMENTO,0) IM_MOVIMENTO_AVERE ");

        sqlBuilderTotUo.setHeader("SELECT MAX(V_MASTRINO_CONTO.PG_SCRITTURA) PG_SCRITTURA, " +
                "NULL DT_CONTABILIZZAZIONE, " +
                "NULL DS_SCRITTURA, " +
                "MAX(V_MASTRINO_CONTO.ESERCIZIO) ESERCIZIO, " +
                "MAX(V_MASTRINO_CONTO.PG_MOVIMENTO) PG_MOVIMENTO, " +
                "V_MASTRINO_CONTO.CD_UNITA_ORGANIZZATIVA, " +
                "MAX(V_MASTRINO_CONTO.CD_CDS) CD_CDS, " +
                "'T1_UO' CD_RIGA, " +
                "NULL SEZIONE, " +
                "V_MASTRINO_CONTO.CD_VOCE_EP, " +
                "NULL CD_TERZO, " +
                "'SALDO_UO' TI_RIGA, " +
                "NULL  CD_CONTRIBUTO_RITENUTA, " +
                "NULL ESERCIZIO_DOCUMENTO, " +
                "NULL CD_TIPO_DOCUMENTO, " +
                "NULL CD_CDS_DOCUMENTO, " +
                "NULL CD_UO_DOCUMENTO, " +
                "NULL PG_NUMERO_DOCUMENTO, " +
                "SUM(Decode(V_MASTRINO_CONTO.SEZIONE,'D',NVL(V_MASTRINO_CONTO.IM_MOVIMENTO,0),0)) IM_MOVIMENTO_DARE, " +
                "SUM(Decode(V_MASTRINO_CONTO.SEZIONE,'A',NVL(V_MASTRINO_CONTO.IM_MOVIMENTO,0),0)) IM_MOVIMENTO_AVERE ");
        sqlBuilderTotUo.addSQLGroupBy("V_MASTRINO_CONTO.CD_VOCE_EP, V_MASTRINO_CONTO.CD_UNITA_ORGANIZZATIVA");

        sqlBuilderTotAll.setHeader("SELECT MAX(V_MASTRINO_CONTO.PG_SCRITTURA) PG_SCRITTURA, " +
                "NULL DT_CONTABILIZZAZIONE, " +
                "NULL DS_SCRITTURA, " +
                "MAX(V_MASTRINO_CONTO.ESERCIZIO) ESERCIZIO, " +
                "MAX(V_MASTRINO_CONTO.PG_MOVIMENTO) PG_MOVIMENTO, " +
                "MAX(V_MASTRINO_CONTO.CD_UNITA_ORGANIZZATIVA) CD_UNITA_ORGANIZZATIVA, " +
                "MAX(V_MASTRINO_CONTO.CD_CDS) CD_CDS, " +
                "'T2_ALL' CD_RIGA, " +
                "NULL SEZIONE, " +
                "V_MASTRINO_CONTO.CD_VOCE_EP, " +
                "NULL CD_TERZO, " +
                "'SALDO' TI_RIGA, " +
                "NULL  CD_CONTRIBUTO_RITENUTA, " +
                "NULL ESERCIZIO_DOCUMENTO, " +
                "NULL CD_TIPO_DOCUMENTO, " +
                "NULL CD_CDS_DOCUMENTO, " +
                "NULL CD_UO_DOCUMENTO, " +
                "NULL PG_NUMERO_DOCUMENTO, " +
                "SUM(Decode(V_MASTRINO_CONTO.SEZIONE,'D',NVL(V_MASTRINO_CONTO.IM_MOVIMENTO,0),0)) IM_MOVIMENTO_DARE, " +
                "SUM(Decode(V_MASTRINO_CONTO.SEZIONE,'A',NVL(V_MASTRINO_CONTO.IM_MOVIMENTO,0),0)) IM_MOVIMENTO_AVERE ");
        sqlBuilderTotAll.addSQLGroupBy("V_MASTRINO_CONTO.CD_VOCE_EP");

        boolean isMultiUo = mapFilter[0].get().getMastriniSelected().size() != 1;

        SQLBuilder sqlResult;
        if (!Optional.ofNullable(mapFilter[0].get().getFiltraUnitaOrganizzativa()).orElse(Boolean.FALSE)) {
            if (mapFilter[0].get().getDetail())
                sqlResult = sqlBuilderDet.union(sqlBuilderTotAll, Boolean.TRUE);
            else
                sqlResult = sqlBuilderTotAll;
        } else if (isMultiUo) {
            if (mapFilter[0].get().getDetail())
                sqlResult = sqlBuilderDet.union(sqlBuilderTotUo, Boolean.TRUE);
            else
                sqlResult = sqlBuilderTotUo.union(sqlBuilderTotAll, Boolean.TRUE);
        } else {
            if (mapFilter[0].get().getDetail())
                sqlResult = sqlBuilderDet.union(sqlBuilderTotUo, Boolean.TRUE);
            else
                sqlResult = sqlBuilderTotUo;
        }
        sqlResult.setOrderBy("cd_voce_ep", it.cnr.jada.util.Orderable.ORDER_ASC);
        sqlResult.setOrderBy("cd_unita_organizzativa", it.cnr.jada.util.Orderable.ORDER_ASC);
        sqlResult.setOrderBy("cd_riga", it.cnr.jada.util.Orderable.ORDER_ASC);
        sqlResult.setOrderBy("pg_scrittura", it.cnr.jada.util.Orderable.ORDER_ASC);
        sqlResult.setOrderBy("pg_movimento", it.cnr.jada.util.Orderable.ORDER_ASC);
        sqlResult.setOrderBy("dt_contabilizzazione", it.cnr.jada.util.Orderable.ORDER_ASC);
        return sqlResult;
    }

    private SQLBuilder createSQLBuilder(UserContext userContext, CompoundFindClause compoundfindclause, Optional<ConsultazioneMastrinoContoBP.MapFilter> mapFilter) {
        SQLBuilder sqlBuilder = super.createSQLBuilderWithoutJoin();
        sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));

        if (mapFilter.isPresent()) {
            if (Optional.ofNullable(mapFilter.get().getFromDataMovimento()).isPresent())
                sqlBuilder.addClause(FindClause.AND, "dt_contabilizzazione", SQLBuilder.GREATER_EQUALS, mapFilter.get().getFromDataMovimento());
            if (Optional.ofNullable(mapFilter.get().getToDataMovimento()).isPresent())
                sqlBuilder.addClause(FindClause.AND, "dt_contabilizzazione", SQLBuilder.LESS_EQUALS, mapFilter.get().getToDataMovimento());

            if (!mapFilter.get().getMastriniSelected().isEmpty()) {
                sqlBuilder.openParenthesis(FindClause.AND);
                for (MastrinoContoBulk mastrinoContoBulk : mapFilter.get().getMastriniSelected()) {
                    sqlBuilder.openParenthesis(FindClause.OR);
                    sqlBuilder.addClause(FindClause.AND, "cd_voce_ep", SQLBuilder.EQUALS, mastrinoContoBulk.getCd_voce_ep());
                    sqlBuilder.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, mastrinoContoBulk.getCd_unita_organizzativa());
                    sqlBuilder.closeParenthesis();
                }
                sqlBuilder.closeParenthesis();
            } else if (!mapFilter.get().getContiSelected().isEmpty()) {
                sqlBuilder.openParenthesis(FindClause.AND);
                for (ContoBulk contoBulk : mapFilter.get().getContiSelected())
                    sqlBuilder.addClause(FindClause.OR, "cd_voce_ep", SQLBuilder.EQUALS, contoBulk.getCd_voce_ep());
                sqlBuilder.closeParenthesis();
            }
        }
        Optional.ofNullable(compoundfindclause).ifPresent(sqlBuilder::addClause);
        return sqlBuilder;
    }
}
