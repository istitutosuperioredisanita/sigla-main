/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;

import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class Chiusura_anno_inventarioHome extends BulkHome {



	public Chiusura_anno_inventarioHome(Connection conn) {
		super(Chiusura_anno_inventarioBulk.class, conn);
	}
	public Chiusura_anno_inventarioHome(Connection conn, PersistentCache persistentCache) {
		super(Chiusura_anno_inventarioBulk.class, conn, persistentCache);
	}



	public String insertChiusuraAnnoInventario() {
		return "INSERT INTO CHIUSURA_ANNO_INVENTARIO( \"PG_CHIUSURA\",\"ANNO\",\"TIPO_CHIUSURA\",\"CD_CATEGORIA_GRUPPO\",\"CD_TIPO_AMMORTAMENTO\" ,\"TI_AMMORTAMENTO\",\"ESERCIZIO_COMPETENZA\" ,\"VALORE_ANNO_PREC\" ,\"VALORE_INCREMENTO\" ,\"VALORE_DECREMENTO\" ,\"QUOTA_AMMORTAMENTO\" ,\"TOTALE_AMMORTAMENTO_ALIENATI\" ,\"QUOTA_AMMORTAMENTO_ANNO_PREC\" ,   \"VALORE_NETTO_ANNO_PREC\",\"DACR\" ,\"UTCR\" ,\"DUVA\" ,\"UTUV\" ,\"PG_VER_REC\" )"+
				"select b.pg_chiusura, "+
					"b.anno, "+
					"b.TIPO_CHIUSURA, "+
					"b.cd_categoria_gruppo, "+
					"b.CD_TIPO_AMMORTAMENTO, "+
					"b.ti_ammortamento ti_ammortamento, "+
					"b.esercizio_competenza esercizio_competenza, "+
					"sum( b.valore_anno_prec) valore_anno_prec, "+
					"sum( b.VALORE_INCREMENTO )VALORE_INCREMENTO, "+
					"sum( b.VALORE_DECREMENTO) VALORE_DECREMENTO, "+
					"sum( b.QUOTA_AMMORTAMENTO)QUOTA_AMMORTAMENTO, "+
					"sum (b.TOTALE_AMMORTAMENTO_ALIENATI) TOTALE_AMMORTAMENTO_ALIENATI, "+
					"sum( b.QUOTA_AMMORTAMENTO_ANNO_PREC) QUOTA_AMMORTAMENTO_ANNO_PREC, "+
					"sum( b.VALORE_NETTO_ANNO_PREC) VALORE_NETTO_ANNO_PREC, "+
					"sysdate dacr, "+
					"'SI' utcr, "+
					"sysdate duva, "+
					"'SI' utuv, "+
					"1 pg_ver_rec "+
				"from ( "+
					"select ? pg_chiusura, "+ // 1 PG_CHIUSURA
						"? anno, "+ // 2 ANNO_AMMORTAMENTO
						"'I' TIPO_CHIUSURA, "+
						"det.cd_categoria_gruppo, "+
						"DECODE( det.fl_migrato,'Y',det.piano_amm_bene_migrato, a.cd_tipo_ammortamento) CD_TIPO_AMMORTAMENTO,"+
						"am.ti_ammortamento, "+
						"a.esercizio_competenza, "+
						"0 VALORE_ANNO_PREC, "+
						"sum(case "+
							"when det.esercizio_carico_bene=? and det.tiporecord='VALORE' then det.valore_iniziale "+ // 3 ANNO_AMMORTAMENTO
							"else 0 "+
						"end )+SUM(case  "+
							"when det.esercizio_buono_carico=? and tiporecord='INCREMENTO' then det.incremento_valore "+ // 4 ANNO_AMMORTAMENTO
							"else 0 "+
						"end ) VALORE_INCREMENTO, "+
						"SUM( case  "+
							"when det.esercizio_buono_carico=? and tiporecord='DECREMENTO' then det.decremento_valore "+ // 5 ANNO_AMMORTAMENTO
							"else 0 "+
						"end ) VALORE_DECREMENTO, "+
						"sum( det.quota_ammortamento) QUOTA_AMMORTAMENTO, "+
						"SUM(QUOTA_AMMO_BENE_ALIENATO )TOTALE_AMMORTAMENTO_ALIENATI, "+
						"0 QUOTA_AMMORTAMENTO_ANNO_PREC, "+
						"0 VALORE_NETTO_ANNO_PREC "+
					"from V_INVENTARIO_BENE_DET det "+
						"inner join ass_tipo_amm_cat_grup_inv a on det.cd_categoria_gruppo=a.cd_categoria_gruppo  and a.esercizio_competenza=? "+ //6 ANNO_AMMORTAMENTO
						"inner join tipo_ammortamento am  on am.cd_tipo_ammortamento=DECODE( det.fl_migrato,'Y',det.piano_amm_bene_migrato, a.cd_tipo_ammortamento) "+
					"where (( det.esercizio_carico_bene=? and  det.tiporecord='VALORE') "+ //7 ANNO_AMMORTAMENTO
						"or ( tiporecord in ( 'DECREMENTO','INCREMENTO') and esercizio_buono_carico in ( ?)) "+ //8 ANNO_AMMORTAMENTO
						"or ( tiporecord ='AMMORTAMENTO' and esercizio_ammortanento=?))"+ //9 ANNO_AMMORTAMENTO
						"group by ?,?,'I', det.cd_categoria_gruppo,DECODE( det.fl_migrato,'Y',det.piano_amm_bene_migrato, a.cd_tipo_ammortamento),am.ti_ammortamento,a.esercizio_competenza "+// 10 PG_CHIUSURA, 11 ANNO_AMMORTAMENTO
					"union all "+
					"select ? pg_chiusura ,"+ //12 PG_CHIUSURA
						"? anno, "+ // 13 ANNO_AMMORTAMENTO
						"'I' TIPO_CHIUSURA, "+
						"det.cd_categoria_gruppo, "+
						"det.CD_TIPO_AMMORTAMENTO, "+
						"det.ti_ammortamento, "+
						"?, "+ // 14 ANNO_AMMORTAMENTO
						"SUM((det.valore_anno_prec+det.valore_incremento-det.valore_decremento)) VALORE_ANNO_PREC, "+
						"sum(0) VALORE_INCREMENTO, "+
						"sum(0) VALORE_DECREMENTO, "+
						"sum(0) QUOTA_AMMORTAMENTO, "+
						"sum(0) TOTALE_AMMORTAMENTO_ALIENATI, "+
						"SUM(det.quota_ammortamento_anno_prec+det.quota_ammortamento-det.TOTALE_AMMORTAMENTO_ALIENATI)  QUOTA_AMMORTAMENTO_ANNO_PREC, "+
						"SUM( VALORE_NETTO_ANNO_PREC)- sum( valore_decremento - totale_ammortamento_alienati) VALORE_NETTO_ANNO_PREC "+
					"from chiusura_anno_inventario det "+
					"where anno=? "+ // 15 ANNO_AMMORTAMENTO - 1
					"group by ?,'?','I',  det.cd_categoria_gruppo,det.cd_tipo_ammortamento,  det.ti_ammortamento) b "+// 16 PG_CHIUSURA, 17 ANNO_AMMORTAMENTO
				"group by b.pg_chiusura,b.anno,b.TIPO_CHIUSURA,b.cd_categoria_gruppo,b.CD_TIPO_AMMORTAMENTO,b.ti_ammortamento,b.esercizio_competenza,sysdate,'SI',sysdate,'SI',1 "+
				"order by b.cd_categoria_gruppo";


	}
}