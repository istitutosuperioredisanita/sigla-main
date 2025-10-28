/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.inventario01.ejb;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
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

//    /**
//     * Elimina SOLO i beni selezionati dal dettaglio
//     */
//    void eliminaBeniSelezionati(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk[] dettagliDaEliminare)
//            throws ComponentException, RemoteException;

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
    // VALIDAZIONE BENI
    // ========================================


//    /**
//     * Valida un bene (dettaglio) per il Rientro.
//     * Verifica la correttezza dei riferimenti al documento di trasporto originale.
//     */
//    void validaBenePerRientro(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk docRientro,
//            Doc_trasporto_rientro_dettBulk dettRientro)
//            throws ComponentException, RemoteException;

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
    // GESTIONE BENI - AGGIUNTA/RIMOZIONE/MODIFICA
    // ========================================

//    /**
//     * Aggiunge un bene al documento di trasporto/rientro.
//     */
//    Doc_trasporto_rientroBulk aggiungiBeneDDT(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Inventario_beniBulk bene)
//            throws ComponentException, RemoteException;

//    /**
//     * Rimuove un bene (dettaglio) dal documento.
//     */
//    Doc_trasporto_rientroBulk rimuoviBeneDDT(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk dettaglio)
//            throws ComponentException, RemoteException;
//
//    /**
//     * Aggiorna la quantità di un bene (dettaglio) nel documento.
//     */
//    Doc_trasporto_rientroBulk aggiornaQuantitaBene(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk dettaglio)
//            throws ComponentException, RemoteException;

    it.cnr.jada.util.RemoteIterator selectEditDettagliTrasporto(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,java.lang.Class param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;


    RemoteIterator getListaBeniDaTrasportare(UserContext userContext, Doc_trasporto_rientroBulk bulk, SimpleBulkList beni_da_escludere, CompoundFindClause clauses)throws ComponentException,java.rmi.RemoteException;


    SimpleBulkList selezionati(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docT) throws it.cnr.jada.comp.ComponentException,RemoteException;


    it.cnr.jada.util.RemoteIterator selectBeniAssociatiByClause(it.cnr.jada.UserContext param0, Doc_trasporto_rientroBulk param2, java.lang.Class param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void annullaModificaTrasportoBeni(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException, EJBException, RemoteException;

    void inizializzaBeniDaTrasportare(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void trasportaTuttiBeni(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void modificaBeniTrasportati(UserContext userContext,Doc_trasporto_rientroBulk docT, OggettoBulk[] beni,java.util.BitSet old_ass,java.util.BitSet ass) throws ComponentException, RemoteException ;

    void eliminaBeniAssociatiConBulk(it.cnr.jada.UserContext param0,OggettoBulk param1) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void eliminaBeniAssociatiConBulk(it.cnr.jada.UserContext param0,OggettoBulk param1,it.cnr.jada.bulk.OggettoBulk[] param2) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

}