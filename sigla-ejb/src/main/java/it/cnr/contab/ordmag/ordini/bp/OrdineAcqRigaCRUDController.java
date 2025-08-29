/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.ordini.bp;

import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk;
import it.cnr.contab.ordmag.ordini.service.OrdineAcqCMISService;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;

import java.util.Optional;

import java.util.Optional;

/**
 * Riga del documento generico passivo
 */

public class OrdineAcqRigaCRUDController extends it.cnr.jada.util.action.SimpleDetailCRUDController {
    public OrdineAcqRigaCRUDController(String name, Class modelClass, String listPropertyName, it.cnr.jada.util.action.FormController parent) {
        super(name, modelClass, listPropertyName, parent);

    }

    protected OggettoBulk getDetail(int i) {
        OrdineAcqRigaBulk dettaglio=( OrdineAcqRigaBulk) super.getDetail(i);
        try {
            if (( !dettaglio.isToBeCreated()) &&  Optional.ofNullable(dettaglio.getArchivioAllegati()).orElse(new BulkList<>()).isEmpty()) {
                CRUDOrdineAcqBP crudOrdineAcqBP =(CRUDOrdineAcqBP) this.getParentController();
                dettaglio.setArchivioAllegati(((OrdineAcqCMISService) crudOrdineAcqBP.getStoreService()).recuperoAllegatiDettaglioOrdine(dettaglio));
            }
        } catch (BusinessProcessException e) {
            e.printStackTrace();
        }

        return dettaglio;
    }

    public boolean isGrowable() {

        OrdineAcqBulk ordine = (OrdineAcqBulk) getParentModel();
        return super.isGrowable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()).isSearching() &&
                !ordine.isAnnullato();
    }
/**
 * Insert the method's description here.
 * Creation date: (12/11/2001 4:23:46 PM)
 * @return boolean
 */
    /**
     * Restituisce true se è possibile aggiungere nuovi elementi
     */
    public boolean isShrinkable() {
        OrdineAcqBulk ordine = (OrdineAcqBulk) getParentModel();
        return super.isShrinkable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()).isSearching() &&
                !ordine.isAnnullato();
        //Tolto come da richiesta 423
        //&&
        //fatturaP.getProtocollo_iva() == null &&
        //fatturaP.getProtocollo_iva_generale() == null;
    }

    @Override
    public void writeHTMLToolbar(
            javax.servlet.jsp.PageContext context,
            boolean reset,
            boolean find,
            boolean delete, boolean closedToolbar) throws java.io.IOException, javax.servlet.ServletException {

        super.writeHTMLToolbar(context, reset, find, delete, false);

        if (getParentController()!=null && this.isAttivaFinanziaria()) {
            boolean isFromBootstrap = HttpActionContext.isFromBootstrap(context);
            String command = "javascript:submitForm('doRicercaObbligazione')";
            CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP) getParentController();
            it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                    context,
                    isFromBootstrap ? "fa fa-fw fa-bolt" : "img/history16.gif",
                    !(isInputReadonly() || getDetails().isEmpty() || bp.isSearching()) ? command : null,
                    true,
                    "Crea/Associa Impegni",
                    "btn-sm btn-outline-primary btn-title",
                    isFromBootstrap);
        }

        super.closeButtonGROUPToolbar(context);
    }


    @Override
    public boolean isBootstrap() {
        return super.isBootstrap();
    }

    private boolean isAttivaFinanziaria() {
        return Optional.ofNullable(this.getParentController()).filter(CRUDOrdineAcqBP.class::isInstance)
                .map(CRUDOrdineAcqBP.class::cast).map(CRUDOrdineAcqBP::isAttivaFinanziaria).orElse(Boolean.FALSE);
    }
}