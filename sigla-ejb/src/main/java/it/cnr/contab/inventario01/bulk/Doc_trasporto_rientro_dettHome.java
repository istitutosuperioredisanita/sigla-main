/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

/**
 * Home per Doc_trasporto_rientro_dettBulk.
 * <p>
 * METODI CHIAVE PER FILTRO UO
 * - findBeniTrasportabiliPerUO: filtra beni per UO (utente o dipendente)
 * - beneBelongsToUO: verifica appartenenza bene a UO
 * - contaBeniTrasportabiliPerUO: conta beni disponibili per UO
 */
public class Doc_trasporto_rientro_dettHome extends BulkHome {

    public Doc_trasporto_rientro_dettHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public Doc_trasporto_rientro_dettHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public Doc_trasporto_rientro_dettHome(Connection conn) {
        super(Doc_trasporto_rientro_dettBulk.class, conn);
    }

    public Doc_trasporto_rientro_dettHome(Connection conn, PersistentCache persistentCache) {
        super(Doc_trasporto_rientro_dettBulk.class, conn, persistentCache);
    }

    /**
     * Recupera tutti i dettagli di un documento
     */
    public java.util.List getDetailsFor(Doc_trasporto_rientroBulk doc)
            throws it.cnr.jada.persistency.PersistencyException {

        if (doc.getPgDocTrasportoRientro() == null) {
            return java.util.Collections.emptyList();
        }

        SQLBuilder sql = createSQLBuilder();

        sql.addSQLClause(FindClause.AND, "ESERCIZIO",
                SQLBuilder.EQUALS, doc.getEsercizio());
        sql.addSQLClause(FindClause.AND, "PG_INVENTARIO",
                SQLBuilder.EQUALS, doc.getPgInventario());
        sql.addSQLClause(FindClause.AND, "TI_DOCUMENTO",
                SQLBuilder.EQUALS, doc.getTiDocumento());
        sql.addSQLClause(FindClause.AND, "PG_DOC_TRASPORTO_RIENTRO",
                SQLBuilder.EQUALS, doc.getPgDocTrasportoRientro());

        sql.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

        return fetchAll(sql);
    }


    /**
     * Restituisce la MAX(dt_validita_variazione) tra tutti i beni
     * presenti nei dettagli del documento.
     *
     * @param doc il documento di trasporto/rientro
     * @return la data di validità più recente, null se non ci sono dettagli
     */
    public java.sql.Timestamp getMaxDataValiditaVariazione(Doc_trasporto_rientroBulk doc)
            throws PersistencyException {

        try {
            String sql =
                    "SELECT MAX(IB.DT_VALIDITA_VARIAZIONE) " +
                            "FROM DOC_TRASPORTO_RIENTRO_DETT DETT " +
                            "INNER JOIN INVENTARIO_BENI IB ON ( " +
                            "  DETT.PG_INVENTARIO = IB.PG_INVENTARIO AND " +
                            "  DETT.NR_INVENTARIO = IB.NR_INVENTARIO AND " +
                            "  DETT.PROGRESSIVO = IB.PROGRESSIVO " +
                            ") " +
                            "WHERE DETT.PG_INVENTARIO = ? " +
                            "  AND DETT.TI_DOCUMENTO = ? " +
                            "  AND DETT.ESERCIZIO = ? " +
                            "  AND DETT.PG_DOC_TRASPORTO_RIENTRO = ?";

            java.sql.PreparedStatement ps = getConnection().prepareStatement(sql);
            try {
                ps.setLong(1, doc.getPgInventario());
                ps.setString(2, doc.getTiDocumento());
                ps.setInt(3, doc.getEsercizio());
                ps.setLong(4, doc.getPgDocTrasportoRientro());

                java.sql.ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        return rs.getTimestamp(1);
                    }
                    return null;

                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }

        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        }
    }

}