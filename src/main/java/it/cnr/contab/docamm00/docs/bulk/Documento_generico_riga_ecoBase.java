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

import it.cnr.jada.persistency.Keyed;

import java.math.BigDecimal;

public class Documento_generico_riga_ecoBase extends Documento_generico_riga_ecoKey implements Keyed {
    // ESERCIZIO_VOCE_ANA DECIMAL(4,0) NOT NULL
    private Integer esercizio_voce_ana;

    // CD_VOCE_ANA VARCHAR(45) NOT NULL
    private String cd_voce_ana;

    // CD_CENTRO_RESPONSABILITA VARCHAR(30) NOT NULL
    private String cd_centro_responsabilita;

    // CD_LINEA_ATTIVITA VARCHAR(10) NOT NULL
    private String cd_linea_attivita;

    private BigDecimal importo;

    public Documento_generico_riga_ecoBase() {
        super();
    }

    public Documento_generico_riga_ecoBase(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico, Long progressivo_riga, Long progressivo_riga_eco) {
        super(cd_cds, cd_unita_organizzativa, esercizio, cd_tipo_documento_amm, pg_documento_generico, progressivo_riga, progressivo_riga_eco);
    }

    public Integer getEsercizio_voce_ana() {
        return esercizio_voce_ana;
    }

    public void setEsercizio_voce_ana(Integer esercizio_voce_ana) {
        this.esercizio_voce_ana = esercizio_voce_ana;
    }

    public String getCd_voce_ana() {
        return cd_voce_ana;
    }

    public void setCd_voce_ana(String cd_voce_ana) {
        this.cd_voce_ana = cd_voce_ana;
    }

    public String getCd_centro_responsabilita() {
        return cd_centro_responsabilita;
    }

    public void setCd_centro_responsabilita(String cd_centro_responsabilita) {
        this.cd_centro_responsabilita = cd_centro_responsabilita;
    }

    public String getCd_linea_attivita() {
        return cd_linea_attivita;
    }

    public void setCd_linea_attivita(String cd_linea_attivita) {
        this.cd_linea_attivita = cd_linea_attivita;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }
}
