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
 * Created on Apr 12, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.config00.bp;

import java.rmi.RemoteException;
import java.util.Enumeration;

import it.cnr.contab.config00.ejb.*;
import it.cnr.contab.config00.pdcep.cla.bulk.*;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteBulkTree;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.action.SimpleDetailCRUDController;

import javax.ejb.EJBException;

/**
 * @author rpagano
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CRUDClassificazioneVociEPBP extends SimpleCRUDBP {
	private BulkList bulkTab = new BulkList();
	private int[] focusTab = {-1,-1,-1,-1,-1,-1,-1,-1};
	private CrudAssLivelli[] crudAssLivelli = {null,null,null,null,null,null,null,null};
	private String[][] tabs = null;
	public Parametri_livelli_epBulk parametriLivelli;
			
	public CRUDClassificazioneVociEPBP()
	{
		super();
	}

	public CRUDClassificazioneVociEPBP(String s)
	{
		super(s);
	}

	public String getLabelCd_livello0(){
		return "Tipo Classificazione";
	}

	public String getLabelCd_livello1(){
		return getParametriLivelli().getDs_livello1();
	}

	public String getLabelCd_livello2(){
		return getParametriLivelli().getDs_livello2();
	}

	public String getLabelCd_livello3(){
		return getParametriLivelli().getDs_livello3();
	}

	public String getLabelCd_livello4(){
		return getParametriLivelli().getDs_livello4();
	}

	public String getLabelCd_livello5(){
		return getParametriLivelli().getDs_livello5();
	}

	public String getLabelCd_livello6(){
		return getParametriLivelli().getDs_livello6();
	}

	public String getLabelCd_livello7(){
		return getParametriLivelli().getDs_livello7();
	}
	public String getLabelCd_livello8(){
		return getParametriLivelli().getDs_livello8();
	}

	public void setField(String field, String label, int lung, int liv) {
		FieldProperty fieldproperty;
		try {
			fieldproperty = getBulkInfo().getFieldProperty(field);
			fieldproperty.setLabel(label);
			fieldproperty.setMaxLength(lung);
		} catch (NullPointerException e){}

		try {
			fieldproperty = getBulkInfo().getFormFieldProperty(field);
			fieldproperty.setLabel(label);
			fieldproperty.setMaxLength(lung);
		} catch (NullPointerException e){}

		try {
			fieldproperty = getBulkInfo().getColumnFieldProperty(field);
			fieldproperty.setLabel(label);
			fieldproperty.setMaxLength(lung);
		} catch (NullPointerException e){}

		try {
			fieldproperty = getBulkInfo().getFindFieldProperty(field);
			fieldproperty.setLabel(label);
			fieldproperty.setMaxLength(lung);
		} catch (NullPointerException e){}
		
		for(Enumeration enumeration = getBulkInfo().getFreeSearchProperties(); enumeration.hasMoreElements();)
		{
			try {
				fieldproperty = (FieldProperty)enumeration.nextElement();
				if (fieldproperty.getName().equals(field)) {
					fieldproperty.setLabel(label);
					fieldproperty.setMaxLength(lung);
				}
			} catch (NullPointerException e){}
		}
	}

	public boolean isSearchButtonHidden() {
		return super.isSearchButtonHidden() || getTabCorrente()>1;
	}

	public boolean isFreeSearchButtonHidden() {
		return super.isFreeSearchButtonHidden() || getTabCorrente()>1;
	}

	@Override
	public boolean isLastSearchButtonHidden() {
		return super.isLastSearchButtonHidden() || getTabCorrente()>1;
	}

	public boolean isDeleteButtonHidden() {
		return	super.isDeleteButtonHidden() || getTabCorrente()!=1;
	}

	public boolean isNewButtonHidden() {
		return super.isNewButtonHidden() || getTabCorrente()>1;
	}

	public Integer getTabCorrente(){
		return Integer.valueOf(getTab("tab").substring("tabLivello".length()));
	}

	public String getNameDsClassificazioniPre(int liv){
		if (((Classificazione_voci_epBulk) getModel()).getLivelloMax() ==liv)
			return "ds_classificazione_vis";
		return "ds_class_padre_pre"+(((Classificazione_voci_epBulk) getModel()).getLivelloMax() - liv);
	}

	public int getLivelliVisualizzati(){
		return ((Classificazione_voci_epBulk) getModel()).getLivelloMax();
	}

	public String[][] getTabs() {
		return tabs;
	}

	public void setTabs(String[][] strings) {
		tabs = strings;
	}

	public CrudAssLivelli getCrudAssLivelli() {
		Class classController;
		int index = (getTabCorrente()==0?1:getTabCorrente())-1;
		if (crudAssLivelli[index] == null) {
			if (getModel() instanceof Classificazione_voci_ep_liv8Bulk)
				classController = Classificazione_voci_ep_liv8Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv7Bulk)
				classController = Classificazione_voci_ep_liv8Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv6Bulk)
				classController = Classificazione_voci_ep_liv7Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv5Bulk)
				classController = Classificazione_voci_ep_liv6Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv4Bulk)
				classController = Classificazione_voci_ep_liv5Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv3Bulk)
				classController = Classificazione_voci_ep_liv4Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv2Bulk)
				classController = Classificazione_voci_ep_liv3Bulk.class;
			else if (getModel() instanceof Classificazione_voci_ep_liv1Bulk)
				classController = Classificazione_voci_ep_liv2Bulk.class;
			else
				classController = Classificazione_voci_epBulk.class;

			crudAssLivelli[index] = new CrudAssLivelli("ClassVociAssociate"+ getTabCorrente(), classController, "classVociAssociate", this);
		}

		return crudAssLivelli[index];
	}

	public void setCrudAssLivelli(CrudAssLivelli controller) {
		crudAssLivelli[getTabCorrente() -1] = controller;
	}

	public BulkList getBulkTab() {
		return bulkTab;
	}

	public void setBulkTab(BulkList list) {
		bulkTab = list;
	}

	public Classificazione_voci_epBulk getBulkTab(int tab) {
		try{
			int myTab = tab==0?1:tab;
			return (Classificazione_voci_epBulk)bulkTab.get(myTab-1);
		} catch (ArrayIndexOutOfBoundsException e){
			return null;
		}
	}

	public void setBulkTab(Classificazione_voci_epBulk bulk, int tab) {
		if ((getBulkTab(tab))==null)
			bulkTab.add(tab-1, bulk);
		else
			bulkTab.set(tab-1, bulk);
	}

	public int[] getFocusTab() {
		return focusTab;
	}

	public void setFocusTab(int[] is) {
		focusTab = is;
	}

	public int getFocusTab(int tab) {
		try{
			return focusTab[tab-1];
		} catch (ArrayIndexOutOfBoundsException e){
			return -1;
		}
	}

	public void setFocusTab(int is, int tab) {
		focusTab[tab-1]=is;
	}

	/*
	 * E' stato selezionato un livello diverso da quello precedentemente memorizzato (il focus del 
	 * controller è diverso da quello memorizzato nel Bulk) vengono annullati tutti i riferimenti 
	 * memorizzati nel BP ai Bulk e focus dei livelli successivi.
	 * Se si naviga dalla Tab1 il processo di annullamento avviene solo se cambia il model del BP
	 **/
	public void allineaFocusBulkTab(int intTabOld){
		if (getBulkTab(intTabOld+1) != null && 
		    ((intTabOld==Classificazione_voci_epHome.LIVELLO_MIN && !getModel().equalsByPrimaryKey(getBulkTab(intTabOld))) ||
			 (intTabOld!=Classificazione_voci_epHome.LIVELLO_MIN && //intTabOld!=Classificazione_voci_epHome.LIVELLO_MAX && 
			  (getCrudAssLivelli().getModel() == null ||!getCrudAssLivelli().getModel().equalsByPrimaryKey(getBulkTab(intTabOld+1)))))) {
			for (int i=intTabOld+1; i<=Classificazione_voci_epHome.LIVELLO_MAX; i++){
				setBulkTab(null, i);
				setFocusTab(-1, i);
				try{
					crudAssLivelli[i-1].clearFilter();
				} catch (NullPointerException e){}
			}
		}
		/*
		 * Settaggio delle informazioni memorizzate sul BP relativamente ai Bulk e focus
		 */
		setBulkTab((Classificazione_voci_epBulk)getModel(), intTabOld);
		setFocusTab(getCrudAssLivelli().getSelection().getFocus(), intTabOld);
	}

	public void allineaDescriptionTab(int intTabNew){
		Classificazione_voci_epBulk model = (Classificazione_voci_epBulk)getModel();
		String descriptionTipo = "Tipo Classificazione "+(model.getTipo()!=null?(String)Classificazione_voci_epBulk.tipoKeys.get(model.getTipo()):" NON DEFINITO");
		String dsLivello = intTabNew==0?"Tipo Classificazione":descriptionTipo+" - "+parametriLivelli.getDs_livello(intTabNew);
		getBulkInfo().setLongDescription(dsLivello);
		getBulkInfo().setShortDescription(dsLivello);
	}

	/**
	 * 	E' stata generata la richiesta di cercare la Classificazione che sarà nodo padre della Classificazione
	 *	che si sta creando.
	 *  Il metodo restituisce un Iteratore che permette di navigare tra le Classificazioni passando
	 *	da un livello ai suoi nodi figli e viceversa. Il metodo isLeaf, permette di definire un 
	 *	"livello foglia", il livello, cioè, che non ha nodi sotto di esso.
	 *
	 * @param context la <code>ActionContext</code> che ha generato la richiesta
	 *
	 * @return <code>RemoteBulkTree</code> l'albero richiesto
	**/
	public RemoteBulkTree getClassificazioniTree(ActionContext context) throws it.cnr.jada.comp.ComponentException{
	  return
		new RemoteBulkTree() {
		  public RemoteIterator getChildren(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
			try{
			  return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(context,((Classificazione_voci_epComponentSession)createComponentSession()).getChildren(context.getUserContext(),bulk));
			}catch(it.cnr.jada.comp.ComponentException ex){
				throw new java.rmi.RemoteException("Component Exception",ex);
			}catch(it.cnr.jada.action.BusinessProcessException ex){
				throw new java.rmi.RemoteException("BusinessProcess Exception",ex);
			}

		  }
		  public OggettoBulk getParent(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
			try{
			  return ((Classificazione_voci_epComponentSession)createComponentSession()).getParent(context.getUserContext(),bulk);
			}catch(it.cnr.jada.comp.ComponentException ex){
				throw new java.rmi.RemoteException("Component Exception",ex);
			}catch(it.cnr.jada.action.BusinessProcessException ex){
				throw new java.rmi.RemoteException("BusinessProcess Exception",ex);
			}
		  }
	  
		  public boolean isLeaf(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
			try{
			  return ((Classificazione_voci_epComponentSession)createComponentSession()).isLeaf(context.getUserContext(),bulk);
			}catch(it.cnr.jada.comp.ComponentException ex){
				throw new java.rmi.RemoteException("Component Exception",ex);
			}catch(it.cnr.jada.action.BusinessProcessException ex){
				throw new java.rmi.RemoteException("BusinessProcess Exception",ex);
			}
		  }
		};
	}

	public Parametri_livelli_epBulk getParametriLivelli(ActionContext actioncontext) throws BusinessProcessException {
		try {
			if (parametriLivelli == null)
				setParametriLivelli(((Classificazione_voci_epComponentSession)createComponentSession()).findParametriLivelli(actioncontext.getUserContext(), CNRUserContext.getEsercizio(actioncontext.getUserContext()), "ECO"));
		} catch(ComponentException | RemoteException e) {
			throw handleException(e);
		}
        return getParametriLivelli();
	}

	public Parametri_livelli_epBulk getParametriLivelli() {
		return parametriLivelli;
	}

	public void setParametriLivelli(Parametri_livelli_epBulk bulk) {
		parametriLivelli = bulk;
	}

	public class CrudAssLivelli extends SimpleDetailCRUDController {
		private CompoundFindClause filterController;

		public CrudAssLivelli(String s, Class class1, String s1, FormController formcontroller) {
			super(s, class1, s1, formcontroller, true);
		}

		public void setFilter(ActionContext actioncontext, CompoundFindClause compoundfindclause) {
			try {
				CRUDClassificazioneVociEPBP bp = (CRUDClassificazioneVociEPBP)actioncontext.getBusinessProcess(); 
				bp.setModel(actioncontext, ((Classificazione_voci_epComponentSession)createComponentSession()).caricaClassVociAssociate(actioncontext.getUserContext(),(Classificazione_voci_epBulk)bp.getModel(),compoundfindclause));
				bp.allineaFocusBulkTab(bp.getTabCorrente().intValue());
				filterController = compoundfindclause;
			} catch (DetailedRuntimeException e) {
				handleException(e);
			} catch (ComponentException e) {
				handleException(e);
			} catch (RemoteException e) {
				handleException(e);
			} catch (BusinessProcessException e) {
				handleException(e);
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
		public void validate(ActionContext actioncontext) throws ValidationException {
			if (getModel()!=null)
			   getModel().validate();
			super.validate(actioncontext);
		}

		public String getLabelCd_livello1(){
			return ((CRUDClassificazioneVociBP)getParentController()).getLabelCd_livello1();
		}

		public String getLabelCd_livello2(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello2();
		}

		public String getLabelCd_livello3(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello3();
		}

		public String getLabelCd_livello4(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello4();
		}

		public String getLabelCd_livello5(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello5();
		}

		public String getLabelCd_livello6(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello6();
		}

		public String getLabelCd_livello7(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello7();
		}
		
		public String getLabelCd_livello8(){
			return ((CRUDClassificazioneVociEPBP)getParentController()).getLabelCd_livello8();
		}
	}

	public void cambiaTipoClassificazione(ActionContext actioncontext) throws BusinessProcessException {
		try {
			Classificazione_voci_epBulk model = (Classificazione_voci_epBulk) getModel();
			if (model.getTipo()==null)
				resetTabs(actioncontext);
			else {
				setParametriLivelli(((Classificazione_voci_epComponentSession)createComponentSession()).findParametriLivelli(actioncontext.getUserContext(), model.getEsercizio(), model.getTipo()));
				allineaDescriptionTab(getTabCorrente());
				if (this.isSearching()) {
					if (getParametriLivelli()==null || getParametriLivelli().getLivelli()>0) {
						tabs = new String[2][3];
						tabs[0] = new String[]{"tabLivello0", "Tipo Classificazione", "/config00/tab_classificazione_voci_ep.jsp"};
						tabs[1] = new String[]{"tabLivello1", getParametriLivelli().getDs_livello(1), "/config00/tab_classificazione_voci_ep.jsp"};
					} else {
						tabs = new String[1][3];
						tabs[0] = new String[]{"tabLivello0", "Tipo Classificazione", "/config00/tab_classificazione_voci_ep.jsp"};
					}
					setTab("tab", "tabLivello0");
				} else {
					tabs = new String[getParametriLivelli().getLivelli() + 1][3];
					tabs[0] = new String[]{"tabLivello0", "Tipo Classificazione", "/config00/tab_classificazione_voci_ep.jsp"};
					for (int i = 1; i <= getParametriLivelli().getLivelli(); i++)
						tabs[i] = new String[]{"tabLivello" + (i), getParametriLivelli().getDs_livello(i), "/config00/tab_classificazione_voci_ep.jsp"};
				}
			}
		} catch(ComponentException | RemoteException e) {
			throw handleException(e);
		}
	}

	@Override
	protected void resetTabs(ActionContext actioncontext) {
		Classificazione_voci_epBulk model = (Classificazione_voci_epBulk) getModel();
		if (this.isSearching() || getParametriLivelli()==null || model.getTipo()==null) {
			tabs = new String[1][3];
			tabs[0] = new String[]{"tabLivello0", "Tipo Classificazione", "/config00/tab_classificazione_voci_ep.jsp"};
			setTab("tab", "tabLivello0");
		} else {
			tabs = new String[getParametriLivelli().getLivelli()+1][3];
			tabs[0] = new String[]{ "tabLivello0","Tipo Classificazione","/config00/tab_classificazione_voci_ep.jsp" };
			for (int i = 1; i<=getParametriLivelli().getLivelli(); i++)
				tabs[i] = new String[]{ "tabLivello"+(i),getParametriLivelli().getDs_livello(i),"/config00/tab_classificazione_voci_ep.jsp" };
		}
	}

	@Override
	public void resetForSearch(ActionContext actioncontext) throws BusinessProcessException {
		super.resetForSearch(actioncontext);
		getBulkInfo().setLongDescription("Tipo Classificazione");
		getBulkInfo().setShortDescription("Tipo Classificazione");
	}
}
