package it.cnr.contab.coepcoan00.action;

import it.cnr.contab.coepcoan00.bp.CRUDAssociazioneContoGruppoBP;
import it.cnr.contab.config00.pdcep.bulk.AssociazioneContoGruppoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CRUDAssociazioneContoGruppoAction extends CRUDAction {

    public Forward doContiDaAssociare(ActionContext actioncontext) throws BusinessProcessException {
        CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
        try {
            fillModel(actioncontext);
            RemoteIterator remoteiterator = bp.cercaContiDaAssociare(actioncontext);
            if (remoteiterator == null || remoteiterator.countElements() == 0) {
                EJBCommonServices.closeRemoteIterator(actioncontext, remoteiterator);
                bp.setMessage("La ricerca non ha fornito alcun risultato.");
                return actioncontext.findDefaultForward();
            }
            if (remoteiterator.countElements() == 1) {
                ContoBulk oggettobulk1 = (ContoBulk) remoteiterator.nextElement();
                EJBCommonServices.closeRemoteIterator(actioncontext, remoteiterator);
                bp.setMessage(FormBP.INFO_MESSAGE, "La ricerca ha fornito un solo risultato.");
                return doSelezionaConto(actioncontext, oggettobulk1);
            } else {
                SelezionatoreListaBP selezionatorelistabp = (SelezionatoreListaBP) actioncontext.createBusinessProcess("Selezionatore");
                selezionatorelistabp.setIterator(actioncontext, remoteiterator);
                selezionatorelistabp.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(ContoBulk.class));
                selezionatorelistabp.setFormField(bp.getFormField("voceEp"));
                actioncontext.addHookForward("seleziona", this, "doSelezionaConto");
                return actioncontext.addBusinessProcess(selezionatorelistabp);
            }
        } catch (Exception _ex) {
            return handleException(actioncontext, _ex);
        }
    }

    public Forward doContiAssociatiMulipli(ActionContext actioncontext) throws RemoteException {
        CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
        try {
            fillModel(actioncontext);
            RemoteIterator remoteiterator = bp.cercaContiMultipli(actioncontext);
            if (remoteiterator == null || remoteiterator.countElements() == 0) {
                EJBCommonServices.closeRemoteIterator(actioncontext, remoteiterator);
                bp.setMessage("La ricerca non ha fornito alcun risultato.");
                return actioncontext.findDefaultForward();
            }
            SelezionatoreListaBP selezionatorelistabp = (SelezionatoreListaBP) actioncontext.createBusinessProcess("Selezionatore");
            selezionatorelistabp.setIterator(actioncontext, remoteiterator);
            selezionatorelistabp.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(AssociazioneContoGruppoBulk.class));
            actioncontext.addHookForward("seleziona", this, "doSelezionaAssociazioneContoGruppoBulk");
            return actioncontext.addBusinessProcess(selezionatorelistabp);
        } catch (Exception _ex) {
            return handleException(actioncontext, _ex);
        }
    }
    public Forward doSelezionaAssociazioneContoGruppoBulk(ActionContext actioncontext) throws BusinessProcessException {
        HookForward caller = (HookForward) actioncontext.getCaller();
        Optional<AssociazioneContoGruppoBulk> associazioneContoGruppoBulk = Optional.ofNullable(caller.getParameter("selectedElements"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(list -> !list.isEmpty())
                .map(list ->
                        list.stream()
                                .filter(AssociazioneContoGruppoBulk.class::isInstance)
                                .map(AssociazioneContoGruppoBulk.class::cast)
                ).orElse(Stream.empty())
                .findAny();
        if (associazioneContoGruppoBulk.isPresent()) {
            CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
            bp.edit(actioncontext, associazioneContoGruppoBulk.get());
        }
        return actioncontext.findDefaultForward();
    }

    public Forward doSelezionaConto(ActionContext actioncontext) throws BusinessProcessException {
        HookForward caller = (HookForward) actioncontext.getCaller();
        Optional<ContoBulk> voceEpBulk = Optional.ofNullable(caller.getParameter("selectedElements"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(list -> !list.isEmpty())
                .map(list ->
                        list.stream()
                                .filter(ContoBulk.class::isInstance)
                                .map(ContoBulk.class::cast)
                ).orElse(Stream.empty())
                .findAny();
        if (voceEpBulk.isPresent()) {
            return doSelezionaConto(actioncontext, voceEpBulk.get());
        }
        return actioncontext.findDefaultForward();
    }

    public Forward doSelezionaConto(ActionContext actioncontext, ContoBulk voceEp) throws BusinessProcessException {
        Optional.ofNullable(voceEp)
                .ifPresent(voceEpBulk -> {
                    CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
                    final AssociazioneContoGruppoBulk model = (AssociazioneContoGruppoBulk) bp.getModel();
                    model.setVoceEp(voceEpBulk);
                });
        return actioncontext.findDefaultForward();
    }

    public Forward doRefresh(ActionContext actioncontext) throws BusinessProcessException {
        CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
        try {
            fillModel(actioncontext);
            final AssociazioneContoGruppoBulk model = (AssociazioneContoGruppoBulk)bp.getModel();
            model.setGruppoEp(null);
            bp.setModel(actioncontext, bp.createComponentSession().initializeKeysAndOptionsInto(actioncontext.getUserContext(), model));
        } catch (BusinessProcessException | ComponentException | RemoteException | FillException e) {
            return handleException(actioncontext, e);
        }
        return actioncontext.findDefaultForward();
    }
}
