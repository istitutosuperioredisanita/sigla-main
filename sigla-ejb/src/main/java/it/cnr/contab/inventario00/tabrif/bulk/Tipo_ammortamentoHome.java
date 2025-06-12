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

package it.cnr.contab.inventario00.tabrif.bulk;


import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.dto.TipoAmmCatGruppoDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.beans.*;
import it.cnr.jada.persistency.sql.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Tipo_ammortamentoHome extends BulkHome {

	private final String statmentSelectTipoAmmNoTipoAmm =
			"SELECT aTA.CD_TIPO_AMMORTAMENTO,aTA.TI_AMMORTAMENTO,aTA.DS_TIPO_AMMORTAMENTO,aTA.PERC_PRIMO_ANNO," +
					"aTA.PERC_SUCCESSIVI,aTA.NUMERO_ANNI,aTA.DT_CANCELLAZIONE " +
			"FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"TIPO_AMMORTAMENTO aTA "+
			"WHERE EXISTS (SELECT 1 " +
						   "FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"ASS_TIPO_AMM_CAT_GRUP_INV " +
			               "WHERE  CD_CATEGORIA_GRUPPO = ? AND " +
			                      "CD_TIPO_AMMORTAMENTO = aTA.CD_TIPO_AMMORTAMENTO AND " +
								  "TI_AMMORTAMENTO = aTA.TI_AMMORTAMENTO AND " +
								  "ESERCIZIO_COMPETENZA = ?)";

	private final String statmentSelectTipoAmmConTipoAmm =
			"SELECT aTA.CD_TIPO_AMMORTAMENTO,aTA.TI_AMMORTAMENTO,aTA.DS_TIPO_AMMORTAMENTO,aTA.PERC_PRIMO_ANNO," +
			"aTA.PERC_SUCCESSIVI,aTA.NUMERO_ANNI,aTA.DT_CANCELLAZIONE " +
			"FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"TIPO_AMMORTAMENTO aTA "+
			"WHERE aTA.TI_AMMORTAMENTO = ? AND "+
			"EXISTS (SELECT 1 " +
				"FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"ASS_TIPO_AMM_CAT_GRUP_INV " +
				"WHERE  CD_CATEGORIA_GRUPPO = ? AND " +
				"CD_TIPO_AMMORTAMENTO = aTA.CD_TIPO_AMMORTAMENTO AND " +
				"TI_AMMORTAMENTO = aTA.TI_AMMORTAMENTO AND " +
				"ESERCIZIO_COMPETENZA = ?)";

	private final String statmentSelectAllTipoAmm =
			"SELECT ass.CD_CATEGORIA_GRUPPO,aTA.CD_TIPO_AMMORTAMENTO,aTA.TI_AMMORTAMENTO,aTA.DS_TIPO_AMMORTAMENTO,aTA.PERC_PRIMO_ANNO," +
			"aTA.PERC_SUCCESSIVI,aTA.NUMERO_ANNI,aTA.DT_CANCELLAZIONE " +
			"FROM   "+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"TIPO_AMMORTAMENTO aTA ," +
			"		"+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"ASS_TIPO_AMM_CAT_GRUP_INV ass " +
			"WHERE ass.CD_TIPO_AMMORTAMENTO = aTA.CD_TIPO_AMMORTAMENTO AND " +
			"      ass.TI_AMMORTAMENTO = aTA.TI_AMMORTAMENTO AND " +
			"      ass.ESERCIZIO_COMPETENZA=?";

	public Tipo_ammortamentoHome(java.sql.Connection conn) {
	super(Tipo_ammortamentoBulk.class,conn);
}
public Tipo_ammortamentoHome(java.sql.Connection conn,PersistentCache persistentCache) {
	super(Tipo_ammortamentoBulk.class,conn,persistentCache);
}
/*
 * Trova i Tipi Ammortamento legati ad una Categoria Gruppo
*/
public java.util.Collection findTipiAmmortamentoFor(UserContext aUC,Categoria_gruppo_inventBulk cat_gruppo)
	throws PersistencyException, IntrospectionException {
	
	if (cat_gruppo == null) 
		return null; 

	it.cnr.jada.persistency.sql.SQLBuilder sql = createSQLBuilder();
	sql.addTableToHeader("ASS_TIPO_AMM_CAT_GRUP_INV");
	sql.addSQLJoin("TIPO_AMMORTAMENTO.CD_TIPO_AMMORTAMENTO","ASS_TIPO_AMM_CAT_GRUP_INV.CD_TIPO_AMMORTAMENTO");	
	sql.addSQLJoin("TIPO_AMMORTAMENTO.TI_AMMORTAMENTO","ASS_TIPO_AMM_CAT_GRUP_INV.TI_AMMORTAMENTO");
	sql.addSQLClause("AND", "ASS_TIPO_AMM_CAT_GRUP_INV.CD_CATEGORIA_GRUPPO", sql.EQUALS, cat_gruppo.getCd_categoria_gruppo());
	sql.addSQLClause("AND", "ASS_TIPO_AMM_CAT_GRUP_INV.ESERCIZIO_COMPETENZA", sql.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC));
	sql.addSQLClause("AND", "ASS_TIPO_AMM_CAT_GRUP_INV.DT_CANCELLAZIONE", sql.ISNULL, null);
	
	return fetchAll(sql);

}
/*
 * Permette di rendere persistenti le associazioni tra il Tipo Ammortamento e le 
 *	Categorie di beni ad esso associato.
*/
public void makePersistentAssTiAmm_CatBeni(it.cnr.jada.UserContext aUC, Tipo_ammortamentoBulk ti_ammort) throws PersistencyException, IntrospectionException {

	LoggableStatement ps = null;
	java.io.StringWriter sql = new java.io.StringWriter();
	java.io.PrintWriter pw = new java.io.PrintWriter(sql);

	SimpleBulkList cat_beni_associati = (SimpleBulkList) ti_ammort.getCatBeni();

	String colonne_associa = "CD_TIPO_AMMORTAMENTO,CD_CATEGORIA_GRUPPO,ESERCIZIO_INIZIO,ESERCIZIO_FINE";
	String colonne_utente = "DACR,DUVA,UTCR,UTUV,PG_VER_REC";
	String valori_associa = "?,?,?,?";
	String valori_utente = "sysdate,sysdate,?,?,1";
	try {
		for (java.util.Iterator i = cat_beni_associati.iterator(); i.hasNext(); ) {

			Categoria_gruppo_inventBulk cat_bene = (Categoria_gruppo_inventBulk) i.next();
			pw.write("INSERT INTO " + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() + "ASS_TIPO_AMM_CAT_GRUP_INV (");
			pw.write(colonne_associa);
			pw.write(",");
			pw.write(colonne_utente);
			pw.write(") VALUES (");
			pw.write(valori_associa);
			pw.write(",");
			pw.write(valori_utente);
			pw.write(")");
			pw.flush();

			ps = new LoggableStatement(getConnection(), sql.toString(), true, this.getClass());
			pw.close();

			ps.setString(1, ti_ammort.getCd_tipo_ammortamento());
			ps.setString(2, cat_bene.getCd_proprio());
			ps.setInt(3, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC).intValue());
			ps.setInt(4, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC).intValue());
			ps.setString(5, ti_ammort.getUtcr());
			ps.setString(6, ti_ammort.getUtuv());

			ps.execute();
		}
	} catch (java.sql.SQLException e) {
		throw new PersistencyException(e);
	} finally {
		if (ps != null)
			try {
				ps.close();
			} catch (java.sql.SQLException e) {
			}
		;
	}
}

	public List<Tipo_ammortamentoBulk> findTipoAmmortamento(String tipoAmmortamento,String catGruppo, Integer esercizio) throws PersistencyException {
		List<Tipo_ammortamentoBulk> list = null;
		String statement =null;

		try {
			if(tipoAmmortamento == null){
				statement = statmentSelectTipoAmmNoTipoAmm;
			}else{
				statement = statmentSelectTipoAmmConTipoAmm;
			}
			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				if(tipoAmmortamento == null){
					ps.setString(1, catGruppo);
					ps.setInt(2, esercizio);
				}else{
					ps.setString(1, tipoAmmortamento);
					ps.setString(2, catGruppo);
					ps.setInt(3, esercizio);
				}

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {

						if (list == null) {
							list = new ArrayList<Tipo_ammortamentoBulk>();
						}
						list.add(getTipoAmmortamento(rs));

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
	public List<TipoAmmCatGruppoDto> findTipoAmmortamento(Integer esercizio) throws PersistencyException {
		List<TipoAmmCatGruppoDto> list = null;
		String statement =statmentSelectAllTipoAmm;

		try {


			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, esercizio);
				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {

						if (list == null) {
							list = new ArrayList<TipoAmmCatGruppoDto>();
						}
						list.add(getTipoAmmortamentoDto(rs));

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
	private Tipo_ammortamentoBulk getTipoAmmortamento(ResultSet rs) throws SQLException {

		Tipo_ammortamentoBulk tipoAmmortamento = new Tipo_ammortamentoBulk();
		tipoAmmortamento.setCd_tipo_ammortamento(rs.getString(1));
		tipoAmmortamento.setTi_ammortamento(rs.getString(2));
		tipoAmmortamento.setDs_tipo_ammortamento(rs.getString(3));
		tipoAmmortamento.setPerc_primo_anno(rs.getBigDecimal(4));
		tipoAmmortamento.setPerc_successivi(rs.getBigDecimal(5));
		tipoAmmortamento.setNumero_anni(rs.getInt(6));
		tipoAmmortamento.setDt_cancellazione(rs.getTimestamp(7));

		return tipoAmmortamento;
	}
	private TipoAmmCatGruppoDto getTipoAmmortamentoDto(ResultSet rs) throws SQLException {

		TipoAmmCatGruppoDto tipoAmmortamentoDto = new TipoAmmCatGruppoDto();
		tipoAmmortamentoDto.setCdCatGruppo(rs.getString(1));
		tipoAmmortamentoDto.setCdTipoAmm(rs.getString(2));
		tipoAmmortamentoDto.setTipoAmm(rs.getString(3));
		tipoAmmortamentoDto.setTipoAmmDesc(rs.getString(4));
		tipoAmmortamentoDto.setPerPrimoAnno(rs.getBigDecimal(5));
		tipoAmmortamentoDto.setPerAnniSucc(rs.getBigDecimal(6));
		tipoAmmortamentoDto.setNumAnni(rs.getInt(7));
		tipoAmmortamentoDto.setDataCanc(rs.getTimestamp(8));

		return tipoAmmortamentoDto;
	}
}

