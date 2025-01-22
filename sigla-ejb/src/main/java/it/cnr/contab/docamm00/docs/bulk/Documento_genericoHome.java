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
import it.cnr.contab.config00.bulk.CausaleContabileHome;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.V_doc_passivo_obbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.V_doc_passivo_obbligazione_wizardBulk;
import it.cnr.contab.fondecon00.core.bulk.Fondo_spesaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Documento_genericoHome extends BulkHome implements
		IDocumentoAmministrativoSpesaHome {
	public Documento_genericoHome(Class aClass, java.sql.Connection conn) {
		super(aClass, conn);
	}

	public Documento_genericoHome(Class aClass, java.sql.Connection conn,
			PersistentCache persistentCache) {
		super(aClass, conn, persistentCache);
	}

	public Documento_genericoHome(java.sql.Connection conn) {
		super(Documento_genericoBulk.class, conn);
	}

	public Documento_genericoHome(java.sql.Connection conn,
			PersistentCache persistentCache) {
		super(Documento_genericoBulk.class, conn, persistentCache);
	}

	/**
	 * Inizializza la chiave primaria di un OggettoBulk per un inserimento. Da
	 * usare principalmente per riempire i progressivi automatici.
	 */
	public java.sql.Timestamp findForMaxDataRegistrazione(
			it.cnr.jada.UserContext userContext, Documento_genericoBulk doc)
			throws PersistencyException, it.cnr.jada.comp.ComponentException {

		if (doc == null)
			return null;
		try {
			java.sql.Connection contact = getConnection();
			String query = "SELECT MAX(DATA_REGISTRAZIONE) FROM "
					+ it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()
					+ "DOCUMENTO_GENERICO " + "WHERE CD_UO_ORIGINE= '"
					+ doc.getCd_uo_origine() + "' AND ESERCIZIO = "
					+ doc.getEsercizio().intValue();

			java.sql.ResultSet rs = contact.createStatement().executeQuery(
					query);

			if (rs.next())
				return rs.getTimestamp(1);
			else
				return null;
		} catch (java.sql.SQLException sqle) {
			throw new PersistencyException(sqle);
		}
	}

	public SQLBuilder selectValuta(Documento_genericoBulk documentoBulk,
			it.cnr.contab.docamm00.tabrif.bulk.DivisaHome divisaHome,
			it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk clause) {

		SQLBuilder sql = divisaHome.createSQLBuilder();

		sql.addTableToHeader("CAMBIO");
		sql.addSQLJoin("DIVISA.CD_DIVISA", "CAMBIO.CD_DIVISA");

		return sql;
	}

	/**
	 * Insert the method's description here. Creation date: (5/10/2002 3:27:22
	 * PM)
	 */
	public void updateFondoEconomale(Fondo_spesaBulk spesa)
			throws it.cnr.jada.persistency.PersistencyException,
			it.cnr.jada.bulk.OutdatedResourceException,
			it.cnr.jada.bulk.BusyResourceException {

		if (spesa == null)
			return;

		Documento_genericoBulk doc = (Documento_genericoBulk) spesa
				.getDocumento();

		lock(doc);

		StringBuffer stm = new StringBuffer("UPDATE  ");
		stm.append(it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema());
		stm.append(getColumnMap().getTableName());
		stm
				.append(" SET STATO_PAGAMENTO_FONDO_ECO = ?, DT_PAGAMENTO_FONDO_ECO = ?, PG_VER_REC = PG_VER_REC+1, DUVA = ?, UTUV = ?");
		stm.append(" WHERE (");
		stm
				.append("CD_CDS = ? AND CD_UNITA_ORGANIZZATIVA = ? AND ESERCIZIO = ? AND PG_DOCUMENTO_GENERICO = ? AND CD_TIPO_DOCUMENTO_AMM = ?)");

		try {
			LoggableStatement loggablestatement = new LoggableStatement(
					getConnection(), stm.toString(), true, this.getClass());
			try {
				loggablestatement.setString(1, (spesa.isToBeCreated() || spesa
						.isToBeUpdated()) ? doc.REGISTRATO_IN_FONDO_ECO
						: doc.FONDO_ECO);
				if (spesa.isToBeCreated() || spesa.isToBeUpdated())
					loggablestatement.setTimestamp(2, spesa.getDt_spesa());
				else
					loggablestatement.setNull(2, java.sql.Types.TIMESTAMP);

				loggablestatement.setTimestamp(3, getServerTimestamp());
				loggablestatement.setString(4, spesa.getUser());

				loggablestatement.setString(5, doc.getCd_cds());
				loggablestatement.setString(6, doc.getCd_unita_organizzativa());
				loggablestatement.setInt(7, doc.getEsercizio().intValue());
				loggablestatement.setLong(8, doc.getPg_documento_generico()
						.longValue());
				loggablestatement.setString(9, doc.getCd_tipo_doc_amm());

				loggablestatement.executeUpdate();
			} finally {
				try {
					loggablestatement.close();
				} catch (java.sql.SQLException e) {
				}
				;
			}
		} catch (java.sql.SQLException e) {
			throw it.cnr.jada.persistency.sql.SQLExceptionHandler.getInstance()
					.handleSQLException(e, spesa);
		}
	}

	public java.util.List findDocumentoGenericoRigheList(Documento_genericoBulk generico ) throws PersistencyException
	{
		PersistentHome home = getHomeCache().getHome(Documento_generico_rigaBulk.class);
		it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "pg_documento_generico", SQLBuilder.EQUALS, generico.getPg_documento_generico());
		sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, generico.getCd_cds());
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, generico.getEsercizio());
		sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, generico.getCd_unita_organizzativa());
		sql.addClause(FindClause.AND, "cd_tipo_documento_amm", SQLBuilder.EQUALS, generico.getCd_tipo_documento_amm());
		List<Documento_generico_rigaBulk> result = home.fetchAll(sql);
		for (Documento_generico_rigaBulk riga : result) {
			SQLBuilder sqlStorno = home.createSQLBuilder();
			sqlStorno.addClause(FindClause.AND, "documento_generico_riga_storno", SQLBuilder.EQUALS, riga);
			sqlStorno.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, Documento_generico_rigaBulk.STATO_ANNULLATO);
			List<Documento_generico_rigaBulk> storni = home.fetchAll(sqlStorno);
			riga.setRigaStornata(!storni.isEmpty());
			riga.setImportoStornato(
					BigDecimal.valueOf(storni
							.stream()
							.collect(Collectors.summarizingDouble(value -> value.getIm_riga().doubleValue())).getSum())
			);
		}
		return result;
	}

	public java.util.List<Documento_generico_rigaBulk> findDocumentoGenericoRigheList(V_doc_passivo_obbligazioneBulk docPassivo ) throws PersistencyException {
		if (TipoDocumentoEnum.valueOf(docPassivo.getCd_tipo_documento_amm()).isDocumentoGenericoPassivo()) {
			PersistentHome home = getHomeCache().getHome(Documento_generico_rigaBulk.class);
			it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
			sql.addClause(FindClause.AND, "pg_documento_generico", SQLBuilder.EQUALS, docPassivo.getPg_documento_amm());
			sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docPassivo.getCd_cds());
			sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docPassivo.getEsercizio());
			sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docPassivo.getCd_unita_organizzativa());
			sql.addClause(FindClause.AND, "cd_tipo_documento_amm", SQLBuilder.EQUALS, docPassivo.getCd_tipo_documento_amm());
			sql.addClause(FindClause.AND, "cd_cds_obbligazione", SQLBuilder.EQUALS, docPassivo.getCd_cds_obbligazione());
			sql.addClause(FindClause.AND, "esercizio_obbligazione", SQLBuilder.EQUALS, docPassivo.getEsercizio_obbligazione());
			sql.addClause(FindClause.AND, "esercizio_ori_obbligazione", SQLBuilder.EQUALS, docPassivo.getEsercizio_ori_obbligazione());
			sql.addClause(FindClause.AND, "pg_obbligazione", SQLBuilder.EQUALS, docPassivo.getPg_obbligazione());
			sql.addClause(FindClause.AND, "pg_obbligazione_scadenzario()", SQLBuilder.EQUALS, docPassivo.getPg_obbligazione_scadenzario());

			return home.fetchAll(sql);
		}
		return Collections.EMPTY_LIST;
	}

	public Hashtable loadTiCausaleContabileKeys(Documento_genericoBulk documentoGenericoBulk) throws PersistencyException {
		SQLBuilder sql = getHomeCache().getHome(CausaleContabileBulk.class).createSQLBuilder();
		if (documentoGenericoBulk.getBpStatus() != FormController.SEARCH) {
			sql.addClause(
					FindClause.AND,
					"cdTipoDocumentoAmm",
					Optional.ofNullable(documentoGenericoBulk.getTipo_documento())
							.flatMap(tipoDocumentoAmmBulk -> Optional.ofNullable(tipoDocumentoAmmBulk.getCd_tipo_documento_amm()))
							.map(t -> SQLBuilder.EQUALS).orElse(SQLBuilder.ISNULL),
					Optional.ofNullable(documentoGenericoBulk.getTipo_documento())
							.flatMap(tipoDocumentoAmmBulk -> Optional.ofNullable(tipoDocumentoAmmBulk.getCd_tipo_documento_amm()))
							.orElse(null)
			);
		}
		sql.addClause(FindClause.AND, "flStorno", SQLBuilder.EQUALS, documentoGenericoBulk.getFl_storno());
		sql.addClause(FindClause.AND, "dtInizioValidita", SQLBuilder.LESS_EQUALS, EJBCommonServices.getServerDate());
		sql.openParenthesis(FindClause.AND);
			sql.addClause(FindClause.AND, "dtFineValidita", SQLBuilder.GREATER_EQUALS, EJBCommonServices.getServerDate());
			sql.addClause(FindClause.OR, "dtFineValidita", SQLBuilder.ISNULL, null);
		sql.closeParenthesis();

		List<CausaleContabileBulk> result = getHomeCache().getHome(CausaleContabileBulk.class).fetchAll(sql);
		return new Hashtable(result
				.stream()
				.collect(Collectors.toMap(
						entry -> entry.getCdCausale(),
						entry -> entry.getDsCausale(),
						(key, value) -> value, HashMap::new)
				));

	}

	public SQLBuilder selectCausaleContabileByClause(
			UserContext userContext,
			Documento_genericoBulk documentoGenericoBulk,
			CausaleContabileHome causaleContabileHome,
			CausaleContabileBulk causaleContabileBulk,
			CompoundFindClause compoundFindClause) {
		SQLBuilder sql = causaleContabileHome.createSQLBuilder();
		Optional.ofNullable(compoundFindClause)
				.ifPresent(sql::addClause);
		if (documentoGenericoBulk.getBpStatus() != FormController.SEARCH) {
			sql.addClause(
					FindClause.AND,
					"cdTipoDocumentoAmm",
					Optional.ofNullable(documentoGenericoBulk.getTipo_documento())
							.flatMap(tipoDocumentoAmmBulk -> Optional.ofNullable(tipoDocumentoAmmBulk.getCd_tipo_documento_amm()))
							.map(t -> SQLBuilder.EQUALS).orElse(SQLBuilder.ISNULL),
					Optional.ofNullable(documentoGenericoBulk.getTipo_documento())
							.flatMap(tipoDocumentoAmmBulk -> Optional.ofNullable(tipoDocumentoAmmBulk.getCd_tipo_documento_amm()))
							.orElse(null)
			);
		}
		sql.addClause(FindClause.AND, "flStorno", SQLBuilder.EQUALS, documentoGenericoBulk.getFl_storno());
		sql.addClause(FindClause.AND, "dtInizioValidita", SQLBuilder.LESS_EQUALS, EJBCommonServices.getServerDate());
		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.AND, "dtFineValidita", SQLBuilder.GREATER_EQUALS, EJBCommonServices.getServerDate());
		sql.addClause(FindClause.OR, "dtFineValidita", SQLBuilder.ISNULL, null);
		sql.closeParenthesis();
		return sql;
	}
}
