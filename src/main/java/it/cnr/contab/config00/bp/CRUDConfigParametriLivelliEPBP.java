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
 * Created on Sep 20, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.config00.bp;

import java.rmi.RemoteException;

import it.cnr.contab.config00.bulk.Parametri_cnrBulk;
import it.cnr.contab.config00.ejb.Parametri_livelli_epComponentSession;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
import it.cnr.contab.config00.pdcep.cla.bulk.Parametri_livelli_epBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;


public class CRUDConfigParametriLivelliEPBP extends it.cnr.jada.util.action.SimpleCRUDBP {
	private boolean isParametriLivelliDisabled=Boolean.FALSE;

	/**
	 * Primo costruttore della classe <code>CRUDConfigParametriCnrBP</code>.
	 */
	public CRUDConfigParametriLivelliEPBP() {
		super();
	}
	/**
	 * Secondo costruttore della classe <code>CRUDConfigParametriCnrBP</code>.
	 */
	public CRUDConfigParametriLivelliEPBP(String function) {
		super(function);
	}

	@Override
	public void setModel(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		if (oggettobulk instanceof Parametri_livelli_epBulk)
			isParametriLivelliDisabled = !this.isParametriLivelliEnabled(actioncontext.getUserContext(), (Parametri_livelli_epBulk)oggettobulk);
		super.setModel(actioncontext, oggettobulk);
	}

	public boolean isParametriLivelliEnabled(UserContext userContext, Parametri_livelli_epBulk parLiv)  throws it.cnr.jada.action.BusinessProcessException {
		try {
			Parametri_livelli_epComponentSession sessione = (Parametri_livelli_epComponentSession) createComponentSession();
			return sessione.isParametriLivelliEnabled(userContext, parLiv);
		} catch (BusinessProcessException | DetailedRuntimeException | ComponentException | RemoteException e) {
			throw handleException(e);
		}
    }

	public boolean isNewButtonHidden() {
		return true;
	}

	public boolean isDeleteButtonHidden() {
		return true;
	}

	public boolean isSaveButtonEnabled() {
		return !isParametriLivelliDisabled;
	}

	@Override
	protected void initialize(ActionContext actioncontext) throws BusinessProcessException {
		resetForSearch(actioncontext);
	}

	@Override
	public OggettoBulk initializeModelForSearch(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		oggettobulk = super.initializeModelForSearch(actioncontext, oggettobulk);
		if (oggettobulk instanceof Parametri_livelli_epBulk)
			((Parametri_livelli_epBulk)oggettobulk).setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return oggettobulk;
	}

	@Override
	public OggettoBulk initializeModelForFreeSearch(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		oggettobulk = super.initializeModelForFreeSearch(actioncontext, oggettobulk);
		if (oggettobulk instanceof Parametri_livelli_epBulk)
			((Parametri_livelli_epBulk)oggettobulk).setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return oggettobulk;
	}

	@Override
	public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws BusinessProcessException {
		if (compoundfindclause==null)
			compoundfindclause = new CompoundFindClause();
		compoundfindclause.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return super.find(actioncontext, compoundfindclause, oggettobulk);
	}
}