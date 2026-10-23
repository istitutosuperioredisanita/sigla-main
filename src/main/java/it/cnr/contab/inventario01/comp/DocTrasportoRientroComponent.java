package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaHome;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRHome;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.service.DocTraspRientFirmaTestService;
import it.cnr.contab.inventario01.util.DocTraspRientUtil;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDDetailComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.EmptyRemoteIterator;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.SendMail;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageException;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import it.iss.si.dto.anagrafica.Contatto;
import it.iss.si.dto.anagrafica.EmployeeDetails;
import it.iss.si.service.AceService;
import it.iss.si.signature.flow.model.SignatureDocument;
import it.iss.si.signature.flow.model.SignatureRequest;
import it.iss.si.signature.flow.service.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.*;

/**
 * Documenti di Trasporto e Rientro: CRUD, stati (INSERITO → INVIATO → DEFINITIVO → ANNULLATO),
 * validazioni, riferimenti Trasporto↔Rientro, stato beni, allegati e firma digitale.
 */
public class DocTrasportoRientroComponent extends CRUDDetailComponent
        implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DocTrasportoRientroComponent.class);

    @Autowired
    private StoreService storeService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private AceService aceService;

    public DocTrasportoRientroComponent() {
    }

    // ---------------------------------------------------------------- INIZIALIZZAZIONE

    public void initializeKeysAndOptionsInto(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        try {
            Tipo_trasporto_rientroHome tipoHome =
                    (Tipo_trasporto_rientroHome) getHome(usercontext, Tipo_trasporto_rientroBulk.class);
            Collection tipi = tipoHome.findTipoMovimenti(
                    ((Doc_trasporto_rientroBulk) oggettobulk).getTiDocumento());
            ((Doc_trasporto_rientroBulk) oggettobulk).setTipoMovimenti(tipi);
        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
        super.initializeKeysAndOptionsInto(usercontext, oggettobulk);
    }

    /** Nuovo documento con valori di default. */
    @Override
    public OggettoBulk inizializzaBulkPerInserimento(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            doc = (Doc_trasporto_rientroBulk) super.inizializzaBulkPerInserimento(aUC, doc);

            if (doc.getTiDocumento() == null || doc.getTiDocumento().isEmpty()) {
                throw new ComponentException("Errore: Tipo documento (T/R) non impostato dal BP.");
            }

            inizializzaTipoMovimento(aUC, doc);
            doc.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());

            doc.setCds_scrivania(CNRUserContext.getCd_cds(aUC));
            doc.setUo_scrivania(CNRUserContext.getCd_unita_organizzativa(aUC));
            doc.setEsercizio(CNRUserContext.getEsercizio(aUC));

            doc.setCondizioni(getHome(aUC, Condizione_beneBulk.class).findAll());
            doc.setInventario(caricaInventario(aUC));

            Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            doc.setConsegnatario(invHome.findConsegnatarioFor(doc.getInventario()));
            doc.setDelegato(invHome.findDelegatoFor(doc.getInventario()));
            doc.setUo_consegnataria(invHome.findUoRespFor(aUC, doc.getInventario()));

            doc.setDataRegistrazione(new Timestamp(System.currentTimeMillis()));
            doc.setToBeCreated();

            doc.setPgInventario(doc.getInventario().getPg_inventario());
            doc.setFlIncaricato(Boolean.FALSE);
            doc.setFlVettore(Boolean.FALSE);

            doc.setUuidFlussoAutorizzativo(null);
            doc.setStatoFlusso(null);
            doc.setDataInvioFirma(null);
            doc.setDataFirma(null);
            doc.setNoteRifiuto(null);
            doc.setDsDocTrasportoRientro("");

            return doc;

        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }

    /** Carica i tipi di movimento disponibili per il documento. */
    protected void inizializzaTipoMovimento(UserContext userContext, OggettoBulk oggettoBulk)
            throws ComponentException {
        try {
            Tipo_trasporto_rientroHome tipoHome =
                    (Tipo_trasporto_rientroHome) getHome(userContext, Tipo_trasporto_rientroBulk.class);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) oggettoBulk;

            Collection tipi = tipoHome.findTipiPerDocumento(userContext, doc.getTiDocumento());
            doc.setTipoMovimenti(tipi);

            if (tipi != null && tipi.size() == 1) {
                doc.setTipoMovimento((Tipo_trasporto_rientroBulk) tipi.iterator().next());
            }
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        super.initializeKeysAndOptionsInto(userContext, oggettoBulk);
    }

    /** Inventario associato alla UO dell'utente. */
    public Id_inventarioBulk caricaInventario(UserContext aUC)
            throws ComponentException, PersistencyException, IntrospectionException {
        Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
        Id_inventarioBulk inventario = invHome.findInventarioFor(aUC, false);
        if (inventario == null) {
            throw new ApplicationException("Nessun inventario associato alla UO!");
        }
        return inventario;
    }

    // ---------------------------------------------------------------- MODIFICA / RICERCA

    /** Inizializza per modifica con lock su documento e beni. */
    @Override
    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null)
            throw new ComponentException("Documento non trovato!");

        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
        docTR = (Doc_trasporto_rientroBulk) super.inizializzaBulkPerModifica(aUC, docTR);

        try {
            Doc_trasporto_rientroHome docHome =
                    (Doc_trasporto_rientroHome) getHome(aUC, Doc_trasporto_rientroBulk.class);

            docTR = (Doc_trasporto_rientroBulk) docHome.findByPrimaryKey(docTR);

            if (docTR == null)
                throw new ComponentException("Documento non trovato nel database!");

            try {
                lockBulk(aUC, docTR);
            } catch (BusyResourceException bre) {
                throw new ApplicationException(
                        "Il documento è già in modifica da un altro utente.\n" +
                                "Attendere che l'altro utente completi le modifiche prima di procedere.");
            } catch (OutdatedResourceException ore) {
                throw new ApplicationException(
                        "Il documento è stato modificato da un altro utente.\n" +
                                "Ricaricare il documento per visualizzare le modifiche più recenti.");
            }

            initializeKeysAndOptionsInto(aUC, docTR);

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (ApplicationException e) {
            throw e;
        }

        inizializzaTipoMovimento(aUC, docTR);

        try {
            docTR.setInventario(caricaInventario(aUC));
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC, docTR.getInventario()));

            // Incaricato
            if (docTR.getCdTerzoIncaricato() != null) {
                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(docTR.getCdTerzoIncaricato()));
                if (terzo != null) {
                    docTR.setTerzoIncRitiro(terzo);
                }
            }

            BulkHome dettHome = getHomeDocumentoTrasportoRientroDett(aUC, docTR);
            docTR.setDoc_trasporto_rientro_dettColl(
                    new BulkList(((Doc_trasporto_rientro_dettHome) dettHome).getDetailsFor(docTR)));

            // Smartworking: terzo dal primo dettaglio
            if (docTR.isSmartworking() &&
                    docTR.getDoc_trasporto_rientro_dettColl() != null &&
                    !docTR.getDoc_trasporto_rientro_dettColl().isEmpty()) {

                Doc_trasporto_rientro_dettBulk primoDettaglio =
                        (Doc_trasporto_rientro_dettBulk) docTR.getDoc_trasporto_rientro_dettColl().get(0);

                if (primoDettaglio.getCdTerzoAssegnatario() != null) {
                    TerzoBulk terzoSmart = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                            .findByPrimaryKey(new TerzoBulk(primoDettaglio.getCdTerzoAssegnatario()));
                    if (terzoSmart != null) {
                        docTR.setTerzoSmartworking(terzoSmart);
                    }
                }
            }

            for (Iterator dett = docTR.getDoc_trasporto_rientro_dettColl().iterator(); dett.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) dett.next();
                dettaglio.setDoc_trasporto_rientro(docTR);

                InventarioDocTRBulk inv = (InventarioDocTRBulk) getHome(aUC, InventarioDocTRBulk.class)
                        .findByPrimaryKey(new InventarioDocTRBulk(
                                dettaglio.getNr_inventario(),
                                dettaglio.getPg_inventario(),
                                new Long(dettaglio.getProgressivo().longValue())));
                dettaglio.setBene(inv);
            }

            getHomeCache(aUC).fetchAll(aUC, dettHome);

        } catch (Exception e) {
            throw new ComponentException(e);
        }

        return docTR;
    }

    /** Inizializza per ricerca. */
    @Override
    public OggettoBulk inizializzaBulkPerRicerca(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null) {
            throw new ApplicationException(
                    "Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!");
        }

        bulk = super.inizializzaBulkPerRicerca(userContext, bulk);
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        doc.setCds_scrivania(CNRUserContext.getCd_cds(userContext));
        doc.setUo_scrivania(CNRUserContext.getCd_unita_organizzativa(userContext));
        doc.setEsercizio(CNRUserContext.getEsercizio(userContext));

        try {
            caricaRelazioni(userContext, doc);
        } catch (PersistencyException | IntrospectionException e) {
            log.warn("Errore caricamento relazioni (ricerca): {}", e.getMessage());
        }

        return doc;
    }

    /** Inizializza per ricerca libera. */
    @Override
    public OggettoBulk inizializzaBulkPerRicercaLibera(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null) {
            throw new ComponentException(
                    "Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!");
        }

        bulk = super.inizializzaBulkPerRicercaLibera(userContext, bulk);
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        doc.setCds_scrivania(CNRUserContext.getCd_cds(userContext));
        doc.setUo_scrivania(CNRUserContext.getCd_unita_organizzativa(userContext));
        doc.setEsercizio(CNRUserContext.getEsercizio(userContext));

        try {
            caricaRelazioni(userContext, doc);
        } catch (PersistencyException | IntrospectionException e) {
            log.warn("Errore caricamento relazioni (ricerca libera): {}", e.getMessage());
        }

        return doc;
    }

    /** Carica inventario, tipo movimento e terzi collegati al documento. */
    private void caricaRelazioni(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws PersistencyException, IntrospectionException, ComponentException {

        if (doc.getPgInventario() != null) {
            Id_inventarioBulk inventario = (Id_inventarioBulk) getHome(userContext, Id_inventarioBulk.class)
                    .findByPrimaryKey(new Id_inventarioBulk(doc.getPgInventario()));
            doc.setInventario(inventario);

            if (inventario != null) {
                Id_inventarioHome inventarioHome =
                        (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);
                doc.setConsegnatario(inventarioHome.findConsegnatarioFor(inventario));
                doc.setDelegato(inventarioHome.findDelegatoFor(inventario));
                doc.setUo_consegnataria(inventarioHome.findUoRespFor(userContext, inventario));
            }
        }

        if (doc.getCd_tipo_trasporto_rientro() != null) {
            Tipo_trasporto_rientroBulk tipoMovimento =
                    (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                            .findByPrimaryKey(new Tipo_trasporto_rientroBulk(doc.getCd_tipo_trasporto_rientro()));
            doc.setTipoMovimento(tipoMovimento);
        }

        if (doc.getCdTerzoIncaricato() != null) {
            TerzoBulk terzoIncaricato = (TerzoBulk) getHome(userContext, TerzoBulk.class)
                    .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoIncaricato()));
            doc.setTerzoIncRitiro(terzoIncaricato);
        }

        if (doc.getCdTerzoResponsabile() != null) {
            TerzoBulk terzoResponsabile = (TerzoBulk) getHome(userContext, TerzoBulk.class)
                    .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoResponsabile()));
            doc.setTerzoRespDip(terzoResponsabile);
        }
    }

    // ---------------------------------------------------------------- CREA / MODIFICA

    /** Crea la testata del documento. */
    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        try {
            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

            if (docT.getPgDocTrasportoRientro() != null) {
                Doc_trasporto_rientroBulk existing =
                        (Doc_trasporto_rientroBulk) findByPrimaryKey(userContext, docT);
                if (existing != null) {
                    return modificaConBulk(userContext, docT);
                }
            }

            verificaDocumentoNonEsistente(userContext, docT);
            validaDoc(userContext, docT, false);

            TerzoBulk terzoResponsabile = recuperaTerzoResponsabileUO(userContext);
            if (terzoResponsabile != null) {
                docT.setTerzoRespDip(terzoResponsabile);
                docT.setCdTerzoResponsabile(terzoResponsabile.getCd_terzo());
            }

            if (docT.getTerzoIncRitiro() != null &&
                    docT.getTerzoIncRitiro().getCd_terzo() != null) {
                docT.setCdTerzoIncaricato(docT.getTerzoIncRitiro().getCd_terzo());
            }

            if (docT.isSmartworking()) {
                if (docT.getTerzoSmartworking() != null &&
                        docT.getTerzoSmartworking().getCd_terzo() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoSmartworking().getCd_terzo());
                }
            } else if (docT.getFlIncaricato()) {
                if (docT.getTerzoIncRitiro() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoIncRitiro().getCd_terzo());
                }
            }

            docT.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
            docT.setToBeCreated();
            docT.setStato(STATO_INSERITO);
            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);
            docT.setToBeUpdated();
            return docT;

        } catch (PersistencyException e) {
            throw handleException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    /** Verifica che il documento non esista già. */
    private void verificaDocumentoNonEsistente(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {

        if (docT.getPgDocTrasportoRientro() != null) return;

        Doc_trasporto_rientroBulk existing =
                (Doc_trasporto_rientroBulk) findByPrimaryKey(userContext, docT);

        if (existing != null) {
            throw new ApplicationException(
                    "Il documento è già stato salvato (PG=" + docT.getPgDocTrasportoRientro() + ")");
        }
    }

    /** Aggiorna il documento (dettagli, validazione, allegati, progressivo). */
    @Override
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);

            docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docT)));

            caricaDettagliDocumento(aUC, docT);

            if (docT.isFirmatoDaCompletare()) {
                if (docT.getDoc_trasporto_rientro_dettColl() == null
                        || docT.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                    throw new ApplicationException("Attenzione: è necessario specificare almeno un bene.");
                }
            } else {
                validaDoc(aUC, docT, true);
            }

            validaAllegatiComp(docT);

            if (isProgressivoTemporaneo(docT)) {
                return gestisciProgressivoTemporaneo(aUC, docT);
            } else {
                return gestisciProgressivoDefinitivo(aUC, docT);
            }

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (Throwable e) {
            throw new ComponentException(e);
        }
    }

    /** Progressivo negativo = documento temporaneo. */
    private boolean isProgressivoTemporaneo(Doc_trasporto_rientroBulk docT) {
        return docT.getPgDocTrasportoRientro() != null
                && docT.getPgDocTrasportoRientro() < 0;
    }

    /** Converte il documento temporaneo in definitivo assegnando un nuovo progressivo. */
    private Doc_trasporto_rientroBulk gestisciProgressivoTemporaneo(
            UserContext aUC,
            Doc_trasporto_rientroBulk docT)
            throws PersistencyException, ComponentException, IntrospectionException {

        Numeratore_doc_t_rHome numHome =
                (Numeratore_doc_t_rHome) getHome(aUC, Numeratore_doc_t_rBulk.class);

        Long nuovoPgDoc = numHome.getNextPg(
                aUC,
                docT.getEsercizio(),
                docT.getPgInventario(),
                docT.getTiDocumento(),
                aUC.getUser()
        );

        Doc_trasporto_rientroHome home =
                (Doc_trasporto_rientroHome) getHome(aUC, docT);

        home.confirmDocTrasportoRientroTemporaneo(aUC, docT, nuovoPgDoc);

        docT.setPgDocTrasportoRientro(nuovoPgDoc);
        docT.setStato(STATO_INSERITO);
        docT.setToBeUpdated();

        // Ricarica i dettagli con il nuovo progressivo
        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);

        docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docT)));

        if (docT.getArchivioAllegati() == null) {
            docT.setArchivioAllegati(new BulkList<>());
        }

        preparaEAggiornaRiferimentiInversi(aUC, docT);

        docT.setCrudStatus(OggettoBulk.NORMAL);

        return docT;
    }

    /** Aggiorna dettagli e allegati di un documento già definitivo. */
    private Doc_trasporto_rientroBulk gestisciProgressivoDefinitivo(
            UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {

        if (docT.getDoc_trasporto_rientro_dettColl() != null) {
            for (Iterator i = docT.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) i.next();
                updateBulk(aUC, dett);
            }
        }

        if (docT.getArchivioAllegati() != null) {
            for (AllegatoGenericoBulk allegato : docT.getArchivioAllegati()) {
                if (allegato != null) {
                    updateBulk(aUC, allegato);
                }
            }
        }

        preparaEAggiornaRiferimentiInversi(aUC, docT);
        docT.setToBeUpdated();

        return (Doc_trasporto_rientroBulk) super.modificaConBulk(aUC, docT);
    }

    // ---------------------------------------------------------------- SALVA DEFINITIVO

    /** Rende definitivo il documento (richiede allegato firmato). */
    public Doc_trasporto_rientroBulk salvaDefinitivo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR) throws ComponentException {
        try {
            if (docTR == null) {
                throw new ApplicationException("Documento non presente.");
            }

            if (docTR.isAnnullato()) {
                throw new ApplicationException("Impossibile rendere definitivo un documento annullato.");
            }

            if (docTR.isDefinitivo()) {
                throw new ApplicationException("Il documento è già definitivo.");
            }

            if (docTR.isInAttesaDiFirma()) {
                throw new ApplicationException("Il documento è ancora in attesa di firma.");
            }

            if (!hasAllegatoFirmato(docTR)) {
                throw new ApplicationException(
                        "È obbligatorio allegare il documento firmato prima di rendere definitivo il documento.");
            }

            // Firma manuale: documento predisposto (INS, flusso null), il flusso diventa FIR
            final boolean firmaManuale = docTR.isPredispostoAllaFirma();

            validaAllegatiComp(docTR);
            caricaDettagliDocumento(userContext, docTR);

            if (docTR instanceof DocumentoRientroBulk) {
                boolean setRifFields = false;

                if (docTR.getDoc_trasporto_rientro_dettColl() == null
                        || docTR.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                    setRifFields = true;
                } else {
                    for (Object o : docTR.getDoc_trasporto_rientro_dettColl()) {
                        DocumentoRientroDettBulk dettRientro = (DocumentoRientroDettBulk) o;
                        if (dettRientro.getDocTrasportoDettRif() == null) {
                            setRifFields = true;
                            break;
                        }
                    }
                }

                if (setRifFields) {
                    popolaCampiRiferimento(userContext, docTR);
                }
            }

            validaDoc(userContext, docTR, true);
            // Il firmato sostituisce il da firmare (rimosso dal documentale in archiviaAllegatiDocTR)
            if (firmaManuale) {
                rimuoviAllegatiDaFirmare(docTR);
            }

            if (docTR.getArchivioAllegati() != null) {
                for (AllegatoGenericoBulk allegato : docTR.getArchivioAllegati()) {
                    if (allegato != null) {
                        updateBulk(userContext, allegato);
                    }
                }
            }

            boolean beneInIstituto = docTR instanceof DocumentoRientroBulk;
            aggiornaStatoBeni(userContext, docTR, beneInIstituto);

            docTR.setStato(STATO_DEFINITIVO);
            if (firmaManuale) {
                docTR.setStatoFlusso(STATO_FLUSSO_FIRMATO);
                if (docTR.getDataFirma() == null) {
                    docTR.setDataFirma(new Timestamp(System.currentTimeMillis()));
                }
            }
            docTR.setToBeUpdated();
            preparaEAggiornaRiferimentiInversi(userContext, docTR);

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (PersistencyException e) {
            throw new ComponentException("Errore durante il salvataggio definitivo", e);
        }
    }

    /** Toglie dalla collection gli allegati DA_FIRMARE (finiscono nella delete list). */
    private void rimuoviAllegatiDaFirmare(Doc_trasporto_rientroBulk doc) {
        String aspect = DocTraspRientUtil.aspectDaFirmare(doc);
        if (aspect == null || doc.getArchivioAllegati() == null) {
            return;
        }
        BulkList<AllegatoGenericoBulk> archivio = doc.getArchivioAllegati();
        for (int i = archivio.size() - 1; i >= 0; i--) {
            AllegatoGenericoBulk a = archivio.get(i);
            if (a instanceof AllegatoDocTraspRientroBulk
                    && aspect.equals(((AllegatoDocTraspRientroBulk) a).getAspectName())) {
                archivio.remove(i);
            }
        }
    }

    /** Popola i riferimenti verso il trasporto sui dettagli di rientro. */
    public void popolaCampiRiferimento(UserContext uc, Doc_trasporto_rientroBulk docTR)
            throws ComponentException, PersistencyException {

        if (!(docTR instanceof DocumentoRientroBulk)) return;
        if (docTR.getDoc_trasporto_rientro_dettColl() == null) return;

        for (Object o : docTR.getDoc_trasporto_rientro_dettColl()) {
            DocumentoRientroDettBulk dettRientro = (DocumentoRientroDettBulk) o;
            DocumentoTrasportoDettBulk dettTrasporto = dettRientro.getDocTrasportoDettRif();
            if (dettTrasporto == null) continue;

            dettRientro.setDocTrasportoDettRif(dettTrasporto);
            dettRientro.setToBeUpdated();
            updateBulk(uc, dettRientro);
        }
    }

    // ---------------------------------------------------------------- STATO

    /** INSERITO → INVIATO (predisposizione alla firma). */
    public Doc_trasporto_rientroBulk changeStatoInInviato(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR)
            throws ComponentException {

        return inviaDocumentoAllaFirma(userContext, docTR);
    }

    // ---------------------------------------------------------------- ANNULLAMENTO

    /** Annulla il documento e aggiorna lo stato dei beni. */
    public Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            caricaDettagliDocumento(userContext, doc);
            validaAnnullamento(doc);

            boolean beneInIstituto;

            if (doc instanceof DocumentoTrasportoBulk) {
                verificaNonEsistonoRientri((DocumentoTrasportoBulk) doc);
                beneInIstituto = true;
            } else if (doc instanceof DocumentoRientroBulk) {
                rimuoviRiferimentiDaTrasportoOriginale((DocumentoRientroBulk) doc);
                beneInIstituto = false;
            } else {
                throw new ComponentException(
                        "Tipo documento non riconosciuto: " + doc.getClass().getName());
            }

            aggiornaStatoBeni(userContext, doc, beneInIstituto);

            doc.setStato(STATO_ANNULLATO);
            doc.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore durante l'annullamento del documento: " + e.getMessage(), e);
        }
    }

    private void validaAnnullamento(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (STATO_ANNULLATO.equals(doc.getStato()))
            throw new ApplicationException("Il documento è già annullato");
        if (STATO_INVIATO.equals(doc.getStato()))
            throw new ApplicationException("Impossibile annullare un documento inviato in firma");
    }

    private void verificaNonEsistonoRientri(DocumentoTrasportoBulk doc) throws ApplicationException {
        if (doc.getDoc_trasporto_rientro_dettColl().isEmpty()) return;
        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            DocumentoTrasportoDettBulk dettaglio = (DocumentoTrasportoDettBulk) obj;
            if (dettaglio.getDocRientroDettRif() != null) {
                throw new ApplicationException(
                        "Impossibile annullare il documento di TRASPORTO: " +
                                "esistono documenti di RIENTRO collegati. " +
                                "Annullare prima i documenti di RIENTRO.");
            }
        }
    }

    private void rimuoviRiferimentiDaTrasportoOriginale(DocumentoRientroBulk doc)
            throws ComponentException {
        if (doc.getDoc_trasporto_rientro_dettColl() == null) return;
        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            Doc_trasporto_rientro_dettBulk dettRientro = (Doc_trasporto_rientro_dettBulk) obj;
            if (dettRientro.getDoc_trasporto_rientroDettRif() != null) {
                Doc_trasporto_rientro_dettBulk ref = dettRientro.getDoc_trasporto_rientroDettRif();
                ref.setDoc_trasporto_rientroDettRif(null);
                ref.setToBeUpdated();
            }
        }
    }

    // ---------------------------------------------------------------- STATO BENI

    /** Aggiorna FL_BENE_IN_IST su tutti i beni del documento. */
    private void aggiornaStatoBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            boolean beneInIstituto)
            throws ComponentException {

        try {
            if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                    doc.getDoc_trasporto_rientro_dettColl().isEmpty()) return;

            InventarioDocTRHome beneHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);

            for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) obj;
                InventarioDocTRBulk bene = caricaBeneDaDettaglio(userContext, beneHome, dettaglio);
                if (bene != null) {
                    bene.setFl_bene_in_ist(beneInIstituto);
                    bene.setToBeUpdated();
                    updateBulk(userContext, bene);
                }
            }
        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore durante l'aggiornamento dello stato dei beni: " + e.getMessage(), e);
        }
    }

    private InventarioDocTRBulk caricaBeneDaDettaglio(
            UserContext userContext,
            InventarioDocTRHome beneHome,
            Doc_trasporto_rientro_dettBulk dettaglio)
            throws PersistencyException {

        return (InventarioDocTRBulk) beneHome.findByPrimaryKey(
                new InventarioDocTRBulk(
                        dettaglio.getNr_inventario(),
                        dettaglio.getPg_inventario(),
                        Long.valueOf(dettaglio.getProgressivo())
                )
        );
    }

    // ---------------------------------------------------------------- HELPER HOME

    /** Home dei dettagli in base a TI_DOCUMENTO. */
    private BulkHome getHomeDocumentoTrasportoRientroDett(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        if (!(bulk instanceof Doc_trasporto_rientroBulk)) {
            throw new ComponentException("Bulk non valido per documento Trasporto/Rientro.");
        }

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            return getHome(userContext, DocumentoTrasportoDettBulk.class);
        }

        if (RIENTRO.equals(doc.getTiDocumento())) {
            return getHome(userContext, DocumentoRientroDettBulk.class);
        }

        throw new ComponentException(
                "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
        );
    }

    private void caricaDettagliDocumento(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException, PersistencyException {

        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

        doc.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(doc)));
    }

    // ---------------------------------------------------------------- PROGRESSIVO

    protected Doc_trasporto_rientroBulk assegnaProgressivo(UserContext userContext,
                                                           Doc_trasporto_rientroBulk doc)
            throws ComponentException, PersistencyException {
        try {
            ((Doc_trasporto_rientroHome) getHome(userContext, doc.getClass()))
                    .inizializzaProgressivo(userContext, doc);
            return doc;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ---------------------------------------------------------------- RICERCA TERZI

    /** Ricerca incaricato tramite componente Terzo. */
    public SQLBuilder selectTerzoIncRitiroByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            TerzoBulk terzo,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {
        return findTerziDipendenti(userContext, terzo, clause);
    }

    /** Ricerca smartworking tramite componente Terzo. */
    public SQLBuilder selectTerzoSmartworkingByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            TerzoBulk terzo,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {
        return findTerziDipendenti(userContext, terzo, clause);
    }

    private SQLBuilder findTerziDipendenti(UserContext userContext, TerzoBulk terzo, CompoundFindClause clause)
            throws ComponentException, RemoteException {
        try {
            TerzoComponentSession sess = (TerzoComponentSession)
                    EJBCommonServices.createEJB(
                            "CNRANAGRAF00_EJB_TerzoComponentSession",
                            TerzoComponentSession.class);
            return sess.findTerziDipendentiByClause(userContext, terzo, clause);
        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }

    // ---------------------------------------------------------------- RIFERIMENTI INVERSI

    /** Aggiorna la relazione bidirezionale Trasporto ↔ Rientro. */
    public void preparaEAggiornaRiferimentiInversi(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        if (!(doc instanceof DocumentoRientroBulk)) return;
        if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                doc.getDoc_trasporto_rientro_dettColl().isEmpty()) return;

        try {
            DocumentoTrasportoDettHome trasportoDettHome =
                    (DocumentoTrasportoDettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

            for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
                if (!(obj instanceof DocumentoRientroDettBulk)) continue;

                DocumentoRientroDettBulk dettaglioRientro = (DocumentoRientroDettBulk) obj;
                DocumentoTrasportoDettBulk dettaglioTrasportoRef = dettaglioRientro.getDocTrasportoDettRif();
                if (dettaglioTrasportoRef == null) continue;

                DocumentoTrasportoDettBulk dettaglioTrasportoDB =
                        (DocumentoTrasportoDettBulk) trasportoDettHome.findByPrimaryKey(dettaglioTrasportoRef);

                if (dettaglioTrasportoDB == null) {
                    throw new ComponentException(
                            "Dettaglio trasporto non trovato per il bene: " +
                                    dettaglioTrasportoRef.getCod_bene());
                }

                dettaglioTrasportoDB.setDocRientroDettRif(dettaglioRientro);
                dettaglioTrasportoDB.setToBeUpdated();
                updateBulk(userContext, dettaglioTrasportoDB);
            }

        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore di persistenza nell'aggiornamento riferimenti inversi", e);
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore nell'aggiornamento dei riferimenti inversi: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- ALLEGATI

    private int countAllegatiFirmati(Doc_trasporto_rientroBulk doc, String aspectAtteso) {
        if (doc == null || doc.getArchivioAllegati() == null || aspectAtteso == null) return 0;
        return (int) doc.getArchivioAllegati().stream()
                .filter(o -> o instanceof AllegatoDocTraspRientroBulk)
                .map(o -> (AllegatoDocTraspRientroBulk) o)
                .filter(a -> a.getCrudStatus() != OggettoBulk.TO_BE_DELETED)
                .filter(a -> aspectAtteso.equals(a.getAspectName()))
                .count();
    }

    private boolean hasAllegatoFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc.getArchivioAllegati() == null) return false;
        return countAllegatiFirmati(doc, DocTraspRientUtil.aspectFirmato(doc)) == 1;
    }

    public void validaAggiuntaAllegatoFirmato(Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            validaAllegatiComp(doc);
        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        }
    }

    private void validaAllegatiComp(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (doc == null || doc.getArchivioAllegati() == null) return;

        List<AllegatoGenericoBulk> attivi = doc.getArchivioAllegati().stream()
                .filter(a -> a.getCrudStatus() != OggettoBulk.TO_BE_DELETED)
                .collect(Collectors.toList());

        Set<String> nomiVisti = new HashSet<>();

        for (AllegatoGenericoBulk allegato : attivi) {
            if (allegato.getCrudStatus() == OggettoBulk.TO_BE_CREATED) {
                boolean haFile = allegato.getFile() != null;
                boolean haNome = allegato.getNome() != null && !allegato.getNome().isEmpty();
                if (!haFile && !haNome) {
                    throw new ApplicationException(
                            "Attenzione: è presente un allegato senza file selezionato. " +
                                    "Selezionare un file per ogni allegato prima di salvare.");
                }
            }

            String nome = DocTraspRientUtil.nomeAllegato(allegato);
            if (nome != null && !nomiVisti.add(nome)) {
                throw new ApplicationException(
                        "Attenzione: impossibile caricare l'allegato '" + allegato.getNome() +
                                "' poiché esiste già un file con lo stesso nome.");
            }
        }

        String aspectAtteso = DocTraspRientUtil.aspectFirmato(doc);
        if (aspectAtteso != null) {
            long countFirmati = attivi.stream()
                    .filter(a -> a instanceof AllegatoDocTraspRientroBulk)
                    .map(a -> (AllegatoDocTraspRientroBulk) a)
                    .filter(a -> aspectAtteso.equals(a.getAspectName()))
                    .count();

            if (countFirmati > 1) {
                String tipo = TRASPORTO.equals(doc.getTiDocumento())
                        ? "Documento di Trasporto"
                        : "Documento di Rientro";
                throw new ApplicationException(
                        "Attenzione: è consentito allegare un solo " + tipo + ".");
            }
        }
        String aspectDaFirmare = DocTraspRientUtil.aspectDaFirmare(doc);
        if (aspectDaFirmare != null) {
            long countDaFirmare = attivi.stream()
                    .filter(a -> a instanceof AllegatoDocTraspRientroBulk)
                    .map(a -> (AllegatoDocTraspRientroBulk) a)
                    .filter(a -> aspectDaFirmare.equals(a.getAspectName()))
                    .count();

            if (countDaFirmare > 1) {
                String tipo = TRASPORTO.equals(doc.getTiDocumento())
                        ? "Documento di Trasporto da firmare"
                        : "Documento di Rientro da firmare";
                throw new ApplicationException(
                        "Attenzione: è consentito allegare un solo " + tipo + ".");
            }
        }
    }

    /**
     * Archivia, aggiorna o cancella gli allegati sul documentale.
     * Niente cast a DocumentoTrasportoBulk/DocumentoRientroBulk: la classe concreta può non coincidere con tiDocumento.
     */
    public void archiviaAllegatiDocTR(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        String storePath = getStorePathDocTrasportoRientro(doc);

        if (doc.getArchivioAllegati() == null) {
            return;
        }

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        for (AllegatoGenericoBulk allegatoGen : doc.getArchivioAllegati()) {
            if (!(allegatoGen instanceof AllegatoDocTraspRientroBulk)) {
                continue;
            }

            AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) allegatoGen;

            if (allegato.isToBeCreated()) {
                File file = Optional.ofNullable(allegato.getFile())
                        .orElseThrow(() -> new ApplicationException(
                                "File non presente per allegato: " + allegato.getNome()));

                try (FileInputStream fis = new FileInputStream(file)) {
                    allegato.complete(userContext);

                    storeService.storeSimpleDocument(
                            allegato,
                            fis,
                            allegato.getContentType(),
                            allegato.getNome(),
                            storePath
                    );

                    allegato.setCrudStatus(OggettoBulk.NORMAL);

                } catch (FileNotFoundException e) {
                    throw new ApplicationException("File non trovato: " + allegato.getNome(), e);

                } catch (StorageException e) {
                    if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                        throw new ApplicationException("File già presente: " + allegato.getNome());
                    }
                    throw new ApplicationException("Errore storage per: " + allegato.getNome(), e);

                } catch (Exception e) {
                    throw new ApplicationException(
                            "Errore archiviazione allegato: " + allegato.getNome(), e);
                }

            } else if (allegato.isToBeUpdated()) {
                try {
                    if (allegato.getFile() != null) {
                        try (FileInputStream fis = new FileInputStream(allegato.getFile())) {
                            storeService.updateStream(
                                    allegato.getStorageKey(),
                                    fis,
                                    allegato.getContentType()
                            );
                        }
                    }

                    allegato.complete(userContext);

                    storeService.updateProperties(
                            allegato,
                            storeService.getStorageObjectBykey(allegato.getStorageKey())
                    );

                    allegato.setCrudStatus(OggettoBulk.NORMAL);

                } catch (FileNotFoundException e) {
                    throw new ApplicationException("File non trovato: " + allegato.getNome(), e);

                } catch (StorageException e) {
                    if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                        throw new ApplicationException("File già presente: " + allegato.getNome());
                    }
                    throw new ApplicationException("Errore storage per: " + allegato.getNome(), e);

                } catch (Exception e) {
                    throw new ApplicationException(
                            "Errore aggiornamento allegato: " + allegato.getNome(), e);
                }
            }
        }

        for (Iterator<AllegatoDocTraspRientroBulk> it =
             doc.getArchivioAllegati().deleteIterator(); it.hasNext(); ) {

            AllegatoDocTraspRientroBulk allegato = it.next();

            if (allegato.isToBeDeleted()) {
                if (allegato.getStorageKey() != null) {
                    storeService.delete(allegato.getStorageKey());
                }
                allegato.setCrudStatus(OggettoBulk.NORMAL);
            }
        }
    }

    // ---------------------------------------------------------------- SELEZIONE BENI

    /** Iteratore sui dettagli del documento per la modifica. */
    public RemoteIterator selectEditDettagliTrasporto(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR,
            Class bulkClass,
            CompoundFindClause clauses) throws ComponentException {

        if (docTR == null || docTR.getPgDocTrasportoRientro() == null)
            return new EmptyRemoteIterator();

        SQLBuilder sql = getHome(userContext,
                (docTR.isRientro() ? DocumentoRientroDettBulk.class : DocumentoTrasportoDettBulk.class))
                .createSQLBuilder();

        sql.addSQLClause("AND", "PG_INVENTARIO", sql.EQUALS, docTR.getPgInventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
        sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS, docTR.getPgDocTrasportoRientro());

        try {
            return iterator(userContext, sql, bulkClass, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public RemoteIterator selectEditDettagliRientro(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            Class bulkClass, CompoundFindClause filters) throws ComponentException {
        return selectEditDettagliTrasporto(userContext, doc, bulkClass, filters);
    }

    /** Beni disponibili per il trasporto. */
    public RemoteIterator getListaBeniDaTrasportare(
            UserContext userContext,
            OggettoBulk bulk,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses) throws ComponentException {

        try {
            InventarioDocTRHome invBeniHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);
            SQLBuilder sql = invBeniHome.getListaBeniDaTrasportare(
                    userContext, (Doc_trasporto_rientroBulk) bulk, beni_da_escludere);
            sql.addClause(clauses);
            try {
                return iterator(userContext, sql, InventarioDocTRBulk.class, null);
            } catch (Exception e) {
                throw handleException(e);
            }
        } catch (PersistencyException | IntrospectionException ex) {
            throw handleException(ex);
        }
    }

    /** SQL dei beni (INVENTARIO_BENI) associati ai dettagli del documento. */
    private SQLBuilder sqlBeniDelDocumento(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {
        SQLBuilder sql = getHome(userContext, InventarioDocTRBulk.class).createSQLBuilder();

        sql.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT");
        sql.addSQLJoin("INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS,
                "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO");
        sql.addSQLJoin("INVENTARIO_BENI.NR_INVENTARIO", SQLBuilder.EQUALS,
                "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO");
        sql.addSQLJoin("INVENTARIO_BENI.PROGRESSIVO", SQLBuilder.EQUALS,
                "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO");

        sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                SQLBuilder.EQUALS, docT.getPgInventario());
        sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO",
                SQLBuilder.EQUALS, docT.getTiDocumento());
        sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO",
                SQLBuilder.EQUALS, docT.getEsercizio());
        sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                SQLBuilder.EQUALS, docT.getPgDocTrasportoRientro());

        sql.addOrderBy("NR_INVENTARIO");
        sql.addOrderBy("PROGRESSIVO");
        return sql;
    }

    /** Beni già selezionati nel documento. */
    public SimpleBulkList selezionati(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null) return new SimpleBulkList();

        try {
            List risultato = getHome(userContext, InventarioDocTRBulk.class)
                    .fetchAll(sqlBeniDelDocumento(userContext, docT));
            if (risultato != null && risultato.size() > 0) return new SimpleBulkList(risultato);

        } catch (PersistencyException pe) {
            throw new ComponentException(pe);
        }
        return new SimpleBulkList();
    }

    /** Iteratore sui beni associati al documento. */
    public RemoteIterator selectBeniAssociatiByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            Class bulkClass) throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null)
            return new EmptyRemoteIterator();

        try {
            return iterator(userContext, sqlBeniDelDocumento(userContext, docT), bulkClass, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ---------------------------------------------------------------- MODIFICA BENI

    /** Aggiunge o rimuove beni (e relativi accessori) dal documento. */
    public void modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            BitSet old_ass,
            BitSet ass)
            throws ComponentException {

        try {
            if (docT.getDoc_trasporto_rientro_dettColl() == null) {
                docT.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
            }

            boolean isTrasporto = docT instanceof DocumentoTrasportoBulk;

            if (!isTrasporto) {
                List<InventarioDocTRBulk> beniDaAggiungere = new ArrayList<>();
                for (int i = 0; i < beni.length; i++) {
                    if (old_ass.get(i) != ass.get(i) && ass.get(i)) {
                        beniDaAggiungere.add((InventarioDocTRBulk) beni[i]);
                    }
                }
                validaBeniRientroOmogenei(userContext, docT, beniDaAggiungere);
            }

            for (int i = 0; i < beni.length; i++) {
                InventarioDocTRBulk bene = (InventarioDocTRBulk) beni[i];

                if (old_ass.get(i) != ass.get(i)) {

                    if (ass.get(i)) {
                        // Ricarica dal DB
                        try {
                            InventarioDocTRBulk beneDB =
                                    (InventarioDocTRBulk) getHome(userContext, InventarioDocTRBulk.class)
                                            .findByPrimaryKey(new InventarioDocTRBulk(
                                                    bene.getNr_inventario(),
                                                    bene.getPg_inventario(),
                                                    bene.getProgressivo()));
                            if (beneDB != null) {
                                bene = beneDB;
                                beni[i] = beneDB;
                            }
                        } catch (PersistencyException pe) {
                            throw handleException(pe);
                        }

                        try {
                            lockBulk(userContext, bene);
                        } catch (OutdatedResourceException | BusyResourceException | PersistencyException e) {
                            if (e instanceof BusyResourceException) {
                                throw new ApplicationException(
                                        "Risorsa occupata.\nIl bene " +
                                                bene.getNumeroBeneCompleto() +
                                                " è bloccato da un altro utente.");
                            }
                            throw handleException(e);
                        }

                        if (isTrasporto) {
                            DocumentoTrasportoDettBulk dett = new DocumentoTrasportoDettBulk();
                            dett.setDocumentoTrasporto((DocumentoTrasportoBulk) docT);
                            dett.setBene(bene);
                            dett.setPg_inventario(docT.getPgInventario());
                            dett.setTi_documento(docT.getTiDocumento());
                            dett.setEsercizio(docT.getEsercizio());
                            dett.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
                            dett.setCdTerzoAssegnatario(calcolaCdTerzoAssegnatario(docT, bene));

                            Doc_trasporto_rientro_dettBulk dettRientroOrig =
                                    trovaDettaglioOriginale(userContext, bene, docT.getPgInventario(), RIENTRO);
                            if (dettRientroOrig != null) {
                                dett.setDocRientroDettRif((DocumentoRientroDettBulk) dettRientroOrig);
                            }

                            docT.getDoc_trasporto_rientro_dettColl().add(dett);
                            dett.setToBeCreated();
                            super.creaConBulk(userContext, dett);

                        } else {
                            DocumentoRientroDettBulk dett = new DocumentoRientroDettBulk();
                            dett.setDocumentoRientro((DocumentoRientroBulk) docT);
                            dett.setBene(bene);
                            dett.setPg_inventario(docT.getPgInventario());
                            dett.setTi_documento(docT.getTiDocumento());
                            dett.setEsercizio(docT.getEsercizio());
                            dett.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
                            dett.setCdTerzoAssegnatario(calcolaCdTerzoAssegnatario(docT, bene));

                            Doc_trasporto_rientro_dettBulk dettTrasportoOrig =
                                    trovaDettaglioOriginale(userContext, bene, docT.getPgInventario(), TRASPORTO);
                            if (dettTrasportoOrig == null) {
                                throw new ApplicationException(
                                        "Bene " + bene.getNumeroBeneCompleto() +
                                                " non presente in nessun documento di trasporto definitivo!");
                            }

                            dett.setDocTrasportoDettRif((DocumentoTrasportoDettBulk) dettTrasportoOrig);
                            docT.getDoc_trasporto_rientro_dettColl().add(dett);
                            dett.setToBeCreated();
                            super.creaConBulk(userContext, dett);
                        }

                    } else {
                        SimpleBulkList collection = docT.getDoc_trasporto_rientro_dettColl();
                        if (collection != null) {
                            Doc_trasporto_rientro_dettBulk toDelete = null;
                            for (Iterator it = collection.iterator(); it.hasNext(); ) {
                                Doc_trasporto_rientro_dettBulk dett =
                                        (Doc_trasporto_rientro_dettBulk) it.next();
                                if (dett.getNr_inventario().equals(bene.getNr_inventario()) &&
                                        dett.getProgressivo().equals(bene.getProgressivo().intValue())) {
                                    toDelete = dett;
                                    it.remove();
                                    break;
                                }
                            }
                            if (toDelete != null && docT.getPgDocTrasportoRientro() != null) {
                                toDelete.setToBeDeleted();
                                super.eliminaConBulk(userContext, toDelete);
                            }
                        }
                    }
                }
            }

        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    public void modificaBeniRientratiConAccessori(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni, BitSet oldSelection, BitSet newSelection)
            throws ComponentException {
        modificaBeniTrasportatiConAccessori(userContext, doc, beni, oldSelection, newSelection);
    }

    // ---------------------------------------------------------------- DETTAGLIO ORIGINALE

    /** Dettaglio più recente (non annullato) di un documento TRASPORTO o RIENTRO per il bene. */
    private Doc_trasporto_rientro_dettBulk trovaDettaglioOriginale(
            UserContext userContext,
            InventarioDocTRBulk bene,
            Long pgInventario,
            String tipoDocumentoDaCercare)
            throws ComponentException {

        try {
            if (OggettoBulk.isNullOrEmpty(String.valueOf(bene.getNr_inventario()))) return null;

            Doc_trasporto_rientro_dettHome dettHome;
            if (TRASPORTO.equals(tipoDocumentoDaCercare)) {
                dettHome = (Doc_trasporto_rientro_dettHome) getHome(
                        userContext, DocumentoTrasportoDettBulk.class);
            } else {
                dettHome = (Doc_trasporto_rientro_dettHome) getHome(
                        userContext, DocumentoRientroDettBulk.class);
            }

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO d");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "d.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "d.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "d.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "d.PG_DOC_TRASPORTO_RIENTRO");

            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                    SQLBuilder.EQUALS, pgInventario);
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO",
                    SQLBuilder.EQUALS, bene.getNr_inventario());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO",
                    SQLBuilder.EQUALS, bene.getProgressivo());
            sql.addSQLClause("AND", "d.TI_DOCUMENTO",
                    SQLBuilder.EQUALS, tipoDocumentoDaCercare);
            sql.addSQLClause("AND", "d.STATO",
                    SQLBuilder.EQUALS, STATO_DEFINITIVO);
            sql.addSQLClause("AND", "d.STATO",
                    SQLBuilder.NOT_EQUALS, STATO_ANNULLATO);
            sql.addOrderBy("d.DATA_REGISTRAZIONE DESC");

            List risultati = dettHome.fetchAll(sql);
            if (risultati != null && !risultati.isEmpty())
                return (Doc_trasporto_rientro_dettBulk) risultati.get(0);

            return null;

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    // ---------------------------------------------------------------- ACCESSORI

    /** Accessori associati a un bene principale. */
    public List cercaBeniAccessoriAssociati(UserContext userContext, InventarioDocTRBulk benePrincipale)
            throws ComponentException {

        try {
            InventarioDocTRHome home = (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);
            SQLBuilder sql = home.createSQLBuilder();

            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, benePrincipale.getPg_inventario());
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, benePrincipale.getNr_inventario());
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS, benePrincipale.getProgressivo());

            List beniAccessori = home.fetchAll(sql);
            if (beniAccessori != null && !beniAccessori.isEmpty()) return beniAccessori;

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
        return Collections.emptyList();
    }

    /** Accessori presenti nel TRASPORTO originale e non ancora rientrati. */
    public List cercaBeniAccessoriPresentinelTrasportoOriginale(
            UserContext userContext,
            InventarioDocTRBulk beneRientro,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            if (beneRientro == null || doc == null) return Collections.emptyList();

            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                    trovaDettaglioOriginale(userContext, beneRientro, doc.getPgInventario(), TRASPORTO);

            if (dettaglioTrasportoOriginale == null) return Collections.emptyList();

            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getPg_inventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getTi_documento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro());
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    beneRientro.getNr_inventario());
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    beneRientro.getProgressivo());

            List dettagliAccessori = dettHome.fetchAll(sql);
            if (dettagliAccessori == null || dettagliAccessori.isEmpty()) return Collections.emptyList();

            InventarioDocTRHome beneHome = (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);
            List<InventarioDocTRBulk> accessoriDisponibili = new ArrayList<>();

            for (Object obj : dettagliAccessori) {
                Doc_trasporto_rientro_dettBulk dettaglioAccessorio = (Doc_trasporto_rientro_dettBulk) obj;
                boolean giaRientrato = verificaSeAccessorioGiaRientrato(userContext, dettaglioAccessorio);
                if (!giaRientrato) {
                    InventarioDocTRBulk accessorio = (InventarioDocTRBulk) beneHome.findByPrimaryKey(
                            new InventarioDocTRBulk(
                                    dettaglioAccessorio.getNr_inventario(),
                                    dettaglioAccessorio.getPg_inventario(),
                                    Long.valueOf(dettaglioAccessorio.getProgressivo())));
                    if (accessorio != null) accessoriDisponibili.add(accessorio);
                }
            }
            return accessoriDisponibili;

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    private boolean verificaSeAccessorioGiaRientrato(
            UserContext userContext,
            Doc_trasporto_rientro_dettBulk dettaglioTrasportoAccessorio)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, DocumentoRientroDettBulk.class);

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO dr");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "dr.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "dr.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "dr.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "dr.PG_DOC_TRASPORTO_RIENTRO");

            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getPg_inventario());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getNr_inventario());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getProgressivo());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getTi_documento());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getEsercizio());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO_RIF",
                    SQLBuilder.EQUALS, dettaglioTrasportoAccessorio.getPg_doc_trasporto_rientro());
            sql.addSQLClause("AND", "dr.TI_DOCUMENTO", SQLBuilder.EQUALS, RIENTRO);
            sql.addSQLClause("AND", "dr.STATO", SQLBuilder.NOT_EQUALS, STATO_ANNULLATO);

            List risultati = dettHome.fetchAll(sql);
            return risultati != null && !risultati.isEmpty();

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /** Accessori di un bene nei dettagli già salvati. */
    public List cercaBeniAccessoriNeiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            InventarioDocTRBulk benePrincipale)
            throws ComponentException {

        if (doc == null || doc.getPgDocTrasportoRientro() == null) return Collections.emptyList();

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, doc.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, doc.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    doc.getPgDocTrasportoRientro());
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    benePrincipale.getNr_inventario());
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    benePrincipale.getProgressivo());

            List dettagli = dettHome.fetchAll(sql);

            if (dettagli != null && !dettagli.isEmpty()) {
                List<InventarioDocTRBulk> beniAccessori = new ArrayList<>();
                InventarioDocTRHome beneHome =
                        (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);

                for (Iterator it = dettagli.iterator(); it.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) it.next();
                    InventarioDocTRBulk bene = (InventarioDocTRBulk) beneHome.findByPrimaryKey(
                            new InventarioDocTRBulk(
                                    dett.getNr_inventario(),
                                    dett.getPg_inventario(),
                                    Long.valueOf(dett.getProgressivo())));
                    if (bene != null) beniAccessori.add(bene);
                }
                return beniAccessori;
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------- CERCA BENI / FILTRI

    public RemoteIterator cercaBeniTrasportabili(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, true);
    }

    public RemoteIterator cercaBeniDaFarRientrare(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, false);
    }

    public RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniDaFarRientrare(userContext, doc, beniEsclusi, clauses);
    }

    /** Ricerca unificata dei beni trasportabili (true) o da far rientrare (false). */
    public RemoteIterator cercaBeniUnificato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses,
            boolean isTrasporto)
            throws ComponentException {

        try {
            InventarioDocTRHome invHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);
            SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);
            sql.setDistinctClause(true);

            if (isTrasporto) {
                applicaFiltriTrasporto(sql, doc, userContext);
            } else {
                applicaFiltriRientro(sql, doc);
            }

            // Esclude i beni in documenti aperti (INS/INV)
            escludiBeniInDocumentiAperti(sql, doc);

            if (beniEsclusi != null && !beniEsclusi.isEmpty()) {
                escludiBeniGiaSelezionati(sql, doc, beniEsclusi);
            }

            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            return iterator(userContext, sql, InventarioDocTRBulk.class, null);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /** Filtri per il TRASPORTO. */
    private void applicaFiltriTrasporto(SQLBuilder sql, Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws ComponentException, PersistencyException, IntrospectionException {

        if (doc.isSmartworking() &&
                doc.getTerzoSmartworking() != null &&
                doc.getTerzoSmartworking().getCd_terzo() != null) {
            sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.CD_ASSEGNATARIO",
                    SQLBuilder.EQUALS, doc.getTerzoSmartworking().getCd_terzo());
        }

        sql.openParenthesis(FindClause.AND);

        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.ISNULL, null);
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "Y");
        sql.closeParenthesis();

        sql.openParenthesis(FindClause.OR);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, TRASPORTO);
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS, STATO_ANNULLATO);
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "Y");
        sql.closeParenthesis();

        sql.openParenthesis(FindClause.OR);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, RIENTRO);
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS, STATO_DEFINITIVO);
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "Y");
        sql.closeParenthesis();

        sql.closeParenthesis();

        if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
            sql.openParenthesis(FindClause.AND);
            sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.ISNULL, null);
            sql.openParenthesis(FindClause.OR);
            sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO",
                    SQLBuilder.NOT_EQUALS, doc.getPgDocTrasportoRientro());
            sql.addSQLClause(FindClause.OR, "t.ESERCIZIO",
                    SQLBuilder.NOT_EQUALS, doc.getEsercizio());
            sql.closeParenthesis();
            sql.closeParenthesis();
        }
    }

    /** Filtri per il RIENTRO. */
    private void applicaFiltriRientro(SQLBuilder sql, Doc_trasporto_rientroBulk doc)
            throws IntrospectionException {

        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "N");
        sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.ISNOTNULL, null);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, TRASPORTO);
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS, STATO_DEFINITIVO);

        StringBuilder notExistsClause = new StringBuilder();
        notExistsClause.append("NOT EXISTS (");
        notExistsClause.append("SELECT 1 FROM DOC_TRASPORTO_RIENTRO_DETT dett_r, DOC_TRASPORTO_RIENTRO doc_r ");
        notExistsClause.append("WHERE dett_r.PG_INVENTARIO = doc_r.PG_INVENTARIO ");
        notExistsClause.append("AND dett_r.TI_DOCUMENTO = doc_r.TI_DOCUMENTO ");
        notExistsClause.append("AND dett_r.ESERCIZIO = doc_r.ESERCIZIO ");
        notExistsClause.append("AND dett_r.PG_DOC_TRASPORTO_RIENTRO = doc_r.PG_DOC_TRASPORTO_RIENTRO ");
        notExistsClause.append("AND dett_r.PG_INVENTARIO = INVENTARIO_BENI.PG_INVENTARIO ");
        notExistsClause.append("AND dett_r.NR_INVENTARIO = INVENTARIO_BENI.NR_INVENTARIO ");
        notExistsClause.append("AND dett_r.PROGRESSIVO = INVENTARIO_BENI.PROGRESSIVO ");
        notExistsClause.append("AND doc_r.TI_DOCUMENTO = '").append(RIENTRO).append("' ");
        notExistsClause.append("AND doc_r.STATO <> '").append(STATO_ANNULLATO).append("' ");

        if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
            notExistsClause.append("AND (doc_r.PG_DOC_TRASPORTO_RIENTRO <> ")
                    .append(doc.getPgDocTrasportoRientro())
                    .append(" OR doc_r.ESERCIZIO <> ")
                    .append(doc.getEsercizio())
                    .append(") ");
        }
        notExistsClause.append(")");

        sql.addSQLClause("AND", notExistsClause.toString());
    }

    private void escludiBeniGiaSelezionati(
            SQLBuilder sql, Doc_trasporto_rientroBulk doc, SimpleBulkList beni_da_escludere) {

        if (beni_da_escludere == null || beni_da_escludere.isEmpty()) return;

        StringBuilder exclusionList = new StringBuilder();
        for (Object obj : beni_da_escludere) {
            InventarioDocTRBulk bene = (InventarioDocTRBulk) obj;
            if (bene.getNr_inventario() != null && bene.getProgressivo() != null) {
                if (exclusionList.length() > 0) exclusionList.append(",");
                exclusionList.append("(")
                        .append(bene.getNr_inventario())
                        .append(",")
                        .append(bene.getProgressivo())
                        .append(")");
            }
        }

        if (exclusionList.length() > 0) {
            sql.addSQLClause("AND",
                    "(INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO) NOT IN (" +
                            exclusionList + ")");
        }
    }

    /** Esclude i beni presenti in altri documenti T/R INS o INV (il corrente è escluso se ha PG positivo). */
    private void escludiBeniInDocumentiAperti(SQLBuilder sql, Doc_trasporto_rientroBulk doc) {

        StringBuilder clause = new StringBuilder();

        clause.append("NOT EXISTS (");
        clause.append("SELECT 1 ");
        clause.append("FROM DOC_TRASPORTO_RIENTRO_DETT dett_x, DOC_TRASPORTO_RIENTRO doc_x ");
        clause.append("WHERE dett_x.PG_INVENTARIO = doc_x.PG_INVENTARIO ");
        clause.append("AND dett_x.TI_DOCUMENTO = doc_x.TI_DOCUMENTO ");
        clause.append("AND dett_x.ESERCIZIO = doc_x.ESERCIZIO ");
        clause.append("AND dett_x.PG_DOC_TRASPORTO_RIENTRO = doc_x.PG_DOC_TRASPORTO_RIENTRO ");
        clause.append("AND dett_x.PG_INVENTARIO = INVENTARIO_BENI.PG_INVENTARIO ");
        clause.append("AND dett_x.NR_INVENTARIO = INVENTARIO_BENI.NR_INVENTARIO ");
        clause.append("AND dett_x.PROGRESSIVO = INVENTARIO_BENI.PROGRESSIVO ");
        clause.append("AND doc_x.STATO IN ('")
                .append(STATO_INSERITO)
                .append("','")
                .append(STATO_INVIATO)
                .append("') ");

        if (doc != null && doc.hasChiaveDocumentoCompleta()) {
            clause.append("AND NOT (");
            clause.append("dett_x.PG_INVENTARIO = ").append(doc.getPgInventario()).append(" ");
            clause.append("AND dett_x.TI_DOCUMENTO = '").append(doc.getTiDocumento()).append("' ");
            clause.append("AND dett_x.ESERCIZIO = ").append(doc.getEsercizio()).append(" ");
            clause.append("AND dett_x.PG_DOC_TRASPORTO_RIENTRO = ")
                    .append(doc.getPgDocTrasportoRientro()).append(" ");
            clause.append(") ");
        }

        clause.append(")");

        sql.addSQLClause("AND", clause.toString());
    }

    // ---------------------------------------------------------------- ELIMINAZIONE DETTAGLI

    /** Elimina tutti i dettagli salvati, gestendo i riferimenti incrociati. */
    public void eliminaTuttiDettagliSalvati(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, doc.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, doc.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    doc.getPgDocTrasportoRientro());

            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare = dettHome.fetchAll(sql);
            if (dettagliDaEliminare == null || dettagliDaEliminare.isEmpty()) return;

            eliminaDettagli(userContext, doc, dettagliDaEliminare);

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /** Elimina i dettagli salvati relativi ai beni indicati. */
    public void eliminaDettagliSalvati(
            UserContext userContext, Doc_trasporto_rientroBulk doc, OggettoBulk[] beni)
            throws ComponentException {

        if (beni == null || beni.length == 0) return;

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare = new ArrayList<>();

            for (OggettoBulk o : beni) {
                InventarioDocTRBulk bene = (InventarioDocTRBulk) o;
                SQLBuilder sql = dettHome.createSQLBuilder();
                sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
                sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, doc.getTiDocumento());
                sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, doc.getEsercizio());
                sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                        doc.getPgDocTrasportoRientro());
                sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
                sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());

                List<Doc_trasporto_rientro_dettBulk> trovati = dettHome.fetchAll(sql);
                if (trovati != null && !trovati.isEmpty()) dettagliDaEliminare.addAll(trovati);
            }

            if (dettagliDaEliminare.isEmpty()) return;

            eliminaDettagli(userContext, doc, dettagliDaEliminare);

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    private void eliminaDettagli(UserContext userContext, Doc_trasporto_rientroBulk doc,
                                 List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare)
            throws ComponentException {

        if (doc instanceof DocumentoRientroBulk) {
            rimuoviRiferimentiTrasportoARientro(userContext, dettagliDaEliminare);
        }

        for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
            dettaglio.setToBeDeleted();
            super.eliminaConBulk(userContext, dettaglio);
        }
    }

    /** Elimina via SQL il bene principale e i suoi accessori dai dettagli salvati. */
    public void eliminaBeniPrincipaleConAccessoriDaDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            InventarioDocTRBulk benePrincipale,
            List beniAccessori)
            throws ComponentException {

        if (doc == null || doc.getPgDocTrasportoRientro() == null)
            throw new ComponentException("Documento non valido per eliminazione dettagli");

        PreparedStatement pstmt = null;

        try {
            StringBuilder inClause = new StringBuilder();
            inClause.append("(")
                    .append(benePrincipale.getNr_inventario())
                    .append(",")
                    .append(benePrincipale.getProgressivo())
                    .append(")");

            if (beniAccessori != null && !beniAccessori.isEmpty()) {
                for (Iterator it = beniAccessori.iterator(); it.hasNext(); ) {
                    InventarioDocTRBulk accessorio = (InventarioDocTRBulk) it.next();
                    inClause.append(",(")
                            .append(accessorio.getNr_inventario())
                            .append(",")
                            .append(accessorio.getProgressivo())
                            .append(")");
                }
            }

            String deleteSQL =
                    "DELETE FROM DOC_TRASPORTO_RIENTRO_DETT " +
                            "WHERE PG_INVENTARIO = ? AND TI_DOCUMENTO = ? AND ESERCIZIO = ? " +
                            "  AND PG_DOC_TRASPORTO_RIENTRO = ? " +
                            "  AND (NR_INVENTARIO, PROGRESSIVO) IN (" + inClause + ")";

            pstmt = getConnection(userContext).prepareStatement(deleteSQL);
            pstmt.setLong(1, doc.getPgInventario());
            pstmt.setString(2, doc.getTiDocumento());
            pstmt.setInt(3, doc.getEsercizio());
            pstmt.setLong(4, doc.getPgDocTrasportoRientro());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new ComponentException(
                    "Errore eliminazione principale + accessori dai dettagli: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                log.warn("Errore chiusura statement: {}", e.getMessage());
            }
        }
    }

    private void rimuoviRiferimentiTrasportoARientro(
            UserContext userContext,
            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare)
            throws ComponentException {

        try {
            for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
                if (!(dettaglio instanceof DocumentoRientroDettBulk)) continue;

                DocumentoRientroDettBulk rientro = (DocumentoRientroDettBulk) dettaglio;
                DocumentoTrasportoDettBulk trasporto = trovaTrasportoChePuntaARientro(userContext, rientro);

                if (trasporto != null) {
                    trasporto.setDocRientroDettRif(null);
                    trasporto.setToBeUpdated();
                    updateBulk(userContext, trasporto);
                }

                rientro.setDoc_trasporto_rientroDettRif(null);
                rientro.setToBeUpdated();
                updateBulk(userContext, rientro);
            }
        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore nella rimozione dei riferimenti tra trasporto e rientro: " + e.getMessage(), e);
        }
    }

    private DocumentoTrasportoDettBulk trovaTrasportoChePuntaARientro(
            UserContext userContext, DocumentoRientroDettBulk rientro)
            throws ComponentException, PersistencyException {

        DocumentoTrasportoDettHome trasportoHome =
                (DocumentoTrasportoDettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

        SQLBuilder sql = trasportoHome.createSQLBuilder();
        sql.addSQLClause("AND", "PG_INVENTARIO_RIF", SQLBuilder.EQUALS, rientro.getPg_inventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO_RIF", SQLBuilder.EQUALS, rientro.getTi_documento());
        sql.addSQLClause("AND", "ESERCIZIO_RIF", SQLBuilder.EQUALS, rientro.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO_RIF", SQLBuilder.EQUALS,
                rientro.getPg_doc_trasporto_rientro());
        sql.addSQLClause("AND", "NR_INVENTARIO_RIF", SQLBuilder.EQUALS, rientro.getNr_inventario());
        sql.addSQLClause("AND", "PROGRESSIVO_RIF", SQLBuilder.EQUALS, rientro.getProgressivo());

        List<DocumentoTrasportoDettBulk> risultati = trasportoHome.fetchAll(sql);
        return (risultati != null && !risultati.isEmpty()) ? risultati.get(0) : null;
    }

    // ---------------------------------------------------------------- SELEZIONE / INSERIMENTO MASSIVO

    /** Seleziona e inserisce tutti i beni che rispettano i filtri. */
    public void selezionaTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            List<InventarioDocTRBulk> beniFiltrati =
                    caricaBeniPerInserimento(userContext, doc, clauses, TRASPORTO.equals(doc.getTiDocumento()));

            if (beniFiltrati.isEmpty()) return;

            inserisciBeniComeDettagli(userContext, doc, beniFiltrati);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /** Query dei beni candidati all'inserimento nel documento. */
    private SQLBuilder buildSQLBeniCandidati(UserContext userContext,
                                             Doc_trasporto_rientroBulk doc,
                                             CompoundFindClause clauses) throws Exception {

        InventarioDocTRHome invHome =
                (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);

        SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            applicaFiltriTrasporto(sql, doc, userContext);
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            applicaFiltriRientro(sql, doc);
        } else {
            throw new ApplicationException(
                    "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento());
        }

        escludiBeniInDocumentiAperti(sql, doc);

        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

        SQLBuilder sqlEsclusiDett = dettHome.createSQLBuilder();
        sqlEsclusiDett.resetColumns();
        sqlEsclusiDett.addColumn("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO");
        sqlEsclusiDett.addColumn("DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO");
        sqlEsclusiDett.addColumn("DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO");

        sqlEsclusiDett.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                SQLBuilder.EQUALS, doc.getPgInventario());
        sqlEsclusiDett.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO",
                SQLBuilder.EQUALS, doc.getTiDocumento());
        sqlEsclusiDett.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO",
                SQLBuilder.EQUALS, doc.getEsercizio());
        sqlEsclusiDett.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                SQLBuilder.EQUALS, doc.getPgDocTrasportoRientro());

        sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                SQLBuilder.EQUALS, "INVENTARIO_BENI.PG_INVENTARIO");
        sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO",
                SQLBuilder.EQUALS, "INVENTARIO_BENI.NR_INVENTARIO");
        sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO",
                SQLBuilder.EQUALS, "INVENTARIO_BENI.PROGRESSIVO");

        sql.addSQLNotExistsClause("AND", sqlEsclusiDett);
        sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

        return sql;
    }

    /** Usato da selectAll: lista dei beni candidati caricata in memoria. */
    public List<InventarioDocTRBulk> caricaBeniPerInserimento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses,
            boolean ignoredIsTrasporto) throws ComponentException {
        try {
            InventarioDocTRHome invHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);
            List risultati = invHome.fetchAll(buildSQLBeniCandidati(userContext, doc, clauses));
            return risultati != null ? risultati : Collections.emptyList();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /** Beni candidati all'inserimento, filtrati dalle clausole del selezionatore. */
    public RemoteIterator selectBeniPerInserimentoByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses) throws ComponentException {
        try {
            SQLBuilder sql = buildSQLBeniCandidati(userContext, doc, clauses);
            return iterator(userContext, sql, InventarioDocTRBulk.class, null);
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /** Crea i dettagli per la lista di beni, assegnando il terzo assegnatario. */
    private void inserisciBeniComeDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            List<InventarioDocTRBulk> beniFiltrati)
            throws ComponentException {

        Integer cdTerzoAssegnatario = null;

        if (doc.isSmartworking() && doc.getTerzoSmartworking() != null) {
            cdTerzoAssegnatario = doc.getTerzoSmartworking().getCd_terzo();
        } else if (doc.getFlIncaricato() && doc.getTerzoIncRitiro() != null) {
            cdTerzoAssegnatario = doc.getTerzoIncRitiro().getCd_terzo();
        }

        boolean isTrasporto = TRASPORTO.equals(doc.getTiDocumento());

        validaBeniRientroOmogenei(userContext, doc, beniFiltrati);

        for (InventarioDocTRBulk bene : beniFiltrati) {
            Doc_trasporto_rientro_dettBulk dett =
                    isTrasporto ? new DocumentoTrasportoDettBulk() : new DocumentoRientroDettBulk();

            dett.setDoc_trasporto_rientro(doc);
            dett.setBene(bene);

            if (cdTerzoAssegnatario != null) {
                dett.setCdTerzoAssegnatario(cdTerzoAssegnatario);
            } else if (bene.getCd_assegnatario() != null) {
                dett.setCdTerzoAssegnatario(bene.getCd_assegnatario());
            }

            dett.setToBeCreated();
            super.creaConBulk(userContext, dett);
        }
    }

    // ---------------------------------------------------------------- VALIDAZIONE BENI

    /** Verifica che i beni non siano occupati in altri documenti T/R. */
    public void validaBeniNonInAltriDocumenti(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            List<InventarioDocTRBulk> beniDaValidare)
            throws ComponentException {

        if (beniDaValidare == null || beniDaValidare.isEmpty()) {
            return;
        }

        try {
            boolean isTrasporto = TRASPORTO.equals(doc.getTiDocumento());

            if (!isTrasporto && !RIENTRO.equals(doc.getTiDocumento())) {
                throw new ApplicationException(
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento());
            }

            String tipoDocDaVerificare = isTrasporto ? TRASPORTO : RIENTRO;
            String tipoDocLabel = isTrasporto ? "TRASPORTO" : "RIENTRO";

            StringBuilder inClauseBeni = new StringBuilder();
            for (int i = 0; i < beniDaValidare.size(); i++) {
                InventarioDocTRBulk bene = beniDaValidare.get(i);
                if (i > 0) inClauseBeni.append(",");
                inClauseBeni.append("(")
                        .append(bene.getNr_inventario())
                        .append(",")
                        .append(bene.getProgressivo())
                        .append(")");
            }

            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            // 1a: altri documenti INS o INV
            SQLBuilder sql1a = buildSqlBeniInAltriDoc(
                    dettHome, doc, tipoDocDaVerificare, inClauseBeni, false);

            sql1a.addSQLClause("AND",
                    "t.STATO IN ('" + STATO_INSERITO + "','" + STATO_INVIATO + "')");

            List beniInAltriAperti = dettHome.fetchAll(sql1a);

            if (beniInAltriAperti != null && !beniInAltriAperti.isEmpty()) {
                throw new ApplicationException(
                        "Impossibile procedere: alcuni beni sono già presenti in un altro documento di "
                                + tipoDocLabel + " in stato INSERITO o INVIATO IN FIRMA.");
            }

            // 1b: altri documenti DEFINITIVI
            SQLBuilder sql1b = buildSqlBeniInAltriDoc(dettHome, doc, tipoDocDaVerificare, inClauseBeni, false);
            sql1b.addSQLClause("AND", "t.STATO", SQLBuilder.EQUALS, STATO_DEFINITIVO);

            if (isTrasporto) {
                sql1b.addSQLClause("AND", notExistsRientroDefinitivo());
            }

            List beniInAltriDefinitivi = dettHome.fetchAll(sql1b);
            if (beniInAltriDefinitivi != null && !beniInAltriDefinitivi.isEmpty()) {
                throw new ApplicationException(
                        "Impossibile procedere: alcuni beni sono già presenti in un altro documento di "
                                + tipoDocLabel + " in stato DEFINITIVO.");
            }

            // 2 (solo RIENTRO): il bene deve avere un TRASPORTO definitivo senza RIENTRO definitivo
            if (!isTrasporto) {
                SQLBuilder sql2 = dettHome.createSQLBuilder();
                sql2.addTableToHeader("DOC_TRASPORTO_RIENTRO t");
                sql2.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "t.PG_INVENTARIO");
                sql2.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "t.TI_DOCUMENTO");
                sql2.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "t.ESERCIZIO");
                sql2.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                        "t.PG_DOC_TRASPORTO_RIENTRO");

                sql2.addSQLClause("AND", "t.TI_DOCUMENTO", SQLBuilder.EQUALS, TRASPORTO);
                sql2.addSQLClause("AND", "t.STATO", SQLBuilder.EQUALS, STATO_DEFINITIVO);
                sql2.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                        SQLBuilder.EQUALS, doc.getPgInventario());
                sql2.addSQLClause("AND",
                        "(DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO, DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO) IN ("
                                + inClauseBeni + ")");
                sql2.addSQLClause("AND", notExistsRientroDefinitivo());

                List beniInTrasportoDefinitivo = dettHome.fetchAll(sql2);

                List<InventarioDocTRBulk> beniTrovati = new ArrayList<>();
                if (beniInTrasportoDefinitivo != null) {
                    for (Object obj : beniInTrasportoDefinitivo) {
                        Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) obj;
                        beniTrovati.add((InventarioDocTRBulk) dett.getBene());
                    }
                }

                List<InventarioDocTRBulk> beniMancanti = new ArrayList<>();
                for (InventarioDocTRBulk bene : beniDaValidare) {
                    boolean trovato = beniTrovati.stream().anyMatch(bt -> bt.equalsByPrimaryKey(bene));
                    if (!trovato) beniMancanti.add(bene);
                }

                if (!beniMancanti.isEmpty()) {
                    StringBuilder msg = new StringBuilder();
                    for (int i = 0; i < beniMancanti.size(); i++) {
                        if (i > 0) msg.append(", ");
                        InventarioDocTRBulk bene = beniMancanti.get(i);
                        msg.append(bene.getNr_inventario()).append(".").append(bene.getProgressivo());
                    }
                    throw new ApplicationException(
                            "Impossibile procedere: alcuni beni non risultano in un TRASPORTO definitivo valido: "
                                    + msg);
                }
            }

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /** Clausola: il bene del dettaglio non ha un RIENTRO definitivo. */
    private String notExistsRientroDefinitivo() {
        return "NOT EXISTS (SELECT 1 FROM DOC_TRASPORTO_RIENTRO_DETT rd2" +
                " JOIN DOC_TRASPORTO_RIENTRO r2" +
                " ON rd2.PG_INVENTARIO = r2.PG_INVENTARIO" +
                " AND rd2.TI_DOCUMENTO = r2.TI_DOCUMENTO" +
                " AND rd2.ESERCIZIO = r2.ESERCIZIO" +
                " AND rd2.PG_DOC_TRASPORTO_RIENTRO = r2.PG_DOC_TRASPORTO_RIENTRO" +
                " WHERE r2.TI_DOCUMENTO = '" + RIENTRO + "'" +
                " AND r2.STATO = '" + STATO_DEFINITIVO + "'" +
                " AND rd2.NR_INVENTARIO = DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO" +
                " AND rd2.PROGRESSIVO = DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO)";
    }

    /** Query di verifica beni in altri documenti. */
    private SQLBuilder buildSqlBeniInAltriDoc(
            Doc_trasporto_rientro_dettHome dettHome,
            Doc_trasporto_rientroBulk doc,
            String tipoDocDaVerificare,
            StringBuilder inClauseBeni,
            boolean ignorato) {

        SQLBuilder sql = dettHome.createSQLBuilder();

        sql.addTableToHeader("DOC_TRASPORTO_RIENTRO t");

        sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "t.PG_INVENTARIO");
        sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "t.TI_DOCUMENTO");
        sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "t.ESERCIZIO");
        sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO", "t.PG_DOC_TRASPORTO_RIENTRO");

        sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                SQLBuilder.EQUALS, doc.getPgInventario());
        sql.addSQLClause("AND", "t.TI_DOCUMENTO", SQLBuilder.EQUALS, tipoDocDaVerificare);

        // Esclude il documento corrente se ha PG definitivo positivo
        if (doc != null && doc.hasChiaveDocumentoCompleta()) {
            sql.addSQLClause("AND",
                    "NOT (" +
                            "t.PG_INVENTARIO = " + doc.getPgInventario() + " " +
                            "AND t.TI_DOCUMENTO = '" + doc.getTiDocumento() + "' " +
                            "AND t.ESERCIZIO = " + doc.getEsercizio() + " " +
                            "AND t.PG_DOC_TRASPORTO_RIENTRO = " + doc.getPgDocTrasportoRientro() +
                            ")");
        }

        sql.addSQLClause("AND",
                "(DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO, DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO) IN ("
                        + inClauseBeni + ")");

        return sql;
    }

    // ---------------------------------------------------------------- DETTAGLI / RESPONSABILE

    /** Recupera tutti i dettagli del documento. */
    public BulkList getDetailsFor(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);
            BulkList dettagli = new BulkList(dettHome.getDetailsFor(doc));
            doc.setDoc_trasporto_rientro_dettColl(dettagli);
            return dettagli;
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /** Terzo responsabile dell'UO di contesto. */
    public TerzoBulk recuperaTerzoResponsabileUO(UserContext userContext) throws ComponentException {
        try {
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);
            if (cdUo == null) return null;

            Unita_organizzativaHome uoHome =
                    (Unita_organizzativaHome) getHome(userContext, Unita_organizzativaBulk.class);
            Unita_organizzativaBulk uo =
                    (Unita_organizzativaBulk) uoHome.findByPrimaryKey(new Unita_organizzativaBulk(cdUo));

            Integer cdTerzoResp = (uo != null) ? uo.getCd_responsabile() : null;
            if (cdTerzoResp == null) return null;

            TerzoHome terzoHome = (TerzoHome) getHome(userContext, TerzoBulk.class);
            return (TerzoBulk) terzoHome.findByPrimaryKey(new TerzoBulk(cdTerzoResp));

        } catch (PersistencyException e) {
            throw new ComponentException("Errore durante il recupero del responsabile UO", e);
        }
    }

    // ---------------------------------------------------------------- ESERCIZIO COEP / FIRMA

    /** Verifica se l'esercizio COEP è chiuso. */
    public boolean isEsercizioCOEPChiuso(UserContext userContext) throws ComponentException {
        LoggableStatement cs = null;
        try {
            cs = new LoggableStatement(getConnection(userContext),
                    "{ ? = call " + EJBCommonServices.getDefaultSchema() +
                            "CNRCTB200.isChiusuraCoepDef(?,?)}", false, this.getClass());
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setObject(2, CNRUserContext.getEsercizio(userContext));
            cs.setObject(3, CNRUserContext.getCd_cds(userContext));
            cs.executeQuery();
            String status = cs.getString(1);
            if ("Y".compareTo(status) == 0) return true;

            Id_inventarioHome inventarioHome =
                    (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);
            Id_inventarioBulk inventario = inventarioHome.findInventarioFor(userContext, false);
            if (!inventarioHome.isAperto(inventario, CNRUserContext.getEsercizio(userContext)))
                return true;

        } catch (SQLException | PersistencyException | IntrospectionException ex) {
            throw handleException(ex);
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException e) {
                throw handleException(e);
            }
        }
        return false;
    }

    /** Documenti in stato INVIATO predisposti alla firma (solo con PG definitivo). */
    public List getDocumentiPredispostiAllaFirma(UserContext userContext) throws ComponentException {
        List documenti = new ArrayList();

        documenti.addAll(getDocumentiPredispostiAllaFirmaPerTipo(userContext, DocumentoTrasportoBulk.class));
        documenti.addAll(getDocumentiPredispostiAllaFirmaPerTipo(userContext, DocumentoRientroBulk.class));

        return documenti;
    }

    private List getDocumentiPredispostiAllaFirmaPerTipo(UserContext userContext, Class bulkClass)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home =
                    (Doc_trasporto_rientroHome) getHome(userContext, bulkClass);

            SQLBuilder sql = home.createSQLBuilder();

            sql.addSQLClause("AND", "STATO", SQLBuilder.EQUALS, STATO_INVIATO);
            sql.addSQLClause("AND", "STATO_FLUSSO", SQLBuilder.EQUALS, "INV");
            sql.addSQLClause("AND", "UUID_FLUSSO_AUTORIZZATIVO", SQLBuilder.ISNOTNULL, null);
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.GREATER, 0L);
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.LESS_EQUALS,
                    CNRUserContext.getEsercizio(userContext));

            sql.addOrderBy("DATA_INVIO_FIRMA ASC");

            List result = home.fetchAll(sql);

            return result != null ? result : Collections.emptyList();

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    // ---------------------------------------------------------------- NOTIFICHE EMAIL

    /**
     * Email del creatore del documento (cd_utente_uid + dominio di configurazione).
     * Nessun controllo di ruolo: viene chiamato dal job con l'utente tecnico SIGN_WORKFLOW.
     * Null se non risolvibile (la notifica non viene inviata).
     */
    public String getEmailCreatore(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            UtenteBulk utente = (UtenteBulk) getHome(userContext, UtenteBulk.class)
                    .findByPrimaryKey(new UtenteBulk(doc.getUtcr()));

            if (utente == null || utente.getCd_utente_uid() == null || utente.getCd_utente_uid().isBlank()) {
                log.warn("Notifica pg={}: utente '{}' senza cd_utente_uid, nessuna email",
                        doc.getPgDocTrasportoRientro(), doc.getUtcr());
                return null;
            }

            String email = DocTraspRientUtil.costruisciEmail(utente.getCd_utente_uid(), getDominioEmail(userContext));
            if (email == null) {
                log.warn("Notifica pg={}: dominio email (PK_COSTANTI/SK_DOMINIO_EMAIL) non configurato",
                        doc.getPgDocTrasportoRientro());
            }
            return email;

        } catch (PersistencyException | RemoteException e) {
            throw handleException(e);
        }
    }

    /** Dominio email dell'esercizio di contesto, con ripiego sull'esercizio 0. */
    private String getDominioEmail(UserContext userContext) throws ComponentException, RemoteException {
        Configurazione_cnrComponentSession conf = Utility.createConfigurazioneCnrComponentSession();
        String dominio = conf.getVal01(userContext, CNRUserContext.getEsercizio(userContext), null,
                Configurazione_cnrBulk.PK_COSTANTI, Configurazione_cnrBulk.SK_DOMINIO_EMAIL);
        if (dominio == null || dominio.isBlank()) {
            dominio = conf.getVal01(userContext, 0, null,
                    Configurazione_cnrBulk.PK_COSTANTI, Configurazione_cnrBulk.SK_DOMINIO_EMAIL);
        }
        return dominio;
    }

    /** Invia la notifica tramite la mail session SIGLA (java:jboss/mail/sigla). */
    public void inviaNotifica(UserContext userContext, String subject, String body,
                              java.util.List<String> destinatari) throws ComponentException {
        log.info("Invio notifica '{}' a {}", subject, destinatari);
        SendMail.sendMail(subject, body, destinatari);
    }

    // ---------------------------------------------------------------- STAMPA

    public OggettoBulk stampaConBulk(UserContext aUC, OggettoBulk bulk) throws ComponentException {
        validateBulkForPrint(aUC, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }

    public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {
        if (bulk instanceof Stampa_doc_trasporto_rientroBulk)
            inizializzaBulkPerStampa(userContext, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }

    public void inizializzaBulkPerStampa(UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa)
            throws ComponentException {

        stampa.setEsercizio(CNRUserContext.getEsercizio(userContext));
        stampa.setCds_scrivania(CNRUserContext.getCd_cds(userContext));
        stampa.setPgInizio(0);
        stampa.setPgFine(999999999);

        try {
            String cd_uo_scrivania = CNRUserContext.getCd_unita_organizzativa(userContext);
            Unita_organizzativaHome uoHome =
                    (Unita_organizzativaHome) getHome(userContext, Unita_organizzativaBulk.class);
            Unita_organizzativaBulk uo =
                    (Unita_organizzativaBulk) uoHome.findByPrimaryKey(
                            new Unita_organizzativaBulk(cd_uo_scrivania));

            if (!uo.isUoCds()) {
                stampa.setUoForPrint(uo);
                stampa.setIsUOForPrintEnabled(false);
            } else {
                stampa.setUoForPrint(new Unita_organizzativaBulk());
                stampa.setIsUOForPrintEnabled(true);
            }
        } catch (PersistencyException pe) {
            throw new ComponentException(pe);
        }
    }

    private void validateBulkForPrint(UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa)
            throws ComponentException {
        try {
            if (stampa.getPgInizio() == null)
                throw new ValidationException("Il campo NUMERO INIZIO è obbligatorio");
            if (stampa.getPgFine() == null)
                throw new ValidationException("Il campo NUMERO FINE è obbligatorio");
            if (stampa.getPgInizio().compareTo(stampa.getPgFine()) > 0)
                throw new ValidationException("Il NUMERO INIZIO non può essere superiore al NUMERO FINE");
            if (stampa.getUoForPrint().getCd_unita_organizzativa() == null)
                throw new ValidationException("Il campo Unità Organizzativa è obbligatorio");
        } catch (ValidationException ex) {
            throw new ApplicationException(ex);
        }
    }

    // ---------------------------------------------------------------- WEB SERVICE

    /** Salva un documento T/R da WS: normalizza, costruisce i dettagli, valida e assegna il PG definitivo. */
    public Doc_trasporto_rientroBulk saveDocFromWS(
            UserContext userContext, Doc_trasporto_rientroBulk bulk) throws ComponentException {

        try {
            Doc_trasporto_rientroBulk doc = normalizzaBulkWS(bulk);
            applicaCampiDaRichiestaWS(doc, userContext);

            BulkList<Doc_trasporto_rientro_dettBulk> dettagliTemp = new BulkList<>();
            if (bulk.getDoc_trasporto_rientro_dettColl() != null) {
                dettagliTemp.addAll(bulk.getDoc_trasporto_rientro_dettColl());
            }

            doc.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
            doc = (Doc_trasporto_rientroBulk) creaConBulk(userContext, doc);

            final boolean hasPgTemporaneo =
                    doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() < 0;

            List<Doc_trasporto_rientro_dettBulk> dettagliCostruiti =
                    costruisciDettagliBeniWS(dettagliTemp, doc);

            validaAllegatiComp(doc);

            if (!dettagliCostruiti.isEmpty()) {
                List<InventarioDocTRBulk> beniDaValidare = dettagliCostruiti.stream()
                        .map(d -> (InventarioDocTRBulk) d.getBene())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                validaBeniNonInAltriDocumenti(userContext, doc, beniDaValidare);
                setDettBeniFromWS(userContext, doc, dettagliCostruiti);
            }

            validaDoc(userContext, doc, true);

            if (hasPgTemporaneo) {
                confermaProgTempToDef(userContext, doc, dettagliCostruiti);
            }

            return doc;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException("Errore durante saveDocFromWS: " + e.getMessage(), e);
        }
    }

    private Doc_trasporto_rientroBulk normalizzaBulkWS(Doc_trasporto_rientroBulk bulk) {
        if (TRASPORTO.equals(bulk.getTiDocumento())) {
            if (bulk instanceof DocumentoTrasportoBulk) return bulk;
            DocumentoTrasportoBulk d = new DocumentoTrasportoBulk();
            copiaCampiBaseWS(bulk, d);
            return d;
        } else {
            if (bulk instanceof DocumentoRientroBulk) return bulk;
            DocumentoRientroBulk d = new DocumentoRientroBulk();
            copiaCampiBaseWS(bulk, d);
            return d;
        }
    }

    private void copiaCampiBaseWS(Doc_trasporto_rientroBulk src, Doc_trasporto_rientroBulk dst) {
        dst.setTiDocumento(src.getTiDocumento());
        dst.setEsercizio(src.getEsercizio());
        dst.setDsDocTrasportoRientro(src.getDsDocTrasportoRientro());
        dst.setCdTipoTrasportoRientro(src.getCdTipoTrasportoRientro());
        dst.setPgInventario(src.getPgInventario());
    }

    /** Applica i default di framework e ripristina i valori ricevuti dal WS. */
    private void applicaCampiDaRichiestaWS(
            Doc_trasporto_rientroBulk src, UserContext userContext) throws ComponentException {

        try {
            Doc_trasporto_rientroBulk dst =
                    (Doc_trasporto_rientroBulk) super.inizializzaBulkPerInserimento(userContext, src);

            if (dst.getTiDocumento() == null || dst.getTiDocumento().isEmpty())
                throw new ComponentException("Tipo documento (T/R) non impostato dopo inizializzazione.");

            inizializzaTipoMovimento(userContext, dst);

            if (dst.getDoc_trasporto_rientro_dettColl() == null)
                dst.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());

            dst.setCds_scrivania(CNRUserContext.getCd_cds(userContext));
            dst.setUo_scrivania(CNRUserContext.getCd_unita_organizzativa(userContext));
            dst.setEsercizio(CNRUserContext.getEsercizio(userContext));

            dst.setCondizioni(getHome(userContext, Condizione_beneBulk.class).findAll());
            dst.setInventario(caricaInventario(userContext));

            Id_inventarioHome invHome = (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);
            dst.setConsegnatario(invHome.findConsegnatarioFor(dst.getInventario()));
            dst.setDelegato(invHome.findDelegatoFor(dst.getInventario()));
            dst.setUo_consegnataria(invHome.findUoRespFor(userContext, dst.getInventario()));

            dst.setDataRegistrazione(new Timestamp(System.currentTimeMillis()));

            dst.setDsDocTrasportoRientro(Optional.ofNullable(src.getDsDocTrasportoRientro()).orElse(""));
            dst.setCdTipoTrasportoRientro(src.getCdTipoTrasportoRientro());

            if (dst.getTipoMovimento() == null && src.getCdTipoTrasportoRientro() != null)
                dst.setTipoMovimento(new Tipo_trasporto_rientroBulk(src.getCdTipoTrasportoRientro()));

            Optional.ofNullable(src.getDataRegistrazione()).ifPresent(dst::setDataRegistrazione);
            Optional.ofNullable(src.getDestinazione()).ifPresent(dst::setDestinazione);
            Optional.ofNullable(src.getIndirizzo()).ifPresent(dst::setIndirizzo);
            Optional.ofNullable(src.getNote()).ifPresent(dst::setNote);
            Optional.ofNullable(src.getNoteRitiro()).ifPresent(dst::setNoteRitiro);
            Optional.ofNullable(src.getStato()).ifPresent(dst::setStato);

            dst.setFlIncaricato(Boolean.TRUE.equals(src.getFlIncaricato()));
            dst.setFlVettore(Boolean.TRUE.equals(src.getFlVettore()));

            if (Boolean.TRUE.equals(src.getFlVettore()))
                Optional.ofNullable(src.getNominativoVettore()).ifPresent(dst::setNominativoVettore);

            if (Boolean.TRUE.equals(src.getFlIncaricato()) && src.getCdTerzoIncaricato() != null) {
                TerzoBulk t = new TerzoBulk();
                t.setCd_terzo(src.getCdTerzoIncaricato());
                dst.setTerzoIncRitiro(t);
                dst.setCdTerzoIncaricato(src.getCdTerzoIncaricato());
            }

            if (src.getTerzoSmartworking() != null &&
                    src.getTerzoSmartworking().getCd_terzo() != null) {
                TerzoBulk t = new TerzoBulk();
                t.setCd_terzo(src.getTerzoSmartworking().getCd_terzo());
                dst.setTerzoSmartworking(t);
            }

            if (src.getCdTerzoResponsabile() != null)
                dst.setCdTerzoResponsabile(src.getCdTerzoResponsabile());

            Optional.ofNullable(src.getPgInventario()).ifPresent(dst::setPgInventario);
            dst.setToBeCreated();

        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }

    /** Salva i dettagli dopo aver verificato esistenza e coerenza dei beni. */
    private void setDettBeniFromWS(
            UserContext userContext,
            Doc_trasporto_rientroBulk docCreato,
            List<Doc_trasporto_rientro_dettBulk> dettagliDTO) throws ComponentException {

        try {
            if (docCreato.getDoc_trasporto_rientro_dettColl() == null)
                docCreato.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());

            boolean isTrasporto = docCreato instanceof DocumentoTrasportoBulk;
            InventarioDocTRHome beneHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);

            for (Doc_trasporto_rientro_dettBulk dett : dettagliDTO) {
                Long pgInventario = dett.getPg_inventario() != null
                        ? dett.getPg_inventario()
                        : docCreato.getPgInventario();

                InventarioDocTRBulk bene = (InventarioDocTRBulk) beneHome.findByPrimaryKey(
                        new InventarioDocTRBulk(dett.getNr_inventario(), pgInventario,
                                Long.valueOf(dett.getProgressivo())));

                if (bene == null)
                    throw new ApplicationException(
                            "Bene non trovato: nr_inventario=" + dett.getNr_inventario() +
                                    ", progressivo=" + dett.getProgressivo() +
                                    ", pg_inventario=" + pgInventario);

                if (isTrasporto && !Boolean.TRUE.equals(bene.getFl_bene_in_ist()))
                    throw new ApplicationException(
                            "Impossibile trasportare il bene " + bene.getNumeroBeneCompleto());

                if (!isTrasporto && Boolean.TRUE.equals(bene.getFl_bene_in_ist()))
                    throw new ApplicationException(
                            "Impossibile far rientrare il bene " + bene.getNumeroBeneCompleto());

                dett.setDoc_trasporto_rientro(docCreato);
                dett.setPg_inventario(docCreato.getPgInventario());
                dett.setTi_documento(docCreato.getTiDocumento());
                dett.setEsercizio(docCreato.getEsercizio());
                dett.setPg_doc_trasporto_rientro(docCreato.getPgDocTrasportoRientro());
                dett.setToBeCreated();

                super.creaConBulk(userContext, dett);
                docCreato.getDoc_trasporto_rientro_dettColl().add(dett);
            }
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    /** Converte il documento temporaneo in definitivo nel contesto WS. */
    private void confermaProgTempToDef(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            List<Doc_trasporto_rientro_dettBulk> dettagliCostruiti) throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null || docT.getPgDocTrasportoRientro() >= 0)
            throw new ApplicationException(
                    "Documento già definitivo: " + docT.getPgDocTrasportoRientro());

        try {
            Numeratore_doc_t_rHome numHome =
                    (Numeratore_doc_t_rHome) getHome(userContext, Numeratore_doc_t_rBulk.class);
            Long nuovoPgDoc = numHome.getNextPg(
                    userContext, docT.getEsercizio(), docT.getPgInventario(),
                    docT.getTiDocumento(), userContext.getUser());

            Doc_trasporto_rientroHome home = (Doc_trasporto_rientroHome) getHome(userContext, docT);
            home.confirmDocTrasportoRientroTemporaneo(userContext, docT, nuovoPgDoc);

            docT.setPgDocTrasportoRientro(nuovoPgDoc);
            for (Doc_trasporto_rientro_dettBulk d : dettagliCostruiti) {
                d.setPg_doc_trasporto_rientro(nuovoPgDoc);
            }

            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, docT);
            docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docT)));

        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    /** Costruisce i dettagli tipizzati a partire dai DTO WS. */
    private List<Doc_trasporto_rientro_dettBulk> costruisciDettagliBeniWS(
            BulkList<Doc_trasporto_rientro_dettBulk> dettagliTemp,
            Doc_trasporto_rientroBulk doc) {

        if (dettagliTemp == null || dettagliTemp.isEmpty()) return Collections.emptyList();

        boolean isTrasporto = TRASPORTO.equals(doc.getTiDocumento());
        List<Doc_trasporto_rientro_dettBulk> result = new ArrayList<>();

        for (Doc_trasporto_rientro_dettBulk srcDett : dettagliTemp) {
            Doc_trasporto_rientro_dettBulk dett;

            if (isTrasporto) {
                dett = new DocumentoTrasportoDettBulk(
                        srcDett.getPg_inventario(), doc.getTiDocumento(),
                        doc.getEsercizio(), doc.getPgDocTrasportoRientro(),
                        srcDett.getNr_inventario(), srcDett.getProgressivo());
            } else {
                DocumentoRientroDettBulk rr = new DocumentoRientroDettBulk(
                        srcDett.getPg_inventario(), doc.getTiDocumento(),
                        doc.getEsercizio(), doc.getPgDocTrasportoRientro(),
                        srcDett.getNr_inventario(), srcDett.getProgressivo());
                if (srcDett instanceof DocumentoRientroDettBulk) {
                    DocumentoRientroDettBulk srcR = (DocumentoRientroDettBulk) srcDett;
                    if (srcR.getDocTrasportoDettRif() != null)
                        rr.setDocTrasportoDettRif(srcR.getDocTrasportoDettRif());
                }
                dett = rr;
            }

            dett.setCdTerzoAssegnatario(srcDett.getCdTerzoAssegnatario());
            dett.setCat_voce(srcDett.getCat_voce());
            dett.setFl_bene_accessorio(srcDett.getFl_bene_accessorio());
            dett.setFl_accessorio_contestuale(srcDett.getFl_accessorio_contestuale());

            if (srcDett.getDoc_trasporto_rientroDettRif() != null)
                dett.setDoc_trasporto_rientroDettRif(srcDett.getDoc_trasporto_rientroDettRif());

            dett.setDoc_trasporto_rientro(doc);

            if (dett.getBene() == null) {
                dett.setBene(new InventarioDocTRBulk(
                        dett.getNr_inventario(),
                        dett.getPg_inventario(),
                        Long.valueOf(dett.getProgressivo())));
            }

            result.add(dett);
        }

        return result;
    }

    // ---------------------------------------------------------------- RICERCA DOCUMENTI

    /** Documenti T/R che contengono il bene, per stato ed esercizio, con dettagli e allegati. */
    public List<Doc_trasporto_rientroBulk> cercaDocumentiPerBene(
            UserContext userContext,
            String tiDocumento,
            String stato,
            String nrInventario,
            Integer esercizio)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home;

            if (TRASPORTO.equals(tiDocumento)) {
                home = (Doc_trasporto_rientroHome) getHome(userContext, DocumentoTrasportoBulk.class);
            } else if (RIENTRO.equals(tiDocumento)) {
                home = (Doc_trasporto_rientroHome) getHome(userContext, DocumentoRientroBulk.class);
            } else {
                home = (Doc_trasporto_rientroHome) getHome(userContext, Doc_trasporto_rientroBulk.class);
            }

            SQLBuilder sql = home.createSQLBuilder();
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett");

            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.PG_INVENTARIO", "dett.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO", "dett.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.ESERCIZIO", "dett.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO", "dett.PG_DOC_TRASPORTO_RIENTRO");

            if (tiDocumento != null) {
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO", SQLBuilder.EQUALS, tiDocumento);
            }
            if (stato != null) {
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.STATO", SQLBuilder.EQUALS, stato);
            }
            if (esercizio != null) {
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.ESERCIZIO", SQLBuilder.EQUALS, esercizio);
            }
            if (nrInventario != null) {
                sql.addSQLClause("AND", "dett.NR_INVENTARIO", SQLBuilder.EQUALS, nrInventario);
            }
            sql.setDistinctClause(true);
            sql.addOrderBy("DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO DESC");

            List<Doc_trasporto_rientroBulk> docs = home.fetchAll(sql);

            if (docs == null || docs.isEmpty()) {
                return Collections.emptyList();
            }

            List<Doc_trasporto_rientroBulk> risultato = new ArrayList<>(docs.size());
            for (Doc_trasporto_rientroBulk documento : docs) {
                getDetailsFor(userContext, documento);
                caricaAllegatiDocumento(userContext, documento);
                risultato.add(documento);
            }

            return risultato;

        } catch (PersistencyException e) {
            throw handleException(e);
        } catch (Exception e) {
            throw new ComponentException("Errore caricamento documenti completi", e);
        }
    }

    /** Carica gli allegati dal documentale. */
    public void caricaAllegatiDocumento(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ApplicationException {
        if (doc == null) return;

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        String path = getStorePathDocTrasportoRientro(doc);
        StorageObject folder = storeService.getStorageObjectByPath(path);

        if (folder == null) {
            doc.setArchivioAllegati(new BulkList<>());
            return;
        }

        BulkList<AllegatoGenericoBulk> archivio = new BulkList<>();
        for (StorageObject storageObject : storeService.getChildren(folder.getKey())) {
            AllegatoDocTraspRientroBulk allegato;

            if (Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())) {
                allegato = new AllegatoDocumentoTrasportoBulk(storageObject.getKey());
            } else {
                allegato = new AllegatoDocumentoRientroBulk(storageObject.getKey());
            }

            allegato.setNome(storageObject.getPropertyValue(StoragePropertyNames.NAME.value()));
            allegato.setDescrizione(storageObject.getPropertyValue(StoragePropertyNames.DESCRIPTION.value()));
            allegato.setTitolo(storageObject.getPropertyValue(StoragePropertyNames.TITLE.value()));
            allegato.setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
            List<String> secondaryTypes = storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");
            String aspect = DocTraspRientUtil.aspectDaStorage(allegato, secondaryTypes);
            if (aspect != null) {
                allegato.setAspectName(aspect);
            }
            allegato.setCrudStatus(OggettoBulk.NORMAL);
            archivio.add(allegato);
        }
        doc.setArchivioAllegati(archivio);
    }

    /** Ricerca per chiave primaria scegliendo la home in base a tiDocumento. */
    public Doc_trasporto_rientroBulk findDocTrasportoRientro(
            UserContext uc,
            Doc_trasporto_rientroBulk chiave) throws ComponentException {

        try {
            Doc_trasporto_rientroBulk docTrovato;
            if (TRASPORTO.equals(chiave.getTiDocumento())) {
                DocumentoTrasportoHome home = (DocumentoTrasportoHome) getHome(uc, DocumentoTrasportoBulk.class);
                docTrovato = home.findDocTrasportoRientro(chiave);
            } else if (RIENTRO.equals(chiave.getTiDocumento())) {
                DocumentoRientroHome home = (DocumentoRientroHome) getHome(uc, DocumentoRientroBulk.class);
                docTrovato = home.findDocTrasportoRientro(chiave);
            } else {
                throw new ApplicationException(
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + chiave.getTiDocumento());
            }

            if (docTrovato != null) {
                getDetailsFor(uc, docTrovato);
                caricaAllegatiDocumento(uc, docTrovato);
            }

            return docTrovato;

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }

    // ---------------------------------------------------------------- VALIDAZIONE DOCUMENTO

    /** Campi obbligatori e coerenza della data con le modifiche dei beni. */
    private void validaDoc(UserContext aUC, Doc_trasporto_rientroBulk docT, boolean validaBeni)
            throws ComponentException {
        try {
            if (docT.getTipoMovimento() == null)
                throw new ApplicationException("Attenzione: specificare un Tipo di Movimento");

            if (docT.getDsDocTrasportoRientro() == null)
                throw new ApplicationException("Attenzione: indicare una Descrizione");

            if (!validaBeni) return;

            if (docT.getDoc_trasporto_rientro_dettColl() == null ||
                    docT.getDoc_trasporto_rientro_dettColl().isEmpty())
                throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");

            boolean hasBeniSalvati = docT.getDoc_trasporto_rientro_dettColl()
                    .stream()
                    .anyMatch(o -> ((Doc_trasporto_rientro_dettBulk) o)
                            .getCrudStatus() == OggettoBulk.NORMAL);

            if (hasBeniSalvati &&
                    docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                String tipoDoc = DocTraspRientUtil.descrizioneTipo(docT);
                throw new ApplicationException(
                        "Attenzione: data del " + tipoDoc + " non valida.\n" +
                                "La Data del " + tipoDoc + " non può essere precedente " +
                                "ad una modifica di uno dei beni movimentati.");
            }

        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    private Timestamp getMaxDataFor(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null)
            throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(
                            userContext, Doc_trasporto_rientro_dettBulk.class);
            Timestamp max_data = dettHome.getMaxDataValiditaVariazione(docT);
            if (max_data == null)
                throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");
            return max_data;
        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    // ---------------------------------------------------------------- INVIO IN FIRMA

    /** Invia il documento al flusso di firma: valida, genera il PDF, avvia il flusso e archivia il PDF da firmare. */
    public Doc_trasporto_rientroBulk inviaDocumentoAllaFirma(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            if (doc == null) {
                throw new ApplicationException("Documento non presente");
            }

            if (!STATO_INSERITO.equals(doc.getStato())) {
                throw new ApplicationException(
                        "Il documento deve essere in stato INSERITO per essere inviato in firma.");
            }

            if (doc.getPgDocTrasportoRientro() == null) {
                throw new ApplicationException("Documento non ancora salvato.");
            }

            caricaDettagliDocumento(userContext, doc);

            if (doc.getDoc_trasporto_rientro_dettColl() == null
                    || doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                throw new ApplicationException("Il documento deve contenere almeno un bene.");
            }

            validaDoc(userContext, doc, true);

            byte[] pdfBytes = generaPdfDocTrasportoRientro(userContext, doc);

            DocTraspRientUtil.validaPdf(pdfBytes, "PDF documento Trasporto/Rientro da inviare in firma");

            String nomeFileDaFirmare = DocTraspRientUtil.nomeFileDaFirmare(doc);

            SignatureService service = getSignatureService();

            validaBeniRientroOmogenei(userContext, doc, Collections.emptyList());
            List<String> codiciFiscali = DocTraspRientFirmaTestService.applicaFirmatari(getCodiciFiscaliFirmatari(userContext, doc));

            SignatureDocument fileDaFirmare = new SignatureDocument(
                    nomeFileDaFirmare,
                    "application/pdf",
                    new ByteArrayInputStream(pdfBytes));

            List<String> emailFirmatari = convertiCodiciFiscaliInEmail(codiciFiscali);

            SignatureRequest request = new SignatureRequest(
                    List.of(fileDaFirmare),
                    "Firma documento " + DocTraspRientUtil.descrizioneTipo(doc),
                    DocTraspRientUtil.descrizioneAllegato(doc, "da firmare"),
                    emailFirmatari,
                    List.of(),
                    List.of(),
                    userContext != null ? userContext.getUser() : null,
                    codiciFiscali,
                    getTemplateFirme());

            String uuidFlussoAut = service.sendToSign(request);

            if (uuidFlussoAut == null || uuidFlussoAut.trim().isEmpty()) {
                throw new ApplicationException("Il flusso autorizzativo non ha restituito UUID documento.");
            }

            // Il file _DAFIRMARE viene archiviato solo se il flusso è stato accettato
            salvaPdfDaFirmareSuDocumentale(userContext, doc, pdfBytes, nomeFileDaFirmare);

            doc.setUuidFlussoAutorizzativo(uuidFlussoAut.trim());
            doc.inizializzaPerInvioFirma();
            doc.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException("Errore invio documento in firma: " + e.getMessage(), e);
        }
    }

    private SignatureService getSignatureService() throws ApplicationException {
        if (signatureService != null) {
            return signatureService;
        }

        try {
            signatureService = SpringUtil.getBean(SignatureService.class);

            if (signatureService == null) {
                throw new ApplicationException("Bean SignatureService non trovato nel contesto Spring.");
            }

            return signatureService;

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException("Workflow firma digitale non disponibile!");
        }
    }

    private AceService getAceService() throws ApplicationException {
        if (aceService == null) {
            try {
                aceService = SpringUtil.getBean(AceService.class);
            } catch (Exception e) {
                throw new ApplicationException("Servizio ACE non disponibile!");
            }
        }
        return aceService;
    }

    /** Converte i CF dei firmatari nelle email (contatto "Mail") recuperate da ACE. */
    private List<String> convertiCodiciFiscaliInEmail(List<String> codiciFiscali) throws ApplicationException {
        AceService ace = getAceService();
        List<String> emails = new ArrayList<>();

        for (String codiceFiscale : codiciFiscali) {
            String cf = codiceFiscale == null ? "" : codiceFiscale.trim().toUpperCase();
            if (cf.isEmpty()) {
                throw new ApplicationException("Codice fiscale firmatario non valorizzato.");
            }

            String email = null;
            try {
                EmployeeDetails persona = ace.getPersonaByCodiceFiscale(cf);
                if (persona != null && persona.getContatti() != null) {
                    for (Contatto contatto : persona.getContatti()) {
                        if (contatto != null
                                && "Mail".equalsIgnoreCase(contatto.getTipoContatto())
                                && contatto.getValore() != null
                                && !contatto.getValore().trim().isEmpty()) {
                            email = contatto.getValore().trim().toLowerCase();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw new ApplicationException(
                        "Errore recupero email firmatario da ACE per il CF " + cf + ": " + e.getMessage());
            }

            if (email == null) {
                throw new ApplicationException("ACE non ha restituito alcuna email per il CF " + cf);
            }
            emails.add(email);
        }
        return emails;
    }

    /** Template di firma di default (flows.templateFirma.default). */
    private String getTemplateFirme() {
        return SpringUtil.getBean(Environment.class).getProperty("flows.templateFirma.default");
    }

    private List<String> getCodiciFiscaliFirmatari(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ApplicationException, ComponentException, PersistencyException {

        Set<String> codiciFiscali = new LinkedHashSet<>();

        // Il responsabile non è ricaricato in inizializzaBulkPerModifica: lo recupero dal codice terzo
        TerzoBulk responsabile = doc.getTerzoRespDip();
        if (responsabile == null && doc.getCdTerzoResponsabile() != null) {
            responsabile = (TerzoBulk) getHome(userContext, TerzoBulk.class)
                    .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoResponsabile()));
        }
        aggiungiCodiceFiscaleFirmatario(userContext, codiciFiscali, responsabile, "responsabile");

        // Rientro: i beni trasportati in Smartworking sono firmati dal loro assegnatario
        boolean beniNonSmartworking = true;
        if (doc instanceof DocumentoRientroBulk) {
            Set<Integer> assegnatariSmartworking = new LinkedHashSet<>();
            beniNonSmartworking = false;

            for (Object o : doc.getDoc_trasporto_rientro_dettColl()) {
                Doc_trasporto_rientro_dettBulk dettRientro = (Doc_trasporto_rientro_dettBulk) o;
                Doc_trasporto_rientro_dettBulk dettTrasporto = trasportoOriginaleDi(userContext, dettRientro, doc);

                if (dettTrasporto != null
                        && dettTrasporto.getCdTerzoAssegnatario() != null
                        && isTrasportatoInSmartworking(userContext, dettTrasporto)) {
                    assegnatariSmartworking.add(dettTrasporto.getCdTerzoAssegnatario());
                } else {
                    beniNonSmartworking = true;
                }
            }

            for (Integer cdTerzo : assegnatariSmartworking) {
                TerzoBulk assegnatario = (TerzoBulk) getHome(userContext, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(cdTerzo));
                aggiungiCodiceFiscaleFirmatario(userContext, codiciFiscali, assegnatario, "assegnatario smartworking");
            }
        }

        // Incaricato al ritiro: ignorato se tutti i beni del rientro erano in smartworking
        if (beniNonSmartworking) {
            if (doc.getTerzoSmartworking() != null) {
                aggiungiCodiceFiscaleFirmatario(
                        userContext, codiciFiscali, doc.getTerzoSmartworking(), "incaricato ritiro smartworking");
            } else if (doc.getTerzoIncRitiro() != null) {
                aggiungiCodiceFiscaleFirmatario(
                        userContext, codiciFiscali, doc.getTerzoIncRitiro(), "incaricato ritiro");
            }
        }

        aggiungiCodiceFiscaleFirmatario(userContext, codiciFiscali, doc.getConsegnatario(), "consegnatario");

        if (codiciFiscali.isEmpty()) {
            throw new ApplicationException(
                    "Nessun codice fiscale firmatario trovato per il documento "
                            + DocTraspRientUtil.descrizioneTipo(doc) + ".");
        }

        log.info("Codici fiscali firmatari recuperati per il documento {}: {}",
                DocTraspRientUtil.descrizioneTipo(doc) + doc.getEsercizio() + doc.getPgDocTrasportoRientro(),
                String.join(", ", codiciFiscali));

        return new ArrayList<>(codiciFiscali);
    }

    /** Dettaglio del trasporto definitivo corrispondente a un bene del rientro (null se assente). */
    private Doc_trasporto_rientro_dettBulk trasportoOriginaleDi(
            UserContext userContext, Doc_trasporto_rientro_dettBulk dettRientro, Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        InventarioDocTRBulk bene = new InventarioDocTRBulk(
                dettRientro.getNr_inventario(),
                dettRientro.getPg_inventario(),
                Long.valueOf(dettRientro.getProgressivo()));

        return trovaDettaglioOriginale(userContext, bene, doc.getPgInventario(), TRASPORTO);
    }

    /** True se il trasporto del dettaglio ha un tipo movimento Smartworking. */
    private boolean isTrasportatoInSmartworking(UserContext userContext, Doc_trasporto_rientro_dettBulk dettTrasporto)
            throws ComponentException, PersistencyException {

        DocumentoTrasportoBulk chiave = new DocumentoTrasportoBulk();
        chiave.setTiDocumento(dettTrasporto.getTi_documento());
        chiave.setEsercizio(dettTrasporto.getEsercizio());
        chiave.setPgInventario(dettTrasporto.getPg_inventario());
        chiave.setPgDocTrasportoRientro(dettTrasporto.getPg_doc_trasporto_rientro());

        DocumentoTrasportoHome home = (DocumentoTrasportoHome) getHome(userContext, DocumentoTrasportoBulk.class);
        Doc_trasporto_rientroBulk docTrasporto = home.findDocTrasportoRientro(chiave);

        if (docTrasporto == null || docTrasporto.getCd_tipo_trasporto_rientro() == null) return false;

        Tipo_trasporto_rientroBulk tipo = (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                .findByPrimaryKey(new Tipo_trasporto_rientroBulk(docTrasporto.getCd_tipo_trasporto_rientro()));

        if (tipo == null) return false;

        // Descrizione SMARTWORKING oppure flag sul tipo movimento
        docTrasporto.setTipoMovimento(tipo);
        return docTrasporto.isSmartworking() || tipo.isTipoSmartWorking();
    }

    /**
     * Regole sui beni di un rientro (presenti più quelli da aggiungere):
     * ogni bene in un trasporto definitivo, niente mix smartworking/altri,
     * beni smartworking tutti dello stesso assegnatario.
     */
    private void validaBeniRientroOmogenei(
            UserContext userContext, Doc_trasporto_rientroBulk doc, List<InventarioDocTRBulk> beniDaAggiungere)
            throws ComponentException {

        if (!(doc instanceof DocumentoRientroBulk)) return;

        List<InventarioDocTRBulk> beni = new ArrayList<>(beniDaAggiungere);
        if (doc.getDoc_trasporto_rientro_dettColl() != null) {
            for (Object o : doc.getDoc_trasporto_rientro_dettColl()) {
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) o;
                beni.add(new InventarioDocTRBulk(
                        dett.getNr_inventario(), dett.getPg_inventario(), Long.valueOf(dett.getProgressivo())));
            }
        }

        boolean smartworking = false;
        boolean altri = false;
        boolean assegnatarioImpostato = false;
        Integer cdAssegnatario = null;

        try {
            for (InventarioDocTRBulk bene : beni) {
                Doc_trasporto_rientro_dettBulk dettTrasporto =
                        trovaDettaglioOriginale(userContext, bene, doc.getPgInventario(), TRASPORTO);

                if (dettTrasporto == null) {
                    throw new ApplicationException("Il bene " + bene.getNr_inventario() + "." + bene.getProgressivo()
                            + " non risulta in un documento di trasporto definitivo.");
                }

                if (isTrasportatoInSmartworking(userContext, dettTrasporto)) {
                    smartworking = true;
                    if (!assegnatarioImpostato) {
                        cdAssegnatario = dettTrasporto.getCdTerzoAssegnatario();
                        assegnatarioImpostato = true;
                    } else if (!Objects.equals(cdAssegnatario, dettTrasporto.getCdTerzoAssegnatario())) {
                        throw new ApplicationException(
                                "Un documento di rientro di beni trasportati in smartworking può contenere solo beni " +
                                        "dello stesso assegnatario. Creare un documento di rientro per ciascun assegnatario.");
                    }
                } else {
                    altri = true;
                }

                if (smartworking && altri) {
                    throw new ApplicationException(
                            "Un documento di rientro non può contenere insieme beni trasportati in smartworking " +
                                    "e beni trasportati con altra tipologia di movimento. " +
                                    "Creare due documenti di rientro distinti.");
                }
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    private void aggiungiCodiceFiscaleFirmatario(UserContext userContext, Set<String> codiciFiscali, TerzoBulk terzo, String ruolo)
            throws ComponentException, PersistencyException {

        if (terzo == null) {
            log.debug("Firmatario '{}' non valorizzato", ruolo);
            return;
        }

        Integer cdAnag = terzo.getCd_anag();
        if (cdAnag == null) {
            throw new ApplicationException("Codice anagrafico (cd_anag) non presente per il firmatario " + ruolo + " terzo=" + terzo.getCd_terzo());
        }

        AnagraficoBulk anagrafico = (AnagraficoBulk) getHome(userContext, AnagraficoBulk.class).findByPrimaryKey(new AnagraficoBulk(cdAnag));

        if (anagrafico == null) {
            throw new ApplicationException("Anagrafica non trovata per il cd_anag=" + cdAnag + " firmatario " + ruolo + " terzo=" + terzo.getCd_terzo());
        }

        String codiceFiscale = anagrafico.getCodice_fiscale();

        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            throw new ApplicationException("Codice fiscale non valorizzato nell'anagrafica (cd_anag=" + cdAnag + ") per il firmatario " + ruolo + " terzo=" + terzo.getCd_terzo());
        }

        codiciFiscali.add(codiceFiscale.trim().toUpperCase());
    }

    /** Genera il PDF tramite report Jasper. */
    private byte[] generaPdfDocTrasportoRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            if (userContext == null) {
                throw new ApplicationException("UserContext non disponibile per la generazione del PDF.");
            }
            if (doc == null) {
                throw new ApplicationException("Documento non disponibile per la generazione del PDF.");
            }
            if (doc.getEsercizio() == null) {
                throw new ApplicationException("Esercizio non valorizzato per la generazione del PDF.");
            }
            if (doc.getPgInventario() == null) {
                throw new ApplicationException("Progressivo inventario non valorizzato per la generazione del PDF.");
            }
            if (doc.getTiDocumento() == null || doc.getTiDocumento().trim().isEmpty()) {
                throw new ApplicationException("Tipo documento non valorizzato per la generazione del PDF.");
            }
            if (doc.getPgDocTrasportoRientro() == null) {
                throw new ApplicationException("Progressivo documento non valorizzato per la generazione del PDF.");
            }

            Print_spoolerBulk print = DocTraspRientUtil.creaStampa(
                    doc, DocTraspRientUtil.nomeFileDaFirmare(doc), userContext.getUser());

            Report report = SpringUtil
                    .getBean("printService", PrintService.class)
                    .executeReport(userContext, print);

            if (report == null) {
                throw new ApplicationException("Il servizio di stampa non ha restituito alcun report.");
            }

            byte[] pdfBytes = report.getBytes();

            if (pdfBytes == null || pdfBytes.length == 0) {
                try (InputStream inputStream = report.getInputStream()) {
                    if (inputStream != null) {
                        pdfBytes = inputStream.readAllBytes();
                    }
                }
            }

            DocTraspRientUtil.validaPdf(pdfBytes, "PDF documento Trasporto/Rientro generato da Jasper");

            return pdfBytes;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ComponentException("Errore nella lettura del PDF generato: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore generazione PDF documento Trasporto/Rientro: " + e.getMessage(), e);
        }
    }

    private void salvaPdfDaFirmareSuDocumentale(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            String nomeFile)
            throws Exception {

        DocTraspRientUtil.validaPdf(pdfBytes, "PDF da firmare");

        String storePath = getStorePathDocTrasportoRientro(doc);

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        File fileDaFirmare = DocTraspRientUtil.creaFileTemporaneoPdf(nomeFile, pdfBytes);

        AllegatoDocTraspRientroBulk allegatoTecnicoDaFirmare = creaAllegatoDocTraspRientroPerTipo(doc, false);

        allegatoTecnicoDaFirmare.setNome(nomeFile);
        allegatoTecnicoDaFirmare.setDescrizione(DocTraspRientUtil.descrizioneAllegato(doc, "da firmare"));
        allegatoTecnicoDaFirmare.setContentType("application/pdf");
        allegatoTecnicoDaFirmare.setFile(fileDaFirmare);
        allegatoTecnicoDaFirmare.setUtenteSIGLA(userContext.getUser());
        allegatoTecnicoDaFirmare.setToBeCreated();

        try (FileInputStream fis = new FileInputStream(fileDaFirmare)) {
            storeService.storeSimpleDocument(
                    allegatoTecnicoDaFirmare,
                    fis,
                    "application/pdf",
                    nomeFile,
                    storePath
            );

            allegatoTecnicoDaFirmare.setCrudStatus(OggettoBulk.NORMAL);

        } catch (StorageException e) {
            if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                throw new ApplicationException(
                        "Esiste già sul documentale un documento da firmare con nome: " + nomeFile);
            }
            throw e;
        }
    }

    private AllegatoGenericoBulk creaAllegatoFirmatoDaPdf(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato)
            throws Exception {

        if (doc == null) {
            throw new ApplicationException("Documento non disponibile per creare l'allegato firmato.");
        }

        DocTraspRientUtil.validaPdf(pdfFirmato, "PDF firmato");

        String nomeFile = DocTraspRientUtil.nomeFileFirmato(doc);
        File fileFirmato = DocTraspRientUtil.creaFileTemporaneoPdf(nomeFile, pdfFirmato);

        AllegatoDocTraspRientroBulk allegato = creaAllegatoDocTraspRientroPerTipo(doc, true);

        allegato.setNome(nomeFile);
        allegato.setDescrizione(DocTraspRientUtil.descrizioneAllegato(doc, "firmato"));
        allegato.setContentType("application/pdf");
        allegato.setFile(fileFirmato);
        allegato.setUtenteSIGLA(userContext.getUser());
        allegato.setToBeCreated();

        return allegato;
    }

    /** Verifiche comuni prima di aggiornare l'esito della firma. */
    private void validaDocumentoPerEsitoFirma(Doc_trasporto_rientroBulk doc, String operazione)
            throws ApplicationException {

        if (doc == null) {
            throw new ApplicationException("Documento non presente.");
        }
        if (doc.isAnnullato()) {
            throw new ApplicationException("Documento annullato: impossibile aggiornare " + operazione + ".");
        }
        if (doc.isDefinitivo()) {
            throw new ApplicationException("Documento già definitivo: impossibile aggiornare " + operazione + ".");
        }
        if (doc.getUuidFlussoAutorizzativo() == null || doc.getUuidFlussoAutorizzativo().trim().isEmpty()) {
            throw new ApplicationException("Documento privo di UUID.");
        }
        if (!TRASPORTO.equals(doc.getTiDocumento()) && !RIENTRO.equals(doc.getTiDocumento())) {
            throw new ApplicationException(
                    "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento());
        }
    }

    /** Aggiorna il documento con il PDF firmato ricevuto dal flusso. */
    public Doc_trasporto_rientroBulk aggiornaDocumentoFirmato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato)
            throws ComponentException {

        try {
            validaDocumentoPerEsitoFirma(doc, "la firma");
            DocTraspRientUtil.validaPdf(pdfFirmato, "PDF firmato ricevuto");

            // Ricarica i dettagli dal DB: non fidarsi della classe concreta del bulk
            caricaDettagliDocumento(userContext, doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            rimuoviAllegatiTecniciDaFirmareDallaCollection(doc);

            rimuoviPdfDaFirmareDalDocumentale(doc);

            // Allegato visibile nel tab Allegati
            AllegatoGenericoBulk allegatoFirmato = creaAllegatoFirmatoDaPdf(userContext, doc, pdfFirmato);
            doc.addToArchivioAllegati(allegatoFirmato);

            doc.aggiornaDopoFirmaCompletata();
            doc.setToBeUpdated();

            Doc_trasporto_rientroBulk docAggiornato =
                    (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

            if (docAggiornato.getArchivioAllegati() == null
                    || docAggiornato.getArchivioAllegati().isEmpty()) {
                docAggiornato.setArchivioAllegati(doc.getArchivioAllegati());
            }

            archiviaAllegatiDocTR(userContext, docAggiornato);

            docAggiornato.setCrudStatus(OggettoBulk.NORMAL);

            return docAggiornato;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException("Errore aggiornamento documento firmato: " + e.getMessage(), e);
        }
    }

    /** Registra il rifiuto della firma sul documento. */
    public Doc_trasporto_rientroBulk aggiornaDocumentoRifiutato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            String motivoRifiuto)
            throws ComponentException {

        try {
            validaDocumentoPerEsitoFirma(doc, "il rifiuto firma");

            caricaDettagliDocumento(userContext, doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            rimuoviAllegatiTecniciDaFirmareDallaCollection(doc);

            doc.aggiornaDopoRifiutoFirma(motivoRifiuto);
            doc.setToBeUpdated();

            Doc_trasporto_rientroBulk docAggiornato =
                    (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

            // Dopo l'update rimuove il _DAFIRMARE, altrimenti il reinvio fallisce
            rimuoviPdfDaFirmareDalDocumentale(docAggiornato);

            docAggiornato.setCrudStatus(OggettoBulk.NORMAL);

            return docAggiornato;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException("Errore aggiornamento documento rifiutato: " + e.getMessage(), e);
        }
    }

    /** Rimuove dalla collection in memoria i file tecnici _DAFIRMARE. */
    private void rimuoviAllegatiTecniciDaFirmareDallaCollection(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getArchivioAllegati() == null) {
            return;
        }

        for (Iterator<AllegatoGenericoBulk> it = doc.getArchivioAllegati().iterator(); it.hasNext(); ) {
            AllegatoGenericoBulk allegato = it.next();
            if (allegato != null && DocTraspRientUtil.isNomeFileDaFirmare(doc, allegato.getNome())) {
                it.remove();
            }
        }
    }

    /** Rimuove dal documentale il PDF tecnico _DAFIRMARE (il _FIRMATO è archiviato da archiviaAllegatiDocTR). */
    private void rimuoviPdfDaFirmareDalDocumentale(Doc_trasporto_rientroBulk doc)
            throws Exception {

        String storePath = getStorePathDocTrasportoRientro(doc);

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        String nomeDaFirmare = DocTraspRientUtil.nomeFileDaFirmare(doc);

        try {
            StorageObject storageObjectDaFirmare =
                    storeService.getStorageObjectByPath(storePath + StorageDriver.SUFFIX + nomeDaFirmare);

            if (storageObjectDaFirmare != null) {
                storeService.delete(storageObjectDaFirmare.getKey());
            }
        } catch (Exception e) {
            // Non bloccante: il file può essere già stato rimosso
        }
    }

    private String getStorePathDocTrasportoRientro(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        if (doc == null) {
            throw new ApplicationException("Documento non disponibile per calcolare lo storage path.");
        }

        if (doc.getEsercizio() == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ApplicationException(
                    "Documento non ancora salvato: impossibile generare lo storage path.");
        }

        String folder = "";
        String descrizioneFolder = "";

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            folder = "Doc. Trasporto";
            descrizioneFolder = "Documento Trasporto ";
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            folder = "Doc. Rientro";
            descrizioneFolder = "Documento Rientro ";
        }

        return Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                folder,
                String.valueOf(doc.getEsercizio()),
                descrizioneFolder
                        + doc.getEsercizio()
                        + Utility.lpad(doc.getPgDocTrasportoRientro().toString(), 10, '0')
        ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));
    }

    private AllegatoDocTraspRientroBulk creaAllegatoDocTraspRientroPerTipo(
            Doc_trasporto_rientroBulk doc,
            boolean firmato)
            throws ApplicationException {

        if (doc == null || doc.getTiDocumento() == null) {
            throw new ApplicationException("Tipo documento non disponibile per creare allegato.");
        }

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())) {
            AllegatoDocumentoTrasportoBulk allegato = new AllegatoDocumentoTrasportoBulk();
            allegato.setAspectName(
                    firmato
                            ? AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO
                            : AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_DA_FIRMARE
            );
            return allegato;
        }

        if (Doc_trasporto_rientroBulk.RIENTRO.equals(doc.getTiDocumento())) {
            AllegatoDocumentoRientroBulk allegato = new AllegatoDocumentoRientroBulk();
            allegato.setAspectName(
                    firmato
                            ? AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO
                            : AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_DA_FIRMARE
            );
            return allegato;
        }

        throw new ApplicationException(
                "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento());
    }

    private Integer calcolaCdTerzoAssegnatario(Doc_trasporto_rientroBulk doc, InventarioDocTRBulk bene) {

        if (doc.isSmartworking() && doc.getTerzoSmartworking() != null) {
            return doc.getTerzoSmartworking().getCd_terzo();
        }

        if (Boolean.TRUE.equals(doc.getFlIncaricato()) && doc.getTerzoIncRitiro() != null) {
            return doc.getTerzoIncRitiro().getCd_terzo();
        }

        return bene.getCd_assegnatario();
    }
}
