package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.anagraf00.ejb.AnagraficoComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaHome;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniHome;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.progettiric00.core.bulk.Progetto_anagraficoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.RIENTRO;
import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.TRASPORTO;

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
            doc.setStato(Doc_trasporto_rientroBulk.STATO_INSERITO);

//            doc.setBene(new Inventario_beniBulk());
//            doc.setAnagIncRitiro(new AnagraficoBulk());
//            doc.setAnagSmartworking(new AnagraficoBulk());

            return doc;

        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }


    private BulkHome getHomeDocumentoTrasportoRientroDett(UserContext aUC, OggettoBulk bulk)throws ComponentException {
        if ( bulk instanceof DocumentoTrasportoBulk)
            return  getHome(aUC, DocumentoTrasportoDettBulk.class);
        else
            return  getHome(aUC, DocumentoRientroDettBulk.class);
    }


    @Override
    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        if (bulk == null)
            throw new ComponentException("Documento non trovato!");

        bulk = super.inizializzaBulkPerModifica(aUC, bulk);
        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(aUC, docTR);

        try {
            docTR.setInventario(caricaInventario(aUC));
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC, docTR.getInventario()));
            docTR = (Doc_trasporto_rientroBulk) getHome(aUC, Doc_trasporto_rientroBulk.class).findByPrimaryKey(docTR);

// ========== CARICA TERZO E ANAGRAFICO INCARICATO ==========
            if (docTR.getCdTerzoIncaricato() != null) {
                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(docTR.getCdTerzoIncaricato()));
                if (terzo != null) {
                    docTR.setTerzoIncRitiro(terzo);

                    // Risali all'anagrafico dal terzo (USA LA VIEW)
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

            // ========== RICOSTRUISCI TERZO E ANAGRAFICO SMARTWORKING DAI DETTAGLI ==========
            if (docTR.isSmartworking() && docTR.getDoc_trasporto_rientro_dettColl() != null
                    && !docTR.getDoc_trasporto_rientro_dettColl().isEmpty()) {

                Doc_trasporto_rientro_dettBulk primoDettaglio =
                        (Doc_trasporto_rientro_dettBulk) docTR.getDoc_trasporto_rientro_dettColl().get(0);

                if (primoDettaglio.getCdTerzoAssegnatario() != null) {
                    TerzoBulk terzoSmart = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                            .findByPrimaryKey(new TerzoBulk(primoDettaglio.getCdTerzoAssegnatario()));

                    if (terzoSmart != null) {
                        docTR.setTerzoSmartworking(terzoSmart);

                        // Risali all'anagrafico dal terzo smartworking (USA LA VIEW)
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

            // ==================== 1. GENERAZIONE PROGRESSIVO E VALIDAZIONE ====================
            generaProgressivoDocumento(userContext, docT);
            verificaDocumentoNonEsistente(userContext, docT);
            validaDoc(userContext, docT);

            // ==================== 2. RECUPERA TERZO DA ANAGRAFICO INCARICATO ====================
            if (docT.getAnagIncRitiro() != null &&
                    docT.getAnagIncRitiro().getCd_anag() != null) {

                // Cerca il terzo tramite CD_ANAG
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

            // ==================== 3. RECUPERA TERZO DA ANAGRAFICO SMARTWORKING ====================
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

            // ==================== 4. LEGGI BENI DA INVENTARIO_BENI_APG ====================
            List beniApg = caricaBeniDaTabellaTempoanea(userContext, docT);

            // ==================== 5. CREA I DETTAGLI ====================
            boolean isRientro = docT instanceof DocumentoRientroBulk;
            SimpleBulkList dettagliList = new SimpleBulkList();
            List<DocumentoTrasportoDettBulk> dettagliTrasportoDaAggiornare = new ArrayList<>();

            Inventario_beniHome invBeniHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            for (Iterator it = beniApg.iterator(); it.hasNext(); ) {
                Inventario_beni_apgBulk beneApg = (Inventario_beni_apgBulk) it.next();

                // Carica il bene completo dal database
                Inventario_beniBulk bene = (Inventario_beniBulk) invBeniHome.findByPrimaryKey(
                        new Inventario_beniBulk(
                                beneApg.getNr_inventario(),
                                beneApg.getPg_inventario(),
                                beneApg.getProgressivo()
                        )
                );

                if (bene == null) {
                    throw new ComponentException(
                            "Bene non trovato: " + beneApg.getNr_inventario() +
                                    "-" + beneApg.getProgressivo());
                }

                if (isRientro) {
                    DocumentoRientroDettBulk dettaglio = creaDettaglioRientro(
                            userContext, (DocumentoRientroBulk) docT, bene, beneApg,
                            dettagliTrasportoDaAggiornare);
                    dettagliList.add(dettaglio);
                } else {
                    DocumentoTrasportoDettBulk dettaglio = creaDettaglioTrasporto(
                            userContext, (DocumentoTrasportoBulk) docT, bene, beneApg);
                    dettagliList.add(dettaglio);
                }
            }

            // ==================== 6. IMPOSTA CD_TERZO_INCARICATO IN TESTATA ====================
            if (docT.isSmartworking()) {
                // SMARTWORKING: copia terzo smartworking in CD_TERZO_INCARICATO
                if (docT.getTerzoSmartworking() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoSmartworking().getCd_terzo());
                }
            } else if (docT.getFlIncaricato()) {
                // INCARICATO: copia terzo incaricato in CD_TERZO_INCARICATO
                if (docT.getTerzoIncRitiro() != null) {
                    docT.setCdTerzoIncaricato(docT.getTerzoIncRitiro().getCd_terzo());
                }
            }

            // ==================== 7. SALVA IL DOCUMENTO ====================
            docT.setDoc_trasporto_rientro_dettColl(dettagliList);
            docT.setToBeCreated();
            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);

            // ==================== 8. AGGIORNA RIFERIMENTI BIDIREZIONALI (solo per RIENTRO) ====================
            if (isRientro) {
                aggiornaRiferimentiInversi(userContext, docT, dettagliTrasportoDaAggiornare);
            }

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
     */
    private void verificaDocumentoNonEsistente(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {
        Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk)
                findByPrimaryKey(userContext, docT);

        if (existing != null) {
            throw new ApplicationException(
                    "Il documento è già stato salvato (PG=" + docT.getPgDocTrasportoRientro() + ")");
        }
    }

    /**
     * Carica i beni selezionati dalla tabella temporanea INVENTARIO_BENI_APG.
     */
    private List caricaBeniDaTabellaTempoanea(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {
        Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                getHome(userContext, Inventario_beni_apgBulk.class);

        SQLBuilder sqlApg = apgHome.createSQLBuilder();
        sqlApg.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS,
                docT.getLocal_transactionID());
        sqlApg.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

        List beniApg = apgHome.fetchAll(sqlApg);

        if (beniApg == null || beniApg.isEmpty()) {
            throw new ApplicationException(
                    "Attenzione: è necessario specificare almeno un bene!");
        }

        return beniApg;
    }

    /**
     * Crea un dettaglio TRASPORTO per il bene specificato.
     * Cerca eventuale RIENTRO precedente e imposta riferimento opzionale.
     */
    private DocumentoTrasportoDettBulk creaDettaglioTrasporto(
            UserContext userContext,
            DocumentoTrasportoBulk docTrasporto,
            Inventario_beniBulk bene,
            Inventario_beni_apgBulk beneApg) throws ComponentException {

        DocumentoTrasportoDettBulk dettaglio = new DocumentoTrasportoDettBulk();
        dettaglio.setDocumentoTrasporto(docTrasporto);

        // ========== IMPOSTA CHIAVI PRIMARIE ==========
        dettaglio.setPg_inventario(docTrasporto.getPgInventario());
        dettaglio.setTi_documento(docTrasporto.getTiDocumento());
        dettaglio.setEsercizio(docTrasporto.getEsercizio());
        dettaglio.setPg_doc_trasporto_rientro(docTrasporto.getPgDocTrasportoRientro());
        dettaglio.setNr_inventario(bene.getNr_inventario());
        dettaglio.setProgressivo(bene.getProgressivo().intValue());

        // ========== IMPOSTA CD_TERZO_ASSEGNATARIO ==========
        if (docTrasporto.isSmartworking()) {
            // SMARTWORKING: usa il terzo selezionato in testata
            if (docTrasporto.getTerzoSmartworking() != null) {
                dettaglio.setCdTerzoAssegnatario(docTrasporto.getTerzoSmartworking().getCd_terzo());
            }
        } else if (docTrasporto.getFlIncaricato()) {
            // INCARICATO: usa il terzo incaricato selezionato
            if (docTrasporto.getTerzoIncRitiro() != null) {
                dettaglio.setCdTerzoAssegnatario(docTrasporto.getTerzoIncRitiro().getCd_terzo());
            }
        } else {
            // ALTRIMENTI: usa il CD_ASSEGNATARIO del bene
            if (bene.getCd_assegnatario() != null) {
                dettaglio.setCdTerzoAssegnatario(bene.getCd_assegnatario());
            }
        }

        // ========== ALTRE IMPOSTAZIONI COMUNI ==========
        dettaglio.setBene(bene);
        dettaglio.setToBeCreated();

        // ========== Cerca RIENTRO precedente (opzionale) ==========
        Doc_trasporto_rientro_dettBulk dettaglioRientroOriginale =
                trovaDettaglioOriginale(userContext, bene, docTrasporto.getPgInventario(), RIENTRO);

        if (dettaglioRientroOriginale != null) {
            dettaglio.setDocRientroDettRif((DocumentoRientroDettBulk) dettaglioRientroOriginale);
        }

        return dettaglio;
    }


    /**
     * Crea un dettaglio RIENTRO per il bene specificato.
     * Cerca TRASPORTO originale (obbligatorio) e imposta riferimento.
     * Aggiunge il dettaglio TRASPORTO alla lista per aggiornamento successivo.
     */
    private DocumentoRientroDettBulk creaDettaglioRientro(
            UserContext userContext,
            DocumentoRientroBulk docRientro,
            Inventario_beniBulk bene,
            Inventario_beni_apgBulk beneApg,
            List<DocumentoTrasportoDettBulk> dettagliTrasportoDaAggiornare) throws ComponentException {

        DocumentoRientroDettBulk dettaglio = new DocumentoRientroDettBulk();
        dettaglio.setDocumentoRientro(docRientro);
        dettaglio.setBene(bene);

        // ========== IMPOSTA CHIAVI PRIMARIE ==========
        dettaglio.setPg_inventario(docRientro.getPgInventario());
        dettaglio.setTi_documento(docRientro.getTiDocumento());
        dettaglio.setEsercizio(docRientro.getEsercizio());
        dettaglio.setPg_doc_trasporto_rientro(docRientro.getPgDocTrasportoRientro());
        dettaglio.setNr_inventario(bene.getNr_inventario());
        dettaglio.setProgressivo(bene.getProgressivo().intValue());

        // ========== IMPOSTA CD_TERZO_ASSEGNATARIO ==========
        if (docRientro.isSmartworking()) {
            // SMARTWORKING: usa il terzo selezionato in testata
            if (docRientro.getTerzoSmartworking() != null) {
                dettaglio.setCdTerzoAssegnatario(docRientro.getTerzoSmartworking().getCd_terzo());
            }
        } else if (docRientro.getFlIncaricato()) {
            // INCARICATO: usa il terzo incaricato selezionato
            if (docRientro.getTerzoIncRitiro() != null) {
                dettaglio.setCdTerzoAssegnatario(docRientro.getTerzoIncRitiro().getCd_terzo());
            }
        } else {
            // ALTRIMENTI: usa il CD_ASSEGNATARIO del bene
            if (bene.getCd_assegnatario() != null) {
                dettaglio.setCdTerzoAssegnatario(bene.getCd_assegnatario());
            }
        }

        // ========== CERCA TRASPORTO ORIGINALE (OBBLIGATORIO) ==========
        Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                trovaDettaglioOriginale(userContext, bene, docRientro.getPgInventario(), TRASPORTO);

        if (dettaglioTrasportoOriginale == null) {
            throw new ApplicationException(
                    "Bene " + bene.getNumeroBeneCompleto() +
                            " non presente in nessun documento di trasporto firmato (stato DEFINITIVO)!");
        }

        DocumentoTrasportoDettBulk docTrasportoDettRif =
                (DocumentoTrasportoDettBulk) dettaglioTrasportoOriginale;

        dettaglio.setDocTrasportoDettRif(docTrasportoDettRif);
        dettagliTrasportoDaAggiornare.add(docTrasportoDettRif);

        dettaglio.setToBeCreated();
        return dettaglio;
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
            Doc_trasporto_rientro_dettHome dettHome=null;
            if ( Doc_trasporto_rientroBulk.RIENTRO.equalsIgnoreCase(tipoDocumentoDaCercare)) {
                dettHome =
                        (Doc_trasporto_rientro_dettHome) getHome(
                                userContext, DocumentoRientroDettBulk.class);
            }else {
                dettHome =
                        (Doc_trasporto_rientro_dettHome) getHome(
                                userContext, DocumentoTrasportoDettBulk.class);
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
            if (RIENTRO.equals(tipoDocumentoDaCercare)) {
                sql.addSQLClause("AND", "d.STATO",
                        SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);
            }

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
     * PreCondition:
     * E' stata generata la richiesta di modificare un Documento di Trasporto/Rientro.
     * PostCondition:
     * Viene consentito il salvataggio.
     *
     * @param aUC  lo <code>UserContext</code> che ha generato la richiesta
     * @param bulk <code>OggettoBulk</code> il Bulk da modificare
     * @return l'oggetto <code>OggettoBulk</code> modificato
     **/
    @Override
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

        // ===== VALIDAZIONE =====
        validaDoc(aUC, docT);

        try {
            // ===== SALVATAGGIO DETTAGLI =====
            if (docT.getDoc_trasporto_rientro_dettColl() != null) {
                for (Iterator i = docT.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dettaglio =
                            (Doc_trasporto_rientro_dettBulk) i.next();
                    updateBulk(aUC, dettaglio);
                }
            }

            // ===== SALVATAGGIO ALLEGATI =====
            if (docT.getArchivioAllegati() != null) {
                for (AllegatoGenericoBulk allegato : docT.getArchivioAllegati()) {
                    if (allegato != null) {
                        updateBulk(aUC, allegato);
                    }
                }
            }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }

        // ===== SALVA DOCUMENTO =====
        return super.modificaConBulk(aUC, docT);
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

            if (docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: data del " + tipoDoc + " non valida.\n" +
                                "La Data del " + tipoDoc + " non può essere precedente ad una modifica di uno dei beni movimentati."
                );
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


    /**
     * Cerca i beni trasportabili per un documento di trasporto.
     *
     * LOGICA BENI DISPONIBILI:
     * - Beni MAI trasportati (non presenti in DOC_TRASPORTO_RIENTRO_DETT con TI_DOCUMENTO='T')
     * - Beni in documenti di TRASPORTO ANNULLATI (considerati disponibili)
     * - Beni trasportati MA già rientrati (presenti in RIENTRO DEFINITIVO)
     *
     * SMARTWORKING:
     * - Se tipo movimento = SMARTWORKING, filtra SOLO per cd_assegnatario (terzo selezionato)
     * - Altrimenti filtra per UO dell'utente
     */
    public RemoteIterator cercaBeniTrasportabili(UserContext userContext,
                                                 Doc_trasporto_rientroBulk doc,
                                                 SimpleBulkList beni_da_escludere,
                                                 CompoundFindClause clauses)
            throws ComponentException, PersistencyException, IntrospectionException {

        SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();
        TerzoHome terzoHome = (TerzoHome)getHome(userContext,TerzoBulk.class);

        // Recupera il terzo SOLO se anagrafico smartworking è valorizzato
        TerzoBulk terzo = null;
        if (doc.getAnagSmartworking() != null && doc.getAnagSmartworking().getCd_anag() != null) {
            terzo = terzoHome.findTerzoByAnag(doc.getAnagSmartworking().getCd_anag());
        }

        if (clauses != null) {
            sql.addClause(clauses);
        }

        // ==================== FILTRI BASE ====================
        sql.addSQLClause("AND", "INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS,
                doc.getInventario().getPg_inventario());

        sql.openParenthesis("AND");
        sql.addSQLClause("AND", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO",
                SQLBuilder.ISNULL, null);
        sql.addSQLClause("OR", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO",
                SQLBuilder.EQUALS, "N");
        sql.closeParenthesis();

        sql.addSQLClause("AND", "INVENTARIO_BENI.DT_VALIDITA_VARIAZIONE",
                SQLBuilder.LESS_EQUALS, doc.getDataRegistrazione());
        sql.addSQLClause("AND", "INVENTARIO_BENI.ESERCIZIO_CARICO_BENE",
                SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(userContext));

        // ==================== FILTRO SMARTWORKING VS NORMALE ====================
        if (doc.isSmartworking() && terzo != null && terzo.getCd_terzo() != null) {
            // Filtro per cd_assegnatario SOLO se anagrafico smartworking è valorizzato
            sql.addSQLClause("AND", "INVENTARIO_BENI.CD_ASSEGNATARIO",
                    SQLBuilder.EQUALS, terzo.getCd_terzo());
        } else {
            // Altrimenti filtra per UO
            filtroPerUnitaOrganizzativaUtente(sql, doc, userContext);
        }

        // ==================== BENI DISPONIBILI PER TRASPORTO ====================
        try {
            includiBeniDisponibiliPerTrasporto(sql, doc.getInventario().getPg_inventario(), userContext);
        } catch (Exception e) {
            throw new ComponentException("Errore nei filtri per beni disponibili: " + e.getMessage(), e);
        }

        // ==================== ESCLUDI BENI GIA' INSERITI ====================
        escludiBeniGiaInseriti(sql, beni_da_escludere);

        sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

        try {
            return iterator(userContext, sql, Inventario_beniBulk.class, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    /**
     * Filtra i beni disponibili per il trasporto con logica:
     *
     * INCLUDI BENI CHE SONO:
     * 1. MAI stati trasportati (non presenti in DOC_TRASPORTO_RIENTRO_DETT con TI_DOCUMENTO='T')
     *    OPPURE
     * 2. Trasportati in documenti ANNULLATI (considerati come mai trasportati)
     *    OPPURE
     * 3. Trasportati MA già rientrati definitivamente (presenti in RIENTRO DEFINITIVO)
     *
     * @param sql          SQLBuilder della query principale
     * @param pgInventario Progressivo inventario
     * @param userContext  Contesto utente
     * @throws ComponentException in caso di errore
     */
    private void includiBeniDisponibiliPerTrasporto(
            SQLBuilder sql,
            Long pgInventario,
            UserContext userContext)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            // ==================== NOT EXISTS: BENI IN TRASPORTO ATTIVO ====================
            // Un bene è disponibile se NON è in trasporto attivo
            // (trasporto attivo = documento TRASPORTO non ANNULLATO SENZA rientro DEFINITIVO)

            SQLBuilder sqlTrasportoAttivo = dettHome.createSQLBuilder();

            sqlTrasportoAttivo.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT DETT_T");
            sqlTrasportoAttivo.addTableToHeader("DOC_TRASPORTO_RIENTRO DOC_T");

            // Join dettaglio-testata TRASPORTO
            sqlTrasportoAttivo.addSQLJoin("DETT_T.PG_INVENTARIO", "DOC_T.PG_INVENTARIO");
            sqlTrasportoAttivo.addSQLJoin("DETT_T.TI_DOCUMENTO", "DOC_T.TI_DOCUMENTO");
            sqlTrasportoAttivo.addSQLJoin("DETT_T.ESERCIZIO", "DOC_T.ESERCIZIO");
            sqlTrasportoAttivo.addSQLJoin("DETT_T.PG_DOC_TRASPORTO_RIENTRO", "DOC_T.PG_DOC_TRASPORTO_RIENTRO");

            // Collega con INVENTARIO_BENI
            sqlTrasportoAttivo.addSQLJoin("DETT_T.NR_INVENTARIO", "INVENTARIO_BENI.NR_INVENTARIO");
            sqlTrasportoAttivo.addSQLJoin("DETT_T.PROGRESSIVO", "INVENTARIO_BENI.PROGRESSIVO");

            // Filtri TRASPORTO
            sqlTrasportoAttivo.addSQLClause("AND", "DETT_T.PG_INVENTARIO",
                    SQLBuilder.EQUALS, pgInventario);
            sqlTrasportoAttivo.addSQLClause("AND", "DOC_T.TI_DOCUMENTO",
                    SQLBuilder.EQUALS, TRASPORTO);
            sqlTrasportoAttivo.addSQLClause("AND", "DOC_T.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            // ==================== NOT EXISTS RIENTRO DEFINITIVO ====================
            // Il trasporto è "attivo" solo se NON ha un rientro definitivo associato

            SQLBuilder sqlRientroDefinitivo = dettHome.createSQLBuilder();

            sqlRientroDefinitivo.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT DETT_R");
            sqlRientroDefinitivo.addTableToHeader("DOC_TRASPORTO_RIENTRO DOC_R");

            // Join dettaglio-testata RIENTRO
            sqlRientroDefinitivo.addSQLJoin("DETT_R.PG_INVENTARIO", "DOC_R.PG_INVENTARIO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.TI_DOCUMENTO", "DOC_R.TI_DOCUMENTO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.ESERCIZIO", "DOC_R.ESERCIZIO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.PG_DOC_TRASPORTO_RIENTRO", "DOC_R.PG_DOC_TRASPORTO_RIENTRO");

            // Collegamento RIENTRO → TRASPORTO tramite FK di riferimento
            sqlRientroDefinitivo.addSQLJoin("DETT_R.PG_INVENTARIO_RIF", "DETT_T.PG_INVENTARIO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.NR_INVENTARIO_RIF", "DETT_T.NR_INVENTARIO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.PROGRESSIVO_RIF", "DETT_T.PROGRESSIVO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.TI_DOCUMENTO_RIF", "DETT_T.TI_DOCUMENTO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.ESERCIZIO_RIF", "DETT_T.ESERCIZIO");
            sqlRientroDefinitivo.addSQLJoin("DETT_R.PG_DOC_TRASPORTO_RIENTRO_RIF", "DETT_T.PG_DOC_TRASPORTO_RIENTRO");

            // Filtri RIENTRO
            sqlRientroDefinitivo.addSQLClause("AND", "DOC_R.TI_DOCUMENTO",
                    SQLBuilder.EQUALS, RIENTRO);
            sqlRientroDefinitivo.addSQLClause("AND", "DOC_R.STATO",
                    SQLBuilder.EQUALS, Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            sqlTrasportoAttivo.addSQLNotExistsClause("AND", sqlRientroDefinitivo);

            sql.addSQLNotExistsClause("AND", sqlTrasportoAttivo);

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore nella costruzione dei filtri per beni disponibili: " + e.getMessage(), e);
        }
    }

    /**
     * Cerca i beni disponibili per il rientro.
     *
     * LOGICA BENI DISPONIBILI:
     * - Beni presenti in documenti di TRASPORTO DEFINITIVI (firmati)
     * - NON già rientrati (non presenti in RIENTRO non ANNULLATO)
     * - Esclusi beni in TRASPORTO ANNULLATO
     */
    public RemoteIterator cercaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();

            // ==================== JOIN CON DOCUMENTI TRASPORTO ====================
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett_t");
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO dt");

            // Join INVENTARIO_BENI con dettagli trasporto
            sql.addSQLJoin("INVENTARIO_BENI.PG_INVENTARIO", "dett_t.PG_INVENTARIO");
            sql.addSQLJoin("INVENTARIO_BENI.NR_INVENTARIO", "dett_t.NR_INVENTARIO");
            sql.addSQLJoin("INVENTARIO_BENI.PROGRESSIVO", "dett_t.PROGRESSIVO");

            // Join dettagli con testata trasporto
            sql.addSQLJoin("dett_t.PG_INVENTARIO", "dt.PG_INVENTARIO");
            sql.addSQLJoin("dett_t.TI_DOCUMENTO", "dt.TI_DOCUMENTO");
            sql.addSQLJoin("dett_t.ESERCIZIO", "dt.ESERCIZIO");
            sql.addSQLJoin("dett_t.PG_DOC_TRASPORTO_RIENTRO", "dt.PG_DOC_TRASPORTO_RIENTRO");

            // ==================== FILTRI BASE TRASPORTO ====================
            sql.addSQLClause("AND", "dt.TI_DOCUMENTO", SQLBuilder.EQUALS, TRASPORTO);
            sql.addSQLClause("AND", "dt.STATO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            sql.addSQLClause("AND", "INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS,
                    doc.getInventario().getPg_inventario());

            // ==================== NOT EXISTS: NON GIÀ RIENTRATI ====================
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            SQLBuilder sqlNotExistsRientro = dettHome.createSQLBuilder();
            sqlNotExistsRientro.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett_r");
            sqlNotExistsRientro.addTableToHeader("DOC_TRASPORTO_RIENTRO dr");

            // Join dettaglio-testata RIENTRO
            sqlNotExistsRientro.addSQLJoin("dett_r.PG_INVENTARIO", "dr.PG_INVENTARIO");
            sqlNotExistsRientro.addSQLJoin("dett_r.TI_DOCUMENTO", "dr.TI_DOCUMENTO");
            sqlNotExistsRientro.addSQLJoin("dett_r.ESERCIZIO", "dr.ESERCIZIO");
            sqlNotExistsRientro.addSQLJoin("dett_r.PG_DOC_TRASPORTO_RIENTRO", "dr.PG_DOC_TRASPORTO_RIENTRO");

            // Collegamento RIENTRO → TRASPORTO
            sqlNotExistsRientro.addSQLJoin("dett_r.PG_INVENTARIO_RIF", "dett_t.PG_INVENTARIO");
            sqlNotExistsRientro.addSQLJoin("dett_r.NR_INVENTARIO_RIF", "dett_t.NR_INVENTARIO");
            sqlNotExistsRientro.addSQLJoin("dett_r.PROGRESSIVO_RIF", "dett_t.PROGRESSIVO");
            sqlNotExistsRientro.addSQLJoin("dett_r.TI_DOCUMENTO_RIF", "dett_t.TI_DOCUMENTO");
            sqlNotExistsRientro.addSQLJoin("dett_r.ESERCIZIO_RIF", "dett_t.ESERCIZIO");
            sqlNotExistsRientro.addSQLJoin("dett_r.PG_DOC_TRASPORTO_RIENTRO_RIF", "dett_t.PG_DOC_TRASPORTO_RIENTRO");

            // Filtra SOLO rientri non ANNULLATI
            sqlNotExistsRientro.addSQLClause("AND", "dr.TI_DOCUMENTO", SQLBuilder.EQUALS, RIENTRO);
            sqlNotExistsRientro.addSQLClause("AND", "dr.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            sql.addSQLNotExistsClause("AND", sqlNotExistsRientro);

            // ==================== FILTRI BENI ====================
            sql.openParenthesis("AND");
            sql.addSQLClause("AND", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO", SQLBuilder.ISNULL, null);
            sql.addSQLClause("OR", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO", SQLBuilder.EQUALS, "N");
            sql.closeParenthesis();

            sql.addSQLClause("AND", "INVENTARIO_BENI.DT_VALIDITA_VARIAZIONE",
                    SQLBuilder.LESS_EQUALS, doc.getDataRegistrazione());
            sql.addSQLClause("AND", "INVENTARIO_BENI.ESERCIZIO_CARICO_BENE",
                    SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(userContext));

            // ==================== ESCLUDI BENI GIA' INSERITI ====================
            escludiBeniGiaInseriti(sql, beniEsclusi);

            // ==================== FILTRI UTENTE ====================
            if (clauses != null) {
                sql.addClause(clauses);
            }

            // ==================== ORDINAMENTO ====================
            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            return iterator(userContext, sql, Inventario_beniBulk.class, null);

        } catch (Exception e) {
            throw handleException(e);
        }
    }


    /**
     * Esclude i beni già presenti nel documento corrente (tabella temporanea).
     * Usa sintassi SQL efficiente con tuple per escludere multiple righe.
     *
     * @param sql SQLBuilder della query principale
     * @param beni_da_escludere Lista di beni da escludere
     */
    private void escludiBeniGiaInseriti(SQLBuilder sql, SimpleBulkList beni_da_escludere) {

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

    public SimpleBulkList selezionati(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docT) throws it.cnr.jada.comp.ComponentException {

        SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class, "V_INVENTARIO_BENI_APG").createSQLBuilder();
        sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", sql.EQUALS, docT.getLocal_transactionID());
        try {
            List risultato = getHome(userContext, Inventario_beniBulk.class).fetchAll(sql);
            if (risultato.size() > 0) {
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
        SQLBuilder sql = null;
        if (docT.getPgDocTrasportoRientro() != null && docT.getPgDocTrasportoRientro() > 0) {
            sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT");
            sql.addSQLJoin("INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS, "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO");
            sql.addSQLJoin("INVENTARIO_BENI.NR_INVENTARIO", SQLBuilder.EQUALS, "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO");
            sql.addSQLJoin("INVENTARIO_BENI.PROGRESSIVO", SQLBuilder.EQUALS, "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO");
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", SQLBuilder.EQUALS, docT.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, docT.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, docT.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS, docT.getPgDocTrasportoRientro());
        } else {
            sql = getHome(userContext, Inventario_beniBulk.class, "V_INVENTARIO_BENI_APG").createSQLBuilder();
            sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", sql.EQUALS, docT.getLocal_transactionID());
        }
        sql.addOrderBy("NR_INVENTARIO");
        sql.addOrderBy("PROGRESSIVO");
        try {
            it.cnr.jada.util.RemoteIterator ri = iterator(userContext, sql, bulkClass, null);
            return ri;
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    public void inizializzaBeniDaTrasportare(it.cnr.jada.UserContext userContext)
            throws it.cnr.jada.comp.ComponentException {
        try {
            setSavepoint(userContext, "INVENTARIO_BENI_APG");
        } catch (java.sql.SQLException e) {
            throw handleException(e);
        }
    }


    /**
     * Annullamento
     * PreCondition:
     * E' stata generata la richiesta di annullare tutte le operazioni Trasporto Inventario.
     * PostCondition:
     * Viene effettuata un rollback fino al punto (SavePoint) impostato in precedenza,
     * (metodo inizializzaBeniDaTrasportare)
     *
     * @param userContext lo <code>UserContext</code> che ha generato la richiesta
     **/
    public void annullaModificaTrasportoBeni(it.cnr.jada.UserContext userContext)
            throws it.cnr.jada.comp.ComponentException {
        try {
            rollbackToSavepoint(userContext, "INVENTARIO_BENI_APG");
        } catch (java.sql.SQLException e) {
            // Verifica se è l'errore ORA-01086
            if (e.getMessage() != null && e.getMessage().contains("ORA-01086")) {
                System.out.println("Savepoint INVENTARIO_BENI_APG non trovato - probabilmente non ancora creato");
                System.out.println("Questo può essere normale durante l'inizializzazione del SelezionatoreListaBP");
                return;
            }
            throw handleException(e);
        }
    }


    /**
     * Trasporta tutti i beni disponibili.
     **/
    public void trasportaTuttiBeni(UserContext userContext, Doc_trasporto_rientroBulk docT, CompoundFindClause clauses) throws ComponentException {

        try {
            Inventario_beniHome homebeni = (Inventario_beniHome) getHome(userContext, Inventario_beniBulk.class);
            SQLBuilder sql = homebeni.createSQLBuilder();

            sql.addClause(clauses);
            sql.addSQLClause("AND", "INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS, docT.getInventario().getPg_inventario());
            //sql.addSQLClause("AND","INVENTARIO_BENI.CD_CDS",SQLBuilder.EQUALS,it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(userContext));
            //sql.addSQLClause("AND","INVENTARIO_BENI.CD_UNITA_ORGANIZZATIVA",SQLBuilder.EQUALS,it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(userContext));
            sql.addSQLClause("AND", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO", SQLBuilder.EQUALS, Inventario_beniBulk.ISNOTTOTALMENTESCARICATO);
            sql.addSQLClause("AND", "INVENTARIO_BENI.DT_VALIDITA_VARIAZIONE", SQLBuilder.LESS_EQUALS, docT.getDataRegistrazione());

            SQLBuilder sql_notExists = getHome(userContext, Inventario_beni_apgBulk.class).createSQLBuilder();
            sql_notExists.addSQLJoin("INVENTARIO_BENI.PG_INVENTARIO", "INVENTARIO_BENI_APG.PG_INVENTARIO(+)");
            sql_notExists.addSQLJoin("INVENTARIO_BENI.NR_INVENTARIO", "INVENTARIO_BENI_APG.NR_INVENTARIO(+)");
            sql_notExists.addSQLJoin("INVENTARIO_BENI.PROGRESSIVO", "INVENTARIO_BENI_APG.PROGRESSIVO(+)");
            sql_notExists.addSQLClause("AND", "INVENTARIO_BENI_APG.LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, docT.getLocal_transactionID());
            sql.addSQLNotExistsClause("AND", sql_notExists);
            // Locka le righe
            sql.setForUpdate(true);
            List beni = homebeni.fetchAll(sql);

            for (Iterator i = beni.iterator(); i.hasNext(); ) {
                Inventario_beniBulk bene = (Inventario_beniBulk) i.next();

                Inventario_beni_apgBulk new_bene_apg = new Inventario_beni_apgBulk();
                new_bene_apg.setPg_inventario(bene.getPg_inventario());
                new_bene_apg.setNr_inventario(bene.getNr_inventario());
                new_bene_apg.setProgressivo(bene.getProgressivo());
                new_bene_apg.setDt_validita_variazione(bene.getDt_validita_variazione());
                new_bene_apg.setLocal_transaction_id(docT.getLocal_transactionID());
                if (docT.getTipoMovimento() != null) {
                    new_bene_apg.setVariazione_meno(bene.getVariazione_meno());
                    new_bene_apg.setValore_alienazione(bene.getValore_alienazione());
                    new_bene_apg.setFl_totalmente_scaricato(bene.getFl_totalmente_scaricato());
                }
                new_bene_apg.setTi_documento(docT.getTiDocumento());
                // Pg_buono_c_s ha il valore del pg doc trasporto rientro
                new_bene_apg.setPg_buono_c_s(docT.getPgDocTrasportoRientro());
                new_bene_apg.setToBeCreated();
                super.creaConBulk(userContext, new_bene_apg);
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    /**
     * Restituisce, tra tutti i beni trasportati nel Documento la MAX(dt_validita_variazione),
     * ossia, la data corrispondente alla modifica più recente.
     **/
    private java.sql.Timestamp getMaxDataFor(UserContext userContext, Doc_trasporto_rientroBulk docT) throws ComponentException {
        Timestamp max_data = null;
        try {
            Inventario_beni_apgHome home = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);
            max_data = home.getMaxDataFor(docT.getLocal_transactionID());
            if (max_data == null)
                throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");
        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
        return max_data;
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


    public void modificaBeniTrasportatiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            OggettoBulk[] beni,
            BitSet old_ass,
            BitSet ass)
            throws ComponentException {

        try {
            for (int i = 0; i < beni.length; i++) {
                Inventario_beniBulk bene = (Inventario_beniBulk) beni[i];
                if (old_ass.get(i) != ass.get(i)) {

                    if (ass.get(i)) {
                        // Locko il bene che è stato selezionato per essere scaricato.
                        try {
                            lockBulk(userContext, bene);
                        } catch (OutdatedResourceException oe) {
                            throw handleException(oe);
                        } catch (BusyResourceException bre) {
                            throw new ApplicationException("Risorsa occupata.\nIl bene " + bene.getNumeroBeneCompleto() + " è bloccato da un altro utente.");
                        } catch (PersistencyException pe) {
                            throw handleException(pe);
                        }
                        Inventario_beni_apgBulk new_bene_apg = new Inventario_beni_apgBulk();
                        new_bene_apg.setPg_inventario(bene.getPg_inventario());
                        new_bene_apg.setNr_inventario(bene.getNr_inventario());
                        new_bene_apg.setProgressivo(bene.getProgressivo());
                        new_bene_apg.setDt_validita_variazione(bene.getDt_validita_variazione());
                        new_bene_apg.setLocal_transaction_id(docT.getLocal_transactionID());
                        new_bene_apg.setTi_documento(docT.getTiDocumento());
                        new_bene_apg.setPg_buono_c_s(docT.getPgDocTrasportoRientro());
                        new_bene_apg.setToBeCreated();
                        super.creaConBulk(userContext, new_bene_apg);
                    } else {
                        Inventario_beni_apgHome home = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);
                        SQLBuilder sql = home.createSQLBuilder();
                        sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, docT.getInventario().getPg_inventario());
                        sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
                        sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());
                        sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, docT.getLocal_transactionID());
                        List beni_canc = home.fetchAll(sql);
                        for (Iterator iteratore = beni_canc.iterator(); iteratore.hasNext(); ) {
                            Inventario_beni_apgBulk new_bene_apg = (Inventario_beni_apgBulk) iteratore.next();
                            new_bene_apg.setToBeDeleted();
                            super.eliminaConBulk(userContext, new_bene_apg);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw handleException(e);
        }
    }


    /**
     * Elimina uno o più beni dalla tabella di appoggio INVENTARIO_BENI_APG.
     * <p>
     * I beni vengono sempre eliminati SOLO da INVENTARIO_BENI_APG.
     * Non tocca i dettagli DOC_TRASPORTO_RIENTRO_DETT.
     *
     * @param userContext contesto utente
     * @param doc         documento di trasporto/rientro
     * @param beni        array di beni da eliminare
     */
    public void eliminaBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException {

        if (beni == null || beni.length == 0) return;

        try {
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            for (OggettoBulk o : beni) {
                Inventario_beniBulk bene = (Inventario_beniBulk) o;

                SQLBuilder sql = apgHome.createSQLBuilder();
                sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, doc.getLocal_transactionID());
                sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
                sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
                sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());

                List apgBeni = apgHome.fetchAll(sql);
                for (Iterator it = apgBeni.iterator(); it.hasNext(); ) {
                    Inventario_beni_apgBulk apg = (Inventario_beni_apgBulk) it.next();
                    apg.setToBeDeleted();
                    super.eliminaConBulk(userContext, apg);
                }
            }
        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Elimina TUTTI i beni dalla tabella di appoggio INVENTARIO_BENI_APG.
     * <p>
     * I beni vengono sempre eliminati SOLO da INVENTARIO_BENI_APG.
     * Non tocca i dettagli DOC_TRASPORTO_RIENTRO_DETT.
     */
    public void eliminaTuttiBeniAssociati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            SQLBuilder sql = apgHome.createSQLBuilder();
            sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, doc.getLocal_transactionID());

            List beniApg = apgHome.fetchAll(sql);
            for (Iterator it = beniApg.iterator(); it.hasNext(); ) {
                Inventario_beni_apgBulk apg = (Inventario_beni_apgBulk) it.next();
                apg.setToBeDeleted();
                super.eliminaConBulk(userContext, apg);
            }
        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Cerca gli accessori associati a un bene principale NELLA TABELLA DI APPOGGIO
     * (INVENTARIO_BENI_APG, non nei dettagli salvati DOC_TRASPORTO_RIENTRO_DETT)
     * <p>
     * Usa per l'eliminazione durante la fase di composizione del documento.
     */
    public List cercaBeniAccessoriAssociatiInDettaglio(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale)
            throws ComponentException {

        try {
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            SQLBuilder sql = apgHome.createSQLBuilder();

            // ==================== FILTRI PER LA TABELLA DI APPOGGIO ====================
            // Filtra per la stessa transazione
            sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS,
                    doc.getLocal_transactionID());

            // Filtra per lo stesso inventario
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                    doc.getPgInventario());

            // Filtra per lo stesso bene (stesso NR_INVENTARIO)
            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    benePrincipale.getNr_inventario());

            // Filtra per accessori (PROGRESSIVO > 0)
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

            // Escludi il bene stesso
            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    benePrincipale.getProgressivo());

            List dettagli = apgHome.fetchAll(sql);

            if (dettagli != null && !dettagli.isEmpty()) {
                // Converte gli APG ai beni
                List beniAccessori = new ArrayList();
                for (Iterator it = dettagli.iterator(); it.hasNext(); ) {
                    Inventario_beni_apgBulk apgBene = (Inventario_beni_apgBulk) it.next();

                    // Crea il bene da ritornare
                    Inventario_beniBulk bene = new Inventario_beniBulk(
                            apgBene.getNr_inventario(),
                            apgBene.getPg_inventario(),
                            apgBene.getProgressivo()
                    );
                    beniAccessori.add(bene);
                }
                return beniAccessori;
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
        // ================================================

        return java.util.Collections.emptyList();
    }

    /**
     * Elimina il bene principale E tutti gli accessori associati dalla tabella di appoggio.
     * <p>
     * I beni vengono sempre eliminati SOLO da INVENTARIO_BENI_APG.
     * Non tocca i dettagli DOC_TRASPORTO_RIENTRO_DETT.
     *
     * @param userContext    contesto utente
     * @param doc            documento di trasporto/rientro
     * @param benePrincipale bene principale da eliminare
     * @param beniAccessori  lista di beni accessori da eliminare insieme
     */
    public void eliminaBeniPrincipaleConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Inventario_beniBulk benePrincipale,
            List beniAccessori)
            throws ComponentException {

        try {
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            // ==================== FASE 1: ELIMINA GLI ACCESSORI ====================
            if (beniAccessori != null && !beniAccessori.isEmpty()) {
                for (Iterator it = beniAccessori.iterator(); it.hasNext(); ) {
                    Inventario_beniBulk accessorio = (Inventario_beniBulk) it.next();

                    SQLBuilder sqlApgAcc = apgHome.createSQLBuilder();
                    sqlApgAcc.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, doc.getLocal_transactionID());
                    sqlApgAcc.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
                    sqlApgAcc.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, accessorio.getNr_inventario());
                    sqlApgAcc.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, accessorio.getProgressivo());

                    List apgAccessori = apgHome.fetchAll(sqlApgAcc);
                    for (Iterator itApg = apgAccessori.iterator(); itApg.hasNext(); ) {
                        Inventario_beni_apgBulk apgAcc = (Inventario_beni_apgBulk) itApg.next();
                        apgAcc.setToBeDeleted();
                        super.eliminaConBulk(userContext, apgAcc);
                    }
                }
            }

            // ==================== FASE 2: ELIMINA IL BENE PRINCIPALE ====================
            SQLBuilder sqlApgMain = apgHome.createSQLBuilder();
            sqlApgMain.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, doc.getLocal_transactionID());
            sqlApgMain.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
            sqlApgMain.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, benePrincipale.getNr_inventario());
            sqlApgMain.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, benePrincipale.getProgressivo());

            List apgMain = apgHome.fetchAll(sqlApgMain);
            for (Iterator itApg = apgMain.iterator(); itApg.hasNext(); ) {
                Inventario_beni_apgBulk apgBene = (Inventario_beni_apgBulk) itApg.next();
                apgBene.setToBeDeleted();
                super.eliminaConBulk(userContext, apgBene);
            }

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }




    /**
     * Filtra i beni per l'Unità Organizzativa dell'utente di contesto.
     *
     * NUOVA LOGICA:
     * - Ignora completamente il "tipo ritiro" (INCARICATO/VETTORE)
     * - Filtra SEMPRE per UO dell'utente collegato a CD_ASSEGNATARIO
     */
    private void filtroPerUnitaOrganizzativaUtente(
            SQLBuilder sql,
            Doc_trasporto_rientroBulk doc,
            UserContext userContext)
            throws ApplicationException {

        String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);

        if (cdUo == null) {
            throw new ApplicationException(
                    "Impossibile determinare l'Unità Organizzativa dell'utente."
            );
        }

        // JOIN con TERZO e ANAGRAFICO per filtrare per UO
        sql.addTableToHeader("TERZO TZ");
        sql.addTableToHeader("ANAGRAFICO AG");

        // Join tra INVENTARIO_BENI e TERZO
        sql.addSQLJoin("INVENTARIO_BENI.CD_ASSEGNATARIO", SQLBuilder.EQUALS, "TZ.CD_TERZO");

        // Join tra TERZO e ANAGRAFICO
        sql.addSQLJoin("TZ.CD_ANAG", SQLBuilder.EQUALS, "AG.CD_ANAG");

        // Filtro per UO dell'utente
        sql.addSQLClause("AND", "AG.CD_UNITA_ORGANIZZATIVA",
                SQLBuilder.EQUALS, cdUo);
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
     * Inizializza la selezione dei beni da far rientrare
     * Crea un savepoint per il rollback successivo
     */
    public void inizializzaBeniDaFarRientrare(UserContext userContext)
            throws ComponentException {

        try {
            setSavepoint(userContext, "INVENTARIO_BENI_APG");
        } catch (java.sql.SQLException e) {
            throw handleException(e);
        }
    }

    /**
     * Fa rientrare tutti i beni filtrati
     * Aggiunge tutti i beni alla tabella temporanea INVENTARIO_BENI_APG
     */
    public void rientraTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            // Usa la stessa logica di cercaBeniDaFarRientrare
            RemoteIterator ri = cercaBeniDaFarRientrare(userContext, doc, null, clauses);

            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            while (ri.hasMoreElements()) {
                Inventario_beniBulk bene = (Inventario_beniBulk) ri.nextElement();

                // Crea record in tabella temporanea
                Inventario_beni_apgBulk apg = new Inventario_beni_apgBulk();
                apg.setPg_inventario(bene.getPg_inventario());
                apg.setNr_inventario(bene.getNr_inventario());
                apg.setProgressivo(bene.getProgressivo());
                apg.setDt_validita_variazione(bene.getDt_validita_variazione());
                apg.setLocal_transaction_id(doc.getLocal_transactionID());
                apg.setTi_documento(doc.getTiDocumento());
                apg.setPg_buono_c_s(doc.getPgDocTrasportoRientro());
                apg.setToBeCreated();

                super.creaConBulk(userContext, apg);
            }

        } catch (Exception e) {
            throw handleException(e);
        }
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
     * Annulla le modifiche sui beni in rientro
     * Rollback al savepoint INVENTARIO_BENI_APG
     */
    public void annullaModificaRientroBeni(UserContext userContext)
            throws ComponentException {

        try {
            rollbackToSavepoint(userContext, "INVENTARIO_BENI_APG");
        } catch (java.sql.SQLException e) {
            // Verifica se è l'errore ORA-01086
            if (e.getMessage() != null && e.getMessage().contains("ORA-01086")) {
                System.out.println("Savepoint INVENTARIO_BENI_APG non trovato - probabilmente non ancora creato");
                return;
            }
            throw handleException(e);
        }
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

        if (doc.getDoc_trasporto_rientro_dettColl() == null) {
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



}