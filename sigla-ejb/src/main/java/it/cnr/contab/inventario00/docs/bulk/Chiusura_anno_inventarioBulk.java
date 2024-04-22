/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.CRUDBP;

public class Chiusura_anno_inventarioBulk extends Chiusura_anno_inventarioBase {
	/**
	 * [CATEGORIA_GRUPPO_INVENT ]
	 **/
	private Categoria_gruppo_inventBulk categoriaGruppoInvent =  new Categoria_gruppo_inventBulk();
	/**
	 * [VOCE_EP ]
	 **/
	private Voce_epBulk voceEp =  new Voce_epBulk();
	/**
	 * [TIPO_AMMORTAMENTO ]
	 **/
	private Tipo_ammortamentoBulk tipoAmmortamento =  new Tipo_ammortamentoBulk();

	private it.cnr.jada.util.OrderedHashtable anniList = new it.cnr.jada.util.OrderedHashtable();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_INVENTARIO
	 **/
	public Chiusura_anno_inventarioBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_INVENTARIO
	 **/
	public Chiusura_anno_inventarioBulk(Integer pgChiusura, Integer anno, String tipoChiusura, String cdCategoriaGruppo) {
		super(pgChiusura, anno, tipoChiusura, cdCategoriaGruppo);
		setCategoriaGruppoInvent( new Categoria_gruppo_inventBulk(cdCategoriaGruppo) );
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
	public Tipo_ammortamentoBulk getTipoAmmortamento() {
		return tipoAmmortamento;
	}
	public void setTipoAmmortamento(Tipo_ammortamentoBulk tipoAmmortamento)  {
		this.tipoAmmortamento=tipoAmmortamento;
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
	 * Restituisce il valore di: [Esercizio voce]
	 **/
	public Integer getEsercizioVoce() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio voce]
	 **/
	public void setEsercizioVoce(Integer esercizioVoce)  {
		this.getVoceEp().setEsercizio(esercizioVoce);
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
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Tipo Ammortamento]
	 **/
	public String getCdTipoAmmortamento() {
		Tipo_ammortamentoBulk tipoAmmortamento = this.getTipoAmmortamento();
		if (tipoAmmortamento == null)
			return null;
		return getTipoAmmortamento().getCd_tipo_ammortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Tipo Ammortamento]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.getTipoAmmortamento().setCd_tipo_ammortamento(cdTipoAmmortamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipo Ammortamento]
	 **/
	public String getTiAmmortamento() {
		Tipo_ammortamentoBulk tipoAmmortamento = this.getTipoAmmortamento();
		if (tipoAmmortamento == null)
			return null;
		return getTipoAmmortamento().getTi_ammortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipo Ammortamento]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.getTipoAmmortamento().setTi_ammortamento(tiAmmortamento);
	}

	public OrderedHashtable getAnniList() {
		return anniList;
	}

	public void setAnniList(OrderedHashtable anniList) {
		this.anniList = anniList;
	}
	public void caricaAnniList(ActionContext actioncontext) {
		caricaAnniList(actioncontext.getUserContext());
	}
	public void caricaAnniList(UserContext usercontext) {
		for (int i = CNRUserContext.getEsercizio(usercontext).intValue(); i>=2023; i--)
			getAnniList().put(new Integer(i), new Integer(i));
	}


	@Override
	public OggettoBulk initializeForPrint(BulkBP bulkBP, ActionContext actioncontext) {
		caricaAnniList(actioncontext);
		return super.initializeForPrint(bulkBP, actioncontext);
	}
	public OggettoBulk initialize(CRUDBP crudbp, ActionContext actioncontext) {
		super.initialize(crudbp, actioncontext);
		caricaAnniList(actioncontext);
		return this;
	}
	public OggettoBulk initializeForEdit(CRUDBP crudbp, ActionContext actioncontext) {
		caricaAnniList(actioncontext);
		return super.initializeForEdit(crudbp, actioncontext);
	}
	public void validate() throws it.cnr.jada.bulk.ValidationException {

		if (getAnno() == null)
			throw new it.cnr.jada.bulk.ValidationException("Imposta l'anno di chiusura!");


	}
}