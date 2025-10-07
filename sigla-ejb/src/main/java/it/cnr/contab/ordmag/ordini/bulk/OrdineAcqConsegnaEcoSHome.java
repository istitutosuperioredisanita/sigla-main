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

package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;

public class OrdineAcqConsegnaEcoSHome extends BulkHome {
    public OrdineAcqConsegnaEcoSHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public OrdineAcqConsegnaEcoSHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public OrdineAcqConsegnaEcoSHome(java.sql.Connection conn) {
        super(OrdineAcqConsegnaEcoSBulk.class, conn);
    }

    public OrdineAcqConsegnaEcoSHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(OrdineAcqConsegnaEcoSBulk.class, conn, persistentCache);
    }

    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, it.cnr.jada.comp.ComponentException {
        if (bulk == null) return;
        try {
            OrdineAcqConsegnaEcoSBulk riga = (OrdineAcqConsegnaEcoSBulk) bulk;
            if (riga.getId()==null) {
                java.sql.Connection contact = getConnection();
                java.sql.ResultSet rs = contact.createStatement().executeQuery("SELECT MAX(ID) FROM " +
                        it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                        "ORDINE_ACQ_CONSEGNA_ECO_S");
                long x;
                if (rs.next())
                    x = rs.getLong(1) + 1;
                else
                    x = 0L;
                riga.setId(x);
            }
        } catch (java.sql.SQLException sqle) {
            throw new PersistencyException(sqle);
        }
    }

    public List<OrdineAcqConsegnaEcoSBulk> getStoricoConsegna(OrdineAcqConsegnaBulk consegna, String tipoStorico) throws PersistencyException {
        PersistentHome rigaHome = getHomeCache().getHome(OrdineAcqConsegnaEcoSBulk.class);
        SQLBuilder sql = rigaHome.createSQLBuilder();
        sql.addClause(FindClause.AND, "tipoStorico", SQLBuilder.EQUALS, tipoStorico);
        sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, consegna.getCdCds());
        sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, consegna.getCdUnitaOperativa());
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, consegna.getEsercizio());
        sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, consegna.getCdNumeratore());
        sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, consegna.getNumero());
        sql.addClause(FindClause.AND, "riga", SQLBuilder.EQUALS, consegna.getRiga());
        sql.addClause(FindClause.AND, "consegna", SQLBuilder.EQUALS, consegna.getConsegna());

        return rigaHome.fetchAll(sql);
    }
}
