/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/12/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.persistency.Keyed;
public class Ass_la_ribalt_pluriennaliBase extends Ass_la_ribalt_pluriennaliKey implements Keyed {
//    CD_CENTRO_RESP_RIBALT VARCHAR(30) NOT NULL
	private String cdCentroRespRibalt;
 
//    CD_LINEA_ATTIVITA_RIBALT VARCHAR(10) NOT NULL
	private String cdLineaAttivitaRibalt;

	private String cdCentroRespGaePrelFondi;
	private String cdLineaAttivitaPrelFondi;

 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_LA_RIBALT_PLURIENNALI
	 **/
	public Ass_la_ribalt_pluriennaliBase() {
		super();
	}
	public Ass_la_ribalt_pluriennaliBase(Integer esercizio, String cdCentroResponsabilita, String cdLineaAttivita) {
		super(esercizio, cdCentroResponsabilita, cdLineaAttivita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public String getCdCentroRespRibalt() {
		return cdCentroRespRibalt;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public void setCdCentroRespRibalt(String cdCentroRespRibalt)  {
		this.cdCentroRespRibalt=cdCentroRespRibalt;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public String getCdLineaAttivitaRibalt() {
		return cdLineaAttivitaRibalt;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public void setCdLineaAttivitaRibalt(String cdLineaAttivitaRibalt)  {
		this.cdLineaAttivitaRibalt=cdLineaAttivitaRibalt;
	}

	public String getCdCentroRespGaePrelFondi() {
		return cdCentroRespGaePrelFondi;
	}

	public void setCdCentroRespGaePrelFondi(String cdCentroRespGaePrelFondi) {
		this.cdCentroRespGaePrelFondi = cdCentroRespGaePrelFondi;
	}

	public String getCdLineaAttivitaPrelFondi() {
		return cdLineaAttivitaPrelFondi;
	}

	public void setCdLineaAttivitaPrelFondi(String cdLineaAttivitaPrelFondi) {
		this.cdLineaAttivitaPrelFondi = cdLineaAttivitaPrelFondi;
	}
}