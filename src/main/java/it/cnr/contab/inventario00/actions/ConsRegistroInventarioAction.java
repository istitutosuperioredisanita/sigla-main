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

/*
 * Created on Nov 9, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.inventario00.actions;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

import it.cnr.contab.anagraf00.bp.CRUDAnagraficaBP;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario00.bp.CRUDInventarioBeniBP;
import it.cnr.contab.inventario00.bp.ConsRegistroInventarioBP;
import it.cnr.contab.inventario00.consultazioni.bulk.V_cons_registro_inventarioBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.ConsultazioniAction;


public class ConsRegistroInventarioAction extends ConsultazioniAction {

	public Forward doConsulta(ActionContext context, String livelloDestinazione) {
		try {
			ConsRegistroInventarioBP bp = (ConsRegistroInventarioBP)context.getBusinessProcess();
			
			ConsRegistroInventarioBP consultazioneBP =(ConsRegistroInventarioBP) context.createBusinessProcess("ConsRegistroInventarioBP");
			bp.setPathConsultazione(livelloDestinazione);
			consultazioneBP.initVariabili(context, bp.getPathConsultazione(), livelloDestinazione);
			consultazioneBP.setIterator(context,consultazioneBP.createConsRegistroInventarioComponentSession().findConsultazione(context.getUserContext(),bp.getPathConsultazione(),consultazioneBP.getBaseclause(),bp.getFindclause()));			
			return context.addBusinessProcess(consultazioneBP);
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doConsultaQuote(ActionContext context) {	
		return doConsulta(context, ConsRegistroInventarioBP.QUOTE);
	}

	public Forward doCloseForm(ActionContext context) throws BusinessProcessException {
		Forward appoForward = super.doCloseForm(context);
		if (context.getBusinessProcess() instanceof ConsRegistroInventarioBP) {
			ConsRegistroInventarioBP bp = (ConsRegistroInventarioBP)context.getBusinessProcess();
			if (bp.getPathConsultazione().equals(bp.QUOTE)) 
				bp.initVariabili(context,bp.getPathConsultazione(),bp.VALORI);
			bp.setTitle();
		}
		return appoForward;
	}

	public Forward doApriTerzoAssegnatario(ActionContext context) throws BusinessProcessException {
		try {
			ConsRegistroInventarioBP bp = (ConsRegistroInventarioBP)context.getBusinessProcess();
			fillModel(context);
			TerzoBulk terzo = Optional.ofNullable(bp.getSelectedElements(context))
					.filter(list -> list.size() == 1)
					.map(List::getFirst)
					.filter(V_cons_registro_inventarioBulk.class::isInstance)
					.map(V_cons_registro_inventarioBulk.class::cast)
					.filter(v -> Optional.ofNullable(v.getCd_assegnatario()).isPresent())
					.map(v ->  {
						TerzoBulk t = new TerzoBulk(Integer.valueOf(v.getCd_assegnatario()));
						try {
							return (TerzoBulk)Utility.createCRUDComponentSession().findByPrimaryKey(context.getUserContext(), t);
						} catch (ComponentException | RemoteException e) {
							throw new RuntimeException(e);
						}
					})
					.orElseThrow(()-> new ApplicationException("Selezionare una riga con l'assegnatario valorizzato!"));


			CRUDAnagraficaBP nbp = (CRUDAnagraficaBP) context.createBusinessProcess("CRUDAnagraficaBP", new Object[]{"M"});
			nbp.basicEdit(context, terzo.getAnagrafico(), true);
			return context.addBusinessProcess(nbp);
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doApriGestioneBene(ActionContext context) throws BusinessProcessException {
		try {
			ConsRegistroInventarioBP bp = (ConsRegistroInventarioBP)context.getBusinessProcess();
			fillModel(context);
			Inventario_beniBulk inventarioBeniBulk = Optional.ofNullable(bp.getSelectedElements(context))
					.filter(list -> list.size() == 1)
					.map(List::getFirst)
					.filter(V_cons_registro_inventarioBulk.class::isInstance)
					.map(V_cons_registro_inventarioBulk.class::cast)
					.filter(v -> Optional.ofNullable(v.getNr_inventario()).isPresent())
					.map(v ->  {
						Inventario_beniBulk t = new Inventario_beniBulk(v.getNr_inventario(), v.getPg_inventario(), Long.valueOf(v.getProgressivo()));
						try {
							return (Inventario_beniBulk)Utility.createCRUDComponentSession().findByPrimaryKey(context.getUserContext(), t);
						} catch (ComponentException | RemoteException e) {
							throw new RuntimeException(e);
						}
					})
					.orElseThrow(()-> new ApplicationException("Selezionare una riga con il bene valorizzato!"));

			CRUDInventarioBeniBP nbp = (CRUDInventarioBeniBP) context.createBusinessProcess("CRUDInventarioBeniBP", new Object[]{"M"});
			nbp.basicEdit(context, inventarioBeniBulk, true);
			return context.addBusinessProcess(nbp);
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}
}
