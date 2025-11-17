/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class OrdineAcqCMISService extends StoreService {

	private transient static final Logger logger = LoggerFactory.getLogger(OrdineAcqCMISService.class);

	// Costanti CMIS e Aspetti
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

	/**
	 * Recupera tutti i file contenuti nella cartella dell'ordine.
	 */
	public List<StorageObject> getFilesOrdine(OrdineAcqBulk ordine) throws BusinessProcessException{
		logger.info("-> getFilesOrdine - Inizio recupero file per Ordine ID: {}", ordine.recuperoIdOrdineAsString());
		StorageObject folderOrdine = recuperoFolderOrdineSigla(ordine);

		if (Optional.ofNullable(folderOrdine).isPresent()) {
			String folderKey = folderOrdine.getKey();
			logger.info("   Folder dell'Ordine trovato. Key: {}", folderKey);
			List<StorageObject> children = getChildren(folderKey);
			logger.info("   Trovati {} oggetti figli nella cartella.", children.size());
			return children;
		}
		logger.warn("   Folder dell'Ordine non trovato per Ordine ID: {}. Restituisce lista vuota.", ordine.recuperoIdOrdineAsString());
		return Collections.EMPTY_LIST;
	}

	/**
	 * Filtra i documenti figli per tipo di allegato.
	 */
	private List<StorageObject> getDocuments(String storageObjectKey, String tipoAllegato) throws ApplicationException {
		logger.debug("-> getDocuments - Ricerca documenti figli per Key: {} con tipo: {}", storageObjectKey, tipoAllegato);
		List<StorageObject> children = getChildren(storageObjectKey);
		List<StorageObject> filtered = children.stream()
				.filter(storageObject -> tipoAllegato == null || storageObject.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value()).equals(tipoAllegato))
				.collect(Collectors.toList());
		logger.debug("   Trovati {} documenti filtrati.", filtered.size());
		return filtered;
	}

	/**
	 * Recupera l'oggetto CMIS (cartella) che rappresenta l'Ordine.
	 */
	public StorageObject recuperoFolderOrdineSigla(OrdineAcqBulk ordine) throws BusinessProcessException{
		String path = getStorePath(ordine);
		logger.info("-> recuperoFolderOrdineSigla - Recupero StorageObject per il path: {}", path);
		StorageObject folder = getStorageObjectByPath(path);
		if (folder != null) {
			logger.info("   Folder recuperato con Key: {}", folder.getKey());
		} else {
			logger.warn("   Folder non trovato per il path: {}", path);
		}
		return folder;
	}

	/**
	 * Crea la cartella principale dell'ordine se non esiste.
	 */
	public String createFolderOrdineIfNotPresent(String path, OrdineAcqBulk ordine) throws ApplicationException{
		logger.info("-> createFolderOrdineIfNotPresent (OrdineAcqBulk) - Path padre: {}", path);
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		String folderName = sanitizeFolderName(ordine.constructCMISNomeFile());
		logger.debug("   Nome della cartella da creare: {}", folderName);

		metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:ordini_acq:main");
		metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERATORE, ordine.getCdNumeratore());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_ANNO, ordine.getEsercizio());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERO, ordine.getNumero());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_UOP, ordine.getCdUnitaOperativa());
		metadataProperties.put("sigla_commons_aspect:utente_applicativo", ordine.getUtuv());

		List<String> aspectsToAdd = Arrays.asList("P:cm:titled", "P:sigla_commons_aspect:utente_applicativo_sigla");
		metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

		String folderKey = createFolderIfNotPresent(path, folderName, metadataProperties);
		logger.info("   Cartella principale creata/esistente. Chiave (Key) CMIS: {}", folderKey);
		return folderKey;
	}

	/**
	 * Crea la sottocartella di dettaglio (riga ordine) se non esiste.
	 */
	public String createFolderOrdineIfNotPresent(String path, OrdineAcqRigaBulk ordineAcqRigaBulk) throws ApplicationException{
		logger.info("-> createFolderOrdineIfNotPresent (OrdineAcqRigaBulk) - Path padre: {}", path);
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		String folderName = sanitizeFolderName(ordineAcqRigaBulk.constructCMISNomeFile());
		logger.debug("   Nome della cartella di dettaglio da creare: {}", folderName);

		metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:ordini_acq_dettaglio:main");
		metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERATORE, ordineAcqRigaBulk.getCdNumeratore());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_ANNO, ordineAcqRigaBulk.getEsercizio());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_NUMERO, ordineAcqRigaBulk.getNumero());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_UOP, ordineAcqRigaBulk.getCdUnitaOperativa());
		metadataProperties.put(OrdineAcqCMISService.CMIS_ORDINI_ACQ_DETTAGLIO_RIGA, ordineAcqRigaBulk.getRiga());
		metadataProperties.put("sigla_commons_aspect:utente_applicativo", ordineAcqRigaBulk.getUtuv());

		List<String> aspectsToAdd = Arrays.asList("P:cm:titled", "P:sigla_commons_aspect:utente_applicativo_sigla");
		metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

		String folderKey = createFolderIfNotPresent(path, folderName, metadataProperties);
		logger.info("   Cartella di dettaglio creata/esistente. Chiave (Key) CMIS: {}", folderKey);
		return folderKey;
	}

	/**
	 * Logica di costruzione del path per gli ordini con la nuova struttura.
	 */
	private String getStorePathNewPath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		logger.info("-> getStorePathNewPath - Applicazione logica path successiva al 2024.");
		try {
			// Path base: ComunicazioniDal/CdUnitaOrganizzativa/Ordini/CdUnitaOperativa/CdNumeratore/Esercizio
			String path =Arrays.asList(
					SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOrganizzativa(),
					"Ordini",
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOperativa(),
					allegatoParentBulk.getCdNumeratore(),
					Optional.ofNullable(allegatoParentBulk.getEsercizio())
							.map(String::valueOf)
							.orElse("0")
			).stream().collect(
					Collectors.joining(StorageDriver.SUFFIX)
			);

			logger.info("   Path base 'New Path' generato: {}", path);
			return createFolderOrdineIfNotPresent(path, allegatoParentBulk);
		} catch (ComponentException e) {
			logger.error("   ERRORE durante la creazione del 'New Path'", e);
			throw new BusinessProcessException(e);
		}
	}

	/**
	 * Logica di costruzione del path per gli ordini con la vecchia struttura (e verifica coerenza).
	 */
	private String getStorePathOldPath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		logger.info("-> getStorePathOldPath - Applicazione logica path precedente o uguale al 2023.");
		try {
			// Path base: ComunicazioniDal/CdUnitaOrganizzativa/Ordini/CdNumeratore/Esercizio
			String path =Arrays.asList(
					SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
					allegatoParentBulk.getUnitaOperativaOrd().getCdUnitaOrganizzativa(),
					"Ordini",
					allegatoParentBulk.getCdNumeratore(),
					Optional.ofNullable(allegatoParentBulk.getEsercizio())
							.map(String::valueOf)
							.orElse("0")
			).stream().collect(
					Collectors.joining(StorageDriver.SUFFIX)
			);
			logger.info("   Path base 'Old Path' generato: {}", path);

			String folderName = sanitizeFolderName(allegatoParentBulk.constructCMISNomeFile());
			String pathFolderOrdine = path.concat(path.equals(StorageDriver.SUFFIX) ? "" : StorageDriver.SUFFIX).concat(folderName);
			logger.debug("   Path completo Old Folder: {}", pathFolderOrdine);

			StorageObject folder = this.getStorageObjectByPath(pathFolderOrdine, true, false);

			if (folder == null) {
				logger.debug("   La vecchia cartella non esiste in: {}", pathFolderOrdine);
				return null; // Il vecchio path non è valido/esistente
			}

			logger.debug("   Vecchia cartella trovata. Key: {}", folder.getKey());
			List<StorageObject> children= Optional.ofNullable(folder).map(m->this.getChildren(m.getPath())).orElse(null);

			if (children == null || children.isEmpty()) {
				logger.debug("   La vecchia cartella è vuota. Non è un Old Path da consolidare.");
				return null;
			}

			// Verifica di coerenza del file di stampa
			StorageObject fileStampaOridne = children.stream()
					.filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_ORDINI))
					.findFirst().orElse(null);

			if (fileStampaOridne != null) {
				boolean bContainNomeFiles = fileStampaOridne.getKey().contains(allegatoParentBulk.recuperoIdOrdineAsString());
				logger.debug("   Trovato file di stampa. Key: {}. Coerenza ID: {}", fileStampaOridne.getKey(), bContainNomeFiles);
				if (!bContainNomeFiles) {
					logger.warn("   File di stampa non coerente con l'ID Ordine. Old Path invalidato.");
					return null;
				}
			}

			logger.info("   Old Path valido. Procedo con createFolderOrdineIfNotPresent.");
			return createFolderOrdineIfNotPresent(path, allegatoParentBulk);
		} catch (ComponentException e) {
			logger.error("   ERRORE durante la creazione/verifica del 'Old Path'", e);
			throw new BusinessProcessException(e);
		}
	}

	/**
	 * Metodo principale per determinare il path CMIS dell'ordine, gestendo la transizione (Old/New Path).
	 */
	public String getStorePath(OrdineAcqBulk allegatoParentBulk) throws BusinessProcessException{
		String path = null;
		Timestamp dateLimite = DateUtils.firstDateOfTheYear(2024);
		logger.info("-> getStorePath - Determina path per Ordine. Data Ordine: {}. Limite Old Path: {}", allegatoParentBulk.getDacr(), dateLimite.getTime());

		// La data di riferimento dell'ordine è precedente o uguale al 01/01/2024
		if (allegatoParentBulk.getDacr().compareTo(dateLimite)<=0) {
			logger.info("   La data Ordine ricade nella finestra 'Old Path'. Tentativo di recupero...");
			path = getStorePathOldPath(allegatoParentBulk);
			if (path != null) {
				logger.info("   Old Path valido e utilizzato. Key Folder: {}", path);
				return path;
			}
			logger.warn("   Old Path non valido/esistente. Esegue fallback su 'New Path'.");
		}

		path = getStorePathNewPath(allegatoParentBulk);
		logger.info("   New Path utilizzato. Key Folder: {}", path);
		return path;
	}

	/**
	 * Determina il path per la cartella di dettaglio di una riga d'ordine.
	 */
	public String getStorePathDettaglio(OrdineAcqRigaBulk ordineAcqRigaBulk) throws BusinessProcessException{
		logger.info("-> getStorePathDettaglio - Determina path per Dettaglio Riga: {}", ordineAcqRigaBulk.getRiga());
		try {
			String parentPath = getStorePath(ordineAcqRigaBulk.getOrdineAcq());
			logger.debug("   Path Folder padre (Ordine): {}", parentPath);

			// Chiama createFolderOrdineIfNotPresent usando il path del folder ordine come genitore
			return createFolderOrdineIfNotPresent(parentPath, ordineAcqRigaBulk);
		} catch (ComponentException e) {
			logger.error("   ERRORE durante il calcolo del path di dettaglio", e);
			throw new BusinessProcessException(e);
		}
	}

	/**
	 * Recupera l'oggetto CMIS che rappresenta il file di stampa dell'ordine.
	 */
	public StorageObject getStorageObjectStampaOrdine(OrdineAcqBulk ordine)throws Exception{
		logger.info("-> getStorageObjectStampaOrdine - Ricerca file di stampa con aspetto: {}", ASPECT_STAMPA_ORDINI);
		return getFilesOrdine(ordine).stream()
				.filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_ORDINI))
				.findFirst()
				.map(storageObject -> {
					logger.info("   File di stampa trovato. Key: {}", storageObject.getKey());
					return storageObject;
				})
				.orElseGet(() -> {
					logger.info("   File di stampa non trovato.");
					return null;
				});
	}

	/**
	 * Verifica se esiste un file di stampa dell'ordine con stato 'ALLA FIRMA' o 'DEFINITIVO'.
	 */
	public boolean alreadyExsistValidStampaOrdine(OrdineAcqBulk ordine) throws Exception{
		logger.info("-> alreadyExsistValidStampaOrdine - Verifica esistenza stampa valida.");
		StorageObject s = getStorageObjectStampaOrdine(ordine);

		if (s == null) {
			logger.debug("   Oggetto di stampa non esistente.");
			return false;
		}

		Optional<String> stato = Optional.ofNullable(s.<String>getPropertyValue("ordine_acq:stato"));
		if (stato.isPresent()) {
			String statoValue = stato.get();
			boolean isValid = statoValue.equalsIgnoreCase(OrdineAcqBulk.STATO.get(OrdineAcqBulk.STATO_ALLA_FIRMA).toString())
					|| statoValue.equalsIgnoreCase(OrdineAcqBulk.STATO.get(OrdineAcqBulk.STATO_DEFINITIVO).toString());
			logger.info("   Stato Ordine (CMIS): {}. Valido: {}", statoValue, isValid);
			return isValid;
		}
		logger.debug("   Proprietà 'ordine_acq:stato' non trovata sull'oggetto di stampa.");
		return false;
	}

	/**
	 * Ottiene lo stream di dati del file di stampa dell'ordine.
	 */
	public InputStream getStreamOrdine(OrdineAcqBulk ordine) throws Exception{
		logger.info("-> getStreamOrdine - Tentativo di recuperare l'InputStream del file di stampa.");
		return Optional.ofNullable(getStorageObjectStampaOrdine(ordine))
				.map(storageObject -> {
					logger.info("   Recupero risorsa (stream) per Key: {}", storageObject.getKey());
					return getResource(storageObject.getKey());
				})
				.orElseGet(() -> {
					logger.warn("   Oggetto di stampa non trovato. Ritorna null.");
					return null;
				});
	}

	/**
	 * Esegue la firma digitale del documento e aggiorna l'oggetto CMIS.
	 */
	public String signOrdine(SignP7M signP7M, String path) throws ApplicationException {
		logger.info("-> signOrdine - Inizio processo di firma. NodeRefSource: {}", signP7M.getNodeRefSource());
		StorageObject storageObject = storageDriver.getObject(signP7M.getNodeRefSource());
		StorageObject docFirmato = null;

		if (storageObject == null) {
			logger.error("   StorageObject di origine non trovato per la firma.");
			throw new ApplicationException("Documento da firmare non trovato.");
		}
		logger.debug("   Oggetto da firmare trovato. Key: {}", storageObject.getKey());

		try (InputStream resourceStream = getResource(storageObject);
			 ByteArrayInputStream signedStream = new ByteArrayInputStream(IOUtils.toByteArray(resourceStream))) {

			// 1. Firma con Aruba
			byte[] bytesSigned = arubaSignServiceClient.pdfsignatureV2(
					signP7M.getUsername(),
					signP7M.getPassword(),
					signP7M.getOtp(),
					IOUtils.toByteArray(signedStream));

			logger.info("   Firma digitale eseguita. Dimensione file firmato: {} bytes.", bytesSigned.length);

			// 2. Aggiornamento contenuto CMIS
			docFirmato = updateStream(storageObject.getKey(), new ByteArrayInputStream(bytesSigned), MimeTypes.PDF.mimetype());
			logger.info("   Contenuto CMIS aggiornato. Key: {}", docFirmato.getKey());

			// 3. Aggiunta Aspect di firma
			addAspect(storageObject, "P:cnr:signedDocument");
			logger.info("   Aspect di firma 'P:cnr:signedDocument' aggiunto all'oggetto.");

			logger.info("   Ordine {} firmato correttamente. Key finale: {}", signP7M.getNomeFile(), docFirmato.getKey());
			return docFirmato.getKey();
		} catch (ArubaSignServiceException _ex) {
			logger.error("   ERRORE durante la firma digitale con Aruba", _ex);
			throw new ApplicationException(FirmaOTPBulk.errorMessage(_ex.getMessage()));
		} catch (IOException e) {
			logger.error("   ERRORE IO durante la firma", e);
			throw new ApplicationException( e );
		}
	}

	/**
	 * Recupera la lista di allegati generici presenti nella cartella di dettaglio di una riga d'ordine.
	 */
	public BulkList<AllegatoGenericoBulk> recuperoAllegatiDettaglioOrdine(OrdineAcqRigaBulk ordineAcqRigaBulk) throws BusinessProcessException {
		logger.info("-> recuperoAllegatiDettaglioOrdine - Inizio recupero allegati per riga: {}", ordineAcqRigaBulk.getRiga());
		BulkList<AllegatoGenericoBulk> allegatoGenericoBulks = new BulkList<AllegatoGenericoBulk>();
		String path = getStorePathDettaglio(ordineAcqRigaBulk);
		logger.debug("   Path della cartella di dettaglio: {}", path);

		StorageObject folderDettaglio = getStorageObjectByPath(path);
		if (folderDettaglio == null) {
			logger.warn("   Cartella di dettaglio non esistente per il path. Ritorna lista vuota.");
			return allegatoGenericoBulks;
		}

		logger.debug("   Cartella trovata. Key: {}. Recupero figli...", folderDettaglio.getKey());
		for (StorageObject storageObject : getChildren(folderDettaglio.getKey())) {
			String objectKey = storageObject.getKey();
			String objectBaseType = storageObject.getPropertyValue(StoragePropertyNames.BASE_TYPE_ID.value());
			logger.debug("   Elaborazione Oggetto: Key={}, BaseType={}", objectKey, objectBaseType);

			if (hasAspect(storageObject, StoragePropertyNames.SYS_ARCHIVED.value())) {
				logger.debug("   Oggetto {} ignorato perché archiviato.", objectKey);
				continue;
			}

			if (Optional.ofNullable(objectBaseType)
					.map(String.class::cast)
					.filter(s -> s.equals(StoragePropertyNames.CMIS_FOLDER.value()))
					.isPresent()) {
				logger.debug("   Oggetto {} ignorato perché è una cartella CMIS.", objectKey);
				continue;
			}

			AllegatoOrdineDettaglioBulk allegato = new AllegatoOrdineDettaglioBulk(objectKey);
			allegato.setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
			allegato.setNome(storageObject.getPropertyValue(StoragePropertyNames.NAME.value()));
			allegato.setDescrizione(storageObject.getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));
			allegato.setTitolo(storageObject.getPropertyValue(StoragePropertyNames.TITLE.value()));

			allegato.setLastModificationDate(
					Optional.ofNullable(storageObject.<Calendar>getPropertyValue(StoragePropertyNames.LAST_MODIFIED.value()))
							.map(Calendar::getTime)
							.orElseGet(Date::new));

			String relativePath = Optional.ofNullable(storageObject.getPath())
					.map(s -> s.substring(s.indexOf(path) + path.length()))
					.map(s -> s.substring(0, s.lastIndexOf(StorageDriver.SUFFIX)))
					.orElse(StorageDriver.SUFFIX);

			allegato.setRelativePath(relativePath);
			allegato.setCrudStatus(OggettoBulk.NORMAL);
			allegatoGenericoBulks.add(allegato);
			logger.debug("   Allegato di dettaglio aggiunto. Key: {}, Nome: {}", objectKey, allegato.getNome());
		}
		logger.info("   Recupero allegati di dettaglio completato. Totale allegati: {}", allegatoGenericoBulks.size());
		return allegatoGenericoBulks;
	}

}