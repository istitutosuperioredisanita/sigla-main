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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Fattura_attiva_rigaIHome extends Fattura_attiva_rigaHome {
	public Fattura_attiva_rigaIHome(java.sql.Connection conn) {
		super(Fattura_attiva_rigaIBulk.class, conn);
	}

	public Fattura_attiva_rigaIHome(java.sql.Connection conn, PersistentCache persistentCache) {
		super(Fattura_attiva_rigaIBulk.class, conn, persistentCache);
	}

	protected SQLBuilder selectForAccertamentoExceptFor(
			it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk scadenza,
			Fattura_attivaBulk fattura) {
		SQLBuilder sql = super.selectForAccertamentoExceptFor(scadenza, fattura);
		sql.addSQLClause("AND", "FATTURA_ATTIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_attivaBulk.TIPO_FATTURA_ATTIVA);
		return sql;
	}

	public SQLBuilder selectDocumentForReverse(
			UserContext usercontext,
			Fattura_attiva_rigaIBulk fatturaAttivaRigaIBulk,
			CompoundFindClause compoundfindclause,
			Object... objects
	) throws PersistencyException {
		Timestamp dataStornoFatture = ((Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class)).getDataStornoFatture(
				usercontext,
				CNRUserContext.getEsercizio(usercontext),
				Configurazione_cnrBulk.StepFineAnno.STORNO_FATT_ATT
		);
		SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.setAutoJoins(true);
		Optional.ofNullable(compoundfindclause).ifPresent(sqlBuilder::addClause);
		sqlBuilder.generateJoin(Fattura_attiva_rigaIBulk.class, Fattura_attiva_IBulk.class, "fattura_attivaI", "FATTURA_ATTIVA");
		sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_ATTIVA.DT_REGISTRAZIONE", SQLBuilder.LESS, dataStornoFatture);
		sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_ATTIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_attiva_IBulk.TIPO_FATTURA_ATTIVA);
		sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_ATTIVA.CD_UO_ORIGINE", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(usercontext));
		Stream.of("FL_INTRA_UE", "FL_EXTRA_UE", "FL_SAN_MARINO").forEach(s -> {
			sqlBuilder.addSQLClause(FindClause.AND, "FATTURA_ATTIVA." + s, SQLBuilder.EQUALS, "N");
		});
		/*
		Devo escludere dalla selezione delle fatture quelle non riportate
		 */
		sqlBuilder.openParenthesis(FindClause.AND);
		sqlBuilder.addSQLClause(FindClause.AND, "ESERCIZIO_ACCERTAMENTO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		sqlBuilder.addSQLClause(FindClause.OR, "ESERCIZIO_ACCERTAMENTO", SQLBuilder.ISNULL, null);
		sqlBuilder.closeParenthesis();

		sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(usercontext));
		sqlBuilder.addClause(FindClause.AND, "im_diponibile_nc", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		Stream.of(Documento_genericoBulk.STATO_ANNULLATO, Documento_genericoBulk.STATO_PAGATO).forEach(s -> {
			sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, s);
		});

		SQLBuilder sqlNotExists = createSQLBuilder();
		sqlNotExists.setFromClause(new StringBuffer("DOCUMENTO_GENERICO_RIGA STORNO, DOCUMENTO_GENERICO TESTATA_STORNO"));
		sqlNotExists.resetColumns();
		sqlNotExists.addColumn("1");
		sqlNotExists.addSQLJoin("STORNO.CD_CDS_STORNO_FA", "FATTURA_ATTIVA_RIGA.CD_CDS");
		sqlNotExists.addSQLJoin("STORNO.CD_UNITA_ORGANIZZATIVA_STORNO_FA", "FATTURA_ATTIVA_RIGA.CD_UNITA_ORGANIZZATIVA");
		sqlNotExists.addSQLJoin("STORNO.ESERCIZIO_STORNO_FA", "FATTURA_ATTIVA_RIGA.ESERCIZIO");
		sqlNotExists.addSQLJoin("STORNO.PG_FATTURA_ATTIVA_STORNO", "FATTURA_ATTIVA_RIGA.PG_FATTURA_ATTIVA");
		sqlNotExists.addSQLJoin("STORNO.PROGRESSIVO_RIGA_STORNO_FA", "FATTURA_ATTIVA_RIGA.PROGRESSIVO_RIGA");

		sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_CDS", "STORNO.CD_CDS");
		sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_UNITA_ORGANIZZATIVA", "STORNO.CD_UNITA_ORGANIZZATIVA");
		sqlNotExists.addSQLJoin("TESTATA_STORNO.ESERCIZIO", "STORNO.ESERCIZIO");
		sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_TIPO_DOCUMENTO_AMM", "STORNO.CD_TIPO_DOCUMENTO_AMM");
		sqlNotExists.addSQLJoin("TESTATA_STORNO.PG_DOCUMENTO_GENERICO", "STORNO.PG_DOCUMENTO_GENERICO");
		sqlNotExists.addSQLClause(FindClause.AND, "TESTATA_STORNO.STATO_COFI", SQLBuilder.NOT_EQUALS, Documento_genericoBulk.STATO_ANNULLATO);
		sqlBuilder.addSQLNotExistsClause(FindClause.AND, sqlNotExists);

		return sqlBuilder;
	}

	public Pair<ContoBulk, List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, Fattura_attiva_rigaBulk docRiga) throws ComponentException {
		ContoBulk aContoEconomico = this.getContoEconomicoDefault(docRiga);
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, docRiga, aContoEconomico, Fattura_attiva_riga_ecoIBulk.class);
		return Pair.of(aContoEconomico, aContiAnalitici);
	}

	public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomici(Fattura_attiva_rigaBulk docRiga) throws PersistencyException {
		ContoBulk aContoEconomico = docRiga.getVoce_ep();
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.findFatturaAttivaRigheEcoList(docRiga).stream()
				.map(IDocumentoDetailAnaCogeBulk.class::cast)
				.collect(Collectors.toList());
		return Pair.of(aContoEconomico, aContiAnalitici);
	}
}
