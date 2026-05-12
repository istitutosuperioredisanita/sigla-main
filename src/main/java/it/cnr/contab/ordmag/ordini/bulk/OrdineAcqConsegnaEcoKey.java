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

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class OrdineAcqConsegnaEcoKey extends OggettoBulk implements KeyedPersistent {
    private String cdCds;
    private String cdUnitaOperativa;
    private Integer esercizio;
    private String cdNumeratore;
    private Integer numero;
    private Integer riga;
    private java.lang.Integer consegna;

    // PROGRESSIVO_RIGA_ECO DECIMAL(10,0) NOT NULL (PK)
    private Long progressivo_riga_eco;

    public OrdineAcqConsegnaEcoKey() {
        super();
    }

    public OrdineAcqConsegnaEcoKey(String cdCds, String cdUnitaOperativa, Integer esercizio, String cdNumeratore, Integer numero, Integer riga, java.lang.Integer consegna, Long progressivo_riga_eco) {
        super();
        this.cdCds=cdCds;
        this.cdUnitaOperativa=cdUnitaOperativa;
        this.esercizio=esercizio;
        this.cdNumeratore=cdNumeratore;
        this.numero=numero;
        this.riga=riga;
        this.consegna=consegna;
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this== o) return true;
        if (!(o instanceof OrdineAcqConsegnaEcoKey)) return false;
        OrdineAcqConsegnaEcoKey k = (OrdineAcqConsegnaEcoKey) o;
        if (!compareKey(getCdCds(), k.getCdCds())) return false;
        if (!compareKey(getCdUnitaOperativa(), k.getCdUnitaOperativa())) return false;
        if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
        if (!compareKey(getCdNumeratore(), k.getCdNumeratore())) return false;
        if (!compareKey(getNumero(), k.getNumero())) return false;
        if (!compareKey(getRiga(), k.getRiga())) return false;
        if (!compareKey(getConsegna(), k.getConsegna())) return false;
        if (!compareKey(getProgressivo_riga_eco(), k.getProgressivo_riga_eco())) return false;
        return true;
    }

    public int primaryKeyHashCode() {
        int i = 0;
        i = i + calculateKeyHashCode(getCdCds());
        i = i + calculateKeyHashCode(getCdUnitaOperativa());
        i = i + calculateKeyHashCode(getEsercizio());
        i = i + calculateKeyHashCode(getCdNumeratore());
        i = i + calculateKeyHashCode(getNumero());
        i = i + calculateKeyHashCode(getRiga());
        i = i + calculateKeyHashCode(getConsegna());
        i = i + calculateKeyHashCode(getProgressivo_riga_eco());
        return i;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [cdCds]
     **/
    public void setCdCds(String cdCds)  {
        this.cdCds=cdCds;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [cdCds]
     **/
    public String getCdCds() {
        return cdCds;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [cdUnitaOperativa]
     **/
    public void setCdUnitaOperativa(String cdUnitaOperativa)  {
        this.cdUnitaOperativa=cdUnitaOperativa;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [cdUnitaOperativa]
     **/
    public String getCdUnitaOperativa() {
        return cdUnitaOperativa;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [esercizio]
     **/
    public void setEsercizio(Integer esercizio)  {
        this.esercizio=esercizio;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [esercizio]
     **/
    public Integer getEsercizio() {
        return esercizio;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [cdNumeratore]
     **/
    public void setCdNumeratore(String cdNumeratore)  {
        this.cdNumeratore=cdNumeratore;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [cdNumeratore]
     **/
    public String getCdNumeratore() {
        return cdNumeratore;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [numero]
     **/
    public void setNumero(Integer numero)  {
        this.numero=numero;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [numero]
     **/
    public Integer getNumero() {
        return numero;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Restituisce il valore di: [riga]
     **/
    public void setRiga(Integer riga)  {
        this.riga=riga;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [riga]
     **/
    public Integer getRiga() {
        return riga;
    }

    public void setConsegna(java.lang.Integer consegna)  {
        this.consegna=consegna;
    }
    /**
     * Created by BulkGenerator 2.0 [07/12/2009]
     * Setta il valore di: [consegna]
     **/
    public java.lang.Integer getConsegna() {
        return consegna;
    }

    public Long getProgressivo_riga_eco() {
        return progressivo_riga_eco;
    }

    public void setProgressivo_riga_eco(Long progressivo_riga_eco) {
        this.progressivo_riga_eco = progressivo_riga_eco;
    }
}
