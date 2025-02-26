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

package it.cnr.contab.anagraf00.core.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class V_terzo_persona_fisicaKey extends OggettoBulk implements KeyedPersistent {
	// CD_TERZO DECIMAL(8,0) NOT NULL (PK)
	private Integer cd_terzo;


public V_terzo_persona_fisicaKey() {
	super();
}
public V_terzo_persona_fisicaKey(Integer cd_terzo) {
	this.cd_terzo = cd_terzo;
}
/* 
 * Getter dell'attributo cd_terzo
 */
public Integer getCd_terzo() {
	return cd_terzo;
}
/* 
 * Setter dell'attributo cd_terzo
 */
public void setCd_terzo(Integer cd_terzo) {
	this.cd_terzo = cd_terzo;
}
}
