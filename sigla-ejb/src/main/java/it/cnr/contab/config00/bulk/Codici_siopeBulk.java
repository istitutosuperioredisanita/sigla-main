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
 * Date 23/04/2007
 */
package it.cnr.contab.config00.bulk;
import it.cnr.contab.config00.pdcep.cla.bulk.V_classificazione_voci_epBulk;
import it.cnr.contab.util.enumeration.GestioneEntrataSpesa;
import it.cnr.contab.util.enumeration.TipoDebitoSIOPE;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.util.Dictionary;

public class Codici_siopeBulk extends Codici_siopeBase {

	private V_classificazione_voci_epBulk v_classificazione_voci_ep_siope;
	private V_classificazione_voci_epBulk v_classificazione_voci_ep_siope_rend;
    public static final Dictionary<String, String> tiTipoDebitoSIOPEKeys = TipoDebitoSIOPE.TipoDebitoSIOPE_CN_Keys;
    public static final Dictionary<String, String> tiGestioneKeys = GestioneEntrataSpesa.ti_gestioneSingolareKeys;

	public Codici_siopeBulk() {
		super();
	}
	public Codici_siopeBulk(java.lang.Integer esercizio, java.lang.String ti_gestione, java.lang.String cd_siope) {
		super(esercizio, ti_gestione, cd_siope);
	}
	public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp,it.cnr.jada.action.ActionContext context) {
		setEsercizio(it.cnr.contab.utenze00.bulk.CNRUserInfo.getEsercizio(context));
		return this;
	}
	public OggettoBulk initializeForSearch(it.cnr.jada.util.action.CRUDBP bp,it.cnr.jada.action.ActionContext context) {
		setEsercizio(it.cnr.contab.utenze00.bulk.CNRUserInfo.getEsercizio(context));
		return this;
	}

	public V_classificazione_voci_epBulk getV_classificazione_voci_ep_siope() {
		return v_classificazione_voci_ep_siope;
	}

	public void setV_classificazione_voci_ep_siope(V_classificazione_voci_epBulk v_classificazione_voci_ep_siope) {
		this.v_classificazione_voci_ep_siope = v_classificazione_voci_ep_siope;
	}

	@Override
	public Integer getId_classificazione_siope() {
		if (getV_classificazione_voci_ep_siope() == null)
			return null;
		return getV_classificazione_voci_ep_siope().getId_classificazione();
	}

	@Override
	public void setId_classificazione_siope(Integer id_classificazione_siope) {
		getV_classificazione_voci_ep_siope().setId_classificazione(id_classificazione_siope);
	}

	public V_classificazione_voci_epBulk getV_classificazione_voci_ep_siope_rend() {
		return v_classificazione_voci_ep_siope_rend;
	}

	public void setV_classificazione_voci_ep_siope_rend(V_classificazione_voci_epBulk v_classificazione_voci_ep_siope_rend) {
		this.v_classificazione_voci_ep_siope_rend = v_classificazione_voci_ep_siope_rend;
	}

	@Override
	public Integer getId_classificazione_siope_rend() {
		if (getV_classificazione_voci_ep_siope_rend() == null)
			return null;
		return getV_classificazione_voci_ep_siope_rend().getId_classificazione();
	}

	@Override
	public void setId_classificazione_siope_rend(Integer id_classificazione_siope_rend) {
		getV_classificazione_voci_ep_siope_rend().setId_classificazione(id_classificazione_siope_rend);
	}
}