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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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
public class DocTrasportoRientroComponent extends it.cnr.jada.comp.CRUDDetailComponent
        implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    public DocTrasportoRientroComponent() {
    }

    /**
     * Inizializza le chiavi e opzioni del documento caricando i tipi di movimento disponibili.
     */
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

    /**
     * Inizializza un nuovo documento di trasporto/rientro con valori di default.
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

            doc.setCds_scrivania(it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(aUC));
            doc.setUo_scrivania(it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(aUC));
            doc.setEsercizio(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(aUC));

            doc.setCondizioni(getHome(aUC, Condizione_beneBulk.class).findAll());
            doc.setInventario(caricaInventario(aUC));

            Id_inventarioHome invHome = (Id_inventarioHome) getHome(aUC, Id_inventarioBulk.class);
            doc.setConsegnatario(invHome.findConsegnatarioFor(doc.getInventario()));
            doc.setDelegato(invHome.findDelegatoFor(doc.getInventario()));
            doc.setUo_consegnataria(invHome.findUoRespFor(aUC, doc.getInventario()));

            doc.setLocal_transactionID(getLocalTransactionID(aUC, true));
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
     * Assegna il progressivo al documento tramite il numeratore.
     */
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

    /**
     * Restituisce la Home appropriata per i dettagli in base al tipo di documento.
     */
    private BulkHome getHomeDocumentoTrasportoRientroDett(UserContext aUC, OggettoBulk bulk) throws ComponentException {
        if (bulk instanceof DocumentoTrasportoBulk)
            return getHome(aUC, DocumentoTrasportoDettBulk.class);
        else
            return getHome(aUC, DocumentoRientroDettBulk.class);
    }

    /**
     * Crea un'istanza del dettaglio appropriato in base alla Home fornita.
     */
    private OggettoBulk getDettBulkFromHome(BulkHome home) throws ComponentException {
        if (home instanceof DocumentoTrasportoDettHome)
            return new DocumentoTrasportoDettBulk();
        else
            return new DocumentoRientroDettBulk();
    }

    /**
     * Inizializza il documento per la modifica con controllo lock su documento e beni.
     * Carica manualmente tutti i dati usando la Home PADRE per evitare problemi di ereditarietà.
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

            if (docTR == null) {
                throw new ComponentException("Documento non trovato nel database!");
            }

            try {
                lockBulk(aUC, docTR);
            } catch (BusyResourceException bre) {
                throw new ApplicationException(
                        "Il documento è già in modifica da un altro utente.\n" +
                                "Attendere che l'altro utente completi le modifiche prima di procedere."
                );
            } catch (OutdatedResourceException ore) {
                throw new ApplicationException(
                        "Il documento è stato modificato da un altro utente.\n" +
                                "Ricaricare il documento per visualizzare le modifiche più recenti."
                );
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

            BulkHome dettHome = getHomeDocumentoTrasportoRientroDett(aUC, docTR);
            docTR.setDoc_trasporto_rientro_dettColl(
                    new BulkList(((Doc_trasporto_rientro_dettHome) dettHome).getDetailsFor(docTR))
            );

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


    /**
     * Inizializza il documento per la ricerca caricando inventario e dati correlati.
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

        bulk = super.inizializzaBulkPerRicerca(userContext, bulk);

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        String cds = CNRUserContext.getCd_cds(userContext);
        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);
        Integer esercizio = CNRUserContext.getEsercizio(userContext);

        doc.setCds_scrivania(cds);
        doc.setUo_scrivania(uo);
        doc.setEsercizio(esercizio);

        try {
            if (doc.getPgInventario() != null) {
                Id_inventarioBulk inventario = (Id_inventarioBulk) getHome(userContext, Id_inventarioBulk.class)
                        .findByPrimaryKey(new Id_inventarioBulk(doc.getPgInventario()));
                doc.setInventario(inventario);

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

            if (doc.getCd_tipo_trasporto_rientro() != null) {
                Tipo_trasporto_rientroBulk tipoMovimento =
                        (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                                .findByPrimaryKey(new Tipo_trasporto_rientroBulk(
                                        doc.getCd_tipo_trasporto_rientro()
                                ));
                doc.setTipoMovimento(tipoMovimento);
            }

            if (doc.getCdTerzoIncaricato() != null) {
                TerzoBulk terzoIncaricato =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoIncaricato()));
                doc.setTerzoIncRitiro(terzoIncaricato);
            }

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
     * Inizializza il documento per la ricerca libera.
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

        bulk = super.inizializzaBulkPerRicercaLibera(userContext, bulk);

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        inizializzaTipoMovimento(userContext, doc);

        String cds = CNRUserContext.getCd_cds(userContext);
        String uo = CNRUserContext.getCd_unita_organizzativa(userContext);
        Integer esercizio = CNRUserContext.getEsercizio(userContext);

        doc.setCds_scrivania(cds);
        doc.setUo_scrivania(uo);
        doc.setEsercizio(esercizio);

        try {
            if (doc.getPgInventario() != null) {
                Id_inventarioBulk inventario = (Id_inventarioBulk) getHome(userContext, Id_inventarioBulk.class)
                        .findByPrimaryKey(new Id_inventarioBulk(doc.getPgInventario()));
                doc.setInventario(inventario);

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

            if (doc.getCd_tipo_trasporto_rientro() != null) {
                Tipo_trasporto_rientroBulk tipoMovimento =
                        (Tipo_trasporto_rientroBulk) getHome(userContext, Tipo_trasporto_rientroBulk.class)
                                .findByPrimaryKey(new Tipo_trasporto_rientroBulk(
                                        doc.getCd_tipo_trasporto_rientro()
                                ));
                doc.setTipoMovimento(tipoMovimento);
            }

            if (doc.getCdTerzoIncaricato() != null) {
                TerzoBulk terzoIncaricato =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoIncaricato()));
                doc.setTerzoIncRitiro(terzoIncaricato);
            }

            if (doc.getCdTerzoResponsabile() != null) {
                TerzoBulk terzoResponsabile =
                        (TerzoBulk) getHome(userContext, TerzoBulk.class)
                                .findByPrimaryKey(new TerzoBulk(doc.getCdTerzoResponsabile()));
                doc.setTerzoRespDip(terzoResponsabile);
            }

        } catch (PersistencyException | IntrospectionException e) {
            System.out.println("Errore caricamento relazioni per ricerca libera: " + e.getMessage());
        }

        return doc;
    }

    /**
     * Crea un nuovo documento di trasporto/rientro e salva solo la testata.
     */
    @Override
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {

        try {
            Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

            if (docT.getPgDocTrasportoRientro() != null) {
                Doc_trasporto_rientroBulk existing = (Doc_trasporto_rientroBulk)
                        findByPrimaryKey(userContext, docT);

                if (existing != null) {
                    return modificaConBulk(userContext, docT);
                }
            }

            verificaDocumentoNonEsistente(userContext, docT);
            validaDoc(userContext, docT);

            TerzoBulk terzoResponsabile = recuperaTerzoResponsabileUO(userContext);

            if (terzoResponsabile != null) {
                docT.setTerzoRespDip(terzoResponsabile);
                docT.setCdTerzoResponsabile(terzoResponsabile.getCd_terzo());
            }

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

            if (docT.isSmartworking()) {
                if (docT.getTerzoSmartworking() != null) {
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
            e.printStackTrace(System.err);
            throw handleException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            throw handleException(e);
        }
    }

    /**
     * Verifica che il documento non sia già presente nel database.
     */
    private void verificaDocumentoNonEsistente(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException, PersistencyException {

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
     * Trova il dettaglio più recente di un documento originale (TRASPORTO o RIENTRO).
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
                    SQLBuilder.EQUALS, Doc_trasporto_rientroBulk.STATO_DEFINITIVO);

            sql.addSQLClause("AND", "d.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

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
     * Modifica un documento confermando progressivo temporaneo o aggiornando dettagli esistenti.
     */
    @Override
    public OggettoBulk modificaConBulk(UserContext aUC, OggettoBulk bulk)
            throws ComponentException {

        Doc_trasporto_rientroBulk docT = (Doc_trasporto_rientroBulk) bulk;

        try {
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(aUC, docT);
            docT.setDoc_trasporto_rientro_dettColl(new BulkList(dettHome.getDetailsFor(docT)));

            // ==================== 1. Carica dettagli dal DB ====================
            caricaDettagliDocumento(aUC, docT);

            // ==================== 2. Validazione ====================
            validaDoc(aUC, docT);

            // ==================== 3. CASO: PROGRESSIVO TEMPORANEO ====================
            if (docT.getPgDocTrasportoRientro() != null && docT.getPgDocTrasportoRientro() < 0) {

                Numeratore_doc_t_rHome numHome = (Numeratore_doc_t_rHome) getHome(aUC, Numeratore_doc_t_rBulk.class);
                Long nuovoPgDoc = numHome.getNextPg(
                        aUC,
                        docT.getEsercizio(),
                        docT.getPgInventario(),
                        docT.getTiDocumento(),
                        aUC.getUser()
                );

                Doc_trasporto_rientroHome home = (Doc_trasporto_rientroHome) getHome(aUC, docT);
                home.confirmDocTrasportoRientroTemporaneo(aUC, docT, nuovoPgDoc);

                docT.setPgDocTrasportoRientro(nuovoPgDoc);

                docT.setArchivioAllegati(new BulkList<>());

                docT.setToBeUpdated();
                docT = (Doc_trasporto_rientroBulk) inizializzaBulkPerModifica(aUC, docT);
                preparaEAggiornaRiferimentiInversi(aUC, docT);

            } else {
                // ==================== 7. CASO: PROGRESSIVO POSITIVO ====================
                if (docT.getDoc_trasporto_rientro_dettColl() != null) {
                    for (Iterator i = docT.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext(); ) {
                        Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) i.next();
                        updateBulk(aUC, dettaglio);
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
                return super.modificaConBulk(aUC, docT);
            }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        } catch (ApplicationException e) {
            throw e;
        } catch (Throwable e) {
            throw new ComponentException(e);
        }
        return docT;
    }


    /**
     * Prepara e aggiorna la relazione bidirezionale tra TRASPORTO e RIENTRO.
     * Per ogni dettaglio di rientro che referenzia un dettaglio trasporto,
     * aggiorna il dettaglio trasporto con il riferimento inverso al rientro.
     */
    private void preparaEAggiornaRiferimentiInversi(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {


        if (!(doc instanceof DocumentoRientroBulk)) {
            return;
        }
        if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            return;
        }

        try {
            DocumentoTrasportoDettHome trasportoDettHome =
                    (DocumentoTrasportoDettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

            for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {

                if (!(obj instanceof DocumentoRientroDettBulk)) {
                    continue;
                }

                DocumentoRientroDettBulk dettaglioRientro = (DocumentoRientroDettBulk) obj;
                DocumentoTrasportoDettBulk dettaglioTrasportoRef = dettaglioRientro.getDocTrasportoDettRif();

                if (dettaglioTrasportoRef == null) {
                    continue;
                }

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


    /**
     * Valida il documento controllando data, tipo movimento e descrizione.
     */
    private void validaDoc(UserContext aUC, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

        try {

            String tipoDoc = "Documento di";
            if (RIENTRO.equals(docT.getTiDocumento())) {
                tipoDoc = "Rientro";
            } else if (TRASPORTO.equals(docT.getTiDocumento())) {
                tipoDoc = "Trasporto";
            }

            if (!docT.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) docT.getDoc_trasporto_rientro_dettColl().get(0);
                if (dett.getCrudStatus() == OggettoBulk.NORMAL) {
                    if (docT.getDataRegistrazione().before(getMaxDataFor(aUC, docT))) {
                        throw new it.cnr.jada.comp.ApplicationException(
                                "Attenzione: data del " + tipoDoc + " non valida.\n" +
                                        "La Data del " + tipoDoc + " non può essere precedente ad una modifica di uno dei beni movimentati."
                        );
                    }
                }
            } else {
                if (docT.getCrudStatus() == OggettoBulk.TO_BE_UPDATED)
                    throw new ApplicationException("Attenzione: è necessario specificare almeno un bene!");
            }

            if (docT.getTipoMovimento() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: specificare un Tipo di Movimento per il Documento");

            if (docT.getDsDocTrasportoRientro() == null)
                throw new it.cnr.jada.comp.ApplicationException("Attenzione: indicare una Descrizione per il Documento");

        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    /**
     * Cambia lo stato del documento da INSERITO a INVIATO predisponendolo alla firma.
     */
    public Doc_trasporto_rientroBulk changeStatoInInviato(UserContext userContext, Doc_trasporto_rientroBulk docTR)
            throws ComponentException {
        try {
            if (!Doc_trasporto_rientroBulk.STATO_INSERITO.equals(docTR.getStato())) {
                throw new ApplicationException("Stato deve essere INSERITO per inviare alla firma.");
            }

            docTR.setStato(Doc_trasporto_rientroBulk.STATO_INVIATO);
            docTR.setStatoFlusso("INV");

            if (docTR.getIdFlussoHappysign() == null || docTR.getIdFlussoHappysign().isEmpty()) {
                throw new ApplicationException("ID flusso HappySign non impostato");
            }

            if (docTR.getDataInvioFirma() == null) {
                docTR.setDataInvioFirma(new Timestamp(System.currentTimeMillis()));
            }

            docTR.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException("Errore predisposizione firma: " + e.getMessage(), e);
        }
    }

    /**
     * Inizializza i tipi di movimento disponibili per il documento.
     */
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

    /**
     * Genera un ID di transazione locale per il documento.
     */
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

    /**
     * Ritorna un iteratore sui dettagli del documento per la modifica.
     */
    public RemoteIterator selectEditDettagliTrasporto(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR,
            Class bulkClass,
            CompoundFindClause clauses) throws ComponentException {

        if (docTR == null || docTR.getPgDocTrasportoRientro() == null)
            return new it.cnr.jada.util.EmptyRemoteIterator();

        SQLBuilder sql = getHome(userContext, (docTR.isRientro() ? DocumentoRientroDettBulk.class : DocumentoTrasportoDettBulk.class)).createSQLBuilder();

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

    /**
     * Ritorna la lista dei beni disponibili per il trasporto.
     */
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

    /**
     * Ritorna la lista dei beni già selezionati nel documento.
     */
    public SimpleBulkList selezionati(it.cnr.jada.UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws it.cnr.jada.comp.ComponentException {

        if (docT.getPgDocTrasportoRientro() == null) {
            return new SimpleBulkList();
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
     * Ritorna iteratore sui beni associati al documento.
     */
    public RemoteIterator selectBeniAssociatiByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk docT,
            Class bulkClass) throws ComponentException {

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
     * Restituisce la data di modifica più recente tra tutti i beni del documento.
     */
    private java.sql.Timestamp getMaxDataFor(UserContext userContext, Doc_trasporto_rientroBulk docT)
            throws ComponentException {

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
     * Cerca gli accessori associati a un bene principale.
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

            sql.addSQLClause("AND", "PG_INVENTARIO",
                    SQLBuilder.EQUALS, benePrincipale.getPg_inventario());
            sql.addSQLClause("AND", "NR_INVENTARIO",
                    SQLBuilder.EQUALS, benePrincipale.getNr_inventario());

            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

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
     * Gestisce l'aggiunta/rimozione di beni con relativi accessori applicando lock sui beni.
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
                Inventario_beniBulk bene = (Inventario_beniBulk) beni[i];

                if (old_ass.get(i) != ass.get(i)) {

                    if (ass.get(i)) {

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
                            DocumentoTrasportoDettBulk dett = new DocumentoTrasportoDettBulk();
                            dett.setDocumentoTrasporto((DocumentoTrasportoBulk) docT);
                            dett.setBene(bene);

                            dett.setPg_inventario(docT.getPgInventario());
                            dett.setTi_documento(docT.getTiDocumento());
                            dett.setEsercizio(docT.getEsercizio());
                            dett.setPg_doc_trasporto_rientro(docT.getPgDocTrasportoRientro());
//                            dett.setNr_inventario(bene.getNr_inventario());
//                            dett.setProgressivo(bene.getProgressivo().intValue());

                            if (docT.isSmartworking() && docT.getTerzoSmartworking() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoSmartworking().getCd_terzo());
                            } else if (docT.getFlIncaricato() && docT.getTerzoIncRitiro() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoIncRitiro().getCd_terzo());
                            } else if (bene.getCd_assegnatario() != null) {
                                dett.setCdTerzoAssegnatario(bene.getCd_assegnatario());
                            }

                            Doc_trasporto_rientro_dettBulk dettaglioRientroOriginale =
                                    trovaDettaglioOriginale(userContext, bene,
                                            docT.getPgInventario(), RIENTRO);

                            if (dettaglioRientroOriginale != null) {
                                dett.setDocRientroDettRif((DocumentoRientroDettBulk) dettaglioRientroOriginale);
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
//                            dett.setNr_inventario(bene.getNr_inventario());
//                            dett.setProgressivo(bene.getProgressivo().intValue());

                            if (docT.isSmartworking() && docT.getTerzoSmartworking() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoSmartworking().getCd_terzo());
                            } else if (docT.getFlIncaricato() && docT.getTerzoIncRitiro() != null) {
                                dett.setCdTerzoAssegnatario(docT.getTerzoIncRitiro().getCd_terzo());
                            } else if (bene.getCd_assegnatario() != null) {
                                dett.setCdTerzoAssegnatario(bene.getCd_assegnatario());
                            }

                            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                                    trovaDettaglioOriginale(userContext, bene,
                                            docT.getPgInventario(), TRASPORTO);

                            if (dettaglioTrasportoOriginale == null) {
                                throw new ApplicationException(
                                        "Bene " + bene.getNumeroBeneCompleto() +
                                                " non presente in nessun documento di trasporto firmato (stato DEFINITIVO)!");
                            }

                            dett.setDocTrasportoDettRif((DocumentoTrasportoDettBulk) dettaglioTrasportoOriginale);
                            docT.getDoc_trasporto_rientro_dettColl().add(dett);

                            dett.setToBeCreated();
                            super.creaConBulk(userContext, dett);
                        }

                    } else {
                        SimpleBulkList collection = docT.getDoc_trasporto_rientro_dettColl();

                        if (collection != null) {
                            Doc_trasporto_rientro_dettBulk dettaglioDaEliminare = null;

                            for (Iterator it = collection.iterator(); it.hasNext(); ) {
                                Doc_trasporto_rientro_dettBulk dett =
                                        (Doc_trasporto_rientro_dettBulk) it.next();

                                if (dett.getNr_inventario().equals(bene.getNr_inventario()) &&
                                        dett.getProgressivo().equals(bene.getProgressivo().intValue())) {

                                    dettaglioDaEliminare = dett;
                                    it.remove();
                                    break;
                                }
                            }

                            if (dettaglioDaEliminare != null &&
                                    docT.getPgDocTrasportoRientro() != null) {
                                dettaglioDaEliminare.setToBeDeleted();
                                super.eliminaConBulk(userContext, dettaglioDaEliminare);
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
     * Wrapper per ottenere la lista dei beni da far rientrare.
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
     * Wrapper per modificare beni rientrati con accessori.
     */
    public void modificaBeniRientratiConAccessori(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException {

        modificaBeniTrasportatiConAccessori(userContext, doc, beni, oldSelection, newSelection);
    }

    /**
     * Wrapper per selezionare dettagli rientro per modifica.
     */
    public RemoteIterator selectEditDettagliRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            Class bulkClass,
            CompoundFindClause filters)
            throws ComponentException {

        return selectEditDettagliTrasporto(userContext, doc, bulkClass, filters);
    }

    /**
     * Verifica se l'esercizio COEP è chiuso.
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
     * Recupera i documenti in stato INVIATO con filtri aggiuntivi.
     */
    public List getDocumentiPredispostiAllaFirma(UserContext userContext)
            throws ComponentException {

        try {
            Doc_trasporto_rientroHome home = (Doc_trasporto_rientroHome)
                    getHome(userContext, Doc_trasporto_rientroBulk.class);

            SQLBuilder sql = home.createSQLBuilder();

            sql.addSQLClause("AND", "STATO", SQLBuilder.EQUALS,
                    Doc_trasporto_rientroBulk.STATO_INVIATO);

            sql.addSQLClause("AND", "STATO_FLUSSO", SQLBuilder.EQUALS, "INV");

            sql.addSQLClause("AND", "ID_FLUSSO_HAPPYSIGN", SQLBuilder.ISNOTNULL, null);

            Integer esercizioCorrente = CNRUserContext.getEsercizio(userContext);
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.LESS_EQUALS, esercizioCorrente);

            sql.addOrderBy("DATA_INVIO_FIRMA ASC");

            List documenti = home.fetchAll(sql);

            return documenti != null ? documenti : java.util.Collections.emptyList();

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Valida il documento per la stampa.
     */
    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(it.cnr.jada.UserContext aUC, it.cnr.jada.bulk.OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {
        validateBulkForPrint(aUC, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }

    /**
     * Inizializza il documento per la stampa.
     */
    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(it.cnr.jada.UserContext userContext, it.cnr.jada.bulk.OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {

        if (bulk instanceof Stampa_doc_trasporto_rientroBulk)
            inizializzaBulkPerStampa(userContext, (Stampa_doc_trasporto_rientroBulk) bulk);
        return bulk;
    }

    /**
     * Prepara i parametri per la stampa del documento.
     */
    public void inizializzaBulkPerStampa(UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa) throws it.cnr.jada.comp.ComponentException {

        Stampa_doc_trasporto_rientroBulk stampaDoc = (Stampa_doc_trasporto_rientroBulk) stampa;
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
     * Valida i parametri di stampa del documento.
     */
    private void validateBulkForPrint(it.cnr.jada.UserContext userContext, Stampa_doc_trasporto_rientroBulk stampa) throws ComponentException {

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

    /**
     * Rende definitivo il documento verificando allegato firmato e aggiornando stato beni.
     */
    public Doc_trasporto_rientroBulk salvaDefinitivo(
            UserContext userContext,
            Doc_trasporto_rientroBulk docTR)
            throws ComponentException {

        try {
            validaDoc(userContext, docTR);

            if (!hasAllegatoFirmato(docTR)) {
                throw new ApplicationException(
                        "È obbligatorio allegare il documento firmato prima di rendere definitivo il documento."
                );
            }

            caricaDettagliDocumento(userContext, docTR);

            boolean beneInIstituto = docTR instanceof DocumentoRientroBulk;
            aggiornaStatoBeni(userContext, docTR, beneInIstituto);

            docTR.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            docTR.setToBeUpdated();

            return (Doc_trasporto_rientroBulk) super.modificaConBulk(userContext, docTR);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Annulla il documento rimuovendo riferimenti e aggiornando stato beni.
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
     * Verifica che il TRASPORTO non abbia rientri collegati.
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
     * Rimuove i riferimenti dal TRASPORTO quando si annulla un RIENTRO.
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

                docTrasportoDettRif.setDoc_trasporto_rientroDettRif(null);
                docTrasportoDettRif.setToBeUpdated();
            }
        }
    }

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
                    doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                return;
            }

            Inventario_beniHome beneHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
                Doc_trasporto_rientro_dettBulk dettaglio =
                        (Doc_trasporto_rientro_dettBulk) obj;

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
     * Verifica presenza di almeno un allegato firmato valido.
     */
    private boolean hasAllegatoFirmato(Doc_trasporto_rientroBulk doc) {

        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        String aspectFirmatoAtteso;
        if (doc instanceof DocumentoTrasportoBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else if (doc instanceof DocumentoRientroBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else {
            return false;
        }

        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) obj;
                String aspectName = allegato.getAspectName();

                if (aspectName != null
                        && aspectName.equals(aspectFirmatoAtteso)
                        && allegato.getCrudStatus() != OggettoBulk.TO_BE_DELETED) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Ricerca anagrafico INCARICATO delegando al componente Anagrafico.
     */
    public SQLBuilder selectAnagIncRitiroByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            AnagraficoBulk anag_find,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {

        try {
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
     * Ricerca anagrafico SMARTWORKING delegando al componente Anagrafico.
     */
    public SQLBuilder selectAnagSmartworkingByClause(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            AnagraficoBulk anag_find,
            CompoundFindClause clause)
            throws ComponentException, RemoteException {

        try {
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
     * Cerca accessori presenti nel TRASPORTO originale per un RIENTRO.
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

            Doc_trasporto_rientro_dettBulk dettaglioTrasportoOriginale =
                    trovaDettaglioOriginale(userContext, beneRientro, doc.getPgInventario(), TRASPORTO);

            if (dettaglioTrasportoOriginale == null) {
                return java.util.Collections.emptyList();
            }

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

            if (dettagliAccessori == null || dettagliAccessori.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            Inventario_beniHome beneHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            List<Inventario_beniBulk> accessoriDisponibili = new ArrayList<>();

            for (Object obj : dettagliAccessori) {
                Doc_trasporto_rientro_dettBulk dettaglioAccessorio =
                        (Doc_trasporto_rientro_dettBulk) obj;

                boolean giaRientrato = verificaSeAccessorioGiaRientrato(
                        userContext,
                        dettaglioAccessorio
                );

                if (!giaRientrato) {
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
     * Verifica se un accessorio è già rientrato.
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
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            List risultati = dettHome.fetchAll(sql);

            return risultati != null && !risultati.isEmpty();

        } catch (PersistencyException pe) {
            throw handleException(pe);
        }
    }

    /**
     * Cerca beni trasportabili (wrapper).
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
     * Cerca beni da far rientrare (wrapper).
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

            SQLBuilder sql = invHome.findBeniUnificato(userContext, doc, clauses);

            if (isTrasporto) {
                applicaFiltriTrasporto(sql, doc, userContext);
            } else {
                applicaFiltriRientro(sql, doc);
            }

            if (beniEsclusi != null && !beniEsclusi.isEmpty()) {
                escludiBeniGiaSelezionati(sql, doc, beniEsclusi);
            }

            sql.addOrderBy("INVENTARIO_BENI.NR_INVENTARIO, INVENTARIO_BENI.PROGRESSIVO");

            return iterator(userContext, sql, Inventario_beniBulk.class, null);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Applica filtri specifici per documenti di TRASPORTO.
     */
    private void applicaFiltriTrasporto(SQLBuilder sql, Doc_trasporto_rientroBulk doc, UserContext userContext)
            throws ComponentException, PersistencyException, IntrospectionException {

        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "Y");

        TerzoHome terzoHome = (TerzoHome) getHome(userContext, TerzoBulk.class);
        TerzoBulk terzo = null;

        if (doc.getAnagSmartworking() != null && doc.getAnagSmartworking().getCd_anag() != null) {
            terzo = terzoHome.findTerzoByAnag(doc.getAnagSmartworking().getCd_anag());
        }

        if (doc.isSmartworking() && terzo != null && terzo.getCd_terzo() != null) {
            sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.CD_ASSEGNATARIO",
                    SQLBuilder.EQUALS, terzo.getCd_terzo());
        }

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
     * Applica filtri specifici per documenti di RIENTRO.
     * Mostra SOLO beni che:
     * 1. Sono in TRASPORTI DEFINITIVI
     * 2. NON sono già in RIENTRI attivi (INSERITO/INVIATO/DEFINITIVO)
     * 3. OPPURE sono in RIENTRI ANNULLATI del documento corrente
     */
    private void applicaFiltriRientro(SQLBuilder sql, Doc_trasporto_rientroBulk doc) throws IntrospectionException {

        // Solo beni fuori dall'istituto
        sql.addSQLClause(FindClause.AND, "INVENTARIO_BENI.FL_BENE_IN_IST", SQLBuilder.EQUALS, "N");

        // Deve avere un documento associato
        sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.ISNOTNULL, null);

        sql.openParenthesis(FindClause.AND);

        // Beni in TRASPORTI DEFINITIVI
        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, "T");
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS,
                Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
        sql.closeParenthesis();

        // Beni in RIENTRI ANNULLATI
        sql.openParenthesis(FindClause.OR);
        sql.addSQLClause(FindClause.AND, "t.TI_DOCUMENTO", SQLBuilder.EQUALS, "R");
        sql.addSQLClause(FindClause.AND, "t.STATO", SQLBuilder.EQUALS,
                Doc_trasporto_rientroBulk.STATO_ANNULLATO);
        sql.closeParenthesis();

        sql.closeParenthesis();

        // Costruisci la subquery NOT EXISTS manualmente
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
        notExistsClause.append("AND doc_r.TI_DOCUMENTO = 'R' ");
        notExistsClause.append("AND doc_r.STATO <> '").append(Doc_trasporto_rientroBulk.STATO_ANNULLATO).append("' ");

        // Escludi il documento corrente dal controllo NOT EXISTS
        if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
            notExistsClause.append("AND (doc_r.PG_DOC_TRASPORTO_RIENTRO <> ")
                    .append(doc.getPgDocTrasportoRientro())
                    .append(" OR doc_r.ESERCIZIO <> ")
                    .append(doc.getEsercizio())
                    .append(") ");
        }

        notExistsClause.append(")");

        // Aggiungi la clausola NOT EXISTS come stringa SQL grezza
        sql.addSQLClause("AND", notExistsClause.toString());
    }

    /**
     * Esclude i beni già selezionati dalla ricerca.
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
     * Elimina tutti i dettagli salvati di un documento, gestendo i riferimenti incrociati.
     */
    public void eliminaTuttiDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws ComponentException {

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            SQLBuilder sql = dettHome.createSQLBuilder();
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, doc.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, doc.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS, doc.getPgDocTrasportoRientro());

            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare = dettHome.fetchAll(sql);

            if (dettagliDaEliminare == null || dettagliDaEliminare.isEmpty()) {
                return;
            }

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
     * Elimina dettagli specifici salvati di un documento, gestendo i riferimenti incrociati.
     */
    public void eliminaDettagliSalvati(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni)
            throws ComponentException {

        if (beni == null || beni.length == 0) return;

        try {
            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare = new ArrayList<>();

            for (OggettoBulk o : beni) {
                Inventario_beniBulk bene = (Inventario_beniBulk) o;

                SQLBuilder sql = dettHome.createSQLBuilder();
                sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, doc.getPgInventario());
                sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS, doc.getTiDocumento());
                sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, doc.getEsercizio());
                sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS, doc.getPgDocTrasportoRientro());
                sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS, bene.getNr_inventario());
                sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.EQUALS, bene.getProgressivo());

                List<Doc_trasporto_rientro_dettBulk> trovati = dettHome.fetchAll(sql);
                if (trovati != null && !trovati.isEmpty()) {
                    dettagliDaEliminare.addAll(trovati);
                }
            }

            if (dettagliDaEliminare.isEmpty()) {
                return;
            }

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
     * Rimuove i riferimenti bidirezionali TRASPORTO ↔ RIENTRO prima di eliminare i RIENTRI.
     */
    private void rimuoviRiferimentiTrasportoARientro(
            UserContext userContext,
            List<Doc_trasporto_rientro_dettBulk> dettagliDaEliminare)
            throws ComponentException {

        try {
            for (Doc_trasporto_rientro_dettBulk dettaglio : dettagliDaEliminare) {
                if (!(dettaglio instanceof DocumentoRientroDettBulk)) {
                    continue;
                }

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

    /**
     * Trova il TRASPORTO che punta a un RIENTRO.
     * Cerca TRASPORTI che hanno nei loro campi _RIF la chiave del RIENTRO.
     */
    private DocumentoTrasportoDettBulk trovaTrasportoChePuntaARientro(
            UserContext userContext,
            DocumentoRientroDettBulk rientro)
            throws ComponentException, PersistencyException {

        DocumentoTrasportoDettHome trasportoHome =
                (DocumentoTrasportoDettHome) getHome(userContext, DocumentoTrasportoDettBulk.class);

        SQLBuilder sql = trasportoHome.createSQLBuilder();
        sql.addSQLClause("AND", "PG_INVENTARIO_RIF", SQLBuilder.EQUALS, rientro.getPg_inventario());
        sql.addSQLClause("AND", "TI_DOCUMENTO_RIF", SQLBuilder.EQUALS, rientro.getTi_documento());
        sql.addSQLClause("AND", "ESERCIZIO_RIF", SQLBuilder.EQUALS, rientro.getEsercizio());
        sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO_RIF", SQLBuilder.EQUALS, rientro.getPg_doc_trasporto_rientro());
        sql.addSQLClause("AND", "NR_INVENTARIO_RIF", SQLBuilder.EQUALS, rientro.getNr_inventario());
        sql.addSQLClause("AND", "PROGRESSIVO_RIF", SQLBuilder.EQUALS, rientro.getProgressivo());

        List<DocumentoTrasportoDettBulk> risultati = trasportoHome.fetchAll(sql);
        return (risultati != null && !risultati.isEmpty()) ? risultati.get(0) : null;
    }

    /**
     * Cerca accessori di un bene nei dettagli già salvati.
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

            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS,
                    doc.getPgInventario());
            sql.addSQLClause("AND", "TI_DOCUMENTO", SQLBuilder.EQUALS,
                    doc.getTiDocumento());
            sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS,
                    doc.getEsercizio());
            sql.addSQLClause("AND", "PG_DOC_TRASPORTO_RIENTRO", SQLBuilder.EQUALS,
                    doc.getPgDocTrasportoRientro());

            sql.addSQLClause("AND", "NR_INVENTARIO", SQLBuilder.EQUALS,
                    benePrincipale.getNr_inventario());

            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.GREATER, 0);

            sql.addSQLClause("AND", "PROGRESSIVO", SQLBuilder.NOT_EQUALS,
                    benePrincipale.getProgressivo());

            List dettagli = dettHome.fetchAll(sql);

            if (dettagli != null && !dettagli.isEmpty()) {
                List<Inventario_beniBulk> beniAccessori = new ArrayList();
                Inventario_beniHome beneHome = (Inventario_beniHome)
                        getHome(userContext, Inventario_beniBulk.class);

                for (Iterator it = dettagli.iterator(); it.hasNext(); ) {
                    Doc_trasporto_rientro_dettBulk dett =
                            (Doc_trasporto_rientro_dettBulk) it.next();

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
     * Elimina bene principale con accessori dai dettagli salvati.
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
            StringBuilder inClause = new StringBuilder();

            inClause.append("(")
                    .append(benePrincipale.getNr_inventario())
                    .append(",")
                    .append(benePrincipale.getProgressivo())
                    .append(")");

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

    /**
     * Seleziona e inserisce tutti i beni senza limite.
     */
    public void selezionaTuttiBeni(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            CompoundFindClause clauses)
            throws ComponentException {

        try {
            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

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

            System.out.println("Caricati " + beniFiltrati.size() +
                    " beni, inizio creazione dettagli...");

            inserisciBeniComeDettagli(userContext, doc, beniFiltrati);

            System.out.println("Creazione completata: inseriti " +
                    beniFiltrati.size() + " dettagli");

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Carica i beni da inserire applicando i filtri appropriati.
     */
    public List<Inventario_beniBulk> caricaBeniPerInserimento(
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
     * Inserisce una lista di beni come dettagli del documento.
     */
    private void inserisciBeniComeDettagli(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            List<Inventario_beniBulk> beniFiltrati)
            throws ComponentException {

        Integer cdTerzoAssegnatario = null;
        if (doc.isSmartworking() && doc.getTerzoSmartworking() != null) {
            cdTerzoAssegnatario = doc.getTerzoSmartworking().getCd_terzo();
        } else if (doc.getFlIncaricato() && doc.getTerzoIncRitiro() != null) {
            cdTerzoAssegnatario = doc.getTerzoIncRitiro().getCd_terzo();
        }

        boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

        int totalInserted = 0;

        for (Inventario_beniBulk bene : beniFiltrati) {

            Doc_trasporto_rientro_dettBulk dett;
            if (isTrasporto) {
                dett = new DocumentoTrasportoDettBulk();
            } else {
                dett = new DocumentoRientroDettBulk();
            }

            dett.setDoc_trasporto_rientro(doc);
//            dett.setNr_inventario(bene.getNr_inventario());
//            dett.setProgressivo(bene.getProgressivo().intValue());
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
                System.out.println("Inseriti " + totalInserted + " / " +
                        beniFiltrati.size() + " beni...");
            }
        }
    }

    /**
     * Verifica che i beni non siano già in altri documenti attivi.
     */
    public void validaBeniNonInAltriDocumenti(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            List<Inventario_beniBulk> beniDaValidare)
            throws ComponentException {

        if (beniDaValidare == null || beniDaValidare.isEmpty()) {
            return;
        }

        try {
            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;
            String tipoDocDaVerificare = isTrasporto ? TRASPORTO : RIENTRO;

            StringBuilder inClauseBeni = new StringBuilder();
            for (int i = 0; i < beniDaValidare.size(); i++) {
                Inventario_beniBulk bene = beniDaValidare.get(i);
                if (i > 0) inClauseBeni.append(",");
                inClauseBeni.append("(")
                        .append(bene.getNr_inventario())
                        .append(",")
                        .append(bene.getProgressivo())
                        .append(")");
            }

            Doc_trasporto_rientro_dettHome dettHome =
                    (Doc_trasporto_rientro_dettHome) getHomeDocumentoTrasportoRientroDett(userContext, doc);

            SQLBuilder sql = dettHome.createSQLBuilder();

            sql.addTableToHeader("DOC_TRASPORTO_RIENTRO t");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO", "t.PG_INVENTARIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.TI_DOCUMENTO", "t.TI_DOCUMENTO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.ESERCIZIO", "t.ESERCIZIO");
            sql.addSQLJoin("DOC_TRASPORTO_RIENTRO_DETT.PG_DOC_TRASPORTO_RIENTRO",
                    "t.PG_DOC_TRASPORTO_RIENTRO");

            sql.addSQLClause("AND", "DOC_TRASPORTO_RIENTRO_DETT.PG_INVENTARIO",
                    SQLBuilder.EQUALS, doc.getPgInventario());
            sql.addSQLClause("AND", "t.TI_DOCUMENTO",
                    SQLBuilder.EQUALS, tipoDocDaVerificare);

            sql.addSQLClause("AND", "t.STATO",
                    SQLBuilder.NOT_EQUALS, Doc_trasporto_rientroBulk.STATO_ANNULLATO);

            if (doc.getPgDocTrasportoRientro() != null && doc.getPgDocTrasportoRientro() > 0) {
                sql.openParenthesis(FindClause.AND);
                sql.addSQLClause(FindClause.AND, "t.PG_DOC_TRASPORTO_RIENTRO",
                        SQLBuilder.NOT_EQUALS, doc.getPgDocTrasportoRientro());
                sql.addSQLClause(FindClause.OR, "t.ESERCIZIO",
                        SQLBuilder.NOT_EQUALS, doc.getEsercizio());
                sql.closeParenthesis();
            }

            sql.addSQLClause("AND",
                    "(DOC_TRASPORTO_RIENTRO_DETT.NR_INVENTARIO, DOC_TRASPORTO_RIENTRO_DETT.PROGRESSIVO) IN ("
                            + inClauseBeni + ")");

            List beniConflitto = dettHome.fetchAll(sql);

            if (beniConflitto != null && !beniConflitto.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Impossibile procedere: i seguenti beni sono già presenti in altri documenti di ")
                        .append(isTrasporto ? "TRASPORTO" : "RIENTRO")
                        .append(" attivi");
                throw new ApplicationException(msg.toString());
            }

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    /**
     * Carica il TerzoBulk completo partendo da un codice anagrafico.
     */
    public TerzoBulk caricaTerzoDaAnagrafico(UserContext userContext, Integer cdAnag)
            throws ComponentException {

        if (cdAnag == null) {
            return null;
        }

        try {
            it.cnr.contab.anagraf00.core.bulk.TerzoHome terzoHome =
                    (it.cnr.contab.anagraf00.core.bulk.TerzoHome)
                            getHome(userContext, TerzoBulk.class);

            return terzoHome.findTerzoByAnag(cdAnag);

        } catch (it.cnr.jada.persistency.PersistencyException e) {
            throw handleException(e);
        } catch (it.cnr.jada.persistency.IntrospectionException e) {
            throw handleException(e);
        }
    }

    /**
     * Recupera tutti i dettagli di un documento.
     */
    public BulkList getDetailsFor(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
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
    public TerzoBulk recuperaTerzoResponsabileUO(UserContext userContext)
            throws ComponentException {
        try {
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);
            if (cdUo == null) {
                return null;
            }

            Unita_organizzativaHome uoHome =
                    (Unita_organizzativaHome) getHome(userContext, Unita_organizzativaBulk.class);

            Unita_organizzativaBulk uo =
                    (Unita_organizzativaBulk) uoHome.findByPrimaryKey(
                            new Unita_organizzativaBulk(cdUo));

            Integer cdTerzoResp = (uo != null) ? uo.getCd_responsabile() : null;
            if (cdTerzoResp == null) {
                return null;
            }

            TerzoHome terzoHome =
                    (TerzoHome) getHome(userContext, TerzoBulk.class);

            return (TerzoBulk) terzoHome.findByPrimaryKey(
                    new TerzoBulk(cdTerzoResp));

        } catch (PersistencyException e) {
            throw new ComponentException(
                    "Errore durante il recupero del responsabile UO", e);
        }
    }

}