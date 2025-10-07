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

package it.cnr.contab.coepcoan00.comp;

import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.compensi00.docs.bulk.*;
import it.cnr.contab.compensi00.tabrif.bulk.*;
import it.cnr.contab.config00.bulk.*;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.config00.esercizio.bulk.EsercizioHome;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageHome;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceHome;
import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.cori00.docs.bulk.*;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.tabrif.bulk.*;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.fondecon00.core.bulk.Fondo_economaleBulk;
import it.cnr.contab.fondecon00.core.bulk.Fondo_economaleHome;
import it.cnr.contab.gestiva00.core.bulk.Liquidazione_ivaBulk;
import it.cnr.contab.gestiva00.core.bulk.Liquidazione_ivaHome;
import it.cnr.contab.gestiva00.core.bulk.Stampa_registri_ivaVBulk;
import it.cnr.contab.missioni00.docs.bulk.*;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.ordini.bulk.*;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.EuroFormat;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProposeScritturaComponent extends CRUDComponent {
	private final static Logger logger = LoggerFactory.getLogger(ProposeScritturaComponent.class);

	private final static Boolean DEFAULT_MODIFICABILE = Boolean.FALSE;
	private final static Boolean DEFAULT_ACCORPABILE = Boolean.TRUE;

    private final static boolean isAttivaGestioneSopravvenienze = Boolean.FALSE;

	private static class DettaglioScrittura {
		public DettaglioScrittura(Voce_epBulk conto, IDocumentoCogeBulk partita, BigDecimal importo) {
			this.conto = conto;
			this.partita = partita;
			this.importo = importo;
		}

		Voce_epBulk conto;
        IDocumentoCogeBulk partita;
		BigDecimal importo;

		public Voce_epBulk getConto() {
			return conto;
		}

		public IDocumentoCogeBulk getPartita() {
			return partita;
		}

		public BigDecimal getImporto() {
			return importo;
		}
	}

	private static class DettaglioAnalitico {
		public DettaglioAnalitico(IDocumentoDetailAnaCogeBulk rigaAna) {
			this(rigaAna.getVoce_analitica(), rigaAna.getLinea_attivita().getCd_centro_responsabilita(), rigaAna.getLinea_attivita().getCd_linea_attivita(), rigaAna.getImporto(), null);
		}

		public DettaglioAnalitico(DettaglioAnalitico dettAna) {
			this(dettAna.getVoceAnalitica(), dettAna.getCdCentroCosto(), dettAna.getCdLineaAttivita(), dettAna.getImporto(), null);
			this.dettaglioAnalitico = dettAna;
		}

		public DettaglioAnalitico(IDocumentoDetailAnaCogeBulk rigaAna, DettaglioPrimaNota dettaglioPrimaNota) {
			this(rigaAna);
			this.dettaglioPrimaNota = dettaglioPrimaNota;
		}

		public DettaglioAnalitico(Voce_analiticaBulk voceAnalitica, String cdCentroCosto, String cdLineaAttivita, BigDecimal importo, DettaglioPrimaNota dettaglioPrimaNota) {
			this.voceAnalitica = voceAnalitica;
			this.cdCentroCosto = cdCentroCosto;
			this.cdLineaAttivita = cdLineaAttivita;
			this.importo = importo;
			this.dettaglioPrimaNota = dettaglioPrimaNota;
		}

		DettaglioPrimaNota dettaglioPrimaNota;

		Voce_analiticaBulk voceAnalitica;

		String cdCentroCosto;

		String cdLineaAttivita;

		BigDecimal importo;

		DettaglioAnalitico dettaglioAnalitico;

		BigDecimal importoCollegato = BigDecimal.ZERO;

		public DettaglioPrimaNota getDettaglioPrimaNota() {
			return dettaglioPrimaNota;
		}

		public Voce_analiticaBulk getVoceAnalitica() {
			return voceAnalitica;
		}

		public String getCdCentroCosto() {
			return cdCentroCosto;
		}

		public String getCdLineaAttivita() {
			return cdLineaAttivita;
		}

		public BigDecimal getImporto() {
			return importo;
		}

		public void setImporto(BigDecimal importo) {
			this.importo = importo;
		}

		public DettaglioAnalitico getDettaglioAnalitico() {
			return dettaglioAnalitico;
		}

		public BigDecimal getImportoCollegato() {
			return importoCollegato;
		}

		public void setImportoCollegato(BigDecimal importoCollegato) {
			this.importoCollegato = importoCollegato;
		}
	}

	private static class DettaglioFinanziario {
		public DettaglioFinanziario(IDocumentoCogeBulk docamm, Voce_epBulk voceEp, BigDecimal imImponibile, BigDecimal imImposta, List<DettaglioAnalitico> dettagliAnalitici) {
			super();
			this.docamm = docamm;
			this.voceEp = voceEp;
			this.dettagliAnalitici = dettagliAnalitici;
			this.cdTerzo = null;
			this.imImponibile = Optional.ofNullable(imImponibile).orElse(BigDecimal.ZERO);
			this.imImposta = Optional.ofNullable(imImposta).orElse(BigDecimal.ZERO);
			this.rigaDocamm = null;
			this.rigaPartita = null;
			this.dtDaCompetenzaCoge = null;
			this.dtACompetenzaCoge = null;
		}
		public DettaglioFinanziario(IDocumentoCogeBulk docamm, Integer cdTerzo, Voce_epBulk voceEp, BigDecimal imImponibile, BigDecimal imImposta, List<DettaglioAnalitico> dettagliAnalitici) {
			super();
			this.docamm = docamm;
			this.voceEp = voceEp;
			this.dettagliAnalitici = dettagliAnalitici;
			this.cdTerzo = cdTerzo;
			this.imImponibile = Optional.ofNullable(imImponibile).orElse(BigDecimal.ZERO);
			this.imImposta = Optional.ofNullable(imImposta).orElse(BigDecimal.ZERO);
			this.rigaDocamm = null;
			this.rigaPartita = null;
			this.dtDaCompetenzaCoge = null;
			this.dtACompetenzaCoge = null;
		}

		public DettaglioFinanziario(IDocumentoCogeBulk docamm, Integer cdTerzo, Voce_epBulk voceEp, BigDecimal imImponibile, BigDecimal imImposta) {
			super();
			this.docamm = docamm;
            this.voceEp = voceEp;
			this.cdTerzo = cdTerzo;
			this.imImponibile = Optional.ofNullable(imImponibile).orElse(BigDecimal.ZERO);
			this.imImposta = Optional.ofNullable(imImposta).orElse(BigDecimal.ZERO);
			this.rigaDocamm = null;
			this.rigaPartita = null;
			this.dtDaCompetenzaCoge = null;
			this.dtACompetenzaCoge = null;
			this.dettagliAnalitici = new ArrayList<>();
		}

		public DettaglioFinanziario(IDocumentoDetailEcoCogeBulk rigaDocamm, IDocumentoDetailEcoCogeBulk rigaPartita, Integer cdTerzo, Voce_epBulk voceEp, List<DettaglioAnalitico> dettagliAnalitici) {
			super();
			this.docamm = rigaDocamm.getFather();
			this.rigaPartita = rigaPartita;
			this.voceEp = voceEp;
			this.dettagliAnalitici = dettagliAnalitici;
			this.cdTerzo = cdTerzo;
			this.dtDaCompetenzaCoge = rigaDocamm.getDt_da_competenza_coge();
			this.dtACompetenzaCoge = rigaDocamm.getDt_a_competenza_coge();
			this.rigaDocamm = rigaDocamm;

			BigDecimal pImImponibile = Optional.ofNullable(((IDocumentoAmministrativoRigaBulk)rigaDocamm).getIm_imponibile()).orElse(BigDecimal.ZERO);
			BigDecimal pImImposta = Optional.ofNullable(((IDocumentoAmministrativoRigaBulk)rigaDocamm).getIm_iva()).orElse(BigDecimal.ZERO);

			if (Optional.of(rigaDocamm).map(IDocumentoDetailEcoCogeBulk::getFather)
					.filter(Fattura_passiva_IBulk.class::isInstance)
					.map(Fattura_passiva_IBulk.class::cast)
					.filter(el->el.getFl_extra_ue()||el.getFl_intra_ue())
					.filter(el->Optional.ofNullable(el.getPg_lettera()).isPresent())
					.isPresent() && (pImImponibile.compareTo(BigDecimal.ZERO)==0 || pImImposta.compareTo(BigDecimal.ZERO)==0)) {
				//Questa gestione funziona solo per ISS perchè mette sempre una riga di imponibile (con iva=0) ed un'altra di iva (con imponibile=0)
				if (pImImponibile.compareTo(BigDecimal.ZERO) != 0) {
					this.imImponibile = Optional.ofNullable(rigaDocamm.getScadenzaDocumentoContabile())
							.map(IScadenzaDocumentoContabileBulk::getIm_associato_doc_amm)
							.orElse(pImImponibile);
					this.imImposta = BigDecimal.ZERO;
				} else {
					this.imImponibile = BigDecimal.ZERO;
					this.imImposta = pImImposta;
				}
			} else {
				this.imImponibile = pImImponibile;
				this.imImposta = pImImposta;
			}
		}

		public DettaglioFinanziario(IDocumentoDetailEcoCogeBulk rigaDocamm, Integer cdTerzo, Voce_epBulk voceEp) {
			super();
			this.docamm = rigaDocamm.getFather();
			this.voceEp = voceEp;
			this.cdTerzo = cdTerzo;
			this.dtDaCompetenzaCoge = rigaDocamm.getDt_da_competenza_coge();
			this.dtACompetenzaCoge = rigaDocamm.getDt_a_competenza_coge();
			this.imImponibile = Optional.ofNullable(((IDocumentoAmministrativoRigaBulk)rigaDocamm).getIm_imponibile()).orElse(BigDecimal.ZERO);
			this.imImposta = Optional.ofNullable(((IDocumentoAmministrativoRigaBulk)rigaDocamm).getIm_iva()).orElse(BigDecimal.ZERO);
			this.rigaDocamm = rigaDocamm;
			this.rigaPartita = null;
			this.dettagliAnalitici = new ArrayList<>();
		}

		private final IDocumentoDetailEcoCogeBulk rigaDocamm;

		private final IDocumentoCogeBulk docamm;

		private final IDocumentoDetailEcoCogeBulk rigaPartita;

		private final Integer cdTerzo;

		private final List<DettaglioAnalitico> dettagliAnalitici;

		private final Voce_epBulk voceEp;

		private final Timestamp dtDaCompetenzaCoge;

		private final Timestamp dtACompetenzaCoge;

		private final BigDecimal imImponibile;

		private final BigDecimal imImposta;

		public IDocumentoDetailEcoCogeBulk getRigaDocamm() {
			return rigaDocamm;
		}

		public IDocumentoCogeBulk getDocamm() {
			return docamm;
		}

		public IDocumentoDetailEcoCogeBulk getRigaPartita() {
			return rigaPartita;
		}

		protected Integer getCdTerzo() {
			return cdTerzo;
		}

		protected Voce_epBulk getVoceEp() {
			return voceEp;
		}

		protected Timestamp getDtDaCompetenzaCoge() {
			return dtDaCompetenzaCoge;
		}

		protected Timestamp getDtACompetenzaCoge() {
			return dtACompetenzaCoge;
		}

		protected BigDecimal getImImponibile() {
			return imImponibile;
		}

		protected BigDecimal getImImposta() {
			return imImposta;
		}

		public List<DettaglioAnalitico> getDettagliAnalitici() {
			return dettagliAnalitici;
		}

		public IDocumentoCogeBulk getPartita() {
			return Optional.ofNullable(this.getRigaPartita()).map(IDocumentoDetailEcoCogeBulk::getFather).orElse(null);
		}
	}

	private static class MandatoRigaComplete {
		public MandatoRigaComplete(IDocumentoAmministrativoBulk docamm, Mandato_rigaBulk mandatoRiga, List<IDocumentoAmministrativoRigaBulk> docammRighe, Integer cdTerzo) {
			super();
			this.docamm = docamm;
			this.mandatoRiga = mandatoRiga;
			this.docammRighe = docammRighe;
			this.cdTerzo = cdTerzo;
		}

		private final IDocumentoAmministrativoBulk docamm;
		private final Mandato_rigaBulk mandatoRiga;
		private final List<IDocumentoAmministrativoRigaBulk> docammRighe;
		private final Integer cdTerzo;
		public IDocumentoAmministrativoBulk getDocamm() {
			return docamm;
		}

		public Mandato_rigaBulk getMandatoRiga() {
			return mandatoRiga;
		}

		public List<IDocumentoAmministrativoRigaBulk> getDocammRighe() {
			return docammRighe;
		}

		protected Integer getCdTerzo() {
			return cdTerzo;
		}
	}

	private class TestataPrimaNota {
		public TestataPrimaNota() {
		}

		public TestataPrimaNota(Timestamp dtDaCompetenzaCoge, Timestamp dtACompetenzaCoge) {
			this(dtDaCompetenzaCoge, dtACompetenzaCoge, null);
		}

        public TestataPrimaNota(Timestamp dtDaCompetenzaCoge, Timestamp dtACompetenzaCoge, IDocumentoCogeBulk doccoge) {
            super();
            this.dtDaCompetenzaCoge = dtDaCompetenzaCoge;
            this.dtACompetenzaCoge = dtACompetenzaCoge;
            this.doccoge = doccoge;
        }

		private Timestamp dtDaCompetenzaCoge;

		private Timestamp dtACompetenzaCoge;

        private IDocumentoCogeBulk doccoge;

		private final List<DettaglioPrimaNota> dett = new ArrayList<>();

		public Timestamp getDtDaCompetenzaCoge() {
			return dtDaCompetenzaCoge;
		}

		public Timestamp getDtACompetenzaCoge() {
			return dtACompetenzaCoge;
		}

        public IDocumentoCogeBulk getDoccoge() {
            return doccoge;
        }

        public List<DettaglioPrimaNota> getDett() {
			return dett;
		}

		public void openDettaglioIva(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, String cdCori) {
			String tipoDettaglio;
			if (docamm instanceof Fattura_attivaBulk)
				tipoDettaglio = Movimento_cogeBulk.TipoRiga.IVA_VENDITE.value();
			else
				tipoDettaglio = Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value();

			DettaglioPrimaNota dettPN = this.addDettaglio(userContext, tipoDettaglio, docamm.getTipoDocumentoEnum().getSezioneIva(), conto, importo, cdTerzo, docamm, cdCori);

			dettPN.setPartita(partita);
		}

		public void closeDettaglioIvaSplit(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, String cdCori) {
			String tipoDettaglio;
			if (docamm instanceof Fattura_attivaBulk)
				tipoDettaglio = Movimento_cogeBulk.TipoRiga.IVA_VENDITE_SPLIT.value();
			else
				tipoDettaglio = Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO_SPLIT.value();

			DettaglioPrimaNota dettPN = this.addDettaglio(userContext, tipoDettaglio, Movimento_cogeBulk.getControSezione(docamm.getTipoDocumentoEnum().getSezioneIva()), conto, importo, cdTerzo, docamm, cdCori);
			dettPN.setPartita(partita);
		}

		private Optional<GregorianCalendar> getDataInizioCompetenzaAnnoCorrenteESuccessivi(int esercizio) {
			GregorianCalendar calDataDa = new GregorianCalendar();
			calDataDa.setTimeInMillis(this.getDtDaCompetenzaCoge().getTime());

			GregorianCalendar calDataA = new GregorianCalendar();
			calDataA.setTimeInMillis(this.getDtACompetenzaCoge().getTime());

			if (calDataA.get(Calendar.YEAR)>=esercizio) {
				if (calDataDa.get(Calendar.YEAR)<esercizio)
					return Optional.of(new GregorianCalendar(esercizio, Calendar.JANUARY, 1));
				return Optional.of(calDataDa);
			}
			return Optional.empty();
		}

		private long getGiorniCompetenzaAnniPrecedenti(int esercizio) {
			GregorianCalendar calDataDa = new GregorianCalendar();
			calDataDa.setTimeInMillis(this.getDtDaCompetenzaCoge().getTime());

			GregorianCalendar calDataA = new GregorianCalendar();
			calDataA.setTimeInMillis(this.getDtACompetenzaCoge().getTime());

			if (calDataDa.get(Calendar.YEAR)<esercizio) {
				if (calDataA.get(Calendar.YEAR)>=esercizio)
					calDataA = new GregorianCalendar(esercizio - 1, Calendar.DECEMBER, 31);
				return calendarDaysBetween(calDataDa, calDataA);
			}
			return 0;
		}

		private long getGiorniCompetenzaAnnoCorrenteESuccessivi(int esercizio) {
			Optional<GregorianCalendar> calDataDa = this.getDataInizioCompetenzaAnnoCorrenteESuccessivi(esercizio);

			if (calDataDa.isPresent()) {
				GregorianCalendar calDataA = new GregorianCalendar();
				calDataA.setTimeInMillis(this.getDtACompetenzaCoge().getTime());
				return calendarDaysBetween(calDataDa.get(), calDataA)+1;
			}
			return 0;
		}

		public void openDettaglioCostoRicavo(UserContext userContext, IDocumentoCogeBulk docamm, DettaglioFinanziario dettFin, IDocumentoCogeBulk partita, FatturaOrdineBulk fatturaOrdine, BigDecimal importo) {
			openDettaglioCostoRicavo(userContext, docamm, dettFin, partita, fatturaOrdine.getOrdineAcqConsegna().getContoBulk(), importo);
        }

        public void closeDettaglioCostoRicavo(UserContext userContext, IDocumentoCogeBulk docamm, DettaglioFinanziario dettFin, IDocumentoCogeBulk partita, FatturaOrdineBulk fatturaOrdine, BigDecimal importo) {
            List<DettaglioPrimaNota> dettPNList = openDettaglioCostoRicavo(userContext, docamm, dettFin, partita, fatturaOrdine.getOrdineAcqConsegna().getContoBulk(), importo);
            dettPNList.forEach(DettaglioPrimaNota::invertiSezione);
        }

        public List<DettaglioPrimaNota> openDettaglioCostoRicavo(UserContext userContext, IDocumentoCogeBulk docamm, DettaglioFinanziario dettFin, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo) {
            List<DettaglioPrimaNota> dettPNList = new ArrayList<>();
            BigDecimal importoAnniPrecedenti = BigDecimal.ZERO;
			BigDecimal importoAnnoCorrenteESuccessivi = BigDecimal.ZERO;

			//Se documento di storno il conto passato è già quello giusto.... quindi non faccio il controllo della competenza
			if (docamm.isDocumentoStorno())
				importoAnnoCorrenteESuccessivi = importo;
			else {
				BigDecimal delta;
				long giorniAnniPrecedenti = this.getGiorniCompetenzaAnniPrecedenti(docamm.getEsercizio());
				long giorniAnnoCorrenteESuccessivi = this.getGiorniCompetenzaAnnoCorrenteESuccessivi(docamm.getEsercizio());
				long giorniTotali = giorniAnniPrecedenti + giorniAnnoCorrenteESuccessivi;

				if (giorniAnniPrecedenti > 0)
					importoAnniPrecedenti = importo.multiply(BigDecimal.valueOf(giorniAnniPrecedenti)).divide(BigDecimal.valueOf(giorniTotali), 2, RoundingMode.HALF_UP);
				if (giorniAnnoCorrenteESuccessivi > 0)
					importoAnnoCorrenteESuccessivi = importo.multiply(BigDecimal.valueOf(giorniAnnoCorrenteESuccessivi)).divide(BigDecimal.valueOf(giorniTotali), 2, RoundingMode.HALF_UP);

				//Scarico il delta prodotto dall'arrotondamento sulla competenza anno in corso....altrimenti su quella anno precedente... altrimenti su quella successiva
				delta = importo.subtract(importoAnniPrecedenti).subtract(importoAnnoCorrenteESuccessivi);
				if (delta.compareTo(BigDecimal.ZERO) != 0) {
					if (importoAnnoCorrenteESuccessivi.compareTo(BigDecimal.ZERO) != 0)
						importoAnnoCorrenteESuccessivi = importoAnnoCorrenteESuccessivi.add(delta);
					else
						importoAnniPrecedenti = importoAnniPrecedenti.add(delta);
				}
			}

			try {
				if (importoAnniPrecedenti.compareTo(BigDecimal.ZERO)!=0) {
					Voce_epBulk aContoEconomico;
					if (docamm.getTipoDocumentoEnum().isDocumentoAttivo()) {
						if (docamm.isDocumentoStorno()) {
							if (docamm.getTipoDocumentoEnum().isNotaCreditoAttiva())
								aContoEconomico = findContoNotaCreditoDaEmettere(userContext, docamm.getEsercizio());
							else
								aContoEconomico = findContoDocumentoGenericoDaEmettere(userContext, docamm.getEsercizio());
						} else
							aContoEconomico = findContoFattureDaEmettere(userContext, docamm.getEsercizio());
					} else {
						if (docamm.isDocumentoStorno()) {
							if (docamm.getTipoDocumentoEnum().isNotaCreditoPassiva())
								aContoEconomico = findContoNotaCreditoDaRicevere(userContext, docamm.getEsercizio());
							else
								aContoEconomico = findContoDocumentoGenericoDaRicevere(userContext, docamm.getEsercizio());
						} else
							aContoEconomico = findContoFattureDaRicevere(userContext, docamm.getEsercizio());
					}
					DettaglioPrimaNota dettPN = this.addDettaglioCostoRicavo(userContext, docamm, dettFin, aContoEconomico, importoAnniPrecedenti, null, null);
					dettPN.setModificabile(Boolean.FALSE);
                    dettPNList.add(dettPN);
				}
				//Tutto l'importo addebitato all'anno corrente e a quelli successivi va a costo. Le scritture  di fine anno effettueranno le scritture di rateo.
				if (importoAnnoCorrenteESuccessivi.compareTo(BigDecimal.ZERO)!=0) {
					//Calcolo il conto economico
					Voce_epBulk aContoEconomico = conto;
					if (isAttivaGestioneSopravvenienze &&
							docamm.isDocumentoStorno() && Optional.ofNullable(partita).map(IDocumentoCogeBulk::getEsercizio)
							.map(el->el.compareTo(docamm.getEsercizio())<0).orElse(Boolean.FALSE)) {
						if (docamm.getTipoDocumentoEnum().isDocumentoAttivo())
							//Un documento di storno su fattura attiva di anni precedenti movimenta il conto delle sopravvenienze passive
							aContoEconomico = findContoSopravvenienzePassive(userContext, docamm.getEsercizio());
						else
							//Un documento di storno su fattura passiva di anni precedenti movimenta il conto delle sopravvenienze attive
							aContoEconomico = findContoSopravvenienzeAttive(userContext, docamm.getEsercizio());
					}
					//Calcolo le date di competenza
					Timestamp tsDtDaCompetenza = null;
					Timestamp tsDtACompetenza = null;
					Optional<GregorianCalendar> gcDtDaCompetenza = this.getDataInizioCompetenzaAnnoCorrenteESuccessivi(docamm.getEsercizio());
					if (gcDtDaCompetenza.isPresent()) {
						tsDtDaCompetenza = new Timestamp(gcDtDaCompetenza.get().getTimeInMillis());
						tsDtACompetenza = this.getDtACompetenzaCoge();
					}
					DettaglioPrimaNota dettPN = this.addDettaglioCostoRicavo(userContext, docamm, dettFin, aContoEconomico, importoAnnoCorrenteESuccessivi, tsDtDaCompetenza, tsDtACompetenza);
					dettPN.setModificabile(Boolean.TRUE);
                    dettPNList.add(dettPN);
				}
			} catch (ComponentException | RemoteException e) {
				throw new ApplicationRuntimeException(e);
			}
            return dettPNList;
		}

		private DettaglioPrimaNota addDettaglioCostoRicavo(UserContext userContext, IDocumentoCogeBulk docamm, DettaglioFinanziario dettFin, Voce_epBulk conto, BigDecimal importo, Timestamp dtDaCompetenzaCoge, Timestamp dtACompetenzaCoge) {
			String mySezione = docamm.getTipoDocumentoEnum().getSezioneEconomica();
			AtomicReference<Voce_epBulk> myConto = new AtomicReference<>(conto);
			if (docamm instanceof Documento_genericoBulk) {
				IDocumentoCogeBulk documentoCausale = docamm.isDocumentoStorno()?dettFin.getPartita():docamm;
				Optional.ofNullable(documentoCausale)
						.filter(Documento_genericoBulk.class::isInstance)
						.map(Documento_genericoBulk.class::cast)
						.flatMap(documentoGenericoBulk -> Optional.ofNullable(documentoGenericoBulk.getCausaleContabile()))
						.ifPresent(causaleContabileBulk -> {
							try {
								List<AssCausaleVoceEPBulk> assCausaleVoce = new BulkList<>(find(userContext, CausaleContabileBulk.class, "findAssCausaleVoceEPBulk", userContext, causaleContabileBulk.getCdCausale()));
								assCausaleVoce.stream().filter(el->el.getTiSezione().equals(docamm.isDocumentoStorno()?Movimento_cogeBulk.getControSezione(mySezione):mySezione)).findAny().ifPresent(el-> myConto.set(el.getVoceEp()));
							} catch (ComponentException ignored) {
							}
						});
			}
			DettaglioPrimaNota dettaglioPrimaNota = this.addDettaglio(userContext, null, mySezione, myConto.get(), importo, null, null, null, dtDaCompetenzaCoge, dtACompetenzaCoge, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
			dettaglioPrimaNota.setDettaglioFinanziario(dettFin);
			return dettaglioPrimaNota;
		}

		public void openDettaglioPatrimonialePartita(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo) {
			openDettaglioPatrimonialePartita(userContext, docamm, partita, conto, importo, cdTerzo, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
		}

		public void openDettaglioPatrimonialePartita(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, boolean isModificabile, boolean isAccorpabile) {
			AtomicReference<Voce_epBulk> myConto = new AtomicReference<>(conto);
			if (docamm instanceof Documento_genericoBulk) {
				IDocumentoCogeBulk documentoCausale = docamm.isDocumentoStorno()?partita:docamm;
				Optional.ofNullable(documentoCausale)
						.filter(Documento_genericoBulk.class::isInstance)
						.map(Documento_genericoBulk.class::cast)
						.flatMap(documentoGenericoBulk -> Optional.ofNullable(documentoGenericoBulk.getCausaleContabile()))
						.ifPresent(causaleContabileBulk -> {
							String mySezione = docamm.getTipoDocumentoEnum().getSezionePatrimoniale();
							try {
								List<AssCausaleVoceEPBulk> assCausaleVoce = new BulkList<>(find(userContext, CausaleContabileBulk.class, "findAssCausaleVoceEPBulk", userContext, causaleContabileBulk.getCdCausale()));
								assCausaleVoce.stream().filter(el->el.getTiSezione().equals(docamm.isDocumentoStorno()?Movimento_cogeBulk.getControSezione(mySezione):mySezione)).findAny().ifPresent(el-> myConto.set(el.getVoceEp()));
							} catch (ComponentException ignored) {
							}
						});
			}
			DettaglioPrimaNota dettPN = openDettaglioPatrimoniale(userContext, docamm, myConto.get(), importo, Boolean.TRUE, cdTerzo, null, isModificabile, isAccorpabile);
			dettPN.setPartita(partita);
		}

		public void openDettaglioPatrimonialeCori(UserContext userContext, IDocumentoCogeBulk docamm, String cdConto, BigDecimal importo, Integer cdTerzo, String cdCori) {
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				Voce_epBulk conto =  (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(cdConto, CNRUserContext.getEsercizio(userContext)));
				this.openDettaglioPatrimonialeCori(userContext, docamm, conto, importo, cdTerzo, cdCori);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}

		public void openDettaglioPatrimonialeCori(UserContext userContext, IDocumentoCogeBulk docamm, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, String cdCori) {
			openDettaglioPatrimoniale(userContext, docamm, conto, importo, Boolean.TRUE, cdTerzo, cdCori);
		}

		public void openDettaglioPatrimoniale(UserContext userContext, IDocumentoCogeBulk docamm, Voce_epBulk conto, BigDecimal importo, boolean registraPartita, Integer cdTerzo, String cdCori) {
			this.openDettaglioPatrimoniale(userContext, docamm, conto, importo, registraPartita, cdTerzo, cdCori, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
		}

		public DettaglioPrimaNota openDettaglioPatrimoniale(UserContext userContext, IDocumentoCogeBulk docamm, Voce_epBulk conto, BigDecimal importo, boolean registraPartita, Integer cdTerzo, String cdCori, boolean isModificabile, boolean isAccorpabile) {
			DettaglioPrimaNota dettPN = this.addDettaglioPatrimoniale(userContext, docamm, conto, importo, registraPartita, cdTerzo, cdCori, Boolean.TRUE);
			dettPN.setModificabile(isModificabile);
			dettPN.setAccorpabile(isAccorpabile);
			return dettPN;
		}

		public void closeDettaglioPatrimonialePartita(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, boolean isModificabile) {
			this.closeDettaglioPatrimonialePartita(userContext, docamm, partita, conto, importo, cdTerzo, isModificabile, DEFAULT_ACCORPABILE);
		}

		public void closeDettaglioPatrimonialePartita(UserContext userContext, IDocumentoCogeBulk docamm, IDocumentoCogeBulk partita, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, boolean isModificabile, boolean isAccorpabile) {
			DettaglioPrimaNota dettPN = this.closeDettaglioPatrimoniale(userContext, docamm, conto, importo, Boolean.TRUE, cdTerzo, null, isModificabile, isAccorpabile);
			dettPN.setPartita(partita);
		}

		public void closeDettaglioPatrimonialeCori(UserContext userContext, IDocumentoCogeBulk docamm, String cdConto, BigDecimal importo, Integer cdTerzo, String cdCori) {
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				Voce_epBulk conto =  (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(cdConto, CNRUserContext.getEsercizio(userContext)));
				this.closeDettaglioPatrimoniale(userContext, docamm, conto, importo, true, cdTerzo, cdCori, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}

		public DettaglioPrimaNota closeDettaglioPatrimoniale(UserContext userContext, IDocumentoCogeBulk docamm, Voce_epBulk conto, BigDecimal importo, boolean registraPartita, Integer cdTerzo, String cdCori, boolean isModificabile, boolean isAccorpabile) {
			DettaglioPrimaNota dettPN = this.addDettaglioPatrimoniale(userContext, docamm, conto, importo, registraPartita, cdTerzo, cdCori,false);
			dettPN.setModificabile(isModificabile);
			dettPN.setAccorpabile(isAccorpabile);
			return dettPN;
		}

		public DettaglioPrimaNota addDettaglioPatrimoniale(UserContext userContext, IDocumentoCogeBulk docamm, Voce_epBulk conto, BigDecimal importo, boolean registraPartita, Integer cdTerzo, String cdCori, boolean isOpen) {
			String mySezione = isOpen?docamm.getTipoDocumentoEnum().getSezionePatrimoniale():Movimento_cogeBulk.getControSezione(docamm.getTipoDocumentoEnum().getSezionePatrimoniale());
			return this.addDettaglio(userContext, docamm.getTipoDocumentoEnum().getTipoPatrimoniale(), mySezione, conto, importo, cdTerzo, registraPartita?docamm:null, cdCori);
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo) {
			addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, null);
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, String cdConto, BigDecimal importo, Integer cdTerzo) {
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				Voce_epBulk conto =  (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(cdConto, CNRUserContext.getEsercizio(userContext)));
				addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo) {
			addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo, null);
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, String cdConto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita) {
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				Voce_epBulk conto =  (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(cdConto, CNRUserContext.getEsercizio(userContext)));
				addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo, partita);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita) {
			addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo, partita, null);
		}

		public DettaglioPrimaNota addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita, String cdCori) {
			return addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo, partita, cdCori, null, null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, String cdConto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita, String cdCori) {
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				Voce_epBulk conto =  (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(cdConto, CNRUserContext.getEsercizio(userContext)));
				addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, cdTerzo, partita, cdCori);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}

		public DettaglioPrimaNota addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, boolean isModificabile) {
			return addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, null, null, null, null, null, isModificabile, DEFAULT_ACCORPABILE);
		}

		public void addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, boolean isModificabile, boolean isAccorpabile) {
			addDettaglio(userContext, tipoDettaglio, sezione, conto, importo, null, null, null, null, null, isModificabile, isAccorpabile);
		}

		public void addDettaglio(UserContext userContext, String sezione, Voce_epBulk conto, BigDecimal importo, boolean isModificabile, boolean isAccorpabile) {
			this.addDettaglio(userContext, null, sezione, conto, importo, null, null, null, null, null, isModificabile, isAccorpabile);
		}

		private DettaglioPrimaNota addDettaglio(UserContext userContext, String tipoDettaglio, String sezione, Voce_epBulk conto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita, String cdCori, Timestamp dtDaCompetenzaCoge, Timestamp dtACompetenzaCoge, boolean isModificabile, boolean isAccorpabile) {
			String mySezione = importo.compareTo(BigDecimal.ZERO)<0?Movimento_cogeBulk.getControSezione(sezione):sezione;
			BigDecimal myImporto = importo.abs();
			String myTipoDettaglio;
			if (!Optional.ofNullable(tipoDettaglio).isPresent() ||
					Optional.of(tipoDettaglio).filter(el->el.equals(Movimento_cogeBulk.TipoRiga.CREDITO.value()) || el.equals(Movimento_cogeBulk.TipoRiga.DEBITO.value()) ||
							el.equals(Movimento_cogeBulk.TipoRiga.COSTO.value()) || el.equals(Movimento_cogeBulk.TipoRiga.RICAVO.value())).isPresent()) {
				myTipoDettaglio = getTipoDettaglioByConto(userContext, conto);

				//Confronto il tipoDettaglio da inserire con la tipologia del conto
				if (Optional.ofNullable(tipoDettaglio).isPresent() && !tipoDettaglio.equals(myTipoDettaglio)) {
					if ((Movimento_cogeBulk.TipoRiga.ATTIVITA.value().equals(myTipoDettaglio) || Movimento_cogeBulk.TipoRiga.PASSIVITA.value().equals(myTipoDettaglio)) &&
							(Movimento_cogeBulk.TipoRiga.CREDITO.value().equals(tipoDettaglio) || Movimento_cogeBulk.TipoRiga.DEBITO.value().equals(tipoDettaglio)))
						myTipoDettaglio = tipoDettaglio;
				}
			} else
				myTipoDettaglio = tipoDettaglio;
			DettaglioPrimaNota dettPN = new DettaglioPrimaNota(myTipoDettaglio, mySezione, conto.getCd_voce_ep(), myImporto, cdTerzo, partita, cdCori, dtDaCompetenzaCoge, dtACompetenzaCoge, isModificabile, isAccorpabile);
			dett.add(dettPN);
			return dettPN;
		}
	}

	private static class DettaglioPrimaNota {
		public DettaglioPrimaNota(String tipoDett, String sezione, String cdConto, BigDecimal importo, Integer cdTerzo, IDocumentoCogeBulk partita, String cdCori, Timestamp dtDaCompetenzaCoge, Timestamp dtACompetenzaCoge, boolean modificabile, boolean accorpabile) {
			super();
			this.tipoDett = tipoDett;
			this.sezione = sezione;
			this.cdConto = cdConto;
			this.importo = importo;
			this.cdTerzo = cdTerzo;
			this.partita = partita;
			this.dtDaCompetenzaCoge = dtDaCompetenzaCoge;
			this.dtACompetenzaCoge = dtACompetenzaCoge;
			this.cdCori = cdCori;
			this.modificabile = modificabile;
			this.accorpabile = accorpabile;
		}

		private String tipoDett;
		private String sezione;
		private String cdConto;
		private BigDecimal importo;
		private IDocumentoCogeBulk partita;
		private Integer cdTerzo;
		private String cdCori;
		private Timestamp dtDaCompetenzaCoge;
		private Timestamp dtACompetenzaCoge;
		private DettaglioFinanziario dettaglioFinanziario;
		private boolean modificabile;
		private boolean accorpabile;

		public String getTipoDett() {
			return tipoDett;
		}

		public String getSezione() {
			return sezione;
		}

		public String getCdConto() {
			return cdConto;
		}

		public BigDecimal getImporto() {
			return importo;
		}

		public IDocumentoCogeBulk getPartita() {
			return partita;
		}

		public void setPartita(IDocumentoCogeBulk partita) {
			this.partita = partita;
		}

		public Integer getCdTerzo() {
			return cdTerzo;
		}

		public String getCdCori() {
			return cdCori;
		}

		public Timestamp getDtDaCompetenzaCoge() {
			return dtDaCompetenzaCoge;
		}

		public Timestamp getDtACompetenzaCoge() {
			return dtACompetenzaCoge;
		}

		public DettaglioFinanziario getDettaglioFinanziario() {
			return dettaglioFinanziario;
		}

        public void invertiSezione() {
            this.sezione = Movimento_cogeBulk.getControSezione(this.sezione);
        }

		public void setDettaglioFinanziario(DettaglioFinanziario dettaglioFinanziario) {
			this.dettaglioFinanziario = dettaglioFinanziario;
		}

		public boolean isModificabile() {
			return modificabile;
		}

		public void setModificabile(Boolean modificabile) {
			this.modificabile = modificabile;
		}

		public boolean isAccorpabile() {
			return accorpabile;
		}

		public void setAccorpabile(boolean accorpabile) {
			this.accorpabile = accorpabile;
		}

		public boolean isDettaglioIva() {
			return Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_VENDITE.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO_SPLIT.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_VENDITE_SPLIT.value().equals(this.getTipoDett());
		}

		public boolean isDettaglioCostoRicavo() {
			return Movimento_cogeBulk.TipoRiga.COSTO.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.RICAVO.value().equals(this.getTipoDett());
		}

		public boolean isDettaglioPatrimoniale() {
			return Movimento_cogeBulk.TipoRiga.DEBITO.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.CREDITO.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO_SPLIT.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_VENDITE.value().equals(this.getTipoDett()) ||
					Movimento_cogeBulk.TipoRiga.IVA_VENDITE_SPLIT.value().equals(this.getTipoDett());
		}

		public boolean isSezioneDare() {
			return Movimento_cogeBulk.SEZIONE_DARE.equals(this.getSezione());
		}

		public boolean isSezioneAvere() {
			return Movimento_cogeBulk.SEZIONE_AVERE.equals(this.getSezione());
		}
	}

	private static class Partita implements IDocumentoCogeBulk {
		public Partita(String cd_tipo_doc, String cd_cds, String cd_uo, Integer esercizio, Long pg_doc, Integer cd_terzo, TipoDocumentoEnum tipoDocumentoEnum) {
			super();
			this.cd_tipo_doc = cd_tipo_doc;
			this.cd_cds = cd_cds;
			this.cd_uo = cd_uo;
			this.esercizio = esercizio;
			this.pg_doc = pg_doc;
			this.cd_terzo = cd_terzo;
			this.tipoDocumentoEnum = tipoDocumentoEnum;
		}

		String cd_tipo_doc;

		String cd_cds;

		String cd_uo;

		Integer esercizio;

		Long pg_doc;

		Integer cd_terzo;

		TipoDocumentoEnum tipoDocumentoEnum;

		Scrittura_partita_doppiaBulk scrittura_partita_doppia;

		Timestamp dt_contabilizzazione;

		@Override
		public String getCd_tipo_doc() {
			return cd_tipo_doc;
		}

		@Override
		public String getCd_cds() {
			return cd_cds;
		}

		@Override
		public String getCd_uo() {
			return cd_uo;
		}

		@Override
		public Integer getEsercizio() {
			return esercizio;
		}

		@Override
		public Long getPg_doc() {
			return pg_doc;
		}

		@Override
		public TipoDocumentoEnum getTipoDocumentoEnum() {
			return tipoDocumentoEnum;
		}

		public Scrittura_partita_doppiaBulk getScrittura_partita_doppia() {
			return scrittura_partita_doppia;
		}

		public void setScrittura_partita_doppia(Scrittura_partita_doppiaBulk scrittura_partita_doppia) {
			this.scrittura_partita_doppia = scrittura_partita_doppia;
		}

		@Override
		public Timestamp getDt_contabilizzazione() {
			return dt_contabilizzazione;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Partita partita = (Partita) o;
			return Objects.equals(cd_tipo_doc, partita.cd_tipo_doc) && Objects.equals(cd_cds, partita.cd_cds) && Objects.equals(cd_uo, partita.cd_uo) && Objects.equals(esercizio, partita.esercizio) && Objects.equals(pg_doc, partita.pg_doc) && tipoDocumentoEnum == partita.tipoDocumentoEnum;
		}

		@Override
		public int hashCode() {
			return Objects.hash(cd_tipo_doc, cd_cds, cd_uo, esercizio, pg_doc, tipoDocumentoEnum);
		}

		@Override
		public void setStato_coge(String stato_coge) {
		}

		@Override
		public String getStato_coge() {
			return null;
		}

		protected int calculateKeyHashCode(Object obj) {
			return obj == null ? 0 : obj.hashCode();
		}

		public int primaryKeyHashCode() {
			return calculateKeyHashCode(getCd_tipo_doc()) +
					calculateKeyHashCode(getCd_cds()) +
					calculateKeyHashCode(getCd_uo()) +
					calculateKeyHashCode(getPg_doc()) +
					calculateKeyHashCode(getTipoDocumentoEnum());
		}

		@Override
		public Timestamp getDtGenerazioneScrittura() {
			return this.getDt_contabilizzazione();
		}

		@Override
		public void setStato_coan(String stato_coan) {
		}

		@Override
		public String getStato_coan() {
			return null;
		}
	}
	/**
	 * ScritturaPartitaDoppiaComponent constructor comment.
	 */
	public ProposeScritturaComponent() {
		super();
	}

    public IDocumentoCogeBulk loadEconomicaFromFinanziaria(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        controllaFattibilitaOperazione(userContext, doccoge);
        return this.loadRigheEco(userContext, doccoge);
    }

    public ResultScrittureContabili proposeScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        return this.proposeScrittureContabili(userContext, doccoge, Boolean.FALSE);
    }

    public ResultScrittureContabili proposeScrittureContabili(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        return this.proposeScrittureContabili(userContext, doccoge, Boolean.TRUE);
    }

    private void controllaFattibilitaOperazione(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        try {
            EsercizioHome home = (EsercizioHome) getHome(userContext, EsercizioBulk.class);
            if (!home.isEsercizioAperto(doccoge.getEsercizio(), doccoge.getCd_cds()))
                throw new ScritturaPartitaDoppiaNotEnabledException("Scrittura Economica non generabile/modificabile. L'esercizio contabile " + doccoge.getEsercizio() +
                        " per il cds " + doccoge.getCd_cds() + " risulta essere non aperto.");

            if (doccoge.getTipoDocumentoEnum().isGenericoCoriVersamentoSpesa())
                throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista per il documento generico di versamento contributi/ritenute. " +
                        "La scrittura principale viene eseguita sul compenso associato.");
            else if (doccoge.getTipoDocumentoEnum().isChiusuraFondo())
                throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista per il documento generico di chiusura fondo economale. " +
                        "La scrittura principale viene eseguita sulla reversale associata.");
            else if (!doccoge.getTipoDocumentoEnum().isScritturaEconomicaRequired())
                throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista per la tipologia di documento selezionato.");
            else if (doccoge.getTipoDocumentoEnum().isCompenso()) {
                if (((CompensoBulk) doccoge).getFl_compenso_stipendi())
                    throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista per la tipologia di documento selezionato.");
            } else if (doccoge.getTipoDocumentoEnum().isMissione()) {
                MissioneBulk missione = (MissioneBulk) doccoge;
                String descMissione = missione.getEsercizio() + "/" + missione.getCd_unita_organizzativa() + "/" + missione.getPg_missione();
                //Le missioni pagate con compenso non creano scritture di prima nota in quanto create direttamente dal compenso stesso
                if (missione.getFl_associato_compenso())
                    throw new ScritturaPartitaDoppiaNotRequiredException("Missione " + descMissione + " associata a compenso. Registrazione economica non prevista.");
                if (missione.isAnnullato())
                    throw new ScritturaPartitaDoppiaNotRequiredException("Missione " + descMissione + " annullata. Registrazione economica non prevista.");
                if (!missione.isMissioneDefinitiva())
                    throw new ScritturaPartitaDoppiaNotEnabledException("Missione " + descMissione + " non definitiva. Registrazione economica non prevista.");
            }
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultScrittureContabili proposeScrittureContabili(UserContext userContext, IDocumentoCogeBulk doccoge, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
        controllaFattibilitaOperazione(userContext, doccoge);
		return this.innerProposeScrittureContabili(userContext, doccoge, Boolean.FALSE, makeAnalitica);
	}

	private Scrittura_partita_doppiaBulk innerProposeScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		return this.innerProposeScritturaPartitaDoppia(userContext, doccoge, Boolean.FALSE);
	}

	private Scrittura_partita_doppiaBulk innerProposeScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk doccoge, boolean isContabilizzaSuInviato) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		return this.innerProposeScrittureContabili(userContext, doccoge, isContabilizzaSuInviato, Boolean.FALSE).getScritturaPartitaDoppiaBulk();
	}

	private ResultScrittureContabili innerProposeScrittureContabili(UserContext userContext, IDocumentoCogeBulk doccoge, boolean isContabilizzaSuInviato, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		try{
			try {
				setSavepoint(userContext, "proposeScritturaPartitaDoppia");
                if (doccoge.getTipoDocumentoEnum().isAnticipo()) {
                    this.loadRigheEco(userContext, doccoge);
                    return this.proposeScritturaPartitaDoppiaAnticipo(userContext, (AnticipoBulk) doccoge, makeAnalitica);
                } else if (doccoge.getTipoDocumentoEnum().isRimborso())
					return new ResultScrittureContabili(doccoge, this.proposeScritturaPartitaDoppiaRimborso(userContext, (RimborsoBulk) doccoge), null);
				else if (doccoge.getTipoDocumentoEnum().isMissione()) {
                    this.loadRigheEco(userContext, doccoge);
                    return this.proposeScritturaPartitaDoppiaMissione(userContext, (MissioneBulk) doccoge, makeAnalitica);
                } else if (doccoge.getTipoDocumentoEnum().isCompenso()) {
					if (!((CompensoBulk) doccoge).getFl_compenso_stipendi()) {
                        this.loadRigheEco(userContext, doccoge);
                        return this.proposeScritturaPartitaDoppiaCompenso(userContext, (CompensoBulk) doccoge, makeAnalitica);
                    }
				} else if (doccoge.getTipoDocumentoEnum().isAperturaFondo())
					return new ResultScrittureContabili(doccoge, this.proposeScritturaPartitaDoppiaAperturaFondo(userContext, (Documento_genericoBulk) doccoge), null);
				else if (doccoge.getTipoDocumentoEnum().isGenericoStipendiSpesa() || doccoge.getTipoDocumentoEnum().isDocumentoPassivo() || doccoge.getTipoDocumentoEnum().isDocumentoAttivo()) {
                    this.loadRigheEco(userContext, doccoge);
                    return this.proposeScritturaPartitaDoppiaDocumento(userContext, (IDocumentoAmministrativoBulk) doccoge, makeAnalitica);
                } else if (doccoge.getTipoDocumentoEnum().isLiquidazioneIva())
					return new ResultScrittureContabili(doccoge, this.proposeScritturaPartitaDoppiaLiquidazioneUo(userContext, (Liquidazione_ivaBulk) doccoge), null);
                else if (doccoge.getTipoDocumentoEnum().isConsegnaOrdineAcquisto()) {
                    this.loadRigheEco(userContext, doccoge);
                    return this.proposeScritturaPartitaDoppiaOrdineConsegna(userContext, (OrdineAcqConsegnaBulk) doccoge, makeAnalitica);
                }
				else if (doccoge.getTipoDocumentoEnum().isMandato())
					return new ResultScrittureContabili(doccoge, this.proposeScritturaPartitaDoppiaMandato(userContext, (MandatoBulk) doccoge, isContabilizzaSuInviato), null);
				else if (doccoge.getTipoDocumentoEnum().isReversale())
					return new ResultScrittureContabili(doccoge, this.proposeScritturaPartitaDoppiaReversale(userContext, (ReversaleBulk) doccoge), null);
				throw new ApplicationException("Scrittura Economica non gestita per la tipologia di documento "+doccoge.getCd_tipo_doc()+" selezionato.");
			} catch (ScritturaPartitaDoppiaNotEnabledException|ScritturaPartitaDoppiaNotRequiredException e) {
				rollbackToSavepoint(userContext, "proposeScritturaPartitaDoppia");
				throw e;
			} catch (RemoteException | IntrospectionException | PersistencyException e) {
                throw new RuntimeException(e);
            }
		} catch (SQLException e) {
			throw handleException(e);
		}
	}

	public ResultScrittureContabili proposeScritturaPartitaDoppiaAnnullo(UserContext userContext, IDocumentoCogeBulk doccoge) throws ComponentException, ScritturaPartitaDoppiaNotEnabledException {
		try{
			try {
				setSavepoint(userContext, "proposeScritturaPartitaDoppiaAnnullo");
				if (doccoge.getTipoDocumentoEnum().isAnticipo()) {
					if (!((AnticipoBulk)doccoge).isAnnullato())
						throw new ScritturaPartitaDoppiaNotEnabledException("L'anticipo non risulta in stato annullato. Annullamento scrittura partita doppia non possibile!");
					Optional<Scrittura_partita_doppiaBulk> scritturaOpt = this.getScritturaPartitaDoppia(userContext, doccoge);
					if (!scritturaOpt.isPresent())
						throw new ScritturaPartitaDoppiaNotEnabledException("L'anticipo non risulta ancora collegato ad una scrittura partita doppia. Annullamento scrittura partita doppia non possibile!");
					return new ResultScrittureContabili(doccoge, this.proposeStornoScritturaPartitaDoppia(userContext, scritturaOpt.get(), ((AnticipoBulk) doccoge).getDt_cancellazione()));
				}
				throw new ApplicationException("Annullamento Scrittura Economica non gestita per la tipologia di documento "+doccoge.getCd_tipo_doc()+" selezionato.");
			} catch (ScritturaPartitaDoppiaNotEnabledException e) {
				rollbackToSavepoint(userContext, "proposeScritturaPartitaDoppiaAnnullo");
				throw e;
			}
		} catch (ApplicationException|ApplicationRuntimeException|PersistencyException e) {
			throw new NoRollbackException(e);
		} catch (SQLException e) {
			throw handleException(e);
		}
	}

	private ResultScrittureContabili proposeScritturaPartitaDoppiaDocumento(UserContext userContext, IDocumentoAmministrativoBulk docamm, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, RemoteException, IntrospectionException, PersistencyException {
        List<TestataPrimaNota> testataPrimaNotaList = this.proposeTestataPrimaNotaDocumento(userContext, docamm);
        List<TestataPrimaNota> testataPrimaNotaDocPrincList = testataPrimaNotaList.stream().filter(el->!Optional.ofNullable(el.getDoccoge()).isPresent()).collect(Collectors.toList());
        ResultScrittureContabili resultScrittureContabili = this.generaScritture(userContext, docamm, testataPrimaNotaDocPrincList, Boolean.TRUE, makeAnalitica);
        Optional<Scrittura_partita_doppiaBulk> spd = Optional.ofNullable(resultScrittureContabili.getScritturaPartitaDoppiaBulk());
        Optional<Scrittura_analiticaBulk> sa = Optional.ofNullable(resultScrittureContabili.getScritturaAnaliticaBulk());
        if (spd.isPresent() || sa.isPresent()) {
            final Optional<Timestamp> dtCancellazione;
            if (docamm instanceof Fattura_passivaBulk && docamm.isAnnullato())
                dtCancellazione = Optional.ofNullable(((Fattura_passivaBulk) docamm).getDt_cancellazione());
            else if (docamm instanceof Fattura_attivaBulk && docamm.isAnnullato())
                dtCancellazione = Optional.ofNullable(((Fattura_attivaBulk) docamm).getDt_cancellazione());
            else if (docamm instanceof Documento_genericoBulk && docamm.isAnnullato())
                dtCancellazione = Optional.ofNullable(((Documento_genericoBulk) docamm).getDt_cancellazione());
            else
                dtCancellazione = Optional.empty();
            if (dtCancellazione.isPresent()) {
                if (spd.isPresent()) {
                    spd.get().setAttiva(Scrittura_partita_doppiaBulk.ATTIVA_NO);
                    spd.get().setDt_cancellazione(dtCancellazione.get());
                }
                if (sa.isPresent()) {
                    sa.get().setAttiva(Scrittura_partita_doppiaBulk.ATTIVA_NO);
                    sa.get().setDt_cancellazione(dtCancellazione.get());
                }
            }
        }

        //Aggiungo eventuali altre scritture
        for (TestataPrimaNota testataPrimaNota : testataPrimaNotaList) {
            if (Optional.ofNullable(testataPrimaNota.getDoccoge()).isPresent()) {
                ResultScrittureContabili myResultScrittureContabili = this.generaScritture(userContext, testataPrimaNota.getDoccoge(), Collections.singletonList(testataPrimaNota), Boolean.TRUE, makeAnalitica);
                Optional<Scrittura_partita_doppiaBulk> mySpd = Optional.ofNullable(myResultScrittureContabili.getScritturaPartitaDoppiaBulk());
                Optional<Scrittura_analiticaBulk> mySa = Optional.ofNullable(myResultScrittureContabili.getScritturaAnaliticaBulk());
                mySpd.ifPresent(scritturaPartitaDoppiaBulk -> {
                    scritturaPartitaDoppiaBulk.setOrigine_scrittura(OrigineScritturaEnum.RISCONTRO_A_VALORE.name());
                    resultScrittureContabili.getOtherScritturaPartitaDoppiaBulk().add(scritturaPartitaDoppiaBulk);
                });
                mySa.ifPresent(scritturaAnaliticaBulk -> {
                    scritturaAnaliticaBulk.setOrigine_scrittura(OrigineScritturaEnum.RISCONTRO_A_VALORE.name());
                    resultScrittureContabili.getOtherScritturaAnaliticaBulk().add(scritturaAnaliticaBulk);
                });
            }
        }

		return resultScrittureContabili;
	}

	private List<TestataPrimaNota> proposeTestataPrimaNotaDocumento(UserContext userContext, IDocumentoAmministrativoBulk docamm) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, RemoteException, IntrospectionException, PersistencyException {
		List<TestataPrimaNota> testataPrimaNotaList = new ArrayList<>();

		final boolean isFatturaPassivaDaOrdini = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).filter(Fattura_passivaBulk::isDaOrdini).isPresent();
		final List<FatturaOrdineBulk> listaFatturaOrdini = new BulkList<>();

		if (docamm instanceof Fattura_passivaBulk) {
			listaFatturaOrdini.addAll(Utility.createFatturaPassivaComponentSession().findFatturaOrdini(userContext, (Fattura_passivaBulk) docamm));
			for (FatturaOrdineBulk fattordine : listaFatturaOrdini)
				fattordine.setOrdineAcqConsegna((OrdineAcqConsegnaBulk) loadObject(userContext, fattordine.getOrdineAcqConsegna()));
		}

		//Le fatture generate da compenso non creano scritture di prima nota in quanto create direttamente dal compenso stesso
		if (Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::isGenerataDaCompenso).orElse(Boolean.FALSE))
			throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non necessaria in quanto fattura collegata a compenso. La scrittura di prima nota viene creata direttamente dal compenso stesso.");

		//Carico i dettagli economici leggendoli dalle obbligazioni collegate
		List<IDocumentoAmministrativoRigaBulk> righeDocamm = this.getRigheDocamm(userContext, docamm);

		//Da questo momento non leggo più dall'obbligazione ma recupero i dati direttamente dalle tabelle economiche caricate nel processo precedente
		//Creo un oggetto con i dettagli finanziari che mi servono per creare la prima nota
		List<DettaglioFinanziario> righeDettFin = new ArrayList<>();

		for (IDocumentoAmministrativoRigaBulk rigaDocamm : righeDocamm)
			righeDettFin.add(this.convertToRigaDettFin(userContext, rigaDocamm));

		/*
		  Le fatture Commerciali con autofattura sono praticamente tutte le fatture Commerciali con Iva positiva
		  Quindi se isCommercialeWithAutofattura=true allora devo procedere a registrare IVA
		  Il flag registraIva viene quindi impostatao a true
		 */
		final Optional<AutofatturaBulk> optAutofattura;
		final boolean isSplitPayment;
		final boolean registraIva;
		if (docamm instanceof Fattura_passivaBulk) {
			if (((Fattura_passivaBulk) docamm).isCommerciale()) {
				try {
					AutofatturaHome autofatturaHome = (AutofatturaHome) getHome(userContext, AutofatturaBulk.class);
					optAutofattura = Optional.ofNullable(autofatturaHome.findFor((Fattura_passivaBulk) docamm));
				} catch (ComponentException | PersistencyException e) {
					throw new ApplicationException(e);
				}
			} else
				optAutofattura = Optional.empty();
			isSplitPayment = ((Fattura_passivaBulk) docamm).getFl_split_payment();
			registraIva = Boolean.TRUE;
		} else if (docamm instanceof Fattura_attivaBulk) {
			optAutofattura = Optional.empty();
			isSplitPayment = ((Fattura_attivaBulk)docamm).getFl_liquidazione_differita();
			registraIva = !isSplitPayment;
		} else {
			optAutofattura = Optional.empty();
			isSplitPayment = Boolean.FALSE;
			registraIva = Boolean.FALSE;
		}

		String valueAssociazioneConti = this.findValueByConfigurazioneCNR(userContext, CNRUserContext.getEsercizio(userContext), Configurazione_cnrBulk.PK_ECONOMICO_PATRIMONIALE, Configurazione_cnrBulk.SK_ASSOCIAZIONE_CONTI, 1);
		final boolean isContoPatrimonialeByTerzo = valueAssociazioneConti.equals("TERZO");

		final boolean isFatturaPassivaIstituzionale = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::isIstituzionale).orElse(Boolean.FALSE);
		final boolean isIntraUE = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFl_intra_ue).orElse(Boolean.FALSE);
		final boolean isExtraUE = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFl_extra_ue).orElse(Boolean.FALSE);
		final boolean isMerceIntraUE = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFl_merce_intra_ue).orElse(Boolean.FALSE);
		final boolean isFatturaDiBeni = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::isFatturaDiBeni).orElse(Boolean.FALSE);
		final boolean isFatturaDiServizi = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::isFatturaDiServizi).orElse(Boolean.FALSE);
		final boolean isSanMarinoSenzaIva = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFl_san_marino_senza_iva).orElse(Boolean.FALSE);
		final boolean hasAutofattura = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFl_autofattura).orElse(Boolean.FALSE);
		final boolean isServiziNonResidenti = Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getTipo_sezionale)
				.map(ts->Optional.of(ts).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
					try {
						Tipo_sezionaleHome home = (Tipo_sezionaleHome) getHome(userContext, Tipo_sezionaleBulk.class);
						return (Tipo_sezionaleBulk) home.findByPrimaryKey(ts);
					} catch (ComponentException | PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				}))
				.map(Tipo_sezionaleBulk::getFl_servizi_non_residenti).orElse(Boolean.FALSE);

		//L'iva viene registrata a costo se prevista le registrazione e se trattasi di fattura istituzionale passiva
		final boolean ivaDaRegistrareACosto = registraIva && isFatturaPassivaIstituzionale;

		final String cdCoriIva, cdCoriIvaSplit;
		final Voce_epBulk aContoIva, aContoIvaSplit;
		try {
			cdCoriIva = registraIva?this.findCodiceTributoIva(userContext):null;
			cdCoriIvaSplit = registraIva && isSplitPayment?this.findCodiceTributoIvaSplit(userContext):null;

			aContoIva = registraIva?this.findContoIva(userContext, docamm):null;
			aContoIvaSplit = registraIva && isSplitPayment?this.findContoIvaSplit(userContext, docamm):null;
		} catch (ComponentException|RemoteException|PersistencyException e) {
			throw new ApplicationRuntimeException(e);
		}

		Map<Integer, Map<Timestamp, Map<Timestamp, List<DettaglioFinanziario>>>> mapTerzo =
				righeDettFin.stream().collect(Collectors.groupingBy(DettaglioFinanziario::getCdTerzo,
						Collectors.groupingBy(DettaglioFinanziario::getDtDaCompetenzaCoge,
								Collectors.groupingBy(DettaglioFinanziario::getDtACompetenzaCoge))));

		mapTerzo.keySet().forEach(aCdTerzo -> mapTerzo.get(aCdTerzo).keySet().forEach(aDtDaCompCoge -> mapTerzo.get(aCdTerzo).get(aDtDaCompCoge).keySet().forEach(aDtACompCoge -> {
			try {
				List<DettaglioFinanziario> righeDettFinTerzo = mapTerzo.get(aCdTerzo).get(aDtDaCompCoge).get(aDtACompCoge);
				TestataPrimaNota testataPrimaNota = new TestataPrimaNota(aDtDaCompCoge, aDtACompCoge);
				testataPrimaNotaList.add(testataPrimaNota);

				final Pair<Voce_epBulk, Voce_epBulk> pairContoCostoGen;

				//Per i documenti attivi:
				// 1. se il documento prevede acconto/anticipo allora il costo sarà scaricato sul conto dei parametri
				// 2. se esistono righe con solo imposta, allora quest'ultima dovrà essere scaricata sul primo conto di ricavo individuato
				if (docamm.getTipoDocumentoEnum().isDocumentoAmministrativoAttivo() || docamm.getTipoDocumentoEnum().isGenericoEntrata()) {
					if (righeDettFinTerzo.stream()
							.filter(el -> Optional.ofNullable(el.getImImponibile()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) == 0)
							.anyMatch(el -> Optional.ofNullable(el.getImImposta()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)) {
						List<DettaglioFinanziario> righeDettFinConImponibile = righeDettFinTerzo.stream().filter(el -> el.getImImponibile().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());

                        if (!righeDettFinConImponibile.isEmpty())
							pairContoCostoGen = this.findPairCosto(userContext, righeDettFinConImponibile.get(0));
						else
							pairContoCostoGen = null;
					} else
						pairContoCostoGen = null;
				} else
					pairContoCostoGen = null;

				Map<Integer, Map<String, List<DettaglioFinanziario>>> mapVoceEp =
						righeDettFinTerzo.stream().collect(Collectors.groupingBy(rigaDettFin->rigaDettFin.getVoceEp().getEsercizio(),
								Collectors.groupingBy(rigaDettFin->rigaDettFin.getVoceEp().getCd_voce_ep())));

				mapVoceEp.keySet().forEach(aEseVoceEp -> mapVoceEp.get(aEseVoceEp).keySet().forEach(aCdVoceEp -> {
					List<DettaglioFinanziario> righeDettFinVoceEp = mapVoceEp.get(aEseVoceEp).get(aCdVoceEp);

					//suddivido per partita.... che potrebbe essere differente come nel caso di note credito/debito
					Map<String, Map<String, Map<String, Map<Integer, Map<Long, List<DettaglioFinanziario>>>>>> mapPartita =
							righeDettFinVoceEp.stream().collect(Collectors.groupingBy(rigaDettFin->rigaDettFin.getPartita().getCd_tipo_doc(),
									Collectors.groupingBy(rigaDettFin->rigaDettFin.getPartita().getCd_cds(),
											Collectors.groupingBy(rigaDettFin->rigaDettFin.getPartita().getCd_uo(),
													Collectors.groupingBy(rigaDettFin->rigaDettFin.getPartita().getEsercizio(),
															Collectors.groupingBy(rigaDettFin->rigaDettFin.getPartita().getPg_doc()))))));

					mapPartita.keySet().forEach(aCd_tipo_doc ->
							mapPartita.get(aCd_tipo_doc).keySet().forEach(aCd_cds ->
									mapPartita.get(aCd_tipo_doc).get(aCd_cds).keySet().forEach(aCd_uo ->
											mapPartita.get(aCd_tipo_doc).get(aCd_cds).get(aCd_uo).keySet().forEach(aEsercizioDoc ->
													mapPartita.get(aCd_tipo_doc).get(aCd_cds).get(aCd_uo).get(aEsercizioDoc).keySet().forEach(aPg_doc -> {
														try {
															List<DettaglioFinanziario> righeDettFinVocePartita = mapPartita.get(aCd_tipo_doc).get(aCd_cds).get(aCd_uo).get(aEsercizioDoc).get(aPg_doc);

															BigDecimal importoIva = righeDettFinVocePartita.stream()
																	.map(rigaDettFin -> Optional.ofNullable(rigaDettFin.getImImposta()).orElse(BigDecimal.ZERO))
																	.reduce(BigDecimal.ZERO, BigDecimal::add);

															//final boolean registraIvaACosto = importoIva.compareTo(BigDecimal.ZERO)!=0 && registraIva && ivaDaRegistrareACosto;
															List<DettaglioScrittura> mapPatrimonialeIva = new ArrayList<>();

															//Registro Imponibile Fattura e iva a costo se previsto
															if (isFatturaPassivaDaOrdini && !listaFatturaOrdini.isEmpty()) {
																for (DettaglioFinanziario rigaDettFin:righeDettFinVocePartita) {
																	//Individuazione conto costo....la variabile pairContoCostoGen è valorizzata se l'imponibile è presente tutto su una sola riga di fattura
																	//in quel caso il costo è derivato tutto da quell'unica riga.....
																	Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCostoRigaDocumento(userContext, rigaDettFin, pairContoCostoGen);
                                                                    IDocumentoCogeBulk partita = rigaDettFin.getPartita();

                                                                    IDocumentoAmministrativoRigaBulk rigaDocamm = (IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm();

																	final boolean registraIvaACosto = Optional.ofNullable(rigaDocamm.getIm_iva()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)!=0 &&
																			registraIva && (ivaDaRegistrareACosto ||
																			!Optional.ofNullable(rigaDocamm.getVoce_iva()).map(Voce_ivaBulk::isDetraibile).orElse(Boolean.FALSE));

																	final List<FatturaOrdineBulk> listaFatturaOrdiniCollRiga = listaFatturaOrdini.stream()
																			.filter(el->el.getFatturaPassivaRiga().equalsByPrimaryKey(rigaDocamm)).collect(Collectors.toList());

																	if (listaFatturaOrdiniCollRiga.isEmpty()) {
																		testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), rigaDocamm.getIm_imponibile());
																		testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, pairContoCosto.getSecond(), rigaDocamm.getIm_imponibile(), aCdTerzo);

																		if (registraIvaACosto) {
																			testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), rigaDocamm.getIm_iva());
																			mapPatrimonialeIva.add(new DettaglioScrittura(pairContoCosto.getSecond(), partita, rigaDocamm.getIm_iva()));
																		}
																	} else {
                                                                        for (FatturaOrdineBulk fatturaOrdineBulk:listaFatturaOrdiniCollRiga) {
                                                                            Voce_epBulk aContoContropartita;
                                                                            if (isContoPatrimonialeByTerzo)
                                                                                aContoContropartita = pairContoCosto.getSecond();
                                                                            else
                                                                                aContoContropartita = this.findContoContropartita(userContext, fatturaOrdineBulk.getOrdineAcqConsegna().getContoBulk());

                                                                            //Recupero la scrittura prima nota della consegna per recuperare il conto fatture da ricevere usato da chiudere
                                                                            List<Movimento_cogeBulk> allMovimentiPrimaNota = this.findMovimentiPrimaNota(userContext, fatturaOrdineBulk.getOrdineAcqConsegna());
                                                                            if (allMovimentiPrimaNota.isEmpty())
                                                                                throw new ScritturaPartitaDoppiaNotEnabledException("Scrittura Economica non possibile. Non risulta presente la scrittura sulla evasione della consegna "+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getEsercizio()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getCdCds()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getCdUnitaOperativa()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getCdNumeratore()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getNumero()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getRiga()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getConsegna()+".");
                                                                            if (allMovimentiPrimaNota.stream().filter(Movimento_cogeBulk::isSezioneAvere).count() != 1)
                                                                                throw new ScritturaPartitaDoppiaNotEnabledException("Scrittura Economica non possibile. Non risulta possibile individuare la riga Fattura da ricevere nella scrittura di evasione della consegna "+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getEsercizio()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getCdCds()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getCdUnitaOperativa()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getCdNumeratore()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getNumero()+"/"+fatturaOrdineBulk.getOrdineAcqConsegna().getRiga()+"/"+
                                                                                        fatturaOrdineBulk.getOrdineAcqConsegna().getConsegna()+".");

                                                                            Movimento_cogeBulk movimentoFattRic = allMovimentiPrimaNota.stream().filter(Movimento_cogeBulk::isSezioneAvere).findFirst().orElse(null);

                                                                            //Registro prima la prima nota di rettifica consegna se gli importi sono differenti
                                                                            if (fatturaOrdineBulk.getImImponibile().compareTo(fatturaOrdineBulk.getImponibilePerRigaFattura()) != 0 ||
                                                                                    (registraIvaACosto && fatturaOrdineBulk.getImIva().compareTo(fatturaOrdineBulk.getIvaPerRigaFattura())!=0)) {
                                                                                TestataPrimaNota testataPrimaNotaConsegna = new TestataPrimaNota(aDtDaCompCoge, aDtACompCoge, fatturaOrdineBulk.getOrdineAcqConsegna());
                                                                                testataPrimaNotaList.add(testataPrimaNotaConsegna);

                                                                                if (fatturaOrdineBulk.getImImponibile().compareTo(fatturaOrdineBulk.getImponibilePerRigaFattura()) > 0) {
                                                                                    testataPrimaNotaConsegna.closeDettaglioPatrimoniale(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), movimentoFattRic.getConto(), fatturaOrdineBulk.getImImponibile().subtract(fatturaOrdineBulk.getImponibilePerRigaFattura()), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                                    testataPrimaNotaConsegna.closeDettaglioCostoRicavo(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), rigaDettFin, partita, fatturaOrdineBulk, fatturaOrdineBulk.getImImponibile().subtract(fatturaOrdineBulk.getImponibilePerRigaFattura()));
                                                                                } else if (fatturaOrdineBulk.getImImponibile().compareTo(fatturaOrdineBulk.getImponibilePerRigaFattura()) < 0) {
                                                                                    testataPrimaNotaConsegna.openDettaglioPatrimoniale(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), movimentoFattRic.getConto(), fatturaOrdineBulk.getImponibilePerRigaFattura().subtract(fatturaOrdineBulk.getImImponibile()), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                                    testataPrimaNotaConsegna.openDettaglioCostoRicavo(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), rigaDettFin, partita, fatturaOrdineBulk, fatturaOrdineBulk.getImponibilePerRigaFattura().subtract(fatturaOrdineBulk.getImImponibile()));
                                                                                }
                                                                                if (registraIvaACosto) {
                                                                                    if (fatturaOrdineBulk.getImIva().compareTo(fatturaOrdineBulk.getIvaPerRigaFattura())>0) {
                                                                                        testataPrimaNotaConsegna.closeDettaglioPatrimoniale(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), movimentoFattRic.getConto(), fatturaOrdineBulk.getImIva().subtract(fatturaOrdineBulk.getIvaPerRigaFattura()), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                                        testataPrimaNotaConsegna.closeDettaglioCostoRicavo(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), rigaDettFin, partita, fatturaOrdineBulk, fatturaOrdineBulk.getImIva().subtract(fatturaOrdineBulk.getIvaPerRigaFattura()));
                                                                                    } else if (fatturaOrdineBulk.getImIva().compareTo(fatturaOrdineBulk.getIvaPerRigaFattura())<0) {
                                                                                        testataPrimaNotaConsegna.openDettaglioPatrimoniale(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), movimentoFattRic.getConto(), fatturaOrdineBulk.getIvaPerRigaFattura().subtract(fatturaOrdineBulk.getImIva()), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                                        testataPrimaNotaConsegna.openDettaglioCostoRicavo(userContext, fatturaOrdineBulk.getOrdineAcqConsegna(), rigaDettFin, partita, fatturaOrdineBulk, fatturaOrdineBulk.getIvaPerRigaFattura().subtract(fatturaOrdineBulk.getImIva()));
                                                                                    }
                                                                                }
                                                                            }

                                                                            //Quindi registro la prima nota della fattura
                                                                            testataPrimaNota.closeDettaglioPatrimoniale(userContext, docamm, movimentoFattRic.getConto(), fatturaOrdineBulk.getImponibilePerRigaFattura(), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                            testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, aContoContropartita, fatturaOrdineBulk.getImponibilePerRigaFattura(), aCdTerzo);

                                                                            if (registraIvaACosto) {
                                                                                testataPrimaNota.closeDettaglioPatrimoniale(userContext, docamm, movimentoFattRic.getConto(), fatturaOrdineBulk.getIvaPerRigaFattura(), Boolean.FALSE, movimentoFattRic.getCd_terzo(), null, DEFAULT_MODIFICABILE, DEFAULT_ACCORPABILE);
                                                                                mapPatrimonialeIva.add(new DettaglioScrittura(aContoContropartita, partita, fatturaOrdineBulk.getIvaPerRigaFattura()));
                                                                            }
                                                                        }
																	}
																}
															} else if (docamm instanceof Nota_di_creditoBulk || docamm instanceof Nota_di_credito_attivaBulk ||
																	(docamm instanceof Documento_genericoBulk && docamm.isDocumentoStorno())) {
																Optional<Scrittura_partita_doppiaBulk> scritturaNota = Optional.ofNullable(docamm.getScrittura_partita_doppia())
																		.map(spd->Optional.of(spd).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED)).orElseGet(()-> {
																			try {
																				Scrittura_partita_doppiaHome home = (Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class);
																				return home.getScrittura(userContext, docamm, Boolean.FALSE);
																			} catch (ComponentException e) {
																				throw new DetailedRuntimeException(e);
																			}
																		});

																righeDettFinVocePartita.forEach(rigaDettFin->{
																	try {
																		//Individuazione conto costo....la variabile pairContoCostoGen è valorizzata se l'imponibile è presente tutto su una sola riga di fattura
																		//in quel caso il costo è derivato tutto da quell'unica riga.....
																		Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCostoRigaDocumento(userContext, rigaDettFin, pairContoCostoGen);
                                                                        IDocumentoCogeBulk partita = rigaDettFin.getPartita();

																		Optional<Scrittura_partita_doppiaBulk> scritturaPartita = Optional.ofNullable(rigaDettFin.getPartita().getScrittura_partita_doppia())
																			.map(spd->Optional.of(spd).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED)).orElseGet(()-> {
																				try {
																					Scrittura_partita_doppiaHome home = (Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class);
																					return home.getScrittura(userContext, rigaDettFin.getPartita(), Boolean.FALSE);
																				} catch (ComponentException e) {
																					throw new DetailedRuntimeException(e);
																				}
																			});

                                                                        Voce_ivaBulk pVoceIva = ((IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm()).getVoce_iva();
																		final boolean registraIvaACosto = Optional.ofNullable(rigaDettFin.getImImposta()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)!=0 &&
																				registraIva && (ivaDaRegistrareACosto ||
																				!Optional.ofNullable(pVoceIva).map(Voce_ivaBulk::isDetraibile).orElse(Boolean.FALSE));

																		//se esiste scrittura partita vuol dire che non si tratta di partita migrata....recupero i conti patrimoniali leggendo la fattura originaria
																		//valutando se da ordine o meno
																		if (scritturaPartita.isPresent()) {
																			//recupero tutti i movimenti della partita cui è legato la nota credito
																			IDocumentoAmministrativoBulk fatturaCollegata = (IDocumentoAmministrativoBulk)loadObject(userContext, (OggettoBulk)rigaDettFin.getPartita());

																			final boolean isFatturaPassivaNotaDaOrdini = Optional.of(fatturaCollegata)
																					.filter(Fattura_passivaBulk.class::isInstance)
																					.map(Fattura_passivaBulk.class::cast)
																					.filter(Fattura_passivaBulk::isDaOrdini).isPresent();

																			final List<FatturaOrdineBulk> listaFatturaNotaOrdini = new BulkList<>();

																			if (isFatturaPassivaNotaDaOrdini) {
																				listaFatturaNotaOrdini.addAll(Utility.createFatturaPassivaComponentSession().findFatturaOrdini(userContext, (Fattura_passivaBulk)fatturaCollegata));
																				for (FatturaOrdineBulk fattordine : listaFatturaNotaOrdini)
																					fattordine.setOrdineAcqConsegna((OrdineAcqConsegnaBulk) loadObject(userContext, fattordine.getOrdineAcqConsegna()));
																			}

																			final List<FatturaOrdineBulk> listaFatturaOrdiniCollRiga = listaFatturaNotaOrdini.stream()
																					.filter(el->el.getFatturaPassivaRiga().equalsByPrimaryKey(((Nota_di_credito_rigaBulk)rigaDettFin.getRigaDocamm()).getRiga_fattura_associata())).collect(Collectors.toList());

																			if (listaFatturaOrdiniCollRiga.isEmpty()) {
																				DettaglioFinanziario rigaDettFinCollegata = this.convertToRigaDettFin(userContext, rigaDettFin.getRigaPartita());
																				Pair<Voce_epBulk, Voce_epBulk> pairContoCostoNota = this.findPairCostoRigaDocumento(userContext, rigaDettFinCollegata, null);

																				//Se fattura originaria è di residuo metto come conto di costo il fatture da ricevere
																				GregorianCalendar calDataDa = new GregorianCalendar();
																				calDataDa.setTimeInMillis(rigaDettFin.getRigaPartita().getDt_da_competenza_coge().getTime());

																				GregorianCalendar calDataA = new GregorianCalendar();
																				calDataA.setTimeInMillis(rigaDettFin.getRigaPartita().getDt_a_competenza_coge().getTime());

																				//Se residuo metto il conto fatture da ricevere
																				if (rigaDettFin.getPartita().getEsercizio().equals(rigaDettFin.getDocamm().getEsercizio()) &&
																						(calDataDa.get(Calendar.YEAR)<rigaDettFin.getPartita().getEsercizio() || calDataA.get(Calendar.YEAR)<rigaDettFin.getPartita().getEsercizio()))
																					pairContoCostoNota = Pair.of(findContoFattureDaRicevere(userContext, docamm.getEsercizio()), pairContoCostoNota.getSecond());

																				testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCostoNota.getFirst(), rigaDettFin.getImImponibile());
																				testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, pairContoCostoNota.getSecond(), rigaDettFin.getImImponibile(), aCdTerzo);

																				if (registraIvaACosto) {
																					testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCostoNota.getFirst(), rigaDettFin.getImImposta());
																					mapPatrimonialeIva.add(new DettaglioScrittura(pairContoCostoNota.getSecond(), partita, rigaDettFin.getImImposta()));
																				}
																			} else {
																				FatturaOrdineBulk fatturaOrdineBulk = listaFatturaOrdiniCollRiga.stream().findAny().get();
																				Voce_epBulk aContoContropartita;
																				if (isContoPatrimonialeByTerzo)
																					aContoContropartita = pairContoCosto.getSecond();
																				else
																					aContoContropartita = this.findContoContropartita(userContext, fatturaOrdineBulk.getOrdineAcqConsegna().getContoBulk());

																				testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, fatturaOrdineBulk, rigaDettFin.getImImponibile());
																				testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, aContoContropartita, rigaDettFin.getImImponibile(), aCdTerzo);

																				if (registraIvaACosto) {
																					testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, fatturaOrdineBulk, rigaDettFin.getImImposta());
																					mapPatrimonialeIva.add(new DettaglioScrittura(aContoContropartita, partita, rigaDettFin.getImImposta()));
																				}
																			}
																		} else {
																			Map<String, Pair<String, BigDecimal>> saldiPartita = this.getSaldiMovimentiPartita(userContext, rigaDettFin.getPartita(), aCdTerzo, scritturaNota);

																			if (saldiPartita.isEmpty())
																				throw new ApplicationException("Errore nell'individuazione del conto patrimoniale da utilizzare per la scrittura di chiusura del debito " +
																						"del documento associato " + rigaDettFin.getPartita().getCd_tipo_doc() + "/" + rigaDettFin.getPartita().getEsercizio() + "/" + rigaDettFin.getPartita().getCd_uo() + "/" + rigaDettFin.getPartita().getPg_doc() + ": non risulta" +
																						" movimentato nessun conto patrimoniale.");
																			else if (saldiPartita.size()>1)
																				throw new ApplicationException("Errore nell'individuazione del conto patrimoniale da utilizzare per la scrittura di chiusura del debito " +
																						"del documento associato " + rigaDettFin.getPartita().getCd_tipo_doc() + "/" + rigaDettFin.getPartita().getEsercizio() + "/" + rigaDettFin.getPartita().getCd_uo() + "/" + rigaDettFin.getPartita().getPg_doc() + ": risultano" +
																						" movimentati troppi conti patrimoniali.");

																			String cdVocePatrimoniale = saldiPartita.keySet().stream().findAny().orElse(null);
																			Voce_epBulk vocePatrimoniale = (Voce_epBulk)this.loadObject(userContext, new Voce_epBulk(cdVocePatrimoniale, docamm.getEsercizio()));

																			//recupero il contro patrimoniale dalle scritture della partita
																			testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), rigaDettFin.getImImponibile());
																			testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, vocePatrimoniale, rigaDettFin.getImImponibile(), aCdTerzo);

																			if (registraIvaACosto) {
																				testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), rigaDettFin.getImImposta());
																				mapPatrimonialeIva.add(new DettaglioScrittura(vocePatrimoniale, partita, rigaDettFin.getImImposta()));
																			}
																		}
																	} catch (ComponentException | PersistencyException | RemoteException | IntrospectionException e) {
	                                                                	throw new RuntimeException(e);
																	}
                                                                });
															} else {
																for (DettaglioFinanziario rigaDettFin:righeDettFinVocePartita) {
																	//Individuazione conto costo....la variabile pairContoCostoGen è valorizzata se l'imponibile è presente tutto su una sola riga di fattura
																	//in quel caso il costo è derivato tutto da quell'unica riga.....
																	Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCostoRigaDocumento(userContext, rigaDettFin, pairContoCostoGen);
                                                                    IDocumentoCogeBulk partita = rigaDettFin.getPartita();
                                                                    BigDecimal pImImponibile = ((IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm()).getIm_imponibile();
                                                                    BigDecimal pImImposta = ((IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm()).getIm_iva();

																	testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), pImImponibile);
																	testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, partita, pairContoCosto.getSecond(), pImImponibile, aCdTerzo);

                                                                    Voce_ivaBulk pVoceIva = ((IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm()).getVoce_iva();
                                                                    final boolean registraIvaACosto = Optional.ofNullable(pImImposta).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)!=0 &&
																			registraIva && (ivaDaRegistrareACosto ||
																			!Optional.ofNullable(pVoceIva).map(Voce_ivaBulk::isDetraibile).orElse(Boolean.FALSE));

																	if (registraIvaACosto) {
																		testataPrimaNota.openDettaglioCostoRicavo(userContext, docamm, rigaDettFin, partita, pairContoCosto.getFirst(), pImImposta);
																		mapPatrimonialeIva.add(new DettaglioScrittura(pairContoCosto.getSecond(), partita, pImImposta));
																	}
																}
															}

															//Il flag registraIva è sempre impostato a true se fattura istituzionale o isCommercialeWithAutofattura
															if (importoIva.compareTo(BigDecimal.ZERO)!=0 && registraIva) {
																righeDettFinVocePartita.stream()
																		.filter(rigaDettFin -> Optional.ofNullable(rigaDettFin.getImImposta()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)!=0)
																		.forEach(rigaDettFin->{
																	//Individuazione conto costo....la variabile pairContoCostoGen è valorizzata se l'imponibile è presente tutto su una sola riga di fattura
																	//in quel caso il costo è derivato tutto da quell'unica riga.....
																	Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCostoRigaDocumento(userContext, rigaDettFin, pairContoCostoGen);
                                                                            IDocumentoCogeBulk partita = rigaDettFin.getPartita();

																	BigDecimal imIva = rigaDettFin.getImImposta();
                                                                    Voce_ivaBulk pVoceIva = ((IDocumentoAmministrativoRigaBulk)rigaDettFin.getRigaDocamm()).getVoce_iva();

                                                                    if (!(ivaDaRegistrareACosto || !pVoceIva.isDetraibile())) {
																		testataPrimaNota.openDettaglioIva(userContext, docamm, partita, aContoIva, imIva, aCdTerzo, cdCoriIva);
																		mapPatrimonialeIva.add(new DettaglioScrittura(pairContoCosto.getSecond(), partita, imIva));
																	}
																});

																//registro il patrimoniale dell'iva
																mapPatrimonialeIva.forEach(el->
																	testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, el.getPartita(), el.getConto(), el.getImporto(), aCdTerzo)
																);

																//Se intraUE o extraUE sposto l'IVA anzichè darla al Fornitore (quindi chiudo il debito) la rilevo come debito verso Erario
																if (isIntraUE || isExtraUE || hasAutofattura) {
																	if (optAutofattura.isPresent()) {
																		Voce_epBulk aContoIvaAutofattura = this.findContoIva(userContext, optAutofattura.get());
																		mapPatrimonialeIva.forEach(el-> {
																			testataPrimaNota.closeDettaglioPatrimonialePartita(userContext, docamm, el.getPartita(), el.getConto(), el.getImporto(), aCdTerzo, DEFAULT_MODIFICABILE);
																			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.IVA_VENDITE.value(), docamm.getTipoDocumentoEnum().getSezionePatrimoniale(), aContoIvaAutofattura, el.getImporto(), aCdTerzo, docamm, cdCoriIva);
																		});
																	}
																}

																if (isFatturaPassivaIstituzionale) {
																	if ((isFatturaDiBeni && (isSanMarinoSenzaIva || isIntraUE || isMerceIntraUE)) ||
																			(isFatturaDiServizi && isServiziNonResidenti)) {
																		mapPatrimonialeIva.forEach(el-> {
																			testataPrimaNota.closeDettaglioPatrimonialePartita(userContext, docamm, el.getPartita(), el.getConto(), el.getImporto(), aCdTerzo, DEFAULT_MODIFICABILE);
																			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value(), docamm.getTipoDocumentoEnum().getSezionePatrimoniale(), aContoIva, el.getImporto(), aCdTerzo, docamm, cdCoriIva);
																		});
																	}
																}

																if (isSplitPayment) {
																	//Rilevo il conto IVA Credito/Debito di tipo SPLIT (a secondo se doc attivo o passivo) e lo compenso con il debito verso il fornitore
																	mapPatrimonialeIva.forEach(el-> {
																		testataPrimaNota.closeDettaglioIvaSplit(userContext, docamm, el.getPartita(), aContoIvaSplit, el.getImporto(), aCdTerzo, cdCoriIvaSplit);
																		testataPrimaNota.closeDettaglioPatrimonialePartita(userContext, docamm, el.getPartita(), el.getConto(), el.getImporto(), aCdTerzo, DEFAULT_MODIFICABILE);
																	});
																}
															}
														} catch (ComponentException|PersistencyException|RemoteException|ScritturaPartitaDoppiaNotEnabledException e) {
															throw new ApplicationRuntimeException(e);
                                                        }
                                                    })))));
				}));
			} catch (ComponentException|PersistencyException|RemoteException e) {
				throw new ApplicationRuntimeException(e);
			}
		})));

		//Se intraUe o ExtraUe senza Merce da paesi IntraUE devo controllare lettera pagamento
		if (isIntraUE || (isExtraUE && !isMerceIntraUE)) {
			Optional<Lettera_pagam_esteroBulk> optLetteraPagamento = Optional.of(docamm)
					.map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getLettera_pagamento_estero)
					.map(lp->Optional.of(lp).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
						try {
							Lettera_pagam_esteroHome home = (Lettera_pagam_esteroHome) getHome(userContext, Lettera_pagam_esteroBulk.class);
							return (Lettera_pagam_esteroBulk) home.findByPrimaryKey(lp);
						} catch (ComponentException | PersistencyException e) {
							throw new DetailedRuntimeException(e);
						}
					}));

			if (optLetteraPagamento.flatMap(el->Optional.ofNullable(el.getCd_sospeso())).isPresent() && !testataPrimaNotaList.isEmpty()) {
				TerzoBulk terzoFattura = Optional.of(docamm).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::getFornitore).orElse(null);
				if (isFatturaPassivaDaOrdini)
					throw new ApplicationException("Attenzione! Documento contabile " +docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
							" da ordini con lettera di pagamento. Scrittura prima nota non possibile in quanto tipologia non gestita!");
				if (testataPrimaNotaList.size()>1)
					throw new ApplicationException("Documento contabile " +	docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
							" associato ad una lettera di pagamento con più terzi. Proposta prima nota non possibile.");

				TestataPrimaNota testataPrimaNota = testataPrimaNotaList.get(0);
				Fattura_passiva_rigaHome rigaHome = (Fattura_passiva_rigaHome) getHome(userContext, Fattura_passiva_rigaBulk.class);

				righeDocamm.stream().filter(Fattura_passiva_rigaBulk.class::isInstance)
						.map(Fattura_passiva_rigaBulk.class::cast)
						.filter(el->Optional.ofNullable(el.getRigheEconomica()).orElse(new BulkList<>()).isEmpty())
						.forEach(fpr->{
					try {
						fpr.setRigheEconomica(new BulkList<>(rigaHome.findFatturaPassivaRigheEcoList(fpr)));
						fpr.getRigheEconomica().forEach(el2->el2.setFattura_passiva_riga(fpr));
					} catch (PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				});

				Map<Voce_epBulk, List<Fattura_passiva_rigaBulk>> mapConto =
						righeDocamm.stream()
								.filter(Fattura_passiva_rigaBulk.class::isInstance).map(Fattura_passiva_rigaBulk.class::cast)
								.filter(el->Optional.ofNullable(el.getRigheEconomica()).orElse(new BulkList<>()).isEmpty())
								.collect(Collectors.groupingBy(Fattura_passiva_rigaBulk::getVoce_ep));

				BigDecimal totDocContabile = mapConto.values().stream().flatMap(Collection::stream).map(Fattura_passiva_rigaBulk::getImCostoEcoRipartito)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal totIva = righeDocamm.stream().map(IDocumentoAmministrativoRigaBulk::getIm_iva).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal totDaControllare = optLetteraPagamento.get().getIm_pagamento();
				final boolean ivaAdded;
				if (((Fattura_passivaBulk)docamm).isIstituzionale() &&
						(((Fattura_passivaBulk)docamm).isFatturaDiServizi() ||
						(isIntraUE && ((Fattura_passivaBulk)docamm).isFatturaDiBeni()))) {
					totDaControllare = totDaControllare.add(totIva);
					ivaAdded = Boolean.TRUE;
				} else
					ivaAdded = Boolean.FALSE;

				if (totDaControllare.compareTo(totDocContabile) != 0)
					throw new ApplicationException("L'importo totale (" + new EuroFormat().format(totDocContabile) +
							") delle scadenze obbligazioni associate alle righe del documento " +
							docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
							" è diverso dall'importo della lettera di pagamento associata "+(ivaAdded?"aumentata dell'Iva":"") +
							" (" + new EuroFormat().format(totDaControllare) +").");

                for (Voce_epBulk conto : mapConto.keySet()) {
                    try {
                        BigDecimal totFattura = mapConto.get(conto).stream().map(Fattura_passiva_rigaBase::getIm_imponibile).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal totStorni = BigDecimal.ZERO;
                        for (Fattura_passiva_rigaBulk rigaFattura : mapConto.get(conto)) {
                            if (rigaFattura instanceof Fattura_passiva_rigaIBulk) {
                                Nota_di_credito_rigaHome home = (Nota_di_credito_rigaHome) getHome(userContext, Nota_di_credito_rigaBulk.class);
                                List<Nota_di_credito_rigaBulk> righeNdC = home.findRigaFor((Fattura_passiva_rigaIBulk) rigaFattura);
                                totStorni = totStorni.add(righeNdC.stream().map(Nota_di_credito_rigaBulk::getIm_imponibile).reduce(BigDecimal.ZERO, BigDecimal::add));
                                if (ivaAdded)
                                    totStorni = totStorni.add(righeNdC.stream().map(Nota_di_credito_rigaBulk::getIm_iva).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                        }
                        if (ivaAdded)
                            totFattura = totFattura.add(mapConto.get(conto).stream().map(Fattura_passiva_rigaBase::getIm_iva).reduce(BigDecimal.ZERO, BigDecimal::add));

                        BigDecimal totCostoEco = mapConto.get(conto).stream().map(Fattura_passiva_rigaBulk::getImCostoEcoRipartito)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal diffCambio = totCostoEco.add(totStorni).subtract(totFattura);
                        if (diffCambio.compareTo(BigDecimal.ZERO) != 0) {
                            Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCosto(userContext, terzoFattura, conto, docamm.getTipoDocumentoEnum().getTipoPatrimoniale());

                            if (diffCambio.compareTo(BigDecimal.ZERO) > 0) {
                                Voce_epBulk contoCambio = this.findContoPerditeCambi(userContext, docamm.getEsercizio());
                                //Rilevo la perdita da cambio e lo compenso con la partita
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCambio, diffCambio, DEFAULT_MODIFICABILE, Boolean.FALSE);
                                testataPrimaNota.openDettaglioPatrimonialePartita(userContext, docamm, docamm, pairContoCosto.getSecond(), diffCambio, terzoFattura.getCd_terzo(),
                                        DEFAULT_MODIFICABILE, Boolean.FALSE);
                                //Chiudo la perdita da cambio e lo compenso con il costo
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.SEZIONE_DARE, pairContoCosto.getFirst(), diffCambio, DEFAULT_MODIFICABILE, Boolean.FALSE);
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoCambio, diffCambio, DEFAULT_MODIFICABILE, Boolean.FALSE);
                            } else {
                                Voce_epBulk contoCambio = this.findContoUtileCambi(userContext, docamm.getEsercizio());
                                //Rilevo l'utile da cambio e lo compenso con la partita
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.RICAVO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoCambio, diffCambio.abs(), DEFAULT_MODIFICABILE, Boolean.FALSE);
                                testataPrimaNota.closeDettaglioPatrimonialePartita(userContext, docamm, docamm, pairContoCosto.getSecond(), diffCambio.abs(), terzoFattura.getCd_terzo(),
                                        DEFAULT_MODIFICABILE, Boolean.FALSE);
                                //Chiudo l'utile da cambio e lo compenso con il costo
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.SEZIONE_AVERE, pairContoCosto.getFirst(), diffCambio.abs(), DEFAULT_MODIFICABILE, Boolean.FALSE);
                                testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.RICAVO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCambio, diffCambio.abs(), DEFAULT_MODIFICABILE, Boolean.FALSE);
                            }
                        }
                    } catch (ComponentException | PersistencyException | RemoteException e) {
                        throw new DetailedRuntimeException(e);
                    }
                }
            }
		}

		return testataPrimaNotaList;
	}

	private ResultScrittureContabili proposeScritturaPartitaDoppiaCompenso(UserContext userContext, CompensoBulk compenso, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException {
		try {
            List<DettaglioAnalitico> dettagliAnaliticos = compenso.getRigheEconomica().stream().map(DettaglioAnalitico::new).collect(Collectors.toList());

			String descCompenso = compenso.getEsercizio() + "/" + compenso.getCd_cds() + "/" + compenso.getPg_compenso();

			List<Contributo_ritenutaBulk> righeCori = Optional.ofNullable(compenso.getChildren()).orElseGet(()->{
				try {
					Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
					return (List<Contributo_ritenutaBulk>)home.loadContributiRitenute(compenso);
				} catch (ComponentException | PersistencyException e) {
					throw new DetailedRuntimeException(e);
				}
			});

			BigDecimal imContributiCaricoEnte = righeCori.stream().filter(Contributo_ritenutaBulk::isContributoEnte)
					.map(Contributo_ritenutaBulk::getAmmontare)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (compenso.getIm_lordo_percipiente().compareTo(BigDecimal.ZERO)==0 && imContributiCaricoEnte.compareTo(BigDecimal.ZERO)==0)
				throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non necessaria in quanto sia il lordo percipiente che i contributi a carico ente sono nulli.");

			final Optional<Pair<Voce_epBulk, Voce_epBulk>> pairContoCostoCompenso;

			if (compenso.getVoce_ep()==null) {
				if (compenso.getIm_lordo_percipiente().compareTo(BigDecimal.ZERO) > 0 || imContributiCaricoEnte.compareTo(BigDecimal.ZERO) > 0)
					throw new ApplicationException("Compenso " + descCompenso + " con lordo percipiente e/o contributi a carico ente senza indicazione del conto di costo. " +
							"Scrittura prima nota non possibile per impossibilità ad individuare il conto di costo!");
				pairContoCostoCompenso = Optional.empty();
			} else
				pairContoCostoCompenso = Optional.ofNullable(this.findPairCosto(userContext, compenso));

			Voce_epBulk contoCostoCompenso = pairContoCostoCompenso.map(Pair::getFirst).orElse(null);
			Voce_epBulk contoPatrimonialeCompenso = pairContoCostoCompenso.map(Pair::getSecond).orElse(null);

			TestataPrimaNota testataPrimaNota = new TestataPrimaNota(compenso.getDt_da_competenza_coge(), compenso.getDt_a_competenza_coge());

			//Nel caso dei compensi rilevo subito il costo prelevando le informazioni dalla riga del compenso stesso
			//Registrazione conto COSTO COMPENSO
			if (compenso.getIm_lordo_percipiente().compareTo(BigDecimal.ZERO)!=0) {
				if (!pairContoCostoCompenso.isPresent())
					throw new ApplicationException("Compenso " + descCompenso + " con lordo percipiente non nullo. Scrittura prima nota non possibile per impossibilità ad individuare il conto di costo!");
				DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoCompenso, compenso.getIm_lordo_percipiente(), Boolean.TRUE);
				//COMPLETO CON I DATI ANALITICI - TRATTANDOSI DI MISSIONE CON ANTICIPO CERCO PER IMPORTO PER FAR COLLEGARE LA RIGA GIUSTA
				if (makeAnalitica)
					this.completeWithDatiAnalitici(userContext,dettPN,compenso,dettagliAnaliticos,Boolean.FALSE);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeCompenso, compenso.getIm_lordo_percipiente(), compenso.getCd_terzo(), compenso);
			}

			//Registrazione conto CONTRIBUTI-RITENUTE
			//Vengono registrati tutti i CORI a carico Ente
			righeCori.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)!=0)
					.filter(el->el.isContributoEnte() || el.isTipoContributoIva())
					.forEach(cori->{
						try {
							BigDecimal imCostoCori = cori.getAmmontare();
							Pair<Voce_epBulk, Voce_epBulk> pairContoCostoCori;

							if (imCostoCori.compareTo(BigDecimal.ZERO)!=0) {
								if (cori.isTipoContributoRivalsa() || cori.isTipoContributoIva()) {
									if (!pairContoCostoCompenso.isPresent())
										throw new ApplicationException("Compenso " + descCompenso + " con contributo non nullo. Scrittura prima nota non possibile per impossibilità ad individuare il conto di costo!");
									if (cori.isTipoContributoRivalsa()) {
										// Se la tipologia di contributo ritenuta è RIVALSA va tutto sul costo principale del compenso
										DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoCompenso, imCostoCori, Boolean.TRUE);
										if (makeAnalitica)
											this.completeWithDatiAnalitici(userContext,dettPN,compenso,dettagliAnaliticos,Boolean.FALSE);
										testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeCompenso, imCostoCori, compenso.getCd_terzo(), compenso);
									} else if (cori.isTipoContributoIva()) {
										//Il primo giro viene fatto solo per il contributo a carico ente.... nel caso di split esiste anche la riga carico
										//percipiente che, in questa fase, deve solo rilevare il credito verso il fornitore che sarà chiuso all'atto del pagamento
										if (cori.isContributoEnte()) {
											if (compenso.isIstituzionale()) {
												// Se la tipologia di contributo ritenuta è IVA ed è istituzionale va tutto sul costo principale del compenso
												DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoCompenso, imCostoCori, Boolean.TRUE);
												if (makeAnalitica)
													this.completeWithDatiAnalitici(userContext, dettPN, compenso, dettagliAnaliticos, Boolean.FALSE);
											} else if (compenso.isCommerciale()) {
												// Se la tipologia di contributo ritenuta è IVA Commerciale il conto è del tributo
												Voce_epBulk voceCosto = this.findContoCosto(userContext, cori.getTipoContributoRitenuta(), cori.getEsercizio(), cori.getSezioneCostoRicavo(), cori.getTi_ente_percipiente());
												if (compenso.getFl_split_payment().equals(Boolean.TRUE))
													testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO_SPLIT.value(), compenso.getTipoDocumentoEnum().getSezioneEconomica(), voceCosto, imCostoCori, compenso.getCd_terzo(), compenso, cori.getCd_contributo_ritenuta());
												else
													testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value(), compenso.getTipoDocumentoEnum().getSezioneEconomica(), voceCosto, imCostoCori, compenso.getCd_terzo(), compenso, cori.getCd_contributo_ritenuta());
											}
											testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeCompenso, imCostoCori, compenso.getCd_terzo(), compenso);

											if (compenso.getFl_split_payment().equals(Boolean.TRUE)) {
												Ass_tipo_cori_voce_epBulk assTipoCori = this.findAssociazioneCoriVoceEp(userContext, cori.getTipoContributoRitenuta(), cori.getEsercizio(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), cori.getTi_ente_percipiente());

												testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO_SPLIT.value(), Movimento_cogeBulk.SEZIONE_AVERE, assTipoCori.getCd_voce_ep_contr(), imCostoCori, compenso.getCd_terzo(), compenso, cori.getCd_contributo_ritenuta());
												testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoPatrimonialeCompenso, imCostoCori, compenso.getCd_terzo(), compenso);
											}
										} else { //Contributo Percipiente
											if (compenso.getFl_split_payment().equals(Boolean.TRUE)) {
												//Rilevo il conto IVA Credito/Debito di tipo SPLIT e apro il conto ritenute split
												Voce_epBulk aContoCreditoRitenuteSplit = this.findContoCreditoRitenuteSplitPayment(userContext, compenso.getEsercizio());
												testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aContoCreditoRitenuteSplit, imCostoCori, compenso.getCd_terzo(), compenso, cori.getCd_contributo_ritenuta());
												testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeCompenso, imCostoCori, compenso.getCd_terzo(), compenso);
											}
										}
									}
								} else {
									pairContoCostoCori = this.findPairCostoCompenso(userContext, cori);
									DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, pairContoCostoCori.getFirst(), imCostoCori, Boolean.TRUE);
									if (makeAnalitica)
										this.completeWithDatiAnalitici(userContext, dettPN, compenso, dettagliAnaliticos, Boolean.FALSE);
									testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, pairContoCostoCori.getSecond(), imCostoCori, compenso.getCd_terzo(), compenso, cori.getCd_contributo_ritenuta());
								}
							}
						} catch (ComponentException|PersistencyException|RemoteException e) {
							throw new ApplicationRuntimeException(e);
						}
					});

			return this.generaScritture(userContext, compenso, Collections.singletonList(testataPrimaNota), Boolean.TRUE, makeAnalitica);
		} catch (PersistencyException|RemoteException e) {
			throw handleException(e);
		}
	}

	private ResultScrittureContabili proposeScritturaPartitaDoppiaAnticipo(UserContext userContext, AnticipoBulk anticipo, boolean makeAnalitica) throws ComponentException {
		try {
			TestataPrimaNota testataPrimaNota = new TestataPrimaNota(anticipo.getDt_da_competenza_coge(), anticipo.getDt_a_competenza_coge());

			//Registrazione conto COSTO ANTICIPO
			BigDecimal imCostoAnticipo = anticipo.getIm_anticipo();
			if (imCostoAnticipo.compareTo(BigDecimal.ZERO)!=0) {
				final Pair<Voce_epBulk, Voce_epBulk> pairContoCostoAnticipo = this.findPairCosto(userContext, anticipo);

                assert pairContoCostoAnticipo != null;
                Voce_epBulk aContoCreditoAnticipo = pairContoCostoAnticipo.getFirst();
				Voce_epBulk aContoDebitoAnticipo = pairContoCostoAnticipo.getSecond();

				//RIGA DARE
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aContoCreditoAnticipo, imCostoAnticipo, anticipo.getCd_terzo(), anticipo);
				//RIGA AVERE
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aContoDebitoAnticipo, imCostoAnticipo, anticipo.getCd_terzo(), anticipo);
			}
			return this.generaScritture(userContext, anticipo, Collections.singletonList(testataPrimaNota), Boolean.FALSE, makeAnalitica);
		} catch (RemoteException | PersistencyException e) {
			throw handleException(e);
        }
    }

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaAperturaFondo(UserContext userContext, Documento_genericoBulk documento) throws ComponentException {
		try {
			if (!documento.getTipoDocumentoEnum().isAperturaFondo())
				throw new ApplicationException("Il documento " + documento.getEsercizio() + "/" + documento.getCd_tipo_doc() + "/" +
						documento.getCd_uo()+"/"+documento.getPg_documento_generico() +
						" non risulta essere di tipo 'Apertura Fondo Econmale'. Proposta di prima nota non possibile.");

			List<IDocumentoAmministrativoRigaBulk> righeDocamm = this.getRigheDocamm(userContext, documento);

			if (righeDocamm.size() != 1)
				throw new ApplicationException("Il documento " + documento.getEsercizio() + "/" + documento.getCd_tipo_doc() + "/" +
						documento.getCd_uo()+"/"+documento.getPg_documento_generico() +
						" ha un numero di righe non coerente con l'unica prevista per un documento di apertura fondo economale. Proposta di prima nota non possibile.");

			Integer cdTerzo = righeDocamm.get(0).getCd_terzo();

			TestataPrimaNota testataPrimaNota = new TestataPrimaNota(documento.getDt_da_competenza_coge(), documento.getDt_a_competenza_coge());

			//Registrazione conto COSTO ANTICIPO
			BigDecimal imCosto = documento.getIm_totale();
			if (imCosto.compareTo(BigDecimal.ZERO)!=0) {
				Voce_epBulk aContoCreditoEconomo = this.findContoCreditoEconomo(userContext, documento.getEsercizio());
				Voce_epBulk aContoDebitoEconomo = this.findContoDebitoEconomo(userContext, documento.getEsercizio());
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aContoCreditoEconomo, imCosto, cdTerzo, documento);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aContoDebitoEconomo, imCosto, cdTerzo, documento);
			}
			return this.generaScrittura(userContext, documento, Collections.singletonList(testataPrimaNota), false);
		} catch (RemoteException e) {
			throw handleException(e);
		}
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaRimborso(UserContext userContext, RimborsoBulk rimborso) throws ComponentException {
		try {
			Optional<AnticipoBulk> optAnticipo = Optional.ofNullable(rimborso.getAnticipo()).map(anticipo->
					Optional.of(anticipo).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
						try {
							AnticipoHome home = (AnticipoHome) getHome(userContext, AnticipoBulk.class);
							return (AnticipoBulk)home.findByPrimaryKey(anticipo);
						} catch (ComponentException | PersistencyException e) {
							throw new DetailedRuntimeException(e);
						}
					})).filter(anticipo->anticipo.getIm_anticipo().compareTo(BigDecimal.ZERO)!=0);

			TestataPrimaNota testataPrimaNota = new TestataPrimaNota(rimborso.getDt_da_competenza_coge(), rimborso.getDt_a_competenza_coge());

			//Registrazione conto COSTO ANTICIPO
			BigDecimal imCostoRimborso = rimborso.getIm_rimborso();
			if (imCostoRimborso.compareTo(BigDecimal.ZERO)!=0) {
				Voce_epBulk aContoCreditoAnticipo = this.findContoCreditoAnticipo(userContext, optAnticipo.get().getEsercizio());
				Voce_epBulk aContoCreditoRimborsoAnticipo = this.findContoCreditoRimborsoAnticipo(userContext, optAnticipo.get().getEsercizio());
				//Il rimborso chiude il credito conto anticipi verso il dipendente
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aContoCreditoAnticipo, imCostoRimborso, optAnticipo.get().getCd_terzo(), optAnticipo.get());
				//e apre credito conto rimborso anticipi dell'ente verso il dipendente
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aContoCreditoRimborsoAnticipo, imCostoRimborso, rimborso.getCd_terzo(), rimborso);
			}
			return this.generaScrittura(userContext, rimborso, Collections.singletonList(testataPrimaNota), false);
		} catch (RemoteException e) {
			throw handleException(e);
		}
	}

	private ResultScrittureContabili proposeScritturaPartitaDoppiaMissione(UserContext userContext, MissioneBulk missione, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		try {
			String descMissione = missione.getEsercizio() + "/" + missione.getCd_unita_organizzativa() + "/" + missione.getPg_missione();

			//Le missioni pagate con compenso non creano scritture di prima nota in quanto create direttamente dal compenso stesso
			if (missione.getFl_associato_compenso())
				throw new ScritturaPartitaDoppiaNotRequiredException("Missione " + descMissione + " associata a compenso. Registrazione economica non prevista.");

			if (missione.isAnnullato())
				throw new ScritturaPartitaDoppiaNotRequiredException("Missione " + descMissione + " annullata. Registrazione economica non prevista.");

			if (!missione.isMissioneDefinitiva())
				throw new ScritturaPartitaDoppiaNotEnabledException("Missione " + descMissione + " non definitiva. Registrazione economica non prevista.");

			List<DettaglioAnalitico> dettagliAnaliticos = missione.getRigheEconomica().stream().map(DettaglioAnalitico::new).collect(Collectors.toList());

			Optional<AnticipoBulk> optAnticipo = Optional.ofNullable(missione.getAnticipo()).map(anticipo->
					Optional.of(anticipo).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
						try {
							AnticipoHome home = (AnticipoHome) getHome(userContext, AnticipoBulk.class);
							return (AnticipoBulk)home.findByPrimaryKey(anticipo);
						} catch (ComponentException | PersistencyException e) {
							throw new DetailedRuntimeException(e);
						}
					})).filter(anticipo->anticipo.getIm_anticipo().compareTo(BigDecimal.ZERO)!=0);

			TestataPrimaNota testataPrimaNota = new TestataPrimaNota(missione.getDt_inizio_missione(), missione.getDt_fine_missione());

			//Registrazione conto COSTO MISSIONE
			BigDecimal imCostoMissioneNettoAnticipo = missione.getIm_totale_missione().subtract(optAnticipo.map(AnticipoBulk::getIm_anticipo).orElse(BigDecimal.ZERO));

			final Optional<Pair<Voce_epBulk, Voce_epBulk>> pairContoCostoMissione = Optional.ofNullable(this.findPairCosto(userContext, missione));

			Voce_epBulk contoCostoMissione = pairContoCostoMissione.map(Pair::getFirst).orElse(null);
			Voce_epBulk contoPatrimonialeMissione = pairContoCostoMissione.map(Pair::getSecond).orElse(null);

			if (imCostoMissioneNettoAnticipo.compareTo(BigDecimal.ZERO)>0) {
				if (!pairContoCostoMissione.isPresent())
					throw new ApplicationException("Missione " + descMissione + " di importo positivo. Scrittura prima nota non possibile per impossibilità ad individuare il conto di costo!");
				DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoMissione, imCostoMissioneNettoAnticipo, Boolean.TRUE);
				//COMPLETO CON I DATI ANALITICI - TRATTANDOSI DI MISSIONE CON ANTICIPO CERCO PER IMPORTO PER FAR COLLEGARE LA RIGA GIUSTA
				if (makeAnalitica)
					this.completeWithDatiAnalitici(userContext,dettPN,missione,dettagliAnaliticos,optAnticipo.isPresent());
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeMissione, imCostoMissioneNettoAnticipo, missione.getCd_terzo(), missione);
			}

			//se esiste anticipo devo fare una serie di registrazioni
			if (optAnticipo.isPresent()) {
				AnticipoBulk anticipo = optAnticipo.get();
				BigDecimal imCostoAnticipo = anticipo.getIm_anticipo();
				if (missione.getIm_totale_missione().compareTo(imCostoAnticipo) < 0)
					imCostoAnticipo = missione.getIm_totale_missione();

				if (imCostoAnticipo.compareTo(BigDecimal.ZERO) != 0) {
					if (!pairContoCostoMissione.isPresent())
						throw new ApplicationException("Missione " + descMissione + " con anticipo di importo non nullo. Scrittura prima nota non possibile per impossibilità ad individuare il conto di costo della missione!");

					// 1. scaricare i costi dell'anticipo compensandoli con il conto patrimoniale della missione
					DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, pairContoCostoMissione.get().getFirst(), imCostoAnticipo, Boolean.TRUE);
					//COMPLETO CON I DATI ANALITICI - TRATTANDOSI DI MISSIONE CON ANTICIPO CERCO PER IMPORTO PER FAR COLLEGARE LA RIGA GIUSTA
					if (makeAnalitica)
						this.completeWithDatiAnalitici(userContext,dettPN,missione,dettagliAnaliticos,true);
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeMissione, imCostoAnticipo, missione.getCd_terzo(), missione);

					//2. chiudere il conto credito per anticipo
					List<Movimento_cogeBulk> allMovimentiPrimaNota = this.findMovimentiPrimaNota(userContext, anticipo);
					Movimento_cogeBulk movimentoAnticipo = this.findMovimentoAperturaCreditoAnticipo(allMovimentiPrimaNota, anticipo, anticipo.getCd_terzo());

					testataPrimaNota.addDettaglio(userContext, movimentoAnticipo.getTi_riga(), Movimento_cogeBulk.SEZIONE_AVERE, movimentoAnticipo.getConto(), imCostoAnticipo, anticipo.getCd_terzo(), anticipo);
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoPatrimonialeMissione, imCostoAnticipo, missione.getCd_terzo(), missione, null, null, null, DEFAULT_MODIFICABILE, Boolean.FALSE);
				}
			}
			return this.generaScritture(userContext, missione, Collections.singletonList(testataPrimaNota), Boolean.TRUE, makeAnalitica);
		} catch (PersistencyException|RemoteException e) {
			throw handleException(e);
		}
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandato(UserContext userContext, MandatoBulk mandato, boolean isContabilizzaSuInviato) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		try {
			//Completo l'oggetto mandato con i sottooggetti che serviranno durante l'elaborazione
			completeMandato(userContext, mandato);

			if (mandato.isMandatoRegolarizzazione())
				throw new ScritturaPartitaDoppiaNotRequiredException("Mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
						" di regolarizzazione. Registrazione economica non prevista.");

			//Il documento deve essere annullato o esitato altrimenti esce
			if (mandato.isAnnullato())
				return this.proposeScritturaPartitaDoppiaManRevAnnullato(userContext, mandato);
			else if (mandato.getDt_trasmissione()!=null && (isContabilizzaSuInviato || mandato.isPagato())) {
				if (mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isCompenso()))
					return this.proposeScritturaPartitaDoppiaMandatoCompenso(userContext, mandato);
				else if (mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoStipendiSpesa()) &&
						mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)>0)
					return this.proposeScritturaPartitaDoppiaMandatoStipendi(userContext, mandato);
				else if (mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoCoriVersamentoSpesa()))
					return this.proposeScritturaPartitaDoppiaMandatoVersamentoCori(userContext, mandato);
				else {
					//devo verificare se trattasi di un mandato generato da righe negative associate a qualche compenso
					Collection<V_doc_cont_compBulk> coll = ((V_doc_cont_compHome) getHome(userContext, V_doc_cont_compBulk.class)).findByDocumento(mandato.getEsercizio(), mandato.getCd_cds(), mandato.getPg_mandato(), V_doc_cont_compBulk.TIPO_DOC_CONT_MANDATO);
					CompensoBulk compenso = null;
					if (!coll.isEmpty()) {
						if (coll.size() > 1)
							throw new ApplicationRuntimeException("Errore nell'individuazione del compenso a cui è collegato il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato()
									+ ": il mandato risulta associato a troppi compensi.");
						V_doc_cont_compBulk docContCompBulk = coll.stream().findAny().get();
						compenso = (CompensoBulk) getHome(userContext, CompensoBulk.class).findByPrimaryKey(new CompensoBulk(docContCompBulk.getCd_cds_compenso(), docContCompBulk.getCd_uo_compenso(),
								docContCompBulk.getEsercizio_compenso(), docContCompBulk.getPg_compenso()));
					}

					if (compenso != null)
						return this.proposeScritturaPartitaDoppiaMandatoCompenso(userContext, mandato, compenso);
					else if (mandato.getMandato_rigaColl().size() == 1 && mandato.getIm_ritenute().compareTo(BigDecimal.ZERO) > 0 &&
							mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
									.anyMatch(el -> TipoDocumentoEnum.fromValue(el).isGenericoSpesa())) {
						//verifico se trattasi di documento generico con ritenute di conguaglio
						boolean isRitenuteConguaglio = false;
						List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);

						for (Ass_mandato_reversaleBulk assMandatoReversaleBulk : result) {
							Collection<V_doc_cont_compBulk> result2 = ((V_doc_cont_compHome) getHome(userContext, V_doc_cont_compBulk.class)).findByDocumento(assMandatoReversaleBulk.getEsercizio_reversale(), assMandatoReversaleBulk.getCd_cds_reversale(), assMandatoReversaleBulk.getPg_reversale(), V_doc_cont_compBulk.TIPO_DOC_CONT_REVERSALE);
							if (!result2.isEmpty())
								isRitenuteConguaglio = true;
						}
						if (isRitenuteConguaglio)
							return this.proposeScritturaPartitaDoppiaMandatoAccantonamentoCoriDocgen(userContext, mandato);
					}
				}

				TestataPrimaNota testataPrimaNota = new TestataPrimaNota();
				if (mandato.isMandatoRegolarizzazione() && mandato.getCd_cds().equals(mandato.getCd_cds_origine()) &&
						mandato.getUnita_organizzativa().getCd_tipo_unita().equals(Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
					return null;

				//raggruppo i mandatiRiga per Partita
				List<MandatoRigaComplete> dettaglioFinanziarioList = this.completeRigheMandato(userContext, mandato);

				Map<IDocumentoAmministrativoBulk, Map<Integer, List<MandatoRigaComplete>>> mapDettagli =
						dettaglioFinanziarioList.stream()
								.collect(Collectors.groupingBy(MandatoRigaComplete::getDocamm,
										Collectors.groupingBy(MandatoRigaComplete::getCdTerzo)));

				mapDettagli.keySet().forEach(aDocamm -> {
					Map<Integer, List<MandatoRigaComplete>> mapDocAmm = mapDettagli.get(aDocamm);
					mapDocAmm.keySet().forEach(aCdTerzo -> {
						try {
							List<MandatoRigaComplete> mandatoRigaCompleteList = mapDocAmm.get(aCdTerzo);
							addDettagliPrimaNotaMandatoDocumentiVari(userContext, testataPrimaNota, mandato, aDocamm, aCdTerzo, mandatoRigaCompleteList);
						} catch (ComponentException | PersistencyException | RemoteException e) {
							throw new ApplicationRuntimeException(e);
						}
					});
				});

				BigDecimal totRitenuteSuRigheMandato = dettaglioFinanziarioList.stream().map(MandatoRigaComplete::getMandatoRiga).map(Mandato_rigaBulk::getIm_ritenute_riga).reduce(BigDecimal.ZERO, BigDecimal::add);
				if (totRitenuteSuRigheMandato.compareTo(BigDecimal.ZERO)==0 && mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)!=0) {
					//Vuol dire che hanno legato reversali al mandato direttamente...... a questo punto produco prima nota reversale e la lego al mandato
					List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);
					for (Ass_mandato_reversaleBulk assManRev : result) {
						ReversaleIHome revHome = (ReversaleIHome) getHome(userContext, ReversaleIBulk.class);
						ReversaleBulk reversale = (ReversaleIBulk) revHome.findByPrimaryKey(new ReversaleIBulk(assManRev.getCd_cds_reversale(), assManRev.getEsercizio_reversale(), assManRev.getPg_reversale()));
						TestataPrimaNota testataPrimaNotaReversale = this.proposeTestataPrimaNotaReversale(userContext, reversale, Boolean.FALSE);
						//Aggiungo i dettagli della reversale al mandato
						testataPrimaNota.getDett().addAll(testataPrimaNotaReversale.getDett());
					}
				} else if (totRitenuteSuRigheMandato.compareTo(mandato.getIm_ritenute())!=0) {
					throw new ApplicationException("Mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
							" con righe che riportano un importo totale di ritenute ("+ new EuroFormat().format(totRitenuteSuRigheMandato)+
							") non coincidente con l'importo ritenute indicato sulla testata del mandato stesso ("+
							new EuroFormat().format(mandato.getIm_ritenute())+").");
				}
				return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), true);
			}
			throw new ScritturaPartitaDoppiaNotEnabledException("Scrittura Economica non prevista in quanto il mandato non risulta pagato.");
		} catch (PersistencyException | RemoteException e) {
			throw handleException(e);
		}
	}

	private List<MandatoRigaComplete> completeRigheMandato(UserContext userContext, MandatoBulk mandato) {
		//raggruppo i mandatiRiga per Partita
		Map<Integer, Map<String, Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>>>> mapRigheMandato =
				mandato.getMandato_rigaColl().stream()
						.collect(Collectors.groupingBy(Mandato_rigaBulk::getEsercizio_doc_amm,
								Collectors.groupingBy(Mandato_rigaBulk::getCd_tipo_documento_amm,
										Collectors.groupingBy(Mandato_rigaBulk::getCd_cds_doc_amm,
												Collectors.groupingBy(Mandato_rigaBulk::getCd_uo_doc_amm,
														Collectors.groupingBy(Mandato_rigaBulk::getPg_doc_amm))))));

		List<MandatoRigaComplete> mandatoRigaCompleteList = new ArrayList<>();
		mapRigheMandato.keySet().forEach(aEsercizioDocamm -> {
			Map<String, Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>>> mapEsercizioDocamm = mapRigheMandato.get(aEsercizioDocamm);
			mapEsercizioDocamm.keySet().forEach(aTipoDocamm -> {
				Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>> mapTipoDocamm = mapEsercizioDocamm.get(aTipoDocamm);
				mapTipoDocamm.keySet().forEach(aCdCdsDocamm -> {
					Map<String, Map<Long, List<Mandato_rigaBulk>>> mapCdCdsDocamm = mapTipoDocamm.get(aCdCdsDocamm);
					mapCdCdsDocamm.keySet().forEach(aCdUoDocamm -> {
						Map<Long, List<Mandato_rigaBulk>> mapCdUoDocamm = mapCdCdsDocamm.get(aCdUoDocamm);
						mapCdUoDocamm.keySet().forEach(aPgDocamm -> {
							try {
								List<Mandato_rigaBulk> listRigheMandato = mapCdUoDocamm.get(aPgDocamm);

								IDocumentoAmministrativoBulk docamm;
								if (TipoDocumentoEnum.fromValue(aTipoDocamm).isFatturaPassiva()) {
									docamm = (Fattura_passiva_IBulk) getHome(userContext, Fattura_passiva_IBulk.class)
											.findByPrimaryKey(new Fattura_passiva_IBulk(aCdCdsDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isNotaDebitoPassiva()) {
									docamm = (Nota_di_debitoBulk) getHome(userContext, Nota_di_debitoBulk.class)
												.findByPrimaryKey(new Nota_di_debitoBulk(aCdCdsDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isNotaCreditoPassiva()) {
									docamm = (Nota_di_creditoBulk) getHome(userContext, Nota_di_creditoBulk.class)
											.findByPrimaryKey(new Nota_di_creditoBulk(aCdCdsDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isDocumentoGenericoPassivo()) {
									Documento_genericoHome home = (Documento_genericoHome) getHome(userContext, Documento_genericoBulk.class);
									docamm = (IDocumentoAmministrativoBulk)home.findByPrimaryKey(new Documento_genericoBulk(aCdCdsDocamm, aTipoDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isMissione()) {
									docamm = (MissioneBulk) getHome(userContext, MissioneBulk.class)
											.findByPrimaryKey(new MissioneBulk(aCdCdsDocamm,aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isAnticipo()) {
									docamm = (AnticipoBulk) getHome(userContext, AnticipoBulk.class)
											.findByPrimaryKey(new AnticipoBulk(aCdCdsDocamm,aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
								} else
									throw new ApplicationRuntimeException("Scrittura Economica non gestita per la tipologia di documento "+aTipoDocamm +
											" collegato al mandato "+ mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() +".");

								mandatoRigaCompleteList.addAll(listRigheMandato.stream().map(rigaMandato->{
									try {
                                        if (docamm instanceof MissioneBulk)
											return new MandatoRigaComplete(docamm, rigaMandato, null, ((MissioneBulk) docamm).getCd_terzo());
										else if (docamm instanceof AnticipoBulk)
											return new MandatoRigaComplete(docamm, rigaMandato, null, ((AnticipoBulk) docamm).getCd_terzo());
										else {
											List<IDocumentoAmministrativoRigaBulk> docammRighe = this.getRigheDocamm(userContext, docamm).stream()
													.filter(el->el.getScadenzaDocumentoContabile() instanceof Obbligazione_scadenzarioBulk)
													.filter(el->
															((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getEsercizio().equals(rigaMandato.getEsercizio_obbligazione()) &&
																	((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getEsercizio_originale().equals(rigaMandato.getEsercizio_ori_obbligazione()) &&
																	((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getCd_cds().equals(rigaMandato.getCd_cds()) &&
																	((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getPg_obbligazione().equals(rigaMandato.getPg_obbligazione()) &&
																	((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getPg_obbligazione_scadenzario().equals(rigaMandato.getPg_obbligazione_scadenzario())
													).collect(Collectors.toList());

											if (docammRighe.isEmpty())
												throw new ApplicationException("Non è stato possibile individuare correttamente la riga del documento " +
														docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc_amm()+
														" associata alla riga del mandato "+ mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() +".");

											if (docammRighe.stream().collect(Collectors.groupingBy(IDocumentoAmministrativoRigaBulk::getCd_terzo)).size()>1)
												throw new ApplicationException("Risultano più righe del documento " +
														docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc_amm()+
														" con terzi diversi associate alla stessa riga del mandato "+ mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() +".");
											return new MandatoRigaComplete(docamm, rigaMandato, docammRighe,
													docammRighe.stream().findAny().map(IDocumentoAmministrativoRigaBulk::getCd_terzo).orElse(null));
										}
									} catch (ComponentException ex) {
										throw new ApplicationRuntimeException(ex);
									}
								}).collect(Collectors.toList()));
							} catch (ComponentException | PersistencyException e) {
								throw new ApplicationRuntimeException(e);
							}
						});
					});
				});
			});
		});
		return mandatoRigaCompleteList;
	}

	private TestataPrimaNota proposeTestataPrimaNotaManRevAnnullato(UserContext userContext, IManRevBulk manrev) throws ComponentException, PersistencyException {
		boolean isMandato = manrev instanceof MandatoBulk;
		if (!manrev.isAnnullato())
			throw new ApplicationException((isMandato?"Il mandato ":"La reversale") + manrev.getEsercizio() + "/" + manrev.getCd_cds() + "/" + manrev.getPg_manrev() +
					" non risulta " + (isMandato?"annullato":"annullata")+". Proposta di prima nota non possibile.");

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();
		//storno scrittura prima nota del mandato
		this.stornoTotaleScritturaPrimaNota(userContext, testataPrimaNota, manrev);
		return testataPrimaNota;
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaManRevAnnullato(UserContext userContext, IManRevBulk manrev) throws ComponentException, PersistencyException, ScritturaPartitaDoppiaNotRequiredException {
		TestataPrimaNota testataPrimaNota = proposeTestataPrimaNotaManRevAnnullato(userContext, manrev);
		if (testataPrimaNota.getDett().isEmpty())
			throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista in quanto "+(manrev instanceof MandatoBulk?"il mandato risulta annullato.":"la reversale risulta annullata."));
		return this.generaScrittura(userContext, manrev, Collections.singletonList(testataPrimaNota), true);
	}

	private void stornoTotaleScritturaPrimaNota(UserContext userContext, TestataPrimaNota testataPrimaNota, IDocumentoCogeBulk docamm) throws ComponentException, PersistencyException {
		//devo stornare scrittura prima nota del mandato
		//prendo tutte le prime note e scrivo registrazioni di senso inverso
		//recupero la scrittura del compenso
		List<Movimento_cogeBulk> allMovimentiPrimaNota = this.findMovimentiPrimaNota(userContext,docamm);

		allMovimentiPrimaNota.forEach(movimento -> {
			Partita partita = Optional.of(movimento).filter(mov -> Optional.ofNullable(mov.getCd_tipo_documento()).isPresent())
					.map(mov -> new Partita(mov.getCd_tipo_documento(), mov.getCd_cds_documento(), mov.getCd_uo_documento(), mov.getEsercizio_documento(), mov.getPg_numero_documento(),
							mov.getCd_terzo(), TipoDocumentoEnum.fromValue(mov.getCd_tipo_documento()))).orElse(null);
			testataPrimaNota.addDettaglio(userContext, movimento.getTi_riga(), Movimento_cogeBulk.getControSezione(movimento.getSezione()), movimento.getConto(), movimento.getIm_movimento(), movimento.getCd_terzo(), partita, movimento.getCd_contributo_ritenuta());
		});
	}

	public Scrittura_partita_doppiaBulk proposeStornoScritturaPartitaDoppia(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaDaStornare, Timestamp dataStorno) {
		Scrittura_partita_doppiaBulk scritturaPartitaDoppiaStorno = new Scrittura_partita_doppiaBulk();

		scritturaPartitaDoppiaStorno.setToBeCreated();
		scritturaPartitaDoppiaStorno.setDt_contabilizzazione(dataStorno);
		scritturaPartitaDoppiaStorno.setUser(userContext.getUser());
		scritturaPartitaDoppiaStorno.setCd_unita_organizzativa(scritturaPartitaDoppiaDaStornare.getCd_unita_organizzativa());
		scritturaPartitaDoppiaStorno.setCd_cds(scritturaPartitaDoppiaDaStornare.getCd_cds());
		scritturaPartitaDoppiaStorno.setTi_scrittura(scritturaPartitaDoppiaDaStornare.getTi_scrittura());
		scritturaPartitaDoppiaStorno.setStato(Scrittura_partita_doppiaBulk.STATO_DEFINITIVO);
		scritturaPartitaDoppiaStorno.setDs_scrittura("Storno "+scritturaPartitaDoppiaDaStornare.getDs_scrittura());
		scritturaPartitaDoppiaStorno.setEsercizio(scritturaPartitaDoppiaDaStornare.getEsercizio());
		scritturaPartitaDoppiaStorno.setEsercizio_documento_amm(scritturaPartitaDoppiaDaStornare.getEsercizio_documento_amm());
		scritturaPartitaDoppiaStorno.setCd_cds_documento(scritturaPartitaDoppiaDaStornare.getCd_cds_documento());
		scritturaPartitaDoppiaStorno.setCd_uo_documento(scritturaPartitaDoppiaDaStornare.getCd_uo_documento());
		scritturaPartitaDoppiaStorno.setCd_tipo_documento(scritturaPartitaDoppiaDaStornare.getCd_tipo_documento());
		scritturaPartitaDoppiaStorno.setPg_numero_documento(scritturaPartitaDoppiaDaStornare.getPg_numero_documento());
		scritturaPartitaDoppiaStorno.setDt_inizio_liquid(scritturaPartitaDoppiaDaStornare.getDt_inizio_liquid());
		scritturaPartitaDoppiaStorno.setDt_fine_liquid(scritturaPartitaDoppiaDaStornare.getDt_fine_liquid());
		scritturaPartitaDoppiaStorno.setTipo_liquidazione(scritturaPartitaDoppiaDaStornare.getTipo_liquidazione());
		scritturaPartitaDoppiaStorno.setReport_id_liquid(scritturaPartitaDoppiaDaStornare.getReport_id_liquid());
		scritturaPartitaDoppiaStorno.setIm_scrittura(scritturaPartitaDoppiaDaStornare.getIm_scrittura());

		scritturaPartitaDoppiaStorno.setAttiva(scritturaPartitaDoppiaDaStornare.getAttiva());
		scritturaPartitaDoppiaStorno.setPg_scrittura_annullata(scritturaPartitaDoppiaDaStornare.getPg_scrittura());

		scritturaPartitaDoppiaDaStornare.getAllMovimentiColl().forEach(movimentoDaStornare -> {
			Movimento_cogeBulk movimentoStorno = new Movimento_cogeBulk();
			movimentoStorno.setToBeCreated();
			movimentoStorno.setUser(userContext.getUser());

			movimentoStorno.setConto(movimentoDaStornare.getConto());
			movimentoStorno.setIm_movimento(movimentoDaStornare.getIm_movimento());
			movimentoStorno.setTerzo(movimentoDaStornare.getTerzo());
			movimentoStorno.setDt_da_competenza_coge(movimentoDaStornare.getDt_da_competenza_coge());
			movimentoStorno.setDt_a_competenza_coge(movimentoDaStornare.getDt_a_competenza_coge());
			movimentoStorno.setStato(Movimento_cogeBulk.STATO_DEFINITIVO);
			movimentoStorno.setCd_contributo_ritenuta(movimentoDaStornare.getCd_contributo_ritenuta());
			movimentoStorno.setFl_modificabile(Boolean.FALSE);
			movimentoStorno.setCd_cds(movimentoDaStornare.getCd_cds());
			movimentoStorno.setEsercizio(movimentoDaStornare.getEsercizio());
			movimentoStorno.setCd_unita_organizzativa(movimentoDaStornare.getCd_unita_organizzativa());
			movimentoStorno.setTi_istituz_commerc(movimentoDaStornare.getTi_istituz_commerc());
			movimentoStorno.setCd_tipo_documento(movimentoDaStornare.getCd_tipo_documento());
			movimentoStorno.setCd_cds_documento(movimentoDaStornare.getCd_cds_documento());
			movimentoStorno.setCd_uo_documento(movimentoDaStornare.getCd_uo_documento());
			movimentoStorno.setEsercizio_documento(movimentoDaStornare.getEsercizio_documento());
			movimentoStorno.setPg_numero_documento(movimentoDaStornare.getPg_numero_documento());
			movimentoStorno.setTi_riga(movimentoDaStornare.getTi_riga());

			if (movimentoDaStornare.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE))
				scritturaPartitaDoppiaStorno.addToMovimentiAvereColl(movimentoStorno);
			else
				scritturaPartitaDoppiaStorno.addToMovimentiDareColl(movimentoStorno);
		});

		return scritturaPartitaDoppiaStorno;
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoCompenso(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException, RemoteException {
		if (mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
				.noneMatch(el -> TipoDocumentoEnum.fromValue(el).isCompenso()))
			throw new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
					" non risulta pagare un compenso. Proposta di prima nota non possibile.");
		if (mandato.getMandato_rigaColl().isEmpty() || mandato.getMandato_rigaColl().size() > 1)
			throw new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
					" ha un numero di righe non coerente con l'unica prevista per un mandato di compenso. Proposta di prima nota non possibile.");

		Mandato_rigaBulk rigaMandato = mandato.getMandato_rigaColl().get(0);
		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		CompensoBulk compenso = (CompensoBulk) getHome(userContext, CompensoBulk.class).findByPrimaryKey(new CompensoBulk(rigaMandato.getCd_cds_doc_amm(), rigaMandato.getCd_uo_doc_amm(), rigaMandato.getEsercizio_doc_amm(),
				rigaMandato.getPg_doc_amm()));

		List<Contributo_ritenutaBulk> righeCori = Optional.ofNullable(compenso.getChildren()).orElseGet(() -> {
			try {
				Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
				return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compenso);
			} catch (ComponentException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		//verifico se esiste conguaglio collegato al compenso principale pagato con il mandato
		CompensoBulk compensoConguaglio = ((CompensoHome) getHome(userContext, CompensoBulk.class)).findCompensoConguaglioAssociato(userContext, compenso);

		//se esiste aggiungo le righe di contributo/ritenuta in quanto anche loro contribuiscono a determinare il netto mandato
		if (compensoConguaglio != null && !compensoConguaglio.equalsByPrimaryKey(compenso)) {
			righeCori.addAll(Optional.ofNullable(compensoConguaglio.getChildren()).orElseGet(() -> {
				try {
					Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
					return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compensoConguaglio);
				} catch (ComponentException | PersistencyException e) {
					throw new DetailedRuntimeException(e);
				}
			}));
		}

		//recupero la missione se il compenso è da missione
		Optional<MissioneBulk> optMissione = Optional.ofNullable(compenso.getMissione()).map(missione->
				Optional.of(missione).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
					try {
						MissioneHome home = (MissioneHome) getHome(userContext, MissioneBulk.class);
						return (MissioneBulk)home.findByPrimaryKey(compenso.getMissione());
					} catch (ComponentException | PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				}));

		//quindi l'anticipo se la missione è stata associata ad esso
		Optional<AnticipoBulk> optAnticipo = optMissione.flatMap(missione->Optional.ofNullable(missione.getAnticipo()).map(anticipo->
				Optional.of(anticipo).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
					try {
						AnticipoHome home = (AnticipoHome) getHome(userContext, AnticipoBulk.class);
						return (AnticipoBulk)home.findByPrimaryKey(anticipo);
					} catch (ComponentException | PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				}))).filter(anticipo->anticipo.getIm_anticipo().compareTo(BigDecimal.ZERO)!=0);

		//recupero la scrittura del compenso
		List<Movimento_cogeBulk> allMovimentiPrimaNotaCompenso = this.findMovimentiPrimaNota(userContext, compenso);

		if (allMovimentiPrimaNotaCompenso.isEmpty())
			throw new ApplicationException("Non risulta generata la scrittura Prima Nota del compenso " + compenso.getEsercizio() + "/" + compenso.getPg_compenso() + " associato del mandato.");

		// Effettuo il controllo che le ritenute associate al compenso originario coincida con le ritenute della riga associata al mandato
		// Se la tipologia di contributo ritenuta è IVA o RIVALSA non calcolo le ritenute perchè vanno direttamente al fornitore
		{
			BigDecimal imRitenuteFittizieSoloCompenso = righeCori.stream()
					.filter(el -> el.getCompenso().equalsByPrimaryKey(compenso))
					.filter(Contributo_ritenutaBulk::isContributoEnte)
					.filter(el -> el.isTipoContributoRivalsa() || el.isTipoContributoIva())
					.map(Contributo_ritenutaBulk::getAmmontare)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal imRitenutePositiveSoloCompenso = righeCori.stream()
					.filter(el -> el.getCompenso().equalsByPrimaryKey(compenso))
					.map(Contributo_ritenutaBulk::getAmmontare)
					.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			//Aggiungo eventuali quote da trattenere indicate sul compenso
			imRitenutePositiveSoloCompenso = imRitenutePositiveSoloCompenso.add(Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO));

			if (imRitenutePositiveSoloCompenso.subtract(imRitenuteFittizieSoloCompenso).compareTo(rigaMandato.getIm_ritenute_riga()) != 0)
				throw new ApplicationException("L'importo delle righe ritenute del compenso associato al mandato non corrisponde con l'importo ritenute associato al mandato.");

			// Effettuo il controllo che il saldo patrimoniale aperto del compenso coincida con la riga del mandato
			BigDecimal imSaldoPatrimoniale = allMovimentiPrimaNotaCompenso.stream()
					.filter(el -> Movimento_cogeBulk.TipoRiga.DEBITO.value().equals(el.getTi_riga()))
					.map(el -> el.isSezioneAvere() ? el.getIm_movimento() : el.getIm_movimento().negate())
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			//se il compenso paga una missione con anticipo, il mandato è al netto dell'anticipo stesso.....
			if (optAnticipo.isPresent()) {
				imSaldoPatrimoniale = imSaldoPatrimoniale.subtract(optAnticipo.map(AnticipoBulk::getIm_anticipo).orElse(BigDecimal.ZERO));

				//se imSaldoPatrimoniale è minore delle ritenute il mandato deve essere stato emesso solo per l'importo delle ritenute
				if (imSaldoPatrimoniale.compareTo(rigaMandato.getIm_ritenute_riga())<0) {
					if (rigaMandato.getIm_ritenute_riga().compareTo(rigaMandato.getIm_mandato_riga()) != 0)
						throw new ApplicationException("Il compenso da missione " + compenso.getEsercizio() + "/" + compenso.getPg_compenso() + ", associato al mandato " +
								mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() + ", al netto dell'anticipo associato ha un importo " +
								"da pagare (" + new EuroFormat().format(imSaldoPatrimoniale) + " inferiore alle ritenute da operare (" +
								new EuroFormat().format(rigaMandato.getIm_ritenute_riga()) + "). Il mandato dovrebbe avere un importo pari alle sole ritenute da trattenere.");
				} else if (imSaldoPatrimoniale.compareTo(rigaMandato.getIm_mandato_riga()) != 0)
					throw new ApplicationException("La scrittura Prima Nota del compenso da missione " + compenso.getEsercizio() + "/" + compenso.getPg_compenso() + ", associato al mandato "+
							mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +", presenta in Avere movimenti di conti patrimoniali " +
							"per un importo ("+new EuroFormat().format(imSaldoPatrimoniale) +
							") che, al netto dell'anticipo associato ("+new EuroFormat().format(optAnticipo.map(AnticipoBulk::getIm_anticipo).orElse(BigDecimal.ZERO))+
							"), non coincide con quello del mandato stesso ("+new EuroFormat().format(rigaMandato.getIm_mandato_riga()) +").");
			} else if (imSaldoPatrimoniale.compareTo(rigaMandato.getIm_mandato_riga()) != 0)
				throw new ApplicationException("La scrittura Prima Nota del compenso " + compenso.getEsercizio() + "/" + compenso.getPg_compenso() + ", associato al mandato "+
						mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +", presenta in Avere movimenti di conti patrimoniali " +
						"per un importo ("+new EuroFormat().format(imSaldoPatrimoniale) +
						") non coincidente con quello del mandato stesso ("+new EuroFormat().format(rigaMandato.getIm_mandato_riga()) +").");
		}

		BigDecimal imRitenuteFittizie = righeCori.stream()
				.filter(Contributo_ritenutaBulk::isContributoEnte)
				.filter(el -> el.isTipoContributoRivalsa() || el.isTipoContributoIva())
				.map(Contributo_ritenutaBulk::getAmmontare)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal imRitenutePositive = righeCori.stream()
				.map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		//Aggiungo eventuali quote da trattenere indicate sul compenso e sul compenso conguaglio
		imRitenutePositive = imRitenutePositive.add(Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO));
		imRitenutePositive = imRitenutePositive.add(Optional.ofNullable(compensoConguaglio).flatMap(el->Optional.ofNullable(el.getIm_netto_da_trattenere())).orElse(BigDecimal.ZERO));

		//sottraggo le ritenute fittizie
		imRitenutePositive = imRitenutePositive.subtract(imRitenuteFittizie);

		BigDecimal imContributiCaricoEnte = righeCori.stream().filter(Contributo_ritenutaBulk::isContributoEnte)
				.map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el->el.compareTo(BigDecimal.ZERO)>0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		//Per il calcolo del netto mandato effettuo la sottrazione tra:
		//1. il lordo mandato
		//2. le ritenute positive
		BigDecimal imNettoMandato = rigaMandato.getIm_mandato_riga().subtract(imRitenutePositive);

		//Chiudo il patrimoniale principale del compenso.... quello con la partita
		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, rigaMandato);

		final Movimento_cogeBulk movimentoPatrimonialeCompenso;

		//Se l'importo del mandato coincide con i contributi carico ente, vuol dire che ho solo costi di contributi e non di partita.....
		//quindi il contoPatrimonialePartita non c'è sul compenso
		if (rigaMandato.getIm_mandato_riga().compareTo(imContributiCaricoEnte)==0)
			movimentoPatrimonialeCompenso = null;
		else {
			movimentoPatrimonialeCompenso = this.findMovimentoAperturaPartita(allMovimentiPrimaNotaCompenso, compenso, compenso.getCd_terzo());

			testataPrimaNota.addDettaglio(userContext, movimentoPatrimonialeCompenso.getTi_riga(), Movimento_cogeBulk.SEZIONE_DARE, movimentoPatrimonialeCompenso.getConto(), imNettoMandato, compenso.getCd_terzo(), compenso);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, imNettoMandato);
		}

		//Registrazione conto CONTRIBUTI-RITENUTE
		righeCori.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)>0).forEach(cori->{
			try {
				BigDecimal imCori = cori.getAmmontare();

				// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
				// L'Iva, sia per compensa istituzionale che commerciale, va sempre a costo,
				if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
					if (cori.isTipoContributoIva() && cori.isContributoPercipiente()) {
						Movimento_cogeBulk movimentoToClose = this.findMovimentoAperturaCoriIVACompenso(allMovimentiPrimaNotaCompenso, cori.getCompenso(), cori.getCompenso().getCd_terzo(), cori.getCd_contributo_ritenuta());
						testataPrimaNota.addDettaglio(userContext, movimentoToClose.getTi_riga(), Movimento_cogeBulk.getControSezione(movimentoToClose.getSezione()), movimentoToClose.getConto(), imCori, movimentoToClose.getCd_terzo(), cori.getCompenso(), cori.getCd_contributo_ritenuta());
						testataPrimaNota.addDettaglio(userContext, movimentoPatrimonialeCompenso.getTi_riga(), Movimento_cogeBulk.SEZIONE_DARE, movimentoPatrimonialeCompenso.getConto(), imCori, cori.getCompenso().getCd_terzo(), cori.getCompenso());
					} else {
						//se rigaMandato.getIm_mandato_riga() è uguale a imContributiCaricoEnte il contoPatrimonialePartita non è impostato.
						//Ma in questo caso non dovrebbe mai verificarsi che ci siano contributi a carico percipiente altrimenti il mandato sarebbe negativo
						if (cori.isContributoPercipiente())
							testataPrimaNota.addDettaglio(userContext, movimentoPatrimonialeCompenso.getTi_riga(), Movimento_cogeBulk.SEZIONE_DARE, movimentoPatrimonialeCompenso.getConto(), imCori, cori.getCompenso().getCd_terzo(), cori.getCompenso());
						else {
							Movimento_cogeBulk movimentoToClose = this.findMovimentoAperturaCori(allMovimentiPrimaNotaCompenso, cori.getCompenso(), cori.getCompenso().getCd_terzo(), cori.getCd_contributo_ritenuta());
							testataPrimaNota.addDettaglio(userContext, movimentoToClose.getTi_riga(), Movimento_cogeBulk.SEZIONE_DARE, movimentoToClose.getConto(), imCori, movimentoToClose.getCd_terzo(), cori.getCompenso(), cori.getCd_contributo_ritenuta());
						}
						if (cori.isTipoContributoDaVersare()) {
							Voce_epBulk contoCori = this.findContoContropartita(userContext, cori);
							testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoCori, imCori, cori.getCompenso().getCd_terzo(), cori.getCompenso(), cori.getCd_contributo_ritenuta());
						} else {
							Voce_epBulk contoCori = this.findContoTributoCori(userContext, cori);
							testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoCori, imCori);
						}
					}
				}
			} catch (ComponentException|PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		});

		//se il compenso è da missione con anticipo bilancio il onto tesoreria con l'anticipo stesso
		//se esiste anticipo devo fare registrazioni una serie di registrazioni
		optAnticipo.ifPresent(anticipo->{
			try {
				BigDecimal imCompensoNettoRitenute = compenso.getIm_totale_compenso().subtract(compenso.getIm_cr_ente()).subtract(compenso.getIm_cr_percipiente());
				BigDecimal imCostoAnticipo = anticipo.getIm_anticipo();

				BigDecimal imMovimento = imCostoAnticipo.compareTo(imCompensoNettoRitenute)<0?imCostoAnticipo:imCompensoNettoRitenute;

				List<Movimento_cogeBulk> allMovimentiPrimaNota = this.findMovimentiPrimaNota(userContext,anticipo);
				Voce_epBulk contoPatrimonialeAperturaCreditoAnticipo = this.findMovimentoAperturaCreditoAnticipo(allMovimentiPrimaNota, anticipo, anticipo.getCd_terzo()).getConto();

				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeAperturaCreditoAnticipo, imMovimento, anticipo.getCd_terzo(), anticipo);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, movimentoPatrimonialeCompenso.getConto(), imMovimento, compenso.getCd_terzo(), compenso);
			} catch (ComponentException|PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		});

		if (Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)>0) {
			//Devo chiudere la tesoreria con la quota da trattenere
			Voce_epBulk contoQuotaDaTrattenere = this.findContoQuotaDaTrattenereByConfig(userContext, CNRUserContext.getEsercizio(userContext));

			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoQuotaDaTrattenere, compenso.getIm_netto_da_trattenere(), compenso.getCd_terzo(), compenso);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, compenso.getIm_netto_da_trattenere());
		}

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), Boolean.TRUE);
	}

	/*
		Questa procedura gestisce la proposta di prima nota di un mandato generato dalle righe negative del compenso
	 */
	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoCompenso(UserContext userContext, MandatoBulk mandato, CompensoBulk compenso) throws ComponentException, PersistencyException, RemoteException {
		String descEstesaMandato = "mandato " + this.getDescrizione(mandato) + " generato da righe negative del compenso "+compenso.getEsercizio()+"/"+compenso.getCd_unita_organizzativa()+"/"+compenso.getPg_compenso();

		if (mandato.getMandato_rigaColl().isEmpty() || mandato.getMandato_rigaColl().size() > 1)
			throw new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
					" ha un numero di righe non coerente con l'unica prevista per un mandato generato da compenso. Proposta di prima nota non possibile.");

		Mandato_rigaBulk rigaMandato = mandato.getMandato_rigaColl().get(0);
		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		List<Contributo_ritenutaBulk> righeCoriCompenso = Optional.ofNullable(compenso.getChildren()).orElseGet(() -> {
			try {
				Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
				return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compenso);
			} catch (ComponentException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		//Cerco tra le righeCori quella del mandato.... cerco per importo... e se ne esistono di più per descrizione..... se non si individua lancio eccezione
		List<Contributo_ritenutaBulk> righeCoriMandatoByImporto = righeCoriCompenso.stream()
				.filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)<0)
				.filter(el->el.getAmmontare().abs().compareTo(rigaMandato.getIm_mandato_riga().subtract(rigaMandato.getIm_ritenute_riga()))==0)
				.collect(Collectors.toList());

		List<Contributo_ritenutaBulk> righeCori = new ArrayList<>();

		if (righeCoriMandatoByImporto.isEmpty())
			throw new ApplicationException("Il " + descEstesaMandato + " ha un importo che non corrisponde a nessuna riga negativa del compenso stesso. Proposta di prima nota non possibile.");
		if (righeCoriMandatoByImporto.size()>1) {
			List<Contributo_ritenutaBulk> righeCoriMandatoByDesc = righeCoriMandatoByImporto.stream()
					.filter(el->rigaMandato.getDs_mandato_riga().contains(el.getCd_contributo_ritenuta()))
					.collect(Collectors.toList());

			if (righeCoriMandatoByDesc.size() != 1)
				throw new ApplicationException("Il " + descEstesaMandato + " ha un importo che corrisponde a più righe negative del compenso stesso. Proposta di prima nota non possibile.");

			righeCori.add(righeCoriMandatoByDesc.get(0));
		}  else
			righeCori.add(righeCoriMandatoByImporto.get(0));

		final CompensoBulk compensoConguaglio;
		if (mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)!=0)
			//verifico se esiste conguaglio collegato al compenso principale pagato con il mandato
			compensoConguaglio = ((CompensoHome) getHome(userContext, CompensoBulk.class)).findCompensoConguaglioAssociato(userContext, compenso);
		else
			compensoConguaglio = null;

		//se esiste aggiungo le righe di contributo/ritenuta in quanto anche loro contribuiscono a determinare il netto mandato
		if (compensoConguaglio != null) {
			String descConguaglio = compensoConguaglio.getEsercizio() + "/" + compensoConguaglio.getCd_cds() + "/" + compensoConguaglio.getPg_compenso();

			List<Contributo_ritenutaBulk> righeCoriConguaglio = Optional.ofNullable(compensoConguaglio.getChildren()).orElseGet(() -> {
				try {
					Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
					return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compensoConguaglio);
				} catch (ComponentException | PersistencyException e) {
					throw new DetailedRuntimeException(e);
				}
			});

			List<Contributo_ritenutaBulk> righeCoriConguaglioRitenute = righeCoriConguaglio.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());

			BigDecimal totRitenute = righeCoriConguaglioRitenute.stream().map(Contributo_ritenutaBulk::getAmmontare)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (totRitenute.compareTo(mandato.getIm_ritenute())==0) {
				//Aggiungo alle righe da processare solo le righe cori positive che generano ritenute
				righeCori.addAll(righeCoriConguaglioRitenute);
			} else {
				//cerco di recuperare puntualmente la ritenuta
				List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);
				for (Ass_mandato_reversaleBulk assManRev : result) {
					ReversaleIHome revHome = (ReversaleIHome) getHome(userContext, ReversaleIBulk.class);
					ReversaleBulk reversale = (ReversaleIBulk) revHome.findByPrimaryKey(new ReversaleIBulk(assManRev.getCd_cds_reversale(), assManRev.getEsercizio_reversale(), assManRev.getPg_reversale()));

					//Cerco tra le righeCori quella della reversale.... cerco per importo... e se ne esistono di più per descrizione..... se non si individua lancio eccezione
					List<Contributo_ritenutaBulk> righeCoriReversaleByImporto = righeCoriConguaglioRitenute.stream()
							.filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)>0)
							.filter(el->el.getAmmontare().abs().compareTo(reversale.getIm_reversale())==0)
							.collect(Collectors.toList());

					if (righeCoriReversaleByImporto.isEmpty())
						throw new ApplicationException("Il " + descEstesaMandato + " risulta avere una ritenuta di importo che non corrisponde a nessuna delle ritenute " +
								" generate dal conguaglio associato " + descConguaglio + ". Proposta di prima nota non possibile.");
					if (righeCoriReversaleByImporto.size()>1) {
						List<Contributo_ritenutaBulk> righeCoriReversaleByDesc = righeCoriReversaleByImporto.stream()
								.filter(el->reversale.getDs_reversale().contains(el.getCd_contributo_ritenuta()))
								.collect(Collectors.toList());

						if (righeCoriReversaleByDesc.size() != 1)
							throw new ApplicationException("Il " + descEstesaMandato + " risulta avere una ritenuta di importo che corrisponde a più ritenute " +
									" generate dal conguaglio associato " + descConguaglio + ". Proposta di prima nota non possibile.");
						righeCori.add(righeCoriReversaleByDesc.get(0));
					}  else
						righeCori.add(righeCoriReversaleByImporto.get(0));
				}
			}
		}

		//Elaboro le righe Cori che devono aver generato il mandato e le reversali
		righeCori.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)!=0).forEach(cori-> {
			try {
				// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
				// L'Iva, sia per compensa istituzionale che commerciale, va sempre a costo,
				if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
					BigDecimal imCori = cori.getAmmontare();

					//Chiudo il patrimoniale principale del compenso.... quello con la partita
					Voce_epBulk voceEpBanca = this.findContoBanca(userContext, rigaMandato);

					if (cori.isTipoContributoIva() && cori.isContributoPercipiente()) {
						//recupero la scrittura del compenso
						List<Movimento_cogeBulk> allMovimentiPrimaNotaCompenso = this.findMovimentiPrimaNota(userContext, cori.getCompenso());
						Movimento_cogeBulk movimentoToClose = this.findMovimentoAperturaCoriIVACompenso(allMovimentiPrimaNotaCompenso, cori.getCompenso(), cori.getCompenso().getCd_terzo(), cori.getCd_contributo_ritenuta());
						testataPrimaNota.addDettaglio(userContext, movimentoToClose.getTi_riga(), Movimento_cogeBulk.getControSezione(movimentoToClose.getSezione()), movimentoToClose.getConto(), imCori, movimentoToClose.getCd_terzo(), cori.getCompenso(), cori.getCd_contributo_ritenuta());
						testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), movimentoToClose.getSezione(), voceEpBanca, imCori);
					} else {
						Voce_epBulk contoCori;
						if (cori.isTipoContributoDaVersare())
							contoCori = this.findContoContropartita(userContext, cori);
						else
							contoCori = this.findContoTributoCori(userContext, cori);

						//se rigaMandato.getIm_mandato_riga() è uguale a imContributiCaricoEnte il contoPatrimonialePartita non è impostato.
						//Ma in questo caso non dovrebbe mai verificarsi che ci siano contributi a carico percipiente altrimenti il mandato sarebbe negativo
						if (cori.isContributoPercipiente()) {
							testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.getControSezione(cori.getCompenso().getTipoDocumentoEnum().getSezionePatrimoniale()), voceEpBanca, imCori);
							if (cori.isTipoContributoDaVersare())
								testataPrimaNota.openDettaglioPatrimonialeCori(userContext, cori.getCompenso(), contoCori, imCori, cori.getCompenso().getCd_terzo(), cori.getCd_contributo_ritenuta());
							else
								testataPrimaNota.addDettaglio(userContext, cori.getCompenso().getTipoDocumentoEnum().getTipoPatrimoniale(), cori.getCompenso().getTipoDocumentoEnum().getSezionePatrimoniale(), contoCori, imCori);
						} else {
							//recupero la scrittura del compenso
							List<Movimento_cogeBulk> allMovimentiPrimaNotaCompenso = this.findMovimentiPrimaNota(userContext, cori.getCompenso());
							Movimento_cogeBulk movimentoCoriToClose = this.findMovimentoAperturaCori(allMovimentiPrimaNotaCompenso, cori.getCompenso(), cori.getCompenso().getCd_terzo(), cori.getCd_contributo_ritenuta());
							if (!movimentoCoriToClose.getIm_movimento().equals(imCori.abs()) || !movimentoCoriToClose.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE))
								throw new ApplicationException("Il " + descEstesaMandato + " ha un importo che non corrisponde al saldo (" +
										new EuroFormat().format(movimentoCoriToClose.getIm_movimento()) +
										") del compenso stesso registrato per il contributo " + cori.getCd_contributo_ritenuta() + ". Proposta di prima nota non possibile.");
							//chiudo il credito verso l'ente e rilevo il minor versamento
							testataPrimaNota.addDettaglio(userContext, movimentoCoriToClose.getTi_riga(), Movimento_cogeBulk.getControSezione(movimentoCoriToClose.getSezione()), movimentoCoriToClose.getConto(), imCori.abs(), movimentoCoriToClose.getCd_terzo(), cori.getCompenso(), movimentoCoriToClose.getCd_contributo_ritenuta());
							if (cori.isTipoContributoDaVersare())
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), movimentoCoriToClose.getSezione(), contoCori, imCori.abs(), movimentoCoriToClose.getCd_terzo(), cori.getCompenso(), movimentoCoriToClose.getCd_contributo_ritenuta());
							else
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), movimentoCoriToClose.getSezione(), contoCori, imCori);

							if (mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)==0) {
								//apro il conto credito associato alla voce della partita giro e chiudo la tesoreria
								Voce_epBulk aContoRicavo = this.findContoCostoRicavo(userContext, rigaMandato.getElemento_voce());

								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aContoRicavo, imCori.abs());
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, imCori.abs());
							}
						}
					}
				}
			} catch (ComponentException | RemoteException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		if (mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)!=0 && compensoConguaglio == null) {
			List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);

			List<ReversaleBulk> reversali = new ArrayList<>();

			for (Ass_mandato_reversaleBulk assManRev : result) {
				ReversaleIHome revHome = (ReversaleIHome) getHome(userContext, ReversaleIBulk.class);
				reversali.add((ReversaleIBulk) revHome.findByPrimaryKey(new ReversaleIBulk(assManRev.getCd_cds_reversale(), assManRev.getEsercizio_reversale(), assManRev.getPg_reversale())));
			}

			BigDecimal totReversali = reversali.stream().map(ReversaleBulk::getIm_reversale)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (totReversali.compareTo(mandato.getIm_ritenute())!=0)
				throw new ApplicationException("Il " + descEstesaMandato + " risulta avere un importo ritenute che non corrisponde alla somma delle reversali" +
						" effettivamente vincolate. Proposta di prima nota non possibile.");

			//aggiungo la scrittura della reversale vincolata al mandato
			reversali.forEach(reversale->{
				TestataPrimaNota testataPrimaNotaReversale = null;
				try {
					testataPrimaNotaReversale = proposeTestataPrimaNotaReversale(userContext, reversale, false);
				} catch (ComponentException e) {
					throw new DetailedRuntimeException(e);
				} catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException ignored) {
				}
				Optional.ofNullable(testataPrimaNotaReversale).ifPresent(el->testataPrimaNota.getDett().addAll(el.getDett()));
			});
		}

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), false);
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoStipendi(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException, RemoteException {
		//recupero il documento generico passivo leggendolo dalla tabelle stipendiCofiBulk
		Stipendi_cofiBulk stipendiCofiBulk = Optional.ofNullable(mandato.getStipendiCofiBulk())
				.orElseThrow(()->new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
						" non risulta pagare uno stipendio. Proposta di prima nota non possibile."));

		stipendiCofiBulk = (Stipendi_cofiBulk)getHome(userContext, Stipendi_cofiBulk.class).findByPrimaryKey(stipendiCofiBulk);

		//Se trattasi del mandato al quale non ho legato le ritenute, allora sollevo errore
		if (mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)<=0)
			throw new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
					" risulta pagare uno stipendio ma non risulta avere ritenute associate. Proposta di prima nota non possibile.");

		//raggruppo i mandatiRiga per Partita
		List<MandatoRigaComplete> dettaglioFinanziarioList = this.completeRigheMandato(userContext, mandato);

		Map<IDocumentoAmministrativoBulk, Map<Integer, List<MandatoRigaComplete>>> mapDettagli =
				dettaglioFinanziarioList.stream()
						.collect(Collectors.groupingBy(MandatoRigaComplete::getDocamm,
								Collectors.groupingBy(MandatoRigaComplete::getCdTerzo)));

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		//Recupero dal documento generico tutti i conti patrimoniali aperti
		mapDettagli.keySet().forEach(aDocamm -> {
			Map<Integer, List<MandatoRigaComplete>> mapDocAmm = mapDettagli.get(aDocamm);
			mapDocAmm.keySet().forEach(aCdTerzo -> {
				try {
					List<MandatoRigaComplete> mandatoRigaCompleteList = mapDocAmm.get(aCdTerzo);
					addDettagliPrimaNotaMandatoDocumentiVari(userContext, testataPrimaNota, mandato, aDocamm, aCdTerzo, mandatoRigaCompleteList);
				} catch (ComponentException | PersistencyException | RemoteException e) {
					throw new ApplicationRuntimeException(e);
				}
			});
		});

		CompensoBulk compenso = (CompensoBulk)getHome(userContext, CompensoBulk.class).findByPrimaryKey(new CompensoBulk(stipendiCofiBulk.getCd_cds_comp(), stipendiCofiBulk.getCd_uo_comp(), stipendiCofiBulk.getEsercizio_comp(),
				stipendiCofiBulk.getPg_comp()));

		//Chiudo i conti patrimoniali.... per farlo devo capire cosa paga il mandato
		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, mandato.getMandato_rigaColl().get(0));

		List<Contributo_ritenutaBulk> righeCori = Optional.ofNullable(compenso.getChildren()).orElseGet(() -> {
			try {
				Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
				return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compenso);
			} catch (ComponentException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		//Devo leggere la reversale vincolata
		BigDecimal imRitenutePositive = righeCori.stream().map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (imRitenutePositive.compareTo(mandato.getIm_ritenute()) != 0) {
			//potrebbe trattarsi di caso che la reversale è stata legata in parte al mandato
			righeCori = findCoriReversaliAssociate(userContext, mandato);

			imRitenutePositive = righeCori.stream().map(Contributo_ritenutaBulk::getAmmontare)
					.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (imRitenutePositive.compareTo(mandato.getIm_ritenute()) != 0)
				throw new ApplicationException("L'importo delle righe ritenute del compenso associato al mandato non corrisponde con l'importo ritenute associato al mandato.");
		}

		//Registrazione conto CONTRIBUTI-RITENUTE
		//Solo ritenute con importo positivo perchè quelle negative generano mandato a parte
		righeCori.stream().filter(el -> el.getAmmontare().compareTo(BigDecimal.ZERO) > 0).forEach(cori -> {
			try {
				BigDecimal imCori = cori.getAmmontare();

				// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
				if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
					//Riduco la tesoreria e rilevo il debito verso l'erario per le ritenute carico ente/percipiente
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.getControSezione(compenso.getTipoDocumentoEnum().getSezionePatrimoniale()), voceEpBanca, imCori);
					if (cori.isTipoContributoDaVersare()) {
						Voce_epBulk contoCori = this.findContoContropartita(userContext, cori);
						testataPrimaNota.openDettaglioPatrimonialeCori(userContext, compenso, contoCori, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
					} else {
						Voce_epBulk contoCori = this.findContoTributoCori(userContext, cori);
						testataPrimaNota.addDettaglio(userContext, compenso.getTipoDocumentoEnum().getTipoPatrimoniale(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), contoCori, imCori);
					}
				}
			} catch (ComponentException | PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		});

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), true);
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoLiquidazione(UserContext userContext, MandatoBulk mandato, List<Liquidazione_ivaBulk> liquidIvaList) throws ComponentException, PersistencyException, RemoteException {
		if (liquidIvaList == null || liquidIvaList.isEmpty())
			throw new ApplicationException("Il mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
					" non risulta pagare una liquidazione iva. Proposta di prima nota non possibile.");

		Liquidazione_ivaHome liquidazioneIvaHome = (Liquidazione_ivaHome) getHome(userContext, Liquidazione_ivaBulk.class);

		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, mandato.getMandato_rigaColl().get(0));

		//recupero il terzo associato alla UO necessario per chiudere i conti IVA
		TerzoBulk terzoEnte = ((TerzoHome)getHome(userContext, TerzoBulk.class)).findTerzoEnte();
		Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		//recupero tutte le scritture di liquidazione
		for (Liquidazione_ivaBulk liquidIva : liquidIvaList) {
			List<Liquidazione_ivaBulk> liquidazioniDefinitive = liquidazioneIvaHome.findLiquidazioniMassiveDefinitiveList(liquidIva);

			liquidazioniDefinitive.stream().filter(el->!el.getCd_unita_organizzativa().equals(uoEnte.getCd_unita_organizzativa())).forEach(liqIvaUo->{
				try {
					Map<String, Pair<String, BigDecimal>> saldiVoceEp = this.getSaldiMovimentiCoriIvaDebitoEnte(userContext, liqIvaUo);

					if (saldiVoceEp.size()>1)
						throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato()+
								". La scrittura prima nota della liquidazione IVA, di tipo "+liqIvaUo.getTipo_liquidazione()+
								", della UO " + liqIvaUo.getCd_unita_organizzativa() + " per il periodo "+new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_inizio())+
								" - "+new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_fine())+" movimenta più conti patrimoniali verso l'ente. Proposta prima nota non possibile.");

					//Trovo il saldo
					BigDecimal saldoDebitoEnte = saldiVoceEp.values().stream()
							.map(el->el.getFirst().equals(Movimento_cogeBulk.SEZIONE_DARE)?el.getSecond():el.getSecond().negate())
							.reduce(BigDecimal.ZERO,BigDecimal::add);

					BigDecimal ivaDebCredPrec = liqIvaUo.getIva_deb_cred_per_prec();

					saldoDebitoEnte = saldoDebitoEnte.add(ivaDebCredPrec);

					if  (saldoDebitoEnte.compareTo(liqIvaUo.getIva_da_versare()) != 0) {
						testataPrimaNota.getDett().stream().filter(el->el.getPartita()!=null).forEach(el-> System.out.println("Compenso: "+Optional.of(el.getPartita()).map(IDocumentoCogeBulk::getPg_doc).orElse(null)+" - Trib: "+el.getCdCori()+" - Conto: "+el.getCdConto()+
								" - Sezione: "+el.getSezione() + " - Importo: " + new EuroFormat().format(el.getImporto())));

						throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " +
								mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() + ". Il saldo del conto debito generato dalla scrittura prima nota della liquidazione IVA, " +
								"di tipo "+liqIvaUo.getTipo_liquidazione()+", della UO " + liqIvaUo.getCd_unita_organizzativa() +
								" per il periodo "+new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_inizio())+
								" - "+new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_fine())+" ("+new EuroFormat().format(saldoDebitoEnte)+
								") non risulterebbe essere uguale all'importo della liquidazione IVA ("+
								new EuroFormat().format(liqIvaUo.getIva_da_versare()) + ").");
					}

					saldiVoceEp.keySet().forEach(cdVoceEp -> {
						Pair<String, BigDecimal> saldoVoce = saldiVoceEp.get(cdVoceEp);
						if (saldoVoce.getSecond().compareTo(BigDecimal.ZERO) != 0)
							if (saldoVoce.getFirst().equals(Movimento_cogeBulk.SEZIONE_AVERE)) {
								//Chiudo il debito IVA fattura e lo giro all'ente
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, cdVoceEp, saldoVoce.getSecond(), terzoEnte.getCd_terzo());
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, saldoVoce.getSecond());
							} else {
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, cdVoceEp, saldoVoce.getSecond(), terzoEnte.getCd_terzo());
								testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_DARE, voceEpBanca, saldoVoce.getSecond());
							}
					});
				} catch(ComponentException|PersistencyException ex) {
					throw new ApplicationRuntimeException(ex);
				}
			});

			Liquidazione_ivaBulk liquidIvaUoEnte = liquidazioniDefinitive.stream().filter(el->el.getCd_unita_organizzativa().equals(uoEnte.getCd_unita_organizzativa()))
					.findAny().orElseThrow(()->new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " +
							mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() + ". Manca la liquidazione definitiva della UO Ente."));

			if (Optional.ofNullable(liquidIvaUoEnte.getAcconto_iva_vers()).filter(el->el.compareTo(BigDecimal.ZERO)!=0).isPresent()) {
				if (liquidIvaUoEnte.getAcconto_iva_vers().compareTo(BigDecimal.ZERO)>0) {
					Voce_epBulk cdVoceEp;
					if ("P".equals(liquidIvaUoEnte.getTipoLiquid()))
						cdVoceEp = findContoIvaSplitCredito(userContext, liquidIvaUoEnte.getEsercizio());
					else
						cdVoceEp = findContoIvaCredito(userContext, liquidIvaUoEnte.getEsercizio());

					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, cdVoceEp, liquidIvaUoEnte.getAcconto_iva_vers(), terzoEnte.getCd_terzo());
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_DARE, voceEpBanca, liquidIvaUoEnte.getAcconto_iva_vers());
				} else {
					Voce_epBulk cdVoceEp;
					if ("P".equals(liquidIvaUoEnte.getTipoLiquid()))
						cdVoceEp = findContoIvaSplitDebito(userContext, liquidIvaUoEnte.getEsercizio());
					else
						cdVoceEp = findContoIvaDebito(userContext, liquidIvaUoEnte.getEsercizio());
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, cdVoceEp, liquidIvaUoEnte.getAcconto_iva_vers(), terzoEnte.getCd_terzo());
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, liquidIvaUoEnte.getAcconto_iva_vers());
				}
			}
		}

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), true);
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaLiquidazioneUo(UserContext userContext, Liquidazione_ivaBulk liqIvaUo) throws ComponentException {
		try {
			Fattura_passiva_IHome fatturaPassivaIHome = (Fattura_passiva_IHome) getHome(userContext, Fattura_passiva_IBulk.class);
			Nota_di_creditoHome notaCreditoPassivaHome = (Nota_di_creditoHome) getHome(userContext, Nota_di_creditoBulk.class);
			Nota_di_debitoHome notaDebitoPassivaHome = (Nota_di_debitoHome) getHome(userContext, Nota_di_debitoBulk.class);
			Fattura_attiva_IHome fatturaAttivaIHome = (Fattura_attiva_IHome) getHome(userContext, Fattura_attiva_IBulk.class);
			Nota_di_credito_attivaHome notaCreditoAttivaHome = (Nota_di_credito_attivaHome) getHome(userContext, Nota_di_credito_attivaBulk.class);
			Nota_di_debito_attivaHome notaDebitoAttivaHome = (Nota_di_debito_attivaHome) getHome(userContext, Nota_di_debito_attivaBulk.class);

			//verifico innanzitutto che la luidazione è definitiva
			if (!liqIvaUo.isDefinitiva())
				throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del tipo liquidazione IVA " + liqIvaUo.getTipo_liquidazione() +
						" della UO " + liqIvaUo.getCd_unita_organizzativa() + " per il periodo " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_inizio()) +
						" - " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_fine()) + ". La liquidazione non risulta in stato definitivo.");

			List<Tipo_sezionaleBulk> allTipiSezionale = getHome(userContext, Tipo_sezionaleBulk.class).findAll();
			//Recupero tutti i registri in base al tipo sezionale oggetto di liquidazione
			List<Tipo_sezionaleBulk> tipiSezionale = allTipiSezionale.stream().filter(el -> el.isTipoSezionale(liqIvaUo.getTipo_liquidazione())).collect(Collectors.toList());

			//recupero il terzo associato alla UO necessario per chiudere i conti IVA
			TerzoBulk terzoEnte = ((TerzoHome) getHome(userContext, TerzoBulk.class)).findTerzoEnte();
			Voce_epBulk aVoceErarioContoIva = this.findContoErarioContoIva(userContext, liqIvaUo.getEsercizio());

			TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

			for (Tipo_sezionaleBulk tipoSezionale : tipiSezionale) {
				List<IDocumentoAmministrativoBulk> allDocumentiUoLiquidati = new ArrayList<>();
				if (tipoSezionale.isAcquisti()) {
					{
						SQLBuilder sqlFatturaPassiva = fatturaPassivaIHome.createSQLBuilder();
						sqlFatturaPassiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlFatturaPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlFatturaPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlFatturaPassiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(fatturaPassivaIHome.fetchAll(sqlFatturaPassiva));
					}
					{
						SQLBuilder sqlNotaCreditoPassiva = notaCreditoPassivaHome.createSQLBuilder();
						sqlNotaCreditoPassiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlNotaCreditoPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlNotaCreditoPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlNotaCreditoPassiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(notaCreditoPassivaHome.fetchAll(sqlNotaCreditoPassiva));
					}
					{
						SQLBuilder sqlNotaDebitoPassiva = notaDebitoPassivaHome.createSQLBuilder();
						sqlNotaDebitoPassiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlNotaDebitoPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlNotaDebitoPassiva.addClause(FindClause.AND, "dt_registrazione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlNotaDebitoPassiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(notaDebitoPassivaHome.fetchAll(sqlNotaDebitoPassiva));
					}
				} else {
					{
						SQLBuilder sqlFatturaAttiva = fatturaAttivaIHome.createSQLBuilder();
						sqlFatturaAttiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlFatturaAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlFatturaAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlFatturaAttiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(fatturaAttivaIHome.fetchAll(sqlFatturaAttiva));
					}
					{
						SQLBuilder sqlNotaCreditoAttiva = notaCreditoAttivaHome.createSQLBuilder();
						sqlNotaCreditoAttiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlNotaCreditoAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlNotaCreditoAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlNotaCreditoAttiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(notaCreditoAttivaHome.fetchAll(sqlNotaCreditoAttiva));
					}
					{
						SQLBuilder sqlNotaDebitoAttiva = notaDebitoAttivaHome.createSQLBuilder();
						sqlNotaDebitoAttiva.addClause(FindClause.AND, "cd_uo_origine", SQLBuilder.EQUALS, liqIvaUo.getCd_unita_organizzativa());
						sqlNotaDebitoAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.GREATER_EQUALS, liqIvaUo.getDt_inizio());
						sqlNotaDebitoAttiva.addClause(FindClause.AND, "dt_emissione", SQLBuilder.LESS_EQUALS, liqIvaUo.getDt_fine());
						sqlNotaDebitoAttiva.addClause(FindClause.AND, "cd_tipo_sezionale", SQLBuilder.EQUALS, tipoSezionale.getCd_tipo_sezionale());
						allDocumentiUoLiquidati.addAll(notaDebitoAttivaHome.fetchAll(sqlNotaDebitoAttiva));
					}
				}

				//Con la lista dei documenti che sono andati in liquidazione, vado a recuperare le scritture generate
				for (IDocumentoAmministrativoBulk docamm : allDocumentiUoLiquidati.stream().filter(el->!el.isAnnullato()).collect(Collectors.toList())) {
					Integer cdTerzoDocamm;
					if (docamm instanceof Fattura_passivaBulk)
						cdTerzoDocamm = ((Fattura_passivaBulk)docamm).getCd_terzo();
					else
						cdTerzoDocamm = ((Fattura_attivaBulk)docamm).getCd_terzo();

					Map<String, Map<String, Pair<String, BigDecimal>>> saldiCoriVoceEp;
					Optional<Scrittura_partita_doppiaBulk> scritturaOpt;

					//recupero tutti i movimenti della partita per ottenere il saldo al netto della scrittura del mandato se già esiste
					if (Optional.of(docamm).filter(Fattura_passivaBulk.class::isInstance).map(Fattura_passivaBulk.class::cast).map(Fattura_passivaBulk::isGenerataDaCompenso).orElse(Boolean.FALSE))
						// Le fatture generate da compenso non creano scritture di prima nota in quanto create direttamente dal compenso stesso
						scritturaOpt = this.getScritturaPartitaDoppia(userContext, ((Fattura_passivaBulk) docamm).getCompenso());
					else
						scritturaOpt = this.getScritturaPartitaDoppia(userContext, docamm);

					if (!scritturaOpt.isPresent())
						throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del tipo liquidazione IVA " + liqIvaUo.getTipo_liquidazione() +
								" della UO " + liqIvaUo.getCd_unita_organizzativa() + " per il periodo " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_inizio()) +
								" - " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_fine()) + ". Per il documento " + docamm.getCd_tipo_doc_amm() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" +
								docamm.getPg_doc() + " non risulta essere stata generata la scrittura di prima nota.");

					saldiCoriVoceEp = this.getSaldiMovimentiCoriIva(scritturaOpt.get());

					//dovrei trovare tra i saldi proprio l'import liquidato
					//Il conto aperto deve essere solo uno per segno...... la presenza di 2 segni capita per le fatture Commerciali con Split
					if (saldiCoriVoceEp.values().stream().flatMap(el -> el.values().stream()).filter(el->el.getFirst().equals(Movimento_cogeBulk.SEZIONE_DARE))
							.filter(el -> el.getSecond().compareTo(BigDecimal.ZERO) != 0).count() > 1)
						throw new ApplicationRuntimeException("Per il documento " + docamm.getCd_tipo_doc_amm() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" +
								docamm.getPg_doc() + " e per le righe IVA esiste più di un conto che presenta un saldo positivo in segno Dare.");

					if (saldiCoriVoceEp.values().stream().flatMap(el -> el.values().stream()).filter(el->el.getFirst().equals(Movimento_cogeBulk.SEZIONE_AVERE))
							.filter(el -> el.getSecond().compareTo(BigDecimal.ZERO) != 0).count() > 1)
						throw new ApplicationRuntimeException("Per il documento " + docamm.getCd_tipo_doc_amm() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" +
								docamm.getPg_doc() + " e per le righe IVA esiste più di un conto che presenta un saldo positivo in segno Avere.");

					scritturaOpt.get().getAllMovimentiColl()
							.stream()
							.filter(Movimento_cogeBulk::isRigaTipoIva)
							.forEach(el->{
								if (el.isSezioneAvere()) {
									//Chiudo il debito IVA fattura e lo giro all'ente
									testataPrimaNota.addDettaglio(userContext, el.getTi_riga(), Movimento_cogeBulk.SEZIONE_DARE, el.getCd_voce_ep(), el.getIm_movimento(), cdTerzoDocamm, el.getDocumentoCoge(), el.getCd_contributo_ritenuta());
									testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aVoceErarioContoIva, el.getIm_movimento(), terzoEnte.getCd_terzo());
								} else {
									testataPrimaNota.addDettaglio(userContext, el.getTi_riga(), Movimento_cogeBulk.SEZIONE_AVERE, el.getCd_voce_ep(), el.getIm_movimento(), cdTerzoDocamm, el.getDocumentoCoge(), el.getCd_contributo_ritenuta());
									testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, aVoceErarioContoIva, el.getIm_movimento(), terzoEnte.getCd_terzo());
								}
							});
				}
			}

			if (liqIvaUo.getIva_liq_esterna().compareTo(BigDecimal.ZERO)!=0) {
				//Cambio il segno perchè se negativo vuol dire che ho un debito
				BigDecimal importoIvaEsterna = liqIvaUo.getIva_liq_esterna().negate();
				Voce_epBulk contoCostoLiquidazioneIvaEsterna = this.findContoCostoLiquidazioneIvaEsterna(userContext, CNRUserContext.getEsercizio(userContext));
				Voce_epBulk contoDebitoUoLiquidazioneIvaEsterna = this.findContoDebitoUoLiquidazioneIvaEsterna(userContext, CNRUserContext.getEsercizio(userContext));
				Voce_epBulk contoDebitoEnteLiquidazioneIvaEsterna = this.findContoDebitoUoLiquidazioneIvaEsterna(userContext, CNRUserContext.getEsercizio(userContext));
				TerzoBulk terzoUo = ((TerzoHome) getHome(userContext, TerzoBulk.class)).findTerzoPerUo(new Unita_organizzativaBulk(liqIvaUo.getCd_unita_organizzativa()));
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoLiquidazioneIvaEsterna, importoIvaEsterna);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoDebitoUoLiquidazioneIvaEsterna, importoIvaEsterna, terzoUo.getCd_terzo());

				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoDebitoUoLiquidazioneIvaEsterna, importoIvaEsterna, terzoUo.getCd_terzo());
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoDebitoEnteLiquidazioneIvaEsterna, importoIvaEsterna, terzoEnte.getCd_terzo());
			}

			BigDecimal importoProRata = Optional.ofNullable(liqIvaUo.getIva_credito_no_prorata()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(liqIvaUo.getIva_credito()).orElse(BigDecimal.ZERO));
			if (importoProRata.compareTo(BigDecimal.ZERO)!=0) {
				Voce_epBulk contoCostoIvaNonDetraibile = this.findContoCostoIvaNonDetraibile(userContext, CNRUserContext.getEsercizio(userContext));
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.COSTO.value(), Movimento_cogeBulk.SEZIONE_DARE, contoCostoIvaNonDetraibile, importoProRata);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aVoceErarioContoIva, importoProRata, terzoEnte.getCd_terzo());
			}

			BigDecimal ivaDebCredPrec = Optional.ofNullable(liqIvaUo.getIva_deb_cred_per_prec()).orElse(BigDecimal.ZERO);

			BigDecimal saldoDebitoUo = testataPrimaNota.getDett().stream()
					.filter(el -> el.getTipoDett().equals(Movimento_cogeBulk.TipoRiga.DEBITO.value()))
					.filter(el -> !Optional.ofNullable(el.getPartita()).isPresent())
					.map(el -> el.isSezioneDare() ? el.getImporto() : el.getImporto().negate())
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			saldoDebitoUo = saldoDebitoUo.add(ivaDebCredPrec);

			if (saldoDebitoUo.compareTo(liqIvaUo.getIva_da_versare()) != 0) {
				testataPrimaNota.getDett().stream().filter(el -> el.getPartita() != null).forEach(el -> System.out.println("Partita: " + el.getPartita() + " - Trib: " + el.getCdCori() + " - Conto: " + el.getCdConto() +
						" - Sezione: " + el.getSezione() + " - Importo: " + new EuroFormat().format(el.getImporto())));

				throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del tipo liquidazione IVA '" +
						Stampa_registri_ivaVBulk.TIPI_SEZIONALE_KEYS.get(liqIvaUo.getTipo_liquidazione()) +
						"' della UO " + liqIvaUo.getCd_unita_organizzativa() + " per il periodo " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_inizio()) +
						" - " + new SimpleDateFormat("dd/MM/yyyy").format(liqIvaUo.getDt_fine()) + ". Il saldo del conto debito (" +
						new EuroFormat().format(saldoDebitoUo) +
						") non risulterebbe essere uguale all'importo della liquidazione IVA (" +
						new EuroFormat().format(liqIvaUo.getIva_da_versare()) + ").");
			}
			return this.generaScrittura(userContext, liqIvaUo, Collections.singletonList(testataPrimaNota), true);
		} catch (PersistencyException|RemoteException ex) {
			throw new ApplicationRuntimeException(ex);
		}
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoVersamentoCori(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException, RemoteException {
		//verifico se trattasi di liquidazione IVA..... per essere tale deve avere un'unica riga ed un documento generico registrato sulla tabella LIQUIDAZIONE
		List<Liquidazione_ivaBulk> liquidazioneIvaList = new ArrayList<>();

		mandato.getMandato_rigaColl().forEach(mr->{
			try {
				liquidazioneIvaList.addAll(((Liquidazione_ivaHome) getHome(userContext, Liquidazione_ivaBulk.class)).findByMandatoRiga(mr));
			} catch(ComponentException|PersistencyException ex) {
				throw new ApplicationRuntimeException(ex);
			}
		});

		if (!liquidazioneIvaList.isEmpty())
			return proposeScritturaPartitaDoppiaMandatoLiquidazione(userContext, mandato, liquidazioneIvaList);

		Collection<Liquid_gruppo_coriBulk> liquidGruppoCoriBulk =
				((Liquid_gruppo_coriHome) getHome(userContext, Liquid_gruppo_coriBulk.class)).findByMandato(userContext, mandato)
						.stream()
						.flatMap(el->{
							if (!el.getFl_accentrato() && !Optional.ofNullable(el.getPg_gruppo_centro()).isPresent())
								return Stream.of(el);
							else {
								try {
									return ((Liquid_gruppo_coriHome) getHome(userContext, Liquid_gruppo_coriBulk.class)).findLiquidazioniAccentrate(userContext, el).stream();
								} catch(ComponentException|PersistencyException ex) {
									throw new ApplicationRuntimeException(ex);
								}
							}
						}).collect(Collectors.toList());

		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, mandato.getMandato_rigaColl().get(0));

		Optional<Scrittura_partita_doppiaBulk> scritturaMandato = Optional.ofNullable(mandato.getScrittura_partita_doppia())
				.map(spd->Optional.of(spd).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED)).orElseGet(()-> {
					try {
						Scrittura_partita_doppiaHome home = (Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class);
						return home.getScrittura(userContext, mandato);
					} catch (ComponentException e) {
						throw new DetailedRuntimeException(e);
					}
				});

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		liquidGruppoCoriBulk.forEach(liquid->{
			try {
				Collection<Liquid_gruppo_cori_detBulk> details = ((Liquid_gruppo_coriHome) getHome(userContext, Liquid_gruppo_coriBulk.class)).findDettagli(userContext, liquid);

				//a questo punto ho la lista dei compensi e dei codici contributi versati
				//Raggruppo per compenso e codice tributo (per evitare che mi escano 2 righe P e C)
				Map<Integer, Map<Long, Map<String, List<Liquid_gruppo_cori_detBulk>>>> detailsCompensi =
						details.stream().collect(Collectors.groupingBy(Liquid_gruppo_cori_detBase::getEsercizio_contributo_ritenuta,
								Collectors.groupingBy(Liquid_gruppo_cori_detKey::getPg_compenso,
										Collectors.groupingBy(Liquid_gruppo_cori_detKey::getCd_contributo_ritenuta))));

				TestataPrimaNota testataPrimaNotaLiquid = new TestataPrimaNota();

				//e per ognuno di essi faccio la scrittura
				detailsCompensi.keySet().forEach(aEsercizioCompenso -> {
					Map<Long, Map<String, List<Liquid_gruppo_cori_detBulk>>> mapPgCompensi = detailsCompensi.get(aEsercizioCompenso);
					mapPgCompensi.keySet().forEach(aPgCompenso -> {
						Map<String, List<Liquid_gruppo_cori_detBulk>> mapCdCori = mapPgCompensi.get(aPgCompenso);
						try {
							CompensoBulk compenso = (CompensoBulk) getHome( userContext, CompensoBulk.class).findByPrimaryKey(
									new CompensoBulk(liquid.getCd_cds_origine(), liquid.getCd_uo_origine(), aEsercizioCompenso, aPgCompenso));

							//verifico se è un compenso da stipendi recuperando il documento generico passivo leggendolo dalla tabelle stipendiCofiBulk
							Stipendi_cofiBulk stipendiCofiBulk = ((Stipendi_cofiHome)getHome(userContext, Stipendi_cofiBulk.class)).findStipendiCofi(compenso);

							//Individuo i mandati associati al compenso per eventuale generazione di scrittura
							List<MandatoBulk> pMandatiCompenso = new ArrayList<>();

							if (stipendiCofiBulk!=null) {
								MandatoBulk mandatoStipendiBulk = (MandatoBulk) getHome(userContext, MandatoIBulk.class)
										.findByPrimaryKey(new MandatoBulk(stipendiCofiBulk.getCd_cds_mandato(), stipendiCofiBulk.getEsercizio_mandato(), stipendiCofiBulk.getPg_mandato()));
								pMandatiCompenso.add(mandatoStipendiBulk);
							}

							List<MandatoBulk> pMandatiAssociati = ((CompensoHome)getHome( userContext, CompensoBulk.class)).findMandatiAssociati(userContext, compenso);

							pMandatiAssociati.stream().filter(el->pMandatiCompenso.stream().noneMatch(el2->el2.equalsByPrimaryKey(el)))
									.forEach(pMandatiCompenso::add);

							pMandatiCompenso.forEach(el-> {
								try {
									completeMandato(userContext, el);
								} catch (ComponentException|PersistencyException e) {
									throw new DetailedRuntimeException(e);
								}
							});

							mapCdCori.keySet()
									.forEach(aCdCori -> {
										try {
											//recupero tutti i movimenti della partita per ottenere il saldo al netto della scrittura del mandato se già esiste
											Map<String, Pair<String, BigDecimal>> saldiCori = this.getSaldiMovimentiCori(userContext, compenso, compenso.getCd_terzo(), aCdCori, scritturaMandato, pMandatiCompenso);

											//dovrei trovare tra i saldi proprio l'import liquidato
											//Il conto aperto deve essere solo uno e deve essere in segno AVERE
											if (saldiCori.values().stream().filter(el -> el.getSecond().compareTo(BigDecimal.ZERO) != 0).count() > 1)
												throw new ApplicationRuntimeException("Per il compenso " + compenso.getEsercizio() + "/" + compenso.getCd_cds() + "/" + compenso.getPg_compenso() +
														" e per il contributo " + aCdCori + " esiste più di un conto che presenta un saldo positivo.");

											saldiCori.keySet().forEach(cdVoceEp -> {
												Pair<String, BigDecimal> saldoVoce = saldiCori.get(cdVoceEp);
												if (saldoVoce.getSecond().compareTo(BigDecimal.ZERO) != 0)
													if (saldoVoce.getFirst().equals(Movimento_cogeBulk.SEZIONE_AVERE)) {
														testataPrimaNotaLiquid.closeDettaglioPatrimonialeCori(userContext, compenso, cdVoceEp, saldoVoce.getSecond(), compenso.getCd_terzo(), aCdCori);
														testataPrimaNotaLiquid.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), voceEpBanca, saldoVoce.getSecond());
													} else {
														testataPrimaNotaLiquid.openDettaglioPatrimonialeCori(userContext, compenso, cdVoceEp, saldoVoce.getSecond(), compenso.getCd_terzo(), aCdCori);
														testataPrimaNotaLiquid.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.getControSezione(compenso.getTipoDocumentoEnum().getSezionePatrimoniale()), voceEpBanca, saldoVoce.getSecond());
													}
											});
										} catch (ComponentException | PersistencyException ex) {
											throw new ApplicationRuntimeException(ex);
										}
									});
						} catch(ComponentException|PersistencyException ex) {
							throw new ApplicationRuntimeException(ex);
						}
					});
				});

				BigDecimal saldoTesoreria = testataPrimaNotaLiquid.getDett().stream()
						.filter(el->el.getTipoDett().equals(Movimento_cogeBulk.TipoRiga.TESORERIA.value()))
						.map(el -> el.isSezioneDare() ? el.getImporto() : el.getImporto().negate())
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				if  (saldoTesoreria.negate().compareTo(liquid.getIm_liquidato()) != 0) {
					testataPrimaNotaLiquid.getDett().stream().filter(el->el.getPartita()!=null).forEach(el-> System.out.println("Compenso: "+Optional.of(el.getPartita()).map(IDocumentoCogeBulk::getPg_doc).orElse(null)+" - Trib: "+el.getCdCori()+" - Conto: "+el.getCdConto()+
							" - Sezione: "+el.getSezione() + " - Importo: " + new EuroFormat().format(el.getImporto())));
					throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " +
							mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() + ". Il saldo del conto tesoreria (" +
							new EuroFormat().format(saldoTesoreria.negate()) +
							") non risulterebbe essere uguale all'importo della liquidazione della UO " + liquid.getCd_uo_origine() + " (" +
							new EuroFormat().format(liquid.getIm_liquidato()) + ").");
				}

//				System.out.println("Liquidazione completata della UO: " + liquid.getCd_uo_origine());

				testataPrimaNota.getDett().addAll(testataPrimaNotaLiquid.getDett());
			} catch(ComponentException|PersistencyException ex) {
				throw new ApplicationRuntimeException(ex);
			}
		});

		if (mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)>0) {
			List<Contributo_ritenutaBulk> righeCoriReversaliAssociate = findCoriReversaliAssociate(userContext, mandato);
			righeCoriReversaliAssociate.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)>0).forEach(cori->{
				try {
					BigDecimal imCori = cori.getAmmontare();
					CompensoBulk compenso = (CompensoBulk)getHome(userContext, CompensoBulk.class).findByPrimaryKey(cori.getCompenso());

					// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
					if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
						//Riduco la tesoreria e rilevo il debito verso l'erario per le ritenute carico ente/percipiente
						testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.getControSezione(compenso.getTipoDocumentoEnum().getSezionePatrimoniale()), voceEpBanca, imCori);
						if (cori.isTipoContributoDaVersare()) {
							Voce_epBulk contoCori = this.findContoContropartita(userContext, cori);
							testataPrimaNota.openDettaglioPatrimonialeCori(userContext, compenso, contoCori, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
						} else {
							Voce_epBulk contoCori = this.findContoTributoCori(userContext, cori);
							testataPrimaNota.addDettaglio(userContext, compenso.getTipoDocumentoEnum().getTipoPatrimoniale(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), contoCori, imCori);
						}
					}
				} catch (ComponentException|PersistencyException e) {
					throw new ApplicationRuntimeException(e);
				}
			});
		}

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), true);
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaMandatoAccantonamentoCoriDocgen(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException, RemoteException {
		String descEstesaMandato = "Mandato " + this.getDescrizione(mandato) + ", di tipo documento generico con ritenute di conguaglio.";

		boolean isAccantonamentoCoriDocgen = mandato.getMandato_rigaColl().size()==1 && mandato.getIm_ritenute().compareTo(BigDecimal.ZERO)>0 &&
				mandato.getMandato_rigaColl().stream().map(Mandato_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoSpesa());

		if (!isAccantonamentoCoriDocgen)
			throw new ApplicationException("La riga del mandato " + this.getDescrizione(mandato) +
					" non risulta pagare un documento generico. Proposta di prima nota non possibile.");

		//Il mandato di questa tipologia deve essere sempre d''importo a ZERO dato che è stato emesso a fronte di un conguaglio che prevede quote ulteriori
		//da trattenere al terzo e non ci sono più compensi da pagare.
		if (mandato.getIm_mandato().subtract(mandato.getIm_ritenute()).compareTo(BigDecimal.ZERO)!=0)
			throw new ApplicationException(descEstesaMandato + " di importo non nullo. Proposta di prima nota non possibile.");

		List<MandatoRigaComplete> dettaglioFinanziarioList = this.completeRigheMandato(userContext, mandato);

		if (dettaglioFinanziarioList.size()>1)
			throw new ApplicationException(descEstesaMandato+" con più di una riga. Proposta di prima nota non possibile.");

		MandatoRigaComplete rigaMandato = dettaglioFinanziarioList.stream().findAny()
				.orElseThrow(()->new ApplicationException(descEstesaMandato + " senza righe. Proposta di prima nota non possibile."));

		Optional<Scrittura_partita_doppiaBulk> scritturaMandato = Optional.ofNullable(mandato.getScrittura_partita_doppia())
				.map(spd->Optional.of(spd).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED)).orElseGet(()-> {
					try {
						Scrittura_partita_doppiaHome home = (Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class);
						return home.getScrittura(userContext, mandato);
					} catch (ComponentException e) {
						throw new DetailedRuntimeException(e);
					}
				});

		//recupero tutti i movimenti della partita per ottenere il saldo al netto della scrittura del mandato se già esiste
		Map<String, Pair<String, BigDecimal>> saldiPartita = this.getSaldiMovimentiPartita(userContext, rigaMandato.getDocamm(), rigaMandato.getCdTerzo(), scritturaMandato);

		if (saldiPartita.isEmpty())
			throw new ApplicationException("Errore nell'individuazione del conto patrimoniale da utilizzare per la scrittura di chiusura del debito " +
					"del documento associato " + rigaMandato.getDocamm().getCd_tipo_doc() + "/" + rigaMandato.getDocamm().getEsercizio() + "/" + rigaMandato.getDocamm().getCd_uo() + "/" + rigaMandato.getDocamm().getPg_doc() + ": non risulta" +
					" movimentato nessun conto patrimoniale.");
		else if (saldiPartita.size()>1)
			throw new ApplicationException("Errore nell'individuazione del conto patrimoniale da utilizzare per la scrittura di chiusura del debito " +
					"del documento associato " + rigaMandato.getDocamm().getCd_tipo_doc() + "/" + rigaMandato.getDocamm().getEsercizio() + "/" + rigaMandato.getDocamm().getCd_uo() + "/" + rigaMandato.getDocamm().getPg_doc() + ": risultano" +
					" movimentati troppi conti patrimoniali.");

		String cdVocePatrimoniale = saldiPartita.keySet().stream().findAny().orElse(null);
		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, mandato.getMandato_rigaColl().get(0));

		//devo recuperare il compenso di conguaglio tramite le reversali associate al mandato
		CompensoBulk compensoConguaglio = null;

		List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);

		for (Ass_mandato_reversaleBulk assMandatoReversaleBulk : result) {
			Collection<V_doc_cont_compBulk> result2 = ((V_doc_cont_compHome) getHome(userContext, V_doc_cont_compBulk.class)).findByDocumento(assMandatoReversaleBulk.getEsercizio_reversale(), assMandatoReversaleBulk.getCd_cds_reversale(), assMandatoReversaleBulk.getPg_reversale(), V_doc_cont_compBulk.TIPO_DOC_CONT_REVERSALE);
			if (result2.isEmpty())
				throw new ApplicationRuntimeException("Errore nell'individuazione del compenso di conguaglio a cui è collegato il mandato " + rigaMandato.getMandatoRiga().getEsercizio() + "/" + rigaMandato.getMandatoRiga().getCd_cds() + "/" + rigaMandato.getMandatoRiga().getPg_mandato() +
						": la reversale " + assMandatoReversaleBulk.getEsercizio_reversale() + "/" + assMandatoReversaleBulk.getCd_cds_reversale() + "/" + assMandatoReversaleBulk.getPg_reversale() +
						", collegata al mandato, non risulta collegata a nessun compenso.");
			if (result2.size() > 1)
				throw new ApplicationRuntimeException("Errore nell'individuazione del compenso di conguaglio a cui è collegato il mandato " + rigaMandato.getMandatoRiga().getEsercizio() + "/" + rigaMandato.getMandatoRiga().getCd_cds() + "/" + rigaMandato.getMandatoRiga().getPg_mandato() +
						": la reversale " + assMandatoReversaleBulk.getEsercizio_reversale() + "/" + assMandatoReversaleBulk.getCd_cds_reversale() + "/" + assMandatoReversaleBulk.getPg_reversale() +
						", collegata al mandato, risulta collegata a troppi compensi.");

			V_doc_cont_compBulk docContCompBulk = result2.stream().findAny().get();
			if (compensoConguaglio == null)
				compensoConguaglio = new CompensoBulk(docContCompBulk.getCd_cds_compenso(), docContCompBulk.getCd_uo_compenso(), docContCompBulk.getEsercizio_compenso(), docContCompBulk.getPg_compenso());
			else if (compensoConguaglio.equalsByPrimaryKey(docContCompBulk.getCompenso()))
				throw new ApplicationRuntimeException("Errore nell'individuazione del compenso di conguaglio a cui è collegato il mandato " + this.getDescrizione(mandato) +
						": le reversali, collegate al mandato del compenso principale, risultano collegate a compensi diversi.");
		}

		CompensoBulk compenso = Optional.ofNullable(compensoConguaglio).map(el->{
			try {
				return (CompensoBulk) getHome(userContext, CompensoBulk.class).findByPrimaryKey(el);
			} catch (ComponentException|PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		}).orElse(null);

        if (compenso != null && !compenso.getFl_compenso_conguaglio())
            throw new ApplicationException(descEstesaMandato + " legato a reversali emesse non a fronte di un conguaglio. Proposta di prima nota non possibile.");

        List<Contributo_ritenutaBulk> righeCori = Optional.ofNullable(compenso.getChildren()).orElseGet(()->{
			try {
				Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
				return (List<Contributo_ritenutaBulk>)home.loadContributiRitenute(compenso);
			} catch (ComponentException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		BigDecimal imRitenutePositive = righeCori.stream()
				.map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		//Aggiungo eventuali quote da trattenere indicate sul compenso e sul compenso conguaglio
		imRitenutePositive = imRitenutePositive.add(Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO));

		if (imRitenutePositive.compareTo(mandato.getIm_ritenute())!=0)
			throw new ApplicationException(descEstesaMandato+" con ritenute ("+ new EuroFormat().format(mandato.getIm_ritenute()) +
					") diverse da quelle indicate sul conguaglio (" + new EuroFormat().format(imRitenutePositive) +
					"). Proposta di prima nota non possibile.");

		//recupero la scrittura del compenso
		List<Movimento_cogeBulk> allMovimentiPrimaNotaCompenso = this.findMovimentiPrimaNota(userContext,compenso);

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		//Prima chiudo il documento generico per il suo importo totale
		testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, cdVocePatrimoniale, rigaMandato.getMandatoRiga().getIm_mandato_riga(), rigaMandato.getCdTerzo(), rigaMandato.getDocamm());

		//Poi movimento il conto banca per l'importo netto del mandato
		BigDecimal imNettoMandato = mandato.getIm_mandato().subtract(mandato.getIm_ritenute());
		testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, imNettoMandato);

		//Registrazione conto CONTRIBUTI-RITENUTE
		righeCori.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)>0).forEach(cori->{
			try {
				BigDecimal imCori = cori.getAmmontare();

				// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
				if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
					//Per i contributi Percipiente non faccio nulla perchè le scritture si compensano tra loro
					if (cori.isContributoEnte()) {
						String contoToClose = this.findMovimentoAperturaCori(allMovimentiPrimaNotaCompenso, compenso, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta()).getCd_voce_ep();
						testataPrimaNota.closeDettaglioPatrimonialeCori(userContext, compenso, contoToClose, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
					}
					if (cori.isTipoContributoDaVersare()) {
						Voce_epBulk contoCori = this.findContoContropartita(userContext, cori);
						testataPrimaNota.openDettaglioPatrimonialeCori(userContext, compenso, contoCori, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
					} else {
						Voce_epBulk contoCori = this.findContoTributoCori(userContext, cori);
						testataPrimaNota.addDettaglio(userContext, compenso.getTipoDocumentoEnum().getTipoPatrimoniale(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), contoCori, imCori);
					}
				}
			} catch (ComponentException|PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		});

		return this.generaScrittura(userContext, mandato, Collections.singletonList(testataPrimaNota), true);
	}

	private Scrittura_partita_doppiaBulk proposeScritturaPartitaDoppiaReversale(UserContext userContext, ReversaleBulk reversale) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		try {
			if (reversale.isAnnullato())
				return this.proposeScritturaPartitaDoppiaManRevAnnullato(userContext, reversale);
			TestataPrimaNota testataPrimaNota = proposeTestataPrimaNotaReversale(userContext, reversale, Boolean.TRUE);
			return Optional.ofNullable(testataPrimaNota).map(el->this.generaScrittura(userContext, reversale, Collections.singletonList(el), true)).orElse(null);
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private TestataPrimaNota proposeTestataPrimaNotaReversale(UserContext userContext, ReversaleBulk reversale, boolean bloccoVincoli) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException {
		try {
			if (bloccoVincoli &&
					((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findMandati(userContext, reversale, false).stream().findFirst().isPresent())
				throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non prevista in quanto la reversale risulta vincolata ad un mandato di pagamento.");

			if (reversale.isReversaleRegolarizzazione())
				throw new ScritturaPartitaDoppiaNotRequiredException("Reversale " + reversale.getEsercizio() + "/" + reversale.getCd_cds() + "/" + reversale.getPg_reversale() +
						" di regolarizzazione. Registrazione economica non prevista.");

			//Se le righe della reversale non sono valorizzate le riempio io
			if (!Optional.ofNullable(reversale.getReversale_rigaColl()).filter(el->!el.isEmpty()).isPresent()) {
				reversale.setReversale_rigaColl(new BulkList<>(((ReversaleHome) getHome(
						userContext, reversale.getClass())).findReversale_riga(userContext, reversale, false)));
				reversale.getReversale_rigaColl().forEach(el->{
					try {
						el.setReversale(reversale);
						((Reversale_rigaHome)getHome(userContext, Reversale_rigaBulk.class)).initializeElemento_voce(userContext, el);
					} catch (ComponentException|PersistencyException e) {
						throw new ApplicationRuntimeException(e);
					}
				});
			}

			if (!Optional.ofNullable(reversale.getReversale_terzo()).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).isPresent())
				reversale.setReversale_terzo(((ReversaleHome) getHome(userContext, reversale.getClass())).findReversale_terzo(userContext, reversale, false));

			//Il documento deve essere annullato o esitato altrimenti esce
			if (reversale.isAnnullato())
				return this.proposeTestataPrimaNotaManRevAnnullato(userContext, reversale);
			else if (reversale.isIncassato()) {
				if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isChiusuraFondo()))
					return this.proposeTestataPrimaNotaReversaleChiusuraFondo(userContext, reversale);
				if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoRecuperoCrediti()))
					return this.proposeTestataPrimaNotaReversaleCompenso(userContext, reversale);
				if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm)
						.anyMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoCoriAccantonamentoEntrata()))
					return this.proposeTestataPrimaNotaReversaleAccantonamentoCori(userContext, reversale);

				//La reversale vincolata non deve generare scritture patrimoniali in quanto rilevate in fase di pagamento mandato
				//Il documento deve essere annullato o esitato altrimenti esce
				TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

				reversale.getReversale_rigaColl().forEach(rigaReversale -> {
					try {
						addDettagliPrimaNotaReversaleDocumentiVari(userContext, testataPrimaNota, rigaReversale);
					} catch (ComponentException | PersistencyException | RemoteException e) {
						throw new ApplicationRuntimeException(e);
					}
				});
				return testataPrimaNota;
			}
			throw new ScritturaPartitaDoppiaNotEnabledException("Scrittura Economica non prevista in quanto la reversale non risulta incassata.");
		} catch (PersistencyException|RemoteException e) {
			throw handleException(e);
		}
	}

	private TestataPrimaNota proposeTestataPrimaNotaReversaleChiusuraFondo(UserContext userContext, ReversaleBulk reversale) throws ComponentException {
		if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm)
				.noneMatch(el->TipoDocumentoEnum.fromValue(el).isChiusuraFondo()))
			throw new ApplicationException("La reversale " + reversale.getEsercizio() + "/" + reversale.getCds() + "/" + reversale.getPg_reversale() +
					" non risulta essere a fronte di una chiusura fondo economale. Proposta di prima nota non possibile.");

		if (reversale.getReversale_rigaColl().isEmpty() || reversale.getReversale_rigaColl().size()>1)
			throw new ApplicationException("La reversale " + reversale.getEsercizio() + "/" + reversale.getCds() + "/" + reversale.getPg_reversale() +
					" ha un numero di righe non coerente con l'unica prevista per una reversale di chiusura fondo economale. Proposta di prima nota non possibile.");

		try {
			TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

			Fondo_economaleBulk fondoEconomaleBulk = ((Fondo_economaleHome)getHome(userContext, Fondo_economaleBulk.class)).findFondoByReversale(reversale);

			if (!Optional.ofNullable(fondoEconomaleBulk).isPresent())
				throw new ApplicationException("La reversale " + reversale.getEsercizio() + "/" + reversale.getCds() + "/" + reversale.getPg_reversale() +
						" non risulta associata a nessun fondo economale. Proposta di prima nota non possibile.");

			//recupero le righe del mandato principale di apertura fondo economale per risalire al documento generico
			Collection<Mandato_rigaBulk> righeMandato = ((MandatoHome) getHome(userContext, MandatoIBulk.class)).findMandato_riga(userContext,
					new MandatoIBulk(fondoEconomaleBulk.getCd_cds(), fondoEconomaleBulk.getEsercizio(), fondoEconomaleBulk.getPg_mandato()));

			if (righeMandato.size() !=1)
				throw new ApplicationException("Il mandato " + fondoEconomaleBulk.getEsercizio() + "/" + fondoEconomaleBulk.getCd_cds() + "/" + fondoEconomaleBulk.getPg_mandato() +
						" risulta avere un numero di righe non coerente per un mandato di apertura fondo economale. Proposta di prima nota non possibile.");

			Mandato_rigaBulk rigaMandato = righeMandato.stream().findAny().orElse(null);

			Documento_genericoHome home = (Documento_genericoHome) getHome(userContext, Documento_genericoBulk.class);
			Documento_genericoBulk docamm = (Documento_genericoBulk)home.findByPrimaryKey(new Documento_genericoBulk(rigaMandato.getCd_cds_doc_amm(), rigaMandato.getCd_tipo_documento_amm(),
					rigaMandato.getCd_uo_doc_amm(), rigaMandato.getEsercizio_doc_amm(), rigaMandato.getPg_doc_amm()));

			List<IDocumentoAmministrativoRigaBulk> righeDocamm = this.getRigheDocamm(userContext, docamm);
			if (righeDocamm.size() !=1)
				throw new ApplicationException("Il documento generico " + docamm.getEsercizio() + "/" + docamm.getCd_cds() + "/" + docamm.getPg_documento_generico() +
						" risulta avere un numero di righe non coerente per un documento generico di apertura fondo economale. Proposta di prima nota non possibile.");

			Integer cdTerzo = righeDocamm.get(0).getCd_terzo();

			//2. chiudere il conto credito per anticipo
			List<Movimento_cogeBulk> allMovimentiPrimaNota = this.findMovimentiPrimaNota(userContext,docamm);
			Voce_epBulk contoPatrimonialeAperturaCredito = this.findMovimentoAperturaCreditoEconomo(allMovimentiPrimaNota, docamm, cdTerzo).getConto();
			Voce_epBulk voceEpBanca = this.findContoBanca(userContext, reversale.getReversale_rigaColl().get(0));

			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, contoPatrimonialeAperturaCredito, reversale.getIm_reversale(), cdTerzo, docamm);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_DARE, voceEpBanca, reversale.getIm_reversale());

			return testataPrimaNota;
		} catch (PersistencyException|RemoteException e) {
			throw handleException(e);
		}
	}

	private TestataPrimaNota proposeTestataPrimaNotaReversaleCompenso(UserContext userContext, ReversaleBulk reversale) throws ComponentException, PersistencyException, RemoteException, ScritturaPartitaDoppiaNotRequiredException {
		String descReversale = reversale.getEsercizio() + "/" + reversale.getCd_cds() + "/" + reversale.getPg_reversale();
		String descEstesaReversale = "reversale " + descReversale + " di tipo recupero crediti da conguaglio";

		if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm)
				.noneMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoRecuperoCrediti()))
			throw new ApplicationException("La reversale "+descReversale+" non risulta pagare un documento generico di recupero crediti." +
					" Proposta di prima nota non possibile.");
		if (reversale.getReversale_rigaColl().isEmpty() || reversale.getReversale_rigaColl().size() > 1)
			throw new ApplicationException("La reversale "+descReversale+
					" ha un numero di righe non coerente con l'unica prevista per una reversale di tipo recupero crediti da conguaglio." +
					" Proposta di prima nota non possibile.");

		Reversale_rigaBulk rigaReversale = reversale.getReversale_rigaColl().get(0);

		//Una reversale di tipo recupero crediti da conguaglio non deve essere vincolata finanziariamente ad altri oggetti. Gli unici vincoli devono essere quelli da compenso di conguaglio
		if (((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findMandati(userContext, reversale, false).stream().findFirst().isPresent())
			throw new ApplicationException("Scrittura Economica non prevista in quanto la " +descEstesaReversale+" risulta vincolata ad un mandato di pagamento.");

		//Devo recuperare il compenso legato alla reversale
		Collection<V_doc_cont_compBulk> listVDocContCompBulkByReversale = ((V_doc_cont_compHome)getHome(userContext, V_doc_cont_compBulk.class)).findByDocumento(reversale.getEsercizio(), reversale.getCd_cds(), reversale.getPg_reversale(), V_doc_cont_compBulk.TIPO_DOC_CONT_REVERSALE);

		if (listVDocContCompBulkByReversale.size() != 1)
			throw new ApplicationException("Errore nell'individuazione del compenso associato alla "+descEstesaReversale+ ": troppi compensi associati (V_DOC_CONT_COMP).");

		V_doc_cont_compBulk vDocContCompBulk = listVDocContCompBulkByReversale.stream().findAny().get();
		CompensoBulk compenso = (CompensoBulk) getHome(userContext, CompensoBulk.class)
				.findByPrimaryKey(new CompensoBulk(vDocContCompBulk.getCd_cds_compenso(), vDocContCompBulk.getCd_uo_compenso(), vDocContCompBulk.getEsercizio_compenso(),
						vDocContCompBulk.getPg_compenso()));

		String descCompenso = compenso.getEsercizio() + "/" + compenso.getCd_cds() + "/" + compenso.getPg_compenso();

		if (!compenso.getFl_compenso_conguaglio())
			throw new ApplicationException("Errore nell'individuazione del compenso associato alla "+descEstesaReversale+ ": il compenso "+descCompenso+" associato risulta non essere " +
					"di tipo conguaglio.");

		//Devo recuperare il compenso legato alla reversale
		Collection<V_doc_cont_compBulk> listVDocContCompBulkByCompenso = ((V_doc_cont_compHome)getHome(userContext, V_doc_cont_compBulk.class)).loadAllDocCont(compenso);

		List<V_doc_cont_compBulk> listDocumentiPrincipali = listVDocContCompBulkByCompenso.stream().filter(V_doc_cont_compBulk::isDocumentoPrincipale).collect(Collectors.toList());
		List<ReversaleBulk> listReversali = listVDocContCompBulkByCompenso.stream().filter(V_doc_cont_compBulk::isTipoDocReversale).map(V_doc_cont_compBulk::getManRev)
				.map(ReversaleBulk.class::cast).collect(Collectors.toList());

		if (listDocumentiPrincipali.size() > 1)
			throw new ApplicationException("Errore nell'individuazione del compenso associato alla "+descEstesaReversale+ ": la reversale risulta collegata al compenso "+descCompenso+
					" che risulta avere troppe documenti collegati definiti come principali (V_DOC_CONT_COMP).");

		if (!vDocContCompBulk.isDocumentoPrincipale()) {
			V_doc_cont_compBulk vDocContCompBulkPrincipale = listDocumentiPrincipali.stream().findAny().get();
			throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non necessaria per la "+descEstesaReversale+": la scrittura viene effettuata sulla reversale principale "
					+vDocContCompBulkPrincipale.getEsercizio_doc_cont()+"/"+vDocContCompBulkPrincipale.getCd_cds_doc_cont()+"/"+vDocContCompBulkPrincipale.getPg_doc_cont()
					+" del compenso "+descCompenso+".");
		}

		//A questo punto so che il documento in essere è il principale.....e quindi procedo a gestire l'intero compenso di conguaglio su di esso
		List<Contributo_ritenutaBulk> righeCori = Optional.ofNullable(compenso.getChildren()).orElseGet(() -> {
			try {
				Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
				return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compenso);
			} catch (ComponentException | PersistencyException e) {
				throw new DetailedRuntimeException(e);
			}
		});

		// Effettuo il controllo che le ritenute associate al compenso originario coincida con le ritenute della riga associata al mandato
		// Se la tipologia di contributo ritenuta è IVA o RIVALSA non calcolo le ritenute perchè vanno direttamente al fornitore
		{
			BigDecimal imRitenuteFittizieSoloCompenso = righeCori.stream()
					.filter(el -> el.getCompenso().equalsByPrimaryKey(compenso))
					.filter(Contributo_ritenutaBulk::isContributoEnte)
					.filter(el -> el.isTipoContributoRivalsa() || el.isTipoContributoIva())
					.map(Contributo_ritenutaBulk::getAmmontare)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal imRitenutePositiveSoloCompenso = righeCori.stream()
					.filter(el -> el.getCompenso().equalsByPrimaryKey(compenso))
					.map(Contributo_ritenutaBulk::getAmmontare)
					.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			//Aggiungo eventuali quote da trattenere indicate sul compenso
			imRitenutePositiveSoloCompenso = imRitenutePositiveSoloCompenso.add(Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO));

			BigDecimal totReversali = listReversali.stream().map(ReversaleBulk::getIm_reversale).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (imRitenutePositiveSoloCompenso.subtract(imRitenuteFittizieSoloCompenso).compareTo(totReversali) != 0)
				throw new ApplicationException("L'importo delle righe ritenute del compenso "+descCompenso+" non corrisponde con la somma delle reversali associate.");
		}

		BigDecimal imRitenuteFittizie = righeCori.stream()
				.filter(Contributo_ritenutaBulk::isContributoEnte)
				.filter(el -> el.isTipoContributoRivalsa() || el.isTipoContributoIva())
				.map(Contributo_ritenutaBulk::getAmmontare)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal imRitenutePositive = righeCori.stream()
				.map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el -> el.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		//Aggiungo eventuali quote da trattenere indicate sul compenso e sul compenso conguaglio
		imRitenutePositive = imRitenutePositive.add(Optional.ofNullable(compenso.getIm_netto_da_trattenere()).orElse(BigDecimal.ZERO));

		//sottraggo le ritenute fittizie
		imRitenutePositive = imRitenutePositive.subtract(imRitenuteFittizie);

		BigDecimal imRitenuteNegative = righeCori.stream().map(Contributo_ritenutaBulk::getAmmontare)
				.filter(el->el.compareTo(BigDecimal.ZERO)<0)
				.reduce(BigDecimal.ZERO, BigDecimal::add).abs();

		//Per il calcolo del netto mandato effettuo la sottrazione tra:
		//1. il lordo mandato calcolato aggiungendo anche le righe ritenute negative che hanno sicuramente generato un mandato vincolato
		//2. le ritenute positive
		BigDecimal imTesoreria = imRitenuteNegative.subtract(imRitenutePositive);

		List<Movimento_cogeBulk> allMovimentiPrimaNotaCompenso = this.findMovimentiPrimaNota(userContext,compenso);

		//Chiudo il patrimoniale principale del compenso.... quello con la partita
		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, rigaReversale);

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();

		testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), voceEpBanca, imTesoreria);

		//Registrazione conto CONTRIBUTI-RITENUTE
		righeCori.stream().filter(el->el.getAmmontare().compareTo(BigDecimal.ZERO)!=0).forEach(cori->{
			try {
				BigDecimal imCori = cori.getAmmontare();

				// Se la tipologia di contributo ritenuta è IVA o RIVALSA non registro la ritenuta in quanto già registrata in fase di chiusura debito mandato (imNettoMandato contiene imCori)
				if (!cori.isTipoContributoRivalsa() && !(cori.isTipoContributoIva() && cori.isContributoEnte())) {
					//Per i contributi Percipiente non faccio nulla perchè le scritture si compensano tra loro
					if (cori.isContributoEnte()) {
						String contoToClose = this.findMovimentoAperturaCori(allMovimentiPrimaNotaCompenso, compenso, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta()).getCd_voce_ep();
						testataPrimaNota.closeDettaglioPatrimonialeCori(userContext, compenso, contoToClose, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
					}
					if (cori.isTipoContributoDaVersare()) {
						Voce_epBulk contoCori = this.findContoContropartita(userContext, cori);
						testataPrimaNota.openDettaglioPatrimonialeCori(userContext, compenso, contoCori, imCori, compenso.getCd_terzo(), cori.getCd_contributo_ritenuta());
					} else {
						Voce_epBulk contoCori = this.findContoTributoCori(userContext, cori);
						testataPrimaNota.addDettaglio(userContext, compenso.getTipoDocumentoEnum().getTipoPatrimoniale(), compenso.getTipoDocumentoEnum().getSezionePatrimoniale(), contoCori, imCori);
					}
				}
			} catch (ComponentException|PersistencyException e) {
				throw new ApplicationRuntimeException(e);
			}
		});

		return testataPrimaNota;
	}

	private void addDettagliPrimaNotaMandatoDocumentiVari(UserContext userContext, TestataPrimaNota testataPrimaNota, MandatoBulk mandato, IDocumentoAmministrativoBulk docamm, Integer cdTerzoDocAmm, List<MandatoRigaComplete> mandatoRigaCompleteList) throws ComponentException, PersistencyException, RemoteException {
		final BigDecimal imLordoRigheMandato = mandatoRigaCompleteList.stream().map(MandatoRigaComplete::getMandatoRiga).map(Mandato_rigaBulk::getIm_mandato_riga).reduce(BigDecimal.ZERO, BigDecimal::add);
		final BigDecimal imRitenuteRigheMandato = mandatoRigaCompleteList.stream().map(MandatoRigaComplete::getMandatoRiga).map(Mandato_rigaBulk::getIm_ritenute_riga).reduce(BigDecimal.ZERO, BigDecimal::add);
		final BigDecimal imNettoRigheMandato = imLordoRigheMandato.subtract(imRitenuteRigheMandato);

		if (imLordoRigheMandato.compareTo(BigDecimal.ZERO)==0 && imRitenuteRigheMandato.compareTo(BigDecimal.ZERO)==0)
			return;

		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, mandato.getMandato_rigaColl().get(0));

		final String cdCoriIvaSplit = this.findCodiceTributoIvaSplit(userContext);

		Optional<Scrittura_partita_doppiaBulk> scritturaMandato = Optional.ofNullable(
				Optional.ofNullable(mandato.getScrittura_partita_doppia())
						.filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED && !el.isToBeCreated())
						.orElseGet(()-> {
							try {
								Scrittura_partita_doppiaHome home = (Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class);
								return home.getScrittura(userContext, mandato).orElse(null);
							} catch (ComponentException e) {
								throw new DetailedRuntimeException(e);
							}
						}));

		//recupero tutti i movimenti della partita per ottenere il saldo al netto della scrittura del mandato se già esiste
		Map<String, Pair<String, BigDecimal>> saldiPartita = this.getSaldiMovimentiPartita(userContext, docamm, cdTerzoDocAmm, scritturaMandato);

		Map<String, Pair<String, Pair<BigDecimal,BigDecimal>>> contiPatrimonialiDaChiudere = new HashMap<>();

		if (saldiPartita.isEmpty())
			throw new ApplicationException("Errore nell'individuazione del conto patrimoniale da utilizzare per la scrittura di chiusura del debito " +
					"del documento associato " + docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() + ": non risulta" +
					" movimentato nessun conto patrimoniale.");
		else if (saldiPartita.size() == 1) {
			String cdVocePatrimoniale = saldiPartita.keySet().stream().findAny().orElse(null);
			BigDecimal imponibile = imLordoRigheMandato.subtract(imRitenuteRigheMandato);
			contiPatrimonialiDaChiudere.put(cdVocePatrimoniale, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, Pair.of(imponibile, imRitenuteRigheMandato)));
		} else {
			//Analizzo prima i dettagli non da ordini
			List<DettaglioFinanziario> list = new ArrayList<>();
			mandatoRigaCompleteList.stream()
					.filter(el->!(el.getDocamm() instanceof Fattura_passivaBulk && ((Fattura_passivaBulk)docamm).isDaOrdini()))
					.forEach(el -> el.docammRighe.forEach(docammRiga-> list.add(new DettaglioFinanziario(docammRiga, cdTerzoDocAmm, docammRiga.getVoce_ep()))));

			//e poi analizzo i dettagli da ordini
			mandatoRigaCompleteList.stream()
					.filter(el->el.getDocamm() instanceof Fattura_passivaBulk && ((Fattura_passivaBulk)docamm).isDaOrdini())
					.forEach(el -> {
						try {
							List<FatturaOrdineBulk> listaFatturaOrdini = Utility.createFatturaPassivaComponentSession().findFatturaOrdini(userContext, (Fattura_passivaBulk) el.getDocamm());
							List<Fattura_passiva_rigaBulk> listaFattureCalcolate = new ArrayList<>();
							for (FatturaOrdineBulk fattordine : listaFatturaOrdini) {
								el.getDocammRighe().stream().filter(Fattura_passiva_rigaBulk.class::isInstance).map(Fattura_passiva_rigaBulk.class::cast)
										.filter(riga->riga.equalsByPrimaryKey(fattordine.getFatturaPassivaRiga())).findAny().ifPresent(rigaFattura -> {
											if (listaFattureCalcolate.stream().anyMatch(rigaFatturaCalcolata->rigaFatturaCalcolata.equalsByPrimaryKey(rigaFattura)))
												throw new ApplicationRuntimeException("La riga " + rigaFattura.getProgressivo_riga() + " della fattura " +
														docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc()+
														" associata al mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
														" risulta essere collegata a più righe di ordine. Non è possibile effettuare la scrittura prima nota.");

											listaFattureCalcolate.add(fattordine.getFatturaPassivaRiga());
											fattordine.setOrdineAcqConsegna((OrdineAcqConsegnaBulk) loadObject(userContext, fattordine.getOrdineAcqConsegna()));
											list.add(new DettaglioFinanziario(docamm, cdTerzoDocAmm, fattordine.getOrdineAcqConsegna().getContoBulk(), rigaFattura.getIm_imponibile(), rigaFattura.getIm_iva()));
										});
							}
							//Faccio il controllo per le righe di una fattura da ordine ma con una riga non associata
							el.getDocammRighe().stream().filter(Fattura_passiva_rigaBulk.class::isInstance).map(Fattura_passiva_rigaBulk.class::cast)
									.forEach(riga->{
										if (listaFatturaOrdini.stream().noneMatch(fatord-> fatord.getFatturaPassivaRiga().equalsByPrimaryKey(riga))) {
											BigDecimal imponibile = riga.getIm_imponibile();
											BigDecimal imposta = riga.getIm_iva();
											if (imponibile.compareTo(BigDecimal.ZERO)!=0) {
												List<DettaglioAnalitico> dettagliAnalitici = new ArrayList<>();
												//carico i dettagli analitici recuperandoli dalle tabelle economiche associate alla riga del documento pagato
												for (IDocumentoDetailAnaCogeBulk rigaAna:riga.getRigheEconomica())
													dettagliAnalitici.add(new DettaglioAnalitico(rigaAna));
												list.add(new DettaglioFinanziario(docamm, cdTerzoDocAmm, riga.getVoce_ep(), imponibile, imposta, dettagliAnalitici));
											}
										}
									});
						} catch (ComponentException | PersistencyException | IntrospectionException | RemoteException e) {
							throw new ApplicationRuntimeException(e);
						}
					});

			list.forEach(rigaDettFin->{
				try {
					Pair<Voce_epBulk, Voce_epBulk> pairContoCosto = this.findPairCosto(userContext, rigaDettFin);
					BigDecimal imponibile = rigaDettFin.getImImponibile();
					BigDecimal imposta = rigaDettFin.getImImposta();
					if (Optional.ofNullable(contiPatrimonialiDaChiudere.get(pairContoCosto.getSecond().getCd_voce_ep())).isPresent()) {
						imponibile = imponibile.add(contiPatrimonialiDaChiudere.get(pairContoCosto.getSecond().getCd_voce_ep()).getSecond().getFirst());
						imposta = imposta.add(contiPatrimonialiDaChiudere.get(pairContoCosto.getSecond().getCd_voce_ep()).getSecond().getSecond());
					}
					contiPatrimonialiDaChiudere.put(pairContoCosto.getSecond().getCd_voce_ep(), Pair.of(Movimento_cogeBulk.SEZIONE_DARE, Pair.of(imponibile,imposta)));
				} catch (ComponentException | PersistencyException | RemoteException e) {
					throw new ApplicationRuntimeException(e);
				}
			});
		}

		//Trovo il saldo partita
		if (imNettoRigheMandato.compareTo(BigDecimal.ZERO)>0)
			for (String cdVocePatrimoniale:contiPatrimonialiDaChiudere.keySet()) {
				if (saldiPartita.get(cdVocePatrimoniale)==null ||
						saldiPartita.get(cdVocePatrimoniale).getFirst().equals(Movimento_cogeBulk.SEZIONE_DARE)) {
					BigDecimal saldoNota = BigDecimal.ZERO;
					List<Movimento_cogeBulk> movimentiPartita = ((Movimento_cogeHome) getHome(userContext, Movimento_cogeBulk.class)).getMovimentiPartita(docamm, cdTerzoDocAmm, scritturaMandato);
					for (Movimento_cogeBulk movimentoCogeBulk : movimentiPartita) {
						Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) this.loadObject(userContext, movimentoCogeBulk.getScrittura());
						if ("FATTURA_P".equals(scrittura.getCd_tipo_documento()) && movimentoCogeBulk.isSezioneDare() && movimentoCogeBulk.getCd_voce_ep().equals(cdVocePatrimoniale))
							saldoNota = saldoNota.add(movimentoCogeBulk.getIm_movimento());
					}
					if (saldoNota.compareTo(BigDecimal.ZERO)==0)
						throw new ApplicationRuntimeException("Il conto debito " + cdVocePatrimoniale + " verso il fornitore del documento " +
								docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
								" associato al mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
								" risulta essere nullo. Non è possibile effettuare la scrittura prima nota.");
				}
			}

		BigDecimal saldoPartita = saldiPartita.values().stream().map(Pair::getSecond).reduce(BigDecimal.ZERO, BigDecimal::add);

		//recupero tutti i movimenti cori split della partita per ottenere il saldo
		Map<String, Pair<String, BigDecimal>> saldiCoriSplit = this.getSaldiMovimentiCori(userContext, docamm, cdTerzoDocAmm, cdCoriIvaSplit, scritturaMandato);
		if (!saldiCoriSplit.isEmpty()) {
			Voce_epBulk aContoCreditoRitenuteSplit = this.findContoCreditoRitenuteSplitPayment(userContext, docamm.getEsercizio());

			//Ricerco il conto patrimoniale della ritenuta split della partita recuperato da CONFIGURAZIONE_CNR
			Optional<String> optCdVoceCoriSplit = saldiCoriSplit.keySet().stream().filter(el -> el.equals(aContoCreditoRitenuteSplit.getCd_voce_ep())).findAny();

			if (optCdVoceCoriSplit.isPresent()) {
				String sezioneCoriSplit = saldiCoriSplit.get(optCdVoceCoriSplit.get()).getFirst();
				BigDecimal saldoCoriSplit = saldiCoriSplit.get(optCdVoceCoriSplit.get()).getSecond();

				//Effettuo il controllo di non sfondamento
				if (sezioneCoriSplit.equals(Movimento_cogeBulk.SEZIONE_AVERE) && saldoCoriSplit.compareTo(BigDecimal.ZERO) >= 0)
					throw new ApplicationException("Il credito split payment verso il fornitore del documento " +
							docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
							" associato al mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
							" risulta essere nullo. Non è possibile effettuare la scrittura prima nota.");

				if (sezioneCoriSplit.equals(Movimento_cogeBulk.SEZIONE_DARE) && saldoCoriSplit.compareTo(imRitenuteRigheMandato) < 0)
					throw new ApplicationException("Il credito split payment verso il fornitore del documento " +
							docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() +
							" associato al mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
							" risulta essere inferiore (" + new EuroFormat().format(saldoCoriSplit) +
							" ) rispetto alle ritenute presenti sul mandato (" + new EuroFormat().format(imRitenuteRigheMandato) + ").");

				//Trovo il saldo cori
				saldoPartita = saldoPartita.subtract(saldoCoriSplit);

				//Compenso il credito split con il debito verso fornitore
				contiPatrimonialiDaChiudere.keySet().forEach(cdVocePatrimoniale->{
					BigDecimal impostaConto = contiPatrimonialiDaChiudere.get(cdVocePatrimoniale).getSecond().getSecond();
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, cdVocePatrimoniale, impostaConto, cdTerzoDocAmm, docamm);
					testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.CREDITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, optCdVoceCoriSplit.get(), impostaConto, cdTerzoDocAmm, docamm, cdCoriIvaSplit);
				});
			}
		}

		//Effettuo il controllo di non sfondamento
		if (imNettoRigheMandato.compareTo(saldoPartita)>0) {
			BigDecimal saldoNota = BigDecimal.ZERO;
			List<Movimento_cogeBulk> movimentiPartita =  ((Movimento_cogeHome) getHome(userContext, Movimento_cogeBulk.class)).getMovimentiPartita(docamm, cdTerzoDocAmm, scritturaMandato);
			for (Movimento_cogeBulk movimentoCogeBulk:movimentiPartita) {
				Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk)this.loadObject(userContext, movimentoCogeBulk.getScrittura());
				if ("FATTURA_P".equals(scrittura.getCd_tipo_documento()) && movimentoCogeBulk.isSezioneDare())
					saldoNota = saldoNota.add(movimentoCogeBulk.getIm_movimento());
			}
			if (imNettoRigheMandato.compareTo(saldoPartita.add(saldoNota))>0)
				throw new ApplicationException("L'importo netto (" + new EuroFormat().format(imNettoRigheMandato) +
						") delle righe del mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato() +
						" è maggiore dal saldo totale fornitore (" + new EuroFormat().format(saldoPartita) + ") del documento associato " +
						docamm.getCd_tipo_doc() + "/" + docamm.getEsercizio() + "/" + docamm.getCd_uo() + "/" + docamm.getPg_doc() + ".");
		}
		contiPatrimonialiDaChiudere.keySet().forEach(cdVocePatrimoniale->{
			BigDecimal imponibileConto = contiPatrimonialiDaChiudere.get(cdVocePatrimoniale).getSecond().getFirst();
			BigDecimal impostaConto = contiPatrimonialiDaChiudere.get(cdVocePatrimoniale).getSecond().getSecond();
			BigDecimal importoLordoConto = imponibileConto.add(impostaConto);
			BigDecimal importoNettoConto = importoLordoConto.subtract(impostaConto);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_DARE, cdVocePatrimoniale, importoNettoConto, cdTerzoDocAmm, docamm);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_AVERE, voceEpBanca, importoNettoConto);
		});
	}

	private void addDettagliPrimaNotaReversaleDocumentiVari(UserContext userContext, TestataPrimaNota testataPrimaNota, Reversale_rigaBulk rigaReversale) throws ComponentException, PersistencyException, RemoteException {
		BigDecimal imReversale = rigaReversale.getIm_reversale_riga();

		try {
			if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaPassiva())
				((Reversale_rigaHome) getHome(userContext, rigaReversale.getClass())).initializeTi_fatturaPerFattura(rigaReversale, "FATTURA_PASSIVA");
			else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaAttiva())
				((Reversale_rigaHome) getHome(userContext, rigaReversale.getClass())).initializeTi_fatturaPerFattura(rigaReversale, "FATTURA_ATTIVA");
		} catch (SQLException ex) {
			throw new ApplicationException(ex);
		}

		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, rigaReversale);

		//La partita non deve essere registrata in caso di versamento ritenute
		IDocumentoCogeBulk partita;
		if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isDocumentoGenericoAttivo())
			partita = (Documento_generico_attivoBulk) getHome(userContext, Documento_generico_attivoBulk.class).findByPrimaryKey(new Documento_generico_attivoBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_tipo_documento_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaAttiva() && rigaReversale.isRigaTipoFatturaAttiva())
			partita = (Fattura_attiva_IBulk) getHome(userContext, Fattura_attiva_IBulk.class).findByPrimaryKey(new Fattura_attiva_IBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaAttiva() && rigaReversale.isRigaTipoNotaCredito())
			partita = (Nota_di_credito_attivaBulk) getHome(userContext, Nota_di_credito_attivaBulk.class).findByPrimaryKey(new Nota_di_credito_attivaBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaAttiva() && rigaReversale.isRigaTipoNotaDebito())
			partita = (Nota_di_debito_attivaBulk) getHome(userContext, Nota_di_debito_attivaBulk.class).findByPrimaryKey(new Nota_di_debito_attivaBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isFatturaPassiva() && rigaReversale.isRigaTipoNotaCredito())
			partita = (Nota_di_creditoBulk) getHome(userContext, Nota_di_creditoBulk.class).findByPrimaryKey(new Nota_di_creditoBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else if (TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()).isRimborso())
			partita = (RimborsoBulk) getHome(userContext, RimborsoBulk.class).findByPrimaryKey(new RimborsoBulk(rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm()));
		else
			partita = new Partita(rigaReversale.getCd_tipo_documento_amm(), rigaReversale.getCd_cds_doc_amm(), rigaReversale.getCd_uo_doc_amm(), rigaReversale.getEsercizio_doc_amm(), rigaReversale.getPg_doc_amm(),
					rigaReversale.getCd_terzo(), TipoDocumentoEnum.fromValue(rigaReversale.getCd_tipo_documento_amm()));

		List<Movimento_cogeBulk> movimenti = this.findMovimentiPrimaNota(userContext, partita);
		List<Movimento_cogeBulk> dettPnPatrimonialePartita = this.findMovimentiPatrimoniali(movimenti,partita);

		final BigDecimal totContiEp = BigDecimal.valueOf(dettPnPatrimonialePartita.stream()
				.mapToDouble(el->el.getIm_movimento().doubleValue())
				.sum());

		BigDecimal imDaRipartire = rigaReversale.isRigaTipoNotaCredito()?imReversale.abs():imReversale;
		//Chiudo i conti in percentuale
		for (Iterator<Movimento_cogeBulk> i = dettPnPatrimonialePartita.iterator(); i.hasNext(); ) {
			Movimento_cogeBulk dettPN=i.next();
			BigDecimal imRiga = imReversale.multiply(dettPN.getIm_movimento()).divide(totContiEp,2, RoundingMode.HALF_UP);
			if (imRiga.compareTo(imDaRipartire)>0 || !i.hasNext()) {
				testataPrimaNota.addDettaglio(userContext, partita.getTipoDocumentoEnum().getTipoPatrimoniale(), Movimento_cogeBulk.getControSezione(partita.getTipoDocumentoEnum().getSezionePatrimoniale()),
						dettPN.getConto(), imDaRipartire, rigaReversale.getCd_terzo(), partita);
				testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), partita.getTipoDocumentoEnum().getSezionePatrimoniale(), voceEpBanca, imDaRipartire);
				break;
			}
			testataPrimaNota.addDettaglio(userContext, partita.getTipoDocumentoEnum().getTipoPatrimoniale(), Movimento_cogeBulk.getControSezione(partita.getTipoDocumentoEnum().getSezionePatrimoniale()),
					dettPN.getConto(), imRiga, rigaReversale.getCd_terzo(), partita);
			testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), partita.getTipoDocumentoEnum().getSezionePatrimoniale(), voceEpBanca, imRiga);
			imDaRipartire = imDaRipartire.subtract(imRiga);
		}
	}

	private Ass_ev_voceepBulk findAssEvVoceep(UserContext userContext, Elemento_voceBulk voceBilancio) throws ComponentException, PersistencyException {
		Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHome(userContext, Ass_ev_voceepBulk.class);
		List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(voceBilancio);
		if (listAss.isEmpty())
			throw new ApplicationException("Associazione tra voce del piano finanziario " + voceBilancio.getEsercizio() + "/" +
					voceBilancio.getTi_appartenenza() + "/" +
					voceBilancio.getTi_gestione() + "/" +
					voceBilancio.getCd_elemento_voce() + " ed economica non trovata.");
		else if (listAss.size() > 1)
			throw new ApplicationException("Troppi conti economici risultano associati alla voce " + voceBilancio.getEsercizio() + "/" +
					voceBilancio.getTi_appartenenza() + "/" +
					voceBilancio.getTi_gestione() + "/" +
					voceBilancio.getCd_elemento_voce() + ".");

		return listAss.get(0);
	}

	private Voce_epBulk findContoIva(UserContext userContext, AutofatturaBulk autofatturaBulk) throws ComponentException, RemoteException, PersistencyException {
		SezionaleBulk sezionale = ((SezionaleHome)getHome(userContext, SezionaleBulk.class)).getSezionaleByTipoDocumento(autofatturaBulk);

		return Optional.ofNullable(sezionale).flatMap(el->Optional.ofNullable(el.getContoIva())).filter(el->el.getCd_voce_ep()!=null).flatMap(el->{
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				return Optional.ofNullable((Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(el.getCd_voce_ep(), el.getEsercizio())));
			} catch(ComponentException|PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}).orElseGet(()->{
			try {
				return this.findContoIvaDebito(userContext, autofatturaBulk.getEsercizio());
			} catch(ComponentException|RemoteException ex) {
				throw new DetailedRuntimeException(ex);
			}
		});
	}

	private Voce_epBulk findContoIva(UserContext userContext, IDocumentoAmministrativoBulk docamm) throws ComponentException, RemoteException, PersistencyException {
		SezionaleBulk sezionale = ((SezionaleHome)getHome(userContext, SezionaleBulk.class)).getSezionaleByTipoDocumento(docamm);

		return Optional.ofNullable(sezionale).flatMap(el->Optional.ofNullable(el.getContoIva())).filter(el->el.getCd_voce_ep()!=null).flatMap(el->{
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				return Optional.ofNullable((Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(el.getCd_voce_ep(), el.getEsercizio())));
			} catch(ComponentException|PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}).orElseGet(()->{
			try {
				if (docamm.getTipoDocumentoEnum().isDocumentoAmministrativoPassivo())
					return this.findContoIvaCredito(userContext, docamm.getEsercizio());
				else
					return this.findContoIvaDebito(userContext, docamm.getEsercizio());
			} catch(ComponentException|RemoteException ex) {
				throw new DetailedRuntimeException(ex);
			}
		});
	}

	private Voce_epBulk findContoIvaSplit(UserContext userContext, IDocumentoAmministrativoBulk docamm) throws ComponentException, RemoteException, PersistencyException {
		SezionaleBulk sezionale = ((SezionaleHome)getHome(userContext, SezionaleBulk.class)).getSezionaleByTipoDocumento(docamm);

		return Optional.ofNullable(sezionale).flatMap(el->Optional.ofNullable(el.getContoIvaSplit())).filter(el->el.getCd_voce_ep()!=null).flatMap(el->{
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				return Optional.ofNullable((Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(el.getCd_voce_ep(), el.getEsercizio())));
			} catch(ComponentException|PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}).orElseGet(()->{
			try {
				if (docamm.getTipoDocumentoEnum().isDocumentoAmministrativoPassivo())
					return this.findContoIvaSplitDebito(userContext, docamm.getEsercizio());
				else
					return this.findContoIvaSplitCredito(userContext, docamm.getEsercizio());
			} catch(ComponentException|RemoteException ex) {
				throw new DetailedRuntimeException(ex);
			}
		});
	}

	private Voce_epBulk findContoQuotaDaTrattenereByConfig(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_ELEMENTO_VOCE_SPECIALE, Configurazione_cnrBulk.SK_NETTO_DA_TRATTENERE, 2);
	}

	private Voce_epBulk findContoCostoDocumentoNonLiquidabileByConfig(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_COSTO_DOC_NON_LIQUIDABILE, 1);
	}

	private Voce_epBulk findContoCostoLiquidazioneIvaEsterna(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CONTI_LIQUIDAZIONE_ESTERNA_IVA, 1);
	}

	private Voce_epBulk findContoDebitoUoLiquidazioneIvaEsterna(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CONTI_LIQUIDAZIONE_ESTERNA_IVA, 2);
	}

	private Voce_epBulk findContoErarioContoIva(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_ERARIO_C_IVA, 1);
	}

	private Voce_epBulk findContoIvaCredito(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_IVA_CREDITO, 1);
	}

	private Voce_epBulk findContoIvaSplitCredito(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_IVA_CREDITO, 2);
	}

	private Voce_epBulk findContoIvaDebito(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_IVA_DEBITO, 1);
	}

	private Voce_epBulk findContoIvaSplitDebito(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_IVA_DEBITO, 2);
	}

	private Voce_epBulk findContoCreditoRitenuteSplitPayment(UserContext userContext, int esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CREDITO_RITENUTE_SPLIT_PAYMENT, 1);
	}

	private Voce_epBulk findContoCostoIvaNonDetraibile(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_COSTO_IVA_NON_DETRAIBILE, 1);
	}

	private Voce_epBulk findContoUtileCambi(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITE_CAMBI, 1);
	}

	private Voce_epBulk findContoPerditeCambi(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITE_CAMBI, 2);
	}

	private Voce_epBulk findContoCommissioniBancarie(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_COMMISSIONI_BANCARIE, 1);
	}

	private Voce_epBulk findContoFattureDaRicevere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_RICEVERE, 1);
	}

	private Voce_epBulk findContoNotaCreditoDaRicevere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_RICEVERE, 2);
	}

	private Voce_epBulk findContoDocumentoGenericoDaRicevere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_RICEVERE, 3);
	}

	private Voce_epBulk findContoFattureDaEmettere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_EMETTERE, 1);
	}

	private Voce_epBulk findContoNotaCreditoDaEmettere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_EMETTERE, 2);
	}

	private Voce_epBulk findContoDocumentoGenericoDaEmettere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_EMETTERE, 3);
	}

	private Voce_epBulk findContoRateiPassivi(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_RATEI_PASSIVI, 1);
	}

	private Voce_epBulk findContoRateiAttivi(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_RATEI_ATTIVI, 1);
	}

	private Voce_epBulk findContoSopravvenienzeAttive(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_SOPRAVVENIENZE_ATTIVE, 1);
	}

	private Voce_epBulk findContoSopravvenienzePassive(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_SOPRAVVENIENZE_PASSIVE, 1);
	}

	private String findCodiceTributoIva(UserContext userContext) throws ComponentException, RemoteException {
		return this.findValueByConfigurazioneCNR(userContext, null, Configurazione_cnrBulk.PK_CORI_SPECIALE, Configurazione_cnrBulk.SK_IVA, 1);
	}

	private String findCodiceTributoIvaSplit(UserContext userContext) throws ComponentException, RemoteException {
		return this.findValueByConfigurazioneCNR(userContext, null, Configurazione_cnrBulk.PK_CORI_SPECIALE, Configurazione_cnrBulk.SK_IVA, 3);
	}

	private Voce_epBulk findContoByConfigurazioneCNR(UserContext userContext, int esercizio, String chiavePrimaria, String chiaveSecondaria, int fieldNumber) throws ComponentException, RemoteException {
		Configurazione_cnrBulk config = Utility.createConfigurazioneCnrComponentSession().getConfigurazione(userContext, esercizio, null, chiavePrimaria, chiaveSecondaria);
		if (config==null)
			config = Utility.createConfigurazioneCnrComponentSession().getConfigurazione(userContext, CNRUserContext.getEsercizio(userContext), null, chiavePrimaria, chiaveSecondaria);

		Integer esercizioConfig = Optional.ofNullable(config).map(Configurazione_cnrKey::getEsercizio)
				.orElseThrow(()->new ApplicationException("Manca la configurazione richiesta nella tabella CONFIGURAZIONE_CNR per gli esercizi "+esercizio+" e/o "+CNRUserContext.getEsercizio(userContext)
						+" ("+chiavePrimaria+" - "+chiaveSecondaria+")."));

		String value = Optional.of(config).flatMap(el->Optional.ofNullable(el.getVal(fieldNumber)))
				.orElseThrow(()->new ApplicationException("Manca il valore richiesto nella tabella CONFIGURAZIONE_CNR per l'esercizio "+esercizio
						+" ("+chiavePrimaria+" - "+chiaveSecondaria+" - VAL0"+fieldNumber+")."));

		return Optional.of(value).map(el->{
			try {
				Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
				return (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(el, esercizioConfig));
			} catch(ComponentException|PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		}).orElseThrow(()->new ApplicationException("Attenzione! Non esiste il conto economico "+ value +" indicato nella tabella CONFIGURAZIONE_CNR per l'esercizio "+esercizioConfig
				+" ("+chiavePrimaria+"-"+chiaveSecondaria+"-VAL0"+fieldNumber+")."));
	}

	private String findValueByConfigurazioneCNR(UserContext userContext, Integer esercizio, String chiavePrimaria, String chiaveSecondaria, int fieldNumber) throws ComponentException, RemoteException {
		Configurazione_cnrBulk config = Utility.createConfigurazioneCnrComponentSession().getConfigurazione(userContext, esercizio, null, chiavePrimaria, chiaveSecondaria);

		return Optional.ofNullable(config).flatMap(el->Optional.ofNullable(el.getVal(fieldNumber)))
				.orElseThrow(()->new ApplicationException("Manca la configurazione richiesta nella tabella CONFIGURAZIONE_CNR per l'esercizio "+esercizio
						+" ("+chiavePrimaria+"-"+chiaveSecondaria+"-VAL0"+fieldNumber+")."));
	}

	private Voce_epBulk findContoCostoRicavo(UserContext userContext, Elemento_voceBulk voceBilancio) throws ComponentException, PersistencyException {
		return this.findAssEvVoceep(userContext, voceBilancio).getVoce_ep();
	}

	private Voce_epBulk findContoCosto(UserContext userContext, Tipo_contributo_ritenutaBulk tipoContributoRitenuta, int esercizio, String sezione, String tipoEntePercipiente) throws ComponentException, PersistencyException {
		Ass_tipo_cori_voce_epBulk assTipoCori = this.findAssociazioneCoriVoceEp(userContext, tipoContributoRitenuta, esercizio, sezione, tipoEntePercipiente);
		return Optional.ofNullable(assTipoCori).map(Ass_tipo_cori_voce_epBulk::getVoce_ep)
				.orElseThrow(()->new ApplicationException("Associazione tra tipo contributo/ritenuta ed economica non trovata (" +
						"Tipo Cori: " + tipoContributoRitenuta.getCd_contributo_ritenuta() +
						" - Esercizio: " + esercizio +
						" - Tipo Ente Percipiente: " + tipoEntePercipiente +
						" - Sezione: " + sezione + ")."));
	}

	private Ass_tipo_cori_voce_epBulk findAssociazioneCoriVoceEp(UserContext userContext, Tipo_contributo_ritenutaBulk tipoContributoRitenuta, int esercizio, String sezione, String tipoEntePercipiente) throws ComponentException, PersistencyException {
		Ass_tipo_cori_voce_epHome assTipoCoriVoceEpHome = (Ass_tipo_cori_voce_epHome) getHome(userContext, Ass_tipo_cori_voce_epBulk.class);
		Ass_tipo_cori_voce_epBulk assTipoCori = assTipoCoriVoceEpHome.getAssCoriEp(esercizio, tipoContributoRitenuta.getCd_contributo_ritenuta(), tipoEntePercipiente, sezione);
		return Optional.ofNullable(assTipoCori).orElseThrow(()->new ApplicationException("Associazione tra tipo contributo/ritenuta ed economica non trovata (" +
				"Tipo Cori: " + tipoContributoRitenuta.getCd_contributo_ritenuta() +
				" - Esercizio: " + esercizio +
				" - Tipo Ente Percipiente: " + tipoEntePercipiente +
				" - Sezione: " + sezione + ")."));
	}

	private Ass_tipo_cori_evBulk findAssociazioneCoriElementoVoce(UserContext userContext, Tipo_contributo_ritenutaBulk tipoContributoRitenuta, int esercizio, String tipoEntePercipiente) throws ComponentException, PersistencyException {
		Ass_tipo_cori_evHome assTipoCoriEvHome = (Ass_tipo_cori_evHome) getHome(userContext, Ass_tipo_cori_evBulk.class);
		Ass_tipo_cori_evBulk assTipoCori = assTipoCoriEvHome.getAssCoriEv(esercizio, tipoContributoRitenuta.getCd_contributo_ritenuta(), Elemento_voceHome.APPARTENENZA_CNR, Elemento_voceHome.GESTIONE_ENTRATE, tipoEntePercipiente);
		return Optional.ofNullable(assTipoCori).orElseThrow(()->new ApplicationException("Associazione tra tipo contributo/ritenuta e voce finanziaria non trovata (" +
				"Tipo Cori: " + tipoContributoRitenuta.getCd_contributo_ritenuta() +
				" - Esercizio: " + esercizio +
				" - Tipo Ente Percipiente: " + tipoEntePercipiente + ")."));
	}

	private Voce_epBulk findContoBanca(UserContext userContext, Mandato_rigaBulk mandatoRigaBulk) throws ComponentException, RemoteException {
		Modalita_pagamentoBulk modalitaPagamentoBulk = (Modalita_pagamentoBulk)this.loadObject(userContext, mandatoRigaBulk.getModalita_pagamento());
		Rif_modalita_pagamentoBulk rifModalitaPagamentoBulk = (Rif_modalita_pagamentoBulk)this.loadObject(userContext, modalitaPagamentoBulk.getRif_modalita_pagamento());
		if (rifModalitaPagamentoBulk.isModalitaBancaItalia())
			return this.findContoByConfigurazioneCNR(userContext, mandatoRigaBulk.getEsercizio(), Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_BANCA, 2);
		return this.findContoByConfigurazioneCNR(userContext, mandatoRigaBulk.getEsercizio(), Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_BANCA, 1);
	}

	private Voce_epBulk findContoBanca(UserContext userContext, Reversale_rigaBulk reversaleRigaBulk) throws ComponentException, RemoteException {
		Modalita_pagamentoBulk modalitaPagamentoBulk = (Modalita_pagamentoBulk)this.loadObject(userContext, reversaleRigaBulk.getModalita_pagamento());
		Rif_modalita_pagamentoBulk rifModalitaPagamentoBulk = (Rif_modalita_pagamentoBulk)this.loadObject(userContext, modalitaPagamentoBulk.getRif_modalita_pagamento());
		if (rifModalitaPagamentoBulk.isModalitaBancaItalia())
			return this.findContoByConfigurazioneCNR(userContext, reversaleRigaBulk.getEsercizio(), Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_BANCA, 2);
		return this.findContoByConfigurazioneCNR(userContext, reversaleRigaBulk.getEsercizio(), Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_BANCA, 1);
	}

	private Voce_epBulk findContoAccontoDocumentoAttivo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_ACCONTO_ANTICIPO_DOCATT, 1);
	}

	private Voce_epBulk findContoAnticipoDocumentoAttivo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_ACCONTO_ANTICIPO_DOCATT, 2);
	}

	private Voce_epBulk findContoCreditoAnticipo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CREDITO_DEBITO_ANTICIPO, 1);
	}

	private Voce_epBulk findContoCreditoRimborsoAnticipo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CREDITO_RIMBORSO_ANTICIPO, 1);
	}

	private Voce_epBulk findContoCreditoEconomo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CREDITO_DEBITO_ECONOMO, 1);
	}

	private Voce_epBulk findContoDebitoEconomo(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CREDITO_DEBITO_ECONOMO, 2);
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCosto(UserContext userContext, AnticipoBulk anticipo) throws ComponentException, RemoteException, PersistencyException {
		if (Optional.ofNullable(anticipo.getVoce_ep()).isPresent())
			return this.findPairCosto(userContext, Optional.ofNullable(anticipo.getTerzo()).orElse(new TerzoBulk(anticipo.getCd_terzo())), anticipo.getVoce_ep(), Movimento_cogeBulk.TipoRiga.COSTO.value());
		return null;
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCosto(UserContext userContext, MissioneBulk missione) throws ComponentException, RemoteException, PersistencyException {
		if (Optional.ofNullable(missione.getVoce_ep()).isPresent())
			return this.findPairCosto(userContext, Optional.ofNullable(missione.getTerzo()).orElse(new TerzoBulk(missione.getCd_terzo())), missione.getVoce_ep(), Movimento_cogeBulk.TipoRiga.COSTO.value());
		return null;
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCosto(UserContext userContext, CompensoBulk compenso) throws ComponentException, RemoteException, PersistencyException {
		if (Optional.ofNullable(compenso.getVoce_ep()).isPresent())
			return this.findPairCosto(userContext, Optional.ofNullable(compenso.getTerzo()).orElse(new TerzoBulk(compenso.getCd_terzo())), compenso.getVoce_ep(), Movimento_cogeBulk.TipoRiga.COSTO.value());
		return null;
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCosto(UserContext userContext, DettaglioFinanziario dettaglioFinanziario) throws ComponentException, RemoteException, PersistencyException {
		Voce_epBulk aContoCosto = (Voce_epBulk) loadObject(userContext, dettaglioFinanziario.getVoceEp());
		//Se esiste causale contabile il conto di contropartita lo recupero direttamente dalla causale
		String cdCausaleContabile = Optional.ofNullable(dettaglioFinanziario.getDocamm())
				.filter(Documento_genericoBulk.class::isInstance)
				.map(Documento_genericoBulk.class::cast)
				.flatMap(el->Optional.ofNullable(el.getCausaleContabile()))
				.flatMap(el->Optional.ofNullable(el.getCdCausale()))
				.orElse(null);
		if (cdCausaleContabile!=null) {
			String mySezione = dettaglioFinanziario.getDocamm().getTipoDocumentoEnum().getSezionePatrimoniale();
			List<AssCausaleVoceEPBulk> assCausaleVoce = new BulkList<>(find(userContext, CausaleContabileBulk.class, "findAssCausaleVoceEPBulk", userContext, cdCausaleContabile));
			AtomicReference<Voce_epBulk> myContoContropartita = new AtomicReference<>(null);
			assCausaleVoce.stream().filter(el->el.getTiSezione().equals(dettaglioFinanziario.getDocamm().isDocumentoStorno()?Movimento_cogeBulk.getControSezione(mySezione):mySezione))
					.findAny()
					.ifPresent(el-> myContoContropartita.set(el.getVoceEp()));
			Voce_epBulk aContoContropartita = (Voce_epBulk) loadObject(userContext, myContoContropartita.get());
			return Pair.of(aContoCosto, aContoContropartita);
		}
		return this.findPairCosto(userContext, new TerzoBulk(dettaglioFinanziario.getCdTerzo()), aContoCosto,
				dettaglioFinanziario.getDocamm().getTipoDocumentoEnum().getTipoPatrimoniale());
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCosto(UserContext userContext, TerzoBulk terzo, Voce_epBulk voceEp, String tipoContoPatrimoniale) throws ComponentException, RemoteException, PersistencyException {
		Voce_epBulk aContoCosto = (Voce_epBulk) loadObject(userContext, voceEp);
		Voce_epBulk aContoContropartita = this.findContoContropartita(userContext, terzo, aContoCosto, tipoContoPatrimoniale);
		return Pair.of(aContoCosto, aContoContropartita);
	}

	/**
	 * Ritorna il conto di costo ed il conto di contropartita associato al contributoRitenuta
	 *
	 * @param userContext userContext
	 * @param cori Contributo_ritenutaBulk
	 * @return Pair - first=costo - second=controcosto
	 * @throws ComponentException ComponentException
	 * @throws PersistencyException PersistencyException
	 */
	private Pair<Voce_epBulk, Voce_epBulk> findPairCostoCompenso(UserContext userContext, Contributo_ritenutaBulk cori) throws ComponentException, PersistencyException {
		Voce_epBulk voceCosto = this.findContoCosto(userContext, cori.getTipoContributoRitenuta(), cori.getEsercizio(), cori.getSezioneCostoRicavo(), cori.getTi_ente_percipiente());
		Voce_epBulk voceContropartita = this.findContoContropartita(userContext, voceCosto);
		return Pair.of(voceCosto, voceContropartita);
	}

	/**
	 * Ritorna il conto tributo legato al codice contributo indicato.
	 * <p>Viene:</p>
	 * <ul>
	 * <li>individuata la voce finanziaria legata al codice tributo;</li>
	 * <li>individuato il conto economico legato alla voce finanziaria di cui al punto precedente.</li>
	 * </ul>
	 * Esempio:
	 * <p>Input: <b>STI9001</b> - PRESTITALIA SPA</p>
	 * <p>Ritorna: <b>A91005</b> - Crediti diversi derivanti da altre ritenute al personale dipendente per conto di terzi</p>
	 */
	private Voce_epBulk findContoTributoCori(UserContext userContext, Contributo_ritenutaBulk cori) throws ComponentException, PersistencyException {
		Ass_tipo_cori_evBulk assTipoCoriEvBulk = this.findAssociazioneCoriElementoVoce(userContext, cori.getTipoContributoRitenuta(), cori.getEsercizio(), cori.getTi_ente_percipiente());
		Ass_ev_voceepBulk assEvVoceepBulk = this.findAssEvVoceep(userContext, assTipoCoriEvBulk.getElemento_voce());
		return (Voce_epBulk)this.loadObject(userContext, assEvVoceepBulk.getVoce_ep());
	}

	/**
	 * Ritorna il conto tributo legato al codice contributo indicato.
	 * <p>Viene:</p>
	 * <ul>
	 * <li>individuata la voce finanziaria legata al codice tributo;</li>
	 * <li>individuato il conto economico legato alla voce finanziaria di cui al punto precedente.</li>
	 * <li>individuato il conto di contropartita del conto economico  di cui al punto precedente.</li>
	 * </ul>
	 * Esempio:
	 * <p>Input: <b>STI9001</b> - PRESTITALIA SPA</p>
	 * <p>Ritorna: <b>P71005</b> - Altri versamenti di ritenute al personale dipendente per conto di terzi</p>
	 */
	private Voce_epBulk findContoContropartita(UserContext userContext, Contributo_ritenutaBulk cori) throws ComponentException, PersistencyException {
		Ass_tipo_cori_evBulk assTipoCoriEvBulk = this.findAssociazioneCoriElementoVoce(userContext, cori.getTipoContributoRitenuta(), cori.getEsercizio(), cori.getTi_ente_percipiente());
		Ass_ev_voceepBulk assEvVoceepBulk = this.findAssEvVoceep(userContext, assTipoCoriEvBulk.getElemento_voce());
		return findContoContropartita(userContext, assEvVoceepBulk);
	}

	private Voce_epBulk findContoContropartita(UserContext userContext, TerzoBulk terzo, Voce_epBulk voceep, String tipoConto) throws ComponentException, RemoteException, PersistencyException {
		String valueAssociazioneConti = this.findValueByConfigurazioneCNR(userContext, CNRUserContext.getEsercizio(userContext), Configurazione_cnrBulk.PK_ECONOMICO_PATRIMONIALE, Configurazione_cnrBulk.SK_ASSOCIAZIONE_CONTI, 1);

		if (valueAssociazioneConti.equals("TERZO"))
			return this.findContoAnag(userContext, terzo, tipoConto);
		else if (valueAssociazioneConti.equals("CONTO")) {
			if (Optional.ofNullable(voceep).isPresent())
				return this.findContoContropartita(userContext, voceep);
			throw new ApplicationRuntimeException("Attenzione! Non è stata indicata alcuna voce di bilancio nè conto per la ricerca della contropartita.");
		}
		throw new ApplicationRuntimeException("Attenzione! Non è gestito il valore "+valueAssociazioneConti+" indicato nella tabella CONFIGURAZIONE_CNR per l'esercizio "+CNRUserContext.getEsercizio(userContext)
				+" ("+Configurazione_cnrBulk.PK_ECONOMICO_PATRIMONIALE+"-"+Configurazione_cnrBulk.SK_ASSOCIAZIONE_CONTI+"-VAL01).");
	}

	private Voce_epBulk findContoContropartita(UserContext userContext, Ass_ev_voceepBulk assEvVoceepBulk) {
		Ass_ev_voceepBulk innerAssEvVoceepBulk = (Ass_ev_voceepBulk)this.loadObject(userContext, assEvVoceepBulk);
		if (Optional.ofNullable(innerAssEvVoceepBulk.getVoce_ep_contr()).isPresent())
			return (Voce_epBulk)this.loadObject(userContext, innerAssEvVoceepBulk.getVoce_ep_contr());
		return this.findContoContropartita(userContext, innerAssEvVoceepBulk.getVoce_ep());
	}

	private Voce_epBulk findContoContropartita(UserContext userContext, Voce_epBulk contoEconomico) {
		Voce_epBulk aContoEconomico = (Voce_epBulk)this.loadObject(userContext, contoEconomico);
		return Optional.of(aContoEconomico)
				.filter(el->Optional.ofNullable(el.getCd_voce_ep_contr()).isPresent())
				.flatMap(el->Optional.ofNullable((Voce_epBulk)this.loadObject(userContext, new Voce_epBulk(el.getCd_voce_ep_contr(), el.getEsercizio()))))
				.orElseThrow(()->new ApplicationRuntimeException("Conto di contropartita mancante in associazione con il conto economico " +
						aContoEconomico.getEsercizio() + "/" + aContoEconomico.getCd_voce_ep() + "."));
	}

	private OggettoBulk loadObject(UserContext userContext, OggettoBulk object) {
		return Optional.of(object).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
			try {
				BulkHome bulkHome = getHome(userContext, object.getClass());
				return (OggettoBulk)bulkHome.findByPrimaryKey(object);
			} catch (ComponentException | PersistencyException ex) {
				throw new DetailedRuntimeException(ex);
			}
		});
	}

	private Voce_epBulk findContoAnag(UserContext userContext, TerzoBulk terzo, String tipoConto) throws ComponentException, PersistencyException {
		Voce_epBulk voceEpBulk = null;

		if (Optional.ofNullable(terzo).isPresent()) {
			TerzoHome terzohome = (TerzoHome) getHome(userContext, TerzoBulk.class);
			TerzoBulk terzoDB = (TerzoBulk) terzohome.findByPrimaryKey(terzo);

			Anagrafico_esercizioHome anagesehome = (Anagrafico_esercizioHome) getHome(userContext, Anagrafico_esercizioBulk.class);
			Anagrafico_esercizioBulk anagEse = (Anagrafico_esercizioBulk) anagesehome.findByPrimaryKey(new Anagrafico_esercizioBulk(terzoDB.getCd_anag(), CNRUserContext.getEsercizio(userContext)));

			if (tipoConto.equals(Movimento_cogeBulk.TipoRiga.DEBITO.value())) {
				if (Optional.ofNullable(anagEse).flatMap(el -> Optional.ofNullable(el.getCd_voce_debito_ep())).isPresent()) {
					Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
					voceEpBulk = (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(anagEse.getCd_voce_debito_ep(), anagEse.getEsercizio_voce_debito_ep()));
				}
			} else {
				if (Optional.ofNullable(anagEse).flatMap(el -> Optional.ofNullable(el.getCd_voce_credito_ep())).isPresent()) {
					Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);
					voceEpBulk = (Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(anagEse.getCd_voce_credito_ep(), anagEse.getEsercizio_voce_credito_ep()));
				}
			}
			return Optional.ofNullable(voceEpBulk)
					.orElseThrow(()->new ApplicationRuntimeException("Conto " +
							(tipoConto.equals(Movimento_cogeBulk.TipoRiga.DEBITO.value()) ? "debito" : "credito") +
							" associato al codice terzo " + terzo.getCd_terzo() + " non individuato."));
		}
		throw new ApplicationRuntimeException("Conto " +
				(tipoConto.equals(Movimento_cogeBulk.TipoRiga.DEBITO.value()) ? "debito" : "credito") +
				" non individuato! Non risulta essere stato specificato il codice terzo di cui individuare l'associazione!");
	}

	private Map<String, Pair<String, BigDecimal>> getSaldiMovimentiPartita(UserContext userContext, IDocumentoCogeBulk docamm, Integer cdTerzoDocAmm, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws ComponentException, PersistencyException {
		Map<String, Pair<String, BigDecimal>> result = ((Movimento_cogeHome) getHome(userContext, Movimento_cogeBulk.class)).getSaldiMovimentiPartita(docamm, cdTerzoDocAmm, scritturaToExclude);
		if (result.isEmpty()) {
			try {
				Collection<Movimento_cogeBulk> allMovimentiCoge = this.innerProposeScritturaPartitaDoppia(userContext, docamm).getAllMovimentiColl()
						.stream().filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
						.filter(el->docamm.getCd_cds().equals(el.getCd_cds_documento()))
						.filter(el->docamm.getCd_uo().equals(el.getCd_uo_documento()))
						.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
						.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
						.filter(el->cdTerzoDocAmm.equals(el.getCd_terzo()))
						.filter(el->!Optional.ofNullable(el.getCd_contributo_ritenuta()).isPresent())
						.filter(el->
								(docamm.getTipoDocumentoEnum().isDocumentoPassivo() && el.getTi_riga().equals(Movimento_cogeBulk.TipoRiga.DEBITO.value())) ||
										(docamm.getTipoDocumentoEnum().isDocumentoAttivo() && el.getTi_riga().equals(Movimento_cogeBulk.TipoRiga.CREDITO.value()))
						)
						.collect(Collectors.toList());

				Map<String, List<Movimento_cogeBulk>> mapVoceEp = allMovimentiCoge.stream().collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep));

				mapVoceEp.keySet().forEach(cdVoceEp->{
					List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
					BigDecimal totaleDare = movimentiList.stream()
							.filter(Movimento_cogeBulk::isSezioneDare)
							.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

					BigDecimal totaleAvere = movimentiList.stream()
							.filter(Movimento_cogeBulk::isSezioneAvere)
							.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

					BigDecimal saldo = totaleDare.subtract(totaleAvere);
					if (saldo.compareTo(BigDecimal.ZERO)>=0)
						result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
					else
						result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
				});
			} catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException ignored) {
				return new HashMap<>();
			}
		}
		return result;
	}

	private Map<String, Pair<String, BigDecimal>> getSaldiMovimentiCori(UserContext userContext, IDocumentoAmministrativoBulk docamm, Integer cdTerzoDocamm, String cdCori, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws ComponentException, PersistencyException {
		return getSaldiMovimentiCori(userContext, docamm, cdTerzoDocamm, cdCori, scritturaToExclude, null);
	}

	private Map<String, Pair<String, BigDecimal>> getSaldiMovimentiCori(UserContext userContext, IDocumentoAmministrativoBulk docamm, Integer cdTerzoDocamm, String cdCori, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude, List<MandatoBulk> pMandatiCompenso) throws ComponentException, PersistencyException {
        List<IDocumentoCogeBulk> docMovimentiCori = new ArrayList<>();
		boolean isContabilizzaSuInviato = Optional.ofNullable(pMandatiCompenso).map(list-> list.stream().filter(el->!el.isPagato()).anyMatch(el->el.getDt_trasmissione()!=null)).orElse(Boolean.FALSE);

        Map<String, Pair<String, BigDecimal>> result = new HashMap<>(((Movimento_cogeHome) getHome(userContext, Movimento_cogeBulk.class)).getSaldiMovimentiCori(docamm, cdTerzoDocamm, cdCori, scritturaToExclude));

		//se i saldi sono nulli verifico se per caso sono tali perchè il mandato principale non risulta pagato...
		//In caso affermativo simulo la sua contabilizzazione che avverrà successivamente
		if (!result.isEmpty() || !isContabilizzaSuInviato)
			return result;

		boolean isAttivaEconomicaDocamm = ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).isAttivaEconomica(docamm.getEsercizio());
		if (!isAttivaEconomicaDocamm || isContabilizzaSuInviato) {
			docMovimentiCori.add(docamm);
			if (!pMandatiCompenso.isEmpty())
				docMovimentiCori.addAll(pMandatiCompenso);

			Collection<Movimento_cogeBulk> allMovimentiCoge = new ArrayList<>();

			docMovimentiCori.forEach(docMov-> {
				try {
					allMovimentiCoge.addAll(innerProposeScritturaPartitaDoppia(userContext, docMov, isContabilizzaSuInviato).getAllMovimentiColl()
							.stream().filter(el -> docamm.getEsercizio().equals(el.getEsercizio_documento()))
							.filter(el -> docamm.getCd_cds().equals(el.getCd_cds_documento()))
							.filter(el -> docamm.getCd_uo().equals(el.getCd_uo_documento()))
							.filter(el -> docamm.getPg_doc().equals(el.getPg_numero_documento()))
							.filter(el -> docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
							.filter(el -> cdTerzoDocamm.equals(el.getCd_terzo()))
							.filter(el -> cdCori.equals(el.getCd_contributo_ritenuta()))
							.collect(Collectors.toList()));
				} catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException ignored) {
				} catch (ComponentException e) {
					throw new ApplicationRuntimeException(e);
				}
			});

			Map<String, List<Movimento_cogeBulk>> mapVoceEp = allMovimentiCoge.stream().collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep));

			mapVoceEp.keySet().forEach(cdVoceEp -> {
				List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
				BigDecimal totaleDare = movimentiList.stream()
						.filter(Movimento_cogeBulk::isSezioneDare)
						.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal totaleAvere = movimentiList.stream()
						.filter(Movimento_cogeBulk::isSezioneAvere)
						.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal saldo = totaleDare.subtract(totaleAvere);
				if (saldo.compareTo(BigDecimal.ZERO) >= 0)
					result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
				else
					result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
			});
		}
		return result;
	}

	/**
	 * Ritorna la lista delle righe prima nota associate al documento
	 * @param userContext userContext
	 * @param docamm docamm
	 * @return List<Movimento_cogeBulk> List<Movimento_cogeBulk>
	 * @throws ComponentException ComponentException
	 * @throws PersistencyException PersistencyException
	 */
	private List<Movimento_cogeBulk> findMovimentiPrimaNota(UserContext userContext, IDocumentoCogeBulk docamm) throws ComponentException, PersistencyException {
		Scrittura_partita_doppiaHome partitaDoppiaHome = Optional.ofNullable(getHome(userContext, Scrittura_partita_doppiaBulk.class))
				.filter(Scrittura_partita_doppiaHome.class::isInstance)
				.map(Scrittura_partita_doppiaHome.class::cast)
				.orElseThrow(() -> new DetailedRuntimeException("Partita doppia Home not found"));

		//Se è attiva l'economica per l'esercizio del documento allora leggo la scrittura, altrimenti ritorno ciò  che proporrei dato che negli anni dove
		//l'economica non è attiva la scrittura è diversa.
		boolean isAttivaEconomica = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).isAttivaEconomica(docamm.getEsercizio());
		if (isAttivaEconomica) {
			final Optional<Scrittura_partita_doppiaBulk> scritturaOpt = partitaDoppiaHome.findByDocumentoAmministrativo(docamm);
			if (scritturaOpt.isPresent()) {
				Scrittura_partita_doppiaBulk scrittura = scritturaOpt.get();
				scrittura.setMovimentiDareColl(new BulkList(((Scrittura_partita_doppiaHome) getHome(userContext, scrittura.getClass()))
						.findMovimentiDareColl(userContext, scrittura, false)));
				scrittura.setMovimentiAvereColl(new BulkList(((Scrittura_partita_doppiaHome) getHome(userContext, scrittura.getClass()))
						.findMovimentiAvereColl(userContext, scrittura, false)));
				return scrittura.getAllMovimentiColl();
			}
		} else {
            //recupero i movimenti legati alla prima scrittura se esiste... altrimenti risimulo la scrittura della partita
            final Movimento_cogeHome movimentoCogeHome = (Movimento_cogeHome)getHome(userContext, Movimento_cogeBulk.class);
            SQLBuilder sqlMc = movimentoCogeHome.createSQLBuilderWithoutJoin();
			sqlMc.addClause(FindClause.AND, "cd_tipo_documento", SQLBuilder.EQUALS, docamm.getCd_tipo_doc());
			sqlMc.addClause(FindClause.AND, "cd_cds_documento", SQLBuilder.EQUALS, docamm.getCd_cds());
			sqlMc.addClause(FindClause.AND, "cd_uo_documento", SQLBuilder.EQUALS, docamm.getCd_uo());
			sqlMc.addClause(FindClause.AND, "esercizio_documento", SQLBuilder.EQUALS, docamm.getEsercizio());
			sqlMc.addClause(FindClause.AND, "pg_numero_documento", SQLBuilder.EQUALS, docamm.getPg_doc());
			sqlMc.addSQLJoin("SCRITTURA_PARTITA_DOPPIA.esercizio","MOVIMENTO_COGE.esercizio");
			sqlMc.addSQLJoin("SCRITTURA_PARTITA_DOPPIA.cd_cds","MOVIMENTO_COGE.cd_cds");
			sqlMc.addSQLJoin("SCRITTURA_PARTITA_DOPPIA.cd_unita_organizzativa","MOVIMENTO_COGE.cd_unita_organizzativa");
			sqlMc.addSQLJoin("SCRITTURA_PARTITA_DOPPIA.pg_scrittura","MOVIMENTO_COGE.pg_scrittura");

			SQLBuilder sqlPd = partitaDoppiaHome.createSQLBuilder();
			sqlPd.addSQLExistsClause(FindClause.AND, sqlMc);

			List<Scrittura_partita_doppiaBulk> resultPd = partitaDoppiaHome.fetchAll(sqlPd);

			if (!resultPd.isEmpty()) {
				Optional<Scrittura_partita_doppiaBulk> spdMin = resultPd.stream().min(Comparator.comparing(Scrittura_partita_doppiaBulk::getDt_contabilizzazione));
                sqlMc = movimentoCogeHome.createSQLBuilderWithoutJoin();
                sqlMc.addClause(FindClause.AND, "cd_tipo_documento", SQLBuilder.EQUALS, docamm.getCd_tipo_doc());
                sqlMc.addClause(FindClause.AND, "cd_cds_documento", SQLBuilder.EQUALS, docamm.getCd_cds());
                sqlMc.addClause(FindClause.AND, "cd_uo_documento", SQLBuilder.EQUALS, docamm.getCd_uo());
                sqlMc.addClause(FindClause.AND, "esercizio_documento", SQLBuilder.EQUALS, docamm.getEsercizio());
                sqlMc.addClause(FindClause.AND, "pg_numero_documento", SQLBuilder.EQUALS, docamm.getPg_doc());
                sqlMc.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, spdMin.get().getEsercizio());
                sqlMc.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, spdMin.get().getCd_cds());
                sqlMc.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, spdMin.get().getCd_unita_organizzativa());
                sqlMc.addClause(FindClause.AND, "pg_scrittura", SQLBuilder.EQUALS, spdMin.get().getPg_scrittura());

                return movimentoCogeHome.fetchAll(sqlMc);
            }
			//se non recupera nulla risimulo la scrittura della partita
 			try {
				return innerProposeScritturaPartitaDoppia(userContext, docamm).getAllMovimentiColl();
			} catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException ignored) {
			}
		}
		return Collections.EMPTY_LIST;
	}

	private Movimento_cogeBulk findMovimentoAperturaPartita(List<Movimento_cogeBulk> movimentiCoge, IDocumentoCogeBulk docamm, Integer cdTerzo) throws ComponentException {
		List<Movimento_cogeBulk> result = movimentiCoge.stream()
				.filter(el->docamm.getTipoDocumentoEnum().getSezionePatrimoniale().equals(el.getSezione()))
				.filter(el->docamm.getTipoDocumentoEnum().getTipoPatrimoniale().equals(el.getTi_riga()))
				.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
				.filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
				.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
				.filter(el->cdTerzo.equals(el.getCd_terzo()))
				.filter(el->el.getCd_contributo_ritenuta()==null)
				.collect(Collectors.toList());

		if (result.size()>1)
			throw new ApplicationException("Errore nell'individuazione del singolo movimento di prima nota di apertura della partita "
					+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": sono state individuate troppe righe.");
		return result.stream().findFirst()
				.orElseThrow(()->new ApplicationRuntimeException("Errore nell'individuazione del singolo movimento di prima nota della partita "
						+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": non è stata individuata nessuna riga."));
	}

	private Movimento_cogeBulk findMovimentoAperturaCori(List<Movimento_cogeBulk> movimentiCoge, IDocumentoCogeBulk docamm, Integer cdTerzo, String cdCori) throws ComponentException {
		List<Movimento_cogeBulk> result = movimentiCoge.stream()
				.filter(el->docamm.getTipoDocumentoEnum().getTipoPatrimoniale().equals(el.getTi_riga()))
				.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
				.filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
				.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
				.filter(el->cdTerzo.equals(el.getCd_terzo()))
				.filter(el->cdCori.equals(el.getCd_contributo_ritenuta()))
				.collect(Collectors.toList());

		if (result.size()>1)
			throw new ApplicationException("Errore nell'individuazione del singolo movimento di prima nota di apertura del contributo "+cdCori
					+ " associato alla partita "+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": sono state individuate troppe righe.");
		return result.stream().findFirst()
				.orElseThrow(()->new ApplicationRuntimeException("Errore nell'individuazione del singolo movimento di prima nota di apertura del contributo "+cdCori
						+ " associato alla partita "+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": non è stata individuata nessuna riga."));
	}

	private Movimento_cogeBulk findMovimentoAperturaCoriIVACompenso(List<Movimento_cogeBulk> movimentiCoge, CompensoBulk docamm, Integer cdTerzo, String cdCori) throws ComponentException {

		List<Movimento_cogeBulk> result = movimentiCoge.stream()
				.filter(el->docamm.getTipoDocumentoEnum().getSezioneIva().equals(el.getSezione()))
				.filter(el-> (docamm.getFl_split_payment()?Movimento_cogeBulk.TipoRiga.CREDITO.value():Movimento_cogeBulk.TipoRiga.IVA_ACQUISTO.value())
						.equals(el.getTi_riga()))
				.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
				.filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
				.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
				.filter(el->cdTerzo.equals(el.getCd_terzo()))
				.filter(el->cdCori.equals(el.getCd_contributo_ritenuta()))
				.collect(Collectors.toList());

		if (result.size()>1)
			throw new ApplicationException("Errore nell'individuazione del singolo movimento di prima nota di apertura del contributo "+cdCori
					+ " associato alla partita "+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": sono state individuate troppe righe.");
		return result.stream().findFirst()
				.orElseThrow(()->new ApplicationRuntimeException("Errore nell'individuazione del singolo movimento di prima nota di apertura del contributo "+cdCori
						+ " associato alla partita "+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": non è stata individuata nessuna riga."));
	}
	private Movimento_cogeBulk findMovimentoAperturaCreditoAnticipo(List<Movimento_cogeBulk> movimentiCoge, AnticipoBulk docamm, Integer cdTerzo) throws ComponentException {
		List<Movimento_cogeBulk> result = movimentiCoge.stream()
				.filter(el->docamm.getTipoDocumentoEnum().getSezioneEconomica().equals(el.getSezione()))
				.filter(el->docamm.getTipoDocumentoEnum().getTipoEconomica().equals(el.getTi_riga()))
				.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
				.filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
				.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
				.filter(el->cdTerzo.equals(el.getCd_terzo()))
				.filter(el->el.getCd_contributo_ritenuta()==null)
				.collect(Collectors.toList());

		if (result.size()>1)
			throw new ApplicationException("Errore nell'individuazione del singolo movimento di prima nota di apertura credito dell'anticipo "
					+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": sono state individuate troppe righe.");
		return result.stream().findFirst()
				.orElseThrow(()->new ApplicationRuntimeException("Errore nell'individuazione del singolo movimento di prima nota di apertura credito dell'anticipo "
						+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": non è stata individuata nessuna riga."));
	}

	private Movimento_cogeBulk findMovimentoAperturaCreditoEconomo(List<Movimento_cogeBulk> movimentiCoge, Documento_genericoBulk docamm, Integer cdTerzo) throws ComponentException {
		List<Movimento_cogeBulk> result = movimentiCoge.stream()
				.filter(el->docamm.getTipoDocumentoEnum().getSezioneEconomica().equals(el.getSezione()))
				.filter(el->docamm.getTipoDocumentoEnum().getTipoEconomica().equals(el.getTi_riga()))
				.filter(el->docamm.getCd_tipo_doc().equals(el.getCd_tipo_documento()))
				.filter(el->docamm.getEsercizio().equals(el.getEsercizio_documento()))
				.filter(el->docamm.getPg_doc().equals(el.getPg_numero_documento()))
				.filter(el->cdTerzo.equals(el.getCd_terzo()))
				.filter(el->el.getCd_contributo_ritenuta()==null)
				.collect(Collectors.toList());

		if (result.size()>1)
			throw new ApplicationException("Errore nell'individuazione del singolo movimento di prima nota di apertura credito dell'economo "
					+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": sono state individuate troppe righe.");
		return result.stream().findFirst()
				.orElseThrow(()->new ApplicationRuntimeException("Errore nell'individuazione del singolo movimento di prima nota di apertura credito dell'economo "
						+docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getPg_doc()+": non è stata individuata nessuna riga."));
	}

	/**
	 * Ritorna la lista delle righe prima nota associate che movimentano i conti patrimoniali di tipo debito/credito associati al documento
	 * @param docamm docamm
	 * @return List<Movimento_cogeBulk> List<Movimento_cogeBulk>
	 */
	private List<Movimento_cogeBulk> findMovimentiPatrimoniali(List<Movimento_cogeBulk> movimentiCoge, IDocumentoCogeBulk docamm) {
		List<Movimento_cogeBulk> movimenti = movimentiCoge.stream().filter(el->el.getSezione().equals(docamm.getTipoDocumentoEnum().getSezionePatrimoniale()))
				.filter(el->docamm.getTipoDocumentoEnum().getTipoPatrimoniale().equals(el.getTi_riga())).collect(Collectors.toList());

		if (movimenti.isEmpty()) {
			throw new ApplicationRuntimeException("Non è stato possibile individuare la riga di tipo debito/credito per il documento " +
					docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_cds()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc()+
					". Proposta di prima nota non possibile.");
		}
		return movimenti;
	}

	private Scrittura_partita_doppiaBulk generaScrittura(UserContext userContext, IDocumentoCogeBulk doccoge, List<TestataPrimaNota> testataPrimaNota, boolean accorpaConti) {
		return generaScritture(userContext, doccoge, testataPrimaNota, accorpaConti, Boolean.FALSE).getScritturaPartitaDoppiaBulk();
	}

	private ResultScrittureContabili generaScritture(UserContext userContext, IDocumentoCogeBulk doccoge, List<TestataPrimaNota> testataPrimaNota, boolean accorpaConti, boolean makeAnalitica) {
		ResultScrittureContabili resultScrittureContabili = this.makeScrittureEconomica(userContext, doccoge, testataPrimaNota, accorpaConti, makeAnalitica);
		Scrittura_partita_doppiaBulk scritturaPartitaDoppia = resultScrittureContabili.getScritturaPartitaDoppiaBulk();

        //Metto il controllo che Dare=Avere
		if (scritturaPartitaDoppia!=null && scritturaPartitaDoppia.getImTotaleDare().compareTo(scritturaPartitaDoppia.getImTotaleAvere())!=0)
			throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del documento "+
					doccoge.getTipoDocumentoEnum()+"/"+doccoge.getEsercizio()+"/"+doccoge.getCd_cds()+"/"+doccoge.getCd_uo()+
					"/"+doccoge.getPg_doc()+". Il totale Dare (" +
					new EuroFormat().format(scritturaPartitaDoppia.getImTotaleDare()) +
					") non risulterebbe essere uguale al totale Avere (" +
					new EuroFormat().format(scritturaPartitaDoppia.getImTotaleAvere())+").");

		if (doccoge instanceof MandatoBulk || doccoge instanceof ReversaleBulk) {
			//Metto il controllo che il conto tesoreria sia sempre pari al netto mandato/reversale se richiesto di fare controllo
			BigDecimal saldoTesoreria = Optional.ofNullable(scritturaPartitaDoppia).map(spd->spd.getAllMovimentiColl().stream()
					.filter(Movimento_cogeBulk::isRigaTipoTesoreria)
					.map(el -> el.isSezioneDare() ? el.getIm_movimento() : el.getIm_movimento().negate())
					.reduce(BigDecimal.ZERO, BigDecimal::add)).orElse(BigDecimal.ZERO);
			if (doccoge instanceof MandatoBulk) {
				if (((MandatoBulk)doccoge).isAnnullato()) {
					if (saldoTesoreria.compareTo(BigDecimal.ZERO) != 0)
						throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " + doccoge.getEsercizio() + "/" +
								doccoge.getCd_cds() + "/" + doccoge.getPg_doc() + ". Il saldo del conto tesoreria (" +
								new EuroFormat().format(saldoTesoreria.negate()) +
								") non risulta nullo anche se il mandato risulta essere annullato.");
				} else if (saldoTesoreria.negate().compareTo(((MandatoBulk) doccoge).getIm_netto()) != 0)
					throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota del mandato " + doccoge.getEsercizio() + "/" +
							doccoge.getCd_cds() + "/" + doccoge.getPg_doc() + ". Il saldo del conto tesoreria (" +
							new EuroFormat().format(saldoTesoreria.negate()) +
							") non risulterebbe essere uguale all'importo netto del mandato (" +
							new EuroFormat().format(((MandatoBulk) doccoge).getIm_netto()) + ").");
			} else {
				if (((ReversaleBulk)doccoge).isAnnullato()) {
					if (saldoTesoreria.compareTo(BigDecimal.ZERO) != 0)
						throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota della reversale " + doccoge.getEsercizio() + "/" +
								doccoge.getCd_cds() + "/" + doccoge.getPg_doc() + ". Il saldo del conto tesoreria (" +
								new EuroFormat().format(saldoTesoreria.negate()) +
								") non risulta nullo anche se la reversale risulta essere annullata.");
				} if (saldoTesoreria.compareTo(((ReversaleBulk) doccoge).getIm_reversale()) != 0)
					throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota della reversale " + doccoge.getEsercizio() + "/" +
							doccoge.getCd_cds() + "/" + doccoge.getPg_doc() + ". Il saldo del conto tesoreria  (" +
							new EuroFormat().format(saldoTesoreria) +
							") non risulterebbe essere uguale all'importo della reversale (" +
							new EuroFormat().format(((ReversaleBulk) doccoge).getIm_reversale()) + ").");
			}
		}

		//Metto controllo che se movimentato costo si movimenta anche l'analitica
		if (makeAnalitica) {
			Scrittura_analiticaBulk scritturaAnalitica = resultScrittureContabili.getScritturaAnaliticaBulk();

            assert scritturaPartitaDoppia != null;
            scritturaPartitaDoppia.getAllMovimentiColl().stream()
					.filter(mc->mc.isRigaTipoCosto() || mc.isRigaTipoRicavo())
					.forEach(mc->{
						if (scritturaAnalitica==null)
							throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota e analitica del documento "+
								doccoge.getTipoDocumentoEnum()+"/"+doccoge.getEsercizio()+"/"+doccoge.getCd_cds()+"/"+doccoge.getCd_uo()+
								"/"+doccoge.getPg_doc()+". Non risulta alcuna ripartizione in analitica per il conto " +
								mc.getCd_voce_ep() + ".");
						BigDecimal totAnalitica = scritturaAnalitica.getMovimentiColl().stream()
								.filter(ana->ana.getMovimentoCoge().equals(mc))
								.filter(ana->ana.getSezione().equals(mc.getSezione()))
								.map(Movimento_coanBulk::getIm_movimento)
								.reduce(BigDecimal.ZERO, BigDecimal::add);
						if (totAnalitica.compareTo(mc.getIm_movimento())!=0)
							throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota e analitica del documento "+
									doccoge.getTipoDocumentoEnum()+"/"+doccoge.getEsercizio()+"/"+doccoge.getCd_cds()+"/"+doccoge.getCd_uo()+
									"/"+doccoge.getPg_doc()+". Il totale ripartito in analitica (" +
									new EuroFormat().format(totAnalitica) +
									") per il conto " + mc.getCd_voce_ep() + " non risulterebbe essere uguale al totale " +
									"del costo rilevato in partita doppia (" +
									new EuroFormat().format(mc.getIm_movimento())+").");
					});

			if (scritturaAnalitica!=null) {
				scritturaPartitaDoppia.getAllMovimentiColl().stream()
						.filter(mc -> !mc.isRigaTipoCosto() && !mc.isRigaTipoRicavo())
						.forEach(mc -> {
							if (scritturaAnalitica.getMovimentiColl().stream()
								.anyMatch(ana -> ana.getMovimentoCoge().equals(mc)))
								throw new ApplicationRuntimeException("Errore nella generazione scrittura prima nota e analitica del documento " +
										doccoge.getTipoDocumentoEnum() + "/" + doccoge.getEsercizio() + "/" + doccoge.getCd_cds() + "/" + doccoge.getCd_uo() +
										"/" + doccoge.getPg_doc() + ". Risultano registrazioni in analitica per il conto " + mc.getCd_voce_ep() +
										" non di tipo COSTO/RICAVO.");
						});
			}
		}

		return resultScrittureContabili;
	}

	private ResultScrittureContabili makeScrittureEconomica(UserContext userContext, IDocumentoCogeBulk doccoge, List<TestataPrimaNota> testataPrimaNota, boolean accorpaConti, boolean makeAnalitica) {
		if (!testataPrimaNota.stream().flatMap(el->el.getDett().stream()).findAny().isPresent())
			return new ResultScrittureContabili(doccoge, null,null);

		Scrittura_partita_doppiaBulk scritturaPartitaDoppia = new Scrittura_partita_doppiaBulk();
		Scrittura_analiticaBulk scritturaAnalitica;

		scritturaPartitaDoppia.setToBeCreated();
		scritturaPartitaDoppia.setDt_contabilizzazione(doccoge.getDt_contabilizzazione());
		scritturaPartitaDoppia.setUser(userContext.getUser());
		scritturaPartitaDoppia.setCd_unita_organizzativa(doccoge.getCd_uo());
		scritturaPartitaDoppia.setCd_cds(doccoge.getCd_cds());
		scritturaPartitaDoppia.setTi_scrittura(Scrittura_partita_doppiaBulk.TIPO_PRIMA_SCRITTURA);
		scritturaPartitaDoppia.setStato(Scrittura_partita_doppiaBulk.STATO_DEFINITIVO);
		if (OrigineScritturaEnum.LIQUID_IVA.name().equals(doccoge.getCd_tipo_doc())) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(doccoge.getDtInizioLiquid().getTime());
            scritturaPartitaDoppia.setDs_scrittura(
                    "Contabilizzazione "
                            .concat(doccoge.getCd_tipo_doc()).concat(" ")
                            .concat(new SimpleDateFormat("MM/yyyy").format(cal.getTime())).concat(": ")
                            .concat(doccoge.getCd_cds()).concat("/")
                            .concat(doccoge.getCd_uo()).concat("/")
                            .concat(String.valueOf(doccoge.getEsercizio()))
            );
        } else if (doccoge instanceof OrdineAcqConsegnaBulk) {
            scritturaPartitaDoppia.setDs_scrittura(
                    "Contabilizzazione "
                            .concat(doccoge.getCd_tipo_doc()).concat(": ")
                            .concat(String.valueOf(doccoge.getEsercizio())).concat("/")
                            .concat(doccoge.getCd_cds()).concat("/")
                            .concat(doccoge.getCdUnitaOperativa()).concat("/")
                            .concat(doccoge.getCdNumeratoreOrdine()).concat("/")
                            .concat(String.valueOf(doccoge.getPg_doc())).concat("/")
                            .concat(String.valueOf(doccoge.getRigaOrdine())).concat("/")
                            .concat(String.valueOf(doccoge.getConsegna()))
            );
		} else {
			scritturaPartitaDoppia.setDs_scrittura(
					"Contabilizzazione "
							.concat(doccoge.getCd_tipo_doc()).concat(": ")
							.concat(doccoge.getCd_cds()).concat("/")
							.concat(doccoge.getCd_uo()).concat("/")
							.concat(String.valueOf(doccoge.getEsercizio())).concat("/")
							.concat(String.valueOf(doccoge.getPg_doc()))
			);
		}
		scritturaPartitaDoppia.setEsercizio(doccoge.getEsercizio());
		scritturaPartitaDoppia.setEsercizio_documento_amm(doccoge.getEsercizio());
		scritturaPartitaDoppia.setCd_cds_documento(doccoge.getCd_cds());
		scritturaPartitaDoppia.setCd_uo_documento(doccoge.getCd_uo());
		scritturaPartitaDoppia.setCd_tipo_documento(doccoge.getCd_tipo_doc());
		scritturaPartitaDoppia.setPg_numero_documento(doccoge.getPg_doc());
		scritturaPartitaDoppia.setDt_inizio_liquid(doccoge.getDtInizioLiquid());
		scritturaPartitaDoppia.setDt_fine_liquid(doccoge.getDtFineLiquid());
		scritturaPartitaDoppia.setTipo_liquidazione(doccoge.getTipoLiquid());
		scritturaPartitaDoppia.setReport_id_liquid(doccoge.getReportIdLiquid());
		scritturaPartitaDoppia.setAttiva(Scrittura_partita_doppiaBulk.ATTIVA_YES);

        scritturaPartitaDoppia.setCdNumeratoreOrdine(doccoge.getCdNumeratoreOrdine());
        scritturaPartitaDoppia.setCdUnitaOperativa(doccoge.getCdUnitaOperativa());
        scritturaPartitaDoppia.setRigaOrdine(doccoge.getRigaOrdine());
        scritturaPartitaDoppia.setConsegna(doccoge.getConsegna());

		TipoDocumentoEnum tipoDocumento = TipoDocumentoEnum.fromValue(scritturaPartitaDoppia.getCd_tipo_documento());
		if (tipoDocumento.isGenericoStipendiSpesa())
			scritturaPartitaDoppia.setOrigine_scrittura(OrigineScritturaEnum.STIPENDI.name());
		else if (tipoDocumento.isLiquidazioneIva())
			scritturaPartitaDoppia.setOrigine_scrittura(OrigineScritturaEnum.LIQUID_IVA.name());
		else if (tipoDocumento.isDocumentoAttivo() || tipoDocumento.isDocumentoPassivo() || tipoDocumento.isConsegnaOrdineAcquisto())
			scritturaPartitaDoppia.setOrigine_scrittura(OrigineScritturaEnum.DOCAMM.name());
		else if (tipoDocumento.isMandato() || tipoDocumento.isReversale())
			scritturaPartitaDoppia.setOrigine_scrittura(OrigineScritturaEnum.DOCCONT.name());
		else
			scritturaPartitaDoppia.setOrigine_scrittura(OrigineScritturaEnum.CAUSALE.name());

		if (makeAnalitica) {
			scritturaAnalitica = new Scrittura_analiticaBulk();
			scritturaAnalitica.setToBeCreated();
			scritturaAnalitica.setDt_contabilizzazione(scritturaPartitaDoppia.getDt_contabilizzazione());
			scritturaAnalitica.setUser(userContext.getUser());
			scritturaAnalitica.setCd_unita_organizzativa(scritturaPartitaDoppia.getCd_unita_organizzativa());
			scritturaAnalitica.setCd_cds(scritturaPartitaDoppia.getCd_cds());
			scritturaAnalitica.setTi_scrittura(scritturaPartitaDoppia.getTi_scrittura());
			scritturaAnalitica.setStato(scritturaPartitaDoppia.getStato());
			scritturaAnalitica.setDs_scrittura(scritturaPartitaDoppia.getDs_scrittura());
			scritturaAnalitica.setEsercizio(scritturaPartitaDoppia.getEsercizio());
			scritturaAnalitica.setEsercizio_documento_amm(scritturaPartitaDoppia.getEsercizio());
			scritturaAnalitica.setCd_cds_documento(scritturaPartitaDoppia.getCd_cds());
			scritturaAnalitica.setCd_uo_documento(scritturaPartitaDoppia.getCd_uo_documento());
			scritturaAnalitica.setCd_tipo_documento(scritturaPartitaDoppia.getCd_tipo_documento());
			scritturaAnalitica.setPg_numero_documento(scritturaPartitaDoppia.getPg_numero_documento());
			scritturaAnalitica.setAttiva(scritturaPartitaDoppia.getAttiva());
			scritturaAnalitica.setOrigine_scrittura(scritturaPartitaDoppia.getOrigine_scrittura());

            scritturaAnalitica.setCdNumeratoreOrdine(scritturaPartitaDoppia.getCdNumeratoreOrdine());
            scritturaAnalitica.setCdUnitaOperativa(scritturaPartitaDoppia.getCdUnitaOperativa());
            scritturaAnalitica.setRigaOrdine(scritturaPartitaDoppia.getRigaOrdine());
            scritturaAnalitica.setConsegna(scritturaPartitaDoppia.getConsegna());
        } else {
            scritturaAnalitica = null;
        }

        testataPrimaNota.forEach(testata -> {
			if (accorpaConti) {
				//Prima analizzo i conti patrimoniali con partita senza cori
				//I conti patrimoniali devono essere accorpati per partita e distinti tra modificabili e non
				Map<IDocumentoCogeBulk, Map<Integer, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>> mapPartitePatrimonialiNoCori = testata.getDett().stream()
						.filter(DettaglioPrimaNota::isAccorpabile)
						.filter(DettaglioPrimaNota::isDettaglioPatrimoniale)
						.filter(el -> Optional.ofNullable(el.getPartita()).isPresent())
						.filter(el -> !Optional.ofNullable(el.getCdCori()).isPresent())
						.collect(Collectors.groupingBy(DettaglioPrimaNota::getPartita,
								Collectors.groupingBy(DettaglioPrimaNota::getCdTerzo,
										Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
												Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
														Collectors.groupingBy(DettaglioPrimaNota::getCdConto))))));

				mapPartitePatrimonialiNoCori.keySet().forEach(aPartita -> {
					Map<Integer, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>> mapCdTerzo = mapPartitePatrimonialiNoCori.get(aPartita);
					mapCdTerzo.keySet().forEach(aCdTerzo -> {
						Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDettPatrimoniali = mapCdTerzo.get(aCdTerzo);
						mapTipoDettPatrimoniali.keySet().forEach(aTipoDett -> {
							Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDettPatrimoniali.get(aTipoDett);
							mapModificabile.keySet().forEach(aTipoModific -> {
								Map<String, List<DettaglioPrimaNota>> mapContiPatrimoniali = mapModificabile.get(aTipoModific);
								mapContiPatrimoniali.keySet().forEach(aContoPatrimoniale -> {
									try {
										Pair<String, BigDecimal> saldoPatrimoniale = this.getSaldo(mapContiPatrimoniali.get(aContoPatrimoniale));
										Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, saldoPatrimoniale.getFirst(), aContoPatrimoniale, saldoPatrimoniale.getSecond(), aPartita, aCdTerzo, null);
										Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
										if (makeAnalitica)
											addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movcoge, mapContiPatrimoniali.get(aContoPatrimoniale));
									} catch (ComponentException e) {
										throw new ApplicationRuntimeException(e);
									}
								});
							});
						});
					});
				});

				//Poi i conti patrimoniali con partita e cori distinti sempre tra modificabili e non
				Map<IDocumentoCogeBulk, Map<Integer, Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>>> mapPartitePatrimonialiCori = testata.getDett().stream()
						.filter(DettaglioPrimaNota::isAccorpabile)
						.filter(DettaglioPrimaNota::isDettaglioPatrimoniale)
						.filter(el -> Optional.ofNullable(el.getPartita()).isPresent())
						.filter(el -> Optional.ofNullable(el.getCdCori()).isPresent())
						.collect(Collectors.groupingBy(DettaglioPrimaNota::getPartita,
								Collectors.groupingBy(DettaglioPrimaNota::getCdTerzo,
										Collectors.groupingBy(DettaglioPrimaNota::getCdCori,
												Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
														Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
																Collectors.groupingBy(DettaglioPrimaNota::getCdConto)))))));

				mapPartitePatrimonialiCori.keySet().forEach(aPartita -> {
					Map<Integer, Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>> mapCdTerzo = mapPartitePatrimonialiCori.get(aPartita);
					mapCdTerzo.keySet().forEach(aCdTerzo -> {
						Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>> mapCoriPatrimoniali = mapCdTerzo.get(aCdTerzo);
						mapCoriPatrimoniali.keySet().forEach(aCdCori -> {
							Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDettPatrimoniali = mapCoriPatrimoniali.get(aCdCori);
							mapTipoDettPatrimoniali.keySet().forEach(aTipoDett -> {
								Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDettPatrimoniali.get(aTipoDett);
								mapModificabile.keySet().forEach(aTipoModific -> {
									Map<String, List<DettaglioPrimaNota>> mapContiPatrimoniali = mapModificabile.get(aTipoModific);
									mapContiPatrimoniali.keySet().forEach(aContoPatrimoniale -> {
										try {
											Pair<String, BigDecimal> saldoPatrimoniale = this.getSaldo(mapContiPatrimoniali.get(aContoPatrimoniale));
											Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, saldoPatrimoniale.getFirst(), aContoPatrimoniale, saldoPatrimoniale.getSecond(), aPartita, aCdTerzo, aCdCori);
											Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
										} catch (ComponentException e) {
											throw new ApplicationRuntimeException(e);
										}
									});
								});
							});
						});
					});
				});

				//Poi analizzo i conti patrimoniali senza partita
				{
					Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDettPatrimonialiSenzaPartita = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(DettaglioPrimaNota::isDettaglioPatrimoniale)
							.filter(el -> !Optional.ofNullable(el.getPartita()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
									Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(DettaglioPrimaNota::getCdConto))));

					mapTipoDettPatrimonialiSenzaPartita.keySet().forEach(aTipoDett -> {
						Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDettPatrimonialiSenzaPartita.get(aTipoDett);
						mapModificabile.keySet().forEach(aTipoModific -> {
							Map<String, List<DettaglioPrimaNota>> mapContiPatrimonialiSenzaPartita = mapModificabile.get(aTipoModific);
							mapContiPatrimonialiSenzaPartita.keySet().forEach(aContoPatrimoniale -> {
								try {
									Pair<String, BigDecimal> saldoPatrimoniale = this.getSaldo(mapContiPatrimonialiSenzaPartita.get(aContoPatrimoniale));
									Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, saldoPatrimoniale.getFirst(), aContoPatrimoniale, saldoPatrimoniale.getSecond());
									Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
								} catch (ComponentException e) {
									throw new ApplicationRuntimeException(e);
								}
							});
						});
					});
				}

				//Poi analizzo i conti IVA
				{
					Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDettIva = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(DettaglioPrimaNota::isDettaglioIva)
							.filter(el -> !Optional.ofNullable(el.getPartita()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
									Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(DettaglioPrimaNota::getCdConto))));

					mapTipoDettIva.keySet().forEach(aTipoDett -> {
						Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDettIva.get(aTipoDett);
						mapModificabile.keySet().forEach(aTipoModific -> {
							Map<String, List<DettaglioPrimaNota>> mapContiIva = mapModificabile.get(aTipoModific);
							mapContiIva.keySet().forEach(aContoIva -> {
								try {
									Pair<String, BigDecimal> saldoIva = this.getSaldo(mapContiIva.get(aContoIva));
									Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, saldoIva.getFirst(), aContoIva, saldoIva.getSecond());
									Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
								} catch (ComponentException e) {
									throw new ApplicationRuntimeException(e);
								}
							});
						});
					});
				}

				//Poi analizzo i conti COSTO/RICAVO distinguendoli tra modificabili e non
				{
					Map<Boolean, Map<String, Map<String, Map<String, List<DettaglioPrimaNota>>>>> mapModificabile = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(DettaglioPrimaNota::isDettaglioCostoRicavo)
							.collect(Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(dett->Optional.ofNullable(dett.getDtDaCompetenzaCoge()).map(Timestamp::toString).orElse("NO_VALUE"),
													Collectors.groupingBy(dett->Optional.ofNullable(dett.getDtACompetenzaCoge()).map(Timestamp::toString).orElse("NO_VALUE"),
															Collectors.groupingBy(DettaglioPrimaNota::getCdConto)))));

					mapModificabile.keySet().forEach(aTipoModific -> {
						Map<String, Map<String, Map<String, List<DettaglioPrimaNota>>>> mapDataDaCompetenza = mapModificabile.get(aTipoModific);
						mapDataDaCompetenza.keySet().forEach(aDataDaCompetenza -> {
							final Timestamp tmsDataDaCompetenza = aDataDaCompetenza.equals("NO_VALUE")?null:Timestamp.valueOf(aDataDaCompetenza);
							Map<String, Map<String, List<DettaglioPrimaNota>>> mapDataACompetenza = mapDataDaCompetenza.get(aDataDaCompetenza);
							mapDataACompetenza.keySet().forEach(aDataACompetenza -> {
								final Timestamp tmsDataACompetenza = aDataACompetenza.equals("NO_VALUE")?null:Timestamp.valueOf(aDataDaCompetenza);
								Map<String, List<DettaglioPrimaNota>> mapContiCostoRicavo = mapDataACompetenza.get(aDataACompetenza);
								mapContiCostoRicavo.keySet().forEach(aContoCostoRicavo -> {
									try {
										Pair<String, BigDecimal> saldoCostoRicavo = this.getSaldo(mapContiCostoRicavo.get(aContoCostoRicavo));
										Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, null, saldoCostoRicavo.getFirst(), aContoCostoRicavo, saldoCostoRicavo.getSecond(), tmsDataDaCompetenza, tmsDataACompetenza);
										Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
										if (makeAnalitica)
											addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movcoge, mapContiCostoRicavo.get(aContoCostoRicavo));
									} catch (ComponentException e) {
										throw new ApplicationRuntimeException(e);
									}
								});
							});
						});
					});
				}

				//Poi analizzo tutti gli altri tipi di conti
				{
					Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDettAltro = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(el -> !el.isDettaglioPatrimoniale())
							.filter(el -> !el.isDettaglioIva())
							.filter(el -> !el.isDettaglioCostoRicavo())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
									Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(DettaglioPrimaNota::getCdConto))));

					mapTipoDettAltro.keySet().forEach(aTipoDett -> {
						Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDettAltro.get(aTipoDett);
						mapModificabile.keySet().forEach(aTipoModific -> {
							Map<String, List<DettaglioPrimaNota>> mapContiAltro = mapModificabile.get(aTipoModific);
							mapContiAltro.keySet().forEach(aContoAltro -> {
								try {
									Pair<String, BigDecimal> saldoCostoRicavo = this.getSaldo(mapContiAltro.get(aContoAltro));
									Movimento_cogeBulk movcoge = addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, saldoCostoRicavo.getFirst(), aContoAltro, saldoCostoRicavo.getSecond());
									Optional.ofNullable(movcoge).ifPresent(el -> el.setFl_modificabile(aTipoModific));
								} catch (ComponentException e) {
									throw new ApplicationRuntimeException(e);
								}
							});
						});
					});
				}
			} else {
				//Prima analizzo i conti con partita senza cori
				//I conti devono essere accorpati per partita
				{
					Map<IDocumentoCogeBulk, Map<Integer, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>> mapPartite = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(el -> Optional.ofNullable(el.getPartita()).isPresent())
							.filter(el -> !Optional.ofNullable(el.getCdCori()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getPartita,
									Collectors.groupingBy(DettaglioPrimaNota::getCdTerzo,
											Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
													Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
															Collectors.groupingBy(DettaglioPrimaNota::getCdConto))))));

					mapPartite.keySet().forEach(aPartita -> {
						Map<Integer, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>> mapCdTerzo = mapPartite.get(aPartita);
						mapCdTerzo.keySet().forEach(aCdTerzo -> {
							Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDett = mapCdTerzo.get(aCdTerzo);
							mapTipoDett.keySet().forEach(aTipoDett -> {
								Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDett.get(aTipoDett);
								mapModificabile.keySet().forEach(aTipoModific -> {
									Map<String, List<DettaglioPrimaNota>> mapConti = mapModificabile.get(aTipoModific);
									mapConti.keySet().forEach(aConto -> {
										try {
											BigDecimal imDare = mapConti.get(aConto).stream()
													.filter(DettaglioPrimaNota::isSezioneDare)
													.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
											Optional<Movimento_cogeBulk> movDareCoge = Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_DARE, aConto, imDare, aPartita, aCdTerzo, null));
											if (movDareCoge.isPresent()) {
												movDareCoge.get().setFl_modificabile(aTipoModific);
												if (makeAnalitica)
													addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movDareCoge.get(), mapConti.get(aConto).stream()
															.filter(DettaglioPrimaNota::isSezioneDare).collect(Collectors.toList()));
											}

											BigDecimal imAvere = mapConti.get(aConto).stream()
													.filter(DettaglioPrimaNota::isSezioneAvere)
													.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
											Optional<Movimento_cogeBulk> movAvereCoge = Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_AVERE, aConto, imAvere, aPartita, aCdTerzo, null));
											if (movAvereCoge.isPresent()) {
												movAvereCoge.get().setFl_modificabile(aTipoModific);
												if (makeAnalitica)
													addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movAvereCoge.get(), mapConti.get(aConto).stream()
															.filter(DettaglioPrimaNota::isSezioneAvere).collect(Collectors.toList()));
											}
										} catch (ComponentException e) {
											throw new ApplicationRuntimeException(e);
										}
									});
								});
							});
						});
					});
				}

				//Poi analizzo i conti con partita e cori
				//I conti devono essere accorpati per partita
				{
					Map<IDocumentoCogeBulk, Map<Integer, Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>>> mapPartite = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(el -> Optional.ofNullable(el.getPartita()).isPresent())
							.filter(el -> Optional.ofNullable(el.getCdCori()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getPartita,
									Collectors.groupingBy(DettaglioPrimaNota::getCdTerzo,
											Collectors.groupingBy(DettaglioPrimaNota::getCdCori,
													Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
															Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
																	Collectors.groupingBy(DettaglioPrimaNota::getCdConto)))))));

					mapPartite.keySet().forEach(aPartita -> {
						Map<Integer, Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>>> mapCdTerzo = mapPartite.get(aPartita);
						mapCdTerzo.keySet().forEach(aCdTerzo -> {
							Map<String, Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>>> mapPartitePatrimonialiCori = mapCdTerzo.get(aCdTerzo);
							mapPartitePatrimonialiCori.keySet().forEach(aCdCori -> {
								Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDett = mapPartitePatrimonialiCori.get(aCdCori);
								mapTipoDett.keySet().forEach(aTipoDett -> {
									Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDett.get(aTipoDett);
									mapModificabile.keySet().forEach(aTipoModific -> {
										Map<String, List<DettaglioPrimaNota>> mapConti = mapModificabile.get(aTipoModific);
										mapConti.keySet().forEach(aConto -> {
											try {
												BigDecimal imDare = mapConti.get(aConto).stream()
														.filter(DettaglioPrimaNota::isSezioneDare)
														.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
												Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_DARE, aConto, imDare, aPartita, aCdTerzo, aCdCori))
														.ifPresent(el -> el.setFl_modificabile(aTipoModific));

												BigDecimal imAvere = mapConti.get(aConto).stream()
														.filter(DettaglioPrimaNota::isSezioneAvere)
														.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
												Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_AVERE, aConto, imAvere, aPartita, aCdTerzo, aCdCori))
														.ifPresent(el -> el.setFl_modificabile(aTipoModific));
											} catch (ComponentException e) {
												throw new ApplicationRuntimeException(e);
											}
										});
									});
								});
							});
						});
					});
				}

				//Poi analizzo i conti costo/ricavo senza partita
				{
					Map<String, Map<Boolean, Map<String, Map<String, Map<String, List<DettaglioPrimaNota>>>>>> mapTipoDett = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(DettaglioPrimaNota::isDettaglioCostoRicavo)
							.filter(el -> !Optional.ofNullable(el.getPartita()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
									Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(dett->Optional.ofNullable(dett.getDtDaCompetenzaCoge()).map(Timestamp::toString).orElse("NO_VALUE"),
													Collectors.groupingBy(dett->Optional.ofNullable(dett.getDtACompetenzaCoge()).map(Timestamp::toString).orElse("NO_VALUE"),
															Collectors.groupingBy(DettaglioPrimaNota::getCdConto))))));

					mapTipoDett.keySet().forEach(aTipoDett -> {
						Map<Boolean, Map<String, Map<String, Map<String, List<DettaglioPrimaNota>>>>> mapModificabile = mapTipoDett.get(aTipoDett);
						mapModificabile.keySet().forEach(aTipoModific -> {
							Map<String, Map<String, Map<String, List<DettaglioPrimaNota>>>> mapDataDaCompetenza = mapModificabile.get(aTipoModific);
							mapDataDaCompetenza.keySet().forEach(aDataDaCompetenza -> {
								Map<String, Map<String, List<DettaglioPrimaNota>>> mapDataACompetenza = mapDataDaCompetenza.get(aDataDaCompetenza);
								mapDataACompetenza.keySet().forEach(aDataACompetenza -> {
									Map<String, List<DettaglioPrimaNota>> mapContiCostoRicavo = mapDataACompetenza.get(aDataACompetenza);
									mapContiCostoRicavo.keySet().forEach(aConto -> {
										try {
											BigDecimal imDare = mapContiCostoRicavo.get(aConto).stream()
													.filter(DettaglioPrimaNota::isSezioneDare)
													.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
											Optional<Movimento_cogeBulk> movDareCoge = Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_DARE, aConto, imDare));
											if (movDareCoge.isPresent()) {
												movDareCoge.get().setFl_modificabile(aTipoModific);
												if (makeAnalitica)
													addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movDareCoge.get(), mapContiCostoRicavo.get(aConto).stream()
															.filter(DettaglioPrimaNota::isSezioneDare).collect(Collectors.toList()));
											}

											BigDecimal imAvere = mapContiCostoRicavo.get(aConto).stream()
													.filter(DettaglioPrimaNota::isSezioneAvere)
													.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
											Optional<Movimento_cogeBulk> movAvereCoge = Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_AVERE, aConto, imAvere));
											if (movAvereCoge.isPresent()) {
												movAvereCoge.get().setFl_modificabile(aTipoModific);
												if (makeAnalitica)
													addMovimentiCoan(userContext, scritturaAnalitica, doccoge, movAvereCoge.get(), mapContiCostoRicavo.get(aConto).stream()
															.filter(DettaglioPrimaNota::isSezioneAvere).collect(Collectors.toList()));
											}
										} catch (ComponentException e) {
											throw new ApplicationRuntimeException(e);
										}
									});
								});
							});
						});
					});
				}

				//Poi analizzo tutti gli altri conti senza partita
				{
					Map<String, Map<Boolean, Map<String, List<DettaglioPrimaNota>>>> mapTipoDett = testata.getDett().stream()
							.filter(DettaglioPrimaNota::isAccorpabile)
							.filter(el->!el.isDettaglioCostoRicavo())
							.filter(el -> !Optional.ofNullable(el.getPartita()).isPresent())
							.collect(Collectors.groupingBy(DettaglioPrimaNota::getTipoDett,
									Collectors.groupingBy(DettaglioPrimaNota::isModificabile,
											Collectors.groupingBy(DettaglioPrimaNota::getCdConto))));

					mapTipoDett.keySet().forEach(aTipoDett -> {
						Map<Boolean, Map<String, List<DettaglioPrimaNota>>> mapModificabile = mapTipoDett.get(aTipoDett);
						mapModificabile.keySet().forEach(aTipoModific -> {
							Map<String, List<DettaglioPrimaNota>> mapConti = mapModificabile.get(aTipoModific);
							mapConti.keySet().forEach(aConto -> {
								try {
									BigDecimal imDare = mapConti.get(aConto).stream()
											.filter(DettaglioPrimaNota::isSezioneDare)
											.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
									Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_DARE, aConto, imDare))
											.ifPresent(el -> el.setFl_modificabile(aTipoModific));

									BigDecimal imAvere = mapConti.get(aConto).stream()
											.filter(DettaglioPrimaNota::isSezioneAvere)
											.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
									Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, Movimento_cogeBulk.SEZIONE_AVERE, aConto, imAvere))
											.ifPresent(el -> el.setFl_modificabile(aTipoModific));
								} catch (ComponentException e) {
									throw new ApplicationRuntimeException(e);
								}
							});
						});
					});
				}
			}

			//infine aggiungo i dettagli non accorpabili
			testata.getDett().stream().filter(el->!el.isAccorpabile()).forEach(dett->{
				try {
					Optional.ofNullable(addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, dett.getTipoDett(), dett.getSezione(), dett.getCdConto(), dett.getImporto(), dett.getPartita(), dett.getCdTerzo(), dett.getCdCori(), dett.getDtDaCompetenzaCoge(), dett.getDtACompetenzaCoge()))
							.ifPresent(el -> el.setFl_modificabile(dett.isModificabile()));
				} catch (ComponentException e) {
					throw new ApplicationRuntimeException(e);
				}
			});
		});
		scritturaPartitaDoppia.setIm_scrittura(scritturaPartitaDoppia.getImTotaleAvere());
		if (makeAnalitica)
			scritturaAnalitica.setIm_scrittura(scritturaAnalitica.getImTotaleMov());
		return new ResultScrittureContabili(doccoge, scritturaPartitaDoppia, Optional.ofNullable(scritturaAnalitica)
				.filter(el->Optional.ofNullable(el.getMovimentiColl()).map(el2->!el2.isEmpty()).orElse(Boolean.FALSE))
				.orElse(null));
	}

	private Movimento_cogeBulk addMovimentoCoge(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppia, IDocumentoCogeBulk doccoge, String aTipoDett, String aSezione, String aCdConto, BigDecimal aImporto) throws ComponentException {
		return this.addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, aSezione, aCdConto, aImporto, null, null, null);
	}

	private Movimento_cogeBulk addMovimentoCoge(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppia, IDocumentoCogeBulk doccoge, String aTipoDett, String aSezione, String aCdConto, BigDecimal aImporto, Timestamp aDataDaCompetenza, Timestamp aDataACompetenza) throws ComponentException {
		return this.addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, aSezione, aCdConto, aImporto, null, null, null, aDataDaCompetenza, aDataACompetenza);
	}

	private Movimento_cogeBulk addMovimentoCoge(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppia, IDocumentoCogeBulk doccoge, String aTipoDett, String aSezione, String aCdConto, BigDecimal aImporto, IDocumentoCogeBulk aPartita, Integer aCdTerzo, String aCdCori) throws ComponentException {
		return this.addMovimentoCoge(userContext, scritturaPartitaDoppia, doccoge, aTipoDett, aSezione, aCdConto, aImporto, aPartita, aCdTerzo, aCdCori, null, null);
	}

	private Movimento_cogeBulk addMovimentoCoge(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppia, IDocumentoCogeBulk doccoge, String aTipoDett, String aSezione, String aCdConto, BigDecimal aImporto, IDocumentoCogeBulk aPartita, Integer aCdTerzo, String aCdCori, Timestamp aDataDaCompetenza, Timestamp aDataACompetenza) throws ComponentException{
		try {
			if (aImporto.compareTo(BigDecimal.ZERO)==0)
				return null;

			Movimento_cogeBulk movimentoCoge = new Movimento_cogeBulk();

			ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
			ContoBulk contoBulk = (ContoBulk)contoHome.findByPrimaryKey(new ContoBulk(aCdConto, CNRUserContext.getEsercizio(userContext)));
			movimentoCoge.setToBeCreated();
			movimentoCoge.setUser(userContext.getUser());

			movimentoCoge.setConto(contoBulk);
			movimentoCoge.setIm_movimento(aImporto);
			movimentoCoge.setTerzo(
					Optional.ofNullable(aCdTerzo)
							.map(cdTerzo -> {
								try {
									return findByPrimaryKey(userContext, new TerzoBulk(cdTerzo));
								} catch (ComponentException e) {
									return handleException(e);
								}
							})
							.filter(TerzoBulk.class::isInstance)
							.map(TerzoBulk.class::cast)
							.orElse(null)
			);
			movimentoCoge.setDt_da_competenza_coge(aDataDaCompetenza);
			movimentoCoge.setDt_a_competenza_coge(aDataACompetenza);
			movimentoCoge.setStato(Movimento_cogeBulk.STATO_DEFINITIVO);
			movimentoCoge.setCd_contributo_ritenuta(aCdCori);
			movimentoCoge.setFl_modificabile(Boolean.FALSE);

			if (doccoge instanceof Fattura_passivaBulk) {
				Fattura_passivaBulk fatpas = (Fattura_passivaBulk) doccoge;

				movimentoCoge.setCd_cds(fatpas.getCd_cds_origine());
				movimentoCoge.setEsercizio(fatpas.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(fatpas.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(fatpas.getTi_istituz_commerc());
			} else if (doccoge instanceof Fattura_attivaBulk) {
				Fattura_attivaBulk fatatt = (Fattura_attivaBulk) doccoge;

				movimentoCoge.setCd_cds(fatatt.getCd_cds_origine());
				movimentoCoge.setEsercizio(fatatt.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(fatatt.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof Documento_genericoBulk) {
				Documento_genericoBulk documento_genericoBulk = (Documento_genericoBulk) doccoge;

				movimentoCoge.setCd_cds(documento_genericoBulk.getCd_cds_origine());
				movimentoCoge.setEsercizio(documento_genericoBulk.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(documento_genericoBulk.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(documento_genericoBulk.getTi_istituz_commerc());
			} else if (doccoge instanceof CompensoBulk) {
				CompensoBulk compensoBulk = (CompensoBulk) doccoge;

				movimentoCoge.setCd_cds(compensoBulk.getCd_cds_origine());
				movimentoCoge.setEsercizio(compensoBulk.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(compensoBulk.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(compensoBulk.getTi_istituz_commerc());
            } else if (doccoge instanceof OrdineAcqConsegnaBulk) {
                OrdineAcqConsegnaBulk ordineAcqConsegnaBulk = (OrdineAcqConsegnaBulk) doccoge;
                movimentoCoge.setCd_cds(ordineAcqConsegnaBulk.getCd_cds());
                movimentoCoge.setEsercizio(ordineAcqConsegnaBulk.getEsercizio());
                UnitaOperativaOrdBulk unitaOperativaOrdBulk = (UnitaOperativaOrdBulk)getHome(userContext, UnitaOperativaOrdBulk.class).findByPrimaryKey(ordineAcqConsegnaBulk.getOrdineAcqRiga().getOrdineAcq().getUnitaOperativaOrd());
                movimentoCoge.setCd_unita_organizzativa(unitaOperativaOrdBulk.getCdUnitaOrganizzativa());
                movimentoCoge.setTi_istituz_commerc(ordineAcqConsegnaBulk.getOrdineAcqRiga().getOrdineAcq().getTiAttivita());
			} else if (doccoge instanceof MandatoBulk) {
				MandatoBulk mandatoBulk = (MandatoBulk) doccoge;

				movimentoCoge.setCd_cds(mandatoBulk.getCd_cds_origine());
				movimentoCoge.setEsercizio(mandatoBulk.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(mandatoBulk.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof ReversaleBulk) {
				ReversaleBulk reversaleBulk = (ReversaleBulk) doccoge;

				movimentoCoge.setCd_cds(reversaleBulk.getCd_cds_origine());
				movimentoCoge.setEsercizio(reversaleBulk.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(reversaleBulk.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof AnticipoBulk) {
				AnticipoBulk anticipo = (AnticipoBulk) doccoge;

				movimentoCoge.setCd_cds(anticipo.getCd_cds_origine());
				movimentoCoge.setEsercizio(anticipo.getEsercizio());
				movimentoCoge.setCd_unita_organizzativa(anticipo.getCd_uo_origine());
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof MissioneBulk) {
				movimentoCoge.setTi_istituz_commerc(((MissioneBulk) doccoge).getTi_istituz_commerc());
			} else if (doccoge instanceof Liquidazione_ivaBulk) {
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof RimborsoBulk) {
				movimentoCoge.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			}

			if (aPartita!=null) {
				movimentoCoge.setCd_tipo_documento(aPartita.getCd_tipo_doc());
				movimentoCoge.setCd_cds_documento(aPartita.getCd_cds());
				movimentoCoge.setCd_uo_documento(aPartita.getCd_uo());
				movimentoCoge.setEsercizio_documento(aPartita.getEsercizio());
				movimentoCoge.setPg_numero_documento(aPartita.getPg_doc());
			}

			String myTipoDett = Optional.ofNullable(aTipoDett).orElse(this.getTipoDettaglioByConto(userContext, movimentoCoge.getConto()));
			movimentoCoge.setTi_riga(myTipoDett);

			if (aSezione.equals(Movimento_cogeBulk.SEZIONE_DARE))
				scritturaPartitaDoppia.addToMovimentiDareColl(movimentoCoge);
			else
				scritturaPartitaDoppia.addToMovimentiAvereColl(movimentoCoge);
/*
			logger.info("TipoRiga: " + myTipoDett + " - Conto: " + aCdConto + " - Sezione: " + aSezione + " - Importo: " + aImporto +
					(aPartita!=null?" - Partita: " +
							aPartita.getCd_tipo_doc() + "/" + aPartita.getCd_cds() + "/" + aPartita.getCd_uo() + "/" + aPartita.getEsercizio() + "/" + aPartita.getPg_doc():""));
*/
			return movimentoCoge;
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void addMovimentoCoan(UserContext userContext, Scrittura_analiticaBulk scritturaAnalitica, IDocumentoCogeBulk doccoge, Movimento_cogeBulk movimentoCoge, String aSezione, String aCdContoAna, String aCdCentroCosto, String aCdLineaAttivita, BigDecimal aImporto) throws ComponentException{
		try {
			if (aImporto.compareTo(BigDecimal.ZERO)==0)
				return;

			Movimento_coanBulk movimentoCoan = new Movimento_coanBulk();

			Voce_analiticaHome voceAnaliticaHome = (Voce_analiticaHome) getHome(userContext, Voce_analiticaBulk.class);
			Voce_analiticaBulk voceAnaliticaBulk = (Voce_analiticaBulk)voceAnaliticaHome.findByPrimaryKey(new Voce_analiticaBulk(aCdContoAna, CNRUserContext.getEsercizio(userContext)));
			movimentoCoan.setToBeCreated();
			movimentoCoan.setUser(userContext.getUser());

			movimentoCoan.setVoceAnalitica(voceAnaliticaBulk);
			movimentoCoan.setIm_movimento(aImporto);
			movimentoCoan.setStato(Movimento_cogeBulk.STATO_DEFINITIVO);
			movimentoCoan.setDs_movimento("Movimento Coan");

			WorkpackageBulk lineaAttivita = ((WorkpackageHome)getHome(userContext, WorkpackageBulk.class)).searchGAECompleta(userContext,CNRUserContext.getEsercizio(userContext), aCdCentroCosto, aCdLineaAttivita);
			movimentoCoan.setLatt(lineaAttivita);

			movimentoCoan.setSezione(aSezione);
			movimentoCoan.setCd_voce_ana(aCdContoAna);

			movimentoCoan.setPg_numero_documento(doccoge.getPg_doc());

			if (doccoge instanceof Fattura_passivaBulk) {
				Fattura_passivaBulk fatpas = (Fattura_passivaBulk) doccoge;

				movimentoCoan.setCd_cds(fatpas.getCd_cds_origine());
				movimentoCoan.setEsercizio(fatpas.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(fatpas.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(fatpas.getTi_istituz_commerc());
			} else if (doccoge instanceof Fattura_attivaBulk) {
				Fattura_attivaBulk fatatt = (Fattura_attivaBulk) doccoge;

				movimentoCoan.setCd_cds(fatatt.getCd_cds_origine());
				movimentoCoan.setEsercizio(fatatt.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(fatatt.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof Documento_genericoBulk) {
				Documento_genericoBulk documento_genericoBulk = (Documento_genericoBulk) doccoge;

				movimentoCoan.setCd_cds(documento_genericoBulk.getCd_cds_origine());
				movimentoCoan.setEsercizio(documento_genericoBulk.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(documento_genericoBulk.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(documento_genericoBulk.getTi_istituz_commerc());
            } else if (doccoge instanceof OrdineAcqConsegnaBulk) {
                OrdineAcqConsegnaBulk ordineAcqConsegnaBulk = (OrdineAcqConsegnaBulk) doccoge;
                movimentoCoan.setCd_cds(ordineAcqConsegnaBulk.getCd_cds());
                movimentoCoan.setEsercizio(ordineAcqConsegnaBulk.getEsercizio());
                UnitaOperativaOrdBulk unitaOperativaOrdBulk = (UnitaOperativaOrdBulk)getHome(userContext, UnitaOperativaOrdBulk.class).findByPrimaryKey(ordineAcqConsegnaBulk.getOrdineAcqRiga().getOrdineAcq().getUnitaOperativaOrd());
                movimentoCoan.setCd_unita_organizzativa(unitaOperativaOrdBulk.getCdUnitaOrganizzativa());
                movimentoCoan.setTi_istituz_commerc(ordineAcqConsegnaBulk.getOrdineAcqRiga().getOrdineAcq().getTiAttivita());
			} else if (doccoge instanceof CompensoBulk) {
				CompensoBulk compensoBulk = (CompensoBulk) doccoge;

				movimentoCoan.setCd_cds(compensoBulk.getCd_cds_origine());
				movimentoCoan.setEsercizio(compensoBulk.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(compensoBulk.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(compensoBulk.getTi_istituz_commerc());
			} else if (doccoge instanceof MandatoBulk) {
				MandatoBulk mandatoBulk = (MandatoBulk) doccoge;

				movimentoCoan.setCd_cds(mandatoBulk.getCd_cds_origine());
				movimentoCoan.setEsercizio(mandatoBulk.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(mandatoBulk.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof ReversaleBulk) {
				ReversaleBulk reversaleBulk = (ReversaleBulk) doccoge;

				movimentoCoan.setCd_cds(reversaleBulk.getCd_cds_origine());
				movimentoCoan.setEsercizio(reversaleBulk.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(reversaleBulk.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof AnticipoBulk) {
				AnticipoBulk anticipo = (AnticipoBulk) doccoge;

				movimentoCoan.setCd_cds(anticipo.getCd_cds_origine());
				movimentoCoan.setEsercizio(anticipo.getEsercizio());
				movimentoCoan.setCd_unita_organizzativa(anticipo.getCd_uo_origine());
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof MissioneBulk) {
				movimentoCoan.setTi_istituz_commerc(((MissioneBulk) doccoge).getTi_istituz_commerc());
			} else if (doccoge instanceof Liquidazione_ivaBulk) {
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			} else if (doccoge instanceof RimborsoBulk) {
				movimentoCoan.setTi_istituz_commerc(TipoIVA.ISTITUZIONALE.value());
			}

			movimentoCoan.setMovimentoCoge(movimentoCoge);
			scritturaAnalitica.addToMovimentiColl(movimentoCoan);
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private Pair<String, BigDecimal> getSaldo(List<DettaglioPrimaNota> dettaglioPrimaNotaList) {
		BigDecimal totaleDare = dettaglioPrimaNotaList.stream()
				.filter(DettaglioPrimaNota::isSezioneDare)
				.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totaleAvere = dettaglioPrimaNotaList.stream()
				.filter(DettaglioPrimaNota::isSezioneAvere)
				.map(DettaglioPrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal saldo = totaleDare.subtract(totaleAvere);
		if (saldo.compareTo(BigDecimal.ZERO)>=0)
			return Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo);
		else
			return Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs());
	}

	private Pair<String, BigDecimal> getSaldoAnalitico(List<DettaglioAnalitico> dettaglioAnaliticoList) {
		BigDecimal totaleDare = dettaglioAnaliticoList.stream()
				.filter(el->el.getDettaglioPrimaNota().isSezioneDare())
				.map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totaleAvere = dettaglioAnaliticoList.stream()
				.filter(el->el.getDettaglioPrimaNota().isSezioneAvere())
				.map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal saldo = totaleDare.subtract(totaleAvere);
		if (saldo.compareTo(BigDecimal.ZERO)>=0)
			return Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo);
		else
			return Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs());
	}

	private List<IDocumentoAmministrativoRigaBulk> getRigheDocamm(UserContext userContext, IDocumentoAmministrativoBulk docamm) {
		try {
			if (Optional.ofNullable(docamm.getChildren()).orElse(Collections.EMPTY_LIST).isEmpty())  {
				List<IDocumentoAmministrativoRigaBulk> result;
				if (docamm instanceof Documento_genericoBulk) {
					Documento_genericoHome home = (Documento_genericoHome) getHome(userContext, Documento_genericoBulk.class);
					result = home.findDocumentoGenericoRigheList((Documento_genericoBulk) docamm);
					result.stream().map(Documento_generico_rigaBulk.class::cast).forEach(el->el.setDocumento_generico((Documento_genericoBulk) docamm));
					((Documento_genericoBulk)docamm).setDocumento_generico_dettColl(new BulkList(result));
				} else if (docamm instanceof Fattura_passivaBulk) {
					result = Utility.createFatturaPassivaComponentSession().findDettagli(userContext, (Fattura_passivaBulk) docamm);
					result.stream().map(Fattura_passiva_rigaBulk.class::cast).forEach(el->el.setFattura_passiva((Fattura_passivaBulk) docamm));
					((Fattura_passivaBulk)docamm).setFattura_passiva_dettColl(new BulkList(result));
				} else if (docamm instanceof Fattura_attivaBulk) {
					result = Utility.createFatturaAttivaSingolaComponentSession().findDettagli(userContext, (Fattura_attivaBulk) docamm);
					result.stream().map(Fattura_attiva_rigaBulk.class::cast).forEach(el->el.setFattura_attiva((Fattura_attivaBulk) docamm));
					((Fattura_attivaBulk) docamm).setFattura_attiva_dettColl(new BulkList(result));
				} else
					throw new ApplicationException("Scrittura Economica non possibile. Non risulta gestito il recupero delle righe di dettaglio di un documento di tipo "+ docamm.getCd_tipo_doc()+".");
				return result;
			}

			//Carico i dettagli economici
			docamm.getChildren().stream().filter(IDocumentoAmministrativoRigaBulk.class::isInstance)
					.forEach(el -> this.loadChildrenAna(userContext, (IDocumentoAmministrativoRigaBulk)el));

			return docamm.getChildren();
		} catch (ComponentException | PersistencyException | RemoteException | IntrospectionException e) {
			throw new ApplicationRuntimeException(e);
		}
	}

	private void completeMandato(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException {
		//Se le righe del mandato non sono valorizzate le riempio io
		if (!Optional.ofNullable(mandato.getMandato_rigaColl()).filter(el->!el.isEmpty()).isPresent()) {
			mandato.setMandato_rigaColl(new BulkList(((MandatoHome) getHome(
					userContext, mandato.getClass())).findMandato_riga(userContext, mandato, false)));
			mandato.getMandato_rigaColl().forEach(el->{
				try {
					el.setMandato(mandato);
					((Mandato_rigaHome)getHome(userContext, Mandato_rigaBulk.class)).initializeElemento_voce(userContext, el);
				} catch (ComponentException|PersistencyException e) {
					throw new ApplicationRuntimeException(e);
				}
			});
		}

		if (!Optional.ofNullable(mandato.getMandato_terzo()).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).isPresent())
			mandato.setMandato_terzo(((MandatoHome) getHome(userContext, mandato.getClass())).findMandato_terzo(userContext, mandato, false));

		if (!Optional.ofNullable(mandato.getUnita_organizzativa()).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).isPresent())
			mandato.setUnita_organizzativa((Unita_organizzativaBulk)getHome(userContext, Unita_organizzativaBulk.class).findByPrimaryKey(mandato.getUnita_organizzativa()));
	}

	private TestataPrimaNota proposeTestataPrimaNotaReversaleAccantonamentoCori(UserContext userContext, ReversaleBulk reversale) throws ComponentException, PersistencyException, RemoteException {
		String keyReversale = reversale.getEsercizio() + "/" + reversale.getCd_cds() + "/" + reversale.getPg_reversale();
		if (reversale.getReversale_rigaColl().stream().map(Reversale_rigaBulk::getCd_tipo_documento_amm).noneMatch(el->TipoDocumentoEnum.fromValue(el).isGenericoCoriAccantonamentoEntrata()))
			throw new ApplicationException("La riga della reversale " + keyReversale + " non risulta incassare un versamento/accantonamento cori. Proposta di prima nota non possibile.");

		if (reversale.getReversale_rigaColl().size()>1)
			throw new ApplicationException("Reversale " + keyReversale + " di versamento/accantonamento cori con più di una riga. Proposta di prima nota non possibile.");

		Reversale_rigaBulk rigaReversale = reversale.getReversale_rigaColl().stream().findAny()
				.orElseThrow(()->new ApplicationException("Reversale " + keyReversale + " di versamento/accantonamento cori senza righe. Proposta di prima nota non possibile."));

		BigDecimal imReversale = rigaReversale.getIm_reversale_riga();

		Voce_epBulk voceEpBanca = this.findContoBanca(userContext, reversale.getReversale_rigaColl().get(0));
		Voce_epBulk aContoRicavo = this.findContoCostoRicavo(userContext, rigaReversale.getElemento_voce());

		TestataPrimaNota testataPrimaNota = new TestataPrimaNota();
		testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.RICAVO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aContoRicavo, imReversale);
		testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.TESORERIA.value(), Movimento_cogeBulk.SEZIONE_DARE, voceEpBanca, imReversale);

		return testataPrimaNota;
	}

	/**
	 * Ritorna la lista dei conti Iva aperti presenti nella scrittura partita doppia indicata
	 *
	 * @param scritturaPartitaDoppiaBulk - la scrittura partita doppia
	 * @return Map<String, Map<String, Pair<String, BigDecimal>>> mappa con i saldi raggruppati per CdCori e VoceEp
	 */
	private Map<String, Map<String, Pair<String, BigDecimal>>> getSaldiMovimentiCoriIva(Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk) {
		return this.getMapCdCoriVoceEp(scritturaPartitaDoppiaBulk.getAllMovimentiColl()
				.stream()
				.filter(Movimento_cogeBulk::isRigaTipoIva)
				.collect(Collectors.toList()));
	}

	/**
	 *
	 * Ritorna la lista dei conti Iva Debito aperti dalla liquidazione UO
	 *
	 * @param userContext userContext
	 * @param liquidIva - la liquidazione della UO
	 * @return Map<String, Map<String, Pair<String, BigDecimal>>> mappa con i saldi raggruppati per VoceEp
	 * @throws ComponentException ComponentException
	 * @throws PersistencyException PersistencyException
	 */
	private Map<String, Pair<String, BigDecimal>> getSaldiMovimentiCoriIvaDebitoEnte(UserContext userContext, Liquidazione_ivaBulk liquidIva) throws ComponentException, PersistencyException {
		Optional<Scrittura_partita_doppiaBulk> scritturaOpt = this.getScritturaPartitaDoppia(userContext, liquidIva, Boolean.TRUE);

		return this.getMapVoceEp(scritturaOpt.map(spd->spd.getAllMovimentiColl()
				.stream()
				.filter(el->el.getTi_riga().equals(Movimento_cogeBulk.TipoRiga.DEBITO.value()))
				.filter(el->!Optional.ofNullable(el.getPartita()).isPresent())
				.collect(Collectors.toList())).orElse(Collections.EMPTY_LIST));
	}

	private Optional<Scrittura_partita_doppiaBulk> getScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk docamm) throws ComponentException, PersistencyException {
		return getScritturaPartitaDoppia(userContext, docamm, Boolean.FALSE);
	}

	private Optional<Scrittura_partita_doppiaBulk> getScritturaPartitaDoppia(UserContext userContext, IDocumentoCogeBulk docamm, boolean createIfNotExist) throws ComponentException, PersistencyException {
		Optional<Scrittura_partita_doppiaBulk> scritturaOpt = Optional.empty();

		boolean isAttivaEconomicaDocamm = ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).isAttivaEconomica(docamm.getEsercizio());
		if (isAttivaEconomicaDocamm)
			scritturaOpt = ((Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class)).getScrittura(userContext, docamm, Boolean.FALSE);

		//Lancio la proposta di prima nota se non attivaEconomica (!isAttivaEconomicaDocamm) o, in caso contrario, se non generata ma viene richiesto di farlo (createIfNotExist && !scritturaOpt.isPresent())
		if (!isAttivaEconomicaDocamm || (createIfNotExist && !scritturaOpt.isPresent())) {
			try {
				scritturaOpt = Optional.ofNullable(innerProposeScritturaPartitaDoppia(userContext, docamm));
			} catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException ignored) {
			} catch (ComponentException e) {
				throw new ApplicationRuntimeException(e);
			}

			//Se viene richiesto di generare la proposta provvedo
			if (isAttivaEconomicaDocamm && scritturaOpt.isPresent()) {
				creaConBulk(userContext, scritturaOpt.get());
				docamm.setStato_coge(Fattura_passivaBulk.REGISTRATO_IN_COGE);
				((OggettoBulk) docamm).setToBeUpdated();
				updateBulk(userContext, (OggettoBulk) docamm);
			}
		}

		return scritturaOpt;
	}

	/**
	 * Ritorna una mappa in cui raggruppa i movimenti coge per CodiceContributo e Voce
	 */
	private Map<String, Map<String, Pair<String, BigDecimal>>> getMapCdCoriVoceEp(Collection<Movimento_cogeBulk> allMovimentiCoge) {
		Map<String, Map<String, Pair<String, BigDecimal>>> result = new HashMap<>();

		Map<String, Map<String, List<Movimento_cogeBulk>>> mapCdCoriVoceEp = allMovimentiCoge.stream()
				.collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_contributo_ritenuta,
						Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep)));

		mapCdCoriVoceEp.keySet().forEach(cdCori -> {
			Map<String, List<Movimento_cogeBulk>> mapVoceEp = mapCdCoriVoceEp.get(cdCori);
			mapVoceEp.keySet().forEach(cdVoceEp -> {
				List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
				BigDecimal totaleDare = movimentiList.stream()
						.filter(Movimento_cogeBulk::isSezioneDare)
						.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal totaleAvere = movimentiList.stream()
						.filter(Movimento_cogeBulk::isSezioneAvere)
						.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal saldo = totaleDare.subtract(totaleAvere);

				Map<String, Pair<String, BigDecimal>> resultBis = Optional.ofNullable(result.get(cdCori)).orElse(new HashMap<>());
				if (saldo.compareTo(BigDecimal.ZERO) >= 0)
					resultBis.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
				else
					resultBis.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
				result.put(cdCori, resultBis);
			});
		});
		return result;
	}

	/**
	 * Ritorna una mappa in cui raggruppa i movimenti coge per Voce
	 */
	private Map<String, Pair<String, BigDecimal>> getMapVoceEp(Collection<Movimento_cogeBulk> allMovimentiCoge) {
		Map<String, Pair<String, BigDecimal>> result = new HashMap<>();

		Map<String, List<Movimento_cogeBulk>> mapVoceEp = allMovimentiCoge.stream()
				.collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep));

		mapVoceEp.keySet().forEach(cdVoceEp -> {
			List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
			BigDecimal totaleDare = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneDare)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal totaleAvere = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneAvere)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal saldo = totaleDare.subtract(totaleAvere);
			if (saldo.compareTo(BigDecimal.ZERO) >= 0)
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
			else
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
		});

		return result;
	}

	public List<Contributo_ritenutaBulk> findCoriReversaliAssociate(UserContext userContext, MandatoBulk mandato) throws ComponentException, PersistencyException {
		//cerco di recuperare puntualmente la ritenuta
		List<Contributo_ritenutaBulk> righeCoriReversale = new ArrayList<>();
		List<Ass_mandato_reversaleBulk> result = ((Ass_mandato_reversaleHome) getHome(userContext, Ass_mandato_reversaleBulk.class)).findReversali(userContext, mandato, false);
		for (Ass_mandato_reversaleBulk assManRev : result) {
			ReversaleIHome revHome = (ReversaleIHome) getHome(userContext, ReversaleIBulk.class);
			ReversaleBulk reversale = (ReversaleIBulk) revHome.findByPrimaryKey(new ReversaleIBulk(assManRev.getCd_cds_reversale(), assManRev.getEsercizio_reversale(), assManRev.getPg_reversale()));

			//riempio le righe della reversale
			reversale.setReversale_rigaColl(new BulkList(((ReversaleHome) getHome(
					userContext, reversale.getClass())).findReversale_riga(userContext, reversale, false)));
			reversale.getReversale_rigaColl().forEach(el->{
				try {
					el.setReversale(reversale);
					((Reversale_rigaHome)getHome(userContext, Reversale_rigaBulk.class)).initializeElemento_voce(userContext, el);
				} catch (ComponentException|PersistencyException e) {
					throw new ApplicationRuntimeException(e);
				}
			});

			Collection<V_doc_cont_compBulk> result2 = ((V_doc_cont_compHome) getHomeCache(userContext).getHome(V_doc_cont_compBulk.class)).findByDocumento(assManRev.getEsercizio_reversale(), assManRev.getCd_cds_reversale(), assManRev.getPg_reversale(), V_doc_cont_compBulk.TIPO_DOC_CONT_REVERSALE);

			if (!result2.isEmpty()) {
				V_doc_cont_compBulk docContCompBulk = result2.stream().findAny().get();
				final CompensoBulk compensoReversale = new CompensoBulk(docContCompBulk.getCd_cds_compenso(), docContCompBulk.getCd_uo_compenso(), docContCompBulk.getEsercizio_compenso(), docContCompBulk.getPg_compenso());

                List<Contributo_ritenutaBulk> righeCoriCompensoReversale = Optional.ofNullable(compensoReversale.getChildren()).orElseGet(() -> {
					try {
						Contributo_ritenutaHome home = (Contributo_ritenutaHome) getHome(userContext, Contributo_ritenutaBulk.class);
						return (List<Contributo_ritenutaBulk>) home.loadContributiRitenute(compensoReversale);
					} catch (ComponentException | PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				});

				//Cerco tra le righeCori quella della reversale.... cerco per importo... e se ne esistono di più per descrizione..... se non si individua lancio eccezione
				List<Contributo_ritenutaBulk> righeCoriReversaleByImporto = righeCoriCompensoReversale.stream()
						.filter(el -> el.getAmmontare().compareTo(BigDecimal.ZERO) > 0)
						.filter(el -> el.getAmmontare().abs().compareTo(reversale.getIm_reversale()) == 0 ||
								reversale.getReversale_rigaColl().stream()
										.anyMatch(riga->riga.getIm_reversale_riga().compareTo(el.getAmmontare())==0))
						.collect(Collectors.toList());

				if (righeCoriReversaleByImporto.isEmpty())
					throw new ApplicationException("Il " + this.getDescrizione(mandato) + " risulta avere una ritenuta di importo che non corrisponde a nessuna delle ritenute " +
							" generate dal compenso associato alla reversale " + this.getDescrizione(compensoReversale) + ". Proposta di prima nota non possibile.");
				if (righeCoriReversaleByImporto.size() != reversale.getReversale_rigaColl().size()) {
					List<Contributo_ritenutaBulk> righeCoriReversaleByDesc = righeCoriReversaleByImporto.stream()
							.filter(el -> reversale.getDs_reversale().contains(el.getCd_contributo_ritenuta()) ||
									reversale.getReversale_rigaColl().stream()
											.anyMatch(riga->Optional.ofNullable(riga.getDs_reversale_riga())
													.map(ds->ds.contains(el.getCd_contributo_ritenuta())).orElse(Boolean.FALSE)))
							.collect(Collectors.toList());
					//se necessario occorrerà andare a leggere anche la descrizione dei documenti generici della reversale
					if (righeCoriReversaleByDesc.size() != reversale.getReversale_rigaColl().size())
						throw new ApplicationException("Il " + this.getDescrizione(mandato) + " risulta avere una ritenuta di importo che corrisponde a più ritenute " +
								" generate dal conguaglio associato " + this.getDescrizione(compensoReversale) + ". Proposta di prima nota non possibile.");
					righeCoriReversale.addAll(righeCoriReversaleByDesc);
				} else
					righeCoriReversale.addAll(righeCoriReversaleByImporto);
			}
		}
		return righeCoriReversale;
	}

	private String getDescrizione(MandatoBulk mandato) {
		return mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_mandato();
	}

	private String getDescrizione(CompensoBulk compenso){
		return compenso.getEsercizio() + "/" + compenso.getCd_cds() + "/" + compenso.getPg_compenso();
	}

	/**
	 * Compute the number of calendar days between two Calendar objects.
	 * The desired value is the number of days of the month between the
	 * two Calendars, not the number of milliseconds' worth of days.
	 * @param startCal The earlier calendar
	 * @param endCal The later calendar
	 * @return the number of calendar days of the month between startCal and endCal
	 */
	private static long calendarDaysBetween(Calendar startCal, Calendar endCal) {
		// Create copies so we don't update the original calendars.
		Calendar start = Calendar.getInstance();
		start.setTimeZone(startCal.getTimeZone());
		start.setTimeInMillis(startCal.getTimeInMillis());

		Calendar end = Calendar.getInstance();
		end.setTimeZone(endCal.getTimeZone());
		end.setTimeInMillis(endCal.getTimeInMillis());

		// Set the copies to be at midnight, but keep the day information.

		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);

		end.set(Calendar.HOUR_OF_DAY, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);

		// At this point, each calendar is set to midnight on
		// their respective days. Now use TimeUnit.MILLISECONDS to
		// compute the number of full days between the two of them.

		return TimeUnit.MILLISECONDS.toDays(
				Math.abs(end.getTimeInMillis() - start.getTimeInMillis()));
	}

	private void addMovimentiCoan(UserContext userContext, Scrittura_analiticaBulk scritturaAnalitica, IDocumentoCogeBulk doccoge, Movimento_cogeBulk movimentoCoge, List<DettaglioPrimaNota> dettaglioPrimaNotaList) throws ComponentException {
		List<DettaglioAnalitico> dettagliAnalitici = new ArrayList<>();
		dettaglioPrimaNotaList.stream()
				.filter(el->el.getDettaglioFinanziario()!=null &&
						el.getDettaglioFinanziario().getDettagliAnalitici()!=null)
				.forEach(dettaglioPrimaNota->{
			BigDecimal totDettaglioFinanziario = dettaglioPrimaNota.getDettaglioFinanziario().getDettagliAnalitici()
					.stream().map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

			List<DettaglioAnalitico> innerDettagliAnalitici = new ArrayList<>();
			dettaglioPrimaNota
					.getDettaglioFinanziario()
					.getDettagliAnalitici()
					.stream()
					.filter(dettaglioAnalitico -> totDettaglioFinanziario.compareTo(BigDecimal.ZERO) != 0)
					.forEach(el-> {
						BigDecimal newImporto = dettaglioPrimaNota.getImporto().multiply(el.getImporto()).divide(totDettaglioFinanziario, 2, RoundingMode.HALF_UP);
						Voce_analiticaBulk voceAnaliticaDef = el.getVoceAnalitica();
						if (Optional.ofNullable(voceAnaliticaDef).isPresent())
							innerDettagliAnalitici.add(new DettaglioAnalitico(voceAnaliticaDef, el.getCdCentroCosto(), el.getCdLineaAttivita(), newImporto, dettaglioPrimaNota));
			});
			BigDecimal totInnerDettagliAnalitici = innerDettagliAnalitici.stream().map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (totInnerDettagliAnalitici.compareTo(dettaglioPrimaNota.getImporto())!=0)
				innerDettagliAnalitici.stream().max(Comparator.comparing(DettaglioAnalitico::getImporto))
						.ifPresent(el->el.setImporto(el.getImporto().add(dettaglioPrimaNota.getImporto().subtract(totInnerDettagliAnalitici))));
			dettagliAnalitici.addAll(innerDettagliAnalitici);
		});
		//Poi analizzo i conti COSTO/RICAVO distinguendoli tra modificabili e non
		{
			Map<String, Map<String, Map<String, List<DettaglioAnalitico>>>> mapTipoDettCostoRicavo = dettagliAnalitici.stream()
					.collect(Collectors.groupingBy(DettaglioAnalitico::getCdCentroCosto,
							Collectors.groupingBy(DettaglioAnalitico::getCdLineaAttivita,
									Collectors.groupingBy(el->el.getVoceAnalitica().getCd_voce_ana()))));

			mapTipoDettCostoRicavo.keySet().forEach(aCdCentroCosto -> {
				Map<String, Map<String, List<DettaglioAnalitico>>> mapCdLineaAttivita = mapTipoDettCostoRicavo.get(aCdCentroCosto);
				mapCdLineaAttivita.keySet().forEach(aCdLineaAttivita -> {
					Map<String, List<DettaglioAnalitico>> mapCdVoceAna = mapCdLineaAttivita.get(aCdLineaAttivita);
					mapCdVoceAna.keySet().forEach(aCdVoceAna -> {
						try {
							Pair<String, BigDecimal> saldoVoceAnalitica = this.getSaldoAnalitico(mapCdVoceAna.get(aCdVoceAna));
							addMovimentoCoan(userContext, scritturaAnalitica, doccoge, movimentoCoge, saldoVoceAnalitica.getFirst(), aCdVoceAna, aCdCentroCosto, aCdLineaAttivita, saldoVoceAnalitica.getSecond());
						} catch (ComponentException e) {
							throw new ApplicationRuntimeException(e);
						}
					});
				});
			});
		}
	}

	private String getTipoDettaglioByConto(UserContext userContext, Voce_epBulk conto) {
		Voce_epBulk myConto = (Voce_epBulk) this.loadObject(userContext, conto);

		String myTipoDettaglio;
		if (myConto.isContoCostoEconomicoEsercizio())
			myTipoDettaglio = Movimento_cogeBulk.TipoRiga.COSTO.value();
		else if (myConto.isContoRicavoEconomicoEsercizio())
			myTipoDettaglio = Movimento_cogeBulk.TipoRiga.RICAVO.value();
		else if (myConto.isContoNumerarioAttivita())
			myTipoDettaglio = Movimento_cogeBulk.TipoRiga.ATTIVITA.value();
		else if (myConto.isContoNumerarioPassivita())
			myTipoDettaglio = Movimento_cogeBulk.TipoRiga.PASSIVITA.value();
		else
			myTipoDettaglio = Movimento_cogeBulk.TipoRiga.GENERICO.value();

		return myTipoDettaglio;
	}

	private Pair<Voce_epBulk, Voce_epBulk> findPairCostoRigaDocumento(UserContext userContext, DettaglioFinanziario rigaDettFin, Pair<Voce_epBulk, Voce_epBulk> pairContoCostoGen) {
		try {
			if (pairContoCostoGen!=null)
				return pairContoCostoGen;
			else
				return this.findPairCosto(userContext, rigaDettFin);
		} catch (ComponentException | RemoteException | PersistencyException e) {
			throw new ApplicationRuntimeException(e);
		}
	}

	private DettaglioFinanziario convertToRigaDettFin(UserContext userContext, IDocumentoDetailEcoCogeBulk rigaDocAmm) throws ApplicationException {
		//Attenzione: recupero il terzo dal docamm perchè sulle righe potrebbe non essere valorizzato
		TerzoBulk terzo;
        IDocumentoCogeBulk docamm = rigaDocAmm.getFather();
		IDocumentoDetailEcoCogeBulk rigaPartita = null;
		if (docamm instanceof Fattura_passivaBulk) {
			terzo = ((Fattura_passivaBulk) docamm).getFornitore();
			if (rigaDocAmm instanceof Nota_di_credito_rigaBulk)
				rigaPartita = Optional.of(rigaDocAmm)
						.map(Nota_di_credito_rigaBulk.class::cast)
						.flatMap(notaDiCreditoRigaBulk -> Optional.ofNullable(notaDiCreditoRigaBulk.getRiga_fattura_origine()))
						.orElse(null);
		} else if (Optional.ofNullable(docamm)
				.filter(Documento_genericoBulk.class::isInstance)
				.map(Documento_genericoBulk.class::cast)
				.map(Documento_genericoBulk::isDocumentoStorno)
				.orElse(Boolean.FALSE)) {
			terzo = ((IDocumentoAmministrativoRigaBulk)rigaDocAmm).getTerzo();
			rigaPartita = Optional.of(rigaDocAmm)
					.filter(Documento_generico_rigaBulk.class::isInstance)
					.map(Documento_generico_rigaBulk.class::cast)
					.flatMap(Documento_generico_rigaBulk::getRigaStorno)
					.orElse(null);
		} else if (docamm instanceof Fattura_attivaBulk) {
			terzo = ((Fattura_attivaBulk) docamm).getCliente();
			if (rigaDocAmm instanceof Nota_di_credito_attiva_rigaBulk)
				rigaPartita = Optional.of(rigaDocAmm)
						.map(Nota_di_credito_attiva_rigaBulk.class::cast)
						.flatMap(notaDiCreditoRigaBulk -> Optional.ofNullable(notaDiCreditoRigaBulk.getRiga_fattura_associata()))
						.orElse(null);
		} else if (docamm instanceof OrdineAcqBulk)
			terzo = ((OrdineAcqBulk) docamm).getFornitore();
		else {
			terzo = ((IDocumentoAmministrativoRigaBulk)rigaDocAmm).getTerzo();
			if (TipoDocumentoEnum.fromValue(docamm.getCd_tipo_doc()).isDocumentoGenericoAttivo()) {
				((Documento_genericoBulk) docamm).setTi_entrate_spese(Documento_genericoBulk.ENTRATE);
				((Documento_generico_rigaBulk) rigaDocAmm).setDocumento_generico((Documento_genericoBulk)docamm);
			} else if (TipoDocumentoEnum.fromValue(docamm.getCd_tipo_doc()).isDocumentoGenericoPassivo()) {
				((Documento_genericoBulk) docamm).setTi_entrate_spese(Documento_genericoBulk.SPESE);
				((Documento_generico_rigaBulk) rigaDocAmm).setDocumento_generico((Documento_genericoBulk)docamm);
			}
		}
		//Setto la rigaPartita con la riga del documento selezionato se non valorizzato
		rigaPartita = (IDocumentoDetailEcoCogeBulk) this.loadObject(userContext, (OggettoBulk)Optional.ofNullable(rigaPartita).orElse(rigaDocAmm));

		List<DettaglioAnalitico> dettagliAnalitici = new ArrayList<>();
		//Carico i dettagli economici
		this.loadChildrenAna(userContext, rigaDocAmm);

		for (IDocumentoDetailAnaCogeBulk rigaAna : rigaDocAmm.getChildrenAna())
			dettagliAnalitici.add(new DettaglioAnalitico(rigaAna));

		if (rigaDocAmm.getVoce_ep()==null || rigaDocAmm.getVoce_ep().getCd_voce_ep()==null)
			throw new ApplicationException("Riga documento senza indicazione del conto di economica. Scrittura economica non possibile.");

		return new DettaglioFinanziario(rigaDocAmm, rigaPartita, terzo.getCd_terzo(), rigaDocAmm.getVoce_ep(), dettagliAnalitici);
	}

	private void completeWithDatiAnalitici(UserContext userContext, DettaglioPrimaNota dettaglioPrimaNota, IDocumentoDetailEcoCogeBulk docamm, List<DettaglioAnalitico> dettagliAnaliticos, boolean firstByImporto) {
		List<DettaglioAnalitico> newDettagliAnalitici = new ArrayList<>();

		if (firstByImporto)
			dettagliAnaliticos.stream()
					.filter(rigaAna->Optional.ofNullable(rigaAna.getImportoCollegato()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO)==0)
					.filter(rigaAna->rigaAna.getImporto().compareTo(dettaglioPrimaNota.getImporto())==0)
					.findAny()
					.ifPresent(dettAna->{
						newDettagliAnalitici.add(new DettaglioAnalitico(dettAna));
						dettAna.setImportoCollegato(dettAna.getImporto());
					});
		if (newDettagliAnalitici.isEmpty()) {
			BigDecimal totAna = dettagliAnaliticos.stream().map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

			dettagliAnaliticos.stream()
					.filter(rigaAna->Optional.ofNullable(rigaAna.getImportoCollegato()).orElse(BigDecimal.ZERO).compareTo(dettaglioPrimaNota.getImporto())!=0)
					.findAny()
					.ifPresent(dettAna->{
						BigDecimal newImportoAna = dettAna.getImporto().multiply(dettaglioPrimaNota.getImporto()).divide(totAna, 2, RoundingMode.HALF_UP);
						if (newImportoAna.compareTo(dettAna.getImporto().subtract(dettAna.getImportoCollegato()))>0)
							newImportoAna = dettAna.getImporto().subtract(dettAna.getImportoCollegato());
						dettAna.setImportoCollegato(dettAna.getImportoCollegato().add(newImportoAna));

						DettaglioAnalitico dettAnaNew = new DettaglioAnalitico(dettAna);
						dettAnaNew.setImporto(newImportoAna);
						newDettagliAnalitici.add(dettAnaNew);
					});
			BigDecimal totAnaNew = newDettagliAnalitici.stream().map(DettaglioAnalitico::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (totAnaNew.compareTo(dettaglioPrimaNota.getImporto())>0) {
				//Devo togliere
				BigDecimal impDaSottrarre = totAnaNew.subtract(dettaglioPrimaNota.getImporto());
				for (DettaglioAnalitico dett : newDettagliAnalitici) {
					if (dett.getImporto().compareTo(impDaSottrarre)>0) {
						dett.setImporto(dett.getImporto().subtract(impDaSottrarre));
						dett.getDettaglioAnalitico().setImportoCollegato(dett.getDettaglioAnalitico().getImportoCollegato().subtract(impDaSottrarre));
						impDaSottrarre = BigDecimal.ZERO;
					} else if (dett.getImporto().compareTo(impDaSottrarre)<=0) {
						impDaSottrarre = impDaSottrarre.subtract(dett.getImporto());
						dett.getDettaglioAnalitico().setImportoCollegato(dett.getDettaglioAnalitico().getImportoCollegato().subtract(dett.getImporto()));
						newDettagliAnalitici.remove(dett);
					}
					if (impDaSottrarre.compareTo(BigDecimal.ZERO)<=0)
						break;
				}
			} else if (totAnaNew.compareTo(dettaglioPrimaNota.getImporto())<0) {
				//Devo aggiungere
				BigDecimal impDaAggiungere = dettaglioPrimaNota.getImporto().subtract(totAnaNew);
				for (DettaglioAnalitico dett : newDettagliAnalitici) {
					BigDecimal impUtile = dett.getImporto().subtract(dett.getImportoCollegato());
					if (impUtile.compareTo(impDaAggiungere) > 0) {
						dett.setImporto(dett.getImporto().add(impDaAggiungere));
						dett.getDettaglioAnalitico().setImportoCollegato(dett.getDettaglioAnalitico().getImportoCollegato().add(impDaAggiungere));
						impDaAggiungere = BigDecimal.ZERO;
					} else if (impUtile.compareTo(impDaAggiungere) <= 0) {
						dett.setImporto(dett.getImporto().add(impUtile));
						dett.getDettaglioAnalitico().setImportoCollegato(dett.getDettaglioAnalitico().getImportoCollegato().add(impUtile));
						impDaAggiungere = impDaAggiungere.subtract(impUtile);
					}
					if (impDaAggiungere.compareTo(BigDecimal.ZERO)<=0)
						break;
				}
			}
		}
        if (docamm instanceof IDocumentoCogeBulk)
    		dettaglioPrimaNota.setDettaglioFinanziario(new DettaglioFinanziario((IDocumentoCogeBulk)docamm, docamm.getVoce_ep(), docamm.getImCostoEco(), BigDecimal.ZERO, newDettagliAnalitici));
        else
            dettaglioPrimaNota.setDettaglioFinanziario(new DettaglioFinanziario(docamm.getFather(), docamm.getVoce_ep(), docamm.getImCostoEco(), BigDecimal.ZERO, newDettagliAnalitici));
	}

	private void loadChildrenAna(UserContext userContext, IDocumentoDetailEcoCogeBulk rigaEco) {
		try {
			if (rigaEco instanceof Documento_generico_rigaBulk) {
				Documento_generico_rigaHome rigaHome = (Documento_generico_rigaHome) getHome(userContext, Documento_generico_rigaBulk.class);
				((Documento_generico_rigaBulk) rigaEco).setRigheEconomica(rigaHome.findDocumentoGenericoRigheEcoList((Documento_generico_rigaBulk) rigaEco));
				((Documento_generico_rigaBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setDocumento_generico_rigaBulk((Documento_generico_rigaBulk) rigaEco));
			} else if (rigaEco instanceof Fattura_passiva_rigaBulk) {
				Fattura_passiva_rigaHome rigaHome = (Fattura_passiva_rigaHome) getHome(userContext, Fattura_passiva_rigaBulk.class);
				((Fattura_passiva_rigaBulk) rigaEco).setRigheEconomica(new BulkList<>(rigaHome.findFatturaPassivaRigheEcoList((Fattura_passiva_rigaBulk) rigaEco)));
				((Fattura_passiva_rigaBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setFattura_passiva_riga((Fattura_passiva_rigaBulk) rigaEco));
			} else if (rigaEco instanceof Fattura_attiva_rigaBulk) {
				Fattura_attiva_rigaHome rigaHome = (Fattura_attiva_rigaHome) getHome(userContext, Fattura_attiva_rigaBulk.class);
				((Fattura_attiva_rigaBulk) rigaEco).setRigheEconomica(rigaHome.findFatturaAttivaRigheEcoList((Fattura_attiva_rigaBulk) rigaEco));
				((Fattura_attiva_rigaBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setFattura_attiva_riga((Fattura_attiva_rigaBulk) rigaEco));
			} else if (rigaEco instanceof CompensoBulk) {
				CompensoHome compensoHome = (CompensoHome) getHome(userContext, CompensoBulk.class);
				((CompensoBulk) rigaEco).setRigheEconomica(compensoHome.findCompensoRigheEcoList((CompensoBulk) rigaEco));
				((CompensoBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setCompenso((CompensoBulk) rigaEco));
			} else if (rigaEco instanceof AnticipoBulk) {
				AnticipoHome anticipoHome = (AnticipoHome) getHome(userContext, AnticipoBulk.class);
				((AnticipoBulk) rigaEco).setRigheEconomica(anticipoHome.findAnticipoRigheEcoList((AnticipoBulk) rigaEco));
				((AnticipoBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setAnticipo((AnticipoBulk) rigaEco));
			} else if (rigaEco instanceof MissioneBulk) {
				MissioneHome missioneHome = (MissioneHome) getHome(userContext, MissioneBulk.class);
				((MissioneBulk) rigaEco).setRigheEconomica(missioneHome.findMissioneRigheEcoList((MissioneBulk) rigaEco));
				((MissioneBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setMissione((MissioneBulk) rigaEco));
			} else if (rigaEco instanceof OrdineAcqRigaBulk) {
				OrdineAcqRigaHome ordineAcqRigaHome = (OrdineAcqRigaHome) getHome(userContext, OrdineAcqRigaBulk.class);
				((OrdineAcqRigaBulk) rigaEco).setRigheEconomica(new BulkList<>(ordineAcqRigaHome.findOrdineAcqRigaEcoList((OrdineAcqRigaBulk) rigaEco)));
				((OrdineAcqRigaBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setOrdineAcqRiga((OrdineAcqRigaBulk) rigaEco));
			} else if (rigaEco instanceof OrdineAcqConsegnaBulk) {
				OrdineAcqConsegnaHome ordineAcqConsegnaHome = (OrdineAcqConsegnaHome) getHome(userContext, OrdineAcqConsegnaBulk.class);
				((OrdineAcqConsegnaBulk) rigaEco).setRigheEconomica(new BulkList<>(ordineAcqConsegnaHome.findOrdineAcqConsegnaEcoList((OrdineAcqConsegnaBulk) rigaEco)));
				((OrdineAcqConsegnaBulk) rigaEco).getRigheEconomica().forEach(el2 -> el2.setOrdineAcqConsegna((OrdineAcqConsegnaBulk) rigaEco));
			}
		} catch (ComponentException | PersistencyException e) {
			throw new DetailedRuntimeException(e);
		}
	}

	private IDocumentoCogeBulk loadRigheEco(UserContext userContext, IDocumentoCogeBulk documentoCoge) {
		try {
			boolean isAttivaEconomicaPuraDocamm = ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).isAttivaEconomicaPura(documentoCoge.getEsercizio());

			if (documentoCoge instanceof IDocumentoDetailEcoCogeBulk) {
				//Carico i dettagli
				loadChildrenAna(userContext, (IDocumentoDetailEcoCogeBulk) documentoCoge);

				//Se non attiva l'economica pura (quindi c'è la finanziaria o la parallela) ripulisco tutte le tabelle di appoggio per ricaricarle ex-novo con i dati
				//prelevati dalla finanziaria
				if (!isAttivaEconomicaPuraDocamm) {
					//Ripulisco tutti i campi
					((IDocumentoDetailEcoCogeBulk) documentoCoge).setVoce_ep(null);
					((OggettoBulk) documentoCoge).setToBeUpdated();
					updateBulk(userContext, (OggettoBulk) documentoCoge);

					for (IDocumentoDetailAnaCogeBulk childrenAna : ((IDocumentoDetailEcoCogeBulk) documentoCoge).getChildrenAna()) {
						((OggettoBulk) childrenAna).setToBeDeleted();
						deleteBulk(userContext, (OggettoBulk) childrenAna);
					}
					((IDocumentoDetailEcoCogeBulk) documentoCoge).clearChildrenAna();

					//e poi ricarico il tutto leggendo dalla finanziaria
					this.loadDocammRigheEcoFromCofi(userContext, (IDocumentoDetailEcoCogeBulk) documentoCoge);
				}
			} else if (documentoCoge instanceof OrdineAcqBulk) {
				OrdineAcqHome home = (OrdineAcqHome) getHome(userContext, OrdineAcqBulk.class);
				List<OrdineAcqRigaBulk> righeOrdine = home.findOrdineRigheList((OrdineAcqBulk) documentoCoge);
				righeOrdine.forEach(el->el.setOrdineAcq((OrdineAcqBulk) documentoCoge));
				((OrdineAcqBulk)documentoCoge).setRigheOrdineColl(new BulkList<>(righeOrdine));

				for (OrdineAcqRigaBulk rigaOrdine : righeOrdine) {
					//Carico i dettagli
					loadChildrenAna(userContext, rigaOrdine);

					//Se non attiva l'economica pura (quindi c'è la finanziaria o la parallela) ripulisco tutte le tabelle di appoggio per ricaricarle ex-novo con i dati
					//prelevati dalla finanziaria
					if (!isAttivaEconomicaPuraDocamm) {
						//Ripulisco tutti i campi della riga ordine
						rigaOrdine.setVoce_ep(null);
						rigaOrdine.setToBeUpdated();
						updateBulk(userContext, rigaOrdine);

						for (IDocumentoDetailAnaCogeBulk childrenAna : rigaOrdine.getChildrenAna()) {
							((OggettoBulk) childrenAna).setToBeDeleted();
							deleteBulk(userContext, (OggettoBulk) childrenAna);
						}

						OrdineAcqRigaHome rigaHome = (OrdineAcqRigaHome)getHome(userContext, OrdineAcqRigaBulk.class);
						List<OrdineAcqConsegnaBulk> consegneOrdine = rigaHome.findOrdineRigheConsegnaList(rigaOrdine);
						rigaOrdine.setRigheConsegnaColl(new BulkList<>(consegneOrdine));
						for (OrdineAcqConsegnaBulk consegnaOrdine : rigaOrdine.getRigheConsegnaColl()) {
							//Carico i dettagli
							loadChildrenAna(userContext, consegnaOrdine);

							//Ripulisco tutti i campi della riga consegna
							consegnaOrdine.setVoce_ep(null);
							consegnaOrdine.setToBeUpdated();
							updateBulk(userContext, consegnaOrdine);

							for (IDocumentoDetailAnaCogeBulk childrenAna : consegnaOrdine.getChildrenAna()) {
								((OggettoBulk) childrenAna).setToBeDeleted();
								deleteBulk(userContext, (OggettoBulk) childrenAna);
							}

						}
						//e poi ricarico il tutto leggendo dalla finanziaria
						this.loadDocammRigheEcoFromCofi(userContext, rigaOrdine);
					}
				}
			} else if (documentoCoge instanceof IDocumentoAmministrativoBulk) {
				List<IDocumentoAmministrativoRigaBulk> righeDocamm = this.getRigheDocamm(userContext, (IDocumentoAmministrativoBulk)documentoCoge);
				for (IDocumentoAmministrativoRigaBulk rigaDocamm : righeDocamm) {
					//Carico i dettagli
					loadChildrenAna(userContext, rigaDocamm);

					//Se non attiva l'economica pura (quindi c'è la finanziaria o la parallela) ripulisco tutte le tabelle di appoggio per ricaricarle ex-novo con i dati
					//prelevati dalla finanziaria
					if (!isAttivaEconomicaPuraDocamm) {
						//Ripulisco tutti i campi
						rigaDocamm.setVoce_ep(null);
						((OggettoBulk) rigaDocamm).setToBeUpdated();
						updateBulk(userContext, (OggettoBulk) rigaDocamm);

						for (IDocumentoDetailAnaCogeBulk childrenAna : rigaDocamm.getChildrenAna()) {
							((OggettoBulk) childrenAna).setToBeDeleted();
							deleteBulk(userContext, (OggettoBulk) childrenAna);
						}

						//e poi ricarico il tutto leggendo dalla finanziaria
						this.loadDocammRigheEcoFromCofi(userContext, rigaDocamm);
					}
				}
			}
            return documentoCoge;
		} catch (PersistencyException | ComponentException | RemoteException e) {
			throw new RuntimeException(e);
        }
    }

	private void loadDocammRigheEcoFromCofi(UserContext userContext, IDocumentoDetailEcoCogeBulk rigaEco) throws ComponentException, PersistencyException, RemoteException {
		Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> datiEcoDefault = null;
		if (rigaEco instanceof Documento_generico_rigaBulk)
			datiEcoDefault = ((Documento_generico_rigaHome) getHome(userContext, Documento_generico_rigaBulk.class)).getDatiEconomiciDefault(userContext, (Documento_generico_rigaBulk) rigaEco);
		else if (rigaEco instanceof Fattura_passiva_rigaIBulk)
			datiEcoDefault = ((Fattura_passiva_rigaIHome) getHome(userContext, Fattura_passiva_rigaIBulk.class)).getDatiEconomiciDefault(userContext, (Fattura_passiva_rigaIBulk) rigaEco);
		else if (rigaEco instanceof Nota_di_credito_rigaBulk)
			datiEcoDefault = ((Nota_di_credito_rigaHome) getHome(userContext, Nota_di_credito_rigaBulk.class)).getDatiEconomiciDefault(userContext, (Nota_di_credito_rigaBulk) rigaEco);
		else if (rigaEco instanceof Nota_di_debito_rigaBulk)
			datiEcoDefault = ((Nota_di_debito_rigaHome) getHome(userContext, Nota_di_debito_rigaBulk.class)).getDatiEconomiciDefault(userContext, (Nota_di_debito_rigaBulk) rigaEco);
		else if (rigaEco instanceof Fattura_attiva_rigaIBulk)
			datiEcoDefault = ((Fattura_attiva_rigaIHome) getHome(userContext, Fattura_attiva_rigaIBulk.class)).getDatiEconomiciDefault(userContext, (Fattura_attiva_rigaIBulk) rigaEco);
		else if (rigaEco instanceof Nota_di_credito_attiva_rigaBulk)
			datiEcoDefault = ((Nota_di_credito_attiva_rigaHome) getHome(userContext, Nota_di_credito_attiva_rigaBulk.class)).getDatiEconomiciDefault(userContext, (Nota_di_credito_attiva_rigaBulk) rigaEco);
		else if (rigaEco instanceof Nota_di_debito_attiva_rigaBulk)
			datiEcoDefault = ((Nota_di_debito_attiva_rigaHome) getHome(userContext, Nota_di_debito_attiva_rigaBulk.class)).getDatiEconomiciDefault(userContext, (Nota_di_debito_attiva_rigaBulk) rigaEco);
		else if (rigaEco instanceof CompensoBulk)
			datiEcoDefault = ((CompensoHome) getHome(userContext, CompensoBulk.class)).getDatiEconomiciDefault(userContext, (CompensoBulk) rigaEco);
		else if (rigaEco instanceof AnticipoBulk)
			datiEcoDefault = ((AnticipoHome) getHome(userContext, AnticipoBulk.class)).getDatiEconomiciDefault(userContext, (AnticipoBulk) rigaEco);
		else if (rigaEco instanceof MissioneBulk)
			datiEcoDefault = ((MissioneHome) getHome(userContext, MissioneBulk.class)).getDatiEconomiciDefault(userContext, (MissioneBulk) rigaEco);
		else if (rigaEco instanceof OrdineAcqRigaBulk)
			datiEcoDefault = ((OrdineAcqRigaHome) getHome(userContext, OrdineAcqRigaBulk.class)).getDatiEconomiciDefault(userContext, (OrdineAcqRigaBulk) rigaEco, Boolean.TRUE);
		else if (rigaEco instanceof OrdineAcqConsegnaBulk)
			datiEcoDefault = ((OrdineAcqConsegnaHome) getHome(userContext, OrdineAcqConsegnaBulk.class)).getDatiEconomiciDefault(userContext, (OrdineAcqConsegnaBulk) rigaEco);

		if (datiEcoDefault!=null) {
			//Ripulisco i dati economici
			rigaEco.setVoce_ep(datiEcoDefault.getFirst());
			((OggettoBulk) rigaEco).setToBeUpdated();
			updateBulk(userContext, (OggettoBulk) rigaEco);

			if (rigaEco instanceof OrdineAcqRigaBulk) {
				//Aggiusto il conto anche sulla testata consegna
				for (OrdineAcqConsegnaBulk consegna : ((OrdineAcqRigaBulk) rigaEco).getRigheConsegnaColl()) {
					consegna.setVoce_ep(datiEcoDefault.getFirst());
					consegna.setToBeUpdated();
					updateBulk(userContext, consegna);
				}
			}

			for (IDocumentoDetailAnaCogeBulk rigaAna : datiEcoDefault.getSecond()) {
				if (rigaEco instanceof Documento_generico_rigaBulk) {
					Documento_generico_rigaBulk myRigaEco = (Documento_generico_rigaBulk) rigaEco;
					Documento_generico_riga_ecoBulk myRigaAna = (Documento_generico_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Fattura_passiva_rigaIBulk) {
					Fattura_passiva_rigaIBulk myRigaEco = (Fattura_passiva_rigaIBulk) rigaEco;
					Fattura_passiva_riga_ecoIBulk myRigaAna = (Fattura_passiva_riga_ecoIBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Nota_di_credito_rigaBulk) {
					Nota_di_credito_rigaBulk myRigaEco = (Nota_di_credito_rigaBulk) rigaEco;
					Nota_di_credito_riga_ecoBulk myRigaAna = (Nota_di_credito_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Nota_di_debito_rigaBulk) {
					Nota_di_debito_rigaBulk myRigaEco = (Nota_di_debito_rigaBulk) rigaEco;
					Nota_di_debito_riga_ecoBulk myRigaAna = (Nota_di_debito_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Fattura_attiva_rigaIBulk) {
					Fattura_attiva_rigaIBulk myRigaEco = (Fattura_attiva_rigaIBulk) rigaEco;
					Fattura_attiva_riga_ecoIBulk myRigaAna = (Fattura_attiva_riga_ecoIBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Nota_di_credito_attiva_rigaBulk) {
					Nota_di_credito_attiva_rigaBulk myRigaEco = (Nota_di_credito_attiva_rigaBulk) rigaEco;
					Nota_di_credito_attiva_riga_ecoBulk myRigaAna = (Nota_di_credito_attiva_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof Nota_di_debito_attiva_rigaBulk) {
					Nota_di_debito_attiva_rigaBulk myRigaEco = (Nota_di_debito_attiva_rigaBulk) rigaEco;
					Nota_di_debito_attiva_riga_ecoBulk myRigaAna = (Nota_di_debito_attiva_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof AnticipoBulk) {
					AnticipoBulk myRigaEco = (AnticipoBulk) rigaEco;
					Anticipo_riga_ecoBulk myRigaAna = (Anticipo_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof MissioneBulk) {
					MissioneBulk myRigaEco = (MissioneBulk) rigaEco;
					Missione_riga_ecoBulk myRigaAna = (Missione_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof CompensoBulk) {
					CompensoBulk myRigaEco = (CompensoBulk) rigaEco;
					Compenso_riga_ecoBulk myRigaAna = (Compenso_riga_ecoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof OrdineAcqRigaBulk && rigaAna instanceof OrdineAcqRigaEcoBulk) {
					OrdineAcqRigaBulk myRigaEco = (OrdineAcqRigaBulk) rigaEco;
					OrdineAcqRigaEcoBulk myRigaAna = (OrdineAcqRigaEcoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				} else if (rigaEco instanceof OrdineAcqRigaBulk && rigaAna instanceof OrdineAcqConsegnaEcoBulk) {
					//Aggiorno le righe consegna
					OrdineAcqConsegnaEcoBulk myRigaAna = (OrdineAcqConsegnaEcoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
				} else {
					OrdineAcqConsegnaBulk myRigaEco = (OrdineAcqConsegnaBulk) rigaEco;
					OrdineAcqConsegnaEcoBulk myRigaAna = (OrdineAcqConsegnaEcoBulk) rigaAna;
					myRigaAna.setToBeCreated();
					insertBulk(userContext, myRigaAna);
					myRigaEco.getRigheEconomica().add(myRigaAna);
				}
			}
		}
	}

    private ResultScrittureContabili proposeScritturaPartitaDoppiaOrdineConsegna(UserContext userContext, OrdineAcqConsegnaBulk consegna, boolean makeAnalitica) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException {
        try {
            if (!consegna.isStatoConsegnaEvasa())
                throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non necessaria in quanto la consegna non risulta evasa.");

            //cerco evasione ordine
            List<EvasioneOrdineRigaBulk> evasioneRigaList = ((EvasioneOrdineRigaHome)getHome(userContext, EvasioneOrdineRigaBulk.class)).findByConsegna(consegna);

            if (evasioneRigaList.isEmpty())
                throw new ScritturaPartitaDoppiaNotRequiredException("Scrittura Economica non necessaria in quanto la consegna risulta evasa ma non è presente alcuna scrittura di evasione associata. Contattare il Customer Support.");

            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache(userContext).getHome(Fattura_passivaBulk.class);
            consegna.setOrdineAcqRiga((OrdineAcqRigaBulk)fatpasHome.loadIfNeededObject(consegna.getOrdineAcqRiga()));
            consegna.getOrdineAcqRiga().setOrdineAcq((OrdineAcqBulk)fatpasHome.loadIfNeededObject(consegna.getOrdineAcqRiga().getOrdineAcq()));
            consegna.getOrdineAcqRiga().getOrdineAcq().setUnitaOperativaOrd((UnitaOperativaOrdBulk) fatpasHome.loadIfNeededObject(consegna.getOrdineAcqRiga().getOrdineAcq().getUnitaOperativaOrd()));

            //Recupero la prima evasione
            EvasioneOrdineRigaBulk evasioneRiga = evasioneRigaList.get(0);
            TestataPrimaNota testataPrimaNota = new TestataPrimaNota(evasioneRiga.getDacr(), evasioneRiga.getDacr());

            //Carico i dettagli economici
            this.loadChildrenAna(userContext, consegna);

            if (consegna.getVoce_ep()==null || consegna.getVoce_ep().getCd_voce_ep()==null)
                throw new ApplicationException("Riga documento senza indicazione del conto di economica. Scrittura economica non possibile.");

            Voce_epBulk aContoFatturaDaRicevere = findContoFattureDaRicevere(userContext, evasioneRiga.getEsercizio());

            DettaglioPrimaNota dettPN = testataPrimaNota.addDettaglio(userContext, null, Movimento_cogeBulk.SEZIONE_DARE, consegna.getContoBulk(), consegna.getImCostoEco(), Boolean.TRUE);
            //COMPLETO CON I DATI ANALITICI - TRATTANDOSI DI MISSIONE CON ANTICIPO CERCO PER IMPORTO PER FAR COLLEGARE LA RIGA GIUSTA
            if (makeAnalitica) {
                List<DettaglioAnalitico> dettagliAnaliticos = consegna.getRigheEconomica().stream().map(DettaglioAnalitico::new).collect(Collectors.toList());
                this.completeWithDatiAnalitici(userContext,dettPN,consegna,dettagliAnaliticos,Boolean.FALSE);
            }

            testataPrimaNota.addDettaglio(userContext, Movimento_cogeBulk.TipoRiga.DEBITO.value(), Movimento_cogeBulk.SEZIONE_AVERE, aContoFatturaDaRicevere, consegna.getImCostoEco(), consegna.getOrdineAcqRiga().getCd_terzo(), null);
            return this.generaScritture(userContext, consegna, Collections.singletonList(testataPrimaNota), Boolean.FALSE, makeAnalitica);
        } catch (PersistencyException|RemoteException e) {
            throw new DetailedRuntimeException(e);
        }
    }
}