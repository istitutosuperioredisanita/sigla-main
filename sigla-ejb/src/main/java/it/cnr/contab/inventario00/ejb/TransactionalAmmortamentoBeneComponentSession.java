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

package it.cnr.contab.inventario00.ejb;

import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

public class TransactionalAmmortamentoBeneComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements AmmortamentoBeneComponentSession {
    @Override
    public Boolean isExistAmmortamentoEsercizio(UserContext param0, Integer param1) throws RemoteException, InvocationTargetException {
        try {
            return (Boolean)invoke("isExistAmmortamentoEsercizio",new Object[] {
                    param0,
                    param1});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }

    }

    @Override
    public List<Ammortamento_bene_invBulk> getAllAmmortamentoEsercizio(UserContext param0, Integer param1) throws RemoteException, InvocationTargetException {
        try {
            return (List<Ammortamento_bene_invBulk>)invoke("getAllAmmortamentoEsercizio",new Object[] {
                    param0,
                    param1});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }

    @Override
    public Integer getNumeroAnnoAmmortamento(UserContext param0, Long param1, Long param2, Long param3) throws RemoteException {
        try {
            return (Integer)invoke("getNumeroAnnoAmmortamento",new Object[] {
                    param0,
                    param1,
                    param2,
                    param3});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }

    @Override
    public Integer getProgressivoRigaAmmortamento(UserContext param0, Long param1, Long param2, Long param3, Integer param4) throws RemoteException {
        try {
            return (Integer)invoke("getProgressivoRigaAmmortamento",new Object[] {
                    param0,
                    param1,
                    param2,
                    param3,
                    param4});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }

    @Override
    public void inserisciAmmortamentoBene(UserContext param0, Ammortamento_bene_invBulk param1) throws ComponentException, RemoteException {
        try {
            invoke("inserisciAmmortamentoBene",new Object[] {
                    param0,
                    param1});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }

    @Override
    public void cancellaAmmortamentiEsercizio(UserContext param0, Integer param1) throws ComponentException, RemoteException {
        try {
            invoke("cancellaAmmortamentiEsercizio",new Object[] {
                    param0,
                    param1});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }

    @Override
    public void aggiornamentoInventarioBeneConAmmortamento(UserContext param0, Integer param1, String param2) throws ComponentException, RemoteException, PersistencyException {
        try {
            invoke("aggiornamentoInventarioBeneConAmmortamento",new Object[] {
                    param0,
                    param1,
                    param2});
        }
        catch(Throwable ex) {
            throw new java.rmi.RemoteException("Uncaugth exception",ex);
        }
    }
}
