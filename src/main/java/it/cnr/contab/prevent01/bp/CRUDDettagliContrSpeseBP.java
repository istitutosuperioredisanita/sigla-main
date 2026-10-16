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

package it.cnr.contab.prevent01.bp;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import it.cnr.contab.config00.bulk.Parametri_cnrBulk;
import it.cnr.contab.config00.sto.bulk.DipartimentoBulk;
import it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.prevent01.bulk.Contrattazione_speseVirtualBulk;
import it.cnr.contab.prevent01.bulk.Pdg_approvato_dip_areaBulk;
import it.cnr.contab.prevent01.bulk.Pdg_contrattazione_speseBulk;
import it.cnr.contab.prevent01.ejb.PdgContrSpeseComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.action.SimpleDetailCRUDController;
import it.cnr.jada.util.jsp.Button;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;

public class CRUDDettagliContrSpeseBP extends SimpleCRUDBP {
	private boolean flNuovoPdg = false;

	private Unita_organizzativaBulk uoSrivania;
	private DipartimentoBulk dipartimentoSrivania;
	private Integer livelloContrattazione;
	private Boolean pdgApprovatoDefinitivo;

	private SimpleDetailCRUDController crudDettagliDipArea = new SimpleDetailCRUDController( "Testata", Pdg_approvato_dip_areaBulk.class, "dettagliDipArea", this, false) {

		public void validateForDelete(ActionContext context, OggettoBulk detail) throws ValidationException {
			super.validateForDelete(context, detail);
		}
		public boolean isFiltered()
		{
			return false;
		}
		public boolean isReadonly()
		{
			return super.isReadonly();
		}
		public boolean isGrowable()
		{
			return super.isGrowable();	
		}
		public boolean isShrinkable()
		{
			if (getModel()!=null && getModel().isNew())
				return super.isShrinkable();
			return false;
		}
	};

	private SimpleDetailCRUDController crudDettagliContrSpese = new SimpleDetailCRUDController( "Dettagli", Pdg_contrattazione_speseBulk.class, "dettagliContrSpese", this, true) {
		private CompoundFindClause filterController;

	    public int addDetail(OggettoBulk oggettobulk) throws BusinessProcessException {
	    	((Pdg_contrattazione_speseBulk)oggettobulk).setEsercizio(getModelPdg_dip_area().getEsercizio());
	    	((Pdg_contrattazione_speseBulk)oggettobulk).setPdg_dip_area(getModelPdg_dip_area());
	    	if (getModelPdg_dip_area()!= null && getModelPdg_dip_area().getArea()!=null &&
	    		getModelPdg_dip_area().getArea().getCd_unita_organizzativa()!=null)
   				((Pdg_contrattazione_speseBulk)oggettobulk).setArea(getModelPdg_dip_area().getArea());
	    	return super.addDetail(oggettobulk);
	    }
		public void validateForDelete(ActionContext context, OggettoBulk detail) throws ValidationException {
			Pdg_contrattazione_speseBulk pdgContr = (Pdg_contrattazione_speseBulk) detail;

			if (pdgContr.getTot_spese_decentr_int().compareTo(BigDecimal.ZERO)!=0 ||
					pdgContr.getTot_spese_decentr_est().compareTo(BigDecimal.ZERO)!=0 )
				throw new ValidationException("Non è possibile eliminare la riga selezionata. Se si desidera annullare la riga azzerare l'imnporto approvato.");
			super.validateForDelete(context, detail);
		}
		public boolean isReadonly()
		{
			return super.isReadonly();
		}
		public boolean isGrowable()
		{
			return super.isGrowable();	
		}
		public boolean isShrinkable()
		{
			if (getModel()!=null && getModel().isNew())
				return super.isShrinkable();
			return false;
		}

		/**
		 * Metodo per aggiungere alla toolbar del Controller un tasto necessario per apporre
		 * il visto da parte del dipartimento.
		 * @param context Il contesto dell'azione
		 */
		@Override
		public void writeHTMLToolbar(
				PageContext context,
				boolean reset,
				boolean find,
				boolean delete, boolean closedToolbar) throws java.io.IOException, ServletException {

			super.writeHTMLToolbar(context, reset, find, delete, false);

			if (getParentController() != null) {
				CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP) getParentController();
				if (!bp.getPdgApprovatoDefinitivo()) {
					String command = "javascript:submitForm('doApprovaDettaglio')";
					it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
							context,
							HttpActionContext.isFromBootstrap(context) ? "fa fa-share" : "img/properties16.gif",
							!(getDetails().isEmpty() || ((CRUDBP) getParentController()).isSearching()) ? command : null,
							true, "Approva",
							HttpActionContext.isFromBootstrap(context));

					String commandUndo = "javascript:submitForm('doUndoApprovaDettaglio')";
					it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
							context,
							HttpActionContext.isFromBootstrap(context) ? "fa fa-reply" : "img/history16.gif",
							!(getDetails().isEmpty() || ((CRUDBP) getParentController()).isSearching()) ? commandUndo : null,
							true,
							"Elimina approvazione",
							HttpActionContext.isFromBootstrap(context));
				}
			}
			super.closeButtonGROUPToolbar(context);
		}

		@Override
		public void setFilter(ActionContext actioncontext, CompoundFindClause compoundfindclause) {
			try {
				CRUDDettagliContrSpeseBP bp = (CRUDDettagliContrSpeseBP)actioncontext.getBusinessProcess();
				Contrattazione_speseVirtualBulk contr_spese = (Contrattazione_speseVirtualBulk)bp.getModel();
				Pdg_approvato_dip_areaBulk pdg_dip_area = (Pdg_approvato_dip_areaBulk) bp.getCrudDettagliDipArea().getModel();
				if (pdg_dip_area!=null)
					bp.caricaDettagli(actioncontext, contr_spese, pdg_dip_area, compoundfindclause);

				filterController = compoundfindclause;
			} catch (DetailedRuntimeException | BusinessProcessException e) {
                throw new RuntimeException(e);
            }
        }
		public boolean isFiltered() {
			return filterController != null;
		}
		public CompoundFindClause getFilter()
		{
			return filterController;
		}
		protected void clearFilter(ActionContext actioncontext)
		{
			clearFilter();
		}
		protected void clearFilter()
		{
			filterController = null;
		}
	};

	public CRUDDettagliContrSpeseBP()
	{
		super();
	}

	public CRUDDettagliContrSpeseBP(String s)
	{
		super(s);
	}

    private Pdg_approvato_dip_areaBulk getModelPdg_dip_area() {
		return (Pdg_approvato_dip_areaBulk) getCrudDettagliDipArea().getModel();
	}

	protected void initialize(ActionContext context) throws BusinessProcessException {
		try {
			super.initialize(context);
			setUoSrivania(it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context));
			if (it.cnr.contab.utenze00.bulk.CNRUserInfo.getDipartimento(context)!=null)
				setDipartimentoSrivania(it.cnr.contab.utenze00.bulk.CNRUserInfo.getDipartimento(context));
			setTab("tab","tabTestata");
			setPdgApprovatoDefinitivo(isApprovatoDefinitivo(context));
			Parametri_cnrBulk parCnr = Utility.createParametriCnrComponentSession().getParametriCnr(context.getUserContext(), CNRUserContext.getEsercizio(context.getUserContext())); 
			setFlNuovoPdg(parCnr.getFl_nuovo_pdg().booleanValue());
	
			try {	
				setLivelloContrattazione(((PdgContrSpeseComponentSession)createComponentSession()).livelloContrattazioneSpese(context.getUserContext()));
			}catch (RemoteException e) {
				throw new BusinessProcessException(e);
			} catch (ComponentException e) {
				throw new BusinessProcessException(e);
			} catch (PersistencyException e) {
				throw new BusinessProcessException(e);
			}
	
			if (getPdgApprovatoDefinitivo() || 
			    !it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context).getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
				setStatus(VIEW);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} 
	}

	public boolean isApprovatoDefinitivo(ActionContext context) throws BusinessProcessException {
		try {	
			PdgContrSpeseComponentSession session = (PdgContrSpeseComponentSession)createComponentSession();
			return session.isApprovatoDefinitivo(context.getUserContext());
		}catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	public Integer livelloContrattazioneSpese(ActionContext context) throws BusinessProcessException {
		try {	
			PdgContrSpeseComponentSession session = (PdgContrSpeseComponentSession)createComponentSession();
			return session.livelloContrattazioneSpese(context.getUserContext());
		}catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		} catch (PersistencyException e) {
			throw new BusinessProcessException(e);
		}
	}
	public boolean isContrSpeseEnabled() {
		return isContrSpeseAggiornabile() && isSaveButtonEnabled();
	}

	public SimpleDetailCRUDController getCrudDettagliContrSpese() {
		return crudDettagliContrSpese;
	}

	public void setCrudDettagliContrSpese(
			SimpleDetailCRUDController crudDettagliContrSpese) {
		this.crudDettagliContrSpese = crudDettagliContrSpese;
	}

	public SimpleDetailCRUDController getCrudDettagliDipArea() {
		return crudDettagliDipArea;
	}

	public void setCrudDettagliDipArea(
			SimpleDetailCRUDController crudDettagliDipArea) {
		this.crudDettagliDipArea = crudDettagliDipArea;
	}
	public Unita_organizzativaBulk getUoSrivania() {
		return uoSrivania;
	}

	public void setUoSrivania(Unita_organizzativaBulk bulk) {
		uoSrivania = bulk;
	}
	public boolean isContrSpeseAggiornabile() {
		Pdg_contrattazione_speseBulk bulk = (Pdg_contrattazione_speseBulk)getCrudDettagliContrSpese().getModel();
		return true;
	}
    public boolean isFreeSearchButtonHidden()
    {
        return true;
    }
	public boolean isStartSearchButtonHidden() 
	{
        return true;
	}
    public boolean isNewButtonHidden()
    {
        return true;
    }
    public boolean isDeleteButtonHidden()
    {
        return true;
    }
    public boolean isSearchButtonHidden()
    {
        return true;
    }

	public void caricaDettagli(ActionContext context, Contrattazione_speseVirtualBulk contr_spese, Pdg_approvato_dip_areaBulk pdg_dip_area) throws BusinessProcessException {
		this.caricaDettagli(context, contr_spese, pdg_dip_area, null);
	}

	public void caricaDettagli(ActionContext context, Contrattazione_speseVirtualBulk contr_spese, Pdg_approvato_dip_areaBulk pdg_dip_area, CompoundFindClause compoundFindClause) throws BusinessProcessException {
		try {

			PdgContrSpeseComponentSession session = (PdgContrSpeseComponentSession)createComponentSession();
			if (pdg_dip_area!=null) {
				contr_spese = (Contrattazione_speseVirtualBulk) session.inizializzaDettagliBulkPerModifica(context.getUserContext(), contr_spese, pdg_dip_area, compoundFindClause);
				setModel(context, contr_spese);
			}
		}catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	public Integer getLivelloContrattazione() {
		return livelloContrattazione;
	}

	private void setLivelloContrattazione(Integer livelloContrattazione) {
		this.livelloContrattazione = livelloContrattazione;
	}

	protected Button[] createToolbar() {
		Button[] toolbar = super.createToolbar();
		Button[] newToolbar = new Button[ toolbar.length + 2];
		int i;
		for ( i = 0; i < toolbar.length; i++ )
			newToolbar[i] = toolbar[i];
		newToolbar[ i ] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"CRUDToolbar.approva");
		newToolbar[ i+1 ] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"CRUDToolbar.undoApprova");
		return newToolbar;
	}
	
    public boolean isApprovaButtonHidden()
    {
        return getPdgApprovatoDefinitivo()||
        	  (getDipartimentoSrivania()!=null && getDipartimentoSrivania().getCd_dipartimento()!=null);
    }
    
    public boolean isApprovaButtonEnabled()
    {
        return getStatus()!=VIEW;
    }

    public boolean isUndoApprovaButtonHidden()
    {
        return !getPdgApprovatoDefinitivo()||
        	   (getDipartimentoSrivania()!=null && getDipartimentoSrivania().getCd_dipartimento()!=null);
    }

    public boolean isUndoApprovaButtonEnabled()
    {
        return getStatus()==VIEW && 
               getUoSrivania().getCd_tipo_unita().equalsIgnoreCase( Tipo_unita_organizzativaHome.TIPO_UO_ENTE );
    }

    public Boolean getPdgApprovatoDefinitivo() {
		return pdgApprovatoDefinitivo;
	}

	private void setPdgApprovatoDefinitivo(Boolean pdgApprovatoDefinitivo) {
		this.pdgApprovatoDefinitivo = pdgApprovatoDefinitivo;
	}

	public void approvaDefinitivamente(ActionContext context) throws BusinessProcessException {
		try {
			PdgContrSpeseComponentSession session = (PdgContrSpeseComponentSession)createComponentSession();
			session.approvaDefinitivamente(context.getUserContext());
			initialize(context);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	public void undoApprovazioneDefinitiva(ActionContext context) throws BusinessProcessException {
		try {
			PdgContrSpeseComponentSession session = (PdgContrSpeseComponentSession)createComponentSession();
			session.undoApprovazioneDefinitiva(context.getUserContext());
			initialize(context);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	private DipartimentoBulk getDipartimentoSrivania() {
		return dipartimentoSrivania;
	}

	private void setDipartimentoSrivania(DipartimentoBulk dipartimentoSrivania) {
		this.dipartimentoSrivania = dipartimentoSrivania;
	}
	public OggettoBulk initializeModelForSearch(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		setStatus(VIEW);
		return super.initializeModelForSearch(actioncontext, oggettobulk);
	}

	public void setFlNuovoPdg(boolean flNuovoPdg) {
		this.flNuovoPdg = flNuovoPdg;
	}
	public boolean isFlNuovoPdg() {
		return flNuovoPdg;
	}
}
