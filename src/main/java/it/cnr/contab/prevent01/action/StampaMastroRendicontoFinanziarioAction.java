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

/*
 * Created on Apr 26, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.prevent01.action;

import it.cnr.contab.prevent01.bp.StampaMastroRendicontoFinanziarioBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;

/**
 * @author xm3ron
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StampaMastroRendicontoFinanziarioAction extends StampaPdgpBilancioAction {

	/**
	 *
	 */
	public StampaMastroRendicontoFinanziarioAction() {
		super();
	}

	public Forward doOnTipoChange(ActionContext context) {
		try{
			StampaMastroRendicontoFinanziarioBP bp = (StampaMastroRendicontoFinanziarioBP) context.getBusinessProcess();
			fillModel(context);
			try {
				bp.loadModelBulkOptions(context);
			} catch (BusinessProcessException e) {
				return handleException(context, e);
			}
			return context.findDefaultForward();
		}catch(it.cnr.jada.bulk.FillException ex){
			return handleException(context, ex);
		}
	}

}
