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

package it.cnr.contab.docamm00.tabrif.bulk;

import it.cnr.jada.persistency.Keyed;

public class Categoria_gruppo_voce_epBase extends Categoria_gruppo_voce_epKey implements Keyed {
	private java.lang.String cd_categoria_gruppo;

	private java.lang.Integer esercizio;

	private java.lang.String sezione;

	private String cd_voce_ep;

	private String cd_voce_ep_contr;

	private String cd_voce_ep_plus;

	private String cd_voce_ep_minus;

	private Boolean flDefault;

	public Categoria_gruppo_voce_epBase() {
		super();
	}

	public Categoria_gruppo_voce_epBase(String rowid) {
		super(rowid);
	}

	public String getCd_categoria_gruppo() {
		return cd_categoria_gruppo;
	}

	public void setCd_categoria_gruppo(String cd_categoria_gruppo) {
		this.cd_categoria_gruppo = cd_categoria_gruppo;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getSezione() {
		return sezione;
	}

	public void setSezione(String sezione) {
		this.sezione = sezione;
	}

	public String getCd_voce_ep() {
		return cd_voce_ep;
	}

	public void setCd_voce_ep(String cd_voce_ep) {
		this.cd_voce_ep = cd_voce_ep;
	}

	public String getCd_voce_ep_contr() {
		return cd_voce_ep_contr;
	}

	public void setCd_voce_ep_contr(String cd_voce_ep_contr) {
		this.cd_voce_ep_contr = cd_voce_ep_contr;
	}

	public String getCd_voce_ep_plus() {
		return cd_voce_ep_plus;
	}

	public void setCd_voce_ep_plus(String cd_voce_ep_plus) {
		this.cd_voce_ep_plus = cd_voce_ep_plus;
	}

	public String getCd_voce_ep_minus() {
		return cd_voce_ep_minus;
	}

	public void setCd_voce_ep_minus(String cd_voce_ep_minus) {
		this.cd_voce_ep_minus = cd_voce_ep_minus;
	}

	public Boolean getFlDefault() {
		return flDefault;
	}

	public void setFlDefault(Boolean flDefault) {
		this.flDefault = flDefault;
	}
}
