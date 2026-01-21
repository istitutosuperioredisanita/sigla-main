package it.cnr.contab.inventario01.actions;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
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
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneSempliceBulk;
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
 * Action astratta per la gestione del flusso di Trasporto e Rientro Beni.
 * Fornisce la logica comune per la manipolazione della testata, la gestione
 * ricorsiva dei beni con accessori e l'integrazione con il workflow di firma.
 */
public abstract class CRUDTraspRientDocAction extends it.cnr.jada.util.action.CRUDAction {

    protected abstract CRUDTraspRientInventarioBP getBP(ActionContext context);
    protected abstract RemoteIterator getListaBeni(ActionContext context, CRUDTraspRientInventarioBP bp, Doc_trasporto_rientroBulk doc, SimpleBulkList selezionati, CompoundFindClause clauses) throws Exception;
    protected abstract String getSelezionaMethod();
    protected abstract String getBringBackMethod();
    protected abstract String getMessageNoResults();
    protected abstract String getDataLabel();
    protected abstract String getTabTestataName();

    /**
     * Gestisce il cambio del dipendente e la rimozione dei beni precedentemente inseriti.
     */
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

    /**
     * Gestisce la navigazione tra i tab, validando l'accesso agli allegati e creando la testata se necessario.
     */
    @Override
    public Forward doTab(ActionContext context, String tabName, String pageName) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = bp.getDoc();

            if (pageName != null && pageName.contains("Allegati")) {
                boolean puoAccedere = false;
                if (doc != null && doc.getCrudStatus() != OggettoBulk.TO_BE_CREATED && doc.getDoc_trasporto_rientro_dettColl() != null && !doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
                    for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
                        Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) obj;
                        if (dett.getCrudStatus() == OggettoBulk.NORMAL) {
                            puoAccedere = true;
                            break;
                        }
                    }
                }
                if (!puoAccedere) {
                    bp.setMessage("Impossibile accedere agli allegati: salvare il documento con almeno un bene.");
                    return context.findDefaultForward();
                }
            }

            if (doc != null && doc.getCrudStatus() == OggettoBulk.TO_BE_CREATED) {
                if (pageName != null && pageName.contains("Dettaglio")) {
                    bp.validaDatiPerDettagli();
                    doc = (Doc_trasporto_rientroBulk) getComponentSession(bp).creaConBulk(context.getUserContext(), doc);
                    bp.setModel(context, doc);
                    if (doc.getDoc_trasporto_rientro_dettColl() == null) {
                        doc.setDoc_trasporto_rientro_dettColl(new SimpleBulkList());
                    }
                    doc.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
                }
            }
            return super.doTab(context, tabName, pageName);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Posiziona l'utente sul tab testata durante la creazione di un nuovo documento.
     */
    @Override
    public Forward doNuovo(ActionContext context) {
        super.doTab(context, "tab", getTabTestataName());
        return super.doNuovo(context);
    }

    /**
     * Inizializza la ricerca libera per l'aggiunta di nuovi beni al documento.
     */
    public Forward doAddToCRUDMain_DettBeniController(ActionContext context) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            if (bp.isDocumentoNonModificabile()) {
                bp.setMessage("Impossibile modificare un documento " + bp.getDoc().getStato());
                return context.findDefaultForward();
            }
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            validaTestataDocumento(doc);
            bp.getDettBeniController().validate(context);

            RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
            rlbp.setCanPerformSearchWithoutClauses(false);

            // Crea il prototype
            Inventario_beniBulk prototype = new Inventario_beniBulk();

            // Configura il freeSearchSet specifico per trasporto/rientro
            rlbp.setPrototype(prototype, null, null, "searchTrasportoRientro");

            //fl_dismesso = false
            CondizioneSempliceBulk condizioneFlDismesso = new CondizioneSempliceBulk(prototype, "searchTrasportoRientro");
            FieldProperty flDismessoField = prototype.getBulkInfo().getFieldProperty("fl_dismesso");
            condizioneFlDismesso.setFindFieldProperty(flDismessoField);
            condizioneFlDismesso.setOperator(SQLBuilder.EQUALS);
            condizioneFlDismesso.setLogicalOperator("AND");
            condizioneFlDismesso.setValue(Boolean.FALSE);

            // Aggiungi alla radice (sarà sempre applicata nella ricerca)
            rlbp.getCondizioneRadice().aggiungiCondizione(condizioneFlDismesso);

            String columnSetName = doc.getTiDocumento().equals("T") ? "righeTrasporto" : "righeRientro";
            rlbp.setColumnSet(columnSetName);

            context.addHookForward("searchResult", this, getBringBackMethod());
            context.addHookForward("filter", this, getBringBackMethod());
            return context.addBusinessProcess(rlbp);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il ritorno dalla ricerca e apre il selezionatore di beni.
     */
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
     * Elabora la risposta dell'utente (SI/NO/ANNULLA) per l'inclusione degli accessori nel flusso ricorsivo.
     */
    public Forward doRispostaRicorsivaGenerica(ActionContext context, int opt) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            boolean isEliminazione = bp.isUltimaOperazioneEliminazione();
            HookForward caller = (HookForward) context.getCaller();
            List<Inventario_beniBulk> beniDaElaborare = (List<Inventario_beniBulk>) caller.getParameter("beniDaElaborare");
            Integer index = (Integer) caller.getParameter("index");

            if (opt == OptionBP.CANCEL_BUTTON) {
                bp.getDettBeniController().reset(context);
                bp.setMessage(isEliminazione ? "Eliminazione annullata." : "Operazione annullata.");
                bp.resetOperazione(isEliminazione);
                return context.findDefaultForward();
            }

            if (opt == OptionBP.YES_BUTTON) {
                bp.elaboraBeneCorrente(context, isEliminazione, true);
            } else if (opt == OptionBP.NO_BUTTON) {
                bp.elaboraBeneCorrente(context, isEliminazione, false);
            }

            if (!isEliminazione) bp.getDettBeniController().reset(context);

            int nextIndex = index + 1;
            if (nextIndex < beniDaElaborare.size()) {
                return apriFlussoRicorsivoGenerico(context, bp, isEliminazione, beniDaElaborare, nextIndex);
            } else {
                finalizeFlusso(context, bp, isEliminazione, opt == OptionBP.YES_BUTTON);
                return context.findDefaultForward();
            }
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la rimozione dei beni selezionati dal dettaglio del documento.
     */
    public Forward doRemoveFromCRUDMain_DettBeniController(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            if (bp.isDocumentoNonModificabile()) {
                bp.setMessage("Impossibile modificare un documento " + bp.getDoc().getStato());
                return context.findDefaultForward();
            }
            bp.getDettBeniController().remove(context);
            if (bp.getPendingDelete() == null) return context.findDefaultForward();

            if (!bp.getPendingDelete().getPrincipaliSenza().isEmpty()) {
                OggettoBulk[] beniDaEliminare = bp.getPendingDelete().getPrincipaliSenza().toArray(new OggettoBulk[0]);
                bp.eliminaBeniDaDettagli(context, beniDaEliminare);
                bp.getDettBeniController().reset(context);
            }

            if (!bp.getPendingDelete().getPrincipaliConAccessori().isEmpty()) {
                List<Inventario_beniBulk> beniConAccessori = new ArrayList<>(bp.getPendingDelete().getPrincipaliConAccessori().keySet());
                return apriFlussoRicorsivoGenerico(context, bp, true, beniConAccessori, 0);
            }

            bp.resetOperazione(true);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Avvia il processo di aggiunta beni distinguendo tra beni semplici e beni con accessori.
     */
    protected Forward doSelezionaBeniGeneric(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            if (bp.getPendingAdd() == null) return context.findDefaultForward();

            aggiungiImmediatamenteBeniSemplici(context, bp);

            List<Inventario_beniBulk> beniConAccessoriDaConfermare = new ArrayList<>(bp.getPendingAdd().getPrincipaliConAccessori().keySet());
            if (beniConAccessoriDaConfermare.isEmpty()) {
                bp.getDettBeniController().reset(context);
                bp.setMessage("Aggiunta beni completata con successo");
                bp.resetOperazione(false);
                return context.findDefaultForward();
            }
            return apriFlussoRicorsivoGenerico(context, bp, false, beniConAccessoriDaConfermare, 0);
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Aggiunge direttamente i beni che non presentano accessori opzionali.
     */
    private void aggiungiImmediatamenteBeniSemplici(ActionContext context, CRUDTraspRientInventarioBP bp) throws BusinessProcessException {
        try {
            List<Inventario_beniBulk> beniSemplici = new ArrayList<>();
            beniSemplici.addAll(bp.getPendingAdd().getPrincipaliSenza());
            beniSemplici.addAll(bp.getPendingAdd().getAccessori());

            if (beniSemplici.isEmpty()) return;

            BitSet newSelection = new BitSet(bp.getPendingAdd().getBulks().length);
            for (Inventario_beniBulk beneSemplice : beniSemplici) {
                for (int i = 0; i < bp.getPendingAdd().getBulks().length; i++) {
                    if (bp.getPendingAdd().getBulks()[i] instanceof Inventario_beniBulk && ((Inventario_beniBulk) bp.getPendingAdd().getBulks()[i]).equalsByPrimaryKey(beneSemplice)) {
                        newSelection.set(i);
                        break;
                    }
                }
            }
            bp.modificaBeniConAccessoriComponente(context, bp.getPendingAdd().getBulks(), new BitSet(bp.getPendingAdd().getBulks().length), newSelection);
        } catch (ComponentException | RemoteException e) {
            throw bp.handleException(e);
        }
    }

    /**
     * Gestisce l'apertura della finestra di conferma per ogni bene con accessori nel flusso ricorsivo.
     */
    private Forward apriFlussoRicorsivoGenerico(ActionContext context, CRUDTraspRientInventarioBP bp, boolean isEliminazione, List<Inventario_beniBulk> beniDaElaborare, int index) throws ComponentException, RemoteException, BusinessProcessException {
        if (beniDaElaborare == null || index >= beniDaElaborare.size()) {
            finalizeFlusso(context, bp, isEliminazione, true);
            return context.findDefaultForward();
        }

        Inventario_beniBulk beneCorrente = beniDaElaborare.get(index);
        bp.setUltimaOperazioneEliminazione(isEliminazione);

        if (isEliminazione) bp.setIndexBeneCurrentePerEliminazione(index);
        else bp.setIndexBeneCurrentePerAggiunta(index);

        String messaggio = String.format("[Bene %d di %d]\n\n%s", index + 1, beniDaElaborare.size(), bp.getMessaggioSingoloBene(isEliminazione));
        int confirmType = isEliminazione ? OptionBP.CONFIRM_YES_NO_CANCEL : OptionBP.CONFIRM_YES_NO;
        openConfirm(context, messaggio, confirmType, "doRispostaRicorsivaGenerica");

        HookForward hookForward = (HookForward) context.findForward("option");
        if (hookForward != null) {
            hookForward.addParameter("beniDaElaborare", beniDaElaborare);
            hookForward.addParameter("index", index);
            hookForward.addParameter("beneCorrente", beneCorrente);
        }
        return context.findDefaultForward();
    }

    /**
     * Conclude il flusso di aggiunta o eliminazione resettando il Business Process.
     */
    private void finalizeFlusso(ActionContext context, CRUDTraspRientInventarioBP bp, boolean isEliminazione, boolean ultimaRispostaSI) throws ComponentException, RemoteException {
        bp.getDettBeniController().reset(context);
        bp.setMessage(isEliminazione ? "Eliminazione completata." : "Aggiunta beni completata con successo");
        bp.resetOperazione(isEliminazione);
    }

    /**
     * Esegue la procedura di invio del documento al servizio di firma HappySign.
     */
    public Forward doInviaInFirma(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            validaDocumentoPerFirma(doc);

            DocTraspRientFirmatariService firmatariService = SpringUtil.getBean("docTraspRientFirmatariService", DocTraspRientFirmatariService.class);
            firmatariService.popolaFirmatari(doc, (CNRUserContext) context.getUserContext());

            File pdfFile = bp.stampaDocTrasportoRientro(context.getUserContext(), doc);
            byte[] pdfBytes;
            try (FileInputStream fis = new FileInputStream(pdfFile)) {
                pdfBytes = IOUtils.toByteArray(fis);
            }

            salvaStampaSuCMIS(context, doc, pdfFile, false);

            DocTraspRientHappySignService happySignService = SpringUtil.getBean("docTraspRientHappySignService", DocTraspRientHappySignService.class);
            String uuidHappysign = happySignService.inviaDocumentoAdHappySign(doc, pdfBytes);

            doc.setIdFlussoHappysign(uuidHappysign);
            doc.setDataInvioFirma(new Timestamp(System.currentTimeMillis()));
            doc = getComponentSession(bp).changeStatoInInviato(context.getUserContext(), doc);

            bp.setModel(context, doc);
            bp.setStatus(VIEW);
            bp.setMessage("Documento inviato alla firma con successo. UUID HappySign: " + uuidHappysign);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Placeholder per il salvataggio del documento su storage CMIS.
     */
    private void salvaStampaSuCMIS(ActionContext context, Doc_trasporto_rientroBulk doc, File pdfFile, boolean isFirmato) throws Exception {
    }

    /**
     * Verifica la completezza dei dati prima di consentire l'invio alla firma.
     */
    private void validaDocumentoPerFirma(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc == null) throw new ValidationException("Documento non presente");
        if (doc.isAnnullato()) throw new ValidationException("Impossibile inviare alla firma un documento annullato");
        if (!doc.isInserito()) throw new ValidationException("Il documento deve essere in stato 'Inserito' per essere inviato in firma");
        if (doc.getDoc_trasporto_rientro_dettColl() == null || doc.getDoc_trasporto_rientro_dettColl().isEmpty()) throw new ValidationException("Il documento deve contenere almeno un bene");
        if (doc.getTipoMovimento() == null) throw new ValidationException("Tipo movimento non specificato");
        if (doc.getDataRegistrazione() == null) throw new ValidationException("Data registrazione non specificata");
        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) throw new ValidationException("Descrizione documento non specificata");
    }

    /**
     * Esegue l'annullamento del documento se non ancora inviato alla firma o già annullato.
     */
    @Override
    public Forward doElimina(ActionContext context) throws RemoteException {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
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

    /**
     * Recupera la sessione EJB specifica per la gestione dei documenti di trasporto.
     */
    protected DocTrasportoRientroComponentSession getComponentSession(CRUDTraspRientInventarioBP bp) throws Exception {
        return (DocTrasportoRientroComponentSession) bp.createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession", DocTrasportoRientroComponentSession.class);
    }

    /**
     * Utility per il confronto di uguaglianza tra oggetti.
     */
    protected <T> boolean valoreUguale(T oldValue, T newValue) {
        return Objects.equals(oldValue, newValue);
    }

    /**
     * Estrae il codice anagrafico associato a un Terzo.
     */
    protected Integer getCdAnag(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        if (terzo == null) return null;
        it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk anag = terzo.getAnagrafico();
        return (anag != null) ? anag.getCd_anag() : null;
    }

    /**
     * Verifica la presenza di beni nei dettagli del documento.
     */
    protected boolean hasBeniInDettaglio(Doc_trasporto_rientroBulk doc) {
        return doc.getDoc_trasporto_rientro_dettColl() != null && !doc.getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    /**
     * Rimuove massivamente tutti i beni dal dettaglio del documento corrente.
     */
    protected void eliminaBeniSePresenti(ActionContext context, CRUDTraspRientInventarioBP bp, Doc_trasporto_rientroBulk doc, String messaggio) throws Exception {
        BulkList dettagli = getComponentSession(bp).getDetailsFor(context.getUserContext(), doc);
        if (!dettagli.isEmpty()) {
            bp.getDettBeniController().removeAll(context);
            doc.getDoc_trasporto_rientro_dettColl().clear();
            bp.setMessage(messaggio);
        }
    }

    /**
     * Valida i dati obbligatori della testata prima di operazioni sui dettagli.
     */
    protected void validaTestataDocumento(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc.getTipoMovimento() == null) throw new ValidationException("Attenzione: specificare un tipo di movimento nella testata.");
        if (doc.getDataRegistrazione() == null) throw new ValidationException("Attenzione: specificare la " + getDataLabel() + ".");
        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) throw new ValidationException("Attenzione: indicare una Descrizione per il Documento.");
    }

    /**
     * Gestisce la stampa di un documento di trasporto o rientro.
     */
    public Forward doStampaDocTraspRient(ActionContext context) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = (CRUDTraspRientInventarioBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk docTrasporto = (Doc_trasporto_rientroBulk) bp.getModel();

            ParametricPrintBP ppbp = (ParametricPrintBP) context.createBusinessProcess("StampaDocTraspRientBP");
            Stampa_doc_trasporto_rientroBulk stampa = (Stampa_doc_trasporto_rientroBulk) ppbp.createNewBulk(context);

            if (docTrasporto != null && docTrasporto.getPgDocTrasportoRientro() != null) {
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
     * Esegue il salvataggio definitivo del documento senza ricaricare gli allegati.
     */
    public Forward doSalvaDefinitivo(ActionContext context) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);

            // Imposta il flag per evitare il reload degli allegati durante il save
            bp.setSkipAllegatiReload(true);
            try {
                bp.salvaDefinitivo(context);
                bp.setMessage("Documento salvato definitivamente con successo.");
                return context.findDefaultForward();
            } finally {
                // Resetta sempre il flag dopo il salvataggio
                bp.setSkipAllegatiReload(false);
            }
        } catch (Throwable ex) {
            return handleException(context, ex);
        }
    }

    /**
     * Esegue il salvataggio standard del documento senza ricaricare gli allegati.
     */
    @Override
    public Forward doSalva(ActionContext context) {
        try {
            fillModel(context);
            CRUDTraspRientInventarioBP bp = getBP(context);

            // Imposta il flag per evitare il reload degli allegati durante il save
            bp.setSkipAllegatiReload(true);
            try {
                bp.save(context);
                return context.findDefaultForward();
            } finally {
                // Resetta sempre il flag dopo il salvataggio
                bp.setSkipAllegatiReload(false);
            }
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce il cambio dell'assegnatario smartworking e rimuove i beni associati.
     */
    public Forward doOnTerzoSmartworkingChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            Integer oldValue = getCdTerzo(doc.getTerzoSmartworking());
            fillModel(context);

            Integer cdTerzo = doc.getTerzoSmartworking() != null ? doc.getTerzoSmartworking().getCd_terzo() : null;

            if (cdTerzo != null) {
                try {
                    TerzoBulk terzoCompleto = (TerzoBulk) bp.createComponentSession().findByPrimaryKey(context.getUserContext(), new TerzoBulk(cdTerzo));
                    if (terzoCompleto != null) {
                        doc.setTerzoSmartworking(terzoCompleto);
                    }
                } catch (Exception e) {
                    throw new it.cnr.jada.comp.ApplicationException("Errore nel caricamento dell'Assegnatario: " + e.getMessage());
                }
            }

            Integer newValue = getCdTerzo(doc.getTerzoSmartworking());

            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            eliminaBeniSePresenti(context, bp, doc, "Assegnatario Smartworking modificato. Beni precedenti rimossi.");
            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Gestisce la selezione del tipo movimento e resetta beni e soggetti coinvolti.
     */
    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            boolean wasSmartworking = doc.isSmartworking();
            fillModel(context);

            try {
                getComponentSession(bp).initializeKeysAndOptionsInto(context.getUserContext(), doc);
            } catch (Exception e) {
                throw new BusinessProcessException("Errore ricaricamento tipi movimento", e);
            }

            boolean isSmartworking = doc.isSmartworking();

            if (doc.getCrudStatus() != OggettoBulk.TO_BE_CREATED) {
                eliminaBeniSePresenti(context, bp, doc, "Tipo Movimento modificato. Beni precedenti rimossi.");
            }

            if (wasSmartworking && !isSmartworking) {
                doc.setTerzoSmartworking(null);
                doc.setAnagSmartworking(null);
            } else if (!wasSmartworking && isSmartworking) {
                doc.setTerzoIncRitiro(null);
                doc.setAnagIncRitiro(null);
            } else {
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
     * Gestisce la variazione del tipo di ritiro resettando beni e dipendente incaricato.
     */
    public Forward doOnTipoRitiroChange(ActionContext context) {
        try {
            CRUDTraspRientInventarioBP bp = getBP(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            String oldValue = doc.getTipoRitiro();
            fillModel(context);
            String newValue = doc.getTipoRitiro();

            if (oldValue != null && valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            String messaggio = null;
            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(oldValue) && Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(newValue)) {
                messaggio = "Cambio da Vettore a Incaricato. Beni precedenti rimossi.";
            } else if (Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(oldValue) && Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
                messaggio = "Cambio da Incaricato a Vettore. Beni precedenti rimossi.";
            } else if (oldValue != null) {
                messaggio = "Tipo Ritiro modificato. Beni precedenti rimossi.";
            }

            if (messaggio != null) {
                eliminaBeniSePresenti(context, bp, doc, messaggio);
            }

            doc.setTerzoIncRitiro(null);
            doc.setAnagIncRitiro(null);

            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_INCARICATO.equals(newValue)) {
                doc.setNominativoVettore(null);
            } else if (!Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
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

    /**
     * Estrae il codice del terzo.
     */
    protected Integer getCdTerzo(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        return (terzo != null) ? terzo.getCd_terzo() : null;
    }

    /**
     * Avvia la ricerca dell'assegnatario smartworking.
     */
    public Forward doSearchFindAnagSmartworking(ActionContext context) {
        return search(context, getFormField(context, "main.findAnagSmartworking"), null);
    }

    /**
     * Pulisce i campi relativi all'assegnatario smartworking.
     */
    public Forward doBlankSearchFindAnagSmartworking(ActionContext context, Doc_trasporto_rientroBulk doc) {
        doc.setAnagSmartworking(new AnagraficoBulk());
        doc.setTerzoSmartworking(null);
        return context.findDefaultForward();
    }

    /**
     * Associa l'anagrafico selezionato come assegnatario smartworking e ricarica il terzo.
     */
    public Forward doBringBackSearchFindAnagSmartworking(ActionContext context, Doc_trasporto_rientroBulk doc, AnagraficoBulk anagSelezionato) {
        try {
            if (anagSelezionato != null && anagSelezionato.getCd_anag() != null) {
                CRUDTraspRientInventarioBP bp = getBP(context);
                Integer oldCdAnag = getCdAnag(doc.getAnagSmartworking());
                doc.setAnagSmartworking(anagSelezionato);
                doc.setTerzoSmartworking(caricaTerzoDaAnagrafico(context, anagSelezionato));

                if (!valoreUguale(oldCdAnag, anagSelezionato.getCd_anag())) {
                    eliminaBeniSePresenti(context, bp, doc, "Assegnatario Smartworking modificato. Beni precedenti rimossi.");
                }
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Avvia la ricerca del dipendente incaricato al ritiro.
     */
    public Forward doSearchFindAnagIncRitiro(ActionContext context) {
        return search(context, getFormField(context, "main.findAnagIncRitiro"), null);
    }

    /**
     * Pulisce i campi relativi al dipendente incaricato al ritiro.
     */
    public Forward doBlankSearchFindAnagIncRitiro(ActionContext context, Doc_trasporto_rientroBulk doc) {
        doc.setAnagIncRitiro(new AnagraficoBulk());
        doc.setTerzoIncRitiro(null);
        return context.findDefaultForward();
    }

    /**
     * Associa l'anagrafico selezionato come dipendente incaricato e ricarica il terzo.
     */
    public Forward doBringBackSearchFindAnagIncRitiro(ActionContext context, Doc_trasporto_rientroBulk doc, AnagraficoBulk anagSelezionato) {
        try {
            if (anagSelezionato != null && anagSelezionato.getCd_anag() != null) {
                CRUDTraspRientInventarioBP bp = getBP(context);
                Integer oldCdAnag = getCdAnag(doc.getAnagIncRitiro());
                doc.setAnagIncRitiro(anagSelezionato);
                doc.setTerzoIncRitiro(caricaTerzoDaAnagrafico(context, anagSelezionato));

                if (!valoreUguale(oldCdAnag, anagSelezionato.getCd_anag())) {
                    eliminaBeniSePresenti(context, bp, doc, "Dipendente Incaricato modificato. Beni precedenti rimossi.");
                }
            }
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    /**
     * Carica il TerzoBulk associato all'anagrafico tramite il Business Process.
     */
    private TerzoBulk caricaTerzoDaAnagrafico(ActionContext context, AnagraficoBulk anagrafico) throws Exception {
        if (anagrafico == null || anagrafico.getCd_anag() == null) return null;
        return getBP(context).caricaTerzoDaAnagrafico(context, anagrafico);
    }

    /**
     * Estrae il codice anagrafico.
     */
    private Integer getCdAnag(AnagraficoBulk anag) {
        return anag != null ? anag.getCd_anag() : null;
    }

}