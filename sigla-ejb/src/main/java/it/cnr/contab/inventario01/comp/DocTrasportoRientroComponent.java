package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaHome;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniHome;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rBulk;
import it.cnr.contab.inventario00.docs.bulk.Numeratore_doc_t_rHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.utenze00.bp.CNRUserContext;
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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Component per la gestione dei documenti di Trasporto e Rientro.
 * UNICA FONTE DI VALIDAZIONE E BUSINESS LOGIC.
 */
public class DocTrasportoRientroComponent extends it.cnr.jada.comp.CRUDDetailComponent
        implements Cloneable, Serializable {

    // ========================================
    // INIZIALIZZAZIONE
    // ========================================
    private String intervallo;
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
            intervallo = "0-0";

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

            if (doc.getBene() == null) doc.setBene(new Inventario_beniBulk());
            if (doc.getTerzoIncRitiro() == null) doc.setTerzoIncRitiro(new TerzoBulk());

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

    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk) throws ComponentException {
        if (bulk == null)
            throw new ComponentException("Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!");

        //Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
        bulk =  super.inizializzaBulkPerModifica(aUC, bulk);
        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(aUC, docTR);
        // Carica l'Inventario associato alla UO
        try {
            docTR.setInventario(caricaInventario(aUC));
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC, docTR.getInventario()));
            docTR = (Doc_trasporto_rientroBulk) getHome(aUC, Doc_trasporto_rientroBulk.class).findByPrimaryKey(docTR);

            // ========== AGGIUNGI QUESTO BLOCCO ==========
            // Carica il TerzoBulk assegnatario se presente
            if (docTR.getCdTerzoAssegnatario() != null) {
                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
                        .findByPrimaryKey(new TerzoBulk(docTR.getCdTerzoAssegnatario()));
                if (terzo != null) {
                    docTR.setTerzoIncRitiro(terzo);
                }
            }
            // ==========================================

            BulkHome dettHome = getHomeDocumentoTrasportoRientroDett(aUC, docTR);

            docTR.setDoc_trasporto_rientro_dettColl(new BulkList(( (Doc_trasporto_rientro_dettHome)dettHome).getDetailsFor(docTR)));
            for (Iterator dett = docTR.getDoc_trasporto_rientro_dettColl().iterator(); dett.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) dett.next();
                dettaglio.setDoc_trasporto_rientro(docTR);
                Inventario_beniBulk inv = (Inventario_beniBulk) getHome(aUC, Inventario_beniBulk.class).findByPrimaryKey
                        (new Inventario_beniBulk(dettaglio.getNr_inventario(), dettaglio.getPg_inventario(), new Long(dettaglio.getProgressivo().longValue())));
                dettaglio.setBene(inv);
            }
            getHomeCache(aUC).fetchAll(aUC, dettHome);
        } catch (it.cnr.jada.persistency.PersistencyException pe) {
            throw new ComponentException(pe);
        } catch (it.cnr.jada.persistency.IntrospectionException ie) {
            throw new ComponentException(ie);
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

            // ========== CARICA TERZO ASSEGNATARIO ==========
            if (doc.getCdTerzoAssegnatario() != null) {
                TerzoBulk terzoAssegnatario =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoAssegnatario()));
                doc.setTerzoIncRitiro(terzoAssegnatario);
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

        // Chiamata al metodo padre
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

            // ========== CARICA TERZO ASSEGNATARIO ==========
            if (doc.getCdTerzoAssegnatario() != null) {
                TerzoBulk terzoAssegnatario =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoAssegnatario()));
                doc.setTerzoIncRitiro(terzoAssegnatario);
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
     * Trova il dettaglio del documento di TRASPORTO originale per un bene.
     * Necessario per popolare i campi _RIF nel documento di RIENTRO.
     *
     * @param userContext  Contesto utente
     * @param bene         Bene da cercare
     * @param pgInventario Progressivo inventario
     * @return Dettaglio del doc di trasporto, oppure null se non trovato
     */
    private Doc_trasporto_rientro_dettBulk trovaDettaglioTrasportoOriginale(
            UserContext userContext,
            Inventario_beniBulk bene,
            Long pgInventario)
            throws ComponentException {

        try {
            DocumentoTrasportoDettHome dettHome = (DocumentoTrasportoDettHome)
                    getHome(userContext, DocumentoTrasportoDettBulk.class);

            SQLBuilder sql = dettHome.createSQLBuilder();

            // Join con DOC_TRASPORTO_RIENTRO per filtrare solo documenti FIRMATI
            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO dt");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "dt.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "dt.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "dt.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "dt.PG_DOC_TRASPORTO_RIENTRO");

            // Filtra per il bene specifico
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                    SQLBuilder.EQUALS, pgInventario);
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO",
                    SQLBuilder.EQUALS, bene.getNr_inventario());
            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO",
                    SQLBuilder.EQUALS, bene.getProgressivo());

            // Filtra per documenti di TRASPORTO
            sql.addSQLClause("AND", "dt.TI_DOCUMENTO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.TRASPORTO);

            // Filtra per documenti FIRMATI (stato = DEF)
            sql.addSQLClause("AND", "dt.STATO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            // ==================== ESCLUDI BENI GIÀ RIENTRATI ====================
            // Subquery NOT EXISTS per escludere beni già usati in altri rientri
            SQLBuilder sqlNotExists = dettHome.createSQLBuilder();

            // Tabelle della subquery con alias
            sqlNotExists.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett_r");
            sqlNotExists.addTableToHeader("DOC_TRASPORTO_RIENTRO dr");

            // Join interni della subquery
            sqlNotExists.addSQLJoin("dett_r.PG_INVENTARIO", "dr.PG_INVENTARIO");
            sqlNotExists.addSQLJoin("dett_r.TI_DOCUMENTO", "dr.TI_DOCUMENTO");
            sqlNotExists.addSQLJoin("dett_r.ESERCIZIO", "dr.ESERCIZIO");
            sqlNotExists.addSQLJoin("dett_r.PG_DOC_TRASPORTO_RIENTRO",
                    "dr.PG_DOC_TRASPORTO_RIENTRO");

            // Correlazione con la query principale
            // Questi confronti collegano la subquery alla query esterna
            sqlNotExists.addSQLClause("AND", "dett_r.PG_INVENTARIO", SQLBuilder.EQUALS,
                    pgInventario);
            sqlNotExists.addSQLClause("AND", "dett_r.NR_INVENTARIO", SQLBuilder.EQUALS,
                    bene.getNr_inventario());
            sqlNotExists.addSQLClause("AND", "dett_r.PROGRESSIVO", SQLBuilder.EQUALS,
                    bene.getProgressivo());

            // Filtra per documenti di RIENTRO non annullati
            sqlNotExists.addSQLClause("AND", "dr.TI_DOCUMENTO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.RIENTRO);
            sqlNotExists.addSQLClause("AND", "dr.STATO", SQLBuilder.NOT_EQUALS,
                    Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            sql.addSQLNotExistsClause("AND", sqlNotExists);

            // Ordina per data (più recente prima)
            sql.addOrderBy("dt.DATA_REGISTRAZIONE DESC");

            List risultati = dettHome.fetchAll(sql);

            if (risultati != null && !risultati.isEmpty()) {
                // Ritorna il documento di trasporto più recente
                return (DocumentoTrasportoDettBulk) risultati.get(0);
            }

            return null;

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    /**
     * CREAZIONE documento con dettagli.
     * Legge i beni selezionati dalla tabella temporanea INVENTARIO_BENI_APG
     * e crea i dettagli corrispondenti.
     */
    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        try {
            System.out.println("========== INIZIO creaConBulk ==========");

            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

            System.out.println("Tipo documento: " + docT.getClass().getSimpleName());
            System.out.println("TI_DOCUMENTO: " + docT.getTiDocumento());
            System.out.println("PG_INVENTARIO: " + docT.getPgInventario());
            System.out.println("ESERCIZIO: " + docT.getEsercizio());

            Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome)
                    getHome(userContext, Numeratore_doc_t_rBulk.class);

            docT.setPgDocTrasportoRientro(numHome.getNextPg(
                    userContext,
                    docT.getEsercizio(),
                    docT.getPgInventario(),
                    docT.getTiDocumento(),
                    userContext.getUser()));

            System.out.println("PG_DOC_TRASPORTO_RIENTRO generato: " + docT.getPgDocTrasportoRientro());

            // Verifica se esiste già nel database
            Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk)
                    findByPrimaryKey(userContext, docT);

            if (existing != null) {
                throw new ApplicationException(
                        "Il documento è già stato salvato (PG=" + docT.getPgDocTrasportoRientro() + ")"
                );
            }

            // ===== 2. VALIDAZIONE =====
            System.out.println("Inizio validazione documento...");
            validaDoc(userContext, docT);
            System.out.println("Validazione completata con successo");

            // ===== 3. LEGGI BENI DA INVENTARIO_BENI_APG =====
            System.out.println("Lettura beni da INVENTARIO_BENI_APG...");

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

            System.out.println("Trovati " + beniApg.size() + " beni in INVENTARIO_BENI_APG");

            // ===== 4. CREA I DETTAGLI =====
            System.out.println("Inizio creazione dettagli...");

            Inventario_beniHome invBeniHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            SimpleBulkList dettagliList = new SimpleBulkList();

            int contatoreBeni = 0;
            for (Iterator it = beniApg.iterator(); it.hasNext(); ) {
                contatoreBeni++;

                Inventario_beni_apgBulk beneApg = (Inventario_beni_apgBulk) it.next();

                System.out.println("---------- Elaborazione bene " + contatoreBeni + "/" + beniApg.size() + " ----------");
                System.out.println("NR_INVENTARIO: " + beneApg.getNr_inventario() + ", PROGRESSIVO: " + beneApg.getProgressivo());

                Inventario_beniBulk bene = (Inventario_beniBulk) invBeniHome.findByPrimaryKey(
                        new Inventario_beniBulk(
                                beneApg.getNr_inventario(),
                                beneApg.getPg_inventario(),
                                beneApg.getProgressivo()
                        )
                );

                if (bene == null) {
                    System.err.println("Bene non trovato: " + beneApg.getNr_inventario() + "-" + beneApg.getProgressivo());
                    throw new ComponentException(
                            "Bene non trovato: " + beneApg.getNr_inventario() +
                                    "-" + beneApg.getProgressivo());
                }

                System.out.println("Bene trovato: " + bene.getNumeroBeneCompleto());

                Doc_trasporto_rientro_dettBulk dettaglio;
                if (docT instanceof DocumentoTrasportoBulk) {
                    System.out.println("Creazione DocumentoTrasportoDettBulk");
                    dettaglio = new DocumentoTrasportoDettBulk();
                } else {
                    System.out.println("Creazione DocumentoRientroDettBulk");
                    dettaglio = new DocumentoRientroDettBulk();
                }

                dettaglio.setDoc_trasporto_rientro(docT);
                dettaglio.setBene(bene);

                // Imposta chiavi primarie
                dettaglio.setPg_inventario(docT.getPgInventario());
                dettaglio.setTi_documento(docT.getTiDocumento());
                dettaglio.setEsercizio(docT.getEsercizio());
                dettaglio.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
                dettaglio.setNr_inventario(bene.getNr_inventario());
                dettaglio.setProgressivo(bene.getProgressivo().intValue());

                System.out.println("Chiavi primarie impostate sul dettaglio");

                // Gestione TRASPORTO/RIENTRO
                if (docT instanceof DocumentoRientroBulk) {
                    System.out.println("*** Documento RIENTRO: ricerca documento di trasporto originale ***");

                    try {
                        Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                                trovaDettaglioTrasportoOriginale(userContext, bene, docT.getPgInventario());

                        if (dettaglioTrasportoOriginale == null) {
                            System.err.println("ERRORE: dettaglio trasporto originale NON TROVATO per bene " +
                                    bene.getNumeroBeneCompleto());
                            throw new ApplicationException(
                                    "Bene " + bene.getNumeroBeneCompleto() +
                                            " non presente in nessun documento di trasporto firmato (stato DEFINITIVO)!");
                        }

                        System.out.println("Dettaglio trasporto trovato - PG_DOC_RIF: " +
                                dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro() +
                                ", ESERCIZIO_RIF: " + dettaglioTrasportoOriginale.getEsercizio() +
                                ", TI_DOC_RIF: " + dettaglioTrasportoOriginale.getTi_documento());

                        dettaglio.setEsercizioRif(dettaglioTrasportoOriginale.getEsercizio());
                        dettaglio.setTiDocumentoRif(dettaglioTrasportoOriginale.getTi_documento());
                        dettaglio.setPgDocTrasportoRientroRif(
                                dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro());

                        System.out.println("Campi _RIF impostati sul dettaglio di rientro");

                    } catch (Exception e) {
                        System.err.println("ECCEZIONE durante trovaDettaglioTrasportoOriginale: " + e.getMessage());
                        e.printStackTrace(System.err);
                        throw e;
                    }

                } else {
                    System.out.println("*** Documento TRASPORTO: campi _RIF impostati a NULL ***");
                    dettaglio.setEsercizioRif(null);
                    dettaglio.setTiDocumentoRif(null);
                    dettaglio.setPgDocTrasportoRientroRif(null);
                }

                dettaglio.setQuantita(1L);
                dettaglio.setIntervallo(calcolaIntervallo(BigDecimal.valueOf(dettaglio.getQuantita())));

                if (beneApg.getDt_validita_variazione() != null) {
                    dettaglio.setDataEffettivaMovimentazione(beneApg.getDt_validita_variazione());
                }

                dettaglio.setToBeCreated();
                dettagliList.add(dettaglio);

                System.out.println("Dettaglio " + contatoreBeni + " aggiunto alla lista");
            }

            System.out.println("Creazione dettagli completata. Totale dettagli: " + dettagliList.size());

            // ===== 5. ASSOCIA I DETTAGLI AL DOCUMENTO =====
            docT.setDoc_trasporto_rientro_dettColl(dettagliList);
            docT.setToBeCreated();

            System.out.println("Dettagli associati al documento. Inizio salvataggio...");

            // ===== SALVA UNA SOLA VOLTA =====
            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);

            System.out.println("Documento salvato con successo. PG_DOC: " + docT.getPgDocTrasportoRientro());
            System.out.println("========== FINE creaConBulk (SUCCESSO) ==========");

            return docT;

        } catch (PersistencyException e) {
            System.err.println("PersistencyException in creaConBulk: " + e.getMessage());
            e.printStackTrace(System.err);
            throw handleException(e);
        } catch (ApplicationException e) {
            System.err.println("ApplicationException in creaConBulk: " + e.getMessage());
            throw e;
        } catch (Throwable e) {
            System.err.println("Throwable generico in creaConBulk: " + e.getMessage());
            e.printStackTrace(System.err);
            throw handleException(e);
        }
    }


    /**
     * Helper per calcolare l'intervallo progressivo
     */
    private String calcolaIntervallo(java.math.BigDecimal quantita) {
        if (quantita == null) {
            quantita = new java.math.BigDecimal(1);
        }

        java.util.StringTokenizer token = new java.util.StringTokenizer(intervallo, "-", false);
        int intervalloMin = Integer.parseInt(token.nextToken());
        int intervalloMax = Integer.parseInt(token.nextToken());

        intervalloMin = intervalloMax + 1;
        intervalloMax = intervalloMax + quantita.intValue();
        intervallo = Integer.toString(intervalloMin) + "-" + Integer.toString(intervalloMax);

        return intervallo;
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

        try {

        if (Optional.ofNullable(docT.getDoc_trasporto_rientro_dettColl()).isPresent()) {
            for (Iterator i = docT.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) i.next();

                // ==================== DIFFERENZIAZIONE TRASPORTO/RIENTRO ====================
                if (Doc_trasporto_rientroBulk.RIENTRO.equals(docT.getTiDocumento())) {
                    if (dettaglio.getPgDocTrasportoRientroRif() == null) {
                        Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                                trovaDettaglioTrasportoOriginale(aUC, dettaglio.getBene(), docT.getPgInventario());

                        if (dettaglioTrasportoOriginale != null) {
                            dettaglio.setEsercizioRif(dettaglioTrasportoOriginale.getEsercizio());
                            dettaglio.setTiDocumentoRif(dettaglioTrasportoOriginale.getTi_documento());
                            dettaglio.setPgDocTrasportoRientroRif(
                                    dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro());
                        }
                    }
                }

                updateBulk(aUC, dettaglio);
            }
        }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }

        // CHIAMATA UNA SOLA VOLTA
        return super.modificaConBulk(aUC, docT);
    }


    private void validaDoc(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {

            validaDataDoc(aUC, docT);
            //validaBeniDettDDT(aUC, docT);

            // Determina il tipo di documento per il messaggio di errore

            String tipoDoc = "Documento di";
            if (Doc_trasporto_rientroBulk.RIENTRO.equals(docT.getTiDocumento())) {
                tipoDoc = "Rientro";
            } else if (Doc_trasporto_rientroBulk.TRASPORTO.equals(docT.getTiDocumento())) {
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
                throw new ApplicationException("Stato deve essere INSERITO per predisporre alla firma.");
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
     * Cerca i beni trasportabili per un documento di trasporto/rientro
     * ECCEZIONE: Se tipo movimento = SMARTWORKING, skippa il controllo sul tipo ritiro
     */
    public RemoteIterator cercaBeniTrasportabili(UserContext userContext,
                                                 Doc_trasporto_rientroBulk doc,
                                                 SimpleBulkList beni_da_escludere,
                                                 CompoundFindClause clauses)
            throws ComponentException, PersistencyException {

        // ==================== VALIDAZIONE TIPO RITIRO (CON ECCEZIONE SMARTWORKING) ====================
        // Se NON è smartworking, richiedi tipo ritiro selezionato
        if (!doc.isSmartworking() && !doc.hasTipoRitiroSelezionato()) {
            throw new ApplicationException("Selezionare il Tipo Ritiro.");
        }

        SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();

        if (clauses != null) {
            sql.addClause(clauses);
        }

        // ==================== FILTRI BASE ====================
        // Qualificare SEMPRE le colonne con la tabella!
        sql.addSQLClause("AND", "INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS,
                doc.getInventario().getPg_inventario());

        // Filtro beni NON totalmente SCARICATI (NULL o 'N')
        sql.openParenthesis("AND");
        sql.addSQLClause("AND", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO",
                SQLBuilder.ISNULL, null);
        sql.addSQLClause("OR", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO",
                SQLBuilder.EQUALS, "N");
        sql.closeParenthesis();

        // Filtro validità e esercizio
        sql.addSQLClause("AND", "INVENTARIO_BENI.DT_VALIDITA_VARIAZIONE",
                SQLBuilder.LESS_EQUALS, doc.getDataRegistrazione());
        sql.addSQLClause("AND", "INVENTARIO_BENI.ESERCIZIO_CARICO_BENE",
                SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(userContext));

        // ==================== FILTRI PER TIPO RITIRO (SOLO SE NON SMARTWORKING) ====================
        // Se è smartworking, NON applicare filtri su tipo ritiro
        if (!doc.isSmartworking()) {
            filtroTipoRitiro(sql, doc, userContext);
        }

        // ==================== ESCLUDI BENI GIÀ IN DOCUMENTI DI TRASPORTO ====================
        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
            dettHome.escludiBeniInDocumentiTrasporto(
                    sql,
                    doc.getInventario().getPg_inventario(),
                    true
            );
        } catch (Exception e) {
            throw new ComponentException("Errore nell'esclusione dei beni già presenti in documenti: " + e.getMessage(), e);
        }

        // ==================== ESCLUDI BENI GIÀ INSERITI DALL'UTENTE ====================
        escludiBeniGiaInseriti(sql, beni_da_escludere);

        // ==================== ORDINAMENTO ED ESECUZIONE ====================
        sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

        try {
            return iterator(userContext, sql, Inventario_beniBulk.class, null);
        } catch (Exception e) {
            if (isSessionExpired(e)) {
                throw new ApplicationException(
                        "Sessione scaduta. Ricaricare la pagina e ripetere l'operazione."
                );
            }
            throw handleException(e);
        }
    }


    private void escludiBeniGiaInseriti(SQLBuilder sql,
                                        SimpleBulkList beni_da_escludere)
            throws ComponentException, PersistencyException {

        if (beni_da_escludere == null || beni_da_escludere.isEmpty()) {
            return;
        }

        StringBuilder exclusionList = new StringBuilder();

        for (Iterator it = beni_da_escludere.iterator(); it.hasNext(); ) {
            Inventario_beniBulk bene = (Inventario_beniBulk) it.next();

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
                    "(INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO) NOT IN (" + exclusionList + ")");
        }
    }



    private void filtroTipoRitiro(SQLBuilder sql,
                                  Doc_trasporto_rientroBulk doc,
                                  UserContext userContext)
            throws ApplicationException {

        if (doc.isRitiroIncaricato()) {
            // RITIRO INCARICATO
            if (doc.getCdTerzoAssegnatario() == null) {
                throw new ApplicationException("Selezionare il Dipendente Incaricato.");
            }
            sql.addSQLClause("AND", "INVENTARIO_BENI.CD_ASSEGNATARIO",
                    SQLBuilder.EQUALS, doc.getCdTerzoAssegnatario());

        } else if (doc.isRitiroVettore()) {
            // RITIRO VETTORE
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);
            if (cdUo == null) {
                throw new ApplicationException("Unità Organizzativa di contesto non determinabile.");
            }

            sql.addTableToHeader("TERZO TZ");
            sql.addTableToHeader("ANAGRAFICO AG");

            // Join qualificati
            sql.addSQLJoin("INVENTARIO_BENI.CD_ASSEGNATARIO", SQLBuilder.EQUALS, "TZ.CD_TERZO");
            sql.addSQLJoin("TZ.CD_ANAG", SQLBuilder.EQUALS, "AG.CD_ANAG");

            // Filtro qualificato
            sql.addSQLClause("AND", "AG.CD_UNITA_ORGANIZZATIVA",
                    SQLBuilder.EQUALS, cdUo);
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

        SQLBuilder sql = getHome(userContext, DocumentoTrasportoDettBulk.class).createSQLBuilder();

        sql.addSQLClause("AND", "PG_INVENTARIO", sql.EQUALS, docTR.getPgInventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
        sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS, docTR.getPgDocTrasportoRientro());

        try {
            it.cnr.jada.util.RemoteIterator ri = iterator(userContext, sql, bulkClass, null);
            return ri;
        } catch (Exception e) {
            if (isSessionExpired(e)) {
                throw new ApplicationException(
                        "Sessione scaduta. Ricaricare la pagina e ripetere l'operazione."
                );
            }
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
                if (isSessionExpired(e)) {
                    throw new ApplicationException(
                            "Sessione scaduta. Ricaricare la pagina e ripetere l'operazione."
                    );
                }
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
            if (isSessionExpired(e)) {
                throw new ApplicationException(
                        "Sessione scaduta. Ricaricare la pagina e ripetere l'operazione."
                );
            }
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
     * Annulla logicamente il documento.
     *
     * @return documento annullato con TUTTE le relazioni caricate
     */
    public Doc_trasporto_rientroBulk annullaDocumento(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            // Verifica esistenza
            doc = (Doc_trasporto_rientroBulk) findByPrimaryKey(userContext, doc);

            // Validazioni
            if (Doc_trasporto_rientroBulk.STATO_ANNULLATO.equals(doc.getStato())) {
                throw new ApplicationException("Il documento è già annullato");
            }

            if (Doc_trasporto_rientroBulk.STATO_INVIATO.equals(doc.getStato())) {
                throw new ApplicationException(
                        "Impossibile annullare un documento predisposto alla firma"
                );
            }

            // ==================== CAMBIA STATO ====================
            doc.setStato(Doc_trasporto_rientroBulk.STATO_ANNULLATO);
            doc.setToBeUpdated();

            // ==================== SALVA UNA SOLA VOLTA ====================
            doc = (Doc_trasporto_rientroBulk) modificaConBulk(userContext, doc);

            // ==================== RICARICA CON TUTTI I DETTAGLI ====================
            doc = (Doc_trasporto_rientroBulk) inizializzaBulkPerModifica(userContext, doc);

            return doc;

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException("Errore annullamento: " + e.getMessage(), e);
        }
    }



// ========================================
// METODI SPECIFICI RIENTRO - DA AGGIUNGERE PRIMA DELLA CHIUSURA }
// DA AGGIUNGERE IN: DocTrasportoRientroComponent.java
// ========================================

    /**
     * Cerca i beni disponibili per il rientro.
     * VINCOLO CHIAVE: Solo beni presenti in documenti di TRASPORTO FIRMATI (stato = DEF)
     */
// ========================================
// METODI SPECIFICI RIENTRO - DA AGGIUNGERE PRIMA DELLA CHIUSURA }
// DA AGGIUNGERE IN: DocTrasportoRientroComponent.java
// ========================================

    /**
     * Cerca i beni disponibili per il rientro.
     * VINCOLO CHIAVE: Solo beni presenti in documenti di TRASPORTO FIRMATI (stato = DEF)
     * ECCEZIONE: Se tipo movimento = SMARTWORKING, skippa il controllo sul tipo ritiro
     */
    public RemoteIterator cercaBeniDaFarRientrare(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beniEsclusi,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            // ==================== VALIDAZIONE TIPO RITIRO (CON ECCEZIONE SMARTWORKING) ====================
            // Se NON è smartworking, richiedi tipo ritiro selezionato
            if (!doc.isSmartworking() && !doc.hasTipoRitiroSelezionato()) {
                throw new ApplicationException("Selezionare il Tipo Ritiro.");
            }

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

            // ==================== FILTRO 1: SOLO DOCUMENTI TRASPORTO ====================
            sql.addSQLClause("AND", "dt.TI_DOCUMENTO", SQLBuilder.EQUALS, "T");

            // ==================== FILTRO 2: SOLO DOCUMENTI FIRMATI ====================
            sql.addSQLClause("AND", "dt.STATO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            // ==================== FILTRO 3: STESSO INVENTARIO ====================
            sql.addSQLClause("AND", "INVENTARIO_BENI.PG_INVENTARIO", SQLBuilder.EQUALS,
                    doc.getInventario().getPg_inventario());

            // ==================== FILTRO 4: NON GIÀ RIENTRATI ====================
            // Subquery NOT EXISTS per escludere beni già rientrati
            SQLBuilder sqlNotExists = getHome(userContext, Doc_trasporto_rientro_dettBulk.class).createSQLBuilder();
            sqlNotExists.addTableToHeader("DOC_TRASPORTO_RIENTRO_DETT dett_r");
            sqlNotExists.addTableToHeader("DOC_TRASPORTO_RIENTRO dr");

            // Join nella subquery
            sqlNotExists.addSQLJoin("dett_r.PG_INVENTARIO", "dr.PG_INVENTARIO");
            sqlNotExists.addSQLJoin("dett_r.TI_DOCUMENTO", "dr.TI_DOCUMENTO");
            sqlNotExists.addSQLJoin("dett_r.ESERCIZIO", "dr.ESERCIZIO");
            sqlNotExists.addSQLJoin("dett_r.PG_DOC_TRASPORTO_RIENTRO", "dr.PG_DOC_TRASPORTO_RIENTRO");

            // Collega con la query principale
            sqlNotExists.addSQLJoin("dett_r.NR_INVENTARIO", "INVENTARIO_BENI.NR_INVENTARIO");
            sqlNotExists.addSQLJoin("dett_r.PROGRESSIVO", "INVENTARIO_BENI.PROGRESSIVO");

            // Filtra per documenti di RIENTRO non annullati
            sqlNotExists.addSQLClause("AND", "dr.TI_DOCUMENTO", SQLBuilder.EQUALS, "R");
            sqlNotExists.addSQLClause("AND", "dr.STATO", SQLBuilder.NOT_EQUALS,
                    Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            sql.addSQLNotExistsClause("AND", sqlNotExists);

            // ==================== FILTRO 5: BENI NON SCARICATI ====================
            sql.openParenthesis("AND");
            sql.addSQLClause("AND", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO", SQLBuilder.ISNULL, null);
            sql.addSQLClause("OR", "INVENTARIO_BENI.FL_TOTALMENTE_SCARICATO", SQLBuilder.EQUALS, "N");
            sql.closeParenthesis();

            // ==================== FILTRI BASE ====================
            sql.addSQLClause("AND", "INVENTARIO_BENI.DT_VALIDITA_VARIAZIONE",
                    SQLBuilder.LESS_EQUALS, doc.getDataRegistrazione());
            sql.addSQLClause("AND", "INVENTARIO_BENI.ESERCIZIO_CARICO_BENE",
                    SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(userContext));

            // ==================== FILTRI PER TIPO RITIRO (SOLO SE NON SMARTWORKING) ====================
            // Se è smartworking, NON applicare filtri su tipo ritiro
            if (!doc.isSmartworking()) {
                filtroTipoRitiro(sql, doc, userContext);
            }

            // ==================== ESCLUDI BENI GIÀ INSERITI ====================
            escludiBeniGiaInseriti(sql, beniEsclusi);

            // ==================== APPLICA FILTRI UTENTE ====================
            if (clauses != null) {
                sql.addClause(clauses);
            }

            // ==================== ORDINAMENTO ====================
            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            try {
                return iterator(userContext, sql, Inventario_beniBulk.class, null);
            } catch (Exception e) {
                if (isSessionExpired(e)) {
                    throw new ApplicationException(
                            "Sessione scaduta. Ricaricare la pagina e ripetere l'operazione."
                    );
                }
                throw handleException(e);
            }
        } catch (PersistencyException e) {
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
     * Verifica se l'eccezione è dovuta a sessione/connessione scaduta
     */
    private boolean isSessionExpired(Throwable e) {
        if (e == null) return false;

        String message = e.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            if (message.contains("connection reset") ||
                    message.contains("i/o error") ||
                    message.contains("closed connection") ||
                    message.contains("socket") ||
                    message.contains("session") ||
                    message.contains("ora-01012") ||
                    message.contains("ora-03113") ||
                    message.contains("ora-03114")) {
                return true;
            }
        }

        // Controlla anche la causa
        return isSessionExpired(e.getCause());
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
     * 1. Valida che il documento sia in stato INSERITO
     * 2. Valida che abbia dettagli associati
     * 3. Aggiorna i riferimenti incrociati (per RIENTRO)
     * 4. Cambia lo stato a DEFINITIVO
     * 5. Persiste le modifiche
     *
     * @param userContext Contesto utente
     * @param docTR Documento da rendere definitivo
     * @return Documento aggiornato in stato DEFINITIVO
     * @throws ComponentException Se validazione o persistenza falliscono
     */
    public Doc_trasporto_rientroBulk salvaDefinitivo(UserContext userContext, Doc_trasporto_rientroBulk docTR)
            throws ComponentException {

        try {
            // ==================== VALIDAZIONI PRELIMINARI ====================

            if (!Doc_trasporto_rientroBulk.STATO_INSERITO.equals(docTR.getStato())) {
                throw new ApplicationException(
                        "Il documento deve essere in stato INSERITO per poter essere reso definitivo. " +
                                "Stato attuale: " + docTR.getStato()
                );
            }

            if (!docTR.hasDettagli()) {
                throw new ApplicationException(
                        "Impossibile rendere definitivo un documento senza dettagli. " +
                                "Aggiungere almeno un bene al documento."
                );
            }

            if (docTR.isAnnullato()) {
                throw new ApplicationException(
                        "Impossibile rendere definitivo un documento annullato."
                );
            }

            // ==================== CARICAMENTO DETTAGLI ====================

            // Ricarica i dettagli dal database per avere i dati più aggiornati
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, docTR);

            docTR.setDoc_trasporto_rientro_dettColl(
                    new BulkList(dettHome.getDetailsFor(docTR))
            );

            // ==================== AGGIORNAMENTO DETTAGLI ====================

            if (Optional.ofNullable(docTR.getDoc_trasporto_rientro_dettColl()).isPresent()) {
                for (Iterator i = docTR.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) i.next();

                    // ==================== DIFFERENZIAZIONE TRASPORTO/RIENTRO ====================

                    // Se è un RIENTRO, aggiorna i riferimenti al documento di TRASPORTO originale
                    if (Doc_trasporto_rientroBulk.RIENTRO.equals(docTR.getTiDocumento())) {

                        // Se non ha già un riferimento al trasporto, cerca di trovarlo
                        if (dettaglio.getPgDocTrasportoRientroRif() == null) {
                            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                                    trovaDettaglioTrasportoOriginale(userContext, dettaglio.getBene(), docTR.getPgInventario());

                            if (dettaglioTrasportoOriginale != null) {
                                dettaglio.setEsercizioRif(dettaglioTrasportoOriginale.getEsercizio());
                                dettaglio.setTiDocumentoRif(dettaglioTrasportoOriginale.getTi_documento());
                                dettaglio.setPgDocTrasportoRientroRif(
                                        dettaglioTrasportoOriginale.getPg_doc_trasporto_rientro()
                                );
                            }
                        }
                    }

                    // Aggiorna il dettaglio nel database
                    updateBulk(userContext, dettaglio);
                }
            }

            // ==================== CAMBIO STATO A DEFINITIVO ====================

            docTR.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            docTR.setToBeUpdated();

            // ==================== PERSISTENZA ====================

            // Salva il documento con il nuovo stato
            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);


        } catch (PersistencyException e) {
            throw new ComponentException("Errore durante il salvataggio definitivo del documento", e);
        } catch (ApplicationException e) {
            throw new ComponentException(e);
        }
    }

}