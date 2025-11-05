package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.*;
import it.cnr.jada.util.ejb.EJBCommonServices;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.Objects;

/**
 * Action BASE per la gestione del flusso di Trasporto/Rientro Beni.
 * <p>
 * Contiene TUTTA la logica comune:
 * - Cambio testata (tipo movimento, tipo ritiro, dipendente, data)
 * - Aggiunta/Selezione beni con gestione accessori (flusso ricorsivo unificato)
 * - Eliminazione beni con gestione accessori (flusso ricorsivo unificato)
 * - Workflow (predisponi alla firma, annullamento)
 * - Helper methods
 * <p>
 * Le classi figlie devono solo implementare:
 * 1. getBP() - cast al tipo specifico
 * 2. getListaBeni() - chiamata specifica (trasportabili vs. rientrare)
 * 3. getSelezionaMethod() - nome metodo callback specifico
 * 4. getBringBackMethod() - nome metodo bring back specifico
 * 5. getMessageNoResults() - messaggio specifico se nessun bene trovato
 * 6. getDataLabel() - label per validazione data
 */
public abstract class CRUDTraspRientDocAction extends it.cnr.jada.util.action.CRUDAction {

    // =======================================================
    // METODI ASTRATTI (da implementare nelle figlie)
    // =======================================================

    protected abstract CRUDTraspRientInventarioBP getBP(ActionContext context);

    protected abstract RemoteIterator getListaBeni(ActionContext context, CRUDTraspRientInventarioBP bp, Doc_trasporto_rientroBulk doc, SimpleBulkList selezionati, CompoundFindClause clauses) throws Exception;

    protected abstract String getSelezionaMethod();

    protected abstract String getBringBackMethod();

    protected abstract String getMessageNoResults();

    protected abstract String getDataLabel();

    // =======================================================
    // GESTIONE CAMBI TESTATA (comune a entrambi)
    // =======================================================

    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk tipoOld = doc.getTipoMovimento();
            fillModel(context);
            if (hasBeniInDettaglio(doc)) {
                doc.setTipoMovimento(tipoOld);
                bp.setMessage("Impossibile cambiare Tipo Movimento: eliminare prima i beni.");
            }
            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnTipoRitiroChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            String oldValue = doc.getTipoRitiro();
            fillModel(context);
            String newValue = doc.getTipoRitiro();
            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }
            eliminaBeniSePresenti(context, bp, doc, "Tipo ritiro modificato. Beni precedenti rimossi.");
            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
                doc.setAnagDipRitiro(null);
            }
            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnDipendenteChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            Integer oldValue = getCdAnag(doc.getAnagDipRitiro());
            fillModel(context);
            Integer newValue = getCdAnag(doc.getAnagDipRitiro());
            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }
            eliminaBeniSePresenti(context, bp, doc, "Dipendente modificato. Beni precedenti rimossi.");
            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnData_registrazioneChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            bp.getDettBeniController().removeAll(context);
            bp.getEditDettController().removeAll(context);
            fillModel(context);
            bp.setModel(context, (Doc_trasporto_rientroBulk) bp.getModel());
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // AGGIUNTA BENI - SELEZIONATORE (comune con hook point)
    // =======================================================

    public Forward doAddToCRUDMain_DettBeniController(ActionContext context) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            validaTestataDocumento(doc);
            bp.getDettBeniController().validate(context);
            SimpleBulkList selezionati = getComponentSession(bp).selezionati(context.getUserContext(), doc);
            RemoteIterator ri = getListaBeni(context, bp, doc, selezionati, null);
            ri = EJBCommonServices.openRemoteIterator(context, ri);
            if (ri.countElements() == 0) {
                bp.setMessage(getMessageNoResults());
                EJBCommonServices.closeRemoteIterator(context, ri);
                return context.findDefaultForward();
            }
            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(false);
            rlbp.setPrototype(new Inventario_beniBulk());
            context.addHookForward("searchResult", this, getBringBackMethod());
            context.addHookForward("filter", this, getBringBackMethod());
            return context.addBusinessProcess(rlbp);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    protected Forward doBringBackGeneric(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            CompoundFindClause clauses = (CompoundFindClause) fwd.getParameter("filter");
            bp.setClauses(clauses);
            context.addHookForward("filter", this, getSelezionaMethod());
            SimpleBulkList selezionati = getComponentSession(bp).selezionati(context.getUserContext(), doc);
            RemoteIterator ri = getListaBeni(context, bp, doc, selezionati, clauses);
            ri = EJBCommonServices.openRemoteIterator(context, ri);
            if (ri.countElements() == 0) {
                bp.setMessage(getMessageNoResults());
                EJBCommonServices.closeRemoteIterator(context, ri);
                return context.findDefaultForward();
            }
            SelezionatoreListaBP slbp = select(context, ri, it.cnr.jada.bulk.BulkInfo.getBulkInfo(Inventario_beniBulk.class), null, getSelezionaMethod(), null, bp);
            slbp.setMultiSelection(true);
            return slbp;
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // ELIMINAZIONE E AGGIUNTA - FLUSSO RICORSIVO UNIFICATO
    // =======================================================

    public Forward doRemoveFromCRUDMain_DettBeniController(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            bp.getDettBeniController().remove(context);
            return bp.hasBeniPrincipaliConAccessoriPerEliminazione() ? apriFlussoRicorsivo(context, bp, true) : context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * METODO CRITICO: Gestisce la selezione dei beni dopo che l'utente li ha selezionati.
     * DEVE:
     * 1. Aggiungere PRIMA i beni semplici (principali senza accessori + accessori singoli)
     * 2. POI avviare il flusso ricorsivo per i principali con accessori
     */
    protected Forward doSelezionaBeniGeneric(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);

            // CONTROLLO CRITICO: Verifica che pendingAdd sia stato inizializzato
            if (bp.getPendingAdd() == null) {
                // Non ci sono beni in pending, semplicemente resetta e ritorna
                bp.getDettBeniController().reset(context);
                return context.findDefaultForward();
            }

            // STEP 1: Aggiungi i beni semplici (principali senza accessori + accessori singoli)
            if (!bp.getPendingAdd().getPrincipaliSenza().isEmpty() || !bp.getPendingAdd().getAccessori().isEmpty()) {
                BitSet tempSel = (BitSet) bp.getPendingAdd().getOldSel().clone();

                // Seleziona tutti i beni semplici
                for (int i = 0; i < bp.getPendingAdd().getBulks().length; i++) {
                    Inventario_beniBulk b = (Inventario_beniBulk) bp.getPendingAdd().getBulks()[i];
                    if (bp.getPendingAdd().getPrincipaliSenza().contains(b) ||
                            bp.getPendingAdd().getAccessori().contains(b)) {
                        tempSel.set(i);
                    }
                }

                // CRITICO: Aggiorna selectionAccumulata per accumulo progressivo
                bp.getPendingAdd().setSelectionAccumulata(tempSel);
            }

            bp.getDettBeniController().reset(context);

            // STEP 2: Se ci sono principali con accessori, avvia il flusso ricorsivo
            if (bp.hasBeniPrincipaliConAccessoriPendenti()) {
                return apriFlussoRicorsivo(context, bp, false);
            }
            // STEP 3: Se NON ci sono principali con accessori, aggiungi i beni semplici ora
            if (!bp.hasBeniPrincipaliConAccessoriPendenti()) {
                if (!bp.getPendingAdd().getPrincipaliSenza().isEmpty() || !bp.getPendingAdd().getAccessori().isEmpty()) {
                    bp.modificaBeniConAccessoriComponente(context, bp.getPendingAdd().getBulks(),
                            bp.getPendingAdd().getOldSel(), bp.getPendingAdd().getSelectionAccumulata());
                    bp.getDettBeniController().reset(context);
                }
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    private Forward apriFlussoRicorsivo(ActionContext context, CRUDTraspRientInventarioBP bp, boolean isEliminazione) {
        bp.setIndexBeneCurrentePerEliminazione(0);
        bp.setIndexBeneCurrentePerAggiunta(0);
        bp.setUltimaOperazioneEliminazione(isEliminazione);
        String messaggio = String.format("[Bene %d di %d]\n\n%s",
                bp.getIndexBeneCorrente(isEliminazione),
                bp.getTotaleBeniPrincipali(isEliminazione),
                bp.getMessaggioSingoloBene(isEliminazione));
        try {
            // Per eliminazione: usa 3 pulsanti (SI/NO/ANNULLA)
            // Per aggiunta: usa 2 pulsanti (SI/NO)
            int confirmType = isEliminazione ? OptionBP.CONFIRM_YES_NO_CANCEL : OptionBP.CONFIRM_YES_NO;
            openConfirm(context, messaggio, confirmType, "doRispostaRicorsiva");
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
        return context.findDefaultForward();
    }

    /**
     * METODO CRITICO: Gestisce la risposta dell'utente per ogni bene principale con accessori.
     * DEVE:
     * - Se YES: aggiungere bene principale CON accessori
     * - Se NO per AGGIUNTA: aggiungere solo bene principale SENZA accessori
     * - Se NO per ELIMINAZIONE: saltare il bene (non eliminare nulla)
     * - Continuare con il prossimo bene o finalizzare
     */
    public Forward doRispostaRicorsiva(ActionContext context, int opt) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            boolean isEliminazione = bp.isUltimaOperazioneEliminazione();

            // Gestione dei 3 pulsanti
            boolean haCliccatoSI = (opt == OptionBP.YES_BUTTON);
            boolean haCliccatoNO = (opt == OptionBP.NO_BUTTON);
            boolean haCliccatoANNULLA = (opt == OptionBP.CANCEL_BUTTON);

            // Se ha cliccato ANNULLA, interrompi tutto
            if (haCliccatoANNULLA) {
                bp.getDettBeniController().reset(context);
                bp.setMessage("Operazione annullata.");
                bp.resetOperazione(isEliminazione);
                return context.findDefaultForward();
            }

            if (haCliccatoSI) {
                // YES: elabora con accessori
                bp.elaboraBeneCorrente(context, isEliminazione, true);
            } else if (haCliccatoNO) {
                // NO
                if (isEliminazione) {
                    // Per eliminazione: elimina SOLO il principale SENZA accessori
                    bp.elaboraBeneCorrente(context, isEliminazione, false);
                } else {
                    // Per aggiunta: aggiungi solo il principale SENZA accessori
                    bp.elaboraBeneCorrente(context, false, false);
                }
            }

            // Passa al prossimo bene
            if (bp.passaAlProssimoBene(isEliminazione)) {
                String messaggio = String.format("[Bene %d di %d]\n\n%s",
                        bp.getIndexBeneCorrente(isEliminazione),
                        bp.getTotaleBeniPrincipali(isEliminazione),
                        bp.getMessaggioSingoloBene(isEliminazione));

                // Per eliminazione: usa 3 pulsanti, per aggiunta: usa 2 pulsanti
                int confirmType = isEliminazione ? OptionBP.CONFIRM_YES_NO_CANCEL : OptionBP.CONFIRM_YES_NO;
                openConfirm(context, messaggio, confirmType, "doRispostaRicorsiva");
            } else {
                // Finito il ciclo
                finalizeFlusso(context, bp, isEliminazione, haCliccatoSI);
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }


    /**
     * Finalizza il flusso ricorsivo con messaggi appropriati
     */
    private void finalizeFlusso(ActionContext context, CRUDTraspRientInventarioBP bp, boolean isEliminazione, boolean ultimaRispostaSI) throws ComponentException, RemoteException {

        // CRITICO: Chiama il component UNA SOLA VOLTA con tutta la selezione accumulata
        if (!isEliminazione && bp.getPendingAdd() != null && bp.getPendingAdd().getSelectionAccumulata() != null) {
            bp.modificaBeniConAccessoriComponente(context, bp.getPendingAdd().getBulks(),
                    bp.getPendingAdd().getOldSel(), bp.getPendingAdd().getSelectionAccumulata());
        }

        bp.getDettBeniController().reset(context);
        bp.getDettBeniController().reset(context);

        if (isEliminazione) {
            // Per eliminazione: se almeno un bene è stato elaborato, mostra "completata", altrimenti "annullata"
            if (bp.getIndexBeneCurrentePerEliminazione() > 0 && !ultimaRispostaSI) {
                bp.setMessage("Eliminazione annullata.");
            } else {
                bp.setMessage("Eliminazione completata.");
            }
        } else {
            bp.setMessage("Aggiunta beni completata con successo");
        }

        bp.resetOperazione(isEliminazione);
    }

    // =======================================================
    // WORKFLOW E ANNULLAMENTO (comune)
    // =======================================================

    public Forward doPredisponiAllaFirma(ActionContext context) {
        try {
            getBP(context).predisponiAllaFirma(context);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doElimina(ActionContext context) throws RemoteException {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            if (!bp.isEditing()) {
                bp.setMessage("Non è possibile annullare in questo momento.");
                return context.findDefaultForward();
            }
            if (doc.isAnnullato()) {
                bp.setMessage("Il documento è già stato annullato.");
                return context.findDefaultForward();
            }
            if (doc.isPredispostoAllaFirma()) {
                bp.setMessage("Impossibile annullare un documento già predisposto alla firma.");
                return context.findDefaultForward();
            }
            bp.annullaDoc(context);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doSalva(ActionContext context) throws RemoteException {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            if (bp.isDocumentoPredispostoAllaFirma()) {
                bp.setMessage("Impossibile salvare il documento: è già stato Predisposto Alla Firma");
                return context.findDefaultForward();
            }
            if (bp.isDocumentoAnnullato()) {
                bp.setMessage("Impossibile salvare un documento annullato");
                return context.findDefaultForward();
            }
            return super.doSalva(context);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // METODI HELPER (comuni)
    // =======================================================

    protected DocTrasportoRientroComponentSession getComponentSession(CRUDTraspRientInventarioBP bp) throws Exception {
        return (DocTrasportoRientroComponentSession) bp.createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession", DocTrasportoRientroComponentSession.class);
    }

    protected <T> boolean valoreUguale(T oldValue, T newValue) {
        return Objects.equals(oldValue, newValue);
    }

    protected Integer getCdAnag(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        if (terzo == null) return null;
        it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk anag = terzo.getAnagrafico();
        return (anag != null) ? anag.getCd_anag() : null;
    }

    protected boolean hasBeniInDettaglio(Doc_trasporto_rientroBulk doc) {
        return doc.getDoc_trasporto_rientro_dettColl() != null && !doc.getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    protected void eliminaBeniSePresenti(ActionContext context, CRUDTraspRientInventarioBP bp, Doc_trasporto_rientroBulk doc, String messaggio) throws Exception {
        bp.getDettBeniController().removeAll(context);
        bp.getEditDettController().removeAll(context);
        if (hasBeniInDettaglio(doc)) {
            getComponentSession(bp).eliminaTuttiBeniDettaglio(context.getUserContext(), doc);
            doc.getDoc_trasporto_rientro_dettColl().clear();
            bp.setDirty(true);
            bp.setMessage(messaggio);
        }
    }

    protected void validaTestataDocumento(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc.getTipoMovimento() == null) {
            throw new ValidationException("Attenzione: specificare un tipo di movimento nella testata.");
        }
        if (doc.getDataRegistrazione() == null) {
            throw new ValidationException("Attenzione: specificare la " + getDataLabel() + ".");
        }
        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) {
            throw new ValidationException("Attenzione: indicare una Descrizione per il Documento.");
        }
    }
}