package it.cnr.contab.pdg00.action;

import it.cnr.contab.pdg00.bp.CaricFlStipBP;
import it.cnr.contab.pdg00.cdip.bulk.CaricFlStipBulk;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;

public class CaricFlStipAction extends it.cnr.jada.util.action.CRUDAction {

    private void validateTipoRapporto(String tipoRapporto) throws it.cnr.jada.comp.ApplicationException {
        if (tipoRapporto == null || tipoRapporto.isEmpty()) {
            throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare il tipo rapporto.");
        }
    }

    @Override
    public Forward doSalva(ActionContext context) {
        try {
            CaricFlStipBP bp = (CaricFlStipBP) context.getBusinessProcess();
            bp.fillModel(context); // select option + file

            CaricFlStipBulk model = (CaricFlStipBulk) bp.getModel();
            validateTipoRapporto(model.getTipo_rapporto());

            bp.create(context); // store + elaborazione + reset

            // il BP setta già INFO
            return context.findDefaultForward();

        } catch (Exception e) {
            return handleException(context, e);
        }
    }


//    @Override
//    public Forward doSalva(ActionContext context) throws RemoteException {
//        try {
//            CaricFlStipBP bp = (CaricFlStipBP) context.getBusinessProcess();
//            bp.fillModel(context);
//            CaricFlStipBulk tipoRapportoBulk = (CaricFlStipBulk) bp.getModel();
//
//            validateTipoRapporto(tipoRapportoBulk.getTipo_rapporto());
//
//            bp.doCaricaGestioneStip(context);
//
//            // Resta in stato INSERT e lascia la select option SEMPRE abilitata
//            bp.setStatus(CRUDBP.INSERT);
//            bp.setMessage(FormBP.INFO_MESSAGE, "Flusso stipendi elaborato correttamente.");
//
//            return context.findDefaultForward();
//
//        } catch (Exception e) {
//            return handleException(context, e);
//        }
//    }
}
