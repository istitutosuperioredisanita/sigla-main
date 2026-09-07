/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/05/2026
 */
package it.cnr.contab.compensi00.tabrif.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.Broker;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
public class Tipo_CompensoHome extends BulkHome {
	public Tipo_CompensoHome(Connection conn) {
		super(Tipo_CompensoBulk.class, conn);
	}
	public Tipo_CompensoHome(Connection conn, PersistentCache persistentCache) {
		super(Tipo_CompensoBulk.class, conn, persistentCache);
	}

	public java.util.List caricaIntervalli(Tipo_CompensoBulk tipoCompenso) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "cdTiCompenso",sql.EQUALS,tipoCompenso.getCdTiCompenso());
		sql.addOrderBy("DT_INIZIO_VALIDITA");
		return fetchAll(sql);
	}
	public 	void addClauseValidita(SQLBuilder sql, java.sql.Timestamp data){

		sql.addClause("AND","dtInizioValidita",sql.LESS_EQUALS,data);
		sql.addClause("AND","dtFineValidita",sql.GREATER_EQUALS,data);
	}

	public Tipo_CompensoBulk findIntervallo(Tipo_CompensoBulk tipoCompensoBulk) throws PersistencyException{

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND","cdTiCompenso",sql.EQUALS,tipoCompensoBulk.getCdTiCompenso());
		addClauseValidita(sql, tipoCompensoBulk.getDtInizioValidita());

		Tipo_CompensoBulk corrente = null;
		Broker broker = createBroker(sql);
		if (broker.next())
			corrente = (Tipo_CompensoBulk)fetch(broker);
		broker.close();

		return corrente;
	}

}