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

package it.cnr.contab.gestiva00.ejb;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.Collection;

public class TransactionalConsRegIvaComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements ConsRegIvaComponentSession {

    public java.util.Collection selectTipi_sezionaliByClause(it.cnr.jada.UserContext param0,it.cnr.jada.bulk.OggettoBulk param1,it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException,it.cnr.jada.comp.ComponentException,it.cnr.jada.persistency.PersistencyException {
        try {
            return (java.util.Collection)invoke("selectTipi_sezionaliByClause",new Object[] {
                    param0,
                    param1,
                    param2,
                    param3 });
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(it.cnr.jada.persistency.PersistencyException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

//    @Override
//    public Collection queryDettRegIva(UserContext param0, String param1) throws ComponentException, RemoteException, PersistencyException {
//        try {
//            return (java.util.Collection)invoke("queryDettRegIva",new Object[] {
//                    param0,
//                    param1 });
//        } catch(java.rmi.RemoteException e) {
//            throw e;
//        } catch(java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch(it.cnr.jada.comp.ComponentException ex) {
//                throw ex;
//            } catch(it.cnr.jada.persistency.PersistencyException ex) {
//                throw ex;
//            } catch(Throwable ex) {
//                throw new java.rmi.RemoteException("Uncaugth exception",ex);
//            }
//        }    }

    public it.cnr.jada.util.RemoteIterator cercaDettRegIva(it.cnr.jada.UserContext param0, String param1)
            throws it.cnr.jada.comp.ComponentException, RemoteException {
        try {
            return (it.cnr.jada.util.RemoteIterator)invoke("cercaDettRegIva", new Object[] {
                    param0,
                    param1 });
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaught exception", ex);
            }
        }
    }

}
