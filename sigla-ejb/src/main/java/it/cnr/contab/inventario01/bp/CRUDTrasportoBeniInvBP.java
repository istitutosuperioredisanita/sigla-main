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
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
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
 * Business Process per la gestione del Trasporto dei beni inventariali.
 *
 *LOGICA FILTRO UO CON DIPENDENTE
 *
 * CASO 1: FL_INCARICATO = N e FL_VETTORE = N
 *   → Campo dipendente NASCOSTO/DISABILITATO
 *   → Campo noteRitiro DISABILITATO
 *   → Usa UO dell'utente loggato
 *
 * CASO 2: FL_INCARICATO = Y o FL_VETTORE = Y
 *   → Campo dipendente VISIBILE e OBBLIGATORIO
 *   → Campo noteRitiro ABILITATO
 *   → Usa UO del dipendente selezionato
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP implements SelectionListener {

    private boolean attivaEtichettaInventarioBene = false;
    private boolean isNumGruppiErrato = false;
    private boolean isQuantitaEnabled = true;
    private BitSet selection = new BitSet();

    public CRUDTrasportoBeniInvBP() {
        super();
        setTab("tab", "tabTrasportoTestata");
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

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

    public void resetTabs() {
        setTab("tab", "tabTrasportoTestata");
    }

    // ========================================
    // INIZIALIZZAZIONE MODEL
    // ========================================

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Tipo_trasporto_rientroBulk.TIPO_TRASPORTO);
        resetTabs();
        bulk = super.initializeModelForEdit(context, documento);
        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Tipo_trasporto_rientroBulk.TIPO_TRASPORTO);
        bulk = super.initializeModelForInsert(context, documento);
        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForFreeSearch(ActionContext actioncontext, OggettoBulk oggettobulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) oggettobulk;
        documento.setTiDocumento(Tipo_trasporto_rientroBulk.TIPO_TRASPORTO);
        oggettobulk = super.initializeModelForFreeSearch(actioncontext, documento);
        return oggettobulk;
    }

    @Override
    public OggettoBulk initializeModelForSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bulk;
        documento.setTiDocumento(Tipo_trasporto_rientroBulk.TIPO_TRASPORTO);
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
                return isInserting();
            }
        };
    }

    public AbstractDetailCRUDController getDettaglio() {
        return dettaglio;
    }

    // ========================================
    // VALIDAZIONI
    // ========================================

    private void validate_Dettagli(ActionContext context, OggettoBulk model) throws ValidationException {

        if (isInserting()) {
            try {
                completeSearchTools(context, this);

                Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) model;
                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) dettaglio.getDoc_trasporto_rientro();

                validate_property_details(dettaglio);

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

    private void validate_property_details(Doc_trasporto_rientro_dettBulk dett) throws ValidationException {

        Inventario_beniBulk bene = dett.getBene();

        if (bene.getCategoria_Bene() == null) {
            throw new ValidationException("Attenzione: indicare la Categoria di appartenenza del Bene");
        }

        if (bene.getDs_bene() == null) {
            throw new ValidationException("Attenzione: indicare la Descrizione del Bene");
        }

        if (bene.getCondizioneBene() == null) {
            throw new ValidationException("Attenzione: indicare una Condizione per il Bene");
        }

        if (dett.isBeneAccessorio() && bene.getBene_principale() == null) {
            throw new ValidationException("Attenzione: indicare un Bene Principale per il Bene Accessorio");
        }

        if (bene.getUbicazione() == null) {
            throw new ValidationException("Attenzione: indicare l'Ubicazione del Bene");
        }

        if (dett.getQuantita() == null) {
            throw new ValidationException("Attenzione: indicare la Quantità");
        }

        if (!dett.isValidoPerTrasporto()) {
            throw new ValidationException(
                    "Attenzione: il bene selezionato non è valido per il trasporto");
        }

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) dett.getDoc_trasporto_rientro();
        if (documento.getDoc_trasporto_rientro_dettColl() != null) {
            for (Iterator i = documento.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext();) {
                Doc_trasporto_rientro_dettBulk existingDett = (Doc_trasporto_rientro_dettBulk) i.next();
                if (existingDett != dett &&
                        existingDett.getNr_inventario().equals(dett.getNr_inventario()) &&
                        existingDett.getProgressivo().equals(dett.getProgressivo())) {
                    throw new ValidationException("Attenzione: il bene è già presente nel documento di trasporto");
                }
            }
        }
    }

    // ========================================
    // GESTIONE BENI
    // ========================================

    public void aggiungiDettagliTrasporto(it.cnr.jada.UserContext userContext, java.util.List beni)
            throws it.cnr.jada.comp.ComponentException {

        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        Doc_trasporto_rientro_dettBulk dettTrasporto = null;
        Inventario_beniBulk bene = null;

        for (Iterator i = beni.iterator(); i.hasNext();) {
            dettTrasporto = new Doc_trasporto_rientro_dettBulk();
            bene = (Inventario_beniBulk) i.next();

            if (bene.getCategoria_Bene() != null &&
                    documento.getDataRegistrazione() != null &&
                    bene.getCategoria_Bene().getData_cancellazione() != null &&
                    bene.getCategoria_Bene().getData_cancellazione()
                            .before(documento.getDataRegistrazione())) {
                throw new ApplicationException(
                        "Il Bene " + bene.getNr_inventario() + " ha una categoria non più valida");
            }

            if (bene.getFl_totalmente_scaricato() != null &&
                    bene.getFl_totalmente_scaricato().booleanValue()) {
                throw new ApplicationException(
                        "Il Bene " + bene.getNr_inventario() + " è stato totalmente scaricato e non può essere trasportato");
            }

            if (bene.getId_transito_beni_ordini() != null) {
                throw new ApplicationException(
                        "Il Bene " + bene.getNr_inventario() + " è in transito e non può essere trasportato");
            }

            dettTrasporto.setDoc_trasporto_rientro(documento);
            dettTrasporto.setBene(bene);
            dettTrasporto.setQuantita(new Long(1));
            dettTrasporto.setStatoTrasporto(Doc_trasporto_rientro_dettBulk.STATO_PREDISPOSTO_FIRMA);
            documento.getDoc_trasporto_rientro_dettColl().add(dettTrasporto);
        }
    }

    // ========================================
    // SEARCH PROVIDER CON FILTRO UO
    // ========================================

    /**
     *Provider per la ricerca dei beni CON FILTRO UO
     */
    public SearchProvider getBeneSearchProvider(ActionContext context) {
        return new SearchProvider() {
            public it.cnr.jada.util.RemoteIterator search(
                    it.cnr.jada.action.ActionContext context,
                    it.cnr.jada.persistency.sql.CompoundFindClause clauses,
                    it.cnr.jada.bulk.OggettoBulk prototype) throws it.cnr.jada.action.BusinessProcessException {

                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
                try {
                    //USA IL METODO CON FILTRO UO
                    return getListaBeniTrasportabiliConFiltroUO(
                            context.getUserContext(),
                            documento,
                            documento.getDoc_trasporto_rientro_dettColl(),
                            clauses);
                } catch (Throwable t) {
                    throw new BusinessProcessException(t);
                }
            }
        };
    }

    /**
     *Cerca beni FILTRATI per UO (utente o dipendente)
     */
    public it.cnr.jada.util.RemoteIterator getListaBeniTrasportabiliConFiltroUO(
            it.cnr.jada.UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, java.rmi.RemoteException, it.cnr.jada.comp.ComponentException {

        try {
            //CHIAMA IL COMPONENT CHE GESTISCE IL FILTRO UO
            //TODO da verificare e in caso implementare
//            return ((DocTrasportoRientroComponentSession) createComponentSession())
//                    .cercaBeniTrasportabiliConFiltroUO(
//                            userContext,
//                            doc,
//                            beni_da_escludere,
//                            clauses);
        } catch (Exception e) {
            throw new BusinessProcessException(e);
        }
        return null;
    }

    // ========================================
    //ABILITAZIONE CAMPI CONDIZIONALI
    // ========================================

    /**
     *Verifica se il campo dipendente è abilitato
     */
    public boolean isDipendenteEnabled() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        if (doc != null) {
            return doc.isDipendenteEnabled();
        }
        return false;
    }

    /**
     *Verifica se il campo noteRitiro è abilitato
     */
    public boolean isNoteRitiroEnabled() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        if (doc != null) {
            return doc.isNoteRitiroEnabled();
        }
        return false;
    }

    /**
     *Verifica se il dipendente è obbligatorio
     */
    public boolean isDipendenteRequired() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        if (doc != null) {
            return doc.isDipendenteRequired();
        }
        return false;
    }

    /**
     *Handler per cambio flag FL_INCARICATO
     */
    public void doOnFlIncaricatoChange(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            // Se viene attivato FL_INCARICATO, disattiva FL_VETTORE
            if (doc.getFlIncaricato() != null && doc.getFlIncaricato().booleanValue()) {
                doc.setFlVettore(Boolean.FALSE);
            }

            // Se viene disattivato, pulisce dipendente e note
            if (doc.getFlIncaricato() == null || !doc.getFlIncaricato().booleanValue()) {
                if (!doc.isRitiroDelegato()) {
                    doc.setAnagDipRitiro(null);
                    doc.setNoteRitiro(null);
                }
            }

        } catch (Exception e) {
            throw new BusinessProcessException(e);
        }
    }

    /**
     *Handler per cambio flag FL_VETTORE
     */
    public void doOnFlVettoreChange(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            // Se viene attivato FL_VETTORE, disattiva FL_INCARICATO
            if (doc.getFlVettore() != null && doc.getFlVettore().booleanValue()) {
                doc.setFlIncaricato(Boolean.FALSE);
            }

            // Se viene disattivato, pulisce dipendente e note
            if (doc.getFlVettore() == null || !doc.getFlVettore().booleanValue()) {
                if (!doc.isRitiroDelegato()) {
                    doc.setAnagDipRitiro(null);
                    doc.setNoteRitiro(null);
                }
            }

        } catch (Exception e) {
            throw new BusinessProcessException(e);
        }
    }

    /**
     *Handler per cambio dipendente
     */
    public void doOnDipendenteChange(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            // Se cambia il dipendente, pulisce i dettagli esistenti
            // perché potrebbero appartenere a un'altra UO
            if (doc.getAnagDipRitiro() != null &&
                    doc.getDoc_trasporto_rientro_dettColl() != null &&
                    !doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {

                // Avvisa l'utente
                throw new ApplicationException(
                        "ATTENZIONE: Cambiando il dipendente, i beni già selezionati verranno rimossi " +
                                "perché potrebbero appartenere a un'unità organizzativa diversa.\n" +
                                "Confermare per procedere.");
            }

        } catch (ApplicationException e) {
            throw new BusinessProcessException(e);
        } catch (Exception e) {
            throw new BusinessProcessException(e);
        }
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
        return "Data Trasporto";
    }

    public boolean isDettaglioModificabile() {
        Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) getDettaglio().getModel();
        if (dett != null && dett.getStatoTrasporto() != null) {
            return Doc_trasporto_rientro_dettBulk.STATO_PREDISPOSTO_FIRMA.equals(dett.getStatoTrasporto()) ||
                    Doc_trasporto_rientro_dettBulk.STATO_ANNULLATO.equals(dett.getStatoTrasporto());
        }
        return true;
    }

    public boolean isConfermabile() {
        Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) getModel();
        if (documento != null && documento.getDoc_trasporto_rientro_dettColl() != null) {
            return documento.getDoc_trasporto_rientro_dettColl().size() > 0;
        }
        return false;
    }
}