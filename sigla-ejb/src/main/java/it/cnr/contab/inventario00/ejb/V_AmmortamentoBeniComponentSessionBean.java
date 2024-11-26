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

import it.cnr.contab.doccont00.comp.AccertamentoComponent;
import it.cnr.contab.inventario00.comp.AmmortamentoBeneComponent;
import it.cnr.contab.inventario00.comp.V_AmmortamentoBeniComponent;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRINVENTARIO00_EJB_V_AmmortamentoBeniComponentSession")
public class V_AmmortamentoBeniComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements V_AmmortamentoBeniComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new AccertamentoComponent();
	}
	@Remove
	public void ejbRemove() throws javax.ejb.EJBException {
		componentObj.release();
	}

	public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new V_AmmortamentoBeniComponentSessionBean();
	}

	@Override
	public List<V_ammortamento_beniBulk> findAllBeniDaAmmortizare(UserContext param0, Integer param1) throws ComponentException, RemoteException  {
		pre_component_invocation(param0,componentObj);
		try {
			List<V_ammortamento_beniBulk> result = ((V_AmmortamentoBeniComponent)componentObj).findAllBeniDaAmmortizare(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		}
	}
}
