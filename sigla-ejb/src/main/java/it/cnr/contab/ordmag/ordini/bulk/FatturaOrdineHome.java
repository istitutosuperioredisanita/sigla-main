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
 * Date 21/09/2017
 */
package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_rigaBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CHARToBooleanConverter;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class FatturaOrdineHome extends BulkHome {
	public FatturaOrdineHome(Connection conn) {
		super(FatturaOrdineBulk.class, conn);
	}
	public FatturaOrdineHome(Connection conn, PersistentCache persistentCache) {
		super(FatturaOrdineBulk.class, conn, persistentCache);
	}

	public List<FatturaOrdineBulk> findByRigaConsegna(OrdineAcqConsegnaBulk ordineAcqConsegnaBulk) throws PersistencyException {
		final PersistentHome persistentHome = getHomeCache().getHome(FatturaOrdineBulk.class, "FATTURA_P");
		final SQLBuilder sqlBuilder = persistentHome.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "ordineAcqConsegna", SQLBuilder.EQUALS, ordineAcqConsegnaBulk);
		return fetchAll(sqlBuilder);
	}

	public FatturaOrdineBulk findFatturaByRigaConsegna(OrdineAcqConsegnaBulk ordineAcqConsegnaBulk) throws PersistencyException {
		final PersistentHome persistentHome = getHomeCache().getHome(FatturaOrdineBulk.class, "FATTURA_P");
		final SQLBuilder sql = persistentHome.createSQLBuilder();
		sql.addSQLClause(FindClause.AND, "CD_CDS_ORDINE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdCds());
		sql.addSQLClause(FindClause.AND, "CD_UNITA_OPERATIVA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdUnitaOperativa());
		sql.addSQLClause(FindClause.AND, "ESERCIZIO_ORDINE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getEsercizio());
		sql.addSQLClause(FindClause.AND, "CD_NUMERATORE", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getCdNumeratore());
		sql.addSQLClause(FindClause.AND, "NUMERO", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getNumero());
		sql.addSQLClause(FindClause.AND, "RIGA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getRiga());
		sql.addSQLClause(FindClause.AND, "CONSEGNA", SQLBuilder.EQUALS, ordineAcqConsegnaBulk.getConsegna());
		sql.addSQLClause(FindClause.AND, "ATTIVA", SQLBuilder.EQUALS, Boolean.TRUE);

		List result = fetchAll(sql);
		if (result.isEmpty())
			return null;
		return (FatturaOrdineBulk) result.getFirst();
	}

    public List<FatturaOrdineBulk> findByFattura(Fattura_passivaBulk fattura_passiva) throws PersistencyException {
        final FatturaOrdineHome fatturaOrdineHome = Optional.ofNullable(getHomeCache().getHome(FatturaOrdineBulk.class, fattura_passiva.getCd_tipo_doc()))
                .filter(FatturaOrdineHome.class::isInstance)
                .map(FatturaOrdineHome.class::cast)
                .orElseThrow(() -> new PersistenceException("Home di FatturaOrdineBulk non trovata!"));
        SQLBuilder sqlBuilder = fatturaOrdineHome.createSQLBuilder();
        sqlBuilder.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, fattura_passiva.getCd_cds());
        sqlBuilder.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fattura_passiva.getCd_unita_organizzativa());
        sqlBuilder.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, fattura_passiva.getEsercizio());
        sqlBuilder.addSQLClause(FindClause.AND, "PG_FATTURA_PASSIVA", SQLBuilder.EQUALS, fattura_passiva.getPg_fattura_passiva());
		sqlBuilder.addSQLClause(FindClause.AND,"ATTIVA",SQLBuilder.EQUALS,Boolean.TRUE,java.sql.Types.VARCHAR,0,new CHARToBooleanConverter(),true, false);

        return fatturaOrdineHome.fetchAll(sqlBuilder);
    }

	public FatturaOrdineBulk findByFatturaRiga(Fattura_passiva_rigaBulk fattura_passiva_riga) throws PersistencyException {
		final FatturaOrdineHome fatturaOrdineHome = Optional.ofNullable(getHomeCache().getHome(FatturaOrdineBulk.class, fattura_passiva_riga.getFattura_passiva().getCd_tipo_doc()))
				.filter(FatturaOrdineHome.class::isInstance)
				.map(FatturaOrdineHome.class::cast)
				.orElseThrow(() -> new PersistenceException("Home di FatturaOrdineBulk non trovata!"));
		SQLBuilder sqlBuilder = fatturaOrdineHome.createSQLBuilder();
		sqlBuilder.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, fattura_passiva_riga.getCd_cds());
		sqlBuilder.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fattura_passiva_riga.getCd_unita_organizzativa());
		sqlBuilder.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, fattura_passiva_riga.getEsercizio());
		sqlBuilder.addSQLClause(FindClause.AND, "PG_FATTURA_PASSIVA", SQLBuilder.EQUALS, fattura_passiva_riga.getPg_fattura_passiva());
		sqlBuilder.addSQLClause(FindClause.AND, "PROGRESSIVO_RIGA", SQLBuilder.EQUALS, fattura_passiva_riga.getProgressivo_riga());
		sqlBuilder.addSQLClause(FindClause.AND,"ATTIVA",SQLBuilder.EQUALS,Boolean.TRUE,java.sql.Types.VARCHAR,0,new CHARToBooleanConverter(),true, false);
		List result = fetchAll(sqlBuilder);
		if (result.isEmpty())
			return null;
		return (FatturaOrdineBulk) result.getFirst();
	}
}