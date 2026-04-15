package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class V_cons_dett_ivaHome extends BulkHome {

    public V_cons_dett_ivaHome(Connection conn) {
        super(V_cons_dett_ivaBulk.class, conn);
    }

    public V_cons_dett_ivaHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_dett_ivaBulk.class, conn, persistentCache);
    }

    @Override
    public SQLBuilder selectByClause(UserContext userContext, CompoundFindClause compoundFindClause) throws PersistencyException {
        try {
            final SQLBuilder sql = super.selectByClause(userContext, null);
            final List<SimpleFindClause> clauses = new ArrayList<>();
            if (compoundFindClause != null) estraiClausole(compoundFindClause, clauses);

            Integer esercizio = null;
            final List<String> tipiSezionali = new ArrayList<>();

            // Estrazione parametri principali dai filtri
            for (SimpleFindClause clause : clauses) {
                if (clause.getPropertyName() == null || clause.getValue() == null) continue;
                String property = clause.getPropertyName().trim().toLowerCase();
                if ("esercizio".equals(property) && clause.getOperator() == SQLBuilder.EQUALS) {
                    esercizio = Integer.valueOf(clause.getValue().toString().trim());
                } else if ("cd_tipo_sezionale".equals(property) && clause.getOperator() == SQLBuilder.EQUALS) {
                    tipiSezionali.add(clause.getValue().toString().trim());
                }
            }

            if (esercizio == null) esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);

            boolean hasSplitPayment = tipiSezionali.stream().anyMatch(t -> "a/com".equalsIgnoreCase(t) || "v/com".equalsIgnoreCase(t));

            // Costruzione query interna aggregata
            StringBuilder inner = new StringBuilder();
            inner.append("SELECT V_CONS_REG_IVA.ESERCIZIO, SUBSTR(V_CONS_REG_IVA.DATA_REGISTRAZIONE, 6, 2) AS MESE, V_CONS_REG_IVA.CD_TIPO_SEZIONALE, ");

            if (hasSplitPayment) {
                inner.append("SUM(DECODE(V_CONS_REG_IVA.SP, 'N', V_CONS_REG_IVA.IVA_D, 0)) AS SP_N, SUM(DECODE(V_CONS_REG_IVA.SP, 'Y', V_CONS_REG_IVA.IVA_D, 0)) AS SP_Y, ");
            } else {
                inner.append("0 AS SP_N, 0 AS SP_Y, ");
            }

            inner.append("SUM(V_CONS_REG_IVA.IVA_D) AS TOT_IVA FROM V_CONS_REG_IVA, TIPO_SEZIONALE ")
                    .append("WHERE V_CONS_REG_IVA.CD_TIPO_SEZIONALE = TIPO_SEZIONALE.CD_TIPO_SEZIONALE ")
                    .append("AND V_CONS_REG_IVA.ESERCIZIO = ").append(esercizio).append(" ");

            if (!tipiSezionali.isEmpty()) {
                if (tipiSezionali.size() == 1) {
                    inner.append("AND V_CONS_REG_IVA.CD_TIPO_SEZIONALE = '").append(tipiSezionali.get(0).replace("'", "''")).append("' ");
                } else {
                    inner.append("AND V_CONS_REG_IVA.CD_TIPO_SEZIONALE IN (");
                    for (int i = 0; i < tipiSezionali.size(); i++) {
                        inner.append(i > 0 ? ", '" : "'").append(tipiSezionali.get(i).replace("'", "''")).append("'");
                    }
                    inner.append(") ");
                }
            }

            inner.append("GROUP BY V_CONS_REG_IVA.ESERCIZIO, SUBSTR(V_CONS_REG_IVA.DATA_REGISTRAZIONE, 6, 2), V_CONS_REG_IVA.CD_TIPO_SEZIONALE");

            sql.setHeader("SELECT *");
            sql.setFromClause(new StringBuffer("(\n" + inner + "\n) X"));

            boolean firstOuterClause = true;
            for (SimpleFindClause clause : clauses) {
                if (clause.getPropertyName() == null) continue;
                String property = clause.getPropertyName().trim().toLowerCase();
                int op = clause.getOperator();
                Object val = clause.getValue();

                if ("esercizio".equals(property) && op == SQLBuilder.EQUALS) continue;
                if ("cd_tipo_sezionale".equals(property) && op == SQLBuilder.EQUALS) continue;

                String logica = (clause.getLogicalOperator() == null || clause.getLogicalOperator().isBlank()) ? "AND" : clause.getLogicalOperator().trim().toUpperCase();
                if (firstOuterClause) {
                    logica = "AND";
                    firstOuterClause = false;
                }

                String field = switch (property) {
                    case "esercizio" -> "X.ESERCIZIO";
                    case "mese" -> "X.MESE";
                    case "cd_tipo_sezionale" -> "X.CD_TIPO_SEZIONALE";
                    case "sp_n" -> "X.SP_N";
                    case "sp_y" -> "X.SP_Y";
                    case "tot_iva" -> "X.TOT_IVA";
                    default -> null;
                };

                if (field == null) continue;
                if (op == SQLBuilder.ISNULL || op == SQLBuilder.ISNOTNULL) {
                    sql.addSQLClause(logica, field, op, null);
                    continue;
                }
                if (val == null) continue;

                if ("mese".equals(property) || "cd_tipo_sezionale".equals(property)) {
                    sql.addSQLClause(logica, field, op, val.toString().trim(), Types.VARCHAR, 0, null, false, false);
                } else if (op == SQLBuilder.CONTAINS || op == SQLBuilder.LIKE || op == SQLBuilder.LIKE_FILTER || op == SQLBuilder.STARTSWITH || op == SQLBuilder.ENDSWITH) {
                    sql.addSQLClause(logica, "TO_CHAR(" + field + ")", op, val.toString().trim(), Types.VARCHAR, 0, null, false, false);
                } else {
                    sql.addSQLClause(logica, field, op, val, Types.NUMERIC, 0, null, true, false);
                }
            }

            sql.addOrderBy("X.ESERCIZIO");
            sql.addOrderBy("X.MESE");
            sql.addOrderBy("X.CD_TIPO_SEZIONALE");
            return sql;
        } catch (Throwable t) {
            throw new PersistencyException(t);
        }
    }

    private void estraiClausole(CompoundFindClause compound, List<SimpleFindClause> result) {
        for (Enumeration<?> e = compound.getClauses(); e.hasMoreElements(); ) {
            Object clause = e.nextElement();
            if (clause instanceof SimpleFindClause simple) result.add(simple);
            else if (clause instanceof CompoundFindClause nested) estraiClausole(nested, result);
        }
    }
}