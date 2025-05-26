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

            this.getBulkInfo().setShortDescription("Chiusura Inventario - calcolo ammortamento");


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
        if(this.getChiusuraAnno() != null && this.getChiusuraAnno().getStato_job() != null && this.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE))
        {
            return false;
        }
        return true;
    }

    public Button[] createToolbar() {
        Button[] baseToolbar = super.createToolbar();


        Button[] toolbar = null;

        toolbar=new Button[3];

        int i = 0;
        for (Button button : baseToolbar) {
            toolbar[i++] = button;
        }
        toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.calcolaAmm");
        return toolbar;
    }

    public boolean isCalcoloButtonHidden()
    {
        if(this.isEsercizioChiusoPerAlmenoUnCds){
            return true;
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

