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

package it.cnr.contab.docamm00.actions;

import it.cnr.contab.docamm00.bp.StornaDocumentoGenericoBP;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.OptionBP;
import it.cnr.jada.util.action.SimpleCRUDBP;

import java.rmi.RemoteException;
import java.util.Optional;

public class StornaDocumentoGenericoAction extends BulkAction {

    public Forward doAnnulla(ActionContext actioncontext) throws BusinessProcessException {
        return doCloseForm(actioncontext);
    }

    public Forward doConferma(ActionContext actioncontext) {
        try {
            StornaDocumentoGenericoBP stornaDocumentoGenericoBP = (StornaDocumentoGenericoBP) actioncontext.getBusinessProcess();
            char tiEntrataSpesa = stornaDocumentoGenericoBP.getTiEntrataSpesa();
            fillModel(actioncontext);
            Documento_genericoBulk documentoGenericoBulk = stornaDocumentoGenericoBP.generaStorno(actioncontext);
            actioncontext.closeBusinessProcess();
            OptionBP optionBP = openConfirm(
                    actioncontext,
                    String.format(
                            "É stato creato il documento generico numero <span class=\"h5\">%s</span><p>Si desidera visualizzare il documento creato?</p>",
                            documentoGenericoBulk.getPg_documento_generico()
                    ),
                    OptionBP.CONFIRM_YES_NO,
                    "doAfterCreateDocument");
            optionBP.addAttribute("documentoGenerico", documentoGenericoBulk);
            optionBP.addAttribute("tiEntrataSpesa", tiEntrataSpesa);
            return optionBP;
        } catch (FillException | BusinessProcessException e) {
            return handleException(actioncontext, e);
        }
    }

    public Forward doAfterCreateDocument(ActionContext actioncontext, OptionBP optionBP) throws RemoteException {
        if (optionBP.getOption() == OptionBP.YES_BUTTON) {
            Documento_genericoBulk documentoGenericoBulk = (Documento_genericoBulk) optionBP.getAttribute("documentoGenerico");
            Character tiEntrataSpesa = (Character) optionBP.getAttribute("tiEntrataSpesa");
            try {
                SimpleCRUDBP nbp = (SimpleCRUDBP) actioncontext.createBusinessProcess(
                        tiEntrataSpesa.equals('E')?"CRUDGenericoAttivoBP":"CRUDGenericoPassivoBP",
                        new Object[]{"V"}
                );
                nbp = (SimpleCRUDBP) actioncontext.addBusinessProcess(nbp);
                nbp.edit(actioncontext, documentoGenericoBulk);
                return nbp;
            } catch (Throwable e) {
                return handleException(actioncontext, e);
            }
        } else {
            return Optional.ofNullable(actioncontext.findForward("close"))
                    .orElse(super.doDefault(actioncontext));
        }
    }

}
