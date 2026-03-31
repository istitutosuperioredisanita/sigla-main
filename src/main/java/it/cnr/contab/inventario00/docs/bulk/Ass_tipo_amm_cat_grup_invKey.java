/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Ass_tipo_amm_cat_grup_invKey extends OggettoBulk implements KeyedPersistent {
	private String cdTipoAmmortamento;
	private String tiAmmortamento;
	private String cdCategoriaGruppo;
	private Integer esercizioCompetenza;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_TIPO_AMM_CAT_GRUP_INV
	 **/
	public Ass_tipo_amm_cat_grup_invKey() {
		super();
	}
	public Ass_tipo_amm_cat_grup_invKey(String cdTipoAmmortamento, String tiAmmortamento, String cdCategoriaGruppo, Integer esercizioCompetenza) {
		super();
		this.cdTipoAmmortamento=cdTipoAmmortamento;
		this.tiAmmortamento=tiAmmortamento;
		this.cdCategoriaGruppo=cdCategoriaGruppo;
		this.esercizioCompetenza=esercizioCompetenza;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Ass_tipo_amm_cat_grup_invKey)) return false;
		Ass_tipo_amm_cat_grup_invKey k = (Ass_tipo_amm_cat_grup_invKey) o;
		if (!compareKey(getCdTipoAmmortamento(), k.getCdTipoAmmortamento())) return false;
		if (!compareKey(getTiAmmortamento(), k.getTiAmmortamento())) return false;
		if (!compareKey(getCdCategoriaGruppo(), k.getCdCategoriaGruppo())) return false;
		if (!compareKey(getEsercizioCompetenza(), k.getEsercizioCompetenza())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdTipoAmmortamento());
		i = i + calculateKeyHashCode(getTiAmmortamento());
		i = i + calculateKeyHashCode(getCdCategoriaGruppo());
		i = i + calculateKeyHashCode(getEsercizioCompetenza());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice univoco del tipo ammortametno]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.cdTipoAmmortamento=cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice univoco del tipo ammortametno]
	 **/
	public String getCdTipoAmmortamento() {
		return cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipologia dell'ammortamento.]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.tiAmmortamento=tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipologia dell'ammortamento.]
	 **/
	public String getTiAmmortamento() {
		return tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public void setEsercizioCompetenza(Integer esercizioCompetenza)  {
		this.esercizioCompetenza=esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public Integer getEsercizioCompetenza() {
		return esercizioCompetenza;
	}
}