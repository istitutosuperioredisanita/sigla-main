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
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;

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

    @Override
    public void eliminaTuttiBeniDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, RemoteException {

        try {
            invoke("eliminaTuttiBeniDettaglio", new Object[]{
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

//    @Override
//    public void eliminaBeniSelezionati(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk[] dettagliDaEliminare)
//            throws ComponentException, RemoteException {
//
//        try {
//            invoke("eliminaBeniSelezionati", new Object[]{
//                    userContext,
//                    doc,
//                    dettagliDaEliminare
//            });
//        } catch (java.rmi.RemoteException e) {
//            throw e;
//        } catch (java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch (ComponentException ex) {
//                throw ex;
//            } catch (Throwable ex) {
//                throw new RemoteException("Uncaught exception", ex);
//            }
//        }
//    }

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
    // VALIDAZIONE BENI
    // ========================================


//    @Override
//    public void validaBenePerRientro(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk docRientro,
//            Doc_trasporto_rientro_dettBulk dettRientro)
//            throws ComponentException, RemoteException {
//
//        try {
//            invoke("validaBenePerRientro", new Object[]{
//                    userContext,
//                    docRientro,
//                    dettRientro
//            });
//        } catch (java.rmi.RemoteException e) {
//            throw e;
//        } catch (java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch (ComponentException ex) {
//                throw ex;
//            } catch (Throwable ex) {
//                throw new RemoteException("Uncaught exception", ex);
//            }
//        }
//    }

    // ========================================
    // GESTIONE WORKFLOW - TRANSIZIONI STATO
    // ========================================


    @Override
    public Doc_trasporto_rientroBulk predisponiAllaFirma(
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
//
//    @Override
//    public Doc_trasporto_rientroBulk aggiungiBeneDDT(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Inventario_beniBulk bene)
//            throws ComponentException, RemoteException {
//
//        try {
//            return (Doc_trasporto_rientroBulk) invoke("aggiungiBeneDDT", new Object[]{
//                    userContext,
//                    doc,
//                    bene
//            });
//        } catch (java.rmi.RemoteException e) {
//            throw e;
//        } catch (java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch (ComponentException ex) {
//                throw ex;
//            } catch (Throwable ex) {
//                throw new RemoteException("Uncaught exception", ex);
//            }
//        }
//    }
//
//    @Override
//    public Doc_trasporto_rientroBulk rimuoviBeneDDT(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk dettaglio)
//            throws ComponentException, RemoteException {
//
//        try {
//            return (Doc_trasporto_rientroBulk) invoke("rimuoviBeneDDT", new Object[]{
//                    userContext,
//                    doc,
//                    dettaglio
//            });
//        } catch (java.rmi.RemoteException e) {
//            throw e;
//        } catch (java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch (ComponentException ex) {
//                throw ex;
//            } catch (Throwable ex) {
//                throw new RemoteException("Uncaught exception", ex);
//            }
//        }
//    }

//    @Override
//    public Doc_trasporto_rientroBulk aggiornaQuantitaBene(
//            UserContext userContext,
//            Doc_trasporto_rientroBulk doc,
//            Doc_trasporto_rientro_dettBulk dettaglio)
//            throws ComponentException, RemoteException {
//
//        try {
//            return (Doc_trasporto_rientroBulk) invoke("aggiornaQuantitaBene", new Object[]{
//                    userContext,
//                    doc,
//                    dettaglio
//            });
//        } catch (java.rmi.RemoteException e) {
//            throw e;
//        } catch (java.lang.reflect.InvocationTargetException e) {
//            try {
//                throw e.getTargetException();
//            } catch (ComponentException ex) {
//                throw ex;
//            } catch (Throwable ex) {
//                throw new RemoteException("Uncaught exception", ex);
//            }
//        }
//    }

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

    public void modificaBeniTrasportati(it.cnr.jada.UserContext param0, it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk param1, it.cnr.jada.bulk.OggettoBulk[] param2, java.util.BitSet param3, java.util.BitSet param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            invoke("modificaBeniTrasportati", new Object[]{
                    param0,
                    param1,
                    param2,
                    param3,
                    param4});
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

    public void eliminaBeniAssociatiConBulk(UserContext param0, OggettoBulk param1) throws ComponentException, RemoteException {
        try {
            invoke("eliminaBeniAssociatiConBulk", new Object[]{
                    param0,
                    param1});
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

    public void eliminaBeniAssociatiConBulk(it.cnr.jada.UserContext param0, OggettoBulk param1, it.cnr.jada.bulk.OggettoBulk[] param2) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            invoke("eliminaBeniAssociatiConBulk", new Object[]{
                    param0,
                    param1,
                    param2});
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
}