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

package it.cnr.contab.docamm00.docs.bulk;
/**
 * Insert the type's description here.
 * Creation date: (9/5/2001 5:02:18 PM)
 * @author: Ardire Alfonso
 */
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Fattura_attiva_rigaHome extends BulkHome {
	public Fattura_attiva_rigaHome(Class classe, java.sql.Connection conn) {
		super(classe,conn);
	}

	public Fattura_attiva_rigaHome(Class classe, java.sql.Connection conn,PersistentCache persistentCache) {
		super(classe,conn,persistentCache);
	}

	public Fattura_attiva_rigaHome(java.sql.Connection conn) {
		super(Fattura_attiva_rigaBulk.class,conn);
	}

	public Fattura_attiva_rigaHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Fattura_attiva_rigaBulk.class,conn,persistentCache);
	}

	public java.util.List findAddebitiForAccertamentoExceptFor(
		it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk scadenza,
		Fattura_attivaBulk fattura)
		throws PersistencyException {

		return fetchAll(selectForAccertamentoExceptFor(scadenza, fattura));
	}

	public java.util.List findStorniForAccertamentoExceptFor(
		it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk scadenza,
		Fattura_attivaBulk fattura)
		throws PersistencyException {

		return fetchAll(selectForAccertamentoExceptFor(scadenza, fattura));
	}

	/**
	 * Inizializza la chiave primaria di un OggettoBulk per un
	 * inserimento. Da usare principalmente per riempire i progressivi
	 * automatici.
	 */
	public SQLBuilder selectAccertamentiPer(
		it.cnr.jada.UserContext userContext,
		Fattura_attivaBulk fatturaAttiva,
		java.math.BigDecimal minIm_scadenza)
		throws PersistencyException {

		if (fatturaAttiva == null) return null;

		TerzoBulk cliente = fatturaAttiva.getCliente();
		if (cliente != null) {
			SQLBuilder sql = createSQLBuilder();
			sql.addTableToHeader("FATTURA_ATTIVA");
			sql.addTableToHeader("ACCERTAMENTO_SCADENZARIO");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.ESERCIZIO", "FATTURA_ATTIVA.ESERCIZIO");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.CD_CDS", "FATTURA_ATTIVA.CD_CDS");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "FATTURA_ATTIVA.CD_UNITA_ORGANIZZATIVA");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.PG_FATTURA_ATTIVA", "FATTURA_ATTIVA.PG_FATTURA_ATTIVA");

			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.CD_CDS_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.CD_CDS");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.ESERCIZIO_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.ESERCIZIO");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.ESERCIZIO_ORI_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.PG_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO");
			sql.addSQLJoin("FATTURA_ATTIVA_RIGA.PG_ACCERTAMENTO_SCADENZARIO", "ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO");

			sql.addSQLClause("AND","FATTURA_ATTIVA.CD_TERZO",sql.EQUALS, cliente.getCd_terzo());
			sql.addSQLClause("AND","FATTURA_ATTIVA.TI_FATTURA",sql.EQUALS, Fattura_attiva_IBulk.TIPO_FATTURA_ATTIVA);
			sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.STATO_COFI",sql.EQUALS, Fattura_attiva_rigaBulk.STATO_CONTABILIZZATO);
			sql.addSQLClause("AND","FATTURA_ATTIVA.CD_CDS_ORIGINE", sql.EQUALS, fatturaAttiva.getCd_cds_origine());
			sql.addSQLClause("AND","FATTURA_ATTIVA.CD_UO_ORIGINE", sql.EQUALS, fatturaAttiva.getCd_uo_origine());
			sql.addSQLClause("AND","FATTURA_ATTIVA.ESERCIZIO", sql.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));

			sql.addSQLClause("AND","ACCERTAMENTO_SCADENZARIO.ESERCIZIO",sql.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));
			sql.addSQLClause("AND","ACCERTAMENTO_SCADENZARIO.IM_SCADENZA",sql.GREATER_EQUALS, minIm_scadenza);

			return sql;
		}
		return null;
	}

	protected SQLBuilder selectForAccertamentoExceptFor(
		it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk scadenza,
		Fattura_attivaBulk fattura) {

		SQLBuilder sql = createSQLBuilder();

		sql.addTableToHeader("FATTURA_ATTIVA");
		sql.addSQLJoin("FATTURA_ATTIVA_RIGA.ESERCIZIO", "FATTURA_ATTIVA.ESERCIZIO");
		sql.addSQLJoin("FATTURA_ATTIVA_RIGA.CD_CDS", "FATTURA_ATTIVA.CD_CDS");
		sql.addSQLJoin("FATTURA_ATTIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "FATTURA_ATTIVA.CD_UNITA_ORGANIZZATIVA");
		sql.addSQLJoin("FATTURA_ATTIVA_RIGA.PG_FATTURA_ATTIVA", "FATTURA_ATTIVA.PG_FATTURA_ATTIVA");
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.CD_CDS_ACCERTAMENTO",sql.EQUALS, scadenza.getCd_cds());
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.ESERCIZIO_ACCERTAMENTO",sql.EQUALS, scadenza.getEsercizio());
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.ESERCIZIO_ORI_ACCERTAMENTO",sql.EQUALS, scadenza.getEsercizio_originale());
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.PG_ACCERTAMENTO",sql.EQUALS, scadenza.getPg_accertamento());
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.PG_ACCERTAMENTO_SCADENZARIO",sql.EQUALS, scadenza.getPg_accertamento_scadenzario());
		sql.addSQLClause("AND","FATTURA_ATTIVA_RIGA.STATO_COFI",sql.NOT_EQUALS, Fattura_attiva_rigaBulk.STATO_ANNULLATO);

		if (fattura != null) {
			sql.addSQLClause("AND", "FATTURA_ATTIVA.PG_FATTURA_ATTIVA", sql.NOT_EQUALS, fattura.getPg_fattura_attiva());
			sql.addSQLClause("AND", "FATTURA_ATTIVA.CD_CDS", sql.EQUALS, fattura.getCd_cds());
			sql.addSQLClause("AND", "FATTURA_ATTIVA.CD_UNITA_ORGANIZZATIVA", sql.EQUALS, fattura.getCd_unita_organizzativa());
		}

		return sql;
	}

	private SQLBuilder selectRigaFor(Fattura_attiva_rigaIBulk rigaFattura) {

		SQLBuilder sql = createSQLBuilder();

		if (rigaFattura != null) {
			sql.addSQLClause("AND", "FATTURA_ATTIVA_RIGA.CD_CDS_ASSNCNA_FIN", sql.EQUALS, rigaFattura.getCd_cds());
			sql.addSQLClause("AND", "FATTURA_ATTIVA_RIGA.CD_UO_ASSNCNA_FIN", sql.EQUALS, rigaFattura.getCd_unita_organizzativa());
			sql.addSQLClause("AND", "FATTURA_ATTIVA_RIGA.ESERCIZIO_RIGA_ASSNCNA_FIN", sql.EQUALS, rigaFattura.getEsercizio());
			sql.addSQLClause("AND", "FATTURA_ATTIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", sql.EQUALS, rigaFattura.getPg_fattura_attiva());
			sql.addSQLClause("AND", "FATTURA_ATTIVA_RIGA.PG_RIGA_ASSNCNA_FIN", sql.EQUALS, rigaFattura.getProgressivo_riga());
		}
		return sql;
	}

	public java.util.List<Fattura_attiva_riga_ecoBulk> findFatturaAttivaRigheEcoList(Fattura_attiva_rigaBulk docRiga ) throws PersistencyException {
		PersistentHome home;
		if (docRiga instanceof Nota_di_credito_attiva_rigaBulk)
			home = getHomeCache().getHome(Nota_di_credito_attiva_riga_ecoBulk.class);
		else if (docRiga instanceof Nota_di_debito_attiva_rigaBulk)
			home = getHomeCache().getHome(Nota_di_debito_attiva_riga_ecoBulk.class);
		else
			home = getHomeCache().getHome(Fattura_attiva_riga_ecoIBulk.class);

		it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docRiga.getEsercizio());
		sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docRiga.getCd_cds());
		sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docRiga.getCd_unita_organizzativa());
		sql.addClause(FindClause.AND, "pg_fattura_attiva", SQLBuilder.EQUALS, docRiga.getPg_fattura_attiva());
		sql.addClause(FindClause.AND, "progressivo_riga", SQLBuilder.EQUALS, docRiga.getProgressivo_riga());
		return home.fetchAll(sql);
	}

	protected ContoBulk getContoEconomicoDefault(Fattura_attiva_rigaBulk docRiga) {
		try {
			Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
			if (Optional.ofNullable(docRiga).isPresent()) {
				if (Optional.ofNullable(docRiga.getAccertamento_scadenzario()).isPresent()) {
					Accertamento_scadenzarioBulk accertScad = (Accertamento_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getAccertamento_scadenzario());

					if (Optional.ofNullable(accertScad).isPresent()) {
						AccertamentoBulk accert = (AccertamentoBulk) fatpasHome.loadIfNeededObject(accertScad.getAccertamento());
						Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
						List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(accert.getCd_elemento_voce(), accert.getEsercizio(), accert.getTi_appartenenza(), accert.getTi_gestione()));
						return Optional.ofNullable(listAss).orElse(new ArrayList<>())
								.stream().map(Ass_ev_voceepBulk::getVoce_ep)
								.findAny().orElseThrow(()->
									new ApplicationPersistencyException("Non risultano associati conti economici alla voce di bilancio "+accert.getTi_gestione()+"/"+accert.getCd_elemento_voce()+"!")
								);
					}
				}
			}
			return null;
		} catch (PersistencyException e) {
			throw new DetailedRuntimeException(e);
        }
    }

	protected List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiDefault(UserContext userContext, Fattura_attiva_rigaBulk docRiga, ContoBulk aContoEconomico, Class rigaEcoClass) throws ComponentException {
		try {
			List<Fattura_attiva_riga_ecoBulk> result = new ArrayList<>();

			if (Optional.ofNullable(aContoEconomico).isPresent()) {
				List<Voce_analiticaBulk> voceAnaliticaList = ((Voce_analiticaHome) getHomeCache().getHome(Voce_analiticaBulk.class)).findVoceAnaliticaList(aContoEconomico);
				if (voceAnaliticaList.isEmpty())
					return new ArrayList<>();
				Voce_analiticaBulk voceAnaliticaDef = voceAnaliticaList.stream()
						.filter(Voce_analiticaBulk::getFl_default).findAny()
						.orElse(voceAnaliticaList.stream().findAny().orElse(null));

				if (Optional.ofNullable(docRiga.getScadenzaDocumentoContabile()).filter(Accertamento_scadenzarioBulk.class::isInstance).isPresent()) {
					//carico i dettagli analitici recuperandoli dall'obbligazione_scad_voce
					Accertamento_scadenzarioHome accertamentoScadenzarioHome = (Accertamento_scadenzarioHome) getHomeCache().getHome(Accertamento_scadenzarioBulk.class);
					List<Accertamento_scad_voceBulk> scadVoceBulks = accertamentoScadenzarioHome.findAccertamento_scad_voceList(userContext, (Accertamento_scadenzarioBulk) docRiga.getScadenzaDocumentoContabile(), Boolean.FALSE);
					BigDecimal totScad = scadVoceBulks.stream().map(Accertamento_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
					for (Accertamento_scad_voceBulk scadVoce : scadVoceBulks) {
						Fattura_attiva_riga_ecoBulk myRigaEco = (Fattura_attiva_riga_ecoBulk)rigaEcoClass.newInstance();
						myRigaEco.setProgressivo_riga_eco((long)result.size() + 1);
						myRigaEco.setVoce_analitica(voceAnaliticaDef);
						myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
						myRigaEco.setFattura_attiva_riga(docRiga);
						if (totScad.compareTo(BigDecimal.ZERO)!=0)
							myRigaEco.setImporto(scadVoce.getIm_voce().multiply(docRiga.getImCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
						else
							myRigaEco.setImporto(docRiga.getImCostoEco().divide(BigDecimal.valueOf(scadVoceBulks.size()), 2, RoundingMode.HALF_UP));
						myRigaEco.setToBeCreated();
						result.add(myRigaEco);
					}
					BigDecimal totRipartito = result.stream().map(Fattura_attiva_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
					BigDecimal diff = totRipartito.subtract(docRiga.getImCostoEco());

					if (diff.compareTo(BigDecimal.ZERO)>0) {
						for (Fattura_attiva_riga_ecoBulk rigaEco : result) {
							if (rigaEco.getImporto().compareTo(diff) >= 0) {
								rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
								break;
							} else {
								diff = diff.subtract(rigaEco.getImporto());
								rigaEco.setImporto(BigDecimal.ZERO);
							}
						}
					} else if (diff.compareTo(BigDecimal.ZERO)<0) {
						for (Fattura_attiva_riga_ecoBulk rigaEco : result) {
							rigaEco.setImporto(rigaEco.getImporto().add(diff));
							break;
						}
					}
				}
			}
			return result.stream()
					.filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
					.collect(Collectors.toList());
		} catch (PersistencyException | InstantiationException | IllegalAccessException ex) {
			throw new ComponentException(ex);
		}
	}
}