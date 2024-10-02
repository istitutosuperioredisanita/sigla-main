/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/11/2023
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class Obbligazione_pluriennale_voceHome extends BulkHome {
	public Obbligazione_pluriennale_voceHome(Connection conn) {
		super(Obbligazione_pluriennale_voceBulk.class, conn);
	}
	public Obbligazione_pluriennale_voceHome(Connection conn, PersistentCache persistentCache) {
		super(Obbligazione_pluriennale_voceBulk.class, conn, persistentCache);
	}
	public Obbligazione_pluriennale_voceBulk findObbligazioniPluriennaliVoceLinea(Obbligazione_pluriennaleBulk bulk, V_pdg_obbligazione_speBulk linea) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, bulk.getPgObbligazione());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());
		sql.addSQLClause("AND", "CD_CENTRO_RESPONSABILITA", SQLBuilder.EQUALS, linea.getCd_centro_responsabilita());
		sql.addSQLClause("AND", "CD_LINEA_ATTIVITA", SQLBuilder.EQUALS, linea.getCd_linea_attivita());

		if(fetchAll(sql).size() == 0){
			return null;
		}
		return (Obbligazione_pluriennale_voceBulk) fetchAll(sql).get(0);
	}
	public Obbligazione_pluriennale_voceBulk findObbligazioniPluriennaliVoceLinea(Obbligazione_pluriennaleBulk bulk, WorkpackageBulk linea) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, bulk.getCdCds());
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, bulk.getEsercizio());
		sql.addSQLClause("AND", "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, bulk.getEsercizioOriginale());
		sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, bulk.getPgObbligazione());
		sql.addSQLClause("AND", "ANNO", SQLBuilder.EQUALS, bulk.getAnno());
		sql.addSQLClause("AND", "CD_CENTRO_RESPONSABILITA", SQLBuilder.EQUALS, linea.getCd_centro_responsabilita());
		sql.addSQLClause("AND", "CD_LINEA_ATTIVITA", SQLBuilder.EQUALS, linea.getCd_linea_attivita());

		if(fetchAll(sql).size() == 0){
			return null;
		}
		return (Obbligazione_pluriennale_voceBulk) fetchAll(sql).get(0);
	}
}