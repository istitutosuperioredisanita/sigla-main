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

package it.cnr.contab.docamm00.docs.bulk;

import it.cnr.jada.persistency.*;

public class Fattura_attiva_rigaBase extends Fattura_attiva_rigaKey implements Keyed {
	// CD_CDS_ACCERTAMENTO VARCHAR(30)
	private java.lang.String cd_cds_accertamento;

	// CD_CDS_ASSNCNA_FIN VARCHAR(30)
	private java.lang.String cd_cds_assncna_fin;

	// CD_CDS_OBBLIGAZIONE VARCHAR(30)
	private java.lang.String cd_cds_obbligazione;

	// CD_TARIFFARIO VARCHAR(10)
	private java.lang.String cd_tariffario;

	// CD_UO_ASSNCNA_FIN VARCHAR(30)
	private java.lang.String cd_uo_assncna_fin;

	// CD_VOCE_IVA VARCHAR(10) NOT NULL
	private java.lang.String cd_voce_iva;

	// DS_RIGA_FATTURA VARCHAR(200)
	private java.lang.String ds_riga_fattura;

	// DT_A_COMPETENZA_COGE TIMESTAMP NOT NULL
	private java.sql.Timestamp dt_a_competenza_coge;

	// DT_CANCELLAZIONE TIMESTAMP
	private java.sql.Timestamp dt_cancellazione;

	// DT_DA_COMPETENZA_COGE TIMESTAMP NOT NULL
	private java.sql.Timestamp dt_da_competenza_coge;

	// ESERCIZIO_ACCERTAMENTO DECIMAL(4,0)
	private java.lang.Integer esercizio_accertamento;

	// ESERCIZIO_ASSNCNA_FIN DECIMAL(4,0)
	private java.lang.Integer esercizio_assncna_fin;

	// ESERCIZIO_OBBLIGAZIONE DECIMAL(4,0)
	private java.lang.Integer esercizio_obbligazione;

	// FL_IVA_FORZATA CHAR(1) NOT NULL
	private java.lang.Boolean fl_iva_forzata;

	// IM_DIPONIBILE_NC DECIMAL(15,2)
	private java.math.BigDecimal im_diponibile_nc;

	// IM_IMPONIBILE DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal im_imponibile;

	// IM_IVA DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal im_iva;

	// IM_TOTALE_DIVISA DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal im_totale_divisa;

	// ESERCIZIO_ORI_ACCERTAMENTO DECIMAL(4,0)
	private java.lang.Integer esercizio_ori_accertamento;

	// PG_ACCERTAMENTO DECIMAL(10,0)
	private java.lang.Long pg_accertamento;

	// PG_ACCERTAMENTO_SCADENZARIO DECIMAL(10,0)
	private java.lang.Long pg_accertamento_scadenzario;

	// PG_FATTURA_ASSNCNA_FIN DECIMAL(10,0)
	private java.lang.Long pg_fattura_assncna_fin;

	// ESERCIZIO_ORI_OBBLIGAZIONE DECIMAL(4,0)
	private java.lang.Integer esercizio_ori_obbligazione;

	// PG_OBBLIGAZIONE DECIMAL(10,0)
	private java.lang.Long pg_obbligazione;

	// PG_OBBLIGAZIONE_SCADENZARIO DECIMAL(10,0)
	private java.lang.Long pg_obbligazione_scadenzario;

	// PG_RIGA_ASSNCNA_FIN DECIMAL(10,0)
	private java.lang.Long pg_riga_assncna_fin;

	// PREZZO_UNITARIO DECIMAL(20,6) NOT NULL
	private java.math.BigDecimal prezzo_unitario;

	// QUANTITA DECIMAL(15,2) NOT NULL
	private java.math.BigDecimal quantita;

	// STATO_COFI CHAR(1) NOT NULL
	private java.lang.String stato_cofi;

	// TI_ASSOCIATO_MANREV CHAR(1) NOT NULL
	private java.lang.String ti_associato_manrev;

	private java.sql.Timestamp data_esigibilita_iva;
	
	private java.lang.String cd_bene_servizio;

	// PG_TROVATO DECIMAL(10,0)
	private java.lang.Long pg_trovato;

	// ESERCIZIO_VOCE_EP DECIMAL(4,0) NOT NULL
	private Integer esercizio_voce_ep;

	// CD_VOCE_EP VARCHAR(45) NOT NULL
	private String cd_voce_ep;

	public Fattura_attiva_rigaBase() {
		super();
	}

	public Fattura_attiva_rigaBase(java.lang.String cd_cds,java.lang.String cd_unita_organizzativa,java.lang.Integer esercizio,java.lang.Long pg_fattura_attiva,java.lang.Long progressivo_riga) {
		super(cd_cds,cd_unita_organizzativa,esercizio,pg_fattura_attiva,progressivo_riga);
	}
	public java.sql.Timestamp getData_esigibilita_iva() {
		return data_esigibilita_iva;
	}

	public void setData_esigibilita_iva( java.sql.Timestamp data_esigibilita_iva) {
		this.data_esigibilita_iva = data_esigibilita_iva;
	}

	/*
	 * Getter dell'attributo cd_cds_accertamento
	 */
	public java.lang.String getCd_cds_accertamento() {
		return cd_cds_accertamento;
	}

	/*
	 * Getter dell'attributo cd_cds_assncna_fin
	 */
	public java.lang.String getCd_cds_assncna_fin() {
		return cd_cds_assncna_fin;
	}

	/*
	 * Getter dell'attributo cd_cds_obbligazione
	 */
	public java.lang.String getCd_cds_obbligazione() {
		return cd_cds_obbligazione;
	}

	/*
	 * Getter dell'attributo cd_tariffario
	 */
	public java.lang.String getCd_tariffario() {
		return cd_tariffario;
	}

	/*
	 * Getter dell'attributo cd_uo_assncna_fin
	 */
	public java.lang.String getCd_uo_assncna_fin() {
		return cd_uo_assncna_fin;
	}

	/*
	 * Getter dell'attributo cd_voce_iva
	 */
	public java.lang.String getCd_voce_iva() {
		return cd_voce_iva;
	}

	/*
	 * Getter dell'attributo ds_riga_fattura
	 */
	public java.lang.String getDs_riga_fattura() {
		return ds_riga_fattura;
	}

	/*
	 * Getter dell'attributo dt_a_competenza_coge
	 */
	public java.sql.Timestamp getDt_a_competenza_coge() {
		return dt_a_competenza_coge;
	}

	/*
	 * Getter dell'attributo dt_cancellazione
	 */
	public java.sql.Timestamp getDt_cancellazione() {
		return dt_cancellazione;
	}

	/*
	 * Getter dell'attributo dt_da_competenza_coge
	 */
	public java.sql.Timestamp getDt_da_competenza_coge() {
		return dt_da_competenza_coge;
	}

	/*
	 * Getter dell'attributo esercizio_accertamento
	 */
	public java.lang.Integer getEsercizio_accertamento() {
		return esercizio_accertamento;
	}

	/*
	 * Getter dell'attributo esercizio_assncna_fin
	 */
	public java.lang.Integer getEsercizio_assncna_fin() {
		return esercizio_assncna_fin;
	}

	/*
	 * Getter dell'attributo esercizio_obbligazione
	 */
	public java.lang.Integer getEsercizio_obbligazione() {
		return esercizio_obbligazione;
	}

	/*
	 * Getter dell'attributo fl_iva_forzata
	 */
	public java.lang.Boolean getFl_iva_forzata() {
		return fl_iva_forzata;
	}

	/*
	 * Getter dell'attributo im_diponibile_nc
	 */
	public java.math.BigDecimal getIm_diponibile_nc() {
		return im_diponibile_nc;
	}

	/*
	 * Getter dell'attributo im_imponibile
	 */
	public java.math.BigDecimal getIm_imponibile() {
		return im_imponibile;
	}

	/*
	 * Getter dell'attributo im_iva
	 */
	public java.math.BigDecimal getIm_iva() {
		return im_iva;
	}

	/*
	 * Getter dell'attributo im_totale_divisa
	 */
	public java.math.BigDecimal getIm_totale_divisa() {
		return im_totale_divisa;
	}

	/*
	 * Getter dell'attributo esercizio_ori_accertamento
	 */
	public java.lang.Integer getEsercizio_ori_accertamento() {
		return esercizio_ori_accertamento;
	}

	/*
	 * Getter dell'attributo pg_accertamento
	 */
	public java.lang.Long getPg_accertamento() {
		return pg_accertamento;
	}

	/*
	 * Getter dell'attributo pg_accertamento_scadenzario
	 */
	public java.lang.Long getPg_accertamento_scadenzario() {
		return pg_accertamento_scadenzario;
	}

	/*
	 * Getter dell'attributo pg_fattura_assncna_fin
	 */
	public java.lang.Long getPg_fattura_assncna_fin() {
		return pg_fattura_assncna_fin;
	}

	/*
	 * Getter dell'attributo esercizio_ori_obbligazione
	 */
	public java.lang.Integer getEsercizio_ori_obbligazione() {
		return esercizio_ori_obbligazione;
	}

	/*
	 * Getter dell'attributo pg_obbligazione
	 */
	public java.lang.Long getPg_obbligazione() {
		return pg_obbligazione;
	}

	/*
	 * Getter dell'attributo pg_obbligazione_scadenzario
	 */
	public java.lang.Long getPg_obbligazione_scadenzario() {
		return pg_obbligazione_scadenzario;
	}

	/*
	 * Getter dell'attributo pg_riga_assncna_fin
	 */
	public java.lang.Long getPg_riga_assncna_fin() {
		return pg_riga_assncna_fin;
	}

	/*
	 * Getter dell'attributo prezzo_unitario
	 */
	public java.math.BigDecimal getPrezzo_unitario() {
		return prezzo_unitario;
	}

	/*
	 * Getter dell'attributo quantita
	 */
	public java.math.BigDecimal getQuantita() {
		return quantita;
	}

	/*
	 * Getter dell'attributo stato_cofi
	 */
	public java.lang.String getStato_cofi() {
		return stato_cofi;
	}

	/*
	 * Getter dell'attributo ti_associato_manrev
	 */
	public java.lang.String getTi_associato_manrev() {
		return ti_associato_manrev;
	}

	/*
	 * Setter dell'attributo cd_cds_accertamento
	 */
	public void setCd_cds_accertamento(java.lang.String cd_cds_accertamento) {
		this.cd_cds_accertamento = cd_cds_accertamento;
	}

	/*
	 * Setter dell'attributo cd_cds_assncna_fin
	 */
	public void setCd_cds_assncna_fin(java.lang.String cd_cds_assncna_fin) {
		this.cd_cds_assncna_fin = cd_cds_assncna_fin;
	}

	/*
	 * Setter dell'attributo cd_cds_obbligazione
	 */
	public void setCd_cds_obbligazione(java.lang.String cd_cds_obbligazione) {
		this.cd_cds_obbligazione = cd_cds_obbligazione;
	}

	/*
	 * Setter dell'attributo cd_tariffario
	 */
	public void setCd_tariffario(java.lang.String cd_tariffario) {
		this.cd_tariffario = cd_tariffario;
	}

	/*
	 * Setter dell'attributo cd_uo_assncna_fin
	 */
	public void setCd_uo_assncna_fin(java.lang.String cd_uo_assncna_fin) {
		this.cd_uo_assncna_fin = cd_uo_assncna_fin;
	}

	/*
	 * Setter dell'attributo cd_voce_iva
	 */
	public void setCd_voce_iva(java.lang.String cd_voce_iva) {
		this.cd_voce_iva = cd_voce_iva;
	}

	/*
	 * Setter dell'attributo ds_riga_fattura
	 */
	public void setDs_riga_fattura(java.lang.String ds_riga_fattura) {
		this.ds_riga_fattura = ds_riga_fattura;
	}

	/*
	 * Setter dell'attributo dt_a_competenza_coge
	 */
	public void setDt_a_competenza_coge(java.sql.Timestamp dt_a_competenza_coge) {
		this.dt_a_competenza_coge = dt_a_competenza_coge;
	}

	/*
	 * Setter dell'attributo dt_cancellazione
	 */
	public void setDt_cancellazione(java.sql.Timestamp dt_cancellazione) {
		this.dt_cancellazione = dt_cancellazione;
	}

	/*
	 * Setter dell'attributo dt_da_competenza_coge
	 */
	public void setDt_da_competenza_coge(java.sql.Timestamp dt_da_competenza_coge) {
		this.dt_da_competenza_coge = dt_da_competenza_coge;
	}

	/*
	 * Setter dell'attributo esercizio_accertamento
	 */
	public void setEsercizio_accertamento(java.lang.Integer esercizio_accertamento) {
		this.esercizio_accertamento = esercizio_accertamento;
	}

	/*
	 * Setter dell'attributo esercizio_assncna_fin
	 */
	public void setEsercizio_assncna_fin(java.lang.Integer esercizio_assncna_fin) {
		this.esercizio_assncna_fin = esercizio_assncna_fin;
	}

	/*
	 * Setter dell'attributo esercizio_obbligazione
	 */
	public void setEsercizio_obbligazione(java.lang.Integer esercizio_obbligazione) {
		this.esercizio_obbligazione = esercizio_obbligazione;
	}

	/*
	 * Setter dell'attributo fl_iva_forzata
	 */
	public void setFl_iva_forzata(java.lang.Boolean fl_iva_forzata) {
		this.fl_iva_forzata = fl_iva_forzata;
	}

	/*
	 * Setter dell'attributo im_diponibile_nc
	 */
	public void setIm_diponibile_nc(java.math.BigDecimal im_diponibile_nc) {
		this.im_diponibile_nc = im_diponibile_nc;
	}

	/*
	 * Setter dell'attributo im_imponibile
	 */
	public void setIm_imponibile(java.math.BigDecimal im_imponibile) {
		this.im_imponibile = im_imponibile;
	}

	/*
	 * Setter dell'attributo im_iva
	 */
	public void setIm_iva(java.math.BigDecimal im_iva) {
		this.im_iva = im_iva;
	}

	/*
	 * Setter dell'attributo im_totale_divisa
	 */
	public void setIm_totale_divisa(java.math.BigDecimal im_totale_divisa) {
		this.im_totale_divisa = im_totale_divisa;
	}

	/*
	 * Setter dell'attributo esercizio_ori_accertamento
	 */
	public void setEsercizio_ori_accertamento(java.lang.Integer esercizio_ori_accertamento) {
		this.esercizio_ori_accertamento = esercizio_ori_accertamento;
	}

	/*
	 * Setter dell'attributo pg_accertamento
	 */
	public void setPg_accertamento(java.lang.Long pg_accertamento) {
		this.pg_accertamento = pg_accertamento;
	}

	/*
	 * Setter dell'attributo pg_accertamento_scadenzario
	 */
	public void setPg_accertamento_scadenzario(java.lang.Long pg_accertamento_scadenzario) {
		this.pg_accertamento_scadenzario = pg_accertamento_scadenzario;
	}

	/*
	 * Setter dell'attributo pg_fattura_assncna_fin
	 */
	public void setPg_fattura_assncna_fin(java.lang.Long pg_fattura_assncna_fin) {
		this.pg_fattura_assncna_fin = pg_fattura_assncna_fin;
	}

	/*
	 * Setter dell'attributo esercizio_ori_obbligazione
	 */
	public void setEsercizio_ori_obbligazione(java.lang.Integer esercizio_ori_obbligazione) {
		this.esercizio_ori_obbligazione = esercizio_ori_obbligazione;
	}

	/*
	 * Setter dell'attributo pg_obbligazione
	 */
	public void setPg_obbligazione(java.lang.Long pg_obbligazione) {
		this.pg_obbligazione = pg_obbligazione;
	}

	/*
	 * Setter dell'attributo pg_obbligazione_scadenzario
	 */
	public void setPg_obbligazione_scadenzario(java.lang.Long pg_obbligazione_scadenzario) {
		this.pg_obbligazione_scadenzario = pg_obbligazione_scadenzario;
	}

	/*
	 * Setter dell'attributo pg_riga_assncna_fin
	 */
	public void setPg_riga_assncna_fin(java.lang.Long pg_riga_assncna_fin) {
		this.pg_riga_assncna_fin = pg_riga_assncna_fin;
	}

	/*
	 * Setter dell'attributo prezzo_unitario
	 */
	public void setPrezzo_unitario(java.math.BigDecimal prezzo_unitario) {
		this.prezzo_unitario = prezzo_unitario;
	}

	/*
	 * Setter dell'attributo quantita
	 */
	public void setQuantita(java.math.BigDecimal quantita) {
		this.quantita = quantita;
	}

	/*
	 * Setter dell'attributo stato_cofi
	 */
	public void setStato_cofi(java.lang.String stato_cofi) {
		this.stato_cofi = stato_cofi;
	}

	/*
	 * Setter dell'attributo ti_associato_manrev
	 */
	public void setTi_associato_manrev(java.lang.String ti_associato_manrev) {
		this.ti_associato_manrev = ti_associato_manrev;
	}

	public java.lang.String getCd_bene_servizio() {
		return cd_bene_servizio;
	}

	public void setCd_bene_servizio(java.lang.String cd_bene_servizio) {
		this.cd_bene_servizio = cd_bene_servizio;
	}

	public void setPg_trovato(java.lang.Long pg_trovato) {
		this.pg_trovato = pg_trovato;
	}

	public java.lang.Long getPg_trovato() {
		return pg_trovato;
	}

	public Integer getEsercizio_voce_ep() {
		return esercizio_voce_ep;
	}

	public void setEsercizio_voce_ep(Integer esercizio_voce_ep) {
		this.esercizio_voce_ep = esercizio_voce_ep;
	}

	public String getCd_voce_ep() {
		return cd_voce_ep;
	}

	public void setCd_voce_ep(String cd_voce_ep) {
		this.cd_voce_ep = cd_voce_ep;
	}
}