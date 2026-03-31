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

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.doccont00.bp.MandatoAutomaticoWizardBP;
import it.cnr.contab.doccont00.core.bulk.MandatoAutomaticoWizardBulk;
import it.cnr.contab.doccont00.core.bulk.V_doc_passivo_obbligazione_wizardBulk;
import it.cnr.contab.util.EuroFormat;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.OptionBP;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Azione che gestisce le richieste relative alla Gestione Documenti Contabili
 * (Mandato di Accreditamento)
 */
public class MandatoAutomaticoWizardAction extends CRUDAbstractMandatoAction {
	public MandatoAutomaticoWizardAction() {
		super();
	}
	/**
	 * Gestisce il cambio del flag imputazione automatica o manuale delle voci
	 * di bilancio CNR nel mandato di accreditamento.
	 	 * @param context <code>ActionContext</code> in uso.
		 *
		 * @return <code>Forward</code>
	 */
	
	public Forward doCambiaFl_imputazione_manuale(ActionContext context) {
		try {
			fillModel( context );
			return context.findDefaultForward();	
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Gestisce il cambio delle modalita di pagamento
	 	 * @param context <code>ActionContext</code> in uso.
		 *
		 * @return <code>Forward</code>
	 */
	public Forward doCambiaModalitaPagamento(ActionContext context) {
		try	{
			fillModel( context );
			CRUDBP bp = getBusinessProcess( context );
			if ( bp instanceof MandatoAutomaticoWizardBP )
				((MandatoAutomaticoWizardBP)bp).cambiaModalitaPagamento( context );
			return context.findDefaultForward();	
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doCambiaBanca(ActionContext context) {
		try	{
			fillModel( context );
			CRUDBP bp = getBusinessProcess( context );
			if ( bp instanceof MandatoAutomaticoWizardBP )
				((MandatoAutomaticoWizardBP)bp).cambiaBanca( context );
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Metodo utilizzato per gestire l'aggiunta di nuove righe al mandato
	 */
	public Forward doConfermaEmettiMandato(ActionContext context,int option)  {
		try {
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP) getBusinessProcess(context);
			MandatoAutomaticoWizardBulk wizard = (MandatoAutomaticoWizardBulk)bp.getModel();		
			if (option == OptionBP.YES_BUTTON) {
				if (wizard.isAutomatismoDaImpegni() && wizard.getImpegniSelezionatiColl().size() == 0 ) {
					if ( wizard.isFl_imputazione_manuale() )
						bp.setMessage("E' necessario specificare almeno un importo da trasferire!");
					else
						bp.setMessage("E' necessario specificare almeno una priorità!");				
				} else if (wizard.isAutomatismoDaDocumentiPassivi() && wizard.getDocPassiviSelezionatiColl().size() == 0 ) {
					bp.setMessage("E' necessario specificare almeno un documento passivo da pagare!");
				} else {
					wizard.validate();
					bp.create(context);
				}
			}	
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Metodo utilizzato per gestire l'aggiunta di nuove righe al mandato
	 */
	public Forward doEmettiMandato(ActionContext context) {
		try {
			fillModel( context );
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
			MandatoAutomaticoWizardBulk wizard = (MandatoAutomaticoWizardBulk)bp.getModel();
			if (wizard.isAutomatismoDaImpegni()) {
				wizard.selezionaImpegni();
				if ( !wizard.isFl_imputazione_manuale() )
					wizard.assegnaImportiInBaseAPriorita();
			} else
				wizard.setDocPassiviSelezionatiColl( bp.getDocumentiPassivi().getSelectedModels(context));
				
			return doConfermaEmettiMandato( context, OptionBP.YES_BUTTON);
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Metodo utilizzato per gestire l'aggiunta di nuove righe al mandato
	 */
	public Forward doVisualizzaMandato(ActionContext context) {
		try {
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
			if ( bp.getMandati().getModel() != null ) {
				CRUDBP crud = (CRUDBP) context.createBusinessProcess( "CRUDMandatoBP", new Object[] {"V" } );
				crud.edit( context,bp.getMandati().getModel(), true );
				return context.addBusinessProcess(crud);
			}
			return context.findDefaultForward();	
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doBringBackSearchFind_creditore(ActionContext context, MandatoAutomaticoWizardBulk mandato, TerzoBulk creditore) {
		try {
			fillModel(context);
			if ( creditore != null ) {
				MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
				bp.setCodice_terzo(creditore.getCd_terzo());	
				bp.setTi_impegni(((MandatoAutomaticoWizardBulk)bp.getModel()).getTi_impegni());	
				bp.reset(context);   
			}
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	  *	Inizializzazione dell'attributo "Linea di Attivita"
	  */
	public Forward doBlankSearchFind_creditore(ActionContext context, MandatoAutomaticoWizardBulk mandato) {
		try {
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
			bp.setCodice_terzo(null);
			bp.setTi_impegni(((MandatoAutomaticoWizardBulk)bp.getModel()).getTi_impegni());	
			bp.reset(context);   
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Gestisce il caricamento dei documenti passivi
	 *
	 */
	public Forward doCercaDocPassivi(ActionContext context)	{
		try {
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
			fillModel( context );
			bp.setTi_automatismo(MandatoAutomaticoWizardBulk.AUTOMATISMO_DA_DOCPASSIVI);
			bp.inizializzaMappaAutomatismo(context);   
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	/**
	 * Gestisce il caricamento degli impegni
	 *
	 */
	public Forward doCercaImpegni(ActionContext context) {
		try {
			MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)getBusinessProcess(context);
			fillModel( context );
			bp.setTi_automatismo(MandatoAutomaticoWizardBulk.AUTOMATISMO_DA_IMPEGNI);
			bp.inizializzaMappaAutomatismo(context);   
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doToggleCriteriRicerca(ActionContext context) {
		MandatoAutomaticoWizardBP bp = Optional.ofNullable(getBusinessProcess(context))
				.filter(MandatoAutomaticoWizardBP.class::isInstance)
				.map(MandatoAutomaticoWizardBP.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Business Process non valido"));
		bp.setCriteriRicercaCollapse(!bp.isCriteriRicercaCollapse());
		return context.findDefaultForward();
	}

	public Forward doToggleDettDocumento(ActionContext context) {
		MandatoAutomaticoWizardBP bp = Optional.ofNullable(getBusinessProcess(context))
				.filter(MandatoAutomaticoWizardBP.class::isInstance)
				.map(MandatoAutomaticoWizardBP.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Business Process non valido"));
		bp.setDettDocumentoCollapse(!bp.isDettDocumentoCollapse());
		return context.findDefaultForward();
	}

	public Forward doOnChangeImponibileRigaMandato(ActionContext context) {
		MandatoAutomaticoWizardBP bp = Optional.ofNullable(getBusinessProcess(context))
				.filter(MandatoAutomaticoWizardBP.class::isInstance)
				.map(MandatoAutomaticoWizardBP.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Business Process non valido"));
		BigDecimal oldImponibile = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast).map(V_doc_passivo_obbligazione_wizardBulk::getImponibileRigaMandato).orElse(BigDecimal.ZERO);
		try	{
			fillModel( context );

			Optional<V_doc_passivo_obbligazione_wizardBulk> newSelected = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
					.map(V_doc_passivo_obbligazione_wizardBulk.class::cast);

			if (newSelected.filter(el->el.getImponibileRigaMandato().compareTo(el.getIm_imponibile_doc_amm())>0).isPresent()) {
				newSelected.ifPresent(el->el.setImponibileRigaMandatoWizard(oldImponibile));
				throw new it.cnr.jada.comp.ApplicationException("Non è possibile inserire un valore superiore a quello iniziale della riga ("+ new EuroFormat().format(newSelected.get().getIm_imponibile_doc_amm())+").");
			}

			bp.onChangeImponibileRigaMandato(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doOnChangeImpostaRigaMandato(ActionContext context) {
		MandatoAutomaticoWizardBP bp = Optional.ofNullable(getBusinessProcess(context))
				.filter(MandatoAutomaticoWizardBP.class::isInstance)
				.map(MandatoAutomaticoWizardBP.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Business Process non valido"));
		BigDecimal oldImposta = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast).map(V_doc_passivo_obbligazione_wizardBulk::getImpostaRigaMandato).orElse(BigDecimal.ZERO);
		try {
			fillModel( context );

			Optional<V_doc_passivo_obbligazione_wizardBulk> newSelected = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
					.map(V_doc_passivo_obbligazione_wizardBulk.class::cast);

			if (newSelected.filter(el->el.getImpostaRigaMandato().compareTo(el.getIm_iva_doc_amm())>0).isPresent()) {
				newSelected.ifPresent(el->el.setImpostaRigaMandatoWizard(oldImposta));
				throw new it.cnr.jada.comp.ApplicationException("Non è possibile inserire un valore superiore a quello iniziale della riga ("+ new EuroFormat().format(newSelected.get().getIm_iva_doc_amm())+").");
			}

			bp.onChangeImpostaRigaMandato(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}

	public Forward doOnChangeImportoRigaMandato(ActionContext context) {
		MandatoAutomaticoWizardBP bp = Optional.ofNullable(getBusinessProcess(context))
				.filter(MandatoAutomaticoWizardBP.class::isInstance)
				.map(MandatoAutomaticoWizardBP.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Business Process non valido"));
		BigDecimal oldImporto = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
				.map(V_doc_passivo_obbligazione_wizardBulk.class::cast).map(V_doc_passivo_obbligazione_wizardBulk::getImportoRigaMandato).orElse(BigDecimal.ZERO);
		try {
			fillModel( context );

			Optional<V_doc_passivo_obbligazione_wizardBulk> newSelected = Optional.ofNullable(bp.getDocumentiPassivi().getModel()).filter(V_doc_passivo_obbligazione_wizardBulk.class::isInstance)
					.map(V_doc_passivo_obbligazione_wizardBulk.class::cast);

			if (newSelected.filter(el->el.getImportoRigaMandato().compareTo(el.getIm_totale_doc_amm())>0).isPresent()) {
				newSelected.ifPresent(el->el.setImportoRigaMandatoWizard(oldImporto));
				throw new it.cnr.jada.comp.ApplicationException("Non è possibile inserire un valore superiore a quello iniziale della riga ("+ new EuroFormat().format(newSelected.get().getIm_totale_doc_amm())+").");
			}

			bp.onChangeImportoRigaMandato(context);
			return context.findDefaultForward();
		} catch(Throwable e) {
			return handleException(context,e);
		}
	}
}
