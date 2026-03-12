/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class Numeratore_doc_t_rBulk extends Numeratore_doc_t_rBase {

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: NUMERATORE_DOC_T_R
	 **/
	public Numeratore_doc_t_rBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: NUMERATORE_DOC_T_R
	 **/
	public Numeratore_doc_t_rBulk(Long pgInventario, String tiTrasportoRientro, Integer esercizio) {
		super(pgInventario, tiTrasportoRientro, esercizio);
	}
}