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
 * * Created on Mar 27, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.doccont00.bp;

import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.MandatoAutomaticoComponentSession;
import it.cnr.contab.doccont00.ejb.MandatoComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.util.action.SimpleDetailCRUDController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business Process che gestisce le attività di CRUD per l'entita' Mandato Automatico da impegni
 */
public class MandatoAutomaticoWizardBP extends it.cnr.jada.util.action.SimpleCRUDBP {
	private final SimpleDetailCRUDController impegni = new SimpleDetailCRUDController("Impegni",V_obbligazioneBulk.class,"impegniColl",this);
	private final SimpleDetailCRUDController mandati = new SimpleDetailCRUDController("Mandati",MandatoBulk.class,"mandatiColl",this);
	private final SimpleDetailCRUDController documentiPassivi = new SimpleDetailCRUDController("DocumentiPassivi",V_doc_passivo_obbligazione_wizardBulk.class,"docPassiviColl",this);
	private Integer codice_terzo;
	private String ti_impegni;
	private String ti_automatismo;
	private boolean criteriRicercaCollapse = false;
	private boolean dettDocumentoCollapse = false;

	public MandatoAutomaticoWizardBP()
	{
		super();
		setTab("tab","tabCriteriRicerca");
	}
	
	public MandatoAutomaticoWizardBP(String function) 
	{
		super(function);
		setTab("tab","tabCriteriRicerca");
	}
	
	public MandatoAutomaticoWizardBP(String function, Integer codice_terzo, String ti_impegni, String ti_automatismo )
	{
		super(function);
		setCodice_terzo(codice_terzo);
		setTi_impegni(ti_impegni);
		setTi_automatismo(ti_automatismo);
		setTab("tab","tabCriteriRicerca");
	}
	
	/**
	 * Metodo utilizzato per gestire la modifica delle coordinate bancarie (BancaBulk) a seguito della 
	 * modifica delle modalita Di Pagamento
	 * @param context <code>ActionContext</code> in uso.
	 *
	 */
	public void cambiaModalitaPagamento(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException  {
		try {
			MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();
			if (mandato.isAutomatismoDaImpegni()) {
				if ( mandato.getModalita_pagamentoOptions() != null && mandato.getModalita_pagamento().getCd_modalita_pag() == null)
					mandato.setModalita_pagamento( (Modalita_pagamentoBulk) mandato.getModalita_pagamentoOptions().get(0));
				List result = ((MandatoAutomaticoComponentSession) createComponentSession()).findBancaOptions(context.getUserContext(), mandato.getMandato_terzo().getCd_terzo(), mandato.getModalita_pagamento().getCd_modalita_pag());
				mandato.setBancaOptions(result);
			} else {
				V_doc_passivo_obbligazione_wizardBulk docPassivoSelected = (V_doc_passivo_obbligazione_wizardBulk)this.getDocumentiPassivi().getModel();
				if ( docPassivoSelected.getModalitaPagamentoOptions() != null && docPassivoSelected.getCd_modalita_pag() == null)
					docPassivoSelected.setModalitaPagamentoRigaDocumentoWizard(docPassivoSelected.getModalitaPagamentoOptions().get(0));
				List result = ((MandatoAutomaticoComponentSession) createComponentSession()).findBancaOptions(context.getUserContext(), docPassivoSelected.getCd_terzo(), docPassivoSelected.getModalitaPagamentoRigaDocumento().getCd_modalita_pag());
				docPassivoSelected.setBancaOptions(result);

				if ( docPassivoSelected.getBancaOptions() != null)
					docPassivoSelected.setBancaRigaDocumentoWizard(docPassivoSelected.getBancaOptions().get(0));
				if ( docPassivoSelected.getBancaRigaDocumentoWizard() != null)
					docPassivoSelected.setCd_terzo_cessionario(docPassivoSelected.getBancaRigaDocumentoWizard().getCd_terzo_delegato());
			}
			setModel(context, mandato);
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public void cambiaBanca(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException  {
		try {
			MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();
			if (!mandato.isAutomatismoDaImpegni()) {
				V_doc_passivo_obbligazione_wizardBulk docPassivoSelected = (V_doc_passivo_obbligazione_wizardBulk)this.getDocumentiPassivi().getModel();
				if ( docPassivoSelected.getBancaRigaDocumentoWizard() != null)
					docPassivoSelected.setCd_terzo_cessionario(docPassivoSelected.getBancaRigaDocumentoWizard().getCd_terzo_delegato());
			}
			setModel(context, mandato);
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public void create(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		try {
			setModel(context, ((MandatoAutomaticoComponentSession)createComponentSession()).creaMandatoAutomatico(context.getUserContext(), getModel()));
			setDirty(false);
			setEditable(false);
			setStatus(VIEW);
			MandatoAutomaticoWizardBulk wizard = (MandatoAutomaticoWizardBulk) getModel();
			if (wizard.getMandatiColl().size() == 0 )
				setMessage( "Attenzione! Nessun mandato è stato generato.");
			else {
				if (wizard.getMandatiColl().size() == 1 )
					setMessage( "Il Mandato automatico" + (wizard.isAutomatismoDaImpegni()?" a favore del Terzo " + wizard.getMandato_terzo().getTerzo().getCd_terzo().toString():"") + " è stato generato.");
				if (wizard.getMandatiColl().size() == 2 )
					setMessage( "I Mandati automatici" + (wizard.isAutomatismoDaImpegni()?" a favore del Terzo " + wizard.getMandato_terzo().getTerzo().getCd_terzo().toString():"") + " sono stati generati.");

				if (wizard.isAutomatismoDaImpegni())
					wizard.setImpegniColl(wizard.getImpegniSelezionatiColl());
				else if (wizard.isAutomatismoDaDocumentiPassivi())
					wizard.setDocPassiviColl(wizard.getDocPassiviSelezionatiColl());
			}
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public void inizializzaMappaAutomatismo(ActionContext actioncontext) throws it.cnr.jada.action.BusinessProcessException {
		try {
			MandatoAutomaticoWizardBulk bulk = (MandatoAutomaticoWizardBulk)getModel();
			setTi_impegni(bulk.getTi_impegni());
			bulk.setTi_automatismo(getTi_automatismo());

			MandatoAutomaticoComponentSession session = (MandatoAutomaticoComponentSession) createComponentSession();
			bulk = (MandatoAutomaticoWizardBulk) session.inizializzaMappaAutomatismo( actioncontext.getUserContext(), bulk );
	
			setModel(actioncontext, bulk);
			setDettDocumentoCollapse(Boolean.TRUE);

			if ((bulk.isAutomatismoDaImpegni() && bulk.getImpegniColl().size()>0) ||
				(bulk.isAutomatismoDaDocumentiPassivi() && bulk.getDocPassiviColl().size()>0)) {
				setCriteriRicercaCollapse(Boolean.TRUE);
				this.setEditable(Boolean.TRUE);
				this.setStatus(EDIT);
			}
		} catch(Exception e) {
			throw handleException(e);
		}
    }

	/**
	 * Metodo utilizzato per creare una toolbar applicativa personalizzata.
	 * @return null In questo caso la toolbar è vuota
	 */
	protected it.cnr.jada.util.jsp.Button[] createToolbar() {
		return null;
	}
	
	/**
	 * <!-- @TODO: da completare -->
	 * Restituisce il valore della proprietà 'codice_terzo'
	 *
	 * @return Il valore della proprietà 'codice_terzo'
	 */
	public Integer getCodice_terzo() {
		return codice_terzo;
	}
	
	/**
	 * @return it.cnr.jada.util.action.SimpleDetailCRUDController
	 */
	public final it.cnr.jada.util.action.SimpleDetailCRUDController getImpegni() {
		return impegni;
	}
	
	/**
	 * @return it.cnr.jada.util.action.SimpleDetailCRUDController
	 */
	public final it.cnr.jada.util.action.SimpleDetailCRUDController getMandati() {
		return mandati;
	}
	
	public it.cnr.jada.util.action.SimpleDetailCRUDController getDocumentiPassivi() {
		return documentiPassivi;
	}

	/**
	 *	Abilito il bottone di emissione del mandato.
	 *	@return			TRUE 	Abilitato se non sono ancora stati emessi mandati
	 *					FALSE 	Non è abilitato se sono già stati emessi i mandati
	 */
    public boolean isEmettiMandatoButtonEnabled() {
		return ((MandatoAutomaticoWizardBulk)getModel()).getMandatiColl().size() ==  0 ;
	
	}
	
	/**
	 *	Abilito il flag di calcolo automatico.
	 *	@return			TRUE 	Il flag di calcolo automatico è abilitato
	 *					FALSE 	Il flag di calcolo automatico non è abilitato 
	 */
	public boolean isFlCalcoloAutomaticoCheckboxEnabled() {
		return ((MandatoAutomaticoWizardBulk)getModel()).getMandatiColl().size() ==  0 ;
	}
	
	/**
	 *	Visualizzo il flag di calcolo automatico.
	 *	@return			TRUE 	Il flag di calcolo automatico è abilitato
	 *					FALSE 	Il flag di calcolo automatico non è abilitato 
	 */
	public boolean isFlCalcoloAutomaticoCheckboxVisible() {
		return false ;
	}

	/**
	 *	Gestisce l'abilitazione o meno del bottone di visualizzazione di un mandato di accreditamento
	 *	@return			TRUE 	Abilitato se un mandato e' stato selezionato nella lista dei mandati
	 *					FALSE 	Non è abilitato se nessun mandato e' stato selezionato nella lista dei mandati
	 */
	public boolean isVisualizzaMandatoButtonEnabled() {
		return getMandati().getModel() != null  ;
	}
	
	public void setCodice_terzo(Integer codice_terzo) {
		this.codice_terzo = codice_terzo;
	}
	
	public String getTi_impegni() {
		return ti_impegni;
	}

	public void setTi_impegni(String ti_impegni) {
		this.ti_impegni = ti_impegni;
	}

	/**
	 * Metodo utilizzato per gestire il caricamento dei documenti passivi.
	 */
	public void cercaDocPassivi(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		MandatoIBulk mandatoI = (MandatoIBulk) getModel();	
		try {
			
			MandatoComponentSession session = (MandatoComponentSession) createComponentSession();
			mandatoI = (MandatoIBulk) session.listaDocPassivi( context.getUserContext(), (MandatoBulk) getModel() );

			setModel( context, mandatoI );
			resyncChildren( context );
		} catch(Exception e) {
			mandatoI.setDocPassiviColl( new ArrayList());
			setModel( context, mandatoI );
			resyncChildren( context );
			throw handleException(e);
		}
	}
	/**
	 * Metodo utilizzato per gestire il caricamento degli impegni.
	  	 * @param context <code>ActionContext</code> in uso.
		 *
		 * @exception <code>BusinessProcessException</code>
	 */
	public void cercaImpegni(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException 
	{
		MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();	
		try 
		{
			MandatoAutomaticoComponentSession session = (MandatoAutomaticoComponentSession) createComponentSession();
			mandato = session.listaImpegniTerzo( context.getUserContext(), mandato );

			setModel( context, mandato );
			resyncChildren( context );
		} catch(Exception e) 
		{
			mandato.setDocPassiviColl( new ArrayList());
			setModel( context, mandato );
			resyncChildren( context );
			throw handleException(e);
		}
	}

	public String getTi_automatismo() {
		return ti_automatismo;
	}
	
	public void setTi_automatismo(String ti_automatismo) {
		this.ti_automatismo = ti_automatismo;
	}

	/**
	 *	Abilito il tab di creazione mandati automatici solo se ci sono sopra, in quanto posso accedere 
	 *  al Tab in questione solo tramite la conferma della ricerca.
	 */
	public boolean isMandatoAutomaticoTabEnabled() {
		return getTab("tab").equals("tabMandatoAutomatico");
	}

	public boolean isCriteriRicercaCollapse() {
		return criteriRicercaCollapse;
	}

	public void setCriteriRicercaCollapse(boolean criteriRicercaCollapse) {
		this.criteriRicercaCollapse = criteriRicercaCollapse;
	}

	public boolean isDettDocumentoCollapse() {
		return dettDocumentoCollapse;
	}

	public void setDettDocumentoCollapse(boolean dettDocumentoCollapse) {
		this.dettDocumentoCollapse = dettDocumentoCollapse;
	}

	public void onChangeImponibileRigaMandato(it.cnr.jada.action.ActionContext context) {
		MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();
		Optional.ofNullable(this.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast)
				.filter(V_doc_passivo_obbligazioneBulk::isFatturaPassiva)
				.ifPresent(el->el.setImpostaRigaMandatoWizard(el.getImportoRigaMandatoWizard().subtract(el.getImponibileRigaMandatoWizard())));
		this.allineaImportiWizard();
	}

	public void onChangeImpostaRigaMandato(it.cnr.jada.action.ActionContext context) {
		MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();
		Optional.ofNullable(this.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast)
				.filter(V_doc_passivo_obbligazioneBulk::isFatturaPassiva)
				.ifPresent(el->el.setImponibileRigaMandatoWizard(el.getImportoRigaMandatoWizard().subtract(el.getImpostaRigaMandatoWizard())));
		this.allineaImportiWizard();
	}

	public void onChangeImportoRigaMandato(it.cnr.jada.action.ActionContext context) {
		MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) getModel();
		Optional.ofNullable(this.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast)
				.filter(V_doc_passivo_obbligazioneBulk::isFatturaPassiva)
				.ifPresent(el->{
					BigDecimal percentualeImportoTotale = el.getImportoRigaMandatoWizard().divide(el.getIm_totale_doc_amm(),10, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN.multiply(BigDecimal.TEN));
					el.setImponibileRigaMandatoWizard(el.getIm_imponibile_doc_amm().multiply(percentualeImportoTotale).divide(BigDecimal.TEN.multiply(BigDecimal.TEN),2, BigDecimal.ROUND_HALF_UP));
					el.setImpostaRigaMandatoWizard(el.getImportoRigaMandatoWizard().subtract(el.getImponibileRigaMandatoWizard()));
				});
	}

	private void allineaImportiWizard() {
		Optional.ofNullable(this.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast)
				.filter(V_doc_passivo_obbligazioneBulk::isFatturaPassiva)
				.ifPresent(el->{
					if (el.getImponibileRigaMandatoWizard().compareTo(el.getIm_imponibile_doc_amm())>0)
						el.setImponibileRigaMandatoWizard(el.getIm_imponibile_doc_amm());
					if (el.getImpostaRigaMandatoWizard().compareTo(el.getIm_iva_doc_amm())>0)
						el.setImpostaRigaMandatoWizard(el.getIm_iva_doc_amm());
					if (el.getImpostaRigaMandatoWizard().compareTo(BigDecimal.ZERO)<0)
						el.setImpostaRigaMandatoWizard(BigDecimal.ZERO);
					if (el.getImponibileRigaMandatoWizard().compareTo(BigDecimal.ZERO)<0)
						el.setImponibileRigaMandatoWizard(BigDecimal.ZERO);
					if (el.getImpostaRigaMandatoWizard().compareTo(el.getImponibileRigaMandatoWizard())>0)
						el.setImpostaRigaMandatoWizard(el.getImponibileRigaMandatoWizard());

					//Valore minimo imposta
					BigDecimal valMinimoImposta = el.getIm_iva_doc_amm().subtract(el.getIm_imponibile_doc_amm().subtract(el.getImponibileRigaMandatoWizard()));
					if (valMinimoImposta.compareTo(BigDecimal.ZERO)<0)
						valMinimoImposta = BigDecimal.ZERO;

					if (el.getImpostaRigaMandatoWizard().compareTo(valMinimoImposta)<0)
						el.setImpostaRigaMandatoWizard(valMinimoImposta);
					el.setImportoRigaMandatoWizard(el.getImponibileRigaMandatoWizard().add(el.getImpostaRigaMandatoWizard()));
				});
	}
}