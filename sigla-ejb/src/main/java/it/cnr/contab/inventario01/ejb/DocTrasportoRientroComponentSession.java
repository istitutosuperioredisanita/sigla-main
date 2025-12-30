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

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.jada.UserContext;
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
 * Interfaccia remota (EJB Session) per il Component dei Documenti di Trasporto/Rientro.
 * Espone i servizi per la gestione del ciclo di vita e dei dettagli del documento.
 */
@Remote
public interface DocTrasportoRientroComponentSession extends CRUDDetailComponentSession,it.cnr.jada.ejb.PrintComponentSession {



    // ========================================
    // GESTIONE ELIMINAZIONE BENI
    // ========================================


    // ========================================
    // RICERCA BENI DISPONIBILI
    // ========================================

    /**
     * Cerca i beni disponibili per il trasporto.
     * Applica filtri su inventario, UO (dipendente/utente) e stato (non trasportato).
     */
    RemoteIterator cercaBeniTrasportabili(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException;

    // ========================================
    // GESTIONE WORKFLOW - TRANSIZIONI STATO
    // ========================================

    /**
     * Sposta il documento di stato da **INSERITO** a **PREDISPOSTO_FIRMA**.
     */
    Doc_trasporto_rientroBulk changeStatoInInviato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    // ========================================
    // GESTIONE BENI - METODI STANDARD
    // ========================================

    it.cnr.jada.util.RemoteIterator selectEditDettagliTrasporto(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,java.lang.Class param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    RemoteIterator getListaBeniDaTrasportare(UserContext userContext, Doc_trasporto_rientroBulk bulk, SimpleBulkList beni_da_escludere, CompoundFindClause clauses)throws ComponentException,java.rmi.RemoteException;

    SimpleBulkList selezionati(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docT) throws it.cnr.jada.comp.ComponentException,RemoteException;

    it.cnr.jada.util.RemoteIterator selectBeniAssociatiByClause(it.cnr.jada.UserContext param0, Doc_trasporto_rientroBulk param2, java.lang.Class param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    // ========================================
    // GESTIONE BENI ACCESSORI [NUOVI METODI]
    // ========================================

    /**
     * Trova i beni accessori associati a un bene principale
     */
    List cercaBeniAccessoriAssociati(
            UserContext userContext,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException;


    /**
     * Modifica i beni trasportati con opzione di includere gli accessori
     *
     * @return
     */
    Doc_trasporto_rientroBulk modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            BitSet old_ass,
            BitSet ass)
            throws ComponentException, RemoteException;


    /**
     * Annulla logicamente il documento cambiando solo lo stato.
     * NON tocca i dettagli associati.
     */
    Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;



    /**
     * Ottiene la lista dei beni disponibili per il rientro
     * @param userContext Contesto utente
     * @param doc Documento rientro
     * @param beniEsclusi Beni da escludere
     * @param clauses Filtri
     * @return Iterator sui beni
     */
    RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses
    ) throws ComponentException, RemoteException;

    /**
     * Modifica i beni rientrati gestendo gli accessori
     * @param userContext Contesto utente
     * @param doc Documento rientro
     * @param bulks Array di beni
     * @param oldSelection Selezione precedente
     * @param newSelection Nuova selezione
     */
    void modificaBeniRientratiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection
    ) throws ComponentException, RemoteException;


    boolean isEsercizioCOEPChiuso(it.cnr.jada.UserContext userContext) throws ComponentException,java.rmi.RemoteException;


    /**
     * Recupera i documenti predisposti alla firma.
     *
     * Criteri di selezione:
     * - Stato = INVIATO (INV)
     * - statoFlusso = INV
     * - idFlussoHappysign valorizzato (NOT NULL)
     *
     * @param userContext contesto utente
     * @return lista dei documenti predisposti alla firma
     * @throws ComponentException in caso di errore
     */
    List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException, RemoteException;


    Doc_trasporto_rientroBulk salvaDefinitivo(UserContext userContext, Doc_trasporto_rientroBulk docTR)
            throws ComponentException, RemoteException;

    List cercaBeniAccessoriPresentinelTrasportoOriginale(
            UserContext userContext,
            Inventario_beniBulk beneRientro,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;


    void selezionaTuttiBeni(UserContext param0, Doc_trasporto_rientroBulk param1, CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;


    // ========================================
// NUOVI METODI - ELIMINAZIONE DA DETTAGLI SALVATI
// ========================================

    /**
     * Elimina TUTTI i dettagli salvati di un documento.
     * Usato durante MODIFICA per "Elimina tutti".
     */
    void eliminaTuttiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /**
     * Elimina dettagli specifici salvati di un documento.
     * Usato durante MODIFICA per eliminazione selettiva.
     */
    void eliminaDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException, RemoteException;

    /**
     * Cerca accessori di un bene principale NEI DETTAGLI SALVATI.
     * Usato durante MODIFICA per verificare accessori già persistiti.
     */
    List cercaBeniAccessoriNeiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException;

    /**
     * Elimina il bene principale E tutti gli accessori DAI DETTAGLI SALVATI.
     * Usato durante MODIFICA per eliminazione ricorsiva.
     */
    void eliminaBeniPrincipaleConAccessoriDaDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException, RemoteException;

    List getDetailsFor(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;
}