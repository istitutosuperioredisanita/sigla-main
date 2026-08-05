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
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
	protected void completeAllegato(AllegatoObbligazioneBulk allegato, StorageObject storageObject) throws ApplicationException {

		Optional.ofNullable(storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()))
				.map(strings -> strings.stream())
				.ifPresent(stringStream -> {
					stringStream
							.filter(s -> AllegatoObbligazioneBulk.aspectNamesKeys.get(s) != null)
							.findFirst()
							.ifPresent(s -> (( AllegatoObbligazioneBulk) allegato).setAspectName(s));
				});
	}
	private String getStorePath(ObbligazioneBulk allegatoParentBulk,boolean completeAllegatocreate){
		return allegatoParentBulk.getStorePath();
	}
	public OggettoBulk findAllegati(  ObbligazioneBulk oggettobulk, boolean includeSubFolder)throws ApplicationException {
		String path = oggettobulk.getStorePath();
		return findAllegati(  oggettobulk, path,  includeSubFolder);
	}
	public OggettoBulk findAllegati( ObbligazioneBulk oggettobulk, String path, boolean includeSubFolder) throws ApplicationException {
			AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) oggettobulk;

			if (path == null)
				return oggettobulk;
			if (getStorageObjectByPath(path) == null)
				return oggettobulk;
			for (StorageObject storageObject : getChildren(getStorageObjectByPath(path).getKey())) {
				if (hasAspect(storageObject, StoragePropertyNames.SYS_ARCHIVED.value()))
					continue;

				if (Optional.ofNullable(storageObject.getPropertyValue(StoragePropertyNames.BASE_TYPE_ID.value()))
						.map(String.class::cast)
						.filter(s -> s.equals(StoragePropertyNames.CMIS_FOLDER.value()))
						.isPresent()) {
					if (includeSubFolder)
						findAllegati( oggettobulk, storageObject.getPath(),Boolean.FALSE);
					continue;
				}
				final String primaryPath = getStorePath( oggettobulk, false);
				AllegatoObbligazioneBulk allegato = new AllegatoObbligazioneBulk( storageObject.getKey());
				allegato.setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
				allegato.setNome(storageObject.getPropertyValue(StoragePropertyNames.NAME.value()));
				allegato.setDescrizione(storageObject.getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));
				allegato.setTitolo(storageObject.getPropertyValue(StoragePropertyNames.TITLE.value()));
				allegato.setLastModificationDate(
						Optional.ofNullable(storageObject.<Calendar>getPropertyValue(StoragePropertyNames.LAST_MODIFIED.value()))
								.map(calendar -> calendar.getTime())
								.orElse(new Date()));

				allegato.setRelativePath(
						Optional.ofNullable(storageObject.getPath())
								.map(s -> s.substring(s.indexOf(primaryPath) + primaryPath.length()))
								.map(s -> s.substring(0, s.lastIndexOf(StorageDriver.SUFFIX)))
								.orElse(StorageDriver.SUFFIX)
				);
				completeAllegato(allegato, storageObject);
				allegato.setCrudStatus(OggettoBulk.NORMAL);
				allegatoParentBulk.addToArchivioAllegati(allegato);
			}
			return oggettobulk;

	}

}