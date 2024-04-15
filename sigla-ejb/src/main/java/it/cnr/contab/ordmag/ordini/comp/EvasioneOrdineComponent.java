/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.ordini.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk;
import it.cnr.contab.docamm00.tabrif.bulk.DivisaHome;
import it.cnr.contab.ordmag.anag00.NumerazioneMagBulk;
import it.cnr.contab.ordmag.ejb.NumeratoriOrdMagComponentSession;
import it.cnr.contab.ordmag.magazzino.bulk.BollaScaricoMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.BollaScaricoMagHome;
import it.cnr.contab.ordmag.magazzino.bulk.MovimentiMagBulk;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.ordmag.ordini.bulk.*;
import it.cnr.contab.ordmag.ordini.ejb.OrdineAcqComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvasioneOrdineComponent extends it.cnr.jada.comp.CRUDComponent implements ICRUDMgr,Cloneable,Serializable {

	public final static String TIPO_TOTALE_COMPLETO = "C";
	public final static String TIPO_TOTALE_PARZIALE = "P";
	private final static int INSERIMENTO = 1;

	public EvasioneOrdineBulk cercaOrdini(UserContext usercontext, EvasioneOrdineBulk filtro) throws ComponentException {
		OrdineAcqConsegnaHome home = (OrdineAcqConsegnaHome) getHome(usercontext, OrdineAcqConsegnaBulk.class, null, getFetchPolicyName("cercaOrdini"));
		it.cnr.jada.persistency.sql.SQLBuilder sql = ricercaOrdini(usercontext, filtro, home);

		try {
			BulkList<OrdineAcqConsegnaBulk> consegne = new BulkList<>(home.fetchAll(sql));
			getHomeCache(usercontext).fetchAll(usercontext, home);
			filtro.setRigheConsegnaDaEvadereColl(consegne);
			return filtro;
		} catch (PersistencyException e) {
			throw new ComponentException(e);
		}
	}

	private it.cnr.jada.persistency.sql.SQLBuilder ricercaOrdini(UserContext context,
																 EvasioneOrdineBulk filtro, OrdineAcqConsegnaHome home) throws ApplicationException {

		if (filtro.getDataBolla() == null) {
			throw new it.cnr.jada.comp.ApplicationException("E' necessario valorizzare la data della bolla.");
		}
		if (filtro.getNumeroBolla() == null) {
			throw new it.cnr.jada.comp.ApplicationException("E' necessario valorizzare il numero della bolla.");
		}
		if (filtro.getDataConsegna() == null) {
			throw new it.cnr.jada.comp.ApplicationException("E' necessario valorizzare la data di consegna.");
		}
		if (filtro.getCdMagazzino() == null) {
			throw new it.cnr.jada.comp.ApplicationException("E' necessario valorizzare il magazzino.");
		}

		it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();

		sql.setAutoJoins(true);

		sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_CONSEGNA.STATO_FATT", SQLBuilder.EQUALS, OrdineAcqConsegnaBulk.STATO_FATT_NON_ASSOCIATA);
		sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_CONSEGNA.STATO", SQLBuilder.EQUALS, OrdineAcqConsegnaBulk.STATO_INSERITA);
		sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_CONSEGNA.PG_OBBLIGAZIONE", SQLBuilder.ISNOTNULL, null);

		sql.generateJoin("ordineAcqRiga", "ORDINE_ACQ_RIGA");

		sql.generateJoin(OrdineAcqRigaBulk.class, OrdineAcqBulk.class, "ordineAcq", "ORDINE_ACQ");
		sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_DEFINITIVO);

		Optional.ofNullable(filtro.getNumerazioneMag())
				.map(NumerazioneMagBulk::getCdMagazzino)
				.ifPresent(cdMagazzino -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_CONSEGNA.CD_MAGAZZINO", SQLBuilder.EQUALS, cdMagazzino));

		Optional.ofNullable(filtro.getFind_data_ordine())
				.ifPresent(findDataOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE", SQLBuilder.EQUALS, findDataOrdine));

		Optional.ofNullable(filtro.getFind_cd_numeratore_ordine())
				.ifPresent(findCdNumeratoreOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_NUMERATORE", SQLBuilder.EQUALS, findCdNumeratoreOrdine));

		Optional.ofNullable(filtro.getFind_esercizio_ordine())
				.ifPresent(findEsercizioOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.ESERCIZIO", SQLBuilder.EQUALS, findEsercizioOrdine));

		Optional.ofNullable(filtro.getFind_numero_ordine())
				.ifPresent(findNumeroOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.NUMERO", SQLBuilder.EQUALS, findNumeroOrdine));

		Optional.ofNullable(filtro.getFind_riga_ordine())
				.ifPresent(findRigaOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_RIGA.RIGA", SQLBuilder.EQUALS, findRigaOrdine));

		Optional.ofNullable(filtro.getFind_consegna_ordine())
				.ifPresent(findConsegnaOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_CONSEGNA.CONSEGNA", SQLBuilder.EQUALS, findConsegnaOrdine));

		Optional.ofNullable(filtro.getFind_cd_uop_ordine())
				.ifPresent(findCdUopOrdine -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_UOP_ORDINE", SQLBuilder.EQUALS, findCdUopOrdine));

		sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE", SQLBuilder.LESS_EQUALS, filtro.getDataConsegna());

		if (filtro.getFind_cd_precedente() != null || filtro.getFind_cd_terzo() != null || filtro.getFind_ragione_sociale() != null) {
			sql.generateJoin(OrdineAcqBulk.class, TerzoBulk.class, "fornitore", "TERZO");

			Optional.ofNullable(filtro.getFind_cd_terzo())
					.ifPresent(findCdTerzo -> sql.addSQLClause(FindClause.AND, "TERZO.CD_TERZO", SQLBuilder.EQUALS, findCdTerzo));

			Optional.ofNullable(filtro.getFind_cd_precedente())
					.ifPresent(findCdPrecedente -> sql.addSQLClause(FindClause.AND, "TERZO.CD_PRECEDENTE", SQLBuilder.EQUALS, findCdPrecedente));

			Optional.ofNullable(filtro.getFind_ragione_sociale())
					.ifPresent(findRagioneSociale -> {
								sql.generateJoin(TerzoBulk.class, AnagraficoBulk.class, "anagrafico", "ANAGRAFICO");
								sql.addSQLClause(FindClause.AND, "ANAGRAFICO.RAGIONE_SOCIALE", SQLBuilder.LIKE, "%" + findRagioneSociale.toUpperCase() + "%");
							}
					);
		}


		return sql;
	}

	public RemoteIterator preparaQueryBolleScaricoDaVisualizzare(UserContext userContext, List<BollaScaricoMagBulk> bolle) throws ComponentException {
		BollaScaricoMagHome homeBolla = (BollaScaricoMagHome) getHome(userContext, BollaScaricoMagBulk.class);
		return iterator(userContext,
				homeBolla.selectBolleGenerate(bolle),
				BollaScaricoMagBulk.class,
				"dafault");
	}

	private DivisaBulk getEuro(UserContext userContext) throws ComponentException {
		try {
			DivisaBulk divisaDefault = ((DivisaHome) getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);
			Optional.ofNullable(divisaDefault)
					.map(DivisaBulk::getCd_divisa)
					.orElseThrow(() -> new it.cnr.jada.comp.ApplicationException("Impossibile caricare la valuta di default! Prima di poter inserire un ordine, immettere tale valore."));
			return divisaDefault;
		} catch (javax.ejb.EJBException | PersistencyException e) {
			handleException(e);
		}
		return null;
	}

	public List<BollaScaricoMagBulk> evadiOrdine(UserContext userContext, EvasioneOrdineBulk evasioneOrdine) throws ComponentException, PersistencyException {
		try {
			OrdineAcqComponentSession ordineComponent = Utility.createOrdineAcqComponentSession();
			MovimentiMagComponentSession movimentiMagComponent = Utility.createMovimentiMagComponentSession();

			List<OrdineAcqConsegnaBulk> listaConsegneDaForzare = new ArrayList<>();
			List<BollaScaricoMagBulk> listaBolleScarico = new ArrayList<>();
			List<MovimentiMagBulk> listaMovimentiScarico = new ArrayList<>();

			validaEvasioneOrdine(userContext,evasioneOrdine);


			final List<OrdineAcqConsegnaBulk> consegneColl = evasioneOrdine.getRigheConsegnaSelezionate();
			OrdineAcqConsegnaHome consegnaHome = (OrdineAcqConsegnaHome) getHome(userContext, OrdineAcqConsegnaBulk.class);

			final Map<OrdineAcqBulk, List<OrdineAcqConsegnaBulk>> mapOrdine =
					consegneColl.stream().collect(Collectors.groupingBy(o -> o.getOrdineAcqRiga().getOrdineAcq()));

			for (OrdineAcqBulk ordineSelected : mapOrdine.keySet()) {
				for (OrdineAcqConsegnaBulk consegnaSelected : consegneColl) {
					Optional.ofNullable(consegnaSelected.getQuantitaEvasa()).filter(el->el.compareTo(BigDecimal.ZERO)>0)
							.orElseThrow(()->new DetailedRuntimeException("Indicare la quantità da evadere per la consegna " + consegnaSelected.getConsegnaOrdineString()));
					if (consegnaSelected.isQuantitaEvasaMinoreOrdine() && consegnaSelected.getOperazioneQuantitaEvasaMinore() == null)
						throw new DetailedRuntimeException("Per la consegna " + consegnaSelected.getConsegnaOrdineString() + " è necessario indicare se bisogna solo sdoppiare la riga o anche evaderla forzatamente");
					if (consegnaSelected.isQuantitaEvasaMaggioreOrdine())
						throw new DetailedRuntimeException("La quantità evasa della consegna " + consegnaSelected.getConsegnaOrdineString() + " non può essere maggiore di quella ordinata.");


					//Ricarico la consegna dal DB per verificare che non sia stata già evasa
					OrdineAcqConsegnaBulk consegnaDB = (OrdineAcqConsegnaBulk) findByPrimaryKey(userContext, consegnaSelected);
					if (consegnaDB.isStatoConsegnaEvasa())
						throw new DetailedRuntimeException("La consegna " + consegnaDB.getConsegnaOrdineString() + " è stata già evasa");

					if (consegnaSelected.isQuantitaEvasaMinoreOrdine()) {
						consegnaDB.setQuantitaOrig(consegnaSelected.getQtEvasaConvertita());
						consegnaDB.setQuantita(consegnaDB.getQuantitaOrig().divide(consegnaSelected.getOrdineAcqRiga().getCoefConv(),5,RoundingMode.HALF_UP));

						//Ricalcolo i campi imponibile e imposta, calcolati in base alla vecchia quantità (consegnaSelected.getQuantita()) in proporzione alla nuova quantita (consegnaDB.getQuantita())
						consegnaDB.setImImponibile(consegnaSelected.getImImponibile().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImIva(consegnaSelected.getImIva().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImImponibileDivisa(consegnaSelected.getImImponibileDivisa().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImIvaDivisa(consegnaSelected.getImIvaDivisa().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImIvaD(consegnaSelected.getImIvaD().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImIvaNd(consegnaSelected.getImIvaNd().multiply(consegnaDB.getQuantita()).divide(consegnaSelected.getQuantita(),2,RoundingMode.HALF_UP));
						consegnaDB.setImTotaleConsegna(consegnaDB.getImImponibile().add(consegnaDB.getImIva()));

						//Creo una nuova consegna
						OrdineAcqConsegnaBulk newConsegna = (OrdineAcqConsegnaBulk) consegnaDB.clone();
						newConsegna = (OrdineAcqConsegnaBulk) newConsegna.inizializza(userContext);
						newConsegna.setVecchiaConsegna(consegnaDB.getConsegna());
						newConsegna.setDtPrevConsegna(consegnaDB.getDtPrevConsegna());
						newConsegna.setQuantitaOrig(consegnaSelected.getQtConvertita().subtract(consegnaDB.getQuantitaOrig()));
						newConsegna.setQuantita(consegnaSelected.getQuantita().subtract(consegnaDB.getQuantita()));
						newConsegna.setImImponibile(consegnaSelected.getImImponibile().subtract(consegnaDB.getImImponibile()));
						newConsegna.setImIva(consegnaSelected.getImIva().subtract(consegnaDB.getImIva()));
						newConsegna.setImImponibileDivisa(consegnaSelected.getImImponibileDivisa().subtract(consegnaDB.getImImponibileDivisa()));
						newConsegna.setImIvaDivisa(consegnaSelected.getImIvaDivisa().subtract(consegnaDB.getImIvaDivisa()));
						newConsegna.setImIvaD(consegnaSelected.getImIvaD().subtract(consegnaDB.getImIvaD()));
						newConsegna.setImIvaNd(consegnaSelected.getImIvaNd().subtract(consegnaDB.getImIvaNd()));
						newConsegna.setImTotaleConsegna(newConsegna.getImImponibile().add(newConsegna.getImIva()));

						//Ripulisco la chiave (valorizzata dall'istruzione clone) altrimenti non crea la nuova consegna
						newConsegna.setConsegna(null);
						newConsegna.setCrudStatus(OggettoBulk.TO_BE_CREATED);
						makeBulkPersistent(userContext, newConsegna);

						if (consegnaSelected.isOperazioneEvasaForzata())
							listaConsegneDaForzare.add(newConsegna);
					}

					consegnaDB.setStato(OrdineAcqConsegnaBulk.STATO_EVASA);
					consegnaDB.setToBeUpdated();
					makeBulkPersistent(userContext, consegnaDB);
				}

				//Ricarico l'ordine
				OrdineAcqBulk ordineComp = ((OrdineAcqHome)getHome(userContext, OrdineAcqBulk.class)).initializeBulkForEdit(ordineSelected);
				getHomeCache(userContext).fetchAll(userContext);

				for (OrdineAcqConsegnaBulk consegnaSelected : consegneColl) {
					//recupero la riga nell'ambito dell'ordine ricaricato
					OrdineAcqConsegnaBulk finalConsegnaEvasa = consegnaSelected;
					OrdineAcqConsegnaBulk consegnaCompSelected = ordineComp.getRigheOrdineColl().stream().flatMap(el -> el.getRigheConsegnaColl().stream()).filter(el -> el.equalsByPrimaryKey(finalConsegnaEvasa))
							.findAny().orElseThrow(() -> new DetailedRuntimeException("Attenzione! Non è stato possibile individuare nell'ordine una riga di consegna che risulta essere stata evasa. Aprire " +
									"una segnalazione HelpDesk!"));

					//Riallineo i campi del lotto fornitore noBaseTable di consegnaCompSelected con quelli presenti in consegnaSelected
					consegnaCompSelected.setLottoFornitore(consegnaSelected.getLottoFornitore());
					consegnaCompSelected.setDtScadenza(consegnaSelected.getDtScadenza());

					//carico l'evasione
					EvasioneOrdineRigaBulk evasioneOrdineRiga = new EvasioneOrdineRigaBulk();
					evasioneOrdineRiga.setOrdineAcqConsegna(consegnaCompSelected);
					evasioneOrdineRiga.setStato(OrdineAcqConsegnaBulk.STATO_INSERITA);
					evasioneOrdineRiga.setQuantitaEvasa(consegnaCompSelected.getQuantita());

					evasioneOrdineRiga.setQuantitaEvasaBolla(consegnaSelected.getQuantitaEvasa());
					evasioneOrdineRiga.setUnitaMisuraEvasaBolla(consegnaSelected.getUnitaMisuraEvasa());
					evasioneOrdineRiga.setCoefConvEvasaBolla(consegnaSelected.getCoefConvEvasa());

					evasioneOrdineRiga.setToBeCreated();

					evasioneOrdine.addToEvasioneOrdineRigheColl(evasioneOrdineRiga);
					//effettuo la movimentazione di magazzino
					try {
						Optional.ofNullable(movimentiMagComponent.caricoDaOrdine(userContext, evasioneOrdineRiga.getOrdineAcqConsegna(), evasioneOrdineRiga))
								.ifPresent(
										movimentoCarico -> {
											if (movimentoCarico.getMovimentoRif() != null) {
												listaMovimentiScarico.add(movimentoCarico);
											}
											evasioneOrdineRiga.setMovimentiMag(movimentoCarico);
										});
					} catch (ComponentException | RemoteException | PersistencyException e) {
						throw new DetailedRuntimeException(e);
					}
				}
			}

			if (!evasioneOrdine.getEvasioneOrdineRigheColl().isEmpty()) {
				evasioneOrdine.setStato(OrdineAcqConsegnaBulk.STATO_INSERITA);
				evasioneOrdine.setToBeCreated();
				//rendo permanente l'evasione ordine
				assegnaProgressivo(userContext, evasioneOrdine);
				creaConBulk(userContext, evasioneOrdine);
			}

			if (!listaMovimentiScarico.isEmpty()) {
				try {
					listaBolleScarico = movimentiMagComponent.generaBolleScarico(userContext, listaMovimentiScarico);
				} catch (RemoteException e) {
					throw handleException(e);
				}
			}

			//Chiudo forzatamente le consegne richieste
			for (OrdineAcqConsegnaBulk consegnaDaForzare : listaConsegneDaForzare)
				ordineComponent.chiusuraForzataOrdini(userContext, consegnaDaForzare);

			return listaBolleScarico;
		} catch (DetailedRuntimeException|RemoteException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	private void validaEvasioneOrdine(UserContext userContext,EvasioneOrdineBulk evasioneOrdine) throws ApplicationException {
		if(evasioneOrdine.getDataConsegna() != null){
			java.util.Calendar gc = java.util.Calendar.getInstance();
			gc.setTime(evasioneOrdine.getDataConsegna());
			int annoCompetenza = gc.get(java.util.Calendar.YEAR);
			int esScrivania = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext).intValue();

			if (annoCompetenza != esScrivania) {
				throw new ApplicationException("La \"Data Consegna\" deve ricadere nell'esercizio selezionato!");
			}

			if(evasioneOrdine.getDataBolla() != null && evasioneOrdine.getDataBolla().compareTo(evasioneOrdine.getDataConsegna()) > 0){
				throw new ApplicationException("La \"Data Bolla\" non può essere maggiore della \"Data Consegna\" ");
			}
		}
	}

	private void assegnaProgressivo(UserContext userContext, EvasioneOrdineBulk evasioneOrdine) throws ComponentException {
		try {
			NumeratoriOrdMagComponentSession progressiviSession = Utility.createNumeratoriOrdMagComponentSession();
			NumerazioneMagBulk numerazione = new NumerazioneMagBulk(evasioneOrdine.getCdCds(), evasioneOrdine.getCdMagazzino(), evasioneOrdine.getEsercizio(), evasioneOrdine.getCdNumeratoreMag());
			evasioneOrdine.setNumero(progressiviSession.getNextPG(userContext, numerazione));
		} catch (Exception t) {
			throw handleException(evasioneOrdine, t);
		}
	}

	public RemoteIterator ricercaEvasioni(UserContext userContext, OrdineAcqConsegnaBulk ordineAcqConsegnaBulk) throws ComponentException {
		final BulkHome home = getHome(userContext, EvasioneOrdineRigaBulk.class);
		final SQLBuilder sqlBuilder = home.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "ordineAcqConsegna", SQLBuilder.EQUALS, ordineAcqConsegnaBulk);
		return  iterator(userContext, sqlBuilder, EvasioneOrdineRigaBulk.class, null);
	}
}