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

import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.ejb.GenericComponentSession;
import jakarta.ejb.Remote;

import java.rmi.RemoteException;
import java.util.List;

@Remote
public interface BilancioAccrualComponentSession extends CRUDComponentSession {

    public List bilancioRiclasPatr(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException;

    public List bilancioRiclasCE(UserContext userContext, AccrualBulk accrualBulk) throws RemoteException, ComponentException;

    public List bilancioRiclasPatrSchedaAgg(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException;

}