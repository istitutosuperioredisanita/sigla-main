package it.cnr.contab.coepcoan00.action;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.CRUDAction;

public class CRUDGruppoEPAction extends CRUDAction {

    public Forward doSalvaAndChiudi(ActionContext actioncontext) {
        try {
            fillModel(actioncontext);
            getBusinessProcess(actioncontext).save(actioncontext);
            return super.doCloseForm(actioncontext);
        } catch (BusinessProcessException | ValidationException | FillException e) {
            return handleException(actioncontext, e);
        }
    }
}
