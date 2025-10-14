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

package it.cnr.contab.inventario01.bp;

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.AbstractDetailCRUDController;
import it.cnr.jada.util.action.SearchProvider;
import it.cnr.jada.util.action.SimpleDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;

import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Business Process per la gestione del Rientro dei beni inventariali.
 * Gestisce il ritorno dei beni trasportati alla loro ubicazione originale o altra sede.
 *
 * @author rpucciarelli
 */
public class CRUDRientroBeniInvBP extends CRUDTraspRientInventarioBP implements SelectionListener {

    private Long progressivo_beni = new Long("0");

    public Long getProgressivo_beni() {
        return progressivo_beni;
    }

    public void setProgressivo_beni(Long long1) {
        progressivo_beni = long1;
    }

    // Flag per abilitare/disabilitare funzionalità
    private boolean attivaEtichettaInventarioBene = false;
    private boolean isNumGruppiErrato = false;
    private boolean isQuantitaEnabled = true;

    // Selezione multipla dei beni
    private BitSet selection = new BitSet();

    // Controller per la selezione del trasporto di riferimento
    private final SimpleDetailCRUDController documentiTrasporto =
            new SimpleDetailCRUDController("DocumentiTrasporto",
                    Doc_trasporto_rientroBulk.class,
                    "documentiTrasportoColl",
                    this);

    /**
     * Costruttore di default
     */
    public CRUDRientroBeniInvBP() {
        super();
        setTab("tab", "tabRientroTestata");
    }

    /**
     * Costruttore con function
     */
    public CRUDRientroBeniInvBP(String function) {
        super(function);
    }

    /**
     * Inizializzazione del Business Process
     */
    @Override
    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws it.cnr.jada.action.BusinessProcessException {

        super.init(config, context);
        resetTabs();

        try {
            attivaEtichettaInventarioBene = Utility.createConfigurazioneCnrComponentSession()
                    .isGestioneEtichettaInventarioBeneAttivo(context.getUserContext());
        } catch (ComponentException | RemoteException e) {
            throw new BusinessProcessException(e);
        }
    }

    // ========================================
    // GESTIONE TAB
    // ========================================

    /**
     * Reset delle tab alla tab principale
     */
    public void resetTabs() {
        setTab("tab", "tabRientroTestata");
    }

    // ========================================
    // INIZIALIZZAZIONE MODEL
    // ========================================

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Doc_trasporto_rientroBulk.RIENTRO);
        resetTabs();
        bulk = super.initializeModelForEdit(context, documento);
        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Doc_trasporto_rientroBulk.RIENTRO);
        bulk = super.initializeModelForInsert(context, documento);
        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForFreeSearch(ActionContext actioncontext, OggettoBulk oggettobulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) oggettobulk;
        documento.setTiDocumento(Doc_trasporto_rientroBulk.RIENTRO);
        oggettobulk = super.initializeModelForFreeSearch(actioncontext, documento);
        return oggettobulk;
    }

    @Override
    public OggettoBulk initializeModelForSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Doc_trasporto_rientroBulk.RIENTRO);
        bulk = super.initializeModelForSearch(context, documento);
        return bulk;
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    // ========================================
    // CONTROLLER DEI DETTAGLI
    // ========================================

    /**
     * Controller per i dettagli del documento di rientro
     */
    protected final AbstractDetailCRUDController dettaglio = createDettaglio();

    protected AbstractDetailCRUDController createDettaglio() {
        return new SimpleDetailCRUDController(
                "Dettaglio",
                Doc_trasporto_rientro_dettBulk.class,
                "doc_trasporto_rientro_dettColl",
                this) {

            @Override
            public void validate(ActionContext context, OggettoBulk model) throws ValidationException {
                validate_Dettagli(context, model);
            }

            @Override
            public boolean isGrowable() {
                // Permetti l'aggiunta di nuove righe solo in inserimento
                return isInserting();
            }
        };
    }

    public AbstractDetailCRUDController getDettaglio() {
        return dettaglio;
    }

    public SimpleDetailCRUDController getDocumentiTrasporto() {
        return documentiTrasporto;
    }

    // ========================================
    // VALIDAZIONI
    // ========================================

    /**
     * Validazione dei dettagli del rientro
     */
    private void validate_Dettagli(ActionContext context, OggettoBulk model) throws ValidationException {

        if (isInserting()) {
            try {
                completeSearchTools(context, this);

                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) model;
                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) dettaglio.getDoc_trasporto_rientro();

                // Controlla che l'Utente abbia inserito tutti i campi Obbligatori
                validate_property_details(dettaglio);

                // Verifica che il rientro sia collegato ad un trasporto
                validate_riferimento_trasporto(dettaglio);

                // Gestione beni accessori contestuali
                PrimaryKeyHashtable accessori_contestuali = null;
                BulkList newBeni_associati = new BulkList();

                if (dettaglio.isAssociatoConAccessorioContestuale()) {
                    if (dettaglio.getQuantita() != null && dettaglio.getQuantita().compareTo(new Long("1")) != 0) {
                        dettaglio.setQuantita(new Long("1"));
                        throw new ValidationException(
                                "Attenzione: la quantità di questa riga deve essere 1, poiché alcuni beni sono suoi accessori");
                    }

                    accessori_contestuali = documento.getAccessoriContestualiHash();
                    BulkList beni_associati = (BulkList) accessori_contestuali.get(dettaglio.getChiaveHash());

                    for (Iterator i = beni_associati.iterator(); i.hasNext();) {
                        Doc_trasporto_rientro_dettBulk dettaglio_associato =
                                (Doc_trasporto_rientro_dettBulk) i.next();
                        dettaglio_associato.getBene().getBene_principale()
                                .setDs_bene(dettaglio.getBene().getDs_bene());
                        dettaglio_associato.getBene().setUbicazione(dettaglio.getBene().getUbicazione());
                        dettaglio_associato.getBene().setCategoria_Bene(dettaglio.getBene().getCategoria_Bene());
                        newBeni_associati.add(dettaglio_associato);
                    }
                    accessori_contestuali.put(dettaglio.getChiaveHash(), newBeni_associati);
                }

                if (dettaglio.isAccessorioContestuale()) {
                    accessori_contestuali = documento.getAccessoriContestualiHash();
                    for (java.util.Enumeration e = accessori_contestuali.keys(); e.hasMoreElements();) {
                        String chiave_bene_padre = (String) e.nextElement();
                        newBeni_associati = (BulkList) accessori_contestuali.get(chiave_bene_padre);
                        if (newBeni_associati.containsByPrimaryKey(dettaglio)) {
                            newBeni_associati.remove(dettaglio);
                            newBeni_associati.add(dettaglio);
                            break;
                        }
                    }
                }

            } catch (BusinessProcessException e1) {
                handleException(e1);
            }
        }
    }

    /**
     * Validazione delle proprietà del dettaglio
     */
    private void validate_property_details(Doc_trasporto_rientro_dettBulk dett) throws ValidationException {

        Inventario_beniBulk bene = dett.getBene();

        // CONTROLLA CHE SIA STATA SPECIFICATA UNA CATEGORIA PER IL BENE
        if (bene.getCategoria_Bene() == null) {
            throw new ValidationException("Attenzione: indicare la Categoria di appartenenza del Bene");
        }

        // CONTROLLA CHE SIA STATA SPECIFICATA UNA DESCRIZIONE PER IL BENE
        if (bene.getDs_bene() == null) {
            throw new ValidationException("Attenzione: indicare la Descrizione del Bene");
        }

        // CONTROLLA CHE SIA STATA SPECIFICATA UNA CONDIZIONE PER IL BENE
        if (bene.getCondizioneBene() == null) {
            throw new ValidationException("Attenzione: indicare una Condizione per il Bene");
        }

        // CONTROLLA, NEL CASO DI UN BENE ACCESSORIO, CHE SIA STATO SPECIFICATO IL BENE PRINCIPALE
        if (dett.isBeneAccessorio() && bene.getBene_principale() == null) {
            throw new ValidationException("Attenzione: indicare un Bene Principale per il Bene Accessorio");
        }

        // CONTROLLA CHE SIA STATA SPECIFICATA UNA UBICAZIONE PER IL BENE
        if (bene.getUbicazione() == null) {
            throw new ValidationException("Attenzione: indicare l'Ubicazione del Bene");
        }

        // CONTROLLA CHE SIA STATO INSERITO LA QUANTITA' PER IL BENE
        if (dett.getQuantita() == null) {
            throw new ValidationException("Attenzione: indicare la Quantità");
        }

        // VERIFICA CHE IL BENE SIA VALIDO PER IL RIENTRO
        if (!dett.isValidoPerRientro()) {
            throw new ValidationException(
                    "Attenzione: il bene selezionato non è valido per il rientro. " +
                            "Verificare che il bene sia stato trasportato e che esista un riferimento al trasporto originale");
        }

        // VERIFICA CHE IL BENE NON SIA GIÀ PRESENTE NEL DOCUMENTO DI RIENTRO
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) dett.getDoc_trasporto_rientro();
        if (documento.getDoc_trasporto_rientro_dettColl() != null) {
            for (Iterator i = documento.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext();) {
                Doc_trasporto_rientro_dettBulk existingDett = (Doc_trasporto_rientro_dettBulk) i.next();
                if (existingDett != dett &&
                        existingDett.getNr_inventario().equals(dett.getNr_inventario()) &&
                        existingDett.getProgressivo().equals(dett.getProgressivo())) {
                    throw new ValidationException("Attenzione: il bene è già presente nel documento di rientro");
                }
            }
        }
    }

    /**
     * Validazione del riferimento al trasporto originale
     */
    private void validate_riferimento_trasporto(Doc_trasporto_rientro_dettBulk dett) throws ValidationException {

        // Verifica che esista il riferimento al trasporto originale
        if (dett.getPgInventarioRif() == null ||
                dett.getPgDocTrasportoRientroRif() == null ||
                dett.getNrInventarioRif() == null) {
            throw new ValidationException(
                    "Attenzione: il dettaglio di rientro deve avere un riferimento al documento di trasporto originale");
        }

        // Verifica coerenza dei riferimenti
        if (!dett.getNrInventarioRif().equals(dett.getNr_inventario()) ||
                !dett.getProgressivoRif().equals(dett.getProgressivo())) {
            throw new ValidationException(
                    "Attenzione: i riferimenti al bene non sono coerenti con il trasporto originale");
        }
    }

    // ========================================
    // GESTIONE BENI
    // ========================================

    /**
     * Aggiunge dettagli di beni al documento di rientro
     */
    public void aggiungiDettagliRientro(it.cnr.jada.UserContext userContext, java.util.List dettagliTrasporto)
            throws it.cnr.jada.comp.ComponentException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        Doc_trasporto_rientro_dettBulk dettRientro = null;
        Doc_trasporto_rientro_dettBulk dettTrasporto = null;

        for (Iterator i = dettagliTrasporto.iterator(); i.hasNext();) {
            dettTrasporto = (Doc_trasporto_rientro_dettBulk) i.next();


            // Verifica che il dettaglio sia stato effettivamente trasportato
            if (!Doc_trasporto_rientroBulk.TRASPORTO.equals(dettTrasporto.getDoc_trasporto_rientro().getTiDocumento()) &&
                    !Doc_trasporto_rientro_dettBulk.STATO_DEFINITIVO.equals(dettTrasporto.getDoc_trasporto_rientro().getTiDocumento())) {
                throw new ApplicationException(
                        "Il Bene " + dettTrasporto.getNr_inventario() +
                                " non è stato ancora trasportato e non può essere inserito nel rientro");
            }

            // Verifica che non sia già rientrato
            if (Doc_trasporto_rientroBulk.RIENTRO.equals(dettTrasporto.getDoc_trasporto_rientro().getTiDocumento())) {
                throw new ApplicationException(
                        "Il Bene " + dettTrasporto.getNr_inventario() +
                                " è già rientrato");
            }

            // Verifica validità categoria
            Inventario_beniBulk bene = dettTrasporto.getBene();
            if (bene.getCategoria_Bene() != null &&
                    documento.getDataRegistrazione() != null &&
                    bene.getCategoria_Bene().getData_cancellazione() != null &&
                    bene.getCategoria_Bene().getData_cancellazione()
                            .before(documento.getDataRegistrazione())) {
                throw new ApplicationException(
                        "Il Bene " + bene.getNr_inventario() + " ha una categoria non più valida");
            }

            // Crea il dettaglio di rientro
            dettRientro = new Doc_trasporto_rientro_dettBulk();
            dettRientro.setDoc_trasporto_rientro(documento);
            dettRientro.setBene(bene);
            dettRientro.setQuantita(dettTrasporto.getQuantita());
            dettRientro.setStatoTrasporto(null); // Stato gestito dal documento

            // Imposta i riferimenti al trasporto originale
            dettRientro.setPgInventarioRif(dettTrasporto.getPg_inventario());
            dettRientro.setTiDocumentoRif(dettTrasporto.getTi_documento());
            dettRientro.setEsercizioRif(dettTrasporto.getEsercizio());
            dettRientro.setPgDocTrasportoRientroRif(dettTrasporto.getPg_doc_trasporto_rientro());
            dettRientro.setNrInventarioRif(dettTrasporto.getNr_inventario());
            dettRientro.setProgressivoRif(dettTrasporto.getProgressivo());

            // Imposta il riferimento circolare
            dettRientro.setDocTrasportoRientroDettRif(dettTrasporto);

            documento.getDoc_trasporto_rientro_dettColl().add(dettRientro);
        }
    }

    /**
     * Aggiunge dettagli da un documento di trasporto specifico
     */
    public void aggiungiDettagliDaTrasporto(it.cnr.jada.UserContext userContext,
                                            Doc_trasporto_rientroBulk docTrasporto)
            throws it.cnr.jada.comp.ComponentException {

        if (docTrasporto == null || docTrasporto.getDoc_trasporto_rientro_dettColl() == null) {
            throw new ApplicationException("Nessun dettaglio disponibile nel documento di trasporto selezionato");
        }

        // Filtra solo i dettagli trasportati e non ancora rientrati
        java.util.List dettagliDisponibili = new java.util.ArrayList();
        for (Iterator i = docTrasporto.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext();) {
            Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) i.next();
            if (Doc_trasporto_rientroBulk.TRASPORTO.equals(dett.getDoc_trasporto_rientro().getTiDocumento()) ||
                    Doc_trasporto_rientro_dettBulk.STATO_DEFINITIVO.equals(dett.getStatoTrasporto())) {
                dettagliDisponibili.add(dett);
            }
        }

        if (dettagliDisponibili.isEmpty()) {
            throw new ApplicationException(
                    "Nessun bene disponibile per il rientro nel documento di trasporto selezionato");
        }

        aggiungiDettagliRientro(userContext, dettagliDisponibili);
    }

    // ========================================
    // SEARCH PROVIDER
    // ========================================

    /**
     * Provider per la ricerca dei beni trasportati disponibili per il rientro
     */
    public SearchProvider getBeneSearchProvider(ActionContext context) {
        return new SearchProvider() {
            public it.cnr.jada.util.RemoteIterator search(
                    it.cnr.jada.action.ActionContext context,
                    it.cnr.jada.persistency.sql.CompoundFindClause clauses,
                    it.cnr.jada.bulk.OggettoBulk prototype) throws it.cnr.jada.action.BusinessProcessException {

                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
                try {
                    return getListaBeniRientrabili(
                            context.getUserContext(),
                            documento.getDoc_trasporto_rientro_dettColl(),
                            clauses);
                } catch (Throwable t) {
                    return null;
                }
            }
        };
    }

    /**
     * Provider per la ricerca dei documenti di trasporto disponibili
     */
    public SearchProvider getTrasportoSearchProvider(ActionContext context) {
        return new SearchProvider() {
            public it.cnr.jada.util.RemoteIterator search(
                    it.cnr.jada.action.ActionContext context,
                    it.cnr.jada.persistency.sql.CompoundFindClause clauses,
                    it.cnr.jada.bulk.OggettoBulk prototype) throws it.cnr.jada.action.BusinessProcessException {

                try {
                    return getListaTrasporti(context.getUserContext(), clauses);
                } catch (Throwable t) {
                    return null;
                }
            }
        };
    }

    /**
     * Cerca i beni trasportati disponibili per il rientro
     */
    public it.cnr.jada.util.RemoteIterator getListaBeniRientrabili(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, java.rmi.RemoteException, it.cnr.jada.comp.ComponentException {
        //TODO da verificare e in caso implementare
//        return ((DocTrasportoRientroComponentSession) createComponentSession())
//                .getListaBeniRientrabili(
//                        userContext,
//                        (Doc_trasporto_rientroBulk) this.getModel(),
//                        beni_da_escludere,
//                        clauses);
        return null;
    }

    /**
     * Cerca i documenti di trasporto con beni disponibili per il rientro
     */
    public it.cnr.jada.util.RemoteIterator getListaTrasporti(
            it.cnr.jada.UserContext userContext,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, java.rmi.RemoteException, it.cnr.jada.comp.ComponentException {
        //TODO da verificare e in caso implementare
//        return ((DocTrasportoRientroComponentSession) createComponentSession())
//                .getListaTrasportiConBeniDisponibili(userContext, clauses);
        return null;
    }

    // ========================================
    // ABILITAZIONE PULSANTI
    // ========================================

    @Override
    public boolean isPrintButtonHidden() {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        return documento.isTemporaneo() || super.isPrintButtonHidden();
    }

    @Override
    public boolean isBringbackButtonEnabled() {
        return isInserting() || isEditing();
    }

    @Override
    public boolean isBringbackButtonHidden() {
        return false;
    }

    public boolean isCRUDAddButtonEnabled() {
        return isInserting();
    }

    public boolean isCRUDDeleteButtonEnabled() {
        return isInserting();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return isEditing();
    }

    @Override
    public boolean isDeleteButtonHidden() {
        return false;
    }

    /**
     * Abilita il pulsante per importare beni da un trasporto
     */
    public boolean isImportaDaTrasportoButtonEnabled() {
        return isInserting();
    }

    // ========================================
    // IMPLEMENTAZIONE SelectionListener
    // ========================================

    @Override
    public void clearSelection(ActionContext actionContext) throws BusinessProcessException {
        selection.clear();
    }

    @Override
    public void deselectAll(ActionContext actionContext) {
        selection.clear();
    }

    @Override
    public BitSet getSelection(ActionContext actionContext, OggettoBulk[] oggettoBulks, BitSet bitSet)
            throws BusinessProcessException {
        return selection;
    }

    @Override
    public void initializeSelection(ActionContext actionContext) throws BusinessProcessException {
        selection.clear();
    }

    @Override
    public void selectAll(ActionContext actionContext) throws BusinessProcessException {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        if (documento != null && documento.getDoc_trasporto_rientro_dettColl() != null) {
            selection.set(0, documento.getDoc_trasporto_rientro_dettColl().size());
        }
    }

    @Override
    public BitSet setSelection(ActionContext actionContext, OggettoBulk[] oggettoBulks,
                               BitSet bitSet, BitSet bitSet1) throws BusinessProcessException {
        selection = bitSet1;
        return selection;
    }

    // ========================================
    // GETTER E SETTER
    // ========================================

    public boolean isAttivaEtichettaInventarioBene() {
        return attivaEtichettaInventarioBene;
    }

    public void setIsNumGruppiErrato(boolean newIsNumGruppiErrato) {
        isNumGruppiErrato = newIsNumGruppiErrato;
    }

    public boolean isNumGruppiErrato() {
        return isNumGruppiErrato;
    }

    public void setIsQuantitaEnabled(boolean enabled) {
        isQuantitaEnabled = enabled;
    }

    public boolean isQuantitaEnabled() {
        return isQuantitaEnabled;
    }

    public String getLabelData_documento() {
        return "Data Rientro";
    }

    // ========================================
    // METODI UTILITY
    // ========================================

    /**
     * Verifica se il dettaglio può essere modificato
     */
    public boolean isDettaglioModificabile() {
        Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) getDettaglio().getModel();
        if (dett != null) {
            // Può essere modificato solo se lo stato è null, INSERITO o ANNULLATO
            return dett.getStatoTrasporto() == null ||
                    Doc_trasporto_rientro_dettBulk.STATO_INSERITO.equals(dett.getStatoTrasporto()) ||
                    Doc_trasporto_rientro_dettBulk.STATO_ANNULLATO.equals(dett.getStatoTrasporto());
        }
        return true;
    }

    /**
     * Verifica se il documento può essere confermato
     */
    public boolean isConfermabile() {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        if (documento != null && documento.getDoc_trasporto_rientro_dettColl() != null) {
            return documento.getDoc_trasporto_rientro_dettColl().size() > 0;
        }
        return false;
    }

    /**
     * Verifica se tutti i dettagli hanno un riferimento valido al trasporto
     */
    public boolean hasTuttiRiferimentiValidi() {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        if (documento != null && documento.getDoc_trasporto_rientro_dettColl() != null) {
            for (Iterator i = documento.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext();) {
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) i.next();
                if (!dett.isValidoPerRientro()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Verifica se esiste un documento di trasporto selezionato
     */
    public boolean hasTrasportoSelezionato() {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        return documento != null &&
                documento.getDoc_trasporto_rientro_dettColl() != null &&
                !documento.getDoc_trasporto_rientro_dettColl().isEmpty();
    }
}