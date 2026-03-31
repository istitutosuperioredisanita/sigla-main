/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 03/01/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.persistency.Keyed;
public class Accertamento_pluriennale_voceBase extends Accertamento_pluriennale_voceKey implements Keyed {
//    IMPORTO DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal importo;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCERTAMENTO_PLURIENNALE_VOCE
	 **/
	public Accertamento_pluriennale_voceBase() {
		super();
	}
	public Accertamento_pluriennale_voceBase(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgAccertamento, Integer anno, String cdCentroResponsabilita, String cdLineaAttivita) {
		super(cdCds, esercizio, esercizioOriginale, pgAccertamento, anno, cdCentroResponsabilita, cdLineaAttivita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo del dettaglio per la linea attivita]
	 **/
	public java.math.BigDecimal getImporto() {
		return importo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo del dettaglio per la linea attivita]
	 **/
	public void setImporto(java.math.BigDecimal importo)  {
		this.importo=importo;
	}
}