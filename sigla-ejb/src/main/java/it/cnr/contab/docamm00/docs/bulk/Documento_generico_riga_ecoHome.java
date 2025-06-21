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

public class Documento_generico_riga_ecoHome extends BulkHome {
    public Documento_generico_riga_ecoHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public Documento_generico_riga_ecoHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public Documento_generico_riga_ecoHome(java.sql.Connection conn) {
        super(Documento_generico_riga_ecoBulk.class, conn);
    }

    public Documento_generico_riga_ecoHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Documento_generico_riga_ecoBulk.class, conn, persistentCache);
    }

    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, it.cnr.jada.comp.ComponentException {

        if (bulk == null) return;
        try {
            Documento_generico_riga_ecoBulk riga = (Documento_generico_riga_ecoBulk) bulk;
            java.sql.Connection contact = getConnection();
            java.sql.ResultSet rs = contact.createStatement().executeQuery("SELECT MAX(PROGRESSIVO_RIGA) FROM " +
                    it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                    "DOCUMENTO_GENERICO_RIGA_ECO WHERE " +
                    "(ESERCIZIO = " + riga.getEsercizio() + ") AND " +
                    "(CD_CDS = '" + riga.getCd_cds() + "') AND " +
                    "(CD_UNITA_ORGANIZZATIVA = '" + riga.getCd_unita_organizzativa() + "') AND " +
                    "(CD_TIPO_DOCUMENTO_AMM = '" + riga.getCd_tipo_documento_amm() + "') AND " +
                    "(PG_DOCUMENTO_GENERICO = " + riga.getPg_documento_generico() + ")");
            Long x;
            if (rs.next())
                x = rs.getLong(1) + 1;
            else
                x = 0L;
            riga.setProgressivo_riga(x);
        } catch (java.sql.SQLException sqle) {
            throw new PersistencyException(sqle);
        }
    }
}
