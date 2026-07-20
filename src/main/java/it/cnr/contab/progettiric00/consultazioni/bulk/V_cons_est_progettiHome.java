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
        applicaFiltriSicurezza(sql, userContext);

        return sql;
    }

    /**
     * Filtri applicativi sempre presenti.
     */
    private void applicaFiltriBase(SQLBuilder sql, UserContext context) {
        Integer esercizioCorrente = CNRUserContext.getEsercizio(context);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".ESERCIZIO", SQLBuilder.EQUALS, esercizioCorrente);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".LIVELLO", SQLBuilder.EQUALS, ProgettoBulk.LIVELLO_PROGETTO_SECONDO);
        sql.addSQLClause(FindClause.AND, VISTA_CONS + ".TIPO_FASE", SQLBuilder.EQUALS, ProgettoBulk.TIPO_FASE_NON_DEFINITA);
    }

    /**
     * Logica di sicurezza: verifica se l'utente appartiene a una UO specifica.
     * Se l'utente non è "centrale" (ente), limita i risultati ai soli progetti
     * visibili per la sua UO.
     */
    private void applicaFiltriSicurezza(SQLBuilder sql, UserContext context)
            throws PersistencyException {

        try {
            Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHomeCache()
                    .getHome(Unita_organizzativa_enteBulk.class)
                    .findAll()
                    .getFirst();

            String uo = CNRUserContext.getCd_unita_organizzativa(context);

            if (uo != null && !uo.equals(ente.getCd_unita_organizzativa())) {

                ProgettoHome ph = (ProgettoHome) getHomeCache().getHome(ProgettoBulk.class);

                sql.addTableToHeader(VISTA_PADRE);
                sql.addSQLJoin(VISTA_CONS + ".PG_PROGETTO", VISTA_PADRE + ".PG_PROGETTO");
                sql.addSQLJoin(VISTA_CONS + ".ESERCIZIO", VISTA_PADRE + ".ESERCIZIO");
                sql.addSQLJoin(VISTA_CONS + ".TIPO_FASE", VISTA_PADRE + ".TIPO_FASE");

                sql.addSQLExistsClause("AND", ph.abilitazioniCommesse(context));

                String subQuery =
                        "EXISTS ( " +
                                "SELECT 1 " +
                                "FROM PROGETTO_UO PUO " +
                                "WHERE PUO.PG_PROGETTO = " + VISTA_CONS + ".PG_PROGETTO " +
                                "AND PUO.FL_VISIBILE = '" + FL_VISIBILE_SI + "' " +
                                "AND PUO.CD_UNITA_ORGANIZZATIVA = '" + uo + "'" +
                                ")";

                sql.addSQLClause("AND", subQuery);
            }

        } catch (Exception e) {
            throw new PersistencyException(
                    "Errore durante l'applicazione dei filtri di sicurezza sui progetti",
                    e);
        }
    }
}