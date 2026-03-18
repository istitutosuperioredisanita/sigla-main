package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

public class InventarioDocTRHome extends Inventario_beniHome {

    public InventarioDocTRHome(Connection conn) {
        super(InventarioDocTRBulk.class, conn);
    }

    public InventarioDocTRHome(Connection conn, PersistentCache persistentCache) {
        super(InventarioDocTRBulk.class, conn, persistentCache);
    }
}