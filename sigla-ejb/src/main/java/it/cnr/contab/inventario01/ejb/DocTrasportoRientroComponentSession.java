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

import javax.ejb.EJBException;
import javax.ejb.Remote;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.List;

/**
 * Interfaccia remota (EJB Session) per il Component dei Documenti di Trasporto/Rientro.
 * Espone i servizi per la gestione del ciclo di vita e dei dettagli del documento.
 */
@Remote
public interface DocTrasportoRientroComponentSession extends CRUDDetailComponentSession {

    // ========================================
    // GESTIONE ELIMINAZIONE BENI
    // ========================================

    /**
     * Elimina tutti i dettagli (beni) associati al documento.
     */
    void eliminaTuttiBeniDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

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
    Doc_trasporto_rientroBulk predisponiAllaFirma(
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

    void annullaModificaTrasportoBeni(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException, EJBException, RemoteException;

    void inizializzaBeniDaTrasportare(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void trasportaTuttiBeni(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

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
     * Elimina uno o più beni dalla tabella di appoggio INVENTARIO_BENI_APG.
     */
    void eliminaBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException, RemoteException;

    /**
     * Elimina TUTTI i beni dalla tabella di appoggio INVENTARIO_BENI_APG.
     */
    void eliminaTuttiBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException;

    /**
     * Cerca gli accessori associati a un bene principale nel dettaglio del documento.
     */
    List cercaBeniAccessoriAssociatiInDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException;

    /**
     * Modifica i beni trasportati con opzione di includere gli accessori
     */
    void modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            java.util.BitSet old_ass,
            java.util.BitSet ass)
            throws ComponentException, RemoteException;

    /**
     * Elimina il bene principale E tutti gli accessori associati dalla tabella di appoggio.
     */
    void eliminaBeniPrincipaleConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
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
     * Seleziona i dettagli del documento di rientro per modifica
     * @param userContext Contesto utente
     * @param doc Documento
     * @param bulkClass Classe bulk
     * @param filters Filtri da applicare
     * @return Iterator sui dettagli
     */
    RemoteIterator selectEditDettagliRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Class bulkClass,
            CompoundFindClause filters
    ) throws ComponentException, RemoteException;

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
     * Annulla le modifiche sui beni in rientro
     * @param userContext Contesto utente
     */
    void annullaModificaRientroBeni(
            UserContext userContext
    ) throws ComponentException, RemoteException;

    /**
     * Inizializza la selezione dei beni da far rientrare
     * @param userContext Contesto utente
     */
    void inizializzaBeniDaFarRientrare(
            UserContext userContext
    ) throws ComponentException, RemoteException;

    /**
     * Fa rientrare tutti i beni filtrati
     * @param userContext Contesto utente
     * @param doc Documento rientro
     * @param clauses Filtri
     */
    void rientraTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
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

}