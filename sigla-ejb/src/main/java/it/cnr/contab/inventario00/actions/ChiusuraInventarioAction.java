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

import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.inventario00.bp.CRUDChiusuraInventarioBP;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.docs.bulk.Chiusura_anno_inventarioBulk;
import it.cnr.contab.inventario00.ejb.AmmortamentoBeneComponentSession;
import it.cnr.contab.inventario00.ejb.AsyncAmmortamentoBeneComponentSession;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.OptionBP;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Insert the type's description here.
 * Creation date: (16/11/2001 13.09.00)
 * @author: Roberto Fantino
 */
public class ChiusuraInventarioAction extends ParametricPrintAction {
	public Forward doPrint(ActionContext context) {

		CRUDChiusuraInventarioBP bp = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

		try {
			Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) bp.getModel();
			fillModel(context);

			ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) bp.createComponentSession(
					"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);
			ChiusuraAnnoBulk chiusuraAnno =  chiusuraAnnoComponent.verificaChiusuraAnno(context.getUserContext(),model.getAnno(),ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);

			if(chiusuraAnno!= null) {
				model.setPgChiusura(chiusuraAnno.getPgChiusura());
			}else{
				bp.setErrorMessage("Nessuna chiusura da stampare per l'anno selezionato");
				return context.findDefaultForward();
			}

		} catch (FillException | BusinessProcessException e) {
			bp.setErrorMessage(e.getMessage());
		} catch (ComponentException e) {
			e.printStackTrace();
		} catch (PersistencyException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return super.doPrint(context);
	}
	protected ParametricPrintBP getBusinessProcess(ActionContext actioncontext) {
		return (ParametricPrintBP)actioncontext.getBusinessProcess();
	}


	public Forward doCalcolaAmmortamento(ActionContext context) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException {

			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			try {
				Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
				fillModel(context);
				//chiusuraInventarioBP.setMessage("Procedura di Ammortamento esercizio "+model.getAnno()+" avviata.Per stato avanzamento consultare 'Log Applicativi' ");
				ammortamentoBeni(context,chiusuraInventarioBP,model);


			} catch (ValidationException e) {
				chiusuraInventarioBP.setErrorMessage(e.getMessage());
			}

		return context.findDefaultForward();
	}

	private void ammortamentoBeni(ActionContext context, CRUDChiusuraInventarioBP chiusuraInventarioBP, Chiusura_anno_inventarioBulk model) throws ValidationException, ComponentException, BusinessProcessException, PersistencyException, RemoteException {

		validaModelPerAmmortamento(context,model);
		AsyncAmmortamentoBeneComponentSession ammortamentoBeneComponent = (AsyncAmmortamentoBeneComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession", AsyncAmmortamentoBeneComponentSession.class);

		ammortamentoBeneComponent.asyncAmmortamentoBeni(context.getUserContext(), model.getAnno(),ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO,true);

	}
	private void validaModelPerAmmortamento(ActionContext context,Chiusura_anno_inventarioBulk model) throws ValidationException, ComponentException, RemoteException, BusinessProcessException {
		if(model.getAnno()== null){
			throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
		}
		if(isEsercizioChiusoPerAlmenoUnCds(context,model.getAnno())){
			throw new it.cnr.jada.bulk.ValidationException("L'esercizio contabile selezionato risulta chiuso definitivamente.Non è più possibile procedere con l'ammortamento");
		}


	}

	public Forward doOnAnnoChange(ActionContext actioncontext) throws FillException, ApplicationException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(actioncontext);

		try {
			Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
			fillModel(actioncontext);

			chiusuraInventarioBP.setEsercizioChiusoPerAlmenoUnCds(model.getAnno() != null ? isEsercizioChiusoPerAlmenoUnCds(actioncontext,model.getAnno()) : false);

			if(chiusuraInventarioBP.isEsercizioChiusoPerAlmenoUnCds()){
				throw new it.cnr.jada.bulk.ValidationException("L'esercizio contabile selezionato risulta chiuso definitivamente.Non è più possibile procedere con l'ammortamento");
			}else{

				verificaChiusuraAnno(actioncontext,model.getAnno());

				if(chiusuraInventarioBP.getChiusuraAnno() != null){
					chiusuraInventarioBP.setMessage("Ammortamento già presente per l'esercizio selezionato. Se si procede con un nuovo ammortamento" +
							"il precedente verrà cancellato e ricalcolato il nuovo. Durante il calcolo sarà possibile procedere con il lavoro e " +
							"consultare lo stato avanzamento della procedura ammortamento accedendo a  'Funzionalità di servizio/ Log Applicativi' ");
				}
			}

			return actioncontext.findDefaultForward();
		} catch (Throwable e) {
			return handleException(actioncontext, e);
		}
	}

	private boolean isEsercizioChiusoPerAlmenoUnCds(ActionContext context,Integer esercizio) throws ComponentException, RemoteException, BusinessProcessException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
		EsercizioComponentSession esercizioComponent = (EsercizioComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRCONFIG00_EJB_EsercizioComponentSession", EsercizioComponentSession.class);

		return esercizioComponent.isEsercizioSpecificoChiusoPerAlmenoUnCds(context.getUserContext(),esercizio);
	}
	private void verificaChiusuraAnno(ActionContext context,Integer esercizio) throws RemoteException, InvocationTargetException, BusinessProcessException, ComponentException, PersistencyException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

		ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

		chiusuraInventarioBP.setChiusuraAnno(chiusuraAnnoComponent.verificaChiusuraAnno(context.getUserContext(), esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO));

	}
	private boolean isExistAmmortamentoEsercizio(ActionContext context,Integer esercizio) throws RemoteException, InvocationTargetException, BusinessProcessException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
		AmmortamentoBeneComponentSession ammortamentoBeneComponent = (AmmortamentoBeneComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRINVENTARIO00_EJB_AmmortamentoBeneComponentSession", AmmortamentoBeneComponentSession.class);

		return ammortamentoBeneComponent.isExistAmmortamentoEsercizio(context.getUserContext(), esercizio);
	}
}
