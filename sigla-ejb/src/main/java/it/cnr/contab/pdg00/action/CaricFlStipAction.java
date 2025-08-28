package it.cnr.contab.pdg00.action;

import it.cnr.contab.doccont00.bp.CaricaFileCassiereBP;
import it.cnr.contab.gestiva00.bp.ConsDettRegIvaBP;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.contab.pdg00.bp.CaricFlStipBP;
import it.cnr.contab.pdg00.bp.ElaboraFileStipendiBP;
import it.cnr.contab.pdg00.cdip.bulk.CaricFlStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.GestioneStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.pdg00.cdip.bulk.V_stipendi_cofi_dettBulk;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.SelezionatoreListaAction;
import it.cnr.jada.util.upload.UploadedFile;

import java.io.IOException;
import java.rmi.RemoteException;

public class CaricFlStipAction extends it.cnr.jada.util.action.CRUDAction {

    private void validateTipoRapporto(String tipoRapporto) throws it.cnr.jada.comp.ApplicationException {
        if (tipoRapporto == null || tipoRapporto.isEmpty()) {
            throw new it.cnr.jada.comp.ApplicationException("Attenzione bisogna indicare un tipo rapporto!");
        }
    }


    @Override
    public Forward doSalva(ActionContext context) throws RemoteException {
        try {
            CaricFlStipBP bp = (CaricFlStipBP) context.getBusinessProcess();
            bp.fillModel(context);
            CaricFlStipBulk tipoRapportoBulk = (CaricFlStipBulk) bp.getModel();

            // Validazione
            validateTipoRapporto(tipoRapportoBulk.getTipo_rapporto());

            // Esegui il caricamento dei dati
            bp.doCaricaGestioneStip(context);
            bp.save(context);
            return context.findDefaultForward();

        } catch (Exception e) {
            return handleException(context, e);
        }
    }


}
