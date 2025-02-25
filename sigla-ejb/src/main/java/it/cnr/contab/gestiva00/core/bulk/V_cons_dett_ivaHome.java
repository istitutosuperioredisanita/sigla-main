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


//todo verificare se si può eliminare (logica presente nel component)
//    public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
//            throws PersistencyException {
//        SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
//
//        Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(usercontext);
//        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);
//
//        if (compoundfindclause != null) {
//            String cdTipoSezionale = ((SimpleFindClause) compoundfindclause.getClauses().nextElement()).getValue().toString();
//
//            StringBuilder selectClause = new StringBuilder(
//                    "SELECT ESERCIZIO, " +
//                            "SUBSTR(DATA_REGISTRAZIONE,6,2) AS MESE, " +
//                            "NVL(CD_TIPO_SEZIONALE,'Tot') AS TIPO_SEZIONALE"
//            );
//
//            if (cdTipoSezionale.equals("a/com") || cdTipoSezionale.equals("v/com")) {
//                selectClause.append(", SUM(DECODE(SP,'N',IVA_D,0)) AS SP_N")
//                        .append(", SUM(DECODE(SP,'Y',IVA_D,0)) AS SP_Y")
//                        .append(", SUM(IVA_D) AS TOT_IVA");
//            } else {
//                selectClause.append(", SUM(IVA_D) AS TOT_IVA");
//            }
//
//            sql.setHeader(selectClause.toString());
//
//            sql.addSQLClause("AND", "CD_TIPO_SEZIONALE", SQLBuilder.EQUALS, cdTipoSezionale);
//
//            sql.addSQLGroupBy("ESERCIZIO");
//            sql.addSQLGroupBy("DATA_REGISTRAZIONE");
//            sql.addSQLGroupBy("CD_TIPO_SEZIONALE");
//
//            sql.addOrderBy("DATA_REGISTRAZIONE");
//            sql.addOrderBy("CD_TIPO_SEZIONALE");
//        }
//
//        return sql;
//    }

}
