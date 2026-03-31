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

package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.contab.anagraf00.tabrif.bulk.Tipo_rapportoBulk;
import it.cnr.contab.util00.bulk.MeseBulk;

import java.util.Map;

public class Stipendi_cofiBulk extends Stipendi_cofiBase {
	private static final java.util.Dictionary statoKeys = new it.cnr.jada.util.OrderedHashtable();
	public static final String STATO_LIQUIDATO = "P";
	public static final String STATO_NON_LIQUIDATO = "I";

	static {
		statoKeys.put(STATO_LIQUIDATO,"Liquidato");
		statoKeys.put(STATO_NON_LIQUIDATO,"Non liquidato");
	}

	public static final Map meseKeys = MeseBulk.meseKeys;
	public static final Map tipoFlussoKeys = MeseTipoFlussoBulk.tipoFlussoKeys;

	public Stipendi_cofiBulk() {
		super();
	}
	public Stipendi_cofiBulk(java.lang.Integer esercizio,java.lang.Integer mese) {
		super(esercizio,mese);
	}
	public final java.util.Dictionary getStatoKeys() {
		return statoKeys;
	}
	/**
	 * @return
	 */
	public static java.util.Map getMeseKeys() {
		return meseKeys;
	}

	public String getMeseRealeText() {
		return  (String)getMeseKeys().get(this.getMese_reale());
	}

	public boolean isLiquidato() {
		return Stipendi_cofiBulk.STATO_LIQUIDATO.equals(this.getStato());
	}

	public boolean isNonLiquidato() {
		return Stipendi_cofiBulk.STATO_NON_LIQUIDATO.equals(this.getStato());
	}

	public String getRecordKey() {
		return (String)getMeseKeys().get(this.getMese_reale())+" - Flusso: "+this.getProg_flusso();
	}

	public boolean isFlussoDipendenti() {
		return Tipo_rapportoBulk.DIPENDENTE.equals(this.getTipo_flusso());
	}

	public boolean isFlussoCollaboratori() {
		return Tipo_rapportoBulk.COLLABORATORE_COORD_E_CONT.equals(this.getTipo_flusso());
	}
}
