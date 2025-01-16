package it.cnr.contab.config00.action;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.CRUDBP;

import java.rmi.RemoteException;

public class CRUDCausaliContabiliAction extends CRUDAction {
    @Override
    public Forward doElimina(ActionContext actioncontext) throws RemoteException {
        CRUDBP crudbp = getBusinessProcess(actioncontext);
        OggettoBulk model = crudbp.getModel();
        super.doElimina(actioncontext);
        try {
            crudbp.edit(actioncontext, model);
        } catch (BusinessProcessException e) {
            return handleException(actioncontext, e);
        }
        return actioncontext.findDefaultForward();
    }
}
