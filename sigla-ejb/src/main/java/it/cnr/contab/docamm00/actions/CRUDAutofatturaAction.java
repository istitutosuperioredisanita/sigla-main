package it.cnr.contab.docamm00.actions;

import it.cnr.contab.docamm00.bp.CRUDAutofatturaBP;
import it.cnr.contab.docamm00.bp.CRUDFatturaPassivaBP;
import it.cnr.contab.docamm00.docs.bulk.AutofatturaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.ejb.AutoFatturaComponentSession;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;

import java.util.Optional;

public class CRUDAutofatturaAction extends EconomicaAction {

    /**
     * Gestisce il cambiamento del tipo sezionale ricaricandoli
     *
     * @param context L'ActionContext della richiesta
     * @return Il Forward alla pagina di risposta
     */

    public Forward doOnIstituzionaleCommercialeChange(ActionContext context) {
        try {
            CRUDAutofatturaBP bp = (CRUDAutofatturaBP) getBusinessProcess(context);
            AutofatturaBulk autofattura = (AutofatturaBulk) bp.getModel();

            java.util.Collection sezionaliOld = autofattura.getSezionali();
            Boolean intraUE = autofattura.getFl_intra_ue();
            Boolean extraUE = autofattura.getFl_extra_ue();
            Boolean sanMarinoCI = autofattura.getFl_san_marino_con_iva();
            Boolean sanMarinoSI = autofattura.getFl_san_marino_senza_iva();
            Boolean autof = autofattura.getFl_autofattura();
            String fattServizi = autofattura.getTi_bene_servizio();
            Boolean liqDiff = autofattura.getFl_liquidazione_differita();
            fillModel(context);
            autofattura = (AutofatturaBulk) bp.getModel();
            try {
                basicDoOnIstituzionaleCommercialeChange(context, autofattura);
                bp.setModel(context, autofattura);
                return context.findDefaultForward();
            } catch (it.cnr.jada.comp.ComponentException e) {
                autofattura.setSezionali(sezionaliOld);
                autofattura.setFl_intra_ue(intraUE);
                autofattura.setFl_extra_ue(extraUE);
                autofattura.setFl_san_marino_con_iva(sanMarinoCI);
                autofattura.setFl_san_marino_senza_iva(sanMarinoSI);
                autofattura.setTi_bene_servizio(fattServizi);
                autofattura.setFl_liquidazione_differita(liqDiff);
                bp.setModel(context, autofattura);
                throw e;
            }
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }

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
    public Forward doVisualizzaFattura(ActionContext context) throws BusinessProcessException {
        CRUDAutofatturaBP autofatturaBP = (CRUDAutofatturaBP) context.getBusinessProcess();
        AutofatturaBulk autofatturaBulk = (AutofatturaBulk) autofatturaBP.getModel();
        Optional<Fattura_passivaBulk> fatturaCollegata = Optional.ofNullable(autofatturaBulk.getFattura_passiva());
        it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk unita_organizzativa = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
        try {
            CRUDFatturaPassivaBP nbp = (CRUDFatturaPassivaBP) context.createBusinessProcess("CRUDFatturaPassivaBP",
                    unita_organizzativa.isUoEnte()?new Object[]{"V"}:new Object[]{"M"}
            );
            nbp = (CRUDFatturaPassivaBP) context.addBusinessProcess(nbp);
            nbp.edit(context, ((AutoFatturaComponentSession) autofatturaBP.createComponentSession()).
                    cercaFatturaPassiva(context.getUserContext(), autofatturaBulk));
            return nbp;
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}
