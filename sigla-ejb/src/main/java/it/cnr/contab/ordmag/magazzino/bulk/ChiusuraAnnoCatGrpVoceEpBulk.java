/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/03/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class ChiusuraAnnoCatGrpVoceEpBulk extends ChiusuraAnnoCatGrpVoceEpBase {
	/**
	 * [CHIUSURA_ANNO ]
	 **/
	private ChiusuraAnnoBulk chiusuraAnno =  new ChiusuraAnnoBulk();
	/**
	 * [CATEGORIA_GRUPPO_INVENT ]
	 **/
	private Categoria_gruppo_inventBulk categoriaGruppoInvent =  new Categoria_gruppo_inventBulk();
	/**
	 * [VOCE_EP ]
	 **/
	private Voce_epBulk voceEp =  new Voce_epBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_CATGRP_VOCE_EP
	 **/
	public ChiusuraAnnoCatGrpVoceEpBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_CATGRP_VOCE_EP
	 **/
	public ChiusuraAnnoCatGrpVoceEpBulk(Integer pgChiusura, Integer anno, String tipoChiusura, String cdCategoriaGruppo, Integer esercizio, String cdVoceEp) {
		super(pgChiusura, anno, tipoChiusura, cdCategoriaGruppo, esercizio, cdVoceEp);
		setChiusuraAnno( new ChiusuraAnnoBulk(pgChiusura,anno,tipoChiusura) );
		setCategoriaGruppoInvent( new Categoria_gruppo_inventBulk(cdCategoriaGruppo) );
		setVoceEp( new Voce_epBulk(cdVoceEp,esercizio) );
	}
	public ChiusuraAnnoBulk getChiusuraAnno() {
		return chiusuraAnno;
	}
	public void setChiusuraAnno(ChiusuraAnnoBulk chiusuraAnno)  {
		this.chiusuraAnno=chiusuraAnno;
	}
	public Categoria_gruppo_inventBulk getCategoriaGruppoInvent() {
		return categoriaGruppoInvent;
	}
	public void setCategoriaGruppoInvent(Categoria_gruppo_inventBulk categoriaGruppoInvent)  {
		this.categoriaGruppoInvent=categoriaGruppoInvent;
	}
	public Voce_epBulk getVoceEp() {
		return voceEp;
	}
	public void setVoceEp(Voce_epBulk voceEp)  {
		this.voceEp=voceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo record chiusura]
	 **/
	public Integer getPgChiusura() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getPgChiusura();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo record chiusura]
	 **/
	public void setPgChiusura(Integer pgChiusura)  {
		this.getChiusuraAnno().setPgChiusura(pgChiusura);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di chiusura]
	 **/
	public Integer getAnno() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getAnno();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di chiusura]
	 **/
	public void setAnno(Integer anno)  {
		this.getChiusuraAnno().setAnno(anno);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [M=Magazzino; I=Inventario]
	 **/
	public String getTipoChiusura() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getTipoChiusura();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [M=Magazzino; I=Inventario]
	 **/
	public void setTipoChiusura(String tipoChiusura)  {
		this.getChiusuraAnno().setTipoChiusura(tipoChiusura);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice categoria/gruppo]
	 **/
	public String getCdCategoriaGruppo() {
		Categoria_gruppo_inventBulk categoriaGruppoInvent = this.getCategoriaGruppoInvent();
		if (categoriaGruppoInvent == null)
			return null;
		return getCategoriaGruppoInvent().getCd_categoria_gruppo();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice categoria/gruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.getCategoriaGruppoInvent().setCd_categoria_gruppo(cdCategoriaGruppo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio.]
	 **/
	public Integer getEsercizio() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio.]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getVoceEp().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public String getCdVoceEp() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getCd_voce_ep();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.getVoceEp().setCd_voce_ep(cdVoceEp);
	}
}