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

package it.cnr.contab.pdg00.action;

import it.cnr.contab.doccont00.consultazioni.bp.ConsControlliPCCBP;
import it.cnr.contab.doccont00.consultazioni.bulk.ControlliPCCParams;
import it.cnr.contab.doccont00.consultazioni.bulk.VControlliPCCBulk;
import it.cnr.contab.pdg00.bp.ElaboraStralcioMensileStipendiBP;
import it.cnr.contab.pdg00.cdip.bulk.V_cnr_estrazione_coriBulk;
import it.cnr.jada.action.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;

public class CaricFlStipAction {


    //TODO metodo che permette di impostare la logica al click sul pulsante excel (da adattare)
//    public Forward doConfirmEstraiCSV(ActionContext context) throws BusinessProcessException {
//        ConsControlliPCCBP bp = (ConsControlliPCCBP) context.getBusinessProcess();
//        HookForward caller = (HookForward) context.getCaller();
//        ControlliPCCParams controlliPCCParams = (ControlliPCCParams) caller.getParameter("model");
//        List<VControlliPCCBulk> vControlliPCCBulks = bp.getSelectedElements(context);
//        bp.elaboraCSV(controlliPCCParams, vControlliPCCBulks);
//        bp.clearSelection(context);
//        return doVisualizzaAllegatiCSV(context);
//    }
//
//    public Forward doVisualizzaAllegatiCSV(ActionContext context) throws BusinessProcessException {
//        final BusinessProcess allegatiPCCBP = context.createBusinessProcess("AllegatiPCCBP", new Object[]{"M"});
//        return context.addBusinessProcess(allegatiPCCBP);
//    }

}