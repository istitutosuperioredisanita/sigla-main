package it.cnr.contab.pdg00.action;

public class CaricFlStipAction extends it.cnr.jada.util.action.CRUDAction {
/*
    @Override
    public Forward doSalva(ActionContext context) {
        try {
            CaricFlStipBP bp = (CaricFlStipBP) context.getBusinessProcess();
            bp.fillModel(context);

            CaricFlStipBulk bulk = (CaricFlStipBulk) bp.getModel();
            validateTipoRapporto(bulk.getTipo_rapporto());

            // delega al BP: salvataggio parent → store XLSX nel documentale → processFlussoStipendi()
            bp.create(context);

            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

        private void validateTipoRapporto(String tipoRapporto) throws ApplicationException {
        if (tipoRapporto == null || tipoRapporto.isEmpty()) {
            throw new ApplicationException("Attenzione: indicare il tipo rapporto.");
        }
    }

 */
}
