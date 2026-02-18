package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.GestConfCNRBP;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.CRUDAction;
import java.rmi.RemoteException;

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

    @Override
    public Forward doRiportaSelezione(ActionContext actioncontext, OggettoBulk oggettobulk) throws RemoteException {

        GestConfCNRBP bp = (GestConfCNRBP) actioncontext.getBusinessProcess();
        Forward forward = super.doRiportaSelezione(actioncontext, oggettobulk);

        if(oggettobulk != null){
            Configurazione_cnrBulk configurazione_cnrBulk = (Configurazione_cnrBulk)oggettobulk;

            if(configurazione_cnrBulk.getEsercizio() != null) {
                boolean isEsercizioAperto = false;
                if (configurazione_cnrBulk.getEsercizio() == 0) {
                    isEsercizioAperto=true;
                    bp.setEsercizioAperto(true);
                } else {
                    try {
                        isEsercizioAperto = bp.controllaEsercizioAperto(actioncontext.getUserContext());
                    } catch (BusinessProcessException e) {
                        throw new RuntimeException(e);
                    } catch (ComponentException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(!isEsercizioAperto){
                    bp.setStatus(bp.VIEW);
                    bp.setEditable(false);
                }
                else {
                    bp.setStatus(bp.EDIT);
                    bp.setEditable(true);
                }
                configurazione_cnrBulk.caricaEsercizioList(actioncontext);
                try {
                    bp.setModel(actioncontext, configurazione_cnrBulk);
                } catch (BusinessProcessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return forward;
    }
}
