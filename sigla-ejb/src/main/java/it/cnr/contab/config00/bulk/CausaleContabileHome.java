/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/12/2024
 */
package it.cnr.contab.config00.bulk;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammBase;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammBulk;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammKey;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.ICancellatoLogicamente;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.ejb.EJBCommonServices;

public class CausaleContabileHome extends BulkHome {
	public CausaleContabileHome(Connection conn) {
		super(CausaleContabileBulk.class, conn);
	}
	public CausaleContabileHome(Connection conn, PersistentCache persistentCache) {
		super(CausaleContabileBulk.class, conn, persistentCache);
	}

	public Hashtable loadTiDocumentoAmmKeys(CausaleContabileBulk causaleContabileBulk) throws PersistencyException {
		SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
		List<Tipo_documento_ammBulk> result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
		return new Hashtable(result
				.stream()
				.collect(Collectors.toMap(
                        Tipo_documento_ammBulk::getCd_tipo_documento_amm,
                        Tipo_documento_ammBulk::getDs_tipo_documento_amm, (key, value) -> value, HashMap::new)
				));

	}

	public List<CausaleContabileBulk> findCausaliStorno(UserContext userContext) throws PersistencyException {
		SQLBuilder sqlBuilder = super.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "flStorno", SQLBuilder.EQUALS, Boolean.TRUE);
		sqlBuilder.addClause(FindClause.AND, "dtInizioValidita", SQLBuilder.LESS_EQUALS, EJBCommonServices.getServerDate());
		sqlBuilder.openParenthesis(FindClause.AND);
		sqlBuilder.addClause(FindClause.AND, "dtFineValidita", SQLBuilder.GREATER_EQUALS, EJBCommonServices.getServerDate());
		sqlBuilder.addClause(FindClause.OR, "dtFineValidita", SQLBuilder.ISNULL, null);
		sqlBuilder.closeParenthesis();
		return fetchAll(sqlBuilder);
	}

	public List<AssCausaleVoceEPBulk> findAssCausaleVoceEPBulk(UserContext userContext, String cdCausale) throws PersistencyException {
		PersistentHome persistentHome = getHomeCache().getHome(AssCausaleVoceEPBulk.class);
		SQLBuilder sqlBuilder = persistentHome.createSQLBuilder();
		sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sqlBuilder.addClause(FindClause.AND, "cdCausale", SQLBuilder.EQUALS, cdCausale);
		List<AssCausaleVoceEPBulk> list = persistentHome.fetchAll(sqlBuilder);
		getHomeCache().fetchAll(userContext);
		return list;
	}

	@Override
	public void delete(Persistent persistent, UserContext userContext) throws PersistencyException {
		CausaleContabileBulk causalecontabilebulk = (CausaleContabileBulk)persistent;
		causalecontabilebulk.cancellaLogicamente();
		causalecontabilebulk.setToBeUpdated();
		super.update(persistent, userContext);
	}
}