package it.cnr.contab.ordmag.magazzino.action;

import it.cnr.contab.anagraf00.bp.CRUDAnagraficaBP;
import it.cnr.contab.config00.esercizio.bulk.EsercizioHome;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario01.bp.CRUDCaricoInventarioBP;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.magazzino.bp.CaricoManualeMagazzinoBP;
import it.cnr.contab.ordmag.magazzino.bp.StampaChiusuraMagazzinoBP;
import it.cnr.contab.ordmag.magazzino.bulk.*;
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.bulk.ValidationParseException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.OptionBP;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public class Chiusura_magazzinoAction extends ParametricPrintAction {



    @Override
    public void init(Config config) {
        super.init(config);

    }

    public Forward doOnEsercizioChange(ActionContext actioncontext) throws FillException, ApplicationException {
        StampaChiusuraMagazzinoBP bp= (StampaChiusuraMagazzinoBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {

            if(model.getEsercizio() == null){
                model.setDataInventario(null);
                model.setDataInventarioInizio(null);
                model.setDataChiusuraMovimento(null);
                bp.setChiusuraAnno(null);
            }else{
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                model.setDataInventario(new java.sql.Timestamp(sdf.parse("31/12/"+model.getEsercizio()).getTime()));
                model.setDataInventarioInizio(new java.sql.Timestamp(sdf.parse("01/01/"+model.getEsercizio()).getTime()));
                int annoChiusura = model.getEsercizio().intValue()+1;

                model.setDataChiusuraMovimento(new java.sql.Timestamp(sdf.parse("01/01/"+annoChiusura).getTime()));

                ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) bp.createComponentSession(
                        "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

                bp.setChiusuraAnno(this.verificaChiusuraAnno(actioncontext, model.getEsercizio()));
                if(bp.getChiusuraAnno()!=null){
                    bp.setEffettuatoCalcolo(true);
                    model.setNascondiCampiStampa(false);
                }else{
                    bp.setEffettuatoCalcolo(false);
                    model.setNascondiCampiStampa(true);
                }

            }
            return actioncontext.findDefaultForward();
        } catch (ValidationParseException val){
            throw new ApplicationException(val.getErrorMessage());
        } catch (Throwable e) {
            return handleException(actioncontext, e);
        }
    }

    public Forward doOnDataInventarioChange(ActionContext actioncontext) throws FillException, ApplicationException {
        StampaChiusuraMagazzinoBP bp= (StampaChiusuraMagazzinoBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {
            if(model.getEsercizio() == null && model.getDataInventario() != null){
                model.setDataInventario(null);
                model.setDataInventarioInizio(null);
                model.setDataChiusuraMovimento(null);
                bp.setChiusuraAnno(null);
                throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");

            }else   if( model.getDataInventario() != null){

                validaDataDiRiferimento(model.getDataInventario(),model.getEsercizio());
            }
            if(model.getEsercizio() != null && bp.getChiusuraAnno()==null){
                bp.setChiusuraAnno(this.verificaChiusuraAnno(actioncontext, model.getEsercizio()));

            }
            return actioncontext.findDefaultForward();
        } catch(Exception e){
            bp.setErrorMessage(e.getMessage());
            return actioncontext.findDefaultForward();
        }catch (Throwable e) {
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
    protected ParametricPrintBP getBusinessProcess(ActionContext actioncontext) {
        return (ParametricPrintBP)actioncontext.getBusinessProcess();
    }
    public Forward doCalcolaRimanenze(ActionContext context) {
        try {
            fillModel(context);
            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);

            if(stampaChiusuraBP.getChiusuraAnno() != null) {
                return openConfirm(context, "Attenzione!Calcolo delle rimanenze già effettuato.Procedere con un nuovo calcolo?", OptionBP.CONFIRM_YES_NO, "doConfirmCalcolaRimanenze");
            }else{
                return doConfirmCalcolaRimanenze(context,OptionBP.YES_BUTTON);
            }
        } catch(Exception e) {
            return handleException(context,e);
        }
    }
    public Forward doOnRaggrReportChange(ActionContext actioncontext) throws FillException{
        ParametricPrintBP bp= (ParametricPrintBP) actioncontext.getBusinessProcess();
        Chiusura_magazzinoBulk model=(Chiusura_magazzinoBulk)bp.getModel();
        fillModel(actioncontext);
        try {
            if(model.getTi_raggr_report().equals(Chiusura_magazzinoBulk.RAGGR_REPORT_CAT_GRUPPO)){
                model.setFlRaggCatGruppo(true);
                model.setFlDettaglioArticolo(false);
            }else{
                model.setFlRaggCatGruppo(false);
                model.setFlDettaglioArticolo(true);
            }
            return actioncontext.findDefaultForward();
        } catch (Throwable e) {
            return handleException(actioncontext, e);
        }
    }
    public Forward doConfirmCalcolaRimanenze(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException, ValidationException {

        if(i == OptionBP.YES_BUTTON) {

            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);
            try {
                Chiusura_magazzinoBulk model = (Chiusura_magazzinoBulk) stampaChiusuraBP.getModel();
                fillModel(context);

                validaModelPerCalcoloRimanenze(model);
                ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                        "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

                ChiusuraAnnoBulk chiusuraAnnoBulk = chiusuraAnnoComponent.calcolaRimanenzeAnno(context.getUserContext(), model.getEsercizio(), model.getDataInventario());

                stampaChiusuraBP.setEffettuatoCalcolo(true);
                model.setNascondiCampiStampa(false);
                stampaChiusuraBP.setChiusuraAnno(chiusuraAnnoBulk);
            } catch (ValidationException e) {
                stampaChiusuraBP.setErrorMessage(e.getMessage());
            }
        }
        return context.findDefaultForward();
    }

    private void validaModelPerCalcoloRimanenze(Chiusura_magazzinoBulk model) throws ValidationException, ApplicationException {
        if(model.getEsercizio() == null){
            throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
        }
        if(model.getDataInventario() == null){
            throw new it.cnr.jada.bulk.ValidationException("Impostare la data di fine periodo");
        }else{
            validaDataDiRiferimento(model.getDataInventario(),model.getEsercizio());
        }
    }

    private void validaDataDiRiferimento(Date dataRiferimento, Integer esercizioModel) throws  ValidationException {
        java.util.Calendar gc = java.util.Calendar.getInstance();
        gc.setTime(dataRiferimento);
        int annoCompetenza = gc.get(java.util.Calendar.YEAR);
        int esercizio = esercizioModel;

        if (annoCompetenza != esercizio) {
            throw new it.cnr.jada.bulk.ValidationException("La \"Data\" deve ricadere nell'esercizio selezionato!");
        }
    }
    private ChiusuraAnnoBulk verificaChiusuraAnno(ActionContext ac,Integer esercizio) throws ComponentException, PersistencyException, RemoteException, BusinessProcessException {
        StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(ac);
        ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

        return chiusuraAnnoComponent.verificaChiusuraAnno(ac.getUserContext(), esercizio, ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);

    }

    public Forward doPrint(ActionContext context) {

        StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP)getBusinessProcess(context);

        if(stampaChiusuraBP.isEffettuatoCalcolo()) {

            Forward forward = super.doPrint(context);
            return forward;
        }else{
            stampaChiusuraBP.setErrorMessage("Prima della stampa è necessario effettuare il calcolo delle rimanenze!");
        }
        return null;
    }


}
