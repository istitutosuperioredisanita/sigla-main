/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class V_ammortamento_beniBase extends V_ammortamento_beniKey implements Keyed {

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_AMMORTAMENTO_BENI
	 **/
	public V_ammortamento_beniBase() {
		super();
	}

	//    PG_INVENTARIO DECIMAL(38,0) NOT NULL
	private Long pgInventario;
 
//    NR_INVENTARIO DECIMAL(38,0) NOT NULL
	private Long nrInventario;
 
//    PROGRESSIVO DECIMAL(38,0) NOT NULL
	private Long progressivo;
 
//    CD_CATEGORIA_GRUPPO VARCHAR(10) NOT NULL
	private String cdCategoriaGruppo;
 
//    TI_AMMORTAMENTO_BENE CHAR(1)
	private String tiAmmortamentoBene;
 
//    FL_AMMORTAMENTO CHAR(1)
	private Boolean flAmmortamento;
 
//    VALORE_INIZIALE DECIMAL(20,6) NOT NULL
	private java.math.BigDecimal valoreIniziale;
 
//    VALORE_AMMORTIZZATO DECIMAL(0,-127)
	private java.math.BigDecimal valoreAmmortizzato;
 
//    VARIAZIONE_PIU DECIMAL(0,-127)
	private java.math.BigDecimal variazionePiu;
 
//    VARIAZIONE_MENO DECIMAL(0,-127)
	private java.math.BigDecimal variazioneMeno;
 
//    IMPONIBILE_AMMORTAMENTO DECIMAL(0,-127)
	private java.math.BigDecimal imponibileAmmortamento;
 
//    FL_TOTALMENTE_SCARICATO CHAR(1) NOT NULL
	private Boolean flTotalmenteScaricato;
 
//    ESERCIZIO_CARICO_BENE DECIMAL(5,0) NOT NULL
	private Integer esercizioCaricoBene;
 
//    CD_CDS VARCHAR(30) NOT NULL
	private String cdCds;
 
//    CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL
	private String cdUnitaOrganizzativa;
 
//    ESERCIZIO_COMPETENZA DECIMAL(5,0)
	private Integer esercizioCompetenza;
 
//    CD_TIPO_AMMORTAMENTO VARCHAR(10)
	private String cdTipoAmmortamento;
 
//    TI_AMMORTAMENTO CHAR(1)
	private String tiAmmortamento;
 
//    DT_CANCELLAZIONE TIMESTAMP(7)
	private java.sql.Timestamp dtCancellazione;
 
//    PERC_PRIMO_ANNO DECIMAL(5,2)
	private java.math.BigDecimal percPrimoAnno;
 
//    PERC_SUCCESSIVI DECIMAL(5,2)
	private java.math.BigDecimal percSuccessivi;
 
//    NUMERO_ANNI DECIMAL(5,0)
	private Integer numeroAnni;
 


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
	 * Restituisce il valore di: [tiAmmortamentoBene]
	 **/
	public String getTiAmmortamentoBene() {
		return tiAmmortamentoBene;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiAmmortamentoBene]
	 **/
	public void setTiAmmortamentoBene(String tiAmmortamentoBene)  {
		this.tiAmmortamentoBene=tiAmmortamentoBene;
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
	 * Restituisce il valore di: [cdCds]
	 **/
	public String getCdCds() {
		return cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCds]
	 **/
	public void setCdCds(String cdCds)  {
		this.cdCds=cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOrganizzativa]
	 **/
	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
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
	 * Restituisce il valore di: [dtCancellazione]
	 **/
	public java.sql.Timestamp getDtCancellazione() {
		return dtCancellazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtCancellazione]
	 **/
	public void setDtCancellazione(java.sql.Timestamp dtCancellazione)  {
		this.dtCancellazione=dtCancellazione;
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
}