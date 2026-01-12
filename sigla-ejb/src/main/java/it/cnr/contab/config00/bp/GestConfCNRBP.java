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

package it.cnr.contab.config00.bp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.jsp.Button;

import java.util.Optional;
import java.util.stream.Stream;

public class GestConfCNRBP extends it.cnr.jada.util.action.SimpleCRUDBP {

public GestConfCNRBP() {
	super();
}

    public GestConfCNRBP(String s) {
        super(s);
    }

    protected void init(it.cnr.jada.action.Config config, ActionContext context) throws BusinessProcessException {
	try {
		super.init(config,context);
		this.setStatus(this.SEARCH);

        setFreeSearchSet("SEARCH_FORM");

        Configurazione_cnrBulk bulk = (Configurazione_cnrBulk)this.getModel();
        if(bulk!=null){
            bulk.setEsercizio(new Integer(0));
        }

        //setFreeSearchSet("CONS_CONF_TOTALE");
		//setColumns(getBulkInfo().getColumnFieldPropertyDictionary("CONS_CONF_TOTALE"));
	}catch(Throwable e) { 
		throw new BusinessProcessException(e);
	}
}
    @Override
    protected Button[] createToolbar() {
        Button[] abutton = new Button[5];
        int i = 0;
        abutton[i++] = new Button(Config.getHandler().getProperties(this.getClass()), "CRUDToolbar.search");
        abutton[i++] = new Button(Config.getHandler().getProperties(this.getClass()), "CRUDToolbar.startSearch");
        abutton[i++] = new Button(Config.getHandler().getProperties(this.getClass()), "CRUDToolbar.freeSearch");
        abutton[i++] = new Button(Config.getHandler().getProperties(this.getClass()), "CRUDToolbar.startLastSearch");
        abutton[i++] = new Button(Config.getHandler().getProperties(this.getClass()), "CRUDToolbar.save");

        return abutton;
    }



    public String getFormName() {
        return "SEARCH_FORM";
    }

//    @Override
//    public boolean isDeleteButtonHidden() {
//        return true;
//    }

//    @Override
//    public boolean isNewButtonHidden() {
//        return true;
//    }
}
