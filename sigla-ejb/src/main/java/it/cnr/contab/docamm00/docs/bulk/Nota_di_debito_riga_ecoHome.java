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

package it.cnr.contab.docamm00.docs.bulk;

public class Nota_di_debito_riga_ecoHome extends Fattura_passiva_riga_ecoHome {

    public Nota_di_debito_riga_ecoHome(java.sql.Connection conn) {
        super(Nota_di_debito_riga_ecoBulk.class, conn);
    }

    public Nota_di_debito_riga_ecoHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(Nota_di_debito_riga_ecoBulk.class, conn, persistentCache);
    }
}
