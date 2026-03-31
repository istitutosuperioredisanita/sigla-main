/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/03/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.cnr.contab.ordmag.magazzino.dto.ValoriChiusuraCatGrVoceEP;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
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

	public java.util.List<ChiusuraAnnoCatGrpVoceEpBulk> findChiusureAnnoMagazzinoList( Integer esercizio ) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(ChiusuraAnnoCatGrpVoceEpBulk.class);
		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND,"anno",SQLBuilder.EQUALS, esercizio);
		sql.addClause(FindClause.AND,"tipoChiusura",SQLBuilder.EQUALS, ChiusuraAnnoCatGrpVoceEpBulk.TIPO_CHIUSURA_MAGAZZINO);

		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"impTotale", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.addClause(FindClause.OR,"impPlusValenze", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.addClause(FindClause.OR,"impMinusValenze", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.closeParenthesis();

		sql.addOrderBy("cd_categoria_gruppo");
		return home.fetchAll(sql);
	}

	public java.util.List<ChiusuraAnnoCatGrpVoceEpBulk> findChiusureAnnoInventarioList( Integer esercizio ) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(ChiusuraAnnoCatGrpVoceEpBulk.class);
		SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND,"anno",SQLBuilder.EQUALS, esercizio);
		sql.addClause(FindClause.AND,"tipoChiusura",SQLBuilder.EQUALS, ChiusuraAnnoCatGrpVoceEpBulk.TIPO_CHIUSURA_INVENTARIO);

		sql.openParenthesis(FindClause.AND);
		sql.addClause(FindClause.OR,"impTotale", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.addClause(FindClause.OR,"impPlusValenze", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.addClause(FindClause.OR,"impMinusValenze", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.addClause(FindClause.OR,"impDecrementi", SQLBuilder.NOT_EQUALS, BigDecimal.ZERO);
		sql.closeParenthesis();

		sql.addOrderBy("cd_categoria_gruppo");
		return home.fetchAll(sql);
	}
	public String insertImportiPerCatGruppoVoceEPInventario(){
		return "INSERT INTO chiusura_anno_catgrp_voce_ep (\"PG_CHIUSURA\",\"ANNO\",\"TIPO_CHIUSURA\",\"CD_CATEGORIA_GRUPPO\",\"ESERCIZIO\",\"CD_VOCE_EP\",\"IMP_TOTALE\",\"DACR\",\"UTCR\",\"DUVA\",\"UTUV\",\"PG_VER_REC\", \"IMP_PLUSVALENZE\", \"IMP_MINUSVALENZE\",\"IMP_DECREMENTI\") "+
				"select * from( "+
					"select ? pg_chiusura ,"+ // 1 PG_CHIUSURA
						"? anno , "+ // 2 ANNO_AMMORTAMENTO
						"'I' tipo_chiusura, "+
						"c.cd_categoria_gruppo, "+
						"v.esercizio,v.cd_voce_ep, "+
						"sum( quota_ammortamento) imp_totale, "+
						"sysdate dacr, "+
						"'SI' utcr, "+
						"sysdate duva, "+
						"'SI' utuv, "+
						"1, "+
						"case  when sum( valore_decremento - totale_ammortamento_alienati) <0  "+
									"then abs(sum( valore_decremento - totale_ammortamento_alienati)) "+
									"else 0 "+
						"end plus_valenze, "+
						"case  when sum( valore_decremento - totale_ammortamento_alienati) >0  "+
									"then sum( valore_decremento - totale_ammortamento_alienati) "+
									"else 0 "+
						"end minus_valenze, "+
						"sum(totale_ammortamento_alienati)  imp_decremento "+
					"from chiusura_anno_inventario c "+
					"inner join ass_catgrp_invent_voce_ep v on c.cd_categoria_gruppo=v.cd_categoria_gruppo and c.anno=v.esercizio and c.pg_chiusura=? "+ //3 PG_CHIUSURA
					"group by ?,?, 'I', c.cd_categoria_gruppo,v.esercizio,v.cd_voce_ep) "+ // 4 PG_CHIUSURA, 5 ANNO_AMMORTAMENTO
				"where ( imp_totale>0 or plus_valenze>0 or minus_valenze>0 or imp_decremento>0 ) ";

	}
}