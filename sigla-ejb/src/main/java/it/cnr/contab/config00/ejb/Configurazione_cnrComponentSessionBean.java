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

import it.cnr.contab.config00.comp.Configurazione_cnrComponent;
import it.cnr.contab.util.enumeration.TipoRapportoTesoreriaEnum;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.AdminUserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.UUID;

@Stateless(name = "CNRCONFIG00_EJB_Configurazione_cnrComponentSession")
public class Configurazione_cnrComponentSessionBean extends it.cnr.jada.ejb.CRUDDetailComponentSessionBean implements Configurazione_cnrComponentSession {
    private transient final static Logger logger = LoggerFactory.getLogger(Configurazione_cnrComponentSessionBean.class);

    public static Configurazione_cnrComponentSessionBean newInstance() throws EJBException {
        return new Configurazione_cnrComponentSessionBean();
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    @Remove
    public void ejbRemove() throws EJBException {
        try {
            shutdowHook();
        } catch (ComponentException e) {
            logger.error("ERROR while shutdow hook", e);
        }
        componentObj.release();
    }

    @PostConstruct
    public void ejbCreate() {
        componentObj = new Configurazione_cnrComponent();
    }

    public void shutdowHook() throws ComponentException, EJBException{
        UserContext param0 = new AdminUserContext(UUID.randomUUID().toString());
        pre_component_invocation(param0, componentObj);
        try {
            ((Configurazione_cnrComponent)componentObj).shutdowHook(param0);
            component_invocation_succes(param0, componentObj);
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public it.cnr.contab.config00.bulk.Configurazione_cnrBulk getConfigurazione(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            it.cnr.contab.config00.bulk.Configurazione_cnrBulk result = ((Configurazione_cnrComponent)componentObj).getConfigurazione(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.sql.Timestamp getDt01(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.sql.Timestamp result = ((Configurazione_cnrComponent)componentObj).getDt01(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.sql.Timestamp getDt01(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.sql.Timestamp result = ((Configurazione_cnrComponent)componentObj).getDt01(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.sql.Timestamp getDt02(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.sql.Timestamp result = ((Configurazione_cnrComponent)componentObj).getDt02(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.sql.Timestamp getDt02(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.sql.Timestamp result = ((Configurazione_cnrComponent)componentObj).getDt02(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.math.BigDecimal getIm01(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.math.BigDecimal result = ((Configurazione_cnrComponent)componentObj).getIm01(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.math.BigDecimal getIm01(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.math.BigDecimal result = ((Configurazione_cnrComponent)componentObj).getIm01(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.math.BigDecimal getIm02(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.math.BigDecimal result = ((Configurazione_cnrComponent)componentObj).getIm02(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public java.math.BigDecimal getIm02(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            java.math.BigDecimal result = ((Configurazione_cnrComponent)componentObj).getIm02(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal01(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal01(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal01(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal01(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal02(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal02(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal02(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal02(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal03(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal03(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal03(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal03(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal04(UserContext param0, Integer param1, String param2, String param3, String param4) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal04(param0, param1, param2, param3, param4);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getVal04(UserContext param0, String param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getVal04(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public Boolean isAttivoOrdini(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivoOrdini(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public Boolean hasGestioneImportiFlussiFinanziari(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).hasGestioneImportiFlussiFinanziari(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public Boolean propostaFatturaDaOrdini(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).propostaFatturaDaOrdini(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getCdrPersonale(UserContext param0, Integer param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getCdrPersonale(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getUoRagioneria(UserContext param0, Integer param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getUoRagioneria(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public String getUoDistintaTuttaSac(UserContext param0, Integer param1) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getUoDistintaTuttaSac(param0, param1);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    public Boolean isUOSpecialeDistintaTuttaSAC(UserContext param0, Integer param1, String param2) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isUOSpecialeDistintaTuttaSAC(param0,param1,param2);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }
    public String getCdsSAC(UserContext userContext, Integer esercizio) throws ComponentException, EJBException {
        pre_component_invocation(userContext, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getCdsSAC(userContext, esercizio);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    public Boolean isEconomicaPatrimonialeAttivaImputazioneManuale(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isEconomicaPatrimonialeAttivaImputazioneManuale(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean getGestioneImpegnoChiusuraForzataCompetenza(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).getGestioneImpegnoChiusuraForzataCompetenza(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public Boolean getGestioneImpegnoChiusuraForzataResiduo(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).getGestioneImpegnoChiusuraForzataResiduo(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public Boolean isAttivaEconomica(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivaEconomica(userContext);
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
    public Boolean isAttivaEconomicaPura(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivaEconomicaPura(userContext);
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
    public Boolean isAttivaEconomicaParallela(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivaEconomicaParallela(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public Boolean isBloccoScrittureProposte(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isBloccoScrittureProposte(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }
    @Override
    public Boolean isAssPrgAnagraficoAttiva(UserContext param0) throws ComponentException, RemoteException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAssPrgAnagraficoAttiva(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isImpegnoPluriennaleAttivo(UserContext param0) throws ComponentException, RemoteException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isImpegnoPluriennaleAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isAccertamentoPluriennaleAttivo(UserContext param0) throws ComponentException, RemoteException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAccertamentoPluriennaleAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isAttachRestContrStoredFromSigla(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttachRestContrStoredFromSigla(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public Boolean isVariazioneAutomaticaSpesa(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isVariazioneAutomaticaSpesa(userContext);
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

    public Integer getCdTerzoDiversiStipendi(UserContext userContext) throws it.cnr.jada.comp.ComponentException, javax.ejb.EJBException {
        pre_component_invocation(userContext, componentObj);
        try {
            Integer result = ((Configurazione_cnrComponent)componentObj).getCdTerzoDiversiStipendi(userContext);
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

    public Integer getCdTerzoDiversiCollaboratori(UserContext userContext) throws it.cnr.jada.comp.ComponentException, javax.ejb.EJBException {
        pre_component_invocation(userContext, componentObj);
        try {
            Integer result = ((Configurazione_cnrComponent)componentObj).getCdTerzoDiversiCollaboratori(userContext);
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

    public String getContoCorrenteEnte(UserContext userContext, Integer esercizio) throws it.cnr.jada.comp.ComponentException, javax.ejb.EJBException {
        pre_component_invocation(userContext, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getContoCorrenteEnte(userContext, esercizio);
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
    public Timestamp getDataFineValiditaCaricoFamiliare(UserContext userContext, String tiPersona) throws it.cnr.jada.comp.ComponentException, javax.ejb.EJBException {
        pre_component_invocation(userContext, componentObj);
        try {
            Timestamp result = ((Configurazione_cnrComponent)componentObj).getDataFineValiditaCaricoFamiliare(userContext, tiPersona);
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
    public Boolean isGestioneEtichettaInventarioBeneAttivo(UserContext param0) throws ComponentException, RemoteException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isGestioneEtichettaInventarioBeneAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public String getTipoStanziamentoLiquidazioneIva(UserContext param0) throws ComponentException, RemoteException {
        pre_component_invocation(param0, componentObj);
        try {
            String result = ((Configurazione_cnrComponent)componentObj).getTipoStanziamentoLiquidazioneIva(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }
    @Override
    public Boolean isGestioneBeneDismessoInventarioAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isGestioneBeneDismessoInventarioAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isPagamentoEsteroISSAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isPagamentoEsteroISSAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public TipoRapportoTesoreriaEnum getTipoRapportoTesoreria(UserContext param0) throws ComponentException, RemoteException {
        try {
            TipoRapportoTesoreriaEnum result = ((Configurazione_cnrComponent)componentObj).getTipoRapportoTesoreria(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isGestioneStatoInizialeSospesiAttivo(UserContext param0) throws ComponentException, RemoteException {
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isGestioneStatoInizialeSospesiAttivo(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isAttivoInventariaDocumenti(UserContext param0) throws ComponentException, RemoteException {
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivoInventariaDocumenti(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }
    public Boolean isAttivoRegitrazioneFattAnnoPrec(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivoRegitrazioneFattAnnoPrec(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }
    public Boolean isAttivoGestModPagDipendenti(UserContext param0) throws ComponentException, EJBException {
        pre_component_invocation(param0, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isAttivoGestModPagDipendenti(param0);
            component_invocation_succes(param0, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(param0, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(param0, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(param0, componentObj, e);
        }
    }

    @Override
    public Boolean isCheckImpIntrastatFattAttiva(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isCheckImpIntrastatFattAttiva(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }

    @Override
    public Boolean isCheckImpIntrastatFattPassiva(UserContext userContext) throws ComponentException, RemoteException {
        pre_component_invocation(userContext, componentObj);
        try {
            Boolean result = ((Configurazione_cnrComponent)componentObj).isCheckImpIntrastatFattPassiva(userContext);
            component_invocation_succes(userContext, componentObj);
            return result;
        } catch (it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(userContext, componentObj);
            throw e;
        } catch (ComponentException e) {
            component_invocation_failure(userContext, componentObj);
            throw e;
        } catch (RuntimeException e) {
            throw uncaughtRuntimeException(userContext, componentObj, e);
        } catch (Error e) {
            throw uncaughtError(userContext, componentObj, e);
        }
    }


}