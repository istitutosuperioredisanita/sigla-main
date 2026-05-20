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

package it.cnr.contab.ordmag.magazzino.action;


import it.cnr.contab.ordmag.ordini.bp.CRUDEvasioneOrdineBP;
import it.cnr.contab.ordmag.ordini.bulk.EvasioneOrdineBulk;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.util.action.SelezionatoreListaAction;


public class ListaBolleScaricoGenerateAction extends SelezionatoreListaAction {
		/**
		 * DocumentiAmministrativiProtocollabiliAction constructor comment.
		 */
		public ListaBolleScaricoGenerateAction() {
			super();
		}
		public Forward basicDoBringBack(ActionContext actioncontext)
				throws BusinessProcessException
			{
				return null;
			}

	public Forward doCloseForm(ActionContext actioncontext)
			throws BusinessProcessException {
		Forward appoForward = super.doCloseForm(actioncontext);
		if (actioncontext.getBusinessProcess() instanceof CRUDEvasioneOrdineBP){
			actioncontext.closeBusinessProcess();
			HookForward hookforward = (HookForward) actioncontext.findForward("close");
			if (hookforward != null)
				return hookforward;
		}

		return appoForward;

		}


}
