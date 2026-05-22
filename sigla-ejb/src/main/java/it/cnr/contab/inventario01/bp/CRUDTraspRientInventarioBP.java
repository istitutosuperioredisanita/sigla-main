package it.cnr.contab.inventario01.bp;

import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
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
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.EmptyRemoteIterator;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;
import it.cnr.jada.util.jsp.Button;
import it.cnr.si.spring.storage.StorageObject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

/**
 * Business Process comune per documenti di Trasporto/Rientro beni inventariali.
 *
 * Gestisce:
 * - testata e dettagli;
 * - selezione beni/accessori;
 * - allegati;
 * - invio e completamento firma HappySign;
 * - salvataggio definitivo;
 * - stampa documento.
 *
 * Regola fondamentale HappySign:
 * dopo firma il documento può risultare "editable" solo per permettere
 * la gestione degli allegati, ma testata e dettagli devono restare bloccati.
 */
public abstract class CRUDTraspRientInventarioBP<
        T extends AllegatoDocTraspRientroBulk,
        K extends Doc_trasporto_rientroBulk>
        extends AllegatiCRUDBP<T, K>
        implements SelectionListener {

    private static final String COMPONENT_SESSION =
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession";

    private static final String CONFIG_COMPONENT_SESSION =
            "CNRCONFIG00_EJB_Configurazione_cnrComponentSession";

    private static final String REPORT_DOC_TRASPORTO_RIENTRO =
            "doc_trasporto_rientro.jasper";

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private String tipo;
    private boolean isAmministratore;
    private boolean isVisualizzazione;
    private boolean isGestioneInvioInFirmaAttiva;
    private boolean skipAllegatiReload;

    private PendingSelection pendingAdd;
    private PendingSelection pendingDelete;

    private int indexBeneCurrentePerEliminazione;
    private int indexBeneCurrentePerAggiunta;
    private boolean ultimaOperazioneEliminazione;
    private CompoundFindClause clauses;

    /**
     * Stato temporaneo della selezione ricorsiva dei beni con accessori.
     */
    public static class PendingSelection {

        private final Map<InventarioDocTRBulk, List<InventarioDocTRBulk>> principaliConAccessori =
                new LinkedHashMap<>();

        private final List<InventarioDocTRBulk> accessori =
                new ArrayList<>();

        private final List<InventarioDocTRBulk> principaliSenza =
                new ArrayList<>();

        private OggettoBulk[] bulks;
        private BitSet oldSel;
        private BitSet newSel;
        private BitSet selectionAccumulata;

        private void clear() {
            principaliConAccessori.clear();
            accessori.clear();
            principaliSenza.clear();
            bulks = null;
            oldSel = null;
            newSel = null;
            selectionAccumulata = null;
        }

        private boolean isEmpty() {
            return principaliConAccessori.isEmpty()
                    && accessori.isEmpty()
                    && principaliSenza.isEmpty();
        }

        public Map<InventarioDocTRBulk, List<InventarioDocTRBulk>> getPrincipaliConAccessori() {
            return principaliConAccessori;
        }

        public List<InventarioDocTRBulk> getAccessori() {
            return accessori;
        }

        public List<InventarioDocTRBulk> getPrincipaliSenza() {
            return principaliSenza;
        }

        public OggettoBulk[] getBulks() {
            return bulks;
        }

        public BitSet getOldSel() {
            return oldSel;
        }

        public BitSet getNewSel() {
            return newSel;
        }

        public BitSet getSelectionAccumulata() {
            return selectionAccumulata;
        }
    }

    public CRUDTraspRientInventarioBP() {
        super("Tn");
    }

    public CRUDTraspRientInventarioBP(String function) {
        super(function + "Tn");
    }

    protected abstract String getDettagliControllerName();

    protected abstract String getEditDettagliControllerName();

    public abstract String getMainTabName();

    public abstract String getLabelData_registrazione();

    public abstract void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException;

    public abstract void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException;

    public abstract void modificaBeniConAccessoriComponente(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException;

    public abstract Class getDocumentoClassDett();

    @Override
    protected void init(Config config, ActionContext context)
            throws BusinessProcessException {

        setTipo(this instanceof CRUDTrasportoBeniInvBP ? TRASPORTO : RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());

        try {
            setAmministratore(
                    UtenteBulk.isAmministratoreInventario(context.getUserContext())
            );

            Configurazione_cnrComponentSession confSession =
                    (Configurazione_cnrComponentSession) createComponentSession(
                            CONFIG_COMPONENT_SESSION,
                            Configurazione_cnrComponentSession.class
                    );

            boolean invioFirmaAttivo =
                    isProfiloSpringIssAttivo()
                            && confSession.isGestioneInvioInFirmaDocTRAttivo(
                            context.getUserContext()
                    );

            setGestioneInvioInFirmaAttiva(invioFirmaAttivo);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }

        super.init(config, context);
        resetTabs();
    }

    private boolean isProfiloSpringIssAttivo() {
        WebApplicationContext springCtx =
                ContextLoader.getCurrentWebApplicationContext();

        return springCtx != null
                && springCtx.getEnvironment() != null
                && Arrays.stream(springCtx.getEnvironment().getActiveProfiles())
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch("iss"::equalsIgnoreCase);
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        bulk = super.initializeModelForInsert(context, bulk);

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        doc.setArchivioAllegati(new BulkList<>());

        return bulk;
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        try {
            setAmministratore(
                    UtenteBulk.isAmministratoreInventario(context.getUserContext())
            );

            bulk = super.initializeModelForEdit(context, bulk);

            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

            if (doc != null && doc.isAnnullato()) {
                setErrorMessage("Documento ANNULLATO - Nessuna modifica consentita");
            }

            return bulk;

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public OggettoBulk initializeModelForEditAllegati(
            ActionContext context,
            OggettoBulk bulk)
            throws BusinessProcessException {

        if (isSkipAllegatiReload()) {
            return bulk;
        }

        return super.initializeModelForEditAllegati(context, bulk);
    }

    @Override
    public void resetForSearch(ActionContext context)
            throws BusinessProcessException {

        super.resetForSearch(context);
        resetTabs();
    }

    public void resetTabs() {
        setTab("tab", getMainTabName());
    }

    @Override
    public void setTab(String tabName, String tabValue) {
        if ("tab".equals(tabName) && tabValue != null) {
            Doc_trasporto_rientroBulk doc = getDoc();

            boolean isTabDettagli = tabValue.endsWith("Dettaglio");
            boolean isTabAllegati = tabValue.endsWith("Allegati");

            if ((isTabDettagli || isTabAllegati)
                    && doc != null
                    && doc.getCrudStatus() == OggettoBulk.UNDEFINED) {
                setErrorMessage(
                        "Non è possibile cambiare tab: in questa fase è consentita solo la ricerca."
                );
                return;
            }

            if (isTabDettagli && !validaAccessoTabDettagli()) {
                return;
            }

            if (isTabAllegati && !validaAccessoTabAllegati(doc)) {
                return;
            }
        }

        super.setTab(tabName, tabValue);
    }

    private boolean validaAccessoTabDettagli() {
        try {
            validaDatiPerDettagli();
            return true;
        } catch (ApplicationException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
    }

    private boolean validaAccessoTabAllegati(Doc_trasporto_rientroBulk doc) {
        if (!isAllegatiAccessibili()) {
            setErrorMessage("Il tab Allegati sarà disponibile solo dopo la firma del documento.");
            return false;
        }

        if (!hasBeniSalvati(doc)) {
            setErrorMessage("Impossibile accedere agli allegati: salvare il documento con almeno un bene.");
            return false;
        }

        return true;
    }

    private boolean hasBeniSalvati(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getDoc_trasporto_rientro_dettColl() == null) {
            return false;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            if (obj instanceof Doc_trasporto_rientro_dettBulk) {
                Doc_trasporto_rientro_dettBulk dett =
                        (Doc_trasporto_rientro_dettBulk) obj;

                if (dett.getCrudStatus() == OggettoBulk.NORMAL) {
                    return true;
                }
            }
        }

        return false;
    }

    public Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAmministratore() {
        return isAmministratore;
    }

    public void setAmministratore(boolean amministratore) {
        isAmministratore = amministratore;
    }

    public boolean isVisualizzazione() {
        return isVisualizzazione;
    }

    public void setVisualizzazione(boolean visualizzazione) {
        isVisualizzazione = visualizzazione;
    }

    public boolean isGestioneInvioInFirmaAttiva() {
        return isGestioneInvioInFirmaAttiva;
    }

    public void setGestioneInvioInFirmaAttiva(boolean gestioneInvioInFirmaAttiva) {
        isGestioneInvioInFirmaAttiva = gestioneInvioInFirmaAttiva;
    }

    public boolean isSkipAllegatiReload() {
        return skipAllegatiReload;
    }

    public void setSkipAllegatiReload(boolean skipAllegatiReload) {
        this.skipAllegatiReload = skipAllegatiReload;
    }

    private boolean hasValidModel() {
        return getModel() != null && getDoc() != null;
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

    public boolean isDocumentoInAttesaDiFirmaHappySign() {
        return getDoc() != null && getDoc().isInAttesaDiFirma();
    }

    public boolean isDocumentoFirmatoDaCompletare() {
        return getDoc() != null && getDoc().isFirmatoDaCompletare();
    }

    /**
     * Indica se testata e dettagli sono bloccati.
     * Non usarlo per stabilire se gli allegati sono modificabili.
     */
    public boolean isTestataEDettagliBloccati() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return false;
        }

        return isVisualizzazione()
                || doc.isAnnullato()
                || doc.isDefinitivo()
                || doc.isInAttesaDiFirma()
                || doc.isFirmatoDaCompletare();
    }

    /**
     * Wrapper mantenuto per compatibilità con JSP/properties.
     */
    public boolean isDocumentoNonModificabile() {
        return isTestataEDettagliBloccati();
    }

    /**
     * Wrapper mantenuto per compatibilità con JSP/properties.
     */
    public boolean isTestataODettagliNonModificabili() {
        return isTestataEDettagliBloccati();
    }

    public boolean isDettagliModificabili() {
        return !isTestataEDettagliBloccati()
                && (isEditing() || isInserting());
    }

    @Override
    protected Boolean isPossibileCancellazione(AllegatoGenericoBulk allegato) {
        return isAllegatiModificabili();
    }

    public boolean isTipoRitiroVisible() {
        return hasValidModel() && !isSmartworking();
    }

    public boolean isTerzoSmartworkingVisible() {
        return hasValidModel() && isSmartworking();
    }

    public boolean isDestinazioneVisible() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().hasTipoRitiroSelezionato();
    }

    public boolean isAssegnatarioVisible() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().isRitiroIncaricato();
    }

    public boolean isNominativoVettoreVisible() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().isNominativoVettoreVisible();
    }

    public boolean isNoteAbilitate() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().getTipoMovimento() != null
                && getDoc().getTipoMovimento().isAbilitaNote();
    }

    public boolean isStatoVisible() {
        return hasValidModel()
                && getDoc().getStato() != null
                && !isInserting();
    }

    public boolean isNoteVisible() {
        return hasValidModel() && getDoc().isNoteRitiroEnabled();
    }

    public boolean isTipoMovimentoReadOnly() {
        return isCampiCriticiReadOnly();
    }

    private boolean isCampiCriticiReadOnly() {
        return isTestataEDettagliBloccati()
                || (isEditing()
                && hasValidModel()
                && getDoc().getDoc_trasporto_rientro_dettColl() != null
                && !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty());
    }

    public boolean isQuantitaEnabled() {
        return isDettagliModificabili();
    }

    public boolean isSmartworking() {
        return hasValidModel() && getDoc().isSmartworking();
    }

    @Override
    public boolean isDeleteButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return false;
        }

        if (doc.isAnnullato() || doc.isInviatoInFirma()) {
            return true;
        }

        if (doc instanceof DocumentoRientroBulk && doc.isDefinitivo()) {
            return true;
        }

        if (doc instanceof DocumentoTrasportoBulk) {
            return haRientroNonAnnullato();
        }

        return false;
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return !isDeleteButtonHidden();
    }

    private boolean haRientroNonAnnullato() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (!(doc instanceof DocumentoTrasportoBulk)
                || doc.getDoc_trasporto_rientro_dettColl() == null
                || doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            return false;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            if (obj instanceof DocumentoTrasportoDettBulk) {
                DocumentoTrasportoDettBulk dettaglio =
                        (DocumentoTrasportoDettBulk) obj;

                DocumentoRientroDettBulk rientroDett =
                        dettaglio.getDocRientroDettRif();

                if (rientroDett != null
                        && rientroDett.getDoc_trasporto_rientro() != null
                        && !rientroDett.getDoc_trasporto_rientro().isAnnullato()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isNewButtonEnabled() {
        return !isVisualizzazione();
    }

    public boolean isStampaDocButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return true;
        }

        return !getMainTabName().equals(getTab("tab"));
    }

    public boolean isStampaDocButtonEnabled() {
        return !isStampaDocButtonHidden() && !isVisualizzazione();
    }

    private boolean hasAllegatoFirmato() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        String aspectFirmatoAtteso = getAspectFirmato(doc);

        if (aspectFirmatoAtteso == null) {
            return false;
        }

        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato =
                        (AllegatoDocTraspRientroBulk) obj;

                if (aspectFirmatoAtteso.equals(allegato.getAspectName())
                        && (OggettoBulk.NORMAL == allegato.getCrudStatus() || isEditing())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getAspectFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc instanceof DocumentoTrasportoBulk) {
            return AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        }

        if (doc instanceof DocumentoRientroBulk) {
            return AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
        }

        return null;
    }

    @Override
    protected void initializePrintBP(
            ActionContext context,
            AbstractPrintBP abstractPrintBP) {

        OfflineReportPrintBP printbp = (OfflineReportPrintBP) abstractPrintBP;
        printbp.setReportName("/cnrdocamm/docamm/doc_trasporto_rientro.jasper");

        Doc_trasporto_rientroBulk doc = Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("Modello vuoto!"));

        addPrintParam(printbp, "esercizio", doc.getEsercizio(), Integer.class);
        addPrintParam(printbp, "pg_inventario", doc.getPgInventario(), Long.class);
        addPrintParam(printbp, "ti_documento", doc.getTiDocumento(), String.class);
        addPrintParam(printbp, "pg_doc_trasporto_rientro",
                doc.getPgDocTrasportoRientro(), Long.class);
    }

    private void addPrintParam(
            OfflineReportPrintBP printbp,
            String nome,
            Object valore,
            Class<?> type) {

        Print_spooler_paramBulk param = new Print_spooler_paramBulk();
        param.setNomeParam(nome);
        param.setValoreParam(valore == null ? null : valore.toString());
        param.setParamType(type.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    @Override
    protected Button[] createToolbar() {
        Properties props =
                it.cnr.jada.util.Config.getHandler().getProperties(getClass());

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

    public boolean isInviaInFirmaButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        int status = getModel() != null ? getModel().getCrudStatus() : -1;

        return !isGestioneInvioInFirmaAttiva()
                || doc == null
                || (status != OggettoBulk.NORMAL && status != OggettoBulk.TO_BE_UPDATED)
                || !doc.isInviabileAllaFirma();
    }

    protected final RemoteDetailCRUDController dettBeniController =
            new RemoteDetailCRUDController(
                    "DettBeniController",
                    InventarioDocTRBulk.class,
                    "",
                    COMPONENT_SESSION,
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
                public void removeAll(ActionContext context)
                        throws ValidationException, BusinessProcessException {

                    if (isTestataEDettagliBloccati()) {
                        throw new ValidationException(
                                "Impossibile modificare i beni del documento in questo stato."
                        );
                    }

                    eliminaTuttiBeniDaDettagli(context);
                    reset(context);
                }

                @Override
                public void removeDetails(ActionContext context, OggettoBulk[] details)
                        throws BusinessProcessException {

                    if (isTestataEDettagliBloccati()) {
                        throw new BusinessProcessException(
                                "Impossibile modificare i beni del documento in questo stato."
                        );
                    }

                    try {
                        PendingSelection ps = creaPendingDelete(context, details);

                        if (ps.getPrincipaliConAccessori().isEmpty()) {
                            eliminaBeniDaDettagli(context, details);
                        } else {
                            setPendingDelete(ps);
                        }

                    } catch (ComponentException | RemoteException e) {
                        throw handleException(e);
                    }
                }
            };

    protected final RemoteDetailCRUDController editDettController =
            new RemoteDetailCRUDController(
                    "editDettController",
                    getDocumentoClassDett(),
                    "",
                    COMPONENT_SESSION,
                    this) {

                public String getName() {
                    return getEditDettagliControllerName();
                }

                @Override
                protected RemoteIterator createRemoteIterator(ActionContext context) {
                    if (isInserting() || isEditing() || isViewing()) {
                        try {
                            return selectEditDettaglibyClause(context);
                        } catch (BusinessProcessException e) {
                            throw new RuntimeException("Errore caricamento dettagli modifica", e);
                        }
                    }

                    return new EmptyRemoteIterator();
                }
            };

    private PendingSelection creaPendingDelete(ActionContext context, OggettoBulk[] details)
            throws ComponentException, RemoteException, BusinessProcessException {

        PendingSelection ps = new PendingSelection();
        ps.bulks = details;

        for (OggettoBulk detail : details) {
            InventarioDocTRBulk bene = (InventarioDocTRBulk) detail;

            if (bene.isBeneAccessorio()) {
                ps.accessori.add(bene);
                continue;
            }

            List<InventarioDocTRBulk> accessori =
                    getComp().cercaBeniAccessoriNeiDettagliSalvati(
                            context.getUserContext(),
                            getDoc(),
                            bene
                    );

            if (accessori != null && !accessori.isEmpty()) {
                ps.principaliConAccessori.put(bene, accessori);
            } else {
                ps.principaliSenza.add(bene);
            }
        }

        return ps;
    }

    protected RemoteIterator selectEditDettaglibyClause(ActionContext context)
            throws BusinessProcessException {

        try {
            CompoundFindClause filters = getEditDettController().getFilter();

            return getComp().selectEditDettagliTrasporto(
                    context.getUserContext(),
                    getDoc(),
                    getDocumentoClassDett(),
                    filters
            );

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
                    InventarioDocTRBulk.class
            );

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    public void inviaAllaFirma(ActionContext context)
            throws BusinessProcessException {

        try {
            fillModel(context);

            Doc_trasporto_rientroBulk doc = getDoc();

            if (doc == null) {
                throw new BusinessProcessException("Documento non presente.");
            }

            if (doc.getCrudStatus() == OggettoBulk.TO_BE_CREATED) {
                create(context);
                commitUserTransaction();
                doc = getDoc();
            } else if (doc.getCrudStatus() == OggettoBulk.TO_BE_UPDATED) {
                update(context);
                commitUserTransaction();
                doc = getDoc();
            }

            validaStatoPerFirma();

            Doc_trasporto_rientroBulk docFirmabile =
                    getComp().inviaDocumentoAllaFirma(
                            context.getUserContext(),
                            doc
                    );

            setModel(context, docFirmabile);
            setStatus(VIEW);

            commitUserTransaction();

            setMessage("Documento inviato alla firma con successo. UUID HappySign: "
                    + docFirmabile.getIdFlussoHappysign());

        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);

        } catch (BusinessProcessException e) {
            rollbackUserTransaction();
            throw e;

        } catch (Exception e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void validaStatoPerFirma()
            throws BusinessProcessException {

        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            throw new BusinessProcessException("Documento non presente");
        }

        if (doc.isAnnullato()) {
            throw new BusinessProcessException(
                    "Impossibile inviare in firma un documento annullato"
            );
        }

        if (!doc.isInserito()) {
            throw new BusinessProcessException(
                    "Il documento deve essere in stato 'Inserito' per essere inviato in firma"
            );
        }

        if (!doc.isInviabileAllaFirma()) {
            throw new BusinessProcessException(
                    "Documento non completo per l'invio in firma"
            );
        }
    }

    public void annullaDoc(ActionContext context)
            throws BusinessProcessException {

        try {
            Doc_trasporto_rientroBulk doc =
                    getComp().annullaDocumento(
                            context.getUserContext(),
                            getDoc()
                    );

            setModel(context, doc);
            commitUserTransaction();

            doc.setStato(Doc_trasporto_rientroBulk.STATO_ANNULLATO);
            doc.setCrudStatus(OggettoBulk.NORMAL);

            setStatus(VIEW);
            setMessage("Documento annullato");

        } catch (ComponentException | RemoteException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    @Override
    public void clearSelection(ActionContext context)
            throws BusinessProcessException {

        try {
            annullaModificaComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void deselectAll(ActionContext context) {
    }

    @Override
    public BitSet getSelection(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet currentSelection)
            throws BusinessProcessException {

        return currentSelection;
    }

    @Override
    public void initializeSelection(ActionContext context)
            throws BusinessProcessException {

        if (isTestataEDettagliBloccati()) {
            return;
        }

        try {
            inizializzaSelezioneComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void selectAll(ActionContext context)
            throws BusinessProcessException {

        if (isTestataEDettagliBloccati()) {
            throw new BusinessProcessException("Impossibile modificare il documento");
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();
            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

            List<InventarioDocTRBulk> beniFiltrati =
                    getComp().caricaBeniPerInserimento(
                            context.getUserContext(),
                            doc,
                            getClauses(),
                            isTrasporto
                    );

            validaMassimoBeni(beniFiltrati);

            getComp().validaBeniNonInAltriDocumenti(
                    context.getUserContext(),
                    doc,
                    beniFiltrati
            );

            getComp().selezionaTuttiBeni(
                    context.getUserContext(),
                    doc,
                    getClauses()
            );

            setClauses(null);
            getDettBeniController().resync(context);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public BitSet setSelection(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection)
            throws BusinessProcessException {

        if (isTestataEDettagliBloccati()) {
            throw new BusinessProcessException(
                    "Impossibile modificare i beni del documento in questo stato."
            );
        }

        try {
            fillModel(context);

            PendingSelection ps =
                    creaPendingAdd(context, bulks, oldSelection, newSelection);

            List<InventarioDocTRBulk> beniDaValidare =
                    raccogliBeniDaValidare(ps);

            validaMassimoBeni(beniDaValidare);

            if (!beniDaValidare.isEmpty()) {
                getComp().validaBeniNonInAltriDocumenti(
                        context.getUserContext(),
                        getDoc(),
                        beniDaValidare
                );
            }

            if (ps.getPrincipaliConAccessori().isEmpty()) {
                if (!ps.isEmpty()) {
                    modificaBeniConAccessoriComponente(
                            context,
                            bulks,
                            oldSelection,
                            newSelection
                    );
                    setModel(context, getDoc());
                }

                return newSelection;
            }

            setPendingAdd(ps);
            return newSelection;

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);

        } catch (FillException e) {
            throw new RuntimeException(e);
        }
    }

    private PendingSelection creaPendingAdd(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {

        PendingSelection ps = new PendingSelection();
        ps.bulks = bulks;
        ps.oldSel = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();
        ps.newSel = newSelection != null ? (BitSet) newSelection.clone() : new BitSet();
        ps.selectionAccumulata =
                oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();

        if (oldSelection == null || newSelection == null) {
            return ps;
        }

        for (int i = 0; i < bulks.length; i++) {
            if (oldSelection.get(i) == newSelection.get(i)) {
                continue;
            }

            InventarioDocTRBulk bene = (InventarioDocTRBulk) bulks[i];

            if (!newSelection.get(i)) {
                continue;
            }

            if (bene.isBeneAccessorio()) {
                ps.accessori.add(bene);
                continue;
            }

            List<InventarioDocTRBulk> accessori = cercaAccessoriPerBene(context, bene);

            if (accessori != null && !accessori.isEmpty()) {
                ps.principaliConAccessori.put(bene, accessori);
            } else {
                ps.principaliSenza.add(bene);
            }
        }

        return ps;
    }

    private List<InventarioDocTRBulk> cercaAccessoriPerBene(
            ActionContext context,
            InventarioDocTRBulk bene)
            throws ComponentException, RemoteException, BusinessProcessException {

        if (getDoc() instanceof DocumentoRientroBulk) {
            return getComp().cercaBeniAccessoriPresentinelTrasportoOriginale(
                    context.getUserContext(),
                    bene,
                    getDoc()
            );
        }

        return getComp().cercaBeniAccessoriAssociati(
                context.getUserContext(),
                bene
        );
    }

    private List<InventarioDocTRBulk> raccogliBeniDaValidare(PendingSelection ps) {
        List<InventarioDocTRBulk> beni = new ArrayList<>();

        beni.addAll(ps.getPrincipaliSenza());
        beni.addAll(ps.getAccessori());

        for (Map.Entry<InventarioDocTRBulk, List<InventarioDocTRBulk>> entry
                : ps.getPrincipaliConAccessori().entrySet()) {

            beni.add(entry.getKey());

            if (entry.getValue() != null) {
                beni.addAll(entry.getValue());
            }
        }

        return beni;
    }

    private void validaMassimoBeni(List<InventarioDocTRBulk> beni)
            throws ApplicationException {

        if (beni != null && beni.size() > 100) {
            throw new ApplicationException(
                    "Impossibile selezionare più di 100 beni contemporaneamente"
            );
        }
    }

    public InventarioDocTRBulk getBenePrincipaleCorrente(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        int index = isEliminazione
                ? indexBeneCurrentePerEliminazione
                : indexBeneCurrentePerAggiunta;

        if (ps == null || ps.getPrincipaliConAccessori().isEmpty()) {
            return null;
        }

        int idx = 0;

        for (InventarioDocTRBulk bene : ps.getPrincipaliConAccessori().keySet()) {
            if (idx == index) {
                return bene;
            }
            idx++;
        }

        return null;
    }

    public List<InventarioDocTRBulk> getAccessoriCorrente(boolean isEliminazione) {
        InventarioDocTRBulk bene = getBenePrincipaleCorrente(isEliminazione);
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;

        if (bene == null || ps == null) {
            return Collections.emptyList();
        }

        List<InventarioDocTRBulk> accessori =
                ps.getPrincipaliConAccessori().get(bene);

        return accessori == null ? Collections.emptyList() : accessori;
    }

    public String getMessaggioSingoloBene(boolean isEliminazione) {
        InventarioDocTRBulk bene = getBenePrincipaleCorrente(isEliminazione);

        if (bene == null) {
            return "";
        }

        int numAccessori = getAccessoriCorrente(isEliminazione).size();

        StringBuilder msg = new StringBuilder();

        if (isEliminazione) {
            msg.append("Il bene principale con codice: ")
                    .append(bene.getNr_inventario());

            appendEtichetta(msg, bene);

            msg.append(" ha ")
                    .append(numAccessori)
                    .append(numAccessori == 1 ? " bene accessorio" : " beni accessori")
                    .append(".\n\n")
                    .append("Come vuoi procedere?\n\n")
                    .append("• SI: Elimina il bene principale CON tutti gli accessori\n")
                    .append("• NO: Elimina SOLO il bene principale (mantieni accessori)\n")
                    .append("• ANNULLA: Interrompi l'operazione");

        } else {
            msg.append("La selezione include il bene principale con codice: ")
                    .append(bene.getNr_inventario());

            appendEtichetta(msg, bene);

            msg.append(" che ha ")
                    .append(numAccessori)
                    .append(numAccessori == 1
                            ? " bene accessorio associato"
                            : " beni accessori associati")
                    .append(".\n\n");

            String articolo = numAccessori == 1
                    ? "questo accessorio"
                    : "questi accessori";

            String azione = TRASPORTO.equals(getTipo())
                    ? "nell'aggiunta"
                    : "nel rientro";

            msg.append("Vuoi includere anche ")
                    .append(articolo)
                    .append(" ")
                    .append(azione)
                    .append("?");
        }

        return msg.toString();
    }

    private void appendEtichetta(StringBuilder msg, InventarioDocTRBulk bene) {
        if (bene.getEtichetta() != null && !bene.getEtichetta().isEmpty()) {
            msg.append(" e etichetta ").append(bene.getEtichetta());
        }
    }

    public void elaboraBeneCorrente(
            ActionContext context,
            boolean isEliminazione,
            boolean includiAccessori)
            throws BusinessProcessException {

        if (isEliminazione) {
            if (includiAccessori) {
                eliminaBeneCorrente(context);
            } else {
                eliminaBenePrincipaleSenzaAccessori(context);
            }
        } else {
            aggiungiBeneCorrente(context, includiAccessori);
        }
    }

    /**
     * Wrapper mantenuto per compatibilità con eventuali chiamate esterne.
     */
    public void elaboraBeneConAccessori(
            ActionContext context,
            boolean isEliminazione,
            boolean includiAccessori)
            throws BusinessProcessException {

        elaboraBeneCorrente(context, isEliminazione, includiAccessori);
    }

    private void aggiungiBeneCorrente(
            ActionContext context,
            boolean includiAccessori)
            throws BusinessProcessException {

        InventarioDocTRBulk benePrincipale = getBenePrincipaleCorrente(false);

        if (benePrincipale == null) {
            return;
        }

        try {
            aggiungiSingoloBene(context, benePrincipale);

            List<InventarioDocTRBulk> accessoriDaAggiungere =
                    calcolaAccessoriDaAggiungere(benePrincipale, includiAccessori);

            for (InventarioDocTRBulk accessorio : accessoriDaAggiungere) {
                aggiungiSingoloBene(context, accessorio);
            }

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void aggiungiSingoloBene(ActionContext context, InventarioDocTRBulk bene)
            throws ComponentException, RemoteException, BusinessProcessException {

        if (beneNelDettaglio(bene)) {
            return;
        }

        OggettoBulk[] singolo = new OggettoBulk[]{bene};
        BitSet oldSelection = new BitSet(1);
        BitSet newSelection = new BitSet(1);
        newSelection.set(0);

        modificaBeniConAccessoriComponente(
                context,
                singolo,
                oldSelection,
                newSelection
        );
    }

    private List<InventarioDocTRBulk> calcolaAccessoriDaAggiungere(
            InventarioDocTRBulk benePrincipale,
            boolean includiAccessori) {

        List<InventarioDocTRBulk> result = new ArrayList<>();

        PendingSelection pending = getPendingAdd();

        if (pending == null) {
            return result;
        }

        if (includiAccessori) {
            List<InventarioDocTRBulk> accessori =
                    pending.getPrincipaliConAccessori().get(benePrincipale);

            aggiungiSeNonPresente(result, accessori);
            return result;
        }

        List<InventarioDocTRBulk> accessoriStrutturali =
                pending.getPrincipaliConAccessori().get(benePrincipale);

        for (InventarioDocTRBulk accessorioManuale : pending.getAccessori()) {
            if (isAccessorioDelPrincipale(accessorioManuale, benePrincipale)
                    && !contieneBene(accessoriStrutturali, accessorioManuale)) {
                aggiungiSeNonPresente(result, accessorioManuale);
            }
        }

        return result;
    }

    private void aggiungiSeNonPresente(
            List<InventarioDocTRBulk> destinazione,
            List<InventarioDocTRBulk> sorgente) {

        if (sorgente == null) {
            return;
        }

        for (InventarioDocTRBulk bene : sorgente) {
            aggiungiSeNonPresente(destinazione, bene);
        }
    }

    private void aggiungiSeNonPresente(
            List<InventarioDocTRBulk> destinazione,
            InventarioDocTRBulk bene) {

        if (bene == null || beneNelDettaglio(bene) || contieneBene(destinazione, bene)) {
            return;
        }

        destinazione.add(bene);
    }

    private boolean isAccessorioDelPrincipale(
            InventarioDocTRBulk accessorio,
            InventarioDocTRBulk principale) {

        return accessorio != null
                && principale != null
                && Objects.equals(accessorio.getNr_inventario(), principale.getNr_inventario())
                && !Objects.equals(accessorio.getProgressivo(), principale.getProgressivo());
    }

    private boolean contieneBene(
            List<InventarioDocTRBulk> lista,
            InventarioDocTRBulk candidato) {

        if (lista == null || candidato == null) {
            return false;
        }

        for (InventarioDocTRBulk presente : lista) {
            if (presente != null && presente.equalsByPrimaryKey(candidato)) {
                return true;
            }
        }

        return false;
    }

    public boolean passaAlProssimoBene(boolean isEliminazione) {
        if (isEliminazione) {
            indexBeneCurrentePerEliminazione++;
            return indexBeneCurrentePerEliminazione < getTotaleBeniPrincipali(true);
        }

        indexBeneCurrentePerAggiunta++;
        return indexBeneCurrentePerAggiunta < getTotaleBeniPrincipali(false);
    }

    public void resetOperazione(boolean isEliminazione) {
        if (isEliminazione) {
            indexBeneCurrentePerEliminazione = 0;

            if (pendingDelete != null) {
                pendingDelete.clear();
            }

            pendingDelete = null;
            return;
        }

        indexBeneCurrentePerAggiunta = 0;

        if (pendingAdd != null) {
            pendingAdd.clear();
        }

        pendingAdd = null;
    }

    public int getTotaleBeniPrincipali(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        return ps == null ? 0 : ps.getPrincipaliConAccessori().size();
    }

    public int getIndexBeneCorrente(boolean isEliminazione) {
        return (isEliminazione
                ? indexBeneCurrentePerEliminazione
                : indexBeneCurrentePerAggiunta) + 1;
    }

    public boolean hasBeniPrincipaliConAccessoriPerEliminazione() {
        return pendingDelete != null
                && !pendingDelete.getPrincipaliConAccessori().isEmpty();
    }

    public boolean hasBeniPrincipaliConAccessoriPendenti() {
        return pendingAdd != null
                && !pendingAdd.getPrincipaliConAccessori().isEmpty();
    }

    public boolean hasBeniAccessoriPendenti() {
        return pendingAdd != null
                && !pendingAdd.getAccessori().isEmpty();
    }

    public int getNumeroBeniPrincipaliConAccessori() {
        return pendingAdd == null ? 0 : pendingAdd.getPrincipaliConAccessori().size();
    }

    public int getNumeroBeniAccessoriTotaliPendenti() {
        if (pendingAdd == null) {
            return 0;
        }

        return pendingAdd.getPrincipaliConAccessori()
                .values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }

    public int getNumeroBeniSemplici() {
        if (pendingAdd == null) {
            return 0;
        }

        return pendingAdd.getPrincipaliSenza().size()
                + pendingAdd.getAccessori().size();
    }

    public int getNumeroBeniAccessoriPendenti() {
        return pendingAdd == null ? 0 : pendingAdd.getAccessori().size();
    }

    public PendingSelection getPendingAdd() {
        return pendingAdd;
    }

    public void setPendingAdd(PendingSelection pendingAdd) {
        this.pendingAdd = pendingAdd;
    }

    public PendingSelection getPendingDelete() {
        return pendingDelete;
    }

    public void setPendingDelete(PendingSelection pendingDelete) {
        this.pendingDelete = pendingDelete;
    }

    public int getIndexBeneCurrentePerEliminazione() {
        return indexBeneCurrentePerEliminazione;
    }

    public int getIndexBeneCurrentePerAggiunta() {
        return indexBeneCurrentePerAggiunta;
    }

    public void setIndexBeneCurrentePerEliminazione(int value) {
        indexBeneCurrentePerEliminazione = value;
    }

    public void setIndexBeneCurrentePerAggiunta(int value) {
        indexBeneCurrentePerAggiunta = value;
    }

    public boolean isUltimaOperazioneEliminazione() {
        return ultimaOperazioneEliminazione;
    }

    public void setUltimaOperazioneEliminazione(boolean value) {
        ultimaOperazioneEliminazione = value;
    }

    public void stampaDocTrasportoRientro(ActionContext context)
            throws Exception {

        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new BusinessProcessException("Documento non valido");
        }

        try (InputStream input = getStreamDocTrasportoRientro(
                context.getUserContext(),
                doc)) {

            if (input == null) {
                return;
            }

            HttpServletResponse response =
                    ((HttpActionContext) context).getResponse();

            response.setContentType("application/pdf");
            response.setHeader(
                    "Content-Disposition",
                    "inline; filename=\"doc_trasporto_rientro_"
                            + doc.getPgDocTrasportoRientro()
                            + ".pdf\""
            );
            response.setDateHeader("Expires", 0);

            OutputStream output = response.getOutputStream();
            byte[] buffer = new byte[response.getBufferSize()];
            int length;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
        }
    }

    private InputStream getStreamDocTrasportoRientro(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc)
            throws Exception {

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

    public void create(ActionContext context)
            throws BusinessProcessException {

        try {
            getModel().setToBeCreated();

            Doc_trasporto_rientroBulk docCreato =
                    (Doc_trasporto_rientroBulk) createComponentSession()
                            .creaConBulk(
                                    context.getUserContext(),
                                    getModel()
                            );

            setModel(context, docCreato);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public DocTrasportoRientroComponentSession getComp()
            throws BusinessProcessException {

        return (DocTrasportoRientroComponentSession) createComponentSession(
                COMPONENT_SESSION,
                DocTrasportoRientroComponentSession.class
        );
    }

    public CompoundFindClause getClauses() {
        return clauses;
    }

    public void setClauses(CompoundFindClause clauses) {
        this.clauses = clauses;
    }

    public RemoteDetailCRUDController getDettBeniController() {
        return dettBeniController;
    }

    public RemoteDetailCRUDController getEditDettController() {
        return editDettController;
    }

    @Override
    protected void completeAllegato(T allegato, StorageObject storageObject)
            throws ApplicationException {

        Optional.ofNullable(storageObject.<String>getPropertyValue(
                        "sigla_commons_aspect:utente_applicativo"))
                .ifPresent(allegato::setUtenteSIGLA);

        List<String> secondaryTypes =
                storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");

        String aspect = estraiAspectAllegato(allegato, secondaryTypes);

        if (aspect != null) {
            allegato.setAspectName(aspect);
        }

        super.completeAllegato(allegato, storageObject);
    }

    private String estraiAspectAllegato(
            T allegato,
            List<String> secondaryTypes) {

        if (secondaryTypes == null || secondaryTypes.isEmpty()) {
            return null;
        }

        String firmato = null;
        String altro = null;

        if (allegato instanceof AllegatoDocumentoTrasportoBulk) {
            firmato = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
            altro = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO;
        } else if (allegato instanceof AllegatoDocumentoRientroBulk) {
            firmato = AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
            altro = AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO;
        }

        if (firmato == null || altro == null) {
            return null;
        }

        for (String secondaryType : secondaryTypes) {
            if (firmato.equals(secondaryType) || altro.equals(secondaryType)) {
                return secondaryType;
            }
        }

        return null;
    }

    public void validaDatiPerDettagli()
            throws ApplicationException {

        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return;
        }

        if (doc.getTipoMovimento() == null) {
            throw new ApplicationException(
                    "Selezionare il Tipo di Movimento prima di accedere al tab Dettagli."
            );
        }

        if (doc.isSmartworking()) {
            validaDatiSmartworking(doc);
            return;
        }

        validaDatiRitiro(doc);
    }

    private void validaDatiSmartworking(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        if (doc.getTerzoSmartworking() == null
                || doc.getTerzoSmartworking().getCd_terzo() == null) {
            throw new ApplicationException(
                    "Per il tipo di movimento Smartworking è necessario selezionare l'Assegnatario Smartworking."
            );
        }
    }

    private void validaDatiRitiro(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        if (!doc.hasTipoRitiroSelezionato()) {
            throw new ApplicationException(
                    "Selezionare il Tipo Ritiro (Incaricato o Vettore)."
            );
        }

        if (doc.isRitiroIncaricato()
                && (doc.getTerzoIncRitiro() == null
                || doc.getTerzoIncRitiro().getCd_terzo() == null)) {
            throw new ApplicationException("Selezionare il Dipendente Incaricato.");
        }

        if (doc.isRitiroVettore() && isNullOrEmpty(doc.getNominativoVettore())) {
            throw new ApplicationException("Specificare il Nominativo del Vettore.");
        }

        if (doc.isRitiroIncaricato() || doc.isRitiroVettore()) {
            if (isNullOrEmpty(doc.getIndirizzo())) {
                throw new ApplicationException("Specificare l'Indirizzo.");
            }

            if (isNullOrEmpty(doc.getDestinazione())) {
                throw new ApplicationException("Specificare la Destinazione.");
            }
        }
    }

    protected void eliminaTuttiBeniDaDettagli(ActionContext context)
            throws BusinessProcessException {

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            getComp().eliminaTuttiDettagliSalvati(
                    context.getUserContext(),
                    doc
            );

            if (doc.getDoc_trasporto_rientro_dettColl() != null) {
                doc.getDoc_trasporto_rientro_dettColl().clear();
            }

            setModel(context, doc);

        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    public void eliminaBeniDaDettagli(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            getComp().eliminaDettagliSalvati(
                    context.getUserContext(),
                    doc,
                    details
            );

            rimuoviBeniDaCollection(doc, details);
            setModel(context, doc);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaBeneCorrente(ActionContext context)
            throws BusinessProcessException {

        InventarioDocTRBulk bene = getBenePrincipaleCorrente(true);

        if (bene == null) {
            return;
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();
            List<InventarioDocTRBulk> accessori = getAccessoriCorrente(true);

            getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(
                    context.getUserContext(),
                    doc,
                    bene,
                    accessori
            );

            rimuoviBeneDaCollection(doc, bene);

            for (InventarioDocTRBulk accessorio : accessori) {
                rimuoviBeneDaCollection(doc, accessorio);
            }

            setModel(context, doc);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context)
            throws BusinessProcessException {

        InventarioDocTRBulk bene = getBenePrincipaleCorrente(true);

        if (bene == null) {
            return;
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();
            OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{bene};

            getComp().eliminaDettagliSalvati(
                    context.getUserContext(),
                    doc,
                    soloIlPrincipale
            );

            rimuoviBeneDaCollection(doc, bene);
            setModel(context, doc);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void rimuoviBeniDaCollection(
            Doc_trasporto_rientroBulk doc,
            OggettoBulk[] beni) {

        if (beni == null) {
            return;
        }

        for (OggettoBulk bene : beni) {
            if (bene instanceof InventarioDocTRBulk) {
                rimuoviBeneDaCollection(doc, (InventarioDocTRBulk) bene);
            }
        }
    }

    private void rimuoviBeneDaCollection(
            Doc_trasporto_rientroBulk doc,
            InventarioDocTRBulk bene) {

        if (doc == null || doc.getDoc_trasporto_rientro_dettColl() == null) {
            return;
        }

        doc.getDoc_trasporto_rientro_dettColl().removeIf(obj ->
                obj instanceof Doc_trasporto_rientro_dettBulk
                        && stessoBene((Doc_trasporto_rientro_dettBulk) obj, bene)
        );
    }

    private boolean beneNelDettaglio(InventarioDocTRBulk bene) {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getDoc_trasporto_rientro_dettColl() == null) {
            return false;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            if (obj instanceof Doc_trasporto_rientro_dettBulk
                    && stessoBene((Doc_trasporto_rientro_dettBulk) obj, bene)) {
                return true;
            }
        }

        return false;
    }

    private boolean stessoBene(
            Doc_trasporto_rientro_dettBulk dettaglio,
            InventarioDocTRBulk bene) {

        if (dettaglio == null || bene == null || bene.getProgressivo() == null) {
            return false;
        }

        return Objects.equals(dettaglio.getNr_inventario(), bene.getNr_inventario())
                && Objects.equals(
                dettaglio.getProgressivo(),
                bene.getProgressivo().intValue()
        );
    }

    private void assegnaFileUploadato(
            ActionContext context,
            Doc_trasporto_rientroBulk doc) {

        if (!(context instanceof HttpActionContext)
                || doc == null
                || doc.getArchivioAllegati() == null) {
            return;
        }

        it.cnr.jada.util.upload.UploadedFile uploadedFile =
                ((HttpActionContext) context)
                        .getMultipartParameter("main.ArchivioAllegati.file");

        if (uploadedFile == null
                || uploadedFile.getName() == null
                || uploadedFile.getName().isEmpty()) {
            return;
        }

        doc.getArchivioAllegati()
                .stream()
                .filter(a -> a.isToBeCreated() && a.getFile() == null)
                .findFirst()
                .ifPresent(a -> {
                    a.setFile(uploadedFile.getFile());
                    a.setContentType(uploadedFile.getContentType());
                    a.setNome(a.parseFilename(uploadedFile.getName()));
                });
    }

    private void validaAllegati(Doc_trasporto_rientroBulk doc)
            throws ValidationException {

        if (doc.getArchivioAllegati() == null) {
            return;
        }

        for (AllegatoGenericoBulk allegato : doc.getArchivioAllegati()) {
            allegato.validate();
        }
    }

    @Override
    public void update(ActionContext context)
            throws BusinessProcessException {

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            validaUpdate(doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            assegnaFileUploadato(context, doc);
            validaAllegati(doc);

            doc.setToBeUpdated();

            Doc_trasporto_rientroBulk docAggiornato =
                    (Doc_trasporto_rientroBulk) createComponentSession()
                            .modificaConBulk(
                                    context.getUserContext(),
                                    doc
                            );

            setModel(context, docAggiornato);
            setSkipAllegatiReload(false);

            getComp().archiviaAllegatiDocTR(
                    context.getUserContext(),
                    docAggiornato
            );

            commitUserTransaction();

            setMessage("Documento salvato correttamente.");

        } catch (ApplicationException e) {
            rollbackUserTransaction();
            throw new BusinessProcessException(e);

        } catch (Exception e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void validaUpdate(Doc_trasporto_rientroBulk doc)
            throws BusinessProcessException {

        if (doc == null) {
            throw new BusinessProcessException("Modello non disponibile.");
        }

        if (doc.isAnnullato() || doc.isDefinitivo()) {
            throw new BusinessProcessException("Documento non modificabile.");
        }

        if (doc.isInAttesaDiFirma()) {
            throw new BusinessProcessException(
                    "Documento in attesa di firma HappySign: allegati non modificabili."
            );
        }

        if (doc.isFirmatoDaCompletare() && !isAllegatiModificabili()) {
            throw new BusinessProcessException("Allegati non modificabili.");
        }
    }

    public void salvaDefinitivo(ActionContext context)
            throws BusinessProcessException {

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            validaSalvaDefinitivo(doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            assegnaFileUploadato(context, doc);
            validaAllegati(doc);

            getComp().validaAggiuntaAllegatoFirmato(doc);

            doc.setToBeUpdated();

            doc = getComp().salvaDefinitivo(context.getUserContext(), doc);

            setModel(context, doc);
            setSkipAllegatiReload(false);

            getComp().archiviaAllegatiDocTR(
                    context.getUserContext(),
                    doc
            );

            doc.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            doc.setCrudStatus(OggettoBulk.NORMAL);

            setStatus(VIEW);
            commitUserTransaction();

            setMessage("Documento salvato in stato DEFINITIVO.");

        } catch (ComponentException | RemoteException | ValidationException e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void validaSalvaDefinitivo(Doc_trasporto_rientroBulk doc)
            throws BusinessProcessException {

        if (doc == null) {
            throw new BusinessProcessException("Modello non disponibile.");
        }

        if (doc.isAnnullato()) {
            throw new BusinessProcessException(
                    "Documento annullato: impossibile salvare definitivo."
            );
        }

        if (doc.isDefinitivo()) {
            throw new BusinessProcessException("Documento già definitivo.");
        }

        if (doc.getDataFirma() == null) {
            throw new BusinessProcessException(
                    "Documento non ancora firmato da HappySign: impossibile salvare definitivo."
            );
        }

        if (doc.isInAttesaDiFirma()) {
            throw new BusinessProcessException(
                    "Documento ancora in attesa di firma HappySign."
            );
        }
    }

    @Override
    public boolean isEditable() {
        if (isVisualizzazione()) {
            return false;
        }

        if (isDocumentoFirmatoDaCompletare()) {
            return true;
        }

        if (isDocumentoNonModificabile()) {
            return false;
        }

        return super.isEditable();
    }

    /**
     * Restituisce true se i campi di input devono essere in sola lettura.
     *
     * OVERRIDE: quando il documento è "firmato da completare" gli allegati
     * sono modificabili, quindi il form allegati NON deve essere in sola lettura,
     * anche se testata e dettagli lo sono.
     *
     * In tutti gli altri casi bloccati delega a isTestataEDettagliBloccati().
     */
    @Override
    public boolean isInputReadonly() {
        if (isAllegatiModificabili()) {
            return false;
        }
        return isTestataEDettagliBloccati();
    }

    public boolean isAnagraficiReadonly() {
        return isTestataEDettagliBloccati()
                || (!isInserting() && !isEditing());
    }

    @Override
    public boolean isSaveButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || isVisualizzazione()) {
            return false;
        }

        if (doc.isDefinitivo()
                || doc.isAnnullato()
                || doc.isInAttesaDiFirma()
                || doc.isInviatoInFirma()) {
            return false;
        }

        return !isDocumentoNonModificabile() || isAllegatiModificabili();
    }

    @Override
    public boolean isSaveButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return false;
        }

        if (doc.isDefinitivo()
                || doc.isAnnullato()
                || doc.isInAttesaDiFirma()
                || doc.isInviatoInFirma()) {
            return true;
        }

        if (isAllegatiModificabili()) {
            return false;
        }

        return doc.getCrudStatus() == OggettoBulk.TO_BE_CREATED;
    }

    public boolean isSalvaDefinitivoButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return true;
        }

        if (doc.isDefinitivo() || doc.isAnnullato()) {
            return true;
        }

        return !doc.isFirmatoDaCompletare();
    }

    public boolean isSalvaDefinitivoButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || isVisualizzazione()) {
            return false;
        }

        if (doc.isAnnullato() || doc.isDefinitivo()) {
            return false;
        }

        return doc.isFirmatoDaCompletare()
                && doc.hasDettagli()
                && hasAllegatoFirmato();
    }

    public boolean isAllegatiAccessibili() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return false;
        }

        if (doc.isInAttesaDiFirma()) {
            return false;
        }

        return doc.isFirmatoDaCompletare()
                || doc.isDefinitivo()
                || doc.isAnnullato();
    }

    public boolean isAllegatiModificabili() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || isVisualizzazione()) {
            return false;
        }

        if (doc.isAnnullato() || doc.isDefinitivo()) {
            return false;
        }

        return doc.isFirmatoDaCompletare();
    }

    @Override
    protected boolean isChildGrowable(boolean isGrowable) {
        return isAllegatiModificabili();
    }

}