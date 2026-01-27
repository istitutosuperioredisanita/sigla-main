package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.GestConfCNRBP;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.inventario01.bp.CRUDCaricoInventarioBP;
import it.cnr.contab.inventario01.bulk.Buono_carico_scaricoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.CRUDBP;

import java.rmi.RemoteException;

public class GestConfCNRAction extends CRUDAction {

    public Forward doChangeEsercizio(ActionContext actioncontext) throws RemoteException {
        try{
            GestConfCNRBP bp = (GestConfCNRBP)actioncontext.getBusinessProcess();
            Configurazione_cnrBulk model=(Configurazione_cnrBulk)bp.getModel();

            fillModel(actioncontext);
            bp.setModel(actioncontext,model);
            return actioncontext.findDefaultForward();
        }catch (Throwable t) {
            return handleException(actioncontext, t);

        }
    }
}
