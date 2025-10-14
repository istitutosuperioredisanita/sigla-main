/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.docamm00.tabrif.bulk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

/**
 * Home per Doc_trasporto_rientro_dettBulk.
 *
 * METODI CHIAVE PER FILTRO UO 
 * - findBeniTrasportabiliPerUO: filtra beni per UO (utente o dipendente)
 * - beneBelongsToUO: verifica appartenenza bene a UO
 * - contaBeniTrasportabiliPerUO: conta beni disponibili per UO
 */
public class Doc_trasporto_rientro_dettHome extends BulkHome {

	private static final String TABLE_INVENTARIO_BENI = "INVENTARIO_BENI";

	public Doc_trasporto_rientro_dettHome(Connection conn) {
		super(Doc_trasporto_rientro_dettBulk.class, conn);
	}

	public Doc_trasporto_rientro_dettHome(Connection conn, PersistentCache persistentCache) {
		super(Doc_trasporto_rientro_dettBulk.class, conn, persistentCache);
	}

	/**
	 * Recupera tutti i dettagli di un documento
	 */
	public java.util.List getDetailsFor(Doc_trasporto_rientroBulk doc)
			throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND", "esercizio", sql.EQUALS, doc.getEsercizio());
		sql.addClause("AND", "pg_inventario", sql.EQUALS, doc.getPg_inventario());
		sql.addClause("AND", "TI_DOCUMENTO", sql.EQUALS, doc.getTiDocumento());
		sql.addClause("AND", "PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS, doc.getPgDocTrasportoRientro());

		return fetchAll(sql);
	}

	/**
	 * METODO PRINCIPALE 
	 * Cerca i beni trasportabili FILTRATI per unità organizzativa.
	 *
	 * L'UO può essere:
	 * - UO dell'utente loggato (se nessun flag ritiro delegato)
	 * - UO del dipendente (se flag ritiro delegato attivo)
	 *
	 * Restituisce SOLO i beni che:
	 * 1. Appartengono all'UO specificata (FILTRO PRINCIPALE)
	 * 2. Non sono totalmente scaricati
	 * 3. Non sono in transito
	 * 4. Sono disponibili per il trasporto
	 *
	 * @param cd_unita_organizzativa UO da filtrare (utente o dipendente)
	 * @param pg_inventario inventario
	 * @param beni_esclusi beni già selezionati da escludere
	 * @return List di beni disponibili
	 */
	public List findBeniTrasportabiliPerUO(
			String cd_unita_organizzativa,
			Long pg_inventario,
			List beni_esclusi)
			throws PersistencyException, SQLException {

		SQLBuilder sql = createSQLBuilder();

		// Seleziona dalla tabella INVENTARIO_BENI
		sql.addTableToHeader(TABLE_INVENTARIO_BENI, "IB");

		// FILTRO 1: Inventario
		sql.addClause("AND", "IB.PG_INVENTARIO", sql.EQUALS, pg_inventario);

		// FILTRO 2: UNITÀ ORGANIZZATIVA (FILTRO PRINCIPALE) 
		// Questo limita i beni SOLO a quelli dell'UO specificata
		// (può essere l'UO dell'utente loggato o del dipendente)
		if (cd_unita_organizzativa != null) {
			sql.addClause("AND", "IB.CD_UNITA_ORGANIZZATIVA", sql.EQUALS, cd_unita_organizzativa);
		}

		// FILTRO 3: Esclude i beni totalmente scaricati
		sql.openParenthesis("AND");
		sql.addClause("AND", "IB.FL_TOTALMENTE_SCARICATO", sql.ISNULL, null);
		sql.addClause("OR", "IB.FL_TOTALMENTE_SCARICATO", sql.EQUALS, "N");
		sql.closeParenthesis();

		// FILTRO 4: Esclude i beni in transito
		sql.addClause("AND", "IB.ID_TRANSITO_BENI_ORDINI", sql.ISNULL, null);

		// FILTRO 5: Esclude i beni già selezionati
		if (beni_esclusi != null && !beni_esclusi.isEmpty()) {
			escludiBeniDallaQuery(sql, beni_esclusi);
		}

		sql.addOrderBy("IB.NR_INVENTARIO, IB.PROGRESSIVO");

		return fetchAll(sql);
	}

	/**
	 * Helper per escludere beni già selezionati (CORRETTO)
	 *
	 * Per ogni bene da escludere aggiunge una clausola:
	 * NOT (NR_INVENTARIO = X AND PROGRESSIVO = Y)
	 * che equivale a:
	 * (NR_INVENTARIO <> X OR PROGRESSIVO <> Y)
	 */
	private void escludiBeniDallaQuery(SQLBuilder sql, List beni_esclusi) {
		if (beni_esclusi == null || beni_esclusi.isEmpty()) {
			return;
		}

		// Per ogni bene escluso, aggiungiamo una condizione
		for (Object obj : beni_esclusi) {
			Inventario_beniBulk bene = null;

			if (obj instanceof Doc_trasporto_rientro_dettBulk) {
				bene = ((Doc_trasporto_rientro_dettBulk) obj).getBene();
			} else if (obj instanceof Inventario_beniBulk) {
				bene = (Inventario_beniBulk) obj;
			}

			if (bene != null && bene.getNr_inventario() != null && bene.getProgressivo() != null) {
				// Aggiungiamo: AND (NR_INVENTARIO <> X OR PROGRESSIVO <> Y)
				// Questo esclude il bene solo se ENTRAMBI nr_inventario E progressivo corrispondono
				sql.openParenthesis("AND");
				sql.addClause("AND", TABLE_INVENTARIO_BENI + ".NR_INVENTARIO",
						sql.NOT_EQUALS, bene.getNr_inventario());
				sql.addClause("OR", TABLE_INVENTARIO_BENI + ".PROGRESSIVO",
						sql.NOT_EQUALS, bene.getProgressivo());
				sql.closeParenthesis();
			}
		}
	}

	/**
	 * Verifica che un bene appartenga all'UO specificata
	 *
	 * @param bene bene da verificare
	 * @param cd_unita_organizzativa UO da verificare
	 * @return true se il bene appartiene all'UO
	 */
	public boolean beneBelongsToUO(Inventario_beniBulk bene, String cd_unita_organizzativa)
			throws PersistencyException, SQLException {

		if (bene == null || cd_unita_organizzativa == null) {
			return false;
		}

		SQLBuilder sql = createSQLBuilder();
		sql.addTableToHeader(TABLE_INVENTARIO_BENI);

		sql.addClause("AND", "NR_INVENTARIO", sql.EQUALS, bene.getNr_inventario());
		sql.addClause("AND", "PG_INVENTARIO", sql.EQUALS, bene.getPg_inventario());
		sql.addClause("AND", "PROGRESSIVO", sql.EQUALS, bene.getProgressivo());
		sql.addClause("AND", "CD_UNITA_ORGANIZZATIVA", sql.EQUALS, cd_unita_organizzativa);

		return sql.executeExistsQuery(getConnection());
	}

	/**
	 * Conta i beni disponibili per un'UO
	 */
	public int contaBeniTrasportabiliPerUO(String cd_unita_organizzativa, Long pg_inventario)
			throws PersistencyException, SQLException {

		SQLBuilder sql = createSQLBuilder();
		sql.addTableToHeader(TABLE_INVENTARIO_BENI, "IB");

		sql.addClause("AND", "IB.PG_INVENTARIO", sql.EQUALS, pg_inventario);
		sql.addClause("AND", "IB.CD_UNITA_ORGANIZZATIVA", sql.EQUALS, cd_unita_organizzativa);

		sql.openParenthesis("AND");
		sql.addClause("AND", "IB.FL_TOTALMENTE_SCARICATO", sql.ISNULL, null);
		sql.addClause("OR", "IB.FL_TOTALMENTE_SCARICATO", sql.EQUALS, "N");
		sql.closeParenthesis();

		sql.addClause("AND", "IB.ID_TRANSITO_BENI_ORDINI", sql.ISNULL, null);

		return sql.executeCountQuery(getConnection());
	}

	/**
	 * Recupera l'UO di un bene
	 */
	/**
	 * Recupera l'UO di un bene
	 */
	public String getUnitaOrganizzativaBene(Inventario_beniBulk bene)
			throws PersistencyException {

		if (bene == null) {
			return null;
		}

		SQLBuilder sql = createSQLBuilder();
		sql.addTableToHeader(TABLE_INVENTARIO_BENI);
		sql.addColumn("CD_UNITA_ORGANIZZATIVA");

		sql.addClause(FindClause.AND, "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
		sql.addClause(FindClause.AND, "PG_INVENTARIO", SQLBuilder.EQUALS, bene.getPg_inventario());
		sql.addClause(FindClause.AND, "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());

		java.util.Collection<?> results = fetchAll(sql);

		if (results == null || results.isEmpty()) {
			return null;
		}

		Inventario_beniBulk record = (Inventario_beniBulk) results.iterator().next();
		return record.getCd_unita_organizzativa();
	}

}