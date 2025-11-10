package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario00.bp.RigheInvDaFatturaCRUDController;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.action.SelectionListener;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.STATO_INSERITO;

public abstract class CRUDTraspRientInventarioBP extends SimpleCRUDBP implements SelectionListener {

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private String tipo;
    private boolean isAmministratore = false;
    private boolean isVisualizzazione = false;

    // ==================== PENDING SELECTION ====================

    public static class PendingSelection {
        Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori = new LinkedHashMap<>();
        List<Inventario_beniBulk> accessori = new ArrayList<>();
        List<Inventario_beniBulk> principaliSenza = new ArrayList<>();
        OggettoBulk[] bulks = null;
        BitSet oldSel = null;
        BitSet newSel = null;
        BitSet selectionAccumulata = null; // NUOVO: per accumulare le selezioni progressive

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
            this.selectionAccumulata = (BitSet) oldSel.clone(); // Inizializza accumulo
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
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class);
            setVisualizzazione(session.isEsercizioCOEPChiuso(context.getUserContext()));
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e) {
            throw handleException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }
        super.init(config, context);
        initVariabili(context, getTipo());
        resetTabs();
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

    public boolean isDocumentoPredispostoAllaFirma() {
        return getDoc() != null && getDoc().isPredispostoAllaFirma();
    }

    public boolean isDocumentoNonModificabile() {
        return isDocumentoAnnullato() || isDocumentoPredispostoAllaFirma();
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
        return hasValidModel() &&
                getDoc().getTipoMovimento() != null &&
                getDoc().getTipoMovimento().isAbilitaNote();
    }

    public boolean isBottoneAggiungiBeneEnabled() {
        return isInserting() && getDoc() != null && getDoc().isInserito() && !isDocumentoAnnullato();
    }

    // ========================================
    // READONLY FIELDS
    // ========================================

    private boolean isCampiCriticiReadOnly() {
        if (isDocumentoNonModificabile()) return true;

        return isEditing() &&
                hasValidModel() &&
                getDoc().getDoc_trasporto_rientro_dettColl() != null &&
                !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty();
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
    public boolean isPrintButtonHidden() {
        return !Optional.ofNullable(getDoc())
                .flatMap(d -> Optional.ofNullable(d.getPgDocTrasportoRientro()))
                .isPresent();
    }

    @Override
    protected void initializePrintBP(ActionContext actioncontext, AbstractPrintBP abstractprintbp) {
        OfflineReportPrintBP printbp = (OfflineReportPrintBP) abstractprintbp;
        printbp.setReportName("/cnrdocamm/docamm/doc_trasporto_rientro.jasper");

        final Doc_trasporto_rientroBulk docT = Optional.ofNullable(getDoc())
                .orElseThrow(() -> new DetailedRuntimeException("Modello vuoto!"));

        addPrintParam(printbp, "esercizio", docT.getEsercizio());
        addPrintParam(printbp, "pg_inventario", docT.getPgInventario());
        addPrintParam(printbp, "ti_documento", docT.getTiDocumento());
        addPrintParam(printbp, "pg_doc_trasporto_rientro", docT.getPgDocTrasportoRientro());
    }

    private void addPrintParam(OfflineReportPrintBP printbp, String name, Object value) {
        Print_spooler_paramBulk param = new Print_spooler_paramBulk();
        param.setNomeParam(name);
        param.setValoreParam(value != null ? String.valueOf(value) : null);
        param.setParamType(value != null ? value.getClass().getCanonicalName() : String.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    // ========================= TOOLBAR =========================

    @Override
    protected it.cnr.jada.util.jsp.Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.concat(
                Arrays.stream(super.createToolbar()),
                Stream.of(new it.cnr.jada.util.jsp.Button(props, "CRUDToolbar.predisponiAllaFirma"))
        ).toArray(it.cnr.jada.util.jsp.Button[]::new);
    }

    public boolean isPredisponiAllaFirmaButtonHidden() {
        if (isDocumentoAnnullato()) return true;
        return !(getModel() != null && (getModel().getCrudStatus() == OggettoBulk.NORMAL ||
                getModel().getCrudStatus() == OggettoBulk.TO_BE_UPDATED) &&
                STATO_INSERITO.equals(getDoc().getStato()));
    }

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
            if (isInserting() || isEditing() && !isDocumentoAnnullato()) {
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
        public void removeDetails(ActionContext context, OggettoBulk[] details)
                throws BusinessProcessException {
            if (isDocumentoAnnullato()) {
                throw new BusinessProcessException("Impossibile modificare un documento annullato");
            }

            try {
                PendingSelection ps = new PendingSelection();
                ps.bulks = details;

                for (OggettoBulk o : details) {
                    Inventario_beniBulk bene = (Inventario_beniBulk) o;

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
                        List<Inventario_beniBulk> found = getComp().cercaBeniAccessoriAssociatiInDettaglio(
                                context.getUserContext(),
                                getDoc(),
                                bene);

                        if (found != null && !found.isEmpty()) {
                            ps.principaliConAccessori.put(bene, found);
                        } else {
                            ps.principaliSenza.add(bene);
                        }
                    }
                }

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
                    Inventario_beniBulk.class);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    // ========================= WORKFLOW =========================

    public void predisponiAllaFirma(ActionContext context) throws BusinessProcessException {
        validaStatoPerFirma();
        try {
            Doc_trasporto_rientroBulk doc = getComp().predisponiAllaFirma(context.getUserContext(), getDoc());
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
            ps.oldSel = (BitSet) oldSelection.clone();
            ps.newSel = (BitSet) newSelection.clone();
            ps.selectionAccumulata = (BitSet) oldSelection.clone(); // CRITICO: inizializza accumulo

            for (int i = 0; i < bulks.length; i++) {
                if (oldSelection.get(i) == newSelection.get(i)) continue;
                OggettoBulk o = bulks[i];
                Inventario_beniBulk bene = (Inventario_beniBulk) o;

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

    /**
     * : Versione che accetta il bene corrente come parametro
     */
    public String getMessaggioSingoloBene(boolean isEliminazione, Inventario_beniBulk beneCorrente) {
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
            String pluraleAccessorio = (numAccessori == 1) ? " bene accessorio" : " beni accessori";
            msg.append(pluraleAccessorio).append(".\n\n");
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
            String pluraleAccessorio = (numAccessori == 1) ? " bene accessorio" : " beni accessori";
            String verbo = (numAccessori == 1) ? "associato" : "associati";
            msg.append(pluraleAccessorio).append(" ").append(verbo).append(".\n\n");
            String articoloFinale = (numAccessori == 1) ? "questo accessorio" : "questi accessori";
            String azione = getTipo().equals(TRASPORTO) ? "nell'aggiunta" : "nel rientro";
            msg.append("Vuoi includere anche ").append(articoloFinale).append(" ").append(azione).append("?");
        }

        return msg.toString();
    }



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
                // Elimina bene principale CON accessori
                eliminaBeneCorrente(context);
            } else {
                // Elimina SOLO bene principale SENZA accessori
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
                getComp().eliminaBeniPrincipaleConAccessori(
                        context.getUserContext(),
                        getDoc(),
                        bene,
                        accessori);
            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    /**
     * Elimina SOLO il bene principale, mantenendo gli accessori
     */
    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context) throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);

        if (bene != null) {
            try {
                // Elimina solo il bene principale, NON gli accessori
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
            // Usa selectionAccumulata invece di oldSel
            BitSet tempSelection = (BitSet) pendingAdd.selectionAccumulata.clone();

            // Aggiungi il bene principale al BitSet
            boolean benePrincipaleAggiunto = false;
            for (int i = 0; i < pendingAdd.bulks.length; i++) {
                if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                    Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                    if (b.equalsByPrimaryKey(bene)) {
                        tempSelection.set(i);
                        benePrincipaleAggiunto = true;

                        break;
                    }
                }
            }

            // Gestisci gli accessori se richiesto
            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = getAccessoriCorrente(false);

                if (accessori != null && !accessori.isEmpty()) {

                    // Aggiungi al BitSet gli accessori presenti in bulks[]
                    int accessoriInBulks = 0;
                    for (Inventario_beniBulk acc : accessori) {
                        for (int i = 0; i < pendingAdd.bulks.length; i++) {
                            if (pendingAdd.bulks[i] instanceof Inventario_beniBulk) {
                                Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                                if (b.equalsByPrimaryKey(acc)) {
                                    tempSelection.set(i);
                                    accessoriInBulks++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            //  Chiama il component per i beni in bulks[]
            // Questo aggiunge il principale + accessori trovati in bulks[]
            modificaBeniConAccessoriComponente(context, pendingAdd.bulks,
                    pendingAdd.selectionAccumulata, tempSelection);


            // Aggiungi gli accessori NON presenti in bulks[]
            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = getAccessoriCorrente(false);
                aggiungiAccessoriMancanti(context, accessori);
            }

            //  Aggiorna selectionAccumulata per il prossimo giro
            pendingAdd.selectionAccumulata = (BitSet) tempSelection.clone();

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }


    /**
     * Aggiunge direttamente gli accessori che NON sono presenti nell'array bulks[]
     * (accessori in pagine successive del selezionatore)
     */
    private void aggiungiAccessoriMancanti(ActionContext context, List<Inventario_beniBulk> accessori)
            throws BusinessProcessException {

        if (accessori == null || accessori.isEmpty()) return;

        try {
            for (Inventario_beniBulk acc : accessori) {
                // Verifica se l'accessorio è presente in bulks[]
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

                // Se NON è in bulks[], aggiungilo direttamente con una chiamata separata
                if (!trovatoInBulks) {

                    // Crea un array con solo questo accessorio
                    OggettoBulk[] accessorioArray = new OggettoBulk[]{acc};
                    BitSet vuoto = new BitSet(1);
                    BitSet selezionato = new BitSet(1);
                    selezionato.set(0);

                    // Chiamata diretta al component per questo accessorio
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

    // ========================= METODI DI VERIFICA =========================

    public boolean hasBeniPrincipaliConAccessoriPerEliminazione() {
        return pendingDelete != null && !pendingDelete.principaliConAccessori.isEmpty();
    }

    public boolean hasBeniPrincipaliConAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.principaliConAccessori.isEmpty();
    }

    public boolean hasBeniAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.accessori.isEmpty();
    }

    // ========================= STATISTICHE =========================

    public int getNumeroBeniPrincipaliConAccessori() {
        return (pendingAdd != null) ? pendingAdd.principaliConAccessori.size() : 0;
    }

    public int getNumeroBeniAccessoriTotaliPendenti() {
        if (pendingAdd == null) return 0;
        return (int) pendingAdd.principaliConAccessori.values().stream().mapToInt(List::size).sum();
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

    // ========================= GETTERS/SETTERS =========================

    public int getIndexBeneCurrentePerEliminazione() {
        return indexBeneCurrentePerEliminazione;
    }

    public void setIndexBeneCurrentePerEliminazione(int value) {
        this.indexBeneCurrentePerEliminazione = value;
    }

    public int getIndexBeneCurrentePerAggiunta() {
        return indexBeneCurrentePerAggiunta;
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

    // ========================= COMPONENT SESSION =========================

    protected DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        return (DocTrasportoRientroComponentSession) createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
    }



    /**
     * Elabora un singolo bene principale con o senza accessori
     */
    public void elaboraBeneConAccessori(ActionContext context, boolean isEliminazione,
                                        Inventario_beniBulk beneCorrente, boolean includiAccessori)
            throws BusinessProcessException {

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


}