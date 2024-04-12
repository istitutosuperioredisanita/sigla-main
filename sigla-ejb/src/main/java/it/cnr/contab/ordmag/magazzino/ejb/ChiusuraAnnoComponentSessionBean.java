/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.magazzino.ejb;

import it.cnr.contab.ordmag.magazzino.bulk.*;
import it.cnr.contab.ordmag.magazzino.comp.ChiusuraAnnoComponent;
import it.cnr.contab.ordmag.magazzino.comp.MovimentiMagComponent;
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.contab.ordmag.ordini.bulk.EvasioneOrdineRigaBulk;
import it.cnr.contab.ordmag.ordini.bulk.FatturaOrdineBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.contab.ordmag.ordini.dto.ImportoOrdine;
import it.cnr.contab.ordmag.ordini.dto.ParametriCalcoloImportoOrdine;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.RemoteIterator;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Stateless(name="CNRORDMAG00_EJB_ChiusuraAnnoComponentSession")
public class ChiusuraAnnoComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements ChiusuraAnnoComponentSession {
@PostConstruct
	public void ejbCreate() {
	componentObj = new ChiusuraAnnoComponent();
}
public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
	return new ChiusuraAnnoComponentSessionBean();
}


	@Override
	public List<ValoriLottoPerAnno> getValoriLottoPerAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo) throws RemoteException, ComponentException, PersistencyException {
		pre_component_invocation(userContext,componentObj);
		try {
			List<ValoriLottoPerAnno> result = ((ChiusuraAnnoComponent)componentObj).getQuantitaAnnoPerLotto(userContext, esercizio,dataFinePeriodo);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

	@Override
	public ChiusuraAnnoBulk calcolaRimanenzeAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo,String statoChiusura) throws RemoteException, ComponentException, PersistencyException {
		pre_component_invocation(userContext,componentObj);
		try {
			ChiusuraAnnoBulk result = ((ChiusuraAnnoComponent)componentObj).calcolaRimanenzeAnno(userContext, esercizio,dataFinePeriodo,statoChiusura);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		} catch (BusyResourceException e) {
			throw new ApplicationException();
		}
	}

	@Override
	public ChiusuraAnnoBulk verificaChiusuraAnno(UserContext userContext, Integer esercizio, String tipoChiusura) throws ComponentException, PersistencyException, RemoteException {
		pre_component_invocation(userContext,componentObj);
		try {
			ChiusuraAnnoBulk result = ((ChiusuraAnnoComponent)componentObj).verificaChiusuraAnno(userContext, esercizio,tipoChiusura);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}



	@Override
	public ChiusuraAnnoBulk salvaChiusuraDefinitiva(UserContext userContext, Integer esercizio, String tipoChiusura,Date dataFinePeriodo) throws RemoteException, ComponentException,PersistencyException, ParseException {
		pre_component_invocation(userContext,componentObj);
		try {
			ChiusuraAnnoBulk result = ((ChiusuraAnnoComponent)componentObj).salvaChiusuraDefinitiva(userContext, esercizio,tipoChiusura,dataFinePeriodo);
			component_invocation_succes(userContext,componentObj);
			return result;
		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

	@Override
	public void annullaChiusuraDefinitiva(UserContext userContext, Integer esercizio, String tipoChiusura) throws RemoteException, ComponentException, PersistencyException, ParseException {
		pre_component_invocation(userContext,componentObj);
		try {
			((ChiusuraAnnoComponent)componentObj).annullaChiusuraDefinitiva(userContext, esercizio,tipoChiusura);
			component_invocation_succes(userContext,componentObj);

		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}


	@Override
	public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException, RemoteException {
		pre_component_invocation(userContext,componentObj);
		try {
			OggettoBulk result = ((ChiusuraAnnoComponent)componentObj).inizializzaBulkPerStampa(userContext, oggettoBulk);
			component_invocation_succes(userContext,componentObj);
			return result;

		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}

	@Override
	public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException, RemoteException {
		pre_component_invocation(userContext,componentObj);
		try {
			OggettoBulk result = ((ChiusuraAnnoComponent)componentObj).inizializzaBulkPerStampa(userContext, oggettoBulk);
			component_invocation_succes(userContext,componentObj);
			return result;

		} catch(RuntimeException e) {
			throw uncaughtRuntimeException(userContext,componentObj,e);
		} catch(Error e) {
			throw uncaughtError(userContext,componentObj,e);
		}
	}


}
