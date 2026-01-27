package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.GestConfCNRBP;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.CRUDAction;

public class GestConfCNRAction extends CRUDAction {

    public Forward doCerca(ActionContext context) throws java.rmi.RemoteException, InstantiationException, javax.ejb.RemoveException {

        try {
            GestConfCNRBP bp = (GestConfCNRBP) context.getBusinessProcess();
            Configurazione_cnrBulk conf =(Configurazione_cnrBulk)bp.getModel();
            fillModel(context);

            if(conf.getEsercizio() == null){
                throw new it.cnr.jada.comp.ApplicationException("Attenzione! impostare l'esercizio");
            }
            return super.doCerca(context);
        } catch (Exception e) {
            return handleException(context,e);
        }
    }

}
