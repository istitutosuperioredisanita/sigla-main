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

import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.dto.NormalizzatoreAmmortamentoDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

public class TransactionalV_InventarioBeneDetComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements V_InventarioBeneDetComponentSession {


    @Override
    public NormalizzatoreAmmortamentoDto findNormalizzatoreBene(UserContext param0, Integer param1, Long param2, Long param3, Long param4) throws ComponentException, RemoteException{
        try {
            return (NormalizzatoreAmmortamentoDto)invoke("findNormalizzatoreBene",new Object[] {
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,});
        } catch (InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        } catch (RemoteException e) {
            throw e;
        }
    }

    @Override
    public List<NormalizzatoreAmmortamentoDto> findNormalizzatoreBeniPerAmm(UserContext param0, Integer param1) throws ComponentException, RemoteException {
        try {
            return (List<NormalizzatoreAmmortamentoDto>)invoke("findNormalizzatoreBeniPerAmm",new Object[] {
                    param0,
                    param1});
        } catch (InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        } catch (RemoteException e) {
            throw e;
        }
    }
}
