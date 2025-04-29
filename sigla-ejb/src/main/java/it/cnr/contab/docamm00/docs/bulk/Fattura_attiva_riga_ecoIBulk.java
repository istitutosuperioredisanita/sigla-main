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

/**
 * Insert the type's description here.
 * Creation date: (10/25/2001 11:52:17 AM)
 * @author: Roberto Peli
 */
public class Fattura_attiva_riga_ecoIBulk extends Fattura_attiva_riga_ecoBulk {

	private Fattura_attiva_rigaIBulk fattura_attiva_rigaIBulk;

	public Fattura_attiva_riga_ecoIBulk() {
		super();
	}

	public Fattura_attiva_riga_ecoIBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_attiva, Long progressivo_riga, Long progressivo_riga_eco) {
		super(cd_cds, cd_unita_organizzativa, esercizio, pg_fattura_attiva, progressivo_riga, progressivo_riga_eco);
	}

	public Fattura_attiva_rigaBulk getFattura_attiva_riga() {
		return getFattura_attiva_rigaI();
	}

	@Override
	public void setFattura_attiva_riga(Fattura_attiva_rigaBulk fattura_attiva_riga) {
		setFattura_attiva_rigaI((Fattura_attiva_rigaIBulk)fattura_attiva_riga);
	}

	public void setFattura_attiva_rigaI(Fattura_attiva_rigaIBulk newFattura_attiva_rigaIBulk) {
		fattura_attiva_rigaIBulk = newFattura_attiva_rigaIBulk;
	}

	public Fattura_attiva_rigaIBulk getFattura_attiva_rigaI() {
		return fattura_attiva_rigaIBulk;
	}
}
