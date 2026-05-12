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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.contratto.bulk.Dettaglio_contrattoBulk;
import it.cnr.contab.config00.contratto.bulk.Dettaglio_contrattoHome;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaHome;
import it.cnr.contab.docamm00.tabrif.bulk.AssCatgrpInventVoceEpBulk;
import it.cnr.contab.docamm00.tabrif.bulk.AssCatgrpInventVoceEpHome;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioHome;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class OrdineAcqRigaHome extends BulkHome {
	public OrdineAcqRigaHome(Connection conn) {
		super(OrdineAcqRigaBulk.class, conn);
	}
	public OrdineAcqRigaHome(Connection conn, PersistentCache persistentCache) {
		super(OrdineAcqRigaBulk.class, conn, persistentCache);
	}

	public Dettaglio_contrattoBulk recuperoDettaglioContratto(OrdineAcqRigaBulk riga) throws PersistencyException {
		if (riga == null || riga.getOrdineAcq() == null || riga.getOrdineAcq().getContratto() == null|| riga.getOrdineAcq().getContratto().getPg_contratto() == null
				|| riga.getBeneServizio() == null ||riga.getOrdineAcq().getContratto().getTipo_dettaglio_contratto() == null){
			return null;
		}
		ContrattoBulk contratto = riga.getOrdineAcq().getContratto();
		Dettaglio_contrattoHome home = ((Dettaglio_contrattoHome)getHomeCache().getHome(Dettaglio_contrattoBulk.class));
		SQLBuilder sql = home.createSQLBuilder();
		sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.STATO", SQLBuilder.NOT_EQUALS, Dettaglio_contrattoBulk.STATO_ANNULLATO);
		sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.PG_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getPg_contratto());
		sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.ESERCIZIO_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getEsercizio());
		sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.STATO_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getStato());
			if (contratto.isDettaglioContrattoPerArticoli()){
				sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.CD_BENE_SERVIZIO", SQLBuilder.EQUALS, riga.getBeneServizio().getCd_bene_servizio());
			} else if (contratto.isDettaglioContrattoPerCategoriaGruppo()){
				Bene_servizioHome homeBene = ((Bene_servizioHome)getHomeCache().getHome(Bene_servizioBulk.class));
				Bene_servizioBulk bene_servizioBulk = (Bene_servizioBulk)homeBene.findByPrimaryKey(riga.getBeneServizio());
				if (bene_servizioBulk != null){
					sql.addSQLClause("AND", "DETTAGLIO_CONTRATTO.CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, bene_servizioBulk.getCategoria_gruppo().getCd_categoria_gruppo());
				} else {
					return null;
				}
			}
		Collection righe = home.fetchAll(sql);
		if (righe != null && righe.size() == 1){
			return (Dettaglio_contrattoBulk) righe.iterator().next();
		}
		return null;
	}

	public SQLBuilder selectDspUopDestByClause(UserContext userContext, OrdineAcqRigaBulk riga,
													  UnitaOperativaOrdHome unitaOperativaHome, UnitaOperativaOrdBulk unitaOperativaBulk,
													  CompoundFindClause compoundfindclause) throws PersistencyException{
		SQLBuilder sql = unitaOperativaHome.selectByClause(userContext, compoundfindclause);

		sql.addSQLClause("AND","UNITA_OPERATIVA_ORD.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));

		return sql;
	}

	public List<OrdineAcqRigaEcoBulk> findOrdineAcqRigaEcoList(OrdineAcqRigaBulk rigaOrdine) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(OrdineAcqRigaEcoBulk.class);

		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, rigaOrdine.getNumero());
		sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, rigaOrdine.getCdCds());
		sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, rigaOrdine.getCdUnitaOperativa());
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, rigaOrdine.getEsercizio());
		sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, rigaOrdine.getCdNumeratore());
		sql.addClause(FindClause.AND, "riga", SQLBuilder.EQUALS, rigaOrdine.getRiga());
		return home.fetchAll(sql);
	}

	public List<OrdineAcqConsegnaBulk> findOrdineRigheConsegnaList(OrdineAcqRigaBulk ordineRiga) throws PersistencyException {
		PersistentHome consegnaHome = getHomeCache().getHome(OrdineAcqConsegnaBulk.class);
		SQLBuilder sqlConsegna = consegnaHome.createSQLBuilder();
		sqlConsegna.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, ordineRiga.getNumero());
		sqlConsegna.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, ordineRiga.getCdCds());
		sqlConsegna.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, ordineRiga.getCdUnitaOperativa());
		sqlConsegna.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, ordineRiga.getEsercizio());
		sqlConsegna.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, ordineRiga.getCdNumeratore());
		sqlConsegna.addClause(FindClause.AND, "riga", SQLBuilder.EQUALS, ordineRiga.getRiga());
		sqlConsegna.addOrderBy("consegna");
		return consegnaHome.fetchAll(sqlConsegna);
	}

	protected ContoBulk getContoEconomicoDefault(OrdineAcqRigaBulk ordineRiga) throws ComponentException {
		try {
			Fattura_passivaHome fatpasHome = (Fattura_passivaHome) getHomeCache().getHome(Fattura_passivaBulk.class);

			if (Optional.ofNullable(ordineRiga).isPresent()) {
				//verifico se sulla riga del docamm ci sia un bene inventariabile
				Bene_servizioBulk myBeneServizio = (Bene_servizioBulk) fatpasHome.loadIfNeededObject(ordineRiga.getBeneServizio());

				if (Optional.ofNullable(myBeneServizio.getCd_categoria_gruppo()).isPresent()) {
					AssCatgrpInventVoceEpHome assCatgrpInventVoceEpHome = (AssCatgrpInventVoceEpHome) getHomeCache().getHome(AssCatgrpInventVoceEpBulk.class);
					AssCatgrpInventVoceEpBulk result = assCatgrpInventVoceEpHome.findDefaultByCategoria(ordineRiga.getEsercizio(), myBeneServizio.getCd_categoria_gruppo());
					if (Optional.ofNullable(result).flatMap(el->Optional.ofNullable(el.getConto())).isPresent())
						return result.getConto();
				}

				//se arrivo qui devo guardare alla voce delle obbligazioni agganciate alla consegna
				Obbligazione_scadenzarioBulk obbligScad = this.findOrdineRigheConsegnaList(ordineRiga).stream()
						.map(OrdineAcqConsegnaBulk::getObbligazioneScadenzario)
						.filter(Objects::nonNull)
						.findFirst().orElse(null);

				if (Optional.ofNullable(obbligScad).isPresent()) {
					ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
					Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
					List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(obblig.getElemento_voce());
					return Optional.ofNullable(listAss).orElse(new ArrayList<>())
							.stream().map(Ass_ev_voceepBulk::getVoce_ep)
							.findAny().orElse(null);
				} else {
					Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
					return configHome.getContoDocumentoNonLiquidabile(ordineRiga);
				}
			}
			return null;
		} catch (PersistencyException e) {
			throw new DetailedRuntimeException(e);
		}
	}

	protected List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiDefault(UserContext userContext, OrdineAcqRigaBulk rigaOrdine, ContoBulk aContoEconomico, boolean includeDatiConsegna) throws ComponentException {
		try {
			List<IDocumentoDetailAnaCogeBulk> result = new ArrayList<>();
			List<IDocumentoDetailAnaCogeBulk> resultConsegne = new ArrayList<>();

			if (Optional.ofNullable(aContoEconomico).isPresent()) {
				OrdineAcqConsegnaHome consegnaHome = (OrdineAcqConsegnaHome)getHomeCache().getHome(OrdineAcqConsegnaBulk.class);
				List<OrdineAcqConsegnaBulk> consegne = this.findOrdineRigheConsegnaList(rigaOrdine);
				for (OrdineAcqConsegnaBulk consegna : consegne) {
					List<IDocumentoDetailAnaCogeBulk> datiAnaliticiConsegna = consegnaHome.findOrdineAcqConsegnaEcoList(consegna).stream()
							.map(IDocumentoDetailAnaCogeBulk.class::cast)
							.collect(Collectors.toList());
					if (datiAnaliticiConsegna.isEmpty())
						datiAnaliticiConsegna = consegnaHome.getDatiAnaliticiDefault(userContext, consegna, aContoEconomico);
					resultConsegne.addAll(datiAnaliticiConsegna);
					datiAnaliticiConsegna.forEach(consegnaEco->{
						IDocumentoDetailAnaCogeBulk ordineRigaEco = result.stream()
								.filter(rigaEco->Optional.ofNullable(rigaEco.getEsercizio_voce_ana()).equals(Optional.ofNullable(consegnaEco.getEsercizio_voce_ana())))
								.filter(rigaEco->Optional.ofNullable(rigaEco.getCd_voce_ana()).equals(Optional.ofNullable(consegnaEco.getCd_voce_ana())))
								.filter(rigaEco->rigaEco.getCd_centro_responsabilita().equals(consegnaEco.getCd_centro_responsabilita()))
								.filter(rigaEco->rigaEco.getCd_linea_attivita().equals(consegnaEco.getCd_linea_attivita()))
								.findAny()
								.orElseGet(()->{
									OrdineAcqRigaEcoBulk myRigaEco = new OrdineAcqRigaEcoBulk();
									myRigaEco.setProgressivo_riga_eco((long) (result.size() + 1));
									myRigaEco.setVoce_analitica(consegnaEco.getVoce_analitica());
									myRigaEco.setLinea_attivita(consegnaEco.getLinea_attivita());
									myRigaEco.setOrdineAcqRiga(rigaOrdine);
									myRigaEco.setImporto(BigDecimal.ZERO);
									myRigaEco.setToBeCreated();
									result.add(myRigaEco);
									return myRigaEco;
								});
						((OrdineAcqRigaEcoBulk)ordineRigaEco).setImporto(ordineRigaEco.getImporto().add(consegnaEco.getImporto()));
					});
				}
			}
			if (includeDatiConsegna)
				result.addAll(resultConsegne);
			return result.stream()
					.filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
					.collect(Collectors.toList());
		} catch (PersistencyException ex) {
			throw new ComponentException(ex);
		}
	}

    public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, OrdineAcqRigaBulk rigaOrdine, boolean includeDatiConsegna) throws ComponentException {
		ContoBulk aContoEconomico = this.getContoEconomicoDefault(rigaOrdine);
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, rigaOrdine, aContoEconomico, includeDatiConsegna);
		return Pair.of(aContoEconomico, aContiAnalitici);
	}

	public Pair<ContoBulk, List<IDocumentoDetailAnaCogeBulk>> getDatiEconomici(OrdineAcqRigaBulk rigaOrdine) throws PersistencyException {
		ContoBulk aContoEconomico = rigaOrdine.getVoce_ep();
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.findOrdineAcqRigaEcoList(rigaOrdine).stream()
				.map(IDocumentoDetailAnaCogeBulk.class::cast)
				.collect(Collectors.toList());
		return Pair.of(aContoEconomico, aContiAnalitici);
	}
}