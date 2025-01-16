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
 *
 * @author: Ardire Alfonso
 */

import it.cnr.contab.anagraf00.core.bulk.Termini_pagamentoBulk;
import it.cnr.contab.coepcoan00.core.bulk.PartitarioBulk;
import it.cnr.contab.config00.bulk.CausaleContabileBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Documento_generico_rigaHome extends BulkHome {
    public Documento_generico_rigaHome(java.sql.Connection conn) {
        super(Documento_generico_rigaBulk.class, conn);
    }

    public Documento_generico_rigaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Documento_generico_rigaBulk.class, conn, persistentCache);
    }

    /**
     * Inizializza la chiave primaria di un OggettoBulk per un
     * inserimento. Da usare principalmente per riempire i progressivi
     * automatici.
     * @param bulk l'OggettoBulk da inizializzare
     */
    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, it.cnr.jada.comp.ComponentException {

        if (bulk == null) return;
        try {
            Documento_generico_rigaBulk riga = (Documento_generico_rigaBulk) bulk;
            java.sql.Connection contact = getConnection();
            java.sql.ResultSet rs = contact.createStatement().executeQuery("SELECT MAX(PROGRESSIVO_RIGA) FROM " +
                    it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                    "DOCUMENTO_GENERICO_RIGA WHERE " +
                    "(ESERCIZIO = " + riga.getEsercizio().intValue() + ") AND " +
                    "(CD_CDS = '" + riga.getCd_cds() + "') AND " +
                    "(CD_UNITA_ORGANIZZATIVA = '" + riga.getCd_unita_organizzativa() + "') AND " +
                    "(CD_TIPO_DOCUMENTO_AMM = '" + riga.getDocumento_generico().getCd_tipo_documento_amm() + "') AND " +
                    "(PG_DOCUMENTO_GENERICO = " + riga.getPg_documento_generico().longValue() + ")");
            Long x;
            if (rs.next())
                x = new Long(rs.getLong(1) + 1);
            else
                x = new Long(0);
            riga.setProgressivo_riga(x);
        } catch (java.sql.SQLException sqle) {
            throw new PersistencyException(sqle);
        }
    }

    public Hashtable loadTiDocumentoAmmKeys(Documento_generico_rigaBulk documentoGenericoRigaBulk) throws PersistencyException {
        SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
        List<Tipo_documento_ammBulk> result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
        return new Hashtable(result
                .stream()
                .collect(Collectors.toMap(
                        Tipo_documento_ammKey::getCd_tipo_documento_amm,
                        Tipo_documento_ammBase::getDs_tipo_documento_amm, (key, value) -> value, HashMap::new)
                ));

    }

    public SQLBuilder selectDocumentForReverse(
            UserContext usercontext,
            Documento_generico_rigaBulk documentoGenericoRigaBulk,
            CompoundFindClause compoundfindclause,
            Object... objects
    ) throws PersistencyException {
        PersistentHome persistentHome = getHomeCache().getHome(Documento_generico_rigaBulk.class, "STORNO");
        SQLBuilder sqlBuilder = persistentHome.createSQLBuilder();
        sqlBuilder.setAutoJoins(true);
        Optional.ofNullable(compoundfindclause).ifPresent(sqlBuilder::addClause);
        sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(usercontext));
        Stream.of(Documento_genericoBulk.STATO_ANNULLATO, Documento_genericoBulk.STATO_PAGATO).forEach(s -> {
            sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, s);
        });
        sqlBuilder.generateJoin(Documento_genericoBulk.class, CausaleContabileBulk.class, "causaleContabile", "CAUSALE_CONTABILE");
        sqlBuilder.addClause(FindClause.AND, "documento_generico.causaleContabile.cdCausale", SQLBuilder.ISNOTNULL, null);
        sqlBuilder.addClause(FindClause.AND, "documento_generico.cd_uo_origine", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(usercontext));
        sqlBuilder.addClause(FindClause.AND, "documento_generico.fl_storno", SQLBuilder.NOT_EQUALS, Boolean.TRUE);
        sqlBuilder.generateJoin(Documento_genericoBulk.class, Tipo_documento_ammBulk.class,  "tipo_documento", "TIPO_DOCUMENTO_AMM");
        sqlBuilder.addClause(FindClause.AND, "documento_generico.tipo_documento.fl_utilizzo_doc_generico", SQLBuilder.EQUALS, Boolean.TRUE);
        sqlBuilder.addClause(
                FindClause.AND,
                "documento_generico.tipo_documento.ti_entrata_spesa",
                SQLBuilder.EQUALS,
                Arrays.stream(objects).map(String::valueOf).findFirst().orElse(null)
        );
        SQLBuilder sqlNotExists = createSQLBuilder();
        sqlNotExists.setFromClause(new StringBuffer("DOCUMENTO_GENERICO_RIGA STORNO, DOCUMENTO_GENERICO TESTATA_STORNO"));
        sqlNotExists.resetColumns();
        sqlNotExists.addColumn("1");
        sqlNotExists.addSQLJoin("STORNO.CD_CDS_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_CDS");
        sqlNotExists.addSQLJoin("STORNO.CD_UNITA_ORGANIZZATIVA_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_UNITA_ORGANIZZATIVA");
        sqlNotExists.addSQLJoin("STORNO.ESERCIZIO_STORNO", "DOCUMENTO_GENERICO_RIGA.ESERCIZIO");
        sqlNotExists.addSQLJoin("STORNO.CD_TIPO_DOCUMENTO_AMM_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_TIPO_DOCUMENTO_AMM");
        sqlNotExists.addSQLJoin("STORNO.PG_DOCUMENTO_GENERICO_STORNO", "DOCUMENTO_GENERICO_RIGA.PG_DOCUMENTO_GENERICO");
        sqlNotExists.addSQLJoin("STORNO.PROGRESSIVO_RIGA_STORNO", "DOCUMENTO_GENERICO_RIGA.PROGRESSIVO_RIGA");

        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_CDS", "STORNO.CD_CDS");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_UNITA_ORGANIZZATIVA", "STORNO.CD_UNITA_ORGANIZZATIVA");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.ESERCIZIO", "STORNO.ESERCIZIO");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_TIPO_DOCUMENTO_AMM", "STORNO.CD_TIPO_DOCUMENTO_AMM");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.PG_DOCUMENTO_GENERICO", "STORNO.PG_DOCUMENTO_GENERICO");
        sqlNotExists.addSQLClause(FindClause.AND, "TESTATA_STORNO.STATO_COFI", SQLBuilder.NOT_EQUALS, Documento_genericoBulk.STATO_ANNULLATO);
        sqlBuilder.addSQLNotExistsClause(FindClause.AND, sqlNotExists);
        return sqlBuilder;
    }
}