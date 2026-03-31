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

package it.cnr.contab.anagraf00.core.bulk;

import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.List;
import java.util.Optional;

public class BancaHome extends BulkHome {
    public BancaHome(java.sql.Connection conn) {
        super(BancaBulk.class, conn);
    }

    public BancaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(BancaBulk.class, conn, persistentCache);
    }

    /**
     * Imposta il pg_banca di un oggetto <code>BancaBulk</code>.
     *
     * @param bank <code>BancaBulk</code>
     * @throws PersistencyException
     */

    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bank) throws PersistencyException {
        try {
            ((BancaBulk) bank).setPg_banca(
					Long.valueOf(((Long) findAndLockMax(bank, "pg_banca", Long.valueOf(0))).longValue() + 1)
            );
        } catch (it.cnr.jada.bulk.BusyResourceException e) {
            throw new PersistencyException(e);
        }
    }

    public SQLBuilder selectBancaFor(CNRUserContext userContext, BancaBulk bancaBulk, CompoundFindClause compoundFindClause, Object... objects) {
        final Optional<Rif_modalita_pagamentoBulk> optionalRifModalitaPagamentoBulk = Optional.ofNullable(objects)
                .map(objects1 -> objects1[0])
                .filter(Rif_modalita_pagamentoBulk.class::isInstance)
                .map(Rif_modalita_pagamentoBulk.class::cast);
        final Optional<Integer> optionalCdTerzo = Optional.ofNullable(objects)
                .map(objects1 -> objects1[1])
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast);
        if (optionalRifModalitaPagamentoBulk.isPresent() && optionalCdTerzo.isPresent()) {
            return selectBancaFor(optionalRifModalitaPagamentoBulk.get(), optionalCdTerzo.get());
        }
        return createSQLBuilder();
    }
    /**
     * Imposta il pg_banca di un oggetto <code>BancaBulk</code>.
     *
     * @param rifModPag <code>Rif_modalita_pagamentoBulk</code>
     * @param codiceTerzo
     * @throws PersistencyException
     */

    public SQLBuilder selectBancaFor(Rif_modalita_pagamentoBulk rifModPag, Integer codiceTerzo) {

        SQLBuilder sql = createSQLBuilder();
        sql.setOrderBy("pg_banca", it.cnr.jada.util.OrderConstants.ORDER_DESC);
        sql.addSQLClause("AND", "BANCA.CD_TERZO", SQLBuilder.EQUALS, codiceTerzo);
        sql.addSQLClause("AND", "BANCA.TI_PAGAMENTO", SQLBuilder.EQUALS, rifModPag.getTi_pagamento());
        sql.addSQLClause("AND", "BANCA.FL_CANCELLATO", SQLBuilder.EQUALS, "N");

        if (rifModPag.getFl_per_cessione() != null &&
                rifModPag.getFl_per_cessione().booleanValue()) {

            sql.addTableToHeader("MODALITA_PAGAMENTO");
            sql.addSQLJoin("BANCA.CD_TERZO", "MODALITA_PAGAMENTO.CD_TERZO");
            // RP. 17/04/2013 commentato perchè possono essere + delegati attivi contemporaneamente
            //sql.addSQLJoin("BANCA.CD_TERZO_DELEGATO","MODALITA_PAGAMENTO.CD_TERZO_DELEGATO");
            sql.addSQLClause("AND", "BANCA.CD_TERZO_DELEGATO", SQLBuilder.ISNOTNULL, null);
            sql.addSQLClause("AND", "MODALITA_PAGAMENTO.CD_MODALITA_PAG", SQLBuilder.EQUALS, rifModPag.getCd_modalita_pag());
        } else
            sql.addSQLClause("AND", "BANCA.CD_TERZO_DELEGATO", SQLBuilder.ISNULL, null);

        return sql;
    }

	public List<BancaBulk> findBancaFor(UserContext userContext, Rif_modalita_pagamentoBulk rifModPag, Integer codiceTerzo) throws PersistencyException {
		final SQLBuilder sqlBuilder = this.selectBancaFor(rifModPag, codiceTerzo);
		return fetchAll(sqlBuilder);
	}
}
