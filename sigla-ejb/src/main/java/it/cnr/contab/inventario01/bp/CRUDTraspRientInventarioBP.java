package it.cnr.contab.inventario01.bp;

import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.inventario00.bp.RigheInvDaFatturaCRUDController;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.inventario01.service.DocTraspRientCMISService;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.GenerazioneReportException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;
//riscrivi logica per la gestione degli allegati e dei file nel documentale azure in CRUDTraspRientInventarioBP come è stato fatto nel bp CaricFlStipBP
public abstract class CRUDTraspRientInventarioBP extends AllegatiCRUDBP<AllegatoGenericoBulk, Doc_trasporto_rientroBulk>
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
    private it.cnr.jada.persistency.sql.CompoundFindClause clauses;

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

    protected abstract String getMainTabName();

    public abstract String getLabelData_registrazione();

    protected abstract void inizializzaSelezioneComponente(ActionContext context) throws ComponentException, RemoteException;

    protected abstract void annullaModificaComponente(ActionContext context) throws ComponentException, RemoteException;

    protected abstract void selezionaTuttiBeniComponente(ActionContext context) throws ComponentException, RemoteException;

    public abstract void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks, BitSet oldSelection, BitSet newSelection) throws ComponentException, RemoteException, BusinessProcessException;

    private PendingSelection getPendingDelete() {
        return pendingDelete;
    }

    private void setPendingDelete(PendingSelection pendingDelete) {
        this.pendingDelete = pendingDelete;
    }

    // ========================================
    // INIT
    // ========================================

    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws it.cnr.jada.action.BusinessProcessException {

        Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context.getUserContext());


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

    @Override
    public StoreService getBeanStoreService(ActionContext actioncontext) throws BusinessProcessException{
        return SpringUtil.getBean("docTraspRientCMISService", DocTraspRientCMISService.class);
    }
    public boolean isGestioneInvioInFirmaAttiva() {
        return isGestioneInvioInFirmaAttiva;
    }

    public void setGestioneInvioInFirmaAttiva(boolean gestioneInvioInFirmaAttiva) {
        this.isGestioneInvioInFirmaAttiva = gestioneInvioInFirmaAttiva;
    }

    public void initVariabili(it.cnr.jada.action.ActionContext context, String Tipo) {
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        try {
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class);
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e1) {
            throw handleException(e1);
        } catch (RemoteException e1) {
            throw handleException(e1);
        }
        bulk = super.initializeModelForEdit(context, bulk);
        return bulk;
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    public void resetTabs() {
        setTab("tab", getMainTabName());
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

    protected Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    public boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    public boolean isDocumentoInviatoInFirma() {
        return getDoc() != null && getDoc().isInviatoInFirma();
    }

    public boolean isDocumentoNonModificabile() {
        return isDocumentoAnnullato() || isDocumentoInviatoInFirma();
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

    public boolean isDestinazioneVisible() {
        return hasValidModel() && getDoc().hasTipoRitiroSelezionato();
    }

    public boolean isAssegnatarioVisible() {
        return hasValidModel() && getDoc().isRitiroIncaricato();
    }

    public boolean isStatoVisible() {
        return hasValidModel() && getDoc().getStato() != null && !isInserting();
    }

    public boolean isNoteVisible() {
        return hasValidModel() && getDoc().isNoteRitiroEnabled();
    }

    public boolean isNoteAbilitate() {
        return hasValidModel()
                && getDoc().getTipoMovimento() != null
                && getDoc().getTipoMovimento().isAbilitaNote();
    }

    public boolean isBottoneAggiungiBeneEnabled() {
        return isInserting() && getDoc() != null && getDoc().isInserito() && !isDocumentoAnnullato();
    }

    // ========================================
    // READONLY FIELDS
    // ========================================

    private boolean isCampiCriticiReadOnly() {
        if (isDocumentoNonModificabile()) return true;

        return isEditing()
                && hasValidModel()
                && getDoc().getDoc_trasporto_rientro_dettColl() != null
                && !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    public boolean isTipoMovimentoReadOnly() {
        return isCampiCriticiReadOnly();
    }

    public boolean isTipoRitiroReadOnly() {
        return isCampiCriticiReadOnly();
    }

    public boolean isAssegnatarioReadOnly() {
        return isCampiCriticiReadOnly();
    }

    public boolean isQuantitaEnabled() {
        return !isDocumentoNonModificabile() && (isEditing() || isInserting());
    }

    // ========================================
    // VISIBILITÀ BOTTONI
    // ========================================

    private boolean isModificationButtonEnabled() {
        return !isDocumentoNonModificabile() && !isVisualizzazione();
    }

    public boolean isDeleteButtonEnabled() {
        return isModificationButtonEnabled();
    }

    public boolean isSaveButtonEnabled() {
        return isModificationButtonEnabled();
    }

    public boolean isModifyButtonEnabled() {
        return isModificationButtonEnabled();
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
    protected it.cnr.jada.util.jsp.Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(getClass());

        return Stream.concat(
                Arrays.stream(super.createToolbar()),
                Stream.of(
//                        new it.cnr.jada.util.jsp.Button(props, "CRUDToolbar.inviaInFirma"),
                        new it.cnr.jada.util.jsp.Button(props, "CRUDToolbar.stampaDoc")
                )
        ).toArray(it.cnr.jada.util.jsp.Button[]::new);
    }

    @Override
    public boolean isPrintButtonHidden() {
        return true;
    }

//    /**
//     * Determina se il pulsante "Invia alla Firma" deve essere nascosto.
//     *
//     * @return true = nascondi pulsante, false = mostra pulsante
//     */
//    public boolean inviaInFirmaButtonHidden() {
//        if (!isGestioneInvioInFirmaAttiva()) return true;
//        if (getModel() == null) return true;
//        if (isDocumentoAnnullato()) return true;
//        if (getModel().getCrudStatus() != OggettoBulk.NORMAL
//                && getModel().getCrudStatus() != OggettoBulk.TO_BE_UPDATED) {
//            return true;
//        }
//
//        // Controllo null-safe per getDoc()
//        Doc_trasporto_rientroBulk doc = getDoc();
//        if (doc == null || doc.getStato() == null) return true;
//
//        return !STATO_INSERITO.equals(doc.getStato());
//    }



    // ==================== CONTROLLERS ====================

    protected final RigheInvDaFatturaCRUDController dettBeniController = new RigheInvDaFatturaCRUDController(
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
            if (isInserting() || (isEditing() && !isDocumentoAnnullato())) {
                try {
                    return selectDettaglibyClause(context);
                } catch (BusinessProcessException e) {
                    throw new RuntimeException("Errore caricamento dettagli beni", e);
                }
            }
            return new it.cnr.jada.util.EmptyRemoteIterator();
        }

        @Override
        public void removeAll(ActionContext context) throws ValidationException, BusinessProcessException {
            if (isDocumentoAnnullato()) {
                throw new ValidationException("Impossibile modificare un documento annullato");
            }
            eliminaDettagliConBulk(context);
            reset(context);
        }

        @Override
        public void removeDetails(ActionContext context, OggettoBulk[] details) throws BusinessProcessException {
            if (isDocumentoAnnullato()) {
                throw new BusinessProcessException("Impossibile modificare un documento annullato");
            }

            try {
                PendingSelection ps = new PendingSelection();
                ps.bulks = details;

                System.out.println("========== INIZIO ELABORAZIONE ELIMINAZIONE ==========");
                System.out.println("Totale beni selezionati: " + details.length);

                for (OggettoBulk o : details) {
                    Inventario_beniBulk bene = (Inventario_beniBulk) o;

                    System.out.println("\n--- Elaboro bene: " + bene.getNr_inventario() + " ---");
                    System.out.println("È accessorio? " + bene.isBeneAccessorio());

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                        System.out.println("Aggiunto a lista accessori standalone");
                    } else {
                        List<Inventario_beniBulk> found = getComp().cercaBeniAccessoriAssociatiInDettaglio(
                                context.getUserContext(),
                                getDoc(),
                                bene
                        );

                        System.out.println("Accessori trovati per questo principale: " + (found != null ? found.size() : 0));
                        if (found != null) {
                            for (Inventario_beniBulk acc : found) {
                                System.out.println("  - Accessorio: " + acc.getNr_inventario());
                            }
                        }

                        if (found != null && !found.isEmpty()) {
                            ps.principaliConAccessori.put(bene, found);
                            System.out.println("Aggiunto a principaliConAccessori");
                        } else {
                            ps.principaliSenza.add(bene);
                            System.out.println("Aggiunto a principaliSenza");
                        }
                    }
                }

                System.out.println("\n========== RIEPILOGO FINALE ==========");
                System.out.println("Principali con accessori: " + ps.principaliConAccessori.size());
                for (Map.Entry<Inventario_beniBulk, List<Inventario_beniBulk>> entry : ps.principaliConAccessori.entrySet()) {
                    System.out.println("  Principale " + entry.getKey().getNr_inventario() + " -> " + entry.getValue().size() + " accessori");
                }
                System.out.println("========== FINE ELABORAZIONE ==========\n");

                if (ps.principaliConAccessori.isEmpty()) {
                    eliminaDettagliConBulk(context, details);
                    return;
                }

                pendingDelete = ps;

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    };

    private final RemoteDetailCRUDController editDettController = new RemoteDetailCRUDController(
            "editDettController",
            Doc_trasporto_rientro_dettBulk.class,
            "",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        public String getName() {
            return getEditDettagliControllerName();
        }

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            if (isInserting() || isDocumentoAnnullato()) {
                return new it.cnr.jada.util.EmptyRemoteIterator();
            }
            try {
                return selectEditDettaglibyClause(context);
            } catch (BusinessProcessException e) {
                throw new RuntimeException("Errore caricamento dettagli modifica", e);
            }
        }
    };

    // ==================== GESTIONE DETTAGLI ====================

    protected void eliminaDettagliConBulk(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    protected void eliminaDettagliConBulk(ActionContext context) throws BusinessProcessException {
        try {
            getComp().eliminaTuttiBeniAssociati(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    protected RemoteIterator selectEditDettaglibyClause(ActionContext context)
            throws BusinessProcessException {
        try {
            it.cnr.jada.persistency.sql.CompoundFindClause filters =
                    ((RemoteDetailCRUDController) getEditDettController()).getFilter();
            return getComp().selectEditDettagliTrasporto(context.getUserContext(), getDoc(),
                    Doc_trasporto_rientro_dettBulk.class, filters);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    protected RemoteIterator selectDettaglibyClause(ActionContext context)
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
            setMessage("Documento predisposto alla firma. Ora in sola lettura.");
        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    public void annullaDoc(ActionContext context) throws BusinessProcessException {
        validaStatoPerFirma();
        try {
            Doc_trasporto_rientroBulk doc = getComp().annullaDocumento(context.getUserContext(), getDoc());
            setModel(context, doc);
            setStatus(VIEW);
            setMessage("Documento annullato. Ora in sola lettura.");
        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void validaStatoPerFirma() throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile predisporre alla firma un documento annullato");
        }
        if (!getDoc().isInserito()) {
            throw new BusinessProcessException("Il documento deve essere in stato 'Inserito' per essere predisposto alla firma");
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
        if (isDocumentoAnnullato()) return;
        try {
            inizializzaSelezioneComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void selectAll(ActionContext context) throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile modificare un documento annullato");
        }
        try {
            selezionaTuttiBeniComponente(context);
            setClauses(null);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public BitSet setSelection(ActionContext context, OggettoBulk[] bulks,
                               BitSet oldSelection, BitSet newSelection)
            throws BusinessProcessException {

        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile modificare un documento annullato");
        }

        try {
            PendingSelection ps = new PendingSelection();
            ps.bulks = bulks;
            ps.oldSel = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();
            ps.newSel = newSelection != null ? (BitSet) newSelection.clone() : new BitSet();
            ps.selectionAccumulata = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();

            for (int i = 0; i < bulks.length; i++) {
                if (oldSelection.get(i) == newSelection.get(i)) continue;
                Inventario_beniBulk bene = (Inventario_beniBulk) bulks[i];

                if (ps.newSel.get(i)) {
                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
                        List<Inventario_beniBulk> found = getComp().cercaBeniAccessoriAssociati(
                                context.getUserContext(), bene);
                        if (found != null && !found.isEmpty())
                            ps.principaliConAccessori.put(bene, found);
                        else
                            ps.principaliSenza.add(bene);
                    }
                }
            }

            if (ps.principaliConAccessori.isEmpty()) {
                if (!ps.isEmpty()) {
                    modificaBeniConAccessoriComponente(context, bulks, oldSelection, newSelection);
                    getDettBeniController().reset(context);
                }
                return newSelection;
            }

            pendingAdd = ps;
            return newSelection;

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
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

    private void eliminaBeneCorrente(ActionContext context) throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);
        List<Inventario_beniBulk> accessori = getAccessoriCorrente(true);

        if (bene != null && accessori != null) {
            try {
                getComp().eliminaBeniPrincipaleConAccessori(context.getUserContext(), getDoc(), bene, accessori);

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context) throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);

        if (bene != null) {
            try {
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{bene};
                getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), soloIlPrincipale);
            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    private void aggiungiBenesCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk bene = getBenePrincipaleCorrente(false);
        if (bene == null) return;

        try {
            BitSet tempSelection = (pendingAdd != null && pendingAdd.selectionAccumulata != null)
                    ? (BitSet) pendingAdd.selectionAccumulata.clone()
                    : new BitSet();

            // Seleziona il bene principale
            for (int i = 0; i < pendingAdd.bulks.length; i++) {
                if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                    Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                    if (b.equalsByPrimaryKey(bene)) {
                        tempSelection.set(i);
                        break;
                    }
                }
            }

            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = getAccessoriCorrente(false);
                if (accessori != null && !accessori.isEmpty()) {
                    for (Inventario_beniBulk acc : accessori) {
                        for (int i = 0; i < pendingAdd.bulks.length; i++) {
                            if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                                Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                                if (b.equalsByPrimaryKey(acc)) {
                                    tempSelection.set(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Applica modifica per elementi in bulks[]
            modificaBeniConAccessoriComponente(context, pendingAdd.bulks,
                    pendingAdd.selectionAccumulata != null ? pendingAdd.selectionAccumulata : new BitSet(),
                    tempSelection);

            // Aggiungi accessori non presenti in bulks[]
            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = getAccessoriCorrente(false);
                aggiungiAccessoriMancanti(context, accessori);
            }

            pendingAdd.selectionAccumulata = (BitSet) tempSelection.clone();

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
            return indexBeneCurrentePerEliminazione < getTotaleBeniPrincipali(true);
        } else {
            indexBeneCurrentePerAggiunta++;
            return indexBeneCurrentePerAggiunta < getTotaleBeniPrincipali(false);
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

    // ========================= CAMPI SPECIFICI UI =========================

    public boolean isNominativoVettoreVisible() {
        return getModel() != null && getDoc() != null && getDoc().isNominativoVettoreVisible();
    }

    public boolean isNominativoVettoreReadOnly() {
        return isCampiCriticiReadOnly();
    }

    // ========================= STAMPA DOCUMENTO =========================



    public void stampaDocTrasportoRientro(ActionContext actioncontext) throws Exception {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        ((HttpActionContext)actioncontext).getResponse().setContentType("application/pdf");
        OutputStream os = ((HttpActionContext)actioncontext).getResponse().getOutputStream();
        ((HttpActionContext)actioncontext).getResponse().setDateHeader("Expires", 0);
        InputStream is = ((DocTraspRientCMISService)storeService).getStreamDoc( doc, actioncontext.getUserContext());
        if ( is==null) {
            UserContext userContext = actioncontext.getUserContext();
            File f = stampaDocTrasportoRientro(userContext, doc);
            IOUtils.copy(Files.newInputStream(f.toPath()), os);
        }else{
            IOUtils.copy(is, os);
        }

        os.flush();
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
        fileName = PDF_DATE_FORMAT.format(new java.util.Date()) + '_' + doc.recuperoIdDocAsString() + '_' + fileName;
        return fileName;
    }

    public void create(it.cnr.jada.action.ActionContext context) throws	it.cnr.jada.action.BusinessProcessException {
        try {
            getModel().setToBeCreated();
            setModel(context, createComponentSession().creaConBulk(context.getUserContext(),getModel()));
        } catch(Exception e) {
            throw handleException(e);
        }
    }


    public void update(it.cnr.jada.action.ActionContext context) throws	it.cnr.jada.action.BusinessProcessException {
        try {
            getModel().setToBeUpdated();
            setModel(context,createComponentSession().modificaConBulk(context.getUserContext(),getModel()));
            allegatoStampaDoc(context.getUserContext());
            archiviaAllegati(context);
        } catch(Exception e) {
            throw handleException(e);
        }
    }

    // ========================= ALLEGATI DOCUMENTALE =========================

    /**
     * Gestisce l'allegato della stampa del documento nel documentale.
     * - Se il documento NON è predisposto alla firma/firmato, elimina eventuali stampe esistenti
     * - Se il documento è predisposto alla firma e non esiste una stampa, la crea e la allega
     *
     * @param userContext il contesto utente
     * @throws Exception in caso di errore
     */
    private void allegatoStampaDoc(UserContext userContext) throws Exception {
        Doc_trasporto_rientroBulk docTrasportoRientro = (Doc_trasporto_rientroBulk) getModel();
        StorageObject s = ((DocTraspRientCMISService) storeService)
                .getStorageObjectStampaDoc(docTrasportoRientro,userContext);
/*
        // Se non in stato predisposto/firmato e una stampa esiste, eliminala
        if (!Doc_trasporto_rientroBulk.STATO_INVIATO.equals(docTrasportoRientro.getStato())
                && !Doc_trasporto_rientroBulk.STATO_DEFINITIVO.equals(docTrasportoRientro.getStato())
                && s != null) {
            storeService.delete(s);
        }

 */

        // Se predisposto alla firma e la stampa non è presente, allega
       // if (Doc_trasporto_rientroBulk.STATO_INVIATO.equals(docTrasportoRientro.getStato()) && s == null) {
        if ( !Optional.ofNullable(s).isPresent()){
            File f = stampaDocTrasportoRientro(userContext, docTrasportoRientro);

            AllegatoGenericoBulk allegatoStampa = new AllegatoGenericoBulk();
            allegatoStampa.setFile(f);
            allegatoStampa.setContentType(new MimetypesFileTypeMap().getContentType(f.getName()));
            allegatoStampa.setNome(f.getName());
            allegatoStampa.setCrudStatus( OggettoBulk.TO_BE_CREATED);
            allegatoStampa.setDescrizione(f.getName());
            allegatoStampa.setTitolo(f.getName());
            docTrasportoRientro.addToArchivioAllegati(allegatoStampa);
        }
    }



    @Override
    protected String getStorePath(Doc_trasporto_rientroBulk allegatoParentBulk, boolean create) throws BusinessProcessException{

        return ( (DocTraspRientCMISService)storeService).getStorePath(allegatoParentBulk);
        /*
        return Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                "Documento Trasporto Rientro",
                Optional.ofNullable(allegatoParentBulk.getEsercizio())
                        .map(esercizio -> String.valueOf(esercizio))
                        .orElse("0"),
                "Doc. Trasporto " + allegatoParentBulk.getEsercizio().toString() + Utility.lpad(allegatoParentBulk.getPgDocTrasportoRientro().toString(), 10, '0')
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        );

         */
    }


    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }

    // ========================= UPDATE OVERRIDE =========================



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

    protected DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        return (DocTrasportoRientroComponentSession) createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
    }

    public it.cnr.jada.persistency.sql.CompoundFindClause getClauses() {
        return clauses;
    }

    public void setClauses(it.cnr.jada.persistency.sql.CompoundFindClause newClauses) {
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
                // Elimina principale + accessori
                List<Inventario_beniBulk> accessori = pendingDelete.principaliConAccessori.get(beneCorrente);
                getComp().eliminaBeniPrincipaleConAccessori(context.getUserContext(), getDoc(), beneCorrente, accessori);
            } else {
                // Elimina SOLO il principale
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{beneCorrente};
                getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), soloIlPrincipale);
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

    /**
     *	Abilito il bottone di stampa del Documento solo se questo e' in fase di
     *	modifica/inserimento.
     *
     */

    public boolean isStampaDocButtonEnabled() {
        return isEditable() || isInserting();
    }


    public boolean isStampaDocButtonHidden() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk)getModel();
        return (doc == null || doc.getPgDocTrasportoRientro() == null);
    }

}
