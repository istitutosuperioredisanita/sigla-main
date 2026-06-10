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

package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.ordmag.ordini.service.OrdineAcqCMISService;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.ArrayList;
import java.util.List;

public class AllegatoObbligazioneBulk extends AllegatoGenericoBulk {
	private static final long serialVersionUID = 1L;

	public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

	public static final String ASPECT_ALLEGATI_OBBLIGAZIONI= "P:obbligazioni_attachment:allegati";
	public static final String ASPECT_ALLEGATI_APPROVAZIONE= "P:obbligazioni_attachment:approvazione";

	static {
		aspectNamesKeys.put(ASPECT_ALLEGATI_OBBLIGAZIONI,"Altro");
		aspectNamesKeys.put(ASPECT_ALLEGATI_APPROVAZIONE,"Approvazione");
	}

	private Integer esercizioDiAppartenenza;
	private String aspectName;

	public String getAspectName() {
		return aspectName;
	}

	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	@StorageProperty(name = "cmis:secondaryObjectTypeIds")
	public List<String> getAspect() {
		List<String> results = new ArrayList<>();

		results.add("P:cm:titled");

		if (getAspectName() != null && !getAspectName().isEmpty()) {
			results.add(getAspectName());
		}

		return results;
	}

	public AllegatoObbligazioneBulk() {
		super();
	}

	public AllegatoObbligazioneBulk(StorageObject storageObject) {
		super(storageObject.getKey());
	}

	public AllegatoObbligazioneBulk(String storageKey) {
		super(storageKey);
	}

	public static OrderedHashtable getAspectNamesKeys() {
		return aspectNamesKeys;
	}

	public static void setAspectNamesKeys(OrderedHashtable aspectNamesKeys) {
		AllegatoObbligazioneBulk.aspectNamesKeys = aspectNamesKeys;
	}

	public void setEsercizioDiAppartenenza(Integer esercizioDiAppartenenza) {
		this.esercizioDiAppartenenza = esercizioDiAppartenenza;
	}
	
	public Integer getEsercizioDiAppartenenza() {
		return esercizioDiAppartenenza;
	}
}