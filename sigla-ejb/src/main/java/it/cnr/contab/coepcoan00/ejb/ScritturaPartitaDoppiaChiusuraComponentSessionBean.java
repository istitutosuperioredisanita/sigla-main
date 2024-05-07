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

package it.cnr.contab.coepcoan00.ejb;

import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaChiusuraComponent;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.List;

@Stateless(name = "CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaChiusuraComponentSession")
public class ScritturaPartitaDoppiaChiusuraComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements ScritturaPartitaDoppiaChiusuraComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ScritturaPartitaDoppiaChiusuraComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new ScritturaPartitaDoppiaChiusuraComponentSessionBean();
    }

    @PostConstruct
    public void ejbCreate() {
        componentObj = new ScritturaPartitaDoppiaChiusuraComponent();
    }

	public void makeScrittureChiusura(UserContext param0, Integer esercizio, boolean isAnnullamento, boolean isDefinitiva) throws it.cnr.jada.comp.ComponentException,javax.ejb.EJBException {
		pre_component_invocation(param0, componentObj);
		try {
			((ScritturaPartitaDoppiaChiusuraComponent) componentObj).makeScrittureChiusura(param0, esercizio, isAnnullamento, isDefinitiva);
			component_invocation_succes(param0, componentObj);
		} catch (it.cnr.jada.comp.NoRollbackException e) {
			component_invocation_succes(param0, componentObj);
			throw e;
		} catch (ComponentException e) {
			component_invocation_failure(param0, componentObj);
			throw e;
		} catch (RuntimeException e) {
			throw uncaughtRuntimeException(param0, componentObj, e);
		} catch (Error e) {
			throw uncaughtError(param0, componentObj, e);
		}
    }
}
