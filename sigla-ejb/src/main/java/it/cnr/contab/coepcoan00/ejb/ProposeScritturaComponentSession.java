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

import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotEnabledException;
import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotRequiredException;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.ResultScrittureContabili;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import jakarta.ejb.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;

@Remote
public interface ProposeScritturaComponentSession extends it.cnr.jada.ejb.CRUDComponentSession {
    IDocumentoCogeBulk loadEconomicaFromFinanziaria(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException;
    ResultScrittureContabili proposeScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk docamm) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException;
    ResultScrittureContabili proposeScritturaPartitaDoppiaAnnullo(UserContext userContext, IDocumentoCogeBulk docamm) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException;
    Scrittura_partita_doppiaBulk proposeStornoScritturaPartitaDoppia(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaDaStornare, Timestamp dataStorno) throws RemoteException;
    ResultScrittureContabili proposeScrittureContabili(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, RemoteException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException;
}
