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

package it.cnr.contab.doccont00.comp;


import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.doccont00.consultazioni.bulk.*;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaIBulk;
import it.cnr.contab.doccont00.core.bulk.Reversale_rigaIBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;


public class ConsRiepilogoSiopeComponent extends CRUDComponent {

    public ConsRiepilogoSiopeComponent() {
        super();
    }

    public RemoteIterator findSiopeDettaglioMandati(UserContext userContext, String pathDestinazione, String livelloDestinazione, CompoundFindClause baseClause, CompoundFindClause findClause, OggettoBulk bulk) throws ComponentException, RemoteException {

        BulkHome home = getHome(userContext, V_cons_siope_mandatiBulk.class, pathDestinazione);
        V_cons_siope_mandatiBulk mandati = (V_cons_siope_mandatiBulk) bulk;
        SQLBuilder sql = home.createSQLBuilder();
        addColumnDett(sql, livelloDestinazione, bulk);
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, mandati.getCd_cds());
        sql.addSQLClause("AND", "CD_SIOPE", SQLBuilder.EQUALS, mandati.getCd_siope());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.GREATER_EQUALS, mandati.getDt_emissione_da());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.LESS_EQUALS, mandati.getDt_emissione_a());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.GREATER_EQUALS, mandati.getDt_trasmissione_da());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.LESS_EQUALS, mandati.getDt_trasmissione_a());
        sql.addSQLClause("AND", "DT_PAGAMENTO", SQLBuilder.GREATER_EQUALS, mandati.getDt_pagamento_da());
        sql.addSQLClause("AND", "DT_PAGAMENTO", SQLBuilder.LESS_EQUALS, mandati.getDt_pagamento_a());
        if (mandati.isTipoPagamentoValorizzato()) {
            final SQLBuilder sqlBuilder = getHome(userContext, Mandato_rigaIBulk.class).createSQLBuilder();
            sqlBuilder.resetColumns();
            sqlBuilder.addColumn("1");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.ESERCIZIO", "MANDATO_RIGA.ESERCIZIO");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.CD_CDS", "MANDATO_RIGA.CD_CDS");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.PG_MANDATO", "MANDATO_RIGA.PG_MANDATO");
            sqlBuilder.generateJoin("banca", "BANCA");
            sqlBuilder.addSQLClause(
                    FindClause.AND,
                    "BANCA.TI_PAGAMENTO",
                    mandati.getTipoPagamento().equalsIgnoreCase(V_cons_siope_mandatiBulk.TipoPagamentoEnum.BANCA_ITALIA.value()) ? SQLBuilder.EQUALS : SQLBuilder.NOT_EQUALS,
                    Rif_modalita_pagamentoBulk.BANCA_ITALIA
            );
            sql.addSQLExistsClause(FindClause.AND, sqlBuilder);
        }
        return iterator(userContext, completaSQL(sql, baseClause, findClause), V_cons_siope_mandatiBulk.class, null);
    }

    public RemoteIterator findSiopeDettaglioReversali(UserContext userContext, String pathDestinazione, String livelloDestinazione, CompoundFindClause baseClause, CompoundFindClause findClause, OggettoBulk bulk) throws ComponentException, RemoteException {

        BulkHome home = getHome(userContext, V_cons_siope_reversaliBulk.class, pathDestinazione);
        V_cons_siope_reversaliBulk reversali = (V_cons_siope_reversaliBulk) bulk;
        SQLBuilder sql = home.createSQLBuilder();
        addColumnDett(sql, livelloDestinazione, bulk);
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, reversali.getCd_cds());
        sql.addSQLClause("AND", "CD_SIOPE", SQLBuilder.EQUALS, reversali.getCd_siope());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.GREATER_EQUALS, reversali.getDt_emissione_da());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.LESS_EQUALS, reversali.getDt_emissione_a());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.GREATER_EQUALS, reversali.getDt_trasmissione_da());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.LESS_EQUALS, reversali.getDt_trasmissione_a());
        sql.addSQLClause("AND", "DT_INCASSO", SQLBuilder.GREATER_EQUALS, reversali.getDt_incasso_da());
        sql.addSQLClause("AND", "DT_INCASSO", SQLBuilder.LESS_EQUALS, reversali.getDt_incasso_a());
        if (reversali.isTipoIncassoValorizzato()) {
            final SQLBuilder sqlBuilder = getHome(userContext, Reversale_rigaIBulk.class).createSQLBuilder();
            sqlBuilder.resetColumns();
            sqlBuilder.addColumn("1");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.ESERCIZIO", "REVERSALE_RIGA.ESERCIZIO");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.CD_CDS", "REVERSALE_RIGA.CD_CDS");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.PG_REVERSALE", "REVERSALE_RIGA.PG_REVERSALE");
            sqlBuilder.generateJoin("banca", "BANCA");
            sqlBuilder.addSQLClause(
                    FindClause.AND,
                    "BANCA.TI_PAGAMENTO",
                    reversali.getTipoIncasso().equalsIgnoreCase(V_cons_siope_mandatiBulk.TipoPagamentoEnum.BANCA_ITALIA.value()) ? SQLBuilder.EQUALS : SQLBuilder.NOT_EQUALS,
                    Rif_modalita_pagamentoBulk.BANCA_ITALIA
            );
            sql.addSQLExistsClause(FindClause.AND, sqlBuilder);
        }
        return iterator(userContext, completaSQL(sql, baseClause, findClause), V_cons_siope_reversaliBulk.class, null);
    }


    public it.cnr.jada.util.RemoteIterator findSiopeMandati(UserContext userContext, OggettoBulk bulk)
            throws ComponentException, RemoteException {
        V_cons_siope_mandatiBulk mandati = (V_cons_siope_mandatiBulk) bulk;
        V_cons_siope_mandatiHome mandatiHome = (V_cons_siope_mandatiHome) getHome(userContext, V_cons_siope_mandatiBulk.class, "BASE");
        SQLBuilder sql = mandatiHome.createSQLBuilder();
        sql.resetColumns();
        addBaseColumns(userContext, sql);
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, mandati.getCd_cds());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.GREATER_EQUALS, mandati.getDt_emissione_da());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.LESS_EQUALS, mandati.getDt_emissione_a());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.GREATER_EQUALS, mandati.getDt_trasmissione_da());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.LESS_EQUALS, mandati.getDt_trasmissione_a());
        sql.addSQLClause("AND", "DT_PAGAMENTO", SQLBuilder.GREATER_EQUALS, mandati.getDt_pagamento_da());
        sql.addSQLClause("AND", "DT_PAGAMENTO", SQLBuilder.LESS_EQUALS, mandati.getDt_pagamento_a());
        if (mandati.isTipoPagamentoValorizzato()) {
            final SQLBuilder sqlBuilder = getHome(userContext, Mandato_rigaIBulk.class).createSQLBuilder();
            sqlBuilder.resetColumns();
            sqlBuilder.addColumn("1");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.ESERCIZIO", "MANDATO_RIGA.ESERCIZIO");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.CD_CDS", "MANDATO_RIGA.CD_CDS");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_MANDATI.PG_MANDATO", "MANDATO_RIGA.PG_MANDATO");
            sqlBuilder.generateJoin("banca", "BANCA");
            sqlBuilder.addSQLClause(
                    FindClause.AND,
                    "BANCA.TI_PAGAMENTO",
                    mandati.getTipoPagamento().equalsIgnoreCase(V_cons_siope_mandatiBulk.TipoPagamentoEnum.BANCA_ITALIA.value()) ? SQLBuilder.EQUALS : SQLBuilder.NOT_EQUALS,
                    Rif_modalita_pagamentoBulk.BANCA_ITALIA
            );
            sql.addSQLExistsClause(FindClause.AND, sqlBuilder);
        }
        return iterator(userContext, sql, V_cons_siope_mandatiBulk.class, null);
    }

    public it.cnr.jada.util.RemoteIterator findSiopeReversali(UserContext userContext, OggettoBulk bulk)
            throws ComponentException, RemoteException {
        V_cons_siope_reversaliBulk reversali = (V_cons_siope_reversaliBulk) bulk;
        V_cons_siope_reversaliHome reversaliHome = (V_cons_siope_reversaliHome) getHome(userContext, V_cons_siope_reversaliBulk.class, "BASE");
        SQLBuilder sql = reversaliHome.createSQLBuilder();
        sql.resetColumns();
        addBaseColumns(userContext, sql);
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, reversali.getCd_cds());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.GREATER_EQUALS, reversali.getDt_emissione_da());
        sql.addSQLClause("AND", "DT_EMISSIONE", SQLBuilder.LESS_EQUALS, reversali.getDt_emissione_a());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.GREATER_EQUALS, reversali.getDt_trasmissione_da());
        sql.addSQLClause("AND", "DT_TRASMISSIONE", SQLBuilder.LESS_EQUALS, reversali.getDt_trasmissione_a());
        sql.addSQLClause("AND", "DT_INCASSO", SQLBuilder.GREATER_EQUALS, reversali.getDt_incasso_da());
        sql.addSQLClause("AND", "DT_INCASSO", SQLBuilder.LESS_EQUALS, reversali.getDt_incasso_a());
        if (reversali.isTipoIncassoValorizzato()) {
            final SQLBuilder sqlBuilder = getHome(userContext, Reversale_rigaIBulk.class).createSQLBuilder();
            sqlBuilder.resetColumns();
            sqlBuilder.addColumn("1");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.ESERCIZIO", "REVERSALE_RIGA.ESERCIZIO");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.CD_CDS", "REVERSALE_RIGA.CD_CDS");
            sqlBuilder.addSQLJoin("V_CONS_SIOPE_REVERSALI.PG_REVERSALE", "REVERSALE_RIGA.PG_REVERSALE");
            sqlBuilder.generateJoin("banca", "BANCA");
            sqlBuilder.addSQLClause(
                    FindClause.AND,
                    "BANCA.TI_PAGAMENTO",
                    reversali.getTipoIncasso().equalsIgnoreCase(V_cons_siope_mandatiBulk.TipoPagamentoEnum.BANCA_ITALIA.value()) ? SQLBuilder.EQUALS : SQLBuilder.NOT_EQUALS,
                    Rif_modalita_pagamentoBulk.BANCA_ITALIA
            );
            sql.addSQLExistsClause(FindClause.AND, sqlBuilder);
        }
        return iterator(userContext, sql, V_cons_siope_reversaliBulk.class, null);
    }

    private SQLBuilder completaSQL(SQLBuilder sql, CompoundFindClause baseClause, CompoundFindClause findClause) {
        sql.addClause(baseClause);
        if (findClause == null)
            return sql;
        else {
            sql.addClause(findClause);
            return sql;
        }
    }


    private void addBaseColumns(UserContext userContext, SQLBuilder sql) throws it.cnr.jada.comp.ComponentException {        //,  String pathDestinazione, String livelloDestinazione,boolean isBaseSQL) throws it.cnr.jada.comp.ComponentException {
        try {
			sql.resetColumns();
			sql.addColumn("ESERCIZIO");
			Unita_organizzativa_enteBulk unitaEnte = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
			if (!CNRUserContext.getCd_unita_organizzativa(userContext).equalsIgnoreCase(unitaEnte.getCd_unita_organizzativa())) {
                sql.addColumn("CD_CDS");
            } else {
                sql.addColumn("''", "CD_CDS");
            }
			sql.addColumn("CD_SIOPE");
			sql.addColumn("DS_SIOPE");
			sql.addColumn("NVL(SUM(IMPORTO),0)", "IMPORTO");
			addSQLGroupBy(sql, "ESERCIZIO", true);
			if (!CNRUserContext.getCd_unita_organizzativa(userContext).equalsIgnoreCase(unitaEnte.getCd_unita_organizzativa()))
				addSQLGroupBy(sql, "CD_CDS", true);
			addSQLGroupBy(sql, "CD_SIOPE", true);
			addSQLGroupBy(sql, "DS_SIOPE", true);
		} catch (PersistencyException _ex) {
			throw handleException(_ex);
		}
    }


    private void addColumnDett(SQLBuilder sql, String livelloDestinazione, OggettoBulk oggettobulk) {
        if (oggettobulk instanceof V_cons_siope_mandatiBulk) {
            sql.resetColumns();
            sql.addColumn("ESERCIZIO");
            sql.addColumn("CD_CDS");
            sql.addColumn("CD_SIOPE");
            sql.addColumn("DS_SIOPE");
            sql.addColumn("PG_MANDATO");
            sql.addColumn("ESERCIZIO_OBBLIGAZIONE");
            sql.addColumn("ESERCIZIO_ORI_OBBLIGAZIONE");
            sql.addColumn("PG_OBBLIGAZIONE");
            sql.addColumn("PG_OBBLIGAZIONE_SCADENZARIO");
            sql.addColumn("CD_CDS_DOC_AMM");
            sql.addColumn("CD_UO_DOC_AMM");
            sql.addColumn("ESERCIZIO_DOC_AMM");
            sql.addColumn("CD_TIPO_DOCUMENTO_AMM");
            sql.addColumn("DS_TIPO_DOC_AMM");
            sql.addColumn("PG_DOC_AMM");
            sql.addColumn("ESERCIZIO_SIOPE");
            sql.addColumn("TI_GESTIONE");
            sql.addColumn("IMPORTO");
            sql.addColumn("DT_EMISSIONE");
            sql.addColumn("DT_TRASMISSIONE");
            sql.addColumn("DT_PAGAMENTO");
        }
        if (oggettobulk instanceof V_cons_siope_reversaliBulk) {
            sql.resetColumns();
            sql.addColumn("ESERCIZIO");
            sql.addColumn("CD_CDS");
            sql.addColumn("CD_SIOPE");
            sql.addColumn("DS_SIOPE");
            sql.addColumn("PG_REVERSALE");
            sql.addColumn("ESERCIZIO_ACCERTAMENTO");
            sql.addColumn("ESERCIZIO_ORI_ACCERTAMENTO");
            sql.addColumn("PG_ACCERTAMENTO");
            sql.addColumn("PG_ACCERTAMENTO_SCADENZARIO");
            sql.addColumn("CD_CDS_DOC_AMM");
            sql.addColumn("CD_UO_DOC_AMM");
            sql.addColumn("ESERCIZIO_DOC_AMM");
            sql.addColumn("CD_TIPO_DOCUMENTO_AMM");
            sql.addColumn("DS_TIPO_DOC_AMM");
            sql.addColumn("PG_DOC_AMM");
            sql.addColumn("ESERCIZIO_SIOPE");
            sql.addColumn("TI_GESTIONE");
            sql.addColumn("IMPORTO");
            sql.addColumn("DT_EMISSIONE");
            sql.addColumn("DT_TRASMISSIONE");
            sql.addColumn("DT_INCASSO");
        }

    }


    private void addSQLGroupBy(SQLBuilder sql, String valore, boolean addGroupBy) {
        if (addGroupBy)
            sql.addSQLGroupBy(valore);
    }

    private boolean isCdrEnte(UserContext userContext, CdrBulk cdr) throws ComponentException {
        try {
            getHome(userContext, cdr.getUnita_padre()).findByPrimaryKey(cdr.getUnita_padre());
            return cdr.isCdrAC();
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    private boolean isUtenteEnte(UserContext userContext) throws ComponentException {
        return isCdrEnte(userContext, cdrFromUserContext(userContext));
    }

    public CdrBulk cdrFromUserContext(UserContext userContext) throws ComponentException {

        try {
            it.cnr.contab.utenze00.bulk.UtenteBulk user = new it.cnr.contab.utenze00.bulk.UtenteBulk(userContext.getUser());
            user = (it.cnr.contab.utenze00.bulk.UtenteBulk) getHome(userContext, user).findByPrimaryKey(user);

            CdrBulk cdr = new CdrBulk(user.getCd_cdr());

            return (CdrBulk) getHome(userContext, cdr).findByPrimaryKey(cdr);
        } catch (it.cnr.jada.persistency.PersistencyException e) {
            throw new ComponentException(e);
        }
    }
    public RemoteIterator findFlussiCassa(UserContext uc, FlussiDiCassaDtoBulk flussi) throws  ComponentException, RemoteException{
        FlussiDiCassaDtoHome reversaliHome = (FlussiDiCassaDtoHome) getHome(uc, FlussiDiCassaDtoBulk.class);


        return null;
    }
}