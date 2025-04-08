/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Keyed;
import it.cnr.jada.persistency.Persistent;

public class V_ammortamento_beni_detBase extends OggettoBulk implements Persistent {

//    TIPORECORD VARCHAR(12)
	private String tiporecord;
 
//    ESERCIZIO_CARICO_BENE DECIMAL(5,0)
	private Integer esercizioCaricoBene;
 
//    PG_INVENTARIO DECIMAL(38,0)
	private Long pgInventario;
 
//    NR_INVENTARIO DECIMAL(38,0)
	private Long nrInventario;
 
//    PROGRESSIVO DECIMAL(38,0)
	private Long progressivo;
 
//    ETICHETTA VARCHAR(50)
	private String etichetta;
 
//    FL_AMMORTAMENTO CHAR(1)
	private Boolean flAmmortamento;
 
//    FL_TOTALMENTE_SCARICATO CHAR(1)
	private Boolean flTotalmenteScaricato;
 
//    TI_AMMORTAMENTO CHAR(1)
	private String tiAmmortamento;
 
//    CD_CATEGORIA_GRUPPO VARCHAR(10)
	private String cdCategoriaGruppo;
 
//    VALORE_INIZIALE DECIMAL(0,-127)
	private java.math.BigDecimal valoreIniziale;
 
//    VALORE_AMMORTIZZATO DECIMAL(0,-127)
	private java.math.BigDecimal valoreAmmortizzato;
 
//    IMPONIBILE_AMMORTAMENTO DECIMAL(0,-127)
	private java.math.BigDecimal imponibileAmmortamento;
 
//    VARIAZIONE_PIU DECIMAL(0,-127)
	private java.math.BigDecimal variazionePiu;
 
//    VARIAZIONE_MENO DECIMAL(0,-127)
	private java.math.BigDecimal variazioneMeno;
 
//    ESERCIZIO_BUONO_CARICO DECIMAL(0,-127)
	private java.math.BigDecimal esercizioBuonoCarico;
 
//    INCREMENTO_VALORE DECIMAL(0,-127)
	private java.math.BigDecimal incrementoValore;
 
//    DECREMENTO_VALORE DECIMAL(0,-127)
	private java.math.BigDecimal decrementoValore;
 
//    ESERCIZIO_AMMORTANENTO DECIMAL(0,-127)
	private java.math.BigDecimal esercizioAmmortanento;
 
//    STORNO DECIMAL(0,-127)
	private java.math.BigDecimal storno;
 
//    ESERCIZIO_COMPETENZA DECIMAL(5,0)
	private Integer esercizioCompetenza;
 
//    CD_TIPO_AMMORTAMENTO VARCHAR(10)
	private String cdTipoAmmortamento;
 
//    DS_TIPO_AMMORTAMENTO VARCHAR(100)
	private String dsTipoAmmortamento;
 
//    PERC_PRIMO_ANNO DECIMAL(5,2)
	private java.math.BigDecimal percPrimoAnno;
 
//    PERC_SUCCESSIVI DECIMAL(5,2)
	private java.math.BigDecimal percSuccessivi;
 
//    NUMERO_ANNO_AMMORTAMENTO DECIMAL(0,-127)
	private java.math.BigDecimal numeroAnnoAmmortamento;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_AMMORTAMENTO_BENI_DET
	 **/
	public V_ammortamento_beni_detBase() {
		super();
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tiporecord]
	 **/
	public String getTiporecord() {
		return tiporecord;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiporecord]
	 **/
	public void setTiporecord(String tiporecord)  {
		this.tiporecord=tiporecord;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioCaricoBene]
	 **/
	public Integer getEsercizioCaricoBene() {
		return esercizioCaricoBene;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioCaricoBene]
	 **/
	public void setEsercizioCaricoBene(Integer esercizioCaricoBene)  {
		this.esercizioCaricoBene=esercizioCaricoBene;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgInventario]
	 **/
	public Long getPgInventario() {
		return pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgInventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.pgInventario=pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nrInventario]
	 **/
	public Long getNrInventario() {
		return nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nrInventario]
	 **/
	public void setNrInventario(Long nrInventario)  {
		this.nrInventario=nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [progressivo]
	 **/
	public Long getProgressivo() {
		return progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [progressivo]
	 **/
	public void setProgressivo(Long progressivo)  {
		this.progressivo=progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [etichetta]
	 **/
	public String getEtichetta() {
		return etichetta;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [etichetta]
	 **/
	public void setEtichetta(String etichetta)  {
		this.etichetta=etichetta;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flAmmortamento]
	 **/
	public Boolean getFlAmmortamento() {
		return flAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flAmmortamento]
	 **/
	public void setFlAmmortamento(Boolean flAmmortamento)  {
		this.flAmmortamento=flAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flTotalmenteScaricato]
	 **/
	public Boolean getFlTotalmenteScaricato() {
		return flTotalmenteScaricato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flTotalmenteScaricato]
	 **/
	public void setFlTotalmenteScaricato(Boolean flTotalmenteScaricato)  {
		this.flTotalmenteScaricato=flTotalmenteScaricato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tiAmmortamento]
	 **/
	public String getTiAmmortamento() {
		return tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiAmmortamento]
	 **/
	public void setTiAmmortamento(String tiAmmortamento)  {
		this.tiAmmortamento=tiAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCategoriaGruppo]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCategoriaGruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [valoreIniziale]
	 **/
	public java.math.BigDecimal getValoreIniziale() {
		return valoreIniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [valoreIniziale]
	 **/
	public void setValoreIniziale(java.math.BigDecimal valoreIniziale)  {
		this.valoreIniziale=valoreIniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [valoreAmmortizzato]
	 **/
	public java.math.BigDecimal getValoreAmmortizzato() {
		return valoreAmmortizzato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [valoreAmmortizzato]
	 **/
	public void setValoreAmmortizzato(java.math.BigDecimal valoreAmmortizzato)  {
		this.valoreAmmortizzato=valoreAmmortizzato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [imponibileAmmortamento]
	 **/
	public java.math.BigDecimal getImponibileAmmortamento() {
		return imponibileAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [imponibileAmmortamento]
	 **/
	public void setImponibileAmmortamento(java.math.BigDecimal imponibileAmmortamento)  {
		this.imponibileAmmortamento=imponibileAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [variazionePiu]
	 **/
	public java.math.BigDecimal getVariazionePiu() {
		return variazionePiu;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [variazionePiu]
	 **/
	public void setVariazionePiu(java.math.BigDecimal variazionePiu)  {
		this.variazionePiu=variazionePiu;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [variazioneMeno]
	 **/
	public java.math.BigDecimal getVariazioneMeno() {
		return variazioneMeno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [variazioneMeno]
	 **/
	public void setVariazioneMeno(java.math.BigDecimal variazioneMeno)  {
		this.variazioneMeno=variazioneMeno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioBuonoCarico]
	 **/
	public java.math.BigDecimal getEsercizioBuonoCarico() {
		return esercizioBuonoCarico;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioBuonoCarico]
	 **/
	public void setEsercizioBuonoCarico(java.math.BigDecimal esercizioBuonoCarico)  {
		this.esercizioBuonoCarico=esercizioBuonoCarico;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [incrementoValore]
	 **/
	public java.math.BigDecimal getIncrementoValore() {
		return incrementoValore;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [incrementoValore]
	 **/
	public void setIncrementoValore(java.math.BigDecimal incrementoValore)  {
		this.incrementoValore=incrementoValore;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [decrementoValore]
	 **/
	public java.math.BigDecimal getDecrementoValore() {
		return decrementoValore;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [decrementoValore]
	 **/
	public void setDecrementoValore(java.math.BigDecimal decrementoValore)  {
		this.decrementoValore=decrementoValore;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioAmmortanento]
	 **/
	public java.math.BigDecimal getEsercizioAmmortanento() {
		return esercizioAmmortanento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioAmmortanento]
	 **/
	public void setEsercizioAmmortanento(java.math.BigDecimal esercizioAmmortanento)  {
		this.esercizioAmmortanento=esercizioAmmortanento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [storno]
	 **/
	public java.math.BigDecimal getStorno() {
		return storno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [storno]
	 **/
	public void setStorno(java.math.BigDecimal storno)  {
		this.storno=storno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioCompetenza]
	 **/
	public Integer getEsercizioCompetenza() {
		return esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioCompetenza]
	 **/
	public void setEsercizioCompetenza(Integer esercizioCompetenza)  {
		this.esercizioCompetenza=esercizioCompetenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdTipoAmmortamento]
	 **/
	public String getCdTipoAmmortamento() {
		return cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdTipoAmmortamento]
	 **/
	public void setCdTipoAmmortamento(String cdTipoAmmortamento)  {
		this.cdTipoAmmortamento=cdTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsTipoAmmortamento]
	 **/
	public String getDsTipoAmmortamento() {
		return dsTipoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsTipoAmmortamento]
	 **/
	public void setDsTipoAmmortamento(String dsTipoAmmortamento)  {
		this.dsTipoAmmortamento=dsTipoAmmortamento;
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
	 * Restituisce il valore di: [numeroAnnoAmmortamento]
	 **/
	public java.math.BigDecimal getNumeroAnnoAmmortamento() {
		return numeroAnnoAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeroAnnoAmmortamento]
	 **/
	public void setNumeroAnnoAmmortamento(java.math.BigDecimal numeroAnnoAmmortamento)  {
		this.numeroAnnoAmmortamento=numeroAnnoAmmortamento;
	}
}