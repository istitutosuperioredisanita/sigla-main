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

public class MagContoGiudizialeBulk extends OggettoBulk implements Persistent {
	private static final long serialVersionUID = 1L;

	private String cd_magazzino;

	private String cd_elemento_voce;

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

	public String getCd_elemento_voce() {
		return cd_elemento_voce;
	}

	public void setCd_elemento_voce(String cd_elemento_voce) {
		this.cd_elemento_voce = cd_elemento_voce;
	}

	public Integer getGiacenzaAttuale() {
		return giacenzaAttuale;
	}

	public void setGiacenzaAttuale(Integer giacenzaAttuale) {
		this.giacenzaAttuale = giacenzaAttuale;
	}

	public Integer getQtaInizioAnno() {
		return qtaInizioAnno;
	}

	public void setQtaInizioAnno(Integer qtaInizioAnno) {
		this.qtaInizioAnno = qtaInizioAnno;
	}

	public Integer getQtaCaricoAnno() {
		return qtaCaricoAnno;
	}

	public void setQtaCaricoAnno(Integer qtaCaricoAnno) {
		this.qtaCaricoAnno = qtaCaricoAnno;
	}

	public Integer getQtaScaricoAnno() {
		return qtaScaricoAnno;
	}

	public void setQtaScaricoAnno(Integer qtaScaricoAnno) {
		this.qtaScaricoAnno = qtaScaricoAnno;
	}

	public Integer getQtaGiacenzaInizioAnno() {
		return qtaGiacenzaInizioAnno;
	}

	public void setQtaGiacenzaInizioAnno(Integer qtaGiacenzaInizioAnno) {
		this.qtaGiacenzaInizioAnno = qtaGiacenzaInizioAnno;
	}
}