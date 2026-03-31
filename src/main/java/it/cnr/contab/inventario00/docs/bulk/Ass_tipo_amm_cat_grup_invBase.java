/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class Ass_tipo_amm_cat_grup_invBase extends Ass_tipo_amm_cat_grup_invKey implements Keyed {
//    DT_CANCELLAZIONE TIMESTAMP(7)
	private java.sql.Timestamp dtCancellazione;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_TIPO_AMM_CAT_GRUP_INV
	 **/
	public Ass_tipo_amm_cat_grup_invBase() {
		super();
	}
	public Ass_tipo_amm_cat_grup_invBase(String cdTipoAmmortamento, String tiAmmortamento, String cdCategoriaGruppo, Integer esercizioCompetenza) {
		super(cdTipoAmmortamento, tiAmmortamento, cdCategoriaGruppo, esercizioCompetenza);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data di fine validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public java.sql.Timestamp getDtCancellazione() {
		return dtCancellazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data di fine validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public void setDtCancellazione(java.sql.Timestamp dtCancellazione)  {
		this.dtCancellazione=dtCancellazione;
	}
}