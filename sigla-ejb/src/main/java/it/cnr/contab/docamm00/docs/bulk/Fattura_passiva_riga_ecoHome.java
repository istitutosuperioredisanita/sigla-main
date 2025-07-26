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

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;

public class Fattura_passiva_riga_ecoHome extends BulkHome {
    public Fattura_passiva_riga_ecoHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public Fattura_passiva_riga_ecoHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public Fattura_passiva_riga_ecoHome(java.sql.Connection conn) {
        super(Fattura_passiva_riga_ecoBulk.class, conn);
    }

    public Fattura_passiva_riga_ecoHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Fattura_passiva_riga_ecoBulk.class, conn, persistentCache);
    }

    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, it.cnr.jada.comp.ComponentException {

        if (bulk == null) return;
        try {
            Fattura_passiva_riga_ecoBulk riga = (Fattura_passiva_riga_ecoBulk) bulk;
            if (riga.getProgressivo_riga_eco()==null) {
                java.sql.Connection contact = getConnection();
                java.sql.ResultSet rs = contact.createStatement().executeQuery("SELECT MAX(PROGRESSIVO_RIGA_ECO) FROM " +
                        it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                        "FATTURA_PASSIVA_RIGA_ECO WHERE " +
                        "(ESERCIZIO = " + riga.getEsercizio() + ") AND " +
                        "(CD_CDS = '" + riga.getCd_cds() + "') AND " +
                        "(CD_UNITA_ORGANIZZATIVA = '" + riga.getCd_unita_organizzativa() + "') AND " +
                        "(PG_FATTURA_PASSIVA = " + riga.getPg_fattura_passiva() + ") AND " +
                        "(PROGRESSIVO_RIGA = " + riga.getProgressivo_riga() + ")");
                Long x;
                if (rs.next())
                    x = rs.getLong(1) + 1;
                else
                    x = 0L;
                riga.setProgressivo_riga_eco(x);
            }
        } catch (java.sql.SQLException sqle) {
            throw new PersistencyException(sqle);
        }
    }
}
