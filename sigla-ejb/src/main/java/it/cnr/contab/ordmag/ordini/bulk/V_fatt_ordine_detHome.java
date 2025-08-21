/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 18/08/2025
 */
package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class V_fatt_ordine_detHome extends BulkHome {
	public V_fatt_ordine_detHome(Connection conn) {
		super(V_fatt_ordine_detBulk.class, conn);
	}
	public V_fatt_ordine_detHome(Connection conn, PersistentCache persistentCache) {
		super(V_fatt_ordine_detBulk.class, conn, persistentCache);
	}

	public List<V_fatt_ordine_detBulk> getFattOrdineDetInv(Fattura_passivaBulk fattura_passivaBulk) throws PersistencyException {
		SQLBuilder sql = createSQLBuilder();
		sql.addClause(FindClause.AND,"idTransito", SQLBuilder.ISNOTNULL,null);
		sql.addClause(FindClause.AND,"cdCds", SQLBuilder.EQUALS, fattura_passivaBulk.getCd_cds());
		sql.addClause(FindClause.AND,"cdUnitaOrganizzativa", SQLBuilder.EQUALS, fattura_passivaBulk.getCd_unita_organizzativa());
		sql.addClause(FindClause.AND,"esercizio", SQLBuilder.EQUALS, fattura_passivaBulk.getEsercizio());
		sql.addClause(FindClause.AND,"pgFatturaPassiva", SQLBuilder.EQUALS, fattura_passivaBulk.getPg_fattura_passiva());
		return this.fetchAll(sql);


	}
}