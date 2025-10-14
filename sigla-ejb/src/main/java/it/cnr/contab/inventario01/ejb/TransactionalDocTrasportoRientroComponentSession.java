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

package it.cnr.contab.inventario01.ejb;

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.bulk.Buono_carico_scarico_dettBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;

public class TransactionalDocTrasportoRientroComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements DocTrasportoRientroComponentSession {

    @Override
    public RemoteIterator cerca(UserContext userContext, CompoundFindClause compoundFindClause, Class aClass, OggettoBulk oggettoBulk, String s) throws ComponentException, RemoteException {
        return null;
    }

    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk oggettoBulk, OggettoBulk oggettoBulk1, String s) throws ComponentException, RemoteException {
        return null;
    }

    @Override
    public void eliminaConBulk(UserContext userContext, OggettoBulk[] oggettoBulks, OggettoBulk oggettoBulk, String s) throws ComponentException, RemoteException {

    }

    @Override
    public void eliminaConBulk(UserContext userContext, OggettoBulk oggettoBulk, String s) throws ComponentException, RemoteException {

    }

    @Override
    public OggettoBulk inizializzaBulkPerInserimento(UserContext userContext, OggettoBulk oggettoBulk, OggettoBulk oggettoBulk1, String s) throws ComponentException, RemoteException {
        return null;
    }

    @Override
    public OggettoBulk inizializzaBulkPerModifica(UserContext userContext, OggettoBulk oggettoBulk, OggettoBulk oggettoBulk1, String s) throws ComponentException, RemoteException {
        return null;
    }

    @Override
    public OggettoBulk modificaConBulk(UserContext userContext, OggettoBulk oggettoBulk, OggettoBulk oggettoBulk1, String s) throws ComponentException, RemoteException {
        return null;
    }

    public boolean isEsercizioCOEPChiuso(UserContext userContext) throws ComponentException, RemoteException {
        try {

            return ((Boolean)invoke("isEsercizioCOEPChiuso",new Object[] {
                    userContext})).booleanValue();
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