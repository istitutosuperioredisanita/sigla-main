package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;

import java.sql.Connection;

public class V_cons_dett_ivaHome extends BulkHome {

    public V_cons_dett_ivaHome(Connection conn) {
        super(V_cons_dett_ivaBulk.class, conn);
    }

    public V_cons_dett_ivaHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_dett_ivaBulk.class, conn, persistentCache);
    }

    public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
        try {
            // Usa il costruttore corretto per SQLBuilder
            SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);

            if (compoundfindclause != null) {
                String cdTipoSezionale = ((SimpleFindClause) compoundfindclause.getClauses().nextElement()).getValue().toString();

                // Costruisci la query SELECT con il join a TIPO_SEZIONALE
                StringBuilder selectClause = new StringBuilder("SELECT V_CONS_REG_IVA.ESERCIZIO, " + "SUBSTR(V_CONS_REG_IVA.DATA_REGISTRAZIONE,6,2) AS MESE, " + "V_CONS_REG_IVA.CD_TIPO_SEZIONALE");

                if (cdTipoSezionale.equals("a/com") || cdTipoSezionale.equals("v/com")) {
                    selectClause.append(", SUM(DECODE(V_CONS_REG_IVA.SP,'N',V_CONS_REG_IVA.IVA_D,0)) AS SP_N").append(", SUM(DECODE(V_CONS_REG_IVA.SP,'Y',V_CONS_REG_IVA.IVA_D,0)) AS SP_Y").append(", SUM(V_CONS_REG_IVA.IVA_D) AS TOT_IVA");
                } else {
                    selectClause.append(", 0 AS SP_N, 0 AS SP_Y, SUM(V_CONS_REG_IVA.IVA_D) AS TOT_IVA");
                }

                sql.setHeader(selectClause.toString());

                // Aggiungi la tabella TIPO_SEZIONALE per il JOIN
                sql.addTableToHeader("TIPO_SEZIONALE");

                // Aggiungi la condizione di JOIN
                sql.addSQLJoin("V_CONS_REG_IVA.CD_TIPO_SEZIONALE", "TIPO_SEZIONALE.CD_TIPO_SEZIONALE");

                // Aggiungi le condizioni di filtro
                sql.addSQLClause("AND", "V_CONS_REG_IVA.ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(usercontext));
                sql.addSQLClause("AND", "V_CONS_REG_IVA.CD_TIPO_SEZIONALE", SQLBuilder.EQUALS, cdTipoSezionale);


                // Aggiungi GROUP BY
                sql.addSQLGroupBy("V_CONS_REG_IVA.ESERCIZIO");
                sql.addSQLGroupBy("SUBSTR(V_CONS_REG_IVA.DATA_REGISTRAZIONE,6,2)");
                sql.addSQLGroupBy("V_CONS_REG_IVA.CD_TIPO_SEZIONALE");

                // Aggiungi ORDER BY
                sql.addOrderBy("SUBSTR(V_CONS_REG_IVA.DATA_REGISTRAZIONE,6,2)");
                sql.addOrderBy("V_CONS_REG_IVA.CD_TIPO_SEZIONALE");

            }
            return sql;
        } catch (Throwable t) {
            throw new PersistencyException(t);
        }
    }

}
