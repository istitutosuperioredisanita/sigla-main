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

import it.cnr.contab.pdg00.cdip.bulk.GestioneStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.pdg00.comp.FlussoStipendiComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSessionBean;
import it.cnr.jada.persistency.PersistencyException;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateless;
import java.rmi.RemoteException;

@Stateless(name="CNRPDG00_EJB_FlussoStipendiComponentSession")
public class FlussoStipendiComponentSessionBean extends CRUDComponentSessionBean implements FlussoStipendiComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new FlussoStipendiComponent();
	}
	@Remove
	public void ejbRemove() throws jakarta.ejb.EJBException {
		componentObj.release();
	}



	@Override
	public GestioneStipBulk gestioneFlussoStipendi(UserContext param0, GestioneStipBulk param1) throws ComponentException,java.rmi.RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			GestioneStipBulk bulk= (GestioneStipBulk) ((FlussoStipendiComponent)componentObj).gestioneFlussoStipendi(param0,param1);
			component_invocation_succes(param0,componentObj);
			return bulk;
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

	@Override
	public void cancellaFlussoNonLiquidato(UserContext param0, Stipendi_cofiBulk param1)throws ComponentException, PersistencyException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			((FlussoStipendiComponent)componentObj).cancellaFlussoNonLiquidato(param0,param1);
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
