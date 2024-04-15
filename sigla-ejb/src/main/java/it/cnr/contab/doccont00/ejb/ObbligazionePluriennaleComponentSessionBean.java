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

package it.cnr.contab.doccont00.ejb;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.doccont00.comp.ObbligazionePluriennaleComponent;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRDOCCONT00_EJB_ObbligazionePluriennaleComponentSession")
public class ObbligazionePluriennaleComponentSessionBean extends ObbligazioneComponentSessionBean implements ObbligazionePluriennaleComponentSession {
@PostConstruct
	public void ejbCreate() {
	componentObj = new ObbligazionePluriennaleComponent();
}
public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
	return new ObbligazionePluriennaleComponentSessionBean();
}

	@Override
	public List<Obbligazione_pluriennaleBulk> findObbligazioniPluriennali(UserContext uc, int esercizio) throws ComponentException, RemoteException {
		pre_component_invocation(uc, componentObj);
		try {
			List<Obbligazione_pluriennaleBulk> result = ((ObbligazionePluriennaleComponent) componentObj).findObbligazioniPluriennali(uc, esercizio);
			component_invocation_succes(uc, componentObj);
			return result;
		} catch (it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(uc, componentObj);
			throw e;
		} catch (it.cnr.jada.comp.ComponentException e) {
			component_invocation_failure(uc, componentObj);
			throw e;
		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(uc, componentObj, e);
		} catch (Error e) {
			throw uncaughtError(uc, componentObj, e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ObbligazioneBulk createObbligazioneNew(UserContext uc, Obbligazione_pluriennaleBulk pluriennaleBulk, Integer esercizio,WorkpackageBulk gaeIniziale) throws ComponentException, RemoteException {
		pre_component_invocation(uc, componentObj);
		try {
			ObbligazioneBulk result = ((ObbligazionePluriennaleComponent) componentObj).createObbligazioneNew(uc, pluriennaleBulk,esercizio,gaeIniziale);
			component_invocation_succes(uc, componentObj);
			return result;
		} catch (it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(uc, componentObj);
			throw e;
		} catch (it.cnr.jada.comp.ComponentException e) {
			component_invocation_failure(uc, componentObj);
			throw e;
		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(uc, componentObj, e);
		} catch (Error e) {
			throw uncaughtError(uc, componentObj, e);
		}
	}


}

