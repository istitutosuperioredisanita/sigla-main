/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/11/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.Collection;

public class Doc_trasporto_rientro_respintoHome extends BulkHome {

	public Doc_trasporto_rientro_respintoHome(Connection conn) {
		super(Doc_trasporto_rientro_respintoBulk.class, conn);
	}

	public Doc_trasporto_rientro_respintoHome(Connection conn, PersistentCache persistentCache) {
		super(Doc_trasporto_rientro_respintoBulk.class, conn, persistentCache);
	}

	/**
	 * Recupera la cronologia dei respingimenti per un documento di trasporto/rientro
	 */
	public Collection getRespingimenti(UserContext userContext,
									   Long pgInventario,
									   String tiDocumento,
									   Integer esercizio,
									   Long pgDocTrasportoRientro) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "pg_inventario", SQLBuilder.EQUALS, pgInventario);
		sql.addClause("AND", "ti_documento", SQLBuilder.EQUALS, tiDocumento);
		sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
		sql.addClause("AND", "pg_doc_trasporto_rientro", SQLBuilder.EQUALS, pgDocTrasportoRientro);

		sql.addOrderBy("data_inserimento DESC");

		return fetchAll(sql);
	}

	/**
	 * Recupera i respingimenti per tipo operazione (TR/RI)
	 */
	public Collection getRespingimentiPerTipoOperazione(UserContext userContext,
														String tipoOperazioneDoc) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "tipo_operazione_doc", SQLBuilder.EQUALS, tipoOperazioneDoc);
		sql.addOrderBy("data_inserimento DESC");

		return fetchAll(sql);
	}

	/**
	 * Recupera i respingimenti per fase (es. RFI)
	 */
	public Collection getRespingimentiPerFase(UserContext userContext,
											  String tipoFaseRespingi) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "tipo_fase_respingi", SQLBuilder.EQUALS, tipoFaseRespingi);
		sql.addOrderBy("data_inserimento DESC");

		return fetchAll(sql);
	}

	/**
	 * Recupera i respingimenti per utente
	 */
	public Collection getRespingimentiPerUtente(UserContext userContext,
												String uidInsert) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "uid_insert", SQLBuilder.EQUALS, uidInsert);
		sql.addOrderBy("data_inserimento DESC");

		return fetchAll(sql);
	}
}
