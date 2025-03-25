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
* Created by Generator 1.0
* Date 09/05/2005
*/
package it.cnr.contab.config00.contratto.bulk;
import it.cnr.contab.consultazioni.bulk.ConsultazioniRestHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Procedure_amministrativeHome extends BulkHome implements ConsultazioniRestHome {

	public Procedure_amministrativeHome(Class clazz, java.sql.Connection conn) {
		super(clazz, conn);
	}

	public Procedure_amministrativeHome(Class clazz, java.sql.Connection conn, PersistentCache persistentCache) {
		super(clazz, conn, persistentCache);
	}
	public Procedure_amministrativeHome(java.sql.Connection conn) {
		super(Procedure_amministrativeBulk.class, conn);
	}
	public Procedure_amministrativeHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Procedure_amministrativeBulk.class, conn, persistentCache);
	}

	@Override
	public SQLBuilder restSelect(UserContext userContext, SQLBuilder sql, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws ComponentException, PersistencyException {
		return sql;
	}
}