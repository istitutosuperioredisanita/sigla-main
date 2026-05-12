package it.cnr.contab.doccont00.action;

import it.cnr.contab.anagraf00.tabrif.bulk.AbicabBulk;
import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.doccont00.bp.CambiaVocePGiroBP;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaBulk;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaIBulk;
import it.cnr.contab.doccont00.core.bulk.Reversale_rigaBulk;
import it.cnr.contab.doccont00.core.bulk.Reversale_rigaIBulk;
import it.cnr.contab.doccont00.ejb.CambiaVocePGiroComponentSession;
import it.cnr.contab.doccont00.tabrif.bulk.CambiaVocePGiroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CambiaVocePGiroAction extends BulkAction {
    public Forward doOnChangeTiGestione(ActionContext actionContext) throws BusinessProcessException {
        try {
            fillModel(actionContext);
            reset(actionContext);
            return actionContext.findDefaultForward();
        } catch (FillException _ex) {
            return handleException(actionContext, _ex);
        }
    }

    public Forward doElabora(ActionContext actionContext) throws BusinessProcessException {
        try {
            fillModel(actionContext);
            final CambiaVocePGiroBP cambiaVocePGiroBP = getCambiaVocePGiroBP(actionContext);
            final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
            ((CambiaVocePGiroComponentSession) cambiaVocePGiroBP.createComponentSession()).elabora(
                    actionContext.getUserContext(),
                    cambiaVocePGiroBulk
            );
            reset(actionContext);
            cambiaVocePGiroBP.setMessage(FormBP.INFO_MESSAGE, "Operazione effettuata.");
            return actionContext.findDefaultForward();
        } catch (FillException | ComponentException | RemoteException _ex) {
            return handleException(actionContext, _ex);
        }
    }

    public Forward doBlankSearchVoceIniziale(ActionContext actionContext, CambiaVocePGiroBulk cambiaVocePGiroBulk) throws BusinessProcessException {
        cambiaVocePGiroBulk.setVoceIniziale(new V_voce_f_partita_giroBulk());
        cambiaVocePGiroBulk.getDettagliEntrata().clear();
        cambiaVocePGiroBulk.getDettagliSpesa().clear();
        return actionContext.findDefaultForward();
    }

    private void reset(ActionContext actionContext) throws BusinessProcessException {
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
        cambiaVocePGiroBulk.setVoceIniziale(null);
        cambiaVocePGiroBulk.setVoceFinale(null);
        cambiaVocePGiroBulk.getDettagliEntrata().clear();
        cambiaVocePGiroBulk.getDettagliSpesa().clear();
    }

    private CambiaVocePGiroBP getCambiaVocePGiroBP(ActionContext actionContext) throws BusinessProcessException {
        return Optional.ofNullable(actionContext.getBusinessProcess())
                .filter(CambiaVocePGiroBP.class::isInstance)
                .map(CambiaVocePGiroBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("BusinessProcess not found!"));
    }

    private CambiaVocePGiroBulk getCambiaVocePGiroBulk(ActionContext actionContext) throws BusinessProcessException {
        return Optional.ofNullable(getCambiaVocePGiroBP(actionContext).getModel())
                .filter(CambiaVocePGiroBulk.class::isInstance)
                .map(CambiaVocePGiroBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Model not found!"));
    }

    public Forward doSelezionaRigheEntrata(ActionContext actionContext) throws BusinessProcessException {
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
        it.cnr.jada.action.HookForward caller = (it.cnr.jada.action.HookForward) actionContext.getCaller();
        final Supplier<Stream<Reversale_rigaBulk>> selectedElements = () ->
                Optional.ofNullable(caller.getParameter("selectedElements"))
                        .filter(List.class::isInstance)
                        .map(List.class::cast)
                        .filter(list -> !list.isEmpty())
                        .map(list ->
                                list.stream()
                                        .filter(Reversale_rigaBulk.class::isInstance)
                                        .map(Reversale_rigaBulk.class::cast)
                        ).orElse(Stream.empty());
        selectedElements.get().forEach(reversaleRigaBulk -> {
            cambiaVocePGiroBulk.addToDettagliEntrata(reversaleRigaBulk);
        });
        return actionContext.findDefaultForward();
    }

    public Forward doAddToCRUDMain_dettaglioEntrataCRUDController(ActionContext actionContext) throws BusinessProcessException {
        try {
            fillModel(actionContext);
            final CambiaVocePGiroBP cambiaVocePGiroBP = getCambiaVocePGiroBP(actionContext);
            final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
            final V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk1 = Optional.ofNullable(cambiaVocePGiroBulk.getVoceIniziale())
                    .filter(vVoceFPartitaGiroBulk -> Optional.ofNullable(vVoceFPartitaGiroBulk.getCd_elemento_voce()).isPresent())
                    .orElseThrow(() -> new ApplicationException("Selezionare la voce iniziale!"));
            it.cnr.jada.util.RemoteIterator ri =
                    cambiaVocePGiroBP.createComponentSession().cercaReversali(
                            actionContext.getUserContext(),
                            cambiaVocePGiroBulk,
                            vVoceFPartitaGiroBulk1,
                            null);
            final SelezionatoreListaBP selezionatoreListaBP = select(actionContext, ri, BulkInfo.getBulkInfo(Reversale_rigaBulk.class), "default", "doSelezionaRigheEntrata");
            final Reversale_rigaIBulk reversaleRigaIBulk = new Reversale_rigaIBulk();
            reversaleRigaIBulk.setModalita_pagamento(null);
            selezionatoreListaBP.setModel(actionContext, reversaleRigaIBulk);
            selezionatoreListaBP.setMultiSelection(Boolean.TRUE);
            return selezionatoreListaBP;
        } catch (Exception e) {
            return handleException(actionContext, e);
        }
    }

    public Forward doAddToCRUDMain_dettaglioSpesaCRUDController(ActionContext actionContext) throws BusinessProcessException {
        try {
            fillModel(actionContext);
            final CambiaVocePGiroBP cambiaVocePGiroBP = getCambiaVocePGiroBP(actionContext);
            final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
            final V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk1 = Optional.ofNullable(cambiaVocePGiroBulk.getVoceIniziale())
                    .filter(vVoceFPartitaGiroBulk -> Optional.ofNullable(vVoceFPartitaGiroBulk.getCd_elemento_voce()).isPresent())
                    .orElseThrow(() -> new ApplicationException("Selezionare la voce iniziale!"));
            it.cnr.jada.util.RemoteIterator ri =
                    cambiaVocePGiroBP.createComponentSession().cercaMandati(
                            actionContext.getUserContext(),
                            cambiaVocePGiroBulk,
                            vVoceFPartitaGiroBulk1,
                            null);
            final SelezionatoreListaBP selezionatoreListaBP = select(actionContext, ri, BulkInfo.getBulkInfo(Mandato_rigaBulk.class), "default", "doSelezionaRigheSpesa");
            final Mandato_rigaIBulk mandatoRigaIBulk = new Mandato_rigaIBulk();
            mandatoRigaIBulk.setModalita_pagamento(null);
            selezionatoreListaBP.setModel(actionContext, mandatoRigaIBulk);
            selezionatoreListaBP.setMultiSelection(Boolean.TRUE);
            return selezionatoreListaBP;
        } catch (Exception e) {
            return handleException(actionContext, e);
        }
    }
    public Forward doSelezionaRigheSpesa(ActionContext actionContext) throws BusinessProcessException {
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = getCambiaVocePGiroBulk(actionContext);
        it.cnr.jada.action.HookForward caller = (it.cnr.jada.action.HookForward) actionContext.getCaller();
        final Supplier<Stream<Mandato_rigaBulk>> selectedElements = () ->
                Optional.ofNullable(caller.getParameter("selectedElements"))
                        .filter(List.class::isInstance)
                        .map(List.class::cast)
                        .filter(list -> !list.isEmpty())
                        .map(list ->
                                list.stream()
                                        .filter(Mandato_rigaBulk.class::isInstance)
                                        .map(Mandato_rigaBulk.class::cast)
                        ).orElse(Stream.empty());
        selectedElements.get().forEach(mandatoRigaBulk -> {
            cambiaVocePGiroBulk.addToDettagliSpesa(mandatoRigaBulk);
        });
        return actionContext.findDefaultForward();
    }
}
