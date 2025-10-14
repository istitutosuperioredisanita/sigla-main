package it.cnr.contab.inventario01.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.AnagraficoHome;
import it.cnr.contab.docamm00.tabrif.bulk.*;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniHome;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;

/**
 * Component per la gestione dei documenti di Trasporto e Rientro.
 *
 *LOGICA FILTRO UO 
 *
 * CASO 1: FL_INCARICATO = N e FL_VETTORE = N
 *   → Usa UO dell'UTENTE LOGGATO
 *   → Campo dipendente NON visibile/disabilitato
 *   → Campo noteRitiro disabilitato
 *
 * CASO 2: FL_INCARICATO = Y o FL_VETTORE = Y
 *   → Campo dipendente OBBLIGATORIO
 *   → Usa UO del DIPENDENTE selezionato
 *   → Campo noteRitiro abilitato
 */
public class DocTrasportoRientroComponent extends it.cnr.jada.comp.CRUDDetailComponent
        implements Cloneable, Serializable {

    // ========================================
    // METODI DI INIZIALIZZAZIONE
    // ========================================

    /**
     * Inizializza il documento per l'inserimento
     */
    public OggettoBulk inizializzaBulkPerInserimento(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

            // Imposta l'inventario
            Id_inventarioHome invHome = (Id_inventarioHome) getHome(userContext, Id_inventarioBulk.class);
            Id_inventarioBulk inventario = invHome.findInventarioFor(userContext, false);
            doc.setInventario(inventario);

            // Imposta l'esercizio corrente
            doc.setEsercizio(it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));

            // Imposta la data di registrazione
            doc.setDataRegistrazione(new Timestamp(System.currentTimeMillis()));

            // Inizializza la collection dei dettagli
            doc.setDoc_trasporto_rientro_dettColl(new it.cnr.jada.bulk.SimpleBulkList());

            // Imposta flag di default (nessun ritiro delegato)
            doc.setFlIncaricato(Boolean.FALSE);
            doc.setFlVettore(Boolean.FALSE);

            // Il dipendente è null di default (si usa l'utente loggato)
            doc.setAnagDipRitiro(null);

            return doc;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ========================================
    //METODI CHIAVE PER DETERMINARE UO 
    // ========================================

    /**
     *METODO PRINCIPALE 
     * Determina quale UO usare per filtrare i beni.
     *
     * LOGICA:
     * 1. Se ritiro delegato (flIncaricato=Y o flVettore=Y)
     *    a. Verifica che dipendente sia selezionato
     *    b. Restituisce UO del dipendente
     * 2. Altrimenti
     *    → Restituisce UO dell'utente loggato
     *
     * @param userContext contesto utente
     * @param doc documento
     * @return codice UO da usare per filtro
     * @throws ComponentException se dipendente non selezionato quando obbligatorio
     */
    private String determinaUOPerFiltro(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            // CASO 1: Ritiro delegato (incaricato o vettore)
            if (doc.isRitiroDelegato()) {
                // Verifica che il dipendente sia selezionato
                if (doc.getAnagDipRitiro() == null) {
                    throw new ApplicationException(
                            "Quando si seleziona 'Ritiro Incaricato' o 'Ritiro Vettore' " +
                                    "è obbligatorio selezionare un dipendente.\n" +
                                    "Il dipendente determina l'unità organizzativa da cui prelevare i beni.");
                }

                // Restituisce l'UO del dipendente
                String cdUO = doc.getAnagDipRitiro().getCd_unita_organizzativa();

                if (cdUO == null || cdUO.trim().isEmpty()) {
                    throw new ApplicationException(
                            "Il dipendente selezionato (" + doc.getAnagDipRitiro().getCd_anag() + ") " +
                                    "non ha un'unità organizzativa associata.\n" +
                                    "Selezionare un dipendente con UO valida.");
                }

                return cdUO;
            }

            // CASO 2: Ritiro normale (usa UO utente loggato)
            String cdUO = recuperaUOUtenteLoggato(userContext);

            if (cdUO == null) {
                throw new ApplicationException(
                        "Impossibile recuperare l'unità organizzativa dell'utente loggato.\n" +
                                "Verificare che l'utente sia associato ad un'unità organizzativa valida.");
            }

            return cdUO;

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Recupera l'UO dell'utente loggato dal UserContext
     *
     * @param userContext contesto utente
     * @return codice UO dell'utente loggato
     */
    private String recuperaUOUtenteLoggato(UserContext userContext) throws ComponentException {
        try {
            // METODO 1: Prova a recuperare dall'UserContext
            String cdUO = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(userContext);

            if (cdUO != null && !cdUO.trim().isEmpty()) {
                return cdUO;
            }

            // METODO 2: Se non disponibile, cerca dall'anagrafica
            AnagraficoBulk anagrafica = recuperaAnagraficaUtenteLoggato(userContext);
            if (anagrafica != null && anagrafica.getCd_unita_organizzativa() != null) {
                return anagrafica.getCd_unita_organizzativa();
            }

            return null;

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Recupera l'anagrafica dell'utente loggato
     * Serve per impostarla nei dettagli per tracciabilità
     *
     * @param userContext contesto utente
     * @return AnagraficoBulk dell'utente loggato
     */
    private AnagraficoBulk recuperaAnagraficaUtenteLoggato(UserContext userContext)
            throws ComponentException {
        try {
            AnagraficoHome AnagraficoHome = (AnagraficoHome) getHome(userContext, AnagraficoBulk.class);
            String cdUtente = userContext.getUser();

            SQLBuilder sql = AnagraficoHome.createSQLBuilder();
            sql.addClause("AND", "CD_ANAGR", sql.EQUALS, cdUtente);

            java.util.List result = AnagraficoHome.fetchAll(sql);
            if (result != null && !result.isEmpty()) {
                return (AnagraficoBulk) result.get(0);
            }

            return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Determina l'anagrafica da salvare nel dettaglio per audit
     *
     * @param userContext contesto utente
     * @param doc documento
     * @return AnagraficoBulk da salvare nel dettaglio
     */
    private AnagraficoBulk determinaAnagraficaPerDettaglio(UserContext userContext,
                                                           Doc_trasporto_rientroBulk doc) throws ComponentException {
        try {
            // Se ritiro delegato, usa l'anagrafica del dipendente
            if (doc.isRitiroDelegato() && doc.getAnagDipRitiro() != null) {
                return doc.getAnagDipRitiro();
            }

            // Altrimenti usa l'anagrafica dell'utente loggato
            return recuperaAnagraficaUtenteLoggato(userContext);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ========================================
    //RICERCA BENI CON FILTRO UO 
    // ========================================

    /**
     *METODO PRINCIPALE PER LA RICERCA BENI 
     *
     * Cerca i beni disponibili per il trasporto, filtrando SOLO quelli
     * che appartengono all'UO determinata dal documento:
     * - Se ritiro delegato → UO del dipendente
     * - Se ritiro normale → UO dell'utente loggato
     *
     * @param userContext contesto utente
     * @param doc documento di trasporto
     * @param beni_esclusi beni già selezionati
     * @param clauses clausole ricerca aggiuntive
     * @return RemoteIterator per selezione beni
     */
    public it.cnr.jada.util.RemoteIterator cercaBeniTrasportabiliConFiltroUO(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            it.cnr.jada.bulk.SimpleBulkList beni_esclusi,
            CompoundFindClause clauses)
            throws ComponentException {
        try {
            //STEP 1: Determina quale UO usare
            String cdUO = determinaUOPerFiltro(userContext, doc);

            //STEP 2: Verifica che ci siano beni disponibili per questa UO
            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
                    getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            int beniDisponibili = dettHome.contaBeniTrasportabiliPerUO(cdUO, doc.getPg_inventario());

            if (beniDisponibili == 0) {
                String messaggioUO = doc.isRitiroDelegato()
                        ? "del dipendente " + doc.getAnagDipRitiro().getCd_anag() + " (" + cdUO + ")"
                        : "della tua unità organizzativa (" + cdUO + ")";

                throw new ApplicationException(
                        "Non ci sono beni disponibili per il trasporto nell'unità organizzativa " + messaggioUO + ".\n" +
                                "Verificare che esistano beni non scaricati associati a questa UO.");
            }

            //STEP 3: Costruisce la query con filtro UO
            Inventario_beniHome beniHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);

            SQLBuilder sql = beniHome.createSQLBuilder();

            // Filtro per inventario
            sql.addClause("AND", "PG_INVENTARIO", sql.EQUALS, doc.getPg_inventario());

            //FILTRO PRINCIPALE: UO DETERMINATA 
            sql.addClause("AND", "CD_UNITA_ORGANIZZATIVA", sql.EQUALS, cdUO);

            // Filtri per beni trasportabili
            sql.openParenthesis("AND");
            sql.addClause("AND", "FL_TOTALMENTE_SCARICATO", sql.ISNULL, null);
            sql.addClause("OR", "FL_TOTALMENTE_SCARICATO", sql.EQUALS, "N");
            sql.closeParenthesis();

            sql.addClause("AND", "ID_TRANSITO_BENI_ORDINI", sql.ISNULL, null);

            // Applica clausole aggiuntive dalla ricerca
            if (clauses != null) {
                sql.addClause(clauses);
            }

            // Esclude beni già selezionati
            if (beni_esclusi != null && !beni_esclusi.isEmpty()) {
                escludiBeniGiaSelezionati(sql, beni_esclusi);
            }

            sql.addOrderBy("NR_INVENTARIO, PROGRESSIVO");

            // Restituisce iterator remoto
            return iterator(userContext, sql, Inventario_beniBulk.class, "default");

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Helper per escludere beni già selezionati
     */
    private void escludiBeniGiaSelezionati(SQLBuilder sql, it.cnr.jada.bulk.SimpleBulkList beni_esclusi) {
        if (beni_esclusi == null || beni_esclusi.isEmpty()) {
            return;
        }

        for (Object obj : beni_esclusi) {
            Inventario_beniBulk bene = null;

            if (obj instanceof Doc_trasporto_rientro_dettBulk) {
                bene = ((Doc_trasporto_rientro_dettBulk) obj).getBene();
            } else if (obj instanceof Inventario_beniBulk) {
                bene = (Inventario_beniBulk) obj;
            }

            if (bene != null && bene.getNr_inventario() != null) {
                sql.openParenthesis("AND");
                sql.addClause("AND", "NR_INVENTARIO", sql.NOT_EQUALS, bene.getNr_inventario());
                sql.addClause("OR", "PROGRESSIVO", sql.NOT_EQUALS, bene.getProgressivo());
                sql.closeParenthesis();
            }
        }
    }

    // ========================================
    // VALIDAZIONI
    // ========================================

    /**
     *Valida il documento prima del salvataggio
     */
    public void validaDocumento(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            // Verifica esercizio
            if (isEsercizioCOEPChiuso(userContext)) {
                throw new ApplicationException("Impossibile operare su un esercizio chiuso");
            }

            // Verifica dettagli
            if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                    doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                throw new ApplicationException("Il documento deve contenere almeno un bene");
            }

            // Verifica tipo movimento
            if (doc.getTipoMovimento() == null || doc.getCd_tipo_trasporto_rientro() == null) {
                throw new ApplicationException("Il tipo di movimento è obbligatorio");
            }

            // Verifica descrizione
            if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) {
                throw new ApplicationException("La descrizione del documento è obbligatoria");
            }

            // Verifica data
            if (doc.getDataRegistrazione() != null &&
                    doc.getDataRegistrazione().after(new Timestamp(System.currentTimeMillis()))) {
                throw new ApplicationException("La data di registrazione non può essere futura");
            }

            //VALIDAZIONE SPECIFICA PER RITIRO DELEGATO
            validaRitiroDelegato(userContext, doc);

            //Valida i dettagli (include controllo UO)
            validaDettagli(userContext, doc);

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Valida la logica del ritiro delegato
     */
    private void validaRitiroDelegato(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            // Se ritiro delegato, verifica dipendente
            if (doc.isRitiroDelegato()) {
                // Dipendente obbligatorio
                if (doc.getAnagDipRitiro() == null) {
                    throw new ApplicationException(
                            "Quando si seleziona 'Ritiro Incaricato' o 'Ritiro Vettore' " +
                                    "è obbligatorio selezionare un dipendente.");
                }

                // Verifica che il dipendente abbia un'UO
                if (doc.getAnagDipRitiro().getCd_unita_organizzativa() == null ||
                        doc.getAnagDipRitiro().getCd_unita_organizzativa().trim().isEmpty()) {
                    throw new ApplicationException(
                            "Il dipendente selezionato non ha un'unità organizzativa associata.\n" +
                                    "Selezionare un dipendente con UO valida.");
                }

                // Le note di ritiro diventano facoltative ma il campo è abilitato
                // (nessuna validazione particolare)
            } else {
                // Se NON è ritiro delegato, il dipendente deve essere NULL
                if (doc.getAnagDipRitiro() != null) {
                    doc.setAnagDipRitiro(null); // Pulizia automatica
                }

                // Le note di ritiro devono essere vuote/null
                if (doc.getNoteRitiro() != null && !doc.getNoteRitiro().trim().isEmpty()) {
                    doc.setNoteRitiro(null); // Pulizia automatica
                }
            }

            // Verifica mutua esclusività flag
            if (doc.getFlIncaricato() != null && doc.getFlIncaricato().booleanValue() &&
                    doc.getFlVettore() != null && doc.getFlVettore().booleanValue()) {
                throw new ApplicationException(
                        "Non è possibile selezionare contemporaneamente 'Ritiro Incaricato' e 'Ritiro Vettore'.\n" +
                                "Selezionare solo una delle due opzioni.");
            }

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Valida i dettagli del documento
     */
    private void validaDettagli(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws ComponentException {
        try {
            // Determina l'anagrafica da usare per i dettagli
            AnagraficoBulk anagraficaPerDettagli = determinaAnagraficaPerDettaglio(userContext, doc);

            // Determina l'UO per validazione
            String cdUO = determinaUOPerFiltro(userContext, doc);

            int index = 0;
            for (Iterator it = doc.getDoc_trasporto_rientro_dettColl().iterator(); it.hasNext();) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) it.next();

                // Valida il dettaglio
                validaDettaglio(userContext, doc, dettaglio, ++index, cdUO);

                //Imposta l'anagrafica corretta nel dettaglio
                if (anagraficaPerDettagli != null) {
                    dettaglio.setAnagDipRitiro(anagraficaPerDettagli);
                }
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Valida singolo dettaglio con controllo UO
     */
    private void validaDettaglio(UserContext userContext, Doc_trasporto_rientroBulk doc,
                                 Doc_trasporto_rientro_dettBulk dettaglio, int index, String cdUO)
            throws ComponentException {
        try {
            if (dettaglio.getBene() == null || dettaglio.getNr_inventario() == null) {
                throw new ApplicationException("Riga " + index + ": il bene è obbligatorio");
            }

            //VALIDA CHE IL BENE APPARTENGA ALL'UO CORRETTA
            validaBenePerUO(userContext, doc, dettaglio.getBene(), cdUO);

            if (dettaglio.getQuantita() == null || dettaglio.getQuantita().longValue() <= 0) {
                throw new ApplicationException("Riga " + index + ": la quantità deve essere maggiore di zero");
            }

            if (Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())) {
                if (!dettaglio.isValidoPerTrasporto()) {
                    throw new ApplicationException("Riga " + index + ": il bene " + dettaglio.getCod_bene() +
                            " non è valido per il trasporto");
                }
            }

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     *Valida che il bene appartenga all'UO specificata
     */
    public void validaBenePerUO(UserContext userContext, Doc_trasporto_rientroBulk doc,
                                Inventario_beniBulk bene, String cdUO) throws ComponentException {
        try {
            if (cdUO == null) {
                throw new ApplicationException(
                        "Impossibile validare il bene: unità organizzativa non disponibile");
            }

            Doc_trasporto_rientro_dettHome dettHome = (Doc_trasporto_rientro_dettHome)
                    getHome(userContext, Doc_trasporto_rientro_dettBulk.class);

            if (!dettHome.beneBelongsToUO(bene, cdUO)) {
                String uoBene = dettHome.getUnitaOrganizzativaBene(bene);

                String messaggioUO = doc.isRitiroDelegato()
                        ? "del dipendente " + doc.getAnagDipRitiro().getCd_anag() + " (UO: " + cdUO + ")"
                        : "tua (UO: " + cdUO + ")";

                throw new ApplicationException(
                        "Il bene " + bene.getNumeroBeneCompleto() +
                                " non può essere aggiunto al trasporto.\n" +
                                "Motivo: appartiene all'unità organizzativa '" + uoBene + "' " +
                                "mentre l'unità organizzativa " + messaggioUO + ".\n" +
                                "Puoi trasportare solo beni dell'unità organizzativa selezionata.");
            }
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ========================================
    // OPERAZIONI CRUD
    // ========================================

    /**
     * Crea documento
     */
    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            validaDocumento(userContext, doc);
            doc.setStato(Doc_trasporto_rientro_dettBulk.STATO_INSERITO);

            for (Iterator it = doc.getDoc_trasporto_rientro_dettColl().iterator(); it.hasNext();) {
                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) it.next();
                if (dettaglio.getStatoTrasporto() == null) {
                    dettaglio.setStatoTrasporto(Doc_trasporto_rientro_dettBulk.STATO_INSERITO);
                }
            }

            return super.creaConBulk(userContext, doc);

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Modifica documento
     */
    public OggettoBulk modificaConBulk(UserContext userContext, OggettoBulk bulk)
            throws ComponentException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            validaDocumento(userContext, doc);
            return super.modificaConBulk(userContext, doc);
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Aggiunge bene con controllo UO
     */
    public Doc_trasporto_rientro_dettBulk aggiungiBene(UserContext userContext,
                                                       Doc_trasporto_rientroBulk doc,
                                                       Inventario_beniBulk bene)
            throws ComponentException {
        try {
            if (doc.includesBene(bene)) {
                throw new ApplicationException("Il bene è già presente nel documento");
            }

            Inventario_beniHome beneHome = (Inventario_beniHome)
                    getHome(userContext, Inventario_beniBulk.class);
            bene = (Inventario_beniBulk) beneHome.findByPrimaryKey(bene);

            //Determina UO e valida
            String cdUO = determinaUOPerFiltro(userContext, doc);
            validaBenePerUO(userContext, doc, bene, cdUO);

            if (Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())) {
                if (!isBeneTrasportabile(userContext, bene)) {
                    throw new ApplicationException(
                            "Il bene non può essere trasportato (è totalmente scaricato o in transito)");
                }
            }

            Doc_trasporto_rientro_dettBulk dettaglio = new Doc_trasporto_rientro_dettBulk();
            dettaglio.setBene(bene);
            dettaglio.setQuantita(1L);
            dettaglio.setStatoTrasporto(Doc_trasporto_rientro_dettBulk.STATO_INSERITO);

            //Imposta anagrafica corretta
            AnagraficoBulk anagrafica = determinaAnagraficaPerDettaglio(userContext, doc);
            if (anagrafica != null) {
                dettaglio.setAnagDipRitiro(anagrafica);
            }

            doc.addToDoc_trasporto_rientro_dettColl(dettaglio);

            return dettaglio;

        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Verifica bene trasportabile
     */
    public boolean isBeneTrasportabile(UserContext userContext, Inventario_beniBulk bene)
            throws ComponentException {
        try {
            if (bene.getFl_totalmente_scaricato() == null || bene.getId_transito_beni_ordini() == null) {
                Inventario_beniHome beneHome = (Inventario_beniHome)
                        getHome(userContext, Inventario_beniBulk.class);
                bene = (Inventario_beniBulk) beneHome.findByPrimaryKey(bene);
            }

            if (bene.getFl_totalmente_scaricato() != null && bene.getFl_totalmente_scaricato().booleanValue()) {
                return false;
            }

            if (bene.getId_transito_beni_ordini() != null) {
                return false;
            }

            return true;

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Verifica esercizio chiuso
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
            if (!inventarioHome.isAperto(inventario,
                    it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext)))
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
}