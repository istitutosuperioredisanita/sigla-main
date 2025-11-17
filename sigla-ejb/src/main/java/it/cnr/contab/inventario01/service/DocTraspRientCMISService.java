package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.service.OrdineAcqCMISService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.DateUtils;
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
import java.sql.Timestamp;
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

    public static final String DOC_TRASPORTO_RIENTRO = "Documenti Trasporto/Rientro";


    /**
     * Recupera tutti i file associati al documento
     */
    public List<StorageObject> getFilesDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws BusinessProcessException {
        logger.info("-> getFilesDoc - Inizio recupero files per documento: Esercizio={}, Inventario={}, Tipo={}, PgDoc={}",
                doc.getEsercizio(), doc.getPgInventario(), doc.getTiDocumento(), doc.getPgDocTrasportoRientro());

        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);

        StorageObject folderDoc = recuperoFolderDocSigla(doc, uo);

        if (Optional.ofNullable(folderDoc).isPresent()) {
            String folderKey = folderDoc.getKey();
            logger.info("   Folder trovata con key: {}", folderKey);
            List<StorageObject> children = getChildren(folderKey);
            logger.info("   Numero files trovati nella folder: {}", children.size());
            return children;
        }
        logger.warn("   Nessuna folder trovata per il documento. Ritorno lista vuota.");
        return Collections.EMPTY_LIST;
    }

    /**
     * Recupera documenti filtrati per tipo allegato
     */
    private List<StorageObject> getDocuments(String storageObjectKey, String tipoAllegato) {
        logger.debug("-> getDocuments - Ricerca documenti figli per Key: {} con tipo: {}", storageObjectKey, tipoAllegato);
        List<StorageObject> filtered = getChildren(storageObjectKey).stream()
                .filter(storageObject -> tipoAllegato == null ||
                        storageObject.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value())
                                .equals(tipoAllegato))
                .collect(Collectors.toList());
        logger.debug("   Trovati {} documenti filtrati.", filtered.size());
        return filtered;
    }

    /**
//     * Recupera il folder principale del documento
//     */
//    public StorageObject recuperoFolderDocSigla(Doc_trasporto_rientroBulk doc, UserContext userContext)
//            throws BusinessProcessException {
//        String path = getStorePath(doc, userContext);
//        logger.info("-> recuperoFolderDocSigla - Recupero StorageObject (folder) dal path: {}", path);
//        StorageObject folder = getStorageObjectByPath(path);
//        if (folder != null) {
//            logger.info("   Folder recuperata con successo - Key: {}", folder.getKey());
//        } else {
//            logger.warn("   Folder NON trovata al path: {}", path);
//        }
//        return folder;
//    }


    /**
     * Recupera l'oggetto CMIS (cartella) che rappresenta il Doc
     */
    public StorageObject recuperoFolderDocSigla(Doc_trasporto_rientroBulk doc, String uo_context) throws BusinessProcessException{
        String path = getStorePath(doc,uo_context);
        logger.info("-> Recupero StorageObject per il path: {}", path);
        StorageObject folder = getStorageObjectByPath(path);
        if (folder != null) {
            logger.info("   Folder recuperato con Key: {}", folder.getKey());
        } else {
            logger.warn("   Folder non trovato per il path: {}", path);
        }
        return folder;
    }

    /**
     * Estrae il path base (senza il nome della folder finale) dal path completo
     */
    private String getBasePathFromFullPath(String fullPath) {
        if (fullPath == null || !fullPath.contains(StorageDriver.SUFFIX)) {
            return fullPath;
        }

        int lastSeparatorIndex = fullPath.lastIndexOf(StorageDriver.SUFFIX);
        if (lastSeparatorIndex > 0) {
            return fullPath.substring(0, lastSeparatorIndex);
        }

        return fullPath;
    }

    /**
     * Crea la cartella principale del documento di trasporto/rientro se non esiste.
     * Restituisce la chiave (Key) CMIS della cartella.
     */
    public String createFolderDocIfNotPresent(String path, Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        logger.info("-> createFolderDocIfNotPresent (Doc_trasporto_rientroBulk) - Path padre: {}", path);

        Map<String, Object> metadataProperties = new HashMap<>();
        String folderName = sanitizeFolderName(doc.constructCMISNomeFile());
        logger.debug("   Nome della cartella da creare: {}", folderName);

        // TIPO FOLDER CMIS PER DOCUMENTI TRASPORTO/RIENTRO
        metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(),
                "F:doc_trasporto_rientro:main");
        metadataProperties.put(StoragePropertyNames.NAME.value(), folderName);

        // METADATI SPECIFICI DEL DOCUMENTO TRASPORTO/RIENTRO
        metadataProperties.put(CMIS_DOC_ESERCIZIO, doc.getEsercizio());
        metadataProperties.put(CMIS_DOC_PG_INVENTARIO, doc.getPgInventario());
        metadataProperties.put(CMIS_DOC_TI_DOCUMENTO, doc.getTiDocumento());
        metadataProperties.put(CMIS_DOC_PG_DOCUMENTO, doc.getPgDocTrasportoRientro());

        // Metadati opzionali
        if (doc.getDsDocTrasportoRientro() != null) {
            metadataProperties.put(CMIS_DOC_OGGETTO, doc.getDsDocTrasportoRientro());
        }

        // Utente applicativo
        metadataProperties.put("sigla_commons_aspect:utente_applicativo", doc.getUtuv());

        // ASPECTS CMIS
        List<String> aspectsToAdd = Arrays.asList(
                "P:cm:titled",
                "P:sigla_commons_aspect:utente_applicativo_sigla"
        );
        metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspectsToAdd);

        String folderKey = createFolderIfNotPresent(path, folderName, metadataProperties);
        logger.info("   Cartella principale Doc T/R creata/esistente. Chiave (Key) CMIS: {}", folderKey);

        return folderKey;
    }


    /**
     * Ottiene il path nuovo per il documento (per date >= 2024)
     */
    private String getStorePathNewPath(Doc_trasporto_rientroBulk doc, String uo_context)
            throws BusinessProcessException {

        logger.info("-> getStorePathNewPath - Applicazione logica path successiva al 2024");

        try {
            // Determina UO e tipo ritiro (codice esistente)
            String tipoRitiro = "";

            if (doc.isRitiroIncaricato()) {
                tipoRitiro = "INCARICATO";

            } else if (doc.isRitiroVettore()) {
                tipoRitiro = "VETTORE";
            }

            logger.info("   Tipo ritiro: {}. UO base: {}", tipoRitiro, uo_context);


            // Path BASE (SENZA la folder finale del documento)
            String basePath = Arrays.asList(
                    SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                    uo_context,
                    DOC_TRASPORTO_RIENTRO,
                    String.valueOf(doc.getEsercizio()),
                    "Inventario",
                    doc.getTiDocumento(),
                    String.valueOf(doc.getPgDocTrasportoRientro())
            ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));


            logger.info("   Path BASE generato: {}", basePath);

            // Crea TUTTA la gerarchia + folder documento
            String fullPath = createFolderDocIfNotPresent(basePath, doc);
            logger.info("   *** Path COMPLETO (con folder documento): {} ***", fullPath);

            return fullPath;

        } catch (ComponentException e) {
            logger.error("   ERRORE durante creazione del 'New Path'", e);
            throw new BusinessProcessException(e);
        }
    }

    /**
     * Ottiene il path vecchio per retrocompatibilità (date < 2024)
     * RESTITUISCE IL PATH COMPLETO DELLA CARTELLA DOCUMENTO (se esiste e valida)
     * Altrimenti NULL per triggherare il fallback su New Path
     */
    private String getStorePathOldPath(Doc_trasporto_rientroBulk doc, String uo_context)
            throws BusinessProcessException {

        logger.info("-> getStorePathOldPath - Applicazione logica path precedente o uguale al 2023 (aggiornata)");

        String tipoRitiro;

        // Determinazione UO
        if (doc.isRitiroIncaricato()) {
            tipoRitiro = "INCARICATO";
        } else if (doc.isRitiroVettore()) {
            tipoRitiro = "VETTORE";
        } else {
            throw new BusinessProcessException("Tipo ritiro non valido. Deve essere INCARICATO o VETTORE.");
        }

        logger.info("   Tipo ritiro: {}. UO base: {}", tipoRitiro, uo_context);

        // Path base Old Path (senza Documenti_Trasporto_Rientro)
        String basePath = Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                uo_context,
                "Inventario",
                doc.getTiDocumento(),
                Optional.ofNullable(doc.getEsercizio()).map(String::valueOf).orElse("0")
        ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));

        logger.info("   Path BASE 'Old Path' generato: {}", basePath);

        // Costruzione nome folder documento
        String folderName = sanitizeFolderName(doc.constructCMISNomeFile());
        String fullPathOldFolder = basePath
                .concat(basePath.endsWith(StorageDriver.SUFFIX) ? "" : StorageDriver.SUFFIX)
                .concat(folderName);

        logger.debug("   Path COMPLETO Old Folder: {}", fullPathOldFolder);

        // Verifica esistenza cartella vecchia
        StorageObject folder = this.getStorageObjectByPath(fullPathOldFolder, true, false);

        if (folder == null)
            return null;

        // Recupero figli per validare
        List<StorageObject> children = Optional.ofNullable(folder)
                .map(m -> this.getChildren(m.getKey()))
                .orElse(null);

        if (children == null || children.isEmpty())
            return null; // cartella vuota → non valida

        // Verifica coerenza file stampa
        StorageObject fileStampaDoc = children.stream()
                .filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_DOC))
                .findFirst()
                .orElse(null);

        if (fileStampaDoc != null) {
            String docId = doc.recuperoIdDocAsString();
            boolean isCoerente = fileStampaDoc.getKey().contains(docId);

            if (!isCoerente)
                return null; // incoerenza → Old Path non valido
        }

        // Old Path valido
        return fullPathOldFolder;
    }


    /**
     * Metodo principale per determinare il path CMIS del doc, gestendo la transizione (Old/New Path).
     */
    public String getStorePath(Doc_trasporto_rientroBulk allegatoParentBulk, String uo_context) throws BusinessProcessException{
        String path = null;
        Timestamp dateLimite = DateUtils.firstDateOfTheYear(2024);
        logger.info("-> getStorePath - Determina path per Doc. Data Doc: {}. Limite Old Path: {}", allegatoParentBulk.getDacr(), dateLimite.getTime());

        // La data di riferimento del doc è precedente o uguale al 01/01/2024
        if (allegatoParentBulk.getDacr().compareTo(dateLimite)<=0) {
            logger.info("   La data Doc ricade nella finestra 'Old Path'. Tentativo di recupero...");
            path = getStorePathOldPath(allegatoParentBulk,uo_context);
            if (path != null) {
                logger.info("   Old Path valido e utilizzato. Key Folder: {}", path);
                return path;
            }
            logger.warn("   Old Path non valido/esistente. Esegue fallback su 'New Path'.");
        }

        path = getStorePathNewPath(allegatoParentBulk,uo_context);
        logger.info("   New Path utilizzato. Key Folder: {}", path);
        return path;
    }

    /**
     * Recupera il file di stampa del documento
     */
    public StorageObject getStorageObjectStampaDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        logger.info("-> getStorageObjectStampaDoc - Ricerca file stampa documento con aspetto: {}", ASPECT_STAMPA_DOC);
        StorageObject stampa = getFilesDoc(doc, userContext).stream()
                .filter(storageObject -> hasAspect(storageObject, ASPECT_STAMPA_DOC))
                .findFirst()
                .orElse(null);
        if (stampa != null) {
            logger.info("   File stampa trovato - Key: {}, Nome: {}",
                    stampa.getKey(), stampa.getPropertyValue(StoragePropertyNames.NAME.value()));
        } else {
            logger.info("   Nessun file stampa trovato.");
        }
        return stampa;
    }

    /**
     * Verifica se esiste già una stampa valida del documento
     */
    public boolean alreadyExistValidStampaDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        logger.info("-> alreadyExistValidStampaDoc - Verifica esistenza stampa valida (Stato: {}).", STATO_STAMPA_DOC_VALIDO);
        boolean isValid = Optional.ofNullable(getStorageObjectStampaDoc(doc, userContext))
                .flatMap(storageObject -> Optional.ofNullable(
                        storageObject.<String>getPropertyValue("doc_trasporto_rientro:stato")))
                .filter(stato -> stato.equalsIgnoreCase(STATO_STAMPA_DOC_VALIDO))
                .isPresent();
        logger.info("   Esito verifica stampa valida: {}", isValid);
        return isValid;
    }

    /**
     * Ottiene lo stream del file di stampa
     */
    public InputStream getStreamDoc(Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws Exception {
        logger.info("-> getStreamDoc - Recupero stream file stampa documento");
        return Optional.ofNullable(getStorageObjectStampaDoc(doc, userContext))
                .map(storageObject -> {
                    logger.info("   Stream recuperato per file: {}", storageObject.getKey());
                    return getResource(storageObject.getKey());
                })
                .orElseGet(() -> {
                    logger.warn("   Nessuno stream disponibile - file stampa non trovato.");
                    return null;
                });
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

        logger.info("-> salvaStampaDocumentoFirmatoSuCMIS - *** INIZIO SALVATAGGIO DOCUMENTO FIRMATO ***");
        logger.debug("   Documento - Esercizio: {}, Inventario: {}, Tipo: {}, PgDoc: {}",
                documento.getEsercizio(), documento.getPgInventario(),
                documento.getTiDocumento(), documento.getPgDocTrasportoRientro());
        logger.info("   Dimensione PDF firmato: {} bytes", pdfFirmato.length);

        try {
            // Recupera lo StorageObject del file di stampa esistente
            StorageObject stampaEsistente = getStorageObjectStampaDoc(documento, userContext);

            if (stampaEsistente != null) {
                logger.info("   File stampa esistente trovato - Key: {}. Aggiornamento del contenuto.", stampaEsistente.getKey());

                // Sostituisce il PDF non firmato con quello firmato
                StorageObject updatedDoc = updateStream(
                        stampaEsistente.getKey(),
                        new ByteArrayInputStream(pdfFirmato),
                        "application/pdf");
                logger.info("   Stream del file aggiornato con successo. Key: {}", updatedDoc.getKey());

                // Aggiorna le proprietà
                Map<String, Object> properties = new HashMap<>();
                properties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                properties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                properties.put("doc_trasporto_rientro:data_firma", new Date());
                logger.debug("   Proprietà da aggiornare: Stato={}, Firmato=true, DataFirma={}", STATO_STAMPA_DOC_VALIDO, properties.get("doc_trasporto_rientro:data_firma"));

                updateProperties(properties, stampaEsistente);
                logger.info("   Proprietà del file aggiornate in CMIS.");

                StorageObject documentoAggiornato = getStorageObjectBykey(stampaEsistente.getKey());
                logger.info("   *** DOCUMENTO FIRMATO AGGIORNATO CON SUCCESSO - Key: {} ***",
                        documentoAggiornato.getKey());

                return documentoAggiornato;

            } else {
                logger.warn("   File stampa NON esistente. Inizio procedura di creazione nuovo file.");
                String uo = CNRUserContext.getCd_unita_organizzativa(userContext);

                String path = getStorePath(documento, uo);
                logger.info("   Path della cartella di destinazione: {}", path);

                StorageObject parentObject = getStorageObjectByPath(path, true, true);
                logger.info("   Parent object recuperato - Key: {}", parentObject.getKey());

                String nomeFile = costruisciNomeFileFirmato(documento);
                logger.info("   Nome file firmato da creare: {}", nomeFile);

                Map<String, Object> metadataProperties = new HashMap<>();
                metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(),
                        "D:doc_trasporto_rientro_attachment:document");
                metadataProperties.put(StoragePropertyNames.NAME.value(), nomeFile);
                metadataProperties.put("doc_trasporto_rientro:stato", STATO_STAMPA_DOC_VALIDO);
                metadataProperties.put("doc_trasporto_rientro:firmato", Boolean.TRUE);
                metadataProperties.put("doc_trasporto_rientro:data_firma", new Date());

                // Aggiungi gli aspect
                List<String> aspects = new ArrayList<>();
                aspects.add(ASPECT_STAMPA_DOC);
                aspects.add(ASPECT_STATO_STAMPA_DOC);
                metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), aspects);
                logger.debug("   Aspects aggiunti: {}", aspects);

                StorageObject nuovoFile = storageDriver.createDocument(
                        new ByteArrayInputStream(pdfFirmato),
                        "application/pdf",
                        metadataProperties,
                        parentObject,
                        path,
                        false
                );

                logger.info("   *** NUOVO DOCUMENTO FIRMATO CREATO CON SUCCESSO - Key: {} ***", nuovoFile.getKey());

                return nuovoFile;
            }

        } catch (Exception e) {
            logger.error("-> salvaStampaDocumentoFirmatoSuCMIS - *** ERRORE FATALE DURANTE SALVATAGGIO DOCUMENTO FIRMATO ***", e);
            logger.error("   Dettagli: Tipo errore: {}, Messaggio: {}",
                    e.getClass().getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Costruisce il nome del file firmato
     */
    private String costruisciNomeFileFirmato(Doc_trasporto_rientroBulk documento) {
        StringBuilder nome = new StringBuilder();
        String tipoDoc;

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(documento.getTiDocumento())) {
            tipoDoc = "Trasporto";
        } else if (Doc_trasporto_rientroBulk.RIENTRO.equals(documento.getTiDocumento())) {
            tipoDoc = "Rientro";
        } else {
            tipoDoc = "DocGenerico";
        }

        nome.append(tipoDoc);
        nome.append("_");
        nome.append(documento.getEsercizio());
        nome.append("_");
        nome.append(documento.getPgInventario());
        nome.append("_");
        nome.append(documento.getPgDocTrasportoRientro());
        nome.append("_firmato.pdf");

        logger.debug("-> costruisciNomeFileFirmato - Nome file generato: {}", nome.toString());
        return nome.toString();
    }
}