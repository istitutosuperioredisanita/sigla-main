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
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.ordmag.anag00.*;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
public class OrdineAcqHome extends BulkHome {
	public OrdineAcqHome(Connection conn) {
		super(OrdineAcqBulk.class, conn);
	}
	public OrdineAcqHome(Connection conn, PersistentCache persistentCache) {
		super(OrdineAcqBulk.class, conn, persistentCache);
	}
	public SQLBuilder selectUnitaOperativaOrdByClause(UserContext userContext, OrdineAcqBulk ordine, 
			UnitaOperativaOrdHome unitaOperativaHome, UnitaOperativaOrdBulk unitaOperativaBulk, 
			CompoundFindClause compoundfindclause) throws PersistencyException{
		SQLBuilder sql = unitaOperativaHome.selectByClause(userContext, compoundfindclause);
		UtenteBulk utente = (UtenteBulk) (getHomeCache().getHome(UtenteBulk.class).findByPrimaryKey(new UtenteBulk(CNRUserContext.getUser(userContext))));

		if (!utente.isSupervisore()) {
			AbilUtenteUopOperHome home = (AbilUtenteUopOperHome) getHomeCache().getHome(AbilUtenteUopOperBulk.class);
			SQLBuilder sqlExists = home.createSQLBuilder();
			sqlExists.addSQLJoin("UNITA_OPERATIVA_ORD.CD_UNITA_OPERATIVA", "ABIL_UTENTE_UOP_OPER.CD_UNITA_OPERATIVA");
			if (ordine.getIsForFirma()) {
				sqlExists.addSQLClause("AND", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_FIRMA_ORDINE);
			} else {
				sqlExists.openParenthesis("AND");
				sqlExists.addSQLClause("OR", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
				sqlExists.addSQLClause("OR", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_APPROVAZIONE_ORDINE);
				sqlExists.closeParenthesis();
			}
			sqlExists.addSQLClause("AND", "ABIL_UTENTE_UOP_OPER.CD_UTENTE", SQLBuilder.EQUALS, userContext.getUser());

			sql.addSQLExistsClause("AND", sqlExists);
		}

		return sql;
	}
	
	public void addConditionAbilUtenteUop(UserContext userContext, SQLBuilder sql) {
		sql.addTableToHeader("ABIL_UTENTE_UOP_OPER");
		sql.openParenthesis("AND");
		sql.addSQLClause("OR", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
		sql.addSQLClause("OR", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_APPROVAZIONE_ORDINE);
		sql.addSQLClause("OR", "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_FIRMA_ORDINE);
		sql.closeParenthesis();
		sql.openParenthesis("AND");
		sql.addSQLClause("OR", "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_DEFINITIVO);
		sql.addSQLClause("OR", "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_IN_APPROVAZIONE);
		sql.addSQLClause("OR", "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_ALLA_FIRMA);
		sql.closeParenthesis();
		sql.addSQLClause("AND", "ABIL_UTENTE_UOP_OPER.CD_UTENTE", SQLBuilder.EQUALS, userContext.getUser());
	}

	public SQLBuilder selectNumerazioneOrdByClause(UserContext userContext, OrdineAcqBulk ordine, 
			NumerazioneOrdHome numerazioneHome, NumerazioneOrdBulk numerazioneBulk, 
			CompoundFindClause compoundfindclause) throws PersistencyException,ApplicationException{
		if (ordine == null || ordine.getCdUopOrdine() == null)
			throw new ApplicationException("Selezionare prima l'unità operativa");
		SQLBuilder sql = numerazioneHome.selectByClause(userContext, compoundfindclause);
		sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.CD_UNITA_OPERATIVA", SQLBuilder.EQUALS, ordine.getCdUopOrdine());
		sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
		return sql;
	}
	public SQLBuilder selectNumerazioneOrdByClause(UserContext userContext, ParametriSelezioneOrdiniAcqBulk parametriSelezioneOrdiniAcqBulk,
												   NumerazioneOrdHome numerazioneHome, NumerazioneOrdBulk numerazioneBulk,
												   CompoundFindClause compoundfindclause) throws PersistencyException,ApplicationException{
        if (parametriSelezioneOrdiniAcqBulk == null ||
                ((parametriSelezioneOrdiniAcqBulk.getUnitaOperativaOrdine() == null || parametriSelezioneOrdiniAcqBulk.getUnitaOperativaOrdine().getCdUnitaOperativa()==null) &&
                 (parametriSelezioneOrdiniAcqBulk.getUnitaOperativaAbilitata()==null || parametriSelezioneOrdiniAcqBulk.getUnitaOperativaAbilitata().getCdUnitaOperativa()==null)))
            throw new ApplicationException("Selezionare prima l'unità operativa");

		SQLBuilder sql = numerazioneHome.selectByClause(userContext, compoundfindclause);

        if (parametriSelezioneOrdiniAcqBulk.getUnitaOperativaOrdine() != null && parametriSelezioneOrdiniAcqBulk.getUnitaOperativaOrdine().getCdUnitaOperativa()!=null)
            sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.CD_UNITA_OPERATIVA", SQLBuilder.EQUALS,
                    Optional.ofNullable(parametriSelezioneOrdiniAcqBulk.getUnitaOperativaOrdine())
                            .map(UnitaOperativaOrdKey::getCdUnitaOperativa)
                            .orElse(null)
            );
        else
            sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.CD_UNITA_OPERATIVA", SQLBuilder.EQUALS,
                    Optional.ofNullable(parametriSelezioneOrdiniAcqBulk.getUnitaOperativaAbilitata())
                            .map(UnitaOperativaOrdKey::getCdUnitaOperativa)
                            .orElse(null)
            );

		sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.addSQLClause(FindClause.AND, "NUMERAZIONE_ORD.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
		return sql;
	}

	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause)
			throws PersistencyException {
		// TODO Auto-generated method stub
		return super.selectByClause(usercontext, compoundfindclause);
	}
	@Override
	public void delete(Persistent persistent, UserContext userContext) throws PersistencyException {
		((OrdineAcqBulk)persistent).setStato(OrdineAcqBulk.STATO_ANNULLATO);
		 super.update(persistent, userContext);
	}

	public Collection findModalita(UserContext userContext, OrdineAcqBulk ordineAcqBulk) throws PersistencyException {
		try {
			return Optional.ofNullable(ordineAcqBulk)
					.flatMap(ordineAcqBulk1 -> Optional.ofNullable(ordineAcqBulk1.getFornitore()))
					.map(terzoBulk -> {
						try {
							TerzoHome terzoHome = (TerzoHome) getHomeCache().getHome(TerzoBulk.class);
							return terzoHome.findRif_modalita_pagamento(terzoBulk);
						} catch (PersistencyException | IntrospectionException e) {
							throw new DetailedRuntimeException(e);
						}
					}).orElse(Collections.emptyList());
		} catch (DetailedRuntimeException ex) {
			throw new PersistencyException(ex);
		}
	}

	public List<OrdineAcqRigaBulk> findOrdineRigheList(OrdineAcqBulk ordine) throws PersistencyException {
		PersistentHome rigaHome = getHomeCache().getHome(OrdineAcqRigaBulk.class);
		SQLBuilder sql = rigaHome.createSQLBuilder();
		sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, ordine.getNumero());
		sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, ordine.getCdCds());
		sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, ordine.getCdUnitaOperativa());
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, ordine.getEsercizio());
		sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, ordine.getCdNumeratore());
		sql.addOrderBy("cd_cds");
		sql.addOrderBy("cd_unita_operativa");
		sql.addOrderBy("esercizio");
		sql.addOrderBy("cd_numeratore");
		sql.addOrderBy("numero");
		sql.addOrderBy("riga");
		return rigaHome.fetchAll(sql);
	}

	public OrdineAcqBulk initializeBulkForEdit(OrdineAcqBulk ordine) throws PersistencyException {
		ordine = (OrdineAcqBulk)this.findByPrimaryKey(ordine);
		if (ordine == null)
			throw new ObjectNotFoundException("Risorsa non pi\371 valida: \350 stata cancellata dall'ultimo caricamento.", null, ordine);

		ordine.setRigheOrdineColl(new BulkList(this.findOrdineRigheList(ordine)));
		OrdineAcqRigaHome rigaHome = (OrdineAcqRigaHome) getHomeCache().getHome(OrdineAcqRigaBulk.class);
		for (OrdineAcqRigaBulk rigaOrdine : ordine.getRigheOrdineColl())
			rigaOrdine.setRigheConsegnaColl(new BulkList(rigaHome.findOrdineRigheConsegnaList(rigaOrdine)));
		return ordine;
	}

	public List<OrdineAcqRigaEcoBulk> findOrdineAcqEcoList(OrdineAcqBulk ordine) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(OrdineAcqEcoBulk.class);

		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, ordine.getNumero());
		sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, ordine.getCdCds());
		sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, ordine.getCdUnitaOperativa());
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, ordine.getEsercizio());
		sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, ordine.getCdNumeratore());
		return home.fetchAll(sql);
	}
}