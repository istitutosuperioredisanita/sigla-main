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

import it.cnr.contab.doccont00.comp.OrdineComponent;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.comp.DocTrasportoRientroComponent;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Bean implementation class per il Component dei Documenti di Trasporto/Rientro
 */
@Stateless(name = "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession")
public class DocTrasportoRientroComponentSessionBean
        extends it.cnr.jada.ejb.CRUDDetailComponentSessionBean
        implements DocTrasportoRientroComponentSession {

    @PostConstruct
    public void ejbCreate() {
        componentObj = new DocTrasportoRientroComponent();
    }

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance()
            throws javax.ejb.EJBException {
        return new DocTrasportoRientroComponentSessionBean();
    }

    // ========================================
    // RICERCA BENI DISPONIBILI
    // ========================================

    @Override
    public RemoteIterator cercaBeniTrasportabili(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            RemoteIterator result = ((DocTrasportoRientroComponent) componentObj)
                    .cercaBeniTrasportabili(userContext, doc, beni_da_escludere, clauses);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }

    // ========================================
    // GESTIONE WORKFLOW - TRANSIZIONI STATO
    // ========================================

    @Override
    public Doc_trasporto_rientroBulk changeStatoInInviato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            Doc_trasporto_rientroBulk result = ((DocTrasportoRientroComponent) componentObj)
                    .changeStatoInInviato(userContext, doc);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    // ========================================
    // GESTIONE BENI - AGGIUNTA/RIMOZIONE/MODIFICA
    // ========================================

    public it.cnr.jada.util.RemoteIterator selectEditDettagliTrasporto(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,java.lang.Class param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.util.RemoteIterator result = ((DocTrasportoRientroComponent)componentObj).selectEditDettagliTrasporto(param0,param1,param2,param3);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    public it.cnr.jada.util.RemoteIterator getListaBeniDaTrasportare(it.cnr.jada.UserContext param0, it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1, SimpleBulkList param3, it.cnr.jada.persistency.sql.CompoundFindClause param4) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.util.RemoteIterator result = ((DocTrasportoRientroComponent)componentObj).getListaBeniDaTrasportare(param0,param1,param3,param4);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    public SimpleBulkList selezionati(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            SimpleBulkList result = ((DocTrasportoRientroComponent)componentObj).selezionati(param0,param1);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }


    public it.cnr.jada.util.RemoteIterator selectBeniAssociatiByClause(it.cnr.jada.UserContext param0, Doc_trasporto_rientroBulk param2, java.lang.Class param3) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.util.RemoteIterator result = ((DocTrasportoRientroComponent)componentObj).selectBeniAssociatiByClause(param0,param2,param3);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    public void annullaModificaTrasportoBeni(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            ((DocTrasportoRientroComponent)componentObj).annullaModificaTrasportoBeni(param0);
            component_invocation_succes(param0,componentObj);
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    public void inizializzaBeniDaTrasportare(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            ((DocTrasportoRientroComponent)componentObj).inizializzaBeniDaTrasportare(param0);
            component_invocation_succes(param0,componentObj);
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    public void trasportaTuttiBeni(it.cnr.jada.UserContext param0,it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            ((DocTrasportoRientroComponent)componentObj).trasportaTuttiBeni(param0,param1,param3);
            component_invocation_succes(param0,componentObj);
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }


    @Override
    public List cercaBeniAccessoriAssociati(
            UserContext userContext,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            List result = ((DocTrasportoRientroComponent) componentObj)
                    .cercaBeniAccessoriAssociati(userContext, benePrincipale);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public void eliminaBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .eliminaBeniAssociati(userContext, doc, beni);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public void eliminaTuttiBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .eliminaTuttiBeniAssociati(userContext, doc);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public List cercaBeniAccessoriAssociatiInDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            List result = ((DocTrasportoRientroComponent) componentObj)
                    .cercaBeniAccessoriAssociatiInDettaglio(userContext, doc, benePrincipale);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    /**
     * [AGGIUNGI] Modifica i beni trasportati con opzione di includere gli accessori
     */
    @Override
    public void modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            java.util.BitSet old_ass,
            java.util.BitSet ass)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .modificaBeniTrasportatiConAccessori(userContext, docT, beni, old_ass, ass);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }


    @Override
    public void eliminaBeniPrincipaleConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .eliminaBeniPrincipaleConAccessori(userContext, doc, benePrincipale, beniAccessori);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }


    /**
     * Annulla logicamente il documento cambiando solo lo stato.
     * NON tocca i dettagli associati.
     */
    @Override
    public Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        Doc_trasporto_rientroBulk docTR;
        try {
            docTR = ((DocTrasportoRientroComponent) componentObj)
                    .annullaDocumento(userContext, doc);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
        return docTR;
    }





    /**
     * Seleziona i dettagli del documento di rientro per modifica
     */
    @Override
    public RemoteIterator selectEditDettagliRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Class bulkClass,
            CompoundFindClause filters)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            RemoteIterator result = ((DocTrasportoRientroComponent) componentObj)
                    .selectEditDettagliRientro(userContext, doc, bulkClass, filters);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }


    /**
     * Ottiene la lista dei beni disponibili per il rientro (wrapper)
     */
    @Override
    public RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            RemoteIterator result = ((DocTrasportoRientroComponent) componentObj)
                    .getListaBeniDaFarRientrare(userContext, doc, beniEsclusi, clauses);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    /**
     * Annulla le modifiche sui beni in rientro
     */
    @Override
    public void annullaModificaRientroBeni(UserContext userContext)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .annullaModificaRientroBeni(userContext);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    /**
     * Inizializza la selezione dei beni da far rientrare
     */
    @Override
    public void inizializzaBeniDaFarRientrare(UserContext userContext)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .inizializzaBeniDaFarRientrare(userContext);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    /**
     * Fa rientrare tutti i beni filtrati
     */
    @Override
    public void rientraTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .rientraTuttiBeni(userContext, doc, clauses);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    /**
     * Modifica i beni rientrati gestendo gli accessori
     */
    @Override
    public void modificaBeniRientratiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] bulks,
            java.util.BitSet oldSelection,
            java.util.BitSet newSelection)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            ((DocTrasportoRientroComponent) componentObj)
                    .modificaBeniRientratiConAccessori(userContext, doc, bulks, oldSelection, newSelection);
            component_invocation_succes(userContext, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    public boolean isEsercizioCOEPChiuso(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException, EJBException{
        pre_component_invocation(param0,componentObj);
        try {
            boolean result = ((DocTrasportoRientroComponent)componentObj).isEsercizioCOEPChiuso(param0);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    @Override
    public List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException, RemoteException {

        pre_component_invocation(userContext, componentObj);
        try {
            List result = ((DocTrasportoRientroComponent) componentObj)
                    .getDocumentiPredispostiAllaFirma(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(it.cnr.jada.UserContext param0,it.cnr.jada.bulk.OggettoBulk param1) throws it.cnr.jada.comp.ComponentException,javax.ejb.EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.bulk.OggettoBulk result = ((DocTrasportoRientroComponent)componentObj).inizializzaBulkPerStampa(param0,param1);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }
    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(it.cnr.jada.UserContext param0,it.cnr.jada.bulk.OggettoBulk param1) throws it.cnr.jada.comp.ComponentException,javax.ejb.EJBException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.bulk.OggettoBulk result = ((DocTrasportoRientroComponent)componentObj).stampaConBulk(param0,param1);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }
}