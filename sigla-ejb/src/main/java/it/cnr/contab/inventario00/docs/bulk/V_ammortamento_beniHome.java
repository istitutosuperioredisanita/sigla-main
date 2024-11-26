/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.cnr.contab.util.RemoveAccent;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class V_ammortamento_beniHome extends BulkHome {

	private final String statmentSelectAllBeniDaAmmortizare = "" +
			"SELECT PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,CD_CATEGORIA_GRUPPO,TI_AMMORTAMENTO_BENE," +
					"FL_AMMORTAMENTO,VALORE_INIZIALE,VALORE_AMMORTIZZATO,VARIAZIONE_PIU,VARIAZIONE_MENO," +
					"IMPONIBILE_AMMORTAMENTO,FL_TOTALMENTE_SCARICATO,ESERCIZIO_CARICO_BENE,CD_CDS," +
					"CD_UNITA_ORGANIZZATIVA,ESERCIZIO_COMPETENZA,CD_TIPO_AMMORTAMENTO,TI_AMMORTAMENTO," +
					"DT_CANCELLAZIONE,PERC_PRIMO_ANNO,PERC_SUCCESSIVI,NUMERO_ANNI "+
			"FROM V_AMMORTAMENTO_BENI "+
			"WHERE (ESERCIZIO_COMPETENZA = ? OR ESERCIZIO_COMPETENZA is null) AND "+
			       "ESERCIZIO_CARICO_BENE < = ?";

	public V_ammortamento_beniHome(Connection conn) {
		super(V_ammortamento_beniBulk.class, conn);
	}
	public V_ammortamento_beniHome(Connection conn, PersistentCache persistentCache) {
		super(V_ammortamento_beniBulk.class, conn, persistentCache);
	}

/*	public List<V_ammortamento_beniBulk> findAllBeniDaAmmortizare(Integer esercizio) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.openParenthesis("AND");
		sql.addSQLClause("OR","ESERCIZIO_COMPETENZA",SQLBuilder.EQUALS,esercizio);
		sql.addSQLClause("OR","ESERCIZIO_COMPETENZA",SQLBuilder.EQUALS, null);
		sql.closeParenthesis();
		sql.addClause(FindClause.AND, "ESERCIZIO_CARICO_BENE", SQLBuilder.LESS_EQUALS, esercizio);

		return fetchAll(sql);

	}*/
	public List<V_ammortamento_beniBulk> findAllBeniDaAmmortizare(Integer esercizio) throws PersistencyException {
		List<V_ammortamento_beniBulk> list = null;

		try {
			String statement = statmentSelectAllBeniDaAmmortizare;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, esercizio);
				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {

						if (list == null) {
							list = new ArrayList<V_ammortamento_beniBulk>();
						}
						list.add(getBeneDaAmmortizzare(rs));

					}
					return list;
				} catch (Exception e) {
					throw new PersistencyException(e);
				} finally {
					try {
						rs.close();
					} catch (java.sql.SQLException e) {
					}

				}

			} catch (SQLException e) {
				throw new PersistencyException(e);
			} finally {
				try {
					ps.close();

				} catch (java.sql.SQLException e) {
				}
			}
		} catch (SQLException e) {
			throw new PersistencyException(e);
		}
	}
	private V_ammortamento_beniBulk getBeneDaAmmortizzare(ResultSet rs) throws SQLException {

		V_ammortamento_beniBulk bene = new V_ammortamento_beniBulk();
		bene.setPgInventario(rs.getLong(1));
		bene.setNrInventario(rs.getLong(2));
		bene.setProgressivo(rs.getLong(3));
		bene.setCdCategoriaGruppo(rs.getString(4));
		bene.setTiAmmortamentoBene(rs.getString(5));
		bene.setFlAmmortamento(rs.getBoolean(6));
		bene.setValoreIniziale(rs.getBigDecimal(7));
		bene.setValoreAmmortizzato(rs.getBigDecimal(8));
		bene.setVariazionePiu(rs.getBigDecimal(9));
		bene.setVariazioneMeno(rs.getBigDecimal(10));
		bene.setImponibileAmmortamento(rs.getBigDecimal(11));
		bene.setFlTotalmenteScaricato(rs.getBoolean(12));
		bene.setEsercizioCaricoBene(rs.getInt(13));
		bene.setCdCds(rs.getString(14));
		bene.setCdUnitaOrganizzativa(rs.getString(15));
		bene.setEsercizioCompetenza(rs.getInt(16));
		bene.setCdTipoAmmortamento(rs.getString(17));
		bene.setTiAmmortamentoBene(rs.getString(18));
		bene.setDtCancellazione(rs.getTimestamp(19));
		bene.setPercPrimoAnno(rs.getBigDecimal(20));
		bene.setPercSuccessivi(rs.getBigDecimal(21));
		bene.setNumeroAnni(rs.getInt(22));

		return bene;
	}
}