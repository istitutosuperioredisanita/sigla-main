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

package it.cnr.contab.inventario00.actions;

import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario00.bp.*;
import it.cnr.jada.action.*;

/**
 * Action per la gestione CRUD del Tipo Trasporto/Rientro
 **/
public class CRUDTipoTrasportoRientroAction extends it.cnr.jada.util.action.CRUDAction {

    /**
     * CRUDTipoTrasportoRientroAction constructor comment.
     */
    public CRUDTipoTrasportoRientroAction() {
        super();
    }

    /**
     * Viene richiamato nel momento in cui si seleziona il Flag TRASPORTO/RIENTRO nel
     * Tipo Trasporto Rientro.
     *
     * @param context il <code>ActionContext</code> che contiene le informazioni relative alla richiesta
     *
     * @return forward <code>Forward</code>
     **/
    public Forward doOnTiDocumentoChange(ActionContext context) {
        try {
            CRUDTipoTrasportoRientroBP bp = (CRUDTipoTrasportoRientroBP)getBusinessProcess(context);
            Tipo_trasporto_rientroBulk movimento = (Tipo_trasporto_rientroBulk)bp.getModel();
            String tiDocumento = movimento.getTiDocumento();

            fillModel(context);

            if (movimento.getTiDocumento() == null) {
                return context.findDefaultForward();
            }

            try {
                // Eventuali logiche future per ora setta solo il model con il tipo di movimento

                bp.setModel(context, movimento);
                return context.findDefaultForward();
            } catch(BusinessProcessException e) {
                movimento.setTiDocumento(tiDocumento);
                bp.setModel(context, movimento);
                throw e;
            }
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }
}