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

import it.cnr.jada.persistency.Keyed;

public class Voce_analiticaBase extends Voce_analiticaKey implements Keyed {
	// DS_VOCE_ANA VARCHAR(100)
	private java.lang.String ds_voce_ana;

	// ESERCIZIO_VOCE_EP DECIMAL(4,0) NOT NULL
	private java.lang.Integer esercizio_voce_ep;

	// CD_VOCE_EP VARCHAR(45) NOT NULL
	private java.lang.String cd_voce_ep;

	// FL_DEFAULT CHAR(1)
	private java.lang.Boolean fl_default;

	public Voce_analiticaBase() {
		super();
	}

	public Voce_analiticaBase(java.lang.String cd_voce_ana, java.lang.Integer esercizio) {
		super(cd_voce_ana,esercizio);
	}

	public java.lang.String getDs_voce_ana() {
		return ds_voce_ana;
	}

	public void setDs_voce_ana(java.lang.String ds_voce_ana) {
		this.ds_voce_ana = ds_voce_ana;
	}

	public java.lang.Integer getEsercizio_voce_ep() {
		return esercizio_voce_ep;
	}

	public void setEsercizio_voce_ep(java.lang.Integer esercizio_voce_ep) {
		this.esercizio_voce_ep = esercizio_voce_ep;
	}

	public java.lang.String getCd_voce_ep() {
		return cd_voce_ep;
	}

	public void setCd_voce_ep(java.lang.String cd_voce_ep) {
		this.cd_voce_ep = cd_voce_ep;
	}

	public java.lang.Boolean getFl_default() {
		return fl_default;
	}

	public void setFl_default(java.lang.Boolean fl_default) {
		this.fl_default = fl_default;
	}
}
