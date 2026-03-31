/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
package it.cnr.contab.util00.action;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.BulkBP;

public class ModalAction extends BulkAction {
    public Forward doAnnulla(ActionContext context) {
        try {
            context.closeBusinessProcess();
        } catch (BusinessProcessException e) {
            return handleException(context, e);
        }
        return context.findDefaultForward();
    }

    public Forward doConferma(ActionContext context) {
        try {
            BulkBP bp = (BulkBP) context.getBusinessProcess();
            bp.fillModel(context);
            bp.validate(context);
            HookForward hookforward = (HookForward) context.findForward("model");
            hookforward.addParameter("model", bp.getModel());
            context.closeBusinessProcess();
            return hookforward;
        } catch (FillException e) {
            return handleException(context, e);
        } catch (BusinessProcessException | ValidationException e) {
            return handleException(context, e);
        }
    }

}
