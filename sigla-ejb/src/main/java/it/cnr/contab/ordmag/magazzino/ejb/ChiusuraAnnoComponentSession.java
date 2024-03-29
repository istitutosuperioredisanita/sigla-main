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
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.contab.ordmag.ordini.bulk.EvasioneOrdineRigaBulk;
import it.cnr.contab.ordmag.ordini.bulk.FatturaOrdineBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.contab.ordmag.ordini.dto.ImportoOrdine;
import it.cnr.contab.ordmag.ordini.dto.ParametriCalcoloImportoOrdine;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.RemoteIterator;

import javax.ejb.Remote;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Remote
public interface ChiusuraAnnoComponentSession extends it.cnr.jada.ejb.CRUDComponentSession, it.cnr.jada.ejb.PrintComponentSession {

   List<ValoriLottoPerAnno> getValoriLottoPerAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo) throws RemoteException, ComponentException, PersistencyException;

   ChiusuraAnnoBulk calcolaRimanenzeAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo,String statoChiusura) throws RemoteException, ComponentException, PersistencyException;

   ChiusuraAnnoBulk verificaChiusuraAnno(UserContext userContext,Integer esercizio,String tipoChiusura) throws ComponentException, PersistencyException, RemoteException;

   ChiusuraAnnoBulk salvaChiusuraDefinitiva(UserContext userContext,Integer esercizio,String tipoChiusura,Date dataFinePeriodo) throws RemoteException, ComponentException,PersistencyException, ParseException;

   void annullaChiusuraDefinitiva(UserContext userContext,Integer esercizio,String tipoChiusura) throws RemoteException, ComponentException,PersistencyException, ParseException;
}