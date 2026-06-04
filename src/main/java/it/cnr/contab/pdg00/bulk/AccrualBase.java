/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;
import it.cnr.jada.persistency.Keyed;
public class AccrualBase extends AccrualKey implements Keyed {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCRUAL
	 **/
	public AccrualBase() {
		super();
	}
	public AccrualBase(Long esercizio, String stato, String esito) {
		super(esercizio, stato, esito);
	}
}