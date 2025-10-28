package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
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
import java.util.Iterator;
import java.util.List;

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

    @Override
    public OggettoBulk inizializzaBulkPerInserimento(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

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
            if (doc.getAnagDipRitiro() == null) doc.setAnagDipRitiro(new TerzoBulk());

            return doc;

        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }


    public OggettoBulk inizializzaBulkPerModifica (UserContext aUC,OggettoBulk bulk) throws ComponentException
    {
        if (bulk == null)
            throw new ComponentException("Attenzione: non esiste alcun documento corrispondente ai criteri di ricerca!");

        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk)bulk;
            docTR = (Doc_trasporto_rientroBulk)super.inizializzaBulkPerModifica(aUC, bulk);

        inizializzaTipoMovimento(aUC,docTR);
        // Carica l'Inventario associato alla UO
        try{
            docTR.setInventario(caricaInventario(aUC));
            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC,docTR.getInventario()));
                docTR = (Doc_trasporto_rientroBulk) getHome(aUC,Doc_trasporto_rientroBulk.class).findByPrimaryKey(docTR);

                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)getHome(aUC, Doc_trasporto_rientro_dettBulk.class);

                docTR.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docTR)));
                for (Iterator dett = docTR.getDoc_trasporto_rientro_dettColl().iterator();dett.hasNext();){
                    Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk)dett.next();
                    Inventario_beniBulk inv =(Inventario_beniBulk)getHome(aUC,Inventario_beniBulk.class).findByPrimaryKey
                            (new Inventario_beniBulk(dettaglio.getNrInventario(),dettaglio.getPgInventario(), new Long(dettaglio.getProgressivo().longValue())));
                    dettaglio.setBene(inv);
                }
                getHomeCache(aUC).fetchAll(aUC,dettHome);
        }
        catch(it.cnr.jada.persistency.PersistencyException pe){
            throw new ComponentException (pe);
        }
        catch (it.cnr.jada.persistency.IntrospectionException ie){
            throw new ComponentException (ie);
        }

        return docTR;
    }

//    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk) throws ComponentException {
//        if (bulk == null)
//            throw new ComponentException("Attenzione: non esiste alcun documento di Trasporto/Rientro corrispondente ai criteri di ricerca!");
//
//        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
//        docTR = (Doc_trasporto_rientroBulk) super.inizializzaBulkPerModifica(aUC, bulk);
//
//        inizializzaTipoMovimento(aUC, docTR);
//
//        try {
//            // Carica l'Inventario associato alla UO
//            docTR.setInventario(caricaInventario(aUC));
//            Id_inventarioHome inventarioHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
//            docTR.setConsegnatario(inventarioHome.findConsegnatarioFor(docTR.getInventario()));
//            docTR.setDelegato(inventarioHome.findDelegatoFor(docTR.getInventario()));
//            docTR.setUo_consegnataria(inventarioHome.findUoRespFor(aUC, docTR.getInventario()));
//
//            // Ricarica la testata completa
//            docTR = (Doc_trasporto_rientroBulk) getHome(aUC, Doc_trasporto_rientroBulk.class).findByPrimaryKey(docTR);
//
//            // Carica i dettagli (ORA funzionerà perché il costruttore inizializza gli oggetti correlati)
//            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHome(aUC, Doc_trasporto_rientro_dettBulk.class);
//            Doc_trasporto_rientro_dettBulk dettDocTR = new Doc_trasporto_rientro_dettBulk();
//            List detailsFor = dettHome.getDetailsFor(docTR);
//            docTR.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docTR)));
//
//            // Completa l'inizializzazione dei dettagli
//            inizializzaDettagli(aUC, docTR);
//
//            getHomeCache(aUC).fetchAll(aUC, dettHome);
//
//        } catch (IntrospectionException e) {
//            throw new RuntimeException(e);
//        } catch (PersistencyException e) {
//            throw new RuntimeException(e);
//        }
//        return docTR;
//    }


//    @Override
//    public OggettoBulk inizializzaBulkPerModifica(UserContext aUC, OggettoBulk bulk)
//            throws ComponentException {
//        if (bulk == null) throw new ComponentException("Documento non trovato!");
//
//        try {
//            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
//
//            if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
//                // Carica testata
//                doc = (Doc_trasporto_rientroBulk) getHome(aUC, Doc_trasporto_rientroBulk.class).findByPrimaryKey(doc);
//
//                // Carica dettagli
//                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
//                        getHome(aUC, Doc_trasporto_rientro_dettBulk.class);
//                doc.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(doc)));
//
//                // Popola riferimenti
//                for (Iterator it = doc.getDoc_trasporto_rientro_dettColl().iterator(); it.hasNext(); ) {
//                    Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) it.next();
//                    dett.setDoc_trasporto_rientro(doc);
//
//                    if (dett.getNrInventario() != null && dett.getProgressivo() != null) {
//                        Inventario_beniBulk bene = new Inventario_beniBulk(
//                                dett.getNrInventario(), dett.getPgInventario(), Long.valueOf(dett.getProgressivo()));
//                        dett.setBene((Inventario_beniBulk) getHome(aUC, Inventario_beniBulk.class).findByPrimaryKey(bene));
//                    }
//
//                    if (dett.getPgInventarioRif() != null) {
//                        Doc_trasporto_rientro_dettBulk dettRif = new Doc_trasporto_rientro_dettBulk(
//                                dett.getPgInventarioRif(), dett.getTiDocumentoRif(), dett.getEsercizioRif(),
//                                dett.getPgDocTrasportoRientroRif(), dett.getNrInventarioRif(), dett.getProgressivoRif());
//                        dett.setDocTrasportoRientroDettRif((Doc_trasporto_rientro_dettBulk)
//                                getHome(aUC, Doc_trasporto_rientro_dettBulk.class).findByPrimaryKey(dettRif));
//                    }
//                }
//                getHomeCache(aUC).fetchAll(aUC, dettHome);
//            }
//
//            // Inizializza contesto
//            inizializzaTipoMovimento(aUC, doc);
//            doc.setCondizioni(getHome(aUC, Condizione_beneBulk.class).findAll());
//            doc.setInventario(caricaInventario(aUC));
//
//            Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
//            doc.setConsegnatario(invHome.findConsegnatarioFor(doc.getInventario()));
//            doc.setDelegato(invHome.findDelegatoFor(doc.getInventario()));
//            doc.setUo_consegnataria(invHome.findUoRespFor(aUC, doc.getInventario()));
//
//            // Carica dipendente incaricato
//            if (doc.getCdTerzoAssegnatario() != null) {
//                TerzoBulk terzo = (TerzoBulk) getHome(aUC, TerzoBulk.class)
//                        .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoAssegnatario()));
//                if (terzo != null) {
//                    if (terzo.getCd_anag() != null && terzo.getAnagrafico() == null) {
//                        terzo.setAnagrafico((AnagraficoBulk) getHome(aUC, AnagraficoBulk.class)
//                                .findByPrimaryKey(new AnagraficoBulk(terzo.getCd_anag())));
//                    }
//                    if (terzo.getAnagrafico() == null /*todo da risolvere || terzo.getAnagrafico().getCd_unita_organizzativa() == null*/) {
//                        throw new ApplicationException("Anagrafico senza UO associata.");
//                    }
//                    doc.setAnagDipRitiro(terzo);
//                } else {
//                    doc.setAnagDipRitiro(new TerzoBulk());
//                }
//            }
//
//            return doc;
//
//        } catch (PersistencyException | IntrospectionException e) {
//            throw new ComponentException(e);
//        }
//    }

    @Override
    public OggettoBulk inizializzaBulkPerRicerca(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            inizializzaTipoMovimento(userContext, doc);
            doc.setInventario(caricaInventario(userContext));
            return doc;
        } catch (PersistencyException | IntrospectionException e) {
            throw new ComponentException(e);
        }
    }

// ========================================
// CREAZIONE E MODIFICA
// ========================================

//
//    /**
//     * CREAZIONE documento con dettagli.
//     * Gestisce l'inserimento della testata e della SimpleBulkList di dettagli.
//     */
//    @Override
//    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
//            throws ComponentException {
//
//        try {
//            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;
//
//            Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome) getHome(userContext, Numeratore_doc_t_rBulk.class);
//
//            try {
//                docT.setPgDocTrasportoRientro(numHome.getNextPg(
//                        userContext,
//                        docT.getEsercizio(),
//                        docT.getPgInventario(),
//                        docT.getTiDocumento(),
//                        userContext.getUser()));
//
//            } catch (Throwable e) {
//                throw new ComponentException(e);
//            }
//
//
//            // ===== VALIDAZIONE TESTATA =====
//            validaDDT(userContext, docT);
//
//            // ===== 1. SALVA LA TESTATA =====
//            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);
//
//            // ===== 2. SALVA I DETTAGLI =====
//            SimpleBulkList dettagliDDT = docT.getDoc_trasporto_rientro_dettColl();
//
//            if (dettagliDDT != null && !dettagliDDT.isEmpty()) {
//                for (Iterator it = dettagliDDT.iterator(); it.hasNext(); ) {
//                    Doc_trasporto_rientro_dettBulk dettaglio =
//                            (Doc_trasporto_rientro_dettBulk) it.next();
//
//                    // Sincronizza le FK con la testata appena salvata
//                    dettaglio.setDoc_trasporto_rientro(docT);
//                    super.creaConBulk(userContext, dettaglio);
//                }
//            }
//
//            return docT;
//
//        } catch (Throwable e) {
//            throw handleException(e);
//        }
//    }


    /**
     * CREAZIONE documento con dettagli.
     * Legge i beni selezionati dalla tabella temporanea INVENTARIO_BENI_APG
     * e crea i dettagli corrispondenti.
     */
//TODO da testare
    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        try {
            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;
            docT.setTiDocumento(docT.getTipoMovimento().getTiDocumento());
            docT.setPgInventario(docT.getInventario().getPg_inventario());
            docT.setTipoMovimento(docT.getTipoMovimento());
            // ===== 1. GENERA IL PROGRESSIVO =====
            Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome)
                    getHome(userContext, Numeratore_doc_t_rBulk.class);

            try {
                docT.setPgDocTrasportoRientro(numHome.getNextPg(
                        userContext,
                        docT.getEsercizio(),
                        docT.getPgInventario(),
                        docT.getTiDocumento(),
                        userContext.getUser()));

            } catch (Throwable e) {
                throw new ComponentException(e);
            }


            // ===== 2. VALIDAZIONE TESTATA =====
            validaDDT(userContext, docT);

            // ===== 3. SALVA LA TESTATA =====
            docT = (Doc_trasporto_rientroBulk) super.creaConBulk(userContext, docT);

            // ===== 4. LEGGI I BENI SELEZIONATI DA INVENTARIO_BENI_APG =====
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome)
                    getHome(userContext, Inventario_beni_apgBulk.class);

            SQLBuilder sqlApg = apgHome.createSQLBuilder();
            sqlApg.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS,
                    docT.getLocal_transactionID());
            sqlApg.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

            List beniApg = apgHome.fetchAll(sqlApg);

            if (beniApg == null || beniApg.isEmpty()) {
                throw new ApplicationException(
                        "Attenzione: è necessario specificare almeno un bene da trasportare!");
            }

            // ===== 5. PER OGNI BENE, CREA IL DETTAGLIO =====
            Inventario_beniHome invBeniHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            for (Iterator it = beniApg.iterator(); it.hasNext(); ) {
                Inventario_beni_apgBulk beneApg = (Inventario_beni_apgBulk) it.next();

                // Carica il bene completo
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

                // ===== 6. CREA IL DETTAGLIO =====
                Doc_trasporto_rientro_dettBulk dettaglio = new Doc_trasporto_rientro_dettBulk();

                // IMPORTANTE: Imposta ESPLICITAMENTE tutte le 6 chiavi primarie
                // usando i metodi SNAKE_CASE
                dettaglio.setPgInventario(docT.getPgInventario());
                dettaglio.setTiDocumento(docT.getTiDocumento());
                dettaglio.setEsercizio(docT.getEsercizio());
                dettaglio.setPgDocTrasportoRientro(docT.getPgDocTrasportoRientro());
                dettaglio.setNrInventario(bene.getNr_inventario());
                dettaglio.setProgressivo(bene.getProgressivo().intValue());

                dettaglio.setDoc_trasporto_rientro(docT);

                // E il bene (per gli altri campi non-chiave)
                dettaglio.setBene(bene);

                // Imposta campi obbligatori
                dettaglio.setQuantita(1L);
                dettaglio.setIntervallo(calcolaIntervallo(BigDecimal.valueOf(dettaglio.getQuantita())));

                // Copia dati da APG se necessario
                if (beneApg.getDt_validita_variazione() != null) {
                    dettaglio.setDataEffettivaMovimentazione(
                            beneApg.getDt_validita_variazione());
                }

                // Marca come da creare
                dettaglio.setToBeCreated();

                // ===== 7. SALVA IL DETTAGLIO =====
                super.creaConBulk(userContext, dettaglio);

            }

            return docT;

        } catch (PersistencyException e) {
            throw handleException(e);
        } catch (Throwable e) {
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

//    /**
//     * MODIFICA documento con dettagli.
//     * Gestisce insert/update/delete della SimpleBulkList di dettagli.
//     */
//    @Override
//    public OggettoBulk modificaConBulk(UserContext userContext, OggettoBulk bulk)
//            throws ComponentException {
//
//        try {
//            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;
//
//            // ===== VALIDAZIONE TESTATA =====
//            validaDDT(userContext, docT);
//
//            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHome(userContext, Doc_trasporto_rientroBulk.class);
//            docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docT)));
//            SimpleBulkList dettagli = docT.getDoc_trasporto_rientro_dettColl();
//            for (Iterator i = dettagli.iterator(); i.hasNext(); ) {
//                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) i.next();
//                Inventario_beniBulk inv = (Inventario_beniBulk) getHome(userContext, Inventario_beniBulk.class).
//                        findByPrimaryKey(new Inventario_beniBulk(dettaglio.getNr_inventario(), dettaglio.getPgInventario(), new Long(dettaglio.getProgressivo().longValue())));
//                dettaglio.setBene(inv);
//                updateBulk(userContext, dettaglio);
//            }
//        } catch (Throwable e) {
//            throw handleException(e);
//        }
//        return super.modificaConBulk(userContext, bulk);
//
//    }

    //TODO da testare
    @Override
    /**
     * Modifica Documento di Trasporto/Rientro
     * PreCondition:
     *   E' stata generata la richiesta di modificare un Documento di Trasporto/Rientro.
     * PostCondition:
     *   Viene consentito il salvataggio.
     *
     * @param aUC lo <code>UserContext</code> che ha generato la richiesta
     * @param bulk <code>OggettoBulk</code> il Bulk da modificare
     *
     * @return l'oggetto <code>OggettoBulk</code> modificato
     **/
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

            // Ricarica i dettagli dal database (come fa buono_carico_scarico per SCARICO)
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
                    getHome(aUC, Doc_trasporto_rientro_dettBulk.class);
            docT.setDoc_trasporto_rientro_dettColl(
                    new BulkList(dettHome.getDetailsFor(docT)));

            SimpleBulkList dettagli = docT.getDoc_trasporto_rientro_dettColl();


            for (Iterator i = dettagli.iterator(); i.hasNext(); ) {
                Doc_trasporto_rientro_dettBulk dettaglio =
                        (Doc_trasporto_rientro_dettBulk) i.next();

                // Ricarica il bene completo dal database
                Inventario_beniBulk inv = (Inventario_beniBulk)
                        getHome(aUC, Inventario_beniBulk.class).findByPrimaryKey(
                                new Inventario_beniBulk(
                                        dettaglio.getNrInventario(),
                                        dettaglio.getPgInventario(),
                                        new Long(dettaglio.getProgressivo().longValue())
                                )
                        );

                dettaglio.setBene(inv);

                // Validazione del documento
                validaDDT(aUC, docT);

                // Salva le modifiche al dettaglio
                updateBulk(aUC, dettaglio);
            }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }

        return super.modificaConBulk(aUC, bulk);
    }


    private void validaDDT(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {
            validaDataDDT(aUC, docT);
            validaBeniDettDDT(aUC, docT);

            if (docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: data di Trasporto non valida.\n La Data di Trasporto non può essere precedente ad una modifica di uno dei beni trasportati");
            }

            if (docT.getTipoMovimento() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: specificare un Tipo di Movimento per il DDT");

            if (docT.getDsDocTrasportoRientro() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare una Descrizione per il DDT");

        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    private void validaDataDDT(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {
            java.sql.Timestamp dataOdierna = getHome(aUC, Doc_trasporto_rientroBulk.class).getServerDate();
            if (docT.getDataRegistrazione() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare una Data di Trasporto");

            // CONTROLLA LA DATA DI Trasporto - DATA DI Trasporto>SYSDATE
            if (docT.getDataRegistrazione().after(dataOdierna))
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: data di Trasporto non valida. La Data di  Trasporto non può essere superiore alla data odierna");

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

    private void validaBeniDettDDT(UserContext userContext, Doc_trasporto_rientroBulk docT) throws ComponentException {

        boolean hasNoDetails = true;
        try {
            Inventario_beni_apgHome apgHome = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);

            SQLBuilder sql = apgHome.createSQLBuilder();
            sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", sql.EQUALS, docT.getLocal_transactionID());
            List beniApg = apgHome.fetchAll(sql);
            for (Iterator i = beniApg.iterator(); i.hasNext(); ) {
                Inventario_beni_apgBulk beneApg = (Inventario_beni_apgBulk) i.next();
                if (beneApg != null) {
                    hasNoDetails = false;
                    //aggiungiDettDDT(userContext, docT, beneDettAss);
                }
            }
            if (hasNoDetails) {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: è necessario specificare almeno un bene da trasportare!");
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


//    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
//            throws ComponentException {
//        Doc_trasporto_rientroBulk docTR = (Doc_trasporto_rientroBulk) bulk;
//        SimpleBulkList dettagliUpd = new SimpleBulkList();
//        try {
//            if (docTR.getTiDocumento().equals(Doc_trasporto_rientroBulk.RIENTRO)) {
//                //TODO SCRIVI LOGICA X RIENTRO
//            } else {
//                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHome(aUC, Doc_trasporto_rientro_dettBulk.class);
//                docTR.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docTR)));
//                validaEInsUpdDDT(aUC, docTR);
//                return super.modificaConBulk(aUC, docTR);
//            }
//        } catch (PersistencyException e) {
//            throw new it.cnr.jada.comp.ComponentException(e);
//        }

    /// /        checkDettagliDDT(docTR);
//        return super.modificaConBulk(aUC, bulk);
//    }


// ========================================
// TRANSIZIONI STATO
// ========================================
    public Doc_trasporto_rientroBulk predisponiAllaFirma(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            if (!Doc_trasporto_rientroBulk.STATO_INSERITO.equals(doc.getStato())) {
                throw new ApplicationException("Stato deve essere INSERITO per predisporre alla firma.");
            }

            doc.setStato(Doc_trasporto_rientroBulk.STATO_PREDISPOSTO_FIRMA);
            makeBulkPersistent(userContext, doc);
            return doc;

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException("Errore predisposizione firma: " + e.getMessage(), e);
        }
    }

// ========================================
// GESTIONE BENI
// ========================================

//    public Doc_trasporto_rientroBulk aggiungiDettDDT(UserContext userContext, Doc_trasporto_rientroBulk doc,
//                                                     Inventario_beniBulk bene) throws ComponentException {
//        try {
//            // Crea dettaglio
//            Doc_trasporto_rientro_dettBulk dettaglio = new Doc_trasporto_rientro_dettBulk();
//            dettaglio.setDoc_trasporto_rientro(doc);
//            dettaglio.setPgInventario(doc.getPgInventario());
//            dettaglio.setTiDocumento(doc.getTiDocumento());
//            dettaglio.setEsercizio(doc.getEsercizio());
//            dettaglio.setPgDocTrasportoRientro(doc.getPgDocTrasportoRientro());
//            dettaglio.setBene(bene);
//            dettaglio.setNr_inventario(bene.getNr_inventario());
//            dettaglio.setProgressivo(bene.getProgressivo().intValue());
//            dettaglio.setQuantita(1L);
//
//
////            // Salva se persistente
////            if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
////                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
////                        getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
////                dettHome.insert(dettaglio, userContext);
////            }
//

    /// /            // Aggiungi a collection
    /// /            if (doc.getDoc_trasporto_rientro_dettColl() == null) {
    /// /                doc.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
    /// /            }
//            doc.getDoc_trasporto_rientro_dettColl().add(dettaglio);
//        } catch (Exception e) {
//            throw new ComponentException(e);
//        }
//
//
//        return doc;
//    }
//    public Doc_trasporto_rientroBulk aggiungiDettDDT(UserContext userContext,
//                                                     Doc_trasporto_rientroBulk doc,
//                                                     Inventario_beniBulk bene)
//            throws ComponentException {
//        try {
//            // Crea dettaglio
//            Doc_trasporto_rientro_dettBulk dettaglio = new Doc_trasporto_rientro_dettBulk();
//
//            // ========================================
//            // CHIAVI PRIMARIE - USA METODI SNAKE_CASE
//            // ========================================
//            dettaglio.setPgInventario(doc.getPg_inventario());
//            dettaglio.setTiDocumento(doc.getTi_documento());
//            dettaglio.setEsercizio(doc.getEsercizio());
//            dettaglio.setPgDocTrasportoRientro(doc.getPg_doc_trasporto_rientro());
//            dettaglio.setNrInventario(bene.getNr_inventario());
//            dettaglio.setProgressivo(bene.getProgressivo().intValue());
//
//            // ========================================
//            // TESTATA E BENE
//            // ========================================
//            dettaglio.setDoc_trasporto_rientro(doc);
//            dettaglio.setBene(bene);
//
//            // ========================================
//            // CAMPI OBBLIGATORI
//            // ========================================
//            dettaglio.setQuantita(1L);
//            dettaglio.setIntervallo("1-1");
//
//            // ========================================
//            // STATO CRUD
//            // ========================================
//            dettaglio.setToBeCreated();
//
//            // Aggiungi alla collection
//            doc.getDoc_trasporto_rientro_dettColl().add(dettaglio);
//
//        } catch (Exception e) {
//            throw new ComponentException(e);
//        }
//
//        return doc;
//    }

//    public Doc_trasporto_rientroBulk rimuoviBeneDDT(UserContext userContext, Doc_trasporto_rientroBulk doc,
//                                                    Doc_trasporto_rientro_dettBulk dettaglio) throws ComponentException {
//        try {
//            if (doc.getPg_doc_trasporto_rientro() != null && doc.getPg_doc_trasporto_rientro() > 0) {
//                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
//                        getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
//                dettHome.delete(dettaglio, userContext);
//            }
//            if (doc.getDoc_trasporto_rientro_dettColl() != null) {
//                doc.getDoc_trasporto_rientro_dettColl().remove(dettaglio);
//            }
//            return doc;
//        } catch (PersistencyException e) {
//            throw new ComponentException(e);
//        }
//    }

//    public Doc_trasporto_rientroBulk aggiornaQuantitaBene(UserContext userContext, Doc_trasporto_rientroBulk doc,
//                                                          Doc_trasporto_rientro_dettBulk dettaglio) throws ComponentException {
//        try {
//            if (dettaglio.getQuantita() == null || dettaglio.getQuantita() <= 0) {
//                throw new ApplicationException("Quantità deve essere maggiore di zero.");
//            }
//            if (doc.getPg_doc_trasporto_rientro() != null && doc.getPg_doc_trasporto_rientro() > 0) {
//                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
//                        getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
//                dettHome.update(dettaglio, userContext);
//            }
//            return doc;
//        } catch (ApplicationException e) {
//            throw e;
//        } catch (PersistencyException e) {
//            throw new ComponentException(e);
//        }
//    }
    public void eliminaTuttiBeniDettaglio(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
                Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
                        getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
                List dettagli = dettHome.getDetailsFor(doc);
                for (Iterator it = dettagli.iterator(); it.hasNext(); ) {
                    dettHome.delete((Doc_trasporto_rientro_dettBulk) it.next(), userContext);
                }
            }
            if (doc.getDoc_trasporto_rientro_dettColl() != null) {
                doc.getDoc_trasporto_rientro_dettColl().clear();
            }
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }
//
//    public void eliminaBeniSelezionati(UserContext userContext, Doc_trasporto_rientroBulk doc,
//                                       Doc_trasporto_rientro_dettBulk[] dettagliDaEliminare) throws ComponentException {
//        try {
//            if (dettagliDaEliminare == null || dettagliDaEliminare.length == 0) {
//                throw new ApplicationException("Nessun bene selezionato.");
//            }
//            if (!doc.isModificabile()) {
//                throw new ApplicationException("Documento non modificabile nello stato: " + doc.getStato());
//            }
//
//            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
//                    getHome(userContext, Doc_trasporto_rientro_dettBulk.class);
//
//            for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
//                if (!dettaglio.getPgDocTrasportoRientro().equals(doc.getPg_doc_trasporto_rientro())) {
//                    throw new ApplicationException("Dettaglio non appartiene al documento.");
//                }
//
//                if (doc.getPg_doc_trasporto_rientro() > 0) {
//                    dettHome.delete(dettaglio, userContext);
//                }
//                if (doc.getDoc_trasporto_rientro_dettColl() != null) {
//                    doc.getDoc_trasporto_rientro_dettColl().remove(dettaglio);
//                }
//            }
//
//        } catch (ApplicationException e) {
//            throw e;
//        } catch (PersistencyException e) {
//            throw new ComponentException("Errore eliminazione beni: " + e.getMessage(), e);
//        }
//    }


    /**
     * Cerca i beni trasportabili per un documento di trasporto/rientro
     * <p>
     * MODIFICHE APPLICATE:
     * 1. Esclude i beni già presenti in altri documenti di trasporto/rientro
     * 2. Applica il filtro per unità organizzativa (tramite filtroTipoRitiro)
     *
     * @param userContext       contesto utente
     * @param doc               documento di trasporto/rientro
     * @param beni_da_escludere beni già selezionati dall'utente
     * @param clauses           clausole aggiuntive di ricerca
     * @return RemoteIterator con i beni disponibili
     */
    public RemoteIterator cercaBeniTrasportabili(UserContext userContext,
                                                 Doc_trasporto_rientroBulk doc,
                                                 SimpleBulkList beni_da_escludere,
                                                 CompoundFindClause clauses)
            throws ComponentException, PersistencyException {

        // ==================== VALIDAZIONE ====================
        if (!doc.hasTipoRitiroSelezionato()) {
            throw new ApplicationException("Selezionare il Tipo Ritiro.");
        }

        // ==================== CREAZIONE SQLBUILDER ====================
        SQLBuilder sql = getHome(userContext, Inventario_beniBulk.class).createSQLBuilder();

        if (clauses != null) {
            sql.addClause(clauses);
        }

        // ==================== FILTRI BASE ====================
        sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                doc.getInventario().getPg_inventario());

        // Filtro beni NON totalmente trasportati (NULL o 'N')
        sql.openParenthesis("AND");
        sql.addSQLClause("AND", "FL_TOTALMENTE_SCARICATO", SQLBuilder.ISNULL, null);
        sql.addSQLClause("OR", "FL_TOTALMENTE_SCARICATO", SQLBuilder.EQUALS, "N");
        sql.closeParenthesis();

        sql.addSQLClause("AND", "ID_TRANSITO_BENI_ORDINI", SQLBuilder.ISNULL, null);
        sql.addSQLClause("AND", "DT_VALIDITA_VARIAZIONE", SQLBuilder.LESS_EQUALS,
                doc.getDataRegistrazione());
        sql.addClause("AND", "esercizio_carico_bene", SQLBuilder.LESS_EQUALS,
                CNRUserContext.getEsercizio(userContext));

        // ==================== FILTRI PER TIPO RITIRO ====================
        // Questo metodo applica anche il filtro per unità organizzativa
        // in base al tipo di ritiro selezionato (UO utente o UO dipendente)
        filtroTipoRitiro(sql, doc, userContext);

        // ==================== ESCLUDI BENI GIÀ IN DOCUMENTI DI TRASPORTO ====================
        // NUOVO: Esclude i beni che sono già presenti in altri documenti di trasporto/rientro
        // Questo evita che lo stesso bene possa essere inserito in più documenti contemporaneamente
        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            // Esclude i beni già presenti in documenti non annullati
            // Il parametro 'true' indica di escludere solo i documenti in stati "aperti" (non annullati)
            dettHome.escludiBeniInDocumentiTrasporto(
                    sql,
                    doc.getInventario().getPg_inventario(),
                    true  // esclude solo documenti non annullati
            );
        } catch (Exception e) {
            throw new ComponentException("Errore nell'esclusione dei beni già presenti in documenti: " + e.getMessage(), e);
        }

        // ==================== ESCLUDI BENI GIÀ INSERITI DALL'UTENTE ====================
        // Esclude i beni che l'utente ha già aggiunto al documento corrente
        escludiBeniGiaInseriti(sql, beni_da_escludere);

        // ==================== ORDINAMENTO ED ESECUZIONE ====================
        sql.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

        return iterator(userContext, sql, Inventario_beniBulk.class, null);
    }

    /**
     * Esclude i beni presenti nella lista passata come parametro
     * <p>
     * Questo metodo esclude i beni che l'utente ha già selezionato
     * e aggiunto al documento corrente (ma non ancora salvato)
     */
    private void escludiBeniGiaInseriti(SQLBuilder sql,
                                        SimpleBulkList beni_da_escludere)
            throws ComponentException, PersistencyException {

        // Verifica che ci siano beni da escludere
        if (beni_da_escludere == null || beni_da_escludere.isEmpty()) {
            return;
        }

        StringBuilder exclusionList = new StringBuilder();

        // Itera sui beni da escludere e costruisce la lista
        for (Iterator it = beni_da_escludere.iterator(); it.hasNext(); ) {
            Inventario_beniBulk bene = (Inventario_beniBulk) it.next();

            // Verifica che il bene abbia i campi chiave valorizzati
            if (bene.getNr_inventario() != null &&
                    bene.getProgressivo() != null) {

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

        // Aggiunge la clausola di esclusione alla query
        if (exclusionList.length() > 0) {
            sql.addSQLClause("AND",
                    "(NR_INVENTARIO, PROGRESSIVO) NOT IN (" + exclusionList + ")");
        }
    }

    /**
     * Applica il filtro appropriato in base al tipo di ritiro
     */
    private void filtroTipoRitiro(SQLBuilder sql,
                                  Doc_trasporto_rientroBulk doc,
                                  UserContext userContext)
            throws ApplicationException {

        if (doc.isRitiroIncaricato()) {
            // RITIRO INCARICATO: filtra per il dipendente specifico
            if (doc.getCdTerzoAssegnatario() == null) {
                throw new ApplicationException("Selezionare il Dipendente Incaricato.");
            }
            sql.addSQLClause("AND", "CD_ASSEGNATARIO", SQLBuilder.EQUALS,
                    doc.getCdTerzoAssegnatario());

        } else if (doc.isRitiroVettore()) {
            // RITIRO VETTORE: utilizza JOIN per filtrare i terzi dell'UO
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);

            if (cdUo == null) {
                throw new ApplicationException(
                        "Unità Organizzativa di contesto non determinabile.");
            }

            // Aggiunge le tabelle TERZO e ANAGRAFICO con i relativi join
            // Questo filtra i beni assegnati a terzi collegati ad anagrafici dell'UO
            sql.addTableToHeader("TERZO");
            sql.addTableToHeader("ANAGRAFICO");

            // Join tra INVENTARIO_BENI e TERZO
            sql.addSQLJoin("INVENTARIO_BENI.CD_ASSEGNATARIO", "TERZO.CD_TERZO");

            // Join tra TERZO e ANAGRAFICO
            sql.addSQLJoin("TERZO.CD_ANAG", "ANAGRAFICO.CD_ANAG");

            // Filtro per Unità Organizzativa
            sql.addSQLClause("AND", "ANAGRAFICO.CD_UNITA_ORGANIZZATIVA",
                    SQLBuilder.EQUALS, cdUo);
        }
    }


//
//    /**
//     * Esclude i beni presenti nella lista passata come parametro
//     */
//    private void escludiBeniGiaInseriti(SQLBuilder sql,
//                                        SimpleBulkList beni_da_escludere)
//            throws ComponentException, PersistencyException {
//
//        // Verifica che ci siano beni da escludere
//        if (beni_da_escludere == null || beni_da_escludere.isEmpty()) {
//            return;
//        }
//
//        StringBuilder exclusionList = new StringBuilder();
//
//        // Itera sui beni da escludere e costruisce la lista
//        for (Iterator it = beni_da_escludere.iterator(); it.hasNext(); ) {
//            Inventario_beniBulk bene = (Inventario_beniBulk) it.next();
//
//            // Verifica che il bene abbia i campi chiave valorizzati
//            if (bene.getNr_inventario() != null &&
//                    bene.getProgressivo() != null) {
//
//                if (exclusionList.length() > 0) {
//                    exclusionList.append(",");
//                }
//
//                exclusionList.append("(")
//                        .append(bene.getNr_inventario())
//                        .append(",")
//                        .append(bene.getProgressivo())
//                        .append(")");
//            }
//        }
//
//        // Aggiunge la clausola di esclusione alla query
//        if (exclusionList.length() > 0) {
//            sql.addSQLClause("AND",
//                    "(NR_INVENTARIO, PROGRESSIVO) NOT IN (" + exclusionList + ")");
//        }
//    }


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

        SQLBuilder sql = getHome(userContext, Doc_trasporto_rientro_dettBulk.class).createSQLBuilder();

        sql.addSQLClause("AND", "PG_INVENTARIO", sql.EQUALS, docTR.getPgInventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO", sql.EQUALS, docTR.getTiDocumento());
        sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, docTR.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", sql.EQUALS, docTR.getPgDocTrasportoRientro());

        it.cnr.jada.util.RemoteIterator ri = iterator(userContext, sql, bulkClass, null);

        return ri;
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
            return iterator(userContext, sql, Inventario_beniBulk.class, null);
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
     * @param docT        il DDT
     * @param bulkClass   la <code>Class</code> modello per il dettaglio.
     * @return iterator <code>RemoteIterator</code> l'Iteratore sulla selezione.
     **/
    public RemoteIterator selectBeniAssociatiByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            Class bulkClass) throws ComponentException {
        SQLBuilder sql = null;

        if (docT.getPgDocTrasportoRientro() != null && docT.getPgDocTrasportoRientro() > 0) {
            // Documento già salvato - cerca nella tabella reale
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
            if (docT.getLocal_transactionID() == null) {
                return new it.cnr.jada.util.EmptyRemoteIterator();
            }

            sql = getHome(userContext, Inventario_beniBulk.class, "V_INVENTARIO_BENI_APG").createSQLBuilder();
            sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", sql.EQUALS, docT.getLocal_transactionID());
        }

        sql.addOrderBy("NR_INVENTARIO");
        sql.addOrderBy("PROGRESSIVO");
        return iterator(userContext, sql, bulkClass, null);
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

//    public void trasportaTuttiBeni(UserContext userContext, Doc_trasporto_rientroBulk docT) throws ComponentException {
//        trasportaTuttiBeni(userContext, docT, null);
//    }


    /**
     * Inserisce i beni temporanei.
     * PreCondition:
     * E' stata generata la richiesta di riportare i beni selezionati dall'utente nella tabella
     * temporanea INVENTARIO_BENI_APG. L'utente, in questa fase, si trova a selezionare dei
     * beni già presenti sul DB, per una operazione di trasporto.
     * PostCondition:
     * Vengono riportati sulla tabella INVENTARIO_BENI_APG i dati relativi ai beni selezionati dall'utente.
     *
     * @param userContext lo <code>UserContext</code> che ha generato la richiesta
     * @param docT        il <code>Doc_trasporto_rientroBulk</code> DDT
     * @param beni        <code>OggettoBulk[]</code> i beni selezionati dall'utente.
     * @param old_ass     la <code>BitSet</code> selezione precedente.
     * @param ass         la <code>BitSet</code> selezione attuale.
     **/
    public void modificaBeniTrasportati(UserContext userContext, Doc_trasporto_rientroBulk docT, OggettoBulk[] beni, java.util.BitSet old_ass, java.util.BitSet ass) throws ComponentException {
        /* Questo metodo viene richiamato tutte le volte che c'è un cambio di pagina del selezionatore
         * dei beni, oppure una richiesta di Riporta.
         */
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
                        } catch (it.cnr.jada.persistency.PersistencyException pe) {
                            throw handleException(pe);
                        }
                        Inventario_beni_apgBulk new_bene_apg = new Inventario_beni_apgBulk();
                        new_bene_apg.setPg_inventario(bene.getPg_inventario());
                        new_bene_apg.setNr_inventario(bene.getNr_inventario());
                        new_bene_apg.setProgressivo(bene.getProgressivo());
                        new_bene_apg.setDt_validita_variazione(bene.getDt_validita_variazione());
                        new_bene_apg.setLocal_transaction_id(docT.getLocal_transactionID());
//                        if (docT.getTipoMovimento() != null && docT.getTipoMovimento().getFl_vendita().booleanValue()) {
//                            new_bene_apg.setVariazione_meno(bene.getValoreBene());
//                            new_bene_apg.setValore_alienazione(bene.getValoreBene(true));
//                            new_bene_apg.setFl_totalmente_scaricato(true);
//                        } else {
//                            new_bene_apg.setVariazione_meno(new java.math.BigDecimal(0));
//                            new_bene_apg.setFl_totalmente_scaricato(bene.getFl_totalmente_scaricato());
//                        }
                        if (docT.getTipoMovimento() != null) {
                            new_bene_apg.setVariazione_meno(bene.getVariazione_meno());
                            new_bene_apg.setValore_alienazione(bene.getValore_alienazione());
                            new_bene_apg.setFl_totalmente_scaricato(bene.getFl_totalmente_scaricato());
                        }
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

    public void eliminaBeniAssociatiConBulk(UserContext userContext, OggettoBulk doc, OggettoBulk[] beni) throws ComponentException {
        try {
            for (int i = 0; i < beni.length; i++) {
                Inventario_beniBulk bene = (Inventario_beniBulk) beni[i];
                Inventario_beni_apgHome homebeni = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);
                SQLBuilder sql = homebeni.createSQLBuilder();
                Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) doc;
                sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, docT.getInventario().getPg_inventario());
                sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, docT.getLocal_transactionID());

                sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
                sql.openParenthesis("AND");
                sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());
                sql.addSQLClause("OR", "PROGRESSIVO", SQLBuilder.NOT_EQUALS, "0");
                sql.addSQLClause("AND", "FL_VISIBILE", SQLBuilder.EQUALS, "N");
                sql.closeParenthesis();
                List listabeni = homebeni.fetchAll(sql);
                for (Iterator iteratore = listabeni.iterator(); iteratore.hasNext(); ) {
                    Inventario_beni_apgBulk bene_apg = (Inventario_beni_apgBulk) iteratore.next();
                    bene_apg.setToBeDeleted();
                    super.eliminaConBulk(userContext, bene_apg);
                }
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public void eliminaBeniAssociatiConBulk(UserContext userContext, OggettoBulk docT) throws ComponentException {
        Inventario_beni_apgHome benihome = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);//,"V_INVENTARIO_BENI_APG");
        SQLBuilder sql = benihome.createSQLBuilder();
        Doc_trasporto_rientroBulk ass = (Doc_trasporto_rientroBulk) docT;
        sql.addSQLClause("AND", "LOCAL_TRANSACTION_ID", SQLBuilder.EQUALS, ass.getLocal_transactionID());

        try {
            List beni = benihome.fetchAll(sql);
            for (Iterator i = beni.iterator(); i.hasNext(); ) {
                Inventario_beni_apgBulk bene_apg = (Inventario_beni_apgBulk) i.next();
                bene_apg.setToBeDeleted();
                super.eliminaConBulk(userContext, bene_apg);
            }
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Restituisce, tra tutti i beni trasportati nel DDT la MAX(dt_validita_variazione),
     * ossia, la data corrispondente alla modifica più recente.
     **/
    private java.sql.Timestamp getMaxDataFor(UserContext userContext, Doc_trasporto_rientroBulk docT) throws ComponentException {
        Timestamp max_data = null;
        try {
            Inventario_beni_apgHome home = (Inventario_beni_apgHome) getHome(userContext, Inventario_beni_apgBulk.class);
            max_data = home.getMaxDataFor(docT.getLocal_transactionID());

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
                if (dettaglio.getNrInventario() != null && dettaglio.getProgressivo() != null) {
                    Inventario_beniBulk beneDaCaricare = new Inventario_beniBulk(
                            dettaglio.getNrInventario(),
                            dettaglio.getPgInventario(),
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

}