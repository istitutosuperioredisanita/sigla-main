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

import it.cnr.contab.inventario00.dto.NormalizzatoreAmmortamentoDto;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.LoggableStatement;

public class V_inventario_bene_detHome extends BulkHome {
	private final String StatmentSelectNormalizzatoreBene =
	" SELECT norm.esercizio_carico_bene,norm.pg_inventario,norm.nr_inventario,norm.progressivo,norm.etichetta,norm.cd_categoria_gruppo,"+
			" SUM(NVL(norm.valore_bene,0)) as valore_bene, "+
			" SUM(NVL(norm.incremento_valore,0)) as incrementi_successivi, "+
			" SUM(NVL(norm.decremento_valore,0)) as decrementi_successivi, "+
			" SUM(NVL(norm.quota_ammortamento,0)) as quota_ammortamenti_precedenti, "+
			" SUM(NVL(norm.valore_ammortizzato,0)) as valore_ammortizzato, "+
			" SUM(NVL(norm.imponibile_ammortamento,0)) as imponibile_ammortamento, "+
			" SUM(NVL(norm.storno,0)) as quota_storno "+
		"FROM ( SELECT "+
					"esercizio_carico_bene,pg_inventario,nr_inventario,progressivo,etichetta,cd_categoria_gruppo,"+
					" ((valore_iniziale+variazione_piu)-variazione_meno) as valore_bene," +
					"incremento_valore,"+
					"decremento_valore,"+
					"quota_ammortamento,"+
					"valore_ammortizzato,"+
					"imponibile_ammortamento,"+
					"storno " +
			   "FROM V_INVENTARIO_BENE_DET  " +
			   "WHERE ESERCIZIO_CARICO_BENE <= ? and " +
					 "PG_INVENTARIO = ? and " +
			         "NR_INVENTARIO = ? and " +
					 "PROGRESSIVO=? and "+
					 "(TIPORECORD = 'VALORE' or TIPORECORD = 'AMMORTAMENTO' or "+
				     "(TIPORECORD = 'INCREMENTENTO' and esercizio_buono_carico > ?) or "+
					 "(TIPORECORD = 'DECREMENTENTO' and esercizio_buono_carico > ?) or "+
			         "(TIPORECORD = 'STORNO' and esercizio_buono_carico > ?)) ) norm"+
		"GROUP BY esercizio_carico_bene,pg_inventario,nr_inventario,progressivo,etichetta,cd_categoria_gruppo";

	public V_inventario_bene_detHome(Connection conn) {
		super(V_inventario_bene_detBulk.class, conn);
	}
	public V_inventario_bene_detHome(Connection conn, PersistentCache persistentCache) {
		super(V_inventario_bene_detBulk.class, conn, persistentCache);
	}

	public NormalizzatoreAmmortamentoDto findBeneDaAmmortizare(Integer esercizio,Long pgInventario,Long nrInventario,Long progressivo) throws PersistencyException {
		NormalizzatoreAmmortamentoDto normalizzatoreAmmortamentoDto = null;

		try {
			String statement = StatmentSelectNormalizzatoreBene;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, esercizio);
				ps.setLong(2, pgInventario);
				ps.setLong(3, nrInventario);
				ps.setLong(4, progressivo);

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {

						normalizzatoreAmmortamentoDto =  getNormalizzatoreBene(rs);
						break;
					}
					return normalizzatoreAmmortamentoDto;

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
	private NormalizzatoreAmmortamentoDto getNormalizzatoreBene(ResultSet rs) throws SQLException {

		NormalizzatoreAmmortamentoDto norm = new NormalizzatoreAmmortamentoDto();
		norm.setEsercizioCaricoBene(rs.getInt(1));
		norm.setPgInventario(rs.getInt(2));
		norm.setNrInventario(rs.getInt(3));
		norm.setProgressivo(rs.getInt(4));
		norm.setEtichetta(rs.getString(5));
		norm.setCdCatGruppo(rs.getString(6));
		norm.setValoreBene(rs.getBigDecimal(7));
		norm.setIncrementiSuccessivi(rs.getBigDecimal(8));
		norm.setDecrementiSuccessivi(rs.getBigDecimal(9));
		norm.setQuotaAmmortamentiPrecedenti(rs.getBigDecimal(10));
		norm.setValoreAmmortizzato(rs.getBigDecimal(11));
		norm.setImponibileAmmortamento(rs.getBigDecimal(12));
		norm.setQuotaStorniSuccessivi(rs.getBigDecimal(13));

		return norm;
	}
}