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

package it.cnr.contab.progettiric00.tabrif.bulk;

import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.beans.*;
import it.cnr.jada.persistency.sql.*;
import jakarta.persistence.Transient;

import java.math.BigDecimal;

public class Tipo_progettoBulk extends Tipo_progettoBase {

	@Transient private BigDecimal importoFinanziato;
	@Transient private BigDecimal importoUtilizzato;

	public Tipo_progettoBulk() {
	super();
}
	public Tipo_progettoBulk(java.lang.String cd_tipo_progetto) {
	super(cd_tipo_progetto);
}

	public BigDecimal getImportoFinanziato() {
		return importoFinanziato;
	}

	public void setImportoFinanziato(BigDecimal importoFinanziato) {
		this.importoFinanziato = importoFinanziato;
	}

	public BigDecimal getImportoUtilizzato() {
		return importoUtilizzato;
	}

	public void setImportoUtilizzato(BigDecimal importoUtilizzato) {
		this.importoUtilizzato = importoUtilizzato;
	}
}
