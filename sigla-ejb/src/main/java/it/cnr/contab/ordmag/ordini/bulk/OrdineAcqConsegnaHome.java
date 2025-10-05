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

/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 28/06/2017
 */
package it.cnr.contab.ordmag.ordini.bulk;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scad_voceBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioHome;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.springframework.data.util.Pair;

public class OrdineAcqConsegnaHome extends BulkHome {
	public OrdineAcqConsegnaHome(Connection conn) {
		super(OrdineAcqConsegnaBulk.class, conn);
	}
	public OrdineAcqConsegnaHome(Connection conn, PersistentCache persistentCache) {
		super(OrdineAcqConsegnaBulk.class, conn, persistentCache);
	}
	public SQLBuilder selectUnitaOperativaOrdByClause(UserContext userContext, OrdineAcqConsegnaBulk cons,
											   UnitaOperativaOrdHome unitaOperativaHome, UnitaOperativaOrdBulk unitaOperativaBulk,
											   CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = unitaOperativaHome.selectByClause(userContext, compoundfindclause);

		sql.addSQLClause("AND","UNITA_OPERATIVA_ORD.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));

		return sql;
	}

	public List<OrdineAcqConsegnaEcoBulk> findOrdineAcqConsegnaEcoList(OrdineAcqConsegnaBulk consegna) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(OrdineAcqConsegnaEcoBulk.class);

		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, consegna.getNumero());
		sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, consegna.getCdCds());
		sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, consegna.getCdUnitaOperativa());
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, consegna.getEsercizio());
		sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, consegna.getCdNumeratore());
		sql.addClause(FindClause.AND, "riga", SQLBuilder.EQUALS, consegna.getRiga());
		sql.addClause(FindClause.AND, "consegna", SQLBuilder.EQUALS, consegna.getConsegna());
		return home.fetchAll(sql);
	}

	/**
	 * Questo metodo fa in modo di gestire l'inizializzazione della chiave primaria
	 * in automatico, ma si limita ad inizializzare il campo "consegna" in quanto il resto
	 * della chiave è stato già valorizzato dal trasporto di chiave dal padre
	 */
	public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, ComponentException {
		if (bulk instanceof OrdineAcqConsegnaBulk) {
			OrdineAcqConsegnaBulk ordineAcqConsegnaBulk = (OrdineAcqConsegnaBulk)bulk;
			if (ordineAcqConsegnaBulk.getConsegna()==null) {
				Integer max = (Integer)findMax(bulk, "consegna", null);
				ordineAcqConsegnaBulk.setConsegna(Optional.ofNullable(max).orElse(0)+1);
			}
		}
	}

	public ContoBulk getContoEconomicoDefault(OrdineAcqConsegnaBulk consegna) throws ComponentException {
		Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);

		if (Optional.ofNullable(consegna).isPresent()) {
			//verifico se sulla riga del docamm ci sia un bene inventariabile
			OrdineAcqRigaBulk ordineAcqRigaBulk = (OrdineAcqRigaBulk)fatpasHome.loadIfNeededObject(consegna.getOrdineAcqRiga());
			ContoBulk conto = ordineAcqRigaBulk.getVoce_ep();
			if (conto == null)
				return ((OrdineAcqRigaHome)getHomeCache().getHome(OrdineAcqRigaBulk.class))
						.getContoEconomicoDefault(ordineAcqRigaBulk);
            return conto;
		}
		return null;
	}

	public List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiDefault(UserContext userContext, OrdineAcqConsegnaBulk consegna, ContoBulk aContoEconomico) throws ComponentException {
		try {
			List<OrdineAcqConsegnaEcoBulk> result = new ArrayList<>();
			if (Optional.ofNullable(aContoEconomico).isPresent() && !consegna.isStatoConsegnaEvasaForzatamente()) {
				List<Voce_analiticaBulk> voceAnaliticaList = ((Voce_analiticaHome) getHomeCache().getHome(Voce_analiticaBulk.class)).findVoceAnaliticaList(aContoEconomico);
				if (voceAnaliticaList.isEmpty())
					return new ArrayList<>();
				Voce_analiticaBulk voceAnaliticaDef = voceAnaliticaList.stream()
						.filter(Voce_analiticaBulk::getFl_default).findAny()
						.orElse(voceAnaliticaList.stream().findAny().orElse(null));

				if (!Optional.ofNullable(consegna.getScadenzaDocumentoContabile()).isPresent()) {
					OrdineAcqRigaHome ordineAcqRigaHome = (OrdineAcqRigaHome) getHomeCache().getHome(OrdineAcqRigaBulk.class);
					List<IDocumentoDetailAnaCogeBulk> datiAnaliticiRiga = ordineAcqRigaHome.getDatiEconomici(consegna.getOrdineAcqRiga()).getSecond();
					if (datiAnaliticiRiga.isEmpty())
						throw new ApplicationException("Errore nei dati: non esiste ripartizione analitica sulla riga dell'ordine!");
				} else if (Optional.ofNullable(consegna.getScadenzaDocumentoContabile()).filter(Obbligazione_scadenzarioBulk.class::isInstance).isPresent()) {
					//carico i dettagli analitici recuperandoli dall'obbligazione_scad_voce
					Obbligazione_scadenzarioHome obbligazioneScadenzarioHome = (Obbligazione_scadenzarioHome) getHomeCache().getHome(Obbligazione_scadenzarioBulk.class);
					List<Obbligazione_scad_voceBulk> scadVoceBulks = obbligazioneScadenzarioHome.findObbligazione_scad_voceList(userContext, (Obbligazione_scadenzarioBulk) consegna.getScadenzaDocumentoContabile());
					BigDecimal totScad = scadVoceBulks.stream().map(Obbligazione_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
					for (Obbligazione_scad_voceBulk scadVoce : scadVoceBulks) {
						OrdineAcqConsegnaEcoBulk myRigaEco = new OrdineAcqConsegnaEcoBulk();
						myRigaEco.setProgressivo_riga_eco((long) (consegna.getChildrenAna().size() + 1));
						myRigaEco.setVoce_analitica(voceAnaliticaDef);
						myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
						myRigaEco.setOrdineAcqConsegna(consegna);
						if (totScad.compareTo(BigDecimal.ZERO)!=0)
							myRigaEco.setImporto(scadVoce.getIm_voce().multiply(consegna.getImCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
						else
							myRigaEco.setImporto(consegna.getImCostoEco().divide(BigDecimal.valueOf(scadVoceBulks.size()), 2, RoundingMode.HALF_UP));
						myRigaEco.setToBeCreated();
						result.add(myRigaEco);
					}
					BigDecimal totRipartito = result.stream().map(OrdineAcqConsegnaEcoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
					BigDecimal diff = totRipartito.subtract(consegna.getImCostoEco());

					if (diff.compareTo(BigDecimal.ZERO)>0) {
						for (OrdineAcqConsegnaEcoBulk rigaEco : result) {
							if (rigaEco.getImporto().compareTo(diff) >= 0) {
								rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
								break;
							} else {
								diff = diff.subtract(rigaEco.getImporto());
								rigaEco.setImporto(BigDecimal.ZERO);
							}
						}
					} else if (diff.compareTo(BigDecimal.ZERO)<0) {
						for (OrdineAcqConsegnaEcoBulk rigaEco : result) {
							rigaEco.setImporto(rigaEco.getImporto().add(diff));
							break;
						}
					}
				}
			}
			return result.stream()
					.filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
					.collect(Collectors.toList());
		} catch (PersistencyException ex) {
			throw new ComponentException(ex);
		}
	}

	public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, OrdineAcqConsegnaBulk consegna) throws ComponentException {
		ContoBulk aContoEconomico = this.getContoEconomicoDefault(consegna);
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, consegna, aContoEconomico);
		return Pair.of(aContoEconomico, aContiAnalitici);
	}

	public Pair<ContoBulk, List<IDocumentoDetailAnaCogeBulk>> getDatiEconomici(OrdineAcqConsegnaBulk consegna) throws PersistencyException {
		ContoBulk aContoEconomico = consegna.getVoce_ep();
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.findOrdineAcqConsegnaEcoList(consegna).stream()
				.map(IDocumentoDetailAnaCogeBulk.class::cast)
				.collect(Collectors.toList());
		return Pair.of(aContoEconomico, aContiAnalitici);
	}
}