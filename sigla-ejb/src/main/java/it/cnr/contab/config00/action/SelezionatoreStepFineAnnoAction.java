package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.SelezionatoreStepFineAnnoBP;
import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.RemoteDetailCRUDController;

import java.rmi.RemoteException;
import java.util.Optional;

public class SelezionatoreStepFineAnnoAction extends BulkAction {

    public SelezionatoreStepFineAnnoAction() {
    }

    public Forward doConferma(ActionContext context) throws BusinessProcessException {
        final SelezionatoreStepFineAnnoBP selezionatoreStepFineAnnoBP = Optional.ofNullable(context.getBusinessProcess())
                .filter(SelezionatoreStepFineAnnoBP.class::isInstance)
                .map(SelezionatoreStepFineAnnoBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("BP not found!"));
        final Configurazione_cnrComponentSession configurazione_cnrComponentSession = (Configurazione_cnrComponentSession)selezionatoreStepFineAnnoBP.createComponentSession(
                "CNRCONFIG00_EJB_Configurazione_cnrComponentSession",
                Configurazione_cnrComponentSession.class
        );
        try {
            fillModel(context);
            final RemoteDetailCRUDController detail = selezionatoreStepFineAnnoBP.getDetail();
            configurazione_cnrComponentSession.modificaConBulk(
                    context.getUserContext(),
                    detail.getModel()
            );
            final int modelIndex = detail.getModelIndex();
            detail.basicReset(context);
            detail.setModelIndex(context, modelIndex);
        } catch (ComponentException | RemoteException | FillException e) {
            throw new BusinessProcessException(e);
        }
        return context.findDefaultForward();
    }


    public Forward doSalva(ActionContext context) throws BusinessProcessException {
        final SelezionatoreStepFineAnnoBP selezionatoreStepFineAnnoBP = Optional.ofNullable(context.getBusinessProcess())
                .filter(SelezionatoreStepFineAnnoBP.class::isInstance)
                .map(SelezionatoreStepFineAnnoBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("BP not found!"));
        try {
            fillModel(context);
            selezionatoreStepFineAnnoBP.save(context);
            return context.findDefaultForward();
        } catch (ValidationException validationexception) {
            selezionatoreStepFineAnnoBP.setErrorMessage(validationexception.getMessage());
        } catch (Throwable throwable) {
            return handleException(context, throwable);
        }
        return context.findDefaultForward();
    }
}
