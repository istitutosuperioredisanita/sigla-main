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

package it.cnr.contab.prevent01.action;

import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.prevent01.bp.CRUDDettagliContrSpeseBP;
import it.cnr.contab.prevent01.bulk.Contrattazione_speseVirtualBulk;
import it.cnr.contab.prevent01.bulk.Pdg_approvato_dip_areaBulk;
import it.cnr.contab.prevent01.bulk.Pdg_contrattazione_speseBulk;
import it.cnr.contab.prevent01.ejb.PdgContrSpeseComponentSession;
import it.cnr.contab.progettiric00.core.bulk.Progetto_sipBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.ColumnFieldProperty;
import it.cnr.jada.ejb.BulkLoaderIterator;
import it.cnr.jada.excel.bp.OfflineExcelSpoolerBP;
import it.cnr.jada.excel.bulk.Excel_spoolerBulk;
import it.cnr.jada.excel.ejb.BframeExcelComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;

public class CRUDDettagliContrSpeseAction extends CRUDAction {
 
	public Forward doTab(ActionContext context, String tabName, String pageName) 
	{
		try
		{
			fillModel( context );
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)getBusinessProcess(context);
			Contrattazione_speseVirtualBulk contr_spese = (Contrattazione_speseVirtualBulk) bp.getModel();
			//bp.getModel().validate();
			if (bp.isDirty())
			{
				getBusinessProcess(context).setErrorMessage("Salvare le modifiche prima di cambiare pagina.");
				return context.findDefaultForward();
			}
			if (pageName.equals("tabDettagli")) {
				Pdg_approvato_dip_areaBulk pdg_dip_area = (Pdg_approvato_dip_areaBulk) bp.getCrudDettagliDipArea().getModel();
				if (pdg_dip_area==null) {
					getBusinessProcess(context).setErrorMessage("Selezionare una riga per visualizzare i dettagli.");
					return context.findDefaultForward();
				}
				if (!contr_spese.getDettagliContrSpese().isEmpty()) {
					if (!pdg_dip_area.equalsByPrimaryKey(((Pdg_contrattazione_speseBulk)contr_spese.getDettagliContrSpese().get(0)).getPdg_dip_area()))
						bp.caricaDettagli(context, contr_spese, pdg_dip_area);
				}
				else
					bp.caricaDettagli(context, contr_spese, pdg_dip_area);			
			}
		}		
		catch(Throwable e) 
		{
			return handleException(context,e);
		}
		return super.doTab(context, tabName, pageName);
	}

	public Forward doSearchSearchtool_progetto(ActionContext context) {
		CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();
		return search(context, getFormField(context, "main.Dettagli.searchtool_progetto"),bp.isFlNuovoPdg()?"progetto_liv2":null);
	}

	public Forward doSearchSearchtool_progetto_liv2(ActionContext context) {
		return doSearchSearchtool_progetto(context);
	}

	public Forward doBlankSearchSearchtool_progetto(ActionContext actioncontext, String s) {
		CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)actioncontext.getBusinessProcess();
		Pdg_contrattazione_speseBulk pdg_contr_spese = (Pdg_contrattazione_speseBulk)bp.getModel();
		pdg_contr_spese.setCdr(null);
		return super.doBlankSearch(actioncontext, s);
	}

	public Forward doBlankSearchSearchtool_progetto_liv2(ActionContext actioncontext, String s) {
		return doBlankSearchSearchtool_progetto(actioncontext, s);
	}

	public it.cnr.jada.action.Forward doBringBackSearchSearchtool_progetto(ActionContext context,Pdg_contrattazione_speseBulk pdg_contr_spese, Progetto_sipBulk progetto) {
		try {
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();

			if (progetto != null) {
				CdrBulk cdr = ((PdgContrSpeseComponentSession)bp.createComponentSession()).caricaCdrAfferenzaDaUo(context.getUserContext(), progetto.getUnita_organizzativa());
				pdg_contr_spese.setProgetto(progetto);
				pdg_contr_spese.setCdr(cdr);
			}
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}
	
	public it.cnr.jada.action.Forward doBringBackSearchSearchtool_progetto_liv2(ActionContext context,Pdg_contrattazione_speseBulk pdg_contr_spese, Progetto_sipBulk progetto) {
		return doBringBackSearchSearchtool_progetto(context, pdg_contr_spese, progetto);
	}

	public it.cnr.jada.action.Forward doApprova(ActionContext context) {
		try {
			fillModel( context );
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();
			if (bp.isDirty())
			{
				bp.setErrorMessage("Salvare le modifiche prima eseguire il comando richiesto.");
				return context.findDefaultForward();
			}
			bp.approvaDefinitivamente(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public it.cnr.jada.action.Forward doUndoApprova(ActionContext context) {
		try {
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();
			bp.undoApprovazioneDefinitiva(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doApprovaDettaglio(ActionContext context) {
		try {
			fillModel(context);
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)getBusinessProcess(context);
			if(bp.getCrudDettagliContrSpese().getSelection().isEmpty())
			{
				bp.setMessage(2, "E' necessario selezionare almeno un elemento");
				return context.findDefaultForward();
			} else
			{
				for (Iterator s = bp.getCrudDettagliContrSpese().getSelectedModels(context).iterator(); s.hasNext();) {
					Pdg_contrattazione_speseBulk pdgCont = (Pdg_contrattazione_speseBulk)s.next();
					pdgCont.setFl_approvato(Boolean.TRUE);
					pdgCont.setToBeUpdated();
				}
			}
			bp.setDirty(Boolean.TRUE);
			return context.findDefaultForward();
		}catch(java.lang.ClassCastException ex){
			return context.findDefaultForward();
		}catch(Throwable ex){
			return handleException(context, ex);
		}
	}

	public Forward doUndoApprovaDettaglio(ActionContext context) {
		try {
			fillModel(context);
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)getBusinessProcess(context);
			if(bp.getCrudDettagliContrSpese().getSelection().isEmpty())
			{
				bp.setMessage(2, "E' necessario selezionare almeno un elemento");
				return context.findDefaultForward();
			} else
			{
				for (Iterator s = bp.getCrudDettagliContrSpese().getSelectedModels(context).iterator(); s.hasNext();) {
					Pdg_contrattazione_speseBulk pdgCont = (Pdg_contrattazione_speseBulk)s.next();
					pdgCont.setFl_approvato(Boolean.FALSE);
					pdgCont.setToBeUpdated();
				}
			}
			bp.setDirty(Boolean.TRUE);
			return context.findDefaultForward();
		}catch(java.lang.ClassCastException ex){
			return context.findDefaultForward();
		}catch(Throwable ex){
			return handleException(context, ex);
		}
	}

	public it.cnr.jada.action.Forward doFilterCRUDMain_Dettagli(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		try {
			fillModel( context );
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();
			if (bp.isDirty())
			{
				bp.setErrorMessage("Salvare le modifiche prima eseguire il comando richiesto.");
				return context.findDefaultForward();
			}
			return filter(context, getController(context, "main.Dettagli"));
		} catch (Exception exception) {
			return handleException(context, exception);
		}
	}

	public Forward doRemoveFilterCRUDMain_Dettagli(ActionContext context) {
		try {
			fillModel(context);
			CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)context.getBusinessProcess();
			if (bp.isDirty())
			{
				bp.setErrorMessage("Salvare le modifiche prima eseguire il comando richiesto.");
				return context.findDefaultForward();
			}
			return removeFilter(context, getController(context, "main.Dettagli"));
		} catch (Exception exception) {
			return handleException(context, exception);
		}
	}
	public Forward doExcel(ActionContext actioncontext) {
		try {
			final CRUDDettagliContrSpeseBP  dettagliContrSpeseBP = (CRUDDettagliContrSpeseBP ) actioncontext.getBusinessProcess();
			Pdg_approvato_dip_areaBulk pdg_dip_area = (Pdg_approvato_dip_areaBulk) dettagliContrSpeseBP.getCrudDettagliDipArea().getModel();

			BframeExcelComponentSession bframeExcelComponentSession = (BframeExcelComponentSession)
					EJBCommonServices.createEJB("BFRAMEEXCEL_EJB_BframeExcelComponentSession");

			PdgContrSpeseComponentSession session = EJBCommonServices.createEJB("CNRPREVENT01_EJB_PdgContrSpeseComponentSession", PdgContrSpeseComponentSession.class);

			String longDescription = dettagliContrSpeseBP.getBulkInfo().getLongDescription();
			final RemoteIterator remoteiterator = session.findPdgContrSpeseDettagli(actioncontext.getUserContext(), pdg_dip_area,new CompoundFindClause());
			Query query = ((BulkLoaderIterator) remoteiterator).getQuery();
			BulkInfo bulkInfo = BulkInfo.getBulkInfo(Pdg_contrattazione_speseBulk.class);


			OrderedHashtable columnLabel = new OrderedHashtable();
			OrderedHashtable columnHeaderLabel = new OrderedHashtable();
			OrderedHashtable colonnedaEstrarre = new OrderedHashtable();
			for (Enumeration enumeration = bulkInfo.getColumnFieldPropertyDictionary().keys(); enumeration.hasMoreElements(); ) {
				String columnName = (String) enumeration.nextElement();
				colonnedaEstrarre.put(columnName, bulkInfo.getColumnFieldPropertyDictionary().get(columnName));
			}
			for (Enumeration enumeration = bulkInfo.getColumnFieldPropertyDictionary().elements(); enumeration.hasMoreElements(); ) {
				ColumnFieldProperty columnfieldproperty = (ColumnFieldProperty) enumeration.nextElement();
				columnLabel.put(columnfieldproperty, columnfieldproperty.getLabel());
				Optional.ofNullable(columnfieldproperty.getHeaderLabel())
						.ifPresent(s -> columnHeaderLabel.put(columnfieldproperty, s));
			}
			Excel_spoolerBulk bulk = bframeExcelComponentSession.addQueue(
					actioncontext.getUserContext(),
					columnLabel,
					columnHeaderLabel, longDescription, colonnedaEstrarre, query.toString(), null, query.getColumnMap(), new VpgBilRiclassificatoBulk());
			OfflineExcelSpoolerBP excelSpoolerBP = (OfflineExcelSpoolerBP) actioncontext.createBusinessProcess("OfflineExcelSpoolerBP");
			excelSpoolerBP.setModel(actioncontext, bulk);
			return actioncontext.addBusinessProcess(excelSpoolerBP);

		} catch (BusinessProcessException businessprocessexception) {
			return handleException(actioncontext, businessprocessexception);
		} catch (Throwable throwable) {
			return handleException(actioncontext, throwable);
		}
	}
}
