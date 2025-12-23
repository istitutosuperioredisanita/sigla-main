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

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Proxy transazionale per il Component dei Documenti di Trasporto/Rientro
 */
public class TransactionalDocTrasportoRientroComponentSession
        extends it.cnr.jada.ejb.TransactionalCRUDComponentSession
        implements DocTrasportoRientroComponentSession {

    // ========================================
    // METODI CRUD STANDARD (ereditati)
    // ========================================

    @Override
    public RemoteIterator cerca(
            UserContext userContext,
            CompoundFindClause compoundFindClause,
            Class aClass,
            OggettoBulk oggettoBulk,
            String s)
            throws ComponentException, RemoteException {

        try {
            return (RemoteIterator) invoke("cerca", new Object[]{
                    userContext, compoundFindClause, aClass, oggettoBulk, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public OggettoBulk creaConBulk(
            UserContext userContext,
            OggettoBulk oggettoBulk,
            OggettoBulk oggettoBulk1,
            String s)
            throws ComponentException, RemoteException {

        try {
            return (OggettoBulk) invoke("creaConBulk", new Object[]{
                    userContext, oggettoBulk, oggettoBulk1, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public void eliminaConBulk(
            UserContext userContext,
            OggettoBulk[] oggettoBulks,
            OggettoBulk oggettoBulk,
            String s)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaConBulk", new Object[]{
                    userContext, oggettoBulks, oggettoBulk, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public void eliminaConBulk(
            UserContext userContext,
            OggettoBulk oggettoBulk,
            String s)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaConBulk", new Object[]{
                    userContext, oggettoBulk, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public OggettoBulk inizializzaBulkPerInserimento(
            UserContext userContext,
            OggettoBulk oggettoBulk,
            OggettoBulk oggettoBulk1,
            String s)
            throws ComponentException, RemoteException {

        try {
            return (OggettoBulk) invoke("inizializzaBulkPerInserimento", new Object[]{
                    userContext, oggettoBulk, oggettoBulk1, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public OggettoBulk inizializzaBulkPerModifica(
            UserContext userContext,
            OggettoBulk oggettoBulk,
            OggettoBulk oggettoBulk1,
            String s)
            throws ComponentException, RemoteException {

        try {
            return (OggettoBulk) invoke("inizializzaBulkPerModifica", new Object[]{
                    userContext, oggettoBulk, oggettoBulk1, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public OggettoBulk modificaConBulk(
            UserContext userContext,
            OggettoBulk oggettoBulk,
            OggettoBulk oggettoBulk1,
            String s)
            throws ComponentException, RemoteException {

        try {
            return (OggettoBulk) invoke("modificaConBulk", new Object[]{
                    userContext, oggettoBulk, oggettoBulk1, s
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }


    // ========================================
    // GESTIONE ELIMINAZIONE BENI
    // ========================================


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

        try {
            return (RemoteIterator) invoke("cercaBeniTrasportabili", new Object[]{
                    userContext,
                    doc,
                    beni_da_escludere,
                    clauses
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
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

        try {
            return (Doc_trasporto_rientroBulk) invoke("predisponiAllaFirma", new Object[]{
                    userContext,
                    doc
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    // ========================================
    // GESTIONE BENI - AGGIUNTA/RIMOZIONE/MODIFICA
    // ========================================

    public it.cnr.jada.util.RemoteIterator selectEditDettagliTrasporto(it.cnr.jada.UserContext param0, it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1, java.lang.Class param2, it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (it.cnr.jada.util.RemoteIterator) invoke("selectEditDettagliTrasporto", new Object[]{
                    param0,
                    param1,
                    param2,
                    param3});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public RemoteIterator getListaBeniDaTrasportare(UserContext userContext, Doc_trasporto_rientroBulk bulk, SimpleBulkList beni_da_escludere, CompoundFindClause clauses) throws ComponentException, RemoteException {
        try {
            return (it.cnr.jada.util.RemoteIterator) invoke("getListaBeniDaTrasportare", new Object[]{
                    userContext,
                    bulk,
                    beni_da_escludere,
                    clauses});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public SimpleBulkList selezionati(UserContext userContext, Doc_trasporto_rientroBulk docT) throws ComponentException, RemoteException {
        try {
            return (SimpleBulkList) invoke("selezionati", new Object[]{
                    userContext,
                    docT});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.util.RemoteIterator selectBeniAssociatiByClause(it.cnr.jada.UserContext param0, Doc_trasporto_rientroBulk param2, java.lang.Class param3) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (it.cnr.jada.util.RemoteIterator) invoke("selectBeniAssociatiByClause", new Object[]{
                    param0,
                    param2,
                    param3});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }

    }

    public void annullaModificaTrasportoBeni(UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            invoke("annullaModificaTrasportoBeni", new Object[]{
                    param0});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void inizializzaBeniDaTrasportare(it.cnr.jada.UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            invoke("inizializzaBeniDaTrasportare", new Object[]{
                    param0});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void trasportaTuttiBeni(it.cnr.jada.UserContext param0, it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1, it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            invoke("trasportaTuttiBeni", new Object[]{
                    param0,
                    param1,
                    param3});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }


    @Override
    public List cercaBeniAccessoriAssociati(
            UserContext userContext,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException {

        try {
            return (List) invoke("cercaBeniAccessoriAssociati", new Object[]{
                    userContext,
                    benePrincipale
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public void eliminaBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaBeniAssociati", new Object[]{
                    userContext,
                    doc,
                    beni
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public void eliminaTuttiBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaTuttiBeniAssociati", new Object[]{
                    userContext,
                    doc
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public List cercaBeniAccessoriAssociatiInDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException, RemoteException {

        try {
            return (List) invoke("cercaBeniAccessoriAssociatiInDettaglio", new Object[]{
                    userContext,
                    doc,
                    benePrincipale
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
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

        try {
            invoke("modificaBeniTrasportatiConAccessori", new Object[]{
                    userContext,
                    docT,
                    beni,
                    old_ass,
                    ass
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    @Override
    public void eliminaBeniPrincipaleConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaBeniPrincipaleConAccessori", new Object[]{
                    userContext,
                    doc,
                    benePrincipale,
                    beniAccessori
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
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

        try {
            invoke("annullaDocumento", new Object[]{
                    userContext,
                    doc
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
        return doc;
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

        try {
            return (RemoteIterator) invoke("selectEditDettagliRientro", new Object[]{
                    userContext,
                    doc,
                    bulkClass,
                    filters
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    /**
     * Ottiene la lista dei beni disponibili per il rientro
     */
    @Override
    public RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException, RemoteException {

        try {
            return (RemoteIterator) invoke("getListaBeniDaFarRientrare", new Object[]{
                    userContext,
                    doc,
                    beniEsclusi,
                    clauses
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    /**
     * Annulla le modifiche sui beni in rientro
     */
    @Override
    public void annullaModificaRientroBeni(UserContext userContext)
            throws ComponentException, RemoteException {

        try {
            invoke("annullaModificaRientroBeni", new Object[]{
                    userContext
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    /**
     * Inizializza la selezione dei beni da far rientrare
     */
    @Override
    public void inizializzaBeniDaFarRientrare(UserContext userContext)
            throws ComponentException, RemoteException {

        try {
            invoke("inizializzaBeniDaFarRientrare", new Object[]{
                    userContext
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
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

        try {
            invoke("rientraTuttiBeni", new Object[]{
                    userContext,
                    doc,
                    clauses
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
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

        try {
            invoke("modificaBeniRientratiConAccessori", new Object[]{
                    userContext,
                    doc,
                    bulks,
                    oldSelection,
                    newSelection
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    public boolean isEsercizioCOEPChiuso(UserContext userContext) throws ComponentException, RemoteException {
        try {

            return ((Boolean)invoke("isEsercizioCOEPChiuso",new Object[] {
                    userContext})).booleanValue();
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException, RemoteException {

        try {
            return (List) invoke("getDocumentiPredispostiAllaFirma", new Object[]{
                    userContext
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(it.cnr.jada.UserContext param0,it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerStampa",new Object[] {
                    param0,
                    param1 });
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(it.cnr.jada.UserContext param0,it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk)invoke("stampaConBulk",new Object[] {
                    param0,
                    param1 });
        } catch(java.rmi.RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public Doc_trasporto_rientroBulk salvaDefinitivo(UserContext userContext, Doc_trasporto_rientroBulk docTR) throws ComponentException, RemoteException {
        try {
            return (Doc_trasporto_rientroBulk) invoke("salvaDefinitivo", new Object[]{
                    userContext,
                    docTR});
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (it.cnr.jada.comp.ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new java.rmi.RemoteException("Uncaugth exception", ex);
            }
        }
    }

    /**
     * Cerca gli accessori presenti nel trasporto originale
     */
    @Override
    public List cercaBeniAccessoriPresentinelTrasportoOriginale(
            UserContext userContext,
            Inventario_beniBulk beneRientro,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        try {
            return (List) invoke("cercaBeniAccessoriPresentinelTrasportoOriginale", new Object[]{
                    userContext,
                    beneRientro,
                    doc
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }

    }

    @Override
    public void selezionaTuttiBeni(UserContext userContext, Doc_trasporto_rientroBulk doc, CompoundFindClause clauses) throws ComponentException, RemoteException {
        try {
            invoke("selezionaTuttiBeni", new Object[]{
                    userContext,
                    doc,
                    clauses
            });
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaught exception", ex);
            }
        }
    }
}