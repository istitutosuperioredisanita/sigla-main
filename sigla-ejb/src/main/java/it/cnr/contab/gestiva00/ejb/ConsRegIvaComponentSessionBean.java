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

package it.cnr.contab.gestiva00.ejb;

import it.cnr.contab.gestiva00.comp.ConsRegIvaComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.NoRollbackException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateless;

@Stateless(name="CNRGESTIVA00_EJB_ConsRegIvaComponentSession")
public class ConsRegIvaComponentSessionBean extends it.cnr.jada.ejb.RicercaComponentSessionBean implements ConsRegIvaComponentSession {
@PostConstruct
	public void ejbCreate() {
		componentObj = new ConsRegIvaComponent();
	}
	@Remove
	public void ejbRemove() throws javax.ejb.EJBException {
		componentObj.release();
	}

	public static it.cnr.jada.ejb.RicercaComponentSessionBean newInstance() throws javax.ejb.EJBException {
		return new ConsRegIvaComponentSessionBean();
	}

}
