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

package it.cnr.contab.doccont00.comp;

import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import javax.ejb.Remote;

@Remote
public interface AsyncConsSostitutivaComponentSession extends it.cnr.jada.ejb.CRUDComponentSession {
    void asyncConsSostitutiva(UserContext param0, Integer esercizio) throws ComponentException, PersistencyException, java.rmi.RemoteException;
}
