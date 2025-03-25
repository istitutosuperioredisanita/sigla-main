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

package it.cnr.contab.config00.pdcep.bulk;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Home che gestisce i conti.
 */
public class ContoHome extends Voce_epHome {

    public ContoHome(java.sql.Connection conn) {
        super(ContoBulk.class, conn);
    }

    public ContoHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(ContoBulk.class, conn, persistentCache);
    }

    /**
     * Restituisce il SQLBuilder per selezionare fra tutte le Voci Economico Patrimoniali quelle relative
     * ai Conti.
     *
     * @return SQLBuilder
     */
    public SQLBuilder createSQLBuilder() {
        SQLBuilder sql = super.createSQLBuilder();
        sql.addClause("AND", "ti_voce_ep", SQLBuilder.EQUALS, TIPO_CONTO);
        return sql;
    }

    /**
     * Restituisce il SQLBuilder per selezionare gli elementi con Sezione di tipo Avere per l'esercizio di scrivania.
     *
     * @param bulk       bulk ricevente
     * @param home       home del bulk su cui si cerca
     * @param bulkClause è l'istanza di bulk che ha indotto le clauses
     * @param clause     clause che arrivano dalle properties (form collegata al search tool)
     * @return it.cnr.jada.persistency.sql.SQLBuilder
     */
    public SQLBuilder selectContiCostoByClause(Voce_epBulk bulk, it.cnr.jada.bulk.BulkHome home, it.cnr.jada.bulk.OggettoBulk bulkClause, CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = home.selectByClause(clause);
        sql.addClause("AND", "ti_sezione", SQLBuilder.NOT_EQUALS, SEZIONE_AVERE);
        sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio());
        return sql;
    }

    /**
     * Restituisce il SQLBuilder per selezionare gli elementi con Sezione di tipo Dare per l'esercizio di scrivania.
     *
     * @param bulk       bulk ricevente
     * @param home       home del bulk su cui si cerca
     * @param bulkClause è l'istanza di bulk che ha indotto le clauses
     * @param clause     clause che arrivano dalle properties (form collegata al search tool)
     * @return it.cnr.jada.persistency.sql.SQLBuilder
     */
    public SQLBuilder selectContiRicavoByClause(Voce_epBulk bulk, it.cnr.jada.bulk.BulkHome home, it.cnr.jada.bulk.OggettoBulk bulkClause, CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = home.selectByClause(clause);
        sql.addClause("AND", "ti_sezione", SQLBuilder.NOT_EQUALS, SEZIONE_DARE);
        sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio());
        return sql;
    }

    public SQLBuilder selectRiapre_a_contoByClause(Voce_epBulk bulk, it.cnr.jada.bulk.BulkHome home, it.cnr.jada.bulk.OggettoBulk bulkClause, CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = home.selectByClause(clause);
        sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio());
        if (bulk != null && bulk.getRiepiloga_a() != null)
            sql.addClause("AND", "riepiloga_a", SQLBuilder.EQUALS, bulk.getRiepiloga_a());
        return sql;
    }

    public SQLBuilder selectContoContropartitaByClause(Voce_epBulk bulk, it.cnr.jada.bulk.BulkHome home, it.cnr.jada.bulk.OggettoBulk bulkClause, CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = home.selectByClause(clause);
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio());
        return sql;
    }

    public SQLBuilder selectContiAssociatiACategoria(CompoundFindClause clause, Integer esercizio, Categoria_gruppo_inventBulk cat) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = selectByClause(clause);
        sql.addSQLClause("AND", "VOCE_EP.ESERCIZIO", SQLBuilder.EQUALS, esercizio);
        sql.addTableToHeader("ASS_CATGRP_INVENT_VOCE_EP");
        sql.addSQLJoin("VOCE_EP.ESERCIZIO", "ASS_CATGRP_INVENT_VOCE_EP.ESERCIZIO");
        sql.addSQLJoin("VOCE_EP.CD_VOCE_EP", "ASS_CATGRP_INVENT_VOCE_EP.CD_VOCE_EP");
        if (cat != null)
            sql.addSQLClause("AND", "ASS_CATGRP_INVENT_VOCE_EP.CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, cat.getCd_categoria_gruppo());
        return sql;
    }

    public SQLBuilder selectContoDefaultAssociatoACategoria(CompoundFindClause clause, Integer esercizio, Categoria_gruppo_inventBulk cat) throws java.lang.reflect.InvocationTargetException, IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = selectContiAssociatiACategoria(clause, esercizio, cat);
        sql.addSQLClause("AND", "ASS_CATGRP_INVENT_VOCE_EP.FL_DEFAULT", SQLBuilder.EQUALS, "Y");
        return sql;
    }

	public SQLBuilder selectContiNonAssociati(UserContext usercontext, ContoBulk contoBulk, CompoundFindClause compoundfindclause, Object... params) throws PersistencyException {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		final PersistentHome home = getHomeCache().getHome(AssociazioneContoGruppoBulk.class);
		final SQLBuilder sqlBuilderNotIN = home.createSQLBuilder();
		sqlBuilderNotIN.resetColumns();
		sqlBuilderNotIN.addColumn("CD_VOCE_EP");
		sqlBuilderNotIN.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
        Stream.of(params)
                .filter(TipoBilancioBulk.class::isInstance)
                .map(TipoBilancioBulk.class::cast)
                .findAny()
                .ifPresent(tipoBilancioBulk -> {
                    sqlBuilderNotIN.addClause(FindClause.AND, "tipoBilancio", SQLBuilder.EQUALS, tipoBilancioBulk);
                });
		sqlBuilder.addSQLNOTINClause(FindClause.AND, "CD_VOCE_EP", sqlBuilderNotIN);
		return sqlBuilder;
	}
}
