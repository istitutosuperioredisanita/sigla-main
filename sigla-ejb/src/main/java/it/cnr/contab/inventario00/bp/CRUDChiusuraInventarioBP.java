/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.inventario00.bp;

import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.util.jsp.Button;

public class CRUDChiusuraInventarioBP extends ParametricPrintBP {

    private boolean isEsercizioChiusoPerAlmenoUnCds;

    private ChiusuraAnnoBulk chiusuraAnno;


    public CRUDChiusuraInventarioBP() {
    }

    public CRUDChiusuraInventarioBP(String function) {
        super(function);
    }

    protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        try {
            super.init(config, context);

            setBulkClassName(config.getInitParameter("bulkClassName"));
            setComponentSessioneName(config.getInitParameter("componentSessionName"));
            if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null) {
                this.getBulkInfo().setShortDescription("Chiusura Inventario Provvisoria - calcolo ammortamento");
            }else{
                this.getBulkInfo().setShortDescription("Chiusura Inventario Definitiva - calcolo ammortamento");
            }


        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Non trovata la classe bulk");
        }
        super.init(config,context);
    }

    @Override
    protected void initialize(ActionContext context) throws BusinessProcessException {
        super.initialize(context);
    }

    public boolean isPrintButtonHidden(){
        // se procedura ammortamento terminata (presente la chiusura dell'inventario con stato job ammortamento completato)
        if(this.getChiusuraAnno() != null && this.getChiusuraAnno().getStato_job() != null &&
                this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE))
        {
            return false;
        }
        return true;
    }

    public Button[] createToolbar() {



        Button[] toolbar = null;

        if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null) {
            toolbar=new Button[3];
        }else{
            toolbar=new Button[5];
        }

        int i = 0;

        toolbar[i++] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "Toolbar.print");
        toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.calcolaAmm");
        toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.refresh");

        if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null) {
            toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.chiusuraDef");
            toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.undochiusuraDef");
        }

        return toolbar;
    }
    public boolean isAnnullaChiusuraDefinitivaButtonHidden(){

        // se esercizio chiuso disabilita pulsante annulla chiusura definitiva
        if(this.isEsercizioChiusoPerAlmenoUnCds){
            return true;
        }
        // se non effettuata nessuna chiusura disabilita pulsante chiusura definitiva
        if(this.getChiusuraAnno()== null )
        {
            return true;
        }
        // se stato diverso da DEFINITIVO o stato job diverso da completo disabilita chiusura definitiva
        if((!this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO) &&
           !this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO)) ||
           !this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE))
        {
            return true;
        }
        return false;
    }
    public boolean isChiusuraDefinitivaButtonHidden(){
        // se esercizio chiuso disabilita pulsante chiusura definitiva
        if(this.isEsercizioChiusoPerAlmenoUnCds){
            return true;
        }
        // se non effettuata nessuna chiusura disabilita pulsante chiusura definitiva
        if(this.getChiusuraAnno()== null )
        {
            return true;
        }
        // se stato diverso da PREDEFINITIVO o stato job diverso da completo disabilita chiusura definitiva
        if(!this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO) ||
           !this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE))
        {
            return true;
        }
        return false;
    }
    public boolean isAggiornaButtonHidden(){
        if(this.getChiusuraAnno() == null ||
         ((this.getChiusuraAnno().getStato_job()==null || !this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_RUNNING))))
        {
            return true;
        }
        return false;
    }

    public boolean isCalcoloButtonHidden()
    {
        // se esercizio chiuso disabilita pulsante calcolo
        if(this.isEsercizioChiusoPerAlmenoUnCds){
            return true;
        }
        if(this.getModel()== null || ((Chiusura_anno_inventarioBulk) this.getModel()).getAnno() == null ){
            return true;
        }

        if(this.getChiusuraAnno()!= null )
        {
            // se Job in stato RUNNING disabilita pulsante calcolo
            if(this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_RUNNING)){
                return true;
            }
            // se chiusura inventario Definitiva
            if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null){
                // se lo stato della chiusura è Provvisorio ma il Job non è in stato Completato
                // oppure se effettuato calcolo predefinitivo viene disabilito il pulsante calcolo
                if((this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO) &&
                    !this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE))
                                                    ||
                    !this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO))
                {
                    return true;
                }
            }
            // se chiusura inventario Provvisoroa
            else{
                // se effettuata già chiusura predefinitiva o definitiva disabilita pulsante calcolo
                if(this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO) ||
                   this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO))
                {
                    return true;
                }
            }
        }
        else{
            // se ancora nessuna chiusura presente e chiusura definitiva disabilita pulsante calcoli
            if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null){
                return true;
            }
        }
        return false;
    }

    public boolean isEsercizioChiusoPerAlmenoUnCds() {
        return isEsercizioChiusoPerAlmenoUnCds;
    }

    public void setEsercizioChiusoPerAlmenoUnCds(boolean esercizioChiusoPerAlmenoUnCds) {
        isEsercizioChiusoPerAlmenoUnCds = esercizioChiusoPerAlmenoUnCds;
    }


    public ChiusuraAnnoBulk getChiusuraAnno() {
        return chiusuraAnno;
    }

    public void setChiusuraAnno(ChiusuraAnnoBulk chiusuraAnno) {
        this.chiusuraAnno = chiusuraAnno;
    }
}

