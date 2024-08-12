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

/*
 * Created by Aurelio's BulkGenerator 1.0
 * Date 09/03/2007
 */
package it.cnr.contab.doccont00.consultazioni.bulk;

public class V_cons_scad_accert_voceHome extends V_cons_scad_accertHome {

	/**
	 * Fattura_passiva_IHome constructor comment.
	 *
	 * @param conn java.sql.Connection
	 */
	public V_cons_scad_accert_voceHome(java.sql.Connection conn) {
		super(V_cons_scad_accert_voceBulk.class, conn);
	}

	/**
	 * Fattura_passiva_IHome constructor comment.
	 *
	 * @param conn            java.sql.Connection
	 * @param persistentCache it.cnr.jada.persistency.PersistentCache
	 */
	public V_cons_scad_accert_voceHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
		super(V_cons_scad_accert_voceBulk.class, conn, persistentCache);
	}

	

}