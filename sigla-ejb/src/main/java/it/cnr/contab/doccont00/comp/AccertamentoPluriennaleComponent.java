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
import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.ApplicationPersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rpagano
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AccertamentoPluriennaleComponent extends AccertamentoComponent {
	public AccertamentoPluriennaleComponent()
	{
		/*Default constructor*/
	}

	public List<Accertamento_pluriennaleBulk> findAccertamentiPluriennali(UserContext uc, int esercizio) throws it.cnr.jada.comp.ComponentException {
		try{
			Accertamento_pluriennaleHome accertamento_pluriennaleHome = ( Accertamento_pluriennaleHome) getHome(uc, Accertamento_pluriennaleBulk.class);
			SQLBuilder sql = accertamento_pluriennaleHome.createSQLBuilder();
			sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, esercizio);
			sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio-1);
			sql.addClause(FindClause.AND, "cdCdsRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "esercizioRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "esercizioOriginaleRif", SQLBuilder.ISNULL, null);
			sql.addClause(FindClause.AND, "pgAccertamentoRif", SQLBuilder.ISNULL, null);
			sql.addTableToHeader("ACCERTAMENTO");
			sql.addSQLJoin("ACCERTAMENTO.CD_CDS", "ACCERTAMENTO_PLURIENNALE.CD_CDS");
			sql.addSQLJoin("ACCERTAMENTO.ESERCIZIO", "ACCERTAMENTO_PLURIENNALE.ESERCIZIO");
			sql.addSQLJoin("ACCERTAMENTO.ESERCIZIO_ORIGINALE", "ACCERTAMENTO_PLURIENNALE.ESERCIZIO_ORIGINALE");
			sql.addSQLJoin("ACCERTAMENTO.PG_ACCERTAMENTO", "ACCERTAMENTO_PLURIENNALE.PG_ACCERTAMENTO");
			sql.addSQLClause(FindClause.AND, "ACCERTAMENTO.DT_CANCELLAZIONE", SQLBuilder.ISNULL,null);

			return accertamento_pluriennaleHome.fetchAll(sql);
		} catch (it.cnr.jada.persistency.PersistencyException e) {
			throw handleException( new ApplicationPersistencyException(e));
		}
	}


	public AccertamentoBulk createAccertamentoNew(UserContext uc, Integer esercizio,Accertamento_pluriennaleBulk accertamentoPluriennaleBulk) throws it.cnr.jada.comp.ComponentException {
		try {
			Accertamento_pluriennaleHome pluriennaleHome = (Accertamento_pluriennaleHome) getHome(uc, Accertamento_pluriennaleBulk.class);
			AccertamentoHome accertamentoHome = (AccertamentoHome) getHome(uc, AccertamentoBulk.class);
			accertamentoPluriennaleBulk.setRigheVoceColl(new BulkList(pluriennaleHome.findAccertamentiPluriennaliVoce(uc, accertamentoPluriennaleBulk)));

			AccertamentoBulk accertamentoBulk =accertamentoHome.findAccertamento(new AccertamentoBulk(accertamentoPluriennaleBulk.getCdCds(),
					accertamentoPluriennaleBulk.getEsercizio(),
					accertamentoPluriennaleBulk.getEsercizioOriginale(),
					accertamentoPluriennaleBulk.getPgAccertamento()));


			AccertamentoBulk newAccertamentoBulk =( AccertamentoBulk) accertamentoBulk.clone();



			newAccertamentoBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);
			newAccertamentoBulk.setRiportato("N");
			newAccertamentoBulk.setPg_accertamento(null);
			newAccertamentoBulk.setEsercizio(esercizio);
			newAccertamentoBulk.setEsercizio_originale(esercizio);
			newAccertamentoBulk.setCd_tipo_documento_cont(Numerazione_doc_contBulk.TIPO_ACR);

			Elemento_voceBulk voce = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();
			voce.setEsercizio(esercizio);
			voce.setTi_appartenenza(newAccertamentoBulk.getTi_appartenenza());
			voce.setTi_gestione(newAccertamentoBulk.getTi_gestione());
			voce.setCd_elemento_voce(newAccertamentoBulk.getCd_elemento_voce());
			V_voce_f_partita_giroBulk voceCapitolo = (V_voce_f_partita_giroBulk)getHome(uc, V_voce_f_partita_giroBulk.class).findByPrimaryKey(voce);

			newAccertamentoBulk.setCapitolo(voceCapitolo);

			if ( Optional.ofNullable(newAccertamentoBulk.getContratto()).map(ContrattoBulk::getPg_contratto).isPresent())
				newAccertamentoBulk.setContratto(((ContrattoBulk) getHome(uc, ContrattoBulk.class).findByPrimaryKey(newAccertamentoBulk.getContratto())));

			newAccertamentoBulk.setDt_registrazione(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate());
			newAccertamentoBulk.setIm_accertamento(accertamentoPluriennaleBulk.getImporto());
			newAccertamentoBulk.setEsercizio_competenza(esercizio);
			Accertamento_scadenzarioBulk acc_scadenza = new Accertamento_scadenzarioBulk();
			acc_scadenza.setUtcr(newAccertamentoBulk.getUtcr());
			acc_scadenza.setToBeCreated();

			acc_scadenza.setAccertamento(newAccertamentoBulk);
			acc_scadenza.setDt_scadenza_incasso(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate());
			acc_scadenza.setDs_scadenza(newAccertamentoBulk.getDs_accertamento());
			newAccertamentoBulk.addToAccertamento_scadenzarioColl(acc_scadenza);
			acc_scadenza.setIm_scadenza(newAccertamentoBulk.getIm_accertamento());
			acc_scadenza.setIm_associato_doc_amm(BigDecimal.ZERO);
			acc_scadenza.setIm_associato_doc_contabile(BigDecimal.ZERO);
			acc_scadenza.setIm_associato_doc_contabile(new BigDecimal(0));

			for(Accertamento_pluriennale_voceBulk plurVoce : accertamentoPluriennaleBulk.getRigheVoceColl()){

				Accertamento_scad_voceBulk acc_scad_voce = new Accertamento_scad_voceBulk();

				acc_scad_voce.setToBeCreated();
				acc_scad_voce.setAccertamento_scadenzario(acc_scadenza);
				acc_scad_voce.setIm_voce(plurVoce.getImporto());

				WorkpackageBulk lineaAttivita = (WorkpackageBulk) getHome(uc, WorkpackageBulk.class).findByPrimaryKey(plurVoce.getLinea_attivita());
				acc_scad_voce.setLinea_attivita(lineaAttivita);

				acc_scadenza.getAccertamento_scad_voceColl().add((acc_scad_voce));
			}
			// CICLARE PER TUTTI I PLURIENNALI VOCE


			//aggiungi pluriennali anni successivi
			List<Accertamento_pluriennaleBulk> accPluriennali =
					(	( List<Accertamento_pluriennaleBulk>)Optional.ofNullable(accertamentoHome.findAccertamentiPluriennali(uc,accertamentoBulk)).
							orElse(new ArrayList<Accertamento_pluriennaleBulk>())).
							stream().filter(pl->pl.getAnno().compareTo(esercizio)>0).collect(Collectors.toList());

			for ( Accertamento_pluriennaleBulk pluriennale:accPluriennali){
				Accertamento_pluriennaleBulk newAccPluriennale= new Accertamento_pluriennaleBulk();
					newAccPluriennale.setAnno( pluriennale.getAnno());
				newAccPluriennale.setAccertamento( newAccertamentoBulk);
				newAccPluriennale.setImporto( pluriennale.getImporto());
				newAccPluriennale.setToBeCreated();
				//aggiungi Accertamento Plurinelle voce
				List<Accertamento_pluriennale_voceBulk> accPluriennaliVoce =
						(	( List<Accertamento_pluriennale_voceBulk>)Optional.ofNullable(pluriennaleHome.findAccertamentiPluriennaliVoce(uc,pluriennale)).
								orElse(new ArrayList<Accertamento_pluriennale_voceBulk>()));
				for (Accertamento_pluriennale_voceBulk pluriennaleVoceBulk : accPluriennaliVoce){
					Accertamento_pluriennale_voceBulk newAccertamentoPluriennaleVoce = new Accertamento_pluriennale_voceBulk();
						newAccertamentoPluriennaleVoce.setAccertamentoPluriennale( newAccPluriennale);

						// PRENDERE LA LINEA DALLA ASSOCIATIAVA

						newAccertamentoPluriennaleVoce.setLinea_attivita( pluriennaleVoceBulk.getLinea_attivita());
						newAccertamentoPluriennaleVoce.setImporto( pluriennaleVoceBulk.getImporto());
						newAccertamentoPluriennaleVoce.setToBeCreated();
					newAccPluriennale.getRigheVoceColl().add( newAccertamentoPluriennaleVoce);

				}
				newAccertamentoBulk.getAccertamentiPluriennali().add( newAccPluriennale);
			}

			//inizializzaBulkPerInserimento(uc,newAccertamentoBulk);
			newAccertamentoBulk = (AccertamentoBulk)creaConBulk(uc, newAccertamentoBulk);
			accertamentoPluriennaleBulk.setAccertamentoRif(newAccertamentoBulk);
			//aggiornamento riferimento accertamente creato
			pluriennaleHome.update(accertamentoPluriennaleBulk,uc);
			return newAccertamentoBulk;
		}catch(Exception e )
		{
			throw handleException(e);
		}
	}
}
