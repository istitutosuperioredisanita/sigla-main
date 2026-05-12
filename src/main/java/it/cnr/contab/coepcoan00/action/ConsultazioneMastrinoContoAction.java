/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.coepcoan00.action;

import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazioneMastrinoContoBP;
import it.cnr.contab.coepcoan00.core.bulk.MastrinoContoBulk;
import it.cnr.contab.util00.action.SelezionatoreSearchProviderAction;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class ConsultazioneMastrinoContoAction extends SelezionatoreSearchProviderAction {
    public Forward doDettagli(ActionContext context) throws BusinessProcessException {
        ConsultazioneMastrinoContoBP bulkBP = (ConsultazioneMastrinoContoBP)context.getBusinessProcess();
        bulkBP.setSelection(context);
        List<MastrinoContoBulk> mastriniSelected = bulkBP.getSelectedElements(context);
        if (!mastriniSelected.isEmpty()) {
            ConsultazioneMastrinoContoBP.MapFilter mapFilter = new ConsultazioneMastrinoContoBP.MapFilter();
            mapFilter.setFiltraUnitaOrganizzativa(bulkBP.getMapFilter().getFiltraUnitaOrganizzativa());
            mapFilter.setUnitaOrganizzativaBulk(bulkBP.getMapFilter().getUnitaOrganizzativaBulk());
            mapFilter.setFromDataMovimento(bulkBP.getMapFilter().getFromDataMovimento());
            mapFilter.setToDataMovimento(bulkBP.getMapFilter().getToDataMovimento());
            mapFilter.setMastriniSelected(mastriniSelected);
            mapFilter.setDetail(Boolean.TRUE);

            ConsultazioneMastrinoContoBP consBP = (ConsultazioneMastrinoContoBP) context.createBusinessProcess(
                    "ConsultazioneMastrinoContoBP",
                    new Object[]{mapFilter}
            );
            RemoteIterator ri = consBP.openIterator(context);
            try {
                if (!Optional.ofNullable(ri).filter(remoteIterator -> {
                    try {
                        return remoteIterator.countElements() > 0;
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }).isPresent()) {
                    it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(context, ri);
                    bulkBP.setMessage("La ricerca non ha fornito alcun risultato.");
                    return context.findDefaultForward();
                }
            } catch (Exception _ex) {
                handleException(context, _ex);
            }
            context.addBusinessProcess(consBP);
            return context.findDefaultForward();
        }
        return context.findDefaultForward();
    }
}
