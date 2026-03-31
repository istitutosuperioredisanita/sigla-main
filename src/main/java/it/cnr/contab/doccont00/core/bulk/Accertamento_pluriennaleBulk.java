/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/09/2021
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;

import java.util.Iterator;

public class Accertamento_pluriennaleBulk extends Accertamento_pluriennaleBase {
	/**
	 * [ACCERTAMENTO ]
	 **/
	private AccertamentoBulk accertamento =  new AccertamentoBulk();
	private AccertamentoBulk accertamentoRif =  new AccertamentoBulk();

	protected BulkList<Accertamento_pluriennale_voceBulk> righeVoceColl = new BulkList<Accertamento_pluriennale_voceBulk>();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCERTAMENTO_PLURIENNALE
	 **/
	public Accertamento_pluriennaleBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCERTAMENTO_PLURIENNALE
	 **/
	public Accertamento_pluriennaleBulk(String cdCds, Integer esercizio, Integer esercizioOriginale, Long pgAccertamento, Integer anno) {
		super(cdCds, esercizio, esercizioOriginale, pgAccertamento, anno);
		setAccertamento( new AccertamentoBulk(cdCds,esercizio,esercizioOriginale,pgAccertamento) );
	}

	public BulkCollection[] getBulkLists() {
		// Metti solo le liste di oggetti che devono essere resi persistenti
		return new it.cnr.jada.bulk.BulkCollection[] {
				righeVoceColl };
	}
	public AccertamentoBulk getAccertamento() {
		return accertamento;
	}
	public void setAccertamento(AccertamentoBulk accertamento)  {
		this.accertamento=accertamento;
	}

	public AccertamentoBulk getAccertamentoRif() {
		return accertamentoRif;
	}

	public void setAccertamentoRif(AccertamentoBulk accertamentoRif) {
		this.accertamentoRif = accertamentoRif;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'accertamento - chiave primaria]
	 **/
	public String getCdCds() {
		AccertamentoBulk accertamento = this.getAccertamento();
		if (accertamento == null)
			return null;
		it.cnr.contab.config00.sto.bulk.CdsBulk cds = accertamento.getCds();
		if (cds == null)
			return null;
		return cds.getCd_unita_organizzativa();

	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'accertamento - chiave primaria]
	 **/
	public void setCdCds(String cdCds)  {
		this.getAccertamento().getCds().setCd_unita_organizzativa(cdCds);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'accertamento - chiave primaria]
	 **/
	public Integer getEsercizio() {
		AccertamentoBulk accertamento = this.getAccertamento();
		if (accertamento == null)
			return null;
		return getAccertamento().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'accertamento - chiave primaria]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getAccertamento().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'accertamento - chiave primaria]
	 **/
	public Integer getEsercizioOriginale() {
		AccertamentoBulk accertamento = this.getAccertamento();
		if (accertamento == null)
			return null;
		return getAccertamento().getEsercizio_originale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'accertamento - chiave primaria]
	 **/
	public void setEsercizioOriginale(Integer esercizioOriginale)  {
		this.getAccertamento().setEsercizio_originale(esercizioOriginale);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'accertamento - chiave primaria]
	 **/
	public Long getPgAccertamento() {
		AccertamentoBulk accertamento = this.getAccertamento();
		if (accertamento == null)
			return null;
		return getAccertamento().getPg_accertamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'accertamento - chiave primaria]
	 **/
	public void setPgAccertamento(Long pgAccertamento)  {
		this.getAccertamento().setPg_accertamento(pgAccertamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Cds dell'accertamento di riferimento]
	 **/
	public String getCdCdsRif() {
		AccertamentoBulk accertamento = this.getAccertamentoRif();
		if (accertamento == null)
			return null;
		it.cnr.contab.config00.sto.bulk.CdsBulk cds = accertamento.getCds();
		if (cds == null)
			return null;
		return cds.getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Cds dell'accertamento di riferimento]
	 **/
	public void setCdCdsRif(String cdCdsRif)  {
		this.getAccertamentoRif().getCds().setCd_unita_organizzativa(cdCdsRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio dell'accertamento di riferimento]
	 **/
	public Integer getEsercizioRif() {
		AccertamentoBulk accertamento = this.getAccertamentoRif();
		if (accertamento == null)
			return null;
		return getAccertamentoRif().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio dell'accertamento di riferimento]
	 **/
	public void setEsercizioRif(Integer esercizioRif)  {
		this.getAccertamentoRif().setEsercizio(esercizioRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Originale dell'accertamento di riferimento]
	 **/
	public Integer getEsercizioOriginaleRif() {
		AccertamentoBulk accertamento = this.getAccertamentoRif();
		if (accertamento == null)
			return null;
		return getAccertamentoRif().getEsercizio_originale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Originale dell'accertamento di riferimento]
	 **/
	public void setEsercizioOriginaleRif(Integer esercizioOriginaleRif)  {
		this.getAccertamentoRif().setEsercizio_originale(esercizioOriginaleRif);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero dell'accertamento di riferimento]
	 **/
	public Long getPgAccertamentoRif() {
		AccertamentoBulk accertamento = this.getAccertamentoRif();
		if (accertamento == null)
			return null;
		return getAccertamentoRif().getPg_accertamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero dell'accertamento di riferimento]
	 **/
	public void setPgAccertamentoRif(Long pgAccertamentoRif)  {
		this.getAccertamentoRif().setPg_accertamento(pgAccertamentoRif);
	}

	public BulkList<Accertamento_pluriennale_voceBulk> getRigheVoceColl() {
		return righeVoceColl;
	}

	public void setRigheVoceColl(BulkList<Accertamento_pluriennale_voceBulk> righeVoceColl) {
		this.righeVoceColl = righeVoceColl;
	}

	public Accertamento_pluriennaleBulk clone(AccertamentoBulk accertamento, it.cnr.jada.action.ActionContext context){
		Accertamento_pluriennaleBulk nuovo = null;
		try {
			nuovo = (Accertamento_pluriennaleBulk)getClass().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		nuovo.setAccertamentoRif(this.getAccertamentoRif());
		nuovo.setAccertamento(accertamento);
		nuovo.setAnno(this.getAnno());
		nuovo.setImporto(this.getImporto());
		nuovo.setCrudStatus(TO_BE_CREATED);
		return nuovo;
	}


	private BulkList<Accertamento_pluriennale_voceBulk> clonaRigheVoceColl(Accertamento_pluriennaleBulk accertamento,it.cnr.jada.action.ActionContext context){

		if ( this.getRigheVoceColl()==null || this.getRigheVoceColl().isEmpty())
			return this.getRigheVoceColl();

		BulkList<Accertamento_pluriennale_voceBulk> pluriennaliVoce= new BulkList<Accertamento_pluriennale_voceBulk>();

		for ( Accertamento_pluriennale_voceBulk pv:this.getRigheVoceColl()){
			Accertamento_pluriennale_voceBulk n = (Accertamento_pluriennale_voceBulk) pv.clone();
			pluriennaliVoce.add( n);
		}
		return pluriennaliVoce;
	}

	public int addToRigheVoceCollBulkList(Accertamento_pluriennale_voceBulk dett){

		getRigheVoceColl().add(dett);
		return getRigheVoceColl().size()-1;
	}

	public Accertamento_pluriennale_voceBulk removeFromRigheVoceCollBulkList(int index) {
		Accertamento_pluriennale_voceBulk dett = (Accertamento_pluriennale_voceBulk)getRigheVoceColl().remove(index);
		dett.setToBeDeleted();
		return dett;
	}
	public void setToBeDeleted() {
		super.setToBeDeleted();
		for (Iterator i = righeVoceColl.iterator(); i.hasNext(); )
			((Accertamento_pluriennale_voceBulk) i.next()).setToBeDeleted();
		for (Iterator i = righeVoceColl.deleteIterator(); i.hasNext(); )
			((Accertamento_pluriennale_voceBulk) i.next()).setToBeDeleted();

	}

}