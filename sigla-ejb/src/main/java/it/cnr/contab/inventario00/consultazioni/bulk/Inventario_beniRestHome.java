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

package it.cnr.contab.inventario00.consultazioni.bulk;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.rest.bulk.RestServicesHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RestHome per Inventario Beni con caricamento della sigla dell'ente assegnatario.
 */
public class Inventario_beniRestHome extends RestServicesHome {

	public Inventario_beniRestHome(java.sql.Connection conn) {
		super(Inventario_beniRestBulk.class, conn);
	}

	public Inventario_beniRestHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Inventario_beniRestBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
			throws PersistencyException {
		SQLBuilder b = super.selectByClause(usercontext, compoundfindclause);
		return super.addConditionCds(usercontext, b, "INVENTARIO_BENI.CD_CDS");
	}

	/**
	 * Popola sigla_int_ente per ogni riga in base all'assegnatario.
	 */
	@Override
	public Persistent completeBulkRowByRow(UserContext userContext, Persistent persistent)
			throws PersistencyException {
		if (persistent instanceof Inventario_beniRestBulk)
			loadSiglaIntEnte(userContext, (Inventario_beniRestBulk) persistent);
		return super.completeBulkRowByRow(userContext, persistent);
	}

	/**
	 * Carica SIGLA_INT_ENTE risalendo da TERZO -> ANAGRAFICO -> UNITA_ORGANIZZATIVA.
	 */
	private void loadSiglaIntEnte(UserContext userContext, Inventario_beniRestBulk bene)
			throws PersistencyException {
		if (bene == null || bene.getAssegnatario() == null || bene.getAssegnatario().getCd_terzo() == null) return;

		try {
			String schema = EJBCommonServices.getDefaultSchema();

            String sql = "SELECT UO.SIGLA_INT_ENTE " +
                    "FROM " + schema + "TERZO TERZO " +
                    "INNER JOIN " + schema + "ANAGRAFICO A ON TERZO.CD_ANAG = A.CD_ANAG " +
                    "LEFT JOIN " + schema + "UNITA_ORGANIZZATIVA UO " +
                    "ON A.CD_UNITA_ORGANIZZATIVA = UO.CD_UNITA_ORGANIZZATIVA " +
                    "WHERE TERZO.CD_TERZO = ?";

			LoggableStatement ps = new LoggableStatement(getConnection(), sql, true, this.getClass());
			try {
				ps.setInt(1, bene.getAssegnatario().getCd_terzo());

				ResultSet rs = ps.executeQuery();
				try {
					if (rs.next()) {
						String siglaIntEnte = rs.getString("SIGLA_INT_ENTE");
						bene.setSigla_int_ente(siglaIntEnte);
					}
				} finally {
					try { rs.close(); } catch (SQLException ignored) {}
				}
			} finally {
				try { ps.close(); } catch (SQLException ignored) {}
			}
		} catch (SQLException e) {
			throw new PersistencyException(
					"Errore caricamento SIGLA_INT_ENTE per CD_TERZO: " + bene.getAssegnatario().getCd_terzo(), e);
		}
	}
}