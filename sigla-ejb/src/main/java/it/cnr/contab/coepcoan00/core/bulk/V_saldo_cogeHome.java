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

package it.cnr.contab.coepcoan00.core.bulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;

public class V_saldo_cogeHome extends BulkHome {
	private static final long serialVersionUID = 1L;

	public V_saldo_cogeHome(java.sql.Connection conn) {
		super(V_saldo_cogeBulk.class, conn);
	}
	public V_saldo_cogeHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(V_saldo_cogeBulk.class, conn, persistentCache);
	}

	public List<V_saldo_cogeBulk> getAllSaldiContoEconomico(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio );
		sql.addClause(FindClause.AND,"stato",SQLBuilder.EQUALS, Movimento_cogeBulk.STATO_DEFINITIVO );

		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"naturaVoce",SQLBuilder.EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_COSTO);
		sql.addClause(FindClause.OR,"naturaVoce",SQLBuilder.EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_RICAVO);
		sql.addClause(FindClause.OR,"riepilogaA",SQLBuilder.EQUALS, "CEC");
		sql.closeParenthesis();

		return this.fetchAll(sql);
	}

	public List<V_saldo_cogeBulk> getAllSaldiStatoPatrimoniale(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio );
		sql.addClause(FindClause.AND,"stato",SQLBuilder.EQUALS, Movimento_cogeBulk.STATO_DEFINITIVO );

		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"naturaVoce",SQLBuilder.ISNULL, null);
		sql.openParenthesis(FindClause.OR);
			sql.addClause(FindClause.AND,"naturaVoce",SQLBuilder.NOT_EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_COSTO);
			sql.addClause(FindClause.AND,"naturaVoce",SQLBuilder.NOT_EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_RICAVO);
		sql.closeParenthesis();
		sql.closeParenthesis();

		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"riepilogaA",SQLBuilder.ISNULL, null);
		sql.addClause(FindClause.OR,"riepilogaA",SQLBuilder.NOT_EQUALS, "CEC");
		sql.closeParenthesis();

		return this.fetchAll(sql);
	}

	public List<V_saldo_cogeBulk> getAllSaldiConto(Integer esercizio, String cdVoceEp) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio );
		sql.addClause(FindClause.AND,"stato",SQLBuilder.EQUALS, Movimento_cogeBulk.STATO_DEFINITIVO );
		sql.addClause(FindClause.AND,"cdVoceEp",SQLBuilder.EQUALS, cdVoceEp );
		return this.fetchAll(sql);
	}
}