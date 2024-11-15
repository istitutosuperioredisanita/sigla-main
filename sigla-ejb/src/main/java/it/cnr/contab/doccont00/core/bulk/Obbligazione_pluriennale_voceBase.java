/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/11/2023
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.persistency.Keyed;
public class Obbligazione_pluriennale_voceBase extends Obbligazione_pluriennale_voceKey implements Keyed {
//    IMPORTO DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal importo;

	private Boolean autoRimodulazione = Boolean.TRUE;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: OBBLIGAZIONE_PLURIENNALE_VOCE
	 **/
	public Obbligazione_pluriennale_voceBase() {
		super();
	}
	public Obbligazione_pluriennale_voceBase(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgObbligazione, Integer anno, String cdCentroResponsabilita, String cdLineaAttivita, Integer esercizioVoce, String tiAppartenenza, String tiGestione, String cdVoce) {
		super(cdCds, esercizio, esercizioOriginale, pgObbligazione, anno, cdCentroResponsabilita, cdLineaAttivita, esercizioVoce, tiAppartenenza, tiGestione, cdVoce);
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

	public Boolean getAutoRimodulazione() {
		return autoRimodulazione;
	}

	public void setAutoRimodulazione(Boolean autoRimodulazione) {
		this.autoRimodulazione = autoRimodulazione;
	}
}