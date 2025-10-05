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

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

public class V_cons_dett_res_speBulk extends OggettoBulk implements Persistent {

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

    // CD_ELEMENTO_VOCE VARCHAR(20)
    private java.lang.String cd_elemento_voce;

    // DESCRIZIONE_VOCE VARCHAR(300)
    private java.lang.String descrizione_voce;

    // CDS_OBB VARCHAR(30)
    private java.lang.String cds_obb;

    // PG_OBB DECIMAL(10,0)
    private java.lang.Long pg_obb;

    // DS_OBBLIGAZIONE VARCHAR(300)
    private java.lang.String ds_obbligazione;

    // PG_OBB_SCAD DECIMAL(10,0)
    private java.lang.Long pg_obb_scad;

    // DS_SCADENZA VARCHAR(300)
    private java.lang.String ds_scadenza;

    // CD_TIPO_DOCUMENTO_CONT VARCHAR(10)
    private java.lang.String cd_tipo_documento_cont;

    // DESCR_MODIFICA VARCHAR(4000)
    private java.lang.String descrModifica;

    // VAR_PIU_OBB_RES_PRO DECIMAL(15,2)
    private java.math.BigDecimal var_piu_obb_res_pro;

    // VAR_MENO_OBB_RES_PRO DECIMAL(15,2)
    private java.math.BigDecimal var_meno_obb_res_pro;

    // IM_IMPROPRIO DECIMAL(15,2)
    private java.math.BigDecimal im_improprio;

    // IM_PROPRIO DECIMAL(15,2)
    private java.math.BigDecimal im_proprio;

    // PAGATO_RES_PROPRIO DECIMAL(15,2)
    private java.math.BigDecimal pagato_res_proprio;

    // PAGATO_RES_IMPROPRIO DECIMAL(15,2)
    private java.math.BigDecimal pagato_res_improprio;

    // IMP_DOC_RES_PROPRIO DECIMAL(15,2)
    private java.math.BigDecimal imp_doc_res_proprio;

    // IMP_DOC_RES_IMPROPRIO DECIMAL(15,2)
    private java.math.BigDecimal imp_doc_res_improprio;

    public V_cons_dett_res_speBulk() {
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

    public java.lang.String getDescrizione_voce() {
        return descrizione_voce;
    }

    public void setDescrizione_voce(java.lang.String descrizione_voce) {
        this.descrizione_voce = descrizione_voce;
    }

    public java.lang.String getCds_obb() {
        return cds_obb;
    }

    public void setCds_obb(java.lang.String cds_obb) {
        this.cds_obb = cds_obb;
    }

    public java.lang.Long getPg_obb() {
        return pg_obb;
    }

    public void setPg_obb(java.lang.Long pg_obb) {
        this.pg_obb = pg_obb;
    }

    public java.lang.String getDs_obbligazione() {
        return ds_obbligazione;
    }

    public void setDs_obbligazione(java.lang.String ds_obbligazione) {
        this.ds_obbligazione = ds_obbligazione;
    }

    public java.lang.Long getPg_obb_scad() {
        return pg_obb_scad;
    }

    public void setPg_obb_scad(java.lang.Long pg_obb_scad) {
        this.pg_obb_scad = pg_obb_scad;
    }

    public java.lang.String getDs_scadenza() {
        return ds_scadenza;
    }

    public void setDs_scadenza(java.lang.String ds_scadenza) {
        this.ds_scadenza = ds_scadenza;
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

    public java.math.BigDecimal getVar_piu_obb_res_pro() {
        return var_piu_obb_res_pro;
    }

    public void setVar_piu_obb_res_pro(java.math.BigDecimal var_piu_obb_res_pro) {
        this.var_piu_obb_res_pro = var_piu_obb_res_pro;
    }

    public java.math.BigDecimal getVar_meno_obb_res_pro() {
        return var_meno_obb_res_pro;
    }

    public void setVar_meno_obb_res_pro(java.math.BigDecimal var_meno_obb_res_pro) {
        this.var_meno_obb_res_pro = var_meno_obb_res_pro;
    }

    public java.math.BigDecimal getIm_improprio() {
        return im_improprio;
    }

    public void setIm_improprio(java.math.BigDecimal im_improprio) {
        this.im_improprio = im_improprio;
    }

    public java.math.BigDecimal getIm_proprio() {
        return im_proprio;
    }

    public void setIm_proprio(java.math.BigDecimal im_proprio) {
        this.im_proprio = im_proprio;
    }

    public java.math.BigDecimal getPagato_res_proprio() {
        return pagato_res_proprio;
    }

    public void setPagato_res_proprio(java.math.BigDecimal pagato_res_proprio) {
        this.pagato_res_proprio = pagato_res_proprio;
    }

    public java.math.BigDecimal getPagato_res_improprio() {
        return pagato_res_improprio;
    }

    public void setPagato_res_improprio(java.math.BigDecimal pagato_res_improprio) {
        this.pagato_res_improprio = pagato_res_improprio;
    }

    public java.math.BigDecimal getImp_doc_res_proprio() {
        return imp_doc_res_proprio;
    }

    public void setImp_doc_res_proprio(java.math.BigDecimal imp_doc_res_proprio) {
        this.imp_doc_res_proprio = imp_doc_res_proprio;
    }

    public java.math.BigDecimal getImp_doc_res_improprio() {
        return imp_doc_res_improprio;
    }

    public void setImp_doc_res_improprio(java.math.BigDecimal imp_doc_res_improprio) {
        this.imp_doc_res_improprio = imp_doc_res_improprio;
    }
}