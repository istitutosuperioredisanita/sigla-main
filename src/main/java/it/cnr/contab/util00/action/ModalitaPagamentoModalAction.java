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
package it.cnr.contab.util00.action;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.CambiaModalitaPagamentoBulk;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.contab.docamm00.bp.CRUDDocumentoGenericoAttivoBP;
import it.cnr.contab.docamm00.bp.CRUDDocumentoGenericoPassivoBP;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.util00.bp.ModalBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.ejb.RicercaComponentSession;

import java.util.List;

public class ModalitaPagamentoModalAction extends ModalAction {
    public Forward doOnModalitaPagamentoChange(ActionContext context) {
        try {
            fillModel(context);
            ModalBP bp = (ModalBP) context.getBusinessProcess();
            final CambiaModalitaPagamentoBulk cambiaModalitaPagamentoBulk = (CambiaModalitaPagamentoBulk) bp.getModel();
            //visualizza la prima banca della lista
            if (cambiaModalitaPagamentoBulk.getModalita_pagamento() != null) {

                List<BancaBulk> bancaBulks = ((RicercaComponentSession)bp.createComponentSession("JADAEJB_CRUDComponentSession")).find(
                        context.getUserContext(),
                        BancaBulk.class,
                        "findBancaFor",
                        context.getUserContext(),
                        cambiaModalitaPagamentoBulk.getModalita_pagamento(),
                        cambiaModalitaPagamentoBulk.getTerzo().getCd_terzo()
                );
                cambiaModalitaPagamentoBulk.setBanca(bancaBulks.stream().findFirst().orElseThrow(() -> new ApplicationException("Non esistono i riferimenti associati!")));
            } else
                cambiaModalitaPagamentoBulk.setBanca(null);

            bp.setModel(context, cambiaModalitaPagamentoBulk);
        } catch (Throwable t) {
            return handleException(context, t);
        }

        return context.findDefaultForward();
    }

    public Forward doSearchListaBanche(ActionContext context) {
        ModalBP bp = (ModalBP) context.getBusinessProcess();
        final CambiaModalitaPagamentoBulk cambiaModalitaPagamentoBulk = (CambiaModalitaPagamentoBulk) bp.getModel();
        return search(context, getFormField(context, "main.listabanche"), cambiaModalitaPagamentoBulk.getModalita_pagamento().getTiPagamentoColumnSet());
    }

}