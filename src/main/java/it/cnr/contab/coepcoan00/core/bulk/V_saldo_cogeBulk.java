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
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;

public class V_saldo_cogeBulk extends OggettoBulk implements Persistent {
	private static final long serialVersionUID = 1L;

	private String cdCds;

	private Integer esercizio;

	private String cdUnitaOrganizzativa;

	private Integer cdTerzo;

	private String tiIstituzCommerc;

	private String cdVoceEp;

	private String naturaVoce;

	private String riepilogaA;

	private BigDecimal totDare;

	private BigDecimal totAvere;

	public V_saldo_cogeBulk() {
		super();
	}

	public String getCdCds() {
		return cdCds;
	}

	public void setCdCds(String cdCds) {
		this.cdCds = cdCds;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}

	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa) {
		this.cdUnitaOrganizzativa = cdUnitaOrganizzativa;
	}

	public Integer getCdTerzo() {
		return cdTerzo;
	}

	public void setCdTerzo(Integer cdTerzo) {
		this.cdTerzo = cdTerzo;
	}

	public String getTiIstituzCommerc() {
		return tiIstituzCommerc;
	}

	public void setTiIstituzCommerc(String tiIstituzCommerc) {
		this.tiIstituzCommerc = tiIstituzCommerc;
	}

	public String getCdVoceEp() {
		return cdVoceEp;
	}

	public void setCdVoceEp(String cdVoceEp) {
		this.cdVoceEp = cdVoceEp;
	}

	public String getNaturaVoce() {
		return naturaVoce;
	}

	public void setNaturaVoce(String naturaVoce) {
		this.naturaVoce = naturaVoce;
	}

	public String getRiepilogaA() {
		return riepilogaA;
	}

	public void setRiepilogaA(String riepilogaA) {
		this.riepilogaA = riepilogaA;
	}

	public BigDecimal getTotDare() {
		return totDare;
	}

	public void setTotDare(BigDecimal totDare) {
		this.totDare = totDare;
	}

	public BigDecimal getTotAvere() {
		return totAvere;
	}

	public void setTotAvere(BigDecimal totAvere) {
		this.totAvere = totAvere;
	}
}