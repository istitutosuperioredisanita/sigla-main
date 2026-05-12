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
 * Created by Aurelio's BulkGenerator 1.0
 * Date 28/09/2006
 */
package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.jada.bulk.OggettoBulk;

import java.util.Optional;

public class Stipendi_cofi_obb_scadBulk extends Stipendi_cofi_obb_scadBase {
	public Stipendi_cofi_obb_scadBulk() {
		super();
	}
	public Stipendi_cofi_obb_scadBulk(java.lang.Integer esercizio, java.lang.Integer mese, java.lang.String cd_cds_obbligazione, java.lang.Integer esercizio_obbligazione, java.lang.Integer esercizio_ori_obbligazione, java.lang.Long pg_obbligazione) {
		super(esercizio, mese, cd_cds_obbligazione, esercizio_obbligazione, esercizio_ori_obbligazione, pg_obbligazione);
	}

	public static final java.util.Map meseKeys = Stipendi_cofiBulk.meseKeys;

	private Stipendi_cofiBulk stipendi_cofi = new Stipendi_cofiBulk();

    private Stipendi_cofi_obbBulk stipendi_cofi_obb = new Stipendi_cofi_obbBulk();    
   
	public java.lang.Integer getEsercizio() {
		return Optional.ofNullable(this.getStipendi_cofi()).map(Stipendi_cofiBulk::getEsercizio).orElse(null);
	}

	public void setEsercizio(java.lang.Integer esercizio) {
		Optional.ofNullable(this.getStipendi_cofi()).ifPresent(el->el.setEsercizio(esercizio));
	}
	
	public java.lang.Integer getMese() {
		return Optional.ofNullable(this.getStipendi_cofi()).map(Stipendi_cofiBulk::getMese).orElse(null);
	}

	public void setMese(java.lang.Integer mese)	{
		Optional.ofNullable(this.getStipendi_cofi()).ifPresent(el->el.setMese(mese));
	}

	public java.lang.Integer getMese_reale() {
		return Optional.ofNullable(this.getStipendi_cofi()).map(Stipendi_cofiBulk::getMese_reale).orElse(null);
	}

	public java.lang.Integer getProg_flusso() {
		return Optional.ofNullable(this.getStipendi_cofi()).map(Stipendi_cofiBulk::getProg_flusso).orElse(null);
	}

    public Stipendi_cofiBulk getStipendi_cofi() {
    	return stipendi_cofi;
    }
    public void setStipendi_cofi(Stipendi_cofiBulk bulk) {
    	stipendi_cofi = bulk;
    }

	public java.lang.String getCd_cds_obbligazione() {
		return Optional.ofNullable(this.getStipendi_cofi_obb()).map(Stipendi_cofi_obbBulk::getCd_cds_obbligazione).orElse(null);
	}
	public void setCd_cds_obbligazione(java.lang.String cd_cds_obbligazione) {
		Optional.ofNullable(this.getStipendi_cofi_obb()).ifPresent(el->el.setCd_cds_obbligazione(cd_cds_obbligazione));
	}
	public java.lang.Integer getEsercizio_obbligazione() {
		return Optional.ofNullable(this.getStipendi_cofi_obb()).map(Stipendi_cofi_obbBulk::getEsercizio_obbligazione).orElse(null);
	}
	public void setEsercizio_obbligazione(java.lang.Integer esercizio_obbligazione) {
		Optional.ofNullable(this.getStipendi_cofi_obb()).ifPresent(el->el.setEsercizio_obbligazione(esercizio_obbligazione));
	}
	public java.lang.Integer getEsercizio_ori_obbligazione() {
		return Optional.ofNullable(this.getStipendi_cofi_obb()).map(Stipendi_cofi_obbBulk::getEsercizio_ori_obbligazione).orElse(null);
	}
	public void setEsercizio_ori_obbligazione(java.lang.Integer esercizio_ori_obbligazione) {
		Optional.ofNullable(this.getStipendi_cofi_obb()).ifPresent(el->el.setEsercizio_ori_obbligazione(esercizio_ori_obbligazione));
	}
	public java.lang.Long getPg_obbligazione() {
		return Optional.ofNullable(this.getStipendi_cofi_obb()).map(Stipendi_cofi_obbBulk::getPg_obbligazione).orElse(null);
	}
	public void setPg_obbligazione(java.lang.Long pg_obbligazione) {
		Optional.ofNullable(this.getStipendi_cofi_obb()).ifPresent(el->el.setPg_obbligazione(pg_obbligazione));
	}
	public OggettoBulk initialize(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context){
		super.initialize(bp, context);
		this.setEsercizio( ((it.cnr.contab.utenze00.bp.CNRUserContext)context.getUserContext()).getEsercizio() );
		return this;
	}
	public Stipendi_cofi_obbBulk getStipendi_cofi_obb() {
		return stipendi_cofi_obb;
	}
	public void setStipendi_cofi_obb(Stipendi_cofi_obbBulk stipendi_cofi_obb) {
		this.stipendi_cofi_obb = stipendi_cofi_obb;
	}

	public static java.util.Map getMeseKeys() {
		return meseKeys;
	}
}