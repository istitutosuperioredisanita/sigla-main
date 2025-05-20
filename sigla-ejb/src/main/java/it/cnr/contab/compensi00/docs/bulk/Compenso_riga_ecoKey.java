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

package it.cnr.contab.compensi00.docs.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class Compenso_riga_ecoKey extends OggettoBulk implements KeyedPersistent {
    // CD_CDS VARCHAR(30) NOT NULL (PK)
    private String cd_cds;

    // CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL (PK)
    private String cd_unita_organizzativa;

    // ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
    private Integer esercizio;

    // PG_COMPENSO DECIMAL(10,0) NOT NULL (PK)
    private Long pg_compenso;

    // PROGRESSIVO_RIGA_ECO DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga_eco;

    public Compenso_riga_ecoKey() {
        super();
    }

    public Compenso_riga_ecoKey(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_compenso, Long progressivo_riga_eco) {
        this.cd_cds = cd_cds;
        this.cd_unita_organizzativa = cd_unita_organizzativa;
        this.esercizio = esercizio;
        this.pg_compenso = pg_compenso;
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compenso_riga_ecoKey)) return false;
        Compenso_riga_ecoKey k = (Compenso_riga_ecoKey) o;
        if (!compareKey(getCd_cds(), k.getCd_cds())) return false;
        if (!compareKey(getCd_unita_organizzativa(), k.getCd_unita_organizzativa())) return false;
        if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
        if (!compareKey(getPg_compenso(), k.getPg_compenso())) return false;
		return compareKey(getProgressivo_riga_eco(), k.getProgressivo_riga_eco());
	}

    /*
     * Getter dell'attributo cd_cds
     */
    public String getCd_cds() {
        return cd_cds;
    }

    /*
     * Setter dell'attributo cd_cds
     */
    public void setCd_cds(String cd_cds) {
        this.cd_cds = cd_cds;
    }

    /*
     * Getter dell'attributo cd_unita_organizzativa
     */
    public String getCd_unita_organizzativa() {
        return cd_unita_organizzativa;
    }

    /*
     * Setter dell'attributo cd_unita_organizzativa
     */
    public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
        this.cd_unita_organizzativa = cd_unita_organizzativa;
    }

    /*
     * Getter dell'attributo esercizio
     */
    public Integer getEsercizio() {
        return esercizio;
    }

    /*
     * Setter dell'attributo esercizio
     */
    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Long getPg_compenso() {
        return pg_compenso;
    }

    public void setPg_compenso(Long pg_compenso) {
        this.pg_compenso = pg_compenso;
    }

    public Long getProgressivo_riga_eco() {
        return progressivo_riga_eco;
    }

    public void setProgressivo_riga_eco(Long progressivo_riga_eco) {
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public int primaryKeyHashCode() {
        return
                calculateKeyHashCode(getCd_cds()) +
                        calculateKeyHashCode(getCd_unita_organizzativa()) +
                        calculateKeyHashCode(getEsercizio()) +
                        calculateKeyHashCode(getPg_compenso()) +
                        calculateKeyHashCode(getProgressivo_riga_eco());
    }
}
