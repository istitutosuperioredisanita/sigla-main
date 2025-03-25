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

package it.cnr.contab.config00.pdcep.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class Voce_analiticaKey extends OggettoBulk implements KeyedPersistent {
	// ESERCIZIO DECIMAL(4,0) NOT NULL (PK)
	private java.lang.Integer esercizio;

	// CD_VOCE_ANA VARCHAR(45) NOT NULL (PK)
	private java.lang.String cd_voce_ana;

	public Voce_analiticaKey() {
	super();
}

	public Voce_analiticaKey(java.lang.String cd_voce_ana, java.lang.Integer esercizio) {
		super();
		this.cd_voce_ana = cd_voce_ana;
		this.esercizio = esercizio;
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this == o) return true;
		if (!(o instanceof Voce_analiticaKey)) return false;
		Voce_analiticaKey k = (Voce_analiticaKey)o;
		if(!compareKey(getCd_voce_ana(),k.getCd_voce_ana())) return false;
		if(!compareKey(getEsercizio(),k.getEsercizio())) return false;
		return true;
	}

	public java.lang.String getCd_voce_ana() {
		return cd_voce_ana;
	}

	public java.lang.Integer getEsercizio() {
	return esercizio;
}

	public int primaryKeyHashCode() {
		return
			calculateKeyHashCode(getCd_voce_ana())+
			calculateKeyHashCode(getEsercizio());
	}

	public void setCd_voce_ana(java.lang.String cd_voce_ana) {
	this.cd_voce_ana = cd_voce_ana;
}

	public void setEsercizio(java.lang.Integer esercizio) {
	this.esercizio = esercizio;
}
}
