/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class Ammortamento_bene_invBulk extends Ammortamento_bene_invBase {
	/**
	 * [INVENTARIO_BENI ]
	 **/
	private Inventario_beniBulk inventarioBeni =  new Inventario_beniBulk();
	/**
	 * [ASS_TIPO_AMM_CAT_GRUP_INV ]
	 **/

	private Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv =  new Ass_tipo_amm_cat_grup_invBulk();
	/**
	 * [UNITA_ORGANIZZATIVA ]
	 **/
	private Unita_organizzativaBulk unitaOrganizzativa =  new Unita_organizzativaBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: AMMORTAMENTO_BENE_INV
	 **/
	public Ammortamento_bene_invBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: AMMORTAMENTO_BENE_INV
	 **/
	public Ammortamento_bene_invBulk(Long pgInventario, Long nrInventario, Long progressivo, Integer esercizio, Integer pgRiga) {
		super(pgInventario, nrInventario, progressivo, esercizio, pgRiga);
		setInventarioBeni( new Inventario_beniBulk(pgInventario,nrInventario,progressivo) );
	}
	public Inventario_beniBulk getInventarioBeni() {
		return inventarioBeni;
	}
	public void setInventarioBeni(Inventario_beniBulk inventarioBeni)  {
		this.inventarioBeni=inventarioBeni;
	}
	public Ass_tipo_amm_cat_grup_invBulk getAssTipoAmmCatGrupInv() {
		return assTipoAmmCatGrupInv;
	}
	public void setAssTipoAmmCatGrupInv(Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv)  {
		this.assTipoAmmCatGrupInv=assTipoAmmCatGrupInv;
	}
	public Unita_organizzativaBulk getUnitaOrganizzativa() {
		return unitaOrganizzativa;
	}
	public void setUnitaOrganizzativa(Unita_organizzativaBulk unitaOrganizzativa)  {
		this.unitaOrganizzativa=unitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del'inventario]
	 **/
	public Long getPgInventario() {
		Inventario_beniBulk inventarioBeni = this.getInventarioBeni();
		if (inventarioBeni == null)
			return null;
		return getInventarioBeni().getPg_inventario();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del'inventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.getInventarioBeni().setPg_inventario(pgInventario);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public Long getNrInventario() {
		Inventario_beniBulk inventarioBeni = this.getInventarioBeni();
		if (inventarioBeni == null)
			return null;
		return getInventarioBeni().getNr_inventario();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public void setNrInventario(Long nrInventario)  {
		this.getInventarioBeni().setNr_inventario(nrInventario);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene accesorio all'interno di un inventario e una volta che risulti noto il bene primario]
	 **/
	public Long getProgressivo() {
		Inventario_beniBulk inventarioBeni = this.getInventarioBeni();
		if (inventarioBeni == null)
			return null;
		return getInventarioBeni().getProgressivo();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene accesorio all'interno di un inventario e una volta che risulti noto il bene primario]
	 **/
	public void setProgressivo(Long progressivo)  {
		this.getInventarioBeni().setProgressivo(progressivo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del tipo di ammortamento]
	 **/
	public String getCdTipoAmmortamento() {
		Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv = this.getAssTipoAmmCatGrupInv();
		if (assTipoAmmCatGrupInv == null)
			return null;
		return getAssTipoAmmCatGrupInv().getCdTipoAmmortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del tipo di ammortamento]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.getAssTipoAmmCatGrupInv().setCdTipoAmmortamento(cdTipoAmmortamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipologia  dell'ammortamento.]
	 **/
	public String getTiAmmortamento() {
		Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv = this.getAssTipoAmmCatGrupInv();
		if (assTipoAmmCatGrupInv == null)
			return null;
		return getAssTipoAmmCatGrupInv().getTiAmmortamento();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipologia  dell'ammortamento.]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.getAssTipoAmmCatGrupInv().setTiAmmortamento(tiAmmortamento);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public String getCdCategoriaGruppo() {
		Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv = this.getAssTipoAmmCatGrupInv();
		if (assTipoAmmCatGrupInv == null)
			return null;
		return getAssTipoAmmCatGrupInv().getCdCategoriaGruppo();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.getAssTipoAmmCatGrupInv().setCdCategoriaGruppo(cdCategoriaGruppo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public Integer getEsercizioCompetenza() {
		Ass_tipo_amm_cat_grup_invBulk assTipoAmmCatGrupInv = this.getAssTipoAmmCatGrupInv();
		if (assTipoAmmCatGrupInv == null)
			return null;
		return getAssTipoAmmCatGrupInv().getEsercizioCompetenza();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public void setEsercizioCompetenza(Integer esercizioCompetenza)  {
		this.getAssTipoAmmCatGrupInv().setEsercizioCompetenza(esercizioCompetenza);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCdsUbicazione]
	 **/
	public String getCdCdsUbicazione() {
		Unita_organizzativaBulk unitaOrganizzativa = this.getUnitaOrganizzativa();
		if (unitaOrganizzativa == null)
			return null;
		return getUnitaOrganizzativa().getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCdsUbicazione]
	 **/
	public void setCdCdsUbicazione(String cdCdsUbicazione)  {
		this.getUnitaOrganizzativa().setCd_unita_organizzativa(cdCdsUbicazione);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUoUbicazione]
	 **/
	public String getCdUoUbicazione() {
		Unita_organizzativaBulk unitaOrganizzativa = this.getUnitaOrganizzativa();
		if (unitaOrganizzativa == null)
			return null;
		return getUnitaOrganizzativa().getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUoUbicazione]
	 **/
	public void setCdUoUbicazione(String cdUoUbicazione)  {
		this.getUnitaOrganizzativa().setCd_unita_organizzativa(cdUoUbicazione);
	}
}