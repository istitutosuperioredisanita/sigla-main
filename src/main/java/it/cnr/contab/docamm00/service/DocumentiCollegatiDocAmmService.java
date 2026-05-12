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

package it.cnr.contab.docamm00.service;

import it.cnr.contab.docamm00.docs.bulk.AutofatturaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_attivaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoElettronicoBulk;
import it.cnr.contab.docamm00.fatturapa.bulk.DocumentoEleTestataBulk;
import it.cnr.contab.docamm00.fatturapa.bulk.DocumentoEleTrasmissioneBulk;
import it.cnr.contab.docamm00.storage.*;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaBulk;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaIBulk;
import it.cnr.contab.doccont00.service.DocumentiContabiliService;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.DetailedException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.GenerazioneReportException;
import it.cnr.si.spring.storage.StorageException;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.bulk.StorageFile;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentiCollegatiDocAmmService extends DocumentiContabiliService {

    private transient static final Logger logger = LoggerFactory.getLogger(DocumentiCollegatiDocAmmService.class);
    private final static String TIPO_ALLEGATO_NON_INVIATO_SDI = "allegati_non_inviati_sdi";
    private final static String TIPO_ALLEGATO_FATTURA_DOPO_PROTOCOLLO = "stampa_fattura_dopo_protocollo";
    private final static String TIPO_ALLEGATO_FATTURA_PRIMA_PROTOCOLLO = "stampa_fattura_prima_protocollo";
    private final static String FILE_FATTURA_XML_FIRMATO = "fattura_elettronica_xml_post_firma";
    private static final DateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private List<String> getNodeRefFatturaAttivaXmlFirmato(Fattura_attivaBulk fattura) throws DetailedException {
       // String folder = getFolderDocumentoAttivo(esercizio, cds, cdUo, pgFattura);
        String folder = getPathFolderFatturaAttiva(fattura);
        List<StorageObject> results = getDocuments(folder,StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_FATTURA_ELETTRONICA_XML_POST_FIRMA.value());
        if (results.size() == 0)
            return null;
        else {
            return results.stream()
                    .map(StorageObject::getKey)
                    .collect(Collectors.toList());
        }

    }
    public List<String> getNodeRefAllegatiDocumentoAttivo(Fattura_attivaBulk fattura) throws DetailedException {
        String folder = getPathFolderFatturaAttiva(fattura);
        List<StorageObject> results = getDocuments(folder, StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_ALLEGATI_NON_INVIATI_SDI.value());
        if (results.size() == 0) {
            return null;
        } else {
            return results.stream()
                    .map(StorageObject::getKey)
                    .collect(Collectors.toList());
        }
    }

    private List<StorageObject> getChildrenByPath(String path){
        return Optional.ofNullable(this.getStorageObjectByPath( path,true,false)).map(so->this.getChildren(so.getKey())).orElse(Collections.emptyList());
    }

    private List<StorageObject> getDocuments(List<StorageObject>  childrens, String tipoAllegato){
        return childrens.stream().filter(storageObject -> hasAspect(storageObject,tipoAllegato)).collect(Collectors.toList());
    }

    private List<StorageObject> getDocuments(StorageObject  soFolder, String tipoAllegato){
        return Optional.ofNullable(soFolder).map(s->getDocuments(s.getKey(),tipoAllegato)).orElse(Collections.emptyList());
    }

    private List<StorageObject> getDocuments(String folder, String tipoAllegato)  {
        List<StorageObject>  children= getChildrenByPath(folder);
        return getDocuments(children,tipoAllegato);
    }

    public StorageObject recuperoFolderFattura(Fattura_attivaBulk fattura)  {
       return  this.getStorageObjectByPath( getPathFolderFatturaAttiva(fattura),true,true);
    }

    public StorageObject recuperoFolderAutofattura(AutofatturaBulk autoFattura) throws DetailedException {
        return  this.getStorageObjectByPath( getPathFolderAutofattura(autoFattura),true,true);
    }

    private InputStream getStream(List<String> ids)
            throws ApplicationException, IOException {
        if (ids != null) {
            if (ids.size() == 1) {
                try {
                    return getResource(ids.get(0));
                } catch (StorageException _ex) {
                }
            } else {
                PDFMergerUtility ut = new PDFMergerUtility();
                ut.setDestinationStream(new ByteArrayOutputStream());
                try {
                    for (String id : ids) {
                        ut.addSource(getResource(id));
                    }
                    ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                    return new ByteArrayInputStream(((ByteArrayOutputStream) ut.getDestinationStream()).toByteArray());
                } catch (IOException e) {
                    throw e;
                } catch (StorageException _ex) {
                }
            }
        }
        return null;
    }

    public List<String> getNodeRefDocumentoAttivo(IDocumentoAmministrativoElettronicoBulk docammElettronico) throws DetailedException {
        List<StorageObject> children;
        if (docammElettronico instanceof Fattura_attivaBulk)
            children = getChildrenByPath(getPathFolderFatturaAttiva((Fattura_attivaBulk)docammElettronico));
        else
            children = getChildrenByPath(getPathFolderAutofattura((AutofatturaBulk) docammElettronico));

        List<StorageObject> results = getDocuments(children,StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_STAMPA_FATTURA_PRIMA_PROTOCOLLO.value());
        if (results.size() == 0) {
            results = getDocuments(children,StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_STAMPA_FATTURA_DOPO_PROTOCOLLO.value());
            if (results.size() == 0) {
                return null;
            } else {
                return results.stream()
                        .map(StorageObject::getKey)
                        .collect(Collectors.toList());
            }
        } else {
            return results.stream()
                    .map(StorageObject::getKey)
                    .collect(Collectors.toList());
        }
    }

    private String getPathFolderFatturaAttiva(Fattura_attivaBulk fattura){
        StorageFolderFatturaAttiva storageFolderFatturaAttiva= new StorageFolderFatturaAttiva(fattura);
        return storageFolderFatturaAttiva.getCMISPath();
    }
    private String getPathFolderAutofattura(AutofatturaBulk autofattura){
        StorageFolderAutofattura storageFolderAutofattura= new StorageFolderAutofattura(autofattura);
        return storageFolderAutofattura.getCMISPath();
    }
    public InputStream getStreamDocumentoAttivo(IDocumentoAmministrativoElettronicoBulk docammElettronico) throws Exception {
        List<String> ids = getNodeRefDocumentoAttivo(docammElettronico);
        return getStream(ids);
    }

    public InputStream getStreamAllegatiDocumentoAttivo(Fattura_attivaBulk fattura) throws Exception {
        List<String> ids = getNodeRefAllegatiDocumentoAttivo(fattura);
        return getStream(ids);
    }

    public InputStream getStreamAllegatiDocumento(Fattura_attivaBulk fattura) throws Exception {

        return getStreamAllegatiDocumentoAttivo(fattura);
    }

    private void archiviaFileCMIS(UserContext userContext, DocumentiCollegatiDocAmmService documentiCollegatiDocAmmService,
                                  IDocumentoAmministrativoElettronicoBulk docammElettronico, File file) throws ComponentException {
        List<StorageFile> storageFileCreate = new ArrayList<StorageFile>();
        List<StorageFile> storageFileAnnullati = new ArrayList<StorageFile>();
        try {
            StorageFile storageFile;
            StorageObject folder;
            if (docammElettronico instanceof Fattura_attivaBulk) {
                storageFile = new StorageFileFatturaAttiva(file, (Fattura_attivaBulk) docammElettronico, "application/pdf", "FAPP" + (((Fattura_attivaBulk) docammElettronico)).constructCMISNomeFile() + ".pdf");
                folder = recuperoFolderFattura((Fattura_attivaBulk) docammElettronico);
            } else {
                storageFile = new StorageFileAutofattura(file, (AutofatturaBulk) docammElettronico, "application/pdf", "FAPP" + (((AutofatturaBulk) docammElettronico)).constructCMISNomeFile() + ".pdf");
                folder = recuperoFolderAutofattura((AutofatturaBulk) docammElettronico);
            }

            String path = storageFile.getStorageParentPath();

            try {
                Optional.ofNullable(documentiCollegatiDocAmmService.restoreSimpleDocument(
                        storageFile,
                        storageFile.getInputStream(),
                        storageFile.getContentType(),
                        storageFile.getFileName(),
                        path,
                        true
                )).ifPresent(storageObject -> {
                    storageFile.setStorageObject(storageObject);
                    storageFileCreate.add(storageFile);
                    List<String> aspects = storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value());
                    aspects.add(StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_STAMPA_FATTURA_PRIMA_PROTOCOLLO.value());

                    if (docammElettronico instanceof Fattura_attivaBulk)
                        documentiCollegatiDocAmmService.updateProperties(((StorageFileFatturaAttiva)storageFile).getCMISFolder((Fattura_attivaBulk)docammElettronico), folder);
                    else
                        documentiCollegatiDocAmmService.updateProperties(((StorageFileAutofattura)storageFile).getCMISFolder((AutofatturaBulk)docammElettronico), folder);

                    documentiCollegatiDocAmmService.updateProperties(
                            Collections.singletonMap(
                                    StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(),
                                    aspects),
                            storageObject);
                    //storageFile.setStorageObject(storageObject);
                    //storageFileCreate.add(storageFile);
                });
            } catch (StorageException _ex) {
                if (_ex.getType().equals(StorageException.Type.CONSTRAINT_VIOLATED))
                    throw new ApplicationException("CMIS - File [" + storageFile.getFileName() + "] già presente o non completo di tutte le proprietà obbligatorie. Inserimento non possibile!");
                throw new ApplicationException("CMIS - Errore nella registrazione del file XML sul Documentale (" + _ex.getMessage() + ")");
            }
        } catch (Exception e) {
            //Codice per riallineare il documentale allo stato precedente rispetto alle modifiche
            for (StorageFile storageFile : storageFileCreate)
                documentiCollegatiDocAmmService.delete(storageFile.getStorageObject());
            for (StorageFile storageFile : storageFileAnnullati) {
                String cmisFileName = storageFile.getFileName();
                String cmisFileEstensione = cmisFileName.substring(cmisFileName.lastIndexOf(".") + 1);
                String stringToDelete = cmisFileName.substring(cmisFileName.indexOf("-ANNULLATO"));
                storageFile.setFileName(cmisFileName.replace(stringToDelete, "." + cmisFileEstensione));
                documentiCollegatiDocAmmService.updateProperties(storageFile, storageFile.getStorageObject());
            }
            throw new ApplicationException(e.getMessage());
        }
    }

    public File gestioneAllegatiPerFatturazioneElettronica(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException {
        if (docammElettronico.isDocumentoFatturazioneElettronica()) {
            DocumentiCollegatiDocAmmService documentiCollegatiDocAmmService = SpringUtil.getBean("documentiCollegatiDocAmmService", DocumentiCollegatiDocAmmService.class);
            File file = lanciaStampaFatturaElettronica(userContext, docammElettronico);
            archiviaFileCMIS(userContext, documentiCollegatiDocAmmService, docammElettronico, file);
            return file;
        }
        return null;
    }

    public File lanciaStampaFatturaElettronica(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException {
        try {
            String nomeProgrammaStampa = docammElettronico instanceof AutofatturaBulk ? "autofattura_provvisoria.jasper":"fattura_attiva_provvisoria.jasper";
            String nomeFileStampaFattura = getOutputFileNameFatturazioneElettronica(nomeProgrammaStampa, docammElettronico);
            File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + nomeFileStampaFattura);
            Print_spoolerBulk print = new Print_spoolerBulk();
            print.setFlEmail(false);
            print.setReport("/docamm/docamm/" + nomeProgrammaStampa);
            print.setNomeFile(nomeFileStampaFattura);
            print.setUtcr(userContext.getUser());
            print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
            print.addParam("esercizio", docammElettronico.getEsercizio(), Integer.class);
            print.addParam("cd_uo_origine", docammElettronico.getCd_uo_origine(), String.class);
            if (docammElettronico instanceof AutofatturaBulk)
                print.addParam("pg_autofattura", docammElettronico.getPg_docamm(), Long.class);
            else
                print.addParam("pg_fattura", docammElettronico.getPg_docamm(), Long.class);

            Report report = SpringUtil.getBean("printService", PrintService.class).executeReport(userContext, print);

            FileOutputStream f = new FileOutputStream(output);
            f.write(report.getBytes());
            return output;
        } catch (IOException e) {
            throw new GenerazioneReportException("Generazione Stampa non riuscita", e);
        }
    }

    private String getOutputFileNameFatturazioneElettronica(String reportName, IDocumentoAmministrativoElettronicoBulk docammElettronico) {
        String fileName = preparaFileNamePerStampa(reportName);
        fileName = PDF_DATE_FORMAT.format(new java.util.Date()) + '_' + docammElettronico.recuperoIdFatturaAsString() + '_' + fileName;
        return fileName;
    }

    private String preparaFileNamePerStampa(String reportName) {
        String fileName = reportName;
        fileName = fileName.replace('/', '_');
        fileName = fileName.replace('\\', '_');
        if (fileName.startsWith("_"))
            fileName = fileName.substring(1);
        if (fileName.endsWith(".jasper"))
            fileName = fileName.substring(0, fileName.length() - 7);
        fileName = fileName + ".pdf";
        return fileName;
    }

    public List<AllegatoGenericoBulk> getAllegatiDocumentiAmministrativi(Mandato_rigaBulk mandato_rigaBulk) {
        return Optional.ofNullable(mandato_rigaBulk)
                .filter(Mandato_rigaIBulk.class::isInstance)
                .map(Mandato_rigaIBulk.class::cast)
                .map(mandato_rigaIBulk -> {
                    try {
                        return Optional.ofNullable(Utility.createMandatoComponentSession().getDocumentoAmministrativoSpesaBulk(null, mandato_rigaIBulk))
                                .filter(AllegatoStorePath.class::isInstance)
                                .map(AllegatoStorePath.class::cast)
                                .map(allegatoStorePath -> {
                                    return Optional.ofNullable(allegatoStorePath.getStorePath())
                                            .filter(storePaths -> !storePaths.isEmpty())
                                            .map(storePaths ->
                                                    storePaths.stream()
                                                            .map(storePath -> Optional.ofNullable(getStorageObjectByPath(storePath))
                                                                    .map(StorageObject::getKey)
                                                                    .map(key -> getChildren(key, -1))
                                                                    .map(storageObjects -> {
                                                                        final Supplier<Stream<StorageObject>> supplierStorageObjects = () -> storageObjects.stream();
                                                                        final Optional<StorageObject> xmlFattura = supplierStorageObjects.get()
                                                                                .filter(storageObject ->
                                                                                        storageObject.<String>getPropertyValue(StoragePropertyNames.OBJECT_TYPE_ID.value())
                                                                                                .equals("D:sigla_fatture_attachment:document"))
                                                                                .filter(storageObject ->
                                                                                        storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value())
                                                                                                .contains("P:sigla_fatture_attachment:trasmissione_fattura")
                                                                                ).findAny();
                                                                        xmlFattura
                                                                                .map(storageObject -> storageObject.<String>getPropertyValue(StoragePropertyNames.NAME.value()))
                                                                                .ifPresent(s -> {
                                                                                    if (supplierStorageObjects.get()
                                                                                            .noneMatch(storageObject ->
                                                                                                    storageObject.<String>getPropertyValue(StoragePropertyNames.NAME.value())
                                                                                                            .equals(s.substring(0, s.lastIndexOf(".")).concat(".html")))) {
                                                                                        Optional.ofNullable(caricaFatturaPDF(allegatoStorePath, storePath, xmlFattura.get()))
                                                                                                .ifPresent(storageObject -> storageObjects.add(storageObject));
                                                                                    }
                                                                                });
                                                                        return storageObjects;
                                                                    })
                                                                    .orElseGet(() -> Collections.emptyList()))
                                                            .collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList())
                                            ).orElseGet(() -> Collections.emptyList());
                                }).map(list -> list.stream()
                                        .filter(storageObject -> !Optional.ofNullable(storageObject.getPropertyValue(StoragePropertyNames.BASE_TYPE_ID.value()))
                                                .map(String.class::cast)
                                                .filter(s -> s.equals(StoragePropertyNames.CMIS_FOLDER.value()))
                                                .isPresent())
                                        .map(storageObject -> {
                                            AllegatoGenericoBulk allegato = new AllegatoGenericoBulk(storageObject.getKey());
                                            allegato.setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
                                            allegato.setNome(storageObject.getPropertyValue(StoragePropertyNames.NAME.value()));
                                            allegato.setDescrizione(storageObject.getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));
                                            allegato.setTitolo(storageObject.getPropertyValue(StoragePropertyNames.TITLE.value()));
                                            return allegato;
                                        }).collect(Collectors.toCollection(ArrayList<AllegatoGenericoBulk>::new)))
                                .orElseGet(() -> {
                                    return new ArrayList<AllegatoGenericoBulk>();
                                });
                    } catch (ComponentException | RemoteException e) {
                        return new ArrayList<AllegatoGenericoBulk>();
                    }
                }).orElseGet(() -> new ArrayList<AllegatoGenericoBulk>());
    }

    private StorageObject caricaFatturaPDF(AllegatoStorePath allegatoStorePath, String storePath, StorageObject storageObject) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final String formatoTrasmissione = Optional.ofNullable(allegatoStorePath)
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .map(Fattura_passivaBulk::getDocumentoEleTestata)
                    .map(DocumentoEleTestataBulk::getDocumentoEleTrasmissione)
                    .map(DocumentoEleTrasmissioneBulk::getFormatoTrasmissione)
                    .orElse("FPA12");
            Source xslDoc = null;
            if (formatoTrasmissione.equals("FPA12")) {
                xslDoc = new StreamSource(this.getClass().getResourceAsStream("/it/cnr/contab/docamm00/bp/fatturapa_v1.2.1.xsl"));
            } else if (formatoTrasmissione.equals("SDI11")) {
                xslDoc = new StreamSource(this.getClass().getResourceAsStream("/it/cnr/contab/docamm00/bp/fatturapa_v1.1.xsl"));
            } else {
                throw new ApplicationException("Il formato trasmissione indicato da SDI non rientra tra i formati attesi");
            }
            Source xmlDoc = new StreamSource(getResource(storageObject.getKey()));
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trasform = factory.newTransformer(xslDoc);
            trasform.transform(xmlDoc, new StreamResult(baos));
            try {
                String name = storageObject.<String>getPropertyValue(StoragePropertyNames.NAME.value());
                Map<String, Object> metadataProperties = new HashMap<String, Object>();
                metadataProperties.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "D:sigla_fatture_attachment:document");
                metadataProperties.put(StoragePropertyNames.NAME.value(), name.substring(0, name.lastIndexOf(".")).concat(".html"));
                metadataProperties.put(StoragePropertyNames.TITLE.value(), "Fattura stampabile");
                metadataProperties.put(StoragePropertyNames.DESCRIPTION.value(), "Fattura stampabile");
                metadataProperties.put(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), Arrays.asList("P:sigla_commons_aspect:utente_applicativo_sigla", "P:cm:titled"));
                metadataProperties.put("sigla_commons_aspect:utente_applicativo", "SDI");
                return storeSimpleDocument(new ByteArrayInputStream(baos.toByteArray()), "text/html", storePath, metadataProperties);
            } catch (StorageException _ex) {
                logger.error("Cannot convert to html document with id {}", storageObject.getKey(), _ex);
                return null;
            }
        } catch (Exception e) {
            logger.error("Cannot convert to html document with id {}", storageObject.getKey(), e);
            return null;
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                logger.error("Cannot convert to html document with id {}", storageObject.getKey(), e);
            }
        }
    }

    public StorageObject recuperoFolderDocammElettronicoByPath(IDocumentoAmministrativoElettronicoBulk docammElettronico) {
        if (docammElettronico instanceof Fattura_attivaBulk)
            return this.recuperoFolderFatturaByPath((Fattura_attivaBulk) docammElettronico);
        else
            return this.recuperoFolderAutofatturaByPath((AutofatturaBulk) docammElettronico);
    }

    public StorageObject recuperoFolderFatturaByPath(Fattura_attivaBulk fattura) {
    	StorageFolderFatturaAttiva folder = new StorageFolderFatturaAttiva(fattura);
    	String path = folder.getCMISPathForSearch();
    	return SpringUtil.getBean("storeService", StoreService.class).getStorageObjectByPath(path);
    }

    public StorageObject recuperoFolderAutofatturaByPath(AutofatturaBulk autofattura) {
        StorageFolderAutofattura folder = new StorageFolderAutofattura(autofattura);
        String path = folder.getCMISPathForSearch();
        return SpringUtil.getBean("storeService", StoreService.class).getStorageObjectByPath(path);
    }

    protected List<StorageObject> recuperoDocumento(StorageObject node, String tipoDocumento) {
        return Optional.ofNullable(node)
                .map(StorageObject::getKey)
                .map(key -> getChildren(key))
                .map(lista -> lista.stream()
                        .filter(storageObj -> storageObj.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()).contains(tipoDocumento))
                        .collect(Collectors.toList())).orElse(new ArrayList<StorageObject>());
    }

    private List<StorageObject> getStorageObjectDocammElettronico(IDocumentoAmministrativoElettronicoBulk docammElettronico, String tipoDocumento) throws DetailedException {
    	StorageObject node = null;
        if (docammElettronico instanceof Fattura_attivaBulk)
            node = recuperoFolderFatturaByPath((Fattura_attivaBulk)docammElettronico);
        else if (docammElettronico instanceof AutofatturaBulk)
            node = recuperoFolderAutofatturaByPath((AutofatturaBulk)docammElettronico);

        List<StorageObject> results = getDocuments(node, tipoDocumento);
        if (results.size() == 0)
            return Collections.EMPTY_LIST;
        else {
            return results;
        }
    }

    private List<String> getDocumentoFatturaAttiva(Fattura_attivaBulk fattura, String tipoDocumento) throws DetailedException {
    	StorageObject node = recuperoFolderFatturaByPath(fattura);
    	
        List<StorageObject> results = getDocuments(node, tipoDocumento);
        if (results.size() == 0)
            return null;
        else {
            return results.stream()
                    .map(StorageObject::getKey)
                    .collect(Collectors.toList());
        }
    }
    public InputStream getStreamXmlFirmatoFatturaAttiva(Fattura_attivaBulk fattura)  throws ApplicationException, IOException {
    	try {
        	List<String> ids = getDocumentoFatturaAttiva(fattura, StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_FATTURA_ELETTRONICA_XML_POST_FIRMA.value());
            return getStream(ids);
		} catch (DetailedException e) {
			throw new ApplicationException(e);
		}
    }

    public InputStream getStreamXmlFatturaAttiva(Fattura_attivaBulk fattura)  throws ApplicationException, IOException {
    	try {
        	List<String> ids = getDocumentoFatturaAttiva(fattura, StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_FATTURA_ELETTRONICA_XML_ANTE_FIRMA.value());
            return getStream(ids);
		} catch (DetailedException e) {
			throw new ApplicationException(e);
		}
    }
    public StorageObject getFileFirmatoFatturaAttiva(Fattura_attivaBulk fattura)  throws ApplicationException, IOException {
    	try {
        	List<StorageObject> lista = getStorageObjectDocammElettronico(fattura, StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_FATTURA_ELETTRONICA_XML_POST_FIRMA.value());
            if (lista.size() == 0)
                throw new ApplicationException("Non esiste il file firmato per la fattura attiva");
            else if (lista.size() > 1) {
                throw new ApplicationException("Esistono due file firmati per la fattura attiva");
            } else {	
            	return lista.get(0);
            }
		} catch (DetailedException e) {
			throw new ApplicationException(e);
		}
    }
    public StorageObject getFileXmlDocammElettronico(IDocumentoAmministrativoElettronicoBulk docammElettronico)  throws ApplicationException, IOException {
    	try {
        	List<StorageObject> lista = getStorageObjectDocammElettronico(docammElettronico, StorageDocAmmAspect.SIGLA_FATTURE_ATTACHMENT_FATTURA_ELETTRONICA_XML_ANTE_FIRMA.value());
            if (lista.size() == 0)
                throw new ApplicationException("Non esiste il file per il documento elettronico "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm());
            else if (lista.size() > 1) {
                throw new ApplicationException("Esistono due file per il documento elettronico "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm());
            } else {	
            	return lista.get(0);
            }
		} catch (DetailedException e) {
			throw new ApplicationException(e);
		}
    }
}
