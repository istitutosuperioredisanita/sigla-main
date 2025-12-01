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

package it.cnr.contab.inventario00.ejb;


import it.cnr.contab.inventario00.comp.V_AmmortamentoBeniComponent;
import it.cnr.contab.inventario00.comp.V_AmmortamentoBeniDetComponent;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beni_detBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateless;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRINVENTARIO00_EJB_V_AmmortamentoBeniDetComponentSession")
public class V_AmmortamentoBeniDetComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements V_AmmortamentoBeniDetComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new V_AmmortamentoBeniDetComponent();
	}
	@Remove
	public void ejbRemove() throws jakarta.ejb.EJBException {
		componentObj.release();
	}

	

	@Override
	public List<V_ammortamento_beni_detBulk> getDatiAmmortamentoBeni(UserContext param0, Integer param1) throws ComponentException, RemoteException  {
		pre_component_invocation(param0,componentObj);
		try {
			List<V_ammortamento_beni_detBulk> result = ((V_AmmortamentoBeniDetComponent)componentObj).getDatiAmmortamentoBeni(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException | PersistencyException e) {
			throw uncaughtRuntimeException(param0,componentObj, (RuntimeException) e);
		}
	}
}
