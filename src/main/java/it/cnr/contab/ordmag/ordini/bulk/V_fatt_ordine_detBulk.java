/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 18/08/2025
 */
package it.cnr.contab.ordmag.ordini.bulk;

public class V_fatt_ordine_detBulk extends V_fatt_ordine_detBase {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_FATT_ORDINE_DET
	 **/
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_FATT_ORDINE_DET
	 **/


	public V_fatt_ordine_detBulk() {
		super();
	}
	public FatturaOrdineKey getFatturaOrdineKey(){

		return new FatturaOrdineKey(getCdCds(),getCdUnitaOrganizzativa(),getEsercizio(),getPgFatturaPassiva(),getProgressivoRiga(),
				getCdCdsOrdine(),getUopOrdine(),getEsercizioOrdine(),getCdNumOrdine(),getNumOrdine(),getRigaOrdine(),getConsOrdine());
	}
}