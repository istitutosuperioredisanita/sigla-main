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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.docamm00.docs.bulk.TipoDocumentoEnum;
import it.cnr.contab.util.Utility;
import it.cnr.jada.*;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

public class Scrittura_analiticaHome extends BulkHome {
	public Scrittura_analiticaHome(java.sql.Connection conn) {
		super(Scrittura_analiticaBulk.class,conn);
	}
	public Scrittura_analiticaHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Scrittura_analiticaBulk.class,conn,persistentCache);
	}
	public Collection findMovimentiColl( UserContext userContext,Scrittura_analiticaBulk scrittura ) throws PersistencyException
	{
		SQLBuilder sql = getHomeCache().getHome( Movimento_coanBulk.class ).createSQLBuilder();
		sql.addClause( "AND", "esercizio", sql.EQUALS, scrittura.getEsercizio());
		sql.addClause( "AND", "cd_cds", sql.EQUALS, scrittura.getCd_cds());
		sql.addClause( "AND", "cd_unita_organizzativa", sql.EQUALS, scrittura.getCd_unita_organizzativa());
		sql.addClause( "AND", "pg_scrittura", sql.EQUALS, scrittura.getPg_scrittura());
		List result = getHomeCache().getHome( Movimento_coanBulk.class ).fetchAll( sql );
		getHomeCache().fetchAll(userContext);
		return result;
	}
		/**
		 * Imposta il pg_scrittura di un oggetto <code>Scrittura_analiticaBulk</code>.
		 *
		 * @param bulk <code>OggettoBulk</code>
		 *
		 * @exception PersistencyException
		 */

	public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext,OggettoBulk bulk) throws ComponentException
	{
		try
		{
			Scrittura_analiticaBulk scrittura = (Scrittura_analiticaBulk) bulk;

			LoggableStatement cs = new LoggableStatement(getConnection(),
				"{ ? = call " +
				it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
				"CNRCTB200.getNextProgressivo(?, ?, ?, ?, ?)}",false,this.getClass());
			try
			{
				cs.registerOutParameter( 1, java.sql.Types.NUMERIC );
				cs.setObject( 2, scrittura.getEsercizio() );
				cs.setString( 3, scrittura.getCd_cds() );
				cs.setString( 4, scrittura.getCd_unita_organizzativa() );
				cs.setString( 5, scrittura.TIPO_COAN );
				cs.setString( 6, scrittura.getUser());
				cs.executeQuery();

				Long result = new Long( cs.getLong( 1 ));
				scrittura.setPg_scrittura( result );
			}
			catch ( SQLException e )
			{
				throw new ComponentException( e );
			}
			finally
			{
				cs.close();
			}

		}
		catch ( java.lang.Exception e )
		{
			throw new ComponentException( e );
		}
	}

	public Optional<Scrittura_analiticaBulk> getScrittura(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk) throws ComponentException {
		return this.getScrittura(userContext, documentoCogeBulk, Boolean.TRUE);
	}

	public Optional<Scrittura_analiticaBulk> getScrittura(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk, boolean fetchAll) throws ComponentException {
		try {
			Optional<Scrittura_analiticaBulk> scritturaOpt = Optional.empty();
			if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(userContext)) {
				scritturaOpt = this.findByDocumentoAmministrativo(documentoCogeBulk);
				if (scritturaOpt.isPresent()) {
					Scrittura_analiticaBulk scrittura = scritturaOpt.get();
					scrittura.setMovimentiColl(new BulkList(this.findMovimentiColl( userContext, scrittura )));
				}
			}
			return scritturaOpt;
		} catch (PersistencyException | RemoteException e) {
			throw new ComponentException(e);
		}
	}

	public Optional<Scrittura_analiticaBulk> findByDocumentoAmministrativo(IDocumentoCogeBulk documentoCogeBulk) throws PersistencyException {
		return findByDocumentoCoge(documentoCogeBulk)
				.stream().min(Comparator.comparing(Scrittura_analiticaKey::getPg_scrittura));
	}

	public List<Scrittura_analiticaBulk> findByDocumentoCoge(IDocumentoCogeBulk documentoCogeBulk) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, documentoCogeBulk.getEsercizio());
		sql.addClause(FindClause.AND, "cd_cds_documento", SQLBuilder.EQUALS, documentoCogeBulk.getCd_cds());
		sql.addClause(FindClause.AND, "cd_uo_documento", SQLBuilder.EQUALS, documentoCogeBulk.getCd_uo());
		sql.addClause(FindClause.AND, "cd_tipo_documento", SQLBuilder.EQUALS, documentoCogeBulk.getCd_tipo_doc());

		if (documentoCogeBulk.getCd_tipo_doc().equals(TipoDocumentoEnum.LIQUIDAZIONE_IVA.getValue())) {
			sql.addClause(FindClause.AND, "dt_inizio_liquid", SQLBuilder.EQUALS, documentoCogeBulk.getDtInizioLiquid());
			sql.addClause(FindClause.AND, "dt_fine_liquid", SQLBuilder.EQUALS, documentoCogeBulk.getDtFineLiquid());
			sql.addClause(FindClause.AND, "tipo_liquidazione", SQLBuilder.EQUALS, documentoCogeBulk.getTipoLiquid());
			sql.addClause(FindClause.AND, "report_id_liquid", SQLBuilder.EQUALS, documentoCogeBulk.getReportIdLiquid());
		} else
			sql.addClause("AND", "pg_numero_documento", SQLBuilder.EQUALS, documentoCogeBulk.getPg_doc());
		return fetchAll(sql);
	}
}
