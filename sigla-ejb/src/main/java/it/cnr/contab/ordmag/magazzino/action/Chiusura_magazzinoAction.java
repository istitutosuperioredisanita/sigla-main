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
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.bulk.ValidationParseException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.OptionBP;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class Chiusura_magazzinoAction extends ParametricPrintAction {



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

                bp.setChiusuraAnno(this.verificaChiusuraAnno(actioncontext, model.getEsercizio()));

                // se presenti dati di chiusura abilita campi per filtri stampa
                if(bp.getChiusuraAnno()!=null){
                    model.setNascondiCampiStampa(false);
                }else{
                    model.setNascondiCampiStampa(true);
                }
                // gestione disabilitazione campi in fase DEFINITIVA
                if(bp.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null) {
                    // nella funzione definitiva sarà possibile stampare solo dopo aver fatto i calcoli definitivi quindi stato PREDEFINITIVO
                    if(bp.getChiusuraAnno() != null && bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)){
                        model.setNascondiCampiStampa(true);
                    }
                    model.setBloccaCampiCalcoloDefinitivo(true);
                }else {
                    // nella funzione provvisoria sarà possibile modificare i campi del calcolo solo in stato provvisorio altrimenti devono essere
                    // readonly (es. data fine periodo)
                    if(bp.getChiusuraAnno() != null && !bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)){
                        model.setBloccaCampiCalcoloDefinitivo(true);
                    }else {
                        model.setBloccaCampiCalcoloDefinitivo(false);
                    }
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


    public Forward doChiusuraDefinitiva(ActionContext context) {
        try {
            fillModel(context);
            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);

            if(stampaChiusuraBP.getChiusuraAnno() != null) {
                return openConfirm(context, "Si avvia la chiusura dei magazzini dell'anno contabile selezionato. Procedere?", OptionBP.CONFIRM_YES_NO, "doConfirmChiusuraDefinitiva");
            }else{
                return doConfirmChiusuraDefinitiva(context,OptionBP.YES_BUTTON);
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

    public Forward doConfirmChiusuraDefinitiva(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException,ValidationException {

        if(i == OptionBP.YES_BUTTON) {

            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);
            try {
                Chiusura_magazzinoBulk model = (Chiusura_magazzinoBulk) stampaChiusuraBP.getModel();
                fillModel(context);

                if(model.getEsercizio() == null){
                    throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
                }

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                model.setDataInventario(new java.sql.Timestamp(sdf.parse("31/12/"+model.getEsercizio()).getTime()));

                ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                        "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

                ChiusuraAnnoBulk chiusuraAnnoBulk = chiusuraAnnoComponent.salvaChiusuraDefinitiva(context.getUserContext(), model.getEsercizio(),ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO,model.getDataInventario());

                model.setBloccaCampiCalcoloDefinitivo(true);
                model.setNascondiCampiStampa(false);

                stampaChiusuraBP.setChiusuraAnno(chiusuraAnnoBulk);

            } catch ( ParseException e) {
                stampaChiusuraBP.setErrorMessage(e.getMessage());
            }
        }
        return context.findDefaultForward();
    }

    public Forward doAnnullaChiusuraDefinitiva(ActionContext context){
        try {
            fillModel(context);
            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);

            if(stampaChiusuraBP.getChiusuraAnno() != null) {
                return openConfirm(context, "Attenzione!Verrà annullata completamente la chiusura magazzino precedentemente effettuata.Procedere?", OptionBP.CONFIRM_YES_NO, "doConfirmAnnullaChiusuraDefinitiva");
            }else{
                return doConfirmAnnullaChiusuraDefinitiva(context,OptionBP.YES_BUTTON);
            }
        } catch(Exception e) {
            return handleException(context,e);
        }

    }
    public Forward doConfirmAnnullaChiusuraDefinitiva(ActionContext context,int i) {

        if(i == OptionBP.YES_BUTTON) {

            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);
            try {
                Chiusura_magazzinoBulk model = (Chiusura_magazzinoBulk) stampaChiusuraBP.getModel();
                fillModel(context);

                if(model.getEsercizio() == null){
                    throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
                }


                ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                        "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

                chiusuraAnnoComponent.annullaChiusuraDefinitiva(context.getUserContext(), model.getEsercizio(),ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);

                model.setBloccaCampiCalcoloDefinitivo(true);
                model.setNascondiCampiStampa(true);

                stampaChiusuraBP.setChiusuraAnno(null);

            } catch (ParseException | FillException | ValidationException | BusinessProcessException e) {
                stampaChiusuraBP.setErrorMessage(e.getMessage());
            } catch (ComponentException e) {
                e.printStackTrace();
            } catch (PersistencyException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return context.findDefaultForward();
    }


    public Forward doConfirmCalcolaRimanenze(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException, ValidationException {

       if(i == OptionBP.YES_BUTTON) {

            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);

            try {
                Chiusura_magazzinoBulk model = (Chiusura_magazzinoBulk) stampaChiusuraBP.getModel();
                fillModel(context);

                calcolaRimanenzeAnno(context.getUserContext(),stampaChiusuraBP,model);
            } catch (ValidationException e) {
                stampaChiusuraBP.setErrorMessage(e.getMessage());
            }
        }
        return context.findDefaultForward();
    }
    private void calcolaRimanenzeAnno(UserContext userContext,StampaChiusuraMagazzinoBP stampaChiusuraBP,Chiusura_magazzinoBulk model) throws ValidationException, ComponentException, BusinessProcessException, PersistencyException, RemoteException {
        String statoChiusura=ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO;

        if(stampaChiusuraBP.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null){
            statoChiusura = ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO;
        }

        validaModelPerCalcoloRimanenze(model);
        ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

        ChiusuraAnnoBulk chiusuraAnnoBulk = chiusuraAnnoComponent.calcolaRimanenzeAnno(userContext, model.getEsercizio(), model.getDataInventario(),statoChiusura);

        model.setNascondiCampiStampa(false);
        stampaChiusuraBP.setChiusuraAnno(chiusuraAnnoBulk);
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

        return super.doPrint(context);

    }
    public Forward doCalcolaRimanenzeDefinitive(ActionContext context) {
        try {
            fillModel(context);
            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);

            if(stampaChiusuraBP.getChiusuraAnno() != null) {
                return openConfirm(context, "Attenzione!Calcolo delle rimanenze definitivo, non sarà più possibile effettuare nuovi calcoli.Procedere?", OptionBP.CONFIRM_YES_NO, "doConfirmCalcolaRimanenzeDefinitiveConfirm");
            }else{
                return doConfirmCalcolaRimanenzeDefinitiveConfirm(context,OptionBP.YES_BUTTON);
            }
        } catch(Exception e) {
            return handleException(context,e);
        }
    }


    public Forward doConfirmCalcolaRimanenzeDefinitiveConfirm(ActionContext context,int i) {
        if(i == OptionBP.YES_BUTTON) {
            StampaChiusuraMagazzinoBP stampaChiusuraBP = (StampaChiusuraMagazzinoBP) getBusinessProcess(context);
            Chiusura_magazzinoBulk model = (Chiusura_magazzinoBulk) stampaChiusuraBP.getModel();

            try {
                fillModel(context);
                if (stampaChiusuraBP.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null) {

                    ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) stampaChiusuraBP.createComponentSession(
                            "CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

                    ChiusuraAnnoBulk chiusuraAnno = chiusuraAnnoComponent.verificaChiusuraAnno(context.getUserContext(), model.getEsercizio(), ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);

                    if (chiusuraAnno.getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        model.setDataInventario(new java.sql.Timestamp(sdf.parse("31/12/" + model.getEsercizio()).getTime()));

                        calcolaRimanenzeAnno(context.getUserContext(), stampaChiusuraBP, model);
                    }
                    model.setBloccaCampiCalcoloDefinitivo(true);
                }
            } catch (FillException | BusinessProcessException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            } catch (ComponentException e) {
                e.printStackTrace();
            } catch (PersistencyException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return context.findDefaultForward();
    }
}
