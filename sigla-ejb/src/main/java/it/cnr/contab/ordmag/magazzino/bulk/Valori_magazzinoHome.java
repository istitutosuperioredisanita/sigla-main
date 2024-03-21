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

package it.cnr.contab.ordmag.magazzino.bulk;


import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Valori_magazzinoHome extends BulkHome {

    private static final Logger _log = LoggerFactory.getLogger(Valori_magazzinoHome.class);
    private final String statementQuantitaAnnoPerLotto =
            " select a.cd_cds,a.pg_lotto,a.esercizio,a.CD_NUMERATORE_MAG,a.cd_magazzino_mag,a.ds_magazzino,a.cd_cds_raggr_rim,a.cd_raggr_magazzino_rim, a.cd_bene_servizio,a.ds_bene_servizio,a.cd_categoria_gruppo,a.cd_categoria_padre,a.cd_proprio,a.unita_misura,NVL(a.prezzo_unitario_chi,0) as prezzo_unitario_chi,NVL(a.prezzo_unitario_lotto,0) as prezzo_unitario_lotto,sum(NVL(giacenza,0)) as giancenzaToday, " +
                    " SUM( NVL(quantita_inizio_anno,0)) as quantita_inizio_anno,sum(NVL(qtCarico,0)) as quantita_carico_anno,sum(NVL(qtSCarico,0)) as quantita_scarico_anno, " +
                    "( (SUM(  NVL(quantita_inizio_anno,0))+sum(NVL(qtCarico,0))) - sum(NVL(qtSCarico,0))) as giacenza_calcolata " +
                    " from " +
                    " (select l.CD_CDS,l.CD_MAGAZZINO,l.ESERCIZIO,l.CD_NUMERATORE_MAG,l.PG_LOTTO,l.cd_magazzino_mag,m.ds_magazzino,m.cd_cds_raggr_rim,m.cd_raggr_magazzino_rim,l.cd_bene_servizio,b.ds_bene_servizio,g.cd_categoria_gruppo,g.cd_categoria_padre,g.cd_proprio,b.unita_misura, giacenza,l.valore_unitario, " +
                    "   mov.PREZZO_UNITARIO  as prezzo_unitario_chi, " +
                    "	l.valore_unitario  as prezzo_unitario_lotto, " +
                    "   quantita_inizio_anno, " +
                    "   ( select sum(l.quantita_carico) from movimenti_mag mc inner join tipo_movimento_mag tmc " +
                    "       	 on mc.cd_tipo_movimento=tmc.cd_tipo_movimento " +
                    "       	 and cd_cds_tipo_movimento=tmc.cd_cds " +
                    "       	 and tmc.tipo in ( 'CM','CA') " +
                    "       	 where mc.stato!='ANN' " +
                    "      		 and mc.dt_riferimento<=? " +
                    "        	 and mc.CD_CDS_LOTTO=l.CD_CDS " +
                    "        	 and mc.CD_MAGAZZINO_LOTTO=l.CD_MAGAZZINO " +
                    "        	 and mc.ESERCIZIO_LOTTO=l.ESERCIZIO " +
                    "        	 and mc.CD_NUMERATORE_LOTTO=l.CD_NUMERATORE_MAG " +
                    "        	 and mc.PG_LOTTO=l.PG_LOTTO )" +
                    "    		 qtCarico, " +
                    "  ( select sum(mc.coeff_conv*quantita) from movimenti_mag mc inner join tipo_movimento_mag tmc " +
                    "      		on mc.cd_tipo_movimento=tmc.cd_tipo_movimento " +
                    "      		and cd_cds_tipo_movimento=tmc.cd_cds " +
                    "      		and tmc.tipo in ('SA','SM') " +
                    "      		where mc.stato!='ANN' " +
                    "      		and mc.dt_riferimento<=? " +
                    "      		and mc.CD_CDS_LOTTO=l.CD_CDS " +
                    "      		and mc.CD_MAGAZZINO_LOTTO=l.CD_MAGAZZINO " +
                    "      		and mc.ESERCIZIO_LOTTO=l.ESERCIZIO " +
                    "      		and mc.CD_NUMERATORE_LOTTO=l.CD_NUMERATORE_MAG " +
                    "     		and mc.PG_LOTTO=l.PG_LOTTO ) " +
                    "      		qtSCarico " +
                    " from lotto_mag l " +
                    "    inner join magazzino m on l.cd_magazzino_mag=m.cd_magazzino and l.cd_cds=m.cd_cds " +
                    "    inner join bene_servizio b on b.cd_bene_servizio=l.cd_bene_servizio " +
                    "    inner join categoria_gruppo_invent g on b.cd_categoria_gruppo=g.cd_categoria_gruppo " +
                    " 	 left outer join movimenti_mag mov on (mov.CD_CDS_LOTTO = l.cd_cds and " +
                    "                                          mov.CD_MAGAZZINO_LOTTO=l.cd_magazzino and " +
                    "                                          mov.ESERCIZIO_LOTTO=l.esercizio and " +
                    "                                          mov.CD_NUMERATORE_LOTTO=l.cd_numeratore_mag and " +
                    "                                          mov.PG_LOTTO=l.pg_lotto and " +
                    "                                          mov.DT_RIFERIMENTO=? and " +
                    "										   mov.CD_TIPO_MOVIMENTO='CHI')" +
                    " where esercizio<= ? and l.dt_carico <= ? ) a ";
    private final String groupByQuantitaAnnoPerLotto = " group by a.cd_cds,a.pg_lotto,a.esercizio,a.CD_NUMERATORE_MAG,a.cd_magazzino_mag,a.ds_magazzino,a.cd_cds_raggr_rim,a.cd_raggr_magazzino_rim,a.cd_bene_servizio,a.ds_bene_servizio,a.cd_categoria_gruppo,a.cd_categoria_padre,a.cd_proprio,a.unita_misura,a.prezzo_unitario_chi,a.prezzo_unitario_lotto";
    private final String orderByQuantitaAnnoPerLotto = " order by  a.cd_cds,a.pg_lotto,a.esercizio,a.CD_NUMERATORE_MAG,a.cd_magazzino_mag ";


    public Valori_magazzinoHome(Connection conn) {
        super(Valori_magazzinoBulk.class, conn);
    }
    public Valori_magazzinoHome(Connection conn, PersistentCache persistentCache) {
        super(Valori_magazzinoBulk.class, conn, persistentCache);
    }

    public List<ValoriLottoPerAnno> getQuantitaAnnoPerLotto(Integer esercizio, Date dataFinePeriodo) throws PersistencyException {

        List<ValoriLottoPerAnno> list = null;
        try {
            String statement = statementQuantitaAnnoPerLotto;

            statement = statement.concat(groupByQuantitaAnnoPerLotto);
            statement = statement.concat(orderByQuantitaAnnoPerLotto);

            LoggableStatement ps = null;
            Connection conn = getConnection();
            ps = new LoggableStatement(conn, statement, true, this.getClass());

            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                Date dataRifMovimentoChi = DateUtils.firstDateOfTheYear(esercizio);


                ps.setDate(1, new java.sql.Date(dataFinePeriodo.getTime()));
                ps.setDate(2, new java.sql.Date(dataFinePeriodo.getTime()));
                ps.setDate(3, new java.sql.Date(dataRifMovimentoChi.getTime()));
                ps.setInt(4, esercizio);
                ps.setDate(5, new java.sql.Date(dataFinePeriodo.getTime()));


                ResultSet rs = ps.executeQuery();

                try {
                    while (rs.next()) {

                        if (list == null) {
                            list = new ArrayList<ValoriLottoPerAnno>();
                        }
                        list.add(getValoreLottoPerAnno(rs));

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

    private ValoriLottoPerAnno getValoreLottoPerAnno(ResultSet rs) throws SQLException {

        ValoriLottoPerAnno valori = new ValoriLottoPerAnno();

        valori.setCdCdsLotto(rs.getString(1));
        valori.setPgLotto(rs.getInt(2));
        valori.setEsercizioLotto(rs.getInt(3));
        valori.setCdNumeratoreMagLotto(rs.getString(4));
        valori.setCdMagazzinoLotto(rs.getString(5));
        valori.setMagazzinoDesc(rs.getString(6));
        valori.setCdsRaggrMag(rs.getString(7));
        valori.setCdRaggrMag(rs.getString(8));
        valori.setCdBeneServizio(rs.getString(9));
        valori.setBeneServizioDesc(rs.getString(10));
        valori.setCdCatGruppo(rs.getString(11));
        valori.setCdCatPadre(rs.getString(12));
        valori.setCdGruppo(rs.getString(13));
        valori.setUnitaMisura(rs.getString(14));
        valori.setValoreUnitarioChi(rs.getBigDecimal(15));
        valori.setValoreUnitarioLotto(rs.getBigDecimal(16));
        valori.setGiacenza(rs.getBigDecimal(17));
        valori.setQtyInizioAnno(rs.getBigDecimal(18));
        valori.setQtyCaricoAnno(rs.getBigDecimal(19));
        valori.setQtyScaricoAnno(rs.getBigDecimal(20));
        valori.setGiacenzaCalcolata(rs.getBigDecimal(21));


        return valori;


    }
}
