/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class Ammortamento_bene_invBase extends Ammortamento_bene_invKey implements Keyed {
//    CD_TIPO_AMMORTAMENTO VARCHAR(10) NOT NULL
	private String cdTipoAmmortamento;
 
//    TI_AMMORTAMENTO CHAR(1) NOT NULL
	private String tiAmmortamento;
 
//    CD_CATEGORIA_GRUPPO VARCHAR(10) NOT NULL
	private String cdCategoriaGruppo;
 
//    ESERCIZIO_COMPETENZA DECIMAL(5,0) NOT NULL
	private Integer esercizioCompetenza;
 
//    IMPONIBILE_AMMORTAMENTO DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal imponibileAmmortamento;
 
//    IM_MOVIMENTO_AMMORT DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal imMovimentoAmmort;
 
//    PERC_AMMORTAMENTO DECIMAL(5,2) NOT NULL
	private java.math.BigDecimal percAmmortamento;
 
//    NUMERO_ANNI DECIMAL(5,0)
	private Integer numeroAnni;
 
//    NUMERO_ANNO DECIMAL(5,0)
	private Integer numeroAnno;
 
//    PERC_PRIMO_ANNO DECIMAL(5,2)
	private java.math.BigDecimal percPrimoAnno;
 
//    PERC_SUCCESSIVI DECIMAL(5,2)
	private java.math.BigDecimal percSuccessivi;
 
//    CD_CDS_UBICAZIONE VARCHAR(30)
	private String cdCdsUbicazione;
 
//    CD_UO_UBICAZIONE VARCHAR(30)
	private String cdUoUbicazione;
 
//    FL_STORNO CHAR(1)
	private Boolean flStorno;
 
//    PG_BUONO_S DECIMAL(38,0)
	private Long pgBuonoS;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: AMMORTAMENTO_BENE_INV
	 **/
	public Ammortamento_bene_invBase() {
		super();
	}
	public Ammortamento_bene_invBase(Long pgInventario, Long nrInventario, Long progressivo, Integer esercizio, Integer pgRiga) {
		super(pgInventario, nrInventario, progressivo, esercizio, pgRiga);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del tipo di ammortamento]
	 **/
	public String getCdTipoAmmortamento() {
		return cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del tipo di ammortamento]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.cdTipoAmmortamento=cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipologia  dell'ammortamento.]
	 **/
	public String getTiAmmortamento() {
		return tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipologia  dell'ammortamento.]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.tiAmmortamento=tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo della categoria-gruppo inventariale o di gestione magazzino di riferimento per un dato bene o servizio. La codifica è organizzata in una struttura ad albero  su due livelli.]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public Integer getEsercizioCompetenza() {
		return esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di validita dell'associazione tra categoria e tipo ammortamento]
	 **/
	public void setEsercizioCompetenza(Integer esercizioCompetenza)  {
		this.esercizioCompetenza=esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Valore su cui viene applicato l'ammortamento, e che rappresenta il totale da ammorttizzare]
	 **/
	public java.math.BigDecimal getImponibileAmmortamento() {
		return imponibileAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Valore su cui viene applicato l'ammortamento, e che rappresenta il totale da ammorttizzare]
	 **/
	public void setImponibileAmmortamento(java.math.BigDecimal imponibileAmmortamento)  {
		this.imponibileAmmortamento=imponibileAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo relativo al movimento di ammortamento]
	 **/
	public java.math.BigDecimal getImMovimentoAmmort() {
		return imMovimentoAmmort;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo relativo al movimento di ammortamento]
	 **/
	public void setImMovimentoAmmort(java.math.BigDecimal imMovimentoAmmort)  {
		this.imMovimentoAmmort=imMovimentoAmmort;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Percentuale applicata al campo IMPONIBILE_AMMORTAMENTO per ricavare IM_MOVIMENTO_AMMORT]
	 **/
	public java.math.BigDecimal getPercAmmortamento() {
		return percAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Percentuale applicata al campo IMPONIBILE_AMMORTAMENTO per ricavare IM_MOVIMENTO_AMMORT]
	 **/
	public void setPercAmmortamento(java.math.BigDecimal percAmmortamento)  {
		this.percAmmortamento=percAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numeroAnni]
	 **/
	public Integer getNumeroAnni() {
		return numeroAnni;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeroAnni]
	 **/
	public void setNumeroAnni(Integer numeroAnni)  {
		this.numeroAnni=numeroAnni;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numeroAnno]
	 **/
	public Integer getNumeroAnno() {
		return numeroAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeroAnno]
	 **/
	public void setNumeroAnno(Integer numeroAnno)  {
		this.numeroAnno=numeroAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [percPrimoAnno]
	 **/
	public java.math.BigDecimal getPercPrimoAnno() {
		return percPrimoAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [percPrimoAnno]
	 **/
	public void setPercPrimoAnno(java.math.BigDecimal percPrimoAnno)  {
		this.percPrimoAnno=percPrimoAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [percSuccessivi]
	 **/
	public java.math.BigDecimal getPercSuccessivi() {
		return percSuccessivi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [percSuccessivi]
	 **/
	public void setPercSuccessivi(java.math.BigDecimal percSuccessivi)  {
		this.percSuccessivi=percSuccessivi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCdsUbicazione]
	 **/
	public String getCdCdsUbicazione() {
		return cdCdsUbicazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCdsUbicazione]
	 **/
	public void setCdCdsUbicazione(String cdCdsUbicazione)  {
		this.cdCdsUbicazione=cdCdsUbicazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUoUbicazione]
	 **/
	public String getCdUoUbicazione() {
		return cdUoUbicazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUoUbicazione]
	 **/
	public void setCdUoUbicazione(String cdUoUbicazione)  {
		this.cdUoUbicazione=cdUoUbicazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Indica se è una quota di storno, che non deve essere toccata dalle procedure di calcolo ammortamento.]
	 **/
	public Boolean getFlStorno() {
		return flStorno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Indica se è una quota di storno, che non deve essere toccata dalle procedure di calcolo ammortamento.]
	 **/
	public void setFlStorno(Boolean flStorno)  {
		this.flStorno=flStorno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo buono scarico che genera quota di storno]
	 **/
	public Long getPgBuonoS() {
		return pgBuonoS;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo buono scarico che genera quota di storno]
	 **/
	public void setPgBuonoS(Long pgBuonoS)  {
		this.pgBuonoS=pgBuonoS;
	}
}