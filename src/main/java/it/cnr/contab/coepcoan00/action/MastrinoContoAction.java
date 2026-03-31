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

import it.cnr.contab.coepcoan00.bp.MastrinoContoBP;
import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazioneMastrinoContoBP;
import it.cnr.contab.coepcoan00.filter.bulk.FiltroRicercaMastrinoContoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkAction;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

public class MastrinoContoAction extends BulkAction {
    public Forward doMastrinoConto(ActionContext context) throws BusinessProcessException, FillException {
        final MastrinoContoBP bulkBP = (MastrinoContoBP) context.getBusinessProcess();
        bulkBP.fillModel(context);
        final Optional<ContoBulk> contoBulk = Optional.ofNullable(bulkBP.getModel())
                .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                .map(FiltroRicercaMastrinoContoBulk.class::cast)
                .flatMap(filtroRicercaMastrinoContoBulk -> Optional.ofNullable(filtroRicercaMastrinoContoBulk.getConto()));
        final Optional<Boolean> filtraUnitaOrganizzativa = Optional.ofNullable(bulkBP.getModel())
                .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                .map(FiltroRicercaMastrinoContoBulk.class::cast)
                .flatMap(filtroRicercaMastrinoContoBulk -> Optional.ofNullable(filtroRicercaMastrinoContoBulk.getFiltraUnitaOrganizzativa()));
        final Optional<Unita_organizzativaBulk> unitaOrganizzativaBulk = Optional.ofNullable(bulkBP.getModel())
                .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                .map(FiltroRicercaMastrinoContoBulk.class::cast)
                .flatMap(filtroRicercaMastrinoContoBulk -> Optional.ofNullable(filtroRicercaMastrinoContoBulk.getUnitaOrganizzativa()));
        final Optional<Timestamp> fromDataMovimento = Optional.ofNullable(bulkBP.getModel())
                .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                .map(FiltroRicercaMastrinoContoBulk.class::cast)
                .flatMap(filtroRicercaMastrinoContoBulk -> Optional.ofNullable(filtroRicercaMastrinoContoBulk.getFromDataMovimento()));
        final Optional<Timestamp> toDataMovimento = Optional.ofNullable(bulkBP.getModel())
                .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                .map(FiltroRicercaMastrinoContoBulk.class::cast)
                .flatMap(filtroRicercaMastrinoContoBulk -> Optional.ofNullable(filtroRicercaMastrinoContoBulk.getToDataMovimento()));

        ConsultazioneMastrinoContoBP consBP = (ConsultazioneMastrinoContoBP) context.createBusinessProcess(
                "ConsultazioneMastrinoContoBP",
                 new Object[]{Arrays.asList(contoBulk.get()),
                            filtraUnitaOrganizzativa.orElse(Boolean.FALSE),
                            unitaOrganizzativaBulk.orElse(null),
                            fromDataMovimento.orElse(null),
                            toDataMovimento.orElse(null),
                            Boolean.FALSE}
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

    public Forward doOnFiltraUnitaOrganizzativa(ActionContext context) {
        try {
            final MastrinoContoBP bulkBP = (MastrinoContoBP) context.getBusinessProcess();
            bulkBP.fillModel(context);
            Optional.ofNullable(bulkBP.getModel())
                    .filter(FiltroRicercaMastrinoContoBulk.class::isInstance)
                    .map(FiltroRicercaMastrinoContoBulk.class::cast)
                    .ifPresent(model -> model.setUnitaOrganizzativa(null));
            return context.findDefaultForward();
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }
}
