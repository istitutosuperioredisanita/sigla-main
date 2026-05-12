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

/*
 * Created by Generator 1.0
 * Date 02/05/2005
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.pdg01.bulk.Pdg_modulo_entrate_gestBulk;

import java.math.BigDecimal;

public class V_pdg_accertamento_etrBulk extends V_pdg_accertamento_etrBase {
    private BigDecimal prcImputazioneFin;

    public V_pdg_accertamento_etrBulk() {
        super();
    }


    public V_pdg_accertamento_etrBulk(BigDecimal prcImputazioneFin) {
        this.prcImputazioneFin = prcImputazioneFin;
    }

    public V_pdg_accertamento_etrBulk(Integer esercizio, Integer esercizio_res, String cd_centro_responsabilita, String ti_appartenenza, String ti_gestione, String cd_elemento_voce, String cd_linea_attivita, String cd_progetto, String cd_progetto_padre, String cd_funzione, String cd_natura, String ds_linea_attivita, BigDecimal im_ra_rce, BigDecimal im_rb_rse, BigDecimal im_rc_esr, BigDecimal im_rd_a2_ricavi, BigDecimal im_re_a2_entrate, BigDecimal im_rf_a3_ricavi, BigDecimal im_rg_a3_entrate, String cd_centro_responsabilita_clgs, String cd_linea_attivita_clgs, String ti_appartenenza_clgs, String ti_gestione_clgs, String cd_elemento_voce_clgs, String stato, String categoria_dettaglio, BigDecimal prcImputazioneFin) {
        super(esercizio, esercizio_res, cd_centro_responsabilita, ti_appartenenza, ti_gestione, cd_elemento_voce, cd_linea_attivita, cd_progetto, cd_progetto_padre, cd_funzione, cd_natura, ds_linea_attivita, im_ra_rce, im_rb_rse, im_rc_esr, im_rd_a2_ricavi, im_re_a2_entrate, im_rf_a3_ricavi, im_rg_a3_entrate, cd_centro_responsabilita_clgs, cd_linea_attivita_clgs, ti_appartenenza_clgs, ti_gestione_clgs, cd_elemento_voce_clgs, stato, categoria_dettaglio);
        this.prcImputazioneFin = prcImputazioneFin;
    }

    public BigDecimal getImporto() {
        if (it.cnr.contab.pdg00.bulk.Pdg_preventivo_spe_detBulk.CAT_SINGOLO.equals(getCategoria_dettaglio()) ||
                Pdg_modulo_entrate_gestBulk.CAT_DIRETTA.equals(getCategoria_dettaglio()))
            return getIm_ra_rce().add(getIm_rc_esr());
        else if (it.cnr.contab.pdg00.bulk.Pdg_preventivo_spe_detBulk.CAT_SCARICO.equals(getCategoria_dettaglio()))
            return getIm_rb_rse();
        return new BigDecimal(0);
    }

    /**
     * <!-- @TODO: da completare -->
     * Restituisce il valore della proprietà 'prcImputazioneFin'
     *
     * @return Il valore della proprietà 'prcImputazioneFin'
     */
    public BigDecimal getPrcImputazioneFin() {
        return prcImputazioneFin;
    }

    /**
     * <!-- @TODO: da completare -->
     * Imposta il valore della proprietà 'prcImputazioneFin'
     *
     * @param newPrcImputazioneFin Il valore da assegnare a 'prcImputazioneFin'
     */
    public void setPrcImputazioneFin(BigDecimal newPrcImputazioneFin) {
        prcImputazioneFin = newPrcImputazioneFin;
    }

    @Override
    public V_pdg_accertamento_etrBulk clone() {

        return new V_pdg_accertamento_etrBulk(this.getEsercizio(), this.getEsercizio_res(), this.getCd_centro_responsabilita(), this.getTi_appartenenza(), this.getTi_gestione(), this.getCd_elemento_voce(), this.getCd_linea_attivita(), this.getCd_progetto(),
                this.getCd_progetto_padre(), this.getCd_funzione(), this.getCd_natura(), this.getDs_linea_attivita(), this.getIm_ra_rce(), this.getIm_rb_rse(), this.getIm_rc_esr(), this.getIm_rd_a2_ricavi(), this.getIm_re_a2_entrate(), this.getIm_rf_a3_ricavi(),
                this.getIm_rg_a3_entrate(),this.getCd_centro_responsabilita_clgs(), this.getCd_linea_attivita_clgs(), this.getTi_appartenenza_clgs(), this.getTi_gestione_clgs(), this.getCd_elemento_voce_clgs(), this.getStato(),
                this.getCategoria_dettaglio(), getPrcImputazioneFin());
    }
}