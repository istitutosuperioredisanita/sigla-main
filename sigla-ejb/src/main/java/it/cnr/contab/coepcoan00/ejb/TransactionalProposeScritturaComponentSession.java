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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.ResultScrittureContabili;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_analiticaBulk;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import java.rmi.RemoteException;
import java.sql.Timestamp;

public class TransactionalProposeScritturaComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements ProposeScritturaComponentSession {
    @Override
    public Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppia(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException {
		try {
			return (Scrittura_partita_doppiaBulk) invoke("proposeScritturaPartitaDoppia", new Object[]{
					param0,
					param1});
		} catch (java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch (ComponentException ex) {
				throw ex;
			} catch (Throwable ex) {
				throw new RemoteException("Uncaugth exception", ex);
			}
		}
    }

    @Override
    public Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaAnnullo(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException {
        try {
            return (Scrittura_partita_doppiaBulk) invoke("proposeScritturaPartitaDoppiaAnnullo", new Object[]{
                    param0,
                    param1});
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Scrittura_analiticaBulk proposeScritturaAnalitica(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException {
        try {
            return (Scrittura_analiticaBulk) invoke("proposeScritturaAnalitica", new Object[]{
                    param0,
                    param1});
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public ResultScrittureContabili proposeScrittureContabili(UserContext param0, IDocumentoCogeBulk param1) throws ComponentException, RemoteException {
        try {
            return (ResultScrittureContabili) invoke("proposeScrittureContabili", new Object[]{
                    param0,
                    param1});
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Scrittura_partita_doppiaBulk proposeStornoScritturaPartitaDoppia(UserContext param0, Scrittura_partita_doppiaBulk param1, Timestamp param2) throws RemoteException {
        try {
            return (Scrittura_partita_doppiaBulk) invoke("proposeStornoScritturaPartitaDoppia", new Object[]{
                    param0,
                    param1});
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }
}
