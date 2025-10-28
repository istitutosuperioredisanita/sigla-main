package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario00.bp.RigheInvDaFatturaCRUDController;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.STATO_INSERITO;

/**
 * Business Process - SOLO UI, DELEGA AL COMPONENT
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP implements SelectionListener {

    private it.cnr.jada.persistency.sql.CompoundFindClause clauses;

    public static final String DOC_T_R = "Documenti Di";


    // ========================================
    // COSTRUTTORI
    // ========================================

    public CRUDTrasportoBeniInvBP() {
        super();
        setTab("tab", "tabTrasportoTestata");
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

    @Override
    protected void init(it.cnr.jada.action.Config config, ActionContext context)
            throws BusinessProcessException {
        super.init(config, context);
        resetTabs();
    }

//    /** Percorso su CMIS: cartella per DOC_T_R / esercizio / inventario / tiDocumento / pgDocTrasportoRientro**/
//    @Override
//    protected String getStorePath(Doc_trasporto_rientroBulk allegatoDDT, boolean create)
//            throws BusinessProcessException {
//        return Doc_trasporto_rientroBulk.getStorePathDDT(DOC_T_R,
//                allegatoDDT.getEsercizio(), allegatoDDT.getPgInventario(),
//                allegatoDDT.getTiDocumento(),allegatoDDT.getPgDocTrasportoRientro());
//    }
//
//    @Override
//    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
//        return AllegatoGenericoBulk.class;
//    }

    // ========================================
    // CONTROLLERS
    // ========================================

    /**
     * Controller per i dettagli dei beni da trasportare (Inventario_beniBulk).
     * Usato nella tab principale per selezionare i beni.
     */
    protected final RigheInvDaFatturaCRUDController dettBeniController = new RigheInvDaFatturaCRUDController(
            "DettBeniController",
            Inventario_beniBulk.class,
            "DettagliTrasporto",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            try {
                return (isInserting() || isEditing()) && !isDocumentoAnnullato()
                        ? selectDettagliTrasportobyClause(context)
                        : new it.cnr.jada.util.EmptyRemoteIterator();
            } catch (BusinessProcessException e) {
                return null;
            }
        }

        @Override
        public void removeAll(ActionContext context) throws ValidationException, BusinessProcessException {
            if (isDocumentoAnnullato()) {
                throw new ValidationException("Impossibile modificare un documento annullato");
            }
            eliminaDettagliTrasportoConBulk(context);
            reset(context);
        }

        @Override
        protected void removeDetails(ActionContext context, OggettoBulk[] details) throws BusinessProcessException {
            if (isDocumentoAnnullato()) {
                throw new BusinessProcessException("Impossibile modificare un documento annullato");
            }
            eliminaDettagliTrasportoConBulk(context, details);
        }
    };

    /**
     * Controller per l'editing dei dettagli già associati (Doc_trasporto_rientro_dettBulk).
     * Usato per modificare quantità e altre proprietà dei beni già aggiunti.
     */
    private final RemoteDetailCRUDController editDettController = new RemoteDetailCRUDController(
            "editDettController",
            Doc_trasporto_rientro_dettBulk.class,
            "EditDettagliTrasporto",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            try {
                return isInserting() || isDocumentoAnnullato()
                        ? new it.cnr.jada.util.EmptyRemoteIterator()
                        : selectEditDettagliTrasportobyClause(context);
            } catch (BusinessProcessException e) {
                return null;
            }
        }
    };

    // ========================================
    // GESTIONE DETTAGLI
    // ========================================

    private void eliminaDettagliTrasportoConBulk(ActionContext context) throws BusinessProcessException {
        try {
            getComp().eliminaBeniAssociatiConBulk(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    private void eliminaDettagliTrasportoConBulk(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaBeniAssociatiConBulk(
                    context.getUserContext(),
                    (Doc_trasporto_rientroBulk) getModel(),
                    details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private RemoteIterator selectDettagliTrasportobyClause(ActionContext context)
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

    private RemoteIterator selectEditDettagliTrasportobyClause(ActionContext context)
            throws BusinessProcessException {
        try {
            it.cnr.jada.persistency.sql.CompoundFindClause clauses =
                    ((RemoteDetailCRUDController) getEditDettController()).getFilter();

            return getComp().selectEditDettagliTrasporto(
                    context.getUserContext(),
                    getDoc(),
                    Doc_trasporto_rientro_dettBulk.class,
                    clauses);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    public RemoteIterator getListaBeniDaTrasportare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(
                userContext,
                getDoc(),
                beni_da_escludere,
                clauses);
    }

    // ========================================
    // INIZIALIZZAZIONE MODELLI
    // ========================================

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        bulk = super.initializeModelForEdit(context, bulk);
        bulk = initializeDocTrasporto(bulk);

        // Controlla se il documento è annullato e mostra il messaggio
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        if (doc.isAnnullato()) {
            setStatus(VIEW);
            setMessage("ATTENZIONE: Il DDT è stato ANNULLATO");
        }

        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocTrasporto(super.initializeModelForInsert(context, bulk));
    }

    @Override
    public OggettoBulk initializeModelForFreeSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocTrasporto(super.initializeModelForFreeSearch(context, bulk));
    }

    @Override
    public OggettoBulk initializeModelForSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocTrasporto(super.initializeModelForSearch(context, bulk));
    }

    private OggettoBulk initializeDocTrasporto(OggettoBulk bulk) {
        ((Doc_trasporto_rientroBulk) bulk).setTiDocumento(TRASPORTO);
        return bulk;
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    public void resetTabs() {
        setTab("tab", "tabTrasportoTestata");
    }

    // ========================================
    // VISIBILITÀ UI
    // ========================================

    /**
     * Verifica se il documento corrente è annullato.
     */
    private boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    /**
     * Verifica se il documento è in modalità visualizzazione.
     * Un documento annullato deve essere sempre in sola lettura.
     */
    public boolean isVisualizzazione() {
        return isDocumentoAnnullato();
    }

    /**
     * Il bottone "Aggiungi Bene" è abilitato solo in inserimento e se il documento è in stato INSERITO.
     */
    public boolean isBottoneAggiungiBeneEnabled() {
        return isInserting() && getDoc() != null && getDoc().isInserito() && !isDocumentoAnnullato();
    }

    public boolean isDeleteButtonEnabled() {
        return !isVisualizzazione();
    }

    @Override
    public boolean isEditable() {
        // Documenti annullati non sono mai modificabili
        if (isDocumentoAnnullato()) {
            return false;
        }
        return !isVisualizzazione() && super.isEditable();
    }

    public String getLabelData_registrazione() {
        return "Data Trasporto";
    }

    // ========================================
    // WORKFLOW E STATO
    // ========================================

    /**
     * AZIONE: Predispone il documento alla firma.
     * TRANSIZIONE DI STATO: INSERITO (INS) → PREDISPOSTO ALLA FIRMA (PAF)
     */
    public void predisponiAllaFirma(ActionContext context) throws BusinessProcessException {
        validaStatoPerFirma();
        try {
            getComp().predisponiAllaFirma(context.getUserContext(), getDoc());
            commitUserTransaction();
            setMessage("Documento predisposto alla firma con successo");
        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    /**
     * Valida che il documento sia in stato INSERITO prima di predisporlo alla firma.
     */
    private void validaStatoPerFirma() throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile predisporre alla firma un documento annullato");
        }
        if (!getDoc().isInserito()) {
            throw new BusinessProcessException(
                    "Il documento deve essere in stato 'Inserito' per essere predisposto alla firma");
        }
    }

    // ========================================
    // SELECTION LISTENER
    // ========================================

    @Override
    public void clearSelection(ActionContext context) throws BusinessProcessException {
        try {
            getComp().annullaModificaTrasportoBeni(context.getUserContext());
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void deselectAll(ActionContext context) {
        // Implementazione vuota - la deselezione viene gestita via clearSelection
    }

    @Override
    public BitSet getSelection(ActionContext context, OggettoBulk[] bulks, BitSet currentSelection)
            throws BusinessProcessException {
        return currentSelection;
    }

    @Override
    public void initializeSelection(ActionContext context) throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            return; // Non inizializzare la selezione per documenti annullati
        }
        try {
            getComp().inizializzaBeniDaTrasportare(context.getUserContext());
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
            getComp().trasportaTuttiBeni(
                    context.getUserContext(),
                    getDoc(),
                    getClauses());
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
            getComp().modificaBeniTrasportati(
                    context.getUserContext(),
                    getDoc(),
                    bulks,
                    oldSelection,
                    newSelection);
            return newSelection;
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    // ========================================
    // TOOLBAR
    // ========================================

    @Override
    protected it.cnr.jada.util.jsp.Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.concat(
                Arrays.stream(super.createToolbar()),
                Stream.of(
                        new it.cnr.jada.util.jsp.Button(props, "CRUDToolbar.predisponiAllaFirma")
                )
        ).toArray(it.cnr.jada.util.jsp.Button[]::new);
    }

    public boolean isPredisponiAllaFirmaButtonHidden() {
        // Nascondi il bottone per documenti annullati
        if (isDocumentoAnnullato()) {
            return true;
        }

        // Mostra il bottone quando il documento è in stato INSERITO
        return !(getModel() != null &&
                getModel().getCrudStatus() == OggettoBulk.NORMAL &&
                STATO_INSERITO.equals(getDoc().getStato()));
    }

    // ========================================
    // HELPER E GETTER
    // ========================================

    public Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    private DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        try {
            return (DocTrasportoRientroComponentSession) createComponentSession(
                    "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                    DocTrasportoRientroComponentSession.class);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public RemoteDetailCRUDController getDettBeniController() {
        return dettBeniController;
    }

    public RemoteDetailCRUDController getEditDettController() {
        return editDettController;
    }

    public it.cnr.jada.persistency.sql.CompoundFindClause getClauses() {
        return clauses;
    }

    public void setClauses(it.cnr.jada.persistency.sql.CompoundFindClause newClauses) {
        clauses = newClauses;
    }


    @Override
    public boolean isPrintButtonHidden() {
        return !Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .flatMap(Doc_trasporto_rientroBulk -> Optional.ofNullable(Doc_trasporto_rientroBulk.getPgDocTrasportoRientro()))
                .isPresent();
    }

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
        param.setValoreParam(
                Optional.ofNullable(docT.getEsercizio())
                        .map(String::valueOf)
                        .orElse(null)
        );
        param.setParamType(Integer.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("pgInventario");
        param.setValoreParam(
                Optional.ofNullable(docT.getPgInventario())
                        .map(String::valueOf)
                        .orElse(null)
        );
        param.setParamType(Integer.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("tiDocumento");
        param.setValoreParam(
                docT.getTiDocumento()
        );
        param.setParamType(String.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

        param = new Print_spooler_paramBulk();
        param.setNomeParam("pgDocTrasportoRientro");
        param.setValoreParam(
                Optional.ofNullable(docT.getPgDocTrasportoRientro())
                        .map(String::valueOf)
                        .orElse(null)
        );
        param.setParamType(Integer.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);

    }
}