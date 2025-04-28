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




}