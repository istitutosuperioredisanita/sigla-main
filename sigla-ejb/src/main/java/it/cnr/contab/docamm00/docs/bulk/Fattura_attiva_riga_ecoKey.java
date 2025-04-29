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

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class Fattura_attiva_riga_ecoKey extends OggettoBulk implements KeyedPersistent {
    // CD_CDS VARCHAR(30) NOT NULL (PK)
    private String cd_cds;

    // CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL (PK)
    private String cd_unita_organizzativa;

    // ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
    private Integer esercizio;

    // PG_FATTURA_ATTIVA DECIMAL(10,0) NOT NULL (PK)
    private Long pg_fattura_attiva;

    // PROGRESSIVO_RIGA DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga;

    // PROGRESSIVO_RIGA_ECO DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga_eco;

    public Fattura_attiva_riga_ecoKey() {
        super();
    }

    public Fattura_attiva_riga_ecoKey(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_attiva, Long progressivo_riga, Long progressivo_riga_eco) {
        this.cd_cds = cd_cds;
        this.cd_unita_organizzativa = cd_unita_organizzativa;
        this.esercizio = esercizio;
        this.pg_fattura_attiva = pg_fattura_attiva;
        this.progressivo_riga = progressivo_riga;
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fattura_attiva_riga_ecoKey)) return false;
        Fattura_attiva_riga_ecoKey k = (Fattura_attiva_riga_ecoKey) o;
        if (!compareKey(getCd_cds(), k.getCd_cds())) return false;
        if (!compareKey(getCd_unita_organizzativa(), k.getCd_unita_organizzativa())) return false;
        if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
        if (!compareKey(getPg_fattura_attiva(), k.getPg_fattura_attiva())) return false;
        if (!compareKey(getProgressivo_riga(), k.getProgressivo_riga())) return false;
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

    /*
     * Getter dell'attributo pg_fattura_attiva
     */
    public Long getPg_fattura_attiva() {
        return pg_fattura_attiva;
    }

    /*
     * Setter dell'attributo pg_fattura_attiva
     */
    public void setPg_fattura_attiva(Long pg_fattura_attiva) {
        this.pg_fattura_attiva = pg_fattura_attiva;
    }

    /*
     * Getter dell'attributo progressivo_riga
     */
    public Long getProgressivo_riga() {
        return progressivo_riga;
    }

    /*
     * Setter dell'attributo progressivo_riga
     */
    public void setProgressivo_riga(Long progressivo_riga) {
        this.progressivo_riga = progressivo_riga;
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
                        calculateKeyHashCode(getPg_fattura_attiva()) +
                        calculateKeyHashCode(getProgressivo_riga()) +
                        calculateKeyHashCode(getProgressivo_riga_eco());
    }
}
