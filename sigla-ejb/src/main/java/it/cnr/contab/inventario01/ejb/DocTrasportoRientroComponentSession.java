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

package it.cnr.contab.inventario01.ejb;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDDetailComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import javax.ejb.Remote;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.List;

/**
 * Interfaccia remota EJB per il Component dei Documenti di Trasporto/Rientro.
 *
 * Espone i servizi per:
 * - CRUD documenti trasporto/rientro
 * - Gestione workflow (Inserito → Inviato → Definitivo)
 * - Selezione e modifica beni con accessori
 * - Validazione e salvataggio definitivo
 */
@Remote
public interface DocTrasportoRientroComponentSession extends CRUDDetailComponentSession, it.cnr.jada.ejb.PrintComponentSession {

    /** Cerca beni disponibili per il trasporto con filtri su inventario/UO/stato */
    RemoteIterator cercaBeniTrasportabili(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException;

    /** Sposta documento da INSERITO a PREDISPOSTO_FIRMA */
    Doc_trasporto_rientroBulk changeStatoInInviato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /** Carica dettagli documento per modifica */
    RemoteIterator selectEditDettagliTrasporto(
            UserContext param0,
            Doc_trasporto_rientroBulk param1,
            Class param2,
            CompoundFindClause param3)
            throws ComponentException, RemoteException;

    /** Restituisce lista beni da trasportare con filtri */
    RemoteIterator getListaBeniDaTrasportare(
            UserContext userContext,
            Doc_trasporto_rientroBulk bulk,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException;

    /** Restituisce beni selezionati per il documento */
    SimpleBulkList selezionati(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT)
            throws ComponentException, RemoteException;

    /** Carica beni associati al documento */
    RemoteIterator selectBeniAssociatiByClause(
            UserContext param0,
            Doc_trasporto_rientroBulk param2,
            Class param3)
            throws ComponentException, RemoteException;

    /** Trova beni accessori associati a un bene principale */
    List cercaBeniAccessoriAssociati(
            UserContext userContext,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException;

    /** Modifica beni trasportati includendo opzionalmente gli accessori */
    Doc_trasporto_rientroBulk modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            BitSet old_ass,
            BitSet ass)
            throws ComponentException, RemoteException;

    /** Annulla documento (solo cambio stato, non tocca dettagli) */
    Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /** Restituisce lista beni disponibili per il rientro */
    RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException;

    /** Modifica beni rientrati gestendo gli accessori */
    Doc_trasporto_rientroBulk modificaBeniRientratiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException, RemoteException;

    /** Verifica se esercizio COEP è chiuso */
    boolean isEsercizioCOEPChiuso(UserContext userContext)
            throws ComponentException, RemoteException;

    /** Recupera documenti predisposti alla firma (stato=INV, idFlusso!=NULL) */
    List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException, RemoteException;

    /** Salva documento in stato DEFINITIVO con validazione */
    Doc_trasporto_rientroBulk salvaDefinitivo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR)
            throws ComponentException, RemoteException;

    /** Cerca accessori presenti nel trasporto originale (per rientro) */
    List cercaBeniAccessoriPresentinelTrasportoOriginale(
            UserContext userContext,
            Inventario_beniBulk beneRientro,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /** Seleziona tutti i beni disponibili con filtri */
    void selezionaTuttiBeni(
            UserContext param0,
            Doc_trasporto_rientroBulk param1,
            CompoundFindClause param3)
            throws ComponentException, RemoteException;

    /** Elimina tutti i dettagli salvati del documento */
    void eliminaTuttiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /** Elimina dettagli specifici salvati */
    void eliminaDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException, RemoteException;

    /** Cerca accessori di un bene principale nei dettagli salvati */
    List cercaBeniAccessoriNeiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException;

    /** Elimina bene principale e accessori dai dettagli salvati */
    void eliminaBeniPrincipaleConAccessoriDaDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException, RemoteException;

    /** Restituisce dettagli del documento */
    BulkList getDetailsFor(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /** Carica beni disponibili per inserimento con validazione */
    List<Inventario_beniBulk> caricaBeniPerInserimento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses,
            boolean isTrasporto)
            throws ComponentException, RemoteException;

    /** Valida che i beni non siano già presenti in altri documenti */
    void validaBeniNonInAltriDocumenti(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            List<Inventario_beniBulk> beniDaValidare)
            throws ComponentException, RemoteException;

    /** Carica TerzoBulk completo da codice anagrafico */
    TerzoBulk caricaTerzoDaAnagrafico(
            UserContext userContext,
            Integer cdAnag)
            throws ComponentException, RemoteException;
}