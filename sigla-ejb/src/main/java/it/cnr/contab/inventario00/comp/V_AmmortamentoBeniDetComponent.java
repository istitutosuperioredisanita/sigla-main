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

package it.cnr.contab.inventario00.comp;

import it.cnr.contab.inventario00.docs.bulk.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class V_AmmortamentoBeniDetComponent
		extends it.cnr.jada.comp.CRUDDetailComponent
			implements  Serializable, Cloneable {
/**
 * Inventario_beniComponent constructor comment.
 */
public V_AmmortamentoBeniDetComponent() {
	super();
}


	public List<V_ammortamento_beni_detBulk> getDatiAmmortamentoBeni(UserContext uc, Integer esercizio) throws PersistencyException {
	/*	try {
			V_ammortamento_beni_detHome v_ammortamentoBeneDetHome = (V_ammortamento_beni_detHome)getHomeCache(uc).getHome( V_ammortamento_beni_detBulk.class,"PROCEDURA_AMMORTAMENTO");

		//	v_ammortamentoBeneDetHome.setColumnMap("PROCEDURA_AMMORTAMENTO");
			SQLBuilder sql = v_ammortamentoBeneDetHome.createSQLBuilder();
		//	SQLBuilder sqlTot = v_ammortamentoBeneDetHome.createSQLBuilder();

			sql.resetColumns();
			sql.addColumn("PG_INVENTARIO");
			sql.addColumn("NR_INVENTARIO");
			sql.addColumn("PROGRESSIVO");
			sql.addColumn("ETICHETTA");
			sql.addColumn("FL_TOTALMENTE_SCARICATO");
			sql.addColumn("TI_AMMORTAMENTO");
			sql.addColumn("CD_CATEGORIA_GRUPPO");
			sql.addColumn("PERC_PRIMO_ANNO");
			sql.addColumn("PERC_SUCCESSIVI");
			sql.addColumn("CD_TIPO_AMMORTAMENTO");
			sql.addColumn("ESERCIZIO_COMPETENZA");
			sql.addColumn("NVL(SUM(VALORE_INIZIALE),0)", "VALORE_INIZIALE");
			sql.addColumn("NVL(SUM(VALORE_AMMORTIZZATO),0)", "VALORE_AMMORTIZZATO_BENE");
			sql.addColumn("NVL(SUM(IMPONIBILE_AMMORTAMENTO),0)", "IMPONIBILE_AMMORTAMENTO_BENE");
			sql.addColumn("NVL(SUM(VARIAZIONE_PIU),0)", "VARIAZIONE_PIU");
			sql.addColumn("NVL(SUM(VARIAZIONE_MENO),0)", "VARIAZIONE_MENO");
			sql.addColumn("NVL(SUM(INCREMENTO_VALORE),0)", "INCREMENTO_VALORE");
			sql.addColumn("NVL(SUM(DECREMENTO_VALORE),0)", "DECREMENTO_VALORE");
			sql.addColumn("NVL(SUM(STORNO),0)", "STORNO");
			sql.addColumn("NVL(SUM(IMPONIBILE_AMMORTAMENTO),0) - NVL(SUM(INCREMENTO_VALORE),0) + NVL(SUM(DECREMENTO_VALORE),0)", "IMPONIBILE_AMMORTAMENTO_CALCOLATO");
			sql.addColumn("NVL(SUM(VALORE_AMMORTIZZATO),0) - NVL(SUM(STORNO),0)", "VALORE_AMMORTIZZATO_CALCOLATO");
			sql.addColumn("MAX(NUMERO_ANNO_AMMORTAMENTO) NUMERO_ANNO_AMMORTAMENTO");


			sql.addSQLClause( "AND", "fl_ammortamento", SQLBuilder.EQUALS, "Y");
			sql.openParenthesis("AND");
			sql.addSQLClause("AND", "esercizio_competenza", SQLBuilder.EQUALS, esercizio);
			sql.addSQLClause("OR", "esercizio_competenza", SQLBuilder.ISNULL,null);
			sql.closeParenthesis();


			sql.openParenthesis("AND");

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "TIPORECORD", SQLBuilder.EQUALS, "INCREMENTO");
			sql.addSQLClause( "AND", "ESERCIZIO_BUONO_CARICO", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "TIPORECORD", SQLBuilder.EQUALS, "DECREMENTO");
			sql.addSQLClause( "AND", "ESERCIZIO_BUONO_CARICO", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "STORNO");
			sql.addSQLClause( "AND", "esercizio_ammortanento", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "VALORE");
			sql.addSQLClause( "AND", "esercizio_carico_bene", SQLBuilder.LESS_EQUALS, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "AMMORTAMENTO");
			sql.addSQLClause( "AND", "esercizio_ammortanento", SQLBuilder.EQUALS, esercizio.intValue()-1);
			sql.closeParenthesis();


			sql.closeParenthesis();

	//		sql.addSQLClause( "AND", "nr_inventario", SQLBuilder.EQUALS, 1346);
	//		sql.addSQLClause( "AND", "pg_inventario", SQLBuilder.EQUALS, 1);
	//		sql.addSQLClause( "AND", "progressivo", SQLBuilder.EQUALS, 0);

		//	sql.addSQLClause( "AND", "ETICHETTA", SQLBuilder.EQUALS, "V-019698");

			sql.addSQLGroupBy("PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,ETICHETTA,FL_TOTALMENTE_SCARICATO,TI_AMMORTAMENTO,CD_CATEGORIA_GRUPPO,PERC_PRIMO_ANNO,PERC_SUCCESSIVI,CD_TIPO_AMMORTAMENTO,ESERCIZIO_COMPETENZA");



			return v_ammortamentoBeneDetHome.fetchAll(sql);




		}catch (ComponentException | PersistencyException ex){
			throw new RuntimeException("Error getDatiAmmortamentoBeni esercizio : "+esercizio);
		}*/
		List<V_ammortamento_beni_detBulk> list = null;

		try {
			V_ammortamento_beni_detHome v_ammortamentoBeneDetHome = (V_ammortamento_beni_detHome)getHomeCache(uc).getHome( V_ammortamento_beni_detBulk.class,"PROCEDURA_AMMORTAMENTO");
			String statement = v_ammortamentoBeneDetHome.selectBeniDaAmmortizzare;

			LoggableStatement ps = null;
			Connection conn = getConnection(uc);
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, esercizio);
				ps.setInt(2, esercizio);
				ps.setInt(3, esercizio);
				ps.setInt(4, esercizio);
				ps.setInt(5, esercizio);
				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {
						if(list==null){
							list=new ArrayList<V_ammortamento_beni_detBulk>();
						}
						list.add(getVAmmortamentoBeniDet(rs));
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
		} catch (SQLException | ComponentException | PersistencyException e) {
			throw new PersistencyException(e);
		}
	}

	private  V_ammortamento_beni_detBulk getVAmmortamentoBeniDet(ResultSet rs) throws SQLException {
		V_ammortamento_beni_detBulk ammDet = new V_ammortamento_beni_detBulk();
		ammDet.setPgInventario(rs.getLong(1));
		ammDet.setNrInventario(rs.getLong(2));
		ammDet.setProgressivo(rs.getLong(3));
		ammDet.setEtichetta(rs.getString(4));
		ammDet.setFlTotalmenteScaricato(rs.getBoolean(5));
		ammDet.setTiAmmortamento(rs.getString(6));
		ammDet.setCdCategoriaGruppo(rs.getString(7));
		ammDet.setPercPrimoAnno(rs.getBigDecimal(8));
		ammDet.setPercSuccessivi(rs.getBigDecimal(9));
		ammDet.setCdTipoAmmortamento(rs.getString(10));
		ammDet.setEsercizioCompetenza(rs.getInt(11));
		ammDet.setValoreIniziale(rs.getBigDecimal(12));
		ammDet.setValoreAmmortizzatoBene(rs.getBigDecimal(13));
		ammDet.setImponibileAmmortamentoBene(rs.getBigDecimal(14));
		ammDet.setVariazionePiu(rs.getBigDecimal(15));
		ammDet.setVariazioneMeno(rs.getBigDecimal(16));
		ammDet.setIncrementoValore(rs.getBigDecimal(17));
		ammDet.setDecrementoValore(rs.getBigDecimal(18));
		ammDet.setStorno(rs.getBigDecimal(19));
		ammDet.setImponibileAmmortamentoCalcolato(rs.getBigDecimal(20));
		ammDet.setValoreAmmortizzatoCalcolato(rs.getBigDecimal(21));
		ammDet.setNumeroAnnoAmmortamento(rs.getBigDecimal(22));

		return ammDet;

	}
}
