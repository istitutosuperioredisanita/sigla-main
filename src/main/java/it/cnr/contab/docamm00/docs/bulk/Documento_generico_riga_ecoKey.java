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

public class Documento_generico_riga_ecoKey extends OggettoBulk implements KeyedPersistent {
    // CD_CDS VARCHAR(30) NOT NULL (PK)
    private String cd_cds;

    // CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL (PK)
    private String cd_unita_organizzativa;

    // ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
    private Integer esercizio;

    // CD_TIPO_DOCUMENTO_AMM VARCHAR(10) NOT NULL (PK)
    private String cd_tipo_documento_amm;

    // PG_DOCUMENTO_GENERICO DECIMAL(10,0) NOT NULL (PK)
    private Long pg_documento_generico;

    // PROGRESSIVO_RIGA DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga;

    // PROGRESSIVO_RIGA_ECO DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga_eco;

    public Documento_generico_riga_ecoKey() {
        super();
    }

    public Documento_generico_riga_ecoKey(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico, Long progressivo_riga, Long progressivo_riga_eco) {
        this.cd_cds = cd_cds;
        this.cd_unita_organizzativa = cd_unita_organizzativa;
        this.esercizio = esercizio;
        this.cd_tipo_documento_amm = cd_tipo_documento_amm;
        this.pg_documento_generico = pg_documento_generico;
        this.progressivo_riga = progressivo_riga;
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this == o) return true;
        if (!(o instanceof Documento_generico_riga_ecoKey)) return false;
        Documento_generico_riga_ecoKey k = (Documento_generico_riga_ecoKey) o;
        if (!compareKey(getCd_cds(), k.getCd_cds())) return false;
        if (!compareKey(getCd_unita_organizzativa(), k.getCd_unita_organizzativa())) return false;
        if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
        if (!compareKey(getCd_tipo_documento_amm(), k.getCd_tipo_documento_amm())) return false;
        if (!compareKey(getPg_documento_generico(), k.getPg_documento_generico())) return false;
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

    public String getCd_tipo_documento_amm() {
        return cd_tipo_documento_amm;
    }

    public void setCd_tipo_documento_amm(String cd_tipo_documento_amm) {
        this.cd_tipo_documento_amm = cd_tipo_documento_amm;
    }

    public Long getPg_documento_generico() {
        return pg_documento_generico;
    }

    public void setPg_documento_generico(Long pg_documento_generico) {
        this.pg_documento_generico = pg_documento_generico;
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
                        calculateKeyHashCode(getCd_tipo_documento_amm()) +
                        calculateKeyHashCode(getPg_documento_generico()) +
                        calculateKeyHashCode(getProgressivo_riga()) +
                        calculateKeyHashCode(getProgressivo_riga_eco());
    }
}
