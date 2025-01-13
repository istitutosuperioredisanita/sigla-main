/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/12/2024
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
public class Ass_la_ribalt_pluriennaliBulk extends Ass_la_ribalt_pluriennaliBase {
	/**
	 * [LINEA_ATTIVITA ]
	 **/
	private WorkpackageBulk lineaAttivita =  new WorkpackageBulk();
	private WorkpackageBulk lineaAttivitaNuovoEser =  new WorkpackageBulk();
	private WorkpackageBulk lineaAttivitaPrelFondi =  new WorkpackageBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_LA_RIBALT_PLURIENNALI
	 **/
	public Ass_la_ribalt_pluriennaliBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_LA_RIBALT_PLURIENNALI
	 **/
	public Ass_la_ribalt_pluriennaliBulk(Integer esercizio, String cdCentroResponsabilita, String cdLineaAttivita) {
		super(esercizio, cdCentroResponsabilita, cdLineaAttivita);
		setLineaAttivita( new WorkpackageBulk() );
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public String getCdCentroResponsabilita() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivita();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivita().getCd_centro_responsabilita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public void setCdCentroResponsabilita(String cdCentroResponsabilita)  {
		this.getLineaAttivita().setCd_centro_responsabilita(cdCentroResponsabilita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public String getCdLineaAttivita() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivita();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivita().getCd_linea_attivita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public void setCdLineaAttivita(String cdLineaAttivita)  {
		this.getLineaAttivita().setCd_linea_attivita(cdLineaAttivita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public String getCdCentroRespRibalt() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivitaNuovoEser();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaNuovoEser().getCd_centro_responsabilita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public void setCdCentroRespRibalt(String cdCentroRespRibalt)  {
		this.getLineaAttivitaNuovoEser().setCd_centro_responsabilita(cdCentroRespRibalt);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public String getCdLineaAttivitaRibalt() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivitaNuovoEser();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaNuovoEser().getCd_linea_attivita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public void setCdLineaAttivitaRibalt(String cdLineaAttivitaRibalt)  {
		this.getLineaAttivitaNuovoEser().setCd_linea_attivita(cdLineaAttivitaRibalt);
	}

	public WorkpackageBulk getLineaAttivita() {
		return lineaAttivita;
	}

	public void setLineaAttivita(WorkpackageBulk lineaAttivita) {
		this.lineaAttivita = lineaAttivita;
	}

	public WorkpackageBulk getLineaAttivitaNuovoEser() {
		return lineaAttivitaNuovoEser;
	}

	public void setLineaAttivitaNuovoEser(WorkpackageBulk lineaAttivitaNuovoEser) {
		this.lineaAttivitaNuovoEser = lineaAttivitaNuovoEser;
	}

	public WorkpackageBulk getLineaAttivitaPrelFondi() {
		return lineaAttivitaPrelFondi;
	}

	public void setLineaAttivitaPrelFondi(WorkpackageBulk lineaAttivitaPrelFondi) {
		this.lineaAttivitaPrelFondi = lineaAttivitaPrelFondi;
	}

	public String getCdCentroRespGaePrelFondi() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivitaPrelFondi();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaPrelFondi().getCd_centro_responsabilita();

	}

	public void setCdCentroRespGaePrelFondi(String cdCentroRespGaePrelFondi) {
		this.getLineaAttivitaPrelFondi().setCd_centro_responsabilita(cdCentroRespGaePrelFondi);
	}

	public String getCdLineaAttivitaPrelFondi() {
		WorkpackageBulk lineaAttivita = this.getLineaAttivitaPrelFondi();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaPrelFondi().getCd_linea_attivita();

	}

	public void setCdLineaAttivitaPrelFondi(String cdLineaAttivitaPrelFondi) {
		this.getLineaAttivitaPrelFondi().setCd_linea_attivita(cdLineaAttivitaPrelFondi);
	}
}