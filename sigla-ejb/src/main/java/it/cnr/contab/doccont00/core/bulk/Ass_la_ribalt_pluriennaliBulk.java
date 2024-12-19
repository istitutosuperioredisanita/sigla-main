/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/12/2024
 */
package it.cnr.contab.doccont00.core.bulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class Ass_la_ribalt_pluriennaliBulk extends Ass_la_ribalt_pluriennaliBase {
	/**
	 * [LINEA_ATTIVITA ]
	 **/
	private Linea_attivitaBulk lineaAttivita =  new Linea_attivitaBulk();
	private Linea_attivitaBulk lineaAttivitaNuovoEser =  new Linea_attivitaBulk();
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
		setLineaAttivita( new Linea_attivitaBulk() );
	}
	public Linea_attivitaBulk getLineaAttivita() {
		return lineaAttivita;
	}
	public void setLineaAttivita(Linea_attivitaBulk lineaAttivita)  {
		this.lineaAttivita=lineaAttivita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public String getCdCentroResponsabilita() {
		Linea_attivitaBulk lineaAttivita = this.getLineaAttivita();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivita().getLinea_att().getCd_centro_responsabilita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita vecchio esercizio]
	 **/
	public void setCdCentroResponsabilita(String cdCentroResponsabilita)  {
		this.getLineaAttivita().getLinea_att().setCd_centro_responsabilita(cdCentroResponsabilita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public String getCdLineaAttivita() {
		Linea_attivitaBulk lineaAttivita = this.getLineaAttivita();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivita().getLinea_att().getCd_linea_attivita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita vecchio esercizio]
	 **/
	public void setCdLineaAttivita(String cdLineaAttivita)  {
		this.getLineaAttivita().getLinea_att().setCd_linea_attivita(cdLineaAttivita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public String getCdCentroRespRibalt() {
		Linea_attivitaBulk lineaAttivita = this.getLineaAttivitaNuovoEser();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaNuovoEser().getLinea_att().getCd_centro_responsabilita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del centro di responsabilità della linea di attivita nuovo esercizio]
	 **/
	public void setCdCentroRespRibalt(String cdCentroRespRibalt)  {
		this.getLineaAttivitaNuovoEser().getLinea_att().setCd_centro_responsabilita(cdCentroRespRibalt);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public String getCdLineaAttivitaRibalt() {
		Linea_attivitaBulk lineaAttivita = this.getLineaAttivitaNuovoEser();
		if (lineaAttivita == null)
			return null;
		return getLineaAttivitaNuovoEser().getLinea_att().getCd_linea_attivita();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice della linea di attivita nuovo esercizio]
	 **/
	public void setCdLineaAttivitaRibalt(String cdLineaAttivitaRibalt)  {
		this.getLineaAttivitaNuovoEser().getLinea_att().setCd_linea_attivita(cdLineaAttivitaRibalt);
	}

	public Linea_attivitaBulk getLineaAttivitaNuovoEser() {
		return lineaAttivitaNuovoEser;
	}

	public void setLineaAttivitaNuovoEser(Linea_attivitaBulk lineaAttivitaNuovoEser) {
		this.lineaAttivitaNuovoEser = lineaAttivitaNuovoEser;
	}
}