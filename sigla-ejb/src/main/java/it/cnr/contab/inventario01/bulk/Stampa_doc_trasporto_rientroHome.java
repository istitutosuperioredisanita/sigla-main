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

package it.cnr.contab.inventario01.bulk;

/**
 * Home class per la gestione della stampa del Documento di Trasporto Rientro.
 * Creation date: (20/05/2003 17.55.08)
 * @author: Gennaro Borriello
 */
public class Stampa_doc_trasporto_rientroHome extends Doc_trasporto_rientroHome {

    public Stampa_doc_trasporto_rientroHome(Class clazz, java.sql.Connection conn) {
        super(clazz, conn);
    }

    public Stampa_doc_trasporto_rientroHome(Class clazz, java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(clazz, conn, persistentCache);
    }

    public Stampa_doc_trasporto_rientroHome(java.sql.Connection conn) {
        super(conn);
    }

    public Stampa_doc_trasporto_rientroHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(conn, persistentCache);
    }
}