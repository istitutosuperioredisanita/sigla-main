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
* Date 17/11/2005
*/
package it.cnr.contab.config00.bulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OutdatedResourceException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;

import java.util.Optional;

public class Semaforo_staticoHome extends BulkHome {
    private static final String INDEFINITO = "*";
    private static final String SEMAFORO_ROSSO = "R";
    private static final String SEMAFORO_VERDE = "G";

	public Semaforo_staticoHome(java.sql.Connection conn) {
		super(Semaforo_staticoBulk.class, conn);
	}

	public Semaforo_staticoHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Semaforo_staticoBulk.class, conn, persistentCache);
	}

    public String getStatoSemStaticoCds(int aEs, String aCdCds, String aTipoSem, boolean isLock) throws PersistencyException {
        try {
            Semaforo_staticoBulk semaforoStaticoBulk;
            if (isLock)
                semaforoStaticoBulk = (Semaforo_staticoBulk) findAndLock(
                        new Semaforo_staticoBulk(aEs, aTipoSem, aCdCds, INDEFINITO, INDEFINITO));
            else
                semaforoStaticoBulk = (Semaforo_staticoBulk) findByPrimaryKey(
                        new Semaforo_staticoBulk(aEs, aTipoSem, aCdCds, INDEFINITO, INDEFINITO));

            return Optional.ofNullable(semaforoStaticoBulk).map(Semaforo_staticoBulk::getStato).orElse(null);
        } catch (OutdatedResourceException | BusyResourceException ex) {
            throw new PersistencyException(ex);
        }
    }

    public boolean isSemStaticoCdsBloccato(int aEs, String aCdCds, String aTipoSem) throws PersistencyException {
        String statoSemStaticoCds = getStatoSemStaticoCds(aEs, aCdCds, aTipoSem, Boolean.FALSE);
        return Optional.ofNullable(statoSemStaticoCds).map(SEMAFORO_ROSSO::equals).orElse(Boolean.FALSE);
    }
}