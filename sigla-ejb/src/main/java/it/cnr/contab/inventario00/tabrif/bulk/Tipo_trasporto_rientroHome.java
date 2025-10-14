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

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Tipo_trasporto_rientroHome extends BulkHome {
public Tipo_trasporto_rientroHome(java.sql.Connection conn) {
	super(Tipo_carico_scaricoBulk.class,conn);
}
public Tipo_trasporto_rientroHome(java.sql.Connection conn, PersistentCache persistentCache) {
	super(Tipo_carico_scaricoBulk.class,conn,persistentCache);
}

	public java.util.Collection findTipoMovimenti(
			it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk docTrasportoRientro)
			throws IntrospectionException, PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		// Filtra per tipo documento (T=Trasporto, R=Rientro)
		sql.addClause("AND", "ti_documento", sql.EQUALS, docTrasportoRientro.getTiDocumento());
		// Esclude i movimenti cancellati (dove dt_cancellazione è NULL)
		sql.addClause("AND", "dt_cancellazione", sql.ISNULL, null);

		return fetchAll(sql);
	}

}
