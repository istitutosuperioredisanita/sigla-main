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

package it.cnr.contab.fondecon00.core.bulk;


import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.config00.pdcep.bulk.Ass_ev_voceepBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.doccont00.core.bulk.Ass_partita_giroBulk;
import it.cnr.contab.doccont00.core.bulk.Numerazione_doc_contBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.ReversaleBulk;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.beans.*;
import it.cnr.jada.persistency.sql.*;

import java.util.List;
import java.util.Optional;

public class Fondo_economaleHome extends BulkHome {
	public Fondo_economaleHome(Class aClass, java.sql.Connection conn) {
		super(aClass,conn);
	}
	public Fondo_economaleHome(Class aClass, java.sql.Connection conn,PersistentCache persistentCache) {
		super(aClass, conn, persistentCache);
	}
	public Fondo_economaleHome(java.sql.Connection conn) {
		super(Fondo_economaleBulk.class,conn);
	}
	public Fondo_economaleHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Fondo_economaleBulk.class,conn,persistentCache);
	}

	private void addClausesObbScad(it.cnr.jada.UserContext context, SQLBuilder sql, it.cnr.contab.fondecon00.core.bulk.Filtro_ricerca_obbligazioniVBulk filtro) throws it.cnr.jada.comp.ComponentException {
		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context));
		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.DT_CANCELLAZIONE", SQLBuilder.ISNULL, null);

		if (ObbligazioneBulk.TIPO_COMPETENZA.equals(filtro.getTipo_obbligazione()))
			sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT", SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB);
		else if (ObbligazioneBulk.TIPO_RESIDUO_PROPRIO.equals(filtro.getTipo_obbligazione()))
			sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT", SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB_RES);
		else if (ObbligazioneBulk.TIPO_RESIDUO_IMPROPRIO.equals(filtro.getTipo_obbligazione()))
			sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT",	SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB_RES_IMPROPRIA);

		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA", SQLBuilder.NOT_EQUALS, new java.math.BigDecimal(0));
		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_CONTABILE IS NULL OR OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_CONTABILE = 0");
		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.RIPORTATO", SQLBuilder.EQUALS, "N");

		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.CD_UNITA_ORGANIZZATIVA",SQLBuilder.EQUALS, filtro.getCd_unita_organizzativa());
		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.STATO_OBBLIGAZIONE",SQLBuilder.EQUALS, it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk.STATO_OBB_DEFINITIVO);

		if (filtro.getFl_fornitore().booleanValue())
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.CD_TERZO",SQLBuilder.EQUALS, filtro.getFornitore().getCd_terzo());
		else {
			sql.addTableToHeader("TERZO");
			sql.addTableToHeader("ANAGRAFICO");
			sql.addSQLJoin("OBBLIGAZIONE.CD_TERZO", "TERZO.CD_TERZO");
			sql.addSQLJoin("TERZO.CD_ANAG", "ANAGRAFICO.CD_ANAG");
			sql.addSQLClause(FindClause.AND,"ANAGRAFICO.TI_ENTITA",SQLBuilder.EQUALS, it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk.DIVERSI);
		}

		if (filtro.getFl_data_scadenziario().booleanValue())
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.DT_SCADENZA",SQLBuilder.EQUALS, filtro.getData_scadenziario());

		if (filtro.getFl_importo().booleanValue())
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA",SQLBuilder.EQUALS, filtro.getIm_importo());

		if (filtro.getFl_nr_obbligazione().booleanValue() && filtro.getEsercizio_ori_obbligazione()!=null)
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.ESERCIZIO_ORIGINALE",SQLBuilder.EQUALS, filtro.getEsercizio_ori_obbligazione());

		if (filtro.getFl_nr_obbligazione().booleanValue()) {
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE.PG_OBBLIGAZIONE",SQLBuilder.EQUALS, filtro.getNr_obbligazione());
			sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO",SQLBuilder.EQUALS, filtro.getNr_scadenza());
		}
	}

	public SQLQuery cercaObb_scad(it.cnr.jada.UserContext context, it.cnr.contab.fondecon00.core.bulk.Filtro_ricerca_obbligazioniVBulk filtro) throws it.cnr.jada.comp.ComponentException {
		SQLBuilder sql1 = cercaObb_scad_Associate(context, filtro);
		if (Optional.ofNullable(filtro.getFl_associate()).orElse(Boolean.FALSE)) {
			sql1.setOrderBy("obbligazione.esercizio_originale", it.cnr.jada.util.Orderable.ORDER_ASC);
			sql1.setOrderBy("obbligazione.pg_obbligazione", it.cnr.jada.util.Orderable.ORDER_ASC);
			return sql1;
		}
		SQLUnion union = sql1.union(cercaObb_scad_Libere(context, filtro), true);
		union.setOrderBy("obbligazione.esercizio_originale", it.cnr.jada.util.Orderable.ORDER_ASC);
		union.setOrderBy("obbligazione.pg_obbligazione", it.cnr.jada.util.Orderable.ORDER_ASC);
		return union;
	}

	private SQLBuilder cercaObb_scad_Associate(it.cnr.jada.UserContext context, it.cnr.contab.fondecon00.core.bulk.Filtro_ricerca_obbligazioniVBulk filtro) throws it.cnr.jada.comp.ComponentException {
		SQLBuilder sql = getHomeCache().getHome(it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk.class).createSQLBuilder();

		sql.setDistinctClause(true);
		sql.addTableToHeader("OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.CD_CDS", "OBBLIGAZIONE.CD_CDS");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", "OBBLIGAZIONE.ESERCIZIO");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE", "OBBLIGAZIONE.ESERCIZIO_ORIGINALE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE", "OBBLIGAZIONE.PG_OBBLIGAZIONE");

		sql.addTableToHeader("FONDO_SPESA");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.CD_CDS", "FONDO_SPESA.CD_CDS_OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", "FONDO_SPESA.ESERCIZIO_OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE", "FONDO_SPESA.ESERCIZIO_ORI_OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE", "FONDO_SPESA.PG_OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO", "FONDO_SPESA.PG_OBBLIGAZIONE_SCADENZARIO");

		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM IS NOT NULL AND OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM > 0");

		Fondo_economaleBulk fondo = filtro.getFondo();
		if (fondo != null) {
			sql.addSQLClause(FindClause.AND,"FONDO_SPESA.CD_CDS", SQLBuilder.EQUALS, fondo.getCd_cds());
			sql.addSQLClause(FindClause.AND,"FONDO_SPESA.ESERCIZIO", SQLBuilder.EQUALS, fondo.getEsercizio());
			sql.addSQLClause(FindClause.AND,"FONDO_SPESA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fondo.getCd_unita_organizzativa());
			sql.addSQLClause(FindClause.AND,"FONDO_SPESA.CD_CODICE_FONDO", SQLBuilder.EQUALS, fondo.getCd_codice_fondo());
		}

		sql.addSQLClause(FindClause.AND,"FONDO_SPESA.FL_DOCUMENTATA", SQLBuilder.EQUALS, "N");
		sql.addSQLClause(FindClause.AND,"FONDO_SPESA.FL_REINTEGRATA", SQLBuilder.EQUALS, "N");

		addClausesObbScad(context, sql, filtro);

		return sql;
	}

	private SQLBuilder cercaObb_scad_Libere(it.cnr.jada.UserContext context, it.cnr.contab.fondecon00.core.bulk.Filtro_ricerca_obbligazioniVBulk filtro) throws it.cnr.jada.comp.ComponentException {
		SQLBuilder sql = getHomeCache().getHome(it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk.class).createSQLBuilder();

		sql.addTableToHeader("OBBLIGAZIONE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.CD_CDS", "OBBLIGAZIONE.CD_CDS");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", "OBBLIGAZIONE.ESERCIZIO");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE", "OBBLIGAZIONE.ESERCIZIO_ORIGINALE");
		sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE", "OBBLIGAZIONE.PG_OBBLIGAZIONE");

		sql.addSQLClause(FindClause.AND,"OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM = 0 OR OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM IS NULL");

		addClausesObbScad(context, sql, filtro);

		return sql;
	}

	public boolean verificaStatoEsercizio(Fondo_economaleBulk fondo) throws PersistencyException, IntrospectionException {

		EsercizioBulk esercizio = (EsercizioBulk) getHomeCache().getHome(EsercizioBulk.class).findByPrimaryKey(
			new EsercizioBulk( fondo.getCd_cds(), fondo.getEsercizio()));
		if (esercizio == null || !esercizio.STATO_APERTO.equalsIgnoreCase(esercizio.getSt_apertura_chiusura()))
			return false;
		return true;
	}

	public Fondo_economaleBulk findFondoByReversale(ReversaleBulk reversale) throws ApplicationException, PersistencyException {
		it.cnr.jada.persistency.sql.SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, reversale.getCd_cds());
		sql.addSQLClause(FindClause.AND, "ESERCIZIO_REVERSALE", SQLBuilder.EQUALS, reversale.getEsercizio());
		sql.addSQLClause(FindClause.AND, "PG_REVERSALE", SQLBuilder.EQUALS, reversale.getPg_reversale());
		List<Fondo_economaleBulk> result = fetchAll(sql);
		if (result.size() > 1)
			throw new ApplicationException("Esiste più di un fondo economale che risulta chiuso dalla reversale "+
					reversale.getCd_cds()+"/"+reversale.getEsercizio()+"/"+reversale.getPg_reversale()+".");
		return result.stream().findAny().orElse(null);
	}
}
