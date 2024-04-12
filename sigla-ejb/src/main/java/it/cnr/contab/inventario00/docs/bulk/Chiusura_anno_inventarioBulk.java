/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
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
}