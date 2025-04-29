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

import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.Optional;

/**
 * Insert the type's description here.
 * Creation date: (10/24/2001 2:32:18 PM)
 *
 * @author: Roberto Peli
 */
public class Nota_di_credito_rigaHome extends Fattura_passiva_rigaHome {
    /**
     * Nota_di_creditoHome constructor comment.
     *
     * @param conn java.sql.Connection
     */
    public Nota_di_credito_rigaHome(java.sql.Connection conn) {

        super(Nota_di_credito_rigaBulk.class, conn);
    }

    /**
     * Nota_di_creditoHome constructor comment.
     *
     * @param conn            java.sql.Connection
     * @param persistentCache it.cnr.jada.persistency.PersistentCache
     */
    public Nota_di_credito_rigaHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
        super(Nota_di_credito_rigaBulk.class, conn, persistentCache);
    }

    public java.util.List findRigaFor(Fattura_passiva_rigaIBulk rigaFattura) {

        try {
            return fetchAll(selectRigaFor(rigaFattura));
        } catch (it.cnr.jada.persistency.PersistencyException e) {
            return null;
        }
    }

    protected SQLBuilder selectForObbligazioneExceptFor(
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk scadenza,
            Fattura_passivaBulk fattura) {

        SQLBuilder sql = super.selectForObbligazioneExceptFor(scadenza, fattura);

        sql.addSQLClause("AND", "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passivaBulk.TIPO_NOTA_DI_CREDITO);

        return sql;
    }

    private SQLBuilder selectRigaFor(Fattura_passiva_rigaIBulk rigaFattura) {

        SQLBuilder sql = createSQLBuilder();

        if (rigaFattura != null) {
            sql.addTableToHeader("FATTURA_PASSIVA");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO", "FATTURA_PASSIVA.ESERCIZIO");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_CDS", "FATTURA_PASSIVA.CD_CDS");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "FATTURA_PASSIVA.CD_UNITA_ORGANIZZATIVA");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA", "FATTURA_PASSIVA.PG_FATTURA_PASSIVA");

            sql.addSQLClause("AND", "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passivaBulk.TIPO_NOTA_DI_CREDITO);

            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.CD_CDS_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_cds());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.CD_UO_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_unita_organizzativa());
            //Gennaro Borriello - (03/11/2004 19.04.48)
            // Fix sul controllo dello "Stato Riportato"
            //if (!rigaFattura.getFattura_passiva().isRiportata() && !rigaFattura.getFattura_passiva().isRiportataInScrivania())
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.ESERCIZIO_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getEsercizio());
            /*
             * Modifica effettuata da marco il 03/11/2004 per gestire il caso in cui la
             * fattura non è ancora salvata
             */
            if (rigaFattura.getPg_fattura_passiva() != null)
                sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getPg_fattura_passiva());
            else
                sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", SQLBuilder.ISNULL, null);
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_RIGA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getProgressivo_riga());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.STATO_COFI", SQLBuilder.NOT_EQUALS, Fattura_passiva_rigaBulk.STATO_ANNULLATO);
        }
        return sql;
    }

	public Optional<Nota_di_credito_rigaBulk> findRigaNota(Fattura_passiva_rigaIBulk fatturaPassivaRigaIBulk) throws PersistencyException {
		final SQLBuilder sqlBuilder = createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "riga_fattura_origine", SQLBuilder.EQUALS, fatturaPassivaRigaIBulk);
		return fetchAll(sqlBuilder)
				.stream()
				.findAny();
	}

    public ContoBulk getContoCostoDefault(Nota_di_credito_rigaBulk docRiga) {
        if (docRiga.getRiga_fattura_origine()!=null && docRiga.getRiga_fattura_origine().getPg_fattura_passiva()!=null) {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            Fattura_passiva_rigaBulk rigaCollegata = (Fattura_passiva_rigaBulk)fatpasHome.loadIfNeededObject(docRiga.getRiga_fattura_origine());
            return super.getContoCostoDefault(rigaCollegata);
        }
        return null;
    }
}
