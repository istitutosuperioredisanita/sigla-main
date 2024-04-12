/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/03/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.cnr.contab.ordmag.magazzino.dto.ValoriChiusuraCatGrVoceEP;
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.util.DateUtils;

public class ChiusuraAnnoCatGrpVoceEpHome extends BulkHome {

	private final String statmentImportoTotCatGruppoVoceEP =
			"  select ass.CD_CATEGORIA_GRUPPO,ass.cd_voce_ep,ass.ESERCIZIO,cg.pg_chiusura,cg.tipo_chiusura,cg.importoTotCatGrp " +
			"  from ASS_CATGRP_INVENT_VOCE_EP ass," +
			"       (select CD_CATEGORIA_GRUPPO ,SUM(GIACENZA*IMPORTO_CMPP_ART) as importoTotCatGrp, pg_chiusura, tipo_chiusura, anno " +
					"  from chiusura_anno_mag_rim a " +
			"		   where anno = ? and tipo_chiusura = ? " +
			"          group by CD_CATEGORIA_GRUPPO,pg_chiusura, tipo_chiusura, anno) cg " +
			" where ass.ESERCIZIO = ? and cg.importoTotCatGrp > 0 and ass.fl_default = 'Y' and " +
			" cg.CD_CATEGORIA_GRUPPO = ass.CD_CATEGORIA_GRUPPO " +
			" group by  ass.CD_CATEGORIA_GRUPPO,ass.cd_voce_ep,ass.ESERCIZIO,cg.pg_chiusura,cg.tipo_chiusura,cg.importoTotCatGrp " +
			" order by  ass.CD_CATEGORIA_GRUPPO,ass.cd_voce_ep ";





	public ChiusuraAnnoCatGrpVoceEpHome(Connection conn) {
		super(ChiusuraAnnoCatGrpVoceEpBulk.class, conn);
	}
	public ChiusuraAnnoCatGrpVoceEpHome(Connection conn, PersistentCache persistentCache) {
		super(ChiusuraAnnoCatGrpVoceEpBulk.class, conn, persistentCache);
	}

	public List<ValoriChiusuraCatGrVoceEP> getImportoChisuraAnnoCatGruppoVoceEP(Integer esercizio,String tipoChiusura) throws PersistencyException {

		List<ValoriChiusuraCatGrVoceEP> list = null;
		try {
			String statement = statmentImportoTotCatGruppoVoceEP;

			LoggableStatement ps = null;
			Connection conn = getConnection();
			ps = new LoggableStatement(conn, statement, true, this.getClass());

			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
				Date dataRifMovimentoChi = DateUtils.firstDateOfTheYear(esercizio);

				ps.setInt( 1,esercizio);
				ps.setString( 2,tipoChiusura);
				ps.setInt( 3,esercizio);

				ResultSet rs = ps.executeQuery();

				try {
					while (rs.next()){

						if(list==null){
							list = new ArrayList<ValoriChiusuraCatGrVoceEP>();
						}
						list.add(getImpCatVoce(rs));

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

			}catch (SQLException e) {
				throw new PersistencyException(e);
			}  finally {
				try {
					ps.close();

				} catch (java.sql.SQLException e) {
				}
			}
		}
		catch (SQLException e) {
			throw new PersistencyException(e);
		}


	}
	private ValoriChiusuraCatGrVoceEP getImpCatVoce(ResultSet rs) throws SQLException {

		ValoriChiusuraCatGrVoceEP valori = new ValoriChiusuraCatGrVoceEP();

		valori.setCdCategoriaGruppo(rs.getString(1));
		valori.setCdVoceEp(rs.getString(2));
		valori.setEsercizio(rs.getInt(3));
		valori.setPgChiusura(rs.getInt(4));
		valori.setTipoChiusura(rs.getString(5));
		valori.setImpTotCatGrVoceEP(rs.getBigDecimal(6));

		return valori;


	}

}