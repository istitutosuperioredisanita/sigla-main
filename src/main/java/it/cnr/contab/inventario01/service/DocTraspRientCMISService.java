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
package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
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
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servizio CMIS per la gestione degli allegati dei documenti di trasporto/rientro inventario.
 *
 * <p>Gestisce il salvataggio, il recupero e l'aggiornamento dei PDF firmati sullo storage
 * documentale (Alfresco/CMIS).</p>
 *
 * <p>Non ha dipendenze da {@code happysign-client}.</p>
 */
public class DocTraspRientCMISService extends StoreService {

    private static final String ROOT = "Documenti Trasporto/Rientro";

    private static final String STATO_VALIDO = "VAL";

    private static final String ASPECT_STATO_STAMPA =
            "P:doc_trasporto_rientro_attachment:stato_stampa";

    private static final String PROP_STATO =
            "doc_trasporto_rientro:stato";

    private static final String PROP_FIRMATO =
            "doc_trasporto_rientro:firmato";

    private static final String PROP_DATA_FIRMA =
            "doc_trasporto_rientro:data_firma";

    @Autowired
    private StorageDriver storageDriver;

    /**
     * Restituisce tutti i file presenti nella cartella CMIS del documento.
     *
     * @param doc documento di trasporto/rientro
     * @param userContext contesto utente corrente
     * @return lista di {@link StorageObject}; mai {@code null}
     */
    public List<StorageObject> getFilesDoc(
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws BusinessProcessException {

        StorageObject folder = getStorageObjectByPath(
                getStorePath(doc, CNRUserContext.getCd_unita_organizzativa(userContext))
        );

        return folder == null
                ? Collections.<StorageObject>emptyList()
                : getChildren(folder.getKey());
    }

    /**
     * Restituisce il documento di stampa firmato se presente, altrimenti {@code null}.
     */
    public StorageObject getStorageObjectStampaDoc(
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws Exception {

        final String aspect = aspectFirmato(doc);

        return getFilesDoc(doc, userContext)
                .stream()
                .filter(so -> hasAspect(so, aspect))
                .findFirst()
                .orElse(null);
    }

    /**
     * Controlla se esiste già una stampa valida del documento.
     *
     * @return {@code true} se il documento firmato esiste ed è in stato valido
     */
    public boolean alreadyExistValidStampaDoc(
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws Exception {

        StorageObject stampa = getStorageObjectStampaDoc(doc, userContext);

        return stampa != null
                && STATO_VALIDO.equalsIgnoreCase(
                stampa.<String>getPropertyValue(PROP_STATO));
    }

    /**
     * Restituisce lo stream del documento firmato, o {@code null} se assente.
     */
    public InputStream getStreamDoc(
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws Exception {

        StorageObject stampa = getStorageObjectStampaDoc(doc, userContext);
        return stampa == null ? null : getResource(stampa.getKey());
    }

    /**
     * Legge il contenuto binario di uno {@link StorageObject}.
     *
     * @return byte[] del documento, o {@code null} se {@code storageObject} è nullo
     */
    public byte[] getBytes(StorageObject storageObject) throws Exception {

        if (storageObject == null) {
            return null;
        }

        try (InputStream inputStream = getResource(storageObject.getKey())) {
            return inputStream == null ? null : IOUtils.toByteArray(inputStream);
        }
    }

    /**
     * Salva o aggiorna il PDF firmato su CMIS.
     *
     * <p>Se esiste già aggiorna, altrimenti crea un nuovo documento con metadati.</p>
     */
    public StorageObject salvaStampaDocumentoFirmatoSuCMIS(
            byte[] pdfFirmato,
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws Exception {

        if (pdfFirmato == null || pdfFirmato.length == 0) {
            throw new IllegalArgumentException("PDF firmato non presente");
        }

        StorageObject esistente = getStorageObjectStampaDoc(doc, userContext);

        if (esistente != null) {
            updateStream(
                    esistente.getKey(),
                    new ByteArrayInputStream(pdfFirmato),
                    "application/pdf"
            );

            updateProperties(propsFirma(), esistente);

            return getStorageObjectBykey(esistente.getKey());
        }

        return creaDocumentoFirmato(pdfFirmato, doc, userContext);
    }

    /**
     * Calcola il path CMIS del documento.
     *
     * <p>Gestisce retrocompatibilità tra struttura storage vecchia e nuova.</p>
     */
    public String getStorePath(
            Doc_trasporto_rientroBulk doc,
            String uoContext)
            throws BusinessProcessException {

        Timestamp limiteOldPath = DateUtils.firstDateOfTheYear(2024);

        if (doc.getDacr() != null && doc.getDacr().compareTo(limiteOldPath) <= 0) {
            String oldPath = getStorePathOld(doc, uoContext);
            if (oldPath != null) {
                return oldPath;
            }
        }

        return getStorePathNew(doc, uoContext);
    }

    private String getStorePathNew(
            Doc_trasporto_rientroBulk doc,
            String uoContext)
            throws BusinessProcessException {

        try {
            String basePath = join(
                    SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                    uoContext,
                    ROOT,
                    String.valueOf(doc.getEsercizio()),
                    "Inventario",
                    doc.getTiDocumento(),
                    String.valueOf(doc.getPgDocTrasportoRientro())
            );

            return createFolderDocIfNotPresent(basePath, doc);

        } catch (ComponentException e) {
            throw new BusinessProcessException(e);
        }
    }

    private String getStorePathOld(
            Doc_trasporto_rientroBulk doc,
            String uoContext)
            throws BusinessProcessException {

        if (!doc.isRitiroIncaricato() && !doc.isRitiroVettore()) {
            throw new BusinessProcessException(
                    "Tipo ritiro non valido: deve essere INCARICATO o VETTORE."
            );
        }

        String basePath = join(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                uoContext,
                "Inventario",
                doc.getTiDocumento(),
                String.valueOf(doc.getEsercizio())
        );

        String oldPath = basePath
                + (basePath.endsWith(StorageDriver.SUFFIX) ? "" : StorageDriver.SUFFIX)
                + sanitizeFolderName(doc.constructCMISNomeFile());

        StorageObject folder = getStorageObjectByPath(oldPath, true, false);

        if (folder == null) {
            return null;
        }

        List<StorageObject> children = getChildren(folder.getKey());

        if (children == null || children.isEmpty()) {
            return null;
        }

        StorageObject firmato = children.stream()
                .filter(so -> hasAspect(so, aspectFirmato(doc)))
                .findFirst()
                .orElse(null);

        if (firmato != null
                && !firmato.getKey().contains(doc.recuperoIdDocAsString())) {
            return null;
        }

        return oldPath;
    }

    private String createFolderDocIfNotPresent(
            String path,
            Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        String folderName = sanitizeFolderName(doc.constructCMISNomeFile());

        Map<String, Object> props = new HashMap<String, Object>();

        props.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), "F:doc_trasporto_rientro:main");
        props.put(StoragePropertyNames.NAME.value(), folderName);
        props.put("doc_trasporto_rientro:esercizio", doc.getEsercizio());
        props.put("doc_trasporto_rientro:pg_inventario", doc.getPgInventario());
        props.put("doc_trasporto_rientro:ti_documento", doc.getTiDocumento());
        props.put(
                "doc_trasporto_rientro:pg_doc_trasporto_rientro",
                doc.getPgDocTrasportoRientro()
        );

        if (doc.getDsDocTrasportoRientro() != null) {
            props.put("doc_trasporto_rientro:oggetto", doc.getDsDocTrasportoRientro());
        }

        props.put("sigla_commons_aspect:utente_applicativo", doc.getUtuv());

        props.put(
                StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(),
                Arrays.asList(
                        "P:cm:titled",
                        "P:sigla_commons_aspect:utente_applicativo_sigla"
                )
        );

        return createFolderIfNotPresent(path, folderName, props);
    }

    /**
     * Crea il documento firmato su CMIS.
     *
     * <p>Inizializza metadati, tipo documento e collega alla cartella corretta.</p>
     */
    private StorageObject creaDocumentoFirmato(
            byte[] pdfFirmato,
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws Exception {

        if (doc == null) {
            throw new IllegalArgumentException("Documento T/R non valorizzato");
        }

        if (pdfFirmato == null || pdfFirmato.length == 0) {
            throw new IllegalArgumentException("PDF firmato non presente o vuoto");
        }

        if (storageDriver == null) {
            throw new IllegalStateException("StorageDriver non inizializzato");
        }

        String uoContext = CNRUserContext.getCd_unita_organizzativa(userContext);
        if (uoContext == null || uoContext.trim().isEmpty()) {
            throw new IllegalArgumentException("Unità organizzativa non valorizzata nel UserContext");
        }

        String path = getStorePath(doc, uoContext);

        StorageObject parent = getStorageObjectByPath(path, true, true);

        if (parent == null) {
            throw new ApplicationException(
                    "Cartella CMIS non trovata o non creata per il documento T/R. Path=" + path
            );
        }

        return storageDriver.createDocument(
                new ByteArrayInputStream(pdfFirmato),
                "application/pdf",
                propsNuovoDocumentoFirmato(doc, userContext),
                parent,
                path,
                false
        );
    }

    private Map<String, Object> propsNuovoDocumentoFirmato(
            Doc_trasporto_rientroBulk doc,
            UserContext userContext) {

        Map<String, Object> props = propsFirma();

        props.put(StoragePropertyNames.OBJECT_TYPE_ID.value(), objectType(doc));
        props.put(StoragePropertyNames.NAME.value(), nomeFileFirmato(doc));

        props.put(
                StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(),
                Arrays.asList(
                        "P:cm:titled",
                        "P:sigla_commons_aspect:utente_applicativo_sigla",
                        aspectFirmato(doc),
                        ASPECT_STATO_STAMPA
                )
        );

        props.put("cm:title", nomeFileFirmato(doc));
        props.put("cm:description", "Documento Trasporto/Rientro firmato digitalmente");

        props.put(
                "sigla_commons_aspect:utente_applicativo",
                CNRUserContext.getUser(userContext)
        );

        return props;
    }

    private Map<String, Object> propsFirma() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(PROP_STATO, STATO_VALIDO);
        props.put(PROP_FIRMATO, Boolean.TRUE);
        props.put(PROP_DATA_FIRMA, new Date());
        return props;
    }

    private static String aspectFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento T/R non valorizzato");
        }

        if (doc.isTrasporto()) {
            return AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        }

        if (doc.isRientro()) {
            return AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
        }

        throw new IllegalArgumentException(
                "Tipo documento T/R non riconosciuto: " + doc.getTiDocumento()
        );
    }

    private static String objectType(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Documento T/R non valorizzato");
        }

        if (doc.isTrasporto()) {
            return "D:sigla_doctrasporto_attachment:document";
        }

        if (doc.isRientro()) {
            return "D:sigla_docrientro_attachment:document";
        }

        throw new IllegalArgumentException(
                "Tipo documento T/R non riconosciuto: " + doc.getTiDocumento()
        );
    }

    private static String nomeFileFirmato(Doc_trasporto_rientroBulk doc) {
        return (doc.isTrasporto() ? "Trasporto" : "Rientro")
                + "_"
                + doc.getEsercizio()
                + "_"
                + doc.getPgInventario()
                + "_"
                + doc.getPgDocTrasportoRientro()
                + "_firmato.pdf";
    }

    private static String join(String... values) {
        return Arrays.stream(values)
                .collect(Collectors.joining(StorageDriver.SUFFIX));
    }
}
