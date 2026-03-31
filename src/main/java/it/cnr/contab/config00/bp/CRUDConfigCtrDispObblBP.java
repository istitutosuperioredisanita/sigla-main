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

package it.cnr.contab.config00.bp;

import it.cnr.contab.util.ICancellatoLogicamente;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.SimpleCRUDBP;

/**
 * Business Process che gestisce le attività di CRUD per l'entita' Unita Organizzativa
 */

public class CRUDConfigCtrDispObblBP extends SimpleCRUDBP {


	public CRUDConfigCtrDispObblBP() {
		super();

	}
	public CRUDConfigCtrDispObblBP(String function) {
		super(function);

	}
	/*protected it.cnr.jada.util.jsp.Button[] createToolbar() {
		it.cnr.jada.util.jsp.Button[] toolbar = new it.cnr.jada.util.jsp.Button[1];
		int i = 0;
		toolbar[i++] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.startSearch");
		return toolbar;
	}*/
	public void basicEdit(ActionContext context, OggettoBulk bulk, boolean doInitializeForEdit) throws BusinessProcessException {

		super.basicEdit(context, bulk, doInitializeForEdit);
		if (getStatus()!=VIEW){
			ICancellatoLogicamente bulkCancellato= (ICancellatoLogicamente)getModel();
			if (bulkCancellato!=null && bulkCancellato.isCancellatoLogicamente()) {
				setStatus(VIEW);
			}
		}
	}
}
