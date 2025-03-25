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

package it.cnr.contab.inventario00.bp;

import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;

public class CRUDChiusuraInventarioBP extends ParametricPrintBP {


    public CRUDChiusuraInventarioBP() {
    }

    public CRUDChiusuraInventarioBP(String function) {
        super(function);
    }

    /*protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        try {
            super.init(config, context);

            setBulkClassName(config.getInitParameter("bulkClassName"));
            setComponentSessioneName(config.getInitParameter("componentSessionName"));

            OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();
            UserContext userContext = context.getUserContext();
            setModel(context, model);

        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Non trovata la classe bulk");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }*/
    @Override
    protected void initialize(ActionContext context) throws BusinessProcessException {
        super.initialize(context);
    }

}

