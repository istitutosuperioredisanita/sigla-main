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

import it.cnr.contab.config00.comp.PDCContoAnaliticoComponent;
import it.cnr.contab.config00.comp.Parametri_uoComponent;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.jada.UserContext;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

/**
 * Bean implementation class for Enterprise Bean: CNRCONFIG00_EJB_PDCContoAnaliticoComponentSession
 */
@Stateless(name="CNRCONFIG00_EJB_PDCContoAnaliticoComponentSession")
public class PDCContoAnaliticoComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements PDCContoAnaliticoComponentSession {
	@PostConstruct
	public void ejbCreate() {
		componentObj = new PDCContoAnaliticoComponent();
	}
	public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new PDCContoAnaliticoComponentSessionBean();
	}

	public java.util.List<ContoBulk> findContiAnaliticiAssociatiList(UserContext userContext, Voce_analiticaBulk voceAnalitica) throws it.cnr.jada.comp.ComponentException,javax.ejb.EJBException {
		pre_component_invocation(userContext,componentObj);
		try {
			java.util.List<ContoBulk> result = ((PDCContoAnaliticoComponent)componentObj).findContiAnaliticiAssociatiList(userContext,voceAnalitica);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(userContext,componentObj);
			throw e;
		} catch(it.cnr.jada.comp.ComponentException e) {
			component_invocation_failure(userContext,componentObj);
			throw e;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}
}
