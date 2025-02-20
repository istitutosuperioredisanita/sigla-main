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

import it.cnr.contab.config00.bulk.CausaleContabileBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Stream;

public class Fattura_passiva_rigaIHome extends Fattura_passiva_rigaHome {
    public Fattura_passiva_rigaIHome(java.sql.Connection conn) {
        super(Fattura_passiva_rigaIBulk.class, conn);
    }

    public Fattura_passiva_rigaIHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Fattura_passiva_rigaIBulk.class, conn, persistentCache);
    }

    protected SQLBuilder selectForObbligazioneExceptFor(
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk scadenza,
            Fattura_passivaBulk fattura) {
        SQLBuilder sql = super.selectForObbligazioneExceptFor(scadenza, fattura);
        sql.addSQLClause("AND", "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passivaBulk.TIPO_FATTURA_PASSIVA);
        return sql;
    }

	public SQLBuilder selectDocumentForReverse(
			UserContext usercontext,
			Fattura_passiva_rigaIBulk fatturaPassivaRigaIBulk,
			CompoundFindClause compoundfindclause,
			Object... objects
	) throws PersistencyException {
		Timestamp dataStornoFatture = ((Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class)).getDataStornoFatture(
				usercontext,
				CNRUserContext.getEsercizio(usercontext),
				Configurazione_cnrBulk.StepFineAnno.STORNO_FATT_PAS
		);
		SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.setAutoJoins(true);
		Optional.ofNullable(compoundfindclause).ifPresent(sqlBuilder::addClause);
		sqlBuilder.generateJoin(Fattura_passiva_rigaIBulk.class, Fattura_passiva_IBulk.class, "fattura_passivaI", "FATTURA_PASSIVA");
		sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_PASSIVA.DT_REGISTRAZIONE", SQLBuilder.LESS, dataStornoFatture);
		sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passiva_IBulk.TIPO_FATTURA_PASSIVA);
		Stream.of("FL_FATTURA_COMPENSO", "FL_INTRA_UE", "FL_EXTRA_UE", "FL_SAN_MARINO_CON_IVA", "FL_SAN_MARINO_SENZA_IVA").forEach(s -> {
			sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_PASSIVA." + s, SQLBuilder.EQUALS, "N");
		});
		sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(usercontext));
		sqlBuilder.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(usercontext));
		sqlBuilder.addClause(FindClause.AND, "im_diponibile_nc", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		Stream.of(Fattura_passivaBulk.STATO_ANNULLATO, Fattura_passivaBulk.STATO_PAGATO).forEach(s -> {
			sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, s);
		});

		SQLBuilder sqlNotExists = createSQLBuilder();
		sqlNotExists.setFromClause(new StringBuffer("DOCUMENTO_GENERICO_RIGA STORNO, DOCUMENTO_GENERICO TESTATA_STORNO"));
		sqlNotExists.resetColumns();
		sqlNotExists.addColumn("1");
		sqlNotExists.addSQLJoin("STORNO.CD_CDS_STORNO_FP", "FATTURA_PASSIVA_RIGA.CD_CDS");
		sqlNotExists.addSQLJoin("STORNO.CD_UNITA_ORGANIZZATIVA_STORNO_FP", "FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA");
		sqlNotExists.addSQLJoin("STORNO.ESERCIZIO_STORNO_FP", "FATTURA_PASSIVA_RIGA.ESERCIZIO");
		sqlNotExists.addSQLJoin("STORNO.PG_FATTURA_PASSIVA_STORNO", "FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA");
		sqlNotExists.addSQLJoin("STORNO.PROGRESSIVO_RIGA_STORNO_FP", "FATTURA_PASSIVA_RIGA.PROGRESSIVO_RIGA");

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
