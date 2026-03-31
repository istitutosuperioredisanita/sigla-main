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

import it.cnr.jada.bulk.OggettoBulk;

public class Numerazione_coge_coanKey extends OggettoBulk {
	// CD_CDS VARCHAR(30) NOT NULL (PK)
	private String cd_cds;

	// ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
	private Integer esercizio;

	// CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL (PK)
	private String cd_unita_organizzativa;

	// CD_TIPO_DOCUMENTO_AMM VARCHAR(10) NOT NULL (PK)
	private String ti_documento;

	public Numerazione_coge_coanKey() {
		super();
	}
	public Numerazione_coge_coanKey(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String ti_documento) {
		super();
		this.esercizio = esercizio;
		this.cd_cds = cd_cds;
		this.cd_unita_organizzativa = cd_unita_organizzativa;
		this.ti_documento = ti_documento;
	}

	public String getCd_cds() {
		return cd_cds;
	}

	public String getCd_unita_organizzativa() {
		return cd_unita_organizzativa;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public String getTi_documento() {
		return ti_documento;
	}

	public void setCd_cds(String cd_cds) {
		this.cd_cds = cd_cds;
	}

	public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
		this.cd_unita_organizzativa = cd_unita_organizzativa;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public void setTi_documento(String ti_documento) {
		this.ti_documento = ti_documento;
	}
}
