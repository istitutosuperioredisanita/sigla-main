/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/12/2024
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class Ass_la_ribalt_pluriennaliHome extends BulkHome {
	public Ass_la_ribalt_pluriennaliHome(Connection conn) {
		super(Ass_la_ribalt_pluriennaliBulk.class, conn);
	}
	public Ass_la_ribalt_pluriennaliHome(Connection conn, PersistentCache persistentCache) {
		super(Ass_la_ribalt_pluriennaliBulk.class, conn, persistentCache);
	}

	public Ass_la_ribalt_pluriennaliBulk getAssLineaAttivita(Integer esercizio, WorkpackageBulk gaePrelevamento) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);
		sql.addSQLClause("AND", "CD_CENTRO_RESPONSABILITA", SQLBuilder.EQUALS,gaePrelevamento.getCentro_responsabilita().getCd_centro_responsabilita());
		sql.addSQLClause("AND", "CD_LINEA_ATTIVITA", SQLBuilder.EQUALS, gaePrelevamento.getCd_linea_attivita());

		List ass =fetchAll(sql);
		if(ass.size() == 0){
			return null;
		}
		return (Ass_la_ribalt_pluriennaliBulk) ass.get(0);
	}
}