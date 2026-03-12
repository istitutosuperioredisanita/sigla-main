package it.cnr.contab.inventario01.bulk;

public class DocumentoTrasportoHome extends Doc_trasporto_rientroHome{
    public DocumentoTrasportoHome(java.sql.Connection conn) {

        super(DocumentoTrasportoBulk.class,conn);
    }
    /**
     * Nota_di_creditoHome constructor comment.
     * @param conn java.sql.Connection
     * @param persistentCache it.cnr.jada.persistency.PersistentCache
     */
    public DocumentoTrasportoHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(DocumentoTrasportoBulk.class, conn, persistentCache);
    }
}
