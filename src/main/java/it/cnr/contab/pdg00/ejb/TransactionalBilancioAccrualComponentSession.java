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

import java.rmi.RemoteException;
import java.util.List;

public class TransactionalBilancioAccrualComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements BilancioAccrualComponentSession {


    @Override
    public List bilancioRiclasPatr(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException {
        try {
            return (java.util.List)invoke("bilancioRiclasPatr",new Object[] {
                    userContext,
                    accrualBulk,
                    tipoAttPass});
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public List bilancioRiclasCE(UserContext userContext, AccrualBulk accrualBulk) throws RemoteException, ComponentException {
        try {
            return (java.util.List)invoke("bilancioRiclasCE",new Object[] {
                    userContext,
                    accrualBulk});
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public List bilancioRiclasPatrSchedaAgg(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException {
        try {
            return (java.util.List)invoke("bilancioRiclasPatrSchedaAgg",new Object[] {
                    userContext,
                    accrualBulk,
                    tipoAttPass});
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

}
