package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.CRUDElaboraBilRiclassificatoBP;
import it.cnr.contab.config00.pdcep.bulk.BilRiclassificatoBulk;
import it.cnr.contab.pdg01.bp.SelezionatoreAssestatoBP;
import it.cnr.contab.prevent00.bulk.V_assestatoBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.varstanz00.bulk.Var_stanz_resBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.action.SelezionatoreListaAction;

import java.math.BigDecimal;
import java.rmi.RemoteException;

public class CRUDElaboraBilRiclassificatoAction extends SelezionatoreListaAction {

    public Forward doElaboraDati(ActionContext actioncontext) throws RemoteException {
        try {
            fillModel(actioncontext);
            CRUDElaboraBilRiclassificatoBP bp = (CRUDElaboraBilRiclassificatoBP) actioncontext.getBusinessProcess();
            bp.elaboraBilancioIRES(actioncontext);
            return actioncontext.findDefaultForward();
        } catch(Exception e) {
            return handleException(actioncontext,e);
        }
    }

    public Forward doOnChangeImportoFinale(ActionContext actioncontext){
        CRUDElaboraBilRiclassificatoBP bp = (CRUDElaboraBilRiclassificatoBP)actioncontext.getBusinessProcess();
        try {
            bp.fillModels(actioncontext);
            bp.aggiornaImportoFinale(actioncontext);
        } catch (FillException e) {
            bp.setMessage(e.getMessage());
        } catch (BusinessProcessException e) {
            return handleException(actioncontext, e);
        }
        return actioncontext.findDefaultForward();
    }

    public Forward doRefresh(ActionContext actioncontext) throws RemoteException {
        try {
            CRUDElaboraBilRiclassificatoBP bp = (CRUDElaboraBilRiclassificatoBP) actioncontext.getBusinessProcess();
            bp.refresh(actioncontext);
            return actioncontext.findDefaultForward();
        } catch(Exception e) {
            return handleException(actioncontext,e);
        }
    }

    public Forward doElimina(ActionContext actioncontext) throws RemoteException {
        try {
            CRUDElaboraBilRiclassificatoBP bp = (CRUDElaboraBilRiclassificatoBP) actioncontext.getBusinessProcess();
            fillModel(actioncontext);
            bp.setSelection(actioncontext);
            bp.elimina(actioncontext);
            return actioncontext.findDefaultForward();
        } catch(Exception e) {
            return handleException(actioncontext,e);
        }
    }
}
