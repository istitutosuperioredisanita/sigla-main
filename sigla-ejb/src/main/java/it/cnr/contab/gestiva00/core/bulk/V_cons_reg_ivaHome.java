package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.sql.Connection;
import java.util.*;

public class V_cons_reg_ivaHome extends BulkHome {

	public V_cons_reg_ivaHome(Connection conn) {
		super(V_cons_reg_ivaBulk.class, conn);
	}

	public V_cons_reg_ivaHome(Connection conn, PersistentCache persistentCache) {
		super(V_cons_reg_ivaBulk.class, conn, persistentCache);
	}

}