/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class V_inventario_bene_detBase extends V_inventario_bene_detKey implements Keyed {
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
 
//    FL_MIGRATO CHAR(1)
	private Boolean flMigrato;
 
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
 
//    PG_BUONO_C_S DECIMAL(0,-127)
	private java.math.BigDecimal pgBuonoCS;
 
//    QUOTA_AMMO_BENE_ALIENATO DECIMAL(0,-127)
	private java.math.BigDecimal quotaAmmoBeneAlienato;
 
//    ESERCIZIO_AMMORTANENTO DECIMAL(0,-127)
	private java.math.BigDecimal esercizioAmmortanento;
 
//    QUOTA_AMMORTAMENTO DECIMAL(0,-127)
	private java.math.BigDecimal quotaAmmortamento;
 
//    STORNO DECIMAL(0,-127)
	private java.math.BigDecimal storno;
 
//    PIANO_AMM_BENE_MIGRATO VARCHAR(10)
	private String pianoAmmBeneMigrato;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_INVENTARIO_BENE_DET
	 **/
	public V_inventario_bene_detBase() {
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
	 * Restituisce il valore di: [flMigrato]
	 **/
	public Boolean getFlMigrato() {
		return flMigrato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flMigrato]
	 **/
	public void setFlMigrato(Boolean flMigrato)  {
		this.flMigrato=flMigrato;
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
	 * Restituisce il valore di: [pgBuonoCS]
	 **/
	public java.math.BigDecimal getPgBuonoCS() {
		return pgBuonoCS;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgBuonoCS]
	 **/
	public void setPgBuonoCS(java.math.BigDecimal pgBuonoCS)  {
		this.pgBuonoCS=pgBuonoCS;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [quotaAmmoBeneAlienato]
	 **/
	public java.math.BigDecimal getQuotaAmmoBeneAlienato() {
		return quotaAmmoBeneAlienato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [quotaAmmoBeneAlienato]
	 **/
	public void setQuotaAmmoBeneAlienato(java.math.BigDecimal quotaAmmoBeneAlienato)  {
		this.quotaAmmoBeneAlienato=quotaAmmoBeneAlienato;
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
	 * Restituisce il valore di: [quotaAmmortamento]
	 **/
	public java.math.BigDecimal getQuotaAmmortamento() {
		return quotaAmmortamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [quotaAmmortamento]
	 **/
	public void setQuotaAmmortamento(java.math.BigDecimal quotaAmmortamento)  {
		this.quotaAmmortamento=quotaAmmortamento;
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
	 * Restituisce il valore di: [pianoAmmBeneMigrato]
	 **/
	public String getPianoAmmBeneMigrato() {
		return pianoAmmBeneMigrato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pianoAmmBeneMigrato]
	 **/
	public void setPianoAmmBeneMigrato(String pianoAmmBeneMigrato)  {
		this.pianoAmmBeneMigrato=pianoAmmBeneMigrato;
	}
}