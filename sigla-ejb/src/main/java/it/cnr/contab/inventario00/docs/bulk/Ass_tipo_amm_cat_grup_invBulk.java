/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class Ass_tipo_amm_cat_grup_invBulk extends Ass_tipo_amm_cat_grup_invBase {
	/**
	 * [TIPO_AMMORTAMENTO ]
	 **/
	private Tipo_ammortamentoBulk tipoAmmortamento =  new Tipo_ammortamentoBulk();
	/**
	 * [CATEGORIA_GRUPPO_INVENT ]
	 **/
	private Categoria_gruppo_inventBulk categoriaGruppoInvent =  new Categoria_gruppo_inventBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_TIPO_AMM_CAT_GRUP_INV
	 **/
	public Ass_tipo_amm_cat_grup_invBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_TIPO_AMM_CAT_GRUP_INV
	 **/
	public Ass_tipo_amm_cat_grup_invBulk(String cdTipoAmmortamento, String tiAmmortamento, String cdCategoriaGruppo, Integer esercizioCompetenza) {
		super(cdTipoAmmortamento, tiAmmortamento, cdCategoriaGruppo, esercizioCompetenza);
		setTipoAmmortamento( new Tipo_ammortamentoBulk(cdTipoAmmortamento,tiAmmortamento) );
		setCategoriaGruppoInvent( new Categoria_gruppo_inventBulk(cdCategoriaGruppo) );
	}
	public Tipo_ammortamentoBulk getTipoAmmortamento() {
		return tipoAmmortamento;
	}
	public void setTipoAmmortamento(Tipo_ammortamentoBulk tipoAmmortamento)  {
		this.tipoAmmortamento=tipoAmmortamento;
	}
	public Categoria_gruppo_inventBulk getCategoriaGruppoInvent() {
		return categoriaGruppoInvent;
	}
	public void setCategoriaGruppoInvent(Categoria_gruppo_inventBulk categoriaGruppoInvent)  {
		this.categoriaGruppoInvent=categoriaGruppoInvent;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice univoco del tipo ammortametno]
	 **/
	public String getCdTipoAmmortamento() {
		Tipo_ammortamentoBulk tipoAmmortamento = this.getTipoAmmortamento();
		if (tipoAmmortamento == null)
			return null;
		return getTipoAmmortamento().getCd_tipo_ammortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice univoco del tipo ammortametno]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.getTipoAmmortamento().setCd_tipo_ammortamento(cdTipoAmmortamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipologia dell'ammortamento.]
	 **/
	public String getTiAmmortamento() {
		Tipo_ammortamentoBulk tipoAmmortamento = this.getTipoAmmortamento();
		if (tipoAmmortamento == null)
			return null;
		return getTipoAmmortamento().getTi_ammortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipologia dell'ammortamento.]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.getTipoAmmortamento().setTi_ammortamento(tiAmmortamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public String getCdCategoriaGruppo() {
		Categoria_gruppo_inventBulk categoriaGruppoInvent = this.getCategoriaGruppoInvent();
		if (categoriaGruppoInvent == null)
			return null;
		return getCategoriaGruppoInvent().getCd_categoria_gruppo();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.getCategoriaGruppoInvent().setCd_categoria_gruppo(cdCategoriaGruppo);
	}
}