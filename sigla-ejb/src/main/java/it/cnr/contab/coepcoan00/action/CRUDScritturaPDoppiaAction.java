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

package it.cnr.contab.coepcoan00.action;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.coepcoan00.bp.CRUDScritturaPDoppiaBP;
import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazionePartitarioBP;
import it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.PartitarioBulk;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.FormField;
import it.cnr.jada.util.action.OptionBP;

import javax.ejb.RemoveException;
import javax.swing.text.html.Option;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Action che gestisce le attività di CRUD per una Scrittura in Partita Doppia
 */

public class CRUDScritturaPDoppiaAction extends it.cnr.jada.util.action.CRUDAction {

    public CRUDScritturaPDoppiaAction() {
        super();
    }

    public Forward doOnCompetenzaChangeDa(ActionContext actionContext) throws BusinessProcessException{
        CRUDScritturaPDoppiaBP scritturaPDoppiaBP = getBusinessProcess(actionContext);
        Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk = getModel(actionContext);
        try {
            final Timestamp dtDaCompetenzaCoge = scritturaPartitaDoppiaBulk.getDt_da_competenza_coge();
            fillModel(actionContext);
            if (Optional.ofNullable(scritturaPartitaDoppiaBulk.getDt_da_competenza_coge())
                            .filter(timestamp -> timestamp.after(scritturaPartitaDoppiaBulk.getDt_contabilizzazione()))
                            .isPresent()) {
                scritturaPartitaDoppiaBulk.setDt_da_competenza_coge(dtDaCompetenzaCoge);
                throw new ApplicationException("La Data di competenza, deve essere inferiore o uguale alla data scrittura!");
            }
        } catch (FillException | ApplicationException e) {
            return handleException(actionContext, e);
        }
        return actionContext.findDefaultForward();
    }
    public Forward doOnCompetenzaChangeA(ActionContext actionContext) throws BusinessProcessException{
        CRUDScritturaPDoppiaBP scritturaPDoppiaBP = getBusinessProcess(actionContext);
        Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk = getModel(actionContext);
        try {
            final Timestamp dtACompetenzaCoge = scritturaPartitaDoppiaBulk.getDt_a_competenza_coge();
            fillModel(actionContext);
            if (Optional.ofNullable(scritturaPartitaDoppiaBulk.getDt_a_competenza_coge())
                    .filter(timestamp -> timestamp.before(scritturaPartitaDoppiaBulk.getDt_da_competenza_coge()))
                    .isPresent()) {
                scritturaPartitaDoppiaBulk.setDt_a_competenza_coge(dtACompetenzaCoge);
                throw new ApplicationException("La Data a competenza, deve essere superiore o uguale alla data da competenza!");
            }
        } catch (FillException | ApplicationException e) {
            return handleException(actionContext, e);
        }
        return actionContext.findDefaultForward();
    }

    public Forward doOnChangeTipologia(ActionContext actionContext) {
        final CRUDScritturaPDoppiaBP businessProcess = getBusinessProcess(actionContext);
        try {
            fillModel(actionContext);
            if (businessProcess.getTab("tab").equalsIgnoreCase("tabDare")) {
                Optional.ofNullable(businessProcess.getMovimentiDare().getModel())
                        .filter(Movimento_cogeBulk.class::isInstance)
                        .map(Movimento_cogeBulk.class::cast)
                        .ifPresent(movimentoCogeBulk -> {
                            movimentoCogeBulk.setConto(null);
                        });
            } else if (businessProcess.getTab("tab").equalsIgnoreCase("tabAvere")) {
                Optional.ofNullable(businessProcess.getMovimentiAvere().getModel())
                        .filter(Movimento_cogeBulk.class::isInstance)
                        .map(Movimento_cogeBulk.class::cast)
                        .ifPresent(movimentoCogeBulk -> {
                            movimentoCogeBulk.setConto(null);
                        });
            }
        } catch (FillException _ex) {
            return handleException(actionContext, _ex);
        }
        return actionContext.findDefaultForward();
    }

    private Optional<Movimento_cogeBulk> getModelDetail(ActionContext actionContext) {
        final CRUDScritturaPDoppiaBP businessProcess = getBusinessProcess(actionContext);
        if (businessProcess.getTab("tab").equalsIgnoreCase("tabDare")) {
            return Optional.ofNullable(businessProcess.getMovimentiDare().getModel())
                    .filter(Movimento_cogeBulk.class::isInstance)
                    .map(Movimento_cogeBulk.class::cast);
        } else {
            return Optional.ofNullable(businessProcess.getMovimentiAvere().getModel())
                    .filter(Movimento_cogeBulk.class::isInstance)
                    .map(Movimento_cogeBulk.class::cast);
        }
    }
    public CRUDScritturaPDoppiaBP getBusinessProcess(ActionContext actionContext){
        return Optional.ofNullable(actionContext.getBusinessProcess())
                .filter(CRUDScritturaPDoppiaBP.class::isInstance)
                .map(CRUDScritturaPDoppiaBP.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("BP not Found!"));
    }

    public Scrittura_partita_doppiaBulk getModel(ActionContext actionContext) {
        return Optional.ofNullable(getBusinessProcess(actionContext))
                .flatMap(crudScritturaPDoppiaBP -> Optional.ofNullable(crudScritturaPDoppiaBP.getModel()))
                .filter(Scrittura_partita_doppiaBulk.class::isInstance)
                .map(Scrittura_partita_doppiaBulk.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("Model not Found!"));

    }

    public Forward doSearchPartitario(ActionContext context) throws BusinessProcessException{
        final CRUDScritturaPDoppiaBP crudScritturaPDoppiaBP = getBusinessProcess(context);
        try {
            crudScritturaPDoppiaBP.fillModel(context);
            final Optional<TerzoBulk> terzoBulk = getModelDetail(context)
                    .flatMap(movimentoCogeBulk -> Optional.ofNullable(movimentoCogeBulk.getTerzo()).filter(terzoBulk1 -> Optional.ofNullable(terzoBulk1.getCd_terzo()).isPresent()));
            if (terzoBulk.isPresent()) {
                ConsultazionePartitarioBP consBP = (ConsultazionePartitarioBP) context.createBusinessProcess(
                        "ConsultazionePartitarioBP",
                        new Object[]{terzoBulk.get(), Boolean.FALSE, "partitario", true}
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
                        crudScritturaPDoppiaBP.setMessage("La ricerca non ha fornito alcun risultato.");
                        return context.findDefaultForward();
                    }
                } catch (Exception _ex) {
                    handleException(context, _ex);
                }
                context.addHookForward("seleziona",this,"doRiportaPartita");
                context.addBusinessProcess(consBP);
                return context.findDefaultForward();
            }
            setMessage(context, FormBP.WARNING_MESSAGE, "Valorizzare il Tezo!");
            return context.findDefaultForward();
        } catch (FillException _ex) {
            return handleException(context, _ex);
        }
    }
    public Forward doRiportaPartita(ActionContext context)  throws java.rmi.RemoteException {
        try {
            final Optional<Movimento_cogeBulk> modelDetail = getModelDetail(context);
            HookForward caller = (HookForward)context.getCaller();
            java.util.List<PartitarioBulk> partitarioBulks = Optional.ofNullable(caller.getParameter("selectedElements"))
                    .map(List.class::cast)
                    .orElse(Collections.emptyList());
            partitarioBulks.stream()
                    .findAny()
                    .ifPresent(partitarioBulk -> {
                        modelDetail.get().setPartitario(partitarioBulk);
                    });
            return context.findDefaultForward();
        } catch(Exception e) {
            return handleException(context,e);
        }
    }

    public Forward doFreeSearchPartitario(ActionContext actioncontext) {
        throw new MessageToUser("La ricerca guidata, non è supportata!");
    }

    @Override
    protected void blankSearch(ActionContext actioncontext, FormField formfield, OggettoBulk oggettobulk) {
        super.blankSearch(actioncontext, formfield, oggettobulk);
        if (formfield.getField().getProperty().equalsIgnoreCase("terzo")){
            getModelDetail(actioncontext).get().setPartitario(new PartitarioBulk());
        }
    }

    @Override
    public Forward doCerca(ActionContext actioncontext) throws RemoteException, InstantiationException, RemoveException {
        return super.doCerca(actioncontext);
    }
}
