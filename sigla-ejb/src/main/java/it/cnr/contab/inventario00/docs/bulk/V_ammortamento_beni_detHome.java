/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.cnr.contab.anagraf00.core.bulk.RapportoBulk;
import it.cnr.contab.inventario00.dto.NormalizzatoreAmmortamentoDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.utenze00.bulk.AccessoBulk;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class V_ammortamento_beni_detHome extends BulkHome {

	public V_ammortamento_beni_detHome(Connection conn) {
		super(V_ammortamento_beni_detBulk.class, conn);
	}
	public V_ammortamento_beni_detHome(Connection conn, PersistentCache persistentCache) {
		super(V_ammortamento_beni_detBulk.class, conn, persistentCache);
	}

	private final String StatmentSelectDatiAmmortamentoBeni = "" +
			"SELECT d.pg_inventario,d.nr_inventario, d.progressivo, d.etichetta, d.fl_ammortamento,d.fl_totalmente_scaricato,d.ti_ammortamento,d.cd_categoria_gruppo," +
			"		SUM(d.valore_iniziale) valore_iniziale," +
			"		SUM(valore_ammortizzato) valore_ammortizzato_da_bene," +
			"		SUM(d.imponibile_ammortamento) imponibile_ammortamento_da_bene," +
			"		SUM(variazione_piu) variazione_piu," +
			"		SUM(variazione_meno) variazione_meno," +
			"		SUM(d.incremento_valore) incremento_valore," +
			"		SUM(d.decremento_Valore) decremento_Valore," +
			"		SUM(d.storno) storno," +
			"		SUM( d.imponibile_ammortamento) + SUM(d.incremento_valore)- SUM(d.decremento_Valore)  imponibile_ammortamento_calcolato," +
			"		SUM(d.valore_ammortizzato)-SUM(d.storno) valore_ammortizzato_calcolato," +
			"		MAX(d.NUMERO_ANNO_AMMORTAMENTO)," +
			"		d.PERC_PRIMO_ANNO," +
			"		d.PERC_SUCCESSIVI" +
			"	FROM"+it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"V_AMMORTAMENTO_BENI_DET d " +
			"   WHERE ( " +
			"			( tiporecord  in ('INCREMENTO','DECREMENTO') AND esercizio_buono_carico>?) " + //2024
			"        		OR " +
			"           ( tiporecord ='STORNO' and esercizio_ammortanento>?) " + //2024
			"        		OR " +
			"			( tiporecord ='VALORE ' and esercizio_carico_bene<=?) " + // 2024
			"         		OR " +
			"        	( tiporecord ='AMMORTAMENTO' and esercizio_ammortanento=?) " + // 2023
			"        ) " +
			"        AND d.fl_ammortamento='Y' " +
			"        AND ( esercizio_competenza=? or esercizio_competenza is null) " +// 2023
			" 	GROUP BY d.pg_inventario,d.nr_inventario, d.progressivo, d.etichetta,d.fl_ammortamento,d.fl_totalmente_scaricato,d.ti_ammortamento,d.cd_categoria_gruppo,d.perc_primo_anno,d.perc_successivi";



	public final String selectBeniDaAmmortizzare = "SELECT a.PG_INVENTARIO,a.NR_INVENTARIO,a.PROGRESSIVO,a.ETICHETTA,a.FL_TOTALMENTE_SCARICATO,a.TI_AMMORTAMENTO,"+
											      		"a.CD_CATEGORIA_GRUPPO,a.PERC_PRIMO_ANNO,a.PERC_SUCCESSIVI,a.CD_TIPO_AMMORTAMENTO,a.ESERCIZIO_COMPETENZA,"+
												  		"a.VALORE_INIZIALE,a.VALORE_AMMORTIZZATO_BENE,a.IMPONIBILE_AMMORTAMENTO_BENE,a.VARIAZIONE_PIU,a.VARIAZIONE_MENO,"+
			                                      		"a.INCREMENTO_VALORE,a.DECREMENTO_VALORE,a.STORNO,a.IMPONIBILE_AMMORTAMENTO_CALCOLATO,a.VALORE_AMMORTIZZATO_CALCOLATO,"+
											      		"a.NUMERO_ANNO_AMMORTAMENTO "+
												  " FROM ( "+
													" SELECT PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,ETICHETTA,FL_TOTALMENTE_SCARICATO,TI_AMMORTAMENTO,CD_CATEGORIA_GRUPPO,"+
															 "PERC_PRIMO_ANNO,PERC_SUCCESSIVI,CD_TIPO_AMMORTAMENTO,ESERCIZIO_COMPETENZA," +
			                                                 "NVL(SUM(VALORE_INIZIALE),0) VALORE_INIZIALE,"+
														     "NVL(SUM(VALORE_AMMORTIZZATO),0) VALORE_AMMORTIZZATO_BENE,"+
															 "NVL(SUM(IMPONIBILE_AMMORTAMENTO),0) IMPONIBILE_AMMORTAMENTO_BENE,"+
	                                                         "NVL(SUM(VARIAZIONE_PIU),0) VARIAZIONE_PIU,"+
				                                             "NVL(SUM(VARIAZIONE_MENO),0) VARIAZIONE_MENO,"+
	                                                         "NVL(SUM(INCREMENTO_VALORE),0) INCREMENTO_VALORE,"+
															 "NVL(SUM(DECREMENTO_VALORE),0) DECREMENTO_VALORE,"+
	                                                         "NVL(SUM(STORNO),0) STORNO,"+
	                                                         "NVL(SUM(IMPONIBILE_AMMORTAMENTO),0) - NVL(SUM(INCREMENTO_VALORE),0) + NVL(SUM(DECREMENTO_VALORE),0) IMPONIBILE_AMMORTAMENTO_CALCOLATO,"+
	                                                         "NVL(SUM(VALORE_AMMORTIZZATO),0) - NVL(SUM(STORNO),0) VALORE_AMMORTIZZATO_CALCOLATO,"+
	                                                         "MAX(NUMERO_ANNO_AMMORTAMENTO) NUMERO_ANNO_AMMORTAMENTO"+
	                                                " FROM "+
	                                                it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema()+"V_AMMORTAMENTO_BENI_DET WHERE "+
			                                        " ( FL_AMMORTAMENTO = 'Y' ) AND "+
	                                                "(  ( ESERCIZIO_COMPETENZA = ? ) OR ( ESERCIZIO_COMPETENZA IS NULL ) ) "+ //2024
	                                                " AND "+
			                                           "( ( ( TIPORECORD = 'INCREMENTO' ) AND  ( ESERCIZIO_BUONO_CARICO > ? ) ) "+ //2024
	                                                " OR "+
			                                        " ( ( TIPORECORD = 'DECREMENTO' ) AND ( ESERCIZIO_BUONO_CARICO > ? )	) "+ //2024
	                                                " OR "+
			                                        " ( ( TIPORECORD = 'STORNO' ) AND ( ESERCIZIO_AMMORTANENTO > ? )	) "+ //2024
	                                                " OR "+
			                                        " ( ( TIPORECORD = 'VALORE' ) AND ( ESERCIZIO_CARICO_BENE <= ? )	) " +  //2024
			                                        " ) "+
												    " GROUP BY PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,ETICHETTA,FL_TOTALMENTE_SCARICATO,TI_AMMORTAMENTO,CD_CATEGORIA_GRUPPO,PERC_PRIMO_ANNO,PERC_SUCCESSIVI,CD_TIPO_AMMORTAMENTO,ESERCIZIO_COMPETENZA)  a "+
												  " WHERE a.IMPONIBILE_AMMORTAMENTO_CALCOLATO > a.VALORE_AMMORTIZZATO_CALCOLATO";
}