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

/*
 * Created on Mar 17, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.progettiric00.core.bulk;

import it.cnr.contab.progettiric00.tabrif.bulk.Voce_piano_economico_prgBulk;

/**
 * @author mspasiano
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Saldi_piano_econom_progettoBulk extends V_saldi_piano_econom_progettoBulk {

	public Saldi_piano_econom_progettoBulk() {
		super();
	}
	private ProgettoBulk progettoBulk;
	private Voce_piano_economico_prgBulk voce_piano_economico;

	public ProgettoBulk getProgettoBulk() {
		return progettoBulk;
	}

	public void setProgettoBulk(ProgettoBulk progettoBulk) {
		this.progettoBulk = progettoBulk;
	}

	public Voce_piano_economico_prgBulk getVoce_piano_economico() {
		return voce_piano_economico;
	}

	public void setVoce_piano_economico(Voce_piano_economico_prgBulk voce_piano_economico) {
		this.voce_piano_economico = voce_piano_economico;
	}

	private String ds_voce_piano;

	public String getDs_voce_piano() {
		Voce_piano_economico_prgBulk vocePianoEconomico = this.getVoce_piano_economico();
		if (vocePianoEconomico == null)
			return null;
		return getVoce_piano_economico().getDs_voce_piano();
	}

	public void setDs_voce_piano(String ds_voce_piano) {
		this.getVoce_piano_economico().setDs_voce_piano(ds_voce_piano);
	}

	private String ds_progetto;

	public String getDs_progetto() {

		ProgettoBulk progettoBulk1= this.getProgettoBulk();
		if ( progettoBulk1==null)
			return null;
		return getProgettoBulk().getDs_progetto();
	}

	public void setDs_progetto(String ds_progetto) {
		this.getProgettoBulk().setDs_progetto(ds_progetto);
	}
}