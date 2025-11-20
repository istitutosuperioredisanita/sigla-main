package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocTraspRientCMISService extends StoreService {

    private transient static final Logger logger = LoggerFactory.getLogger(DocTraspRientCMISService.class);

    // ========================================
    // ASPECTS CMIS - TESTATA (UNICA GESTIONE)
    // ========================================
    public static final String ASPECT_ALLEGATI_DOC = "P:doc_trasporto_rientro_attachment:allegati";
    public static final String ASPECT_STAMPA_DOC = "P:doc_trasporto_rientro_attachment:stampa";
    public static final String ASPECT_STATO_STAMPA_DOC = "P:doc_trasporto_rientro_attachment:stato_stampa";
    public static final String ASPECT_DDT = "P:doc_trasporto_rientro_attachment:ddt";
    public static final String ASPECT_VERBALE_CONSEGNA = "P:doc_trasporto_rientro_attachment:verbale_consegna";
    public static final String ASPECT_AUTORIZZAZIONE = "P:doc_trasporto_rientro_attachment:autorizzazione";

    // Metadati CMIS
    public static final String CMIS_DOC_ESERCIZIO = "doc_trasporto_rientro:esercizio";
    public static final String CMIS_DOC_PG_INVENTARIO = "doc_trasporto_rientro:pg_inventario";
    public static final String CMIS_DOC_TI_DOCUMENTO = "doc_trasporto_rientro:ti_documento";
    public static final String CMIS_DOC_PG_DOCUMENTO = "doc_trasporto_rientro:pg_doc_trasporto_rientro";
    public static final String CMIS_DOC_OGGETTO = "doc_trasporto_rientro:oggetto";

    // Stati stampa
    public static final String STATO_STAMPA_DOC_VALIDO = "VAL";
    public static final String STATO_STAMPA_DOC_ANNULLATO = "ANN";

    @Autowired
    private StorageDriver storageDriver;

    public static final String DOC_TRASPORTO_RIENTRO = "Documenti Trasporto - Rientro";
    public static final String DOC_TRASPORTO_FILEFOLDER = "Doc. Trasporto";
    public static final String DOC_RIENTRO_FILEFOLDER = "Doc. Rientro";



    /**
     * Recupera tutti i file associati al documento
     */
    public List<StorageObject> getFilesDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);
        StorageObject folderDoc = recuperoFolderDocSigla(doc, uo);

        if (Optional.ofNullable(folderDoc).isPresent()) {
            return getChildren(folderDoc.getKey());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Recupera l'oggetto CMIS (cartella) che rappresenta il Doc
     */
    public StorageObject recuperoFolderDocSigla(Doc_trasporto_rientroBulk doc, String uo_context) throws BusinessProcessException {
        String path = getStorePath(doc);
        StorageObject folder = getStorageObjectByPath(path, true);
        if (folder == null) {
            logger.warn("Cartella CMIS NON trovata per documento: {} al path: {}", doc.recuperoIdDocAsString(), path);
        }
        return folder;
    }

    /**
     * Metodo principale per determinare il path CMIS del doc.
     * Struttura:
     * Comunicazioni da ISS / Documenti di Trasporto e Rientro / 2025 / Doc. Trasporto / 20250000000007
     * Comunicazioni da ISS / Documenti di Trasporto e Rientro / 2025 / Doc. Rientro / 20250000000002
     */
    public String getStorePath(Doc_trasporto_rientroBulk doc)
            throws BusinessProcessException {

        String tipoFolderName = "";      // "Doc. Trasporto" o "Doc. Rientro"
        String numeroDocumento = "";     // "20250000000007"

        // Determina il nome della cartella tipo
        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())) {
            tipoFolderName = DOC_TRASPORTO_FILEFOLDER; // "Doc. Trasporto"
        } else if (Doc_trasporto_rientroBulk.RIENTRO.equals(doc.getTiDocumento())) {
            tipoFolderName = DOC_RIENTRO_FILEFOLDER;   // "Doc. Rientro"
        }

        // Crea il numero documento formattato: esercizio + progressivo
        numeroDocumento = doc.getEsercizio().toString() +
                Utility.lpad(doc.getPgDocTrasportoRientro().toString(), 10, '0');

        // Costruisce path completo:
        // Comunicazioni da ISS/Documenti di Trasporto e Rientro/2025/Doc. Trasporto/20250000000007
        String path = Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                DOC_TRASPORTO_RIENTRO,  // "Documenti di Trasporto e Rientro"
                Optional.ofNullable(doc.getEsercizio())
                        .map(esercizio -> String.valueOf(esercizio))
                        .orElse("0"),
                tipoFolderName,         // "Doc. Trasporto" o "Doc. Rientro"
                numeroDocumento         // "20250000000007"
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        );

        return path;
    }


    public String createFolderDocIfNotPresent(String path, Doc_trasporto_rientroBulk doc) throws ApplicationException {
        Map<String, Object> metadataProperties = new HashMap<String, Object>();
        String folderName = sanitizeFolderName(doc.constructCMISNomeFile());

        metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:doc_trasporto:main");
        metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);

        List<String> aspectsToAdd = Arrays.asList("P:cm:titled", "P:sigla_commons_aspect:utente_applicativo_sigla");
        metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

        String folderKey = createFolderIfNotPresent(path, folderName, metadataProperties);
        logger.info("Folder CMIS creata/esistente per documento {}. Key: {}", doc.recuperoIdDocAsString(), folderKey);
        return folderKey;
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
     * Ottiene lo stream del file di stampa
     */
    public InputStream getStreamDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        return Optional.ofNullable(getStorageObjectStampaDoc(doc, userContext))
                .map(storageObject -> getResource(storageObject.getKey()))
                .orElse(null);
    }

    /**
     * Salva il documento firmato da HappySign su CMIS/Azure
     */
    public StorageObject salvaStampaDocumentoFirmatoSuCMIS(
            byte[] pdfFirmato,
            Doc_trasporto_rientroBulk documento,
            UserContext userContext) throws Exception {

        try {
            StorageObject stampaEsistente = getStorageObjectStampaDoc(documento, userContext);

            if (stampaEsistente != null) {
                // Aggiornamento
                StorageObject updatedDoc = updateStream(
                        stampaEsistente.getKey(),
                        new ByteArrayInputStream(pdfFirmato),
                        "application/pdf");

                Map<String, Object> properties = new HashMap<>();
                properties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                properties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                properties.put("doc_trasporto_rientro:data_firma", new Date());

                updateProperties(properties, stampaEsistente);

                StorageObject documentoAggiornato = getStorageObjectBykey(stampaEsistente.getKey());
                logger.info("DOCUMENTO FIRMATO AGGIORNATO (UPDATE) con successo. Key: {}", documentoAggiornato.getKey());
                return documentoAggiornato;

            } else {
                // Creazione
                String path = getStorePath(documento);
                StorageObject parentObject = getStorageObjectByPath(path, true, true);

                if (parentObject == null) {
                    throw new ApplicationException("Cartella padre CMIS non trovata: " + path);
                }

                String nomeFile = costruisciNomeFileFirmato(documento);

                Map<String, Object> metadataProperties = new HashMap<>();
                metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(),
                        "D:doc_trasporto_rientro_attachment:document");
                metadataProperties.put(StoragePropertyNames.NAME.value(), nomeFile);
                metadataProperties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                metadataProperties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                metadataProperties.put("doc_trasporto_rientro:data_firma", new Date());

                List<String> aspects = new ArrayList<>();
                aspects.add(ASPECT_STAMPA_DOC);
                aspects.add(ASPECT_STATO_STAMPA_DOC);
                metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspects);

                StorageObject nuovoFile = storageDriver.createDocument(
                        new ByteArrayInputStream(pdfFirmato),
                        "application/pdf",
                        metadataProperties,
                        parentObject,
                        path,
                        false
                );
                logger.info("DOCUMENTO FIRMATO CREATO (NEW) con successo. Key: {}", nuovoFile.getKey());
                return nuovoFile;
            }

        } catch (Exception e) {
            logger.error("ERRORE FATALE durante il salvataggio del documento firmato per: {}", documento.recuperoIdDocAsString(), e);
            throw e;
        }
    }

    /**
     * Costruisce il nome del file firmato
     */
    private String costruisciNomeFileFirmato(Doc_trasporto_rientroBulk documento) {
        StringBuilder nome = new StringBuilder();
        String tipoDoc = "";

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(documento.getTiDocumento())) {
            tipoDoc = "Trasporto";
        } else if (Doc_trasporto_rientroBulk.RIENTRO.equals(documento.getTiDocumento())) {
            tipoDoc = "Rientro";
        }

        nome.append(tipoDoc);
        nome.append("_");
        nome.append(documento.getEsercizio());
        nome.append("_");
        nome.append(documento.getPgInventario());
        nome.append("_");
        nome.append(documento.getPgDocTrasportoRientro());
        nome.append("_firmato.pdf");

        return nome.toString();
    }

    /**
     * Salva il file di stampa su CMIS con aspect corretti.
     */
    public void salvaFileStampaSuCMIS(
            byte[] pdfContent,
            String nomeFile,
            Doc_trasporto_rientroBulk documento,
            UserContext userContext) throws Exception {

        try {
            StorageObject esistente = getStorageObjectStampaDoc(documento, userContext);
            if (esistente != null) {
                logger.warn("File stampa già esistente (Key: {}). Salvataggio saltato.", esistente.getKey());
                return;
            }

            String path = getStorePath(documento);
            StorageObject parentFolder = getStorageObjectByPath(path, true, true);

            if (parentFolder == null) {
                logger.error("Cartella padre CMIS non trovata al path: {}", path);
                throw new ApplicationException("Cartella non trovata: " + path);
            }

            Map<String, Object> metadataProperties = new HashMap<>();
            metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(),
                    "D:doc_trasporto_rientro_attachment:document");
            metadataProperties.put(StoragePropertyNames.NAME.value(), nomeFile);
            metadataProperties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
            metadataProperties.put("doc_trasporto_rientro:firmato", Boolean.FALSE);
            metadataProperties.put("doc_trasporto_rientro:data_creazione", new Date());

            List<String> aspects = new ArrayList<>();
            aspects.add(ASPECT_STAMPA_DOC);
            aspects.add(ASPECT_STATO_STAMPA_DOC);
            aspects.add("P:cm:titled");
            aspects.add("P:sigla_commons_aspect:utente_applicativo_sigla");
            metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspects);

            StorageObject nuovoFile = storageDriver.createDocument(
                    new ByteArrayInputStream(pdfContent),
                    "application/pdf",
                    metadataProperties,
                    parentFolder,
                    path,
                    false
            );

            logger.info("FILE STAMPA CREATO con successo. Key: {}", nuovoFile.getKey());

        } catch (Exception e) {
            logger.error("ERRORE durante il salvataggio del file di stampa per: {}", documento.recuperoIdDocAsString(), e);
            throw e;
        }
    }
}