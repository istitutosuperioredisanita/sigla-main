package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroHome;
import it.cnr.contab.inventario01.ejb.NumerazioneTempDocTRComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.sql.Connection;
import java.util.Collection;

public class Doc_trasporto_rientroHome extends BulkHome {

	public Doc_trasporto_rientroHome(Connection conn) {
		super(Doc_trasporto_rientroBulk.class, conn);
	}

	public Doc_trasporto_rientroHome(Connection conn, PersistentCache persistentCache) {
		super(Doc_trasporto_rientroBulk.class, conn, persistentCache);
	}

	public Doc_trasporto_rientroHome(Class class1, Connection connection) {
		super(class1, connection);
	}

	public Doc_trasporto_rientroHome(Class class1, Connection connection, PersistentCache persistentcache) {
		super(class1, connection, persistentcache);
	}

	/**
	 * Trova i tipi di movimento disponibili per un documento di trasporto/rientro
	 */
	public Collection findTipoMovimenti(UserContext userContext, Doc_trasporto_rientroBulk docTR, Tipo_trasporto_rientroHome h,
										Tipo_trasporto_rientroBulk clause)
			throws PersistencyException, IntrospectionException {
		return h.findTipiPerDocumento(userContext,docTR.getTiDocumento());
	}

	/**
	 * Inizializza la chiave primaria per l'inserimento di un nuovo documento
	 */
	public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk)
			throws PersistencyException, ComponentException {
		try {
			Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
			if (docTR.getPgDocTrasportoRientro() == null) {
				docTR.setPgDocTrasportoRientro(generaNuovoProgressivo(userContext, docTR));
			}
		} catch (ApplicationException e) {
			throw new ComponentException(e);
		} catch (Throwable e) {
			throw new PersistencyException(e);
		}
	}

	/**
	 * Genera un nuovo progressivo per il documento
	 */
	private Long generaNuovoProgressivo(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docTR)
			throws Exception {
		Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome) getHomeCache()
				.getHome(Numeratore_doc_t_rBulk.class);

		if (!userContext.isTransactional()) {
			return numHome.getNextPg(userContext, docTR.getEsercizio(),
					docTR.getPgInventario(), docTR.getTiDocumento(), userContext.getUser());
		} else {
			NumerazioneTempDocTRComponentSession session = (NumerazioneTempDocTRComponentSession)
					EJBCommonServices.createEJB("CNRINVENTARIO01_EJB_NumerazioneTempDocTRComponentSession",
							NumerazioneTempDocTRComponentSession.class);
			return session.getNextTempPG(userContext, docTR);
		}
	}
	@Override
	public SQLBuilder selectByClause(CompoundFindClause compoundfindclause)
			throws PersistencyException {
		SQLBuilder sqlbuilder = super.selectByClause(compoundfindclause);
		sqlbuilder.addOrderBy("ESERCIZIO desc,PG_DOC_TRASPORTO_RIENTRO desc");

		return sqlbuilder;
	}


}