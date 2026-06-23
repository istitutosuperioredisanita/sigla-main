package it.cnr.contab.pdg00.action;

import it.cnr.contab.compensi00.ejb.AddizionaliComponentSession;
import it.cnr.contab.doccont00.bp.CRUDDistintaCassiereBP;
import it.cnr.contab.doccont00.bp.CRUDObbligazioneBP;
import it.cnr.contab.doccont00.intcass.bulk.Distinta_cassiereBulk;
import it.cnr.contab.pdg00.bp.CRUDAccrualMefBP;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.OptionBP;

public class CRUDAccrualMefAction extends CRUDAction {

    private CRUDAccrualMefBP getBP(ActionContext context) {
        return (CRUDAccrualMefBP) getBusinessProcess(context);
    }
    public Forward doCreaFileAccrual(ActionContext context) throws BusinessProcessException, FillException {
        CRUDAccrualMefBP bp = (CRUDAccrualMefBP) context.getBusinessProcess();
        try {
            bp.fillModel(context);
            return openConfirm(context,"Sei sicuro di voler generare il file Accrual?",OptionBP.CONFIRM_YES_NO,"doConfermaCreaFileAccrual");

        } catch (Exception e) {
            return handleException(context,e);
        }
    }
    public Forward doConfermaCreaFileAccrual(ActionContext context, int option) {
        try {
            if (option == OptionBP.YES_BUTTON) {
                CRUDAccrualMefBP bp = (CRUDAccrualMefBP) getBusinessProcess(context);
                bp.creaFileAccrual(context, (AccrualBulk) bp.getModel());
                bp.setMessage("Creazione File Accrual effettuato correttamente.");
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }



}