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
* Date 22/11/2005
*/
package it.cnr.contab.anagraf00.core.bulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
public class Cnr_anadipHome extends BulkHome {
	public Cnr_anadipHome(java.sql.Connection conn) {
		super(Cnr_anadipBulk.class, conn);
	}
	public Cnr_anadipHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Cnr_anadipBulk.class, conn, persistentCache);
	}
}