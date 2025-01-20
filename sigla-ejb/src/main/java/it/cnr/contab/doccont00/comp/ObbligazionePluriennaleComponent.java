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
 * Created on Feb 6, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.doccont00.comp;

import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.pdg00.bulk.Pdg_variazioneBulk;
import it.cnr.contab.pdg00.bulk.Pdg_variazioneHome;
import it.cnr.contab.pdg01.bulk.Pdg_variazione_riga_gestBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.ApplicationPersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rpagano
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ObbligazionePluriennaleComponent extends ObbligazioneComponent {
	public ObbligazionePluriennaleComponent()
	{
		/*Default constructor*/
	}

	public List<Obbligazione_pluriennaleBulk> findObbligazioniPluriennali(UserContext uc, int esercizio) throws it.cnr.jada.comp.ComponentException {
		try{
			Obbligazione_pluriennaleHome obbligazionePluriennaleHome = ( Obbligazione_pluriennaleHome) getHome(uc, Obbligazione_pluriennaleBulk.class);
			SQLBuilder sql = obbligazionePluriennaleHome.createSQLBuilder();
			sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, esercizio);
			sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio-1);
			sql.addClause(FindClause.AND, "cdCdsRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "esercizioRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "esercizioOriginaleRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "pgObbligazioneRif", SQLBuilder.ISNULL, null);
			sql.addTableToHeader("OBBLIGAZIONE");
			sql.addSQLJoin("OBBLIGAZIONE.CD_CDS", "OBBLIGAZIONE_PLURIENNALE.CD_CDS");
			sql.addSQLJoin("OBBLIGAZIONE.ESERCIZIO", "OBBLIGAZIONE_PLURIENNALE.ESERCIZIO");
			sql.addSQLJoin("OBBLIGAZIONE.ESERCIZIO_ORIGINALE", "OBBLIGAZIONE_PLURIENNALE.ESERCIZIO_ORIGINALE");
			sql.addSQLJoin("OBBLIGAZIONE.PG_OBBLIGAZIONE", "OBBLIGAZIONE_PLURIENNALE.PG_OBBLIGAZIONE");
			sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.DT_CANCELLAZIONE", SQLBuilder.ISNULL,null);
			//sql.addClause(FindClause.AND, "pgObbligazione", SQLBuilder.EQUALS, 4557);
			//sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, "000");

			return obbligazionePluriennaleHome.fetchAll(sql);
		} catch (it.cnr.jada.persistency.PersistencyException e) {
			throw handleException( new ApplicationPersistencyException(e));
		}
	}

	private Boolean isObbligazioneVarColl( UserContext uc,ObbligazioneBulk obbligazione) throws PersistencyException, ComponentException {


		if (Optional.ofNullable( getFirstVariazione(uc,obbligazione)).isPresent())
			return Boolean.TRUE;
		return Boolean.FALSE;

	}
	private Pdg_variazioneBulk getFirstVariazione( UserContext uc,ObbligazioneBulk obbligazione) throws PersistencyException, ComponentException {
		List<Pdg_variazioneBulk> listVariazioni = Collections.emptyList();
		ObbligazioneHome obbHome = (ObbligazioneHome) getHome(uc, ObbligazioneBulk.class);
		listVariazioni = obbHome.findVariazioniCollegate(obbligazione);
		if (listVariazioni.isEmpty())
			return null;
		return listVariazioni.get(0);

	}
	private WorkpackageBulk getCurrentGaePrelFondiVariazione(UserContext uc,Integer esercizio,ObbligazioneBulk obbligazione) throws ComponentException, PersistencyException {
		Pdg_variazioneBulk pdgVariazioneBulk= getFirstVariazione( uc,obbligazione);
		Pdg_variazioneHome pdgVariazioneHome =(Pdg_variazioneHome) getHome(uc, Pdg_variazioneBulk.class);
		List<Pdg_variazione_riga_gestBulk> dettagliVariazione = ( List<Pdg_variazione_riga_gestBulk>) pdgVariazioneHome.findDettagliVariazioneGestionale( pdgVariazioneBulk);

		WorkpackageBulk gaePrelevamento=Optional.ofNullable(dettagliVariazione)
				.orElseGet(()-> Collections.emptyList()).
				stream().
				filter(det->det.getIm_variazione().compareTo(BigDecimal.ZERO)<0).
						findFirst().map(e->{
					return new WorkpackageBulk(e.getCd_cdr_assegnatario(),e.getCd_linea_attivita());
				}).orElse(null);
		return getAssGaePrelevamento ( uc, esercizio,gaePrelevamento);
		/*accesso alla nuova tabella
		Ass_la_ribalt_pluriennaliHome assLaPlurHome =(Ass_la_ribalt_pluriennaliHome) getHome(uc, Ass_la_ribalt_pluriennaliBulk.class);
		/Ass_la_ribalt_pluriennaliBulk assLaPlur = assLaPlurHome.getAssLineaAttivita(esercizio, gaePrelevamento);

		if(assLaPlur!=null){
			gaePrelevamento = new WorkpackageBulk(assLaPlur.getCdCentroRespRibalt(),assLaPlur.getCdLineaAttivitaRibalt());
		}
		gaePrelevamento = (WorkpackageBulk) getHome(uc, WorkpackageBulk.class).findByPrimaryKey(gaePrelevamento);
		gaePrelevamento.setCentro_responsabilita((CdrBulk) getHome(uc, CdrBulk.class).findByPrimaryKey(gaePrelevamento.getCentro_responsabilita()));
		return gaePrelevamento;
		*/
	}

	private WorkpackageBulk getAssGaePrelevamento(UserContext uc,Integer esercizio,WorkpackageBulk gaePrelevamento) throws PersistencyException, ComponentException {

		Ass_la_ribalt_pluriennaliBulk assLaPlur = getAssociazioneGAENuovoEsercizio(uc,esercizio, gaePrelevamento);

		if(assLaPlur!=null){
			gaePrelevamento = new WorkpackageBulk(assLaPlur.getCdCentroRespRibalt(),assLaPlur.getCdLineaAttivitaRibalt());
		}
		gaePrelevamento = (WorkpackageBulk) getHome(uc, WorkpackageBulk.class).findByPrimaryKey(gaePrelevamento);
		gaePrelevamento.setCentro_responsabilita((CdrBulk) getHome(uc, CdrBulk.class).findByPrimaryKey(gaePrelevamento.getCentro_responsabilita()));
		return gaePrelevamento;
	}

	private Ass_la_ribalt_pluriennaliBulk getAssociazioneGAENuovoEsercizio(UserContext uc,Integer esercizio,WorkpackageBulk gaeVecchioEsercizio) throws ComponentException, PersistencyException {
		Ass_la_ribalt_pluriennaliHome assLaPlurHome =(Ass_la_ribalt_pluriennaliHome) getHome(uc, Ass_la_ribalt_pluriennaliBulk.class);
		Ass_la_ribalt_pluriennaliBulk ass= assLaPlurHome.getAssLineaAttivita(esercizio, gaeVecchioEsercizio);
		return ass;
	}



	public ObbligazioneBulk createObbligazioneNew(UserContext uc, Obbligazione_pluriennaleBulk pluriennaleBulk, Integer esercizio,WorkpackageBulk gaeIniziale) throws it.cnr.jada.comp.ComponentException {
		try {
			Obbligazione_pluriennaleHome pluriennaleHome = (Obbligazione_pluriennaleHome) getHome(uc, Obbligazione_pluriennaleBulk.class);
			pluriennaleBulk.setRigheVoceColl(new BulkList(pluriennaleHome.findObbligazioniPluriennaliVoce(uc, pluriennaleBulk)));
			if ( pluriennaleBulk.getRigheVoceColl()==null || pluriennaleBulk.getRigheVoceColl().isEmpty())
				throw new ComponentException("Nessuna Liena Attività per Obbligazione Pluriennale "+pluriennaleBulk.toString());
			//if ( pluriennaleBulk.getRigheVoceColl().size()>1)
			//	throw new ComponentException("Obbligazione Pluriennale su Multigae "+pluriennaleBulk.toString());
			ObbligazioneHome obbligazioneHome = (ObbligazioneHome) getHome(uc, ObbligazioneBulk.class);
			 ObbligazioneBulk obbligazioneBulk =obbligazioneHome.findObbligazione(new ObbligazioneOrdBulk(pluriennaleBulk.getCdCds(),
																		pluriennaleBulk.getEsercizio(),
																	pluriennaleBulk.getEsercizioOriginale(),
																	pluriennaleBulk.getPgObbligazione()));
			ObbligazioneBulk obbligazioneBulkNew =( ObbligazioneBulk) obbligazioneBulk.clone();
			obbligazioneBulkNew.setCrudStatus(OggettoBulk.TO_BE_CREATED);
			obbligazioneBulkNew.setRiportato("N");
			//obbligazioneBulkNew.setCreditore(obbligazioneBulk.getCreditore());

			// Prende l'ElementoVoce dal Pluriennale voce perchè potrebbe essere aggiornato con nuovi valori per il nuovo esercizio
			Elemento_voceBulk nuovaVoce = pluriennaleBulk.getRigheVoceColl().get(0).getElementoVoce();

			/*
			Elemento_voceBulk voce = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();
			voce.setEsercizio(esercizio);
			voce.setTi_appartenenza(obbligazioneBulkNew.getElemento_voce().getTi_appartenenza());
			voce.setTi_gestione(obbligazioneBulkNew.getElemento_voce().getTi_gestione());
			voce.setCd_elemento_voce(obbligazioneBulkNew.getElemento_voce().getCd_elemento_voce());
			*/

			obbligazioneBulkNew.setElemento_voce((Elemento_voceBulk)getHome(uc, Elemento_voceBulk.class).findByPrimaryKey(nuovaVoce));
			obbligazioneBulkNew = listaCapitoliPerCdsVoce(uc, obbligazioneBulkNew);
			obbligazioneBulkNew.setCapitoliDiSpesaCdsSelezionatiColl(obbligazioneBulkNew.getCapitoliDiSpesaCdsColl());
			if ( Optional.ofNullable(obbligazioneBulkNew.getContratto()).map(ContrattoBulk::getPg_progetto).isPresent())
				obbligazioneBulkNew.setContratto(((ContrattoBulk) getHome(uc, ContrattoBulk.class).findByPrimaryKey(obbligazioneBulkNew.getContratto())));
			obbligazioneBulkNew.setCds((CdsBulk) getHome(uc, CdsBulk.class).findByPrimaryKey(obbligazioneBulkNew.getCds()));
			obbligazioneBulkNew.setPg_obbligazione(null);
			obbligazioneBulkNew.setEsercizio(esercizio);
			obbligazioneBulkNew.setEsercizio_originale(esercizio);
			obbligazioneBulkNew.setIm_obbligazione(pluriennaleBulk.getImporto());
			//obbligazioneBulkNew.setUtcr(uc.getUser());
			//obbligazioneBulkNew.setUtcr(uc.getUser());
			obbligazioneBulkNew.setPg_ver_rec(null);
			obbligazioneBulkNew.setStato_obbligazione(ObbligazioneBulk.STATO_OBB_DEFINITIVO);
			obbligazioneBulkNew.setEsercizio_competenza(esercizio);


			obbligazioneBulkNew.setCdrColl( listaCdrPerCapitoli( uc,  obbligazioneBulkNew));
			obbligazioneBulkNew.setLineeAttivitaColl( listaLineeAttivitaPerCapitoliCdr( uc,  obbligazioneBulkNew));
			obbligazioneBulkNew.setDt_registrazione(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate());
			obbligazioneBulkNew.getCds().setCd_unita_organizzativa(obbligazioneBulkNew.getCds().getCd_unita_organizzativa());

			Obbligazione_scadenzarioBulk obb_scadenza = new Obbligazione_scadenzarioBulk();
			obb_scadenza.setUtcr(obbligazioneBulkNew.getUtcr());
			obb_scadenza.setToBeCreated();

			obb_scadenza.setObbligazione(obbligazioneBulkNew);
			obb_scadenza.setDt_scadenza(obbligazioneBulkNew.getDt_registrazione());
			obb_scadenza.setDs_scadenza(obbligazioneBulkNew.getDs_obbligazione());
			obbligazioneBulkNew.addToObbligazione_scadenzarioColl(obb_scadenza);
			obb_scadenza.setIm_scadenza(obbligazioneBulkNew.getIm_obbligazione());
			obb_scadenza.setIm_associato_doc_amm(new BigDecimal(0));
			obb_scadenza.setIm_associato_doc_contabile(new BigDecimal(0));

			WorkpackageBulk gaePrelevamentoFondi = null;

			boolean isObbligazioneMultiGae = pluriennaleBulk.getRigheVoceColl().size()>1;
			// prendo associazione GAE del nuovo esercizio selezionandola dal primo elemento di Obbligazione_pluriennale_voce associato al pluriennale
			Ass_la_ribalt_pluriennaliBulk associazioneGae = getAssociazioneGAENuovoEsercizio(uc,esercizio,pluriennaleBulk.getRigheVoceColl().get(0).getLinea_attivita());

			// SE ESISTE L'ASSOCIATIVA CON UNA NUOVA GAE DI PRELEVAMENTO FONDI
			if(associazioneGae != null && associazioneGae.getCdLineaAttivitaPrelFondi() !=null){
				// L'OBBLIGAZIONE NON PUO' ESSERE MULTI GAE
				if ( isObbligazioneMultiGae)
					throw new ComponentException("Obbligazione Pluriennale con prelevamento con Multigae "+pluriennaleBulk.toString());

				 gaePrelevamentoFondi = (WorkpackageBulk) getHome(uc, WorkpackageBulk.class).findByPrimaryKey(associazioneGae.getLineaAttivitaPrelFondi());
				 gaePrelevamentoFondi.setCentro_responsabilita((CdrBulk) getHome(uc, CdrBulk.class).findByPrimaryKey(gaePrelevamentoFondi.getCentro_responsabilita()));

				WorkpackageBulk gaeDestinazione = (WorkpackageBulk) getHome(uc, WorkpackageBulk.class).findByPrimaryKey(associazioneGae.getLineaAttivitaNuovoEser());

				gaeDestinazione.setCentro_responsabilita((CdrBulk) getHome(uc, CdrBulk.class).findByPrimaryKey(gaeDestinazione.getCentro_responsabilita()));
				gaeDestinazione.setNatura((NaturaBulk) getHome(uc, NaturaBulk.class).findByPrimaryKey(gaeDestinazione.getNatura()));
				Unita_organizzativaBulk uoPadre = (Unita_organizzativaBulk)
						getHome(uc, Unita_organizzativaBulk.class).findByPrimaryKey(new Unita_organizzativaBulk(gaeDestinazione.getCentro_responsabilita().getCd_unita_organizzativa()));
				gaeDestinazione.getCentro_responsabilita().setUnita_padre(uoPadre);

				obbligazioneBulkNew.setGaeDestinazioneFinale(gaeDestinazione);
			}

			for ( Obbligazione_pluriennale_voceBulk pluriennaleVoceBulk: pluriennaleBulk.getRigheVoceColl()){
				//scadenza
				WorkpackageBulk gaeVoce = null;
				Obbligazione_scad_voceBulk obb_scad_voce = new Obbligazione_scad_voceBulk();
				obb_scadenza.setUtcr(obbligazioneBulkNew.getUtcr());
				obb_scad_voce.setToBeCreated();
				obb_scad_voce.setObbligazione_scadenzario(obb_scadenza);
				obb_scad_voce.setIm_voce(pluriennaleVoceBulk.getImporto());
				obb_scad_voce.setCd_voce(pluriennaleVoceBulk.getElementoVoce().getCd_voce());
				obb_scad_voce.setTi_gestione(pluriennaleVoceBulk.getElementoVoce().getTi_gestione());
				obb_scad_voce.setTi_appartenenza(pluriennaleVoceBulk.getElementoVoce().getTi_appartenenza());
				ObbligazioneHome obbHome = (ObbligazioneHome) getHome(uc, ObbligazioneBulk.class);
				obb_scadenza.getObbligazione_scad_voceColl().add((obb_scad_voce));

				if ( Optional.ofNullable(gaePrelevamentoFondi).isPresent())
					gaeVoce= gaePrelevamentoFondi;
				else {

					gaeVoce = getAssGaePrelevamento(uc,esercizio,pluriennaleVoceBulk.getLinea_attivita());

				}
				gaeVoce.setNatura((NaturaBulk) getHome(uc, NaturaBulk.class).findByPrimaryKey(gaeVoce.getNatura()));
				Linea_attivitaBulk nuovaLatt = new Linea_attivitaBulk();
				nuovaLatt.setLinea_att(gaeVoce);
				nuovaLatt.setPrcImputazioneFin(obb_scad_voce.getIm_voce().multiply(new BigDecimal(100)).divide(obb_scadenza.getIm_scadenza(), 2, RoundingMode.HALF_UP));

				nuovaLatt.setObbligazione(obbligazioneBulkNew);
				if (obbligazioneBulkNew.getNuoveLineeAttivitaColl() == null)
					obbligazioneBulkNew.setNuoveLineeAttivitaColl(new BulkList());
				obbligazioneBulkNew.getNuoveLineeAttivitaColl().add(nuovaLatt);
				obb_scad_voce.setLinea_attivita(gaeVoce);
			};
			//aggiungi pluriennali anni successivi
			List<Obbligazione_pluriennaleBulk> obbPluriennali =
					(	( List<Obbligazione_pluriennaleBulk>)Optional.ofNullable(obbligazioneHome.findObbligazioniPluriennali(uc,obbligazioneBulk)).
							orElse(new ArrayList<Accertamento_pluriennaleBulk>())).
							stream().filter(pl->pl.getAnno().compareTo(esercizio)>0).collect(Collectors.toList());

			for ( Obbligazione_pluriennaleBulk pluriennale:obbPluriennali){
				Obbligazione_pluriennaleBulk newObbPluriennale= new Obbligazione_pluriennaleBulk();
				newObbPluriennale.setObbligazione(obbligazioneBulkNew);
				newObbPluriennale.setAnno( pluriennale.getAnno());
				newObbPluriennale.setImporto( pluriennale.getImporto());
				newObbPluriennale.setToBeCreated();
				//aggiungi Accertamento Plurinelle voce
				List<Obbligazione_pluriennale_voceBulk> obbPluriennaliVoce =
						(	( List<Obbligazione_pluriennale_voceBulk>)Optional.ofNullable(pluriennaleHome.findObbligazioniPluriennaliVoce(uc,pluriennale)).
								orElse(new ArrayList<Obbligazione_pluriennale_voceBulk>()));
				for (Obbligazione_pluriennale_voceBulk pluriennaleVoceBulk : obbPluriennaliVoce){

					Obbligazione_pluriennale_voceBulk newObbligazionePluriennaleVoce = new Obbligazione_pluriennale_voceBulk();
					newObbligazionePluriennaleVoce.setObbligazionePluriennale( newObbPluriennale);
					newObbligazionePluriennaleVoce.setElementoVoce(pluriennaleVoceBulk.getElementoVoce());
					newObbligazionePluriennaleVoce.setEsercizioVoce(esercizio);

					//if ( Optional.ofNullable(gaePrelevamentoFondi).isPresent())
					//	gaeVoce= gaePrelevamentoFondi;
					//else {
					WorkpackageBulk gaeVoce = getAssGaePrelevamento(uc,esercizio,pluriennaleVoceBulk.getLinea_attivita());
					//}
					newObbligazionePluriennaleVoce.setLinea_attivita( gaeVoce);
					newObbligazionePluriennaleVoce.setImporto( pluriennaleVoceBulk.getImporto());
					newObbligazionePluriennaleVoce.setAutoRimodulazione(pluriennaleVoceBulk.getAutoRimodulazione());
					newObbligazionePluriennaleVoce.setToBeCreated();
					newObbPluriennale.getRigheVoceColl().add(newObbligazionePluriennaleVoce);
				}
				obbligazioneBulkNew.getObbligazioniPluriennali().add( newObbPluriennale);
			}

			//inizializzaBulkPerInserimento(uc,obbligazioneBulkNew);
			obbligazioneBulkNew= (ObbligazioneBulk) creaConBulk(uc, obbligazioneBulkNew);
			//aggiornamento riferimento obbligazione creata
			pluriennaleBulk.setObbligazioneRif(obbligazioneBulkNew);
			pluriennaleHome.update(pluriennaleBulk,uc);

			return obbligazioneBulkNew;
		}catch(Exception e )
			{
				throw handleException(e);
		}
	}
}
