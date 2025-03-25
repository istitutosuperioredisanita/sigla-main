/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/02/2025
 */
package it.cnr.contab.gestiva00.core.bulk;
import it.cnr.jada.persistency.Keyed;
public class V_cons_reg_ivaBase extends V_cons_reg_ivaKey implements Keyed {
//    ESERCIZIO DECIMAL(5,0)
	private Integer esercizio;
 
//    TIPO VARCHAR(13)
	private String tipo;
 
//    DATA_REGISTRAZIONE VARCHAR(7)
	private String dataRegistrazione;
 
//    DT_EMISSIONE VARCHAR(10)
	private String dtEmissione;
 
//    DT_EMISS_AMM VARCHAR(7)
	private String dtEmissAmm;
 
//    TI_BENE_SERVIZIO CHAR(1)
	private String tiBeneServizio;
 
//    CD_TIPO_SEZIONALE VARCHAR(10)
	private String cdTipoSezionale;
 
//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private String cdUnitaOrganizzativa;
 
//    CD_CDS_ORIGINE VARCHAR(30)
	private String cdCdsOrigine;
 
//    CD_UO_ORIGINE VARCHAR(30)
	private String cdUoOrigine;
 
//    PG_FATTURA DECIMAL(38,0)
	private Long pgFattura;
 
//    PROT_IVA DECIMAL(0,-127)
	private java.math.BigDecimal protIva;
 
//    NRGREG_IVA VARCHAR(81)
	private String nrgregIva;
 
//    PGR_U DECIMAL(38,0)
	private Long pgrU;
 
//    DS_FATTURA VARCHAR(1000)
	private String dsFattura;
 
//    IMP DECIMAL(0,-127)
	private java.math.BigDecimal imp;
 
//    IVA DECIMAL(0,-127)
	private java.math.BigDecimal iva;
 
//    TOT DECIMAL(0,-127)
	private java.math.BigDecimal tot;
 
//    CD_FORN DECIMAL(38,0)
	private Long cdForn;
 
//    DENO_FORN VARCHAR(100)
	private String denoForn;
 
//    CF_PIVA VARCHAR(41)
	private String cfPiva;
 
//    NR_FATTURA VARCHAR(40)
	private String nrFattura;
 
//    UE CHAR(1)
	private String ue;
 
//    TIPO_FATTURA CHAR(1)
	private String tipoFattura;
 
//    PGR_D DECIMAL(38,0)
	private Long pgrD;
 
//    IMP_D DECIMAL(0,-127)
	private java.math.BigDecimal impD;
 
//    IVA_D DECIMAL(0,-127)
	private java.math.BigDecimal ivaD;
 
//    TOT_D DECIMAL(0,-127)
	private java.math.BigDecimal totD;
 
//    CD_VOCE_IVA VARCHAR(10)
	private String cdVoceIva;
 
//    CD_GRUPPO_IVA VARCHAR(10)
	private String cdGruppoIva;
 
//    DS_GRUPPO_IVA VARCHAR(100)
	private String dsGruppoIva;
 
//    PERCENTUALE DECIMAL(5,2)
	private java.math.BigDecimal percentuale;
 
//    D CHAR(1)
	private String d;
 
//    PERC_D DECIMAL(5,2)
	private java.math.BigDecimal percD;
 
//    SP CHAR(1)
	private String sp;
 
//    TIPO_DOCUMENTO VARCHAR(7)
	private String tipoDocumento;
 
//    FL_AUTOFATTURA CHAR(1)
	private Boolean flAutofattura;
 
//    FL_SPEDIZIONIERE CHAR(1)
	private Boolean flSpedizioniere;
 
//    FL_BOLLA_DOGANALE CHAR(1)
	private Boolean flBollaDoganale;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONS_REG_IVA
	 **/
	public V_cons_reg_ivaBase() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tipo]
	 **/
	public String getTipo() {
		return tipo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tipo]
	 **/
	public void setTipo(String tipo)  {
		this.tipo=tipo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dataRegistrazione]
	 **/
	public String getDataRegistrazione() {
		return dataRegistrazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dataRegistrazione]
	 **/
	public void setDataRegistrazione(String dataRegistrazione)  {
		this.dataRegistrazione=dataRegistrazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dtEmissione]
	 **/
	public String getDtEmissione() {
		return dtEmissione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtEmissione]
	 **/
	public void setDtEmissione(String dtEmissione)  {
		this.dtEmissione=dtEmissione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dtEmissAmm]
	 **/
	public String getDtEmissAmm() {
		return dtEmissAmm;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtEmissAmm]
	 **/
	public void setDtEmissAmm(String dtEmissAmm)  {
		this.dtEmissAmm=dtEmissAmm;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tiBeneServizio]
	 **/
	public String getTiBeneServizio() {
		return tiBeneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tiBeneServizio]
	 **/
	public void setTiBeneServizio(String tiBeneServizio)  {
		this.tiBeneServizio=tiBeneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdTipoSezionale]
	 **/
	public String getCdTipoSezionale() {
		return cdTipoSezionale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdTipoSezionale]
	 **/
	public void setCdTipoSezionale(String cdTipoSezionale)  {
		this.cdTipoSezionale=cdTipoSezionale;
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
	 * Restituisce il valore di: [cdCdsOrigine]
	 **/
	public String getCdCdsOrigine() {
		return cdCdsOrigine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCdsOrigine]
	 **/
	public void setCdCdsOrigine(String cdCdsOrigine)  {
		this.cdCdsOrigine=cdCdsOrigine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUoOrigine]
	 **/
	public String getCdUoOrigine() {
		return cdUoOrigine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUoOrigine]
	 **/
	public void setCdUoOrigine(String cdUoOrigine)  {
		this.cdUoOrigine=cdUoOrigine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgFattura]
	 **/
	public Long getPgFattura() {
		return pgFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgFattura]
	 **/
	public void setPgFattura(Long pgFattura)  {
		this.pgFattura=pgFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [protIva]
	 **/
	public java.math.BigDecimal getProtIva() {
		return protIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [protIva]
	 **/
	public void setProtIva(java.math.BigDecimal protIva)  {
		this.protIva=protIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nrgregIva]
	 **/
	public String getNrgregIva() {
		return nrgregIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nrgregIva]
	 **/
	public void setNrgregIva(String nrgregIva)  {
		this.nrgregIva=nrgregIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgrU]
	 **/
	public Long getPgrU() {
		return pgrU;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgrU]
	 **/
	public void setPgrU(Long pgrU)  {
		this.pgrU=pgrU;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsFattura]
	 **/
	public String getDsFattura() {
		return dsFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsFattura]
	 **/
	public void setDsFattura(String dsFattura)  {
		this.dsFattura=dsFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [imp]
	 **/
	public java.math.BigDecimal getImp() {
		return imp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [imp]
	 **/
	public void setImp(java.math.BigDecimal imp)  {
		this.imp=imp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [iva]
	 **/
	public java.math.BigDecimal getIva() {
		return iva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [iva]
	 **/
	public void setIva(java.math.BigDecimal iva)  {
		this.iva=iva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tot]
	 **/
	public java.math.BigDecimal getTot() {
		return tot;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tot]
	 **/
	public void setTot(java.math.BigDecimal tot)  {
		this.tot=tot;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdForn]
	 **/
	public Long getCdForn() {
		return cdForn;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdForn]
	 **/
	public void setCdForn(Long cdForn)  {
		this.cdForn=cdForn;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [denoForn]
	 **/
	public String getDenoForn() {
		return denoForn;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [denoForn]
	 **/
	public void setDenoForn(String denoForn)  {
		this.denoForn=denoForn;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cfPiva]
	 **/
	public String getCfPiva() {
		return cfPiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cfPiva]
	 **/
	public void setCfPiva(String cfPiva)  {
		this.cfPiva=cfPiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nrFattura]
	 **/
	public String getNrFattura() {
		return nrFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nrFattura]
	 **/
	public void setNrFattura(String nrFattura)  {
		this.nrFattura=nrFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [ue]
	 **/
	public String getUe() {
		return ue;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [ue]
	 **/
	public void setUe(String ue)  {
		this.ue=ue;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tipoFattura]
	 **/
	public String getTipoFattura() {
		return tipoFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tipoFattura]
	 **/
	public void setTipoFattura(String tipoFattura)  {
		this.tipoFattura=tipoFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgrD]
	 **/
	public Long getPgrD() {
		return pgrD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgrD]
	 **/
	public void setPgrD(Long pgrD)  {
		this.pgrD=pgrD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [impD]
	 **/
	public java.math.BigDecimal getImpD() {
		return impD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [impD]
	 **/
	public void setImpD(java.math.BigDecimal impD)  {
		this.impD=impD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [ivaD]
	 **/
	public java.math.BigDecimal getIvaD() {
		return ivaD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [ivaD]
	 **/
	public void setIvaD(java.math.BigDecimal ivaD)  {
		this.ivaD=ivaD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totD]
	 **/
	public java.math.BigDecimal getTotD() {
		return totD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totD]
	 **/
	public void setTotD(java.math.BigDecimal totD)  {
		this.totD=totD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdVoceIva]
	 **/
	public String getCdVoceIva() {
		return cdVoceIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdVoceIva]
	 **/
	public void setCdVoceIva(String cdVoceIva)  {
		this.cdVoceIva=cdVoceIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdGruppoIva]
	 **/
	public String getCdGruppoIva() {
		return cdGruppoIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdGruppoIva]
	 **/
	public void setCdGruppoIva(String cdGruppoIva)  {
		this.cdGruppoIva=cdGruppoIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsGruppoIva]
	 **/
	public String getDsGruppoIva() {
		return dsGruppoIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsGruppoIva]
	 **/
	public void setDsGruppoIva(String dsGruppoIva)  {
		this.dsGruppoIva=dsGruppoIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [percentuale]
	 **/
	public java.math.BigDecimal getPercentuale() {
		return percentuale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [percentuale]
	 **/
	public void setPercentuale(java.math.BigDecimal percentuale)  {
		this.percentuale=percentuale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [d]
	 **/
	public String getD() {
		return d;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [d]
	 **/
	public void setD(String d)  {
		this.d=d;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [percD]
	 **/
	public java.math.BigDecimal getPercD() {
		return percD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [percD]
	 **/
	public void setPercD(java.math.BigDecimal percD)  {
		this.percD=percD;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [sp]
	 **/
	public String getSp() {
		return sp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [sp]
	 **/
	public void setSp(String sp)  {
		this.sp=sp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tipoDocumento]
	 **/
	public String getTipoDocumento() {
		return tipoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tipoDocumento]
	 **/
	public void setTipoDocumento(String tipoDocumento)  {
		this.tipoDocumento=tipoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flAutofattura]
	 **/
	public Boolean getFlAutofattura() {
		return flAutofattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flAutofattura]
	 **/
	public void setFlAutofattura(Boolean flAutofattura)  {
		this.flAutofattura=flAutofattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flSpedizioniere]
	 **/
	public Boolean getFlSpedizioniere() {
		return flSpedizioniere;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flSpedizioniere]
	 **/
	public void setFlSpedizioniere(Boolean flSpedizioniere)  {
		this.flSpedizioniere=flSpedizioniere;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flBollaDoganale]
	 **/
	public Boolean getFlBollaDoganale() {
		return flBollaDoganale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flBollaDoganale]
	 **/
	public void setFlBollaDoganale(Boolean flBollaDoganale)  {
		this.flBollaDoganale=flBollaDoganale;
	}
}