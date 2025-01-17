/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/09/2021
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;

import java.util.Iterator;

public class Obbligazione_pluriennaleBulk extends Obbligazione_pluriennaleBase {
	/**
	 * [OBBLIGAZIONE ]
	 **/
	private ObbligazioneBulk obbligazione =  new ObbligazioneBulk();
	private ObbligazioneBulk obbligazioneRif =  new ObbligazioneBulk();

	protected BulkList<Obbligazione_pluriennale_voceBulk> righeVoceColl = new BulkList<Obbligazione_pluriennale_voceBulk>();

	public BulkCollection[] getBulkLists() {
		// Metti solo le liste di oggetti che devono essere resi persistenti
		return new it.cnr.jada.bulk.BulkCollection[] {
				righeVoceColl };
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: OBBLIGAZIONE_PLURIENNALE
	 **/
	public Obbligazione_pluriennaleBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: OBBLIGAZIONE_PLURIENNALE
	 **/
	public Obbligazione_pluriennaleBulk(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgObbligazione, Integer anno) {
		super(cdCds, esercizio, esercizioOriginale, pgObbligazione, anno);
		setObbligazione( new ObbligazioneBulk(cdCds,esercizio,esercizioOriginale,pgObbligazione) );
	}
	public ObbligazioneBulk getObbligazione() {
		return obbligazione;
	}
	public void setObbligazione(ObbligazioneBulk obbligazione)  {
		this.obbligazione=obbligazione;
	}

	public ObbligazioneBulk getObbligazioneRif() {return obbligazioneRif;}

	public void setObbligazioneRif(ObbligazioneBulk obbligazioneRif) {this.obbligazioneRif = obbligazioneRif;}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'obbligazione]
	 **/
	public String getCdCds() {
		it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk obbligazione = this.getObbligazione();
		if (obbligazione == null)
			return null;
		it.cnr.contab.config00.sto.bulk.CdsBulk cds = obbligazione.getCds();
		if (cds == null)
			return null;
		return cds.getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'obbligazione]
	 **/
	public void setCdCds(String cdCds)  {
		this.getObbligazione().getCds().setCd_unita_organizzativa(cdCds);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'obbligazione]
	 **/
	public Integer getEsercizio() {
		ObbligazioneBulk obbligazione = this.getObbligazione();
		if (obbligazione == null)
			return null;
		return getObbligazione().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'obbligazione]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getObbligazione().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'obbligazione]
	 **/
	public Integer getEsercizioOriginale() {
		ObbligazioneBulk obbligazione = this.getObbligazione();
		if (obbligazione == null)
			return null;
		return getObbligazione().getEsercizio_originale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'obbligazione]
	 **/
	public void setEsercizioOriginale(Integer esercizioOriginale)  {
		this.getObbligazione().setEsercizio_originale(esercizioOriginale);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'obbligazione]
	 **/
	public Long getPgObbligazione() {
		ObbligazioneBulk obbligazione = this.getObbligazione();
		if (obbligazione == null)
			return null;
		return getObbligazione().getPg_obbligazione();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'obbligazione]
	 **/
	public void setPgObbligazione(Long pgObbligazione)  {
		this.getObbligazione().setPg_obbligazione(pgObbligazione);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'obbligazione di riferimento]
	 **/
	public String getCdCdsRif() {
		it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk obbligazione = this.getObbligazioneRif();
		if (obbligazione == null)
			return null;
		it.cnr.contab.config00.sto.bulk.CdsBulk cds = obbligazione.getCds();
		if (cds == null)
			return null;
		return cds.getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'obbligazione di riferimento]
	 **/
	public void setCdCdsRif(String cdCdsRif)  {
		this.getObbligazioneRif().getCds().setCd_unita_organizzativa(cdCdsRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'obbligazione di riferimento]
	 **/
	public Integer getEsercizioRif() {
		ObbligazioneBulk obbligazione = this.getObbligazioneRif();
		if (obbligazione == null)
			return null;
		return getObbligazioneRif().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'obbligazione di riferimento]
	 **/
	public void setEsercizioRif(Integer esercizioRif)  {
		this.getObbligazioneRif().setEsercizio(esercizioRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'obbligazione di riferimento]
	 **/
	public Integer getEsercizioOriginaleRif() {
		ObbligazioneBulk obbligazione = this.getObbligazioneRif();
		if (obbligazione == null)
			return null;
		return getObbligazioneRif().getEsercizio_originale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'obbligazione di riferimento]
	 **/
	public void setEsercizioOriginaleRif(Integer esercizioOriginaleRif)  {
		this.getObbligazioneRif().setEsercizio_originale(esercizioOriginaleRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'obbligazione di riferimento]
	 **/
	public Long getPgObbligazioneRif() {
		ObbligazioneBulk obbligazione = this.getObbligazioneRif();
		if (obbligazione == null)
			return null;
		return getObbligazioneRif().getPg_obbligazione();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'obbligazione di riferimento]
	 **/
	public void setPgObbligazioneRif(Long pgObbligazioneRif)  {
		this.getObbligazioneRif().setPg_obbligazione(pgObbligazioneRif);
	}

	public BulkList<Obbligazione_pluriennale_voceBulk> getRigheVoceColl() {
		return righeVoceColl;
	}

	public void setRigheVoceColl(BulkList<Obbligazione_pluriennale_voceBulk> righeVoceColl) {
		this.righeVoceColl = righeVoceColl;
	}

	public Obbligazione_pluriennaleBulk clone(ObbligazioneBulk obbligazione, it.cnr.jada.action.ActionContext context){
		Obbligazione_pluriennaleBulk nuovo = null;

		try {
			nuovo = (Obbligazione_pluriennaleBulk)getClass().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		nuovo.setObbligazione(obbligazione);
		nuovo.setAnno(this.getAnno());
		nuovo.setImporto(this.getImporto());
		nuovo.setObbligazioneRif(this.getObbligazioneRif());
		nuovo.setCrudStatus(TO_BE_CREATED);

		return nuovo;
	}
	private BulkList<Obbligazione_pluriennale_voceBulk> clonaObbligazioniPluriennali(Obbligazione_pluriennaleBulk obbligazione,it.cnr.jada.action.ActionContext context){

		if ( this.getRigheVoceColl()==null || this.getRigheVoceColl().isEmpty())
			return this.getRigheVoceColl();

		BulkList<Obbligazione_pluriennale_voceBulk> pluriennaliVoce= new BulkList<Obbligazione_pluriennale_voceBulk>();

		for ( Obbligazione_pluriennale_voceBulk pv:this.getRigheVoceColl()){
			Obbligazione_pluriennale_voceBulk n = (Obbligazione_pluriennale_voceBulk) pv.clone();
			pluriennaliVoce.add( n);
		}
		return pluriennaliVoce;
	}



	public int addToRigheVoceCollBulkList(Obbligazione_pluriennale_voceBulk dett){

		getRigheVoceColl().add(dett);
		return getRigheVoceColl().size()-1;
	}
	public Obbligazione_pluriennale_voceBulk removeFromRigheVoceCollBulkList(int index) {
		Obbligazione_pluriennale_voceBulk dett = (Obbligazione_pluriennale_voceBulk)getRigheVoceColl().remove(index);
		dett.setToBeDeleted();
		return dett;
	}
	public void setToBeDeleted() {
		super.setToBeDeleted();
		for (Iterator i = righeVoceColl.iterator(); i.hasNext(); )
			((Obbligazione_pluriennale_voceBulk) i.next()).setToBeDeleted();
		for (Iterator i = righeVoceColl.deleteIterator(); i.hasNext(); )
			((Obbligazione_pluriennale_voceBulk) i.next()).setToBeDeleted();

	}
}