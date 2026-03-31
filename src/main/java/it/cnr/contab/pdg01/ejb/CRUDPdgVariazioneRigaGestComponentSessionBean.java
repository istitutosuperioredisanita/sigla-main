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

package it.cnr.contab.pdg01.ejb;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;

import it.cnr.contab.pdg01.comp.CRUDPdgVariazioneRigaGestComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.NoRollbackException;

/**
 * Bean implementation class for Enterprise Bean: CNRPDG01_EJB_CRUDPdgVariazioneRigaGestComponentSession
 */
@Stateless(name="CNRPDG01_EJB_CRUDPdgVariazioneRigaGestComponentSession")
public class CRUDPdgVariazioneRigaGestComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean  implements CRUDPdgVariazioneRigaGestComponentSession{
	@PostConstruct
	public void ejbCreate() {
	componentObj = new it.cnr.contab.pdg01.comp.CRUDPdgVariazioneRigaGestComponent();
}
	
	public it.cnr.contab.progettiric00.core.bulk.ProgettoBulk getProgettoLineaAttivita(it.cnr.jada.UserContext param0,it.cnr.contab.pdg01.bulk.Pdg_variazione_riga_gestBulk param1) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
		pre_component_invocation(param0,componentObj);
		try {
			it.cnr.contab.progettiric00.core.bulk.ProgettoBulk result = ((CRUDPdgVariazioneRigaGestComponent)componentObj).getProgettoLineaAttivita(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;
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

	public OggettoBulk modificaConBulk(UserContext usercontext, OggettoBulk oggettobulk, boolean sendMessage) throws ComponentException, EJBException {
		pre_component_invocation(usercontext, componentObj);
		try {
			OggettoBulk oggettobulk1 = ((CRUDPdgVariazioneRigaGestComponent) componentObj).modificaConBulk(usercontext, oggettobulk, sendMessage);
			component_invocation_succes(usercontext, componentObj);
			return oggettobulk1;
		} catch (NoRollbackException norollbackexception) {
			component_invocation_succes(usercontext, componentObj);
			throw norollbackexception;
		} catch (ComponentException componentexception) {
			component_invocation_failure(usercontext, componentObj);
			throw componentexception;
		} catch (RuntimeException runtimeexception) {
			throw uncaughtRuntimeException(usercontext, componentObj, runtimeexception);
		} catch (Error error) {
			throw uncaughtError(usercontext, componentObj, error);
		}
	}
}
