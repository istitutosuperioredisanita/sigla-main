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

import it.cnr.contab.doccont00.comp.AccertamentoPluriennaleComponent;
import it.cnr.contab.doccont00.core.bulk.AccertamentoBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_pluriennaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRDOCCONT00_EJB_AccertamentoPluriennaleComponentSession")
public class AccertamentoPluriennaleComponentSessionBean extends AccertamentoComponentSessionBean implements AccertamentoPluriennaleComponentSession {
@PostConstruct
	public void ejbCreate() {
	componentObj = new AccertamentoPluriennaleComponent();
}
public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
	return new AccertamentoPluriennaleComponentSessionBean();
}

	@Override
	public List<Accertamento_pluriennaleBulk> findAccertamentiPluriennali(UserContext uc, int esercizio) throws ComponentException, RemoteException {
		pre_component_invocation(uc, componentObj);
		try {
			List<Accertamento_pluriennaleBulk> result = ((AccertamentoPluriennaleComponent) componentObj).findAccertamentiPluriennali(uc, esercizio);
			component_invocation_succes(uc, componentObj);
			return result;
		} catch (it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(uc, componentObj);
			throw e;
		} catch (ComponentException e) {
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
	public AccertamentoBulk createAccertamentoNew(UserContext uc, Integer esercizio, Accertamento_pluriennaleBulk pluriennaleBulk) throws ComponentException {
		pre_component_invocation(uc, componentObj);
		try {
			AccertamentoBulk result = ((AccertamentoPluriennaleComponent) componentObj).createAccertamentoNew(uc, esercizio,pluriennaleBulk);
			component_invocation_succes(uc, componentObj);
			return result;
		} catch (it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(uc, componentObj);
			throw e;
		} catch (ComponentException e) {
			component_invocation_failure(uc, componentObj);
			throw e;
		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(uc, componentObj, e);
		} catch (Error e) {
			throw uncaughtError(uc, componentObj, e);
		}
	}
}

