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
 * Created on Aug 29, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.config00.pdcep.cla.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Classificazione_voci_ep_liv4Home extends Classificazione_voci_ep_liv3Home {
	protected Classificazione_voci_ep_liv4Home(Class clazz, java.sql.Connection connection) {
		super(clazz,connection);
	}
	protected Classificazione_voci_ep_liv4Home(Class clazz, java.sql.Connection connection, PersistentCache persistentCache) {
		super(clazz,connection,persistentCache);
	}

	public Classificazione_voci_ep_liv4Home(java.sql.Connection conn) {
		super(Classificazione_voci_ep_liv4Bulk.class, conn);
	}
	public Classificazione_voci_ep_liv4Home(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Classificazione_voci_ep_liv4Bulk.class, conn, persistentCache);
	}
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
		throws PersistencyException
	{
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
		sql.addSQLClause(FindClause.AND,"cd_livello4",SQLBuilder.ISNOTNULL, null);
		sql.addSQLClause(FindClause.AND,"cd_livello5",SQLBuilder.ISNULL, null);
		return sql;
	}	
}
