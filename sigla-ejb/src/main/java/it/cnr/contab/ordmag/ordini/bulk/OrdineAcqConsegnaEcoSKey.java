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

public class OrdineAcqConsegnaEcoSKey extends OggettoBulk implements KeyedPersistent {
    private Long id;
    public OrdineAcqConsegnaEcoSKey() {
        super();
    }

    public OrdineAcqConsegnaEcoSKey(Long id) {
        super();
        this.id=id;
    }

    public boolean equalsByPrimaryKey(Object o) {
        if (this== o) return true;
        if (!(o instanceof OrdineAcqConsegnaEcoSKey)) return false;
        OrdineAcqConsegnaEcoSKey k = (OrdineAcqConsegnaEcoSKey) o;
        if (!compareKey(getId(), k.getId())) return false;
        return true;
    }

    public int primaryKeyHashCode() {
        int i = 0;
        i = i + calculateKeyHashCode(getId());
        return i;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
