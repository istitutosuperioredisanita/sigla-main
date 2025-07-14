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

package it.cnr.contab.docamm00.consultazioni.action;

import it.cnr.contab.docamm00.consultazioni.bp.ConfCostiCogeCofiBP;
import it.cnr.contab.docamm00.consultazioni.bp.ConsDocammAnagBP;
import it.cnr.contab.docamm00.consultazioni.bulk.VDocAmmAnagManrevBulk;
import it.cnr.contab.docamm00.consultazioni.bulk.V_confronta_costi_coge_cofiBulk;
import it.cnr.contab.doccont00.bp.AbstractFirmaDigitaleDocContBP;
import it.cnr.contab.doccont00.intcass.bulk.StatoTrasmissione;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.ConsultazioniAction;

public class ConfCostiCogeCofiAction extends ConsultazioniAction {


	public Forward doCambiaVisualizzazione(ActionContext context) {
		ConfCostiCogeCofiBP bp = (ConfCostiCogeCofiBP)context.getBusinessProcess();
		OggettoBulk bulk = bp.getModel();
		V_confronta_costi_coge_cofiBulk v_confronta_costi_coge_cofiBulk = ((V_confronta_costi_coge_cofiBulk)bulk);
		try {
			fillModel(context);
			String tipoVisualizzazione = v_confronta_costi_coge_cofiBulk.getTipo_visualizzazione();
			bulk = (OggettoBulk)bp.getBulkInfo().getBulkClass().newInstance();
			v_confronta_costi_coge_cofiBulk = ((V_confronta_costi_coge_cofiBulk)bulk);
			v_confronta_costi_coge_cofiBulk.setTipo_visualizzazione(tipoVisualizzazione);
			bp.setModel(context, bulk);
			bp.openIterator(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}
}