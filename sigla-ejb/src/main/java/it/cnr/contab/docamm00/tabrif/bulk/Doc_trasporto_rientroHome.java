/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.docamm00.tabrif.bulk;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroHome;
import it.cnr.contab.inventario01.ejb.NumerazioneTempDocTRComponentSession;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;

public class Doc_trasporto_rientroHome extends BulkHome {

	// ========================================
	// COSTANTI
	// ========================================
	private static final String TABLE_DETT = "DOC_TRASPORTO_RIENTRO_DETT";
	private static final String FIELD_DATA_REGISTRAZIONE = "data_registrazione";

	// ========================================
	// COSTRUTTORI
	// ========================================
	public Doc_trasporto_rientroHome(Connection conn) {
		super(Doc_trasporto_rientroBulk.class, conn);
	}

	public Doc_trasporto_rientroHome(Connection conn, PersistentCache persistentCache) {
		super(Doc_trasporto_rientroBulk.class, conn, persistentCache);
	}

	// ========================================
	// METODI DI UTILITÀ BASE
	// ========================================

	/**
	 * Restituisce la data massima di registrazione per un determinato inventario ed esercizio
	 */
	public Timestamp GetMaxDataRegistrazione(Id_inventarioBulk inventario, Integer esercizio)
			throws PersistencyException {
		Doc_trasporto_rientroBulk bulk = new Doc_trasporto_rientroBulk();
		bulk.setEsercizio(esercizio);
		bulk.setInventario(inventario);
		return (Timestamp) findMax(bulk, FIELD_DATA_REGISTRAZIONE);
	}

	/**
	 * Restituisce la data massima di registrazione per i documenti di trasporto
	 */
	public Timestamp getData_di_Trasporto(Doc_trasporto_rientroBulk docTR) throws PersistencyException {
		Doc_trasporto_rientroBulk bulk = new Doc_trasporto_rientroBulk();
		bulk.setEsercizio(docTR.getEsercizio());
		bulk.setInventario(docTR.getInventario());
		bulk.setTiDocumento(Doc_trasporto_rientroBulk.TRASPORTO);
		return (Timestamp) findMax(bulk, FIELD_DATA_REGISTRAZIONE);
	}

	/**
	 * Trova i tipi di movimento disponibili per un documento di trasporto/rientro
	 */
	public Collection findTipoMovimenti(Doc_trasporto_rientroBulk docTR, Tipo_trasporto_rientroHome h,
										Tipo_trasporto_rientroBulk clause) throws PersistencyException, IntrospectionException {
		return h.findTipoMovimenti(docTR);
	}

	/**
	 * Inizializza la chiave primaria per l'inserimento di un nuovo documento
	 */
	public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk)
			throws PersistencyException, ComponentException {
		try {
			Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
			if (docTR.getPgDocTrasportoRientro() == null) {
				docTR.setPgDocTrasportoRientro(generaNuovoProgressivo(userContext, docTR));
			}
		} catch (ApplicationException e) {
			throw new ComponentException(e);
		} catch (Throwable e) {
			throw new PersistencyException(e);
		}
	}

	// ========================================
	// METODI HELPER PRIVATI
	// ========================================

	/**
	 * Genera un nuovo progressivo per il documento
	 */
	private Long generaNuovoProgressivo(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docTR)
			throws Exception {
		Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome) getHomeCache()
				.getHome(Numeratore_doc_t_rBulk.class);

		if (!userContext.isTransactional()) {
			return numHome.getNextPg(userContext, docTR.getEsercizio(),
					docTR.getPg_inventario(), docTR.getTiDocumento(), userContext.getUser());
		} else {
			NumerazioneTempDocTRComponentSession session = (NumerazioneTempDocTRComponentSession)
					EJBCommonServices.createEJB("CNRINVENTARIO01_EJB_NumerazioneTempDocTRComponentSession",
							NumerazioneTempDocTRComponentSession.class);
			return session.getNextTempPG(userContext, docTR);
		}
	}

	/**
	 * Crea un SQLBuilder con le join standard tra testata e dettaglio
	 */
	private SQLBuilder createSQLBuilderWithJoins() {
		SQLBuilder sql = createSQLBuilder();
		sql.addTableToHeader(TABLE_DETT);
		sql.addSQLJoin(TABLE_DETT + ".ESERCIZIO", "DOC_TRASPORTO_RIENTRO.ESERCIZIO");
		sql.addSQLJoin(TABLE_DETT + ".PG_INVENTARIO", "DOC_TRASPORTO_RIENTRO.PG_INVENTARIO");
		sql.addSQLJoin(TABLE_DETT + ".TI_DOCUMENTO", "DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO");
		sql.addSQLJoin(TABLE_DETT + ".PG_DOC_TRASPORTO_RIENTRO", "DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO");
		return sql;
	}

	/**
	 * Aggiunge le clausole WHERE standard per il documento
	 */
	private void addDocumentWhereClause(SQLBuilder sql, Doc_trasporto_rientroBulk docTR) {
		sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
		sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.PG_INVENTARIO", sql.EQUALS, docTR.getPg_inventario());
		sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
		sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS,
				docTR.getPgDocTrasportoRientro());
	}

	// ========================================
	// METODI DI VERIFICA STATO
	// ========================================

	/**
	 * Verifica se esistono dettagli con lo stato specificato
	 */
	private boolean hasDettagliConStato(Doc_trasporto_rientroBulk docTR, String... stati) throws SQLException {
		SQLBuilder sql = createSQLBuilderWithJoins();
		addDocumentWhereClause(sql, docTR);

		if (stati.length == 1) {
			sql.addSQLClause("AND", TABLE_DETT + ".STATO_TRASPORTO", sql.EQUALS, stati[0]);
		} else {
			sql.openParenthesis("AND");
			for (int i = 0; i < stati.length; i++) {
				sql.addSQLClause(i == 0 ? "AND" : "OR", TABLE_DETT + ".STATO_TRASPORTO", sql.EQUALS, stati[i]);
			}
			sql.closeParenthesis();
		}

		return sql.executeExistsQuery(getConnection());
	}

	/**
	 * Verifica se il documento ha dettagli con stato definitivo
	 */
	public boolean hasDettagliDefinitivi(Doc_trasporto_rientroBulk docTR) throws SQLException {
		return hasDettagliConStato(docTR, Doc_trasporto_rientro_dettBulk.STATO_DEFINITIVO);
	}

	/**
	 * Verifica se il documento ha dettagli annullati
	 */
	public boolean hasDettagliAnnullati(Doc_trasporto_rientroBulk docTR) throws SQLException {
		return hasDettagliConStato(docTR, Doc_trasporto_rientro_dettBulk.STATO_ANNULLATO);
	}

	/**
	 * Verifica se il documento è modificabile
	 * Un documento NON è modificabile se ha dettagli DEFINITIVI o PREDISPOSTI ALLA FIRMA
	 */
	public boolean isDocumentoModificabile(Doc_trasporto_rientroBulk docTR) throws SQLException {
		return !hasDettagliConStato(docTR,
				Doc_trasporto_rientro_dettBulk.STATO_DEFINITIVO,
				Doc_trasporto_rientro_dettBulk.STATO_PREDISPOSTO_FIRMA);
	}

	/**
	 * Verifica se il documento di rientro ha un riferimento valido al trasporto originale
	 */
	public boolean hasRiferimentoTrasporto(Doc_trasporto_rientroBulk docRientro) throws SQLException {
		if (!Doc_trasporto_rientroBulk.RIENTRO.equals(docRientro.getTiDocumento())) {
			return false;
		}

		SQLBuilder sql = createSQLBuilderWithJoins();
		addDocumentWhereClause(sql, docRientro);
		sql.addSQLClause("AND", TABLE_DETT + ".PG_INVENTARIO_RIF", sql.ISNOTNULL, null);
		sql.addSQLClause("AND", TABLE_DETT + ".PG_DOC_TRASPORTO_RIENTRO_RIF", sql.ISNOTNULL, null);

		return sql.executeExistsQuery(getConnection());
	}

	/**
	 * Conta il numero di beni presenti nel documento
	 */
	public int contaBeniDocumento(Doc_trasporto_rientroBulk docTR) throws PersistencyException, SQLException {
		SQLBuilder sql = createSQLBuilder();
		sql.addTableToHeader(TABLE_DETT);
		sql.addSQLClause("AND", TABLE_DETT + ".ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
		sql.addSQLClause("AND", TABLE_DETT + ".PG_INVENTARIO", sql.EQUALS, docTR.getPg_inventario());
		sql.addSQLClause("AND", TABLE_DETT + ".TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
		sql.addSQLClause("AND", TABLE_DETT + ".PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS,
				docTR.getPgDocTrasportoRientro());

		return sql.executeCountQuery(getConnection());
	}

	/**
	 * Verifica se il documento ha dettagli
	 */
	public boolean hasDettagli(Doc_trasporto_rientroBulk docTR) throws PersistencyException, SQLException {
		return contaBeniDocumento(docTR) > 0;
	}
}