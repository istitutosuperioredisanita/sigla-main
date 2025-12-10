package it.cnr.contab.inventario01.actions;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Stampa_doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.inventario01.service.DocTraspRientFirmatariService;
import it.cnr.contab.inventario01.service.DocTraspRientHappySignService;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.OptionBP;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import static it.cnr.jada.util.action.FormController.VIEW;

/**
 * Action BASE per la gestione del flusso di Trasporto/Rientro Beni.
 * <p>
 * Contiene TUTTA la logica comune:
 * - Cambio testata (tipo movimento, tipo ritiro, dipendente, data)
 * - Aggiunta/Selezione beni con gestione accessori (flusso ricorsivo unificato)
 * - Eliminazione beni con gestione accessori (flusso ricorsivo unificato)
 * - Workflow (invia alla firma, annullamento)
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


    public Forward doOnDipendenteChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            Integer oldValue = getCdAnag(doc.getTerzoIncRitiro());
            fillModel(context);
            Integer newValue = getCdAnag(doc.getTerzoIncRitiro());
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
            Doc_trasporto_rientroBulk model=(Doc_trasporto_rientroBulk)bp.getModel();
            bp.setModel(context,model);
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



    /**
     * ✅ VERSIONE CORRETTA: Gestisce correttamente SI/NO/ANNULLA per ogni bene
     *
     * COMPORTAMENTO CORRETTO:
     * - SI (AGGIUNTA/ELIMINAZIONE): Elabora bene principale + accessori
     * - NO (AGGIUNTA/ELIMINAZIONE): Elabora SOLO bene principale (SENZA accessori)
     * - ANNULLA: Interrompe l'intera operazione
     */
    public Forward doRispostaRicorsivaGenerica(ActionContext context, int opt) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            boolean isEliminazione = bp.isUltimaOperazioneEliminazione();

            HookForward caller = (HookForward) context.getCaller();
            List<Inventario_beniBulk> beniDaElaborare = (List<Inventario_beniBulk>) caller.getParameter("beniDaElaborare");
            Integer index = (Integer) caller.getParameter("index");
            Inventario_beniBulk beneCorrente = (Inventario_beniBulk) caller.getParameter("beneCorrente");

            boolean haCliccatoSI = (opt == OptionBP.YES_BUTTON);
            boolean haCliccatoNO = (opt == OptionBP.NO_BUTTON);
            boolean haCliccatoANNULLA = (opt == OptionBP.CANCEL_BUTTON);

            // ANNULLA
            if (haCliccatoANNULLA) {
                bp.getDettBeniController().reset(context);

                if (isEliminazione) {
                    bp.setMessage("Eliminazione annullata.");
                } else {
                    bp.setMessage("Operazione annullata.");
                }

                bp.resetOperazione(isEliminazione);
                return context.findDefaultForward();
            }

            // SI
            if (haCliccatoSI) {
                bp.elaboraBeneCorrente(context, isEliminazione, true);
            }

            // NO
            else if (haCliccatoNO) {
                if (isEliminazione) {
                    bp.elaboraBeneCorrente(context, true, false);
                } else {
                    bp.elaboraBeneCorrente(context, false, false);
                }
            }

            // Aggiorna vista
            if (!isEliminazione) {
                bp.getDettBeniController().reset(context);
            } else {
                bp.getEditDettController().reset(context);
            }

            // Step successivo
            int nextIndex = index + 1;

            if (nextIndex < beniDaElaborare.size()) {
                return apriFlussoRicorsivoGenerico(context, bp, isEliminazione, beniDaElaborare, nextIndex);
            } else {
                finalizeFlusso(context, bp, isEliminazione, haCliccatoSI);
                return context.findDefaultForward();
            }

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }


    public Forward doRemoveFromCRUDMain_DettBeniController(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);

            bp.getDettBeniController().remove(context);

            if (bp.getPendingDelete() == null) {
                return context.findDefaultForward();
            }

            // Elimina beni semplici
            if (!bp.getPendingDelete().getPrincipaliSenza().isEmpty()) {

                for (Inventario_beniBulk beneSemplice : bp.getPendingDelete().getPrincipaliSenza()) {
                    try {
                        bp.getComp().eliminaBeniAssociati(
                                context.getUserContext(),
                                bp.getDoc(),
                                new it.cnr.jada.bulk.OggettoBulk[]{beneSemplice}
                        );
                    } catch (Exception e) {
                        throw bp.handleException(e);
                    }
                }

                bp.getDettBeniController().reset(context);
            }

            // Beni con accessori → avvia flusso ricorsivo
            if (!bp.getPendingDelete().getPrincipaliConAccessori().isEmpty()) {

                List<Inventario_beniBulk> beniConAccessori = new ArrayList<>(
                        bp.getPendingDelete().getPrincipaliConAccessori().keySet()
                );

                return apriFlussoRicorsivoGenerico(context, bp, true, beniConAccessori, 0);
            }

            // Fine
            bp.resetOperazione(true);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }


    /**
     * METODO CRITICO: Gestisce la selezione dei beni dopo che l'utente li ha selezionati.
     * DEVE:
     * 1. Raccogliere TUTTI i beni selezionati (semplici e con accessori)
     * 2. Avviare il flusso ricorsivo generalizzato per elaborarli uno per uno
     *    - Beni semplici: elaborare direttamente SENZA accessori
     *    - Beni con accessori: chiedere conferma per includere accessori
     */
    protected Forward doSelezionaBeniGeneric(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);

            if (bp.getPendingAdd() == null) {
                return context.findDefaultForward();
            }

            // Aggiungi SUBITO i beni semplici (principali senza accessori + accessori standalone)
            // Questi NON richiedono conferma, quindi li elaboriamo prima del flusso ricorsivo
            aggiungiImmediatamenteBeniSemplici(context, bp);

            // Crea una lista SOLO dei beni principali CON accessori (che richiedono conferma)
            List<Inventario_beniBulk> beniConAccessoriDaConfermare = new ArrayList<>(
                    bp.getPendingAdd().getPrincipaliConAccessori().keySet()
            );

            // Se non ci sono beni con accessori, finisci subito
            if (beniConAccessoriDaConfermare.isEmpty()) {
                bp.getDettBeniController().reset(context);
                bp.setMessage("Aggiunta beni completata con successo");
                bp.resetOperazione(false);
                return context.findDefaultForward();
            }

            //  3: Avvia il flusso ricorsivo SOLO per i beni con accessori
            return apriFlussoRicorsivoGenerico(context, bp, false, beniConAccessoriDaConfermare, 0);

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Aggiunge immediatamente i beni semplici senza chiedere conferma
     */
    private void aggiungiImmediatamenteBeniSemplici(ActionContext context, CRUDTraspRientInventarioBP bp)
            throws BusinessProcessException {
        try {
            List<Inventario_beniBulk> beniSemplici = new ArrayList<>();
            beniSemplici.addAll(bp.getPendingAdd().getPrincipaliSenza());
            beniSemplici.addAll(bp.getPendingAdd().getAccessori());

            if (beniSemplici.isEmpty()) {
                return;
            }

            // Crea un BitSet per selezionare tutti i beni semplici
            BitSet oldSelection = new BitSet(bp.getPendingAdd().getBulks().length);
            BitSet newSelection = new BitSet(bp.getPendingAdd().getBulks().length);

            for (Inventario_beniBulk beneSemplice : beniSemplici) {
                for (int i = 0; i < bp.getPendingAdd().getBulks().length; i++) {
                    if (bp.getPendingAdd().getBulks()[i] instanceof Inventario_beniBulk) {
                        Inventario_beniBulk b = (Inventario_beniBulk) bp.getPendingAdd().getBulks()[i];
                        if (b.equalsByPrimaryKey(beneSemplice)) {
                            newSelection.set(i);
                            break;
                        }
                    }
                }
            }

            // Aggiungi tutti i beni semplici in una sola chiamata
            bp.modificaBeniConAccessoriComponente(context, bp.getPendingAdd().getBulks(), oldSelection, newSelection);

        } catch (ComponentException | RemoteException e) {
            throw bp.handleException(e);
        }
    }


    private Forward apriFlussoRicorsivoGenerico(ActionContext context, CRUDTraspRientInventarioBP bp,
                                                boolean isEliminazione,
                                                List<Inventario_beniBulk> beniDaElaborare,
                                                int index)
            throws ComponentException, RemoteException, BusinessProcessException {

        if (beniDaElaborare == null || index >= beniDaElaborare.size()) {
            finalizeFlusso(context, bp, isEliminazione, true);
            return context.findDefaultForward();
        }

        Inventario_beniBulk beneCorrente = beniDaElaborare.get(index);

        bp.setUltimaOperazioneEliminazione(isEliminazione);

        // ========== AGGIORNA L'INDICE INTERNO DEL BP ==========
        if (isEliminazione) {
            bp.setIndexBeneCurrentePerEliminazione(index);
        } else {
            bp.setIndexBeneCurrentePerAggiunta(index);
        }
        // ======================================================

        String messaggio = String.format("[Bene %d di %d]\n\n%s",
                index + 1,
                beniDaElaborare.size(),
                bp.getMessaggioSingoloBene(isEliminazione));

        try {
            int confirmType = isEliminazione ? OptionBP.CONFIRM_YES_NO_CANCEL : OptionBP.CONFIRM_YES_NO;
            openConfirm(context, messaggio, confirmType, "doRispostaRicorsivaGenerica");

            HookForward hookForward = (HookForward) context.findForward("option");
            if (hookForward != null) {
                hookForward.addParameter("beniDaElaborare", beniDaElaborare);
                hookForward.addParameter("index", index);
                hookForward.addParameter("beneCorrente", beneCorrente);
            }
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }

        return context.findDefaultForward();
    }




    private Forward apriFlussoRicorsivo(ActionContext context, CRUDTraspRientInventarioBP bp, boolean isEliminazione) {
        bp.setIndexBeneCurrentePerEliminazione(0);
        bp.setIndexBeneCurrentePerAggiunta(0);
        bp.setUltimaOperazioneEliminazione(isEliminazione);
        Inventario_beniBulk beneProssimoCorrente = bp.getBenePrincipaleCorrente(isEliminazione);

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

            // Recupera prima il bene corrente, poi passa entrambi i parametri
            Inventario_beniBulk beneProssimoCorrente = bp.getBenePrincipaleCorrente(isEliminazione);

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

    private void finalizeFlusso(ActionContext context, CRUDTraspRientInventarioBP bp,
                                boolean isEliminazione, boolean ultimaRispostaSI)
            throws ComponentException, RemoteException {

        bp.getDettBeniController().reset(context);

        if (isEliminazione) {
            // Per eliminazione: messaggio sempre "completata" (perché ANNULLA esce prima)
            bp.setMessage("Eliminazione completata.");
        } else {
            // Per aggiunta: sempre successo
            bp.setMessage("Aggiunta beni completata con successo");
        }

        bp.resetOperazione(isEliminazione);
    }

    // =======================================================
    // WORKFLOW E ANNULLAMENTO (comune)
    // =======================================================

    /**
     * Gestisce il click su "Invia alla Firma"
     * Esegue l'intero flusso:
     * 1. Validazioni
     * 2. Popolazione firmatari
     * 3. Generazione PDF (con dati testata + dettagli)
     * 4. Salvataggio PDF NON firmato su CMIS (temporaneo)
     * 5. Invio a HappySign
     * 6. Aggiornamento stato documento
     * 7. Quando HappySign richiama il callback, il PDF firmato sostituirà quello temporaneo
     */
    public Forward doInviaInFirma(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            // ==================== 1. VALIDAZIONI ====================
            validaDocumentoPerFirma(doc);

            // ==================== 2. POPOLAZIONE FIRMATARI ====================
            DocTraspRientFirmatariService firmatariService =
                    SpringUtil.getBean("docTraspRientFirmatariService", DocTraspRientFirmatariService.class);

            firmatariService.popolaFirmatari(doc, (CNRUserContext) context.getUserContext());

            // ==================== 3. GENERAZIONE PDF ====================
            // Questo PDF contiene TUTTI i dati (testata + dettagli)
            File pdfFile = bp.stampaDocTrasportoRientro(context.getUserContext(), doc);

            // Leggi il PDF come byte array
            byte[] pdfBytes;
            try (FileInputStream fis = new FileInputStream(pdfFile)) {
                pdfBytes = IOUtils.toByteArray(fis);
            }

            // ==================== 4. SALVATAGGIO PDF TEMPORANEO SU CMIS ====================
            //  Questo è il PDF NON ancora firmato
            salvaStampaSuCMIS(context, doc, pdfFile, false); // ← false = non firmato

            // ==================== 5. INVIO A HAPPYSIGN ====================
            DocTraspRientHappySignService happySignService =
                    SpringUtil.getBean("docTraspRientHappySignService", DocTraspRientHappySignService.class);

            String uuidHappysign = happySignService.inviaDocumentoAdHappySign(doc, pdfBytes);

            // ==================== 6. AGGIORNAMENTO STATO DOCUMENTO ====================
            doc.setIdFlussoHappysign(uuidHappysign);
            doc.setDataInvioFirma(new Timestamp(System.currentTimeMillis()));

            // Usa il metodo del Component per cambiare stato
            doc = getComponentSession(bp).changeStatoInInviato(context.getUserContext(), doc);

            // Aggiorna il model nel BP
            bp.setModel(context, doc);
            bp.setStatus(VIEW);

            bp.setMessage("Documento inviato alla firma con successo. UUID HappySign: " + uuidHappysign);

            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Salva la stampa del documento su CMIS
     *
     * @param context il contesto dell'azione
     * @param doc il documento di trasporto/rientro
     * @param pdfFile il file PDF da salvare
     * @param isFirmato true se il PDF è già firmato, false se è temporaneo
     */
    private void salvaStampaSuCMIS(ActionContext context, Doc_trasporto_rientroBulk doc,
                                   File pdfFile, boolean isFirmato)
            throws Exception {
/*
        DocTraspRientCMISService storeService =
                SpringUtil.getBean("docTraspRientCMISService", DocTraspRientCMISService.class);

        // Verifica se esiste già una stampa
        StorageObject stampaEsistente = storeService.getStorageObjectStampaDoc(doc, context.getUserContext());

        if (stampaEsistente != null) {
            // ========== AGGIORNA FILE ESISTENTE ==========
            try (FileInputStream fis = new FileInputStream(pdfFile)) {
                storeService.updateStream(
                        stampaEsistente.getKey(),
                        fis,
                        "application/pdf"
                );
            }

            // Aggiorna le proprietà
            Map<String, Object> properties = new HashMap<>();
            properties.put("doc_trasporto_rientro:stato",
                    isFirmato ? DocTraspRientCMISService.STATO_STAMPA_DOC_VALIDO
                            : DocTraspRientCMISService.STATO_STAMPA_DOC_ANNULLATO);
            properties.put("doc_trasporto_rientro:firmato", isFirmato);
            if (isFirmato) {
                properties.put("doc_trasporto_rientro:data_firma", new Date());
            }

            storeService.updateProperties(properties, stampaEsistente);

        } else {
            // ========== CREA NUOVO FILE ==========
            AllegatoGenericoBulk allegatoStampa = new AllegatoGenericoBulk();
            allegatoStampa.setFile(pdfFile);
            allegatoStampa.setContentType("application/pdf");
            allegatoStampa.setNome(pdfFile.getName());
            allegatoStampa.setDescrizione("Stampa documento " + doc.getTiDocumento() + " " + doc.getPgDocTrasportoRientro());
            allegatoStampa.setTitolo(pdfFile.getName());
            allegatoStampa.setCrudStatus(OggettoBulk.TO_BE_CREATED);
            String uo = CNRUserContext.getCd_unita_organizzativa(context.getUserContext());

            try (FileInputStream fis = new FileInputStream(pdfFile)) {
                storeService.storeSimpleDocument(
                        allegatoStampa,
                        fis,
                        "application/pdf",
                        pdfFile.getName(),
                        storeService.getStorePath(doc)
                );
            }
        }*/
    }

    /**
     * Validazioni pre-invio firma
     */
    private void validaDocumentoPerFirma(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc == null) {
            throw new ValidationException("Documento non presente");
        }

        if (doc.isAnnullato()) {
            throw new ValidationException("Impossibile inviare alla firma un documento annullato");
        }

        if (!doc.isInserito()) {
            throw new ValidationException("Il documento deve essere in stato 'Inserito' per essere inviato in firma");
        }

        if (doc.getDoc_trasporto_rientro_dettColl() == null ||
                doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            throw new ValidationException("Il documento deve contenere almeno un bene");
        }

        if (doc.getTipoMovimento() == null) {
            throw new ValidationException("Tipo movimento non specificato");
        }

        if (doc.getDataRegistrazione() == null) {
            throw new ValidationException("Data registrazione non specificata");
        }

        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) {
            throw new ValidationException("Descrizione documento non specificata");
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
            if (doc.isInviatoInFirma()) {
                bp.setMessage("Impossibile annullare un documento già inviato in firma.");
                return context.findDefaultForward();
            }
            bp.annullaDoc(context);
            return context.findDefaultForward();
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
        SimpleBulkList selezionati = getComponentSession(bp).selezionati(context.getUserContext(), doc);
        if (!selezionati.isEmpty()) {
            bp.getDettBeniController().removeAll(context);
            doc.getDoc_trasporto_rientro_dettColl().clear();
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


    /**
     * Gestisce la Stampa di un Documento di Trasporto/Rientro.
     *
     * @param context <code>ActionContext</code> in uso.
     * @return <code>Forward</code>
     */
    public Forward doStampaDocTraspRient(ActionContext context) {

        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = (CRUDTraspRientInventarioBP)getBusinessProcess(context);
            Doc_trasporto_rientroBulk docTrasporto = (Doc_trasporto_rientroBulk)bp.getModel();

            ParametricPrintBP ppbp = (ParametricPrintBP)context.createBusinessProcess("StampaDocTraspRientBP");
            Stampa_doc_trasporto_rientroBulk stampa = (Stampa_doc_trasporto_rientroBulk)ppbp.createNewBulk(context);

            if (docTrasporto != null && docTrasporto.getPgDocTrasportoRientro() != null){
                stampa.setPgInizio(new Integer(docTrasporto.getPgDocTrasportoRientro().intValue()));
                stampa.setPgFine(new Integer(docTrasporto.getPgDocTrasportoRientro().intValue()));
            }
            ppbp.setModel(context, stampa);

            return context.addBusinessProcess(ppbp);

        } catch (Throwable t) {
            return handleException(context, t);
        }
    }

    /**
     * Gestione della richiesta di salvataggio di una variazione come definitiva
     *
     * @param context	L'ActionContext della richiesta
     * @return Il Forward alla pagina di risposta
     */
    public Forward doSalvaDefinitivo(ActionContext context) {

        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            bp.salvaDefinitivo(context);
            bp.setMessage("Documento salvato definitivamente con successo.");
            return context.findDefaultForward();

        }catch(Throwable ex){
            return handleException(context, ex);
        }

    }

    @Override
    public Forward doSalva(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            bp.save(context);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il cambio dell'Assegnatario Smartworking.
     * Elimina i beni precedenti se l'assegnatario cambia.
     */
    public Forward doOnTerzoSmartworkingChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            Integer oldValue = getCdTerzo(doc.getTerzoSmartworking());
            fillModel(context);

            // Carica il terzo completo se presente solo il codice
            Integer cdTerzo = doc.getTerzoSmartworking() != null ?
                    doc.getTerzoSmartworking().getCd_terzo() : null;

            if (cdTerzo != null) {
                try {
                    TerzoBulk terzoCompleto = (TerzoBulk) bp.createComponentSession()
                            .findByPrimaryKey(context.getUserContext(), new TerzoBulk(cdTerzo));

                    if (terzoCompleto != null) {
                        doc.setTerzoSmartworking(terzoCompleto);
                    }
                } catch (Exception e) {
                    throw new it.cnr.jada.comp.ApplicationException(
                            "Errore nel caricamento dell'Assegnatario: " + e.getMessage()
                    );
                }
            }

            Integer newValue = getCdTerzo(doc.getTerzoSmartworking());

            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            // Elimina beni precedenti se assegnatario cambia
            eliminaBeniSePresenti(context, bp, doc,
                    "Assegnatario Smartworking modificato. Beni precedenti rimossi.");

            bp.setModel(context, doc);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il cambio del Tipo Movimento.
     * COMPORTAMENTO CORRETTO:
     * 1. Reset SEMPRE dei beni (con o senza messaggio a seconda del caso)
     * 2. Reset SEMPRE del dipendente appropriato:
     *    - Se cambio DA Smartworking: reset terzoSmartworking + anagraficoDipSW
     *    - Se cambio VERSO Smartworking: reset terzoIncRitiro + anagraficoDipIncRit
     *    - Se cambio tra NON-Smartworking: reset terzoIncRitiro + anagraficoDipIncRit
     */
    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            // Salva lo stato precedente
            boolean wasSmartworking = doc.isSmartworking();

            // Aggiorna il model con i dati dal form
            fillModel(context);

            // Ricarica la lista dei tipi movimento in base a tiDocumento (T/R)
            try {
                DocTrasportoRientroComponentSession session = getComponentSession(bp);
                session.initializeKeysAndOptionsInto(context.getUserContext(), doc);
            } catch (Exception e) {
                throw new BusinessProcessException("Errore ricaricamento tipi movimento", e);
            }

            // Verifica il nuovo stato
            boolean isSmartworking = doc.isSmartworking();

            // ========== RESET BENI ==========
            if (wasSmartworking != isSmartworking) {
                // Da/A Smartworking: reset SILENZIOSO
                resetBeniSilenzioso(context, bp, doc);
            } else {
                // Tra tipi dello stesso gruppo: reset CON messaggio
                eliminaBeniSePresenti(context, bp, doc, "Tipo Movimento modificato. Beni precedenti rimossi.");
            }

            // ========== RESET DIPENDENTE/ASSEGNATARIO ==========
            if (wasSmartworking && !isSmartworking) {
                // DA Smartworking → Altro: reset campi smartworking
                doc.setTerzoSmartworking(null);
                doc.setAnagSmartworking(null);

            } else if (!wasSmartworking && isSmartworking) {
                // DA Altro → Smartworking: reset campi incaricato
                doc.setTerzoIncRitiro(null);
                doc.setAnagIncRitiro(null);

            } else if (!wasSmartworking && !isSmartworking) {
                // Tra NON-Smartworking: reset campi incaricato
                doc.setTerzoIncRitiro(null);
                doc.setAnagIncRitiro(null);
            }

            bp.setModel(context, doc);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il cambio del Tipo Ritiro (Incaricato/Vettore).
     * COMPORTAMENTO CORRETTO:
     * 1. Reset SEMPRE dei beni con messaggio
     * 2. Reset SEMPRE del dipendente incaricato quando si cambia tipo ritiro
     */
    public Forward doOnTipoRitiroChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            // Salva il valore precedente del Tipo Ritiro
            String oldValue = doc.getTipoRitiro();

            // Aggiorna il model con i nuovi dati dal form
            fillModel(context);

            // Recupera il nuovo valore
            String newValue = doc.getTipoRitiro();

            // Se il valore non è cambiato, esci
            if (oldValue != null && valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            // ========== RESET BENI ==========
            String messaggio = null;

            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(oldValue) &&
                    Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(newValue)) {
                messaggio = "Cambio da Vettore a Incaricato. Beni precedenti rimossi.";

            } else if (Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(oldValue) &&
                    Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
                messaggio = "Cambio da Incaricato a Vettore. Beni precedenti rimossi.";

            } else if (oldValue != null) {
                messaggio = "Tipo Ritiro modificato. Beni precedenti rimossi.";
            }

            if (messaggio != null) {
                eliminaBeniSePresenti(context, bp, doc, messaggio);
            }

            // ========== RESET DIPENDENTE INCARICATO ==========
            // Qualsiasi cambio di tipo ritiro resetta il dipendente incaricato
            doc.setTerzoIncRitiro(null);
            doc.setAnagIncRitiro(null);

            // ========== PULIZIA CAMPI DIPENDENTI ==========
            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
                // Modalità Vettore: mantieni nominativo e destinazione

            } else if (Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(newValue)) {
                // Modalità Incaricato: pulisci nominativo vettore
                doc.setNominativoVettore(null);

            } else {
                // Tipo Ritiro deselezionato: pulisci tutto
                doc.setNominativoVettore(null);
                doc.setDestinazione(null);
                doc.setIndirizzo(null);
            }

            bp.setModel(context, doc);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

// ============================================================
// METODI HELPER PER RESET
// ============================================================

    /**
     * Resetta SOLO i beni SENZA mostrare messaggi.
     * Usato quando si cambia da/a Smartworking.
     */
    private void resetBeniSilenzioso(ActionContext context,
                                     CRUDTraspRientInventarioBP bp,
                                     Doc_trasporto_rientroBulk doc) throws Exception {
        SimpleBulkList selezionati = getComponentSession(bp).selezionati(context.getUserContext(), doc);
        if (!selezionati.isEmpty()) {
            bp.getDettBeniController().removeAll(context);
            doc.getDoc_trasporto_rientro_dettColl().clear();
            // NESSUN messaggio
        }
    }


    protected Integer getCdTerzo(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        return (terzo != null) ? terzo.getCd_terzo() : null;
    }
}