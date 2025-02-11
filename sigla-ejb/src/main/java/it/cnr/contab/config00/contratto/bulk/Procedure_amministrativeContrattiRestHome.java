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

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Procedure_amministrativeContrattiRestHome extends Procedure_amministrativeHome {
	public Procedure_amministrativeContrattiRestHome(java.sql.Connection conn) {
		super(Procedure_amministrativeContrattiRestBulk.class, conn);
	}
	public Procedure_amministrativeContrattiRestHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Procedure_amministrativeContrattiRestBulk.class, conn, persistentCache);
	}
	@Override
	public SQLBuilder restSelect(UserContext userContext, SQLBuilder sql, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws ComponentException, PersistencyException {
			sql.addClause(FindClause.AND,"fl_cancellato", SQLBuilder.EQUALS, Boolean.FALSE);
			sql.openParenthesis(FindClause.AND);
				sql.addClause(FindClause.OR, "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_FORNITURA_SERVIZI);
				sql.addClause(FindClause.OR, "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_GENERICA);
			sql.closeParenthesis();
			return sql;
	}

}