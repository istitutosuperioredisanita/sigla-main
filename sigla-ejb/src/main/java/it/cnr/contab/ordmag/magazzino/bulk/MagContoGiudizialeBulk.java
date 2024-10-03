/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
 * Date 21/09/2017
 */
package it.cnr.contab.ordmag.magazzino.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.util.Optional;

public class MagContoGiudizialeBulk extends OggettoBulk implements Persistent {
	private static final long serialVersionUID = 1L;

	private String cd_magazzino;
	private String dsMagazzino;
	private String cd_bene_servizio;
	private String ds_bene_servizio;
	private String cd_categoria_padre;
	private String cd_proprio;
	private String unita_misura;
	private Integer giacenzaAttuale;
	private Integer qtaInizioAnno;
	private Integer qtaCaricoAnno;
	private Integer qtaScaricoAnno;
	private Integer qtaGiacenzaInizioAnno;


	public MagContoGiudizialeBulk() {
		super();
	}

	public String getCd_magazzino() {
		return cd_magazzino;
	}

	public void setCd_magazzino(String cd_magazzino) {
		this.cd_magazzino = cd_magazzino;
	}

	public String getDsMagazzino() {
		return dsMagazzino;
	}

	public void setDsMagazzino(String dsMagazzino) {
		this.dsMagazzino = dsMagazzino;
	}

	public String getCd_bene_servizio(){
		return cd_bene_servizio;
	}

	public void setCd_bene_servizio(String cd_bene_servizio) {
		this.cd_bene_servizio = cd_bene_servizio;
	}

	public String getDs_bene_servizio() {
		return ds_bene_servizio;
	}

	public void setDs_bene_servizio(String ds_bene_servizio) {
		this.ds_bene_servizio = ds_bene_servizio;
	}

	public String getCd_categoria_padre() {
		return cd_categoria_padre;
	}

	public void setCd_categoria_padre(String cd_categoria_padre) {
		this.cd_categoria_padre = cd_categoria_padre;
	}

	public String getCd_proprio() {
		return cd_proprio;
	}

	public void setCd_proprio(String cd_proprio) {
		this.cd_proprio = cd_proprio;
	}

	public String getUnita_misura() {
		return unita_misura;
	}

	public void setUnita_misura(String unita_misura) {
		this.unita_misura = unita_misura;
	}

	public Integer getGiacenzaAttuale() {
		return giacenzaAttuale;
	}

	public void setGiacenzaAttuale(Integer giacenzaAttuale) {
		this.giacenzaAttuale = giacenzaAttuale;
	}

	public Integer getQtaInizioAnno() {
		return Optional.ofNullable(qtaInizioAnno).orElse(0);
	}

	public void setQtaInizioAnno(Integer qtaInizioAnno) {
		this.qtaInizioAnno = qtaInizioAnno;
	}

	public Integer getQtaCaricoAnno() {
		return Optional.ofNullable(qtaCaricoAnno).orElse(0);
	}

	public void setQtaCaricoAnno(Integer qtaCaricoAnno) {
		this.qtaCaricoAnno = qtaCaricoAnno;
	}

	public Integer getQtaScaricoAnno() {
		return Optional.ofNullable(qtaScaricoAnno).orElse(0);
	}

	public void setQtaScaricoAnno(Integer qtaScaricoAnno) {
		this.qtaScaricoAnno = qtaScaricoAnno;
	}

	public Integer getQtaGiacenzaInizioAnno() {
		return (getQtaInizioAnno()+getQtaCaricoAnno())-getQtaScaricoAnno();
	}

	public void setQtaGiacenzaInizioAnno(Integer qtaGiacenzaInizioAnno) {
		this.qtaGiacenzaInizioAnno = qtaGiacenzaInizioAnno;
	}
}