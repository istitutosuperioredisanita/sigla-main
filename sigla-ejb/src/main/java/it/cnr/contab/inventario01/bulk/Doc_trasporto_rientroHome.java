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

import java.sql.Connection;
import java.util.Collection;

public class Doc_trasporto_rientroHome extends BulkHome {

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

    /**
     * Trova i tipi di movimento disponibili per un documento di trasporto/rientro
     */
    public Collection findTipoMovimenti(UserContext userContext, Doc_trasporto_rientroBulk docTR, Tipo_trasporto_rientroHome h,
                                        Tipo_trasporto_rientroBulk clause)
            throws PersistencyException, IntrospectionException {
        return h.findTipiPerDocumento(userContext, docTR.getTiDocumento());
    }

    /**
     * Inizializza la chiave primaria per l'inserimento di un nuovo documento
     */
    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk)
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

    /**
     * Genera un nuovo progressivo per il documento
     */
    private Long generaPgProvv(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docTR)
            throws Exception {
        NumerazioneTempDocTRComponentSession session = (NumerazioneTempDocTRComponentSession)
                EJBCommonServices.createEJB("CNRINVENTARIO01_EJB_NumerazioneTempDocTRComponentSession",
                        NumerazioneTempDocTRComponentSession.class);
        return session.getNextTempPG(userContext, docTR);
    }

    @Override
    public SQLBuilder selectByClause(CompoundFindClause compoundfindclause)
            throws PersistencyException {
        SQLBuilder sqlbuilder = super.selectByClause(compoundfindclause);
        sqlbuilder.addOrderBy("ESERCIZIO desc,PG_DOC_TRASPORTO_RIENTRO desc");

        return sqlbuilder;
    }

    /**
     * Imposta il progressivo di un documento
     */

    public void inizializzaProgressivo(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk doc) throws BusyResourceException, PersistencyException, ComponentException {
        Long result = (Long) findMax(doc, "pgDocTrasportoRientro", new Long(0), true);
        doc.setPgDocTrasportoRientro(new Long(result.longValue() + 1));
    }


    /**
     * Conferma un documento di trasporto rientro temporaneo (con progressivo negativo)
     * creando una nuova versione con progressivo definitivo
     *
     * @param userContext
     * @param docTemporaneo
     * @param pg
     * @throws IntrospectionException
     * @throws PersistencyException
     */
    public void confirmDocTrasportoRientroTemporaneo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTemporaneo,
            Long pg)
            throws IntrospectionException, PersistencyException {

        if (pg == null)
            throw new PersistencyException("Impossibile ottenere un progressivo definitivo per il documento inserito!");

        LoggableStatement ps = null;
        java.io.StringWriter sql = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sql);
        String condition = " WHERE PG_INVENTARIO = ? AND TI_DOCUMENTO = ? AND ESERCIZIO = ? AND PG_DOC_TRASPORTO_RIENTRO = ?";

        try {
            // ===== INSERT TESTATA CON NUOVO PROGRESSIVO =====
            pw.write("INSERT INTO " + EJBCommonServices.getDefaultSchema() + getColumnMap().getTableName() + " (");
            pw.write(getPersistenColumnNamesReplacingWith(this, null).toString());
            pw.write(") SELECT ");
            pw.write(getPersistenColumnNamesReplacingWith(
                    this,
                    new String[][]{{"PG_DOC_TRASPORTO_RIENTRO", "?"}}).toString());
            pw.write(" FROM " + EJBCommonServices.getDefaultSchema() + getColumnMap().getTableName());
            pw.write(condition);
            pw.flush();

            ps = new LoggableStatement(getConnection(), sql.toString(), true, this.getClass());
            pw.close();
            ps.setLong(1, pg.longValue());
            ps.setInt(2, docTemporaneo.getPgInventario().intValue());
            ps.setString(3, docTemporaneo.getTiDocumento());
            ps.setInt(4, docTemporaneo.getEsercizio().intValue());
            ps.setLong(5, docTemporaneo.getPgDocTrasportoRientro().longValue());

            ps.execute();
        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        } finally {
            try {
                ps.close();
            } catch (java.sql.SQLException e) {
            }
        }

        try {
            // ===== INSERT DETTAGLI CON NUOVO PROGRESSIVO =====
            sql = new java.io.StringWriter();
            pw = new java.io.PrintWriter(sql);
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHomeCache().getHome(Doc_trasporto_rientro_dettBulk.class);
            pw.write("INSERT INTO " + EJBCommonServices.getDefaultSchema() + dettHome.getColumnMap().getTableName() + " (");
            pw.write(getPersistenColumnNamesReplacingWith(dettHome, null).toString());
            pw.write(") SELECT ");
            pw.write(getPersistenColumnNamesReplacingWith(
                    dettHome,
                    new String[][]{{"PG_DOC_TRASPORTO_RIENTRO", "?"}}).toString());
            pw.write(" FROM " + EJBCommonServices.getDefaultSchema() + dettHome.getColumnMap().getTableName());
            pw.write(condition);
            pw.flush();
            ps = new LoggableStatement(getConnection(), sql.toString(), true, this.getClass());
            pw.close();
            ps.setLong(1, pg.longValue());
            ps.setInt(2, docTemporaneo.getPgInventario().intValue());
            ps.setString(3, docTemporaneo.getTiDocumento());
            ps.setInt(4, docTemporaneo.getEsercizio().intValue());
            ps.setLong(5, docTemporaneo.getPgDocTrasportoRientro().longValue());

            ps.execute();
        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        } finally {
            try {
                ps.close();
            } catch (java.sql.SQLException e) {
            }
        }

        // ===== DELETE DOCUMENTO TEMPORANEO =====
        delete(docTemporaneo, userContext);

        docTemporaneo.setPgDocTrasportoRientro(pg);
    }

    /**
     * Metodo di utilità per costruire i nomi delle colonne con eventuali sostituzioni
     *
     * @param home
     * @param fields
     * @return
     * @throws PersistencyException
     */
    private StringBuffer getPersistenColumnNamesReplacingWith(
            BulkHome home,
            String[][] fields)
            throws PersistencyException {

        java.io.StringWriter columns = new java.io.StringWriter();

        if (home == null)
            throw new PersistencyException("Impossibile ottenere la home per l'aggiornamento dei progressivi temporanei del documento!");

        java.io.PrintWriter pw = new java.io.PrintWriter(columns);
        String[] persistenColumns = home.getColumnMap().getColumnNames();
        for (int i = 0; i < persistenColumns.length; i++) {
            String columnName = persistenColumns[i];
            if (fields != null) {
                for (int j = 0; j < fields.length; j++) {
                    String[] field = fields[j];
                    if (columnName.equalsIgnoreCase(field[0])) {
                        columnName = field[1];
                        break;
                    }
                }
            }
            pw.print(columnName);
            pw.print((i == persistenColumns.length - 1) ? "" : ", ");
        }
        pw.close();
        return columns.getBuffer();
    }


}