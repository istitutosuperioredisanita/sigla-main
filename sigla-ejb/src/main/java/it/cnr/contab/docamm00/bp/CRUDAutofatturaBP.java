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

import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.util.action.SimpleCRUDBP;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * Gestisce le autofatture.
 */
public  class CRUDAutofatturaBP extends SimpleCRUDBP {

    private static final long serialVersionUID = 1L;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(CRUDAutofatturaBP.class);

    /**
     * CRUDAutofatturaBP constructor comment.
     */
    public CRUDAutofatturaBP(String function) throws BusinessProcessException {
        super(function);
    }

    public CRUDAutofatturaBP() {
        super();
    }

    private static final String[] TAB_AUTOFATTURA = new String[]{ "tabAutofattura","Testata","/docamm00/tab_autofattura.jsp" };
    private static final String[] TAB_CLIENTE = new String[]{ "tabCliente","Cliente","/docamm00/tab_autofattura_cliente.jsp" };

    //private static final String[] TAB_ALLEGATI = new String[]{ "tabFatturaAttivaAllegati","Allegati Aggiunti","/docamm00/tab_fattura_attiva_allegati.jsp" };

    public String[][] getTabs() {
        TreeMap<Integer, String[]> pages = new TreeMap<Integer, String[]>();
        int i = 0;
        pages.put(i++, TAB_AUTOFATTURA);
        pages.put(i++, TAB_CLIENTE);
    String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++)
            tabs[j] = new String[]{pages.get(j)[0], pages.get(j)[1], pages.get(j)[2]};
        return tabs;
    }



}
