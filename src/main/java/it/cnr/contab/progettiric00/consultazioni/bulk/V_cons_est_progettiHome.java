package it.cnr.contab.progettiric00.consultazioni.bulk;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

public class V_cons_est_progettiHome extends BulkHome {

    private static final String VISTA_PADRE = "V_PROGETTO_PADRE";
    private static final String VISTA_CONS = "V_CONS_EST_PROGETTI";
    private static final String FL_VISIBILE_SI = "Y";

    public V_cons_est_progettiHome(Connection conn) {
        super(V_cons_est_progettiBulk.class, conn);
    }

    public V_cons_est_progettiHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_est_progettiBulk.class, conn, persistentCache);
    }

    @Override
    public SQLBuilder selectByClause(UserContext userContext, CompoundFindClause clauses)
            throws PersistencyException {

        SQLBuilder sql = super.selectByClause(userContext, clauses);
        applicaFiltriBase(sql, userContext);
        return sql;
    }

    /**
     * Filtri applicativi sempre presenti.
     */
    private void applicaFiltriBase(SQLBuilder sql, UserContext context) throws PersistencyException {
        Integer esercizioCorrente = CNRUserContext.getEsercizio(context);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".ESERCIZIO", SQLBuilder.EQUALS, esercizioCorrente);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".TIPO_FASE", SQLBuilder.EQUALS, ProgettoBulk.TIPO_FASE_NON_DEFINITA);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".ESERCIZIO_PIANO", SQLBuilder.EQUALS, esercizioCorrente);
        Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHomeCache()
                .getHome(Unita_organizzativa_enteBulk.class)
                .findAll()
                .getFirst();

        String uo = CNRUserContext.getCd_unita_organizzativa(context);

        if (uo != null && !uo.equals(ente.getCd_unita_organizzativa())) {
            sql.addSQLClause(FindClause.AND, VISTA_CONS + ".CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, uo);
        }

    }
}