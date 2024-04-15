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

package it.cnr.contab.doccont00.ejb;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.ejb.Remote;
import java.rmi.RemoteException;
import java.util.List;

@Remote
public interface ObbligazionePluriennaleComponentSession extends ObbligazioneComponentSession {
    List<Obbligazione_pluriennaleBulk> findObbligazioniPluriennali(UserContext uc, int esercizio) throws it.cnr.jada.comp.ComponentException, RemoteException;

    ObbligazioneBulk createObbligazioneNew(UserContext usercontext, Obbligazione_pluriennaleBulk pluriennaleBulk, Integer esercizio,WorkpackageBulk gaeIniziale) throws ComponentException, RemoteException;
}
