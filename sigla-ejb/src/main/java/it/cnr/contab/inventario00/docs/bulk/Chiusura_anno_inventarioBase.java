/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class Chiusura_anno_inventarioBase extends Chiusura_anno_inventarioKey implements Keyed {
//    ESERCIZIO_VOCE DECIMAL(5,0) NOT NULL
	private Integer esercizioVoce;
 
//    CD_VOCE_EP VARCHAR(20) NOT NULL
	private String cdVoceEp;
 
//    CD_TIPO_AMMORTAMENTO VARCHAR(10) NOT NULL
	private String cdTipoAmmortamento;
 
//    TI_AMMORTAMENTO CHAR(1) NOT NULL
	private String tiAmmortamento;
 
//    ESERCIZIO_COMPETENZA DECIMAL(5,0) NOT NULL
	private Integer esercizioCompetenza;
 
//    VALORE_ANNO_PREC DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal valoreAnnoPrec;
 
//    VALORE_INCREMENTO DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal valoreIncremento;
 
//    VALORE_DECREMENTO DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal valoreDecremento;
 
//    QUOTA_AMMORTAMENTO DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal quotaAmmortamento;
 
//    TOTALE_AMMORTAMENTO_ALIENATI DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal totaleAmmortamentoAlienati;
 
//    QUOTA_AMMORTAMENTO_ANNO_PREC DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal quotaAmmortamentoAnnoPrec;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_INVENTARIO
	 **/
	public Chiusura_anno_inventarioBase() {
		super();
	}
	public Chiusura_anno_inventarioBase(Long pgChiusura, Integer anno, String tipoChiusura, String cdCategoriaGruppo) {
		super(pgChiusura, anno, tipoChiusura, cdCategoriaGruppo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio voce]
	 **/
	public Integer getEsercizioVoce() {
		return esercizioVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio voce]
	 **/
	public void setEsercizioVoce(Integer esercizioVoce)  {
		this.esercizioVoce=esercizioVoce;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public String getCdVoceEp() {
		return cdVoceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.cdVoceEp=cdVoceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Tipo Ammortamento]
	 **/
	public String getCdTipoAmmortamento() {
		return cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Tipo Ammortamento]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.cdTipoAmmortamento=cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipo Ammortamento]
	 **/
	public String getTiAmmortamento() {
		return tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipo Ammortamento]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.tiAmmortamento=tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio Competenza]
	 **/
	public Integer getEsercizioCompetenza() {
		return esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio Competenza]
	 **/
	public void setEsercizioCompetenza(Integer esercizioCompetenza)  {
		this.esercizioCompetenza=esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Valori riferiti all'anno precedente]
	 **/
	public java.math.BigDecimal getValoreAnnoPrec() {
		return valoreAnnoPrec;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Valori riferiti all'anno precedente]
	 **/
	public void setValoreAnnoPrec(java.math.BigDecimal valoreAnnoPrec)  {
		this.valoreAnnoPrec=valoreAnnoPrec;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Nuove acquisizioni e incremento valori bene]
	 **/
	public java.math.BigDecimal getValoreIncremento() {
		return valoreIncremento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Nuove acquisizioni e incremento valori bene]
	 **/
	public void setValoreIncremento(java.math.BigDecimal valoreIncremento)  {
		this.valoreIncremento=valoreIncremento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Dismissioni e decremento valori bene]
	 **/
	public java.math.BigDecimal getValoreDecremento() {
		return valoreDecremento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Dismissioni e decremento valori bene]
	 **/
	public void setValoreDecremento(java.math.BigDecimal valoreDecremento)  {
		this.valoreDecremento=valoreDecremento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Totale ammortamento per l'anno]
	 **/
	public java.math.BigDecimal getQuotaAmmortamento() {
		return quotaAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Totale ammortamento per l'anno]
	 **/
	public void setQuotaAmmortamento(java.math.BigDecimal quotaAmmortamento)  {
		this.quotaAmmortamento=quotaAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Totale degli ammortamenti dei beni alienati nell'anno]
	 **/
	public java.math.BigDecimal getTotaleAmmortamentoAlienati() {
		return totaleAmmortamentoAlienati;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Totale degli ammortamenti dei beni alienati nell'anno]
	 **/
	public void setTotaleAmmortamentoAlienati(java.math.BigDecimal totaleAmmortamentoAlienati)  {
		this.totaleAmmortamentoAlienati=totaleAmmortamentoAlienati;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Totale ammortamento anno precedente]
	 **/
	public java.math.BigDecimal getQuotaAmmortamentoAnnoPrec() {
		return quotaAmmortamentoAnnoPrec;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Totale ammortamento anno precedente]
	 **/
	public void setQuotaAmmortamentoAnnoPrec(java.math.BigDecimal quotaAmmortamentoAnnoPrec)  {
		this.quotaAmmortamentoAnnoPrec=quotaAmmortamentoAnnoPrec;
	}
}