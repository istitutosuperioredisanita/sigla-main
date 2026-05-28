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

package it.cnr.contab.config00.ejb;

import it.cnr.contab.config00.comp.PDCEconPatrComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;

import java.rmi.RemoteException;

@Stateless(name="CNRCONFIG00_EJB_PDCEconPatrComponentSession")
public class PDCEconPatrComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements PDCEconPatrComponentSession{
@PostConstruct
	public void ejbCreate() {
	componentObj = new it.cnr.contab.config00.comp.PDCEconPatrComponent();
}

	@Override
	public void generaBilancio(UserContext param0, String param1, String param2) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			((PDCEconPatrComponent)componentObj).generaBilancio(param0, param1, param2);
			component_invocation_succes(param0,componentObj);
		} catch(it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(param0,componentObj);
			throw e;
		} catch(it.cnr.jada.comp.ComponentException e) {
			component_invocation_failure(param0,componentObj);
			throw e;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}

	}
}
