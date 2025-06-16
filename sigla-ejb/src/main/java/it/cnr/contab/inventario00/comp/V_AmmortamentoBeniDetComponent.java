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
