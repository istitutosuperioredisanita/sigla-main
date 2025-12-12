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

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.CRUDBP;

import java.rmi.RemoteException;

public class CRUDObbligazioneModificaAction extends it.cnr.jada.util.action.CRUDAction {

    public Forward doElimina(ActionContext actioncontext)
            throws RemoteException {
        try {
            fillModel(actioncontext);
            CRUDBP crudbp = getBusinessProcess(actioncontext);
            crudbp.delete(actioncontext);
            crudbp.reset(actioncontext);
            crudbp.setMessage("Cancellazione effettuata");
            return actioncontext.closeBusinessProcess();
        } catch (Throwable throwable) {
            return handleException(actioncontext, throwable);
        }
    }
}
