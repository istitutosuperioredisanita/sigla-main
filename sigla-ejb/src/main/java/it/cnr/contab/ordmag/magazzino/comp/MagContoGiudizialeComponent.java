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
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.LottoMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.MagContoGiudizialeBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class MagContoGiudizialeComponent extends it.cnr.jada.comp.CRUDComponent  {
	protected Query select(UserContext userContext, CompoundFindClause clauses, OggettoBulk bulk) throws ComponentException, it.cnr.jada.persistency.PersistencyException
	{
		SQLBuilder sql = getHome(userContext, MagContoGiudizialeBulk.class).createSQLBuilder();
		//sql.resetColumns();
		sql.addColumn("1 qtaInizioAnno " );
		//sql.addColumn("BENE_SERVIZIO.cd_bene_servizio as cd_elemento_voce ");
		//sql.addColumn("CATEGORIA_GRUPPO.cd_categoria_padre");
		//sql.addColumn("CATEGORIA_GRUPPO.cd_categoria_padre");
		sql.addClause("AND","esercizio",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.setAutoJoins(true);
		sql.generateJoin(LottoMagBulk.class, MagazzinoBulk.class, "magazzino", "MAGAZZINO");
		sql.generateJoin(LottoMagBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
		sql.generateJoin(Bene_servizioBulk.class, Categoria_gruppo_inventBulk.class, "categoria_gruppo", "CATEGORIA_GRUPPO");
		sql.addSQLClause("AND","magazzino.cd_magazzino",SQLBuilder.EQUALS,"PC");
		//sql.addSQLGroupBy("LOTTO_MAG.cd_magazzino_mag, beneServizio.cd_bene_servizio,categoria_gruppo.cd_categoria_padre,categoria_gruppo.cd_proprio,beneServizio.unita_misura");
		return sql;

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
