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
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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

            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passivaBulk.TIPO_NOTA_DI_CREDITO);

            sql.openParenthesis(FindClause.AND);

            sql.openParenthesis(FindClause.OR);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE", SQLBuilder.ISNOTNULL, null);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_CDS_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_cds());
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_UO_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_unita_organizzativa());

            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.ESERCIZIO_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getEsercizio());
            if (rigaFattura.getPg_fattura_passiva() != null)
                sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getPg_fattura_passiva());
            else
                sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", SQLBuilder.ISNULL, null);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_RIGA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getProgressivo_riga());
            sql.closeParenthesis();

            sql.openParenthesis(FindClause.OR);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE", SQLBuilder.ISNULL, null);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_CDS_ASSNCNA_ECO", SQLBuilder.EQUALS, rigaFattura.getCd_cds());
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_UO_ASSNCNA_ECO", SQLBuilder.EQUALS, rigaFattura.getCd_unita_organizzativa());

            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.ESERCIZIO_ASSNCNA_ECO", SQLBuilder.EQUALS, rigaFattura.getEsercizio());
            if (rigaFattura.getPg_fattura_passiva() != null)
                sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_ECO", SQLBuilder.EQUALS, rigaFattura.getPg_fattura_passiva());
            else
                sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_ECO", SQLBuilder.ISNULL, null);
            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_RIGA_ASSNCNA_ECO", SQLBuilder.EQUALS, rigaFattura.getProgressivo_riga());
            sql.closeParenthesis();

            sql.closeParenthesis();

            sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.STATO_COFI", SQLBuilder.NOT_EQUALS, Fattura_passiva_rigaBulk.STATO_ANNULLATO);
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

    public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, Nota_di_credito_rigaBulk docRiga) throws ComponentException, PersistencyException {
        if (docRiga.getRiga_fattura_origine()!=null && docRiga.getRiga_fattura_origine().getPg_fattura_passiva()!=null) {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            Fattura_passiva_rigaIHome fatpasRigaHome = (Fattura_passiva_rigaIHome)getHomeCache().getHome(Fattura_passiva_rigaIBulk.class);
            Fattura_passiva_rigaBulk rigaCollegata = (Fattura_passiva_rigaBulk)fatpasHome.loadIfNeededObject(docRiga.getRiga_fattura_origine());

            Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> datiEcoFattura = fatpasRigaHome.getDatiEconomici(rigaCollegata);
            if (datiEcoFattura.getFirst().getCd_voce_ep()==null)
                datiEcoFattura = fatpasRigaHome.getDatiEconomiciDefault(userContext, rigaCollegata);
            BigDecimal totaleImportiAnalitici = datiEcoFattura.getSecond().stream().map(IDocumentoDetailAnaCogeBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

            ContoBulk aContoEconomico = datiEcoFattura.getFirst();
            List<Fattura_passiva_riga_ecoBulk> aContiAnalitici = new ArrayList<>();

            for (IDocumentoDetailAnaCogeBulk rigaEco : datiEcoFattura.getSecond()) {
                Nota_di_credito_riga_ecoBulk myRigaEco = new Nota_di_credito_riga_ecoBulk();
                myRigaEco.setProgressivo_riga_eco((long) aContiAnalitici.size() + 1);
                myRigaEco.setVoce_analitica(rigaEco.getVoce_analitica());
                myRigaEco.setLinea_attivita(rigaEco.getLinea_attivita());
                myRigaEco.setFattura_passiva_riga(docRiga);
                myRigaEco.setImporto(rigaEco.getImporto().multiply(docRiga.getImCostoEco()).divide(totaleImportiAnalitici,2, RoundingMode.HALF_UP));
                myRigaEco.setToBeCreated();
                aContiAnalitici.add(myRigaEco);
            }

            BigDecimal totRipartito = aContiAnalitici.stream().map(Fattura_passiva_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal diff = totRipartito.subtract(docRiga.getImCostoEco());

            if (diff.compareTo(BigDecimal.ZERO)>0) {
                for (Fattura_passiva_riga_ecoBulk rigaEco : aContiAnalitici) {
                    if (rigaEco.getImporto().compareTo(diff) >= 0) {
                        rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
                        break;
                    } else {
                        diff = diff.subtract(rigaEco.getImporto());
                        rigaEco.setImporto(BigDecimal.ZERO);
                    }
                }
            } else if (diff.compareTo(BigDecimal.ZERO)<0) {
                for (Fattura_passiva_riga_ecoBulk rigaEco : aContiAnalitici) {
                    rigaEco.setImporto(rigaEco.getImporto().add(diff));
                    break;
                }
            }
            return Pair.of(aContoEconomico,
                    aContiAnalitici.stream().filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
                            .collect(Collectors.toList()));
        } else {
            ContoBulk aContoEconomico = this.getContoEconomicoDefault(docRiga);
            List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, docRiga, aContoEconomico, Nota_di_credito_riga_ecoBulk.class);
            return Pair.of(aContoEconomico, aContiAnalitici);
        }
    }
}
