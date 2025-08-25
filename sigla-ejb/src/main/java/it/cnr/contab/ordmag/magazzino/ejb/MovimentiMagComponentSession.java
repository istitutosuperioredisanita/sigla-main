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
import java.util.List;

@Remote
public interface MovimentiMagComponentSession extends it.cnr.jada.ejb.CRUDComponentSession, it.cnr.jada.ejb.PrintComponentSession{
	MovimentiMagBulk caricoDaOrdine(UserContext userContext, OrdineAcqConsegnaBulk consegna, EvasioneOrdineRigaBulk evasioneOrdineRiga) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    List<BollaScaricoMagBulk> generaBolleScarico(UserContext userContext, List<MovimentiMagBulk> listaMovimentiScarico) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    ScaricoMagazzinoBulk scaricaMagazzino(UserContext userContext, ScaricoMagazzinoBulk scaricoMagazzino) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    CaricoMagazzinoBulk caricaMagazzino(UserContext userContext, CaricoMagazzinoBulk caricoMagazzino) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    void annullaMovimento(UserContext userContext, MovimentiMagBulk movimentiMagBulk) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    MovimentiMagazzinoBulk initializeMovimentiMagazzino(UserContext usercontext, MovimentiMagazzinoBulk movimentiMagazzinoBulk) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
	AbilitazioneMagazzinoBulk initializeAbilitazioneMovimentiMagazzino(UserContext usercontext, AbilitazioneMagazzinoBulk abilitazioneMagazzinoBulk) throws PersistencyException, ComponentException , RemoteException, ApplicationException;
	RemoteIterator preparaQueryBolleScaricoDaVisualizzare(UserContext userContext, List<BollaScaricoMagBulk> bolle)throws ComponentException, RemoteException;
    public java.util.Collection<LottoMagBulk> findLottiMagazzino(UserContext userContext, MovimentiMagazzinoRigaBulk movimentiMagazzinoRigaBulk) throws ComponentException, PersistencyException, RemoteException, ApplicationException;
    public RemoteIterator ricercaMovimenti(UserContext userContext, ParametriSelezioneMovimentiBulk parametri) throws ComponentException, RemoteException;
    public MovimentiMagBulk creaMovimentoRettificaValoreOrdine(UserContext userContext, FatturaOrdineBulk fatturaOrdineBulk) throws ComponentException, RemoteException;
    public ImportoOrdine calcoloImporto(UserContext userContext, ParametriCalcoloImportoOrdine parametri) throws RemoteException,ComponentException;
    void creaMovimentoChiusura(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura, java.sql.Timestamp dataRiferimentoMovimento) throws RemoteException, ComponentException;
    void eliminaMovimentoChiusura(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura, java.sql.Timestamp dataRiferimentoMovimento) throws RemoteException, ComponentException;

    List<EvasioneOrdineRigaBulk> caricoDaOrdineRigheEvase(UserContext userContext,  List<EvasioneOrdineRigaBulk> righeEvase) throws ComponentException, PersistencyException, RemoteException, ApplicationException;

}
