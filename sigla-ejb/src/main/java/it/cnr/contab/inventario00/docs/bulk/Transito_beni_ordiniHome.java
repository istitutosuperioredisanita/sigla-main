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

package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioHome;
import it.cnr.contab.ordmag.magazzino.bulk.LottoMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.MovimentiMagBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.DateUtils;

import java.sql.Timestamp;
import java.util.*;

public class Transito_beni_ordiniHome extends BulkHome {

public Transito_beni_ordiniHome(java.sql.Connection conn) {
	super(Transito_beni_ordiniBulk.class,conn);
}
public Transito_beni_ordiniHome(java.sql.Connection conn, PersistentCache persistentCache) {
	super(Transito_beni_ordiniBulk.class,conn,persistentCache);
}
	public Long recuperoId(UserContext userContext) throws PersistencyException,it.cnr.jada.comp.ComponentException {
		return new Long(this.fetchNextSequenceValue(userContext,"CNRSEQ00_TRANSITO_BENI_ORDINI").longValue());
	}
	public void initializePrimaryKeyForInsert(UserContext userContext, OggettoBulk bulk) throws PersistencyException,it.cnr.jada.comp.ComponentException {
		Transito_beni_ordiniBulk transito = (Transito_beni_ordiniBulk)bulk;
		if (transito.getId() == null)
			transito.setId(recuperoId(userContext));
	}


	private void validateDataBolla(UserContext usercontext,Object value) throws PersistencyException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((Date)value);
		int yearDateBolla= calendar.get( Calendar.YEAR);
		if ( yearDateBolla< CNRUserContext.getEsercizio(usercontext)||
				yearDateBolla>CNRUserContext.getEsercizio(usercontext))
			throw new ApplicationPersistencyException("La data della Bolla deve essere compresa nell'anno di lavoro selezionato");
	}
	private void addDataBollaCondition(UserContext usercontext,SQLBuilder sql,int operator,Object value) throws PersistencyException {
		validateDataBolla( usercontext,value);
		sql.addSQLClause("AND", "MOVIMENTI_MAG.DATA_BOLLA", operator, value);
	}
	private void addDataRifMovimentoCondition(UserContext usercontext,SQLBuilder sql,int operator,Object value) throws PersistencyException {
		sql.addSQLClause("AND", "MOVIMENTI_MAG.DT_RIFERIMENTO", operator, value);
	}
	private Timestamp lastDateOfTheYear(int year) {
		GregorianCalendar dataInizio = (GregorianCalendar)GregorianCalendar.getInstance();
		dataInizio.setTime((new GregorianCalendar(year, 11, 31)).getTime());
		return new Timestamp(dataInizio.getTimeInMillis());
	}
	@Override
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
		boolean ricercaAnnullati=false;

		sql.generateJoin(Transito_beni_ordiniBulk.class, MovimentiMagBulk.class, "movimentiMag", "MOVIMENTI_MAG");
		addDataRifMovimentoCondition( usercontext,sql,SQLBuilder.GREATER_EQUALS, DateUtils.firstDateOfTheYear(CNRUserContext.getEsercizio(usercontext)));
		addDataRifMovimentoCondition( usercontext,sql,SQLBuilder.LESS_EQUALS,  lastDateOfTheYear(CNRUserContext.getEsercizio(usercontext)));
		if (compoundfindclause != null && compoundfindclause.getClauses() != null) {
			Enumeration e = compoundfindclause.getClauses();

			while (e.hasMoreElements()) {
				FindClause findClause = (FindClause) e.nextElement();
				if (findClause instanceof SimpleFindClause) {
					SimpleFindClause clause = (SimpleFindClause) findClause;

					if (clause.getPropertyName() != null && clause.getPropertyName().equals("cd_categoria_gruppo") ||
						clause.getPropertyName() != null && clause.getPropertyName().equals("numeroOrdine") ||
						clause.getPropertyName() != null && clause.getPropertyName().equals("numeroBolla") ||
						clause.getPropertyName() != null && clause.getPropertyName().equals("dataBolla") ||
						clause.getPropertyName() != null && clause.getPropertyName().equals("dtOrdine") ||
						clause.getPropertyName() != null && clause.getPropertyName().equals("numeratoreOrdine")) {
						if (clause.getPropertyName() != null && clause.getPropertyName().equals("numeroBolla")) {
							sql.addSQLClause("AND", "MOVIMENTI_MAG.NUMERO_BOLLA", clause.getOperator(), clause.getValue());
						}
						else if (clause.getPropertyName() != null && clause.getPropertyName().equals("dataBolla")) {
							addDataBollaCondition( usercontext,sql,clause.getOperator(),clause.getValue());
						}
						else {
							sql.generateJoin(MovimentiMagBulk.class, LottoMagBulk.class, "lottoMag", "LOTTO_MAG");

							if(clause.getPropertyName() != null && clause.getPropertyName().equals("cd_categoria_gruppo")){
								sql.generateJoin(LottoMagBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
								sql.generateJoin(Bene_servizioBulk.class, Categoria_gruppo_inventBulk.class, "categoria_gruppo", "CATEGORIA_GRUPPO");
								sql.addSQLClause("AND", "CATEGORIA_GRUPPO.CD_CATEGORIA_GRUPPO", clause.getOperator(), clause.getValue());
							}else {

								sql.generateJoin(LottoMagBulk.class, OrdineAcqConsegnaBulk.class, "ordineAcqConsegna", "ORDINE_ACQ_CONSEGNA");

								if (clause.getPropertyName() != null && clause.getPropertyName().equals("dtOrdine")) {
									sql.generateJoin(OrdineAcqConsegnaBulk.class, OrdineAcqRigaBulk.class, "ordineAcqRiga", "ORDINE_ACQ_RIGA");
									sql.generateJoin(OrdineAcqRigaBulk.class, OrdineAcqBulk.class, "ordineAcq", "ORDINE_ACQ");
									sql.addSQLClause("AND", "ORDINE_ACQ.DATA_ORDINE", clause.getOperator(), clause.getValue());

								} else if (clause.getPropertyName() != null && clause.getPropertyName().equals("numeroOrdine")) {
									sql.addSQLClause("AND", "ORDINE_ACQ_CONSEGNA.NUMERO", clause.getOperator(), clause.getValue());
								}
								//NUMERATORE ORDINE
								else {
									sql.addSQLClause("AND", "ORDINE_ACQ_CONSEGNA.CD_NUMERATORE", clause.getOperator(), clause.getValue());
								}
							}
						}
					}
					if (clause.getPropertyName() != null && clause.getPropertyName().equals("search_ann")) {
						ricercaAnnullati=true;
					}
				}

			}
		}
		if(ricercaAnnullati){
			sql.openParenthesis("AND");
			sql.addClause("AND", "stato", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_ANNULLATO);
			sql.closeParenthesis();
		}else {
			sql.openParenthesis("AND");

			sql.addClause("AND", "stato", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_COMPLETO);
			sql.addClause("OR", "stato", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_INSERITO);
			sql.closeParenthesis();
		}
		Id_inventarioHome inventarioHome = (Id_inventarioHome) getHomeCache().getHome(Id_inventarioBulk.class);
		try {
			Id_inventarioBulk inventario = inventarioHome.findInventarioFor(usercontext,false);
			sql.addClause("AND", "pg_inventario", SQLBuilder.EQUALS, inventario.getPg_inventario());
		} catch (IntrospectionException e) {
			throw new PersistencyException(e);
			}
		sql.addOrderBy("id");
		return sql;

	}

	public SQLBuilder selectBeniInTransitoDaInventariare(UserContext usercontext, Transito_beni_ordiniBulk transito, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sqlBuilder = super.createSQLBuilder();
		if(compoundfindclause == null){
			if(transito != null)
				compoundfindclause = transito.buildFindClauses(null);
		} else {
			compoundfindclause = CompoundFindClause.and(compoundfindclause, transito.buildFindClauses(Boolean.FALSE));
		}
		sqlBuilder.addClause(compoundfindclause);



		sqlBuilder.addClause("AND", "stato", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_COMPLETO);
		Id_inventarioHome inventarioHome = (Id_inventarioHome) getHomeCache().getHome(Id_inventarioBulk.class);
		try {
			Id_inventarioBulk inventario = inventarioHome.findInventarioFor(usercontext,false);
			sqlBuilder.addClause("AND", "pg_inventario", SQLBuilder.EQUALS, inventario.getPg_inventario());
		} catch (IntrospectionException e) {
			throw new PersistencyException(e);
		}


		return sqlBuilder;
	}

	@Override
	public void update(Persistent persistent, UserContext userContext) throws PersistencyException {
		Transito_beni_ordiniBulk transito = (Transito_beni_ordiniBulk)persistent;

		if(transito.getFl_transito_canc()){
			transito.setStato(Transito_beni_ordiniBulk.STATO_ANNULLATO);
		}
		if (!transito.isStatoTrasferito() && !transito.isStatoAnnullato()){
			if (transito.isTuttiCampiValorizzatiPerInventariazione()){
				transito.setStato(Transito_beni_ordiniBulk.STATO_COMPLETO);
			} else {
				transito.setStato(Transito_beni_ordiniBulk.STATO_INSERITO);
			}
		}
		if (transito.getFl_ammortamento() != null && !transito.getFl_ammortamento()){
			transito.setTi_ammortamento(null);
		}

		super.update(persistent, userContext);
	}

	@Override
	public Persistent completeBulkRowByRow(UserContext userContext, Persistent persistent) throws PersistencyException {
		return super.completeBulkRowByRow(userContext, persistent);
	}
	public List findTransitiBeniOrdini(MovimentiMagBulk movimento) throws PersistencyException {
		SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.openParenthesis(FindClause.AND);
		sqlBuilder.addSQLClause(FindClause.OR, "STATO", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_COMPLETO);
		sqlBuilder.addSQLClause(FindClause.OR, "STATO", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_INSERITO);
		sqlBuilder.addSQLClause(FindClause.OR, "STATO", SQLBuilder.EQUALS, Transito_beni_ordiniBulk.STATO_TRASFERITO);
		sqlBuilder.closeParenthesis();
		sqlBuilder.addSQLClause(FindClause.AND, "ID_MOVIMENTI_MAG", SQLBuilder.EQUALS, movimento.getPgMovimento());
		return fetchAll(sqlBuilder);
	}

}
