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

package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.ArrayList;
import java.util.List;

public abstract class AllegatoDocTraspRientroBulk extends AllegatoGenericoBulk {
	private static final long serialVersionUID = 1L;



	public AllegatoDocTraspRientroBulk() {
		super();
	}

	public AllegatoDocTraspRientroBulk(String storageKey) {
		super(storageKey);
	}

	public abstract String getAspectName() ;


	@StorageProperty(name="cmis:secondaryObjectTypeIds")
	public List<String> getAspect() {
		List<String> results = new ArrayList<String>();
		results.add("P:cm:titled");
		results.add(getAspectName());
		return results;
	}
	@StoragePolicy(
			name = "P:sigla_commons_aspect:utente_applicativo_sigla",
			property = @StorageProperty(name = "sigla_commons_aspect:utente_applicativo")
	)
	private String utenteSIGLA;

	public String getUtenteSIGLA() {
		return utenteSIGLA;
	}
	@Override
	public void complete(UserContext userContext) {
		setUtenteSIGLA(CNRUserContext.getUser(userContext));
		super.complete(userContext);
	}

	public void setUtenteSIGLA(String utenteSIGLA) {
		this.utenteSIGLA = utenteSIGLA;
	}

/*
	@Override
	public void validate() throws ValidationException {
		Optional.ofNullable(getAspectName())
				.orElseThrow(() -> new ValidationException("Attenzione: selezionare la tipologia di File!"));
		if (getAspectName().equalsIgnoreCase(P_SIGLA_FATTURE_ATTACHMENT_LIQUIDAZIONE)) {
			Optional.ofNullable(getDataProtocollo())
					.orElseThrow(() -> new ValidationException("Attenzione: la data protocollo è obbligatoria!"));
			Optional.ofNullable(getNumProtocollo())
					.orElseThrow(() -> new ValidationException("Attenzione: il numero di protocollo è obbligatorio!"));
		}
		super.validate();
	}
*/

}