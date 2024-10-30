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

package it.cnr.contab.docamm00.bp;

import it.cnr.contab.docamm00.docs.bulk.AutofatturaBulk;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.jsp.Button;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Gestisce le autofatture.
 */
public  class CRUDAutofatturaBP extends AllegatiCRUDBP<AllegatoGenericoBulk, AutofatturaBulk> {

    private static final long serialVersionUID = 1L;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(CRUDAutofatturaBP.class);

    /**
     * CRUDAutofatturaBP constructor comment.
     */
    public CRUDAutofatturaBP(String function) throws BusinessProcessException {
        super(function);
    }


    protected void init(it.cnr.jada.action.Config config,
                        it.cnr.jada.action.ActionContext context)
            throws it.cnr.jada.action.BusinessProcessException {
        super.init(config, context);
        resetTabs();
    }
    protected it.cnr.jada.util.jsp.Button[] createToolbar() {
        Button[] buttons = super.createToolbar();
        List<Button> toolbar = new ArrayList<Button>();
        toolbar.addAll(Arrays.asList(buttons));
        toolbar.add(new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config
                .getHandler().getProperties(getClass()), "Toolbar.visuallizza.fattura"));

        return toolbar.toArray(new Button[toolbar.size()]);
    }
    @Override
    protected String getStorePath(AutofatturaBulk allegatoParentBulk, boolean create) throws BusinessProcessException {
        return allegatoParentBulk.getStorePath().get(0);
    }

    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }

    public CRUDAutofatturaBP() {
        super();
    }


    public OggettoBulk createNewSearchBulk(ActionContext context)
            throws BusinessProcessException {
        AutofatturaBulk fs = (AutofatturaBulk) super
                .createNewSearchBulk(context);
        fs.setFl_autofattura(Boolean.TRUE);
        return fs;
    }

    private static final String[] TAB_AUTOFATTURA = new String[]{ "tabAutofattura","Testata","/docamm00/tab_autofattura.jsp" };
    private static final String[] TAB_ALLEGATI = new String[]{ "tabAllegati","Allegati","/util00/tab_allegati.jsp"};


    public String[][] getTabs() {
        TreeMap<Integer, String[]> pages = new TreeMap<Integer, String[]>();
        int i = 0;
        pages.put(i++, TAB_AUTOFATTURA);
        AutofatturaBulk autofatturaBulk = ( AutofatturaBulk) this.getModel();
        if (Optional.ofNullable(autofatturaBulk.getPg_autofattura()).isPresent() && autofatturaBulk.getPg_autofattura()>0) {
            pages.put(i++,TAB_ALLEGATI);
        }

    String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++)
            tabs[j] = new String[]{pages.get(j)[0], pages.get(j)[1], pages.get(j)[2]};
        return tabs;
    }

    public void resetForSearch(ActionContext context) throws BusinessProcessException {

        super.resetForSearch(context);
        resetTabs();

    }


    public void resetTabs() {
        setTab("tab", "tabAutofattura");
    }
    public boolean isVisualizzaFatturaButtonEnabled() {
        AutofatturaBulk model = (AutofatturaBulk)getModel();
        return (model != null &&
                Optional.ofNullable(model.getFattura_passiva()).isPresent() &&
                Optional.ofNullable(model.getFattura_passiva().getPg_fattura_passiva()).isPresent());
    }



}
