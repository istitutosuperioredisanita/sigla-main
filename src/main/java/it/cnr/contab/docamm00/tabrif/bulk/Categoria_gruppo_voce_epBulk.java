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

import it.cnr.contab.config00.pdcep.bulk.ContoBulk;

import java.util.Optional;

public class Categoria_gruppo_voce_epBulk extends Categoria_gruppo_voce_epBase {

	private Categoria_gruppo_inventBulk categoria_gruppo = new Categoria_gruppo_inventBulk();

	protected ContoBulk conto = new ContoBulk();

	protected ContoBulk contoContropartita = new ContoBulk();

	protected ContoBulk contoMinusvalenza = new ContoBulk();

	protected ContoBulk contoPlusvalenza = new ContoBulk();

	public Categoria_gruppo_voce_epBulk() {
		super();
	}

	public Categoria_gruppo_voce_epBulk(String rowid) {
		super(rowid);
	}

	public Categoria_gruppo_inventBulk getCategoria_gruppo() {
	return categoria_gruppo;
}

	public void setCategoria_gruppo(Categoria_gruppo_inventBulk newCategoria_gruppo) {
		categoria_gruppo = newCategoria_gruppo;
	}

	public String getCd_categoria_gruppo() {
		return Optional.ofNullable(this.getCategoria_gruppo()).map(Categoria_gruppo_inventBulk::getCd_categoria_gruppo).orElse(null);
	}

	public void setCd_categoria_gruppo(String cd_categoria_gruppo) {
		if (!Optional.ofNullable(this.getCategoria_gruppo()).isPresent())
			this.setCategoria_gruppo(new Categoria_gruppo_inventBulk());
		this.getCategoria_gruppo().setCd_categoria_gruppo(cd_categoria_gruppo);
	}

	public ContoBulk getConto() {
		return conto;
	}

	public void setConto(ContoBulk conto) {
		this.conto = conto;
	}

	@Override
	public String getCd_voce_ep() {
		return Optional.ofNullable(this.getConto()).map(ContoBulk::getCd_voce_ep).orElse(null);
	}

	public ContoBulk getContoContropartita() {
		return contoContropartita;
	}

	public void setContoContropartita(ContoBulk contoContropartita) {
		this.contoContropartita = contoContropartita;
	}

	@Override
	public String getCd_voce_ep_contr() {
		return Optional.ofNullable(this.getContoContropartita()).map(ContoBulk::getCd_voce_ep).orElse(null);
	}

	public ContoBulk getContoMinusvalenza() {
		return contoMinusvalenza;
	}

	public void setContoMinusvalenza(ContoBulk contoMinusvalenza) {
		this.contoMinusvalenza = contoMinusvalenza;
	}

	@Override
	public String getCd_voce_ep_minus() {
		return Optional.ofNullable(this.getContoMinusvalenza()).map(ContoBulk::getCd_voce_ep).orElse(null);
	}

	public ContoBulk getContoPlusvalenza() {
		return contoPlusvalenza;
	}

	public void setContoPlusvalenza(ContoBulk contoPlusvalenza) {
		this.contoPlusvalenza = contoPlusvalenza;
	}

	@Override
	public String getCd_voce_ep_plus() {
		return Optional.ofNullable(this.getContoPlusvalenza()).map(ContoBulk::getCd_voce_ep).orElse(null);
	}
}