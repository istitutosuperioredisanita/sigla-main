/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.cnr.contab.ordmag.magazzino.dto.ValoriChiusuraMagRim;
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.DateUtils;

public class ChiusuraAnnoMagRimHome extends BulkHome {
	private final String statmentUpdateCmpp = "" +
			" update chiusura_anno_mag_rim r" +
			" set IMPORTO_CMPP_ART=(select DECODE( sum( totCarichi+totCarcIni),0,0,sum(impTotArtAnno+impTotArtAnnoPrec) /sum( totCarichi+totCarcIni)) cmpp " +
			" from (" +
			" select cd_bene_servizio," +
			" SUM( " +
			"   case when esercizio_lotto= ? " +
			"        then CARICO_ANNO*IMPORTO_UNITARIO_LOTTO " +
			"        else 0 " +
			"  end) as impTotArtAnno, " +
			" SUM( "+
			"   case when esercizio_lotto= ? " +
			"       then CARICO_ANNO " +
			"       else  0 " +
			"  end)  totCarichi , " +
			" SUM( " +
			"    case when esercizio_lotto< ? " +
			"        then a.carico_iniziale*IMPORTO_UNITARIO_CHIU " +
			"        else 0" +
			"  end ) impTotArtAnnoPrec , " +
			" SUM( " +
			"   case when esercizio_lotto< ? " +
			"        then a.carico_iniziale " +
			"        else 0 " +
			"  end ) totCarcIni " +
			" from chiusura_anno_mag_rim a " +
			" where anno=? " +
			" group by cd_bene_servizio) x " +
			" where r.cd_bene_servizio=x.cd_bene_servizio " +
			" group by x.cd_bene_servizio) ";

	private final String statmentSearchChiusuraMagRim = "" +
			" SELECT c.GIACENZA, " +
			" c.CD_NUMERATORE_LOTTO, " +
			" c.PG_LOTTO, " +
			" c.TIPO_CHIUSURA, " +
			" c.CD_BENE_SERVIZIO, " +
			" c.CD_CDS_MAG, " +
			" c.IMPORTO_CMPP_ART, " +
			" c.CD_MAGAZZINO,  " +
			" c.ESERCIZIO_LOTTO, " +
			" c.CD_CDS_RAGGR_MAG, " +
			" c.CD_CATEGORIA_GRUPPO, " +
			" c.CD_CDS_LOTTO, " +
			" c.CD_MAGAZZINO_LOTTO,  " +
			" c.ANNO,  " +
			" c.CD_RAGGR_MAG,  " +
			" c.PG_CHIUSURA ,  " +
			" c.UNITA_MISURA, " +
			" l.CD_DIVISA, " +
			" l.CAMBIO, " +
			" l.CD_VOCE_IVA, " +
			" l.CD_TERZO, " +
			" m.CD_UNITA_OPERATIVA " +
			" FROM  " +
			" CHIUSURA_ANNO_MAG_RIM c " +
			"        inner join MAGAZZINO m on c.CD_MAGAZZINO=m.CD_MAGAZZINO and c.CD_CDS_MAG=m.CD_CDS " +
			"        inner join LOTTO_MAG l on c.CD_CDS_LOTTO = l.cd_cds and c.CD_MAGAZZINO_LOTTO=l.cd_magazzino " +
			"								and c.ESERCIZIO_LOTTO=l.esercizio and c.CD_NUMERATORE_LOTTO=l.cd_numeratore_mag " +
			"                               and c.PG_LOTTO=l.pg_lotto " +
			"  WHERE  " +
			"  ( c.PG_CHIUSURA = ? ) AND  " +
			"  ( c.ANNO = ? ) AND " +
			"  ( c.TIPO_CHIUSURA = ? ) ";


	public ChiusuraAnnoMagRimHome(Connection conn) {
		super(ChiusuraAnnoMagRimBulk.class, conn);
	}

	public ChiusuraAnnoMagRimHome(Connection conn, PersistentCache persistentCache) {
		super(ChiusuraAnnoMagRimBulk.class, conn, persistentCache);
	}

	public List<ValoriChiusuraMagRim> getChiusuraAnnoMagRim(Integer pgChiusura, Integer anno, String tipoChiusura) throws PersistencyException {
		/*SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND, "pgChiusura", SQLBuilder.EQUALS, pgChiusura);
		sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, anno);
		sql.addClause(FindClause.AND, "tipoChiusura", SQLBuilder.EQUALS, tipoChiusura);

		List lista = this.fetchAll(sql);
		return lista;*/
		List<ValoriChiusuraMagRim> list = null;
		try {
			String statement = statmentSearchChiusuraMagRim;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				ps.setInt(1, pgChiusura);
				ps.setInt(2, anno);
				ps.setString(3, tipoChiusura);

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()) {

						if (list == null) {
							list = new ArrayList<ValoriChiusuraMagRim>();
						}
						list.add(getChiusuraAnnoMagRim(rs));

					}
					return list;
				} catch (Exception e) {
					throw new PersistencyException(e);
				} finally {
					try {
						rs.close();
					} catch (java.sql.SQLException e) {
					}

				}

			} catch (SQLException e) {
				throw new PersistencyException(e);
			} finally {
				try {
					ps.close();

				} catch (java.sql.SQLException e) {
				}
			}
		} catch (SQLException e) {
			throw new PersistencyException(e);
		}
	}

	private ValoriChiusuraMagRim getChiusuraAnnoMagRim(ResultSet rs) throws SQLException {

		ValoriChiusuraMagRim chiusura = new ValoriChiusuraMagRim();

		chiusura.setGiacenza(rs.getBigDecimal(1));
		chiusura.setCdNumeratoreLotto(rs.getString(2));
		chiusura.setPgLotto(rs.getInt(3));
		chiusura.setTipoChiusura(rs.getString(4));
		chiusura.setCdBeneServizio(rs.getString(5));
		chiusura.setCdCdsMag(rs.getString(6));
		chiusura.setImportoCmppArt(rs.getBigDecimal(7));
		chiusura.setCdMagazzino(rs.getString(8));
		chiusura.setEsercizioLotto(rs.getInt(9));
		chiusura.setCdCdsRaggrMag(rs.getString(10));
		chiusura.setCdCategoriaGruppo(rs.getString(11));
		chiusura.setCdCdsLotto(rs.getString(12));
		chiusura.setCdMagazzinoLotto(rs.getString(13));
		chiusura.setAnno(rs.getInt(14));
		chiusura.setCdRaggrMag(rs.getString(15));
		chiusura.setPgChiusura(rs.getInt(16));
		chiusura.setUnitaMisura(rs.getString(17));
		chiusura.setCdDivisa(rs.getString(18));
		chiusura.setCambio(rs.getBigDecimal(19));
		chiusura.setCdVoceIva(rs.getString(20));
		chiusura.setCdTerzo(rs.getInt(21));
		chiusura.setCdUnitaOperativa(rs.getString(22));

		return chiusura;
	}




	public List<ChiusuraAnnoMagRimBulk> getCalcoliChiusuraAnno(UserContext uc, Integer esercizio, Date dataFine, Date dataInizio, String codRaggrMag, String catGruppo) {

		List<ChiusuraAnnoMagRimBulk> chiusure = null;
		try {

			SQLBuilder sql = this.createSQLBuilder();
			sql.addSQLClause(FindClause.AND, "CHIUSURA_ANNO_MAG_RIM.ESERCIZIO_LOTTO", SQLBuilder.EQUALS, esercizio);
			sql.addSQLClause(FindClause.AND, "CHIUSURA_ANNO_MAG_RIM.DT_CARICO_LOTTO", SQLBuilder.LESS_EQUALS, new Timestamp(dataFine.getTime()));
			sql.addSQLClause(FindClause.AND, "CHIUSURA_ANNO_MAG_RIM.DT_CARICO_LOTTO", SQLBuilder.GREATER_EQUALS, new Timestamp(dataInizio.getTime()));
			if (catGruppo != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)) {
				sql.addSQLClause(FindClause.AND, "CHIUSURA_ANNO_MAG_RIM.CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, catGruppo);
			}
			if (codRaggrMag != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)) {
				sql.addSQLClause(FindClause.AND, "CHIUSURA_ANNO_MAG_RIM.CD_RAGGR_MAG", SQLBuilder.EQUALS, codRaggrMag);
			}

			chiusure = fetchAll(sql);
			getHomeCache().fetchAll(uc);
		} catch (PersistencyException e) {
			e.printStackTrace();
		}
		return chiusure;
	}

	public void getCmppPerArticolo(UserContext uc, Integer esercizio) throws PersistencyException {

		LoggableStatement ps = null;
		ResultSet rs = null;
		try {
			String statement = statmentUpdateCmpp;

			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			ps.setInt(1, esercizio);
			ps.setInt(2, esercizio);
			ps.setInt(3, esercizio);
			ps.setInt(4, esercizio);
			ps.setInt(5, esercizio);
			rs = ps.executeQuery();

		} catch (SQLException e) {
			throw new PersistencyException(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (java.sql.SQLException e) {
			}

		}
	}
}