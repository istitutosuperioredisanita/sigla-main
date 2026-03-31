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

package it.cnr.contab.anagraf00.core.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.beans.*;
import it.cnr.jada.persistency.sql.*;

import java.util.List;

public class Modalita_pagamentoHome extends BulkHome {
	public Modalita_pagamentoHome(java.sql.Connection conn) {
		super(Modalita_pagamentoBulk.class,conn);
	}
	public Modalita_pagamentoHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Modalita_pagamentoBulk.class,conn,persistentCache);
	}

	public List<Modalita_pagamentoBulk> findModalitaByTerzo(UserContext userContext, TerzoBulk terzoBulk) throws PersistencyException{
		SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "terzo", SQLBuilder.EQUALS, terzoBulk);
		final List<Modalita_pagamentoBulk> result = fetchAll(sqlBuilder);
		getHomeCache().fetchAll(userContext);
		return result;
	}
}
