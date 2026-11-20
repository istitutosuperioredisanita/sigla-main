package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario01.bp.SelezionatoreBeniInventarioBP;
import it.cnr.contab.inventario01.bp.SelezionatoreBeniProvider;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneComplessaBulk;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaAction;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.io.Serializable;
import java.rmi.RemoteException;

/** Action del selezionatore beni comune. */
public class SelezionatoreBeniInventarioAction extends SelezionatoreListaAction implements Serializable {

    private static final String[] HOOK_RICERCA = {"filter", "searchResult", "close"};

    /** Apre la ricerca libera sui campi del bene. */
    public Forward doFiltraBeni(ActionContext context) {
        try {
            SelezionatoreBeniInventarioBP slbp = (SelezionatoreBeniInventarioBP) context.getBusinessProcess();
            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(true);
            slbp.getProvider().configuraRicercaBeni(context, rlbp);
            rlbp.setShowSearchResult(false);
            ripristinaCondizioni(context, rlbp, slbp.getCondizioneCorrente());
            slbp.setRicercaFiltroBeni(rlbp);

            context.addHookForward("searchResult", this, "doApplicaFiltroBeni");
            context.addHookForward("filter", this, "doApplicaFiltroBeni");
            context.addHookForward("close", this, "doChiudiFiltroBeni");
            return context.addBusinessProcess(rlbp);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /** Chiude la ricerca senza applicare filtri. */
    public Forward doChiudiFiltroBeni(ActionContext context) {
        rimuoviHookRicerca(context);
        if (context.getBusinessProcess() instanceof SelezionatoreBeniInventarioBP) {
            SelezionatoreBeniInventarioBP slbp = (SelezionatoreBeniInventarioBP) context.getBusinessProcess();
            RicercaLiberaBP rlbp = slbp.getRicercaFiltroBeni();
            if (rlbp != null && rlbp.isNuovaCondizione()) {
                rlbp.cancellaCondizioneCorrente(context);
            }
            slbp.setRicercaFiltroBeni(null);
        }
        return context.findDefaultForward();
    }

    /** Applica il filtro e ricrea il selezionatore. */
    public Forward doApplicaFiltroBeni(ActionContext context) {
        try {
            rimuoviHookRicerca(context);
            HookForward fwd = (HookForward) context.getCaller();
            CompoundFindClause clauses = (CompoundFindClause) fwd.getParameter("filter");
            if (clauses == null) {
                clauses = new CompoundFindClause();
            }

            SelezionatoreBeniInventarioBP corrente = (SelezionatoreBeniInventarioBP) context.getBusinessProcess();
            SelezionatoreBeniProvider provider = corrente.getProvider();
            CondizioneComplessaBulk condizione = corrente.getRicercaFiltroBeni() != null
                    ? corrente.getRicercaFiltroBeni().getCondizioneRadice()
                    : null;
            CompoundFindClause clausesPrecedenti = corrente.getClausesFiltro();
            CondizioneComplessaBulk condizionePrecedente = corrente.getCondizioneCorrente();
            context.closeBusinessProcess();

            String messaggio = null;
            RemoteIterator ri = apriBeni(context, provider, clauses);
            if (ri.countElements() == 0) {
                EJBCommonServices.closeRemoteIterator(context, ri);
                messaggio = "Nessun bene corrisponde al filtro impostato: è stato mantenuto il filtro precedente.";
                clauses = clausesPrecedenti;
                condizione = condizionePrecedente;
                ri = apriBeni(context, provider, clauses);
                if (ri.countElements() == 0) {
                    EJBCommonServices.closeRemoteIterator(context, ri);
                    if (context.getBusinessProcess() instanceof FormBP) {
                        ((FormBP) context.getBusinessProcess()).setMessage("Nessun bene disponibile.");
                    }
                    return context.findDefaultForward();
                }
            }

            provider.setClauses(clauses);
            SelezionatoreBeniInventarioBP nuovo = SelezionatoreBeniInventarioBP.crea(context, provider, ri);
            nuovo.setClausesFiltro(clauses);
            nuovo.setCondizioneCorrente(condizione);
            if (messaggio != null) {
                nuovo.setMessage(messaggio);
            }
            return context.addBusinessProcess(nuovo);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /** Ripristina le condizioni del filtro corrente nella ricerca libera. */
    private void ripristinaCondizioni(ActionContext context, RicercaLiberaBP rlbp,
                                      CondizioneComplessaBulk condizioni) {
        if (condizioni == null || condizioni.children == null || condizioni.children.isEmpty()) {
            return;
        }
        rlbp.setNuovaCondizione(false);
        rlbp.setCondizioneRadice(condizioni);
        rlbp.setRadice(condizioni);
        rlbp.setRigaSelezionata(context, 0);
        int ultima = rlbp.size() - 1;
        if (ultima > 0) {
            rlbp.setRigaSelezionata(context, ultima);
        }
        rlbp.nuovaCondizione();
    }

    /** Apre l'iteratore dei beni candidati. */
    private RemoteIterator apriBeni(ActionContext context, SelezionatoreBeniProvider provider,
                                    CompoundFindClause clauses) throws BusinessProcessException, RemoteException {
        return EJBCommonServices.openRemoteIterator(context, provider.selectBeniCandidatiByClause(context, clauses));
    }

    /** Rimuove gli hook di ricerca non utilizzati. */
    private void rimuoviHookRicerca(ActionContext context) {
        for (String hook : HOOK_RICERCA) {
            context.removeHookForward(hook);
        }
    }
}