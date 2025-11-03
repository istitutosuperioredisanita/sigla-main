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

import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bp.CRUDRientroBeniInvBP;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.action.*;

import java.rmi.RemoteException;

/**
 * Action per la gestione dei documenti di rientro beni inventariali
 * Si occupa di gestire le richieste provenienti dalle pagine dedicate al Rientro
 * dei beni trasportati
 */
public class CRUDRientroDocAction extends it.cnr.jada.util.action.CRUDAction {

    /**
     * Costruttore di default
     */
    public CRUDRientroDocAction() {
        super();
    }

    /**
     * Gestisce un comando di cancellazione
     */
    public Forward doElimina(ActionContext context) throws java.rmi.RemoteException {
        try {
            fillModel(context);

            CRUDBP bp = getBusinessProcess(context);
            if (!bp.isEditing()) {
                bp.setMessage("Non è possibile cancellare in questo momento");
            } else {
                bp.delete(context);
                bp.reset(context);
                bp.setMessage("Cancellazione effettuata");
            }
            return doConfermaNuovo(context, OptionBP.YES_BUTTON);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doSalva(ActionContext actioncontext) throws RemoteException {
        return super.doSalva(actioncontext);
    }

    /**
     * Gestisce la selezione del tipo di movimento rientro
     */
    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
            Tipo_trasporto_rientroBulk tipoMovimentoOld = documento.getTipoMovimento();

            fillModel(context);

            if (tipoMovimentoOld != null && documento.getTipoMovimento() != null) {
                if (bp.isEditing() || bp.getDettaglio().countDetails() != 0) {
                    // Verifica se il tipo documento è cambiato (non dovrebbe mai cambiare da RIENTRO)
                    if (!documento.getTipoMovimento().isRientro()) {
                        documento.setTipoMovimento(tipoMovimentoOld);
                        throw new ApplicationException("Attenzione: il tipo movimento deve essere di tipo Rientro");
                    }

                    // Verifica se ci sono dettagli già inseriti
                    if (documento.getDoc_trasporto_rientro_dettColl() != null &&
                            documento.getDoc_trasporto_rientro_dettColl().size() > 0) {
                        OptionBP optionBP = openConfirm(context,
                                "Attenzione: tutti i dettagli inseriti verranno persi. Vuoi continuare?",
                                OptionBP.CONFIRM_YES_NO, "doConfirmTipoMovimento");
                        optionBP.addAttribute("documentoRientro", documento);
                        optionBP.addAttribute("tipoMovimento", tipoMovimentoOld);
                        return optionBP;
                    }
                }
            }
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Conferma il cambio di tipo movimento
     */
    public Forward doConfirmTipoMovimento(ActionContext context, OptionBP optionBP)
            throws it.cnr.jada.comp.ApplicationException {
        try {
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) optionBP.getAttribute("documentoRientro");

            if (optionBP.getOption() == OptionBP.YES_BUTTON) {
                documento.setDoc_trasporto_rientro_dettColl(new it.cnr.jada.bulk.SimpleBulkList());
                documento.setAccessoriContestualiHash(new PrimaryKeyHashtable());
                bp.getDettaglio().setModelIndex(context, -1);
            } else {
                documento.setTipoMovimento((Tipo_trasporto_rientroBulk) optionBP.getAttribute("tipoMovimento"));
            }
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

//    /**
//     * Gestisce l'aggiunta di beni al documento di rientro
//     * Permette di selezionare i beni da far rientrare
//     */
//    public Forward doAddToCRUDMain_Dettaglio(ActionContext context) {
//        try {
//            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
//
//            // Controlla che sia stato specificato un Tipo Movimento
//            if (documento.getTipoMovimento() == null) {
//                return handleException(context,
//                        new it.cnr.jada.bulk.ValidationException(
//                                "Attenzione: specificare un tipo di movimento nella testata"));
//            }
//
//            // Controlla che la data di registrazione sia stata specificata
//            if (documento.getDataRegistrazione() == null) {
//                return handleException(context,
//                        new it.cnr.jada.bulk.ValidationException(
//                                "Attenzione: specificare la data di rientro nella testata"));
//            }
//
//            // Apre una ricerca guidata sui beni trasportati disponibili per il rientro
//            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
//            rlbp.setCanPerformSearchWithoutClauses(false);
//            rlbp.setSearchProvider(bp.getBeneSearchProvider(context));
//            rlbp.setPrototype(new Inventario_beniBulk());
//            rlbp.setMultiSelection(true);
//            context.addHookForward("seleziona", this, "doBringBackAddToBeniDaRientrare");
//            return context.addBusinessProcess(rlbp);
//
//        } catch (Throwable e) {
//            return handleException(context, e);
//        }
//    }

    /**
     * Gestisce il ritorno dalla selezione dei beni da far rientrare
     */
    public Forward doBringBackAddToBeniDaRientrare(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            java.util.List selectedModels = (java.util.List) fwd.getParameter("selectedElements");

            if (selectedModels != null && !selectedModels.isEmpty()) {
                CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
                bp.aggiungiDettagliRientro(context.getUserContext(), selectedModels);
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

//    /**
//     * Importa tutti i beni da un documento di trasporto selezionato
//     */
//    public Forward doImportaDaTrasporto(ActionContext context) {
//        try {
//            fillModel(context);
//            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
//
//            // Verifica che sia stato specificato un tipo movimento
//            if (documento.getTipoMovimento() == null) {
//                return handleException(context,
//                        new it.cnr.jada.bulk.ValidationException(
//                                "Attenzione: specificare un tipo di movimento nella testata"));
//            }
//
//            // Apre una ricerca sui documenti di trasporto disponibili
//            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
//            rlbp.setCanPerformSearchWithoutClauses(false);
//            rlbp.setSearchProvider(bp.getTrasportoSearchProvider(context));
//            rlbp.setPrototype(new Doc_trasporto_rientroBulk());
//            rlbp.setMultiSelection(false);
//            context.addHookForward("seleziona", this, "doBringBackTrasportoSelezionato");
//            return context.addBusinessProcess(rlbp);
//
//        } catch (Throwable e) {
//            return handleException(context, e);
//        }
//    }

    /**
     * Gestisce il ritorno dalla selezione del trasporto
     */
    public Forward doBringBackTrasportoSelezionato(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            java.util.List selectedModels = (java.util.List) fwd.getParameter("selectedElements");

            if (selectedModels != null && !selectedModels.isEmpty()) {
                CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
                Doc_trasporto_rientroBulk docTrasporto = (Doc_trasporto_rientroBulk) selectedModels.get(0);
                bp.aggiungiDettagliDaTrasporto(context.getUserContext(), docTrasporto);
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il risultato di una ricerca sulla Categoria Gruppo del Bene
     */
    public Forward doBringBackSearchFind_categoria_bene(ActionContext context,
                                                        Doc_trasporto_rientro_dettBulk dettaglio,
                                                        Categoria_gruppo_inventBulk cat_gruppo) {
        try {
            fillModel(context);
            if (cat_gruppo != null) {
                CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);

                dettaglio.getBene().setCategoria_Bene(cat_gruppo);

                // Verifica se è una pubblicazione
                String cd_pubblicazioni = ((it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession)
                        it.cnr.jada.util.ejb.EJBCommonServices.createEJB(
                                "CNRCONFIG00_EJB_Configurazione_cnrComponentSession",
                                it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession.class))
                        .getVal01(context.getUserContext(), new Integer(0), "*",
                                "CD_CATEGORIA_GRUPPO_SPECIALE", "PUBBLICAZIONI");

                if (cd_pubblicazioni != null) {
                    dettaglio.getBene().setPubblicazione(
                            cat_gruppo.getCd_categoria_padre().equalsIgnoreCase(cd_pubblicazioni));
                }

                if (!dettaglio.getBene().isPubblicazione()) {
                    dettaglio.getBene().setCollocazione(null);
                }
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce l'azzeramento del searchtool per la Categoria Gruppo
     */
    public Forward doBlankSearchFind_categoria_bene(ActionContext context,
                                                    Doc_trasporto_rientro_dettBulk dettaglio)
            throws java.rmi.RemoteException {
        try {
            dettaglio.getBene().setCategoria_Bene(
                    new it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk());
            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la selezione del flag "Bene Accessorio"
     */
    public Forward doSelezionaBeneAccessorio(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
            fillModel(context);

            // Verifiche preliminari
            if (riga.getBene().getCd_categoria_gruppo() == null) {
                riga.setFl_bene_accessorio(Boolean.FALSE);
                throw new it.cnr.jada.comp.ApplicationException("Bisogna valorizzare prima la categoria");
            }

            if (riga.isAssociatoConAccessorioContestuale()) {
                riga.setFl_bene_accessorio(Boolean.FALSE);
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione. Questo bene non può essere accessorio poichè ha dei beni associati ad esso");
            }

            // Se il bene ha un bene principale associato, utilizziamo la sua etichetta
            if (riga.getFl_bene_accessorio() != null && riga.getFl_bene_accessorio().booleanValue()) {
                if (riga.getBene().getBene_principale() != null &&
                        riga.getBene().getBene_principale().getEtichetta() != null) {
                    riga.getBene().setEtichetta(riga.getBene().getBene_principale().getEtichetta());
                } else {
                    riga.getBene().setEtichetta(null);
                }
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

//    /**
//     * Gestisce la deselezione del flag "Bene Accessorio"
//     */
//    public Forward doDeselezionaBeneAccessorio(ActionContext context) {
//        try {
//            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//            Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
//            fillModel(context);
//
//            if (riga.isAccessorioContestuale()) {
//                doDeselezionaAccessoriContestuali(context);
//                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
//                documento.removeFromAccessoriContestualiHash(riga);
//            }
//
//            riga.getBene().setBene_principale(null);
//            riga.getBene().setAssegnatario(null);
//            riga.getBene().setUbicazione(null);
//            riga.setFl_accessorio_contestuale(Boolean.FALSE);
//            riga.getBene().setEtichetta(null);
//
//            return context.findDefaultForward();
//        } catch (Throwable e) {
//            return handleException(context, e);
//        }
//    }

    /**
     * Trova gli accessori contestuali disponibili
     */
    public Forward doFindAccessoriContestuali(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientro_dettBulk riga_da_associare =
                    (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
            fillModel(context);

            if (riga_da_associare.getBene().getCd_categoria_gruppo() == null) {
                throw new it.cnr.jada.comp.ApplicationException("Bisogna valorizzare prima la categoria");
            }

            java.util.Vector selectedModels = new java.util.Vector();
            for (java.util.Enumeration e = bp.getDettaglio().getElements(); e.hasMoreElements();) {
                Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) e.nextElement();

                if (!riga.getFl_accessorio_contestuale() &&
                        (riga_da_associare.getBene().getCd_categoria_gruppo()
                                .compareTo(riga.getBene().getCd_categoria_gruppo()) == 0)) {
                    if ((!riga.isBeneAccessorio()) &&
                            (riga.getQuantita() != null) &&
                            (riga.getQuantita().compareTo(new Long(1)) == 0)) {
                        selectedModels.add(riga);
                    }
                }
            }

            if (selectedModels.size() == 0) {
                riga_da_associare.setFl_accessorio_contestuale(Boolean.FALSE);
                throw new it.cnr.jada.comp.ApplicationException(
                        "Non ci sono elementi validi da associare come Bene Principale");
            }

            it.cnr.jada.util.action.SelezionatoreListaBP slbp =
                    (it.cnr.jada.util.action.SelezionatoreListaBP) select(
                            context,
                            new it.cnr.jada.util.ListRemoteIterator(selectedModels),
                            it.cnr.jada.bulk.BulkInfo.getBulkInfo(Doc_trasporto_rientro_dettBulk.class),
                            "righeSet",
                            "doBringBackAccessoriContestuali");
            slbp.setMultiSelection(false);
            return slbp;
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

//    /**
//     * Gestisce il ritorno dalla selezione dell'accessorio contestuale
//     */
//    public Forward doBringBackAccessoriContestuali(ActionContext context) {
//        try {
//            HookForward caller = (HookForward) context.getCaller();
//            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
//            Doc_trasporto_rientro_dettBulk selezionato =
//                    (Doc_trasporto_rientro_dettBulk) caller.getParameter("focusedElement");
//            Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
//            java.util.List selectedModels = (java.util.List) caller.getParameter("selectedElements");
//
//            if (selectedModels != null && !selectedModels.isEmpty()) {
//                riga.getBene().setBene_principale(new Inventario_beniBulk());
//                riga.getBene().getBene_principale().setDs_bene(selezionato.getBene().getDs_bene());
//
//                // Copia l'etichetta dal bene principale
//                if (selezionato.getBene().getEtichetta() != null) {
//                    riga.getBene().getBene_principale().setEtichetta(selezionato.getBene().getEtichetta());
//                    riga.getBene().setEtichetta(selezionato.getBene().getEtichetta());
//                }
//
//                riga.getBene().setUbicazione(selezionato.getBene().getUbicazione());
//                riga.getBene().setAssegnatario(selezionato.getBene().getAssegnatario());
//                riga.setFl_bene_accessorio(Boolean.TRUE);
//                riga.setFl_accessorio_contestuale(Boolean.TRUE);
//                bp.setProgressivo_beni(documento.addToAccessoriContestualiHash(selezionato, riga,
//                        bp.getProgressivo_beni()));
//            } else {
//                riga.setFl_accessorio_contestuale(Boolean.FALSE);
//            }
//
//            return context.findDefaultForward();
//        } catch (Throwable e) {
//            return handleException(context, e);
//        }
//    }

//    /**
//     * Deseleziona gli accessori contestuali
//     */
//    public Forward doDeselezionaAccessoriContestuali(ActionContext context) {
//        try {
//            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//            fillModel(context);
//            Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
//            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
//
//            riga.getBene().setBene_principale(null);
//            riga.getBene().setAssegnatario(null);
//            riga.getBene().setUbicazione(null);
//            riga.setFl_accessorio_contestuale(Boolean.FALSE);
//            riga.getBene().setEtichetta(null);
//
//            documento.removeFromAccessoriContestualiHash(riga);
//
//            return context.findDefaultForward();
//        } catch (Throwable e) {
//            return handleException(context, e);
//        }
//    }

    /**
     * Apre la ricerca libera per il bene principale
     */
    public Forward doFreeSearchFind_bene_principale(ActionContext context) {
        try {
            BulkBP bp = (BulkBP) context.getBusinessProcess();
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();
            bp.fillModel(context);
            FormField field = getFormField(context, "main.Dettaglio.find_bene_principale");
            Inventario_beniBulk bene_principale = new Inventario_beniBulk();
            bene_principale.setCondizioni(documento.getCondizioni());
            return freeSearch(context, field, bene_principale);
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il ritorno dalla ricerca del bene principale
     */
    public Forward doBringBackSearchFind_bene_principale(
            ActionContext context,
            Doc_trasporto_rientro_dettBulk dettaglio_accessorio,
            Inventario_beniBulk selezionato) {
        try {
            if (selezionato != null) {
                dettaglio_accessorio.getBene().setBene_principale(selezionato);
                dettaglio_accessorio.getBene().setAssegnatario(selezionato.getAssegnatario());
                dettaglio_accessorio.getBene().setUbicazione(selezionato.getUbicazione());

                // Imposta l'etichetta del bene accessorio con quella del bene principale
                if (selezionato.getEtichetta() != null) {
                    dettaglio_accessorio.getBene().setEtichetta(selezionato.getEtichetta());
                }
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

//    /**
//     * Gestisce l'eliminazione di un bene dal documento di rientro
//     */
//    public Forward doRemoveFromCRUDMain_Dettaglio(ActionContext context)
//            throws java.rmi.RemoteException, BusinessProcessException {
//
//        CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
//        Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();
//
//        if (riga != null) {
//            // Il bene è un bene padre di beni segnalati come accessori
//            if (riga.isAssociatoConAccessorioContestuale()) {
//                return handleException(context,
//                        new it.cnr.jada.comp.ApplicationException(
//                                "Il bene selezionato non può essere cancellato, poichè associato ad altri beni"));
//            }
//
//            try {
//                bp.getDettaglio().remove(context);
//            } catch (it.cnr.jada.bulk.ValidationException vge) {
//            }
//
//            if (riga.isAccessorioContestuale()) {
//                Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) riga.getDoc_trasporto_rientro();
//                documento.removeFromAccessoriContestualiHash(riga);
//            }
//        }
//        return context.findDefaultForward();
//    }

    /**
     * Gestisce la modifica della data di registrazione
     * Rimuove tutti i dettagli quando cambia la data
     */
    public Forward doOnData_registrazioneChange(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) context.getBusinessProcess();
            Doc_trasporto_rientroBulk model = (Doc_trasporto_rientroBulk) bp.getModel();

            // Rimuove i dettagli se la data cambia
            bp.getDettaglio().removeAll(context);

            fillModel(context);
            bp.setModel(context, model);
            return context.findDefaultForward();
        } catch (Throwable t) {
            return handleException(context, t);
        }
    }

    /**
     * Calcola e aggiorna la quantità per il dettaglio
     */
    public Forward doCalcolaQuantita(ActionContext context) {
        CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
        Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();

        Long qta = riga.getQuantita();

        try {
            fillModel(context);

            // Se la riga è per un bene accessorio, controlla che la Quantità indicata NON sia superiore a 999
            if (riga.isBeneAccessorio() && riga.getQuantita() != null &&
                    riga.getQuantita().compareTo(new Long(999)) > 0) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: non è possibile indicare una Quantità maggiore di 999 per un Bene Accessorio.");
            }

            //TODO controlla e riscrivi logica senza campi _rif
//            // Verifica che la quantità di rientro non sia superiore alla quantità trasportata
//            if (riga.getDocTrasportoRientroDettRif() != null &&
//                    riga.getQuantita() != null &&
//                    riga.getQuantita().compareTo(riga.getDocTrasportoRientroDettRif().getQuantita()) > 0) {
//                throw new it.cnr.jada.comp.ApplicationException(
//                        "Attenzione: la quantità di rientro non può essere superiore alla quantità trasportata (" +
//                                riga.getDocTrasportoRientroDettRif().getQuantita() + ")");
//            }

        } catch (Throwable e) {
            riga.setQuantita(qta);
            return handleException(context, e);
        }
        return context.findDefaultForward();
    }

    /**
     * Verifica la validità del riferimento al trasporto
     */
    public Forward doVerificaRiferimentoTrasporto(ActionContext context) {
        try {
            fillModel(context);
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk) bp.getDettaglio().getModel();

//            if (!riga.isValidoPerRientro()) {
//                throw new it.cnr.jada.comp.ApplicationException(
//                        "Attenzione: il dettaglio non ha un riferimento valido al documento di trasporto. " +
//                                "Verificare che il bene sia stato effettivamente trasportato.");
//            }

            bp.setMessage("Riferimento al trasporto valido");
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Azzera il searchtool per la ricerca del bene principale
     */
    public Forward doBlankSearchFind_bene_principale(ActionContext context,
                                                     Doc_trasporto_rientro_dettBulk dettaglio)
            throws java.rmi.RemoteException {
        try {
            dettaglio.getBene().setBene_principale(null);
            dettaglio.getBene().setEtichetta(null);
            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    /**
     * Override del metodo blankSearch per gestire l'azzeramento dell'etichetta
     */
    protected void blankSearch(ActionContext actioncontext, FormField formfield, OggettoBulk oggettobulk) {
        try {
            OggettoBulk oggettobulk1 = formfield.getModel();
            if (oggettobulk1 != null) {
                // Verifica se il modello è un Doc_trasporto_rientro_dettBulk
                if (oggettobulk1 instanceof Doc_trasporto_rientro_dettBulk) {
                    Doc_trasporto_rientro_dettBulk dettaglio = (Doc_trasporto_rientro_dettBulk) oggettobulk1;

                    // Azzera l'etichetta se il bene esiste
                    if (dettaglio.getBene() != null) {
                        dettaglio.getBene().setEtichetta(null);
                    }
                }

                // Imposta il valore del campo
                formfield.getField().setValueIn(oggettobulk1, oggettobulk);
                formfield.getFormController().setDirty(true);
            }
        } catch (Exception exception) {
            throw new ActionPerformingError(exception);
        }
    }

    /**
     * Rientra tutti i beni disponibili dal trasporto selezionato
     */
    public Forward doRientraTutti(ActionContext context) {
        try {
            fillModel(context);
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();

            if (documento.getTipoMovimento() == null) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: specificare un tipo di movimento nella testata");
            }

            if (documento.getDataRegistrazione() == null) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: specificare la data di rientro nella testata");
            }

            // Verifica che ci sia almeno un riferimento al trasporto
            if (!bp.hasTrasportoSelezionato()) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: non è stato selezionato alcun documento di trasporto. " +
                                "Utilizzare 'Importa da Trasporto' per selezionare i beni da far rientrare.");
            }

            bp.setMessage("Tutti i beni disponibili sono stati aggiunti al rientro");
            bp.getDettaglio().reset(context);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Conferma il documento di rientro
     */
    public Forward doConfermaRientro(ActionContext context) {
        try {
            fillModel(context);
            CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk documento = (Doc_trasporto_rientroBulk) bp.getModel();

            // Validazioni finali
            if (!bp.isConfermabile()) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: il documento non può essere confermato. Verificare che siano presenti dei dettagli.");
            }

            if (!bp.hasTuttiRiferimentiValidi()) {
                throw new it.cnr.jada.comp.ApplicationException(
                        "Attenzione: uno o più dettagli non hanno un riferimento valido al documento di trasporto. " +
                                "Verificare tutti i riferimenti prima di confermare.");
            }

            // Procede con il salvataggio
            return doSalva(context);

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }
}