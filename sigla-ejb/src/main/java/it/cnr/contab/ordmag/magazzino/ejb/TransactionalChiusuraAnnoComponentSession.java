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
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.RemoteIterator;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class TransactionalChiusuraAnnoComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements ChiusuraAnnoComponentSession {


    @Override
    public List<ValoriLottoPerAnno> getValoriLottoPerAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo) throws RemoteException, ComponentException, PersistencyException {
        try {
            return ( List<ValoriLottoPerAnno>)invoke("getValoriLotto",new Object[] {
                    userContext, esercizio,dataFinePeriodo});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public ChiusuraAnnoBulk calcolaRimanenzeAnno(UserContext userContext, Integer esercizio, Date dataFinePeriodo,String statoChiusura) throws RemoteException, ComponentException, PersistencyException {
        try {
            return ( ChiusuraAnnoBulk)invoke("calcolaRimanenzeAnno",new Object[] {
                    userContext, esercizio,dataFinePeriodo,statoChiusura});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public ChiusuraAnnoBulk verificaChiusuraAnno(UserContext userContext, Integer esercizio, String tipoChiusura) throws ComponentException, PersistencyException, RemoteException {
        try {
            return ( ChiusuraAnnoBulk)invoke("verificaChiusuraAnno",new Object[] {
                    userContext, esercizio,tipoChiusura});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }



    @Override
    public ChiusuraAnnoBulk salvaChiusuraDefinitiva(UserContext userContext, Integer esercizio, String tipoChiusura,Date dataFinePeriodo) throws RemoteException, ComponentException,PersistencyException, ParseException {
        try {
            return ( ChiusuraAnnoBulk)invoke("salvaChiusuraDefinitiva",new Object[] {
                    userContext, esercizio,tipoChiusura,dataFinePeriodo});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public void annullaChiusuraDefinitiva(UserContext userContext, Integer esercizio, String tipoChiusura) throws RemoteException, ComponentException, PersistencyException, ParseException {
        try {
           invoke("annullaChiusuraDefinitiva",new Object[] {
                    userContext, esercizio,tipoChiusura});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public ChiusuraAnnoBulk eliminaDatiChiusuraInventarioECreaNuovaChiusuraRequestNew(UserContext userContext, Integer esercizio, String tipoChiusura,String statoChiusuraInventario, BigDecimal pgJob ) throws RemoteException, ComponentException, PersistencyException, ParseException {
        try {
           return ( ChiusuraAnnoBulk)invoke("eliminaDatiChiusuraInventarioECreaNuovaChiusuraRequestNew",new Object[] {
                    userContext, esercizio,tipoChiusura,statoChiusuraInventario,pgJob});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public OggettoBulk creaConBulkRequiresNew(UserContext usercontext, OggettoBulk oggettoBulk) throws RemoteException, ComponentException {
        try
        {
            return (OggettoBulk)invoke("creaConBulkRequiresNew", new Object[] {
                    usercontext, oggettoBulk
            });
        }
        catch(RemoteException remoteexception)
        {
            throw remoteexception;
        }
        catch(InvocationTargetException invocationtargetexception)
        {
            try
            {
                throw invocationtargetexception.getTargetException();
            }
            catch(ComponentException componentexception)
            {
                throw componentexception;
            }
            catch(Throwable throwable)
            {
                throw new RemoteException("Uncaugth exception", throwable);
            }
        }
    }

    @Override
    public OggettoBulk modificaConBulkRequiresNew(UserContext usercontext, OggettoBulk oggettoBulk) throws RemoteException, ComponentException {
        try
        {
            return (OggettoBulk)invoke("modificaConBulkRequiresNew", new Object[] {
                    usercontext, oggettoBulk
            });
        }
        catch(RemoteException remoteexception)
        {
            throw remoteexception;
        }
        catch(InvocationTargetException invocationtargetexception)
        {
            try
            {
                throw invocationtargetexception.getTargetException();
            }
            catch(ComponentException componentexception)
            {
                throw componentexception;
            }
            catch(Throwable throwable)
            {
                throw new RemoteException("Uncaugth exception", throwable);
            }
        }
    }

    @Override
    public Integer getNuovoProgressivoChiusura(UserContext userContext, ChiusuraAnnoBulk chiusuraAnno) throws ComponentException, BusyResourceException, PersistencyException, RemoteException {
        try
        {
            return (Integer) invoke("creaConBulkRequiresNew", new Object[] {
                    userContext, chiusuraAnno
            });
        }
        
        catch(InvocationTargetException invocationtargetexception)
        {
            try
            {
                throw invocationtargetexception.getTargetException();
            }
            catch(ComponentException componentexception)
            {
                throw componentexception;
            }
            catch(Throwable throwable)
            {
                throw new RemoteException("Uncaugth exception", throwable);
            }
        }
    }

    @Override
    public void inserisciDettagliChiusuraInventario(UserContext userContext, Integer esercizio, Integer pgChiusura) throws RemoteException, ComponentException {
        try {
            invoke("inserisciDettagliChiusuraInventario",new Object[] {
                    userContext, esercizio,pgChiusura});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public void inserisciImportiPerCatGruppoVoceEPInventario(UserContext userContext, Integer esercizio, Integer pgChiusura) throws RemoteException, ComponentException {
        try {
            invoke("inserisciImportiPerCatGruppoVoceEPInventario",new Object[] {
                    userContext, esercizio,pgChiusura});
        } catch(RemoteException e) {
            throw e;
        } catch(java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch(ComponentException ex) {
                throw ex;
            } catch(Throwable ex) {
                throw new RemoteException("Uncaugth exception",ex);
            }
        }
    }

    @Override
    public boolean isJobChiusuraInventarioComplete(UserContext userContext, Integer esercizio) throws RemoteException, ComponentException {
        try {
            return (boolean)invoke("isJobChiusuraInventarioComplete",new Object[] {
                    userContext,
                    esercizio });
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
    public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException, RemoteException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerStampa",new Object[] {
                    userContext,
                    oggettoBulk });
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
    public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException, RemoteException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk)invoke("stampaConBulk",new Object[] {
                    userContext,
                    oggettoBulk });
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


}
