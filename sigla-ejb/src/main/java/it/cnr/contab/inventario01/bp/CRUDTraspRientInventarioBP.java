package it.cnr.contab.inventario01.bp;

import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.GenerazioneReportException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.EmptyRemoteIterator;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;
import it.cnr.jada.util.jsp.Button;
import it.cnr.si.spring.storage.StorageObject;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk.STATO_INSERITO;

//riscrivi logica per la gestione degli allegati e dei file nel documentale azure in CRUDTraspRientInventarioBP come è stato fatto nel bp CaricFlStipBP
public abstract class CRUDTraspRientInventarioBP<T extends AllegatoDocTraspRientroBulk, K extends Doc_trasporto_rientroBulk> extends AllegatiCRUDBP<T, K>
        implements SelectionListener {

    private static final DateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final String REPORT_DOC_TRASPORTO_RIENTRO = "doc_trasporto_rientro.jasper";

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private String tipo;
    private boolean isAmministratore = false;
    private boolean isVisualizzazione = false;

    private boolean isGestioneInvioInFirmaAttiva = false;

    // ==================== PENDING SELECTION ====================

    public static class PendingSelection {
        Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori = new LinkedHashMap<>();
        List<Inventario_beniBulk> accessori = new ArrayList<>();
        List<Inventario_beniBulk> principaliSenza = new ArrayList<>();
        OggettoBulk[] bulks = null;
        BitSet oldSel = null;
        BitSet newSel = null;
        BitSet selectionAccumulata = null; // per accumulare le selezioni progressive

        void clear() {
            principaliConAccessori.clear();
            accessori.clear();
            principaliSenza.clear();
            bulks = null;
            oldSel = null;
            newSel = null;
            selectionAccumulata = null;
        }

        public PendingSelection() {
        }

        public PendingSelection(Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori,
                                List<Inventario_beniBulk> accessori,
                                List<Inventario_beniBulk> principaliSenza,
                                OggettoBulk[] bulks,
                                BitSet oldSel,
                                BitSet newSel) {
            this.principaliConAccessori = principaliConAccessori;
            this.accessori = accessori;
            this.principaliSenza = principaliSenza;
            this.bulks = bulks;
            this.oldSel = oldSel;
            this.newSel = newSel;
            // null-safe
            this.selectionAccumulata = oldSel != null ? (BitSet) oldSel.clone() : new BitSet();
        }

        boolean isEmpty() {
            return principaliConAccessori.isEmpty() && accessori.isEmpty() && principaliSenza.isEmpty();
        }

        public Map<Inventario_beniBulk, List<Inventario_beniBulk>> getPrincipaliConAccessori() {
            return principaliConAccessori;
        }

        public void setPrincipaliConAccessori(Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori) {
            this.principaliConAccessori = principaliConAccessori;
        }

        public List<Inventario_beniBulk> getAccessori() {
            return accessori;
        }

        public void setAccessori(List<Inventario_beniBulk> accessori) {
            this.accessori = accessori;
        }

        public List<Inventario_beniBulk> getPrincipaliSenza() {
            return principaliSenza;
        }

        public void setPrincipaliSenza(List<Inventario_beniBulk> principaliSenza) {
            this.principaliSenza = principaliSenza;
        }

        public OggettoBulk[] getBulks() {
            return bulks;
        }

        public void setBulks(OggettoBulk[] bulks) {
            this.bulks = bulks;
        }

        public BitSet getOldSel() {
            return oldSel;
        }

        public void setOldSel(BitSet oldSel) {
            this.oldSel = oldSel;
        }

        public BitSet getNewSel() {
            return newSel;
        }

        public void setNewSel(BitSet newSel) {
            this.newSel = newSel;
        }

        public BitSet getSelectionAccumulata() {
            return selectionAccumulata;
        }

        public void setSelectionAccumulata(BitSet selectionAccumulata) {
            this.selectionAccumulata = selectionAccumulata;
        }
    }

    public PendingSelection pendingAdd = null;
    public PendingSelection pendingDelete = null;

    private int indexBeneCurrentePerEliminazione = 0;
    private int indexBeneCurrentePerAggiunta = 0;
    private boolean ultimaOperazioneEliminazione = false;
    private CompoundFindClause clauses;

    // ========================================
    // COSTRUTTORI
    // ========================================

    public CRUDTraspRientInventarioBP() {
        super();
    }

    public CRUDTraspRientInventarioBP(String function) {
        super(function);
    }

    // ========================================
    // METODI ASTRATTI
    // ========================================

    protected abstract String getDettagliControllerName();

    protected abstract String getEditDettagliControllerName();

    public abstract String getMainTabName();

    public abstract String getLabelData_registrazione();

    protected abstract void inizializzaSelezioneComponente(ActionContext context) throws ComponentException, RemoteException;

    protected abstract void annullaModificaComponente(ActionContext context) throws ComponentException, RemoteException;

    public abstract void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks, BitSet oldSelection, BitSet newSelection) throws ComponentException, RemoteException, BusinessProcessException;

    public PendingSelection getPendingDelete() {
        return pendingDelete;
    }

    private void setPendingDelete(PendingSelection pendingDelete) {
        this.pendingDelete = pendingDelete;
    }

    // ========================================
    // INIT
    // ========================================

    protected void init(Config config, ActionContext context)
            throws BusinessProcessException {

        Integer esercizio = CNRUserContext.getEsercizio(context.getUserContext());
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);

        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());

        try {
            // Session documenti trasporto/rientro
            DocTrasportoRientroComponentSession docSession = (DocTrasportoRientroComponentSession)
                    createComponentSession(
                            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class
                    );

            setVisualizzazione(docSession.isEsercizioCOEPChiuso(context.getUserContext()));
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));

            // verifica abilitazione firma
            Configurazione_cnrComponentSession confSession = (Configurazione_cnrComponentSession)
                    createComponentSession(
                            "CNRCONFIG00_EJB_Configurazione_cnrComponentSession",
                            Configurazione_cnrComponentSession.class
                    );

            boolean abilitazioneInvioFirma = confSession.isGestioneInvioInFirmaDocTRAttivo(
                    context.getUserContext()
            );
            setGestioneInvioInFirmaAttiva(abilitazioneInvioFirma);

        } catch (ComponentException e) {
            throw handleException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }

        super.init(config, context);


        initVariabili(context, getTipo());
        resetTabs();
    }

    public boolean isGestioneInvioInFirmaAttiva() {
        return isGestioneInvioInFirmaAttiva;
    }

    public void setGestioneInvioInFirmaAttiva(boolean gestioneInvioInFirmaAttiva) {
        this.isGestioneInvioInFirmaAttiva = gestioneInvioInFirmaAttiva;
    }

    public void initVariabili(ActionContext context, String Tipo) {
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        try {
            // ========== 1. INIZIALIZZAZIONE STANDARD ==========
            createComponentSession(
                    "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                    DocTrasportoRientroComponentSession.class
            );

            setAmministratore(
                    UtenteBulk.isAmministratoreInventario(context.getUserContext())
            );

            // ========== 2. CHIAMA IL METODO PADRE (include allegati) ==========
            bulk = super.initializeModelForEdit(context, bulk);

            // ========== 3. CONTROLLA SE DOCUMENTO È ANNULLATO ==========
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

            if (doc != null && Doc_trasporto_rientroBulk.STATO_ANNULLATO.equals(doc.getStato())) {
                setErrorMessage("⚠️ ATTENZIONE: Documento Annullato - Nessuna modifica consentita");
            }

            return bulk;

        } catch (ComponentException | RemoteException e1) {
            throw handleException(e1);
        }
    }


    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    public void resetTabs() {
        setTab("tab", getMainTabName());
    }


    /**
     * Override per validare i dati prima di passare al tab dei dettagli.
     * Se la validazione fallisce, rimane sul tab corrente e mostra il messaggio di errore.
     */
    @Override
    public void setTab(String tabName, String tabValue) {

        // intercetta SOLO il cambio tab reale
        if ("tab".equals(tabName) && tabValue != null) {

            Doc_trasporto_rientroBulk doc = getDoc();

            // ======= TAB DETTAGLI / ALLEGATI =======
            boolean isTabDettagli = tabValue.endsWith("Dettaglio") || tabValue.endsWith("Allegati");

            if (isTabDettagli) {

                // ======= FASE DI RICERCA =======
                if (doc != null && doc.getCrudStatus() == OggettoBulk.UNDEFINED) {
                    setErrorMessage("Non è possibile cambiare tab: in questa fase è consentita solo la ricerca.");
                    return;
                }

                // ======= ALTRE FASI: VALIDAZIONE =======
                try {
                    validaDatiPerDettagli();
                } catch (ApplicationException e) {
                    setErrorMessage(e.getMessage());
                    return;
                }
            }
        }

        super.setTab(tabName, tabValue);
    }


    // ========================================
    // GETTER E SETTER
    // ========================================

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAmministratore() {
        return isAmministratore;
    }

    public void setAmministratore(boolean isAmministratore) {
        this.isAmministratore = isAmministratore;
    }

    public boolean isVisualizzazione() {
        return isVisualizzazione;
    }

    public void setVisualizzazione(boolean isVisualizzazione) {
        this.isVisualizzazione = isVisualizzazione;
    }

    // ========================================
    // GESTIONE STATO
    // ========================================

    public Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    public boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    public boolean isDocumentoInviatoInFirma() {
        return getDoc() != null && getDoc().isInviatoInFirma();
    }

    public boolean isDocumentoDefinitivo() {
        return getDoc() != null && getDoc().isDefinitivo();
    }

    public boolean isDocumentoNonModificabile() {
        return isDocumentoAnnullato() || isDocumentoInviatoInFirma() || isDocumentoDefinitivo();
    }


    @Override
    public boolean isEditable() {
        if (isDocumentoNonModificabile()) {
            return false;
        }
        return !isVisualizzazione() && super.isEditable();
    }

    public boolean isInputReadonly() {
        return isDocumentoNonModificabile() || isVisualizzazione();
    }

// ========================================
// VISIBILITÀ UI
// ========================================

    private boolean hasValidModel() {
        return getModel() != null && getDoc() != null;
    }

    // Tipo Ritiro visibile SOLO se NON è Smartworking
    public boolean isTipoRitiroVisible() {
        return hasValidModel() && !isSmartworking();
    }

    // Assegnatario Smartworking visibile SOLO se è Smartworking
    public boolean isTerzoSmartworkingVisible() {
        return hasValidModel() && isSmartworking();
    }

    // Tutti gli altri campi visibili SOLO se NON è Smartworking E hanno le loro condizioni
    public boolean isDestinazioneVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().hasTipoRitiroSelezionato();
    }

    public boolean isAssegnatarioVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().isRitiroIncaricato();
    }

    public boolean isNominativoVettoreVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().isNominativoVettoreVisible();
    }

    public boolean isNoteAbilitate() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().getTipoMovimento() != null
                && getDoc().getTipoMovimento().isAbilitaNote();
    }


    public boolean isStatoVisible() {
        return hasValidModel() && getDoc().getStato() != null && !isInserting();
    }

    public boolean isNoteVisible() {
        return hasValidModel() && getDoc().isNoteRitiroEnabled();
    }


    // ========================================
    // READONLY FIELDS
    // ========================================

    public boolean isTipoMovimentoReadOnly() {
        return isCampiCriticiReadOnly();
    }

    private boolean isCampiCriticiReadOnly() {
        if (isDocumentoNonModificabile()) return true;
        return isEditing() && hasValidModel()
                && getDoc().getDoc_trasporto_rientro_dettColl() != null
                && !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty();
    }


    public boolean isQuantitaEnabled() {
        return !isDocumentoNonModificabile() && (isEditing() || isInserting());
    }

    // ========================================
    // VISIBILITÀ BOTTONI
    // ========================================


    /**
     * Controlla se il bottone ELIMINA deve essere abilitato.
     * <p>
     * LOGICA:
     * - RIENTRO DEFINITIVO: sempre DISABILITATO
     * - TRASPORTO DEFINITIVO: DISABILITATO solo se ha UN rientro associato
     * - TRASPORTO DEFINITIVO: ABILITATO (può essere annullato)
     * - Altri stati: disabilitato se ANNULLATO o INVIATO IN FIRMA
     */
    @Override
    public boolean isDeleteButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();

        // ========== DOCUMENTI DEFINITIVI ==========
        if (isDocumentoDefinitivo()) {

            // Per RIENTRO definitivo: SEMPRE DISABILITATO
            if (doc instanceof DocumentoRientroBulk) {
                return false; // Mai abilitato per rientri definitivi
            }

            // Per TRASPORTO definitivo: disabilitato SOLO se ha rientro associato
            if (doc instanceof DocumentoTrasportoBulk) {
                return !haRientroAssociato();
            }
            return false;
        }

        // ========== ALTRI STATI (INSERITO, INVIATO) ==========
        return !isDocumentoNonModificabile();
    }


    /**
     * Verifica se il documento di TRASPORTO ha almeno un rientro associato.
     * Ogni dettaglio può avere al massimo UN rientro associato (relazione 1:1).
     *
     * @return true se esiste almeno un rientro associato, false altrimenti
     */
    private boolean haRientroAssociato() {
        // Solo per documenti di TRASPORTO
        if (!(getDoc() instanceof DocumentoTrasportoBulk)) {
            return false;
        }

        Doc_trasporto_rientroBulk doc = getDoc();

        // Verifica che ci siano dettagli
        if (doc == null ||
                doc.getDoc_trasporto_rientro_dettColl() == null ||
                doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            return false;
        }

        // Controlla se almeno un dettaglio ha un rientro associato
        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            if (obj instanceof DocumentoTrasportoDettBulk) {
                DocumentoTrasportoDettBulk dettaglio = (DocumentoTrasportoDettBulk) obj;

                // Se esiste un riferimento a un documento di rientro (relazione 1:1)
                if (dettaglio.getDocRientroDettRif() != null) {
                    return true; // Trovato almeno un rientro, basta per bloccare l'eliminazione
                }
            }
        }

        return false;
    }

    /**
     * Controlla se il bottone SALVA deve essere abilitato.
     * In stato DEFINITIVO: DEVE ESSERE DISABILITATO
     */
    @Override
    public boolean isSaveButtonEnabled() {

        return !isDocumentoNonModificabile();
    }

    /**
     * Controlla se il bottone SALVA deve essere nascosto.
     * In stato DEFINITIVO: DEVE ESSERE NASCOSTO
     */
    @Override
    public boolean isSaveButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        return doc != null && (doc.isDefinitivo() || doc.isAnnullato());
    }


    /**
     * Controlla se il bottone NUOVO deve essere abilitato.
     * SEMPRE ABILITATO (indipendentemente dallo stato del documento corrente)
     */
    @Override
    public boolean isNewButtonEnabled() {
        return !isVisualizzazione();
    }

    /**
     * Controlla se il bottone STAMPA DOCUMENTO deve essere nascosto.
     * Il bottone deve essere visibile solo se il documento esiste ed è salvato.
     */
    public boolean isStampaDocButtonHidden() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        return doc == null || doc.getPgDocTrasportoRientro() == null;
    }

    /**
     * Controlla se il bottone STAMPA DOCUMENTO deve essere abilitato.
     * In stato DEFINITIVO: DEVE ESSERE ABILITATO
     */
    public boolean isStampaDocButtonEnabled() {
        return !isStampaDocButtonHidden() && !isVisualizzazione();
    }

    /**
     * Controlla se il bottone SALVA DEFINITIVO deve essere nascosto.
     * Nasconde il bottone se:
     * - Documento non ancora salvato (PG null)
     * - Documento già in stato DEFINITIVO
     */
    public boolean isSalvaDefinitivoButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        return doc == null
                || doc.getPgDocTrasportoRientro() == null
                || isDocumentoNonModificabile();

    }

    /**
     * Controlla se il bottone SALVA DEFINITIVO deve essere abilitato.
     * Abilita il bottone se:
     * - Documento in stato INSERITO
     * - Non annullato
     * - Ha dettagli
     * - Ha almeno un allegato firmato (controllo UI-only, validazione reale nel Component)
     */
    public boolean isSalvaDefinitivoButtonEnabled() {

        Doc_trasporto_rientroBulk doc = getDoc();

        // ========== VALIDAZIONI UI ==========
        return doc != null
                && doc.isInserito()
                && !doc.isAnnullato()
                && doc.hasDettagli()
                && hasAllegatoFirmato()  // Controllo UI-only per user feedback
                && !isVisualizzazione();
    }

    /**
     * Verifica presenza allegato firmato per controllo UI.
     * NOTA: Questa è solo una validazione UI per dare feedback immediato all'utente.
     * La validazione DEFINITIVA avviene nel Component.
     */
    private boolean hasAllegatoFirmato() {
        // Verifica modello valido
        if (getModel() == null) {
            return false;
        }

        // Recupera documento
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        // Determina l'aspect atteso in base al tipo di documento
        String aspectFirmatoAtteso;
        if (doc instanceof DocumentoTrasportoBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else if (doc instanceof DocumentoRientroBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else {
            return false;
        }

        // Cerca un allegato che abbia l'aspect di documento firmato
        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) obj;
                String aspectName = allegato.getAspectName();

                // Nel BP controlliamo NORMAL o isEditing() per feedback UI immediato
                if (aspectName != null
                        && aspectName.equals(aspectFirmatoAtteso)
                        && (OggettoBulk.NORMAL == allegato.getCrudStatus() || isEditing())) {
                    return true;
                }
            }
        }

        return false;
    }


    // ========================================
// VISIBILITÀ UI - SMARTWORKING
// ========================================

    /**
     * Controlla se il tipo movimento selezionato è Smartworking
     */
    public boolean isSmartworking() {
        return hasValidModel() && getDoc().isSmartworking();
    }


    // ========================================
    // PRINT
    // ========================================

    @Override
    protected void initializePrintBP(ActionContext actioncontext, AbstractPrintBP abstractprintbp) {
        OfflineReportPrintBP printbp = (OfflineReportPrintBP) abstractprintbp;
        printbp.setReportName("/cnrdocamm/docamm/doc_trasporto_rientro.jasper");

        final Doc_trasporto_rientroBulk docT = Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("Modello vuoto!"));

        Print_spooler_paramBulk param;

        param = new Print_spooler_paramBulk();
        param.setNomeParam("esercizio");
        param.setValoreParam(Optional.ofNullable(docT.getEsercizio()).map(Object::toString).orElse(null));
        param.setParamType(Integer.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("pg_inventario");
        param.setValoreParam(Optional.ofNullable(docT.getPgInventario()).map(Object::toString).orElse(null));
        param.setParamType(Long.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("ti_documento");
        param.setValoreParam(docT.getTiDocumento());
        param.setParamType(String.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("pg_doc_trasporto_rientro");
        param.setValoreParam(Optional.ofNullable(docT.getPgDocTrasportoRientro()).map(Object::toString).orElse(null));
        param.setParamType(Long.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    // ========================= TOOLBAR =========================


    @Override
    protected Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(getClass());

        return Stream.concat(
                Arrays.stream(super.createToolbar()),
                Stream.of(
                        new Button(props, "CRUDToolbar.inviaInFirma"),
                        new Button(props, "CRUDToolbar.salvaDefinitivo"),
                        new Button(props, "CRUDToolbar.stampaDoc")
                )
        ).toArray(Button[]::new);
    }

    @Override
    public boolean isPrintButtonHidden() {
        return true;
    }

    /**
     * Determina se il pulsante "Invia in Firma" deve essere nascosto.
     *
     * @return true = nascondi pulsante, false = mostra pulsante
     */
    public boolean isInviaInFirmaButtonHidden() {
        int status = getModel() != null ? getModel().getCrudStatus() : -1;
        Doc_trasporto_rientroBulk doc = getDoc();

        return !isGestioneInvioInFirmaAttiva()
                || getModel() == null
                || isDocumentoNonModificabile()
                || (status != OggettoBulk.NORMAL && status != OggettoBulk.TO_BE_UPDATED)
                || doc == null
                || doc.getStato() == null
                || !STATO_INSERITO.equals(doc.getStato());
    }


    // ==================== CONTROLLERS ====================

    protected final RemoteDetailCRUDController dettBeniController = new RemoteDetailCRUDController(
            "DettBeniController",
            Inventario_beniBulk.class,
            "",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        public String getName() {
            return getDettagliControllerName();
        }

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            if (isInserting() || isEditing() || isViewing()) {
                try {
                    return selectDettaglibyClause(context);
                } catch (BusinessProcessException e) {
                    throw new RuntimeException("Errore caricamento dettagli beni", e);
                }
            }
            return new EmptyRemoteIterator();
        }

        @Override
        public void removeAll(ActionContext context) throws ValidationException, BusinessProcessException {
            if (isDocumentoNonModificabile()) {
                throw new ValidationException("Impossibile modificare un documento " + getDoc().getStato());
            }

            // ========== SEMPRE SU DETTAGLIO ==========
            eliminaTuttiBeniDaDettagli(context);
            getDettBeniController().reset(context);
            getDettBeniController().resync(context);
        }


        @Override
        public void removeDetails(ActionContext context, OggettoBulk[] details)
                throws BusinessProcessException {

            if (isDocumentoNonModificabile()) {
                throw new BusinessProcessException("Impossibile modificare un documento " + getDoc().getStato());
            }

            try {
                PendingSelection ps = new PendingSelection();
                ps.bulks = details;

                // ========== CLASSIFICA I BENI ==========
                for (OggettoBulk o : details) {
                    Inventario_beniBulk bene = (Inventario_beniBulk) o;

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
                        // ========== CERCA ACCESSORI NEI DETTAGLI SALVATI ==========
                        List<Inventario_beniBulk> found = getComp().cercaBeniAccessoriNeiDettagliSalvati(
                                context.getUserContext(),
                                getDoc(),
                                bene
                        );

                        if (found != null && !found.isEmpty()) {
                            ps.principaliConAccessori.put(bene, found);
                        } else {
                            ps.principaliSenza.add(bene);
                        }
                    }
                }

                // ========== GESTIONE ELIMINAZIONE ==========
                if (ps.principaliConAccessori.isEmpty()) {
                    // Nessun accessorio → elimina direttamente DAI DETTAGLI
                    eliminaBeniDaDettagli(context, details);
                } else {
                    // Ci sono accessori → avvia flusso ricorsivo
                    pendingDelete = ps;
                }

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }

    };

    public abstract Class getDocumentoClassDett();

    protected final RemoteDetailCRUDController editDettController = new RemoteDetailCRUDController(
            "editDettController",
            getDocumentoClassDett(),
            "",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        public String getName() {
            return getEditDettagliControllerName();
        }

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {

            // ========== CARICA DETTAGLI ANCHE PER DOCUMENTI ANNULLATI ==========
            if (isInserting() || isEditing() || isViewing()) {
                try {
                    return selectEditDettaglibyClause(context);
                } catch (BusinessProcessException e) {
                    throw new RuntimeException("Errore caricamento dettagli modifica", e);
                }
            }

            // Solo in inserimento ritorna vuoto
            return new EmptyRemoteIterator();
        }
    };


    // ==================== GESTIONE DETTAGLI ====================

    protected RemoteIterator selectEditDettaglibyClause(ActionContext context)
            throws BusinessProcessException {
        try {

            CompoundFindClause filters =
                    ((RemoteDetailCRUDController) getEditDettController()).getFilter();
            return getComp().selectEditDettagliTrasporto(context.getUserContext(), getDoc(),
                    getDocumentoClassDett(), filters);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    public RemoteIterator selectDettaglibyClause(ActionContext context)
            throws BusinessProcessException {
        try {
            return getComp().selectBeniAssociatiByClause(
                    context.getUserContext(),
                    getDoc(),
                    Inventario_beniBulk.class
            );
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    // ========================= WORKFLOW =========================

    public void inviaAllaFirma(ActionContext context) throws BusinessProcessException {
        validaStatoPerFirma();
        try {
            Doc_trasporto_rientroBulk doc = getComp().changeStatoInInviato(context.getUserContext(), getDoc());
            setModel(context, doc);
            setStatus(VIEW);
            setMessage("Documento inviato in Firma");
        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    public void annullaDoc(ActionContext context) throws BusinessProcessException {
        //validaStatoPerFirma();
        try {
            Doc_trasporto_rientroBulk doc = getComp().annullaDocumento(context.getUserContext(), getDoc());
            setModel(context, doc);
            setStatus(VIEW);
            setMessage("Documento annullato");
        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void validaStatoPerFirma() throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile inviare in firma un documento annullato");
        }
        if (!getDoc().isInserito()) {
            throw new BusinessProcessException("Il documento deve essere in stato 'Inserito' per essere inviato in firma");
        }
    }

    // ========================= SELECTION LISTENER =========================

    @Override
    public void clearSelection(ActionContext context) throws BusinessProcessException {
        try {
            annullaModificaComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void deselectAll(ActionContext context) {
        // no-op
    }

    @Override
    public BitSet getSelection(ActionContext context, OggettoBulk[] bulks, BitSet currentSelection)
            throws BusinessProcessException {
        return currentSelection;
    }

    @Override
    public void initializeSelection(ActionContext context) throws BusinessProcessException {
        if (isDocumentoNonModificabile()) return;
        try {
            inizializzaSelezioneComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void selectAll(ActionContext context) throws BusinessProcessException {

        if (isDocumentoNonModificabile()) {
            throw new BusinessProcessException("Impossibile modificare il documento");
        }

        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            // ========== CHIAMA IL METODO BATCH ==========
            getComp().selezionaTuttiBeni(context.getUserContext(), doc, getClauses());

            setClauses(null);

            // ========== RICARICA LA TABELLA ==========
            getDettBeniController().reset(context);
            getDettBeniController().resync(context);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Modifica il metodo setSelection per distinguere tra TRASPORTO e RIENTRO
     * nella ricerca degli accessori.
     */

    @Override
    public BitSet setSelection(ActionContext context, OggettoBulk[] bulks,
                               BitSet oldSelection, BitSet newSelection)
            throws BusinessProcessException {

        if (isDocumentoNonModificabile()) {
            throw new BusinessProcessException("Impossibile modificare documento");
        }

        try {
            fillModel(context);
            PendingSelection ps = new PendingSelection();
            ps.bulks = bulks;
            ps.oldSel = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();
            ps.newSel = newSelection != null ? (BitSet) newSelection.clone() : new BitSet();
            ps.selectionAccumulata = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();

            for (int i = 0; i < bulks.length; i++) {

                assert oldSelection != null;
                assert newSelection != null;
                if (oldSelection.get(i) == newSelection.get(i)) {
                    continue;
                }

                Inventario_beniBulk bene = (Inventario_beniBulk) bulks[i];

                if (ps.newSel.get(i)) {

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
                        // ==================== QUI LA MODIFICA PRINCIPALE ====================
                        List found;

                        if (getDoc() instanceof DocumentoRientroBulk) {
                            // ========== RIENTRO: Cerca solo accessori presenti nel TRASPORTO originale ==========
                            found = getComp().cercaBeniAccessoriPresentinelTrasportoOriginale(
                                    context.getUserContext(),
                                    bene,
                                    getDoc()
                            );
                        } else {
                            // ========== TRASPORTO: Cerca tutti gli accessori del bene ==========
                            found = getComp().cercaBeniAccessoriAssociati(
                                    context.getUserContext(),
                                    bene
                            );
                        }

                        if (found != null && !found.isEmpty()) {
                            ps.principaliConAccessori.put(bene, found);
                        } else {
                            ps.principaliSenza.add(bene);
                        }
                    }
                }
            }

            if (ps.principaliConAccessori.isEmpty()) {

                if (!ps.isEmpty()) {
                    modificaBeniConAccessoriComponente(context, bulks, oldSelection, newSelection);
                    setModel(context, getDoc());
                    getDettBeniController().reset(context);
                    getDettBeniController().resync(context);
                }

                return newSelection;
            }

            pendingAdd = ps;
            return newSelection;

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        } catch (FillException e) {
            throw new RuntimeException(e);
        }
    }

    // ========================= FLUSSO RICORSIVO =========================

    public Inventario_beniBulk getBenePrincipaleCorrente(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        int index = isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta;

        if (ps == null || ps.principaliConAccessori.isEmpty()) {
            return null;
        }

        int idx = 0;
        for (Inventario_beniBulk bene : ps.principaliConAccessori.keySet()) {
            if (idx == index) {
                return bene;
            }
            idx++;
        }
        return null;
    }

    public List<Inventario_beniBulk> getAccessoriCorrente(boolean isEliminazione) {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(isEliminazione);
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;

        if (bene != null && ps != null) {
            return ps.principaliConAccessori.get(bene);
        }
        return Collections.emptyList();
    }

    public String getMessaggioSingoloBene(boolean isEliminazione) {
        // Recupera il bene corrente usando il metodo esistente
        Inventario_beniBulk beneCorrente = getBenePrincipaleCorrente(isEliminazione);

        if (beneCorrente == null) {
            return "";
        }

        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        List<Inventario_beniBulk> accessori = (ps != null) ? ps.principaliConAccessori.get(beneCorrente) : null;
        int numAccessori = (accessori != null) ? accessori.size() : 0;

        StringBuilder msg = new StringBuilder();

        if (isEliminazione) {
            msg.append("Il bene principale con codice: ").append(beneCorrente.getNr_inventario());
            if (beneCorrente.getEtichetta() != null && !beneCorrente.getEtichetta().isEmpty()) {
                msg.append(" e etichetta ").append(beneCorrente.getEtichetta());
            }
            msg.append(" ha ").append(numAccessori);
            msg.append(numAccessori == 1 ? " bene accessorio" : " beni accessori").append(".\n\n");
            msg.append("Come vuoi procedere?\n\n");
            msg.append("• SI: Elimina il bene principale CON tutti gli accessori\n");
            msg.append("• NO: Elimina SOLO il bene principale (mantieni accessori)\n");
            msg.append("• ANNULLA: Interrompi l'operazione");
        } else {
            msg.append("La selezione include il bene principale con codice: ").append(beneCorrente.getNr_inventario());
            if (beneCorrente.getEtichetta() != null && !beneCorrente.getEtichetta().isEmpty()) {
                msg.append(" e etichetta ").append(beneCorrente.getEtichetta());
            }
            msg.append(" che ha ").append(numAccessori);
            msg.append(numAccessori == 1 ? " bene accessorio associato" : " beni accessori associati").append(".\n\n");
            String articoloFinale = (numAccessori == 1) ? "questo accessorio" : "questi accessori";
            String azione = getTipo().equals(TRASPORTO) ? "nell'aggiunta" : "nel rientro";
            msg.append("Vuoi includere anche ").append(articoloFinale).append(" ").append(azione).append("?");
        }

        return msg.toString();
    }

    // ========================= AVANZAMENTO/ELABORAZIONE =========================

    public int getTotaleBeniPrincipali(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        return (ps != null) ? ps.principaliConAccessori.size() : 0;
    }

    public int getIndexBeneCorrente(boolean isEliminazione) {
        int index = isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta;
        return index + 1;
    }

    public void elaboraBeneCorrente(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk beneCorrente = getBenePrincipaleCorrente(isEliminazione);

        if (isEliminazione) {
            if (includiAccessori) {
                eliminaBeneCorrente(context);
            } else {
                eliminaBenePrincipaleSenzaAccessori(context);
            }
        } else {
            aggiungiBenesCorrente(context, includiAccessori);
        }
    }


//    private void eliminaBeneCorrente(ActionContext context) throws BusinessProcessException {
//        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);
//        List<Inventario_beniBulk> accessori = getAccessoriCorrente(true);
//
//        if (bene != null && accessori != null) {
//            try {
//                getComp().eliminaBeniPrincipaleConAccessori(context.getUserContext(), getDoc(), bene, accessori);
//
//            } catch (ComponentException | RemoteException e) {
//                throw handleException(e);
//            }
//        }
//    }
//
//    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context) throws BusinessProcessException {
//        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);
//
//        if (bene != null) {
//            try {
//                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{bene};
//                getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), soloIlPrincipale);
//            } catch (ComponentException | RemoteException e) {
//                throw handleException(e);
//            }
//        }
//    }

    /**
     * Aggiunge il bene principale corrente con o senza accessori.
     * <p>
     * LOGICA CRITICA:
     * - includiAccessori=true  → Aggiunge principale + TUTTI gli accessori
     * - includiAccessori=false → Aggiunge SOLO il principale (scarta accessori)
     */
    private void aggiungiBenesCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk benePrincipale = getBenePrincipaleCorrente(false);
        if (benePrincipale == null) {
            return;
        }

        try {
            // STEP 1: aggiungi bene principale
            OggettoBulk[] soloBenePrincipale = new OggettoBulk[]{benePrincipale};
            BitSet vuoto = new BitSet(1);
            BitSet selezionato = new BitSet(1);
            selezionato.set(0);

            modificaBeniConAccessoriComponente(context, soloBenePrincipale, vuoto, selezionato);

            // STEP 2: accessori
            if (includiAccessori) {

                List<Inventario_beniBulk> accessori = getAccessoriCorrente(false);

                if (accessori != null && !accessori.isEmpty()) {

                    for (Inventario_beniBulk accessorio : accessori) {

                        OggettoBulk[] soloAccessorio = new OggettoBulk[]{accessorio};
                        BitSet vuotoAcc = new BitSet(1);
                        BitSet selezionatoAcc = new BitSet(1);
                        selezionatoAcc.set(0);

                        modificaBeniConAccessoriComponente(context, soloAccessorio, vuotoAcc, selezionatoAcc);
                    }
                }

            } else {
                // NO → accessori scartati (nessuna azione)
            }

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }


    private void aggiungiAccessoriMancanti(ActionContext context, List<Inventario_beniBulk> accessori)
            throws BusinessProcessException {

        if (accessori == null || accessori.isEmpty()) return;

        try {
            for (Inventario_beniBulk acc : accessori) {
                boolean trovatoInBulks = false;
                for (int i = 0; i < pendingAdd.bulks.length; i++) {
                    if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                        Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                        if (b.equalsByPrimaryKey(acc)) {
                            trovatoInBulks = true;
                            break;
                        }
                    }
                }

                if (!trovatoInBulks) {
                    OggettoBulk[] accessorioArray = new OggettoBulk[]{acc};
                    BitSet vuoto = new BitSet(1);
                    BitSet selezionato = new BitSet(1);
                    selezionato.set(0);
                    modificaBeniConAccessoriComponente(context, accessorioArray, vuoto, selezionato);
                }
            }
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    public boolean passaAlProssimoBene(boolean isEliminazione) {
        if (isEliminazione) {
            indexBeneCurrentePerEliminazione++;
            int total = getTotaleBeniPrincipali(true);
            return indexBeneCurrentePerEliminazione < total;
        } else {
            indexBeneCurrentePerAggiunta++;
            int total = getTotaleBeniPrincipali(false);
            return indexBeneCurrentePerAggiunta < total;
        }
    }


    public void resetOperazione(boolean isEliminazione) {
        if (isEliminazione) {
            indexBeneCurrentePerEliminazione = 0;

            if (pendingDelete != null) {
                pendingDelete.clear();
            }
            pendingDelete = null;

        } else {
            indexBeneCurrentePerAggiunta = 0;

            if (pendingAdd != null) {
                pendingAdd.clear();
            }
            pendingAdd = null;
        }
    }


    // ========================= STATISTICHE E STATO =========================

    public boolean hasBeniPrincipaliConAccessoriPerEliminazione() {
        return pendingDelete != null && !pendingDelete.principaliConAccessori.isEmpty();
    }

    public boolean hasBeniPrincipaliConAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.principaliConAccessori.isEmpty();
    }

    public boolean hasBeniAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.accessori.isEmpty();
    }

    public int getNumeroBeniPrincipaliConAccessori() {
        return (pendingAdd != null) ? pendingAdd.principaliConAccessori.size() : 0;
    }

    public int getNumeroBeniAccessoriTotaliPendenti() {
        if (pendingAdd == null) return 0;
        return pendingAdd.principaliConAccessori.values().stream().mapToInt(List::size).sum();
    }

    public int getNumeroBeniSemplici() {
        if (pendingAdd == null) return 0;
        return pendingAdd.principaliSenza.size() + pendingAdd.accessori.size();
    }

    public int getNumeroBeniAccessoriPendenti() {
        return (pendingAdd != null) ? pendingAdd.accessori.size() : 0;
    }

    public PendingSelection getPendingAdd() {
        return pendingAdd;
    }

    public void setPendingAdd(PendingSelection pendingAdd) {
        this.pendingAdd = pendingAdd;
    }

    public int getIndexBeneCurrentePerEliminazione() {
        return indexBeneCurrentePerEliminazione;
    }

    public int getIndexBeneCurrentePerAggiunta() {
        return indexBeneCurrentePerAggiunta;
    }

    // ========================= GETTERS/SETTERS =========================

    public void setIndexBeneCurrentePerEliminazione(int value) {
        this.indexBeneCurrentePerEliminazione = value;
    }


    public void setIndexBeneCurrentePerAggiunta(int value) {
        this.indexBeneCurrentePerAggiunta = value;
    }

    public boolean isUltimaOperazioneEliminazione() {
        return ultimaOperazioneEliminazione;
    }

    public void setUltimaOperazioneEliminazione(boolean value) {
        this.ultimaOperazioneEliminazione = value;
    }


    // ========================= STAMPA DOCUMENTO =========================


    // BP - Usa il model corrente (già caricato e validato)
    public void stampaDocTrasportoRientro(ActionContext actioncontext) throws Exception {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new BusinessProcessException("Documento non valido");
        }

        InputStream is = getStreamDocTrasportoRientro(actioncontext.getUserContext(), doc);

        if (is != null) {
            HttpServletResponse response = ((HttpActionContext) actioncontext).getResponse();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "inline; filename=\"doc_trasporto_rientro_" + doc.getPgDocTrasportoRientro() + ".pdf\"");
            response.setDateHeader("Expires", 0);

            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[response.getBufferSize()];
            int buflength;

            while ((buflength = is.read(buffer)) > 0) {
                os.write(buffer, 0, buflength);
            }

            is.close();
            os.flush();
        }
    }

    private InputStream getStreamDocTrasportoRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc) throws Exception {

        Print_spoolerBulk print = new Print_spoolerBulk();
        print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
        print.setFlEmail(false);
        print.setReport("/cnrdocamm/docamm/" + REPORT_DOC_TRASPORTO_RIENTRO);
        print.setNomeFile("DocTrasportoRientro_" + doc.getPgDocTrasportoRientro());
        print.setUtcr(userContext.getUser());

        print.addParam("esercizio", doc.getEsercizio(), Integer.class);
        print.addParam("pg_inventario", doc.getPgInventario(), Long.class);
        print.addParam("ti_documento", doc.getTiDocumento(), String.class);
        print.addParam("pg_doc_trasporto_rientro", doc.getPgDocTrasportoRientro(), Long.class);
        print.addParam("DIR_IMAGE", "", String.class);

        Report report = SpringUtil.getBean("printService", PrintService.class)
                .executeReport(userContext, print);

        return report.getInputStream();
    }

    public File stampaDocTrasportoRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc) throws ComponentException {
        try {
            String nomeFileOrdineOut = getOutputFileNameOrdine(REPORT_DOC_TRASPORTO_RIENTRO, doc);
            File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + nomeFileOrdineOut);
            Print_spoolerBulk print = new Print_spoolerBulk();
            print.setFlEmail(false);
            print.setReport("/cnrdocamm/docamm/" + REPORT_DOC_TRASPORTO_RIENTRO);
            print.setNomeFile(nomeFileOrdineOut);
            print.setUtcr(userContext.getUser());
            print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
            print.addParam("esercizio", doc.getEsercizio(), Integer.class);
            print.addParam("pg_inventario", doc.getPgInventario(), Long.class);
            print.addParam("ti_documento", doc.getTiDocumento(), String.class);
            print.addParam("pg_doc_trasporto_rientro", doc.getPgDocTrasportoRientro(), Long.class);
            print.addParam("DIR_IMAGE", "", String.class);
            Report report = SpringUtil.getBean("printService", PrintService.class).executeReport(userContext, print);

            FileOutputStream f = new FileOutputStream(output);
            f.write(report.getBytes());
            return output;
        } catch (IOException e) {
            throw new GenerazioneReportException("Generazione Stampa non riuscita", e);
        }
    }


    private String getOutputFileNameOrdine(String reportName, Doc_trasporto_rientroBulk doc) {
        String fileName = preparaFileNamePerStampa(reportName);
        fileName = PDF_DATE_FORMAT.format(new Date()) + '_' + doc.recuperoIdDocAsString() + '_' + fileName;
        return fileName;
    }

    public void create(ActionContext context)
            throws BusinessProcessException {
        try {
            // ==================== CREA IL DOCUMENTO (serve PG per allegati) ====================
            getModel().setToBeCreated();
            Doc_trasporto_rientroBulk docCreato = (Doc_trasporto_rientroBulk)
                    createComponentSession().creaConBulk(
                            context.getUserContext(),
                            getModel()
                    );
            setModel(context, docCreato);

            // ==================== ARCHIVIA ALLEGATI (dopo creazione documento) ====================
            archiviaAllegati(context);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    //il messaggio deve uscire a schermo con application exceptuion

    @Override
    public void update(ActionContext context) throws BusinessProcessException {
        try {
            // ==================== FILL MODEL ====================
            fillModel(context);

            // ==================== BLOCCA SOSTITUZIONE ALLEGATI CON STESSO NOME ====================
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            if (doc.getArchivioAllegati() != null) {
                List<AllegatoGenericoBulk> allegati = doc.getArchivioAllegati();

                for (AllegatoGenericoBulk nuovo : allegati) {
                    if (nuovo != null && nuovo.getFile() != null) {
                        String nomeNuovo = nuovo.parseFilename(nuovo.getFile().getName());

                        boolean duplicatoTrovato = allegati.stream()
                                .filter(esistente -> esistente != nuovo)
                                .anyMatch(esistente ->
                                        esistente != null &&
                                                esistente.getNome() != null &&
                                                esistente.getNome().equalsIgnoreCase(nomeNuovo)
                                );

                        if (duplicatoTrovato) {
                            throw new ApplicationException(
                                    "Impossibile caricare l'allegato '" + nomeNuovo +
                                            "': esiste già un file con lo stesso nome sul documento."
                            );
                        }
                    }
                }
            }

            // ==================== ARCHIVIA ALLEGATI (PRIMA DI SALVARE) ====================
            archiviaAllegati(context);

            // ==================== SALVA IL DOCUMENTO ====================
            getModel().setToBeUpdated();
            Doc_trasporto_rientroBulk docAggiornato = (Doc_trasporto_rientroBulk)
                    createComponentSession().modificaConBulk(
                            context.getUserContext(),
                            getModel()
                    );

            // ==================== AGGIORNA IL MODEL NEL BP ====================
            setModel(context, docAggiornato);

        } catch (ApplicationException e) {
            throw new BusinessProcessException(e);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // ========================= UTILS STAMPA =========================


    private String preparaFileNamePerStampa(String reportName) {
        String fileName = reportName;
        fileName = fileName.replace('/', '_').replace('\\', '_');
        if (fileName.startsWith("_"))
            fileName = fileName.substring(1);
        if (fileName.endsWith(".jasper"))
            fileName = fileName.substring(0, fileName.length() - 7);
        return fileName + ".pdf";
    }

    public DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        return (DocTrasportoRientroComponentSession) createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
    }

    public CompoundFindClause getClauses() {
        return clauses;
    }

    public void setClauses(CompoundFindClause newClauses) {
        clauses = newClauses;
    }

    public RemoteDetailCRUDController getDettBeniController() {
        return dettBeniController;
    }

    public RemoteDetailCRUDController getEditDettController() {
        return editDettController;
    }

    // ========================= GETTERS/SETTERS =========================

    /**
     * Elabora un singolo bene principale con o senza accessori
     */
    public void elaboraBeneConAccessori(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {

        // Recupera il bene corrente internamente
        Inventario_beniBulk beneCorrente = getBenePrincipaleCorrente(isEliminazione);

        if (beneCorrente == null) {
            throw new BusinessProcessException("Nessun bene corrente da elaborare");
        }

        if (isEliminazione) {
            elaboraBenePerEliminazione(context, beneCorrente, includiAccessori);
        } else {
            elaboraBenePerAggiunta(context, beneCorrente, includiAccessori);
        }
    }

    /**
     * Elabora un bene per ELIMINAZIONE
     */
    private void elaboraBenePerEliminazione(ActionContext context, Inventario_beniBulk beneCorrente,
                                            boolean includiAccessori) throws BusinessProcessException {
        try {
            if (includiAccessori) {
                // Elimina principale + accessori DAI DETTAGLI
                List<Inventario_beniBulk> accessori = pendingDelete.principaliConAccessori.get(beneCorrente);
                getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(
                        context.getUserContext(), getDoc(), beneCorrente, accessori);
            } else {
                // Elimina SOLO il principale DAI DETTAGLI
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{beneCorrente};
                getComp().eliminaDettagliSalvati(
                        context.getUserContext(), getDoc(), soloIlPrincipale);
            }
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Elabora un bene per AGGIUNTA
     */
    private void elaboraBenePerAggiunta(ActionContext context, Inventario_beniBulk beneCorrente,
                                        boolean includiAccessori) throws BusinessProcessException {
        try {
            // Trova l'indice del bene principale in bulks[]
            int indiceBenePrincipale = -1;
            for (int i = 0; i < pendingAdd.bulks.length; i++) {
                if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                    Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                    if (b.equalsByPrimaryKey(beneCorrente)) {
                        indiceBenePrincipale = i;
                        break;
                    }
                }
            }

            if (indiceBenePrincipale == -1) {
                throw new BusinessProcessException("Bene principale non trovato in bulks[]");
            }

            // Crea BitSet per la selezione
            BitSet oldSelection = new BitSet(pendingAdd.bulks.length);
            BitSet newSelection = new BitSet(pendingAdd.bulks.length);

            // Seleziona il bene principale
            newSelection.set(indiceBenePrincipale);

            if (includiAccessori) {
                //  Aggiungi TUTTI gli accessori (anche quelli non in bulks[])
                List<Inventario_beniBulk> accessori = pendingAdd.principaliConAccessori.get(beneCorrente);

                if (accessori != null && !accessori.isEmpty()) {
                    // Aggiungi accessori presenti in bulks[]
                    for (Inventario_beniBulk acc : accessori) {
                        for (int i = 0; i < pendingAdd.bulks.length; i++) {
                            if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                                Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                                if (b.equalsByPrimaryKey(acc)) {
                                    newSelection.set(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Aggiungi il principale (e accessori in bulks[] se includiAccessori=true)
            modificaBeniConAccessoriComponente(context, pendingAdd.bulks, oldSelection, newSelection);

            //   Aggiungi accessori NON presenti in bulks[] (pagine successive)
            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = pendingAdd.principaliConAccessori.get(beneCorrente);
                aggiungiAccessoriMancanti(context, accessori);
            }

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }


    @Override
    protected void completeAllegato(T allegato, StorageObject storageObject)
            throws ApplicationException {

        // ========== 1. CAMPO UTENTE SIGLA (già gestito nella classe base) ==========
        Optional.ofNullable(storageObject.<String>getPropertyValue(
                        "sigla_commons_aspect:utente_applicativo"))
                .ifPresent(allegato::setUtenteSIGLA);

        // ========== 2. ESTRAZIONE ASPECT NAME DA CMIS ==========
        // CRITICO: Fix per il bug dell'aspectName
        List<String> secondaryTypes = storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");

        if (secondaryTypes != null && !secondaryTypes.isEmpty()) {

            // ========== DETERMINA GLI ASPECT VALIDI IN BASE AL TIPO DOCUMENTO ==========
            String aspectFirmatoAtteso = null;
            String aspectAltroAtteso = null;

            if (allegato instanceof AllegatoDocumentoTrasportoBulk) {
                aspectFirmatoAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
                aspectAltroAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO;
            } else if (allegato instanceof AllegatoDocumentoRientroBulk) {
                aspectFirmatoAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
                aspectAltroAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO;
            }

            if (aspectFirmatoAtteso != null && aspectAltroAtteso != null) {

                // ========== CERCA L'ASPECT SPECIFICO NELLA LISTA ==========
                for (String secondaryType : secondaryTypes) {
                    // Salta gli aspect standard
                    if (secondaryType.equals("P:cm:titled") ||
                            secondaryType.contains("sigla_commons_aspect")) {
                        continue;
                    }

                    // ========== TROVA L'ASPECT SPECIFICO DEL DOCUMENTO ==========
                    if (secondaryType.equals(aspectFirmatoAtteso)) {
                        allegato.setAspectName(aspectFirmatoAtteso);
                        break;

                    } else if (secondaryType.equals(aspectAltroAtteso)) {
                        allegato.setAspectName(aspectAltroAtteso);
                        break;
                    }
                }
            }
        }

        // ========== 3. CHIAMA IL METODO PADRE ==========
        super.completeAllegato(allegato, storageObject);
    }


    public void salvaDefinitivo(ActionContext context)
            throws BusinessProcessException {
        try {
            fillModel(context);

            // ==================== ARCHIVIA ALLEGATI ====================
            archiviaAllegati(context);

            // ==================== SALVATAGGIO DEFINITIVO CON CONTROLLO ALLEGATO ====================
            getModel().setToBeUpdated();
            Doc_trasporto_rientroBulk doc = getComp().salvaDefinitivo(
                    context.getUserContext(),
                    getDoc()
            );

            setModel(context, doc);  // Aggiorna il model del BP con stato DEFINITIVO

            setStatus(VIEW);
            setMessage("Documento salvato in stato DEFINITIVO");

        } catch (ComponentException | RemoteException ex) {
            throw handleException(ex);
        } catch (FillException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Valida i dati necessari prima di accedere al tab dei dettagli.
     * Controlla che siano compilati tutti i campi obbligatori per la ricerca dei beni.
     *
     * @throws BusinessProcessException se la validazione fallisce
     */
    public void validaDatiPerDettagli() throws ApplicationException {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return;
        }

        if (doc.getTipoMovimento() == null) {
            throw new ApplicationException(
                    "Selezionare il Tipo di Movimento prima di accedere ai tab Dettagli e Allegati."
            );
        }

        if (doc.isSmartworking()) {
            if (doc.getAnagSmartworking() == null ||
                    doc.getAnagSmartworking().getCd_anag() == null) {

                throw new ApplicationException(
                        "Per il tipo di movimento Smartworking è necessario selezionare l'Assegnatario Smartworking."
                );
            }
        } else {

            if (!doc.hasTipoRitiroSelezionato()) {
                throw new ApplicationException(
                        "Selezionare il Tipo Ritiro (Incaricato o Vettore)."
                );
            }

            if (doc.isRitiroIncaricato()) {
                if (doc.getAnagIncRitiro() == null ||
                        doc.getAnagIncRitiro().getCd_anag() == null) {

                    throw new ApplicationException(
                            "Selezionare il Dipendente Incaricato."
                    );
                }
            }

            if (doc.isRitiroVettore()) {
                if (doc.getNominativoVettore() == null ||
                        doc.getNominativoVettore().trim().isEmpty()) {

                    throw new ApplicationException(
                            "Specificare il Nominativo del Vettore."
                    );
                }
            }
        }
    }


    /**
     * Determina se i campi anagrafico (Smartworking, Incaricato, ecc.) devono essere readonly
     */
    public boolean isAnagraficiReadonly() {
        return isDocumentoNonModificabile()
                || isVisualizzazione()
                || (!isInserting() && !isEditing());
    }


//    /**
//     * Elimina TUTTI i beni dalla tabella temporanea INVENTARIO_BENI_APG.
//     * Usato durante INSERIMENTO.
//     */
//    protected void eliminaTuttiBeniDaTabellaTempo(ActionContext context)
//            throws BusinessProcessException {
//        try {
//            getComp().eliminaTuttiBeniAssociati(context.getUserContext(), getDoc());
//        } catch (Throwable e) {
//            throw handleException(e);
//        }
//    }

    /**
     * Elimina TUTTI i beni dai dettagli salvati DOC_TRASPORTO_RIENTRO_DETT.
     * Usato durante MODIFICA.
     */
    protected void eliminaTuttiBeniDaDettagli(ActionContext context)
            throws BusinessProcessException {
        try {
            getComp().eliminaTuttiDettagliSalvati(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

//    /**
//     * Elimina beni specifici dalla tabella temporanea INVENTARIO_BENI_APG.
//     * Usato durante INSERIMENTO.
//     */
//    protected void eliminaBeniDaTabellaTempo(ActionContext context, OggettoBulk[] details)
//            throws BusinessProcessException {
//        try {
//            getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), details);
//        } catch (ComponentException | RemoteException e) {
//            throw handleException(e);
//        }
//    }

    /**
     * Elimina beni specifici dai dettagli salvati DOC_TRASPORTO_RIENTRO_DETT.
     * Usato durante MODIFICA.
     */
    public void eliminaBeniDaDettagli(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaDettagliSalvati(context.getUserContext(), getDoc(), details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaBeneCorrente(ActionContext context) throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);
        List<Inventario_beniBulk> accessori = getAccessoriCorrente(true);

        if (bene != null && accessori != null) {
            try {
                // ========== SEMPRE SU DETTAGLIO ==========
                getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(
                        context.getUserContext(), getDoc(), bene, accessori);

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context)
            throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);

        if (bene != null) {
            try {
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{bene};

                // ========== SEMPRE SU DETTAGLIO ==========
                getComp().eliminaDettagliSalvati(
                        context.getUserContext(), getDoc(), soloIlPrincipale);

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }


}

