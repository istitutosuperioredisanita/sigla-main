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

package it.cnr.contab.doccont00.action;

import it.cnr.contab.doccont00.bp.CRUDObbligazioneModificaBP;
import it.cnr.contab.doccont00.bp.CRUDObbligazioneResBP;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_modificaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcess;
import it.cnr.jada.action.Forward;

import java.rmi.RemoteException;
import java.util.Optional;

public class CRUDObbligazioneModificaAction extends it.cnr.jada.util.action.CRUDAction {

    public Forward doElimina(ActionContext actioncontext)
            throws RemoteException {
        try {
            fillModel(actioncontext);
            CRUDObbligazioneModificaBP crudObbligazioneModificaBP = (CRUDObbligazioneModificaBP)getBusinessProcess(actioncontext);
            Optional<ObbligazioneBulk> obbligazioneBulk = Optional.ofNullable(crudObbligazioneModificaBP.getModel())
                    .map(Obbligazione_modificaBulk.class::cast)
                    .map(Obbligazione_modificaBulk::getObbligazione);
            crudObbligazioneModificaBP.delete(actioncontext);
            crudObbligazioneModificaBP.setMessage("Cancellazione effettuata");
            BusinessProcess businessProcess = actioncontext.closeBusinessProcess();
            if (getBusinessProcess(actioncontext) instanceof CRUDObbligazioneResBP) {
                CRUDObbligazioneResBP crudObbligazioneResBP = (CRUDObbligazioneResBP) getBusinessProcess(actioncontext);
                crudObbligazioneResBP.commitUserTransaction();
                crudObbligazioneResBP.basicEdit(actioncontext, obbligazioneBulk.orElse(null), true);
            }
            return businessProcess;
        } catch (Throwable throwable) {
            return handleException(actioncontext, throwable);
        }
    }
}
