/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/12/2024
 */
package it.cnr.contab.config00.bulk;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderedHashtable;

public class CausaleContabileHome extends BulkHome {
	public CausaleContabileHome(Connection conn) {
		super(CausaleContabileBulk.class, conn);
	}
	public CausaleContabileHome(Connection conn, PersistentCache persistentCache) {
		super(CausaleContabileBulk.class, conn, persistentCache);
	}

	public Hashtable loadTiDocumentoAmmKeys(CausaleContabileBulk causaleContabileBulk) throws PersistencyException {
		SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
		List<Tipo_documento_ammBulk> result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
		return new Hashtable(result
				.stream()
				.collect(Collectors.toMap(
						entry -> entry.getCd_tipo_documento_amm(),
						entry -> entry.getDs_tipo_documento_amm(), (key, value) -> value, HashMap::new)
				));

	}
}