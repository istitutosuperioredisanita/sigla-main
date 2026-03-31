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
* Date 27/10/2005
*/
package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.docamm00.docs.bulk.TipoDocumentoEnum;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.util.OrderedHashtable;

import java.util.Date;
import java.util.Dictionary;

public class V_documenti_da_contabilizzareBulk extends OggettoBulk implements Persistent {
	private String tipodoc;

	private Integer esercizio_cont;

	private Integer esercizio;

	private String cd_cds;

	private String cd_unita_organizzativa;

	private Integer pg_doc;

	private String cd_unita_operativa;

	private String cd_numeratore;

	private Integer riga;

	private Integer consegna;

	private Date dt_registrazione;

	public V_documenti_da_contabilizzareBulk() {
		super();
	}

	public String getTipodoc() {
		return tipodoc;
	}

	public void setTipodoc(String tipodoc) {
		this.tipodoc = tipodoc;
	}

	public Integer getEsercizio_cont() {
		return esercizio_cont;
	}

	public void setEsercizio_cont(Integer esercizio_cont) {
		this.esercizio_cont = esercizio_cont;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getCd_cds() {
		return cd_cds;
	}

	public void setCd_cds(String cd_cds) {
		this.cd_cds = cd_cds;
	}

	public String getCd_unita_organizzativa() {
		return cd_unita_organizzativa;
	}

	public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
		this.cd_unita_organizzativa = cd_unita_organizzativa;
	}

	public Integer getPg_doc() {
		return pg_doc;
	}

	public void setPg_doc(Integer pg_doc) {
		this.pg_doc = pg_doc;
	}

	public String getCd_unita_operativa() {
		return cd_unita_operativa;
	}

	public void setCd_unita_operativa(String cd_unita_operativa) {
		this.cd_unita_operativa = cd_unita_operativa;
	}

	public String getCd_numeratore() {
		return cd_numeratore;
	}

	public void setCd_numeratore(String cd_numeratore) {
		this.cd_numeratore = cd_numeratore;
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

	public Date getDt_registrazione() {
		return dt_registrazione;
	}

	public void setDt_registrazione(Date dt_registrazione) {
		this.dt_registrazione = dt_registrazione;
	}

	public TipoDocumentoEnum getTipoDocumentoEnum() {
		return TipoDocumentoEnum.fromValue(this.getTipodoc());
	}
}