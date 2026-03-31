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
 * Created on Mar 30, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.doccont00.comp;

import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.doccont00.core.ObbligazioneWizard;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.tabrif.bulk.Tipo_bolloBulk;
import it.cnr.contab.doccont00.tabrif.bulk.Tipo_bolloHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.EuroFormat;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ApplicationRuntimeException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class MandatoAutomaticoComponent extends MandatoComponent {
	/**
	 *  creazione mandato
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione un Mandato Automatico da impegni e il mandato supera la validazione
	 *      (metodo verificaMandato)
	 *    PostCondition:
	 *      Vengono aggiornati gli importi dei sospesi eventualmente associati al mandato (metodo aggiornaImportoSospesi),
	 *      vengono aggiornati gli importi associati a documenti contabili di tutte le scadenze di obbligazioni specificate
	 *      nelle righe del mandato (metodo aggiornaImportoObbligazione), vengo aggiornati i saldi relativi ai capitoli di spesa
	 *      (metodo aggiornaStatoFattura), vengono aggiornati gli stati delle fatture specificate nelle righe dei mandati
	 *      (metodo aggiornaCapitoloSaldoRiga)
	 *  creazione mandato automatico da impegno di competenza
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione un Mandato automatico e l'utente ha selezionato solo
	 *      impegni di competenza
	 *    PostCondition:
	 *      Viene richiesto alla Component che gestisce i documenti amministrativi generici di creare un documento
	 *      generico di spesa (di tipo GENERICO_S) con tante righe quanti sono gli impegni selezionati dall'utente,
	 *      quanti sono gli impegni selezionati dall'utente. Con il metodo 'aggiornaImportoObbligazione'
	 *		 vengono incrementati gli importi (im_associato_doc_contabili)
	 *      degli impegni selezionati con l'importo trasferito nel mandato. Con il metodo 'aggiornaCapitoloSaldoRiga' vengono aggiornati i saldi relativi ai
	 *      capitoli di competenza degli impegni selezionati.
	 *  creazione mandato automatico da impegno residuo
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione un Mandato automatico e l'utente ha selezionato solo
	 *      impegni residui
	 *    PostCondition:
	 *      Viene richiesto alla Component che gestisce i documenti amministrativi generici di creare un documento
	 *      generico di spesa (di tipo GENERICO_S) con tante righe quanti sono gli impegni selezionati dall'utente,
	 *      quanti sono gli impegni selezionati dall'utente. Con il metodo 'aggiornaImportoObbligazione'
	 *		 vengono incrementati gli importi (im_associato_doc_contabili)
	 *      degli impegni selezionati con l'importo trasferito nel mandato. Con il metodo 'aggiornaCapitoloSaldoRiga' vengono aggiornati i saldi relativi ai
	 *      capitoli residui degli impegni selezionati.
	 *  creazione di 2 mandati di trasferimento residuo+competenza
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione un Mandato di trasferimento e l'utente ha selezionato sia
	 *      impegni residui che di competenza
	 *    PostCondition:
	 *      Vengono creati 2 mandati uno di competenza e uno residuo e sono da considerarsi valide entrambe le
	 *      postconditions: 'creazione mandato di trasferimento residuo' e 'creazione mandato di trasferimento competenza'
	 *
	 * @param userContext lo <code>UserContext</code> che ha generato la richiesta
	 * @param bulk <code>OggettoBulk</code> il mandato da creare
	 *
	 * @return wizard il Mandato Automatico creato
	 */
	public OggettoBulk creaMandatoAutomatico (UserContext userContext,OggettoBulk bulk) throws ComponentException {
		MandatoAutomaticoWizardBulk wizard = (MandatoAutomaticoWizardBulk) bulk;
		if (wizard.isAutomatismoDaImpegni())
			return creaMandatoAutomaticoDaImpegni(userContext, wizard, wizard.getImpegniSelezionatiColl());
		else if (wizard.isAutomatismoDaDocumentiPassivi())
			return creaMandatoAutomaticoDaDocPassivi(userContext, wizard, wizard.getDocPassiviSelezionatiColl());
		return bulk;
	}

	private OggettoBulk creaMandatoAutomaticoDaImpegni (UserContext userContext, MandatoAutomaticoWizardBulk wizard, java.util.Collection<ObbligazioneWizard> impegniColl) throws ComponentException {
		try {
			wizard.getModelloDocumento().setEsercizio(Optional.ofNullable(wizard.getEsercizio()).orElse(wizard.getEsercizio()));
			wizard.getModelloDocumento().setCd_cds(Optional.ofNullable(wizard.getCd_cds()).orElse(wizard.getCd_cds()));
			wizard.getModelloDocumento().setCd_unita_organizzativa(Optional.ofNullable(wizard.getCd_unita_organizzativa()).orElse(wizard.getCd_unita_organizzativa()));
			wizard.getModelloDocumento().setCd_cds_origine(Optional.ofNullable(wizard.getCd_cds_origine()).orElse(wizard.getCd_cds_origine()));
			wizard.getModelloDocumento().setCd_uo_origine(Optional.ofNullable(wizard.getCd_uo_origine()).orElse(wizard.getCd_uo_origine()));
			wizard.getModelloDocumento().setData_registrazione(Optional.ofNullable(wizard.getModelloDocumento().getData_registrazione()).orElse(wizard.getDt_emissione()));
			wizard.getModelloDocumento().setDt_da_competenza_coge(Optional.ofNullable(wizard.getModelloDocumento().getDt_da_competenza_coge()).orElse(wizard.getDt_emissione()));
			wizard.getModelloDocumento().setDt_a_competenza_coge(Optional.ofNullable(wizard.getModelloDocumento().getDt_a_competenza_coge()).orElse(wizard.getDt_emissione()));
			wizard.getModelloDocumento().setUser(Optional.ofNullable(wizard.getUser()).orElse(wizard.getUser()));
			wizard.getModelloDocumento().setTi_associato_manrev(Documento_genericoBulk.ASSOCIATO_A_MANDATO);

			Documento_genericoBulk documentoGenericoBulk = Utility.createDocumentoGenericoComponentSession().creaDocumentoGenericoDaImpegni(userContext, wizard.getModelloDocumento(), impegniColl);

			wizard.setTi_automatismo(MandatoAutomaticoWizardBulk.AUTOMATISMO_DA_DOCPASSIVI);
			wizard.setDocPassiviSelezionatiColl(((MandatoAutomaticoWizardHome)getHome(userContext, MandatoAutomaticoWizardBulk.class)).findDocPassivi(documentoGenericoBulk));

			//Rimetto sugli oggetti documentiPassiviWizard la descrizione delle righe mandato impostate sugli impegni
			wizard.getDocPassiviSelezionatiColl().forEach(docpassivo-> impegniColl.stream()
					.filter(el->el.getObbligazioneBulk().getEsercizio().equals(docpassivo.getEsercizio_obbligazione()))
					.filter(el->el.getObbligazioneBulk().getEsercizio_originale().equals(docpassivo.getEsercizio_ori_obbligazione()))
					.filter(el->el.getObbligazioneBulk().getCd_cds().equals(docpassivo.getCd_cds_obbligazione()))
					.filter(el->el.getObbligazioneBulk().getPg_obbligazione().equals(docpassivo.getPg_obbligazione()))
					.filter(el->el.getObbligazioneBulk().getPg_obbligazione_scadenzario().equals(docpassivo.getPg_obbligazione_scadenzario()))
					.findFirst()
					.ifPresent(impegno->docpassivo.setDescrizioneRigaMandatoWizard(impegno.getDescrizioneRigaMandatoWizard())));

			return this.creaMandatoAutomatico(userContext, wizard);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	private OggettoBulk creaMandatoAutomaticoDaDocPassivi(UserContext userContext, MandatoAutomaticoWizardBulk wizard, java.util.Collection<V_doc_passivo_obbligazione_wizardBulk> docPassiviColl) throws ComponentException {
		try {
			final boolean isMandatoMonoVoce;
			if (wizard.isFlGeneraMandatoMonoVoce())
				isMandatoMonoVoce = Boolean.TRUE;
			else {
				Configurazione_cnrBulk configTagBilancio;
				try {
					configTagBilancio = getConfigurazioneInviaBilancio(userContext);
				} catch (PersistencyException e) {
					throw new ComponentException(e);
				}
				isMandatoMonoVoce = Optional.ofNullable(configTagBilancio).map(s->Boolean.valueOf(s.getVal01())).orElse(Boolean.FALSE) &&
						Optional.of(configTagBilancio).map(s->Integer.valueOf(s.getVal02())).orElse(1)<=1;
			}

			final boolean isMandatoFondoEconomale = Optional.ofNullable(wizard.getFondoEconomaleBulk()).isPresent();

			//Verifico che ci siano tutti i dati di cui necessito
			for (V_doc_passivo_obbligazione_wizardBulk docTerzo:docPassiviColl) {
				if (!Optional.ofNullable(docTerzo.getCdModalitaPagamentoRigaDocumento()).isPresent() && !isMandatoFondoEconomale)
					throw new ApplicationException("Operazione non possibile! Non è stata indicata per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" la modalità di pagamento.");
				if (!Optional.ofNullable(docTerzo.getPgBancaRigaDocumento()).isPresent() && !isMandatoFondoEconomale)
					throw new ApplicationException("Operazione non possibile! Non sono state indicate per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" le coordinate.");

				if (docTerzo.getImportoRigaMandato().compareTo(BigDecimal.ZERO) < 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" un importo di pagamento (" + new EuroFormat().format(docTerzo.getImportoRigaMandato()) +
							") negativo o nullo.");
				else if (docTerzo.isFatturaPassiva() && docTerzo.getImportoRigaMandato().compareTo(docTerzo.getImponibileRigaMandato().add(docTerzo.getImpostaRigaMandato())) != 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" un importo di pagamento (" + new EuroFormat().format(docTerzo.getImportoRigaMandato()) +
							") non coincidente con la somma dell'imponibile ed imposta ("+ new EuroFormat().format(docTerzo.getImponibileRigaMandato().add(docTerzo.getImpostaRigaMandato()))+").");
				else if (docTerzo.getImponibileRigaMandato().compareTo(docTerzo.getIm_imponibile_doc_amm()) > 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" un imponibile (" + new EuroFormat().format(docTerzo.getImponibileRigaMandato()) +
							") maggiore dell'importo originario (" + new EuroFormat().format(docTerzo.getIm_imponibile_doc_amm()) +
							").");
				else if (docTerzo.getImpostaRigaMandato().compareTo(docTerzo.getIm_iva_doc_amm()) > 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" una imposta (" + new EuroFormat().format(docTerzo.getImpostaRigaMandato()) +
							") maggiore dell'importo originario (" + new EuroFormat().format(docTerzo.getIm_iva_doc_amm()) +
							").");
				else if (docTerzo.getImponibileRigaMandato().compareTo(docTerzo.getImpostaRigaMandato()) < 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" un imponibile (" + new EuroFormat().format(docTerzo.getImponibileRigaMandato()) +
							") minore dell'imposta (" + new EuroFormat().format(docTerzo.getImpostaRigaMandato()) +
							").");
				else if (docTerzo.getImportoRigaMandato().compareTo(docTerzo.getIm_totale_doc_amm()) > 0)
					throw new ApplicationException("Operazione non possibile! E' stato indicato per il documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" un importo di pagamento (" + new EuroFormat().format(docTerzo.getImportoRigaMandato()) +
							") maggiore dell'importo iniziale (" + new EuroFormat().format(docTerzo.getIm_totale_doc_amm()) +
							").");
				else if (docTerzo.getImportoRigaMandato().compareTo(docTerzo.getIm_totale_doc_amm()) < 0) {
					//sdoppio riga
					V_doc_passivo_obbligazioneBulk docTerzoAgg;
					if (docTerzo.isDocumentoGenericoPassivo())
						docTerzoAgg = Utility.createDocumentoGenericoComponentSession().sdoppiaDettagliInAutomatico(userContext, docTerzo, docTerzo.getImportoRigaMandato());
					else if (docTerzo.isFatturaPassiva())
						docTerzoAgg = Utility.createFatturaPassivaComponentSession().sdoppiaDettagliInAutomatico(userContext, docTerzo, docTerzo.getImponibileRigaMandato(), docTerzo.getImpostaRigaMandato());
					else
						throw new ApplicationException("Operazione non possibile! Il documento " +
								docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
								" risulta essere di una tipologia per la quale non è previsto il pagamento parziale.");
					//Allineo l'oggetto con il risultato
					docTerzo.setIm_imponibile_doc_amm(docTerzoAgg.getIm_imponibile_doc_amm());
					docTerzo.setIm_iva_doc_amm(docTerzoAgg.getIm_iva_doc_amm());
					docTerzo.setIm_scadenza(docTerzoAgg.getIm_scadenza());
					docTerzo.setIm_totale_doc_amm(docTerzoAgg.getIm_totale_doc_amm());
				} else if (docTerzo.getImponibileRigaMandato().compareTo(docTerzo.getIm_imponibile_doc_amm())!=0 || docTerzo.getImpostaRigaMandato().compareTo(docTerzo.getIm_iva_doc_amm())!=0)
					throw new ApplicationException("Operazione non possibile! Per la riga del documento " +
							docTerzo.getEsercizio() + "/" + docTerzo.getCd_cds() + "/" + docTerzo.getCd_tipo_documento_amm() + "/" + docTerzo.getPg_documento_amm() +
							" è stato richiesto il pagamento totale modificando la ripartizione dell'importo tra imponibile ed imposta.");

				if (!isMandatoFondoEconomale) {
					if (!docTerzo.getCd_modalita_pag().equals(docTerzo.getCdModalitaPagamentoRigaDocumento()) || !docTerzo.getPg_banca().equals(docTerzo.getPgBancaRigaDocumento())) {
						//Aggiorno modalità pagamento su riga da pagare
						if (docTerzo.isDocumentoGenericoPassivo())
							Utility.createDocumentoGenericoComponentSession().aggiornaModalitaPagamento(userContext, docTerzo, docTerzo.getModalitaPagamentoRigaDocumento(), docTerzo.getBancaRigaDocumento());
						else if (docTerzo.isFatturaPassiva())
							Utility.createFatturaPassivaComponentSession().aggiornaModalitaPagamento(userContext, docTerzo, docTerzo.getModalitaPagamentoRigaDocumento(), docTerzo.getBancaRigaDocumento());

						if (docTerzo.getModalitaPagamentoRigaDocumento().getRif_modalita_pagamento().getFl_per_cessione()) {
							BancaBulk bancaCessionario = (BancaBulk) getHome(userContext, BancaBulk.class).findByPrimaryKey(new BancaBulk(docTerzo.getBancaRigaDocumento().getCd_terzo_delegato(), docTerzo.getBancaRigaDocumento().getPg_banca_delegato()));
							Modalita_pagamentoBulk modalitaCessionario = this.findModalita_pagamentoOptions( userContext, bancaCessionario.getCd_terzo())
									.stream().filter(el->el.getRif_modalita_pagamento().getTi_pagamento().equals(bancaCessionario.getTi_pagamento()))
									.findFirst().orElse(null);
							docTerzo.setCd_modalita_pag(modalitaCessionario.getCd_modalita_pag());
							docTerzo.setPg_banca(bancaCessionario.getPg_banca());
							docTerzo.setTi_pagamento(bancaCessionario.getTi_pagamento());
						} else {
							docTerzo.setCd_modalita_pag(docTerzo.getCdModalitaPagamentoRigaDocumento());
							docTerzo.setPg_banca(docTerzo.getPgBancaRigaDocumento());
							docTerzo.setTi_pagamento(docTerzo.getModalitaPagamentoRigaDocumento().getRif_modalita_pagamento().getTi_pagamento());
						}
					}
				}
			}

			Map<Integer, Map<String, Map<Long, List<V_doc_passivo_obbligazione_wizardBulk>>>> mapTerzo;
			TerzoModalitaPagamento terzoModalitaPagamento;

			if (!isMandatoFondoEconomale) {
				terzoModalitaPagamento = null;
				mapTerzo = docPassiviColl.stream().collect(Collectors.groupingBy(V_doc_passivo_obbligazione_wizardBulk::getCd_terzo,
						Collectors.groupingBy(V_doc_passivo_obbligazione_wizardBulk::getCdModalitaPagamentoRigaDocumento,
								Collectors.groupingBy(V_doc_passivo_obbligazione_wizardBulk::getPgBancaRigaDocumento))));
			} else {
				Hashtable mapSecond = new Hashtable<>();
				mapSecond.put(wizard.getFondoEconomaleBulk().getPg_banca(), docPassiviColl);

				Hashtable mapFirst = new Hashtable<>();
				mapFirst.put(wizard.getFondoEconomaleBulk().getCd_modalita_pag(), mapSecond);

				mapTerzo = new HashMap<>();
				mapTerzo.put(wizard.getFondoEconomaleBulk().getCd_terzo(), mapFirst);

				terzoModalitaPagamento = new TerzoModalitaPagamento();
				terzoModalitaPagamento.setTerzoBulk(cercaTerzo(userContext, wizard.getFondoEconomaleBulk().getCd_terzo()));

				Modalita_pagamentoBulk modPagamento = new Modalita_pagamentoBulk();
				modPagamento.setTerzo(terzoModalitaPagamento.getTerzoBulk());
				modPagamento.setRif_modalita_pagamento(wizard.getFondoEconomaleBulk().getModalita_pagamento());
				modPagamento = (Modalita_pagamentoBulk) getHome(userContext, Modalita_pagamentoBulk.class).findByPrimaryKey(modPagamento);
				terzoModalitaPagamento.setModalitaPagamentoBulk(modPagamento);
				terzoModalitaPagamento.setBancaBulk((BancaBulk)getHome(userContext, BancaBulk.class).findByPrimaryKey(wizard.getFondoEconomaleBulk().getBanca()));
			}

			mapTerzo.keySet().forEach(aCdTerzo -> mapTerzo.get(aCdTerzo).keySet().forEach(aCdModalitaPag -> mapTerzo.get(aCdTerzo).get(aCdModalitaPag).keySet().forEach(aPgBanca -> {
				List<V_doc_passivo_obbligazione_wizardBulk> result = mapTerzo.get(aCdTerzo).get(aCdModalitaPag).get(aPgBanca);

				final Map<Integer, Map<String, List<V_doc_passivo_obbligazione_wizardBulk>>> mapEsercizioVoce;

				//Se rottura per voce la carico sugli oggetti per le successive operazioni
				if (isMandatoMonoVoce) {
					result.forEach(docTerzo->{
						try {
							ObbligazioneBulk obbBulk = (ObbligazioneBulk)getHome(userContext, ObbligazioneBulk.class).findAndLock(new ObbligazioneBulk(docTerzo.getCd_cds_obbligazione(), docTerzo.getEsercizio_obbligazione(), docTerzo.getEsercizio_ori_obbligazione(), docTerzo.getPg_obbligazione()));
							Elemento_voceBulk evBulk = (Elemento_voceBulk)getHome(userContext, Elemento_voceBulk.class).findByPrimaryKey(obbBulk.getElemento_voce());

							docTerzo.setCdElementoVoce(evBulk.getCd_elemento_voce());
						} catch (Exception e) {
							throw new ApplicationRuntimeException(e);
						}
					});

					mapEsercizioVoce = result.stream().collect(
							Collectors.groupingBy(V_doc_passivo_obbligazione_wizardBulk::getEsercizio_ori_obbligazione,
									Collectors.groupingBy(V_doc_passivo_obbligazione_wizardBulk::getCdElementoVoce)));
				} else {
					mapEsercizioVoce = new HashMap<>();
					Map mapVoce = new HashMap<>();
					mapVoce.put("XXX", result);
					mapEsercizioVoce.put(0, mapVoce);
				}

				List<V_doc_passivo_obbligazione_wizardBulk> docPassiviMandatoEmesso = new ArrayList();

				mapEsercizioVoce.keySet().forEach(aEsercizio -> mapEsercizioVoce.get(aEsercizio).keySet().forEach(aCdVoce -> {
					try {
						List docPassiviCompetenzaColl = new ArrayList();
						List docPassiviResiduiColl = new ArrayList();

						mapEsercizioVoce.get(aEsercizio).get(aCdVoce).forEach(docTerzo -> {
							try {
								Obbligazione_scadenzarioBulk os = (Obbligazione_scadenzarioBulk)
										getHome(userContext, Obbligazione_scadenzarioBulk.class).findAndLock(new Obbligazione_scadenzarioBulk(docTerzo.getCd_cds_obbligazione(), docTerzo.getEsercizio_obbligazione(), docTerzo.getEsercizio_ori_obbligazione(), docTerzo.getPg_obbligazione(), docTerzo.getPg_obbligazione_scadenzario()));

								if (os.getIm_scadenza().compareTo(docTerzo.getImportoRigaMandato()) != 0 ||
										os.getIm_associato_doc_contabile().compareTo(docTerzo.getIm_associato_doc_contabile()) != 0)
									throw new ApplicationException("Operazione non possibile! E' stata utilizzata da un altro utente la scadenza nr." + docTerzo.getPg_obbligazione_scadenzario() + " dell'impegno " + docTerzo.getEsercizio_ori_obbligazione() + "/" + docTerzo.getPg_obbligazione());

								//se il docTerzo ha un documento presente in docPassiviMandatoEmesso allora devo aggiornare il pg_ver_rec_docamm
								//che, essendo stato modificato proprio dal mandato automatico, potrebbe bloccare la successiva emissione di nuovi mandati
								//sullo stesso documento amministrativo
								for (V_doc_passivo_obbligazione_wizardBulk docPassivoMandatoEmesso:docPassiviMandatoEmesso){
									if (docPassivoMandatoEmesso.getEsercizio().equals(docTerzo.getEsercizio()) &&
											docPassivoMandatoEmesso.getCd_cds().equals(docTerzo.getCd_cds()) &&
											docPassivoMandatoEmesso.getCd_unita_organizzativa().equals(docTerzo.getCd_unita_organizzativa()) &&
											docPassivoMandatoEmesso.getCd_tipo_documento_amm().equals(docTerzo.getCd_tipo_documento_amm()) &&
											docPassivoMandatoEmesso.getPg_documento_amm().equals(docTerzo.getPg_documento_amm())) {
										//recupero dal db il record e aggiorno il pg_ver_rec_docamm
										V_doc_passivo_obbligazioneBulk docBulk = ((V_doc_passivo_obbligazioneHome)getHome(userContext, V_doc_passivo_obbligazioneBulk.class)).findDocPassivo(docTerzo);
										docTerzo.setPg_ver_rec(docBulk.getPg_ver_rec());
									}
								}
								if (docTerzo.isCompetenza() || wizard.isFlGeneraMandatoUnico())
									docPassiviCompetenzaColl.add(docTerzo);
								else
									docPassiviResiduiColl.add(docTerzo);
							} catch (Exception e) {
								throw new ApplicationRuntimeException(e);
							}
						});

						MandatoBulk mandatoCompetenza, mandatoResiduo;
						if (!docPassiviCompetenzaColl.isEmpty()) {
							mandatoCompetenza = creaMandatoAutomatico(userContext, wizard, MandatoBulk.TIPO_COMPETENZA);
							mandatoCompetenza.setMandato_terzo(creaMandatoTerzo(mandatoCompetenza, cercaTerzo(userContext, aCdTerzo), Optional.ofNullable(wizard.getMandato_terzo()).map(Mandato_terzoBulk::getTipoBollo).orElse(null)));
							mandatoCompetenza = aggiungiDocPassivi(userContext, mandatoCompetenza, docPassiviCompetenzaColl, terzoModalitaPagamento);

							mandatoCompetenza.refreshImporto();
							super.creaConBulk(userContext, mandatoCompetenza);
							docPassiviMandatoEmesso.addAll(docPassiviCompetenzaColl);
							aggiornaStatoFattura(userContext, mandatoCompetenza, INSERIMENTO_MANDATO_ACTION);
							wizard.getMandatiColl().add(mandatoCompetenza);
						}
						if (!docPassiviResiduiColl.isEmpty()) {
							mandatoResiduo = creaMandatoAutomatico(userContext, wizard, MandatoBulk.TIPO_RESIDUO);
							mandatoResiduo.setMandato_terzo(creaMandatoTerzo(mandatoResiduo, cercaTerzo(userContext, aCdTerzo), Optional.ofNullable(wizard.getMandato_terzo()).map(Mandato_terzoBulk::getTipoBollo).orElse(null)));
							mandatoResiduo = aggiungiDocPassivi(userContext, mandatoResiduo, docPassiviResiduiColl, terzoModalitaPagamento);

							mandatoResiduo.refreshImporto();
							super.creaConBulk(userContext, mandatoResiduo);
							docPassiviMandatoEmesso.addAll(docPassiviResiduiColl);
							aggiornaStatoFattura(userContext, mandatoResiduo, INSERIMENTO_MANDATO_ACTION);
							wizard.getMandatiColl().add(mandatoResiduo);
						}
					} catch (ComponentException e) {
						throw new ApplicationRuntimeException(e);
					}
				}));
			})));
			return wizard;
		} catch ( Exception e ) {
			throw handleException(e);
		}
	}

	/**
	 *  creazione mandato automatico da impegno
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione un Mandato di accreditamento
	 *    PostCondition:
	 *      Viene creato un mandato di tipo Accreditamento con CDS/UO = Ente e CDS/UO origine = CDS/UO di scrivania.
	 *      Viene creato un mandatoTerzo coi dati relativi al terzo CDS verso cui il mandato e' emesso
	 *
	 * @param userContext lo <code>UserContext</code> che ha generato la richiesta
	 * @param wizard <code>MandatoAccreditamentoWizardBulk</code> il mandato di accreditamento
	 * @param ti_competenza_residuo tipologia (competenza/residuo) del mandato da creare
	 *
	 * @return mandato <code>MandatoAccreditamentoBulk</code> il mandato di regolarizzazione creato
	 */
	private MandatoBulk creaMandatoAutomatico (UserContext userContext, MandatoAutomaticoWizardBulk wizard, String ti_competenza_residuo ) throws ComponentException {
		try {
			MandatoIBulk mandato = new MandatoIBulk();
			mandato.setToBeCreated();
			mandato.setUser( wizard.getUser() );
			mandato.setEsercizio( wizard.getEsercizio());
			mandato.setCds( wizard.getCds());
			mandato.setUnita_organizzativa( wizard.getUnita_organizzativa());
			mandato.setCd_cds_origine(Optional.ofNullable(wizard.getCd_cds_origine()).orElse(((it.cnr.contab.utenze00.bp.CNRUserContext) userContext).getCd_cds()));
			mandato.setCd_uo_origine(Optional.ofNullable(wizard.getCd_uo_origine()).orElse(((it.cnr.contab.utenze00.bp.CNRUserContext) userContext).getCd_unita_organizzativa()));
			mandato.setStato(MandatoBulk.STATO_MANDATO_EMESSO);
			mandato.setDt_emissione( wizard.getDt_emissione());
			mandato.setIm_mandato(new BigDecimal(0));
			mandato.setIm_pagato(new BigDecimal(0));

			mandato.setDs_mandato(Optional.ofNullable(wizard.getDs_mandato()).orElse("Mandato automatico da "+(wizard.isAutomatismoDaImpegni()?"impegno":"documenti passivi")));

			mandato.setTi_mandato(MandatoBulk.TIPO_PAGAMENTO);
			mandato.setCd_tipo_documento_cont( Numerazione_doc_contBulk.TIPO_MAN);
			mandato.setTi_competenza_residuo( ti_competenza_residuo );
			mandato.setStato_trasmissione(MandatoBulk.STATO_TRASMISSIONE_NON_INSERITO);
			mandato.setStato_coge(MandatoBulk.STATO_COGE_N);
			mandato.setIm_ritenute( new java.math.BigDecimal(0));

			return mandato;
		} catch ( Exception e ) {
			throw handleException( e );
		}
	}

	/**
	 *  creazione mandato terzo
	 *    PreCondition:
	 *      E' stata generata la richiesta di creazione di un Mandato_terzoBulk
	 *    PostCondition:
	 *      Viene creata una istanza di Mandato_terzoBulk coi dati del beneficiario del mandato e viene impostato
	 *      il tipo bollo di default.
	 *
	 * @param mandato <code>MandatoAutomaticoBulk</code> il mandato
	 * @param terzo Il codice del beneficiario del mandato
	 *
	 * @return mTerzo l'istanza di <code>Mandato_terzoBulk</code> creata
	 */
	private Mandato_terzoBulk creaMandatoTerzo (MandatoBulk mandato, TerzoBulk terzo, Tipo_bolloBulk bollo ) {
		Mandato_terzoBulk mTerzo = new Mandato_terzoIBulk();

		if (!(mandato instanceof MandatoAutomaticoWizardBulk))
			mTerzo.setToBeCreated();

		mTerzo.setUser( mandato.getUser() );
		mTerzo.setMandato( mandato );

		//imposto il terzo
		mTerzo.setTerzo( terzo );

		//imposto il tipo bollo di default
		mTerzo.setTipoBollo( bollo );
		return mTerzo;
	}

	private Mandato_terzoBulk creaMandatoTerzo (UserContext userContext, MandatoBulk mandato, TerzoBulk terzo ) throws ComponentException {
		try {
			return creaMandatoTerzo(mandato, terzo, ((Tipo_bolloHome)getHome( userContext, Tipo_bolloBulk.class )).findTipoBolloDefault(Tipo_bolloBulk.TIPO_SPESA));
		} catch ( Exception e ) {
			throw handleException( e );
		}
	}

	/**
	 *  lista le coordinate bancarie
	 *    PreCondition:
	 *      E' stato creata una riga di mandato di trasferimento
	 *    PostCondition:
	 *     La lista delle coordinate bancarie del terzo beneficiario del mandato viene estratta
	 *
	 * @param userContext lo <code>UserContext</code> che ha generato la richiesta
	 * @param cdTerzo il codice Terzo di cui ricercare i dati
	 *
	 * @return result la lista delle banche definite per il terzo beneficiario del mandato
	 *			null non è stata definita nessuna banca per il terzo beneficiario del mandato
	 */
	public List<BancaBulk> findBancaOptions(UserContext userContext, Integer cdTerzo, String cdModalitaPagamento) throws it.cnr.jada.persistency.PersistencyException, it.cnr.jada.persistency.IntrospectionException, ComponentException {
		if ( cdTerzo != null) {
			Rif_modalita_pagamentoBulk rifModalitaPagamentoBulk = (Rif_modalita_pagamentoBulk)this.findByPrimaryKey(userContext, new Rif_modalita_pagamentoBulk(cdModalitaPagamento));
			BancaHome bancaHome = (BancaHome) getHome(userContext, BancaBulk.class);
			return getHome( userContext, BancaBulk.class ).fetchAll(bancaHome.selectBancaFor(rifModalitaPagamentoBulk, cdTerzo));
		}
		return null;
	}

	/**
	 *  lista le modalità di pagamento
	 *    PreCondition:
	 *      E' stato creata una riga di mandato  di trasferimento
	 *    PostCondition:
	 *     La lista delle modalità di pagamento del terzo beneficiario, tutte appartenenti alla stessa classe (Bancario/Postale/..) per cui si sta emettendo il mandato,
	 *     viene estratta.Vengono escluse le modalità di pagamento riferite a terzi cessionari
	 *
	 * @param userContext lo <code>UserContext</code> che ha generato la richiesta
	 * @param cdTerzo il codice Terzo di cui ricercare i dati
	 *
	 * @return result la lista delle modalità di pagamento definite per il terzo beneficiario del mandato
	 *			null non è stata definita nessuna modalità di pagamento per il terzo beneficiario del mandato
	 */

	private List<Modalita_pagamentoBulk> findModalita_pagamentoOptions (UserContext userContext, Integer cdTerzo) throws it.cnr.jada.persistency.PersistencyException, ComponentException {
		if ( cdTerzo != null ) {
			SQLBuilder sql = getHome( userContext, Modalita_pagamentoBulk.class ).createSQLBuilder();
			sql.addTableToHeader( "RIF_MODALITA_PAGAMENTO");
			sql.addSQLJoin("MODALITA_PAGAMENTO.CD_MODALITA_PAG","RIF_MODALITA_PAGAMENTO.CD_MODALITA_PAG" );
			sql.addClause( FindClause.AND, "cd_terzo", SQLBuilder.EQUALS, cdTerzo );
			List<Modalita_pagamentoBulk> result = getHome( userContext, Modalita_pagamentoBulk.class ).fetchAll( sql );
			if ( result.size() == 0 )
				throw new ApplicationException("Non esistono modalità di pagamento per il terzo " + cdTerzo);
			for (Modalita_pagamentoBulk modpag:result)
				modpag.setRif_modalita_pagamento((Rif_modalita_pagamentoBulk) getHome( userContext, Rif_modalita_pagamentoBulk.class ).findByPrimaryKey(modpag.getRif_modalita_pagamento()));
			return result;
		}
		return null;
	}

	/**
	 *  inizializzazione di una istanza di MandatoBulk
	 *    PreCondition:
	 *     E' stata richiesta l'inizializzazione di una istanza di MandatoBulk
	 *    PostCondition:
	 *     Viene impostata la data di emissione del mandato con la data del Server
	 *  inizializzazione di una istanza di MandatoAutomaticoWizardBulkmandato
	 *    PreCondition:
	 *     E' stata richiesta l'inizializzazione di una istanza di MandatoAutomaticoWizardBulk, l'oggetto bulk
	 *     utilizzato come wizard per la generazione dei mandati automatici da impegno
	 *    PostCondition:
	 *     Viene impostata la data di emissione del wizard con la data del Server, il Cds e l'UO di appartenenza con
	 *     il Cds e l'UO collegato, il mandato terzo con il codice terzo che corrisponde al codice terzo
	 *     del mandato automatico (metodo creaMandatoTerzo), viene impostata la lista degli impegni
	 *     (metodo listaImpegniCNR) del CNR
	 *
	 * @param aUC lo <code>UserContext</code> che ha generato la richiesta
	 * @param bulk <code>OggettoBulk</code> il mandato da inizializzare per l'inserimento
	 *
	 * @return bulk <code>OggettoBulk</code> il Mandato inizializzato per l'inserimento
	 *
	 */
	public OggettoBulk inizializzaBulkPerInserimento (UserContext aUC,OggettoBulk bulk) throws ComponentException {
		try {
			MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk) bulk;
			mandato.setCds( (CdsBulk)getHome( aUC, CdsBulk.class).findByPrimaryKey( new CdsBulk(CNRUserContext.getCd_cds(aUC))));
			mandato.setUnita_organizzativa( (Unita_organizzativaBulk)getHome( aUC, Unita_organizzativaBulk.class).findByPrimaryKey( new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(aUC))));

			bulk = super.inizializzaBulkPerInserimento( aUC, bulk );

			return bulk;
		} catch ( Exception e ) {
			throw handleException( bulk, e );
		}
	}

	public OggettoBulk inizializzaMappaAutomatismo (UserContext aUC, OggettoBulk bulk) throws ComponentException {
		try {
			MandatoAutomaticoWizardBulk mandato = (MandatoAutomaticoWizardBulk)bulk;
			mandato.getMandatiColl().clear();
			if (mandato.isAutomatismoDaImpegni()) {
				if (mandato.getFind_doc_passivi().getCd_terzo() == null &&
						mandato.getFind_doc_passivi().getCd_precedente() == null &&
						mandato.getFind_doc_passivi().getCognome() == null &&
						mandato.getFind_doc_passivi().getRagione_sociale() == null &&
						mandato.getFind_doc_passivi().getNome()  == null &&
						mandato.getFind_doc_passivi().getPartita_iva()  == null &&
						mandato.getFind_doc_passivi().getCodice_fiscale()  == null)
					throw new ApplicationException( "Attenzione! Deve essere specificato almeno un campo dell'anagrafica." );
				else {
					mandato.getImpegniSelezionatiColl().clear();
					mandato.getImpegniColl().clear();
					mandato.setMandato_terzo( creaMandatoTerzo( aUC, mandato, cercaTerzo( aUC, mandato ) ) );
					mandato = listaImpegniTerzo( aUC, mandato );
					mandato.setModalita_pagamento(this.findModalita_pagamentoOptions( aUC, mandato.getMandato_terzo().getCd_terzo()).get(0));
					mandato.setBanca(findBancaOptions(aUC, mandato.getMandato_terzo().getCd_terzo(), mandato.getModalita_pagamento().getCd_modalita_pag()).get(0));
					mandato.getModelloDocumento().setDt_da_competenza_coge(mandato.getDt_emissione());
					mandato.getModelloDocumento().setDt_a_competenza_coge(mandato.getDt_emissione());
					initializeKeysAndOptionsInto(aUC, bulk);
				}
			} else if (mandato.isAutomatismoDaDocumentiPassivi()) {
				mandato.getDocPassiviColl().clear();

				/*
				 * Necessario per caricare il bollo di default 
				 */
				mandato.setMandato_terzo( creaMandatoTerzo( aUC, mandato, null ) );
				bulk = listaDocPassivi( aUC, mandato );
				for (Object docPassivo:((MandatoIBulk)bulk).getDocPassiviColl()) {
					if (docPassivo instanceof V_doc_passivo_obbligazione_wizardBulk) {
						V_doc_passivo_obbligazione_wizardBulk docPassivoWizard = (V_doc_passivo_obbligazione_wizardBulk)docPassivo;

						docPassivoWizard.setModalitaPagamentoOptions(findModalita_pagamentoOptions( aUC, docPassivoWizard.getCd_terzo()));
						docPassivoWizard.setModalitaPagamentoRigaDocumentoWizard(docPassivoWizard.getModalitaPagamentoRigaDocumento());

						if (docPassivoWizard.getModalitaPagamentoRigaDocumentoWizard()!=null) {
							docPassivoWizard.setBancaOptions(findBancaOptions(aUC, docPassivoWizard.getCd_terzo(), docPassivoWizard.getModalitaPagamentoRigaDocumentoWizard().getCd_modalita_pag()));
							docPassivoWizard.setBancaRigaDocumentoWizard(docPassivoWizard.getBancaRigaDocumento());
						}

						docPassivoWizard.setImponibileRigaMandatoWizard(docPassivoWizard.getImponibileRigaMandato());
						docPassivoWizard.setImpostaRigaMandatoWizard(docPassivoWizard.getImpostaRigaMandato());
						docPassivoWizard.setImportoRigaMandatoWizard(docPassivoWizard.getImportoRigaMandato());
					}
				}
			}

			return bulk;
		} catch ( Exception e ) {
			throw handleException( bulk, e );
		}
	}

	/**
	 *  ricerca impegni Terzo
	 *    PreCondition:
	 *     E' stata richiesta la ricerca degli impegni del Terzo per emettere un mandato di pagamento
	 *    PostCondition:
	 *     Vengono ricercati tutti gli impegni che hanno un importo disponibile ( importo disponibile = importo iniziale
	 *     dell'impegno - importo già associato ai documenti contabili) e il cui Terzo sia quello indicato
	 *
	 * @param aUC lo <code>UserContext</code> che ha generato la richiesta
	 * @param mandato <code>MandatoBulk</code> il mandato di pagamento
	 *
	 * @return mandato il Mandato automatico emesso dopo la ricerca degli impegni del CNR
	 *
	 */
	public MandatoAutomaticoWizardBulk listaImpegniTerzo (UserContext aUC, MandatoAutomaticoWizardBulk mandato) throws ComponentException {
		try {
			Collection result = ((MandatoAutomaticoWizardHome)getHome( aUC, mandato.getClass())).findImpegni( mandato );
			mandato.setImpegniColl(result);
			int size = mandato.getImpegniColl().size();
			if ( size == 0 )
				throw new ApplicationException( "La ricerca degli Impegni non ha fornito alcun risultato.");
			for (Object o : mandato.getImpegniColl()) ((V_obbligazioneBulk) o).setNrImpegni(size);
			return mandato;
		} catch (PersistencyException | IntrospectionException e ) {
			throw handleException( mandato, e );
		}

	}

	private TerzoBulk cercaTerzo (UserContext aUC, MandatoAutomaticoWizardBulk wizard) throws ComponentException {
		try {
			Collection result = ((MandatoAutomaticoWizardHome)getHome( aUC, wizard.getClass())).findTerzi( wizard );
			if ( result.size() == 0 )
				throw new ApplicationException( "La ricerca dei Terzi non ha fornito alcun risultato.");
			if ( result.size() > 1 )
				throw new ApplicationException( "Esiste piu' di un terzo che soddisfa i criteri di ricerca.");

			return (TerzoBulk)getHome( aUC, TerzoBulk.class).findByPrimaryKey( new TerzoBulk(((V_anagrafico_terzoBulk)result.iterator().next()).getCd_terzo()));
		} catch (PersistencyException | IntrospectionException e ) {
			throw handleException( wizard, e );
		}
	}

	private TerzoBulk cercaTerzo (UserContext aUC, Integer cd_terzo) throws ComponentException {
		try {
			return (TerzoBulk)getHome( aUC, TerzoBulk.class).findByPrimaryKey( new TerzoBulk(cd_terzo));
		} catch ( it.cnr.jada.persistency.PersistencyException e ) {
			throw handleException( e );
		}
	}

	public List<BancaBulk> findBancaOptions(UserContext userContext, MandatoAutomaticoWizardBulk mandato) throws it.cnr.jada.persistency.PersistencyException, it.cnr.jada.persistency.IntrospectionException, ComponentException {
		return this.findBancaOptions(userContext, Optional.ofNullable(mandato.getMandato_terzo()).map(Mandato_terzoBulk::getCd_terzo).orElse(null),
				Optional.of(mandato).map(MandatoAutomaticoWizardBulk::getModalita_pagamento).map(Modalita_pagamentoBulk::getCd_modalita_pag).orElse(null));
	}

	public List<Modalita_pagamentoBulk> findModalita_pagamentoOptions(UserContext userContext, MandatoAutomaticoWizardBulk mandato) throws it.cnr.jada.persistency.PersistencyException, it.cnr.jada.persistency.IntrospectionException, ComponentException {
		return this.findModalita_pagamentoOptions(userContext, Optional.ofNullable(mandato.getMandato_terzo()).map(Mandato_terzoBulk::getCd_terzo).orElse(null));
	}
}
