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

import it.cnr.contab.coepcoan00.comp.ProposeScritturaComponent;
import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotEnabledException;
import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotRequiredException;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.ResultScrittureContabili;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationRuntimeException;
import it.cnr.jada.comp.ComponentException;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import java.rmi.RemoteException;
import java.sql.Timestamp;

@Stateless(name = "CNRCOEPCOAN00_EJB_ProposeScritturaComponentSession")
public class ProposeScritturaComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements ProposeScritturaComponentSession {


    @PostConstruct
    public void ejbCreate() {
        componentObj = new ProposeScritturaComponent();
    }

    public IDocumentoCogeBulk loadEconomicaFromFinanziaria(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        pre_component_invocation(param0, componentObj);
        try {
            IDocumentoCogeBulk result = ((ProposeScritturaComponent) componentObj).loadEconomicaFromFinanziaria(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException | ScritturaPartitaDoppiaNotEnabledException | ScritturaPartitaDoppiaNotRequiredException e) {
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

    public ResultScrittureContabili proposeScritturaPartitaDoppiaAnnullo(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, ScritturaPartitaDoppiaNotEnabledException {
        pre_component_invocation(param0, componentObj);
        try {
            ResultScrittureContabili result = ((ProposeScritturaComponent) componentObj).proposeScritturaPartitaDoppiaAnnullo(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
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

    public ResultScrittureContabili proposeScritturaPartitaDoppia(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        pre_component_invocation(param0, componentObj);
        try {
            ResultScrittureContabili result = ((ProposeScritturaComponent) componentObj).proposeScritturaPartitaDoppia(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException | ScritturaPartitaDoppiaNotEnabledException | ScritturaPartitaDoppiaNotRequiredException e) {
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

    public ResultScrittureContabili proposeScrittureContabili(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        pre_component_invocation(param0, componentObj);
        try {
            ResultScrittureContabili result = ((ProposeScritturaComponent) componentObj).proposeScrittureContabili(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException | ScritturaPartitaDoppiaNotEnabledException | ScritturaPartitaDoppiaNotRequiredException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (ApplicationRuntimeException e) {
            component_invocation_failure(param0, componentObj);
            throw new ComponentException(e);
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public Scrittura_partita_doppiaBulk proposeStornoScritturaPartitaDoppia(UserContext param0, Scrittura_partita_doppiaBulk param1, Timestamp param2)  {
        pre_component_invocation(param0, componentObj);
        try {
            Scrittura_partita_doppiaBulk result = ((ProposeScritturaComponent) componentObj).proposeStornoScritturaPartitaDoppia(param0, param1, param2);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }
}