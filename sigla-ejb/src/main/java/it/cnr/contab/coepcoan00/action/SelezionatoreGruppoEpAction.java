package it.cnr.contab.coepcoan00.action;

import it.cnr.contab.coepcoan00.bp.SelezionatoreGruppoEpBP;
import it.cnr.contab.config00.pdcep.bulk.GruppoEPBulk;
import it.cnr.contab.doccont00.bp.CRUDMandatoBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.OptionBP;
import it.cnr.jada.util.action.SelezionatoreListaAlberoAction;
import it.cnr.jada.util.action.SimpleCRUDBP;

import java.rmi.RemoteException;
import java.util.UUID;

public class SelezionatoreGruppoEpAction extends SelezionatoreListaAlberoAction {

    public Forward doCambiaTipoBilancio(ActionContext actioncontext) throws RemoteException, FillException {
        try {
            fillModel(actioncontext);
            SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actioncontext.getBusinessProcess();
            selezionatoreGruppoEpBP.getHistory().clear();
            selezionatoreGruppoEpBP.setParentElement(null);
            selezionatoreGruppoEpBP.refreshRemoteBulkTree(actioncontext);
            return actioncontext.findDefaultForward();
        } catch (BusinessProcessException e) {
            return handleException(actioncontext, e);
        }
    }

    public Forward doRefreshRemoteBulkTree(ActionContext actionContext) throws BusinessProcessException {
        try {
            SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actionContext.getBusinessProcess();
            selezionatoreGruppoEpBP.refreshRemoteBulkTree(actionContext);
            return actionContext.findDefaultForward();
        } catch (BusinessProcessException e) {
            return handleException(actionContext, e);
        }
    }

    public Forward doModificaRamo(ActionContext actionContext) throws BusinessProcessException {
        SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actionContext.getBusinessProcess();
        SimpleCRUDBP simpleCRUDBP = (SimpleCRUDBP) actionContext.createBusinessProcess("CRUDGruppoEPBP", new Object[]{"M"});
        simpleCRUDBP.edit(actionContext, (OggettoBulk) selezionatoreGruppoEpBP.getFocusedElement());
        actionContext.addHookForward("close", this, "doRefreshRemoteBulkTree");
        return actionContext.addBusinessProcess(simpleCRUDBP);
    }

    public Forward doNuovoRamo(ActionContext actionContext) throws BusinessProcessException {
        SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actionContext.getBusinessProcess();
        SimpleCRUDBP simpleCRUDBP = (SimpleCRUDBP) actionContext.createBusinessProcess("CRUDGruppoEPBP", new Object[]{"M"});
        GruppoEPBulk gruppoEPBulk = selezionatoreGruppoEpBP.getSelezione_tipo_bilancio();
        GruppoEPBulk mewGruppoEPBulk = new GruppoEPBulk();
        mewGruppoEPBulk.setRowid(UUID.randomUUID().toString());
        mewGruppoEPBulk.setTipoBilancio(gruppoEPBulk.getTipoBilancio());
        mewGruppoEPBulk.setToBeCreated();
        simpleCRUDBP.setModel(actionContext, mewGruppoEPBulk);
        actionContext.addHookForward("close", this, "doRefreshRemoteBulkTree");
        return actionContext.addBusinessProcess(simpleCRUDBP);
    }

    public Forward doNuovoRamoFiglio(ActionContext actionContext) throws BusinessProcessException {
        SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actionContext.getBusinessProcess();
        SimpleCRUDBP simpleCRUDBP = (SimpleCRUDBP) actionContext.createBusinessProcess("CRUDGruppoEPBP", new Object[]{"M"});
        GruppoEPBulk gruppoEPBulk = (GruppoEPBulk) selezionatoreGruppoEpBP.getFocusedElement();
        GruppoEPBulk mewGruppoEPBulk = new GruppoEPBulk();
        mewGruppoEPBulk.setRowid(UUID.randomUUID().toString());
        mewGruppoEPBulk.setTipoBilancio(gruppoEPBulk.getTipoBilancio());
        mewGruppoEPBulk.setCdPianoGruppi(gruppoEPBulk.getCdPianoGruppi());
        mewGruppoEPBulk.setCdGruppoPadre(gruppoEPBulk.getCdGruppoEp());
        mewGruppoEPBulk.setCdPianoPadre(gruppoEPBulk.getCdPianoGruppi());
        mewGruppoEPBulk.setToBeCreated();
        simpleCRUDBP.setModel(actionContext, mewGruppoEPBulk);
        actionContext.addHookForward("close", this, "doRefreshRemoteBulkTree");
        return actionContext.addBusinessProcess(simpleCRUDBP);
    }

    public Forward doCancellaRamo(ActionContext actionContext) throws BusinessProcessException {
        return openConfirm(actionContext,"Confermi la cancellazione del ramo corrente?", OptionBP.CONFIRM_YES_NO,"doConfermaCancellaRamo");
    }

    public Forward doConfermaCancellaRamo(ActionContext actionContext, int option) throws BusinessProcessException {
        if (option == OptionBP.YES_BUTTON) {
            SelezionatoreGruppoEpBP selezionatoreGruppoEpBP = (SelezionatoreGruppoEpBP)actionContext.getBusinessProcess();
            GruppoEPBulk gruppoEPBulk = (GruppoEPBulk) selezionatoreGruppoEpBP.getFocusedElement();
            try {
                gruppoEPBulk.setToBeDeleted();
                selezionatoreGruppoEpBP.createComponentSession().eliminaConBulk(actionContext.getUserContext(), gruppoEPBulk);
                return doRefreshRemoteBulkTree(actionContext);
            } catch (ComponentException|RemoteException e) {
                return handleException(actionContext, e);
            }
        }
        return actionContext.findDefaultForward();
    }
}
