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

/*
 * Created by Aurelio's BulkGenerator 1.0
 * Date 07/05/2007
 */
package it.cnr.contab.config00.pdcfin.bulk;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;

import it.cnr.contab.config00.bulk.Codici_siopeBulk;
import it.cnr.contab.config00.bulk.Codici_siopeHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Ass_ev_siopeHome extends BulkHome {
	public Ass_ev_siopeHome(Connection conn) {
		super(Ass_ev_siopeBulk.class, conn);
	}
	public Ass_ev_siopeHome(Connection conn, PersistentCache persistentCache) {
		super(Ass_ev_siopeBulk.class, conn, persistentCache);
	}
	
	public SQLBuilder selectElemento_voceByClause( Ass_ev_siopeBulk bulk, Elemento_voceHome home,it.cnr.jada.bulk.OggettoBulk bulkClause,CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException,IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio() );		
		sql.addClause("AND", "ti_appartenenza", SQLBuilder.EQUALS,bulk.getTi_appartenenza());
		sql.addClause("AND", "ti_gestione", SQLBuilder.EQUALS, bulk.getTi_gestione());

		sql.addTableToHeader("PARAMETRI_CNR");
		sql.addSQLJoin("PARAMETRI_CNR.ESERCIZIO","ELEMENTO_VOCE.ESERCIZIO");

		sql.openParenthesis(FindClause.AND);
		sql.addSQLClause(FindClause.OR,"PARAMETRI_CNR.FL_NUOVO_PDG='Y'");
		sql.addClause(FindClause.OR, "ti_elemento_voce", SQLBuilder.EQUALS, Elemento_voceHome.TIPO_CAPITOLO);
		sql.closeParenthesis();
		
		sql.addClause( clause );
		return sql;
	}
	
	public SQLBuilder selectCodici_siopeByClause( Ass_ev_siopeBulk bulk, Codici_siopeHome home,it.cnr.jada.bulk.OggettoBulk bulkClause,CompoundFindClause clause) throws java.lang.reflect.InvocationTargetException,IllegalAccessException, it.cnr.jada.persistency.PersistencyException {
		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause("AND", "ti_gestione", SQLBuilder.EQUALS, bulk.getTi_gestione());
		sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, bulk.getEsercizio() );	
		sql.addClause( clause );
		return sql;
	}
	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
	    sql.addClause("AND","esercizio",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
		return sql;
	}

	public Codici_siopeBulk findByElementoVoce(UserContext userContext, Elemento_voceBulk elementoVoceBulk) throws PersistencyException, ApplicationException {
		SQLBuilder sql = createSQLBuilder();
		sql.addClause(FindClause.AND, "elemento_voce", SQLBuilder.EQUALS, elementoVoceBulk);
		final List<Ass_ev_siopeBulk> result = fetchAll(sql);
		if (result.size() == 0)
			throw new ApplicationMessageFormatException("Non esistono codici SIOPE associati alla voce {0}", elementoVoceBulk.getCd_elemento_voce());
		else if (result.size() > 1)
			throw new ApplicationMessageFormatException("Esisto più codici SIOPE associati alla voce {0}", elementoVoceBulk.getCd_elemento_voce());
		return result.get(0).getCodici_siope();
	}
	
}