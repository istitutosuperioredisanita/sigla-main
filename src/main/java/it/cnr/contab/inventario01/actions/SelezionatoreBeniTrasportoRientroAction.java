package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bp.SelezionatoreBeniTrasportoRientroBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaAction;

import java.io.Serializable;

/**
 * Action del selezionatore beni. Estende SelezionatoreListaAction
 * ed eredita i comandi standard (Excel, selezione, colonne, ecc.).
 */
public class SelezionatoreBeniTrasportoRientroAction extends SelezionatoreListaAction
        implements Serializable {

    /** Apre la ricerca libera sui campi del bene (prototipo InventarioDocTRBulk). */
    public Forward doFiltraBeni(ActionContext context) {
        try {
            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(true);
            rlbp.setPrototype(new InventarioDocTRBulk(), null, null, "searchTrasportoRientro");
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

            SelezionatoreBeniTrasportoRientroBP slbp =
                    (SelezionatoreBeniTrasportoRientroBP) context.getBusinessProcess();
            CRUDTraspRientInventarioBP bp = slbp.getDocumentoBP();

            bp.setClauses(clauses);
            slbp.setIterator(context, bp.selectBeniCandidatiByClause(context, clauses));
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}