package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroHome;
import it.cnr.contab.inventario01.ejb.NumerazioneTempDocTRComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class Doc_trasporto_rientroHome extends BulkHome {

    private static final String CONDITION_PK =
            " WHERE PG_INVENTARIO = ? AND TI_DOCUMENTO = ? AND ESERCIZIO = ? AND PG_DOC_TRASPORTO_RIENTRO = ?";

    public Doc_trasporto_rientroHome(Connection conn) {
        super(Doc_trasporto_rientroBulk.class, conn);
    }

    public Doc_trasporto_rientroHome(Connection conn, PersistentCache persistentCache) {
        super(Doc_trasporto_rientroBulk.class, conn, persistentCache);
    }

    public Doc_trasporto_rientroHome(Class class1, Connection connection) {
        super(class1, connection);
    }

    public Doc_trasporto_rientroHome(Class class1, Connection connection, PersistentCache persistentcache) {
        super(class1, connection, persistentcache);
    }

    public Collection findTipoMovimenti(UserContext userContext, Doc_trasporto_rientroBulk docTR,
                                        Tipo_trasporto_rientroHome h, Tipo_trasporto_rientroBulk clause)
            throws PersistencyException, IntrospectionException {
        return h.findTipiPerDocumento(userContext, docTR.getTiDocumento());
    }

    public void initializePrimaryKeyForInsert(UserContext userContext, OggettoBulk bulk)
            throws PersistencyException, ComponentException {
        try {
            Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
            docTR.setPgDocTrasportoRientro(generaPgProvv(userContext, docTR));
        } catch (ApplicationException e) {
            throw new ComponentException(e);
        } catch (Throwable e) {
            throw new PersistencyException(e);
        }
    }

    private Long generaPgProvv(UserContext userContext, Doc_trasporto_rientroBulk docTR) throws Exception {
        NumerazioneTempDocTRComponentSession session = (NumerazioneTempDocTRComponentSession)
                EJBCommonServices.createEJB("CNRINVENTARIO01_EJB_NumerazioneTempDocTRComponentSession",
                        NumerazioneTempDocTRComponentSession.class);
        return session.getNextTempPG(userContext, docTR);
    }

    @Override
    public SQLBuilder selectByClause(CompoundFindClause compoundfindclause) throws PersistencyException {
        SQLBuilder sqlbuilder = super.selectByClause(compoundfindclause);
        sqlbuilder.addOrderBy("ESERCIZIO desc,PG_DOC_TRASPORTO_RIENTRO desc");
        return sqlbuilder;
    }

    public void inizializzaProgressivo(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws BusyResourceException, PersistencyException, ComponentException {
        Long result = (Long) findMax(doc, "pgDocTrasportoRientro", Long.valueOf(0), true);
        doc.setPgDocTrasportoRientro(Long.valueOf(result.longValue() + 1));
    }

    /**
     * Converte un documento temporaneo (PG negativo) in definitivo: copia testata e dettagli
     * con il nuovo progressivo, poi elimina il temporaneo.
     */
    public void confirmDocTrasportoRientroTemporaneo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTemporaneo,
            Long pg)
            throws IntrospectionException, PersistencyException {

        if (pg == null) {
            throw new PersistencyException("Impossibile ottenere un progressivo definitivo per il documento inserito!");
        }

        copiaConNuovoPg(this, docTemporaneo, pg);

        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeCache().getHome(Doc_trasporto_rientro_dettBulk.class);
        copiaConNuovoPg(dettHome, docTemporaneo, pg);

        delete(docTemporaneo, userContext);
        docTemporaneo.setPgDocTrasportoRientro(pg);
    }

    /** Esegue INSERT ... SELECT copiando le righe del temporaneo con il nuovo PG. */
    private void copiaConNuovoPg(BulkHome home, Doc_trasporto_rientroBulk docTemporaneo, Long pg)
            throws PersistencyException, IntrospectionException {

        String schema = EJBCommonServices.getDefaultSchema();
        String tabella = schema + home.getColumnMap().getTableName();

        String sql = "INSERT INTO " + tabella + " (" + elencoColonne(home, null) + ") SELECT "
                + elencoColonne(home, new String[][]{{"PG_DOC_TRASPORTO_RIENTRO", "?"}})
                + " FROM " + tabella + CONDITION_PK;

        LoggableStatement ps = null;
        try {
            ps = new LoggableStatement(getConnection(), sql, true, this.getClass());
            ps.setLong(1, pg);
            ps.setInt(2, docTemporaneo.getPgInventario().intValue());
            ps.setString(3, docTemporaneo.getTiDocumento());
            ps.setInt(4, docTemporaneo.getEsercizio().intValue());
            ps.setLong(5, docTemporaneo.getPgDocTrasportoRientro());
            ps.execute();
        } catch (SQLException e) {
            throw new PersistencyException(e);
        } finally {
            closeQuietly(ps);
        }
    }

    private static void closeQuietly(LoggableStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException ignored) {
            // chiusura best-effort
        }
    }

    /** Elenco colonne separate da virgola, con eventuali sostituzioni (nome → espressione). */
    private String elencoColonne(BulkHome home, String[][] sostituzioni) throws PersistencyException {
        if (home == null) {
            throw new PersistencyException("Impossibile ottenere la home per l'aggiornamento dei progressivi temporanei del documento!");
        }

        String[] colonne = home.getColumnMap().getColumnNames();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        for (int i = 0; i < colonne.length; i++) {
            String nome = colonne[i];
            if (sostituzioni != null) {
                for (String[] sostituzione : sostituzioni) {
                    if (nome.equalsIgnoreCase(sostituzione[0])) {
                        nome = sostituzione[1];
                        break;
                    }
                }
            }
            pw.print(nome);
            if (i < colonne.length - 1) {
                pw.print(", ");
            }
        }
        pw.close();
        return sw.toString();
    }

    public Doc_trasporto_rientroBulk findDocTrasportoRientro(Doc_trasporto_rientroBulk chiave)
            throws PersistencyException {
        return (Doc_trasporto_rientroBulk) findByPrimaryKey(chiave);
    }
}