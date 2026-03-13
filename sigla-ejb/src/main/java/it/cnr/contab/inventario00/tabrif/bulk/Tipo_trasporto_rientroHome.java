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

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Tipo_trasporto_rientroHome extends BulkHome {
public Tipo_trasporto_rientroHome(java.sql.Connection conn) {
	super(Tipo_trasporto_rientroBulk.class,conn);
}
public Tipo_trasporto_rientroHome(java.sql.Connection conn, PersistentCache persistentCache) {
	super(Tipo_trasporto_rientroBulk.class,conn,persistentCache);
}

	/**
	 * Cerca i tipi di movimento per il tipo documento (T/R)
	 */
	public java.util.Collection findTipiPerDocumento(UserContext userContext, String tiDocumento)
			throws PersistencyException {

		CompoundFindClause clause = new CompoundFindClause();
		clause.addClause("AND", "tiDocumento", SQLBuilder.EQUALS, tiDocumento);
		clause.addClause("AND", "dtCancellazione", SQLBuilder.ISNULL, null);
		SQLBuilder sql = selectByClause(userContext, clause);

		sql.addOrderBy("CD_TIPO_TRASPORTO_RIENTRO");

		return fetchAll(sql);
	}

	public java.util.Collection findTipoMovimenti(String tipo) throws IntrospectionException, PersistencyException{

		SQLBuilder sql = createSQLBuilder();
		sql.addClause("AND","ti_documento",sql.EQUALS, tipo);
		sql.addClause("AND","dt_cancellazione", sql.ISNULL,null);

		return fetchAll(sql);
	}

}
