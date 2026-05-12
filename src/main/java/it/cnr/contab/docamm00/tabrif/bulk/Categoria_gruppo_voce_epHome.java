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

package it.cnr.contab.docamm00.tabrif.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;

public class Categoria_gruppo_voce_epHome extends BulkHome {

	public Categoria_gruppo_voce_epHome(java.sql.Connection conn) {
		super(Categoria_gruppo_voce_epBulk.class,conn);
	}

	public Categoria_gruppo_voce_epHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Categoria_gruppo_voce_epBulk.class,conn,persistentCache);
	}

	public Categoria_gruppo_voce_epBulk findDefaultByCategoria(Integer esercizio, String cd_categoria_gruppo ) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(Categoria_gruppo_voce_epBulk.class);
		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio);
		sql.addClause(FindClause.AND,"cd_categoria_gruppo",SQLBuilder.EQUALS, cd_categoria_gruppo);
		sql.addClause(FindClause.AND,"fl_default",SQLBuilder.EQUALS, Boolean.TRUE);
		List<Categoria_gruppo_voce_epBulk> result = home.fetchAll(sql);
		if (!result.isEmpty())
			return result.get(0);
		return null;
	}
}
