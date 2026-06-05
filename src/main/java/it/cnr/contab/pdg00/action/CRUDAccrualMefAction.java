package it.cnr.contab.pdg00.action;

import it.cnr.contab.pdg00.bp.CRUDAccrualMefBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.CRUDAction;

public class CRUDAccrualMefAction extends CRUDAction {

    private CRUDAccrualMefBP getBP(ActionContext context) {
        return (CRUDAccrualMefBP) getBusinessProcess(context);
    }
}