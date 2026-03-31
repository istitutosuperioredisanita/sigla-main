/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

public class V_cons_acc_dett_modBulk extends OggettoBulk implements Persistent {

    // ESERCIZIO DECIMAL(4,0)
    private java.lang.Integer esercizio;

    // ESERCIZIO_RES DECIMAL(4,0)
    private java.lang.Integer esercizio_res;

    // CD_CENTRO_RESPONSABILITA VARCHAR(30)
    private java.lang.String cd_centro_responsabilita;

    // TI_APPARTENENZA CHAR(1)
    private java.lang.String ti_appartenenza;

    // TI_GESTIONE CHAR(1)
    private java.lang.String ti_gestione;

    // CD_ELEMENTO_VOCE VARCHAR(45)
    private java.lang.String cd_elemento_voce;

    // DS_ELEMENTO_VOCE VARCHAR(300)
    private java.lang.String ds_elemento_voce;

    // CDS_ACCERTAMENTO VARCHAR(30)
    private java.lang.String cds_accertamento;

    // PG_ACCERTAMENTO DECIMAL(10,0)
    private java.lang.Long pg_accertamento;

    // DS_ACCERTAMENTO VARCHAR(500)
    private java.lang.String ds_accertamento;

    // PG_MODIFICA DECIMAL(10,0)
    private java.lang.Long pg_modifica;

    // CD_TIPO_DOCUMENTO_CONT VARCHAR(10)
    private java.lang.String cd_tipo_documento_cont;

    // DS_VAR_RES_PRO VARCHAR(300)
    private java.lang.String descrModifica;

    // VAR_PIU_ACC_RES_PRO DECIMAL(15,2)
    private java.math.BigDecimal variazioni_piu;

    // VAR_MENO_ACC_RES_PRO DECIMAL(15,2)
    private java.math.BigDecimal variazioni_meno;

    // IM_OBBL_RES_PRO DECIMAL(15,2)
    private java.math.BigDecimal im_accertamento;

    public V_cons_acc_dett_modBulk() {
        super();
    }

    // Getters and Setters

    public java.lang.Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(java.lang.Integer esercizio) {
        this.esercizio = esercizio;
    }

    public java.lang.Integer getEsercizio_res() {
        return esercizio_res;
    }

    public void setEsercizio_res(java.lang.Integer esercizio_res) {
        this.esercizio_res = esercizio_res;
    }

    public java.lang.String getCd_centro_responsabilita() {
        return cd_centro_responsabilita;
    }

    public void setCd_centro_responsabilita(java.lang.String cd_centro_responsabilita) {
        this.cd_centro_responsabilita = cd_centro_responsabilita;
    }

    public java.lang.String getTi_appartenenza() {
        return ti_appartenenza;
    }

    public void setTi_appartenenza(java.lang.String ti_appartenenza) {
        this.ti_appartenenza = ti_appartenenza;
    }

    public java.lang.String getTi_gestione() {
        return ti_gestione;
    }

    public void setTi_gestione(java.lang.String ti_gestione) {
        this.ti_gestione = ti_gestione;
    }

    public java.lang.String getCd_elemento_voce() {
        return cd_elemento_voce;
    }

    public void setCd_elemento_voce(java.lang.String cd_elemento_voce) {
        this.cd_elemento_voce = cd_elemento_voce;
    }

    public java.lang.String getDs_elemento_voce() {
        return ds_elemento_voce;
    }

    public void setDs_elemento_voce(java.lang.String ds_elemento_voce) {
        this.ds_elemento_voce = ds_elemento_voce;
    }

    public java.lang.String getCds_accertamento() {
        return cds_accertamento;
    }

    public void setCds_accertamento(java.lang.String cds_accertamento) {
        this.cds_accertamento = cds_accertamento;
    }

    public java.lang.Long getPg_accertamento() {
        return pg_accertamento;
    }

    public void setPg_accertamento(java.lang.Long pg_accertamento) {
        this.pg_accertamento = pg_accertamento;
    }

    public java.lang.String getDs_accertamento() {
        return ds_accertamento;
    }

    public void setDs_accertamento(java.lang.String ds_accertamento) {
        this.ds_accertamento = ds_accertamento;
    }

    public java.lang.Long getPg_modifica() {
        return pg_modifica;
    }

    public void setPg_modifica(java.lang.Long pg_modifica) {
        this.pg_modifica = pg_modifica;
    }

    public java.lang.String getCd_tipo_documento_cont() {
        return cd_tipo_documento_cont;
    }

    public void setCd_tipo_documento_cont(java.lang.String cd_tipo_documento_cont) {
        this.cd_tipo_documento_cont = cd_tipo_documento_cont;
    }

    public java.lang.String getDescrModifica() {
        return descrModifica;
    }

    public void setDescrModifica(java.lang.String descrModifica) {
        this.descrModifica = descrModifica;
    }

    public java.math.BigDecimal getVariazioni_piu() {
        return variazioni_piu;
    }

    public void setVariazioni_piu(java.math.BigDecimal variazioni_piu) {
        this.variazioni_piu = variazioni_piu;
    }

    public java.math.BigDecimal getVariazioni_meno() {
        return variazioni_meno;
    }

    public void setVariazioni_meno(java.math.BigDecimal variazioni_meno) {
        this.variazioni_meno = variazioni_meno;
    }

    public java.math.BigDecimal getIm_accertamento() {
        return im_accertamento;
    }

    public void setIm_accertamento(java.math.BigDecimal im_accertamento) {
        this.im_accertamento = im_accertamento;
    }
}