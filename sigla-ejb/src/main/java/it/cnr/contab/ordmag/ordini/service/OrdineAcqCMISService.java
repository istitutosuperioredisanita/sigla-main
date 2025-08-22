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

package it.cnr.contab.ordmag.ordini.service;

import it.cnr.contab.firma.bulk.FirmaOTPBulk;
import it.cnr.contab.ordmag.ordini.bulk.AllegatoOrdineDettaglioBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util.SignP7M;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.DateUtils;
import it.cnr.si.firmadigitale.firma.arss.ArubaSignServiceClient;
import it.cnr.si.firmadigitale.firma.arss.ArubaSignServiceException;
import it.cnr.si.spring.storage.MimeTypes;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class OrdineAcqCMISService extends StoreService {

	private transient static final Logger logger = LoggerFactory.getLogger(OrdineAcqCMISService.class);
	public static final String ASPECT_STAMPA_ORDINI = "P:ordini_acq_attachment:stampa";
	public static final String ASPECT_STATO_STAMPA_ORDINI = "P:ordini_acq_attachment:stato_stampa";
	public static final String ASPECT_ORDINI_DETTAGLIO = "P:ordini_acq_attachment:allegati_dettaglio";
	public static final String ASPECT_ALLEGATI_ORDINI = "P:ordini_acq_attachment:allegati";
	public static final String CMIS_ORDINI_ACQ_ANNO = "ordini_acq:anno";
	public static final String CMIS_ORDINI_ACQ_NUMERO = "ordini_acq:numero";
	public static final String CMIS_ORDINI_ACQ_OGGETTO = "ordini_acq:oggetto";
	public static final String CMIS_ORDINI_ACQ_DETTAGLIO_RIGA = "ordini_acq_dettaglio:riga";
	public static final String CMIS_ORDINI_ACQ_UOP = "ordini_acq:cd_unita_operativa";
	public static final String CMIS_ORDINI_ACQ_NUMERATORE = "ordini_acq:cd_numeratore";
	public static final String STATO_STAMPA_ORDINE_VALIDO = "VAL";
	public static final String STATO_STAMPA_ORDINE_ANNULLATO = "ANN";

	@Autowired
	private StorageDriver storageDriver;
	@Autowired
	private ArubaSignServiceClient arubaSignServiceClient;


	public List<StorageObject> getFilesOrdine(OrdineAcqBulk ordine) throws BusinessProcessException{
    	if ( Optional.ofNullable(recuperoFolderOrdineSigla(ordine)).isPresent())
			return getChildren(recuperoFolderOrdineSigla(ordine).getKey());
    	return Collections.EMPTY_LIST;
    }

    private List<StorageObject> getDocuments(String storageObjectKey, String tipoAllegato) throws ApplicationException {
        return getChildren(storageObjectKey).stream()
                .filter(storageObject -> tipoAllegato == null || storageObject.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value()).equals(tipoAllegato))
                .collect(Collectors.toList());
    }

	public StorageObject recuperoFolderOrdineSigla(OrdineAcqBulk ordine) throws BusinessProcessException{
        return getStorageObjectByPath(getStorePath(ordine));
	}
	
	public String createFolderOrdineIfNotPresent(String path, OrdineAcqBulk ordine) throws ApplicationException{
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		String folderName = sanitizeFolderName(ordine.constructCMISNomeFile());
		metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:ordini_acq:main");
		metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERATORE, ordine.getCdNumeratore());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_ANNO, ordine.getEsercizio());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERO, ordine.getNumero());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_UOP, ordine.getCdUnitaOperativa());
		metadataProperties.put("sigla_commons_aspect:utente_applicativo", ordine.getUtuv());
		List<String> aspectsToAdd = new ArrayList<String>();
		aspectsToAdd.add("P:cm:titled");
		aspectsToAdd.add("P:sigla_commons_aspect:utente_applicativo_sigla");
        metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);
        return createFolderIfNotPresent(path, folderName, metadataProperties);
	}

	public String createFolderOrdineIfNotPresent(String path, OrdineAcqRigaBulk ordineAcqRigaBulk) throws ApplicationException{
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		String folderName = sanitizeFolderName(ordineAcqRigaBulk.constructCMISNomeFile());
		metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:ordini_acq_dettaglio:main");
		metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERATORE, ordineAcqRigaBulk.getCdNumeratore());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_ANNO, ordineAcqRigaBulk.getEsercizio());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERO, ordineAcqRigaBulk.getNumero());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_UOP, ordineAcqRigaBulk.getCdUnitaOperativa());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_DETTAGLIO_RIGA, ordineAcqRigaBulk.getRiga());
		metadataProperties.put("sigla_commons_aspect:utente_applicativo", ordineAcqRigaBulk.getUtuv());
		List<String> aspectsToAdd = new ArrayList<String>();
		aspectsToAdd.add("P:cm:titled");
		aspectsToAdd.add("P:sigla_commons_aspect:utente_applicativo_sigla");
		metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);
		return createFolderIfNotPresent(path, folderName, metadataProperties);
	}
	private String getStorePathNewPath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		try {
			String path =Arrays.asList(
					SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOrganizzativa(),
					"Ordini",
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOperativa(),
					allegatoParentBulk.getCdNumeratore(),
					Optional.ofNullable(allegatoParentBulk.getEsercizio())
							.map(esercizio -> String.valueOf(esercizio))
							.orElse("0")
			).stream().collect(
					Collectors.joining(StorageDriver.SUFFIX)
			);

			return createFolderOrdineIfNotPresent(path, allegatoParentBulk);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	//refactoring path ordini issue
	private String getStorePathOldPath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		try {
			String path =Arrays.asList(
					SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOrganizzativa(),
					"Ordini",
					allegatoParentBulk.getCdNumeratore(),
					Optional.ofNullable(allegatoParentBulk.getEsercizio())
							.map(esercizio -> String.valueOf(esercizio))
							.orElse("0")
			).stream().collect(
					Collectors.joining(StorageDriver.SUFFIX)
			);
			String pathFolderOrdine = path.concat(path.equals("/") ? "" : "/").concat(sanitizeFolderName(allegatoParentBulk.constructCMISNomeFile()));
			StorageObject folder =this.getStorageObjectByPath( pathFolderOrdine,true,false);
			List<StorageObject> children= Optional.ofNullable(folder).map(m->this.getChildren(m.getPath())).orElse(null);
			//List<StorageObject> children=this.getChildren(folder.getPath());
			if ( !children.isEmpty()) {
				//check se esiste il file di stampa e verifica il nome
				StorageObject fileStampaOridne = children.stream()
						.filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_ORDINI))
						.findFirst().map(
								storageObject->storageObject
						).orElse(null);
				boolean bContainNomeFiles;
				//se esiste il file del report verifico che sia quello dell'ordine in esame
				if ( fileStampaOridne!=null) {
					bContainNomeFiles = fileStampaOridne.getKey().contains(allegatoParentBulk.recuperoIdOrdineAsString());
					if ( !bContainNomeFiles)
						return null;
				}
				return createFolderOrdineIfNotPresent(path, allegatoParentBulk);
			}
			return null;
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}

	public String getStorePath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		String path =null;
		if ( allegatoParentBulk.getDacr().compareTo(DateUtils.firstDateOfTheYear(2024))<=0) {
			path = getStorePathOldPath(allegatoParentBulk);
			if (path != null)
				return path;
		}
		return getStorePathNewPath(allegatoParentBulk);
	}

	public String getStorePathDettaglio(OrdineAcqRigaBulk ordineAcqRigaBulk) throws BusinessProcessException{
		try {
			String path = Arrays.asList(
					getStorePath(ordineAcqRigaBulk.getOrdineAcq()),
					ordineAcqRigaBulk.constructCMISNomeFile()
			).stream().collect(
					Collectors.joining(StorageDriver.SUFFIX)
			);
			return createFolderOrdineIfNotPresent(path, ordineAcqRigaBulk);
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		}
	}
	public StorageObject getStorageObjectStampaOrdine(OrdineAcqBulk ordine)throws Exception{
		return getFilesOrdine(ordine).stream()
				.filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_ORDINI))
				.findFirst().map(
						storageObject->storageObject
				).orElse(null);
	}

	public boolean alreadyExsistValidStampaOrdine(OrdineAcqBulk ordine) throws Exception{
		StorageObject s = getStorageObjectStampaOrdine(ordine);
		if (Optional.ofNullable(getStorageObjectStampaOrdine(ordine))
				.flatMap(storageObject -> Optional.ofNullable(storageObject.<String>getPropertyValue("ordine_acq:stato")))
				.filter(stato->stato.equalsIgnoreCase(OrdineAcqBulk.STATO.get(OrdineAcqBulk.STATO_ALLA_FIRMA).toString())
							  ||stato.equalsIgnoreCase(OrdineAcqBulk.STATO.get(OrdineAcqBulk.STATO_DEFINITIVO).toString())).isPresent())
					return true;
		return false;

	}
    public InputStream getStreamOrdine(OrdineAcqBulk ordine) throws Exception{
    	return Optional.ofNullable(getStorageObjectStampaOrdine( ordine)).map(
				storageObject -> getResource(storageObject.getKey())
		).orElse(null);
    }
	public String signOrdine(SignP7M signP7M, String path) throws ApplicationException {
		StorageObject storageObject = storageDriver.getObject(signP7M.getNodeRefSource());
		StorageObject docFirmato =null;
		try {

			byte[] bytesSigned = arubaSignServiceClient.pdfsignatureV2(
					signP7M.getUsername(),
					signP7M.getPassword(),
					signP7M.getOtp(),
					IOUtils.toByteArray(getResource(storageObject)));

			Map<String, Object> metadataProperties = new HashMap<>();
			metadataProperties.put(StoragePropertyNames.NAME.value(), signP7M.getNomeFile());
			docFirmato=updateStream(storageObject.getKey(),new ByteArrayInputStream(bytesSigned),MimeTypes.PDF.mimetype());
			addAspect(storageObject,"P:cnr:signedDocument");

			logger.info("Ordine {} firmato correttamente.", signP7M.getNomeFile());

			return docFirmato.getKey();
		} catch (ArubaSignServiceException _ex) {
			logger.error("ERROR firma fatture attive", _ex);
			throw new ApplicationException(FirmaOTPBulk.errorMessage(_ex.getMessage()));
		} catch (IOException e) {
			throw new ApplicationException( e );
		}
	}

	public BulkList<AllegatoGenericoBulk> recuperoAllegatiDettaglioOrdine(OrdineAcqRigaBulk ordineAcqRigaBulk) throws BusinessProcessException {
		BulkList<AllegatoGenericoBulk> allegatoGenericoBulks = new BulkList<AllegatoGenericoBulk>();
		String path = getStorePathDettaglio(ordineAcqRigaBulk);
		if (getStorageObjectByPath(path) == null)
			return allegatoGenericoBulks;
		for (StorageObject storageObject : getChildren(getStorageObjectByPath(path).getKey())) {
			if (hasAspect(storageObject, StoragePropertyNames.SYS_ARCHIVED.value()))
				continue;
			if (Optional.ofNullable(storageObject.getPropertyValue(StoragePropertyNames.BASE_TYPE_ID.value()))
					.map(String.class::cast)
					.filter(s -> s.equals(StoragePropertyNames.CMIS_FOLDER.value()))
					.isPresent()) {
				continue;
			}
			AllegatoOrdineDettaglioBulk allegato = new AllegatoOrdineDettaglioBulk(storageObject.getKey());
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
							.map(s -> s.substring(s.indexOf(path) + path.length()))
							.map(s -> s.substring(0, s.lastIndexOf(StorageDriver.SUFFIX)))
							.orElse(StorageDriver.SUFFIX)
			);
			allegato.setCrudStatus(OggettoBulk.NORMAL);
			allegatoGenericoBulks.add(allegato);
		}
		return allegatoGenericoBulks;
	}

}
