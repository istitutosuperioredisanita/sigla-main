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

public enum OrigineScritturaEnum {
	CAUSALE("Causale"),
	PRIMA_NOTA_MANUALE("Prima Nota Manuale"),
	DOCAMM( "Documento Amministrativo"),
	DOCCONT("Documento Contabile"),
    RISCONTRO_A_VALORE("Riscontro a Valore"),
	STIPENDI("Stipendi"),
	LIQUID_IVA("Liquidazione IVA"),
	PRECHIUSURA("Prechiusura Bilancio"),
	CHIUSURA("Chiusura Bilancio"),
	APERTURA("Apertura Bilancio");

	private final String label;

	OrigineScritturaEnum(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
