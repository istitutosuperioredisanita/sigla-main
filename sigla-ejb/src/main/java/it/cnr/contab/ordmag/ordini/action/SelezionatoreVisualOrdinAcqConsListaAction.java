package it.cnr.contab.ordmag.ordini.action;

import it.cnr.contab.docamm00.bp.CRUDFatturaPassivaBP;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.ejb.FatturaElettronicaPassivaComponentSession;
import it.cnr.contab.ordmag.ordini.bp.SelezionatoreVisualOrdinAcqConsListaBP;
import it.cnr.contab.ordmag.ordini.bulk.EvasioneOrdineRigaBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SelezionatoreListaAction;
import it.cnr.jada.util.action.SelezionatoreListaBP;

import java.rmi.RemoteException;
import java.util.Optional;

public class SelezionatoreVisualOrdinAcqConsListaAction extends SelezionatoreListaAction {
    public SelezionatoreVisualOrdinAcqConsListaAction() {
        super();
    }

    public Forward doVisualizzaFattura(ActionContext actioncontext) throws BusinessProcessException {
        final SelezionatoreVisualOrdinAcqConsListaBP selezionatoreVisualOrdinAcqConsListaBP =
                Optional.ofNullable(actioncontext.getCurrentBusinessProcess())
                .filter(SelezionatoreVisualOrdinAcqConsListaBP.class::isInstance)
                .map(SelezionatoreVisualOrdinAcqConsListaBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("BusinessProcess not found"));
        final Optional<Fattura_passivaBulk> fatturaPassivaBulk = selezionatoreVisualOrdinAcqConsListaBP.visualizzaFattura(actioncontext);
        if (fatturaPassivaBulk.isPresent()) {
            try {
                CRUDFatturaPassivaBP nbp = (CRUDFatturaPassivaBP) actioncontext.createBusinessProcess("CRUDFatturaPassivaBP", new Object[]{"M"} );
                nbp = (CRUDFatturaPassivaBP) actioncontext.addBusinessProcess(nbp);
                nbp.edit(actioncontext, fatturaPassivaBulk.get());
                return nbp;
            } catch (Throwable e) {
                return handleException(actioncontext, e);
            }
        }
        setMessage(actioncontext, FormBP.ERROR_MESSAGE, "Fattura legata a consegna non trovata!");
        return actioncontext.findDefaultForward();
    }

    public Forward doVisualizzaEvasione(ActionContext actioncontext) throws BusinessProcessException {
        try {
            final SelezionatoreVisualOrdinAcqConsListaBP selezionatoreVisualOrdinAcqConsListaBP =
                    Optional.ofNullable(actioncontext.getCurrentBusinessProcess())
                            .filter(SelezionatoreVisualOrdinAcqConsListaBP.class::isInstance)
                            .map(SelezionatoreVisualOrdinAcqConsListaBP.class::cast)
                            .orElseThrow(() -> new BusinessProcessException("BusinessProcess not found"));
            it.cnr.jada.util.RemoteIterator ri = selezionatoreVisualOrdinAcqConsListaBP.ricercaEvasioni(actioncontext);
            SelezionatoreListaBP nbp = (SelezionatoreListaBP)actioncontext.createBusinessProcess("SelezionatoreVisualEvasioneListaBP");
            nbp.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(EvasioneOrdineRigaBulk.class));
            nbp.setColumns(nbp.getBulkInfo().getColumnFieldPropertyDictionary("seleziona_visualizza"));
            nbp.setIterator(actioncontext,ri);
            HookForward hook = (HookForward)actioncontext.findForward("seleziona");
            return actioncontext.addBusinessProcess(nbp);
        } catch (RemoteException e) {
            return handleException(actioncontext, e);
        }
    }
}
