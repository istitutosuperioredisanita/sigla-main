package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaHome;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRHome;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.service.HappysignDocService;
import it.cnr.contab.inventario01.service.HappysignDocServiceFactory;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
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
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StorageException;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.*;

/**
 * Component per la gestione dei documenti di Trasporto e Rientro.
 * <p>
 * Responsabilità principali:
 * - Creazione, modifica, eliminazione e ricerca di documenti di trasporto/rientro
 * - Gestione del ciclo di vita e transizioni di stato (INSERITO → INVIATO → DEFINITIVO → ANNULLATO)
 * - Validazione delle business rules e dei vincoli di integrità
 * - Gestione dei riferimenti bidirezionali tra documenti di trasporto e rientro
 * - Controllo della concorrenza tramite lock esclusivi su documenti e beni
 * - Aggiornamento automatico dello stato FL_BENE_IN_IST sui beni movimentati
 * - Gestione degli allegati firmati digitalmente
 * - Integrazione con il sistema di numerazione automatica dei documenti
 * <p>
 * UNICA FONTE DI VALIDAZIONE E BUSINESS LOGIC per i documenti di trasporto/rientro.
 */
public class DocTrasportoRientroComponent extends CRUDDetailComponent
        implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DocTrasportoRientroComponent.class);


    @Autowired
    private StoreService storeService;

    public DocTrasportoRientroComponent() {
    }

    // =========================================================================
    // INIZIALIZZAZIONE
    // =========================================================================

    /**
     * Inizializza le chiavi e opzioni del documento caricando i tipi di movimento disponibili.
     */
    public void initializeKeysAndOptionsInto(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        try {
            Tipo_trasporto_rientroHome tipoHome =
                    (Tipo_trasporto_rientroHome) getHome(usercontext, Tipo_trasporto_rientroBulk.class);
            Collection tipi = tipoHome.findTipoMovimenti(
                    ((Doc_trasporto_rientroBulk) oggettobulk).getTiDocumento());
            ((Doc_trasporto_rientroBulk) oggettobulk).setTipoMovimenti(tipi);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (IntrospectionException e) {
            throw new ComponentException(e);
        }
        super.initializeKeysAndOptionsInto(usercontext, oggettobulk);
    }

    /**
     * Inizializza un nuovo documento di trasporto/rientro con valori di default.
     * NOTA: non imposta local_transactionID.
     */
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

            doc.setIdFlussoHappysign(null);
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

    /**
     * Inizializza i tipi di movimento disponibili per il documento.
     */
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

    /**
     * Carica l'inventario associato alla UO dell'utente.
     */
    public Id_inventarioBulk caricaInventario(UserContext aUC)
            throws ComponentException, PersistencyException, IntrospectionException {
        Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
        Id_inventarioBulk inventario = invHome.findInventarioFor(aUC, false);
        if (inventario == null) {
            throw new ApplicationException("Nessun inventario associato alla UO!");
        }
        return inventario;
    }

    // =========================================================================
    // INIZIALIZZA PER MODIFICA
    // =========================================================================

    /**
     * Inizializza il documento per la modifica con controllo lock su documento e beni.
     * Carica incaricato e smartworking direttamente come TerzoBulk.
     */
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

            // INCARICATO: carica direttamente TerzoBulk
            if (docTR.getCdTerzoIncaricato() != null) {
                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(docTR.getCdTerzoIncaricato()));
                if (terzo != null) {
                    docTR.setTerzoIncRitiro(terzo);
                }
            }

            BulkHome dettHome = getHomeDocumentoTrasportoRientroDett(aUC, docTR);
            docTR.setDoc_trasporto_rientro_dettColl(
                    new BulkList(((Doc_trasporto_rientro_dettHome) dettHome).getDetailsFor(docTR))
            );

            // SMARTWORKING: carica TerzoBulk dal primo dettaglio
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
                                new Long(dettaglio.getProgressivo().longValue())
                        ));
                dettaglio.setBene(inv);
            }

            getHomeCache(aUC).fetchAll(aUC, dettHome);

        } catch (Exception e) {
            throw new ComponentException(e);
        }

        return docTR;
    }

    // =========================================================================
    // INIZIALIZZA PER RICERCA
    // =========================================================================

    /**
     * Inizializza il documento per la ricerca caricando inventario e dati correlati.
     */
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
            log.warn("Errore caricamento relazioni per ricerca: " + e.getMessage());
        }

        return doc;
    }

    /**
     * Inizializza il documento per la ricerca libera.
     */
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
            log.warn("Errore caricamento relazioni per ricerca libera: " + e.getMessage());
        }

        return doc;
    }

    /**
     * Carica le relazioni comuni (inventario, tipo movimento, terzi) per la ricerca.
     */
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

    // =========================================================================
    // CREA / MODIFICA
    // =========================================================================

    /**
     * Crea un nuovo documento di trasporto/rientro salvando solo la testata.
     * Gestisce incaricato e smartworking direttamente tramite TerzoBulk.
     */
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

            // INCARICATO: già TerzoBulk, nessuna conversione da Anagrafico
            if (docT.getTerzoIncRitiro() != null &&
                    docT.getTerzoIncRitiro().getCd_terzo() != null) {
                docT.setCdTerzoIncaricato(docT.getTerzoIncRitiro().getCd_terzo());
            }

            // SMARTWORKING: già TerzoBulk, nessuna conversione da Anagrafico
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
            log.error(System.err.toString());
            throw handleException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            log.error(System.err.toString());
            throw handleException(e);
        }
    }

    /**
     * Verifica che il documento non sia già presente nel database.
     */
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

    /**
     * Aggiorna il documento caricando dettagli, validandolo,
     * verificando l'unicità dell'allegato firmato e gestendo
     * progressivo temporaneo o definitivo.
     */
    @Override
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);

            docT.setDoc_trasporto_rientro_dettColl(
                    new BulkList(dettHome.getDetailsFor(docT))
            );

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

    /**
     * Restituisce true se il documento ha un progressivo temporaneo (valore negativo).
     */
    private boolean isProgressivoTemporaneo(Doc_trasporto_rientroBulk docT) {
        return docT.getPgDocTrasportoRientro() != null
                && docT.getPgDocTrasportoRientro() < 0;
    }

    /**
     * Assegna un nuovo progressivo definitivo al documento,
     * riallinea dati e ricostruisce correttamente la struttura del bulk.
     */
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

        /*
         * Converte il documento temporaneo in documento definitivo.
         * Dopo questa chiamata il vecchio progressivo negativo non deve più
         * essere usato per ricaricare il documento.
         */
        home.confirmDocTrasportoRientroTemporaneo(aUC, docT, nuovoPgDoc);

        docT.setPgDocTrasportoRientro(nuovoPgDoc);
        docT.setStato(STATO_INSERITO);
        docT.setToBeUpdated();

        /*
         * Ricarica i dettagli usando il nuovo progressivo definitivo.
         * NON chiamare inizializzaBulkPerModifica sul vecchio bulk negativo.
         */
        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);

        docT.setDoc_trasporto_rientro_dettColl(
                new BulkList(dettHome.getDetailsFor(docT))
        );

        /*
         * Manteniamo la collection allegati corrente, perché in questa fase
         * il documento è solo salvato in INSERITO, non definitivo.
         */
        if (docT.getArchivioAllegati() == null) {
            docT.setArchivioAllegati(new BulkList<>());
        }

        preparaEAggiornaRiferimentiInversi(aUC, docT);

        docT.setCrudStatus(OggettoBulk.NORMAL);

        return docT;
    }

    /**
     * Aggiorna dettagli e allegati di un documento già definitivo
     * e salva le modifiche tramite il componente.
     */
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

    // =========================================================================
    // SALVA DEFINITIVO
    // =========================================================================

    /**
     * Salva definitivamente il documento verificando la presenza dell'allegato firmato,
     * validando dati e allegati e aggiornando lo stato a DEFINITIVO.
     */
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
                throw new ApplicationException("Il documento è ancora in attesa di firma HappySign.");
            }

            if (!hasAllegatoFirmato(docTR)) {
                throw new ApplicationException(
                        "È obbligatorio allegare il documento firmato prima di rendere definitivo il documento.");
            }

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
            docTR.setToBeUpdated();
            preparaEAggiornaRiferimentiInversi(userContext, docTR);

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (PersistencyException e) {
            throw new ComponentException("Errore durante il salvataggio definitivo", e);
        }
    }

    /**
     * Popola i campi di riferimento sui dettagli di rientro.
     */
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

    // =========================================================================
    // CAMBIO STATO
    // =========================================================================

    /**
     * Cambia lo stato del documento da INSERITO a INVIATO predisponendolo alla firma.
     */
    public Doc_trasporto_rientroBulk changeStatoInInviato(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR)
            throws ComponentException {

        return inviaDocumentoAllaFirma(userContext, docTR);
    }

    // =========================================================================
    // ANNULLAMENTO
    // =========================================================================

    /**
     * Annulla il documento: ricarica i dettagli, valida, verifica le condizioni di annullamento,
     * aggiorna lo stato dei beni e imposta lo stato del documento su ANNULLATO.
     */
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

    // =========================================================================
    // AGGIORNAMENTO STATO BENI (InventarioDocTRBulk/Home)
    // =========================================================================

    /**
     * Aggiorna FL_BENE_IN_IST per tutti i beni del documento.
     */
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

    /**
     * Carica un InventarioDocTRBulk dal database usando le informazioni del dettaglio.
     */
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

    // =========================================================================
    // HELPER HOMES
    // =========================================================================

    /**
     * Restituisce la Home appropriata per i dettagli in base a TI_DOCUMENTO,
     * non in base alla classe concreta Java.
     */
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

    /**
     * Carica i dettagli del documento dal database.
     */
    private void caricaDettagliDocumento(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException, PersistencyException {

        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

        doc.setDoc_trasporto_rientro_dettColl(
                new BulkList(dettHome.getDetailsFor(doc))
        );
    }

    // =========================================================================
    // ASSEGNA PROGRESSIVO
    // =========================================================================

    /**
     * Assegna il progressivo al documento tramite il numeratore.
     */
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

    // =========================================================================
    // RICERCA TERZI (TerzoBulk diretto)
    // =========================================================================

    /**
     * Ricerca INCARICATO delegando al componente Terzo (TerzoBulk diretto).
     */
    public SQLBuilder selectTerzoIncRitiroByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            TerzoBulk terzo,
            CompoundFindClause clause)
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

    /**
     * Ricerca SMARTWORKING delegando al componente Terzo (TerzoBulk diretto).
     */
    public SQLBuilder selectTerzoSmartworkingByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            TerzoBulk terzo,
            CompoundFindClause clause)
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

    // =========================================================================
    // RIFERIMENTI INVERSI TRASPORTO ↔ RIENTRO
    // =========================================================================

    /**
     * Prepara e aggiorna la relazione bidirezionale tra TRASPORTO e RIENTRO.
     */
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

    // =========================================================================
    // ALLEGATI
    // =========================================================================

    private String getAspectFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getTiDocumento() == null) {
            return null;
        }

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            return AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        }

        if (RIENTRO.equals(doc.getTiDocumento())) {
            return AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
        }

        return null;
    }

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
        String aspectAtteso = getAspectFirmato(doc);
        if (doc.getArchivioAllegati() == null) return false;
        return countAllegatiFirmati(doc, aspectAtteso) == 1;
    }

    public void validaAggiuntaAllegatoFirmato(Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            validaAllegatiComp(doc);
        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        }
    }

    private String estraiNomeAllegato(AllegatoGenericoBulk allegato) {
        if (allegato == null) return null;
        String nome = allegato.getNome();
        if (nome != null && !nome.isEmpty()) return nome.toLowerCase();
        if (allegato.getFile() != null && allegato.getFile().getName() != null) {
            String fromFile = allegato.parseFilename(allegato.getFile().getName());
            if (fromFile != null && !fromFile.isEmpty()) return fromFile.toLowerCase();
        }
        return null;
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

            String nome = estraiNomeAllegato(allegato);
            if (nome != null) {
                if (!nomiVisti.add(nome)) {
                    throw new ApplicationException(
                            "Attenzione: impossibile caricare l'allegato '" + allegato.getNome() +
                                    "' poiché esiste già un file con lo stesso nome.");
                }
            }
        }

        String aspectAtteso = getAspectFirmato(doc);
        if (aspectAtteso != null) {
            long countFirmati = attivi.stream()
                    .filter(a -> a instanceof AllegatoDocTraspRientroBulk)
                    .map(a -> (AllegatoDocTraspRientroBulk) a)
                    .filter(a -> aspectAtteso.equals(a.getAspectName()))
                    .count();

            if (countFirmati > 1) {
                String tipo = TRASPORTO.equals(doc.getTiDocumento())
                        ? "Documento di Trasporto FIRMATO"
                        : "Documento di Rientro FIRMATO";
                throw new ApplicationException(
                        "Attenzione: è consentito allegare un solo " + tipo + ".");
            }
        }
    }

    /**
     * Gestisce archiviazione, aggiornamento e cancellazione degli allegati T/R su CMIS.
     *
     * IMPORTANTE:
     * Non usare cast a DocumentoTrasportoBulk / DocumentoRientroBulk.
     * In alcuni flussi il bulk può arrivare con classe concreta non coerente,
     * ma tiDocumento corretto.
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

            AllegatoDocTraspRientroBulk allegato =
                    (AllegatoDocTraspRientroBulk) allegatoGen;

            if (allegato.isToBeCreated()) {
                File file = Optional.ofNullable(allegato.getFile())
                        .orElseThrow(() -> new ApplicationException(
                                "File non presente per allegato: " + allegato.getNome()
                        ));

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
                    throw new ApplicationException(
                            "File non trovato: " + allegato.getNome(),
                            e
                    );

                } catch (StorageException e) {
                    if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                        throw new ApplicationException(
                                "File già presente: " + allegato.getNome()
                        );
                    }

                    throw new ApplicationException(
                            "Errore storage per: " + allegato.getNome(),
                            e
                    );

                } catch (Exception e) {
                    throw new ApplicationException(
                            "Errore archiviazione allegato: " + allegato.getNome(),
                            e
                    );
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
                    throw new ApplicationException(
                            "File non trovato: " + allegato.getNome(),
                            e
                    );

                } catch (StorageException e) {
                    if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                        throw new ApplicationException(
                                "File già presente: " + allegato.getNome()
                        );
                    }

                    throw new ApplicationException(
                            "Errore storage per: " + allegato.getNome(),
                            e
                    );

                } catch (Exception e) {
                    throw new ApplicationException(
                            "Errore aggiornamento allegato: " + allegato.getNome(),
                            e
                    );
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


    // =========================================================================
    // SELEZIONE / ITERATORI BENI (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Ritorna un iteratore sui dettagli del documento per la modifica.
     */
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

    /**
     * Wrapper per selezionare dettagli rientro per modifica.
     */
    public RemoteIterator selectEditDettagliRientro(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            Class bulkClass, CompoundFindClause filters) throws ComponentException {
        return selectEditDettagliTrasporto(userContext, doc, bulkClass, filters);
    }

    /**
     * Ritorna la lista dei beni disponibili per il trasporto (usa InventarioDocTRBulk).
     */
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
        } catch (PersistencyException ex) {
            throw handleException(ex);
        } catch (IntrospectionException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Ritorna la lista dei beni già selezionati nel documento (usa InventarioDocTRBulk).
     */
    public SimpleBulkList selezionati(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null) return new SimpleBulkList();

        try {
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

            List risultato = getHome(userContext, InventarioDocTRBulk.class).fetchAll(sql);
            if (risultato != null && risultato.size() > 0) return new SimpleBulkList(risultato);

        } catch (PersistencyException pe) {
            throw new ComponentException(pe);
        }
        return new SimpleBulkList();
    }

    /**
     * Ritorna iteratore sui beni associati al documento (usa InventarioDocTRBulk).
     */
    public RemoteIterator selectBeniAssociatiByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            Class bulkClass) throws ComponentException {

        if (docT.getPgDocTrasportoRientro() == null)
            return new EmptyRemoteIterator();

        try {
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

            return iterator(userContext, sql, bulkClass, null);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // =========================================================================
    // MODIFICA BENI (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Aggiunge o rimuove beni dal documento di TRASPORTO o RIENTRO.
     * Usa InventarioDocTRBulk e TerzoBulk direttamente.
     */
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

            for (int i = 0; i < beni.length; i++) {
                InventarioDocTRBulk bene = (InventarioDocTRBulk) beni[i];

                if (old_ass.get(i) != ass.get(i)) {

                    if (ass.get(i)) {
                        // Ricarica dal DB per allineare versione
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

                            // Assegnatario da TerzoBulk
                            dett.setCdTerzoAssegnatario(
                                    calcolaCdTerzoAssegnatario(docT, bene)
                            );

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

                            // Assegnatario da TerzoBulk
                            dett.setCdTerzoAssegnatario(
                                    calcolaCdTerzoAssegnatario(docT, bene)
                            );

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

    /**
     * Wrapper per modificare beni rientrati con accessori.
     */
    public void modificaBeniRientratiConAccessori(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni, BitSet oldSelection, BitSet newSelection)
            throws ComponentException {
        modificaBeniTrasportatiConAccessori(userContext, doc, beni, oldSelection, newSelection);
    }

    // =========================================================================
    // TROVA DETTAGLIO ORIGINALE (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Trova il dettaglio più recente di un documento originale (TRASPORTO o RIENTRO).
     * Accetta InventarioDocTRBulk.
     */
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

    // =========================================================================
    // ACCESSORI (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Cerca gli accessori associati a un bene principale (usa InventarioDocTRBulk).
     */
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

    /**
     * Cerca accessori presenti nel TRASPORTO originale per un RIENTRO (usa InventarioDocTRBulk).
     */
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
                Doc_trasporto_rientro_dettBulk dettaglioAccessorio =
                        (Doc_trasporto_rientro_dettBulk) obj;
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
            sql.addSQLClause("AND", "dr.STATO",
                    SQLBuilder.NOT_EQUALS, STATO_ANNULLATO);

            List risultati = dettHome.fetchAll(sql);
            return risultati != null && !risultati.isEmpty();

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Cerca accessori di un bene nei dettagli già salvati (usa InventarioDocTRBulk).
     */
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

    // =========================================================================
    // CERCA BENI UNIFICATO / FILTRI (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Wrapper beni trasportabili.
     */
    public RemoteIterator cercaBeniTrasportabili(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, true);
    }

    /**
     * Wrapper beni da far rientrare.
     */
    public RemoteIterator cercaBeniDaFarRientrare(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, false);
    }

    /**
     * Wrapper lista beni da far rientrare.
     */
    public RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext, Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi, CompoundFindClause clauses) throws ComponentException {
        return cercaBeniDaFarRientrare(userContext, doc, beniEsclusi, clauses);
    }

    /**
     * Metodo unificato per cercare beni trasportabili o da far rientrare (usa InventarioDocTRBulk).
     */
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

            /*
             * I beni già presenti in altri documenti T/R aperti
             * non devono comparire nella lista di selezione.
             *
             * Stati esclusi:
             * - INS: documento inserito/non definitivo
             * - INV: documento inviato in firma
             *
             * Il documento corrente viene escluso dal controllo, così in modifica
             * i suoi beni già associati non bloccano la logica interna.
             */
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

    /**
     * Applica filtri per TRASPORTO.
     * Filtra smartworking direttamente da TerzoBulk (doc.getTerzoSmartworking().getCd_terzo()).
     */
    private void applicaFiltriTrasporto(SQLBuilder sql, Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws ComponentException, PersistencyException, IntrospectionException {

        // SMARTWORKING: filtro diretto su TerzoBulk
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

    /**
     * Applica filtri per RIENTRO.
     */
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

    /**
     * Esclude dalla lista i beni già presenti in altri documenti T/R aperti.
     *
     * Stati bloccanti:
     * - INS
     * - INV
     *
     * Il documento corrente viene escluso solo se ha PG positivo.
     */
    private void escludiBeniInDocumentiAperti(
            SQLBuilder sql,
            Doc_trasporto_rientroBulk doc) {

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

        if (doc != null
                && doc.getPgInventario() != null
                && doc.getTiDocumento() != null
                && doc.getEsercizio() != null
                && doc.getPgDocTrasportoRientro() != null
                && doc.getPgDocTrasportoRientro().longValue() > 0L) {

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


    // =========================================================================
    // ELIMINA DETTAGLI
    // =========================================================================

    /**
     * Elimina tutti i dettagli salvati di un documento, gestendo i riferimenti incrociati.
     */
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

            if (doc instanceof DocumentoRientroBulk) {
                rimuoviRiferimentiTrasportoARientro(userContext, dettagliDaEliminare);
            }

            for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
                dettaglio.setToBeDeleted();
                super.eliminaConBulk(userContext, dettaglio);
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Elimina dettagli specifici salvati di un documento (usa InventarioDocTRBulk).
     */
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

            if (doc instanceof DocumentoRientroBulk) {
                rimuoviRiferimentiTrasportoARientro(userContext, dettagliDaEliminare);
            }

            for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
                dettaglio.setToBeDeleted();
                super.eliminaConBulk(userContext, dettaglio);
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Elimina bene principale con accessori dai dettagli salvati (usa InventarioDocTRBulk).
     */
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

            int recordEliminati = pstmt.executeUpdate();
            log.warn("Eliminati " + recordEliminati +
                    " dettagli (principale + accessori) dal documento " +
                    doc.getPgDocTrasportoRientro());

        } catch (SQLException e) {
            throw new ComponentException(
                    "Errore eliminazione principale + accessori dai dettagli: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                log.warn("Errore chiusura statement: " + e);
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

    // =========================================================================
    // SELEZIONA TUTTI / CARICA PER INSERIMENTO (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Seleziona e inserisce tutti i beni senza limite.
     */
    public void selezionaTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            List<InventarioDocTRBulk> beniFiltrati =
                    caricaBeniPerInserimento(userContext, doc, clauses, TRASPORTO.equals(doc.getTiDocumento()));

            if (beniFiltrati.isEmpty()) {
                log.warn("Nessun bene da inserire");
                return;
            }

            log.warn("Caricati " + beniFiltrati.size() + " beni, inizio creazione dettagli...");

            inserisciBeniComeDettagli(userContext, doc, beniFiltrati);

            log.warn("Creazione completata: inseriti " + beniFiltrati.size() + " dettagli");

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Carica i beni da inserire applicando i filtri appropriati (usa InventarioDocTRBulk).
     */
    public List<InventarioDocTRBulk> caricaBeniPerInserimento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses,
            boolean ignoredIsTrasporto)
            throws ComponentException {

        try {
            InventarioDocTRHome invHome =
                    (InventarioDocTRHome) getHome(userContext, InventarioDocTRBulk.class);

            SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);

            boolean isTrasporto = TRASPORTO.equals(doc.getTiDocumento());

            if (isTrasporto) {
                applicaFiltriTrasporto(sql, doc, userContext);
            } else if (RIENTRO.equals(doc.getTiDocumento())) {
                applicaFiltriRientro(sql, doc);
            } else {
                throw new ApplicationException(
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
                );
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

            List risultati = invHome.fetchAll(sql);
            return risultati != null ? risultati : Collections.emptyList();

        } catch (ApplicationException e) {
            throw handleException(e);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Inserisce una lista di beni come dettagli del documento (usa InventarioDocTRBulk).
     * Assegnatario determinato da TerzoBulk direttamente.
     */
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

        int totalInserted = 0;

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

            totalInserted++;

            if (totalInserted % 100 == 0) {
                log.warn("Inseriti " + totalInserted + " / " + beniFiltrati.size() + " beni...");
            }
        }
    }


    // =========================================================================
    // VALIDA BENI NON IN ALTRI DOCUMENTI (InventarioDocTRBulk)
    // =========================================================================

    /**
     * Valida che i beni non siano già occupati in altri documenti attivi (usa InventarioDocTRBulk).
     */
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
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
                );
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

            // Controllo 1a: beni in altri documenti aperti INS o INV
            SQLBuilder sql1a = buildSqlBeniInAltriDoc(
                    dettHome,
                    doc,
                    tipoDocDaVerificare,
                    inClauseBeni,
                    false
            );

            sql1a.addSQLClause("AND",
                    "t.STATO IN ('" + STATO_INSERITO + "','" + STATO_INVIATO + "')");

            List beniInAltriAperti = dettHome.fetchAll(sql1a);

            if (beniInAltriAperti != null && !beniInAltriAperti.isEmpty()) {
                throw new ApplicationException(
                        "Impossibile procedere: alcuni beni sono già presenti in un altro documento di "
                                + tipoDocLabel +
                                " in stato INSERITO o INVIATO IN FIRMA."
                );
            }

            // Controllo 1b: beni in altri DEFINITIVI
            SQLBuilder sql1b = buildSqlBeniInAltriDoc(dettHome, doc, tipoDocDaVerificare, inClauseBeni, false);
            sql1b.addSQLClause("AND", "t.STATO", SQLBuilder.EQUALS, STATO_DEFINITIVO);

            if (isTrasporto) {
                sql1b.addSQLClause("AND",
                        "NOT EXISTS (SELECT 1 FROM DOC_TRASPORTO_RIENTRO_DETT rd2" +
                                " JOIN DOC_TRASPORTO_RIENTRO r2" +
                                " ON rd2.PG_INVENTARIO = r2.PG_INVENTARIO" +
                                " AND rd2.TI_DOCUMENTO = r2.TI_DOCUMENTO" +
                                " AND rd2.ESERCIZIO = r2.ESERCIZIO" +
                                " AND rd2.PG_DOC_TRASPORTO_RIENTRO = r2.PG_DOC_TRASPORTO_RIENTRO" +
                                " WHERE r2.TI_DOCUMENTO = '" + RIENTRO + "'" +
                                " AND r2.STATO = '" + STATO_DEFINITIVO + "'" +
                                " AND rd2.NR_INVENTARIO = DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO" +
                                " AND rd2.PROGRESSIVO = DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO)");
            }

            List beniInAltriDefinitivi = dettHome.fetchAll(sql1b);
            if (beniInAltriDefinitivi != null && !beniInAltriDefinitivi.isEmpty()) {
                throw new ApplicationException(
                        "Impossibile procedere: alcuni beni sono già presenti in un altro documento di "
                                + tipoDocLabel + " in stato DEFINITIVO.");
            }

            // Controllo 2 (solo RIENTRO): bene deve avere un TRASPORTO definitivo senza RIENTRO definitivo
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
                sql2.addSQLClause("AND",
                        "NOT EXISTS (SELECT 1 FROM DOC_TRASPORTO_RIENTRO_DETT rd2" +
                                " JOIN DOC_TRASPORTO_RIENTRO r2" +
                                " ON rd2.PG_INVENTARIO = r2.PG_INVENTARIO" +
                                " AND rd2.TI_DOCUMENTO = r2.TI_DOCUMENTO" +
                                " AND rd2.ESERCIZIO = r2.ESERCIZIO" +
                                " AND rd2.PG_DOC_TRASPORTO_RIENTRO = r2.PG_DOC_TRASPORTO_RIENTRO" +
                                " WHERE r2.TI_DOCUMENTO = '" + RIENTRO + "'" +
                                " AND r2.STATO = '" + STATO_DEFINITIVO + "'" +
                                " AND rd2.NR_INVENTARIO = DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO" +
                                " AND rd2.PROGRESSIVO = DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO)");

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
                                    + msg.toString());
                }
            }

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Helper per costruire la query di verifica beni in altri documenti.
     */
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

        sql.addSQLClause("AND", "t.TI_DOCUMENTO",
                SQLBuilder.EQUALS, tipoDocDaVerificare);

        /*
         * Escludo il documento corrente solo se ha PG definitivo positivo.
         */
        if (doc.getPgDocTrasportoRientro() != null
                && doc.getPgDocTrasportoRientro().longValue() > 0L) {

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

    // =========================================================================
    // RECUPERO DETTAGLI / TERZO RESPONSABILE
    // =========================================================================

    /**
     * Recupera tutti i dettagli di un documento.
     */
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

    /**
     * Recupera il terzo responsabile dell'UO di contesto.
     */
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

    // =========================================================================
    // ESERCIZIO COEP / DOCUMENTI PREDISPOSTI ALLA FIRMA
    // =========================================================================

    /**
     * Verifica se l'esercizio COEP è chiuso.
     */
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

        } catch (SQLException ex) {
            throw handleException(ex);
        } catch (PersistencyException e) {
            throw handleException(e);
        } catch (IntrospectionException e) {
            throw handleException(e);
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (SQLException e) {
                throw handleException(e);
            }
        }
        return false;
    }

    /**
     * Recupera i documenti in stato INVIATO predisposti alla firma.
     */
    public List getDocumentiPredispostiAllaFirma(UserContext userContext) throws ComponentException {
        List documenti = new ArrayList();

        documenti.addAll(getDocumentiPredispostiAllaFirmaPerTipo(
                userContext,
                DocumentoTrasportoBulk.class
        ));

        documenti.addAll(getDocumentiPredispostiAllaFirmaPerTipo(
                userContext,
                DocumentoRientroBulk.class
        ));

        return documenti;
    }

    private List getDocumentiPredispostiAllaFirmaPerTipo(
            UserContext userContext,
            Class bulkClass)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home =
                    (Doc_trasporto_rientroHome) getHome(userContext, bulkClass);

            SQLBuilder sql = home.createSQLBuilder();

            sql.addSQLClause("AND", "STATO", SQLBuilder.EQUALS, STATO_INVIATO);
            sql.addSQLClause("AND", "STATO_FLUSSO", SQLBuilder.EQUALS, "INV");
            sql.addSQLClause("AND", "ID_FLUSSO_HAPPYSIGN", SQLBuilder.ISNOTNULL, null);

            /*
             * Il cron deve lavorare solo documenti con PG definitivo.
             */
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


    // =========================================================================
    // STAMPA
    // =========================================================================

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

    // =========================================================================
    // WEB SERVICE (InventarioDocTRBulk, TerzoBulk diretto)
    // =========================================================================

    /**
     * Salva un documento T/R da WS: normalizza, applica default, costruisce dettagli,
     * valida allegati/beni/documento, assegna PG definitivo.
     * Usa InventarioDocTRBulk e TerzoBulk direttamente (niente AnagraficoBulk, niente local_transactionID).
     */
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
            }

            if (!dettagliCostruiti.isEmpty()) {
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

    /**
     * Inizializza il Bulk con i default di framework e ripristina i valori dal WS.
     * Gestisce incaricato/smartworking come TerzoBulk diretto.
     * Non imposta local_transactionID.
     */
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

            // INCARICATO: TerzoBulk diretto
            if (Boolean.TRUE.equals(src.getFlIncaricato()) && src.getCdTerzoIncaricato() != null) {
                TerzoBulk t = new TerzoBulk();
                t.setCd_terzo(src.getCdTerzoIncaricato());
                dst.setTerzoIncRitiro(t);
                dst.setCdTerzoIncaricato(src.getCdTerzoIncaricato());
            }

            // SMARTWORKING: TerzoBulk diretto
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

    /**
     * Salva i dettagli verificando esistenza del bene (InventarioDocTRBulk).
     */
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

    /**
     * Costruisce la lista tipizzata dei dettagli partendo dai DTO WS (usa InventarioDocTRBulk).
     */
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

    // =========================================================================
    // CERCA DOCUMENTI PER BENE
    // =========================================================================

    /**
     * Cerca il primo documento T/R che contiene un bene dato per stato ed esercizio.
     */
    public Doc_trasporto_rientroBulk cercaDocumentiPerBene(
            UserContext userContext,
            String tiDocumento, String stato, Long nrInventario, Integer esercizio)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home;
            if (TRASPORTO.equals(tiDocumento))
                home = (Doc_trasporto_rientroHome) getHome(userContext, DocumentoTrasportoBulk.class);
            else if (RIENTRO.equals(tiDocumento))
                home = (Doc_trasporto_rientroHome) getHome(userContext, DocumentoRientroBulk.class);
            else
                home = (Doc_trasporto_rientroHome) getHome(userContext, Doc_trasporto_rientroBulk.class);

            SQLBuilder sql = home.createSQLBuilder();
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.PG_INVENTARIO", "dett.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO", "dett.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.ESERCIZIO", "dett.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO",
                    "dett.PG_DOC_TRASPORTO_RIENTRO");

            if (tiDocumento != null)
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.TI_DOCUMENTO",
                        SQLBuilder.EQUALS, tiDocumento);
            if (stato != null)
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.STATO", SQLBuilder.EQUALS, stato);
            if (esercizio != null)
                sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO.ESERCIZIO",
                        SQLBuilder.EQUALS, esercizio);
            if (nrInventario != null)
                sql.addSQLClause("AND", "dett.NR_INVENTARIO", SQLBuilder.EQUALS, nrInventario);

            sql.setDistinctClause(true);
            sql.addOrderBy("DOC_TRASPORTO_RIENTRO.PG_DOC_TRASPORTO_RIENTRO DESC");

            List<Doc_trasporto_rientroBulk> docs = home.fetchAll(sql);
            return (docs != null && !docs.isEmpty()) ? docs.get(0) : null;

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    // =========================================================================
    // VALIDAZIONE DOCUMENTO
    // =========================================================================

    /**
     * Valida obbligatori del documento e coerenza data rispetto alle modifiche dei beni.
     */
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
                throw new ApplicationException(
                        "Attenzione: è necessario specificare almeno un bene!");

            boolean hasBeniSalvati = docT.getDoc_trasporto_rientro_dettColl()
                    .stream()
                    .anyMatch(o -> ((Doc_trasporto_rientro_dettBulk) o)
                            .getCrudStatus() == OggettoBulk.NORMAL);

            if (hasBeniSalvati &&
                    docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                String tipoDoc = RIENTRO.equals(docT.getTiDocumento()) ? "Rientro" : "Trasporto";
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


    /**
     * Invia il documento Trasporto/Rientro al flusso HappySign.
     *
     * Flusso corretto:
     * 1. valida il documento;
     * 2. genera il PDF Jasper;
     * 3. salva nel documentale il PDF originale da inviare in firma;
     * 4. invia ad HappySign lo stesso identico PDF;
     * 5. aggiorna UUID, stato flusso e stato documento.
     */
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
                        "Il documento deve essere in stato INSERITO per essere inviato in firma."
                );
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

            validaPdfGenerato(
                    pdfBytes,
                    "PDF documento Trasporto/Rientro da inviare a HappySign"
            );

            String nomeFileDaFirmare = generaNomeFilePdfDaFirmare(doc);

            HappysignDocService happySignService = HappysignDocServiceFactory.get();

            /*
             * PRIMA HappySign.
             * Se questa chiamata fallisce, non viene salvato nulla su Azure.
             */
            String uuidHappysign =
                    happySignService.inviaDocumentoAdHappySign(
                            doc,
                            pdfBytes,
                            nomeFileDaFirmare,
                            (CNRUserContext) userContext
                    );

            if (uuidHappysign == null || uuidHappysign.trim().isEmpty()) {
                throw new ApplicationException("HappySign non ha restituito UUID documento.");
            }

            /*
             * DOPO Azure.
             * Il file tecnico _DAFIRMARE viene creato solo se HappySign ha accettato il flusso.
             */
            salvaPdfDaFirmareSuAzure(
                    userContext,
                    doc,
                    pdfBytes,
                    nomeFileDaFirmare
            );

            doc.setIdFlussoHappysign(uuidHappysign.trim());
            doc.inizializzaPerInvioFirma();
            doc.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

        } catch (ApplicationException e) {
            throw e;

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore invio documento a HappySign: " + e.getMessage(),
                    e
            );
        }
    }

    private String generaNomeFilePdfDaFirmare(Doc_trasporto_rientroBulk doc) {
        String prefisso = "";

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            prefisso = "DocTrasporto";
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            prefisso = "DocRientro";
        }

        return prefisso
                + "_"
                + doc.getEsercizio()
                + "_"
                + doc.getPgDocTrasportoRientro()
                + "_DAFIRMARE.pdf";
    }

    private String generaNomeFilePdfFirmato(Doc_trasporto_rientroBulk doc) {
        String prefisso = "";

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            prefisso = "DocTrasporto";
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            prefisso = "DocRientro";
        }

        return prefisso
                + "_"
                + doc.getEsercizio()
                + "_"
                + doc.getPgDocTrasportoRientro()
                + "_FIRMATO.pdf";
    }


    /**
     * Genera il PDF del documento Trasporto/Rientro usando il report Jasper
     * /cnrdocamm/docamm/doc_trasporto_rientro.jasper.
     *
     * Restituisce i byte del PDF, pronti per essere salvati nel documentale
     * e inviati ad HappySign.
     */
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

            Print_spoolerBulk print = new Print_spoolerBulk();
            print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
            print.setFlEmail(false);
            print.setReport("/cnrdocamm/docamm/doc_trasporto_rientro.jasper");
            print.setNomeFile(generaNomeFilePdfDaFirmare(doc));
            print.setUtcr(userContext.getUser());

            print.addParam("esercizio", doc.getEsercizio(), Integer.class);
            print.addParam("pg_inventario", doc.getPgInventario(), Long.class);
            print.addParam("ti_documento", doc.getTiDocumento(), String.class);
            print.addParam("pg_doc_trasporto_rientro", doc.getPgDocTrasportoRientro(), Long.class);
            print.addParam("DIR_IMAGE", "", String.class);

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

            validaPdfGenerato(pdfBytes, "PDF documento Trasporto/Rientro generato da Jasper");

            return pdfBytes;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);

        } catch (IOException e) {
            throw new ComponentException(
                    "Errore nella lettura del PDF generato: " + e.getMessage(),
                    e
            );

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore generazione PDF documento Trasporto/Rientro: " + e.getMessage(),
                    e
            );
        }
    }


    private void salvaPdfDaFirmareSuAzure(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            String nomeFile)
            throws Exception {

        validaPdfGenerato(pdfBytes, "PDF da firmare");

        String storePath = getStorePathDocTrasportoRientro(doc);

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        File fileDaFirmare = creaFileTemporaneoPdf(nomeFile, pdfBytes);

        AllegatoDocTraspRientroBulk allegatoTecnicoDaFirmare =
                creaAllegatoDocTraspRientroPerTipo(doc, false);

        allegatoTecnicoDaFirmare.setNome(nomeFile);
        allegatoTecnicoDaFirmare.setDescrizione(generaDescrizioneAllegatoDaFirmare(doc));
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
                        "Esiste già su Azure un documento da firmare con nome: " + nomeFile
                );
            }

            throw e;
        }
    }

    /**
     * Scrive un array di byte PDF su un file temporaneo.
     *
     * Il file temporaneo serve solo perché il sistema allegati/CMIS
     * lavora su File/InputStream.
     */
    private File creaFileTemporaneoPdf(String nomeFile, byte[] pdfBytes)
            throws Exception {

        validaPdfGenerato(pdfBytes, "PDF temporaneo " + nomeFile);

        File dir = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, nomeFile);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pdfBytes);
        }

        file.deleteOnExit();

        return file;
    }


    /**
     * Valida che i byte rappresentino un PDF non vuoto.
     */
    private void validaPdfGenerato(byte[] pdfBytes, String descrizione)
            throws ApplicationException {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ApplicationException(descrizione + " non disponibile o vuoto.");
        }

        if (pdfBytes.length < 5) {
            throw new ApplicationException(descrizione + " non valido.");
        }

        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));

        if (!header.startsWith("%PDF")) {
            throw new ApplicationException(descrizione + " non è un PDF valido.");
        }
    }

    /**
     * Restituisce una piccola anteprima testuale dei byte,
     * utile quando il print server restituisce HTML/JSON invece di PDF.
     */
    private String previewBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        return new String(
                bytes,
                0,
                Math.min(bytes.length, 500),
                StandardCharsets.UTF_8
        )
                .replace('\n', ' ')
                .replace('\r', ' ');
    }


    private AllegatoGenericoBulk creaAllegatoFirmatoDaPdf(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato)
            throws Exception {

        if (doc == null) {
            throw new ApplicationException(
                    "Documento non disponibile per creare l'allegato firmato."
            );
        }

        validaPdfGenerato(pdfFirmato, "PDF firmato");

        String nomeFile = generaNomeFilePdfFirmato(doc);
        File fileFirmato = creaFileTemporaneoPdf(nomeFile, pdfFirmato);

        AllegatoDocTraspRientroBulk allegato =
                creaAllegatoDocTraspRientroPerTipo(doc, true);

        allegato.setNome(nomeFile);
        allegato.setDescrizione(generaDescrizioneAllegatoFirmato(doc));
        allegato.setContentType("application/pdf");
        allegato.setFile(fileFirmato);
        allegato.setUtenteSIGLA(userContext.getUser());
        allegato.setToBeCreated();

        return allegato;
    }


    public Doc_trasporto_rientroBulk aggiornaDocumentoFirmatoDaHappySign(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato)
            throws ComponentException {

        try {
            if (doc == null) {
                throw new ApplicationException("Documento non presente.");
            }

            if (doc.isAnnullato()) {
                throw new ApplicationException("Documento annullato: impossibile aggiornare la firma.");
            }

            if (doc.isDefinitivo()) {
                throw new ApplicationException("Documento già definitivo.");
            }

            if (doc.getIdFlussoHappysign() == null
                    || doc.getIdFlussoHappysign().trim().isEmpty()) {
                throw new ApplicationException("Documento privo di UUID HappySign.");
            }

            if (!TRASPORTO.equals(doc.getTiDocumento())
                    && !RIENTRO.equals(doc.getTiDocumento())) {
                throw new ApplicationException(
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
                );
            }

            validaPdfGenerato(pdfFirmato, "PDF firmato ricevuto da HappySign");

            /*
             * Ricarico i dettagli dal DB usando tiDocumento.
             * Non devo fidarmi della classe concreta del bulk arrivato dal cron.
             */
            caricaDettagliDocumento(userContext, doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            /*
             * Se per errore il documento tecnico DAFIRMARE è entrato nella collection,
             * lo elimino solo dalla collection in memoria.
             */
            rimuoviAllegatiTecniciDaFirmareDallaCollection(doc);

            /*
             * Su Azure sostituisco il PDF tecnico DAFIRMARE con il PDF FIRMATO.
             * Questo file resta tecnico su Azure e NON è la riga allegato utente.
             */
            sostituisciPdfDaFirmareConFirmatoSuAzure(
                    userContext,
                    doc,
                    pdfFirmato
            );

            /*
             * Questo invece è l'allegato vero, visibile nel tab Allegati SIGLA.
             */
            AllegatoGenericoBulk allegatoFirmato =
                    creaAllegatoFirmatoDaPdf(userContext, doc, pdfFirmato);

            doc.addToArchivioAllegati(allegatoFirmato);

            /*
             * Dopo firma:
             * - STATO resta INV;
             * - STATO_FLUSSO diventa FIR;
             * - DATA_FIRMA valorizzata;
             * - tab Allegati sbloccabile lato BP/UI.
             */
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
            throw new ComponentException(
                    "Errore aggiornamento documento firmato HappySign: " + e.getMessage(),
                    e
            );
        }
    }


    public Doc_trasporto_rientroBulk aggiornaDocumentoRifiutatoDaHappySign(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            String motivoRifiuto)
            throws ComponentException {

        try {
            if (doc == null) {
                throw new ApplicationException("Documento non presente.");
            }

            if (doc.isAnnullato()) {
                throw new ApplicationException("Documento annullato: impossibile aggiornare il rifiuto firma.");
            }

            if (doc.isDefinitivo()) {
                throw new ApplicationException("Documento già definitivo: impossibile aggiornare il rifiuto firma.");
            }

            if (doc.getIdFlussoHappysign() == null
                    || doc.getIdFlussoHappysign().trim().isEmpty()) {
                throw new ApplicationException("Documento privo di UUID HappySign.");
            }

            if (!TRASPORTO.equals(doc.getTiDocumento())
                    && !RIENTRO.equals(doc.getTiDocumento())) {
                throw new ApplicationException(
                        "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
                );
            }

            caricaDettagliDocumento(userContext, doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            rimuoviAllegatiTecniciDaFirmareDallaCollection(doc);

            doc.aggiornaDopoRifiutoFirma(motivoRifiuto);
            doc.setToBeUpdated();

            Doc_trasporto_rientroBulk docAggiornato =
                    (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

            docAggiornato.setCrudStatus(OggettoBulk.NORMAL);

            return docAggiornato;

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore aggiornamento documento rifiutato HappySign: " + e.getMessage(),
                    e
            );
        }
    }


    private void rimuoviAllegatiTecniciDaFirmareDallaCollection(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getArchivioAllegati() == null) {
            return;
        }

        String nomeDaFirmare = generaNomeFilePdfDaFirmare(doc);

        for (Iterator<AllegatoGenericoBulk> it = doc.getArchivioAllegati().iterator(); it.hasNext(); ) {
            AllegatoGenericoBulk allegato = it.next();

            if (allegato == null || allegato.getNome() == null) {
                continue;
            }

            String nome = allegato.getNome();

            if (nomeDaFirmare.equalsIgnoreCase(nome)
                    || nome.toUpperCase().contains("_DAFIRMARE")
                    || nome.toUpperCase().contains("_DA_FIRMARE")
                    || nome.toUpperCase().contains("_DA_FIRMA")) {
                it.remove();
            }
        }
    }

    private void sostituisciPdfDaFirmareConFirmatoSuAzure(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato)
            throws Exception {

        validaPdfGenerato(pdfFirmato, "PDF firmato ricevuto da HappySign");

        String storePath = getStorePathDocTrasportoRientro(doc);

        if (storeService == null) {
            storeService = SpringUtil.getBean("storeService", StoreService.class);
        }

        String nomeDaFirmare = generaNomeFilePdfDaFirmare(doc);
        String nomeFirmato = generaNomeFilePdfFirmato(doc);

        File fileFirmato = creaFileTemporaneoPdf(nomeFirmato, pdfFirmato);

        /*
         * Cancello il file tecnico DAFIRMARE da Azure.
         * Questo file non deve comparire tra gli allegati SIGLA.
         */
        try {
            StorageObject storageObjectDaFirmare =
                    storeService.getStorageObjectByPath(
                            storePath + StorageDriver.SUFFIX + nomeDaFirmare
                    );

            if (storageObjectDaFirmare != null) {
                storeService.delete(storageObjectDaFirmare.getKey());
            }

        } catch (Exception e) {
            /*
             * Se non lo trovo, non blocco.
             * Può essere già stato cancellato o può avere nome storico.
             */
        }

        AllegatoDocTraspRientroBulk allegatoTecnicoFirmato =
                creaAllegatoDocTraspRientroPerTipo(doc, false);

        allegatoTecnicoFirmato.setNome(nomeFirmato);
        allegatoTecnicoFirmato.setDescrizione(generaDescrizioneAllegatoFirmato(doc));
        allegatoTecnicoFirmato.setContentType("application/pdf");
        allegatoTecnicoFirmato.setFile(fileFirmato);
        allegatoTecnicoFirmato.setUtenteSIGLA(userContext.getUser());
        allegatoTecnicoFirmato.setToBeCreated();

        try (FileInputStream fis = new FileInputStream(fileFirmato)) {
            storeService.storeSimpleDocument(
                    allegatoTecnicoFirmato,
                    fis,
                    "application/pdf",
                    nomeFirmato,
                    storePath
            );

            allegatoTecnicoFirmato.setCrudStatus(OggettoBulk.NORMAL);

        } catch (StorageException e) {
            if (StorageException.Type.CONSTRAINT_VIOLATED.equals(e.getType())) {
                /*
                 * Se esiste già, non faccio fallire il cron.
                 * Il documento firmato è già stato archiviato tecnicamente.
                 */
                return;
            }

            throw e;
        }
    }


    private String getStorePathDocTrasportoRientro(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        if (doc == null) {
            throw new ApplicationException("Documento non disponibile per calcolare lo storage path.");
        }

        if (doc.getEsercizio() == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ApplicationException(
                    "Documento non ancora salvato: impossibile generare lo storage path."
            );
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

    private String generaDescrizioneAllegatoFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            return "Documento firmato";
        }

        String tipoDocumento = "";

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            tipoDocumento = "Documento di Trasporto firmato";
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            tipoDocumento = "Documento di Rientro firmato";
        }

        return tipoDocumento
                + " - Esercizio "
                + doc.getEsercizio()
                + " - Progressivo "
                + doc.getPgDocTrasportoRientro();
    }

    private String generaDescrizioneAllegatoDaFirmare(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            return "Documento da firmare";
        }

        String tipoDocumento = "";

        if (TRASPORTO.equals(doc.getTiDocumento())) {
            tipoDocumento = "Documento di Trasporto da firmare";
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            tipoDocumento = "Documento di Rientro da firmare";
        }

        return tipoDocumento
                + " - Esercizio "
                + doc.getEsercizio()
                + " - Progressivo "
                + doc.getPgDocTrasportoRientro();
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
                            : AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO
            );

            return allegato;
        }

        if (Doc_trasporto_rientroBulk.RIENTRO.equals(doc.getTiDocumento())) {
            AllegatoDocumentoRientroBulk allegato = new AllegatoDocumentoRientroBulk();

            allegato.setAspectName(
                    firmato
                            ? AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO
                            : AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO
            );

            return allegato;
        }

        throw new ApplicationException(
                "Tipo documento Trasporto/Rientro non riconosciuto: " + doc.getTiDocumento()
        );
    }
    private Integer calcolaCdTerzoAssegnatario(
            Doc_trasporto_rientroBulk doc,
            InventarioDocTRBulk bene) {

        if (doc.isSmartworking()
                && doc.getTerzoSmartworking() != null) {
            return doc.getTerzoSmartworking().getCd_terzo();
        }

        if (Boolean.TRUE.equals(doc.getFlIncaricato())
                && doc.getTerzoIncRitiro() != null) {
            return doc.getTerzoIncRitiro().getCd_terzo();
        }

        return bene.getCd_assegnatario();
    }

}