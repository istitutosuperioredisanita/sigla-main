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


import it.cnr.contab.inventario00.comp.V_InventarioBeneDetComponent;

import it.cnr.contab.inventario00.dto.NormalizzatoreAmmortamentoDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRINVENTARIO00_EJB_V_InventarioBeneDetComponentSession")
public class V_InventarioBeneDetComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements V_InventarioBeneDetComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new V_InventarioBeneDetComponent();
	}
	@Remove
	public void ejbRemove() throws javax.ejb.EJBException {
		componentObj.release();
	}

	public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new V_InventarioBeneDetComponentSessionBean();
	}


	@Override
	public NormalizzatoreAmmortamentoDto findNormalizzatoreBene(UserContext param0, Integer param1, Long param2, Long param3, Long param4) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			NormalizzatoreAmmortamentoDto result = ((V_InventarioBeneDetComponent)componentObj).findNormalizzatoreBene(param0,param1,param2,param3,param4);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		}
	}

	@Override
	public List<NormalizzatoreAmmortamentoDto> findNormalizzatoreBeniPerAmm(UserContext param0, Integer param1) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			List<NormalizzatoreAmmortamentoDto> result = ((V_InventarioBeneDetComponent)componentObj).findNormalizzatoreBeniPerAmm(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		}
	}
}
