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

package it.cnr.contab.doccont00.bp;

import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.docamm00.consultazioni.bulk.VConsRiepCompensiBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

public class CRUDObbligazioneResCreazioneBP extends CRUDObbligazioneBP {
	private static final long serialVersionUID = 1L;

	public CRUDObbligazioneResCreazioneBP() {
		super();
	}

	public CRUDObbligazioneResCreazioneBP(String function) {
		super(function);
	}

	public RemoteIterator find(ActionContext actionContext, it.cnr.jada.persistency.sql.CompoundFindClause clauses, Elemento_voceBulk bulk, ObbligazioneBulk context, String property) throws it.cnr.jada.action.BusinessProcessException {
		/*
		if (bulk instanceof VConsRiepCompensiBulk){
			try {
				return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext,createComponentSession().findRiepilogoCompensi(actionContext.getUserContext(),(VConsRiepCompensiBulk)bulk));
			} catch(Exception e) {
				throw new it.cnr.jada.action.BusinessProcessException(e);
			}
		} else {
			try {
				return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext,createComponentSession().cerca(actionContext.getUserContext(),clauses,bulk,context,property));
			} catch(Exception e) {
				throw new it.cnr.jada.action.BusinessProcessException(e);
			}
		}

		 */
		try {
			return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext,createComponentSession().cerca(actionContext.getUserContext(),clauses,bulk,context,property));
		} catch(Exception e) {
			throw new it.cnr.jada.action.BusinessProcessException(e);
		}
	}

}
