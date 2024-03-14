package it.cnr.contab.coepcoan00.action;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.coepcoan00.bp.CRUDAssociazioneContoGruppoBP;
import it.cnr.contab.config00.pdcep.bulk.AssociazioneContoGruppoBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.docamm00.fatturapa.bulk.DocumentoEleTestataBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.OggettoBulk;
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
                Voce_epBulk oggettobulk1 = (Voce_epBulk) remoteiterator.nextElement();
                EJBCommonServices.closeRemoteIterator(actioncontext, remoteiterator);
                bp.setMessage(FormBP.INFO_MESSAGE, "La ricerca ha fornito un solo risultato.");
                return doSelezionaConto(actioncontext, oggettobulk1);
            } else {
                SelezionatoreListaBP selezionatorelistabp = (SelezionatoreListaBP) actioncontext.createBusinessProcess("Selezionatore");
                selezionatorelistabp.setIterator(actioncontext, remoteiterator);
                selezionatorelistabp.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(Voce_epBulk.class));
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
        Optional<Voce_epBulk> voceEpBulk = Optional.ofNullable(caller.getParameter("selectedElements"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(list -> !list.isEmpty())
                .map(list ->
                        list.stream()
                                .filter(Voce_epBulk.class::isInstance)
                                .map(Voce_epBulk.class::cast)
                ).orElse(Stream.empty())
                .findAny();
        if (voceEpBulk.isPresent()) {
            return doSelezionaConto(actioncontext, voceEpBulk.get());
        }
        return actioncontext.findDefaultForward();
    }
    public Forward doSelezionaConto(ActionContext actioncontext, Voce_epBulk voceEp) throws BusinessProcessException {
        Optional.ofNullable(voceEp)
                .ifPresent(voceEpBulk -> {
                    try {
                        CRUDAssociazioneContoGruppoBP bp = (CRUDAssociazioneContoGruppoBP)actioncontext.getBusinessProcess();
                        bp.reset(actioncontext);
                        final AssociazioneContoGruppoBulk model = (AssociazioneContoGruppoBulk) bp.getModel();
                        model.setVoceEp(voceEpBulk);
                    } catch (BusinessProcessException e) {
                        throw new DetailedRuntimeException(e);
                    }
                });
        return actioncontext.findDefaultForward();
    }

}
