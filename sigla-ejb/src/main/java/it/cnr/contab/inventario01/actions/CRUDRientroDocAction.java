package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bp.CRUDRientroBeniInvBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.*;
import it.cnr.jada.util.ejb.EJBCommonServices;
import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Action per la gestione del flusso di Rientro Beni.
 *
 * CARATTERISTICHE SPECIFICHE RIENTRO:
 * - Validazione esistenza documento di trasporto firmato
 * - Controllo che i beni non siano già rientrati
 * - Collegamento automatico con documento di trasporto di riferimento
 * - Gestione accessori identica al trasporto
 *
 * Coordina:
 * - Cambio testata (tipo movimento, tipo ritiro, dipendente, data)
 * - Aggiunta/Selezione beni con gestione accessori (flusso ricorsivo unificato)
 * - Eliminazione beni con gestione accessori (flusso ricorsivo unificato)
 * - Workflow (predisponi alla firma)
 * - Annullamento documento
 */
public class CRUDRientroDocAction extends it.cnr.jada.util.action.CRUDAction {

    public CRUDRientroDocAction() {
        super();
    }

    // =======================================================
    // GESTIONE CAMBI TESTATA
    // =======================================================

    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = getBP(context);
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
            CRUDRientroBeniInvBP bp = getBP(context);
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
            CRUDRientroBeniInvBP bp = getBP(context);
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
            CRUDRientroBeniInvBP bp = getBP(context);

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
    // AGGIUNTA BENI - SELEZIONATORE
    // =======================================================

    /**
     * METODO SPECIFICO RIENTRO:
     * Carica solo beni presenti in documenti di TRASPORTO FIRMATI
     */
    public Forward doAddToCRUDMain_DettBeniController(ActionContext context) {
        try {
            fillModel(context);

            CRUDRientroBeniInvBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            validaTestataDocumento(doc);
            bp.getDettBeniController().validate(context);

            SimpleBulkList selezionati = getComponentSession(bp).selezionati(
                    context.getUserContext(), doc);

            // SPECIFICO RIENTRO: cerca solo beni trasportati e firmati
            RemoteIterator ri = getComponentSession(bp).getListaBeniDaFarRientrare(
                    context.getUserContext(), doc, selezionati, null);
            ri = EJBCommonServices.openRemoteIterator(context, ri);

            if (ri.countElements() == 0) {
                bp.setMessage("Nessun Bene recuperato da documenti di trasporto firmati.");
                EJBCommonServices.closeRemoteIterator(context, ri);
                return context.findDefaultForward();
            }

            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(false);
            rlbp.setPrototype(new Inventario_beniBulk());

            context.addHookForward("searchResult", this, "doBringBackAddBeniRientro");
            context.addHookForward("filter", this, "doBringBackAddBeniRientro");

            return context.addBusinessProcess(rlbp);

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doBringBackAddBeniRientro(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            CRUDRientroBeniInvBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            CompoundFindClause clauses = (CompoundFindClause) fwd.getParameter("filter");
            bp.setClauses(clauses);
            context.addHookForward("filter", this, "doSelezionaBeniRientro");

            SimpleBulkList selezionati = getComponentSession(bp).selezionati(
                    context.getUserContext(), doc);

            // SPECIFICO RIENTRO: cerca solo beni trasportati e firmati
            RemoteIterator ri = getComponentSession(bp).getListaBeniDaFarRientrare(
                    context.getUserContext(), doc, selezionati, clauses);
            ri = EJBCommonServices.openRemoteIterator(context, ri);

            if (ri.countElements() == 0) {
                bp.setMessage("Nessun Bene associabile da documenti di trasporto firmati.");
                EJBCommonServices.closeRemoteIterator(context, ri);
                return context.findDefaultForward();
            }

            SelezionatoreListaBP slbp = select(context, ri,
                    it.cnr.jada.bulk.BulkInfo.getBulkInfo(Inventario_beniBulk.class),
                    null, "doSelezionaBeniRientro", null, bp);
            slbp.setMultiSelection(true);
            return slbp;

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // ELIMINAZIONE E AGGIUNTA - FLUSSO RICORSIVO UNIFICATO
    // =======================================================

    /**
     * Metodo richiamato quando l'utente preme "Elimina Selezionati"
     */
    public Forward doRemoveFromCRUDMain_DettBeniController_V3(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = getBP(context);
            bp.getDettBeniController().remove(context);

            // Apri flusso ricorsivo solo se ci sono beni con accessori
            return bp.hasBeniPrincipaliConAccessoriPerEliminazione()
                    ? apriFlussoRicorsivo(context, bp, true)
                    : context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Quando il Selezionatore Lista chiude, controlla la presenza di beni con accessori.
     */
    public Forward doSelezionaBeniRientro(ActionContext context) {
        try {
            CRUDRientroBeniInvBP bp = getBP(context);
            bp.getDettBeniController().reset(context);

            if (bp.hasBeniPrincipaliConAccessoriPendenti()) {
                return apriFlussoRicorsivo(context, bp, false);
            } else {
                bp.getDettBeniController().reset(context);
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Apre il flusso ricorsivo (valido per ELIMINAZIONE e AGGIUNTA)
     */
    private Forward apriFlussoRicorsivo(ActionContext context, CRUDRientroBeniInvBP bp,
                                        boolean isEliminazione) {
        // Reimposta gli indici
        bp.setIndexBeneCurrentePerEliminazione(0);
        bp.setIndexBeneCurrentePerAggiunta(0);

        // Salva il flag per sapere quale operazione siamo in corso
        bp.setUltimaOperazioneEliminazione(isEliminazione);

        String messaggio = String.format(
                "[Bene %d di %d]\n\n%s",
                bp.getIndexBeneCorrente(isEliminazione),
                bp.getTotaleBeniPrincipali(isEliminazione),
                bp.getMessaggioSingoloBene(isEliminazione)
        );

        try {
            openConfirm(context, messaggio, OptionBP.CONFIRM_YES_NO, "doRispostaRicorsiva");
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }

        return context.findDefaultForward();
    }

    /**
     * Gestisce la risposta ricorsiva (UNIFICATA per ELIMINAZIONE e AGGIUNTA)
     * SI → elabora il bene con gli accessori
     * NO → salta il bene
     */
    public Forward doRispostaRicorsiva(ActionContext context, int opt) {
        try {
            CRUDRientroBeniInvBP bp = getBP(context);
            boolean isEliminazione = bp.isUltimaOperazioneEliminazione();

            // Elabora il bene corrente se SI
            if (opt == OptionBP.YES_BUTTON) {
                // Per eliminazione: sempre elimina bene + accessori
                // Per aggiunta: YES = include accessori, NO = senza accessori
                boolean includiAccessori = !isEliminazione;
                bp.elaboraBeneCorrente(context, isEliminazione, includiAccessori);
            }
            // Se NO: salta senza fare nulla

            // Passa al prossimo bene
            if (bp.passaAlProssimoBene(isEliminazione)) {
                // Ancora beni: continua il flusso
                String messaggio = String.format(
                        "[Bene %d di %d]\n\n%s",
                        bp.getIndexBeneCorrente(isEliminazione),
                        bp.getTotaleBeniPrincipali(isEliminazione),
                        bp.getMessaggioSingoloBene(isEliminazione)
                );

                openConfirm(context, messaggio, OptionBP.CONFIRM_YES_NO, "doRispostaRicorsiva");
            } else {
                // Tutti i beni elaborati
                finalizeFlusso(context, bp, isEliminazione);
            }

            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Finalizza il flusso ricorsivo
     */
    private void finalizeFlusso(ActionContext context, CRUDRientroBeniInvBP bp,
                                boolean isEliminazione) {
        bp.getDettBeniController().reset(context);

        if (isEliminazione) {
            bp.setMessage("Eliminazione completata.");
        } else {
            int totaleAggiunti = bp.getTotaleBeniPrincipali(false);
            int totaleAccessori = bp.getNumeroBeniAccessoriTotaliPendenti();
            int totaleSemplici = bp.getNumeroBeniSemplici();

            String msg = String.format(
                    "Aggiunta completata:\n- %d beni principali\n- %d beni accessori\n- %d beni semplici",
                    totaleAggiunti,
                    totaleAccessori,
                    totaleSemplici
            );
            bp.setMessage(msg);
        }

        bp.resetOperazione(isEliminazione);
    }

    // =======================================================
    // WORKFLOW E ANNULLAMENTO
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

            CRUDRientroBeniInvBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            // Validazioni
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

            getBP(context).annullaDoc(context);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    @Override
    public Forward doSalva(ActionContext context) throws RemoteException {
        try {
            fillModel(context);
            CRUDRientroBeniInvBP bp = getBP(context);

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
    // METODI HELPER 💡
    // =======================================================

    private CRUDRientroBeniInvBP getBP(ActionContext context) {
        return (CRUDRientroBeniInvBP) getBusinessProcess(context);
    }

    private DocTrasportoRientroComponentSession getComponentSession(CRUDRientroBeniInvBP bp) throws Exception {
        return (DocTrasportoRientroComponentSession) bp.createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
    }

    private <T> boolean valoreUguale(T oldValue, T newValue) {
        return Objects.equals(oldValue, newValue);
    }

    private Integer getCdAnag(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        if (terzo == null) return null;
        it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk anag = terzo.getAnagrafico();
        return (anag != null) ? anag.getCd_anag() : null;
    }

    private boolean hasBeniInDettaglio(Doc_trasporto_rientroBulk doc) {
        return doc.getDoc_trasporto_rientro_dettColl() != null &&
                !doc.getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    private void eliminaBeniSePresenti(ActionContext context, CRUDRientroBeniInvBP bp,
                                       Doc_trasporto_rientroBulk doc, String messaggio) throws Exception {

        bp.getDettBeniController().removeAll(context);
        bp.getEditDettController().removeAll(context);

        if (hasBeniInDettaglio(doc)) {
            getComponentSession(bp).eliminaTuttiBeniDettaglio(context.getUserContext(), doc);
            doc.getDoc_trasporto_rientro_dettColl().clear();
            bp.setDirty(true);
            bp.setMessage(messaggio);
        }
    }

    private void validaTestataDocumento(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc.getTipoMovimento() == null) {
            throw new ValidationException("Attenzione: specificare un tipo di movimento nella testata.");
        }

        if (doc.getDataRegistrazione() == null) {
            throw new ValidationException("Attenzione: specificare la data rientro.");
        }

        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) {
            throw new ValidationException("Attenzione: indicare una Descrizione per il Documento di Rientro.");
        }
    }
}