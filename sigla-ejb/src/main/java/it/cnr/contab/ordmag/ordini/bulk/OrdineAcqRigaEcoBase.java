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

package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.jada.persistency.Keyed;

import java.math.BigDecimal;

public class OrdineAcqRigaEcoBase extends OrdineAcqRigaEcoKey implements Keyed {
    // ESERCIZIO_VOCE_ANA DECIMAL(4,0) NOT NULL
    private Integer esercizio_voce_ana;

    // CD_VOCE_ANA VARCHAR(45) NOT NULL
    private String cd_voce_ana;

    // CD_CENTRO_RESPONSABILITA VARCHAR(30) NOT NULL
    private String cd_centro_responsabilita;

    // CD_LINEA_ATTIVITA VARCHAR(10) NOT NULL
    private String cd_linea_attivita;

    private BigDecimal importo;

    public OrdineAcqRigaEcoBase() {
        super();
    }

    public OrdineAcqRigaEcoBase(java.lang.String cdCds, java.lang.String cdUnitaOperativa, java.lang.Integer esercizio, java.lang.String cdNumeratore, java.lang.Integer numero, java.lang.Integer riga, java.lang.Long progressivo_riga_eco) {
        super(cdCds, cdUnitaOperativa, esercizio, cdNumeratore, numero, riga, progressivo_riga_eco);
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
