package it.cnr.contab.ordmag.magazzino.action;

import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.magazzino.bulk.Chiusura_magazzinoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.Stampa_consumiBulk;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.comp.ApplicationException;

public class Chiusura_magazzinoAction extends ParametricPrintAction {
    public Forward doOnEsercizioChange(ActionContext actioncontext) throws FillException {
        ParametricPrintBP bp= (ParametricPrintBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {
            if(model.getEsercizio() == null){
                model.setDataInventario(null);
                model.setDataInventarioInizio(null);
                model.setDataChiusuraMovimento(null);
            }else{
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                model.setDataInventario(new java.sql.Timestamp(sdf.parse("31/12/"+model.getEsercizio()).getTime()));
                model.setDataInventarioInizio(new java.sql.Timestamp(sdf.parse("01/01/"+model.getEsercizio()).getTime()));
                model.setDataChiusuraMovimento(new java.sql.Timestamp(sdf.parse("01/01/"+model.getEsercizio()+1).getTime()));
            }
            return actioncontext.findDefaultForward();
        } catch (Throwable e) {
            return handleException(actioncontext, e);
        }
    }

    public Forward doOnDataInventarioChange(ActionContext actioncontext) throws FillException {
        ParametricPrintBP bp= (ParametricPrintBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {
            if(model.getEsercizio() == null && model.getDataInventario() != null){
                model.setDataInventario(null);
                model.setDataInventarioInizio(null);
                model.setDataChiusuraMovimento(null);
                throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");

            }else   if( model.getDataInventario() != null){
                java.util.Calendar gc = java.util.Calendar.getInstance();
                gc.setTime(model.getDataInventario());
                int annoCompetenza = gc.get(java.util.Calendar.YEAR);
                int esercizio = model.getEsercizio();

                if (annoCompetenza != esercizio) {
                    throw new ApplicationException("La \"Data di Riferimento\" deve ricadere nell'esercizio selezionato!");
                }

            }
            return actioncontext.findDefaultForward();
        } catch (Throwable e) {
            return handleException(actioncontext, e);
        }
    }
    public Forward doOnTi_operazioneChange(ActionContext actioncontext) throws FillException {
        ParametricPrintBP bp= (ParametricPrintBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {
            if(model.getTi_operazione().equals(Chiusura_magazzinoBulk.CALCOLO_RIMANENZE)){
                model.setCalcoloRimanenze(true);
                model.setTi_valorizzazione(null);
            }else{
                model.setCalcoloRimanenze(false);
            }
            return actioncontext.findDefaultForward();
        } catch (Throwable e) {
            return handleException(actioncontext, e);
        }
    }


}
