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
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/02/2013
 */
package it.cnr.contab.config00.pdcep.consultazioni.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

public class VConsPianoContiEconomicoBulk extends OggettoBulk implements Persistent  {
	public VConsPianoContiEconomicoBulk() {
		super();
	}

	private Integer esercizio;

	private String tipo;

    private String tiporec;

    private Integer nr_livello;

	private String cd_classificazione;

	private String ds_classificazione;

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTiporec() {
        return tiporec;
    }

    public void setTiporec(String tiporec) {
        this.tiporec = tiporec;
    }

    public Integer getNr_livello() {
        return nr_livello;
    }

    public void setNr_livello(Integer nr_livello) {
        this.nr_livello = nr_livello;
    }

    public String getCd_classificazione() {
        return cd_classificazione;
    }

    public void setCd_classificazione(String cd_classificazione) {
        this.cd_classificazione = cd_classificazione;
    }

    public String getDs_classificazione() {
        return ds_classificazione;
    }

    public void setDs_classificazione(String ds_classificazione) {
        this.ds_classificazione = ds_classificazione;
    }
}