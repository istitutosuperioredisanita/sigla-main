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
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.bp.CRUDChiusuraInventarioBP;
import it.cnr.contab.inventario00.bp.CRUDInventarioBeniBP;
import it.cnr.contab.inventario00.consultazioni.bulk.V_cons_registro_inventarioBulk;
import it.cnr.contab.inventario00.docs.bulk.*;
import it.cnr.contab.inventario00.ejb.AmmortamentoBeneComponentSession;
import it.cnr.contab.inventario00.ejb.AsyncAmmortamentoBeneComponentSession;
import it.cnr.contab.inventario00.ejb.Inventario_beniComponentSession;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
import it.cnr.contab.ordmag.magazzino.bp.StampaChiusuraMagazzinoBP;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.Chiusura_magazzinoBulk;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.bulk.ValidationParseException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.OptionBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
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


	public Forward doCalcolaAmmortamento(ActionContext context) {
		try {
			fillModel(context);
			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			if(chiusuraInventarioBP.isCalcoloAmmortamentoEffettuato()) {
				return openConfirm(context, "Attenzione!Ammortamento già effettuato.Procedere con un ammortamento?", OptionBP.CONFIRM_YES_NO, "doConfirmAmmortamento");
			}else{
				return doConfirmAmmortamento(context,OptionBP.YES_BUTTON);
			}
		} catch(Exception e) {
			return handleException(context,e);
		}
	}
	public Forward doConfirmAmmortamento(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException, ValidationException {

		if(i == OptionBP.YES_BUTTON) {

			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			try {
				Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
				fillModel(context);

				ammortamentoBeni(context,chiusuraInventarioBP,model);
				chiusuraInventarioBP.setMessage("Ammortamento "+model.getAnno()+" terminata correttamente");

			} catch (ValidationException e) {
				chiusuraInventarioBP.setErrorMessage(e.getMessage());
			}
		}
		return context.findDefaultForward();
	}
	private void ammortamentoBeni(ActionContext context, CRUDChiusuraInventarioBP chiusuraInventarioBP, Chiusura_anno_inventarioBulk model) throws ValidationException, ComponentException, BusinessProcessException, PersistencyException, RemoteException {

		validaModelPerAmmortamento(context,model);
		AsyncAmmortamentoBeneComponentSession ammortamentoBeneComponent = (AsyncAmmortamentoBeneComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession", AsyncAmmortamentoBeneComponentSession.class);

		ammortamentoBeneComponent.asyncAmmortamentoBeni(context.getUserContext(), model.getAnno());
		chiusuraInventarioBP.setCalcoloAmmortamentoEffettuato(true);
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

			// verifica se esiste una chiusura anno per l'esercizio così da abilitare la stampa anche se esercizio chiuso
			chiusuraInventarioBP.setAbilitaStampa(isAnnoChiusuraInventario(actioncontext,model.getAnno() ));
			chiusuraInventarioBP.setEsercizioChiusoPerAlmenoUnCds(model.getAnno() != null ? isEsercizioChiusoPerAlmenoUnCds(actioncontext,model.getAnno()) : false);

			if(chiusuraInventarioBP.isEsercizioChiusoPerAlmenoUnCds()){
				throw new it.cnr.jada.bulk.ValidationException("L'esercizio contabile selezionato risulta chiuso definitivamente.Non è più possibile procedere con l'ammortamento");
			}else{
				chiusuraInventarioBP.setCalcoloAmmortamentoEffettuato(isCalcoloAmmortamentoEffettutato(actioncontext,model.getAnno()));
			}
			return actioncontext.findDefaultForward();
		} catch (Throwable e) {
			return handleException(actioncontext, e);
		}
	}
	private boolean isAnnoChiusuraInventario(ActionContext context,Integer esercizio) throws BusinessProcessException, ComponentException, PersistencyException, RemoteException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
		ChiusuraAnnoComponentSession chiusuraComponent = (ChiusuraAnnoComponentSession)chiusuraInventarioBP.createComponentSession(
				"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

		ChiusuraAnnoBulk chiusura = chiusuraComponent.verificaChiusuraAnno(context.getUserContext(),esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
		return chiusura!=null?true:false;
	}
	private boolean isEsercizioChiusoPerAlmenoUnCds(ActionContext context,Integer esercizio) throws ComponentException, RemoteException, BusinessProcessException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
		EsercizioComponentSession esercizioComponent = (EsercizioComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRCONFIG00_EJB_EsercizioComponentSession", EsercizioComponentSession.class);

		return esercizioComponent.isEsercizioSpecificoChiusoPerAlmenoUnCds(context.getUserContext(),esercizio);
	}
	private boolean isCalcoloAmmortamentoEffettutato(ActionContext context,Integer esercizio) throws RemoteException, InvocationTargetException {

		AmmortamentoBeneComponentSession ammortamentoBeneComponent = Utility.createAmmortamentoBeneComponentSession();
		List<Ammortamento_bene_invBulk> listaAmmortamenti = ammortamentoBeneComponent.findAllAmmortamenti(context.getUserContext(), esercizio);
		if(listaAmmortamenti != null && !listaAmmortamenti.isEmpty()){
			return true;
		}
		return false;

	}
}
