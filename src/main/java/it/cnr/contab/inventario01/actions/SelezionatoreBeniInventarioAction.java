package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario01.bp.SelezionatoreBeniInventarioBP;
import it.cnr.contab.inventario01.bp.SelezionatoreBeniProvider;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaAction;

import java.io.Serializable;

/**
 * Action del selezionatore beni comune. Estende SelezionatoreListaAction
 * ed eredita i comandi standard (Excel, selezione, colonne, ecc.).
 */
public class SelezionatoreBeniInventarioAction extends SelezionatoreListaAction implements Serializable {

    /** Apre la ricerca libera sui campi del bene, configurata dal BP del documento. */
    public Forward doFiltraBeni(ActionContext context) {
        try {
            SelezionatoreBeniInventarioBP slbp = (SelezionatoreBeniInventarioBP) context.getBusinessProcess();
            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(true);
            slbp.getProvider().configuraRicercaBeni(context, rlbp);
            rlbp.setShowSearchResult(false);
            context.addHookForward("searchResult", this, "doApplicaFiltroBeni");
            context.addHookForward("filter", this, "doApplicaFiltroBeni");
            return context.addBusinessProcess(rlbp);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /** Riceve le clausole e ricarica la lista con i beni candidati filtrati. */
    public Forward doApplicaFiltroBeni(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            CompoundFindClause clauses = (CompoundFindClause) fwd.getParameter("filter");
            if (clauses == null) {
                clauses = new CompoundFindClause();
            }
            SelezionatoreBeniInventarioBP slbp = (SelezionatoreBeniInventarioBP) context.getBusinessProcess();
            SelezionatoreBeniProvider provider = slbp.getProvider();
            provider.setClauses(clauses);
            slbp.setIterator(context, provider.selectBeniCandidatiByClause(context, clauses));
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}
