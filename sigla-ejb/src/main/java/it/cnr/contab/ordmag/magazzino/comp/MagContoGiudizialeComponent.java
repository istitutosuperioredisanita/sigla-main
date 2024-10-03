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
import it.cnr.contab.ordmag.anag00.RaggrMagazzinoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.LottoMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.MagContoGiudizialeBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.apache.commons.lang.StringUtils;

public class MagContoGiudizialeComponent extends it.cnr.jada.comp.CRUDComponent  {
	protected Query select(UserContext userContext, CompoundFindClause clauses, OggettoBulk bulk) throws ComponentException, it.cnr.jada.persistency.PersistencyException
	{
		SQLBuilder sql = getHome(userContext, MagContoGiudizialeBulk.class).createSQLBuilder();
		sql.resetColumns();
		sql.addColumn("LOTTO_MAG.CD_MAGAZZINO_MAG");
		sql.addColumn("MAGAZZINO.DS_MAGAZZINO");
		sql.addColumn("LOTTO_MAG.CD_BENE_SERVIZIO");
		sql.addColumn("BENE_SERVIZIO.DS_BENE_SERVIZIO");
		sql.addColumn("CATEGORIA_GRUPPO.cd_categoria_padre");
		sql.addColumn("CATEGORIA_GRUPPO.cd_proprio");
		sql.addColumn("BENE_SERVIZIO.unita_misura");
		sql.addColumn("SUM( CASE \n" +
				"    WHEN esercizio<" + CNRUserContext.getEsercizio(userContext)+"\n"+
				"        THEN quantita_carico \n" +
				"    ELSE \n" +
				"        0 \n" +
				"  END) qtaInizioAnno " );
		sql.addColumn( getQueryCaricoScarico( EnumMagazioneOper.CARICO,CNRUserContext.getEsercizio(userContext)).concat( " qtaCaricoAnno"));
		sql.addColumn( getQueryCaricoScarico( EnumMagazioneOper.SCARICO,CNRUserContext.getEsercizio(userContext)).concat( " qtaScaricoAnno"));
		sql.setAutoJoins(true);
		sql.generateJoin(LottoMagBulk.class, MagazzinoBulk.class, "magazzino", "MAGAZZINO");
		sql.generateJoin(LottoMagBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
		sql.generateJoin(Bene_servizioBulk.class, Categoria_gruppo_inventBulk.class, "categoria_gruppo", "CATEGORIA_GRUPPO");
		sql.generateJoin(MagazzinoBulk.class, RaggrMagazzinoBulk.class, "raggrMagazzinoRim", "RAGGR_MAGAZZINO_RIM");

		sql.addSQLClause(FindClause.AND,"RAGGR_MAGAZZINO_RIM.TIPO",SQLBuilder.EQUALS,RaggrMagazzinoBulk.TIPO_RIM);
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.esercizio",SQLBuilder.LESS_EQUALS,CNRUserContext.getEsercizio(userContext));
		sql.openParenthesis(FindClause.AND);
			sql.addClause(FindClause.OR,"cd_magazzino",SQLBuilder.EQUALS, "MV");
			sql.addClause(FindClause.OR,"cd_magazzino",SQLBuilder.EQUALS, "PC");
			sql.addClause(FindClause.OR,"cd_magazzino",SQLBuilder.EQUALS, "GG");
			sql.addClause(FindClause.OR,"cd_magazzino",SQLBuilder.EQUALS, "CS");
			sql.addClause(FindClause.OR,"cd_magazzino",SQLBuilder.EQUALS, "PT");
		sql.closeParenthesis();
		sql.addSQLGroupBy("LOTTO_MAG.cd_magazzino_mag, " +
					"MAGAZZINO.DS_MAGAZZINO,LOTTO_MAG.cd_bene_servizio," +
					"BENE_SERVIZIO.DS_BENE_SERVIZIO,CATEGORIA_GRUPPO.cd_categoria_padre,CATEGORIA_GRUPPO.cd_proprio,BENE_SERVIZIO.unita_misura");
		return sql;

	}

	private enum EnumMagazioneOper {
		CARICO,
		SCARICO
	}
	private String getFiltroTipoMov ( EnumMagazioneOper tipoOperazione){
		if ( EnumMagazioneOper.CARICO.equals(tipoOperazione))
			return  (" and tmc.tipo in ( 'CM','CA')  \n");
		if ( EnumMagazioneOper.SCARICO.equals(tipoOperazione))
			return (" and tmc.tipo in ( 'SM','SA')    \n");
		return StringUtils.EMPTY;
	}
	private String getQueryCaricoScarico(EnumMagazioneOper tipoOperazione,Integer esercizio ){
		StringBuffer sb = new StringBuffer( "SUM(( select sum(mc.coeff_conv*quantita) from movimenti_mag mc inner join tipo_movimento_mag tmc ");
		sb.append(" on mc.cd_tipo_movimento=tmc.cd_tipo_movimento  \n").
		append("  and cd_cds_tipo_movimento=tmc.cd_cds  \n");
		sb.append (getFiltroTipoMov( tipoOperazione));
		sb.append( " where mc.stato!='ANN' \n").
		append(" and mc.dt_riferimento<=to_date('31/12/").append(esercizio).append("','dd/mm/yyyy')  \n").
		append(" and mc.CD_CDS_LOTTO=LOTTO_MAG.CD_CDS  \n").
		append(" and mc.CD_MAGAZZINO_LOTTO=LOTTO_MAG.CD_MAGAZZINO  \n").
		append(" and mc.ESERCIZIO_LOTTO=LOTTO_MAG.ESERCIZIO  \n").
		append(" and mc.CD_NUMERATORE_LOTTO=LOTTO_MAG.CD_NUMERATORE_MAG  \n").
		append(" and mc.PG_LOTTO=LOTTO_MAG.PG_LOTTO  \n").
		append(" and NOT EXISTS  \n").
		append("( select msc.pg_movimento from movimenti_mag msc  \n").
		append("where msc.pg_movimento_rif=mc.pg_movimento  \n").
		append("and msc.cd_tipo_movimento=tmc.cd_tipo_movimento_rif))) ");
		return sb.toString();
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
