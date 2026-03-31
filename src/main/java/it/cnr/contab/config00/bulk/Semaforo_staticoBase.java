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

import it.cnr.jada.persistency.Keyed;

public class Semaforo_staticoBase extends Semaforo_staticoKey implements Keyed {

    private String stato;

    public Semaforo_staticoBase() {
    }

    public Semaforo_staticoBase(Integer esercizio, String cd_tipo_semaforo, String cd_cds, String cd_unita_organizzativa, String cd_centro_responsabilita) {
        super(esercizio, cd_tipo_semaforo, cd_cds, cd_unita_organizzativa, cd_centro_responsabilita);
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}