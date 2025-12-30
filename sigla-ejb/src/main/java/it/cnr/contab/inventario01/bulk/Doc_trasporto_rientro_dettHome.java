/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

	public Doc_trasporto_rientro_dettHome(Class classe, java.sql.Connection conn) {
		super(classe, conn);
	}

	public Doc_trasporto_rientro_dettHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
		super(classe, conn, persistentCache);
	}
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
			throws it.cnr.jada.persistency.PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		// Usa i nomi delle COLONNE DATABASE, non delle proprietà Java
		sql.addSQLClause(FindClause.AND, "ESERCIZIO",
				SQLBuilder.EQUALS, doc.getEsercizio());
		sql.addSQLClause(FindClause.AND, "PG_INVENTARIO",
				SQLBuilder.EQUALS, doc.getPgInventario());
		sql.addSQLClause(FindClause.AND, "TI_DOCUMENTO",
				SQLBuilder.EQUALS, doc.getTiDocumento());
		sql.addSQLClause(FindClause.AND, "PG_DOC_TRASPORTO_RIENTRO",
				SQLBuilder.EQUALS, doc.getPgDocTrasportoRientro());

		sql.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

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


	public void escludiBeniInDocumentiTrasporto(
			SQLBuilder sql,
			Long pg_inventario,
			boolean escludiSoloDocumentiNonAnnullati) throws IntrospectionException {

		// Creo un SQLBuilder per la subquery NOT EXISTS
		SQLBuilder subquery = createSQLBuilder();

		// Aggiungo la tabella DOC_TRASPORTO_RIENTRO_DETT come tabella principale
		subquery.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT", "DTR_DETT");

		// Se necessario, aggiungo anche la tabella HEAD e i JOIN
		if (escludiSoloDocumentiNonAnnullati) {
			// IMPORTANTE: Devo aggiungere anche DOC_TRASPORTO_RIENTRO al FROM
			subquery.addTableToHeader("DOC_TRASPORTO_RIENTRO", "DTR_HEAD");

			// Aggiungo i join tra DETT e HEAD usando i 4 campi della PK
			subquery.addSQLJoin("DTR_DETT.PG_INVENTARIO", "DTR_HEAD.PG_INVENTARIO");
			subquery.addSQLJoin("DTR_DETT.TI_DOCUMENTO", "DTR_HEAD.TI_DOCUMENTO");
			subquery.addSQLJoin("DTR_DETT.ESERCIZIO", "DTR_HEAD.ESERCIZIO");
			subquery.addSQLJoin("DTR_DETT.PG_DOC_TRASPORTO_RIENTRO", "DTR_HEAD.PG_DOC_TRASPORTO_RIENTRO");
		}

		// Aggiungo la condizione WHERE sul PG_INVENTARIO
		subquery.addSQLClause("AND", "DTR_DETT.PG_INVENTARIO", SQLBuilder.EQUALS, pg_inventario);

		// Aggiungo le condizioni di correlazione con la query principale (INVENTARIO_BENI)
		String mainTableAlias = sql.getColumnMap() != null ?
				sql.getColumnMap().getTableName() : "INVENTARIO_BENI";

		// Correlazione sui campi NR_INVENTARIO e PROGRESSIVO
		subquery.addSQLClause("AND", "DTR_DETT.NR_INVENTARIO = " + mainTableAlias + ".NR_INVENTARIO");
		subquery.addSQLClause("AND", "DTR_DETT.PROGRESSIVO = " + mainTableAlias + ".PROGRESSIVO");

		// Se necessario, aggiungo la condizione sullo STATO (esclude gli annullati)
		if (escludiSoloDocumentiNonAnnullati) {
			subquery.addSQLClause("AND", "DTR_HEAD.STATO", SQLBuilder.NOT_EQUALS, "ANN");
		}

		// Aggiungo la clausola NOT EXISTS alla query principale
		sql.addSQLNotExistsClause("AND", subquery);
	}


	/**
	 * Restituisce la MAX(dt_validita_variazione) tra tutti i beni
	 * presenti nei dettagli del documento.
	 *
	 * @param doc il documento di trasporto/rientro
	 * @return la data di validità più recente, null se non ci sono dettagli
	 */
	public java.sql.Timestamp getMaxDataValiditaVariazione(Doc_trasporto_rientroBulk doc)
			throws PersistencyException {

		try {
			// Query per trovare MAX(dt_validita_variazione) con JOIN a INVENTARIO_BENI
			String sql =
					"SELECT MAX(IB.DT_VALIDITA_VARIAZIONE) " +
							"FROM DOC_TRASPORTO_RIENTRO_DETT DETT " +
							"INNER JOIN INVENTARIO_BENI IB ON ( " +
							"  DETT.PG_INVENTARIO = IB.PG_INVENTARIO AND " +
							"  DETT.NR_INVENTARIO = IB.NR_INVENTARIO AND " +
							"  DETT.PROGRESSIVO = IB.PROGRESSIVO " +
							") " +
							"WHERE DETT.PG_INVENTARIO = ? " +
							"  AND DETT.TI_DOCUMENTO = ? " +
							"  AND DETT.ESERCIZIO = ? " +
							"  AND DETT.PG_DOC_TRASPORTO_RIENTRO = ?";

			java.sql.PreparedStatement ps = getConnection().prepareStatement(sql);
			try {
				ps.setLong(1, doc.getPgInventario());
				ps.setString(2, doc.getTiDocumento());
				ps.setInt(3, doc.getEsercizio());
				ps.setLong(4, doc.getPgDocTrasportoRientro());

				java.sql.ResultSet rs = ps.executeQuery();
				try {
					if (rs.next()) {
						return rs.getTimestamp(1);
					}
					return null;

				} finally {
					rs.close();
				}
			} finally {
				ps.close();
			}

		} catch (java.sql.SQLException e) {
			throw new PersistencyException(e);
		}
	}

}