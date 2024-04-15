/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class ChiusuraAnnoBulk extends ChiusuraAnnoBase {

	public static final String TIPO_CHIUSURA_MAGAZZINO = "M";
	public static final String TIPO_CHIUSURA_INVENTARIO = "I";

	public static final String STATO_CHIUSURA_PROVVISORIO = "P";
	public static final String STATO_CHIUSURA_PREDEFINITIVO = "X";
	public static final String STATO_CHIUSURA_DEFINITIVO = "D";
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO
	 **/
	public ChiusuraAnnoBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO
	 **/
	public ChiusuraAnnoBulk(Integer pgChiusura, Integer anno, String tipoChiusura) {
		super(pgChiusura, anno, tipoChiusura);
	}
}