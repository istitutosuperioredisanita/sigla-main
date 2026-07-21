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

package it.cnr.contab.doccont00.service;

import it.cnr.contab.doccont00.core.bulk.AllegatoObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObbligazioneService extends StoreService {
	private transient static final Logger logger = LoggerFactory.getLogger(ObbligazioneService.class);


	public StorageObject getFolderObbligazione (ObbligazioneBulk obbligazione) throws ApplicationException{
		return Optional.ofNullable(getStorageObjectByPath(getCMISPathFolderObbligazione(obbligazione)))
				.orElseGet(() -> {
					return null;
				});
	}


	private List<String> getBasePath(ObbligazioneBulk obbligazioneBulk) {
		return  Arrays.asList(obbligazioneBulk.getBasePath());
	}

	public String getCMISPath(ObbligazioneBulk obbligazioneBulk) {
		return getBasePath(obbligazioneBulk).stream().collect(
				Collectors.joining(StorageDriver.SUFFIX)
		);
	}

	public String getCMISPathFolderObbligazione(ObbligazioneBulk obbligazioneBulk) {
		return Stream.concat(getBasePath(obbligazioneBulk).stream(), Stream.of(obbligazioneBulk.getCMISFolderName())).collect(
				Collectors.joining(StorageDriver.SUFFIX)
		);
	}

	public String getCMISPathAllegati(ObbligazioneBulk obbligazioneBulk){
		List<String > l = new ArrayList<String>(getBasePath(obbligazioneBulk));
		l.add(obbligazioneBulk.getCMISFolderName());
		return l.stream().collect(Collectors.joining(StorageDriver.SUFFIX));
	}



	public void changeProgressivoNodeRef(StorageObject oldStorageObject, ObbligazioneBulk obbligazioneBulk) throws ApplicationException {
		List<StorageObject> children = getChildren(oldStorageObject.getKey());
		for (StorageObject child : children) {
			AllegatoObbligazioneBulk allegato = new AllegatoObbligazioneBulk(child.getKey());
			allegato.setNome(child.<String>getPropertyValue("cmis:name"));
			//allegato.setType(child.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value()));
			allegato.setTitolo(child.<String>getPropertyValue(StoragePropertyNames.TITLE.value()));
			allegato.setDescrizione(child.<String>getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));

			updateProperties(allegato, child);

		}
		updateProperties(obbligazioneBulk, oldStorageObject);
	}

}