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

package it.cnr.contab.inventario01.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bp.CRUDTrasportoBeniInvBP;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.action.*;

/**
 * Action per la gestione dei documenti di trasporto beni inventariali
 */
public class CRUDTrasportoDocAction extends it.cnr.jada.util.action.CRUDAction {

    /**
     * Costruttore
     */
    public CRUDTrasportoDocAction() {
        super();
    }

    /**
     * Gestisce la selezione del tipo di trasporto/rientro
     * Controlla che il cambio di tipologia sia possibile verificando:
     * - Se il documento è in editing o ha già dettagli
     * - Se il documento è già associato o contabilizzato
     * - Se il tipo documento (T/R) è cambiato
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doSelezionaTipoTrasportoRientro(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();
            Tipo_trasporto_rientroBulk tipoMovimentoOld = documento.getTipoMovimento();

            fillModel(context);

            if (tipoMovimentoOld != null && documento.getTipoMovimento() != null) {
                if (bp.isEditing() || bp.getDettaglio().countDetails() != 0) {
                    // Verifica se il tipo documento (T/R) è cambiato
                    if (!documento.getTipoMovimento().getTiDocumento().equals(tipoMovimentoOld.getTiDocumento())) {
                        documento.setTipoMovimento(tipoMovimentoOld);
                        throw new ApplicationException("Cambio tipologia documento non possibile.");
                    }
                }
            }
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce l'aggiunta di beni al documento di trasporto
     * Verifica che:
     * - Sia stato specificato un tipo di trasporto
     * - Sia stata specificata la data di registrazione
     * - Sia stata specificata la destinazione
     * Propone all'utente una ricerca guidata sui beni disponibili
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doAddToCRUDMain_Dettaglio(ActionContext context) {
        try {
            fillModel(context);
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();

            // Validazioni
            if (documento.getTipoMovimento() == null) {
                return handleException(context,
                        new it.cnr.jada.bulk.ValidationException(
                                "Attenzione: specificare un tipo di trasporto nella testata"));
            }

            if (documento.getDataRegistrazione() == null) {
                return handleException(context,
                        new it.cnr.jada.bulk.ValidationException(
                                "Attenzione: specificare la data di registrazione"));
            }

            // Per i trasporti, la destinazione è obbligatoria
            if (documento.getTipoMovimento().isTrasporto() &&
                    (documento.getDestinazione() == null || documento.getDestinazione().trim().isEmpty())) {
                return handleException(context,
                        new it.cnr.jada.bulk.ValidationException(
                                "Attenzione: specificare la destinazione per il trasporto"));
            }

            bp.getDettaglio().validate(context);

            // Recupera i beni già selezionati
            SimpleBulkList selezionati = documento.getDoc_trasporto_rientro_dettColl() != null ?
                    documento.getDoc_trasporto_rientro_dettColl() : new SimpleBulkList();

//            // Cerca i beni disponibili per il trasporto
            //TODO da verificare e in caso implementare
//            it.cnr.jada.util.RemoteIterator ri = ((DocTrasportoRientroComponentSession)bp.createComponentSession())
//                    .cercaBeniTrasportabili(context.getUserContext(), documento, selezionati, null);
//
//            ri = it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(context, ri);
//            int count = ri.countElements();
//
//            if (count == 0) {
//                bp.setMessage("Nessun Bene recuperato");
//                it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(context, ri);
//            } else {
//                RicercaLiberaBP rlbp = (RicercaLiberaBP)context.createBusinessProcess("RicercaLibera");
//                rlbp.setCanPerformSearchWithoutClauses(false);
//                rlbp.setPrototype(new Inventario_beniBulk());
//                context.addHookForward("searchResult", this, "doBringBackAddBeni");
//                context.addHookForward("filter", this, "doBringBackAddBeni");
//                return context.addBusinessProcess(rlbp);
//            }
            return context.findDefaultForward();

        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il ritorno dalla ricerca dei beni
     * Applica i filtri di ricerca e propone la selezione dei beni
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doBringBackAddBeni(ActionContext context) {
        try {
            HookForward fwd = (HookForward)context.getCaller();
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();

            it.cnr.jada.persistency.sql.CompoundFindClause clauses =
                    (it.cnr.jada.persistency.sql.CompoundFindClause)fwd.getParameter("filter");

            context.addHookForward("filter", this, "doSelezionaBeni");

            SimpleBulkList selezionati = documento.getDoc_trasporto_rientro_dettColl() != null ?
                    documento.getDoc_trasporto_rientro_dettColl() : new SimpleBulkList();

            it.cnr.jada.util.RemoteIterator iterator = bp.getListaBeniTrasportabili(
                    context.getUserContext(), selezionati, clauses);

            if (iterator != null) {
                iterator = it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(context, iterator);
                int count = iterator.countElements();

                if (count == 0) {
                    bp.setMessage("Nessun Bene associabile");
                    it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(context, iterator);
                } else {
                    SelezionatoreListaBP slbp = select(context, iterator,
                            it.cnr.jada.bulk.BulkInfo.getBulkInfo(Inventario_beniBulk.class),
                            null, "doSelezionaBeni", null, bp);
                    slbp.setMultiSelection(true);
                    return slbp;
                }
            } else {
                bp.setMessage("Funzionalità non ancora implementata");
            }
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la conferma della selezione dei beni
     * Aggiunge i beni selezionati al documento di trasporto
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doSelezionaBeni(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)context.getBusinessProcess();

            // Recupera i beni selezionati dal selezionatore
            HookForward hookForward = (HookForward)context.getCaller();
            java.util.List beniSelezionati = (java.util.List)hookForward.getParameter("beniSelezionati");

            if (beniSelezionati != null && !beniSelezionati.isEmpty()) {
                // Aggiungi i beni al documento tramite il BP
                bp.aggiungiDettagliTrasporto(context.getUserContext(), beniSelezionati);
            }

            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch(Exception e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la selezione di un dettaglio nel controller
     * Imposta la selezione e gestisce il comportamento automatico
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doSelectDettaglio(ActionContext context) {
        CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)context.getBusinessProcess();
        try {
            bp.getDettaglio().setSelection(context);

            if (Optional.ofNullable(bp.getModel()).isPresent()) {
                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();
                Doc_trasporto_rientro_dettBulk dettaglio =
                        (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();

                if (dettaglio != null && dettaglio.getBene() != null) {
                    // Gestione automatica per beni con accessori
                    if (dettaglio.isAssociatoConAccessorioContestuale()) {
                        return impostaAccessoriContestuali(context, bp, dettaglio);
                    }
                }
            }
        } catch (Throwable e) {
            return handleException(context, e);
        }
        return context.findDefaultForward();
    }

    /**
     * Imposta gli accessori contestuali per un bene principale
     * Gestisce la sincronizzazione delle proprietà tra bene principale e accessori
     *
     * @param context il contesto dell'azione
     * @param bp il business process
     * @param dettaglio il dettaglio del documento
     * @return Forward
     */
    public Forward impostaAccessoriContestuali(ActionContext context, CRUDTrasportoBeniInvBP bp,
                                               Doc_trasporto_rientro_dettBulk dettaglio)
            throws it.cnr.jada.comp.ApplicationException {
        try {
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();

            if (documento.getAccessoriContestualiHash() != null) {
                BulkList accessori = (BulkList)documento.getAccessoriContestualiHash()
                        .get(dettaglio.getChiaveHash());

                if (accessori != null && !accessori.isEmpty()) {
                    // Sincronizza le proprietà del bene principale con gli accessori
                    for (Iterator i = accessori.iterator(); i.hasNext();) {
                        Doc_trasporto_rientro_dettBulk accessorio =
                                (Doc_trasporto_rientro_dettBulk)i.next();
                        accessorio.getBene().setUbicazione(dettaglio.getBene().getUbicazione());
                        accessorio.getBene().setCategoria_Bene(dettaglio.getBene().getCategoria_Bene());
                    }
                }
            }

            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il click sul flag di trasporto totale/scarico totale
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doClickFlagTrasportoTotale(ActionContext context)
            throws it.cnr.jada.comp.ApplicationException {

        CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
        Doc_trasporto_rientro_dettBulk dettaglio =
                (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();

        try {
            fillModel(context);

            Inventario_beniBulk bene = dettaglio.getBene();

            if (bene.getFl_totalmente_scaricato() != null &&
                    bene.getFl_totalmente_scaricato().booleanValue()) {

                // Verifica se ci sono accessori da gestire
                if (dettaglio.isAssociatoConAccessorioContestuale()) {
                    OptionBP optionBP = openConfirm(context,
                            "Attenzione: il bene selezionato ha degli accessori. Verranno trasportati totalmente. Vuoi continuare?",
                            OptionBP.CONFIRM_YES_NO, "doConfirmTrasportoTotaleBene");
                    optionBP.addAttribute("DettaglioTrasporto", dettaglio);
                    return optionBP;
                }
            }

            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Conferma il trasporto totale del bene
     *
     * @param context il contesto dell'azione
     * @param optionBP l'option BP con i parametri
     * @return Forward
     */
    public Forward doConfirmTrasportoTotaleBene(ActionContext context, OptionBP optionBP)
            throws it.cnr.jada.comp.ApplicationException, BusinessProcessException {

        CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
        Doc_trasporto_rientro_dettBulk dettaglio =
                (Doc_trasporto_rientro_dettBulk)optionBP.getAttribute("DettaglioTrasporto");

        if (optionBP.getOption() == OptionBP.YES_BUTTON) {
            // Procedi con il trasporto totale
            bp.getDettaglio().reset(context);
        } else {
            // Annulla l'operazione
            dettaglio.getBene().setFl_totalmente_scaricato(Boolean.FALSE);
        }
        return context.findDefaultForward();
    }

    /**
     * Gestisce la modifica della data di registrazione
     * Rimuove tutti i dettagli esistenti quando cambia la data
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doOnData_registrazioneChange(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)context.getBusinessProcess();
            bp.getDettaglio().removeAll(context);
            fillModel(context);

            Doc_trasporto_rientroBulk model = (Doc_trasporto_rientroBulk)bp.getModel();
            bp.setModel(context, model);
            return context.findDefaultForward();
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }

    /**
     * Annulla il documento e riporta le associazioni
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doAnnullaRiporta(ActionContext context) throws BusinessProcessException {
        try {
            fillModel(context);
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)context.getBusinessProcess();
            List dettagli = bp.getDettaglio().getDetails();
            //TODO da verificare e in caso implementare
//            ((DocTrasportoRientroComponentSession)bp.createComponentSession())
//                    .annullaRiportaTrasporto(context.getUserContext(), bp.getModel(), dettagli);

            context.closeBusinessProcess();
            HookForward bringBackForward = (HookForward)context.findForward("bringback");
            if (bringBackForward != null)
                return bringBackForward;
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Assegna la quantità al dettaglio selezionato
     * Verifica che la quantità sia valida
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doAssegnaQuantita(ActionContext context)
            throws it.cnr.jada.comp.ApplicationException {

        CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
        Doc_trasporto_rientro_dettBulk dettaglio =
                (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();

        try {
            fillModel(context);

            // Validazione quantità
            if (dettaglio.getQuantita() == null || dettaglio.getQuantita() <= 0) {
                throw new ApplicationException(
                        "Attenzione: specificare una quantità valida (maggiore di 0)");
            }

            // Per beni con accessori contestuali, la quantità deve essere 1
            if (dettaglio.isAssociatoConAccessorioContestuale() &&
                    dettaglio.getQuantita().compareTo(new Long("1")) != 0) {
                dettaglio.setQuantita(new Long("1"));
                throw new ApplicationException(
                        "Attenzione: la quantità di questa riga deve essere 1, poiché alcuni beni sono suoi accessori");
            }

            bp.getDettaglio().reset(context);

        } catch (Throwable e) {
            return handleException(context, e);
        }

        return context.findDefaultForward();
    }

    /**
     * Trasporta tutti i beni disponibili in un'unica operazione
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doTrasportaTutti(ActionContext context) {
        try {
            fillModel(context);
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk)bp.getModel();

            // Verifica che ci siano dettagli da trasportare
            if (documento.getDoc_trasporto_rientro_dettColl() == null ||
                    documento.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                throw new ApplicationException("Attenzione: non ci sono beni da trasportare");
            }

            // Imposta tutti i dettagli come trasportati
            for (Iterator i = documento.getDoc_trasporto_rientro_dettColl().iterator(); i.hasNext();) {
                Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk)i.next();
                if (dett.isValidoPerTrasporto()) {
                    dett.setStatoTrasporto(Doc_trasporto_rientro_dettBulk.STATO_DEFINITIVO);
                }
            }

            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch(Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il cambio dello stato trasporto per un dettaglio
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doOnStatoTrasportoChange(ActionContext context) {
        try {
            fillModel(context);
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)context.getBusinessProcess();
            Doc_trasporto_rientro_dettBulk dettaglio =
                    (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();

            // Verifica che il cambio di stato sia consentito
            if (!bp.isDettaglioModificabile()) {
                throw new ApplicationException(
                        "Attenzione: impossibile modificare lo stato per questo dettaglio");
            }

            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la selezione di un bene principale per un accessorio
     *
     * @param context il contesto dell'azione
     * @return Forward
     */
    public Forward doSelezionaBenePrincipale(ActionContext context) {
        try {
            fillModel(context);
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)getBusinessProcess(context);
            Doc_trasporto_rientro_dettBulk dettaglio =
                    (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();

            // Se è un bene accessorio, verifica che il bene principale sia valido
            if (dettaglio.isBeneAccessorio()) {
                Inventario_beniBulk benePrincipale = dettaglio.getBene().getBene_principale();
                if (benePrincipale != null && benePrincipale.getFl_totalmente_scaricato() != null &&
                        benePrincipale.getFl_totalmente_scaricato().booleanValue()) {
                    throw new ApplicationException(
                            "Attenzione: il bene principale selezionato è stato totalmente scaricato");
                }
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}