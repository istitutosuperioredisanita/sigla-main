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
public class Fattura_passiva_riga_ecoIBulk extends Fattura_passiva_riga_ecoBulk {

	private Fattura_passiva_rigaIBulk fattura_passiva_rigaIBulk;

	public Fattura_passiva_riga_ecoIBulk() {
		super();
	}

	public Fattura_passiva_riga_ecoIBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_passiva, Long progressivo_riga, Long progressivo_riga_eco) {
		super(cd_cds, cd_unita_organizzativa, esercizio, pg_fattura_passiva, progressivo_riga, progressivo_riga_eco);
	}

	public Fattura_passiva_rigaBulk getFattura_passiva_riga() {
		return getFattura_passiva_rigaI();
	}

	@Override
	public void setFattura_passiva_riga(Fattura_passiva_rigaBulk fattura_passiva_riga) {
		setFattura_passiva_rigaI((Fattura_passiva_rigaIBulk)fattura_passiva_riga);
	}

	public void setFattura_passiva_rigaI(Fattura_passiva_rigaIBulk newFattura_passiva_rigaIBulk) {
		fattura_passiva_rigaIBulk = newFattura_passiva_rigaIBulk;
	}

	public Fattura_passiva_rigaIBulk getFattura_passiva_rigaI() {
		return fattura_passiva_rigaIBulk;
	}
}
