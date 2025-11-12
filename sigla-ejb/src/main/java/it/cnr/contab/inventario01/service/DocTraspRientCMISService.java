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

package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario01.bulk.AllegatoDocTraspRientDettaglioBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.DateUtils;
import it.cnr.si.firmadigitale.firma.arss.ArubaSignServiceClient;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocTraspRientCMISService extends StoreService {

    private transient static final Logger logger = LoggerFactory.getLogger(DocTraspRientCMISService.class);

    // Aspect per stampa documento
    public static final String ASPECT_STAMPA_DOC = "P:doc_trasporto_rientro_attachment:stampa";
    public static final String ASPECT_STATO_STAMPA_DOC = "P:doc_trasporto_rientro_attachment:stato_stampa";

    // Aspect per allegati
    public static final String ASPECT_DOC_DETTAGLIO = "P:doc_trasporto_rientro_attachment:allegati_dettaglio";
    public static final String ASPECT_ALLEGATI_DOC = "P:doc_trasporto_rientro_attachment:allegati";

    // Metadati CMIS
    public static final String CMIS_DOC_ESERCIZIO = "doc_trasporto_rientro:esercizio";
    public static final String CMIS_DOC_PG_INVENTARIO = "doc_trasporto_rientro:pg_inventario";
    public static final String CMIS_DOC_TI_DOCUMENTO = "doc_trasporto_rientro:ti_documento";
    public static final String CMIS_DOC_PG_DOCUMENTO = "doc_trasporto_rientro:pg_doc_trasporto_rientro";
    public static final String CMIS_DOC_OGGETTO = "doc_trasporto_rientro:oggetto";
    public static final String CMIS_DOC_DETTAGLIO_PROGRESSIVO = "doc_trasporto_rientro_dettaglio:progressivo";

    // Stati stampa
    public static final String STATO_STAMPA_DOC_VALIDO = "VAL";
    public static final String STATO_STAMPA_DOC_ANNULLATO = "ANN";

    @Autowired
    private StorageDriver storageDriver;

    @Autowired
    private ArubaSignServiceClient arubaSignServiceClient;

    /**
     * Recupera tutti i file associati al documento
     */
    public List<StorageObject> getFilesDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        if (Optional.ofNullable(recuperoFolderDocSigla(doc, userContext)).isPresent())
            return getChildren(recuperoFolderDocSigla(doc, userContext).getKey());
        return Collections.EMPTY_LIST;
    }

    /**
     * Recupera documenti filtrati per tipo allegato
     */
    private List<StorageObject> getDocuments(String storageObjectKey, String tipoAllegato)
            throws ApplicationException {
        return getChildren(storageObjectKey).stream()
                .filter(storageObject -> tipoAllegato == null ||
                        storageObject.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value())
                                .equals(tipoAllegato))
                .collect(Collectors.toList());
    }

    /**
     * Recupera il folder principale del documento
     */
    public StorageObject recuperoFolderDocSigla(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        return getStorageObjectByPath(getStorePath(doc, userContext));
    }

    /**
     * Crea folder per il documento se non presente
     */
    public String createFolderDocIfNotPresent(String path, Doc_trasporto_rientroBulk doc)
            throws ApplicationException {
        Map<String, Object> metadataProperties = new HashMap<>();
        String folderName = sanitizeFolderName(doc.constructCMISNomeFile());

        metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:doc_trasporto_rientro:main");
        metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
        metadataProperties.put(CMIS_DOC_ESERCIZIO, doc.getEsercizio());
        metadataProperties.put(CMIS_DOC_PG_INVENTARIO, doc.getPgInventario());
        metadataProperties.put(CMIS_DOC_TI_DOCUMENTO, doc.getTiDocumento());
        metadataProperties.put(CMIS_DOC_PG_DOCUMENTO, doc.getPgDocTrasportoRientro());
        metadataProperties.put("sigla_commons_aspect:utente_applicativo", doc.getUtuv());

        List<String> aspectsToAdd = new ArrayList<>();
        aspectsToAdd.add("P:cm:titled");
        aspectsToAdd.add("P:sigla_commons_aspect:utente_applicativo_sigla");
        metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

        return createFolderIfNotPresent(path, folderName, metadataProperties);
    }

    /**
     * Crea folder per il dettaglio documento se non presente
     */
    public String createFolderDocDettaglioIfNotPresent(String path, Doc_trasporto_rientro_dettBulk dettaglio)
            throws ApplicationException {
        Map<String, Object> metadataProperties = new HashMap<>();
        String folderName = sanitizeFolderName(dettaglio.constructCMISNomeFile());

        metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:doc_trasporto_rientro_dettaglio:main");
        metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);
        metadataProperties.put(CMIS_DOC_ESERCIZIO, dettaglio.getEsercizio());
        metadataProperties.put(CMIS_DOC_PG_INVENTARIO, dettaglio.getPgInventario());
        metadataProperties.put(CMIS_DOC_TI_DOCUMENTO, dettaglio.getTiDocumento());
        metadataProperties.put(CMIS_DOC_PG_DOCUMENTO, dettaglio.getPgDocTrasportoRientro());
        metadataProperties.put(CMIS_DOC_DETTAGLIO_PROGRESSIVO, dettaglio.getProgressivo());
        metadataProperties.put("sigla_commons_aspect:utente_applicativo", dettaglio.getUtuv());

        List<String> aspectsToAdd = new ArrayList<>();
        aspectsToAdd.add("P:cm:titled");
        aspectsToAdd.add("P:sigla_commons_aspect:utente_applicativo_sigla");
        metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

        return createFolderIfNotPresent(path, folderName, metadataProperties);
    }

    /**
     * Ottiene il path nuovo per il documento (per date >= 2024)
     */
    private String getStorePathNewPath(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        try {
            // Determina l'unità organizzativa in base al tipo ritiro
            String cdUnitaOrganizzativa;

            if (doc.isRitiroIncaricato()) {
                // Se ritiro INCARICATO, prende la UO dall'anagrafico del dipendente incaricato
                TerzoBulk anagDipRitiro = doc.getAnagDipRitiro();
                if (anagDipRitiro == null) {
                    throw new BusinessProcessException(
                            "Per il ritiro tramite INCARICATO è necessario che sia valorizzato il Dipendente Incaricato.");
                }

                AnagraficoBulk anagrafico = anagDipRitiro.getAnagrafico();
                if (anagrafico == null || anagrafico.getCd_unita_organizzativa() == null) {
                    throw new BusinessProcessException(
                            "L'anagrafica del dipendente incaricato non ha un'unità organizzativa associata.");
                }

                cdUnitaOrganizzativa = anagrafico.getCd_unita_organizzativa();

            } else if (doc.isRitiroVettore()) {
                // Se ritiro VETTORE, prende la UO dal contesto utente
                cdUnitaOrganizzativa = CNRUserContext.getCd_unita_organizzativa(userContext);

                if (cdUnitaOrganizzativa == null) {
                    throw new BusinessProcessException(
                            "Impossibile determinare l'unità organizzativa dal contesto utente.");
                }

            } else {
                throw new BusinessProcessException(
                        "Tipo ritiro non valido. Deve essere INCARICATO o VETTORE.");
            }

            String path = Arrays.asList(
                    SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                    cdUnitaOrganizzativa,
                    "Inventario",
                    "Documenti_Trasporto_Rientro",
                    doc.getTiDocumento(),
                    Optional.ofNullable(doc.getEsercizio())
                            .map(String::valueOf)
                            .orElse("0")
            ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));

            return createFolderDocIfNotPresent(path, doc);
        } catch (ComponentException e) {
            throw new BusinessProcessException(e);
        }
    }

    /**
     * Ottiene il path vecchio per retrocompatibilità (date < 2024)
     */
    private String getStorePathOldPath(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        try {
            // Determina l'unità organizzativa in base al tipo ritiro
            String cdUnitaOrganizzativa;

            if (doc.isRitiroIncaricato()) {
                TerzoBulk anagDipRitiro = doc.getAnagDipRitiro();
                if (anagDipRitiro == null) {
                    throw new BusinessProcessException(
                            "Per il ritiro tramite INCARICATO è necessario che sia valorizzato il Dipendente Incaricato.");
                }

                AnagraficoBulk anagrafico = anagDipRitiro.getAnagrafico();
                if (anagrafico == null || anagrafico.getCd_unita_organizzativa() == null) {
                    throw new BusinessProcessException(
                            "L'anagrafica del dipendente incaricato non ha un'unità organizzativa associata.");
                }

                cdUnitaOrganizzativa = anagrafico.getCd_unita_organizzativa();

            } else if (doc.isRitiroVettore()) {
                cdUnitaOrganizzativa = CNRUserContext.getCd_unita_organizzativa(userContext);

                if (cdUnitaOrganizzativa == null) {
                    throw new BusinessProcessException(
                            "Impossibile determinare l'unità organizzativa dal contesto utente.");
                }

            } else {
                throw new BusinessProcessException(
                        "Tipo ritiro non valido. Deve essere INCARICATO o VETTORE.");
            }

            String path = Arrays.asList(
                    SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                    cdUnitaOrganizzativa,
                    "Inventario",
                    doc.getTiDocumento(),
                    Optional.ofNullable(doc.getEsercizio())
                            .map(String::valueOf)
                            .orElse("0")
            ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));

            String pathFolderDoc = path.concat(path.equals("/") ? "" : "/")
                    .concat(sanitizeFolderName(doc.constructCMISNomeFile()));
            StorageObject folder = this.getStorageObjectByPath(pathFolderDoc, true, false);
            List<StorageObject> children = Optional.ofNullable(folder)
                    .map(m -> this.getChildren(m.getPath()))
                    .orElse(null);

            if (children != null && !children.isEmpty()) {
                StorageObject fileStampaDoc = children.stream()
                        .filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_DOC))
                        .findFirst()
                        .orElse(null);

                boolean bContainNomeFiles = true;
                if (fileStampaDoc != null) {
                    bContainNomeFiles = fileStampaDoc.getKey().contains(doc.recuperoIdDocAsString());
                    if (!bContainNomeFiles)
                        return null;
                }
                return createFolderDocIfNotPresent(path, doc);
            }
            return null;
        } catch (ComponentException e) {
            throw new BusinessProcessException(e);
        }
    }

    /**
     * Ottiene il path del documento con gestione retrocompatibilità
     */
    public String getStorePath(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        String path = null;
        if (doc.getDacr().compareTo(DateUtils.firstDateOfTheYear(2024)) <= 0) {
            path = getStorePathOldPath(doc, userContext);
            if (path != null)
                return path;
        }
        return getStorePathNewPath(doc, userContext);
    }

    /**
     * Ottiene il path per il dettaglio documento
     */
    public String getStorePathDettaglio(Doc_trasporto_rientro_dettBulk dettaglio, UserContext userContext)
            throws BusinessProcessException {
        try {
            return createFolderDocDettaglioIfNotPresent(
                    getStorePath(dettaglio.getDoc_trasporto_rientro(), userContext),
                    dettaglio
            );
        } catch (ComponentException e) {
            throw new BusinessProcessException(e);
        }
    }

    /**
     * Recupera il file di stampa del documento
     */
    public StorageObject getStorageObjectStampaDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        return getFilesDoc(doc, userContext).stream()
                .filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_DOC))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se esiste già una stampa valida del documento
     */
    public boolean alreadyExistValidStampaDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        return Optional.ofNullable(getStorageObjectStampaDoc(doc, userContext))
                .flatMap(storageObject -> Optional.ofNullable(
                        storageObject.<String>getPropertyValue("doc_trasporto_rientro:stato")))
                .filter(stato -> stato.equalsIgnoreCase(STATO_STAMPA_DOC_VALIDO))
                .isPresent();
    }

    /**
     * Ottiene lo stream del file di stampa
     */
    public InputStream getStreamDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        return Optional.ofNullable(getStorageObjectStampaDoc(doc, userContext))
                .map(storageObject -> getResource(storageObject.getKey()))
                .orElse(null);
    }

    /**
     * Recupera tutti gli allegati del dettaglio documento
     */
    public BulkList<AllegatoGenericoBulk> recuperoAllegatiDettaglioDoc(
            Doc_trasporto_rientro_dettBulk dettaglio, UserContext userContext)
            throws BusinessProcessException {
        BulkList<AllegatoGenericoBulk> allegatoGenericoBulks = new BulkList<>();
        String path = getStorePathDettaglio(dettaglio, userContext);

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

            AllegatoDocTraspRientDettaglioBulk allegato =
                    new AllegatoDocTraspRientDettaglioBulk(storageObject.getKey());
            allegato.setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
            allegato.setNome(storageObject.getPropertyValue(StoragePropertyNames.NAME.value()));
            allegato.setDescrizione(storageObject.getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));
            allegato.setTitolo(storageObject.getPropertyValue(StoragePropertyNames.TITLE.value()));
            allegato.setLastModificationDate(
                    Optional.ofNullable(storageObject.<Calendar>getPropertyValue(StoragePropertyNames.LAST_MODIFIED.value()))
                            .map(Calendar::getTime)
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

    /**
     * Salva il documento firmato da HappySign su CMIS/Azure
     * Sostituisce il contenuto del file di stampa esistente con quello firmato
     *
     * @param pdfFirmato contenuto del PDF firmato
     * @param documento il documento di trasporto/rientro
     * @param userContext il contesto utente (CNRUserContext)
     * @return lo StorageObject del documento salvato
     */
    public StorageObject salvaStampaDocumentoFirmatoSuCMIS(
            byte[] pdfFirmato,
            Doc_trasporto_rientroBulk documento,
            UserContext userContext) throws Exception {

        try {
            // Usa il userContext passato come parametro

            // Recupera lo StorageObject del file di stampa esistente
            StorageObject stampaEsistente = getStorageObjectStampaDoc(documento, userContext);

            if (stampaEsistente != null) {
                // Aggiorna il contenuto del file esistente con quello firmato
                updateStream(
                        stampaEsistente.getKey(),
                        new java.io.ByteArrayInputStream(pdfFirmato),
                        "application/pdf");

                // Aggiorna le proprietà
                Map<String, Object> properties = new HashMap<>();
                properties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                properties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                properties.put("doc_trasporto_rientro:data_firma", new Date());

                updateProperties(properties, stampaEsistente);

                logger.info("Documento firmato aggiornato su CMIS - Key: {}", stampaEsistente.getKey());

                return getStorageObjectBykey(stampaEsistente.getKey());

            } else {
                // Se non esiste, crea un nuovo file
                String path = getStorePath(documento, userContext);
                StorageObject parentObject = getStorageObjectByPath(path, true, true);

                Map<String, Object> metadataProperties = new HashMap<>();
                metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(),
                        "D:doc_trasporto_rientro_attachment:document");
                metadataProperties.put(StoragePropertyNames.NAME.value(),
                        costruisciNomeFileFirmato(documento));
                metadataProperties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                metadataProperties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                metadataProperties.put("doc_trasporto_rientro:data_firma", new Date());

                // Aggiungi gli aspect
                List<String> aspects = new ArrayList<>();
                aspects.add(ASPECT_STAMPA_DOC);
                aspects.add(ASPECT_STATO_STAMPA_DOC);
                metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspects);

                StorageObject nuovoFile = storageDriver.createDocument(
                        new java.io.ByteArrayInputStream(pdfFirmato),
                        "application/pdf",
                        metadataProperties,
                        parentObject,
                        path,
                        false
                );

                logger.info("Documento firmato creato su CMIS - Key: {}", nuovoFile.getKey());

                return nuovoFile;
            }

        } catch (Exception e) {
            logger.error("Errore durante il salvataggio del documento firmato su CMIS", e);
            throw e;
        }
    }

    /**
     * Costruisce il nome del file firmato
     */
    private String costruisciNomeFileFirmato(Doc_trasporto_rientroBulk documento) {
        StringBuilder nome = new StringBuilder();

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(documento.getTiDocumento())) {
            nome.append("Trasporto");
        } else {
            nome.append("Rientro");
        }

        nome.append("_");
        nome.append(documento.getEsercizio());
        nome.append("_");
        nome.append(documento.getPgInventario());
        nome.append("_");
        nome.append(documento.getPgDocTrasportoRientro());
        nome.append("_firmato.pdf");

        return nome.toString();
    }

}