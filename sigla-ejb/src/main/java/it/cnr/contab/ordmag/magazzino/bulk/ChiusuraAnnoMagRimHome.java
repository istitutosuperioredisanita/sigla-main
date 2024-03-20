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
			" set IMPORTO_CMPP_ART=(select DECODE( sum( totGiacenza+totCarcIni),0,0,sum(impTotArtAnno+impTotArtAnnoPrec) /sum( totGiacenza+totCarcIni)) cmpp " +
			" from (" +
			" select cd_bene_servizio,SUM(GIACENZA*IMPORTO_UNITARIO_LOTTO) as impTotArtAnno, sum( Giacenza) totGiacenza, sum( a.carico_iniziale*IMPORTO_UNITARIO_CHIU) impTotArtAnnoPrec, " +
			" sum(a.carico_iniziale) totCarcIni from chiusura_anno_mag_rim a " +
			" where anno=? " +
			" group by cd_bene_servizio) x " +
			" where r.cd_bene_servizio=x.cd_bene_servizio " +
			" group by x.cd_bene_servizio) ";


	public ChiusuraAnnoMagRimHome(Connection conn) {
		super(ChiusuraAnnoMagRimBulk.class, conn);
	}

	public ChiusuraAnnoMagRimHome(Connection conn, PersistentCache persistentCache) {
		super(ChiusuraAnnoMagRimBulk.class, conn, persistentCache);
	}

	public List<ChiusuraAnnoMagRimBulk> getChiusuraAnnoMagRim(Integer pgChiusura, Integer anno, String tipoChiusura) throws PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND, "pg_chiusura", SQLBuilder.EQUALS, pgChiusura);
		sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, anno);
		sql.addClause(FindClause.AND, "tipo_chiusura", SQLBuilder.EQUALS, tipoChiusura);

		List lista = this.fetchAll(sql);
		return lista;
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