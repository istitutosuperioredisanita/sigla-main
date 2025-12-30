package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.anagraf00.ejb.AnagraficoComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaHome;
import it.cnr.contab.doccont00.intcass.bulk.Distinta_cassiereBulk;
import it.cnr.contab.doccont00.intcass.bulk.Distinta_cassiereHome;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniHome;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.*;
import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

/**
 * Component per la gestione dei documenti di Trasporto e Rientro.
 * UNICA FONTE DI VALIDAZIONE E BUSINESS LOGIC.
 */
public class DocTrasportoRientroComponent extends it.cnr.jada.comp.CRUDDetailComponent
        implements Cloneable, Serializable {

    // ========================================
    // INIZIALIZZAZIONE
    // ========================================
    private static final long serialVersionUID = 1L;

    public DocTrasportoRientroComponent() {
    }

    public void initializeKeysAndOptionsInto(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException {
        try {
            Tipo_trasporto_rientroHome tipoHome = (Tipo_trasporto_rientroHome) getHome(usercontext, Tipo_trasporto_rientroBulk.class);
            java.util.Collection tipi;
            tipi = tipoHome.findTipoMovimenti(((Doc_trasporto_rientroBulk) oggettobulk).getTiDocumento());
            ((Doc_trasporto_rientroBulk) oggettobulk).setTipoMovimenti(tipi);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (IntrospectionException e) {
            throw new ComponentException(e);
        }
        super.initializeKeysAndOptionsInto(usercontext, oggettobulk);
    }

    @Override
    public OggettoBulk inizializzaBulkPerInserimento(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            doc = (Doc_trasporto_rientroBulk) super.inizializzaBulkPerInserimento(aUC, doc);

            if (doc.getTiDocumento() == null || doc.getTiDocumento().isEmpty()) {
                throw new ComponentException(
                        "Errore: Tipo documento (T/R) non impostato dal BP. "
                );
            }


            inizializzaTipoMovimento(aUC, doc);
            doc.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());

            // Dati contesto utente
            doc.setCds_scrivania(it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(aUC));
            doc.setUo_scrivania(it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(aUC));
            doc.setEsercizio(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC));

            // Carica dati riferimento
            doc.setCondizioni(getHome(aUC, Condizione_beneBulk.class).findAll());
            doc.setInventario(caricaInventario(aUC));

            Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            doc.setConsegnatario(invHome.findConsegnatarioFor(doc.getInventario()));
            doc.setDelegato(invHome.findDelegatoFor(doc.getInventario()));
            doc.setUo_consegnataria(invHome.findUoRespFor(aUC, doc.getInventario()));

            // Valori default
            doc.setLocal_transactionID(getLocalTransactionID(aUC, true));
            doc.setDataRegistrazione(new Timestamp(System.currentTimeMillis()));
            doc.setToBeCreated();

            // Inizializza FK necessarie
            doc.setPgInventario(doc.getInventario().getPg_inventario());

            // Inizializza flag tipo ritiro
            doc.setFlIncaricato(Boolean.FALSE);
            doc.setFlVettore(Boolean.FALSE);

            // Inizializza campi firma digitale
            doc.setIdFlussoHappysign(null);
            doc.setStatoFlusso(null);
            doc.setDataInvioFirma(null);
            doc.setDataFirma(null);
            doc.setNoteRifiuto(null);

            // Assegna progressivo prima di creare il record
            assegnaProgressivo(aUC, doc);
            doc.setDsDocTrasportoRientro("");

            return doc;

        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }


    protected Doc_trasporto_rientroBulk assegnaProgressivo(UserContext userContext,
                                                           Doc_trasporto_rientroBulk doc) throws ComponentException,
            PersistencyException {
        try {
            ((Doc_trasporto_rientroHome) getHome(userContext, doc.getClass()))
                    .inizializzaProgressivo(userContext, doc);
            return doc;
        } catch (Exception e) {
            throw handleException(e);
        }
    }



    private BulkHome getHomeDocumentoTrasportoRientroDett(UserContext aUC, OggettoBulk bulk)throws ComponentException {
        if ( bulk instanceof DocumentoTrasportoBulk)
            return  getHome(aUC, DocumentoTrasportoDettBulk.class);
        else
            return  getHome(aUC, DocumentoRientroDettBulk.class);
    }


    /**
     * Prepara un documento per la modifica.
     * NON chiama super.inizializzaBulkPerModifica perché usa la Home FIGLIA sbagliata.
     * Carica manualmente tutto usando la Home PADRE.
     */
    @Override
    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null)
            throw new ComponentException("Documento non trovato!");

        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;

        // ❌ NON CHIAMARE super.inizializzaBulkPerModifica(aUC, bulk);
        // Il metodo padre usa la Home FIGLIA che non ha findByPrimaryKey corretto

        // ========== CARICA MANUALMENTE IL DOCUMENTO ==========
        try {
            // ✅ USA LA HOME PADRE (Doc_trasporto_rientroHome)
            Doc_trasporto_rientroHome docHome =
                    (Doc_trasporto_rientroHome) getHome(aUC, Doc_trasporto_rientroBulk.class);

            docTR = (Doc_trasporto_rientroBulk) docHome.findByPrimaryKey(docTR);

            if (docTR == null) {
                throw new ComponentException("Documento non trovato nel database!");
            }

            // ✅ Inizializza keys e options manualmente (sostituisce il super)
            initializeKeysAndOptionsInto(aUC, docTR);

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }

        // ========== INIZIALIZZA TIPO MOVIMENTO ==========
        inizializzaTipoMovimento(aUC, docTR);

        try {
            // ========== CARICA INVENTARIO, CONSEGNATARIO, ECC. ==========
            docTR.setInventario(caricaInventario(aUC));
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC, docTR.getInventario()));

            // ========== CARICA TERZO E ANAGRAFICO INCARICATO ==========
            if (docTR.getCdTerzoIncaricato() != null) {
                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(docTR.getCdTerzoIncaricato()));
                if (terzo != null) {
                    docTR.setTerzoIncRitiro(terzo);

                    if (terzo.getCd_anag() != null) {
                        AnagraficoBulk anagrafico = (AnagraficoBulk)
                                getHome(aUC, AnagraficoBulk.class)
                                        .findByPrimaryKey(new AnagraficoBulk(terzo.getCd_anag()));
                        if (anagrafico != null) {
                            docTR.setAnagIncRitiro(anagrafico);
                        }
                    }
                }
            }

            // ========== CARICA DETTAGLI ==========
            BulkHome dettHome = getHomeDocumentoTrasportoRientroDett(aUC, docTR);
            docTR.setDoc_trasporto_rientro_dettColl(
                    new BulkList(((Doc_trasporto_rientro_dettHome) dettHome).getDetailsFor(docTR))
            );

            // ========== RICOSTRUISCI SMARTWORKING ==========
            if (docTR.isSmartworking() && docTR.getDoc_trasporto_rientro_dettColl() != null
                    && !docTR.getDoc_trasporto_rientro_dettColl().isEmpty()) {

                Doc_trasporto_rientro_dettBulk primoDettaglio =
                        (Doc_trasporto_rientro_dettBulk) docTR.getDoc_trasporto_rientro_dettColl().get(0);

                if (primoDettaglio.getCdTerzoAssegnatario() != null) {
                    TerzoBulk terzoSmart = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                            .findByPrimaryKey(new TerzoBulk(primoDettaglio.getCdTerzoAssegnatario()));

                    if (terzoSmart != null) {
                        docTR.setTerzoSmartworking(terzoSmart);

                        if (terzoSmart.getCd_anag() != null) {
                            AnagraficoBulk anagSmart = (AnagraficoBulk)
                                    getHome(aUC, AnagraficoBulk.class)
                                            .findByPrimaryKey(new AnagraficoBulk(terzoSmart.getCd_anag()));
                            if (anagSmart != null) {
                                docTR.setAnagSmartworking(anagSmart);
                            }
                        }
                    }
                }
            }

            // ========== INIZIALIZZA BENI NEI DETTAGLI ==========
            for (Iterator dett = docTR.getDoc_trasporto_rientro_dettColl().iterator(); dett.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) dett.next();
                dettaglio.setDoc_trasporto_rientro(docTR);

                Inventario_beniBulk inv = (Inventario_beniBulk) getHome(aUC, Inventario_beniBulk.class)
                        .findByPrimaryKey(new Inventario_beniBulk(
                                dettaglio.getNr_inventario(),
                                dettaglio.getPg_inventario(),
                                new Long(dettaglio.getProgressivo().longValue())
                        ));
                dettaglio.setBene(inv);
            }

            // ✅ Sostituisce getHomeCache(aUC).fetchAll(aUC, dettHome) del padre
            getHomeCache(aUC).fetchAll(aUC, dettHome);

        } catch (Exception e) {
            throw new ComponentException(e);
        }

        return docTR;
    }


//    @Override
//    public OggettoBulk inizializzaBulkPerRicerca(UserContext userContext, OggettoBulk bulk)
//            throws ComponentException {
//        try {
//            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
//            inizializzaTipoMovimento(userContext, doc);
//            doc.setInventario(caricaInventario(userContext));
//            return doc;
//        } catch (PersistencyException | IntrospectionException e) {
//            throw new ComponentException(e);
//        }
//    }


    /**
     * Inizializza il documento di trasporto/rientro per la ricerca.
     * Carica inventario, consegnatario, delegato e UO consegnataria.
     */
    @Override
    public OggettoBulk inizializzaBulkPerRicerca(
            UserContext userContext,
            OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null) {
            throw new ApplicationException(
                    "Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!"
            );
        }

        // Chiamata al metodo padre
        bulk = super.inizializzaBulkPerRicerca(userContext, bulk);

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        // ========== INIZIALIZZA DATI DA SCRIVANIA ==========
        String cds = CNRUserContext.getCd_cds(userContext);
        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);
        Integer esercizio = CNRUserContext.getEsercizio(userContext);

        doc.setCds_scrivania(cds);
        doc.setUo_scrivania(uo);
        doc.setEsercizio(esercizio);

        try {
            // ========== CARICA INVENTARIO ==========
            if (doc.getPgInventario() != null) {
                Id_inventarioBulk inventario = (Id_inventarioBulk) getHome(userContext, Id_inventarioBulk.class)
                        .findByPrimaryKey(new Id_inventarioBulk(doc.getPgInventario()));
                doc.setInventario(inventario);

                // ========== CARICA CONSEGNATARIO, DELEGATO, UO ==========
                if (inventario != null) {
                    Id_inventarioHome inventarioHome =
                            (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);

                    doc.setConsegnatario(
                            inventarioHome.findConsegnatarioFor(inventario)
                    );
                    doc.setDelegato(
                            inventarioHome.findDelegatoFor(inventario)
                    );
                    doc.setUo_consegnataria(
                            inventarioHome.findUoRespFor(userContext, inventario)
                    );
                }
            }

            // ========== CARICA TIPO MOVIMENTO ==========
            if (doc.getCd_tipo_trasporto_rientro() != null) {
                Tipo_trasporto_rientroBulk tipoMovimento =
                        (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                                .findByPrimaryKey(new Tipo_trasporto_rientroBulk(
                                        doc.getCd_tipo_trasporto_rientro()
                                ));
                doc.setTipoMovimento(tipoMovimento);
            }

            // ========== CARICA TERZO INCARICATO ==========
            if (doc.getCdTerzoIncaricato() != null) {
                TerzoBulk terzoIncaricato =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoIncaricato()));
                doc.setTerzoIncRitiro(terzoIncaricato);
            }

            // ========== CARICA TERZO RESPONSABILE ==========
            if (doc.getCdTerzoResponsabile() != null) {
                TerzoBulk terzoResponsabile =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoResponsabile()));
                doc.setTerzoRespDip(terzoResponsabile);
            }

        } catch (PersistencyException | IntrospectionException e) {
            System.out.println("Errore caricamento relazioni per ricerca: " + e.getMessage());
        }

        return doc;
    }


    /**
     * Inizializza il documento di trasporto/rientro per la ricerca libera.
     * Carica inventario, consegnatario, delegato e UO consegnataria.
     */
    @Override
    public OggettoBulk inizializzaBulkPerRicercaLibera(
            UserContext userContext,
            OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null) {
            throw new ComponentException(
                    "Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!"
            );
        }

        // ========== CHIAMATA AL METODO PADRE ==========
        bulk = super.inizializzaBulkPerRicercaLibera(userContext, bulk);

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        // ========== INIZIALIZZA DATI DA SCRIVANIA ==========
        String cds = CNRUserContext.getCd_cds(userContext);
        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);
        Integer esercizio = CNRUserContext.getEsercizio(userContext);

        doc.setCds_scrivania(cds);
        doc.setUo_scrivania(uo);
        doc.setEsercizio(esercizio);

        try {
            // ========== CARICA INVENTARIO ==========
            if (doc.getPgInventario() != null) {
                Id_inventarioBulk inventario = (Id_inventarioBulk) getHome(userContext, Id_inventarioBulk.class)
                        .findByPrimaryKey(new Id_inventarioBulk(doc.getPgInventario()));
                doc.setInventario(inventario);

                // ========== CARICA CONSEGNATARIO, DELEGATO, UO ==========
                if (inventario != null) {
                    Id_inventarioHome inventarioHome =
                            (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);

                    doc.setConsegnatario(
                            inventarioHome.findConsegnatarioFor(inventario)
                    );
                    doc.setDelegato(
                            inventarioHome.findDelegatoFor(inventario)
                    );
                    doc.setUo_consegnataria(
                            inventarioHome.findUoRespFor(userContext, inventario)
                    );
                }
            }

            // ========== CARICA TIPO MOVIMENTO ==========
            if (doc.getCd_tipo_trasporto_rientro() != null) {
                Tipo_trasporto_rientroBulk tipoMovimento =
                        (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                                .findByPrimaryKey(new Tipo_trasporto_rientroBulk(
                                        doc.getCd_tipo_trasporto_rientro()
                                ));
                doc.setTipoMovimento(tipoMovimento);
            }

            // ========== CARICA TERZO INCARICATO ==========
            if (doc.getCdTerzoIncaricato() != null) {
                TerzoBulk terzoIncaricato =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoIncaricato()));
                doc.setTerzoIncRitiro(terzoIncaricato);
            }

            // ========== CARICA TERZO RESPONSABILE ==========
            if (doc.getCdTerzoResponsabile() != null) {
                TerzoBulk terzoResponsabile =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoResponsabile()));
                doc.setTerzoRespDip(terzoResponsabile);
            }

        } catch (PersistencyException | IntrospectionException e) {
            // Log dell'errore ma non blocca l'operazione
            System.out.println("Errore caricamento relazioni per ricerca libera: " + e.getMessage());
        }

        return doc;
    }


    /**
     * Crea un documento di TRASPORTO o RIENTRO con i relativi dettagli.
     *
     * FLUSSO OPERATIVO:
     * 1. Genera progressivo documento e verifica univocità
     * 2. Valida il documento
     * 3. Carica i beni selezionati dalla tabella temporanea INVENTARIO_BENI_APG
     * 4. Per ogni bene crea un dettaglio specifico:
     *    - TRASPORTO: cerca eventuale RIENTRO precedente e imposta riferimento opzionale
     *    - RIENTRO: cerca TRASPORTO originale (obbligatorio) e imposta riferimento
     * 5. Salva il documento con tutti i dettagli
     * 6. Solo per RIENTRO: aggiorna i dettagli del TRASPORTO con riferimenti inversi bidirezionali
     *
     * GESTIONE RIFERIMENTI BIDIREZIONALI:
     * - TRASPORTO → RIENTRO: riferimento opzionale (null se primo trasporto del bene)
     * - RIENTRO → TRASPORTO: riferimento obbligatorio (errore se trasporto non trovato)
     * - Dopo salvataggio RIENTRO: aggiorna TRASPORTO con riferimento inverso al RIENTRO appena creato
     *
     * @param userContext contesto utente
     * @param bulk documento da creare (DocumentoTrasportoBulk o DocumentoRientroBulk)
     * @return documento creato con dettagli persistiti
     * @throws ComponentException se validazione fallisce, bene non trovato, o errore in persistenza
     */
    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        try {
            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

            // ==================== CASO 1: DOCUMENTO GIÀ CREATO NEL doTab ====================
            if (docT.getPgDocTrasportoRientro() != null) {
                Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk)
                        findByPrimaryKey(userContext, docT);

                if (existing != null) {
                    // ✅ DOCUMENTO GIÀ ESISTE → USA modificaConBulk
                    System.out.println("Documento già esistente (PG=" +
                            docT.getPgDocTrasportoRientro() + "), uso modificaConBulk");
                    return modificaConBulk(userContext, docT);
                }
            }

            // ==================== CASO 2: PRIMA CREAZIONE (dal doTab) ====================
            verificaDocumentoNonEsistente(userContext, docT);
            validaDoc(userContext, docT);

            // ==================== RECUPERA TERZO DA ANAGRAFICO INCARICATO ====================
            if (docT.getAnagIncRitiro() != null &&
                    docT.getAnagIncRitiro().getCd_anag() != null) {

                it.cnr.contab.anagraf00.core.bulk.TerzoHome terzoHome =
                        (it.cnr.contab.anagraf00.core.bulk.TerzoHome) getHome(
                                userContext, TerzoBulk.class);

                SQLBuilder sql = terzoHome.createSQLBuilder();
                sql.addSQLClause("AND", "CD_ANAG", SQLBuilder.EQUALS,
                        docT.getAnagIncRitiro().getCd_anag());

                List terzi = terzoHome.fetchAll(sql);

                if (terzi != null && !terzi.isEmpty()) {
                    TerzoBulk terzo = (TerzoBulk) terzi.get(0);
                    docT.setTerzoIncRitiro(terzo);
                    docT.setCdTerzoIncaricato(terzo.getCd_terzo());
                }
            }

            // ==================== RECUPERA TERZO DA ANAGRAFICO SMARTWORKING ====================
            if (docT.isSmartworking() &&
                    docT.getAnagSmartworking() != null &&
                    docT.getAnagSmartworking().getCd_anag() != null) {

                it.cnr.contab.anagraf00.core.bulk.TerzoHome terzoHome =
                        (it.cnr.contab.anagraf00.core.bulk.TerzoHome) getHome(
                                userContext, TerzoBulk.class);

                SQLBuilder sql = terzoHome.createSQLBuilder();
                sql.addSQLClause("AND", "CD_ANAG", SQLBuilder.EQUALS,
                        docT.getAnagSmartworking().getCd_anag());

                List terzi = terzoHome.fetchAll(sql);

                if (terzi != null && !terzi.isEmpty()) {
                    TerzoBulk terzo = (TerzoBulk) terzi.get(0);
                    docT.setTerzoSmartworking(terzo);
                }
            }

            // ==================== IMPOSTA CD_TERZO_INCARICATO IN TESTATA ====================
            if (docT.isSmartworking()) {
                if (docT.getTerzoSmartworking() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoSmartworking().getCd_terzo());
                }
            } else if (docT.getFlIncaricato()) {
                if (docT.getTerzoIncRitiro() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoIncRitiro().getCd_terzo());
                }
            }

            // ==================== SALVA SOLO LA TESTATA (prima creazione nel doTab) ====================
            docT.setDoc_trasporto_rientro_dettColl(new SimpleBulkList()); // Vuota
            docT.setToBeCreated();
            docT.setStato(STATO_INSERITO);
            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);

            // ✅ ✅ ✅ IMPOSTA TO_BE_UPDATED DOPO LA CREAZIONE ✅ ✅ ✅
            docT.setToBeUpdated();

            System.out.println("=== TESTATA CREATA SUL DB ===");
            System.out.println("PG_DOC: " + docT.getPgDocTrasportoRientro());
            System.out.println("STATO CRUD: TO_BE_UPDATED");

            return docT;

        } catch (PersistencyException e) {
            e.printStackTrace(System.err);
            throw handleException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            throw handleException(e);
        }
    }

// ==================== METODI DI SUPPORTO ====================

    /**
     * Genera il progressivo documento tramite numeratore.
     */
    private void generaProgressivoDocumento(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException, IntrospectionException {
        Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome)
                getHome(userContext, Numeratore_doc_t_rBulk.class);

        docT.setPgDocTrasportoRientro(numHome.getNextPg(
                userContext,
                docT.getEsercizio(),
                docT.getPgInventario(),
                docT.getTiDocumento(),
                userContext.getUser()));
    }

    /**
     * Verifica che il documento non sia già presente nel database.
     * ✅ Salta il controllo se chiamato da modificaConBulk
     */
    private void verificaDocumentoNonEsistente(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {

        // ✅ Se il documento ha già un PG, è normale che esista
        // (viene da doTab o da modifica)
        if (docT.getPgDocTrasportoRientro() != null) {
            return;
        }

        Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk)
                findByPrimaryKey(userContext, docT);

        if (existing != null) {
            throw new ApplicationException(
                    "Il documento è già stato salvato (PG=" + docT.getPgDocTrasportoRientro() + ")");
        }
    }


    /**
     * Aggiorna i dettagli TRASPORTO con i riferimenti inversi ai dettagli RIENTRO appena creati.
     * Questo crea la relazione bidirezionale TRASPORTO ↔ RIENTRO.
     */
    private void aggiornaRiferimentiInversi(UserContext userContext,
                                            Doc_trasporto_rientroBulk docRientro,
                                            List<DocumentoTrasportoDettBulk> dettagliTrasportoDaAggiornare)
            throws ComponentException {

        int index = 0;
        for (Iterator it = docRientro.getDoc_trasporto_rientro_dettColl().iterator(); it.hasNext(); ) {
            DocumentoRientroDettBulk dettaglioRientro = (DocumentoRientroDettBulk) it.next();

            if (index < dettagliTrasportoDaAggiornare.size()) {
                DocumentoTrasportoDettBulk dettaglioTrasporto = dettagliTrasportoDaAggiornare.get(index);

                // Imposta riferimento inverso TRASPORTO → RIENTRO
                dettaglioTrasporto.setDocRientroDettRif(dettaglioRientro);
                dettaglioTrasporto.setToBeUpdated();

                try {
                    updateBulk(userContext, dettaglioTrasporto);
                } catch (Exception e) {
                    throw new ComponentException(
                            "Errore nell'aggiornamento del riferimento del documento di trasporto " +
                                    "per il bene " + dettaglioTrasporto.getCod_bene() + ": " + e.getMessage(), e);
                }
            }
            index++;
        }
    }


    /**
     * Trova il dettaglio più recente di un documento originale (TRASPORTO o RIENTRO)
     * necessario per popolare i campi _RIF.
     *
     * @param userContext  Contesto utente
     * @param bene         Bene da cercare
     * @param pgInventario PG inventario
     * @param tipoDocumentoDaCercare  "T" = Trasporto, "R" = Rientro
     * @return dettaglio del documento oppure null
     */
    private Doc_trasporto_rientro_dettBulk trovaDettaglioOriginale(
            UserContext userContext,
            Inventario_beniBulk bene,
            Long pgInventario,
            String tipoDocumentoDaCercare)
            throws ComponentException {

        try {
            if (OggettoBulk.isNullOrEmpty(String.valueOf(bene.getNr_inventario()))) {
                return null;
            }

            // ==================== SELEZIONA LA HOME CORRETTA ====================
            Doc_trasporto_rientro_dettHome dettHome;

            if (TRASPORTO.equals(tipoDocumentoDaCercare)) {
                dettHome = (Doc_trasporto_rientro_dettHome) getHome(
                        userContext, DocumentoTrasportoDettBulk.class);
            } else {
                dettHome = (Doc_trasporto_rientro_dettHome) getHome(
                        userContext, DocumentoRientroDettBulk.class);
            }

            SQLBuilder sql = dettHome.createSQLBuilder();

            // Join con padre DOC_TRASPORTO_RIENTRO
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO d");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "d.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "d.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "d.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "d.PG_DOC_TRASPORTO_RIENTRO");

            // Filtro bene
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                    SQLBuilder.EQUALS, pgInventario);
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO",
                    SQLBuilder.EQUALS, bene.getNr_inventario());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO",
                    SQLBuilder.EQUALS, bene.getProgressivo());

            // Tipo documento richiesto (T o R)
            sql.addSQLClause("AND", "d.TI_DOCUMENTO",
                    SQLBuilder.EQUALS, tipoDocumentoDaCercare);

            // Solo documenti firmati (definitivi)
            sql.addSQLClause("AND", "d.STATO",
                    SQLBuilder.EQUALS, Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            // Escludi documenti annullati
            sql.addSQLClause("AND", "d.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            // Ordine: più recente
            sql.addOrderBy("d.DATA_REGISTRAZIONE DESC");

            List risultati = dettHome.fetchAll(sql);

            if (risultati != null && !risultati.isEmpty()) {
                return (Doc_trasporto_rientro_dettBulk) risultati.get(0);
            }

            return null;

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }



    /**
     * Modifica Documento di Trasporto/Rientro
     *
     * FLUSSO:
     * 1. Valida il documento
     * 2. Carica i dettagli dal database
     * 3. Salva la testata
     * 4. Salva gli allegati
     * 5. Aggiorna i riferimenti bidirezionali (solo RIENTRO)
     * 6. Ricarica il documento completo
     */
    @Override
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

        try {
            // ==================== 1. VERIFICA CHIAVI PRIMARIE ====================
            if (docT.getPgDocTrasportoRientro() == null ||
                    docT.getEsercizio() == null ||
                    docT.getTiDocumento() == null ||
                    docT.getPgInventario() == null) {

                throw new ComponentException(
                        "Errore: chiavi primarie mancanti per modifica!\n" +
                                "PG_DOC: " + docT.getPgDocTrasportoRientro() + "\n" +
                                "ESERCIZIO: " + docT.getEsercizio() + "\n" +
                                "TI_DOC: " + docT.getTiDocumento() + "\n" +
                                "PG_INV: " + docT.getPgInventario()
                );
            }

            // ==================== 2. VERIFICA ESISTENZA (USA HOME PADRE) ====================
            Doc_trasporto_rientroHome docHome =
                    (Doc_trasporto_rientroHome) getHome(aUC, Doc_trasporto_rientroBulk.class);

            Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk) docHome.findByPrimaryKey(docT);

            if (existing == null) {
                throw new ComponentException(
                        "Documento non trovato nel database con le chiavi specificate!"
                );
            }

            // ==================== 3. VALIDAZIONE ====================
            validaDoc(aUC, docT);

            // ==================== 4. CARICA DETTAGLI DAL DB ====================
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);

            List dettagliDb = dettHome.getDetailsFor(docT);
            List<DocumentoTrasportoDettBulk> dettagliTrasportoDaAggiornare = new ArrayList<>();

            if (dettagliDb != null && !dettagliDb.isEmpty()) {
                docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettagliDb));

                // ========== PREPARA AGGIORNAMENTI PER RIENTRO ==========
                boolean isRientro = docT instanceof DocumentoRientroBulk;

                if (isRientro) {
                    for (Object obj : docT.getDoc_trasporto_rientro_dettColl()) {
                        if (obj instanceof DocumentoRientroDettBulk) {
                            DocumentoRientroDettBulk dettRientro = (DocumentoRientroDettBulk) obj;

                            if (dettRientro.getDocTrasportoDettRif() != null) {
                                dettagliTrasportoDaAggiornare.add(dettRientro.getDocTrasportoDettRif());
                            }
                        }
                    }
                }
            } else {
                docT.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
            }

            // ==================== 5. SALVA TESTATA ====================
            docT.setToBeUpdated();
            updateBulk(aUC, docT);

            // ==================== 6. SALVA ALLEGATI ====================
            if (docT.getArchivioAllegati() != null) {
                for (AllegatoGenericoBulk allegato : docT.getArchivioAllegati()) {
                    if (allegato != null && allegato.getCrudStatus() != OggettoBulk.NORMAL) {

                        if (allegato.getCrudStatus() == OggettoBulk.TO_BE_CREATED) {
                            insertBulk(aUC, allegato);

                        } else if (allegato.getCrudStatus() == OggettoBulk.TO_BE_UPDATED) {
                            updateBulk(aUC, allegato);

                        } else if (allegato.getCrudStatus() == OggettoBulk.TO_BE_DELETED) {
                            deleteBulk(aUC, allegato);
                        }
                    }
                }
            }

            // ==================== 7. AGGIORNA RIFERIMENTI BIDIREZIONALI (solo RIENTRO) ====================
            if (docT instanceof DocumentoRientroBulk && !dettagliTrasportoDaAggiornare.isEmpty()) {
                aggiornaRiferimentiInversi(aUC, docT, dettagliTrasportoDaAggiornare);
            }

            // ==================== 8. RICARICA DOCUMENTO COMPLETO ====================
            docT = (Doc_trasporto_rientroBulk) docHome.findByPrimaryKey(docT);

            if (docT == null) {
                throw new ComponentException("Errore ricaricamento documento dopo salvataggio");
            }

            // Ricarica dettagli
            docT.setDoc_trasporto_rientro_dettColl(
                    new BulkList(dettHome.getDetailsFor(docT))
            );

            // ==================== 9. INIZIALIZZA BENI NEI DETTAGLI ====================
            if (docT.getDoc_trasporto_rientro_dettColl() != null) {
                for (Iterator dett = docT.getDoc_trasporto_rientro_dettColl().iterator(); dett.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) dett.next();
                    dettaglio.setDoc_trasporto_rientro(docT);

                    if (dettaglio.getNr_inventario() != null && dettaglio.getProgressivo() != null) {
                        Inventario_beniBulk inv = (Inventario_beniBulk)
                                getHome(aUC, Inventario_beniBulk.class)
                                        .findByPrimaryKey(new Inventario_beniBulk(
                                                dettaglio.getNr_inventario(),
                                                dettaglio.getPg_inventario(),
                                                Long.valueOf(dettaglio.getProgressivo())
                                        ));
                        dettaglio.setBene(inv);
                    }
                }
            }

            return docT;

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            throw new ComponentException(e);
        }
    }



    private void validaDoc(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {

            validaDataDoc(aUC, docT);

            // Determina il tipo di documento per il messaggio di errore
            String tipoDoc = "Documento di";
            if (RIENTRO.equals(docT.getTiDocumento())) {
                tipoDoc = "Rientro";
            } else if (TRASPORTO.equals(docT.getTiDocumento())) {
                tipoDoc = "Trasporto";
            }


            if(!docT.getDoc_trasporto_rientro_dettColl().isEmpty()){
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) docT.getDoc_trasporto_rientro_dettColl().get(0);
                if(dett.getCrudStatus()==OggettoBulk.NORMAL){
                    if (docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                        throw new it.cnr.jada.comp.ApplicationException(
                                "Attenzione: data del " + tipoDoc + " non valida.\n" +
                                        "La Data del " + tipoDoc + " non può essere precedente ad una modifica di uno dei beni movimentati."
                        );
                    }
                }
            }

            if (docT.getTipoMovimento() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: specificare un Tipo di Movimento per il Documento");

            if (docT.getDsDocTrasportoRientro() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare una Descrizione per il Documento");
        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    private void validaDataDoc(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {
            Timestamp dataOdiernaTimestamp = getHome(aUC, Doc_trasporto_rientroBulk.class).getServerDate();
            Timestamp dataDiTrasporto = docT.getDataRegistrazione();
            if (dataDiTrasporto == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare una Data di Trasporto");

            // Converti la data di Trasporto in LocalDate
            LocalDate dataTrasportoSoloGiorno = dataDiTrasporto.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Converti la data odierna in LocalDate
            LocalDate dataOdiernaSoloGiorno = dataOdiernaTimestamp.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Confronta solo il giorno/mese/anno
            if (dataTrasportoSoloGiorno.isAfter(dataOdiernaSoloGiorno)) {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: data di Trasporto non valida. La Data di Trasporto non può essere superiore alla data odierna");
            }
            // CONTROLLA LA DATA DI Trasporto - DATA DI Trasporto ALL'INTERNO DELL'ESERCIZIO DI SCRIVANIA
            java.sql.Timestamp firstDayOfYear = it.cnr.contab.doccont00.comp.DateServices.getFirstDayOfYear(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC).intValue());
            java.sql.Timestamp lastDayOfYear = it.cnr.contab.doccont00.comp.DateServices.getLastDayOfYear(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC).intValue());
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy");
            if (docT.getDataRegistrazione().before(firstDayOfYear)) {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: data di  Trasporto non valida. La data di Trasporto non può essere inferiore di " + formatter.format(firstDayOfYear));
            }
            if (docT.getDataRegistrazione().after(lastDayOfYear)) {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: data di  Trasporto non valida. La data di  Trasporto non può essere maggiore di " + formatter.format(lastDayOfYear));
            }
        } catch (Throwable t) {
            throw handleException(t);
        }
    }


    // ========================================
// TRANSIZIONI STATO
// ========================================

    /**
     * Predispone il documento alla firma
     * Cambia lo stato da INSERITO a INVIATO
     *
     * @param userContext contesto utente
     * @param docTR       documento da predisporre
     * @return documento aggiornato
     * @throws ComponentException in caso di errore
     */
    public Doc_trasporto_rientroBulk changeStatoInInviato(UserContext userContext, Doc_trasporto_rientroBulk docTR)
            throws ComponentException {
        try {
            // Validazione stato
            if (!Doc_trasporto_rientroBulk.STATO_INSERITO.equals(docTR.getStato())) {
                throw new ApplicationException("Stato deve essere INSERITO per inviare alla firma.");
            }

            // ==================== CAMBIA STATO A INVIATO ====================
            docTR.setStato(Doc_trasporto_rientroBulk.STATO_INVIATO);
            docTR.setStatoFlusso("INV");

            // ID flusso e data invio sono già impostati dall'Action
            // Se non sono impostati, genera errore
            if (docTR.getIdFlussoHappysign() == null || docTR.getIdFlussoHappysign().isEmpty()) {
                throw new ApplicationException("ID flusso HappySign non impostato");
            }

            if (docTR.getDataInvioFirma() == null) {
                docTR.setDataInvioFirma(new Timestamp(System.currentTimeMillis()));
            }

            docTR.setToBeUpdated();

            // ==================== SALVA UNA SOLA VOLTA ====================
            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException("Errore predisposizione firma: " + e.getMessage(), e);
        }
    }





// ========================================
// METODI UTILITY
// ========================================

    protected void inizializzaTipoMovimento(UserContext userContext, OggettoBulk oggettoBulk)
            throws ComponentException {
        try {
            Tipo_trasporto_rientroHome tipoHome = (Tipo_trasporto_rientroHome)
                    getHome(userContext, Tipo_trasporto_rientroBulk.class);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) oggettoBulk;

            java.util.Collection tipi = tipoHome.findTipiPerDocumento(userContext, doc.getTiDocumento());
            doc.setTipoMovimenti(tipi);

            if (tipi != null && tipi.size() == 1) {
                doc.setTipoMovimento((Tipo_trasporto_rientroBulk) tipi.iterator().next());
            }
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        super.initializeKeysAndOptionsInto(userContext, oggettoBulk);
    }

    public Id_inventarioBulk caricaInventario(UserContext aUC)
            throws ComponentException, PersistencyException, IntrospectionException {
        Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
        Id_inventarioBulk inventario = invHome.findInventarioFor(aUC, false);
        if (inventario == null) {
            throw new ApplicationException("Nessun inventario associato alla UO!");
        }
        return inventario;
    }

    public String getLocalTransactionID(UserContext aUC, boolean force)
            throws ComponentException, PersistencyException, IntrospectionException {
        LoggableStatement cs = null;
        try {
            cs = new LoggableStatement(getConnection(aUC),
                    "{ ? = call " + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                            "IBMUTL001.getLocalTransactionID(" + (force ? "TRUE" : "FALSE") + ")}",
                    false, this.getClass());
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            cs.executeQuery();
            return cs.getString(1);
        } catch (Throwable e) {
            throw handleException(e);
        } finally {
            try {
                if (cs != null) cs.close();
            } catch (java.sql.SQLException e) {
                throw handleException(e);
            }
        }
    }

    public RemoteIterator selectEditDettagliTrasporto(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR,
            Class bulkClass,
            CompoundFindClause clauses) throws ComponentException {

        if (docTR == null || docTR.getPgDocTrasportoRientro() == null)
            return new it.cnr.jada.util.EmptyRemoteIterator();

        SQLBuilder sql = getHome(userContext, ( docTR.isRientro()?DocumentoRientroDettBulk.class:DocumentoTrasportoDettBulk.class)).createSQLBuilder();

        sql.addSQLClause("AND", "PG_INVENTARIO", sql.EQUALS, docTR.getPgInventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
        sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS, docTR.getPgDocTrasportoRientro());

        try {
            it.cnr.jada.util.RemoteIterator ri = iterator(userContext, sql, bulkClass, null);
            return ri;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public RemoteIterator getListaBeniDaTrasportare(
            UserContext userContext,
            OggettoBulk bulk,
            SimpleBulkList beni_da_escludere,
            CompoundFindClause clauses) throws ComponentException {

        try {
            SQLBuilder sql = new SQLBuilder();
            Inventario_beniHome invBeniHome = (Inventario_beniHome) getHome(userContext, Inventario_beniBulk.class);
            sql = invBeniHome.getListaBeniDaTrasportare(
                    userContext
                    , (Doc_trasporto_rientroBulk) bulk
                    , beni_da_escludere);

            sql.addClause(clauses);
            try {
                return iterator(userContext, sql, Inventario_beniBulk.class, null);
            } catch (Exception e) {
                throw handleException(e);
            }
        } catch (PersistencyException ex) {
            throw handleException(ex);
        } catch (it.cnr.jada.persistency.IntrospectionException ex) {
            throw handleException(ex);
        }
    }

    public SimpleBulkList selezionati(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws it.cnr.jada.comp.ComponentException {

        // Se il documento non è ancora salvato, ritorna lista vuota
        if (docT.getPgDocTrasportoRientro() == null) {
            return new SimpleBulkList();
        }

        try {
            // Query su DOC_TRASPORTO_RIENTRO_DETT con JOIN a INVENTARIO_BENI
            SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();

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

            List risultato = getHome(userContext, Inventario_beniBulk.class).fetchAll(sql);

            if (risultato != null && risultato.size() > 0) {
                return new SimpleBulkList(risultato);
            }

        } catch (PersistencyException pe) {
            throw new ComponentException(pe);
        }

        return new SimpleBulkList();
    }

    /**
     * Seleziona beni trasportati
     * PreCondition:
     * E' stata generata la richiesta di cercare i beni trasportati.
     * PostCondition:
     * Viene restituito un Iteratore sui beni presenti sulla tabella INVENTARIO_BENI_APG
     *
     * @param userContext lo <code>UserContext</code> che ha generato la richiesta
     * @param docT        il Documento
     * @param bulkClass   la <code>Class</code> modello per il dettaglio.
     * @return iterator <code>RemoteIterator</code> l'Iteratore sulla selezione.
     **/
    public RemoteIterator selectBeniAssociatiByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            Class bulkClass) throws ComponentException {

        // ========== SEMPRE SU DETTAGLIO ==========
        // Se il documento non ha PG, ritorna iteratore vuoto
        if (docT.getPgDocTrasportoRientro() == null) {
            return new it.cnr.jada.util.EmptyRemoteIterator();
        }

        try {
            SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();

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




    /**
     * Restituisce, tra tutti i beni trasportati nel Documento la MAX(dt_validita_variazione),
     * ossia, la data corrispondente alla modifica più recente.
     **/
    private java.sql.Timestamp getMaxDataFor(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        // Se documento non salvato, non ha dettagli
        if (docT.getPgDocTrasportoRientro() == null) {
            throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");
        }

        try {
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
                    getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            Timestamp max_data = dettHome.getMaxDataValiditaVariazione(docT);

            if (max_data == null) {
                throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");
            }

            return max_data;

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }


    /**
     * Completa l'inizializzazione dei riferimenti tra testata e dettagli.
     * Carica i beni associati dal database.
     */
    private void inizializzaDettagli(UserContext userContext, Doc_trasporto_rientroBulk testata)
            throws ComponentException {
        try {
            if (testata.getDoc_trasporto_rientro_dettColl() == null ||
                    testata.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                return;
            }

            Inventario_beniHome beneHome = (Inventario_beniHome) getHome(userContext, Inventario_beniBulk.class);

            for (Iterator it = testata.getDoc_trasporto_rientro_dettColl().iterator(); it.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) it.next();

                // Sostituisci l'oggetto doc_trasporto_rientro placeholder con quello reale
                dettaglio.setDoc_trasporto_rientro(testata);

                // Carica il bene completo dal database
                if (dettaglio.getNr_inventario() != null && dettaglio.getProgressivo() != null) {
                    Inventario_beniBulk beneDaCaricare = new Inventario_beniBulk(
                            dettaglio.getNr_inventario(),
                            dettaglio.getPg_inventario(),
                            Long.valueOf(dettaglio.getProgressivo())
                    );
                    Inventario_beniBulk beneCaricato = (Inventario_beniBulk) beneHome.findByPrimaryKey(beneDaCaricare);
                    dettaglio.setBene(beneCaricato);
                }
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    /**
     * Trova i beni accessori associati a un bene principale
     * <p>
     * Cerca tutti i beni con lo stesso PG_INVENTARIO e NR_INVENTARIO,
     * ma esclude il bene stesso (per evitare di trovare se stesso nel caso
     * in cui il bene selezionato sia già un accessorio).
     */
    public List cercaBeniAccessoriAssociati(
            UserContext userContext,
            Inventario_beniBulk benePrincipale)
            throws ComponentException {

        try {
            Inventario_beniHome home = (Inventario_beniHome) getHome(
                    userContext,
                    Inventario_beniBulk.class
            );

            SQLBuilder sql = home.createSQLBuilder();

            // Cerca i beni con lo stesso inventario e numero bene
            sql.addSQLClause("AND", "PG_INVENTARIO",
                    SQLBuilder.EQUALS, benePrincipale.getPg_inventario());
            sql.addSQLClause("AND", "NR_INVENTARIO",
                    SQLBuilder.EQUALS, benePrincipale.getNr_inventario());

            // Filtra per accessori (PROGRESSIVO > 0, non principale)
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

            // Escludi il bene stesso
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS, benePrincipale.getProgressivo());

            List beniAccessori = home.fetchAll(sql);

            if (beniAccessori != null && !beniAccessori.isEmpty()) {
                return beniAccessori;
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Modifica i beni trasportati e RITORNA il documento aggiornato.
     * SEMPRE salva/elimina direttamente sul DB (sia in inserimento che in modifica).
     */
    public Doc_trasporto_rientroBulk modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            BitSet old_ass,
            BitSet ass)
            throws ComponentException {

        try {
            // ========== INIZIALIZZA LA COLLECTION SE NON ESISTE ==========
            if (docT.getDoc_trasporto_rientro_dettColl() == null) {
                docT.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
            }

            boolean isTrasporto = docT instanceof DocumentoTrasportoBulk;

            for (int i = 0; i < beni.length; i++) {
                Inventario_beniBulk bene = (Inventario_beniBulk) beni[i];

                if (old_ass.get(i) != ass.get(i)) {

                    if (ass.get(i)) {
                        // ========== AGGIUNTA BENE ==========
                        try {
                            lockBulk(userContext, bene);
                        } catch (OutdatedResourceException oe) {
                            throw handleException(oe);
                        } catch (BusyResourceException bre) {
                            throw new ApplicationException(
                                    "Risorsa occupata.\nIl bene " +
                                            bene.getNumeroBeneCompleto() +
                                            " è bloccato da un altro utente."
                            );
                        } catch (PersistencyException pe) {
                            throw handleException(pe);
                        }

                        if (isTrasporto) {
                            // ========== CREA DETTAGLIO TRASPORTO ==========
                            DocumentoTrasportoDettBulk dett = new DocumentoTrasportoDettBulk();
                            dett.setDocumentoTrasporto((DocumentoTrasportoBulk) docT);
                            dett.setBene(bene);

                            // Imposta chiavi primarie
                            dett.setPg_inventario(docT.getPgInventario());
                            dett.setTi_documento(docT.getTiDocumento());
                            dett.setEsercizio(docT.getEsercizio());
                            dett.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
                            dett.setNr_inventario(bene.getNr_inventario());
                            dett.setProgressivo(bene.getProgressivo().intValue());

                            // Imposta CD_TERZO_ASSEGNATARIO
                            if (docT.isSmartworking() && docT.getTerzoSmartworking() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoSmartworking().getCd_terzo());
                            } else if (docT.getFlIncaricato() && docT.getTerzoIncRitiro() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoIncRitiro().getCd_terzo());
                            } else if (bene.getCd_assegnatario() != null) {
                                dett.setCdTerzoAssegnatario(bene.getCd_assegnatario());
                            }

                            // Cerca RIENTRO precedente (opzionale)
                            Doc_trasporto_rientro_dettBulk dettaglioRientroOriginale =
                                    trovaDettaglioOriginale(userContext, bene,
                                            docT.getPgInventario(), RIENTRO);

                            if (dettaglioRientroOriginale != null) {
                                dett.setDocRientroDettRif((DocumentoRientroDettBulk) dettaglioRientroOriginale);
                            }

                            dett.setToBeCreated();

                            // ✅ AGGIUNGI ALLA COLLECTION IN MEMORIA
                            docT.getDoc_trasporto_rientro_dettColl().add(dett);

                            // ✅ SALVA SUBITO SUL DB
                            super.creaConBulk(userContext, docT);
                            dett.setToBeUpdated();

                        } else {
                            // ========== CREA DETTAGLIO RIENTRO ==========
                            DocumentoRientroDettBulk dett = new DocumentoRientroDettBulk();
                            dett.setDocumentoRientro((DocumentoRientroBulk) docT);
                            dett.setBene(bene);

                            // Imposta chiavi primarie
                            dett.setPg_inventario(docT.getPgInventario());
                            dett.setTi_documento(docT.getTiDocumento());
                            dett.setEsercizio(docT.getEsercizio());
                            dett.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
                            dett.setNr_inventario(bene.getNr_inventario());
                            dett.setProgressivo(bene.getProgressivo().intValue());

                            // Imposta CD_TERZO_ASSEGNATARIO
                            if (docT.isSmartworking() && docT.getTerzoSmartworking() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoSmartworking().getCd_terzo());
                            } else if (docT.getFlIncaricato() && docT.getTerzoIncRitiro() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoIncRitiro().getCd_terzo());
                            } else if (bene.getCd_assegnatario() != null) {
                                dett.setCdTerzoAssegnatario(bene.getCd_assegnatario());
                            }

                            // Cerca TRASPORTO originale (OBBLIGATORIO)
                            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                                    trovaDettaglioOriginale(userContext, bene,
                                            docT.getPgInventario(), TRASPORTO);

                            if (dettaglioTrasportoOriginale == null) {
                                throw new ApplicationException(
                                        "Bene " + bene.getNumeroBeneCompleto() +
                                                " non presente in nessun documento di trasporto firmato (stato DEFINITIVO)!");
                            }

                            dett.setDocTrasportoDettRif((DocumentoTrasportoDettBulk) dettaglioTrasportoOriginale);
                            dett.setToBeCreated();

                            // ✅ AGGIUNGI ALLA COLLECTION IN MEMORIA
                            docT.getDoc_trasporto_rientro_dettColl().add(dett);

                            // ✅ SALVA SUBITO SUL DB
                            super.creaConBulk(userContext, docT);
                            dett.setToBeUpdated();
                        }

                    } else {
                        // ========== RIMOZIONE BENE ==========
                        SimpleBulkList collection = docT.getDoc_trasporto_rientro_dettColl();

                        if (collection != null) {
                            Doc_trasporto_rientro_dettBulk dettaglioDaEliminare = null;

                            // Cerca il dettaglio da rimuovere
                            for (Iterator it = collection.iterator(); it.hasNext(); ) {
                                Doc_trasporto_rientro_dettBulk dett =
                                        (Doc_trasporto_rientro_dettBulk) it.next();

                                if (dett.getNr_inventario().equals(bene.getNr_inventario()) &&
                                        dett.getProgressivo().equals(bene.getProgressivo().intValue())) {

                                    dettaglioDaEliminare = dett;

                                    // ✅ RIMUOVI DALLA COLLECTION
                                    it.remove();
                                    break;
                                }
                            }

                            // ✅ ELIMINA SEMPRE DAL DB (se trovato)
                            if (dettaglioDaEliminare != null) {
                                dettaglioDaEliminare.setToBeDeleted();
                                super.eliminaConBulk(userContext, dettaglioDaEliminare);
                            }
                        }
                    }
                }
            }

            // ✅ RITORNA IL DOCUMENTO AGGIORNATO
            return docT;

        } catch (Throwable e) {
            throw handleException(e);
        }
    }





    /**
     * Ottiene la lista dei beni disponibili per il rientro (wrapper)
     */
    public RemoteIterator getListaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException {

        return cercaBeniDaFarRientrare(userContext, doc, beniEsclusi, clauses);
    }


    /**
     * Modifica i beni rientrati con gestione accessori
     * Stessa logica di modificaBeniTrasportatiConAccessori
     */
    public void modificaBeniRientratiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException {

        // Riusa la stessa implementazione del trasporto
        // TODO in caso da modificare
        modificaBeniTrasportatiConAccessori(userContext, doc, beni, oldSelection, newSelection);
    }


    /**
     * Seleziona i dettagli del documento di rientro per modifica
     * Stessa logica di selectEditDettagliTrasporto
     */
    public RemoteIterator selectEditDettagliRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Class bulkClass,
            CompoundFindClause filters)
            throws ComponentException {

        // Riusa la stessa implementazione del trasporto
        // TODO in caso da modificare
        return selectEditDettagliTrasporto(userContext, doc, bulkClass, filters);
    }


    /**
     * Viene richiamata la funziona che controlla se l'esercizio coep è chiuso
     */
    public boolean isEsercizioCOEPChiuso(UserContext userContext) throws ComponentException {
        LoggableStatement cs = null;
        String status = null;
        try {
            cs = new LoggableStatement(getConnection(userContext),
                    "{ ? = call " + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                            "CNRCTB200.isChiusuraCoepDef(?,?)}", false, this.getClass());
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            cs.setObject(2, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));
            cs.setObject(3, it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(userContext));
            cs.executeQuery();
            status = new String(cs.getString(1));
            if (status.compareTo("Y") == 0)
                return true;
            //controlla anche se è chiuso l'inventario
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);
            Id_inventarioBulk inventario = inventarioHome.findInventarioFor(userContext, false);
            if (!inventarioHome.isAperto(inventario, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext)))
                return true;

        } catch (java.sql.SQLException ex) {
            throw handleException(ex);
        } catch (PersistencyException e) {
            throw handleException(e);
        } catch (IntrospectionException e) {
            throw handleException(e);
        } finally {
            try {
                if (cs != null)
                    cs.close();
            } catch (java.sql.SQLException e) {
                throw handleException(e);
            }
        }
        return false;
    }




    /**
     * Recupera i documenti predisposti alla firma con filtri aggiuntivi.
     *
     * @param userContext contesto utente
     * @return lista dei documenti predisposti alla firma
     * @throws ComponentException in caso di errore
     */
    public List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home = (Doc_trasporto_rientroHome)
                    getHome(userContext, Doc_trasporto_rientroBulk.class);

            SQLBuilder sql = home.createSQLBuilder();

            // ==================== FILTRI PRINCIPALI ====================

            // 1. Stato = INVIATO
            sql.addSQLClause("AND", "STATO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.STATO_INVIATO);

            // 2. StatoFlusso = INV (inviato al flusso)
            sql.addSQLClause("AND", "STATO_FLUSSO", SQLBuilder.EQUALS, "INV");

            // 3. ID flusso HappySign valorizzato
            sql.addSQLClause("AND", "ID_FLUSSO_HAPPYSIGN", SQLBuilder.ISNOTNULL, null);

            // ==================== FILTRI AGGIUNTIVI ====================

            // 4. Solo documenti dell'esercizio corrente o precedente
            Integer esercizioCorrente = CNRUserContext.getEsercizio(userContext);
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.LESS_EQUALS, esercizioCorrente);

//            // 5. OPZIONALE: Solo documenti inviati negli ultimi N giorni
//            // (evita di controllare documenti troppo vecchi)
//            java.sql.Timestamp limitDate = new java.sql.Timestamp(
//                    System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 giorni fa
//            );
//            sql.addSQLClause("AND", "DATA_INVIO_FIRMA", SQLBuilder.GREATER_EQUALS, limitDate);

            // ==================== ORDINAMENTO ====================
            // Ordina per data invio (più vecchi prima = priorità maggiore)
            sql.addOrderBy("DATA_INVIO_FIRMA ASC");

            // ==================== ESECUZIONE ====================
            List documenti = home.fetchAll(sql);

            System.out.println(String.format("Recuperati %d documenti predisposti alla firma",
                    documenti != null ? documenti.size() : 0));

            return documenti != null ? documenti : java.util.Collections.emptyList();

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    /**
     * stampaConBulk method comment.
     */
    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(it.cnr.jada.UserContext aUC, it.cnr.jada.bulk.OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {
        validateBulkForPrint(aUC, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }


    /**
     * inizializzaBulkPerStampa method comment.
     */
    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(it.cnr.jada.UserContext userContext, it.cnr.jada.bulk.OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {

        if (bulk instanceof Stampa_doc_trasporto_rientroBulk)
            inizializzaBulkPerStampa(userContext, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }

    /**
     * inizializzaBulkPerStampa method comment.
     */
    public void inizializzaBulkPerStampa(UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa) throws it.cnr.jada.comp.ComponentException {

        Stampa_doc_trasporto_rientroBulk stampaDoc = (Stampa_doc_trasporto_rientroBulk) stampa;
        //TODO da sostituire (con quale oggetto?)
//        stampaDoc.setObbligazione(new ObbligazioneBulk());
        stampaDoc.setEsercizio(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));
        stampaDoc.setCds_scrivania(it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(userContext));
        stampaDoc.setPgInizio(new Integer(0));
        stampaDoc.setPgFine(new Integer(999999999));

        try {
            String cd_uo_scrivania = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(userContext);

            Unita_organizzativaHome uoHome = (Unita_organizzativaHome) getHome(userContext, Unita_organizzativaBulk.class);
            Unita_organizzativaBulk uo = (Unita_organizzativaBulk) uoHome.findByPrimaryKey(new Unita_organizzativaBulk(cd_uo_scrivania));

            if (!uo.isUoCds()) {
                stampaDoc.setUoForPrint(uo);
                stampaDoc.setIsUOForPrintEnabled(false);
            } else {
                stampaDoc.setUoForPrint(new Unita_organizzativaBulk());
                stampaDoc.setIsUOForPrintEnabled(true);
            }

        } catch (it.cnr.jada.persistency.PersistencyException pe) {
            throw new ComponentException(pe);
        }
    }

    /**
     * Validazione dell'oggetto in fase di stampa
     */
    private void validateBulkForPrint(it.cnr.jada.UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa) throws ComponentException {

        try {
            /**** Controlli sui PG_INIZIO/PG_FINE *****/
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


    /**
     * Salva il documento in stato DEFINITIVO.
     *
     * Questo metodo:
     * 1. Valida il documento e verifica la presenza di un allegato firmato
     * 2. Carica i dettagli associati
     * 3. Aggiorna FL_BENE_IN_IST in base al tipo di documento:
     *    - TRASPORTO → FL_BENE_IN_IST = FALSE (bene esce dall'istituto)
     *    - RIENTRO → FL_BENE_IN_IST = TRUE (bene rientra nell'istituto)
     * 4. Cambia lo stato a DEFINITIVO e persiste le modifiche
     *
     * @param userContext Contesto utente
     * @param docTR Documento da rendere definitivo
     * @return Documento aggiornato in stato DEFINITIVO
     * @throws ComponentException Se validazione o persistenza falliscono
     */
    public Doc_trasporto_rientroBulk salvaDefinitivo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR)
            throws ComponentException {

        try {
            // ==================== VALIDAZIONI ====================
            validaDoc(userContext, docTR);

            if (!hasAllegatoFirmato(docTR)) {
                throw new ApplicationException(
                        "È obbligatorio allegare il documento firmato prima di rendere definitivo il documento."
                );
            }

            // ==================== CARICAMENTO DETTAGLI ====================
            caricaDettagliDocumento(userContext, docTR);

            // ==================== AGGIORNAMENTO STATO BENI ====================
            boolean beneInIstituto = docTR instanceof DocumentoRientroBulk;
            aggiornaStatoBeni(userContext, docTR, beneInIstituto);

            // ==================== CAMBIO STATO E PERSISTENZA ====================
            docTR.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            docTR.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore durante il salvataggio definitivo del documento: " + e.getMessage(), e);
        }
    }

    /**
     * Annulla logicamente il documento e rimuove i riferimenti incrociati nei dettagli.
     *
     * LOGICA:
     * - TRASPORTO: può essere annullato SOLO se NON ha documenti di RIENTRO collegati
     *              Quando annullato, FL_BENE_IN_IST = TRUE (bene torna nell'istituto)
     * - RIENTRO: può essere annullato liberamente, eliminando i riferimenti dal TRASPORTO associato
     *            Quando annullato, FL_BENE_IN_IST = FALSE (bene torna fuori dall'istituto)
     *
     * @param userContext Contesto utente
     * @param doc Documento da annullare
     * @return Documento annullato con TUTTE le relazioni caricate
     * @throws ComponentException in caso di errore
     */
    public Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            // ==================== CARICAMENTO E VALIDAZIONI ====================
            caricaDettagliDocumento(userContext, doc);
            validaAnnullamento(doc);

            // ==================== LOGICA SPECIFICA PER TIPO DOCUMENTO ====================
            boolean beneInIstituto;

            if (doc instanceof DocumentoTrasportoBulk) {
                verificaNonEsistonoRientri((DocumentoTrasportoBulk) doc);
                beneInIstituto = true; // TRASPORTO annullato → bene torna nell'istituto

            } else if (doc instanceof DocumentoRientroBulk) {
                rimuoviRiferimentiDaTrasportoOriginale((DocumentoRientroBulk) doc);
                beneInIstituto = false; // RIENTRO annullato → bene torna fuori dall'istituto

            } else {
                throw new ComponentException(
                        "Tipo documento non riconosciuto: " + doc.getClass().getName());
            }

            // ==================== AGGIORNAMENTO STATO BENI ====================
            aggiornaStatoBeni(userContext, doc, beneInIstituto);

            // ==================== CAMBIO STATO E PERSISTENZA ====================
            doc.setStato(Doc_trasporto_rientroBulk.STATO_ANNULLATO);
            doc.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, doc);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore durante l'annullamento del documento: " + e.getMessage(), e);
        }
    }

// ==================== METODI DI SUPPORTO ====================

    /**
     * Carica i dettagli del documento dal database.
     */
    private void caricaDettagliDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException, PersistencyException {

        Doc_trasporto_rientro_dettHome dettHome =
                (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

        doc.setDoc_trasporto_rientro_dettColl(
                new BulkList(dettHome.getDetailsFor(doc))
        );
    }

    /**
     * Valida che il documento possa essere annullato.
     */
    private void validaAnnullamento(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        if (Doc_trasporto_rientroBulk.STATO_ANNULLATO.equals(doc.getStato())) {
            throw new ApplicationException("Il documento è già annullato");
        }

        if (Doc_trasporto_rientroBulk.STATO_INVIATO.equals(doc.getStato())) {
            throw new ApplicationException(
                    "Impossibile annullare un documento inviato in firma"
            );
        }
    }

    /**
     * Verifica che il documento di TRASPORTO non abbia rientri collegati.
     */
    private void verificaNonEsistonoRientri(DocumentoTrasportoBulk doc)
            throws ApplicationException {

        if (doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            return;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            DocumentoTrasportoDettBulk dettaglio = (DocumentoTrasportoDettBulk) obj;

            if (dettaglio.getDocRientroDettRif() != null) {
                throw new ApplicationException(
                        "Impossibile annullare il documento di TRASPORTO: " +
                                "esistono documenti di RIENTRO collegati. " +
                                "Annullare prima i documenti di RIENTRO."
                );
            }
        }
    }

    /**
     * Rimuove i riferimenti dal trasporto originale quando si annulla un RIENTRO.
     */
    private void rimuoviRiferimentiDaTrasportoOriginale(DocumentoRientroBulk doc)
            throws ComponentException {

        if (doc.getDoc_trasporto_rientro_dettColl() == null) {
            return;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            Doc_trasporto_rientro_dettBulk dettRientro =
                    (Doc_trasporto_rientro_dettBulk) obj;

            if (dettRientro.getDoc_trasporto_rientroDettRif() != null) {
                Doc_trasporto_rientro_dettBulk docTrasportoDettRif =
                        dettRientro.getDoc_trasporto_rientroDettRif();

                // Rimuove riferimenti incrociati
                docTrasportoDettRif.setDoc_trasporto_rientroDettRif(null);
//                docTrasportoDettRif.setEsercizioRif(null);
//                docTrasportoDettRif.setTiDocumentoRif(null);
//                docTrasportoDettRif.setPgDocTrasportoRientroRif(null);
                docTrasportoDettRif.setToBeUpdated();
            }
        }
    }

    /**
     * Aggiorna il campo FL_BENE_IN_IST per tutti i beni del documento.
     *
     * LOGICA UNIFICATA:
     * - TRASPORTO DEFINITIVO → FL_BENE_IN_IST = FALSE (bene esce)
     * - TRASPORTO ANNULLATO → FL_BENE_IN_IST = TRUE (bene rientra)
     * - RIENTRO DEFINITIVO → FL_BENE_IN_IST = TRUE (bene rientra)
     * - RIENTRO ANNULLATO → FL_BENE_IN_IST = FALSE (bene esce di nuovo)
     *
     * @param userContext Contesto utente
     * @param doc Documento di trasporto/rientro
     * @param beneInIstituto TRUE se il bene deve essere nell'istituto, FALSE altrimenti
     * @throws ComponentException in caso di errore
     */
    private void aggiornaStatoBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            boolean beneInIstituto)
            throws ComponentException {

        try {
            if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                    doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                return;
            }

            Inventario_beniHome beneHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
                Doc_trasporto_rientro_dettBulk dettaglio =
                        (Doc_trasporto_rientro_dettBulk) obj;

                // Carica e aggiorna il bene
                Inventario_beniBulk bene = caricaBeneDaDettaglio(
                        userContext, beneHome, dettaglio);

                if (bene != null) {
                    bene.setFl_bene_in_ist(beneInIstituto);
                    bene.setToBeUpdated();
                    updateBulk(userContext, bene);
                }
            }

        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore durante l'aggiornamento dello stato dei beni: " +
                            e.getMessage(), e);
        }
    }

    /**
     * Carica un bene dal database usando le informazioni del dettaglio.
     */
    private Inventario_beniBulk caricaBeneDaDettaglio(
            UserContext userContext,
            Inventario_beniHome beneHome,
            Doc_trasporto_rientro_dettBulk dettaglio)
            throws PersistencyException {

        return (Inventario_beniBulk) beneHome.findByPrimaryKey(
                new Inventario_beniBulk(
                        dettaglio.getNr_inventario(),
                        dettaglio.getPg_inventario(),
                        Long.valueOf(dettaglio.getProgressivo())
                )
        );
    }

    /**
     * Verifica che il documento abbia almeno un allegato firmato.
     * VALIDAZIONE SERVER-SIDE DEFINITIVA.
     *
     * @param doc Documento da verificare
     * @return true se esiste almeno un allegato firmato valido, false altrimenti
     */
    private boolean hasAllegatoFirmato(Doc_trasporto_rientroBulk doc) {

        // Verifica documento valido
        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        // Determina l'aspect atteso in base al tipo di documento
        String aspectFirmatoAtteso;
        if (doc instanceof DocumentoTrasportoBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else if (doc instanceof DocumentoRientroBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else {
            return false;
        }

        // Cerca un allegato che abbia l'aspect di documento firmato
        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) obj;
                String aspectName = allegato.getAspectName();

                // Verifica aspect corretto e che non sia cancellato (gli allegati cancellati
                // rimangono nella collection con crudStatus=TO_BE_DELETED fino al salvataggio)
                if (aspectName != null
                        && aspectName.equals(aspectFirmatoAtteso)
                        && allegato.getCrudStatus() != OggettoBulk.TO_BE_DELETED) {
                    return true;
                }
            }
        }

        return false;
    }


    // ========================================
// SEARCHTOOL ANAGRAFICI - METODI SELECTBYCLAUSE
// ========================================

    /**
     * Metodo per la ricerca dell'anagrafico INCARICATO tramite SEARCHTOOL.
     * Delega la ricerca al componente Anagrafico per ottenere solo dipendenti attivi.
     */
    public SQLBuilder selectAnagIncRitiroByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            AnagraficoBulk anag_find,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {

        try {
            // Delega al componente Anagrafico per cercare solo dipendenti attivi
            AnagraficoComponentSession sess = (AnagraficoComponentSession)
                    it.cnr.jada.util.ejb.EJBCommonServices.createEJB(
                            "CNRANAGRAF00_EJB_AnagraficoComponentSession",
                            AnagraficoComponentSession.class
                    );

            return sess.findAnagraficoDipendenteByClause(userContext, anag_find, clause);

        } catch (Exception e) {
            throw new ComponentException("Errore ricerca anagrafico incaricato: " + e.getMessage(), e);
        }
    }

    /**
     * Metodo per la ricerca dell'anagrafico SMARTWORKING tramite SEARCHTOOL.
     * Delega la ricerca al componente Anagrafico per ottenere solo dipendenti attivi.
     */
    public SQLBuilder selectAnagSmartworkingByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            AnagraficoBulk anag_find,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {

        try {
            // Delega al componente Anagrafico per cercare solo dipendenti attivi
            AnagraficoComponentSession sess = (AnagraficoComponentSession)
                    it.cnr.jada.util.ejb.EJBCommonServices.createEJB(
                            "CNRANAGRAF00_EJB_AnagraficoComponentSession",
                            AnagraficoComponentSession.class
                    );

            return sess.findAnagraficoDipendenteByClause(userContext, anag_find, clause);

        } catch (Exception e) {
            throw new ComponentException("Errore ricerca anagrafico smartworking: " + e.getMessage(), e);
        }
    }


    /**
     * Cerca gli accessori di un bene principale CHE ERANO EFFETTIVAMENTE PRESENTI
     * nel documento di TRASPORTO originale (quello da cui si sta facendo il rientro).
     * Questo metodo è specifico per RIENTRO e serve a filtrare solo gli accessori
     * che erano stati effettivamente trasportati, evitando di proporre accessori
     * che il bene potrebbe avere ma che non erano nel trasporto.
     */
    public List cercaBeniAccessoriPresentinelTrasportoOriginale(
            UserContext userContext,
            Inventario_beniBulk beneRientro,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            if (beneRientro == null || doc == null) {
                return java.util.Collections.emptyList();
            }

            // ==================== TROVA IL TRASPORTO ORIGINALE ====================
            // Cerca il dettaglio del TRASPORTO da cui proviene questo bene
            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                    trovaDettaglioOriginale(userContext, beneRientro, doc.getPgInventario(), TRASPORTO);

            if (dettaglioTrasportoOriginale == null) {
                // Se non c'è trasporto originale, nessun accessorio da proporre
                return java.util.Collections.emptyList();
            }

            // ==================== CERCA GLI ACCESSORI NEL TRASPORTO ORIGINALE ====================
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

            SQLBuilder sql = dettHome.createSQLBuilder();

            // Filtra per lo stesso documento di trasporto
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getPg_inventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getTi_documento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro());

            // Filtra per stesso NR_INVENTARIO (stesso bene base)
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    beneRientro.getNr_inventario());

            // Filtra solo ACCESSORI (PROGRESSIVO > 0)
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

            // Escludi il bene stesso (se fosse un accessorio)
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    beneRientro.getProgressivo());

            // ==================== CARICA I DETTAGLI ACCESSORI ====================
            List dettagliAccessori = dettHome.fetchAll(sql);

            if (dettagliAccessori == null || dettagliAccessori.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // ==================== VERIFICA CHE NON SIANO GIÀ RIENTRATI ====================
            // Per ogni accessorio trovato nel trasporto, verifica che NON sia già rientrato
            Inventario_beniHome beneHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            List<Inventario_beniBulk> accessoriDisponibili = new ArrayList<>();

            for (Object obj : dettagliAccessori) {
                Doc_trasporto_rientro_dettBulk dettaglioAccessorio =
                        (Doc_trasporto_rientro_dettBulk) obj;

                // Verifica se questo accessorio è già rientrato
                boolean giaRientrato = verificaSeAccessorioGiaRientrato(
                        userContext,
                        dettaglioAccessorio
                );

                if (!giaRientrato) {
                    // Carica il bene completo
                    Inventario_beniBulk accessorio = (Inventario_beniBulk) beneHome.findByPrimaryKey(
                            new Inventario_beniBulk(
                                    dettaglioAccessorio.getNr_inventario(),
                                    dettaglioAccessorio.getPg_inventario(),
                                    Long.valueOf(dettaglioAccessorio.getProgressivo())
                            )
                    );

                    if (accessorio != null) {
                        accessoriDisponibili.add(accessorio);
                    }
                }
            }

            return accessoriDisponibili;

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Verifica se un accessorio del trasporto è già stato fatto rientrare.
     */
    private boolean verificaSeAccessorioGiaRientrato(
            UserContext userContext,
            Doc_trasporto_rientro_dettBulk dettaglioTrasportoAccessorio)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(
                            userContext, DocumentoRientroDettBulk.class);

            SQLBuilder sql = dettHome.createSQLBuilder();

            // Join con testata per verificare lo stato
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO dr");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "dr.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "dr.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "dr.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "dr.PG_DOC_TRASPORTO_RIENTRO");

            // Cerca rientri di questo specifico accessorio dal trasporto originale
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

            // Solo rientri non annullati
            sql.addSQLClause("AND", "dr.TI_DOCUMENTO", SQLBuilder.EQUALS, RIENTRO);
            sql.addSQLClause("AND", "dr.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            List risultati = dettHome.fetchAll(sql);

            return risultati != null && !risultati.isEmpty();

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }


    /**
     * Cerca beni trasportabili (wrapper del metodo unificato)
     */
    public RemoteIterator cercaBeniTrasportabili(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, true);
    }

    /**
     * Cerca beni da far rientrare (wrapper del metodo unificato)
     */
    public RemoteIterator cercaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException {
        return cercaBeniUnificato(userContext, doc, beniEsclusi, clauses, false);
    }


    /**
     * Metodo unificato per cercare beni trasportabili o da far rientrare.
     * Aggiunge i filtri specifici TRASPORTO/RIENTRO alla query base della Home.
     */
    public RemoteIterator cercaBeniUnificato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses,
            boolean isTrasporto)
            throws ComponentException {

        try {
            Inventario_beniHome invHome =
                    (Inventario_beniHome) getHome(
                            userContext, Inventario_beniBulk.class);

            // ========== COSTRUISCI QUERY BASE DALLA HOME ==========
            SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);

            // ========== AGGIUNGI FILTRI SPECIFICI PER TIPO ==========
            if (isTrasporto) {
                applicaFiltriTrasporto(sql, doc,userContext);
            } else {
                applicaFiltriRientro(sql, doc);
            }

            // ========== ESCLUDI BENI GIÀ INSERITI ==========
            if (beniEsclusi != null && !beniEsclusi.isEmpty()) {
                escludiBeniGiaSelezionati(sql, doc, beniEsclusi);
            }

            // ========== ORDER BY CON ALIAS ==========
            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            return iterator(userContext, sql, Inventario_beniBulk.class, null);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

// ==================== METODI PRIVATI - FILTRI SPECIFICI ====================

    private void applicaFiltriTrasporto(SQLBuilder sql, Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws ComponentException, PersistencyException, IntrospectionException {

        // ========== BENI NELL'ISTITUTO ==========
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "Y");

        // ========== FILTRO SMARTWORKING VS NORMALE ==========
        TerzoHome terzoHome = (TerzoHome) getHome(userContext, TerzoBulk.class);
        TerzoBulk terzo = null;

        // Recupera il terzo SOLO se anagrafico smartworking è valorizzato
        if (doc.getAnagSmartworking() != null && doc.getAnagSmartworking().getCd_anag() != null) {
            terzo = terzoHome.findTerzoByAnag(doc.getAnagSmartworking().getCd_anag());
        }

        if (doc.isSmartworking() && terzo != null && terzo.getCd_terzo() != null) {
            // Filtro per cd_assegnatario (il filtro UO è già stato escluso in applicaFiltriBaseComuni)
            sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.CD_ASSEGNATARIO",
                    SQLBuilder.EQUALS, terzo.getCd_terzo());
        }

        // ========== ESCLUDI BENI IN ALTRI DOCUMENTI DI TRASPORTO CON STATO INSERITO ==========
        sql.openParenthesis(FindClause.AND);

        sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.ISNULL, null);

        sql.openParenthesis(FindClause.OR);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, "T");
        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS,
                Doc_trasporto_rientroBulk.STATO_ANNULLATO);
        sql.addSQLClause(FindClause.OR, "t.STATO", SQLBuilder.NOT_EQUALS,
                Doc_trasporto_rientroBulk.STATO_INSERITO);
        sql.closeParenthesis();
        sql.closeParenthesis();

        sql.closeParenthesis();

        // ========== ESCLUDI DOCUMENTO CORRENTE SE IN EDITING ==========
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
     * Applica i filtri specifici per RIENTRO.
     */
    private void applicaFiltriRientro(SQLBuilder sql, Doc_trasporto_rientroBulk doc) {

        // ========== BENI FUORI DALL'ISTITUTO ==========
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "N");

        // ========== DEVE ESSERCI UN DOCUMENTO ASSOCIATO ==========
        sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO",
                SQLBuilder.ISNOTNULL, null);

        // ========== LOGICA COMPLESSA PER RIENTRO ==========
        sql.openParenthesis(FindClause.AND);

        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, "T");
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS,
                Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
        sql.closeParenthesis();

        sql.openParenthesis(FindClause.OR);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, "R");
        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS,
                Doc_trasporto_rientroBulk.STATO_ANNULLATO);
        sql.addSQLClause(FindClause.OR, "t.STATO", SQLBuilder.NOT_EQUALS,
                Doc_trasporto_rientroBulk.STATO_INSERITO);
        sql.closeParenthesis();
        sql.closeParenthesis();

        sql.closeParenthesis();

        // ========== ESCLUDI DOCUMENTO CORRENTE SE IN EDITING ==========
        if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
            sql.openParenthesis(FindClause.AND);

            sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO",
                    SQLBuilder.NOT_EQUALS, doc.getPgDocTrasportoRientro());
            sql.addSQLClause(FindClause.OR, "t.ESERCIZIO",
                    SQLBuilder.NOT_EQUALS, doc.getEsercizio());

            sql.closeParenthesis();
        }
    }

    /**
     * Esclude i beni già presenti nella lista di esclusione.
     */
    private void escludiBeniGiaSelezionati(
            SQLBuilder sql,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beni_da_escludere) {

        if (beni_da_escludere == null || beni_da_escludere.isEmpty()) {
            return;
        }

        StringBuilder exclusionList = new StringBuilder();

        for (Object obj : beni_da_escludere) {
            Inventario_beniBulk bene = (Inventario_beniBulk) obj;

            if (bene.getNr_inventario() != null && bene.getProgressivo() != null) {
                if (exclusionList.length() > 0) {
                    exclusionList.append(",");
                }

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
     * Elimina TUTTI i dettagli salvati di un documento.
     * Usato quando si fa "Elimina tutti" durante MODIFICA documento.
     *
     * @param userContext Contesto utente
     * @param doc Documento di cui eliminare i dettagli
     * @throws ComponentException in caso di errore
     */
    public void eliminaTuttiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ComponentException("Documento non valido per eliminazione dettagli");
        }

        PreparedStatement pstmt = null;

        try {
            // ========== DELETE DIRETTA SUI DETTAGLI SALVATI ==========
            String deleteSQL =
                    "DELETE FROM DOC_TRASPORTO_RIENTRO_DETT " +
                            "WHERE PG_INVENTARIO = ? " +
                            "  AND TI_DOCUMENTO = ? " +
                            "  AND ESERCIZIO = ? " +
                            "  AND PG_DOC_TRASPORTO_RIENTRO = ?";

            pstmt = getConnection(userContext).prepareStatement(deleteSQL);
            pstmt.setLong(1, doc.getPgInventario());
            pstmt.setString(2, doc.getTiDocumento());
            pstmt.setInt(3, doc.getEsercizio());
            pstmt.setLong(4, doc.getPgDocTrasportoRientro());

            int recordEliminati = pstmt.executeUpdate();

            System.out.println("Eliminati " + recordEliminati +
                    " dettagli dal documento " + doc.getPgDocTrasportoRientro());

        } catch (java.sql.SQLException e) {
            throw new ComponentException(
                    "Errore eliminazione dettagli salvati: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (java.sql.SQLException e) {
                System.out.println("Errore chiusura statement: " + e);
            }
        }
    }

    /**
     * Elimina dettagli specifici salvati di un documento.
     * Usato quando si selezionano beni da eliminare durante MODIFICA documento.
     *
     * @param userContext Contesto utente
     * @param doc Documento di cui eliminare i dettagli
     * @param beni Array di beni i cui dettagli devono essere eliminati
     * @throws ComponentException in caso di errore
     */
    public void eliminaDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException {

        if (beni == null || beni.length == 0) {
            return;
        }

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ComponentException("Documento non valido per eliminazione dettagli");
        }

        PreparedStatement pstmt = null;

        try {
            // ========== COSTRUISCI LA IN CLAUSE ==========
            StringBuilder inClause = new StringBuilder();

            for (int i = 0; i < beni.length; i++) {
                Inventario_beniBulk bene = (Inventario_beniBulk) beni[i];

                if (i > 0) {
                    inClause.append(",");
                }

                inClause.append("(")
                        .append(bene.getNr_inventario())
                        .append(",")
                        .append(bene.getProgressivo())
                        .append(")");
            }

            // ========== DELETE DIRETTA CON IN CLAUSE ==========
            String deleteSQL =
                    "DELETE FROM DOC_TRASPORTO_RIENTRO_DETT " +
                            "WHERE PG_INVENTARIO = ? " +
                            "  AND TI_DOCUMENTO = ? " +
                            "  AND ESERCIZIO = ? " +
                            "  AND PG_DOC_TRASPORTO_RIENTRO = ? " +
                            "  AND (NR_INVENTARIO, PROGRESSIVO) IN (" + inClause + ")";

            pstmt = getConnection(userContext).prepareStatement(deleteSQL);
            pstmt.setLong(1, doc.getPgInventario());
            pstmt.setString(2, doc.getTiDocumento());
            pstmt.setInt(3, doc.getEsercizio());
            pstmt.setLong(4, doc.getPgDocTrasportoRientro());

            int recordEliminati = pstmt.executeUpdate();

            System.out.println("Eliminati " + recordEliminati +
                    " dettagli specifici dal documento " + doc.getPgDocTrasportoRientro());

        } catch (java.sql.SQLException e) {
            throw new ComponentException(
                    "Errore eliminazione dettagli specifici salvati: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (java.sql.SQLException e) {
                System.out.println("Errore chiusura statement: " + e);
            }
        }
    }

    /**
     * Cerca accessori di un bene principale NEI DETTAGLI SALVATI.
     * Usato durante MODIFICA per sapere se un bene ha accessori già persistiti.
     *
     * @param userContext Contesto utente
     * @param doc Documento in modifica
     * @param benePrincipale Bene principale di cui cercare accessori
     * @return Lista di accessori trovati nei dettagli salvati
     * @throws ComponentException in caso di errore
     */
    public List cercaBeniAccessoriNeiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException {

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return java.util.Collections.emptyList();
        }

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            SQLBuilder sql = dettHome.createSQLBuilder();

            // ========== FILTRI PER TROVARE ACCESSORI NEI DETTAGLI SALVATI ==========
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                    doc.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS,
                    doc.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS,
                    doc.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    doc.getPgDocTrasportoRientro());

            // Stesso NR_INVENTARIO del principale
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    benePrincipale.getNr_inventario());

            // Solo accessori (PROGRESSIVO > 0)
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

            // Escludi il bene stesso
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    benePrincipale.getProgressivo());

            List dettagli = dettHome.fetchAll(sql);

            if (dettagli != null && !dettagli.isEmpty()) {
                // Converti i dettagli in beni
                List<Inventario_beniBulk> beniAccessori = new ArrayList();
                Inventario_beniHome beneHome = (Inventario_beniHome)
                        getHome(userContext, Inventario_beniBulk.class);

                for (Iterator it = dettagli.iterator(); it.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dett =
                            (Doc_trasporto_rientro_dettBulk) it.next();

                    // Carica il bene completo
                    Inventario_beniBulk bene = (Inventario_beniBulk) beneHome.findByPrimaryKey(
                            new Inventario_beniBulk(
                                    dett.getNr_inventario(),
                                    dett.getPg_inventario(),
                                    Long.valueOf(dett.getProgressivo())
                            )
                    );

                    if (bene != null) {
                        beniAccessori.add(bene);
                    }
                }

                return beniAccessori;
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }

        return java.util.Collections.emptyList();
    }



    /**
     * Elimina il bene principale E tutti gli accessori associati DAI DETTAGLI SALVATI.
     * Usato durante MODIFICA documento.
     *
     * @param userContext Contesto utente
     * @param doc Documento in modifica
     * @param benePrincipale Bene principale da eliminare
     * @param beniAccessori Lista di beni accessori da eliminare insieme
     * @throws ComponentException in caso di errore
     */
    public void eliminaBeniPrincipaleConAccessoriDaDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException {

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ComponentException("Documento non valido per eliminazione dettagli");
        }

        PreparedStatement pstmt = null;

        try {
            // ========== COSTRUISCI LA IN CLAUSE CON PRINCIPALE + ACCESSORI ==========
            StringBuilder inClause = new StringBuilder();

            // Aggiungi il principale
            inClause.append("(")
                    .append(benePrincipale.getNr_inventario())
                    .append(",")
                    .append(benePrincipale.getProgressivo())
                    .append(")");

            // Aggiungi gli accessori
            if (beniAccessori != null && !beniAccessori.isEmpty()) {
                for (Iterator it = beniAccessori.iterator(); it.hasNext(); ) {
                    Inventario_beniBulk accessorio = (Inventario_beniBulk) it.next();
                    inClause.append(",(")
                            .append(accessorio.getNr_inventario())
                            .append(",")
                            .append(accessorio.getProgressivo())
                            .append(")");
                }
            }

            // ========== DELETE DIRETTA ==========
            String deleteSQL =
                    "DELETE FROM DOC_TRASPORTO_RIENTRO_DETT " +
                            "WHERE PG_INVENTARIO = ? " +
                            "  AND TI_DOCUMENTO = ? " +
                            "  AND ESERCIZIO = ? " +
                            "  AND PG_DOC_TRASPORTO_RIENTRO = ? " +
                            "  AND (NR_INVENTARIO, PROGRESSIVO) IN (" + inClause + ")";

            pstmt = getConnection(userContext).prepareStatement(deleteSQL);
            pstmt.setLong(1, doc.getPgInventario());
            pstmt.setString(2, doc.getTiDocumento());
            pstmt.setInt(3, doc.getEsercizio());
            pstmt.setLong(4, doc.getPgDocTrasportoRientro());

            int recordEliminati = pstmt.executeUpdate();

            System.out.println("Eliminati " + recordEliminati +
                    " dettagli (principale + accessori) dal documento " +
                    doc.getPgDocTrasportoRientro());

        } catch (java.sql.SQLException e) {
            throw new ComponentException(
                    "Errore eliminazione principale + accessori dai dettagli: " +
                            e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (java.sql.SQLException e) {
                System.out.println("Errore chiusura statement: " + e);
            }
        }
    }


    public void selezionaTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException {

        PreparedStatement pstmt = null;

        try {
            // ========== DETERMINA SE È TRASPORTO O RIENTRO ==========
            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

            // ========== COSTRUISCI LA QUERY CON fetchAll() ==========
            List<Inventario_beniBulk> beniFiltrati = caricaBeniPerInserimento(
                    userContext,
                    doc,
                    clauses,
                    isTrasporto
            );

            if (beniFiltrati.isEmpty()) {
                System.out.println("Nessun bene da inserire");
                return;
            }

            System.out.println("Caricati " + beniFiltrati.size() + " beni, inizio INSERT come dettagli...");

            // ========== PREPARA DATI DI AUDIT ==========
            String userId = userContext.getUser();
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

            // ========== PREPARA INSERT BATCH SUI DETTAGLI ==========
            String insertSQL =
                    "INSERT INTO DOC_TRASPORTO_RIENTRO_DETT " +
                            "(PG_INVENTARIO, TI_DOCUMENTO, ESERCIZIO, PG_DOC_TRASPORTO_RIENTRO, " +
                            " NR_INVENTARIO, PROGRESSIVO, CD_TERZO_ASSEGNATARIO, " +
                            " DACR, UTCR, DUVA, UTUV, PG_VER_REC) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = getConnection(userContext).prepareStatement(insertSQL);

            int batchSize = 0;
            int totalInserted = 0;
            final int BATCH_LIMIT = 1000;

            // ========== Determina CD_TERZO_ASSEGNATARIO una volta ==========
            Integer cdTerzoAssegnatario = null;
            if (doc.isSmartworking() && doc.getTerzoSmartworking() != null) {
                cdTerzoAssegnatario = doc.getTerzoSmartworking().getCd_terzo();
            } else if (doc.getFlIncaricato() && doc.getTerzoIncRitiro() != null) {
                cdTerzoAssegnatario = doc.getTerzoIncRitiro().getCd_terzo();
            }

            // ========== ITERA SULLA LISTA ==========
            for (Inventario_beniBulk bene : beniFiltrati) {

                pstmt.setLong(1, doc.getPgInventario());
                pstmt.setString(2, doc.getTiDocumento());
                pstmt.setInt(3, doc.getEsercizio());
                pstmt.setLong(4, doc.getPgDocTrasportoRientro());
                pstmt.setLong(5, bene.getNr_inventario());
                pstmt.setLong(6, bene.getProgressivo());

                // CD_TERZO_ASSEGNATARIO
                if (cdTerzoAssegnatario != null) {
                    pstmt.setInt(7, cdTerzoAssegnatario);
                } else if (bene.getCd_assegnatario() != null) {
                    pstmt.setInt(7, bene.getCd_assegnatario());
                } else {
                    pstmt.setNull(7, java.sql.Types.INTEGER);
                }

                // ========== CAMPI DI AUDIT ==========
                pstmt.setTimestamp(8, now);
                pstmt.setString(9, userId);
                pstmt.setTimestamp(10, now);
                pstmt.setString(11, userId);
                pstmt.setLong(12, 1L);

                pstmt.addBatch();
                batchSize++;

                if (batchSize >= BATCH_LIMIT) {
                    int[] results = pstmt.executeBatch();
                    totalInserted += results.length;
                    batchSize = 0;
                    System.out.println("Inseriti " + totalInserted + " / " + beniFiltrati.size() + " beni...");
                }
            }

            // ========== ESEGUI BATCH RIMANENTE ==========
            if (batchSize > 0) {
                int[] results = pstmt.executeBatch();
                totalInserted += results.length;
            }

            System.out.println("INSERT BATCH completato: inseriti " + totalInserted + " dettagli");

        } catch (java.sql.SQLException e) {
            throw new ComponentException("Errore INSERT batch dettagli: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (java.sql.SQLException e) {
                System.out.println("Errore chiusura statement: " + e);
            }
        }
    }


    private List<Inventario_beniBulk> caricaBeniPerInserimento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses,
            boolean isTrasporto)
            throws ComponentException {

        try {
            Inventario_beniHome invHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);

            if (isTrasporto) {
                applicaFiltriTrasporto(sql, doc, userContext);
            } else {
                applicaFiltriRientro(sql, doc);
            }

            // ========== ESCLUDI BENI GIÀ PRESENTI NEI DETTAGLI ==========
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

            sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", SQLBuilder.EQUALS,
                    "INVENTARIO_BENI.PG_INVENTARIO");
            sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO", SQLBuilder.EQUALS,
                    "INVENTARIO_BENI.NR_INVENTARIO");
            sqlEsclusiDett.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO", SQLBuilder.EQUALS,
                    "INVENTARIO_BENI.PROGRESSIVO");

            sql.addSQLNotExistsClause("AND", sqlEsclusiDett);
            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            List risultati = invHome.fetchAll(sql);

            return risultati != null ? risultati : java.util.Collections.emptyList();

        } catch (Exception e) {
            throw handleException(e);
        }
    }


    /**
     * Recupera tutti i dettagli di un documento di trasporto/rientro
     *
     * @param userContext Il contesto utente
     * @param doc Il documento di cui recuperare i dettagli
     * @return Lista dei dettagli ordinati per numero inventario e progressivo
     * @throws ComponentException Se si verifica un errore durante il recupero
     */
    public List getDetailsFor(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);
            return dettHome.getDetailsFor(doc);

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

}