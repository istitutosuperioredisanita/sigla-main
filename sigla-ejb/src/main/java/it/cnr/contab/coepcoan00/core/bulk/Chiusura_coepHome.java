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
* Date 17/05/2005
*/
package it.cnr.contab.coepcoan00.core.bulk;
import it.cnr.contab.config00.bulk.Semaforo_baseBulk;
import it.cnr.contab.config00.bulk.Semaforo_staticoBulk;
import it.cnr.contab.config00.bulk.Semaforo_staticoHome;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;
import java.util.Optional;

public class Chiusura_coepHome extends BulkHome {
    private static final String SEMAFORO_CHIUSURA = "CHIUSURA_COGE00";
    private static final String STATO_CHIUSURA_DEF = "C";

    public Chiusura_coepHome(java.sql.Connection conn) {
		super(Chiusura_coepBulk.class, conn);
	}
	public Chiusura_coepHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Chiusura_coepBulk.class, conn, persistentCache);
	}

	public List<Chiusura_coepBulk> getAllChiusureCoep(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio );
		return this.fetchAll(sql);
	}

	public Chiusura_coepBulk getChiusuraCoep(Integer esercizio, String cdCds) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio",SQLBuilder.EQUALS, esercizio );
		sql.addClause(FindClause.AND,"cd_cds",SQLBuilder.EQUALS, cdCds );
		List<Chiusura_coepBulk> result = this.fetchAll(sql);
		if (!result.isEmpty())
			return result.get(0);
		return null;
	}

    public boolean isChiusuraCoepDef(Integer esercizio, String cdCds) {
        try {
            boolean isSemStaticoCdsBloccato = ((Semaforo_staticoHome)getHomeCache().getHome(Semaforo_staticoBulk.class)).isSemStaticoCdsBloccato(esercizio, cdCds, SEMAFORO_CHIUSURA);
            if (isSemStaticoCdsBloccato)
                return Boolean.TRUE;

            Chiusura_coepBulk chiusuraCoepBulk = getChiusuraCoep(esercizio, cdCds);
            return Optional.ofNullable(chiusuraCoepBulk).flatMap(el->Optional.ofNullable(el.getStato()))
                    .map(STATO_CHIUSURA_DEF::equals).orElse(Boolean.FALSE);
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }
}