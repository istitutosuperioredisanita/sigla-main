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


import it.cnr.contab.inventario00.comp.AmmortamentoBeneComponent;
import it.cnr.contab.inventario00.comp.IdInventarioComponent;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.comp.ComponentException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRINVENTARIO00_EJB_AmmortamentoBeneComponentSession")
public class AmmortamentoBeneComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AmmortamentoBeneComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new AmmortamentoBeneComponent();
	}
	@Remove
	public void ejbRemove() throws javax.ejb.EJBException {
		componentObj.release();
	}

	public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new AmmortamentoBeneComponentSessionBean();
	}

	@Override
	public Boolean isExistAmmortamentoEsercizio(UserContext param0, Integer param1)  throws RemoteException, InvocationTargetException {
		pre_component_invocation(param0,componentObj);
		try {
			Boolean result = ((AmmortamentoBeneComponent)componentObj).isExistAmmortamentoEsercizio(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}

	@Override
	public List<Ammortamento_bene_invBulk> getAllAmmortamentoEsercizio(UserContext param0, Integer param1) throws RemoteException, InvocationTargetException {
		pre_component_invocation(param0,componentObj);
		try {
			List<Ammortamento_bene_invBulk> result = ((AmmortamentoBeneComponent)componentObj).getAllAmmortamentoEsercizio(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}

	@Override
	public Integer getNumeroAnnoAmmortamento(UserContext param0, Long param1, Long param2, Long param3) throws RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			Integer result = ((AmmortamentoBeneComponent)componentObj).getNumeroAnnoAmmortamento(param0,param1,param2,param3);
			component_invocation_succes(param0,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}

	@Override
	public Integer getProgressivoRigaAmmortamento(UserContext param0, Long param1, Long param2, Long param3,Integer param4) throws RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			Integer result = ((AmmortamentoBeneComponent)componentObj).getNumeroAnnoAmmortamento(param0,param1,param2,param3);
			component_invocation_succes(param0,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void inserisciAmmortamentoBene(UserContext param0, Ammortamento_bene_invBulk param1) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			((AmmortamentoBeneComponent)componentObj).inserisciAmmortamentoBene(param0,param1);
			component_invocation_succes(param0,componentObj);

		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void cancellaAmmortamentiEsercizio(UserContext param0,Integer  param1) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			((AmmortamentoBeneComponent)componentObj).cancellaAmmortamentiEsercizio(param0,param1);
			component_invocation_succes(param0,componentObj);

		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(param0,componentObj,e);
		}
	}
}
