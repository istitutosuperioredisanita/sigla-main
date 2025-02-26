/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.anagraf00.core.bulk;

import it.cnr.jada.persistency.Keyed;

public class V_terzo_persona_fisicaBase extends V_terzo_persona_fisicaKey implements Keyed {
	// ALIQUOTA_FISCALE DECIMAL(6,3)
	private java.math.BigDecimal aliquota_fiscale;

	// ALTRA_ASS_PREVID_INPS VARCHAR(5)
	private String altra_ass_previd_inps;

	// CAP_COMUNE_FISCALE VARCHAR(5)
	private String cap_comune_fiscale;

	// CAP_COMUNE_SEDE VARCHAR(5)
	private String cap_comune_sede;

	// CAUSALE_FINE_RAPPORTO VARCHAR(50)
	private String causale_fine_rapporto;

	// CD_ATTIVITA_INPS VARCHAR(5)
	private String cd_attivita_inps;

	// CD_CLASSIFIC_ANAG VARCHAR(10)
	private String cd_classific_anag;

	// CD_ENTE_APPARTENENZA DECIMAL(8,0)
	private Integer cd_ente_appartenenza;

	// CD_PRECEDENTE VARCHAR(20)
	private String cd_precedente;

	// CD_PROVINCIA_FISCALE VARCHAR(10)
	private String cd_provincia_fiscale;

	// CD_PROVINCIA_NASCITA VARCHAR(10)
	private String cd_provincia_nascita;

	// CD_PROVINCIA_SEDE VARCHAR(10)
	private String cd_provincia_sede;

	// CD_UNITA_ORGANIZZATIVA VARCHAR(60)
	private String cd_unita_organizzativa;

	// CODICE_FISCALE VARCHAR(20)
	private String codice_fiscale;

	// CODICE_FISCALE_CAF VARCHAR(20)
	private String codice_fiscale_caf;

	// COGNOME VARCHAR(50)
	private String cognome;

	// CONTO_NUMERARIO_CREDITO VARCHAR(50)
	private String conto_numerario_credito;

	// CONTO_NUMERARIO_DEBITO VARCHAR(50)
	private String conto_numerario_debito;

	// DENOMINAZIONE_CAF VARCHAR(100)
	private String denominazione_caf;

	// DENOMINAZIONE_SEDE VARCHAR(200) NOT NULL
	private String denominazione_sede;

	// DS_COMUNE_FISCALE VARCHAR(100) NOT NULL
	private String ds_comune_fiscale;

	// DS_COMUNE_NASCITA VARCHAR(100)
	private String ds_comune_nascita;

	// DS_COMUNE_SEDE VARCHAR(100) NOT NULL
	private String ds_comune_sede;

	// DS_PROVINCIA_FISCALE VARCHAR(100)
	private String ds_provincia_fiscale;

	// DS_PROVINCIA_NASCITA VARCHAR(100)
	private String ds_provincia_nascita;

	// DS_PROVINCIA_SEDE VARCHAR(100)
	private String ds_provincia_sede;

	// DT_ANTIMAFIA TIMESTAMP
	private java.sql.Timestamp dt_antimafia;

	// DT_CANC TIMESTAMP
	private java.sql.Timestamp dt_canc;

	// DT_FINE_RAPPORTO TIMESTAMP
	private java.sql.Timestamp dt_fine_rapporto;

	// DT_NASCITA TIMESTAMP
	private java.sql.Timestamp dt_nascita;

	// FL_FATTURAZIONE_DIFFERITA CHAR(1)
	private Boolean fl_fatturazione_differita;

	// FL_OCCASIONALE CHAR(1) NOT NULL
	private Boolean fl_occasionale;

	// FL_SOGGETTO_IVA CHAR(1) NOT NULL
	private Boolean fl_soggetto_iva;

	// FRAZIONE_FISCALE VARCHAR(100)
	private String frazione_fiscale;

	// FRAZIONE_SEDE VARCHAR(100)
	private String frazione_sede;

	// ID_FISCALE_ESTERO VARCHAR(20)
	private String id_fiscale_estero;

	// MATRICOLA_INAIL VARCHAR(30)
	private String matricola_inail;

	// NOME VARCHAR(50)
	private String nome;

	// NOME_UNITA_ORGANIZZATIVA VARCHAR(100)
	private String nome_unita_organizzativa;

	// NOTE VARCHAR(300)
	private String note;

	// NUMERO_CIVICO_SEDE VARCHAR(5)
	private String numero_civico_sede;

	// NUM_CIVICO_FISCALE VARCHAR(10)
	private String num_civico_fiscale;

	// NUM_ISCRIZ_ALBO VARCHAR(10)
	private String num_iscriz_albo;

	// NUM_ISCRIZ_CCIAA VARCHAR(10)
	private String num_iscriz_cciaa;

	// PARTITA_IVA VARCHAR(20)
	private String partita_iva;

	// PG_COMUNE_FISCALE DECIMAL(10,0) NOT NULL
	private Long pg_comune_fiscale;

	// PG_COMUNE_NASCITA DECIMAL(10,0)
	private Long pg_comune_nascita;

	// PG_COMUNE_SEDE DECIMAL(10,0) NOT NULL
	private Long pg_comune_sede;

	// PG_NAZIONE_FISCALE DECIMAL(10,0)
	private Long pg_nazione_fiscale;

	// PG_NAZIONE_NAZIONALITA DECIMAL(10,0)
	private Long pg_nazione_nazionalita;

	// PG_RAPP_LEGALE DECIMAL(10,0)
	private Long pg_rapp_legale;

	// RAGIONE_SOCIALE VARCHAR(100)
	private String ragione_sociale;

	// SEDE_INAIL VARCHAR(50)
	private String sede_inail;

	// TI_ENTITA CHAR(1) NOT NULL
	private String ti_entita;

	// TI_ENTITA_FISICA CHAR(1)
	private String ti_entita_fisica;

	// TI_ENTITA_GIURIDICA CHAR(1)
	private String ti_entita_giuridica;

	// TI_ITALIANO_ESTERO CHAR(1) NOT NULL
	private String ti_italiano_estero;

	// TI_SESSO CHAR(1)
	private String ti_sesso;

	// TI_TERZO CHAR(1) NOT NULL
	private String ti_terzo;

	// VIA_FISCALE VARCHAR(100) NOT NULL
	private String via_fiscale;

	// VIA_SEDE VARCHAR(100) NOT NULL
	private String via_sede;

	private Integer cd_anag;
	// FL_FIRMATARIO_CONTRATTO CHAR(1) NOT NULL
	private Boolean flFirmatarioContratto;

public V_terzo_persona_fisicaBase() {
	super();
}
public V_terzo_persona_fisicaBase(Integer terzo) {
	super(terzo);
}
/* 
 * Getter dell'attributo aliquota_fiscale
 */
public java.math.BigDecimal getAliquota_fiscale() {
	return aliquota_fiscale;
}
/* 
 * Getter dell'attributo altra_ass_previd_inps
 */
public String getAltra_ass_previd_inps() {
	return altra_ass_previd_inps;
}
/* 
 * Getter dell'attributo cap_comune_fiscale
 */
public String getCap_comune_fiscale() {
	return cap_comune_fiscale;
}
/* 
 * Getter dell'attributo cap_comune_sede
 */
public String getCap_comune_sede() {
	return cap_comune_sede;
}
/* 
 * Getter dell'attributo causale_fine_rapporto
 */
public String getCausale_fine_rapporto() {
	return causale_fine_rapporto;
}
public Integer getCd_anag() {
	return cd_anag;
}
/* 
 * Getter dell'attributo cd_attivita_inps
 */
public String getCd_attivita_inps() {
	return cd_attivita_inps;
}
/* 
 * Getter dell'attributo cd_classific_anag
 */
public String getCd_classific_anag() {
	return cd_classific_anag;
}
/* 
 * Getter dell'attributo cd_ente_appartenenza
 */
public Integer getCd_ente_appartenenza() {
	return cd_ente_appartenenza;
}
/* 
 * Getter dell'attributo cd_precedente
 */
public String getCd_precedente() {
	return cd_precedente;
}
/* 
 * Getter dell'attributo cd_provincia_fiscale
 */
public String getCd_provincia_fiscale() {
	return cd_provincia_fiscale;
}
/* 
 * Getter dell'attributo cd_provincia_nascita
 */
public String getCd_provincia_nascita() {
	return cd_provincia_nascita;
}
/* 
 * Getter dell'attributo cd_provincia_sede
 */
public String getCd_provincia_sede() {
	return cd_provincia_sede;
}
/* 
 * Getter dell'attributo cd_unita_organizzativa
 */
public String getCd_unita_organizzativa() {
	return cd_unita_organizzativa;
}
/* 
 * Getter dell'attributo codice_fiscale
 */
public String getCodice_fiscale() {
	return codice_fiscale;
}
/* 
 * Getter dell'attributo codice_fiscale_caf
 */
public String getCodice_fiscale_caf() {
	return codice_fiscale_caf;
}
/* 
 * Getter dell'attributo cognome
 */
public String getCognome() {
	return cognome;
}
/* 
 * Getter dell'attributo conto_numerario_credito
 */
public String getConto_numerario_credito() {
	return conto_numerario_credito;
}
/* 
 * Getter dell'attributo conto_numerario_debito
 */
public String getConto_numerario_debito() {
	return conto_numerario_debito;
}
/* 
 * Getter dell'attributo denominazione_caf
 */
public String getDenominazione_caf() {
	return denominazione_caf;
}
/* 
 * Getter dell'attributo denominazione_sede
 */
public String getDenominazione_sede() {
	return denominazione_sede;
}
/* 
 * Getter dell'attributo ds_comune_fiscale
 */
public String getDs_comune_fiscale() {
	return ds_comune_fiscale;
}
/* 
 * Getter dell'attributo ds_comune_nascita
 */
public String getDs_comune_nascita() {
	return ds_comune_nascita;
}
/* 
 * Getter dell'attributo ds_comune_sede
 */
public String getDs_comune_sede() {
	return ds_comune_sede;
}
/* 
 * Getter dell'attributo ds_provincia_fiscale
 */
public String getDs_provincia_fiscale() {
	return ds_provincia_fiscale;
}
/* 
 * Getter dell'attributo ds_provincia_nascita
 */
public String getDs_provincia_nascita() {
	return ds_provincia_nascita;
}
/* 
 * Getter dell'attributo ds_provincia_sede
 */
public String getDs_provincia_sede() {
	return ds_provincia_sede;
}
/* 
 * Getter dell'attributo dt_antimafia
 */
public java.sql.Timestamp getDt_antimafia() {
	return dt_antimafia;
}
/* 
 * Getter dell'attributo dt_canc
 */
public java.sql.Timestamp getDt_canc() {
	return dt_canc;
}
/* 
 * Getter dell'attributo dt_fine_rapporto
 */
public java.sql.Timestamp getDt_fine_rapporto() {
	return dt_fine_rapporto;
}
/* 
 * Getter dell'attributo dt_nascita
 */
public java.sql.Timestamp getDt_nascita() {
	return dt_nascita;
}
/* 
 * Getter dell'attributo fl_fatturazione_differita
 */
public Boolean getFl_fatturazione_differita() {
	return fl_fatturazione_differita;
}
/* 
 * Getter dell'attributo fl_occasionale
 */
public Boolean getFl_occasionale() {
	return fl_occasionale;
}
/* 
 * Getter dell'attributo fl_soggetto_iva
 */
public Boolean getFl_soggetto_iva() {
	return fl_soggetto_iva;
}
/* 
 * Getter dell'attributo frazione_fiscale
 */
public String getFrazione_fiscale() {
	return frazione_fiscale;
}
/* 
 * Getter dell'attributo frazione_sede
 */
public String getFrazione_sede() {
	return frazione_sede;
}
/* 
 * Getter dell'attributo id_fiscale_estero
 */
public String getId_fiscale_estero() {
	return id_fiscale_estero;
}
/* 
 * Getter dell'attributo matricola_inail
 */
public String getMatricola_inail() {
	return matricola_inail;
}
/* 
 * Getter dell'attributo nome
 */
public String getNome() {
	return nome;
}
/* 
 * Getter dell'attributo nome_unita_organizzativa
 */
public String getNome_unita_organizzativa() {
	return nome_unita_organizzativa;
}
/* 
 * Getter dell'attributo note
 */
public String getNote() {
	return note;
}
/* 
 * Getter dell'attributo num_civico_fiscale
 */
public String getNum_civico_fiscale() {
	return num_civico_fiscale;
}
/* 
 * Getter dell'attributo num_iscriz_albo
 */
public String getNum_iscriz_albo() {
	return num_iscriz_albo;
}
/* 
 * Getter dell'attributo num_iscriz_cciaa
 */
public String getNum_iscriz_cciaa() {
	return num_iscriz_cciaa;
}
/* 
 * Getter dell'attributo numero_civico_sede
 */
public String getNumero_civico_sede() {
	return numero_civico_sede;
}
/* 
 * Getter dell'attributo partita_iva
 */
public String getPartita_iva() {
	return partita_iva;
}
/* 
 * Getter dell'attributo pg_comune_fiscale
 */
public Long getPg_comune_fiscale() {
	return pg_comune_fiscale;
}
/* 
 * Getter dell'attributo pg_comune_nascita
 */
public Long getPg_comune_nascita() {
	return pg_comune_nascita;
}
/* 
 * Getter dell'attributo pg_comune_sede
 */
public Long getPg_comune_sede() {
	return pg_comune_sede;
}
/* 
 * Getter dell'attributo pg_nazione_fiscale
 */
public Long getPg_nazione_fiscale() {
	return pg_nazione_fiscale;
}
/* 
 * Getter dell'attributo pg_nazione_nazionalita
 */
public Long getPg_nazione_nazionalita() {
	return pg_nazione_nazionalita;
}
/* 
 * Getter dell'attributo pg_rapp_legale
 */
public Long getPg_rapp_legale() {
	return pg_rapp_legale;
}
/* 
 * Getter dell'attributo ragione_sociale
 */
public String getRagione_sociale() {
	return ragione_sociale;
}
/* 
 * Getter dell'attributo sede_inail
 */
public String getSede_inail() {
	return sede_inail;
}
/* 
 * Getter dell'attributo ti_entita
 */
public String getTi_entita() {
	return ti_entita;
}
/* 
 * Getter dell'attributo ti_entita_fisica
 */
public String getTi_entita_fisica() {
	return ti_entita_fisica;
}
/* 
 * Getter dell'attributo ti_entita_giuridica
 */
public String getTi_entita_giuridica() {
	return ti_entita_giuridica;
}
/* 
 * Getter dell'attributo ti_italiano_estero
 */
public String getTi_italiano_estero() {
	return ti_italiano_estero;
}
/* 
 * Getter dell'attributo ti_sesso
 */
public String getTi_sesso() {
	return ti_sesso;
}
/* 
 * Getter dell'attributo ti_terzo
 */
public String getTi_terzo() {
	return ti_terzo;
}
/* 
 * Getter dell'attributo via_fiscale
 */
public String getVia_fiscale() {
	return via_fiscale;
}
/* 
 * Getter dell'attributo via_sede
 */
public String getVia_sede() {
	return via_sede;
}
/* 
 * Setter dell'attributo aliquota_fiscale
 */
public void setAliquota_fiscale(java.math.BigDecimal aliquota_fiscale) {
	this.aliquota_fiscale = aliquota_fiscale;
}
/* 
 * Setter dell'attributo altra_ass_previd_inps
 */
public void setAltra_ass_previd_inps(String altra_ass_previd_inps) {
	this.altra_ass_previd_inps = altra_ass_previd_inps;
}
/* 
 * Setter dell'attributo cap_comune_fiscale
 */
public void setCap_comune_fiscale(String cap_comune_fiscale) {
	this.cap_comune_fiscale = cap_comune_fiscale;
}
/* 
 * Setter dell'attributo cap_comune_sede
 */
public void setCap_comune_sede(String cap_comune_sede) {
	this.cap_comune_sede = cap_comune_sede;
}
/* 
 * Setter dell'attributo causale_fine_rapporto
 */
public void setCausale_fine_rapporto(String causale_fine_rapporto) {
	this.causale_fine_rapporto = causale_fine_rapporto;
}
/**
 * 
 * @param newCd_anag java.lang.String
 */
public void setCd_anag(Integer newCd_anag) {
	cd_anag = newCd_anag;
}
/* 
 * Setter dell'attributo cd_attivita_inps
 */
public void setCd_attivita_inps(String cd_attivita_inps) {
	this.cd_attivita_inps = cd_attivita_inps;
}
/* 
 * Setter dell'attributo cd_classific_anag
 */
public void setCd_classific_anag(String cd_classific_anag) {
	this.cd_classific_anag = cd_classific_anag;
}
/* 
 * Setter dell'attributo cd_ente_appartenenza
 */
public void setCd_ente_appartenenza(Integer cd_ente_appartenenza) {
	this.cd_ente_appartenenza = cd_ente_appartenenza;
}
/* 
 * Setter dell'attributo cd_precedente
 */
public void setCd_precedente(String cd_precedente) {
	this.cd_precedente = cd_precedente;
}
/* 
 * Setter dell'attributo cd_provincia_fiscale
 */
public void setCd_provincia_fiscale(String cd_provincia_fiscale) {
	this.cd_provincia_fiscale = cd_provincia_fiscale;
}
/* 
 * Setter dell'attributo cd_provincia_nascita
 */
public void setCd_provincia_nascita(String cd_provincia_nascita) {
	this.cd_provincia_nascita = cd_provincia_nascita;
}
/* 
 * Setter dell'attributo cd_provincia_sede
 */
public void setCd_provincia_sede(String cd_provincia_sede) {
	this.cd_provincia_sede = cd_provincia_sede;
}
/* 
 * Setter dell'attributo cd_unita_organizzativa
 */
public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
	this.cd_unita_organizzativa = cd_unita_organizzativa;
}
/* 
 * Setter dell'attributo codice_fiscale
 */
public void setCodice_fiscale(String codice_fiscale) {
	this.codice_fiscale = codice_fiscale;
}
/* 
 * Setter dell'attributo codice_fiscale_caf
 */
public void setCodice_fiscale_caf(String codice_fiscale_caf) {
	this.codice_fiscale_caf = codice_fiscale_caf;
}
/* 
 * Setter dell'attributo cognome
 */
public void setCognome(String cognome) {
	this.cognome = cognome;
}
/* 
 * Setter dell'attributo conto_numerario_credito
 */
public void setConto_numerario_credito(String conto_numerario_credito) {
	this.conto_numerario_credito = conto_numerario_credito;
}
/* 
 * Setter dell'attributo conto_numerario_debito
 */
public void setConto_numerario_debito(String conto_numerario_debito) {
	this.conto_numerario_debito = conto_numerario_debito;
}
/* 
 * Setter dell'attributo denominazione_caf
 */
public void setDenominazione_caf(String denominazione_caf) {
	this.denominazione_caf = denominazione_caf;
}
/* 
 * Setter dell'attributo denominazione_sede
 */
public void setDenominazione_sede(String denominazione_sede) {
	this.denominazione_sede = denominazione_sede;
}
/* 
 * Setter dell'attributo ds_comune_fiscale
 */
public void setDs_comune_fiscale(String ds_comune_fiscale) {
	this.ds_comune_fiscale = ds_comune_fiscale;
}
/* 
 * Setter dell'attributo ds_comune_nascita
 */
public void setDs_comune_nascita(String ds_comune_nascita) {
	this.ds_comune_nascita = ds_comune_nascita;
}
/* 
 * Setter dell'attributo ds_comune_sede
 */
public void setDs_comune_sede(String ds_comune_sede) {
	this.ds_comune_sede = ds_comune_sede;
}
/* 
 * Setter dell'attributo ds_provincia_fiscale
 */
public void setDs_provincia_fiscale(String ds_provincia_fiscale) {
	this.ds_provincia_fiscale = ds_provincia_fiscale;
}
/* 
 * Setter dell'attributo ds_provincia_nascita
 */
public void setDs_provincia_nascita(String ds_provincia_nascita) {
	this.ds_provincia_nascita = ds_provincia_nascita;
}
/* 
 * Setter dell'attributo ds_provincia_sede
 */
public void setDs_provincia_sede(String ds_provincia_sede) {
	this.ds_provincia_sede = ds_provincia_sede;
}
/* 
 * Setter dell'attributo dt_antimafia
 */
public void setDt_antimafia(java.sql.Timestamp dt_antimafia) {
	this.dt_antimafia = dt_antimafia;
}
/* 
 * Setter dell'attributo dt_canc
 */
public void setDt_canc(java.sql.Timestamp dt_canc) {
	this.dt_canc = dt_canc;
}
/* 
 * Setter dell'attributo dt_fine_rapporto
 */
public void setDt_fine_rapporto(java.sql.Timestamp dt_fine_rapporto) {
	this.dt_fine_rapporto = dt_fine_rapporto;
}
/* 
 * Setter dell'attributo dt_nascita
 */
public void setDt_nascita(java.sql.Timestamp dt_nascita) {
	this.dt_nascita = dt_nascita;
}
/* 
 * Setter dell'attributo fl_fatturazione_differita
 */
public void setFl_fatturazione_differita(Boolean fl_fatturazione_differita) {
	this.fl_fatturazione_differita = fl_fatturazione_differita;
}
/* 
 * Setter dell'attributo fl_occasionale
 */
public void setFl_occasionale(Boolean fl_occasionale) {
	this.fl_occasionale = fl_occasionale;
}
/* 
 * Setter dell'attributo fl_soggetto_iva
 */
public void setFl_soggetto_iva(Boolean fl_soggetto_iva) {
	this.fl_soggetto_iva = fl_soggetto_iva;
}
/* 
 * Setter dell'attributo frazione_fiscale
 */
public void setFrazione_fiscale(String frazione_fiscale) {
	this.frazione_fiscale = frazione_fiscale;
}
/* 
 * Setter dell'attributo frazione_sede
 */
public void setFrazione_sede(String frazione_sede) {
	this.frazione_sede = frazione_sede;
}
/* 
 * Setter dell'attributo id_fiscale_estero
 */
public void setId_fiscale_estero(String id_fiscale_estero) {
	this.id_fiscale_estero = id_fiscale_estero;
}
/* 
 * Setter dell'attributo matricola_inail
 */
public void setMatricola_inail(String matricola_inail) {
	this.matricola_inail = matricola_inail;
}
/* 
 * Setter dell'attributo nome
 */
public void setNome(String nome) {
	this.nome = nome;
}
/* 
 * Setter dell'attributo nome_unita_organizzativa
 */
public void setNome_unita_organizzativa(String nome_unita_organizzativa) {
	this.nome_unita_organizzativa = nome_unita_organizzativa;
}
/* 
 * Setter dell'attributo note
 */
public void setNote(String note) {
	this.note = note;
}
/* 
 * Setter dell'attributo num_civico_fiscale
 */
public void setNum_civico_fiscale(String num_civico_fiscale) {
	this.num_civico_fiscale = num_civico_fiscale;
}
/* 
 * Setter dell'attributo num_iscriz_albo
 */
public void setNum_iscriz_albo(String num_iscriz_albo) {
	this.num_iscriz_albo = num_iscriz_albo;
}
/* 
 * Setter dell'attributo num_iscriz_cciaa
 */
public void setNum_iscriz_cciaa(String num_iscriz_cciaa) {
	this.num_iscriz_cciaa = num_iscriz_cciaa;
}
/* 
 * Setter dell'attributo numero_civico_sede
 */
public void setNumero_civico_sede(String numero_civico_sede) {
	this.numero_civico_sede = numero_civico_sede;
}
/* 
 * Setter dell'attributo partita_iva
 */
public void setPartita_iva(String partita_iva) {
	this.partita_iva = partita_iva;
}
/* 
 * Setter dell'attributo pg_comune_fiscale
 */
public void setPg_comune_fiscale(Long pg_comune_fiscale) {
	this.pg_comune_fiscale = pg_comune_fiscale;
}
/* 
 * Setter dell'attributo pg_comune_nascita
 */
public void setPg_comune_nascita(Long pg_comune_nascita) {
	this.pg_comune_nascita = pg_comune_nascita;
}
/* 
 * Setter dell'attributo pg_comune_sede
 */
public void setPg_comune_sede(Long pg_comune_sede) {
	this.pg_comune_sede = pg_comune_sede;
}
/* 
 * Setter dell'attributo pg_nazione_fiscale
 */
public void setPg_nazione_fiscale(Long pg_nazione_fiscale) {
	this.pg_nazione_fiscale = pg_nazione_fiscale;
}
/* 
 * Setter dell'attributo pg_nazione_nazionalita
 */
public void setPg_nazione_nazionalita(Long pg_nazione_nazionalita) {
	this.pg_nazione_nazionalita = pg_nazione_nazionalita;
}
/* 
 * Setter dell'attributo pg_rapp_legale
 */
public void setPg_rapp_legale(Long pg_rapp_legale) {
	this.pg_rapp_legale = pg_rapp_legale;
}
/* 
 * Setter dell'attributo ragione_sociale
 */
public void setRagione_sociale(String ragione_sociale) {
	this.ragione_sociale = ragione_sociale;
}
/* 
 * Setter dell'attributo sede_inail
 */
public void setSede_inail(String sede_inail) {
	this.sede_inail = sede_inail;
}
/* 
 * Setter dell'attributo ti_entita
 */
public void setTi_entita(String ti_entita) {
	this.ti_entita = ti_entita;
}
/* 
 * Setter dell'attributo ti_entita_fisica
 */
public void setTi_entita_fisica(String ti_entita_fisica) {
	this.ti_entita_fisica = ti_entita_fisica;
}
/* 
 * Setter dell'attributo ti_entita_giuridica
 */
public void setTi_entita_giuridica(String ti_entita_giuridica) {
	this.ti_entita_giuridica = ti_entita_giuridica;
}
/* 
 * Setter dell'attributo ti_italiano_estero
 */
public void setTi_italiano_estero(String ti_italiano_estero) {
	this.ti_italiano_estero = ti_italiano_estero;
}
/* 
 * Setter dell'attributo ti_sesso
 */
public void setTi_sesso(String ti_sesso) {
	this.ti_sesso = ti_sesso;
}
/* 
 * Setter dell'attributo ti_terzo
 */
public void setTi_terzo(String ti_terzo) {
	this.ti_terzo = ti_terzo;
}
/* 
 * Setter dell'attributo via_fiscale
 */
public void setVia_fiscale(String via_fiscale) {
	this.via_fiscale = via_fiscale;
}
/* 
 * Setter dell'attributo via_sede
 */
public void setVia_sede(String via_sede) {
	this.via_sede = via_sede;
}

	public Boolean getFlFirmatarioContratto() {
		return flFirmatarioContratto;
	}

	public void setFlFirmatarioContratto(Boolean flFirmatarioContratto) {
		this.flFirmatarioContratto = flFirmatarioContratto;
	}
}

