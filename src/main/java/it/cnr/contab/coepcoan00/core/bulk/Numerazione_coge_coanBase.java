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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.jada.persistency.KeyedPersistent;

public class Numerazione_coge_coanBase extends Numerazione_coge_coanKey implements KeyedPersistent {
	private Long primo;

	private Long corrente;

	private Long ultimo;

	public Numerazione_coge_coanBase() {
	super();
}

	public Numerazione_coge_coanBase(String cd_cds, String cd_unita_organizzativa, Integer esercizio,String ti_documento) {
		super(cd_cds,cd_unita_organizzativa,esercizio,ti_documento);
	}

	public Long getPrimo() {
		return primo;
	}

	public void setPrimo(Long primo) {
		this.primo = primo;
	}

	public Long getCorrente() {
		return corrente;
	}

	public void setCorrente(Long corrente) {
		this.corrente = corrente;
	}

	public Long getUltimo() {
		return ultimo;
	}

	public void setUltimo(Long ultimo) {
		this.ultimo = ultimo;
	}
}
