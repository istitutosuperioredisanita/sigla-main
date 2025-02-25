package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
import java.sql.Connection;

public class V_cons_reg_ivaHome extends BulkHome {

    public V_cons_reg_ivaHome(Connection conn) {
        super(V_cons_reg_ivaBulk.class, conn);
    }

    public V_cons_reg_ivaHome(Connection conn, PersistentCache persistentCache) {
        super(V_cons_reg_ivaBulk.class, conn, persistentCache);
    }
}