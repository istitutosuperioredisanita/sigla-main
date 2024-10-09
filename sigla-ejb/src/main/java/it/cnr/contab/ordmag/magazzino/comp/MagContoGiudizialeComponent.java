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
 * Created on Jun 23, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.ordmag.magazzino.comp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.ordmag.magazzino.bulk.V_dettaglio_lotti_magBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;

public class MagContoGiudizialeComponent extends it.cnr.jada.comp.CRUDComponent  {

	public RemoteIterator findMagContoGiudiziale(UserContext userContext, String columnMapName,CompoundFindClause baseClause, CompoundFindClause findClause) throws ComponentException, RemoteException, IntrospectionException {

		/*
		select cd_magazzino_mag,DS_MAGAZZINO,cd_bene_servizio,DS_BENE_SERVIZIO,cd_categoria_padre,cd_proprio,unita_misura,
    sum( quantita_apertura) qtaInizioAnno,
    sum ( quantita_carico ) quantita_carico ,
    sum( quantita_scarico ) quanti_scarico,
    sum ( quantita_attuale) quantita_attuale from V_DETTAGLIO_LOTTI_MAG l
    where l.cd_bene_servizio='275058'
    and esercizio<=2023
    and ( anno_riferimento_movimento is null or anno_riferimento_movimento=2023)
    group by cd_magazzino_mag,DS_MAGAZZINO,cd_bene_servizio,DS_BENE_SERVIZIO,cd_categoria_padre,cd_proprio,unita_misura
		 */
			SQLBuilder sql = getHome(userContext, V_dettaglio_lotti_magBulk.class,columnMapName).createSQLBuilder();
		sql.resetColumns();
		sql.addColumn("CD_MAGAZZINO_MAG");
		sql.addColumn("DS_MAGAZZINO");
		sql.addColumn("CD_BENE_SERVIZIO");
		sql.addColumn("DS_BENE_SERVIZIO");
		sql.addColumn("cd_categoria_padre");
		sql.addColumn("cd_proprio");
		sql.addColumn("unita_misura");
		sql.addColumn("SUM( quantita_apertura ) qtaInizioAnno " );
		sql.addColumn( "SUM(quantita_carico)  qtaCaricoAnno");
		sql.addColumn( "SUM(quantita_scarico)  qtaScaricoAnno");
		sql.addSQLClause(FindClause.AND,"ESERCIZIO<="+CNRUserContext.getEsercizio(userContext));
		sql.openParenthesis(FindClause.AND);
		sql.addSQLClause(FindClause.OR, "anno_riferimento_movimento",SQLBuilder.ISNULL,null);
		sql.addSQLClause(FindClause.OR, "anno_riferimento_movimento",SQLBuilder.EQUALS,CNRUserContext.getEsercizio(userContext));
		sql.closeParenthesis();
		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"cdMagazzinoMag",SQLBuilder.EQUALS, "MV");
		sql.addClause(FindClause.OR,"cdMagazzinoMag",SQLBuilder.EQUALS, "PC");
		sql.addClause(FindClause.OR,"cdMagazzinoMag",SQLBuilder.EQUALS, "GG");
		sql.addClause(FindClause.OR,"cdMagazzinoMag",SQLBuilder.EQUALS, "CS");
		sql.addClause(FindClause.OR,"cdMagazzinoMag",SQLBuilder.EQUALS, "PT");

		sql.closeParenthesis();
		sql.addSQLGroupBy("cd_magazzino_mag, " +
				"DS_MAGAZZINO,cd_bene_servizio," +
				"DS_BENE_SERVIZIO,cd_categoria_padre,cd_proprio,unita_misura");
		return iterator(userContext, completaSQL(sql,baseClause,findClause), V_dettaglio_lotti_magBulk.class,null);
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

	private boolean isCdsEnte(UserContext userContext) throws ComponentException {
		try {
			Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHome( userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
			if (((CNRUserContext) userContext).getCd_unita_organizzativa().equals( ente.getCd_unita_organizzativa()))
				return true;
			else
				return false;
		} catch(Throwable e) {
			throw handleException(e);
		}
	}

}
