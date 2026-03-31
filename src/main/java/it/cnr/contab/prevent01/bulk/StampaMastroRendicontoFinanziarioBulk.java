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
 * Created on Oct 4, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.prevent01.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

import java.util.Enumeration;

/**
 * @author 
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StampaMastroRendicontoFinanziarioBulk extends Stampa_pdgp_bilancioBulk implements KeyedPersistent {

	public final static Integer TIPO_VOCE = 6;

	public Integer getLivello_stampa() {
		return livello_stampa;
	}

	public void setLivello_stampa(Integer livello_stampa) {
		this.livello_stampa = livello_stampa;
	}

	private Integer livello_stampa;

	private it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_da = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();

	public it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk getVoce_da() {
		return voce_da;
	}

	public void setVoce_da(it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_da) {
		this.voce_da = voce_da;
	}

	public java.lang.String getCd_voce_da() {
		it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_da = this.getVoce_da();
		if (voce_da == null)
			return null;
		return voce_da.getCd_elemento_voce();
	}

	private it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_a = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();

	public it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk getVoce_a() {
		return voce_a;
	}

	public void setVoce_a(it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_a) {
		this.voce_a = voce_a;
	}

	public java.lang.String getCd_voce_a() {
		it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk voce_a = this.getVoce_a();
		if (voce_a == null)
			return null;
		return voce_a.getCd_elemento_voce();
	}

	public StampaMastroRendicontoFinanziarioBulk() {
		super();
	}

	public Integer getNr_livello() {
		return 5;
	}

}
