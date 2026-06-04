package it.cnr.contab.pdg00.action;

import it.cnr.contab.pdg00.bp.CRUDAccrualMefBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.CRUDAction;

public class CRUDAccrualMefAction extends CRUDAction {

    private CRUDAccrualMefBP getBP(ActionContext context) {
        return (CRUDAccrualMefBP) getBusinessProcess(context);
    }

    @Override
    public Forward doNuovo(ActionContext context) {
        try {
            CRUDAccrualMefBP bp = getBP(context);

            if (bp.isEsercizioChiuso()) {
                bp.setErrorMessage(
                        "Esercizio chiuso: non è possibile inserire un nuovo modulo Accrual MEF."
                );
                return context.findDefaultForward();
            }

            super.doTab(context, "tab", "tabModulo");
            return super.doNuovo(context);

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doTab(ActionContext context, String tabName, String pageName) {
        try {
            fillModel(context);

            /*
             * Il tab Allegati deve essere accessibile anche se il modulo Accrual
             * non è ancora stato salvato.
             *
             * Quindi NON va fatto nessun controllo del tipo:
             * "salvare prima il modulo".
             */
            return super.doTab(context, tabName, pageName);

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doSalva(ActionContext context) {
        try {
            fillModel(context);

            CRUDAccrualMefBP bp = getBP(context);

            if (bp.isEsercizioChiuso()) {
                bp.setErrorMessage(
                        "Esercizio chiuso: non è possibile salvare il modulo Accrual MEF."
                );
                return context.findDefaultForward();
            }

            bp.setSkipAllegatiReload(true);

            try {
                Forward forward = super.doSalva(context);
                bp.setMessage("Modulo Accrual MEF salvato correttamente.");
                return forward;
            } finally {
                bp.setSkipAllegatiReload(false);
            }

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}