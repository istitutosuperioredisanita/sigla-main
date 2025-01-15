package it.cnr.contab.docamm00.actions;

import it.cnr.contab.config00.bulk.CausaleContabileBulk;
import it.cnr.contab.docamm00.bp.SelezionatoreStornaDocumentoGenericoBP;
import it.cnr.contab.docamm00.bp.StornaDocumentoGenericoBP;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.action.SelezionatoreListaAction;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelezionatoreStornaDocumentoGenericoAction extends SelezionatoreListaAction {
    public Forward doStorno(ActionContext context) {
        SelezionatoreStornaDocumentoGenericoBP bp = (SelezionatoreStornaDocumentoGenericoBP) context.getBusinessProcess();
        try {
            fillModel(context);
            bp.setSelection(context);
            List<Documento_generico_rigaBulk> selectedElements = bp.getSelectedElements(context);
            if (selectedElements == null || selectedElements.isEmpty())
                throw new ApplicationException("Selezionare almeno una documento da stornare!");
            List<CausaleContabileBulk> causaliStorno = Utility.createCRUDComponentSession().find(
                    context.getUserContext(),
                    CausaleContabileBulk.class,
                    "findCausaliStorno",
                    context.getUserContext()
            );
            StornaDocumentoGenericoBP allegatiMultipliFatturaPassivaBP =
                    (StornaDocumentoGenericoBP) context.createBusinessProcess(
                            "StornaDocumentoGenericoBP", new Object[]{
                                    Character.valueOf(bp.getTiEntrataSpesa()),
                                    selectedElements,
                                    new Hashtable<>(causaliStorno
                                            .stream()
                                            .collect(Collectors.toMap(
                                                    CausaleContabileBulk::getCdCausale,
                                                    CausaleContabileBulk::getDsCausale, (key, value) -> value, HashMap::new)
                                            ))
                            }
                    );
            context.addHookForward("close", this, "doRefresh");
            return context.addBusinessProcess(allegatiMultipliFatturaPassivaBP);
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    public Forward doRefresh(ActionContext actioncontext) throws BusinessProcessException {
        Optional.ofNullable(actioncontext.getBusinessProcess())
                .filter(SelezionatoreStornaDocumentoGenericoBP.class::isInstance)
                .map(SelezionatoreStornaDocumentoGenericoBP.class::cast)
                .ifPresent(businessProcess -> {
                    try {
                        businessProcess.refresh(actioncontext);
                    } catch (BusinessProcessException e) {
                        throw new DetailedRuntimeException(e);
                    }
                });
        return actioncontext.findDefaultForward();
    }
}
