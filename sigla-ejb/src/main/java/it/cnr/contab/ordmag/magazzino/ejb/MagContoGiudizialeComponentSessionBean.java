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

/*
 * Created on Jun 23, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.ordmag.magazzino.ejb;

import it.cnr.contab.ordmag.magazzino.comp.MagContoGiudizialeComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import java.rmi.RemoteException;

@Stateless(name="CNRDOCCONT00_EJB_MagContoGiudizialeComponentSession")
public class MagContoGiudizialeComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements MagContoGiudizialeComponentSession {
	@PostConstruct
	public void ejbCreate() {
		componentObj = new MagContoGiudizialeComponent();
	}
	@Remove
	public void ejbRemove() throws javax.ejb.EJBException {
		componentObj.release();
	}
	
	public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new MagContoGiudizialeComponentSessionBean();
	}

	@Override
	public RemoteIterator findMagContoGiudiziale(UserContext userContext,String columnMapName, CompoundFindClause baseClause, CompoundFindClause findClause) throws ComponentException, RemoteException, IntrospectionException {
		pre_component_invocation(userContext,componentObj);
		try {
			it.cnr.jada.util.RemoteIterator result = (( MagContoGiudizialeComponent)componentObj).findMagContoGiudiziale(userContext,columnMapName,baseClause,findClause);
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
