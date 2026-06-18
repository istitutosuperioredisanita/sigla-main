package it.cnr.contab.pdg00.action;

import it.cnr.contab.compensi00.ejb.AddizionaliComponentSession;
import it.cnr.contab.doccont00.bp.CRUDObbligazioneBP;
import it.cnr.contab.pdg00.bp.CRUDAccrualMefBP;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.action.CRUDAction;

public class CRUDAccrualMefAction extends CRUDAction {

    private CRUDAccrualMefBP getBP(ActionContext context) {
        return (CRUDAccrualMefBP) getBusinessProcess(context);
    }
    public Forward doCreaFileAccrual(ActionContext context) throws BusinessProcessException, FillException {
        CRUDAccrualMefBP bp = (CRUDAccrualMefBP) context.getBusinessProcess();
        try {
            bp.fillModel(context);
        } catch (Exception e) {
            return handleException(context,e);
        }
        bp.creaFileAccrual(context, (AccrualBulk) bp.getModel());
        return context.findDefaultForward();
    }



}