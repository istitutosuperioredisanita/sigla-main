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
import it.cnr.contab.gestiva00.comp.ConsRegIvaComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import java.rmi.RemoteException;

@Stateless(name = "CNRGESTIVA00_EJB_ConsRegIvaComponentSession")
public class ConsRegIvaComponentSessionBean extends it.cnr.jada.ejb.RicercaComponentSessionBean implements ConsRegIvaComponentSession {
    @PostConstruct
    public void ejbCreate() {
        componentObj = new ConsRegIvaComponent();
    }

    @Remove
    public void ejbRemove() throws javax.ejb.EJBException {
        componentObj.release();
    }

    public static it.cnr.jada.ejb.RicercaComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new ConsRegIvaComponentSessionBean();
    }


    public java.util.Collection selectTipi_sezionaliByClause(UserContext userContext,
                                                             OggettoBulk model, Tipo_sezionaleBulk prototype, CompoundFindClause clause)
            throws ComponentException, RemoteException, PersistencyException {

        pre_component_invocation(userContext, componentObj);
        try {
            java.util.Collection result = ((ConsRegIvaComponent) componentObj).selectTipi_sezionaliByClause(
                    userContext, model, prototype, clause);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

}
