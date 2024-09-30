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

import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class MagContoGiudizialeBulk extends AbilitazioneMagazzinoBulk implements KeyedPersistent{
	private static final long serialVersionUID = 1L;

	private Integer giacenza;
	private Integer qtaInizio;
	private Integer qtaCarico;
	private Integer qtaScarico;
	private Integer qtaGiacenza;
	private MagazzinoBulk magazzino = new MagazzinoBulk();


	public MagContoGiudizialeBulk() {
		super();
	}

	public Integer getGiacenza() {
		return giacenza;
	}

	public void setGiacenza(Integer giacenza) {
		this.giacenza = giacenza;
	}

	public Integer getQtaInizio() {
		return qtaInizio;
	}

	public void setQtaInizio(Integer qtaInizio) {
		this.qtaInizio = qtaInizio;
	}

	public Integer getQtaCarico() {
		return qtaCarico;
	}

	public void setQtaCarico(Integer qtaCarico) {
		this.qtaCarico = qtaCarico;
	}

	public Integer getQtaScarico() {
		return qtaScarico;
	}

	public void setQtaScarico(Integer qtaScarico) {
		this.qtaScarico = qtaScarico;
	}

	public Integer getQtaGiacenza() {
		return qtaGiacenza;
	}

	public void setQtaGiacenza(Integer qtaGiacenza) {
		this.qtaGiacenza = qtaGiacenza;
	}

	public MagazzinoBulk getMagazzino() {
		return magazzino;
	}

	public void setMagazzino(MagazzinoBulk magazzino) {
		this.magazzino = magazzino;
	}
}