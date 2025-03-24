package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.sql.Connection;
import java.util.*;

public class V_cons_reg_ivaHome extends BulkHome {

	public V_cons_reg_ivaHome(Connection conn) {
		super(V_cons_reg_ivaBulk.class, conn);
	}

	public V_cons_reg_ivaHome(Connection conn, PersistentCache persistentCache) {
		super(V_cons_reg_ivaBulk.class, conn, persistentCache);
	}

	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		try {
			SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);

			// Aggiungo la condizione per l'esercizio
			sql.addSQLClause("AND", "V_CONS_REG_IVA.ESERCIZIO", SQLBuilder.EQUALS,
					CNRUserContext.getEsercizio(usercontext));

			// Recupero l'unità organizzativa dal contesto
			String unitaOrganizzativa = CNRUserContext.getCd_unita_organizzativa(usercontext);

			// Recupero il dettaglio dell'UO per verificare se è UO Ente
			Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHomeCache()
					.getHome(Unita_organizzativaBulk.class)
					.findByPrimaryKey(new Unita_organizzativaBulk(unitaOrganizzativa));

			boolean isUoEnte = uo.getCd_tipo_unita().compareTo(Tipo_unita_organizzativaHome.TIPO_UO_ENTE) == 0;

			if (!isUoEnte) {
				sql.openParenthesis("AND");

				// Condizione per documenti attivi
				sql.openParenthesis("");
				sql.addSQLClause("AND", "V_CONS_REG_IVA.TIPO_DOCUMENTO", SQLBuilder.EQUALS, "Attivo");
				sql.addSQLClause("AND", "V_CONS_REG_IVA.CD_UO_ORIGINE", SQLBuilder.EQUALS, unitaOrganizzativa);
				sql.closeParenthesis();

				// Condizione per documenti passivi
				sql.addSQLClause("OR", "V_CONS_REG_IVA.TIPO_DOCUMENTO", SQLBuilder.EQUALS, "Passivo");
				sql.addSQLClause("AND", "V_CONS_REG_IVA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, unitaOrganizzativa);

				sql.closeParenthesis();
			}

			return sql;
		} catch (Throwable t) {
			throw new PersistencyException(t);
		}
	}
}