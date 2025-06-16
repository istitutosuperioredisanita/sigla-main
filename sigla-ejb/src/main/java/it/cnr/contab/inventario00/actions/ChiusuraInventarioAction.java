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

import it.cnr.contab.config00.bp.HTTPSessionBP;
import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.inventario00.bp.CRUDChiusuraInventarioBP;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.docs.bulk.Chiusura_anno_inventarioBulk;
import it.cnr.contab.inventario00.ejb.AmmortamentoBeneComponentSession;
import it.cnr.contab.inventario00.ejb.AsyncAmmortamentoBeneComponentSession;
import it.cnr.contab.inventario00.ejb.V_AmmortamentoBeniDetComponentSession;
import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.ordmag.magazzino.bp.StampaChiusuraMagazzinoBP;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.Chiusura_magazzinoBulk;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.OptionBP;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
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

	public Forward doCalcolaAmmortamentoDefinitivo(ActionContext context) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException {
		return context.findDefaultForward();
	}

	public Forward doCalcolaAmmortamento(ActionContext context) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException {

			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			try {
				Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
				fillModel(context);

				ammortamentoBeni(context,chiusuraInventarioBP,model);


			} catch (ValidationException | BusyResourceException | InvocationTargetException | ParseException e) {
				chiusuraInventarioBP.setErrorMessage(e.getMessage());
			}

		return context.findDefaultForward();
	}

	private void ammortamentoBeni(ActionContext context, CRUDChiusuraInventarioBP chiusuraInventarioBP, Chiusura_anno_inventarioBulk model) throws ValidationException, ComponentException, BusinessProcessException, PersistencyException, RemoteException, BusyResourceException, InvocationTargetException, ParseException {

		validaModelPerAmmortamento(context,model);
		AsyncAmmortamentoBeneComponentSession ammortamentoBeneComponent = (AsyncAmmortamentoBeneComponentSession) chiusuraInventarioBP.createComponentSession(
				"CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession", AsyncAmmortamentoBeneComponentSession.class);

		String statoChiusura = ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO;
		boolean gestisciRipristinoInventario = false;
		boolean gestisciAggiornamentoInventario = false;
		boolean gestisciChiusura = true;

		// in fase di chiusura pre definitiva viene aggiornato l'inventario con l'imponibile ammortamento
		if(chiusuraInventarioBP.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") != null){
			statoChiusura = ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO;
			gestisciAggiornamentoInventario=true;
		}

		// Elimina chiusura precedente e crea nuova chiusura inventario
		eliminaECreaNuovaChiusuraInventario(context.getUserContext(),model.getAnno(),statoChiusura,null);

		ammortamentoBeneComponent.asyncAmmortamentoBeni(context.getUserContext(), model.getAnno(),statoChiusura,gestisciChiusura,gestisciRipristinoInventario,gestisciAggiornamentoInventario);
		verificaChiusuraAnno(context, model.getAnno());

	}
	private ChiusuraAnnoBulk eliminaECreaNuovaChiusuraInventario(UserContext uc,  Integer esercizio, String statoChiusuraInventario, BigDecimal pgJob ) throws ComponentException, PersistencyException, RemoteException, ParseException, BusyResourceException {
		ChiusuraAnnoComponentSession chiusuraAnnoComponent = Utility.createChiusuraAnnoComponentSession();
		ChiusuraAnnoBulk chiusuraAnno = null;
		if(chiusuraAnnoComponent.verificaChiusuraAnno(uc,esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO)!=null) {
			chiusuraAnno = chiusuraAnnoComponent.eliminaDatiChiusuraInventarioECreaNuovaChiusuraRequestNew(uc, esercizio, ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO,statoChiusuraInventario,pgJob);
		}
		else{
			chiusuraAnno = new ChiusuraAnnoBulk();
			chiusuraAnno.setPgChiusura(chiusuraAnnoComponent.getNuovoProgressivoChiusura(uc,chiusuraAnno));
			chiusuraAnno.setTipoChiusura(ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
			chiusuraAnno.setAnno(esercizio);
			chiusuraAnno.setStato(statoChiusuraInventario);
			chiusuraAnno.setDataCalcolo(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp());
			chiusuraAnno.setPg_job(pgJob);
			chiusuraAnno.setStato_job(Batch_log_tstaBulk.STATO_JOB_RUNNING);
			chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_CREATED);

			chiusuraAnnoComponent.creaConBulkRequiresNew(uc,chiusuraAnno);
		}
		return chiusuraAnno;
	}
	private void validaModelPerAmmortamento(ActionContext context,Chiusura_anno_inventarioBulk model) throws ValidationException, ComponentException, RemoteException, BusinessProcessException {
		if(model.getAnno()== null){
			throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
		}
		if(isEsercizioChiusoPerAlmenoUnCds(context,model.getAnno())){
			throw new it.cnr.jada.bulk.ValidationException("L'esercizio contabile selezionato risulta chiuso definitivamente.Non è più possibile procedere con l'ammortamento");
		}


	}
	public Forward doAnnullaChiusuraDefinitiva(ActionContext context){
		try {
			fillModel(context);
			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			if(chiusuraInventarioBP.getChiusuraAnno() != null) {
				return openConfirm(context, "Attenzione.Verrà annullata completamente la chiusura inventario ed il calcolo ammortiamento precedentemente effettuati. Procedere?", OptionBP.CONFIRM_YES_NO, "doConfirmAnnullaChiusuraDefinitiva");
			}else{
				return doConfirmAnnullaChiusuraDefinitiva(context,OptionBP.YES_BUTTON);
			}
		} catch(Exception e) {
			return handleException(context,e);
		}
	}
	public Forward doChiusuraDefinitiva(ActionContext context) {
		try {
			fillModel(context);
			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

			if(chiusuraInventarioBP.getChiusuraAnno() != null) {
				return openConfirm(context, "Si avvia la chiusura Inventario dell'anno contabile selezionato. Procedere?", OptionBP.CONFIRM_YES_NO, "doConfirmChiusuraDefinitiva");
			}else{
				return doConfirmChiusuraDefinitiva(context,OptionBP.YES_BUTTON);
			}
		} catch(Exception e) {
			return handleException(context,e);
		}
	}
	public Forward doConfirmAnnullaChiusuraDefinitiva(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException,ValidationException {

		if(i == OptionBP.YES_BUTTON) {

			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
			try {
				Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
				fillModel(context);

				if(model.getAnno() == null){
					throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
				}
				AmmortamentoBeneComponentSession ammortamentoBeneComponent = Utility.createAmmortamentoBeneComponentSession();
				ChiusuraAnnoComponentSession chiusuraAnnoComponent = Utility.createChiusuraAnnoComponentSession();

				ammortamentoBeneComponent.aggiornamentoInventarioBeneConAmmortamento(context.getUserContext(), model.getAnno(), Ammortamento_bene_invBulk.DECREMENTA_VALORE_AMMORTIZZATO);
				ammortamentoBeneComponent.cancellaAmmortamentiEsercizio(context.getUserContext(), model.getAnno());

				chiusuraAnnoComponent.eliminaDatiChiusuraInventario(context.getUserContext(), model.getAnno(),ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);

				chiusuraInventarioBP.setChiusuraAnno(null);

			} catch ( ComponentException e) {
				chiusuraInventarioBP.setErrorMessage(e.getMessage());
			}
		}
		return context.findDefaultForward();
	}

	public Forward doConfirmChiusuraDefinitiva(ActionContext context,int i) throws BusinessProcessException, FillException, ComponentException, PersistencyException, RemoteException,ValidationException {

		if(i == OptionBP.YES_BUTTON) {

			CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(context);
			try {
				Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
				fillModel(context);

				if(model.getAnno() == null){
					throw new it.cnr.jada.bulk.ValidationException("Impostare prima l'esercizio");
				}
				ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) chiusuraInventarioBP.createComponentSession(
						"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);
				ChiusuraAnnoBulk chiusuraAnnoBulk = chiusuraAnnoComponent.verificaChiusuraAnno(context.getUserContext(),model.getAnno(),ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
				chiusuraAnnoBulk.setStato(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO);
				chiusuraAnnoBulk.setDataCalcolo(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp());
				chiusuraAnnoBulk.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
				chiusuraAnnoComponent.modificaConBulk(context.getUserContext(), chiusuraAnnoBulk);
				chiusuraInventarioBP.setChiusuraAnno(chiusuraAnnoBulk);

			} catch ( ComponentException e) {
				chiusuraInventarioBP.setErrorMessage(e.getMessage());
			}
		}
		return context.findDefaultForward();
	}

	public Forward doOnAnnoChange(ActionContext actioncontext) throws FillException, ApplicationException {
		CRUDChiusuraInventarioBP chiusuraInventarioBP = (CRUDChiusuraInventarioBP) getBusinessProcess(actioncontext);

		try {
			Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) chiusuraInventarioBP.getModel();
			fillModel(actioncontext);

			verificaChiusuraAnno(actioncontext,model.getAnno());
			chiusuraInventarioBP.setEsercizioChiusoPerAlmenoUnCds(model.getAnno() != null ? isEsercizioChiusoPerAlmenoUnCds(actioncontext,model.getAnno()) : false);

			if(chiusuraInventarioBP.isEsercizioChiusoPerAlmenoUnCds()){
				throw new it.cnr.jada.bulk.ValidationException("L'esercizio contabile selezionato risulta chiuso definitivamente.Non è più possibile procedere con l'ammortamento");
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

	public Forward doAggiorna(ActionContext actioncontext) throws RemoteException {
		try{
			CRUDChiusuraInventarioBP bp = (CRUDChiusuraInventarioBP)actioncontext.getBusinessProcess();
			ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) bp.createComponentSession(
					"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);

			bp.setChiusuraAnno(chiusuraAnnoComponent.findByPrimaryKey(actioncontext.getUserContext(),bp.getChiusuraAnno()));
			return actioncontext.findDefaultForward();
		}catch(Throwable throwable){
			return handleException(actioncontext, throwable);
		}
	}

}
