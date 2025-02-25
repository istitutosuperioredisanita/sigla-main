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

package it.cnr.contab.gestiva00.comp;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleHome;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.RicercaComponent;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

/**
 * Insert the type's description here.
 * Creation date: (08/10/2001 15:39:09)
 *
 * @author: CNRADM
 */
public class ConsRegIvaComponent extends RicercaComponent {

    public ConsRegIvaComponent() {
        super();
    }


    public java.util.Collection selectTipi_sezionaliByClause(UserContext userContext,
                                                  OggettoBulk model, Tipo_sezionaleBulk prototype, CompoundFindClause clause)
            throws ComponentException, PersistencyException {

        try {
            Tipo_sezionaleHome home = (Tipo_sezionaleHome) getHome(userContext, Tipo_sezionaleBulk.class);
            SQLBuilder sql = home.createSQLBuilder();
            V_cons_dett_ivaBulk bulk = (V_cons_dett_ivaBulk) model;
            Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);

            if (clause != null)
                sql.addClause(clause);
            sql.setDistinctClause(true);

            sql.addTableToHeader("SEZIONALE");
            sql.addSQLJoin("TIPO_SEZIONALE.CD_TIPO_SEZIONALE", "SEZIONALE.CD_TIPO_SEZIONALE");
//            sql.addSQLClause("AND", "SEZIONALE.CD_CDS", SQLBuilder.EQUALS, bulk.getCdCdsOrigine());
//            sql.addSQLClause("AND", "SEZIONALE.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, bulk.getCdUnitaOrganizzativa());
            sql.addSQLClause("AND", "SEZIONALE.ESERCIZIO", SQLBuilder.EQUALS, esercizio);
            sql.addSQLClause("AND", "TIPO_SEZIONALE.FL_AUTOFATTURA", SQLBuilder.EQUALS, "N");

            // Clausole OR per i diversi tipi di sezionali
            sql.openParenthesis("AND");

            // Commerciali (a/com, v/com)
            sql.openParenthesis("OR");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_ISTITUZ_COMMERC", SQLBuilder.EQUALS, "C");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.FL_ORDINARIO", SQLBuilder.EQUALS, "Y");
            sql.closeParenthesis();

            // Servizi non residenti (a/isnr)
            sql.openParenthesis("OR");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_ISTITUZ_COMMERC", SQLBuilder.EQUALS, "I");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_BENE_SERVIZIO", SQLBuilder.EQUALS, "S");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.FL_SERVIZI_NON_RESIDENTI", SQLBuilder.EQUALS, "Y");
            sql.closeParenthesis();

            // Split payment (a/ispp)
            sql.openParenthesis("OR");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_ISTITUZ_COMMERC", SQLBuilder.EQUALS, "I");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.FL_SPLIT_PAYMENT", SQLBuilder.EQUALS, "Y");
            sql.closeParenthesis();

            // Beni non residenti (a/iue)
            sql.openParenthesis("OR");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_ISTITUZ_COMMERC", SQLBuilder.EQUALS, "I");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.TI_BENE_SERVIZIO", SQLBuilder.EQUALS, "B");
            sql.addSQLClause("AND", "TIPO_SEZIONALE.FL_INTRA_UE", SQLBuilder.EQUALS, "Y");
            sql.closeParenthesis();

            sql.closeParenthesis();

            // Ordinamento per il campo ORDINA
            sql.addOrderBy("TIPO_SEZIONALE.ORDINA");

            return home.fetchAll(sql);
        } catch(PersistencyException e) {
            throw handleException(e);
        }
    }

//
//    // This method should be in your component interface (ConsRegIvaComponentSession)
//    public Collection queryDettRegIva(UserContext userContext, String cdTipoSezionale)
//            throws ComponentException {
//        try {
//            // Crea direttamente un'istanza della home usando il costruttore con PersistentCache
//            PersistentCache persistentCache = getComponentSession().getPersistentCache();
//            V_cons_dett_ivaHome home = new V_cons_dett_ivaHome(getConnection(userContext), persistentCache);
//
//            // Crea SQLBuilder manualmente
//            SQLBuilder sql = home.createSQLBuilder();
//
//            // Aggiungi le condizioni
//            Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);
//            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);
//            sql.addSQLClause("AND", "CD_TIPO_SEZIONALE", SQLBuilder.EQUALS, cdTipoSezionale);
//
//            // Costruisci la query come nel metodo originale
//            StringBuilder selectClause = new StringBuilder(
//                    "SELECT ESERCIZIO, " +
//                            "SUBSTR(DATA_REGISTRAZIONE,6,2) AS MESE, " +
//                            "NVL(CD_TIPO_SEZIONALE,'Tot') AS TIPO_SEZIONALE");
//
//            if (cdTipoSezionale.equals("a/com") || cdTipoSezionale.equals("v/com")) {
//                selectClause.append(", SUM(DECODE(SP,'N',IVA_D,0)) AS SP_N")
//                        .append(", SUM(DECODE(SP,'Y',IVA_D,0)) AS SP_Y")
//                        .append(", SUM(IVA_D) AS TOT_IVA");
//            } else {
//                selectClause.append(", SUM(IVA_D) AS TOT_IVA");
//            }
//
//            sql.setHeader(selectClause.toString());
//
//            sql.addSQLGroupBy("ESERCIZIO");
//            sql.addSQLGroupBy("DATA_REGISTRAZIONE");
//            sql.addSQLGroupBy("CD_TIPO_SEZIONALE");
//
//            sql.addOrderBy("DATA_REGISTRAZIONE");
//            sql.addOrderBy("CD_TIPO_SEZIONALE");
//
//            // Esegui la query e restituisci i risultati
//            return home.fetchAll(sql);
//        } catch (PersistencyException e) {
//            throw new ComponentException(e);
//        }
//    }

//
//    // Private method with the logic from selectByClause
//    private SQLBuilder createSQLBuilder(UserContext userContext, V_cons_dett_ivaHome home,
//                                        String cdTipoSezionale) throws PersistencyException {
//        // Base SQL builder
//        SQLBuilder sql = home.createSQLBuilder();
//
//        // Add exercise condition
//        Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);
//        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);
//
//        if (cdTipoSezionale != null) {
//
//            // Build the select clause
//            StringBuilder selectClause = new StringBuilder(
//                    "SELECT ESERCIZIO, " +
//                            "SUBSTR(DATA_REGISTRAZIONE,6,2) AS MESE, " +
//                            "NVL(CD_TIPO_SEZIONALE,'Tot') AS TIPO_SEZIONALE"
//            );
//
//            if (cdTipoSezionale.equals("a/com") || cdTipoSezionale.equals("v/com")) {
//                selectClause.append(", SUM(DECODE(SP,'N',IVA_D,0)) AS SP_N")
//                        .append(", SUM(DECODE(SP,'Y',IVA_D,0)) AS SP_Y")
//                        .append(", SUM(IVA_D) AS TOT_IVA");
//            } else {
//                selectClause.append(", SUM(IVA_D) AS TOT_IVA");
//            }
//
//            sql.setHeader(selectClause.toString());
//
//            // Add the tipo_sezionale condition
//            sql.addSQLClause("AND", "CD_TIPO_SEZIONALE", SQLBuilder.EQUALS, cdTipoSezionale);
//
//            // Add GROUP BY clauses
//            sql.addSQLGroupBy("ESERCIZIO");
//            sql.addSQLGroupBy("DATA_REGISTRAZIONE");
//            sql.addSQLGroupBy("CD_TIPO_SEZIONALE");
//
//            // Add ORDER BY clauses
//            sql.addOrderBy("DATA_REGISTRAZIONE");
//            sql.addOrderBy("CD_TIPO_SEZIONALE");
//        }
//
//        return sql;
//    }



    public RemoteIterator cercaDettRegIva(UserContext userContext, String cdTipoSezionale) throws ComponentException {
        try {
            // Usa il costruttore corretto per SQLBuilder
            V_cons_dett_ivaHome home = (V_cons_dett_ivaHome) getHome(userContext, V_cons_dett_ivaBulk.class);
            SQLBuilder sql = home.createSQLBuilder();

            // Oppure, se esiste un metodo factory o un altro costruttore
            // SQLBuilder sql = SQLBuilder.newBuilder(getConnection(userContext));

            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));
            sql.addSQLClause("AND", "CD_TIPO_SEZIONALE", SQLBuilder.EQUALS, cdTipoSezionale);

            // Imposta la query SELECT...
            StringBuilder selectClause = new StringBuilder(
                    "SELECT ESERCIZIO, " +
                            "SUBSTR(DATA_REGISTRAZIONE,6,2) AS MESE, " +
                            "NVL(CD_TIPO_SEZIONALE,'Tot') AS CD_TIPO_SEZIONALE"
            );

            if (cdTipoSezionale.equals("a/com") || cdTipoSezionale.equals("v/com")) {
                selectClause.append(", SUM(DECODE(SP,'N',IVA_D,0)) AS SP_N")
                        .append(", SUM(DECODE(SP,'Y',IVA_D,0)) AS SP_Y")
                        .append(", SUM(IVA_D) AS TOT_IVA");
            } else {
                selectClause.append(", SUM(IVA_D) AS TOT_IVA");
            }

            sql.setHeader(selectClause.toString());

            sql.addSQLGroupBy("ESERCIZIO");
            sql.addSQLGroupBy("DATA_REGISTRAZIONE");
            sql.addSQLGroupBy("CD_TIPO_SEZIONALE");

            sql.addOrderBy("DATA_REGISTRAZIONE");
            sql.addOrderBy("CD_TIPO_SEZIONALE");

            // Esegui la query direttamente
            return iterator(userContext, sql, V_cons_dett_ivaBulk.class, "default");
        } catch (Throwable t) {
            throw handleException(t);
        }
    }

}