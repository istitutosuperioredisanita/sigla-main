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

import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.bulk.Configurazione_cnrKey;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.config00.esercizio.bulk.EsercizioHome;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.config00.sto.bulk.CdsHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.docamm00.tabrif.bulk.*;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoCatGrpVoceEpBulk;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoCatGrpVoceEpHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


public class ScritturaPartitaDoppiaChiusuraComponent extends CRUDComponent  {
	private static class DettaglioScrittura {
		public DettaglioScrittura(Voce_epBulk conto, String sezione, BigDecimal importo, String tiAttivita) {
			this.conto = conto;
			this.sezione = sezione;
			this.importo = importo;
			this.tiAttivita = tiAttivita;
		}

		Voce_epBulk conto;
		String sezione;
		BigDecimal importo;
		String tiAttivita;

		public Voce_epBulk getConto() {
			return conto;
		}

		public void setConto(Voce_epBulk conto) {
			this.conto = conto;
		}

		public String getSezione() {
			return sezione;
		}

		public void setSezione(String sezione) {
			this.sezione = sezione;
		}

		public BigDecimal getImporto() {
			return importo;
		}

		public void setImporto(BigDecimal importo) {
			this.importo = importo;
		}

		public String getTiAttivita() {
			return tiAttivita;
		}

		public void setTiAttivita(String tiAttivita) {
			this.tiAttivita = tiAttivita;
		}

		public boolean isIstituzionale() {
			return TipoIVA.ISTITUZIONALE.value().equals(getTiAttivita());
		}

		public boolean isCommerciale() {
			return TipoIVA.COMMERCIALE.value().equals(getTiAttivita());
		}

		public boolean isSezioneDare() {
			return Movimento_cogeBulk.SEZIONE_DARE.equals(getSezione());
		}

		public boolean isSezioneAvere() {
			return Movimento_cogeBulk.SEZIONE_AVERE.equals(getSezione());
		}
	}

	private void annullaScrittureChiusura(UserContext userContext, Integer esercizio) throws ComponentException {
		try {
			Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
			List<Scrittura_partita_doppiaBulk> resultApertura = ((Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class)).getScrittureAperturaBilancio(userContext, esercizio+1, uoEnte.getCd_cds());
			for (Scrittura_partita_doppiaBulk scrittura : resultApertura) {
				for (Movimento_cogeBulk movimento : scrittura.getAllMovimentiColl())
					movimento.setToBeDeleted();
				scrittura.setToBeDeleted();
				makeBulkPersistent(userContext, scrittura);
			}

			List<Scrittura_partita_doppiaBulk> resultChiusura = ((Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class)).getScrittureChiusuraDefinitivaBilancio(userContext, esercizio, uoEnte.getCd_cds());
			for (Scrittura_partita_doppiaBulk scrittura : resultChiusura) {
				for (Movimento_cogeBulk movimento : scrittura.getAllMovimentiColl())
					movimento.setToBeDeleted();
				scrittura.setToBeDeleted();
				makeBulkPersistent(userContext, scrittura);
			}

			List<Scrittura_partita_doppiaBulk> resultChiusuraProvvisoria = ((Scrittura_partita_doppiaHome) getHome(userContext, Scrittura_partita_doppiaBulk.class)).getScrittureChiusuraProvvisoriaBilancio(userContext, esercizio, uoEnte.getCd_cds());
			for (Scrittura_partita_doppiaBulk scrittura : resultChiusuraProvvisoria) {
				for (Movimento_cogeBulk movimento : scrittura.getAllMovimentiColl())
					movimento.setToBeDeleted();
				scrittura.setToBeDeleted();
				makeBulkPersistent(userContext, scrittura);
			}

			Configurazione_cnrBulk configRiaperturaConti = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneRiaperturaConti(esercizio+1)
					.orElseThrow(()->new ApplicationException("Manca Record Configurazione Riapertura dei conti per esercizio "+esercizio+1));
			configRiaperturaConti.setVal02("N");
			configRiaperturaConti.setToBeUpdated();
			makeBulkPersistent(userContext, configRiaperturaConti);

			Configurazione_cnrBulk configChiusuraDefinitiva = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneChiusuraBilancioDefinitiva(esercizio)
					.orElseThrow(()->new ApplicationException("Manca Record Configurazione Chiusura Bilancio Definitiva per esercizio "+esercizio));
			configChiusuraDefinitiva.setVal02("N");
			configChiusuraDefinitiva.setToBeUpdated();
			makeBulkPersistent(userContext, configChiusuraDefinitiva);

			Configurazione_cnrBulk configChiusuraProvvisoria = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneChiusuraBilancioProvvisoria(esercizio)
					.orElseThrow(()->new ApplicationException("Manca Record Configurazione Chiusura Bilancio Provvisoria per esercizio "+esercizio));
			configChiusuraProvvisoria.setVal02("N");
			configChiusuraProvvisoria.setToBeUpdated();
			makeBulkPersistent(userContext, configChiusuraProvvisoria);
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	public void makeScrittureChiusura(UserContext userContext, Integer esercizio, boolean isAnnullamento, boolean isDefinitiva) throws ComponentException {
		try {
			List<CdsBulk> allCds = ((CdsHome)getHome(userContext, CdsBulk.class)).findAllCds(userContext, esercizio);

			for (CdsBulk cds : allCds) {
				boolean isEsercizioAperto = ((EsercizioHome) getHome(userContext, EsercizioBulk.class)).isEsercizioAperto(esercizio, cds.getCd_unita_organizzativa());

				if (isEsercizioAperto)
					throw new ApplicationException("Risulta aperto l'esercizio finanziario del CDS: " + cds.getCd_unita_organizzativa() + " per l'esercizio " + esercizio + ". Operazione non consentita.");

				Chiusura_coepBulk chiusuraCds = ((Chiusura_coepHome) getHome(userContext, Chiusura_coepBulk.class)).getChiusuraCoep(esercizio, cds.getCd_unita_organizzativa());
				if (Optional.ofNullable(chiusuraCds).filter(el -> Chiusura_coepBulk.STATO_CHIUSO_DEFINITIVAMENTE.equals(el.getStato())).isPresent())
					throw new ApplicationException("Esercizio economico "+esercizio+" chiuso per il CDS: " + cds.getCd_unita_organizzativa() + ". Operazione non consentita.");
			}

			if (isAnnullamento)
				annullaScrittureChiusura(userContext, esercizio);
			else { //effettuo chiusura
				if (Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
						.filter(Configurazione_cnrHome.class::isInstance)
						.map(Configurazione_cnrHome.class::cast)
						.orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
						.isChiusuraBilancioDefinitivaEffettuata(esercizio))
					throw new ApplicationException("Operazione non eseguibile. Risultano essere state già effettuate in modalità definitiva le scritture di chiusura bilancio per l'esercizio " + esercizio + ".");

				if (!Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
						.filter(Configurazione_cnrHome.class::isInstance)
						.map(Configurazione_cnrHome.class::cast)
						.orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
						.isTerminataAttivitaEvasioneOrdini(esercizio))
					throw new ApplicationException("Scritture di chiusura non eseguibili in quanto non risulta ancora terminata l'attività di evasione ordini per l'esercizio " + esercizio + ".");

				if (!Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
						.filter(Configurazione_cnrHome.class::isInstance)
						.map(Configurazione_cnrHome.class::cast)
						.orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
						.isTerminataAttivitaMagazzino(esercizio))
					throw new ApplicationException("Scritture di chiusura non eseguibili in quanto non risulta ancora terminata l'attività di carico/scarico magazzino per l'esercizio " + esercizio + ".");

				boolean isChiusuraProvvisoriaMade = Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
						.filter(Configurazione_cnrHome.class::isInstance)
						.map(Configurazione_cnrHome.class::cast)
						.orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
						.isChiusuraBilancioProvvisoriaEffettuata(esercizio);

				if (!isDefinitiva && isChiusuraProvvisoriaMade)
					throw new ApplicationException("Operazione non eseguibile. Risultano essere state già effettuate in modalità provvisoria le scritture di chiusura bilancio per l'esercizio " + esercizio + ".");

				if (!isChiusuraProvvisoriaMade) {
					makeScrittureChiusuraMagazzino(userContext, esercizio);
					makeScrittureChiusuraInventario(userContext, esercizio);
					makeScrittureChiusuraFattureDaRicevereDaOrdini(userContext, esercizio);

					Configurazione_cnrBulk configChiusuraProvvisoria = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneChiusuraBilancioProvvisoria(esercizio)
							.orElseThrow(()->new ApplicationException("Manca Record Configurazione Bilancio Provvisorio per esercizio "+esercizio));
					configChiusuraProvvisoria.setVal02("Y");
					configChiusuraProvvisoria.setToBeUpdated();
					makeBulkPersistent(userContext, configChiusuraProvvisoria);
				}

				if (isDefinitiva) {
					makeScrittureChiusuraContoEconomico(userContext, esercizio);
					makeScrittureChiusuraUtilePerdita(userContext, esercizio);
					List<Scrittura_partita_doppiaBulk> scrittureStatoPatrimoniale = makeScrittureChiusuraStatoPatrimoniale(userContext, esercizio);
					makeScrittureAperturaStatoPatrimoniale(userContext, esercizio, scrittureStatoPatrimoniale);

					Configurazione_cnrBulk configChiusuraDefinitiva = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneChiusuraBilancioDefinitiva(esercizio)
							.orElseThrow(()->new ApplicationException("Manca Record Configurazione Bilancio Definitivo per esercizio "+esercizio));
					configChiusuraDefinitiva.setVal02("Y");
					configChiusuraDefinitiva.setToBeUpdated();
					makeBulkPersistent(userContext, configChiusuraDefinitiva);

					Configurazione_cnrBulk configRiaperturaConti = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class)).getConfigurazioneRiaperturaConti(esercizio+1)
							.orElseThrow(()->new ApplicationException("Manca Record Configurazione Riapertura dei Conti per esercizio "+(esercizio+1)));
					configRiaperturaConti.setVal02("Y");
					configRiaperturaConti.setToBeUpdated();
					makeBulkPersistent(userContext, configRiaperturaConti);
				}
			}
		} catch (RemoteException | PersistencyException | IntrospectionException e) {
			throw handleException(e);
        }
    }

	private void makeScrittureChiusuraMagazzino(UserContext userContext, Integer esercizio) throws ComponentException {
		try {
			List<DettaglioScrittura> details = new ArrayList<>();

			List<ChiusuraAnnoCatGrpVoceEpBulk> listChiusure = ((ChiusuraAnnoCatGrpVoceEpHome) getHome(userContext, ChiusuraAnnoCatGrpVoceEpBulk.class)).findChiusureAnnoMagazzinoList(esercizio);

			listChiusure.stream().filter(chiusura -> Optional.ofNullable(chiusura.getImpTotale()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent() ||
							Optional.ofNullable(chiusura.getImpMinusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent() ||
							Optional.ofNullable(chiusura.getImpPlusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent())
					.forEach(chiusura -> {
						try {
							//trovo i conti della categoria gruppo
							Categoria_gruppo_voce_epBulk contiCategoria = ((Categoria_gruppo_voce_epHome) getHome(userContext, Categoria_gruppo_voce_epBulk.class)).findDefaultByCategoria(esercizio, chiusura.getCdCategoriaGruppo());
							if (Optional.ofNullable(chiusura.getImpTotale()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contiCategoria.getConto(), chiusura.getImpTotale(), TipoIVA.ISTITUZIONALE.value());
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contiCategoria.getContoContropartita(), chiusura.getImpTotale(), TipoIVA.ISTITUZIONALE.value());
							}
							if (Optional.ofNullable(chiusura.getImpMinusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
								ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
								ContoBulk contoMinusvalenzaBulk = (ContoBulk) contoHome.findByPrimaryKey(contiCategoria.getContoMinusvalenza());
								ContoBulk contoContropartitaBulk = (ContoBulk) contoHome.findByPrimaryKey(chiusura.getVoceEp());
								if (contoMinusvalenzaBulk == null || contoContropartitaBulk == null)
									throw new ApplicationException("Non risulta correttamente individuato nelle scritture di chiusura di magazzino per il gruppo " + chiusura.getCdCategoriaGruppo()+" il conto minusvalenza o la sua contropartita.");
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoMinusvalenzaBulk, chiusura.getImpMinusValenze(), TipoIVA.ISTITUZIONALE.value());
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoContropartitaBulk, chiusura.getImpMinusValenze(), TipoIVA.ISTITUZIONALE.value());
							}
							if (Optional.ofNullable(chiusura.getImpPlusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
								ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
								ContoBulk contoPlusvalenzaBulk = (ContoBulk) contoHome.findByPrimaryKey(contiCategoria.getContoPlusvalenza());
								ContoBulk contoContropartitaBulk = (ContoBulk) contoHome.findByPrimaryKey(chiusura.getVoceEp());
								if (contoPlusvalenzaBulk == null || contoContropartitaBulk == null)
									throw new ApplicationException("Non risulta correttamente individuato nelle scritture di chiusura di magazzino per il gruppo " + chiusura.getCdCategoriaGruppo()+" il conto plusvalenza o la sua contropartita.");
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoContropartitaBulk, chiusura.getImpPlusValenze(), TipoIVA.ISTITUZIONALE.value());
								aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoPlusvalenzaBulk, chiusura.getImpPlusValenze(), TipoIVA.ISTITUZIONALE.value());
							}
						} catch (ComponentException | PersistencyException ex) {
							throw new RuntimeException(ex);
						}
					});

			makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.PRECHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.RIMANENZE_MAGAZZINO.name(), Scrittura_partita_doppiaBulk.Causale.RIMANENZE_MAGAZZINO.label());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void makeScrittureChiusuraInventario(UserContext userContext, Integer esercizio) throws ComponentException {
		try {
			ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);

			List<DettaglioScrittura> details = new ArrayList<>();
			List<DettaglioScrittura> detailsFondoAmmortamento = new ArrayList<>();

			List<ChiusuraAnnoCatGrpVoceEpBulk> listChiusure = ((ChiusuraAnnoCatGrpVoceEpHome)getHome(userContext, ChiusuraAnnoCatGrpVoceEpBulk.class)).findChiusureAnnoInventarioList(esercizio);

			listChiusure.stream().filter(chiusura->Optional.ofNullable(chiusura.getImpTotale()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent() ||
							Optional.ofNullable(chiusura.getImpMinusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent() ||
							Optional.ofNullable(chiusura.getImpPlusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent() ||
							Optional.ofNullable(chiusura.getImpDecrementi()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent())
				.forEach(chiusura-> {
					try {
						//trovo i conti della categoria gruppo
						Categoria_gruppo_voce_epBulk contiCategoria = ((Categoria_gruppo_voce_epHome) getHome(userContext, Categoria_gruppo_voce_epBulk.class)).findDefaultByCategoria(esercizio, chiusura.getCdCategoriaGruppo());
						if (Optional.ofNullable(chiusura.getImpTotale()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contiCategoria.getConto(), chiusura.getImpTotale(), TipoIVA.ISTITUZIONALE.value());
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contiCategoria.getContoContropartita(), chiusura.getImpTotale(), TipoIVA.ISTITUZIONALE.value());
						}
						if (Optional.ofNullable(chiusura.getImpMinusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
							ContoBulk contoMinusvalenzaBulk = (ContoBulk) contoHome.findByPrimaryKey(contiCategoria.getContoMinusvalenza());
							ContoBulk contoContropartitaBulk = (ContoBulk) contoHome.findByPrimaryKey(chiusura.getVoceEp());
							if (contoMinusvalenzaBulk == null || contoContropartitaBulk == null)
								throw new ApplicationException("Non risulta correttamente individuato nelle scritture di chiusura inventario per il gruppo " + chiusura.getCdCategoriaGruppo()+" il conto minusvalenza o la sua contropartita.");
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoMinusvalenzaBulk, chiusura.getImpMinusValenze(), TipoIVA.ISTITUZIONALE.value());
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoContropartitaBulk, chiusura.getImpMinusValenze(), TipoIVA.ISTITUZIONALE.value());
						}
						if (Optional.ofNullable(chiusura.getImpPlusValenze()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
							ContoBulk contoPlusvalenzaBulk = (ContoBulk) contoHome.findByPrimaryKey(contiCategoria.getContoPlusvalenza());
							ContoBulk contoContropartitaBulk = (ContoBulk) contoHome.findByPrimaryKey(chiusura.getVoceEp());
							if (contoPlusvalenzaBulk == null || contoContropartitaBulk == null)
								throw new ApplicationException("Non risulta correttamente individuato nelle scritture di chiusura inventario per il gruppo " + chiusura.getCdCategoriaGruppo()+" il conto plusvalenza o la sua contropartita.");
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoContropartitaBulk, chiusura.getImpPlusValenze(), TipoIVA.ISTITUZIONALE.value());
							aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoPlusvalenzaBulk, chiusura.getImpPlusValenze(), TipoIVA.ISTITUZIONALE.value());
						}
						if (Optional.ofNullable(chiusura.getImpDecrementi()).filter(el -> el.compareTo(BigDecimal.ZERO) != 0).isPresent()) {
							AssCatgrpInventVoceEpBulk assCatgrpInventVoceEp = ((AssCatgrpInventVoceEpHome) getHome(userContext, AssCatgrpInventVoceEpBulk.class)).findDefaultByCategoria(esercizio, chiusura.getCdCategoriaGruppo());

							ContoBulk contoFondoAmmortamentoBulk = (ContoBulk) contoHome.findByPrimaryKey(contiCategoria.getContoContropartita());
							ContoBulk contoAttivitaBulk = (ContoBulk) contoHome.findByPrimaryKey(assCatgrpInventVoceEp.getConto());
							if (contoFondoAmmortamentoBulk == null || contoAttivitaBulk == null)
								throw new ApplicationException("Non risulta correttamente individuato nelle scritture di chiusura inventario per il gruppo " + chiusura.getCdCategoriaGruppo()+" il conto fondo ammortamento o il conto attività.");
							aggiornaDetail(detailsFondoAmmortamento, Movimento_cogeBulk.SEZIONE_DARE, contoFondoAmmortamentoBulk, chiusura.getImpDecrementi(), TipoIVA.ISTITUZIONALE.value());
							aggiornaDetail(detailsFondoAmmortamento, Movimento_cogeBulk.SEZIONE_AVERE, contoAttivitaBulk, chiusura.getImpDecrementi(), TipoIVA.ISTITUZIONALE.value());
						}
					} catch (ComponentException | PersistencyException ex) {
						throw new RuntimeException(ex);
					}
				});

			makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.PRECHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.AMMORTAMENTO.name(), Scrittura_partita_doppiaBulk.Causale.AMMORTAMENTO.label());
			makePersistentScritture(userContext, esercizio, detailsFondoAmmortamento, Scrittura_partita_doppiaBulk.Origine.PRECHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.DISMISSIONE_BENE_DUREVOLE.name(), Scrittura_partita_doppiaBulk.Causale.DISMISSIONE_BENE_DUREVOLE.label());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void makeScrittureChiusuraFattureDaRicevereDaOrdini(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		try {
			List<DettaglioScrittura> details = new ArrayList<>();

			Voce_epBulk contoFattureDaRicevere = this.findContoFattureDaRicevere(userContext, esercizio);

			List<V_controllo_ordine_acq_consegnaBulk> allDetailsFattureRicevere = ((V_controllo_ordine_acq_consegnaHome)getHome(userContext, V_controllo_ordine_acq_consegnaBulk.class)).getDetailFattureDaRicevere(esercizio);

			Map<String, Map<String, List<V_controllo_ordine_acq_consegnaBulk>>> mapTiAttivitaVoceEp = allDetailsFattureRicevere.stream()
					.collect(Collectors.groupingBy(V_controllo_ordine_acq_consegnaBulk::getTiAttivita,
							Collectors.groupingBy(V_controllo_ordine_acq_consegnaBulk::getCdVoceEp)));

			mapTiAttivitaVoceEp.keySet().forEach(tiAttivita -> {
				Map<String, List<V_controllo_ordine_acq_consegnaBulk>> mapVoceEp = mapTiAttivitaVoceEp.get(tiAttivita);
				mapVoceEp.keySet().forEach(cdVoceEp -> {
					try {
						List<V_controllo_ordine_acq_consegnaBulk> movimentiList = mapVoceEp.get(cdVoceEp);
						Voce_epBulk contoCosto = (Voce_epBulk) getHome(userContext, Voce_epBulk.class).findByPrimaryKey(new Voce_epBulk(cdVoceEp, esercizio));
						BigDecimal totaleImportoFattureRicevere = movimentiList.stream()
								.map(V_controllo_ordine_acq_consegnaBulk::getImportoFattureDaRicevere).reduce(BigDecimal.ZERO, BigDecimal::add);

						aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoCosto, totaleImportoFattureRicevere, tiAttivita);
						aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoFattureDaRicevere, totaleImportoFattureRicevere, tiAttivita);
					} catch (ComponentException | PersistencyException ex) {
						throw new RuntimeException(ex);
					}
				});
			});

			makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.PRECHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.FATTURE_DA_RICEVERE.name(), "Determinazione "+Scrittura_partita_doppiaBulk.Causale.FATTURE_DA_RICEVERE.label()+" da ordini");
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void makeScrittureChiusuraContoEconomico(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		try {
			List<Scrittura_partita_doppiaBulk> result = new ArrayList<>();

			Voce_epBulk contoEconomico = this.findContoEconomico(userContext, esercizio);
			List<V_saldo_cogeBulk> allMovimentiCoge = ((V_saldo_cogeHome)getHome(userContext, V_saldo_cogeBulk.class)).getAllSaldiContoEconomico(esercizio);

			List<DettaglioScrittura> details = makeDetailsChiusuraConti(userContext, allMovimentiCoge, contoEconomico);

			makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.CHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.CHIUSURA_CONTO_ECONOMICO.name(), Scrittura_partita_doppiaBulk.Causale.CHIUSURA_CONTO_ECONOMICO.label());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void makeScrittureChiusuraUtilePerdita(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		try {
			List<Scrittura_partita_doppiaBulk> result = new ArrayList<>();

			List<DettaglioScrittura> details = new ArrayList<>();
			{
				Voce_epBulk contoEconomico = this.findContoEconomico(userContext, esercizio);
				List<V_saldo_cogeBulk> allMovimentiCoge = ((V_saldo_cogeHome)getHome(userContext, V_saldo_cogeBulk.class)).getAllSaldiConto(esercizio, contoEconomico.getCd_voce_ep());

				//Calcolo il saldo del conto economico per capire se movimentare il conto utile o perdita
				BigDecimal totaleDare = allMovimentiCoge.stream()
						.map(V_saldo_cogeBulk::getTotDare).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal totaleAvere = allMovimentiCoge.stream()
						.map(V_saldo_cogeBulk::getTotAvere).reduce(BigDecimal.ZERO, BigDecimal::add);

				Voce_epBulk contoUtilePerdita;
				if (totaleAvere.compareTo(totaleDare)>=0)
					contoUtilePerdita = this.findContoUtileEsercizio(userContext, esercizio);
				else
					contoUtilePerdita = this.findContoPerditaEsercizio(userContext, esercizio);

				details.addAll(makeDetailsChiusuraConti(userContext, allMovimentiCoge, contoUtilePerdita));
			}

			makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.CHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.DETERMINAZIONE_UTILE_PERDITA.name(), Scrittura_partita_doppiaBulk.Causale.DETERMINAZIONE_UTILE_PERDITA.label());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private List<Scrittura_partita_doppiaBulk> makeScrittureChiusuraStatoPatrimoniale(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		try {
			List<Scrittura_partita_doppiaBulk> result = new ArrayList<>();

			Voce_epBulk contoStatoPatrimonialeFinale = this.findContoStatoPatrimonialeFinale(userContext, esercizio);
			List<V_saldo_cogeBulk> allMovimentiCoge = ((V_saldo_cogeHome)getHome(userContext, V_saldo_cogeBulk.class)).getAllSaldiStatoPatrimoniale(esercizio);

			List<DettaglioScrittura> details = makeDetailsChiusuraConti(userContext, allMovimentiCoge, contoStatoPatrimonialeFinale);

			return makePersistentScritture(userContext, esercizio, details, Scrittura_partita_doppiaBulk.Origine.CHIUSURA.name(), Scrittura_partita_doppiaBulk.Causale.CHIUSURA_STATO_PATRIMONIALE.name(), Scrittura_partita_doppiaBulk.Causale.CHIUSURA_STATO_PATRIMONIALE.label());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void makeScrittureAperturaStatoPatrimoniale(UserContext userContext, Integer esercizio, List<Scrittura_partita_doppiaBulk> scrittureStatoPatrimonialeChiusuraList) throws ComponentException, RemoteException {
		try {
			List<Scrittura_partita_doppiaBulk> result = new ArrayList<>();

			Voce_epHome voceEpHome = (Voce_epHome) getHome(userContext, Voce_epBulk.class);

			final Voce_epBulk contoStatoPatrimonialeFinale = this.findContoStatoPatrimonialeFinale(userContext, esercizio);
			final Voce_epBulk contoUtileEsercizio = this.findContoUtileEsercizio(userContext, esercizio);
			final Voce_epBulk contoPerditaEsercizio =  this.findContoPerditaEsercizio(userContext, esercizio);

			final Voce_epBulk contoStatoPatrimonialeIniziale = this.findContoStatoPatrimonialeIniziale(userContext, esercizio+1);
			final Voce_epBulk contoUtileEsercizioPrecedente = this.findContoUtileEsercizioPrecedente(userContext, esercizio+1);
			final Voce_epBulk contoPerditaEsercizioPrecedente = this.findContoPerditaEsercizioPrecedente(userContext, esercizio+1);

			List<DettaglioScrittura> details = new ArrayList<>();
			List<DettaglioScrittura> detailsAssegnazioneUtilePerdita = new ArrayList<>();

			for (Scrittura_partita_doppiaBulk scritturaStatoPatrimonialeChiusura : scrittureStatoPatrimonialeChiusuraList) {
				scritturaStatoPatrimonialeChiusura.getAllMovimentiColl().forEach(el -> {
					try {
						Voce_epBulk voceEpBulk;
						if (el.getCd_voce_ep().equals(contoStatoPatrimonialeFinale.getCd_voce_ep()))
							voceEpBulk = Optional.ofNullable(contoStatoPatrimonialeIniziale)
									.orElseThrow(() -> new ApplicationException("Manca la definizione del conto 'Stato patrimoniale iniziale' per l'esercizio " + (esercizio + 1) + ")."));
						else
							voceEpBulk = Optional.ofNullable((Voce_epBulk) voceEpHome.findByPrimaryKey(new Voce_epBulk(el.getCd_voce_ep(), esercizio + 1)))
									.orElseThrow(() -> new ApplicationException("Manca il conto " + el.getCd_voce_ep() + " per l'esercizio " + (esercizio + 1) + ")."));

						details.add(new DettaglioScrittura(voceEpBulk, el.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE) ? Movimento_cogeBulk.SEZIONE_AVERE : Movimento_cogeBulk.SEZIONE_DARE, el.getIm_movimento(), el.getTi_istituz_commerc()));

						//carico i dettagli per l'assegnazione utile ad anni precedenti
						if (el.getCd_voce_ep().equals(contoUtileEsercizio.getCd_voce_ep()) || el.getCd_voce_ep().equals(contoPerditaEsercizio.getCd_voce_ep())) {
							Voce_epBulk voceUtilePerditaEserciziPrecedenti;
							if (el.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE)) // VUOL DIRE CHE SI TRATTA DI UTILE
								voceUtilePerditaEserciziPrecedenti = Optional.ofNullable(contoUtileEsercizioPrecedente)
										.orElseThrow(() -> new ApplicationException("Manca la definizione del conto 'Utile di Esercizio Anni Precedenti' per l'esercizio " + (esercizio + 1) + ")."));
							else
								voceUtilePerditaEserciziPrecedenti = Optional.ofNullable(contoPerditaEsercizioPrecedente)
										.orElseThrow(() -> new ApplicationException("Manca la definizione del conto 'Perdita di Esercizio Anni Precedenti' per l'esercizio " + (esercizio + 1) + ")."));

							detailsAssegnazioneUtilePerdita.add(new DettaglioScrittura(voceEpBulk, el.getSezione(), el.getIm_movimento(), el.getTi_istituz_commerc()));
							detailsAssegnazioneUtilePerdita.add(new DettaglioScrittura(voceUtilePerditaEserciziPrecedenti, el.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE) ? Movimento_cogeBulk.SEZIONE_AVERE : Movimento_cogeBulk.SEZIONE_DARE, el.getIm_movimento(), el.getTi_istituz_commerc()));
						}
					} catch (ApplicationException | PersistencyException e) {
						throw new RuntimeException(e);
					}
				});
			}

			makePersistentScritture(userContext, esercizio+1, details, Scrittura_partita_doppiaBulk.Origine.APERTURA.name(), Scrittura_partita_doppiaBulk.Causale.RIAPERTURA_CONTI.name(), Scrittura_partita_doppiaBulk.Causale.RIAPERTURA_CONTI.label());
			makePersistentScritture(userContext, esercizio+1, detailsAssegnazioneUtilePerdita, Scrittura_partita_doppiaBulk.Origine.APERTURA.name(), Scrittura_partita_doppiaBulk.Causale.RIAPERTURA_CONTI.name(), "Destinazione Utile/perdita di esercizio");
		} catch (PersistencyException e) {
			throw handleException(e);
		}
    }

	private Movimento_cogeBulk createMovimentoCoge(UserContext userContext, Scrittura_partita_doppiaBulk scritturaPartitaDoppia, String aSezione, Voce_epBulk aConto, BigDecimal aImporto, String tiAttivita) throws ComponentException{
		try {
			if (aImporto.compareTo(BigDecimal.ZERO)==0)
				return null;

			Movimento_cogeBulk movimentoCoge = new Movimento_cogeBulk();

			ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
			ContoBulk contoBulk = (ContoBulk)contoHome.findByPrimaryKey(aConto);
			movimentoCoge.setToBeCreated();
			movimentoCoge.setUser(userContext.getUser());

			movimentoCoge.setConto(contoBulk);
			movimentoCoge.setSezione(aSezione);
			movimentoCoge.setIm_movimento(aImporto);
			movimentoCoge.setTi_istituz_commerc(tiAttivita);

			if (contoBulk.isContoNumerarioAttivita())
				movimentoCoge.setTi_riga(Movimento_cogeBulk.TipoRiga.ATTIVITA.value());
			else if (contoBulk.isContoNumerarioPassivita())
				movimentoCoge.setTi_riga(Movimento_cogeBulk.TipoRiga.PASSIVITA.value());
			else if (contoBulk.isContoCostoEconomicoEsercizio())
				movimentoCoge.setTi_riga(Movimento_cogeBulk.TipoRiga.COSTO.value());
			else if (contoBulk.isContoRicavoEconomicoEsercizio())
				movimentoCoge.setTi_riga(Movimento_cogeBulk.TipoRiga.RICAVO.value());

			movimentoCoge.setStato(Movimento_cogeBulk.STATO_DEFINITIVO);
			movimentoCoge.setFl_modificabile(Boolean.TRUE);
			movimentoCoge.setToBeCreated();
			return movimentoCoge;
		} catch (PersistencyException e) {
			throw handleException(e);
		}
	}

	private void aggiornaDetail(List<DettaglioScrittura> details, String aSezione, Voce_epBulk conto, BigDecimal aImporto, String tiAttivita) {
		String newSezione;
		BigDecimal newImporto = aImporto;
		if (aImporto.compareTo(BigDecimal.ZERO)<0) {
			newSezione = aSezione.equals(Movimento_cogeBulk.SEZIONE_DARE)?Movimento_cogeBulk.SEZIONE_AVERE:Movimento_cogeBulk.SEZIONE_DARE;
			newImporto = aImporto.negate();
		} else {
			newSezione = aSezione;
		}
		newImporto = newImporto.setScale(2, RoundingMode.HALF_UP);
		DettaglioScrittura detail = details.stream().filter(el->el.getTiAttivita().equals(tiAttivita))
				.filter(el->el.getConto().equalsByPrimaryKey(conto)).findAny()
				.orElseGet(()->{
					DettaglioScrittura newDetail = new DettaglioScrittura(conto, newSezione, BigDecimal.ZERO, tiAttivita);
					details.add(newDetail);
					return newDetail;
				});

		if (detail.getSezione().compareTo(newSezione)!=0) {
			if (detail.getImporto().compareTo(newImporto)>0)
				detail.setImporto(detail.getImporto().subtract(newImporto));
			else {
				detail.setSezione(newSezione);
				detail.setImporto(newImporto.subtract(detail.getImporto()));
			}
		} else { //stessa sezione
			detail.setImporto(detail.getImporto().add(newImporto));
		}
	}

	private Voce_epBulk findContoEconomico(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_CONTO_ECONOMICO, 1);
	}

	private Voce_epBulk findContoStatoPatrimonialeIniziale(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_STATO_PATRIMONIALE, 1);
	}

	private Voce_epBulk findContoStatoPatrimonialeFinale(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_STATO_PATRIMONIALE, 2);
	}

	private Voce_epBulk findContoUtileEsercizio(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITA_ESERCIZIO, 1);
	}

	private Voce_epBulk findContoPerditaEsercizio(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITA_ESERCIZIO, 2);
	}

	private Voce_epBulk findContoUtileEsercizioPrecedente(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITA_ESERCIZIO, 3);
	}

	private Voce_epBulk findContoPerditaEsercizioPrecedente(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_UTILE_PERDITA_ESERCIZIO, 4);
	}

	private Voce_epBulk findContoFattureDaRicevere(UserContext userContext, Integer esercizio) throws ComponentException, RemoteException {
		return this.findContoByConfigurazioneCNR(userContext, esercizio, Configurazione_cnrBulk.PK_VOCEEP_SPECIALE, Configurazione_cnrBulk.SK_FATTURE_DA_RICEVERE, 1);
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

	private Scrittura_partita_doppiaBulk makeBaseScritturaPartitaDoppia(UserContext userContext, Integer esercizio, List<DettaglioScrittura> details) throws ComponentException {
		try {
			Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
			Scrittura_partita_doppiaBulk scritturaPartitaDoppia = new Scrittura_partita_doppiaBulk();

			scritturaPartitaDoppia.setDt_contabilizzazione(new Timestamp(new GregorianCalendar(esercizio, Calendar.DECEMBER, 31).getTime().getTime()));
			scritturaPartitaDoppia.setUser(userContext.getUser());
			scritturaPartitaDoppia.setCd_unita_organizzativa(uoEnte.getCd_unita_organizzativa());
			scritturaPartitaDoppia.setCd_cds(uoEnte.getCd_cds());
			scritturaPartitaDoppia.setTi_scrittura(Scrittura_partita_doppiaBulk.TIPO_PRIMA_SCRITTURA);
			scritturaPartitaDoppia.setStato(Scrittura_partita_doppiaBulk.STATO_DEFINITIVO);
			scritturaPartitaDoppia.setEsercizio(esercizio);
			scritturaPartitaDoppia.setAttiva(Scrittura_partita_doppiaBulk.ATTIVA_YES);

			scritturaPartitaDoppia.setToBeCreated();

			BigDecimal totDare = BigDecimal.ZERO;
			BigDecimal totAvere = BigDecimal.ZERO;

			for (DettaglioScrittura detail : details) {
				if (detail.getImporto().compareTo(BigDecimal.ZERO) != 0) {
					if (detail.getSezione().equals(Movimento_cogeBulk.SEZIONE_DARE)) {
						totDare = totDare.add(detail.getImporto());
						scritturaPartitaDoppia.addToMovimentiDareColl(createMovimentoCoge(userContext, scritturaPartitaDoppia, detail.getSezione(), detail.getConto(), detail.getImporto(), detail.getTiAttivita()));
					} else {
						totAvere = totAvere.add(detail.getImporto());
						scritturaPartitaDoppia.addToMovimentiAvereColl(createMovimentoCoge(userContext, scritturaPartitaDoppia, detail.getSezione(), detail.getConto(), detail.getImporto(), detail.getTiAttivita()));
					}
				}
			}
			scritturaPartitaDoppia.setIm_scrittura(totDare);
			if (totDare.compareTo(totAvere) != 0)
				throw new ApplicationException("Attenzione! Il totale dare non risulta uguale al totale Avere.");
			return scritturaPartitaDoppia;
		} catch(PersistencyException e) {
			throw handleException(e);
		}
	}

	private List<DettaglioScrittura> makeDetailsChiusuraConti(UserContext userContext, List<V_saldo_cogeBulk> allMovimentiCoge, Voce_epBulk controConto) throws ComponentException {
		ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
		List<DettaglioScrittura> details = new ArrayList<>();

		Map<String, Map<String, List<V_saldo_cogeBulk>>> mapTiAttivitaVoceEp = allMovimentiCoge.stream().collect(Collectors.groupingBy(V_saldo_cogeBulk::getTiIstituzCommerc,
						Collectors.groupingBy(V_saldo_cogeBulk::getCdVoceEp)));

		mapTiAttivitaVoceEp.keySet().forEach(tiAttivita -> {
			Map<String, List<V_saldo_cogeBulk>> mapVoceEp = mapTiAttivitaVoceEp.get(tiAttivita);
			BigDecimal totaleDareAttivita = BigDecimal.ZERO;
			BigDecimal totaleAvereAttivita = BigDecimal.ZERO;

			for (String cdVoceEp : mapVoceEp.keySet()) {
				try {
					List<V_saldo_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
					ContoBulk contoBulk = (ContoBulk) contoHome.findByPrimaryKey(new ContoBulk(cdVoceEp, CNRUserContext.getEsercizio(userContext)));

					BigDecimal totaleDareConto = movimentiList.stream()
							.map(V_saldo_cogeBulk::getTotDare).reduce(BigDecimal.ZERO, BigDecimal::add);

					BigDecimal totaleAvereConto = movimentiList.stream()
							.map(V_saldo_cogeBulk::getTotAvere).reduce(BigDecimal.ZERO, BigDecimal::add);

					BigDecimal saldoConto = totaleDareConto.subtract(totaleAvereConto);

					if (saldoConto.compareTo(BigDecimal.ZERO)>0) {
						totaleAvereAttivita = totaleAvereAttivita.add(saldoConto.abs());
						aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_AVERE, contoBulk, saldoConto.abs(), tiAttivita);
					} else if (saldoConto.compareTo(BigDecimal.ZERO)<0) {
						totaleDareAttivita = totaleDareAttivita.add(saldoConto.abs());
						aggiornaDetail(details, Movimento_cogeBulk.SEZIONE_DARE, contoBulk, saldoConto.abs(), tiAttivita);
					}
				} catch (PersistencyException ex) {
					throw new RuntimeException(ex);
				}
			}

			if (totaleDareAttivita.compareTo(BigDecimal.ZERO)!=0)
				details.add(new DettaglioScrittura(controConto, Movimento_cogeBulk.SEZIONE_AVERE, totaleDareAttivita, tiAttivita));
			if (totaleAvereAttivita.compareTo(BigDecimal.ZERO)!=0)
				details.add(new DettaglioScrittura(controConto, Movimento_cogeBulk.SEZIONE_DARE, totaleAvereAttivita, tiAttivita));
		});
		return details;
	}

	private List<Scrittura_partita_doppiaBulk> makePersistentScritture(UserContext userContext, Integer esercizio, List<DettaglioScrittura> details, String origineScrittura, String pCausaleCoge, String pDsScrittura) throws ComponentException, PersistencyException {
		List<Scrittura_partita_doppiaBulk> result = new ArrayList<>();

		GregorianCalendar aDataComp = new GregorianCalendar();

		if (origineScrittura.equals(Scrittura_partita_doppiaBulk.Origine.APERTURA.name()))
			aDataComp = new GregorianCalendar(esercizio, Calendar.JANUARY, 1);
		else
			aDataComp = new GregorianCalendar(esercizio, Calendar.DECEMBER, 31);

		List<DettaglioScrittura> detailsIstituzionali = details.stream().filter(el->TipoIVA.ISTITUZIONALE.value().equals(el.getTiAttivita())).collect(Collectors.toList());
		List<DettaglioScrittura> detailsCommerciali = details.stream().filter(el->!TipoIVA.ISTITUZIONALE.value().equals(el.getTiAttivita())).collect(Collectors.toList());

		if (!detailsIstituzionali.isEmpty()) {
			Scrittura_partita_doppiaBulk scritturaPartitaDoppia = this.makeBaseScritturaPartitaDoppia(userContext, esercizio, detailsIstituzionali);
			scritturaPartitaDoppia.setOrigine_scrittura(origineScrittura);
			scritturaPartitaDoppia.setCd_causale_coge(pCausaleCoge);
			scritturaPartitaDoppia.setDs_scrittura(pDsScrittura+" - Attività Istituzionale");
			//valorizzo le date di competenza coge
			scritturaPartitaDoppia.setDt_da_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			scritturaPartitaDoppia.setDt_a_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			for (Movimento_cogeBulk movimento : scritturaPartitaDoppia.getAllMovimentiColl()) {
				movimento.setDt_da_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
				movimento.setDt_a_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			}
			makeBulkPersistent(userContext, scritturaPartitaDoppia);
			result.add(scritturaPartitaDoppia);
		}
		if (!detailsCommerciali.isEmpty()) {
			Scrittura_partita_doppiaBulk scritturaPartitaDoppia = this.makeBaseScritturaPartitaDoppia(userContext, esercizio, detailsCommerciali);
			scritturaPartitaDoppia.setOrigine_scrittura(origineScrittura);
			scritturaPartitaDoppia.setCd_causale_coge(pCausaleCoge);
			scritturaPartitaDoppia.setDs_scrittura(pDsScrittura+" - Attività Commerciale");
			//valorizzo le date di competenza coge
			scritturaPartitaDoppia.setDt_da_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			scritturaPartitaDoppia.setDt_a_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			for (Movimento_cogeBulk movimento : scritturaPartitaDoppia.getAllMovimentiColl()) {
				movimento.setDt_da_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
				movimento.setDt_a_competenza_coge(new Timestamp(aDataComp.getTimeInMillis()));
			}
			makeBulkPersistent(userContext, scritturaPartitaDoppia);
			result.add(scritturaPartitaDoppia);
		}
		return result;
	}
}