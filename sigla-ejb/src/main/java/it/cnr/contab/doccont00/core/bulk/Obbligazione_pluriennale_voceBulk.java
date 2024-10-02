/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/11/2023
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.progettiric00.core.bulk.Ass_progetto_piaeco_voceBulk;
import it.cnr.contab.progettiric00.core.bulk.Progetto_piano_economicoBulk;
import it.cnr.jada.bulk.BulkList;

import java.util.stream.Collectors;

public class Obbligazione_pluriennale_voceBulk extends Obbligazione_pluriennale_voceBase {
	/**
	 * [OBBLIGAZIONE_PLURIENNALE ]
	 **/
	private Obbligazione_pluriennaleBulk obbligazionePluriennale =  new Obbligazione_pluriennaleBulk();
	/**
	 * [ELEMENTO_VOCE ]
	 **/
	private Elemento_voceBulk elementoVoce =  new Elemento_voceBulk();

	private WorkpackageBulk linea_attivita = new WorkpackageBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: OBBLIGAZIONE_PLURIENNALE_VOCE
	 **/
	public Obbligazione_pluriennale_voceBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: OBBLIGAZIONE_PLURIENNALE_VOCE
	 **/
	public Obbligazione_pluriennale_voceBulk(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgObbligazione, Integer anno, String cdCentroResponsabilita, String cdLineaAttivita, Integer esercizioVoce, String tiAppartenenza, String tiGestione, String cdVoce) {
		super(cdCds, esercizio, esercizioOriginale, pgObbligazione, anno, cdCentroResponsabilita, cdLineaAttivita, esercizioVoce, tiAppartenenza, tiGestione, cdVoce);
		setObbligazionePluriennale( new Obbligazione_pluriennaleBulk(cdCds,esercizio,esercizioOriginale,pgObbligazione,anno) );
		setElementoVoce( new Elemento_voceBulk(cdVoce,esercizioVoce,tiAppartenenza,tiGestione) );
		setLinea_attivita(new it.cnr.contab.config00.latt.bulk.WorkpackageBulk(cdCentroResponsabilita,cdLineaAttivita));

	}
	public Obbligazione_pluriennaleBulk getObbligazionePluriennale() {
		return obbligazionePluriennale;
	}
	public void setObbligazionePluriennale(Obbligazione_pluriennaleBulk obbligazionePluriennale)  {
		this.obbligazionePluriennale=obbligazionePluriennale;
	}
	public Elemento_voceBulk getElementoVoce() {
		return elementoVoce;
	}
	public void setElementoVoce(Elemento_voceBulk elementoVoce)  {
		this.elementoVoce=elementoVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'obbligazione]
	 **/
	public String getCdCds() {
		Obbligazione_pluriennaleBulk obbligazionePluriennale = this.getObbligazionePluriennale();
		if (obbligazionePluriennale == null)
			return null;
		return getObbligazionePluriennale().getCdCds();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'obbligazione]
	 **/
	public void setCdCds(String cdCds)  {
		this.getObbligazionePluriennale().setCdCds(cdCds);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'obbligazione]
	 **/
	public Integer getEsercizio() {
		Obbligazione_pluriennaleBulk obbligazionePluriennale = this.getObbligazionePluriennale();
		if (obbligazionePluriennale == null)
			return null;
		return getObbligazionePluriennale().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'obbligazione]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getObbligazionePluriennale().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'obbligazione]
	 **/
	public Integer getEsercizioOriginale() {
		Obbligazione_pluriennaleBulk obbligazionePluriennale = this.getObbligazionePluriennale();
		if (obbligazionePluriennale == null)
			return null;
		return getObbligazionePluriennale().getEsercizioOriginale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'obbligazione]
	 **/
	public void setEsercizioOriginale(Integer esercizioOriginale)  {
		this.getObbligazionePluriennale().setEsercizioOriginale(esercizioOriginale);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'obbligazione]
	 **/
	public Long getPgObbligazione() {
		Obbligazione_pluriennaleBulk obbligazionePluriennale = this.getObbligazionePluriennale();
		if (obbligazionePluriennale == null)
			return null;
		return getObbligazionePluriennale().getPgObbligazione();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'obbligazione]
	 **/
	public void setPgObbligazione(Long pgObbligazione)  {
		this.getObbligazionePluriennale().setPgObbligazione(pgObbligazione);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno Obbligazione Pluriennale]
	 **/
	public Integer getAnno() {
		Obbligazione_pluriennaleBulk obbligazionePluriennale = this.getObbligazionePluriennale();
		if (obbligazionePluriennale == null)
			return null;
		return getObbligazionePluriennale().getAnno();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno Obbligazione Pluriennale]
	 **/
	public void setAnno(Integer anno)  {
		this.getObbligazionePluriennale().setAnno(anno);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio della voce del capitolo]
	 **/
	public Integer getEsercizioVoce() {
		Elemento_voceBulk elementoVoce = this.getElementoVoce();
		if (elementoVoce == null)
			return null;
		return getElementoVoce().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio della voce del capitolo]
	 **/
	public void setEsercizioVoce(Integer esercizioVoce)  {
		this.getElementoVoce().setEsercizio(esercizioVoce);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Definisce l'appartenenza (CNR o CdS) delle voci del piano finanziario di riferimento dell'obbligazione]
	 **/
	public String getTiAppartenenza() {
		Elemento_voceBulk elementoVoce = this.getElementoVoce();
		if (elementoVoce == null)
			return null;
		return getElementoVoce().getTi_appartenenza();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Definisce l'appartenenza (CNR o CdS) delle voci del piano finanziario di riferimento dell'obbligazione]
	 **/
	public void setTiAppartenenza(String tiAppartenenza)  {
		this.getElementoVoce().setTi_appartenenza(tiAppartenenza);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Definisce la sezione (entrata o spesa) delle voci del piano finanziario di riferimento dell'obbligazione]
	 **/
	public String getTiGestione() {
		Elemento_voceBulk elementoVoce = this.getElementoVoce();
		if (elementoVoce == null)
			return null;
		return getElementoVoce().getTi_gestione();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Definisce la sezione (entrata o spesa) delle voci del piano finanziario di riferimento dell'obbligazione]
	 **/
	public void setTiGestione(String tiGestione)  {
		this.getElementoVoce().setTi_gestione(tiGestione);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del capitolo finanziario (mastrino VOCE_F) di riferimento dell'obbligazione]
	 **/
	public String getCdVoce() {
		Elemento_voceBulk elementoVoce = this.getElementoVoce();
		if (elementoVoce == null)
			return null;
		return getElementoVoce().getCd_elemento_voce();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del capitolo finanziario (mastrino VOCE_F) di riferimento dell'obbligazione]
	 **/
	public void setCdVoce(String cdVoce)  {
		this.getElementoVoce().setCd_elemento_voce(cdVoce);
	}


	public java.lang.String getCdCentroResponsabilita() {
		it.cnr.contab.config00.latt.bulk.WorkpackageBulk linea_attivita = this.getLinea_attivita();
		if (linea_attivita == null)
			return null;
		it.cnr.contab.config00.sto.bulk.CdrBulk centro_responsabilita = linea_attivita.getCentro_responsabilita();
		if (centro_responsabilita == null)
			return null;
		return centro_responsabilita.getCd_centro_responsabilita();
	}

	public void setCdCentroResponsabilita(java.lang.String cd_centro_responsabilita) {
		this.getLinea_attivita().getCentro_responsabilita().setCd_centro_responsabilita(cd_centro_responsabilita);
	}
	public WorkpackageBulk getLinea_attivita() {
		return linea_attivita;
	}
	public void cdCentroResponsabilita(java.lang.String cd_linea_attivita) {
		this.getLinea_attivita().setCd_linea_attivita(cd_linea_attivita);
	}
	public java.lang.String getCdLineaAttivita() {
		it.cnr.contab.config00.latt.bulk.WorkpackageBulk linea_attivita = this.getLinea_attivita();
		if (linea_attivita == null)
			return null;
		return linea_attivita.getCd_linea_attivita();
	}
	public void setLinea_attivita(WorkpackageBulk linea_attivita) {
		this.linea_attivita = linea_attivita;
	}

	@Override
	public Obbligazione_pluriennale_voceBulk clone() {

		Obbligazione_pluriennale_voceBulk nuovo = null;

		try {
			nuovo = (Obbligazione_pluriennale_voceBulk)getClass().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		nuovo.setObbligazionePluriennale(this.getObbligazionePluriennale());
		nuovo.setAnno(this.getAnno());
		nuovo.setImporto(this.getImporto());
		nuovo.setLinea_attivita(this.getLinea_attivita());
		nuovo.setCdVoce(this.getCdVoce());

		nuovo.setCrudStatus(TO_BE_CREATED);

		return nuovo;
	}
}