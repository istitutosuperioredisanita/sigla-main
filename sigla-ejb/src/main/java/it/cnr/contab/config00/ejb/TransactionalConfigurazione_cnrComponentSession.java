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

package it.cnr.contab.config00.ejb;

import it.cnr.contab.util.enumeration.TipoRapportoTesoreriaEnum;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;

public class TransactionalConfigurazione_cnrComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements Configurazione_cnrComponentSession {
    public it.cnr.contab.config00.bulk.Configurazione_cnrBulk getConfigurazione(it.cnr.jada.UserContext userContext, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (it.cnr.contab.config00.bulk.Configurazione_cnrBulk) invoke("getConfigurazione", new Object[]{
                    userContext,
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

    public java.sql.Timestamp getDt01(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.sql.Timestamp) invoke("getDt01", new Object[]{
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

    public java.sql.Timestamp getDt01(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.sql.Timestamp) invoke("getDt01", new Object[]{
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

    public java.sql.Timestamp getDt02(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.sql.Timestamp) invoke("getDt02", new Object[]{
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

    public java.sql.Timestamp getDt02(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.sql.Timestamp) invoke("getDt02", new Object[]{
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

    public java.math.BigDecimal getIm01(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.math.BigDecimal) invoke("getIm01", new Object[]{
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

    public java.math.BigDecimal getIm01(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.math.BigDecimal) invoke("getIm01", new Object[]{
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

    public java.math.BigDecimal getIm02(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.math.BigDecimal) invoke("getIm02", new Object[]{
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

    public java.math.BigDecimal getIm02(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.math.BigDecimal) invoke("getIm02", new Object[]{
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

    public java.lang.String getVal01(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal01", new Object[]{
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

    public java.lang.String getVal01(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal01", new Object[]{
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

    public java.lang.String getVal02(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal02", new Object[]{
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

    public java.lang.String getVal02(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal02", new Object[]{
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

    public java.lang.String getVal03(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal03", new Object[]{
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

    public java.lang.String getVal03(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal03", new Object[]{
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

    public java.lang.String getVal04(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2, java.lang.String param3, java.lang.String param4) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal04", new Object[]{
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

    public java.lang.String getVal04(it.cnr.jada.UserContext param0, java.lang.String param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getVal04", new Object[]{
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

    public java.lang.Boolean isAttivoOrdini(it.cnr.jada.UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.Boolean) invoke("isAttivoOrdini", new Object[]{
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

    @Override
    public Boolean hasGestioneImportiFlussiFinanziari(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("hasGestioneImportiFlussiFinanziari", new Object[]{
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

    public java.lang.Boolean propostaFatturaDaOrdini(it.cnr.jada.UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.Boolean) invoke("propostaFatturaDaOrdini", new Object[]{
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

    public java.lang.String getCdrPersonale(it.cnr.jada.UserContext param0, java.lang.Integer param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getCdrPersonale", new Object[]{
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

    public java.lang.String getUoRagioneria(it.cnr.jada.UserContext param0, java.lang.Integer param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getUoRagioneria", new Object[]{
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

    public java.lang.String getUoDistintaTuttaSac(it.cnr.jada.UserContext param0, java.lang.Integer param1) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.String) invoke("getUoDistintaTuttaSac", new Object[]{
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

    public java.lang.Boolean isUOSpecialeDistintaTuttaSAC(it.cnr.jada.UserContext param0, java.lang.Integer param1, java.lang.String param2) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.Boolean) invoke("isUOSpecialeDistintaTuttaSAC", new Object[]{
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
    public String getCdsSAC(UserContext userContext, Integer esercizio)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (String) invoke("getCdsSAC", new Object[]{
                    userContext,
                    esercizio});
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
    public java.lang.Boolean isEconomicaPatrimonialeAttivaImputazioneManuale(it.cnr.jada.UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.Boolean) invoke("isEconomicaPatrimonialeAttivaImputazioneManuale", new Object[]{
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

    @Override
    public Boolean getGestioneImpegnoChiusuraForzataCompetenza(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("getGestioneImpegnoChiusuraForzataCompetenza", new Object[]{
                    userContext});
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
    public Boolean getGestioneImpegnoChiusuraForzataResiduo(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("getGestioneImpegnoChiusuraForzataResiduo", new Object[]{
                    userContext});
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
    public Boolean isAttivaEconomica(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isAttivaEconomica", new Object[]{
                    userContext});
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
    public Boolean isAttivaEconomicaPura(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isAttivaEconomicaPura", new Object[]{
                    userContext});
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
    public Boolean isAttivaEconomicaParallela(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isAttivaEconomicaParallela", new Object[]{
                    userContext});
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
    public Boolean isBloccoScrittureProposte(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isBloccoScrittureProposte", new Object[]{
                    userContext});
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
    public Boolean isAssPrgAnagraficoAttiva(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isAssPrgAnagraficoAttiva", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }
    
    @Override
    public Boolean isImpegnoPluriennaleAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isImpegnoPluriennaleAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isAccertamentoPluriennaleAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isAccertamentoPluriennaleAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isVariazioneAutomaticaSpesa(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isVariazioneAutomaticaSpesa", new Object[]{
                    userContext});
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
    public Boolean isAttachRestContrStoredFromSigla(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isAttachRestContrStoredFromSigla", new Object[]{
                    userContext});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }
    public Integer getCdTerzoDiversiStipendi(UserContext userContext)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (Integer) invoke("getCdTerzoDiversiStipendi", new Object[]{
                    userContext});
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
    public Integer getCdTerzoDiversiCollaboratori(UserContext userContext)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (Integer) invoke("getCdTerzoDiversiCollaboratori", new Object[]{
                    userContext});
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
    public Timestamp getDataFineValiditaCaricoFamiliare(UserContext userContext, String tiPersona)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (Timestamp) invoke("getDataFineValiditaCaricoFamiliare", new Object[]{
                    userContext, tiPersona});
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
    public Boolean isGestioneEtichettaInventarioBeneAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isGestioneEtichettaInventarioBeneAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isGestioneBeneDismessoInventarioAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isGestioneBeneDismessoInventarioAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isPagamentoEsteroISSAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isPagamentoEsteroISSAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public String getContoCorrenteEnte(UserContext userContext, Integer esercizio)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (String) invoke("getContoCorrenteEnte", new Object[]{
                    userContext,
                    esercizio});
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

    public String getTipoStanziamentoLiquidazioneIva(UserContext userContext)  throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (String) invoke("getTipoStanziamentoLiquidazioneIva", new Object[]{
                    userContext});
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
    public TipoRapportoTesoreriaEnum getTipoRapportoTesoreria(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (TipoRapportoTesoreriaEnum) invoke("getTipoRapportoTesoreria", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isGestioneStatoInizialeSospesiAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isGestioneStatoInizialeSospesiAttivo", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public Boolean isAttivoInventariaDocumenti(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (Boolean) invoke("isAttivoInventariaDocumenti", new Object[]{
                    param0});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }


    public RemoteIterator cerca(UserContext usercontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (RemoteIterator)invoke("cerca", new Object[] {
                    usercontext, compoundfindclause, oggettobulk
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

    public RemoteIterator cerca(UserContext usercontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (RemoteIterator)invoke("cerca", new Object[] {
                    usercontext, compoundfindclause, oggettobulk, oggettobulk1, s
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

    public RemoteIterator cerca(UserContext usercontext, CompoundFindClause compoundfindclause, Class class1, OggettoBulk oggettobulk, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (RemoteIterator)invoke("cerca", new Object[] {
                    usercontext, compoundfindclause, class1, oggettobulk, s
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

    public OggettoBulk creaConBulk(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("creaConBulk", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk creaConBulk(UserContext usercontext, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("creaConBulk", new Object[] {
                    usercontext, oggettobulk, oggettobulk1, s
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

    public OggettoBulk[] creaConBulk(UserContext usercontext, OggettoBulk aoggettobulk[])
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk[])invoke("creaConBulk", new Object[] {
                    usercontext, aoggettobulk
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

    public void eliminaConBulk(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            invoke("eliminaConBulk", new Object[] {
                    usercontext, oggettobulk
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

    public void eliminaConBulk(UserContext usercontext, OggettoBulk oggettobulk, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            invoke("eliminaConBulk", new Object[] {
                    usercontext, oggettobulk, s
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

    public void eliminaConBulk(UserContext usercontext, OggettoBulk aoggettobulk[])
            throws RemoteException, ComponentException
    {
        try
        {
            invoke("eliminaConBulk", new Object[] {
                    usercontext, aoggettobulk
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

    public void eliminaConBulk(UserContext usercontext, OggettoBulk aoggettobulk[], OggettoBulk oggettobulk, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            invoke("eliminaConBulk", new Object[] {
                    usercontext, aoggettobulk, oggettobulk, s
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

    public OggettoBulk inizializzaBulkPerInserimento(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerInserimento", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk inizializzaBulkPerInserimento(UserContext usercontext, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerInserimento", new Object[] {
                    usercontext, oggettobulk, oggettobulk1, s
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

    public OggettoBulk inizializzaBulkPerModifica(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerModifica", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk inizializzaBulkPerModifica(UserContext usercontext, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerModifica", new Object[] {
                    usercontext, oggettobulk, oggettobulk1, s
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

    public OggettoBulk[] inizializzaBulkPerModifica(UserContext usercontext, OggettoBulk aoggettobulk[])
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk[])invoke("inizializzaBulkPerModifica", new Object[] {
                    usercontext, aoggettobulk
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

    public OggettoBulk inizializzaBulkPerRicerca(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerRicerca", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk inizializzaBulkPerRicercaLibera(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("inizializzaBulkPerRicercaLibera", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk modificaConBulk(UserContext usercontext, OggettoBulk oggettobulk)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("modificaConBulk", new Object[] {
                    usercontext, oggettobulk
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

    public OggettoBulk modificaConBulk(UserContext usercontext, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s)
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk)invoke("modificaConBulk", new Object[] {
                    usercontext, oggettobulk, oggettobulk1, s
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

    public OggettoBulk[] modificaConBulk(UserContext usercontext, OggettoBulk aoggettobulk[])
            throws RemoteException, ComponentException
    {
        try
        {
            return (OggettoBulk[])invoke("modificaConBulk", new Object[] {
                    usercontext, aoggettobulk
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

    public java.lang.Boolean isAttivoRegitrazioneFattAnnoPrec(it.cnr.jada.UserContext param0) throws RemoteException, it.cnr.jada.comp.ComponentException {
        try {
            return (java.lang.Boolean) invoke("isAttivoRegitrazioneFattAnnoPrec", new Object[]{
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

    @Override
    public Boolean isAttivoGestModPagDipendenti(UserContext param0) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isAttivoGestModPagDipendenti", new Object[]{
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

    @Override
    public Boolean isCheckImpIntrastatFattAttiva(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isCheckImpIntrastatFattAttiva", new Object[]{
                    userContext});
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
    public Boolean isCheckImpIntrastatFattPassiva(UserContext userContext) throws ComponentException, RemoteException {
        try {
            return (java.lang.Boolean) invoke("isCheckImpIntrastatFattPassiva", new Object[]{
                    userContext});
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
