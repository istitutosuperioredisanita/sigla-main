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

package it.cnr.contab.pdg00.comp;

import it.cnr.contab.compensi00.docs.bulk.EstrazioneCUDBulk;
import it.cnr.contab.config00.pdcep.bulk.TipoBilancioBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIHome;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SQLExceptionHandler;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BilancioAccrualComponent extends CRUDComponent implements ICRUDMgr, Cloneable, Serializable {
    private void callProcedureCE (UserContext userContext, AccrualBulk accrualBulk, String cdTipoBilancio)throws ComponentException{
        LoggableStatement cs = null;
        try{
            try {
                cs = new LoggableStatement(getConnection(userContext), "{call "
                        + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()
                        + "PRT_S_CE_RICLASSIFICATO_J(?,?,?,?,?,?) }", false, this
                        .getClass());

                cs.setString(1, "*"); // Ist/comm
                cs.setInt(2,accrualBulk.getEsercizio()); // Esercizio
                cs.setString(3, "*"); // CDS
                cs.setString(4, "*"); // UO
                cs.setString(5, "N"); // Dettaglio Conti
                cs.setString(6, cdTipoBilancio); // Tipo Bilancio

                cs.executeQuery();

            } catch (SQLException e) {
                throw SQLExceptionHandler.getInstance().handleSQLException(e);
            } finally {
                if (cs != null)
                    cs.close();
            }
        } catch (SQLException | PersistencyException ex) {
            throw handleException(ex);
        }
    }

    private void callProcedureSp (UserContext userContext, AccrualBulk accrualBulk, String cdTipoBilancio, String tipoAttPass)throws ComponentException{
        LoggableStatement cs = null;
        try{
            try {
                cs = new LoggableStatement(getConnection(userContext), "{call "
                        + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()
                        + "PRT_S_SP_RICLASSIFICATO(?,?,?,?,?,?,?) }", false, this
                        .getClass());

                cs.setString(1, tipoAttPass); // Attivo Passivo
                cs.setString(2,"*"); // Ist/comm
                cs.setInt(3, accrualBulk.getEsercizio()); // Esercizio
                cs.setString(4, "*"); // Cds
                cs.setString(5, "*"); // uo
                cs.setString(6, "N"); // dettagli Comti
                cs.setString(7, cdTipoBilancio); // Tipo Bilancio

                cs.executeQuery();

            } catch (SQLException e) {
                throw SQLExceptionHandler.getInstance().handleSQLException(e);
            } finally {
                if (cs != null)
                    cs.close();
            }
        } catch (SQLException | PersistencyException ex) {
            throw handleException(ex);
        }
    }
    private List<VpgBilRiclassificatoBulk> getList(UserContext userContext) throws ComponentException {
        try {
            VpgBilRiclassificatoHome vpgBilRiclassificatoHome = Optional.ofNullable(getHome(userContext, VpgBilRiclassificatoBulk.class))
                    .filter(VpgBilRiclassificatoHome.class::isInstance)
                    .map(VpgBilRiclassificatoHome.class::cast)
                    .orElseThrow(() -> new ComponentException("Home fi VpgBilRiclassificat non trovata!"));
            SQLBuilder sqlBuilder = vpgBilRiclassificatoHome.createSQLBuilder();
            sqlBuilder.addClause(FindClause.AND,"nomeTassAccrual",SQLBuilder.ISNOTNULL,null);
            sqlBuilder.addOrderBy("SEQUENZA ASC");
            return  vpgBilRiclassificatoHome.fetchAll(sqlBuilder);
        }catch (PersistencyException ex) {
            throw handleException(ex);
        }
    }

    public List bilancioRiclasPatr(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws ComponentException {
            callProcedureSp(userContext, accrualBulk, TipoBilancioBulk.ACCRUAL, tipoAttPass);
            return  getList(userContext);
    }

    public List bilancioRiclasCE(UserContext userContext, AccrualBulk accrualBulk) throws ComponentException {
            callProcedureCE(userContext, accrualBulk, TipoBilancioBulk.ACCRUAL);
            return  getList(userContext);
    }

    public List bilancioRiclasPatrSchedaAgg(UserContext userContext, AccrualBulk accrualBulk,String tipoAttPass) throws ComponentException {
            callProcedureSp(userContext, accrualBulk, TipoBilancioBulk.ACCRUAL_AGG, tipoAttPass);
            return  getList(userContext);
    }
}
