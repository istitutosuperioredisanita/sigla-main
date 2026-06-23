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

package it.cnr.contab.pdg00.ejb;

import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.comp.BilancioAccrualComponent;
import it.cnr.contab.pdg00.comp.CostiDipendenteComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSessionBean;
import it.cnr.jada.ejb.GenericComponentSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateless;

import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRPDG00_EJB_BilancioAccrualComponentSession")
public class BilancioAccrualComponentSessionBean extends CRUDComponentSessionBean implements BilancioAccrualComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new BilancioAccrualComponent();
	}
	@Remove
	public void ejbRemove() throws jakarta.ejb.EJBException {
		componentObj.release();
	}



	@Override
	public List bilancioRiclasPatr(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException {
		pre_component_invocation(userContext,componentObj);
		try {
			java.util.List result = ((BilancioAccrualComponent)componentObj).bilancioRiclasPatr(userContext,accrualBulk,tipoAttPass);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

	@Override
	public List bilancioRiclasCE(UserContext userContext, AccrualBulk accrualBulk) throws RemoteException, ComponentException {
		pre_component_invocation(userContext,componentObj);
		try {
			java.util.List result = ((BilancioAccrualComponent)componentObj).bilancioRiclasCE(userContext,accrualBulk);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

	@Override
	public List bilancioRiclasPatrSchedaAgg(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws RemoteException, ComponentException {
		pre_component_invocation(userContext,componentObj);
		try {
			java.util.List result = ((BilancioAccrualComponent)componentObj).bilancioRiclasPatrSchedaAgg(userContext,accrualBulk,tipoAttPass);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

}