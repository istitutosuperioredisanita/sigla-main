package it.cnr.contab.inventario01.bulk;

public class DocumentoRientroHome extends Doc_trasporto_rientroHome{
    public DocumentoRientroHome(java.sql.Connection conn) {

        super(DocumentoRientroBulk.class,conn);
    }
    /**
     * Nota_di_creditoHome constructor comment.
     * @param conn java.sql.Connection
     * @param persistentCache it.cnr.jada.persistency.PersistentCache
     */
    public DocumentoRientroHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(DocumentoRientroBulk.class, conn, persistentCache);
    }
}
