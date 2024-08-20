package it.cnr.contab.docamm00.actions;

import it.cnr.contab.docamm00.bp.CRUDAutofatturaBP;
import it.cnr.contab.docamm00.docs.bulk.AutofatturaBulk;
import it.cnr.contab.docamm00.ejb.AutoFatturaComponentSession;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;

public class CRUDAutofatturaAction extends EconomicaAction {

    /**
     * Viene richiamato nel momento in cui si seleziona una valuta dal combo Valuta nella
     * testata della fattura.
     * Richiama a sua volta il metodo cercaCambio dalla component.
     */
    protected void basicDoOnIstituzionaleCommercialeChange(ActionContext context, AutofatturaBulk autofatturaBulk)
            throws it.cnr.jada.comp.ComponentException {

        try {
            AutoFatturaComponentSession h = (AutoFatturaComponentSession) ((CRUDAutofatturaBP) getBusinessProcess(context)).createComponentSession();
            java.util.Vector sezionali = h.estraeSezionali(context.getUserContext(), autofatturaBulk);
            autofatturaBulk.setSezionali(sezionali);
            if (!getBusinessProcess(context).isSearching() &&
                    sezionali != null && !sezionali.isEmpty())
                autofatturaBulk.setTipo_sezionale((Tipo_sezionaleBulk) sezionali.firstElement());
            else
                autofatturaBulk.setTipo_sezionale(null);
        } catch (Throwable t) {
            throw new it.cnr.jada.comp.ComponentException(t);
        }
    }

    /**
     * Gestisce il cambiamento del sezionale
     *
     * @param context L'ActionContext della richiesta
     * @return Il Forward alla pagina di risposta
     */

    public Forward doOnSezionaliFlagsChange(ActionContext context) {

        try {
            CRUDAutofatturaBP bp = (CRUDAutofatturaBP) getBusinessProcess(context);
            AutofatturaBulk fattura = (AutofatturaBulk) bp.getModel();
            fillModel(context);
            try {

                basicDoOnIstituzionaleCommercialeChange(context, fattura);
                bp.setModel(context, fattura);
                return context.findDefaultForward();
            } catch (it.cnr.jada.comp.ComponentException e) {
                bp.setModel(context, fattura);
                throw e;
            }
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }
}
