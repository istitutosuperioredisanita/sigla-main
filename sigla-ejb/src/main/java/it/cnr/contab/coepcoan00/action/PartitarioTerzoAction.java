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

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazionePartitarioBP;
import it.cnr.contab.coepcoan00.filter.bulk.FiltroRicercaPartitarioBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.FormBP;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Optional;

public class PartitarioTerzoAction extends BulkAction {

    public Forward doBlankSearchTerzo(ActionContext context, FiltroRicercaPartitarioBulk filtroRicercaPartitarioBulk) {
        final TerzoBulk terzoBulk = new TerzoBulk();
        terzoBulk.setAnagrafico(new AnagraficoBulk());
        filtroRicercaPartitarioBulk.setTerzo(terzoBulk);
        return context.findDefaultForward();
    }

    public Forward doPartitario(ActionContext context) throws BusinessProcessException {
        final BulkBP bulkBP = (BulkBP) context.getBusinessProcess();
        try {
            bulkBP.fillModel(context);

            final Optional<TerzoBulk> terzoBulk = Optional.ofNullable(bulkBP.getModel())
                    .filter(FiltroRicercaPartitarioBulk.class::isInstance)
                    .map(FiltroRicercaPartitarioBulk.class::cast)
                    .flatMap(filtroRicercaPartitarioBulk -> Optional.ofNullable(filtroRicercaPartitarioBulk.getTerzo()));
            final Boolean isDettaglioTributi = Optional.ofNullable(bulkBP.getModel())
                    .filter(FiltroRicercaPartitarioBulk.class::isInstance)
                    .map(FiltroRicercaPartitarioBulk.class::cast)
                    .flatMap(filtroRicercaPartitarioBulk -> Optional.ofNullable(filtroRicercaPartitarioBulk.getDettaglioTributi()))
                    .orElse(Boolean.FALSE);
            final String partite = Optional.ofNullable(bulkBP.getModel())
                    .filter(FiltroRicercaPartitarioBulk.class::isInstance)
                    .map(FiltroRicercaPartitarioBulk.class::cast)
                    .flatMap(filtroRicercaPartitarioBulk -> Optional.ofNullable(filtroRicercaPartitarioBulk.getPartite()))
                    .orElse(FiltroRicercaPartitarioBulk.Partite.T.name());
            final Timestamp toDataMovimento = Optional.ofNullable(bulkBP.getModel())
                    .filter(FiltroRicercaPartitarioBulk.class::isInstance)
                    .map(FiltroRicercaPartitarioBulk.class::cast)
                    .flatMap(filtroRicercaPartitarioBulk -> Optional.ofNullable(filtroRicercaPartitarioBulk.getToDataMovimento()))
                    .orElse(null);
            if (terzoBulk.isPresent()) {
                ConsultazionePartitarioBP consBP = (ConsultazionePartitarioBP) context.createBusinessProcess(
                        "ConsultazionePartitarioBP",
                        new Object[]{
                                terzoBulk.get(),
                                isDettaglioTributi,
                                partite,
                                toDataMovimento,
                                "partitario",
                                true
                        }
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
            setMessage(context, FormBP.WARNING_MESSAGE, "Valorizzare il Tezo!");
            return context.findDefaultForward();
        } catch (FillException e) {
            return handleException(context, e);
        }
    }
}
