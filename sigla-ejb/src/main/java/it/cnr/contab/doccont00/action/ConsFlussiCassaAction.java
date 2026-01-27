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

package it.cnr.contab.doccont00.action;


import it.cnr.contab.doccont00.bp.ConsFlussiCassaBP;
import it.cnr.contab.doccont00.consultazioni.bulk.FlussiDiCassaDtoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.SelezionatoreListaBP;

import javax.ejb.RemoveException;
import java.rmi.RemoteException;
import java.util.Optional;

public class ConsFlussiCassaAction extends BulkAction{

	public Forward doCercaFlussiCassa(ActionContext context) throws RemoteException, InstantiationException, RemoveException{
		it.cnr.jada.util.RemoteIterator ri =null;
			try {

				ConsFlussiCassaBP bp = (ConsFlussiCassaBP) context.getBusinessProcess();
				FlussiDiCassaDtoBulk flussoCassa = (FlussiDiCassaDtoBulk)bp.getModel();
				flussoCassa.setCdCds(flussoCassa.getCds().getCd_unita_organizzativa());
				if(bp.isRendiconto()){
					flussoCassa.setLivello(FlussiDiCassaDtoBulk.TERZO);
				}
				bp.fillModel(context);
				bp.validaRichiestaFlussi(flussoCassa);

				ri = bp.createComponentSession().findFlussiCassa(context.getUserContext(), flussoCassa);

				ri = it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(context,ri);
				if (ri.countElements() == 0) {
					it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(context,ri);
					throw new it.cnr.jada.comp.ApplicationException("Attenzione: Nessun dato disponibile");
				}
				SelezionatoreListaBP selezionatorelistabp = (SelezionatoreListaBP) context.createBusinessProcess("Selezionatore");
				selezionatorelistabp.setIterator(context, ri);
				selezionatorelistabp.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(FlussiDiCassaDtoBulk.class));
				if(!bp.isRendiconto()) {
					selezionatorelistabp.setColumns(selezionatorelistabp.getBulkInfo().getColumnFieldPropertyDictionary("FLUSSO_CASSA"));
				}else{
					selezionatorelistabp.setColumns(it.cnr.jada.bulk.BulkInfo.getBulkInfo(FlussiDiCassaDtoBulk.class).getColumnFieldPropertyDictionary("RENDICONTO"));
				}
				return context.addBusinessProcess(selezionatorelistabp);
			} catch (Exception e) {
					return handleException(context,e); 
			}
	}

}
