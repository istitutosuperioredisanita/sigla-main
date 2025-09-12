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

package it.cnr.contab.pdg00.ejb;

import it.cnr.contab.pdg00.cdip.bulk.GestioneStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;

import javax.ejb.Remote;
import java.rmi.RemoteException;

@Remote
public interface FlussoStipendiComponentSession extends CRUDComponentSession {

    GestioneStipBulk gestioneFlussoStipendi(UserContext userContext, GestioneStipBulk bulk) throws ComponentException,java.rmi.RemoteException;
    void cancellaFlussoNonLiquidato(UserContext uc, Stipendi_cofiBulk stipendiCofiBulk) throws ComponentException, PersistencyException, RemoteException;

}
