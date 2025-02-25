package it.cnr.contab.gestiva00.core.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;

public class V_cons_reg_ivaBase extends OggettoBulk implements Persistent {

	private Integer esercizio;
	private String tipo;
	private String data_registrazione;
	private String dt_emissione;
	private String dt_emiss_amm;
	private String ti_bene_servizio;
	private String cd_tipo_sezionale;
	private String cd_unita_organizzativa;
	private String cd_cds_origine;
	private String cd_uo_origine;
	private Long pg_fattura;
	private Long prot_iva;
	private String nrgreg_iva;
	private Long pgr_u;
	private String ds_fattura;
	private BigDecimal imp;
	private BigDecimal iva;
	private BigDecimal tot;
	private String cd_forn;
	private String deno_forn;
	private String cf_piva;
	private String nr_fattura;
	private String ue;
	private String tipo_fattura;
	private Long pgr_d;
	private BigDecimal imp_d;
	private BigDecimal iva_d;
	private BigDecimal tot_d;
	private String cd_voce_iva;
	private String cd_gruppo_iva;
	private String ds_gruppo_iva;
	private BigDecimal percentuale;
	private String d;
	private BigDecimal perc_d;
	private String sp;
	private String tipo_documento;
	private String fl_autofattura;
	private String fl_spedizioniere;
	private String fl_bolla_doganale;

	public V_cons_reg_ivaBase() {
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getData_registrazione() {
		return data_registrazione;
	}

	public void setData_registrazione(String data_registrazione) {
		this.data_registrazione = data_registrazione;
	}

	public String getDt_emissione() {
		return dt_emissione;
	}

	public void setDt_emissione(String dt_emissione) {
		this.dt_emissione = dt_emissione;
	}

	public String getDt_emiss_amm() {
		return dt_emiss_amm;
	}

	public void setDt_emiss_amm(String dt_emiss_amm) {
		this.dt_emiss_amm = dt_emiss_amm;
	}

	public String getTi_bene_servizio() {
		return ti_bene_servizio;
	}

	public void setTi_bene_servizio(String ti_bene_servizio) {
		this.ti_bene_servizio = ti_bene_servizio;
	}

	public String getCd_tipo_sezionale() {
		return cd_tipo_sezionale;
	}

	public void setCd_tipo_sezionale(String cd_tipo_sezionale) {
		this.cd_tipo_sezionale = cd_tipo_sezionale;
	}

	public String getCd_unita_organizzativa() {
		return cd_unita_organizzativa;
	}

	public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
		this.cd_unita_organizzativa = cd_unita_organizzativa;
	}

	public String getCd_cds_origine() {
		return cd_cds_origine;
	}

	public void setCd_cds_origine(String cd_cds_origine) {
		this.cd_cds_origine = cd_cds_origine;
	}

	public String getCd_uo_origine() {
		return cd_uo_origine;
	}

	public void setCd_uo_origine(String cd_uo_origine) {
		this.cd_uo_origine = cd_uo_origine;
	}

	public Long getPg_fattura() {
		return pg_fattura;
	}

	public void setPg_fattura(Long pg_fattura) {
		this.pg_fattura = pg_fattura;
	}

	public Long getProt_iva() {
		return prot_iva;
	}

	public void setProt_iva(Long prot_iva) {
		this.prot_iva = prot_iva;
	}

	public String getNrgreg_iva() {
		return nrgreg_iva;
	}

	public void setNrgreg_iva(String nrgreg_iva) {
		this.nrgreg_iva = nrgreg_iva;
	}

	public Long getPgr_u() {
		return pgr_u;
	}

	public void setPgr_u(Long pgr_u) {
		this.pgr_u = pgr_u;
	}

	public String getDs_fattura() {
		return ds_fattura;
	}

	public void setDs_fattura(String ds_fattura) {
		this.ds_fattura = ds_fattura;
	}

	public BigDecimal getImp() {
		return imp;
	}

	public void setImp(BigDecimal imp) {
		this.imp = imp;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getTot() {
		return tot;
	}

	public void setTot(BigDecimal tot) {
		this.tot = tot;
	}

	public String getCd_forn() {
		return cd_forn;
	}

	public void setCd_forn(String cd_forn) {
		this.cd_forn = cd_forn;
	}

	public String getDeno_forn() {
		return deno_forn;
	}

	public void setDeno_forn(String deno_forn) {
		this.deno_forn = deno_forn;
	}

	public String getCf_piva() {
		return cf_piva;
	}

	public void setCf_piva(String cf_piva) {
		this.cf_piva = cf_piva;
	}

	public String getNr_fattura() {
		return nr_fattura;
	}

	public void setNr_fattura(String nr_fattura) {
		this.nr_fattura = nr_fattura;
	}

	public String getUe() {
		return ue;
	}

	public void setUe(String ue) {
		this.ue = ue;
	}

	public String getTipo_fattura() {
		return tipo_fattura;
	}

	public void setTipo_fattura(String tipo_fattura) {
		this.tipo_fattura = tipo_fattura;
	}

	public Long getPgr_d() {
		return pgr_d;
	}

	public void setPgr_d(Long pgr_d) {
		this.pgr_d = pgr_d;
	}

	public BigDecimal getImp_d() {
		return imp_d;
	}

	public void setImp_d(BigDecimal imp_d) {
		this.imp_d = imp_d;
	}

	public BigDecimal getIva_d() {
		return iva_d;
	}

	public void setIva_d(BigDecimal iva_d) {
		this.iva_d = iva_d;
	}

	public BigDecimal getTot_d() {
		return tot_d;
	}

	public void setTot_d(BigDecimal tot_d) {
		this.tot_d = tot_d;
	}

	public String getCd_voce_iva() {
		return cd_voce_iva;
	}

	public void setCd_voce_iva(String cd_voce_iva) {
		this.cd_voce_iva = cd_voce_iva;
	}

	public String getCd_gruppo_iva() {
		return cd_gruppo_iva;
	}

	public void setCd_gruppo_iva(String cd_gruppo_iva) {
		this.cd_gruppo_iva = cd_gruppo_iva;
	}

	public String getDs_gruppo_iva() {
		return ds_gruppo_iva;
	}

	public void setDs_gruppo_iva(String ds_gruppo_iva) {
		this.ds_gruppo_iva = ds_gruppo_iva;
	}

	public BigDecimal getPercentuale() {
		return percentuale;
	}

	public void setPercentuale(BigDecimal percentuale) {
		this.percentuale = percentuale;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public BigDecimal getPerc_d() {
		return perc_d;
	}

	public void setPerc_d(BigDecimal perc_d) {
		this.perc_d = perc_d;
	}

	public String getSp() {
		return sp;
	}

	public void setSp(String sp) {
		this.sp = sp;
	}

	public String getTipo_documento() {
		return tipo_documento;
	}

	public void setTipo_documento(String tipo_documento) {
		this.tipo_documento = tipo_documento;
	}

	public String getFl_autofattura() {
		return fl_autofattura;
	}

	public void setFl_autofattura(String fl_autofattura) {
		this.fl_autofattura = fl_autofattura;
	}

	public String getFl_spedizioniere() {
		return fl_spedizioniere;
	}

	public void setFl_spedizioniere(String fl_spedizioniere) {
		this.fl_spedizioniere = fl_spedizioniere;
	}

	public String getFl_bolla_doganale() {
		return fl_bolla_doganale;
	}

	public void setFl_bolla_doganale(String fl_bolla_doganale) {
		this.fl_bolla_doganale = fl_bolla_doganale;
	}
}