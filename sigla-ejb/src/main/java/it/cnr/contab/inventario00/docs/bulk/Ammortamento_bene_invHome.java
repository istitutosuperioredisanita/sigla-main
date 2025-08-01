/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Ammortamento_bene_invHome extends BulkHome {

	private final String statmentSelectAllAmmortamenti =
			"SELECT PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,ESERCIZIO,CD_TIPO_AMMORTAMENTO,TI_AMMORTAMENTO," +
					"CD_CATEGORIA_GRUPPO,ESERCIZIO_COMPETENZA,IMPONIBILE_AMMORTAMENTO,IM_MOVIMENTO_AMMORT," +
					"PERC_AMMORTAMENTO,NUMERO_ANNI,NUMERO_ANNO,PERC_PRIMO_ANNO," +
					"PERC_SUCCESSIVI,CD_CDS_UBICAZIONE,CD_UO_UBICAZIONE,FL_STORNO,PG_RIGA,PG_BUONO_S "+
			"FROM "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"AMMORTAMENTO_BENE_INV "+
			"WHERE ESERCIZIO = ? AND FL_STORNO='N' ";

	private final String statmentCountNumeroAnno =
			" SELECT NVL(COUNT(*)+1,1) " +
			" FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"AMMORTAMENTO_BENE_INV " +
			" WHERE  pg_inventario = ? AND " +
			"        nr_inventario = ? AND" +
			"        progressivo = ?AND " +
			"        fl_storno ='N'";

	private final String statmentProgressivoRiga =
			"SELECT NVL(Max(PG_RIGA)+1,1)  " +
			"FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"AMMORTAMENTO_BENE_INV  " +
			"WHERE  pg_inventario = ? AND " +
			"		nr_inventario = ? AND " +
			"		progressivo = ? AND " +
			"		esercizio = ?";


	public Ammortamento_bene_invHome(Connection conn) {
		super(Ammortamento_bene_invBulk.class, conn);
	}
	public Ammortamento_bene_invHome(Connection conn, PersistentCache persistentCache) {
		super(Ammortamento_bene_invBulk.class, conn, persistentCache);
	}

	public List<Ammortamento_bene_invBulk> getAllAmmortamentoEsercizio(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = createSQLBuilder();

		sql.addSQLClause( "AND", "FL_STORNO", SQLBuilder.EQUALS, "N");
		sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);

		return this.fetchAll(sql);
	}

	public Boolean isExistAmmortamentoEsercizio(Integer esercizio) throws PersistencyException {

		List<Ammortamento_bene_invBulk> list = null;

		try {
			String statement = statmentSelectAllAmmortamenti;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, esercizio);
				ResultSet rs = ps.executeQuery();

				try {
					if (rs.next()) {
						return true;
					}
					return false;

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

	private Ammortamento_bene_invBulk getAmmortamentoBeneInv(ResultSet rs) throws SQLException {

		Ammortamento_bene_invBulk ammortamento = new Ammortamento_bene_invBulk();
		ammortamento.setPgInventario(rs.getLong(1));
		ammortamento.setNrInventario(rs.getLong(2));
		ammortamento.setProgressivo(rs.getLong(3));
		ammortamento.setEsercizio(rs.getInt(4));
		ammortamento.setCdTipoAmmortamento(rs.getString(5));
		ammortamento.setTiAmmortamento(rs.getString(6));
		ammortamento.setCdCategoriaGruppo(rs.getString(7));
		ammortamento.setEsercizioCompetenza(rs.getInt(8));
		ammortamento.setImponibileAmmortamento(rs.getBigDecimal(9));
		ammortamento.setImMovimentoAmmort(rs.getBigDecimal(10));
		ammortamento.setPercAmmortamento(rs.getBigDecimal(11));
		ammortamento.setNumeroAnni(rs.getInt(12));
		ammortamento.setNumeroAnno(rs.getInt(13));
		ammortamento.setPercPrimoAnno(rs.getBigDecimal(14));
		ammortamento.setPercSuccessivi(rs.getBigDecimal(15));
		ammortamento.setCdCdsUbicazione(rs.getString(16));
		ammortamento.setCdUoUbicazione(rs.getString(17));
		ammortamento.setFlStorno(rs.getBoolean(18));
		ammortamento.setPgRiga(rs.getInt(19));
		ammortamento.setPgBuonoS(rs.getLong(20));

		return ammortamento;
	}
	public Integer getNumeroAnnoAmmortamento(Long pgInventario,Long nrInventario,Long progressivo) throws PersistencyException {
		Integer numeroAnno=null;

		try {
			String statement = statmentCountNumeroAnno;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setLong(1, pgInventario);
				ps.setLong(2, nrInventario);
				ps.setLong(3, progressivo);

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {
						numeroAnno=rs.getInt(1);
						break;
					}
					return numeroAnno;
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
	public Integer getProgressivoRigaAmmortamento(Long pgInventario,Long nrInventario,Long progressivo,Integer esercizio) throws PersistencyException {
		Integer pgRiga=null;

		try {
			String statement = statmentProgressivoRiga;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setLong(1, pgInventario);
				ps.setLong(2, nrInventario);
				ps.setLong(3, progressivo);
				ps.setInt(4,esercizio);

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {
						pgRiga=rs.getInt(0);
						break;
					}
					return pgRiga;
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

	public String inserimentoSqlAmmortamentoBene(UserContext uc, Ammortamento_bene_invBulk amm){
		return " INSERT INTO "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"AMMORTAMENTO_BENE_INV ( "+
				"PG_INVENTARIO,"+
				"NR_INVENTARIO,"+
				"PROGRESSIVO,"+
				"ESERCIZIO,"+
				"CD_TIPO_AMMORTAMENTO,"+
				"TI_AMMORTAMENTO,"+
				"CD_CATEGORIA_GRUPPO,"+
				"ESERCIZIO_COMPETENZA,"+
				"IMPONIBILE_AMMORTAMENTO,"+
				"IM_MOVIMENTO_AMMORT,"+
				"PERC_AMMORTAMENTO,"+
				"DACR,"+
				"UTCR,"+
				"DUVA,"+
				"UTUV,"+
				"PG_VER_REC,"+
				"NUMERO_ANNI,"+
				"NUMERO_ANNO,"+
				"PERC_PRIMO_ANNO,"+
				"PERC_SUCCESSIVI,"+
				"CD_CDS_UBICAZIONE,"+
				"CD_UO_UBICAZIONE,"+
				"FL_STORNO,"+
				"PG_RIGA,"+
				"PG_BUONO_S ) "+
				"VALUES ("+amm.getPgInventario()+","+amm.getNrInventario()+","+amm.getProgressivo()+","+amm.getEsercizio()+",'" +
				amm.getCdTipoAmmortamento()+"','"+amm.getTiAmmortamento()+"','"+amm.getCdCategoriaGruppo()+"',"+amm.getEsercizioCompetenza()+"," +
				amm.getImponibileAmmortamento()+","+amm.getImMovimentoAmmort()+","+amm.getPercAmmortamento()+",SYSDATE,'SI',SYSDATE,'SI',1," +
				amm.getNumeroAnni()+","+amm.getNumeroAnno()+","+amm.getPercPrimoAnno()+","+amm.getPercSuccessivi()+","+amm.getCdCdsUbicazione()+"," +
				amm.getCdUoUbicazione()+","+ (amm.getFlStorno()?"'S'":"'N'") +","+amm.getPgRiga()+","+amm.getPgBuonoS()+")";


	}
	public String deleteSqlAmmortamento(UserContext uc, Integer esercizio){
		return  "DELETE FROM "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"AMMORTAMENTO_BENE_INV "+
		"WHERE  esercizio = "+esercizio+ " AND fl_storno = 'N'";
	}
}