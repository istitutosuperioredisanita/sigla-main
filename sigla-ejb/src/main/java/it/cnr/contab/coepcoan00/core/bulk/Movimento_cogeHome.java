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

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk;
import it.cnr.contab.doccont00.comp.DateServices;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;
import org.springframework.data.util.Pair;

public class Movimento_cogeHome extends BulkHome {
	public Movimento_cogeHome(java.sql.Connection conn) {
		super(Movimento_cogeBulk.class,conn);
	}
	public Movimento_cogeHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Movimento_cogeBulk.class,conn,persistentCache);
	}

	public Movimento_cogeHome(Class class1, Connection conn, PersistentCache persistentCache) {
		super(class1, conn, persistentCache);
	}
	public Movimento_cogeHome(Class class1, Connection conn) {
		super(class1, conn);
	}

	/**
	 * Ritorna un SQLBuilder con la columnMap del ricevente
	 */
	public SQLBuilder createSQLBuilder()
	{
		SQLBuilder sql = super.createSQLBuilder();
		sql.addTableToHeader("SCRITTURA_PARTITA_DOPPIA");
		sql.addSQLJoin( "SCRITTURA_PARTITA_DOPPIA.ESERCIZIO","MOVIMENTO_COGE.ESERCIZIO");
		sql.addSQLJoin( "SCRITTURA_PARTITA_DOPPIA.CD_CDS", "MOVIMENTO_COGE.CD_CDS");
		sql.addSQLJoin( "SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA","MOVIMENTO_COGE.PG_SCRITTURA");
		sql.addSQLJoin( "SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA","MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA");

		return sql;
	}
	public SQLBuilder createSQLBuilderWithoutJoin()
	{
		return super.createSQLBuilder();
	}
	/**
	 * Imposta il pg_movimento di un oggetto <code>Movimento_cogeBulk</code>.
	 * @param userContext userContext
	 * @param bulk <code>Movimento_cogeBulk</code>
	 * @exception PersistencyException PersistencyException
	*/

	public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext,OggettoBulk bulk) throws PersistencyException
	{
		try
		{
			Movimento_cogeBulk mov = (Movimento_cogeBulk) bulk;
			Long x;

			LoggableStatement ps = new LoggableStatement(getConnection(),
				"SELECT PG_MOVIMENTO FROM " +
				it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
				"MOVIMENTO_COGE " +
				"WHERE ESERCIZIO = ? AND " +
				"CD_CDS = ? AND " +
				"CD_UNITA_ORGANIZZATIVA = ? AND " +
				"PG_SCRITTURA = ? AND " +
				"PG_MOVIMENTO = ( SELECT MAX(PG_MOVIMENTO) " +
				"FROM " +
				it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
				"MOVIMENTO_COGE " +
				"WHERE ESERCIZIO = ? AND " +
				"CD_CDS = ? AND " +
				"CD_UNITA_ORGANIZZATIVA = ? AND " +
				"PG_SCRITTURA = ? )" +
				"FOR UPDATE NOWAIT",true,this.getClass());
			try
			{
				ps.setObject( 1, mov.getEsercizio() );
				ps.setString( 2, mov.getCd_cds() );
				ps.setString( 3, mov.getCd_unita_organizzativa() );
				ps.setObject( 4, mov.getPg_scrittura() );
				ps.setObject( 5, mov.getEsercizio() );
				ps.setString( 6, mov.getCd_cds() );
				ps.setString( 7, mov.getCd_unita_organizzativa() );
				ps.setObject( 8, mov.getPg_scrittura() );

				ResultSet rs = ps.executeQuery();
				try
				{
					if(rs.next())
						x = rs.getLong(1) + 1;
					else
						x = 0L;
				}
				catch( SQLException e )
				{
					throw new PersistencyException( e );
				}
				finally
				{
					try{rs.close();}catch( java.sql.SQLException e ){}
				}
			}
			catch( SQLException e )
			{
				throw new PersistencyException( e );
			}
			finally
			{
				try{ps.close();}catch( java.sql.SQLException e ){}
			}

			mov.setPg_movimento( x );
		}
		catch ( SQLException e )
		{
				throw new PersistencyException( e );
		}

	}

	public List<Movimento_cogeBulk> getMovimentiPartita(IDocumentoCogeBulk docamm, Integer cdTerzo, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio_documento",SQLBuilder.EQUALS, docamm.getEsercizio() );
		sql.addClause(FindClause.AND,"cd_cds_documento",SQLBuilder.EQUALS, docamm.getCd_cds() );
		sql.addClause(FindClause.AND,"cd_uo_documento",SQLBuilder.EQUALS, docamm.getCd_uo() );
		sql.addClause(FindClause.AND,"pg_numero_documento",SQLBuilder.EQUALS, docamm.getPg_doc() );
		sql.addClause(FindClause.AND,"cd_tipo_documento",SQLBuilder.EQUALS, docamm.getCd_tipo_doc() );
		sql.addClause(FindClause.AND,"cd_terzo",SQLBuilder.EQUALS, cdTerzo );
		sql.addClause(FindClause.AND,"cd_contributo_ritenuta",SQLBuilder.ISNULL, null );
		List<Movimento_cogeBulk> allMovimentiCoge = this.fetchAll(sql);

		if (scritturaToExclude.isPresent())
			return allMovimentiCoge.stream().filter(el->!el.getScrittura().equalsByPrimaryKey(scritturaToExclude.get())).collect(Collectors.toList());
		return allMovimentiCoge;
	}

	public Map<String, Pair<String, BigDecimal>> getSaldiMovimentiPartita(IDocumentoCogeBulk docamm, Integer cdTerzo, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws ComponentException, PersistencyException {
		Map<String, Pair<String, BigDecimal>> result = new HashMap<>();

		Collection<Movimento_cogeBulk> allMovimentiCoge = this.getMovimentiPartita(docamm, cdTerzo, scritturaToExclude);

		Map<String, List<Movimento_cogeBulk>> mapVoceEp = allMovimentiCoge.stream()
				.filter(el->
						(docamm.getTipoDocumentoEnum().isDocumentoPassivo() && el.getTi_riga().equals(Movimento_cogeBulk.TipoRiga.DEBITO.value())) ||
						(docamm.getTipoDocumentoEnum().isDocumentoAttivo() && el.getTi_riga().equals(Movimento_cogeBulk.TipoRiga.CREDITO.value()))
				)
				.collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep));

		mapVoceEp.keySet().forEach(cdVoceEp->{
			List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
			BigDecimal totaleDare = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneDare)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal totaleAvere = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneAvere)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal saldo = totaleDare.subtract(totaleAvere);
			if (saldo.compareTo(BigDecimal.ZERO)>=0)
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
			else
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
		});
		return result;
	}

	public List<Movimento_cogeBulk> getMovimentiCori(IDocumentoAmministrativoBulk docamm, Integer cdTerzo, String cdCori, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND,"esercizio_documento",SQLBuilder.EQUALS, docamm.getEsercizio() );
		sql.addClause(FindClause.AND,"cd_cds_documento",SQLBuilder.EQUALS, docamm.getCd_cds() );
		sql.addClause(FindClause.AND,"cd_uo_documento",SQLBuilder.EQUALS, docamm.getCd_uo() );
		sql.addClause(FindClause.AND,"pg_numero_documento",SQLBuilder.EQUALS, docamm.getPg_doc() );
		sql.addClause(FindClause.AND,"cd_tipo_documento",SQLBuilder.EQUALS, docamm.getCd_tipo_doc() );
		sql.addClause(FindClause.AND,"cd_terzo",SQLBuilder.EQUALS, cdTerzo );
		sql.addClause(FindClause.AND,"cd_contributo_ritenuta",SQLBuilder.EQUALS, cdCori );

		List<Movimento_cogeBulk> allMovimentiCoge = this.fetchAll(sql);

		if (scritturaToExclude.isPresent())
			return allMovimentiCoge.stream().filter(el->!el.getScrittura().equalsByPrimaryKey(scritturaToExclude.get())).collect(Collectors.toList());
		return allMovimentiCoge;
	}

	public Map<String, Pair<String, BigDecimal>> getSaldiMovimentiCori(IDocumentoAmministrativoBulk docamm, Integer cdTerzo, String cdCori, Optional<Scrittura_partita_doppiaBulk> scritturaToExclude) throws ComponentException, PersistencyException {
		Map<String, Pair<String, BigDecimal>> result = new HashMap<>();

		Collection<Movimento_cogeBulk> allMovimentiCoge = this.getMovimentiCori(docamm, cdTerzo, cdCori, scritturaToExclude);

		Map<String, List<Movimento_cogeBulk>> mapVoceEp = allMovimentiCoge.stream().collect(Collectors.groupingBy(Movimento_cogeBulk::getCd_voce_ep));

		mapVoceEp.keySet().forEach(cdVoceEp->{
			List<Movimento_cogeBulk> movimentiList = mapVoceEp.get(cdVoceEp);
			BigDecimal totaleDare = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneDare)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal totaleAvere = movimentiList.stream()
					.filter(Movimento_cogeBulk::isSezioneAvere)
					.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal saldo = totaleDare.subtract(totaleAvere);
			if (saldo.compareTo(BigDecimal.ZERO)>=0)
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_DARE, saldo));
			else
				result.put(cdVoceEp, Pair.of(Movimento_cogeBulk.SEZIONE_AVERE, saldo.abs()));
		});
		return result;
	}
	/**
	 * @param userContext 			Contesto utente, da cui viene recuperato l'esercizio
	 * @param voceEP 				Conto di economica
	 * @param ti_istituz_commerc 	Istituzionale/Commerciale
	 * @return Pair composta dal totale dare e totale avere
	 */
	public Pair<BigDecimal, BigDecimal> getMovimentiConto(UserContext userContext, String voceEP, String ti_istituz_commerc) throws PersistencyException{
		SQLBuilder sql = this.createSQLBuilder();
		sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.ATTIVA", SQLBuilder.EQUALS, "Y");
		sql.addClause(FindClause.AND, "cd_voce_ep", SQLBuilder.EQUALS, voceEP);
		Optional.ofNullable(ti_istituz_commerc).ifPresent(s -> sql.addClause(FindClause.AND, "ti_istituz_commerc", SQLBuilder.EQUALS, s));

		// Recupero il dettaglio dell'UO per verificare se è UO Ente
		Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHomeCache()
				.getHome(Unita_organizzativaBulk.class)
				.findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));

		if (uo.getCd_tipo_unita().compareTo(Tipo_unita_organizzativaHome.TIPO_UO_ENTE) != 0) {
			sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, uo.getCd_unita_organizzativa());
		}

		sql.openParenthesis(FindClause.AND);
			sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.CD_CAUSALE_COGE", SQLBuilder.ISNULL, null);
			sql.openParenthesis(FindClause.OR);
				sql.addSQLClause(FindClause.OR, "SCRITTURA_PARTITA_DOPPIA.CD_CAUSALE_COGE", SQLBuilder.EQUALS, Scrittura_partita_doppiaBulk.Causale.CHIUSURA_STATO_PATRIMONIALE.name());
				sql.addSQLClause(FindClause.OR, "SCRITTURA_PARTITA_DOPPIA.CD_CAUSALE_COGE", SQLBuilder.EQUALS, Scrittura_partita_doppiaBulk.Causale.CHIUSURA_CONTO_ECONOMICO.name());
				sql.addSQLClause(FindClause.OR, "SCRITTURA_PARTITA_DOPPIA.CD_CAUSALE_COGE", SQLBuilder.EQUALS, Scrittura_partita_doppiaBulk.Causale.DETERMINAZIONE_UTILE_PERDITA.name());
			sql.closeParenthesis();
		sql.closeParenthesis();
		sql.openParenthesis(FindClause.AND);
			sql.openParenthesis(FindClause.OR);
				sql.addClause(FindClause.AND, "dt_da_competenza_coge", SQLBuilder.ISNULL, null);
				sql.addClause(FindClause.OR, "dt_a_competenza_coge", SQLBuilder.ISNULL, null);
			sql.closeParenthesis();
			/** ANNO IN CORSO (SOLA COMPETENZA) **/
			sql.openParenthesis(FindClause.OR);
				sql.addBetweenClause(FindClause.AND, "dt_da_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
				sql.addBetweenClause(FindClause.AND, "dt_a_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
			sql.closeParenthesis();
			/** A CAVALLO TRA ANNO PRECEDENTE ED ANNO ATTUALE **/
			sql.openParenthesis(FindClause.OR);
				sql.addBetweenClause(FindClause.AND, "dt_da_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext) - 1),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext) - 1));
				sql.addBetweenClause(FindClause.AND, "dt_a_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
			sql.closeParenthesis();
			/** A CAVALLO TRA ANNO ATTUALE ED ANNO SUCCESSIVO (TANTO CI SONO I RISCONTI) **/
			sql.openParenthesis(FindClause.OR);
				sql.addBetweenClause(FindClause.AND, "dt_da_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
				sql.addBetweenClause(FindClause.AND, "dt_a_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext) + 1),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext) + 1));
			sql.closeParenthesis();
			/** O COMPLETAMENTE IN ESERCIZIO SUCCESSIVO (TANTO CI SONO I RISCONTI) **/
			sql.openParenthesis(FindClause.OR);
				sql.addBetweenClause(FindClause.AND, "dt_da_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext) + 1),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext) + 1));
				sql.addBetweenClause(FindClause.AND, "dt_a_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext) + 1),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext) + 1));
			sql.closeParenthesis();
		sql.closeParenthesis();
		List<Movimento_cogeBulk> allMovimentiCoge = this.fetchAll(sql);
		BigDecimal totaleDare = allMovimentiCoge.stream()
				.filter(Movimento_cogeBulk::isSezioneDare)
				.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totaleAvere = allMovimentiCoge.stream()
				.filter(Movimento_cogeBulk::isSezioneAvere)
				.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);
		return Pair.of(totaleDare, totaleAvere);
	}
	/**
	 * @param userContext 			Contesto utente, da cui viene recuperato l'esercizio
	 * @param voceEP 				Conto di economica
	 * @param ti_istituz_commerc 	Istituzionale/Commerciale
	 * @return Pair composta dal totale dare e totale avere
	 */
	public Pair<BigDecimal, BigDecimal> getMovimentiContoAvanzo(UserContext userContext, String voceEP, String ti_istituz_commerc, String contoEconomico) throws PersistencyException{
		SQLBuilder sql = this.createSQLBuilder();
		sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.ATTIVA", SQLBuilder.EQUALS, "Y");
		sql.addClause(FindClause.AND, "cd_voce_ep", SQLBuilder.EQUALS, voceEP);
		Optional.ofNullable(ti_istituz_commerc).ifPresent(s -> sql.addClause(FindClause.AND, "ti_istituz_commerc", SQLBuilder.EQUALS, s));

		// Recupero il dettaglio dell'UO per verificare se è UO Ente
		Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHomeCache()
				.getHome(Unita_organizzativaBulk.class)
				.findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));

		if (uo.getCd_tipo_unita().compareTo(Tipo_unita_organizzativaHome.TIPO_UO_ENTE) != 0) {
			sql.addSQLClause(FindClause.AND, "SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, uo.getCd_unita_organizzativa());
		}
		sql.openParenthesis(FindClause.AND);
			sql.openParenthesis(FindClause.AND);
				sql.addClause(FindClause.AND, "dt_da_competenza_coge", SQLBuilder.ISNULL, null);
				sql.addBetweenClause(FindClause.OR, "dt_da_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
			sql.closeParenthesis();
			sql.openParenthesis(FindClause.OR);
				sql.addClause(FindClause.AND, "dt_da_competenza_coge", SQLBuilder.ISNULL, null);
				sql.addClause(FindClause.OR, "dt_a_competenza_coge", SQLBuilder.ISNULL, null);
			sql.closeParenthesis();
			sql.openParenthesis(FindClause.OR);
				sql.addClause(FindClause.AND, "dt_a_competenza_coge", SQLBuilder.ISNULL, null);
				sql.addBetweenClause(FindClause.OR, "dt_a_competenza_coge",
						DateServices.getFirstDayOfYear(CNRUserContext.getEsercizio(userContext)),
						DateServices.getLastDayOfYear(CNRUserContext.getEsercizio(userContext)));
			sql.closeParenthesis();
		sql.closeParenthesis();
		SQLBuilder sqlExsists = this.createSQLBuilderWithoutJoin();
		sqlExsists.setFromClause(new StringBuffer("MOVIMENTO_COGE M2"));
		sqlExsists.addSQLJoin("MOVIMENTO_COGE.CD_CDS", "M2.CD_CDS");
		sqlExsists.addSQLJoin("MOVIMENTO_COGE.ESERCIZIO", "M2.ESERCIZIO");
		sqlExsists.addSQLJoin("MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA", "M2.CD_UNITA_ORGANIZZATIVA");
		sqlExsists.addSQLJoin("MOVIMENTO_COGE.PG_SCRITTURA", "M2.PG_SCRITTURA");
		sqlExsists.addClause(FindClause.AND, "cd_voce_ep", SQLBuilder.EQUALS, contoEconomico);
		sql.addSQLExistsClause(FindClause.AND, sqlExsists);

		List<Movimento_cogeBulk> allMovimentiCoge = this.fetchAll(sql);
		BigDecimal totaleDare = allMovimentiCoge.stream()
				.filter(Movimento_cogeBulk::isSezioneDare)
				.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totaleAvere = allMovimentiCoge.stream()
				.filter(Movimento_cogeBulk::isSezioneAvere)
				.map(Movimento_cogeBulk::getIm_movimento).reduce(BigDecimal.ZERO, BigDecimal::add);
		return Pair.of(totaleDare, totaleAvere);
	}
}
