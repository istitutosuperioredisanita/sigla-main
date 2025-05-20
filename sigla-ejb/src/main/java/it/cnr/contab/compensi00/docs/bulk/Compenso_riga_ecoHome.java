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

package it.cnr.contab.compensi00.docs.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;

public class Compenso_riga_ecoHome extends BulkHome {
    public Compenso_riga_ecoHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public Compenso_riga_ecoHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public Compenso_riga_ecoHome(java.sql.Connection conn) {
        super(Compenso_riga_ecoBulk.class, conn);
    }

    public Compenso_riga_ecoHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Compenso_riga_ecoBulk.class, conn, persistentCache);
    }
}
