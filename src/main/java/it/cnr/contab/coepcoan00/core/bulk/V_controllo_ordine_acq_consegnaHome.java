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
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.MovimentiMagBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;

public class V_controllo_ordine_acq_consegnaHome extends BulkHome {
	private static final long serialVersionUID = 1L;

	public V_controllo_ordine_acq_consegnaHome(java.sql.Connection conn) {
		super(V_controllo_ordine_acq_consegnaBulk.class, conn);
	}
	public V_controllo_ordine_acq_consegnaHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(V_controllo_ordine_acq_consegnaBulk.class, conn, persistentCache);
	}

	public List<V_controllo_ordine_acq_consegnaBulk> getDetailFattureDaRicevere(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizioEvasione",SQLBuilder.EQUALS, esercizio );
		sql.addClause(FindClause.AND,"statoCaricoMagazzino",SQLBuilder.NOT_EQUALS, MovimentiMagBulk.STATO_ANNULLATO);
		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"esercizioFattura",SQLBuilder.ISNULL, null );
		sql.addClause(FindClause.OR,"esercizioFattura",SQLBuilder.GREATER, esercizio );
		sql.closeParenthesis();

		return this.fetchAll(sql);
	}
}