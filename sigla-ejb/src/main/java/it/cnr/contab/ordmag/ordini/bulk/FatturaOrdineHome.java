/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/09/2017
 */
package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;
import java.util.List;

public class FatturaOrdineHome extends BulkHome {
	public FatturaOrdineHome(Connection conn) {
		super(FatturaOrdineBulk.class, conn);
	}
	public FatturaOrdineHome(Connection conn, PersistentCache persistentCache) {
		super(FatturaOrdineBulk.class, conn, persistentCache);
	}

	public List<FatturaOrdineBulk> findByRigaConsegna(OrdineAcqConsegnaBulk ordineAcqConsegnaBulk) throws PersistencyException {
		final PersistentHome persistentHome = getHomeCache().getHome(FatturaOrdineBulk.class, "FATTURA_P");
		final SQLBuilder sqlBuilder = persistentHome.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "ordineAcqConsegna", SQLBuilder.EQUALS, ordineAcqConsegnaBulk);
		return fetchAll(sqlBuilder);
	}

	public FatturaOrdineBulk findFatturaByRigaConsegna(OrdineAcqConsegnaBulk ordineAcqConsegnaBulk) throws PersistencyException {
		final PersistentHome persistentHome = getHomeCache().getHome(FatturaOrdineBulk.class, "FATTURA_P");
		final SQLBuilder sql = persistentHome.createSQLBuilder();
		sql.addSQLClause("AND", "CD_CDS_ORDINE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdCds());
		sql.addSQLClause("AND", "CD_UNITA_OPERATIVA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdUnitaOperativa());
		sql.addSQLClause("AND", "ESERCIZIO_ORDINE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getEsercizio());
		sql.addSQLClause("AND", "CD_NUMERATORE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdNumeratore());
		sql.addSQLClause("AND", "NUMERO", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getNumero());
		sql.addSQLClause("AND", "RIGA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getRiga());
		sql.addSQLClause("AND", "CONSEGNA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getConsegna());

		if(fetchAll(sql).size() == 0){
			return null;
		}
		return (FatturaOrdineBulk) fetchAll(sql).get(0);
	}
}