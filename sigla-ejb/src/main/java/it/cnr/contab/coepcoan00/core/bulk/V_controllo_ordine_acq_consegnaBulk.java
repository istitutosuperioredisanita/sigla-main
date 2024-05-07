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

public class V_controllo_ordine_acq_consegnaBulk extends OggettoBulk implements Persistent {
	private static final long serialVersionUID = 1L;

	private java.lang.String cdCds;

	private java.lang.String cdUnitaOperativa;

	private java.lang.Integer esercizio;

	private java.lang.String cdNumeratore;

	private java.lang.Integer numero;

	private java.lang.Integer riga;

	private java.lang.Integer consegna;

	private BigDecimal prezzoUnitarioCaricoMagazzino;

	private java.math.BigDecimal quantitaCaricoMagazzino;

	private java.lang.Integer esercizioEvasione;

	private java.lang.String statoCaricoMagazzino;

	private java.lang.Integer esercizioFattura;

	private java.lang.String cdVoceEp;

	private java.lang.String tiAttivita;

	public V_controllo_ordine_acq_consegnaBulk() {
		super();
	}

	public String getCdCds() {
		return cdCds;
	}

	public void setCdCds(String cdCds) {
		this.cdCds = cdCds;
	}

	public String getCdUnitaOperativa() {
		return cdUnitaOperativa;
	}

	public void setCdUnitaOperativa(String cdUnitaOperativa) {
		this.cdUnitaOperativa = cdUnitaOperativa;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getCdNumeratore() {
		return cdNumeratore;
	}

	public void setCdNumeratore(String cdNumeratore) {
		this.cdNumeratore = cdNumeratore;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Integer getRiga() {
		return riga;
	}

	public void setRiga(Integer riga) {
		this.riga = riga;
	}

	public Integer getConsegna() {
		return consegna;
	}

	public void setConsegna(Integer consegna) {
		this.consegna = consegna;
	}

	public BigDecimal getPrezzoUnitarioCaricoMagazzino() {
		return prezzoUnitarioCaricoMagazzino;
	}

	public void setPrezzoUnitarioCaricoMagazzino(BigDecimal prezzoUnitarioCaricoMagazzino) {
		this.prezzoUnitarioCaricoMagazzino = prezzoUnitarioCaricoMagazzino;
	}

	public BigDecimal getQuantitaCaricoMagazzino() {
		return quantitaCaricoMagazzino;
	}

	public void setQuantitaCaricoMagazzino(BigDecimal quantitaCaricoMagazzino) {
		this.quantitaCaricoMagazzino = quantitaCaricoMagazzino;
	}

	public Integer getEsercizioEvasione() {
		return esercizioEvasione;
	}

	public void setEsercizioEvasione(Integer esercizioEvasione) {
		this.esercizioEvasione = esercizioEvasione;
	}

	public String getStatoCaricoMagazzino() {
		return statoCaricoMagazzino;
	}

	public void setStatoCaricoMagazzino(String statoCaricoMagazzino) {
		this.statoCaricoMagazzino = statoCaricoMagazzino;
	}

	public Integer getEsercizioFattura() {
		return esercizioFattura;
	}

	public void setEsercizioFattura(Integer esercizioFattura) {
		this.esercizioFattura = esercizioFattura;
	}

	public String getCdVoceEp() {
		return cdVoceEp;
	}

	public void setCdVoceEp(String cdVoceEp) {
		this.cdVoceEp = cdVoceEp;
	}

	public String getTiAttivita() {
		return tiAttivita;
	}

	public void setTiAttivita(String tiAttivita) {
		this.tiAttivita = tiAttivita;
	}

	public BigDecimal getImportoFattureDaRicevere() {
		return this.getPrezzoUnitarioCaricoMagazzino().multiply(this.getQuantitaCaricoMagazzino()).setScale(2,
				BigDecimal.ROUND_HALF_UP);
	}
}