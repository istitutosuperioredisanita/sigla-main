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

package it.cnr.contab.config00.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class Semaforo_baseKey extends OggettoBulk implements KeyedPersistent {
	// ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
	private Integer esercizio;

    private String cd_tipo_semaforo;

	private String cd_cds;

    private String cd_unita_organizzativa;

    private String cd_centro_responsabilita;

	public Semaforo_baseKey() {
		super();
	}

    public Semaforo_baseKey(Integer esercizio, String cd_tipo_semaforo, String cd_cds, String cd_unita_organizzativa, String cd_centro_responsabilita) {
        this.esercizio = esercizio;
        this.cd_tipo_semaforo = cd_tipo_semaforo;
        this.cd_cds = cd_cds;
        this.cd_unita_organizzativa = cd_unita_organizzativa;
        this.cd_centro_responsabilita = cd_centro_responsabilita;
    }

    public boolean equalsByPrimaryKey(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Semaforo_baseKey))
			return false;
		Semaforo_baseKey k = (Semaforo_baseKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio()))
			return false;
        if (!compareKey(getCd_tipo_semaforo(), k.getCd_tipo_semaforo()))
            return false;
        if (!compareKey(getCd_cds(), k.getCd_cds()))
            return false;
        if (!compareKey(getCd_unita_organizzativa(), k.getCd_unita_organizzativa()))
            return false;
        if (!compareKey(getCd_centro_responsabilita(), k.getCd_centro_responsabilita()))
            return false;
		return true;
	}

	public int primaryKeyHashCode() {
		return calculateKeyHashCode(getEsercizio()) +
                calculateKeyHashCode(getCd_tipo_semaforo())+
                calculateKeyHashCode(getCd_cds())+
                calculateKeyHashCode(getCd_unita_organizzativa())+
                calculateKeyHashCode(getCd_centro_responsabilita());
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

    public String getCd_tipo_semaforo() {
        return cd_tipo_semaforo;
    }

    public void setCd_tipo_semaforo(String cd_tipo_semaforo) {
        this.cd_tipo_semaforo = cd_tipo_semaforo;
    }

    public String getCd_cds() {
		return cd_cds;
	}

	public void setCd_cds(String string) {
		cd_cds = string;
	}

    public String getCd_unita_organizzativa() {
        return cd_unita_organizzativa;
    }

    public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
        this.cd_unita_organizzativa = cd_unita_organizzativa;
    }

    public String getCd_centro_responsabilita() {
        return cd_centro_responsabilita;
    }

    public void setCd_centro_responsabilita(String cd_centro_responsabilita) {
        this.cd_centro_responsabilita = cd_centro_responsabilita;
    }
}
