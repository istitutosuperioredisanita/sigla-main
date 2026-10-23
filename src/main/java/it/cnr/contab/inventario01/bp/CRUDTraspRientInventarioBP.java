package it.cnr.contab.inventario01.bp;

import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.inventario01.util.DocTraspRientUtil;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

/**
 * BP comune per i documenti di Trasporto/Rientro: testata, dettagli, selezione beni/accessori,
 * allegati, firma, salvataggio definitivo e stampa.
 * Dopo la firma testata e dettagli restano bloccati; sono modificabili solo gli allegati.
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

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private static final int MAX_BENI_SELEZIONABILI = 100;

    /** Margine tra DATA_INVIO_FIRMA e la data degli allegati nel documentale (orologi diversi). */
    private static final long MARGINE_ALLEGATI_PREDISPOSIZIONE_MS = 5000L;

    private String tipo;
    private boolean isAmministratore;
    private boolean isGestioneInvioInFirmaAttiva;
    private boolean skipAllegatiReload;
    /** Firma manuale: gli allegati vanno nel documentale solo con Predisponi in firma e Salva definitivo. */
    private boolean archiviaAllegatiNelDocumentale;
    private boolean ultimoSalvataggioArchiviato = true;

    private PendingSelection pendingAdd;
    private PendingSelection pendingDelete;

    private int indexBeneCurrentePerEliminazione;
    private int indexBeneCurrentePerAggiunta;
    private boolean ultimaOperazioneEliminazione;
    private CompoundFindClause clauses;

    /** Stato temporaneo della selezione dei beni con relativi accessori. */
    public static class PendingSelection {

        private final Map<InventarioDocTRBulk, List<InventarioDocTRBulk>> principaliConAccessori =
                new LinkedHashMap<>();
        private final List<InventarioDocTRBulk> accessori = new ArrayList<>();
        private final List<InventarioDocTRBulk> principaliSenza = new ArrayList<>();

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
    protected void init(Config config, ActionContext context) throws BusinessProcessException {

        setTipo(this instanceof CRUDTrasportoBeniInvBP ? TRASPORTO : RIENTRO);
        try {
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));

            Configurazione_cnrComponentSession confSession =
                    (Configurazione_cnrComponentSession) createComponentSession(
                            CONFIG_COMPONENT_SESSION,
                            Configurazione_cnrComponentSession.class);

            final boolean profiloIss = DocTraspRientUtil.isProfiloIssAttivo();
            final boolean configAttiva = profiloIss
                    && Boolean.TRUE.equals(confSession.isGestioneInvioInFirmaDocTRAttivo(context.getUserContext()));
            setGestioneInvioInFirmaAttiva(configAttiva);
            org.slf4j.LoggerFactory.getLogger(CRUDTraspRientInventarioBP.class).info(
                    "Invio in firma T/R: profilo Spring iss={}, gestione invio attiva={} (false = firma manuale)",
                    profiloIss, configAttiva);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }

        super.init(config, context);
        resetTabs();
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        bulk = super.initializeModelForInsert(context, bulk);
        ((Doc_trasporto_rientroBulk) bulk).setArchivioAllegati(new BulkList<>());
        return bulk;
    }

    @Override
    public void basicEdit(ActionContext context, OggettoBulk bulk, boolean doInitializeForEdit)
            throws BusinessProcessException {

        super.basicEdit(context, bulk, doInitializeForEdit);

        if (getStatus() != VIEW) {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
            if (doc != null) {
                if (doc.isDefinitivo()) {
                    setStatus(VIEW);
                    setMessage("Documento Definitivo. Non è possibile modificarlo.");
                } else if (doc.isInviatoInFirma() || doc.isInAttesaDiFirma()) {
                    setStatus(VIEW);
                    setMessage("Documento Inviato o In Attesa di Firma. Non è possibile modificarlo.");
                } else if (doc.isAnnullato()) {
                    setStatus(VIEW);
                    setMessage("Documento Annullato. Non è possibile modificarlo.");
                }
            }
        }
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        try {
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
            return super.initializeModelForEdit(context, bulk);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public OggettoBulk initializeModelForEditAllegati(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        if (isSkipAllegatiReload()) {
            return bulk;
        }
        OggettoBulk result = super.initializeModelForEditAllegati(context, bulk);
        nascondiAllegatiAntecedentiPredisposizione(result);
        return result;
    }

    /** Firma manuale, documento predisposto: mostra solo gli allegati aggiunti dopo la predisposizione. */
    private void nascondiAllegatiAntecedentiPredisposizione(OggettoBulk bulk) {
        if (!isFirmaManuale() || !(bulk instanceof Doc_trasporto_rientroBulk)) {
            return;
        }
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        if (!doc.isPredispostoAllaFirma() || doc.getArchivioAllegati() == null) {
            return;
        }
        long soglia = doc.getDataInvioFirma().getTime() + MARGINE_ALLEGATI_PREDISPOSIZIONE_MS;
        BulkList<AllegatoGenericoBulk> visibili = new BulkList<>();
        for (AllegatoGenericoBulk a : doc.getArchivioAllegati()) {
            if (a.getLastModificationDate() == null || a.getLastModificationDate().getTime() > soglia) {
                visibili.add(a);
            }
        }
        doc.setArchivioAllegati(visibili);
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
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
                setErrorMessage("Non è possibile cambiare tab: in questa fase è consentita solo la ricerca.");
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
            setErrorMessage(getMessaggioAllegatiNonAccessibili());
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
            if (obj instanceof Doc_trasporto_rientro_dettBulk
                    && ((Doc_trasporto_rientro_dettBulk) obj).getCrudStatus() == OggettoBulk.NORMAL) {
                return true;
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

    /** False dopo un Salva in bozza di firma manuale: gli allegati sono ancora solo in memoria. */
    public boolean isUltimoSalvataggioArchiviato() {
        return ultimoSalvataggioArchiviato;
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

    public boolean isDocumentoInAttesaDiFirma() {
        return getDoc() != null && getDoc().isInAttesaDiFirma();
    }

    /** Fase finale (firmato da workflow o predisposto manualmente): solo allegati modificabili. */
    public boolean isDocumentoFirmatoDaCompletare() {
        return getDoc() != null && getDoc().isFirmaCompletabile();
    }

    public boolean isDocumentoPredispostoAllaFirma() {
        return getDoc() != null && getDoc().isPredispostoAllaFirma();
    }

    /** Nessun workflow di firma: l'utente carica a mano il documento da firmare e poi quello firmato. */
    public boolean isFirmaManuale() {
        return !isGestioneInvioInFirmaAttiva();
    }

    public String getMessaggioAllegatiNonAccessibili() {
        return isFirmaManuale()
                ? "Il tab Allegati sarà disponibile dopo aver salvato il documento con almeno un bene."
                : "Il tab Allegati sarà disponibile solo dopo l'invio del documento in firma.";
    }

    /** Testata e dettagli bloccati (non riguarda gli allegati). */
    public boolean isTestataEDettagliBloccati() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null) {
            return false;
        }

        return isViewing()
                || doc.isAnnullato()
                || doc.isDefinitivo()
                || doc.isInAttesaDiFirma()
                || doc.isFirmaCompletabile()
                || doc.isInviatoInFirma();
    }

    /** Wrapper per JSP/properties. */
    public boolean isDocumentoNonModificabile() {
        return isTestataEDettagliBloccati();
    }

    /** Wrapper per JSP/properties. */
    public boolean isTestataODettagliNonModificabili() {
        return isTestataEDettagliBloccati();
    }

    public boolean isDettagliModificabili() {
        return !isTestataEDettagliBloccati() && (isEditing() || isInserting());
    }

    @Override
    protected Boolean isPossibileCancellazione(AllegatoGenericoBulk allegato) {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || isViewing()
                || doc.isDefinitivo() || doc.isAnnullato() || doc.isInAttesaDiFirma() || doc.isInviatoInFirma()) {
            return false;
        }
        // Firma manuale: eliminazione sempre abilitata fino al definitivo
        if (isFirmaManuale()) {
            return true;
        }
        // Workflow: il FIRMATO già archiviato non si elimina
        if (allegato != null
                && allegato.getCrudStatus() != OggettoBulk.TO_BE_CREATED
                && isAllegatoFirmato(allegato)) {
            return false;
        }
        return isAllegatiModificabili();
    }

    private boolean isAllegatoFirmato(AllegatoGenericoBulk allegato) {
        if (!(allegato instanceof AllegatoDocTraspRientroBulk)) return false;
        String aspectFirmato = DocTraspRientUtil.aspectFirmato(getDoc());
        return aspectFirmato != null
                && aspectFirmato.equals(((AllegatoDocTraspRientroBulk) allegato).getAspectName());
    }

    public boolean isTipoRitiroVisible() {
        return hasValidModel() && !isSmartworking();
    }

    public boolean isTerzoSmartworkingVisible() {
        return hasValidModel() && isSmartworking();
    }

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

        // Come doElimina: annullato, inviato in firma o firmato
        if (doc.isAnnullato() || doc.isInviatoInFirma() || doc.getDataFirma() != null) {
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
        return super.isDeleteButtonEnabled() && (isEditing() || isInserting()) && !isDeleteButtonHidden();
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
                DocumentoRientroDettBulk rientroDett =
                        ((DocumentoTrasportoDettBulk) obj).getDocRientroDettRif();

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
        return super.isNewButtonEnabled() && (isEditing() || isInserting());
    }

    public boolean isStampaDocButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return true;
        }
        return !getMainTabName().equals(getTab("tab"));
    }

    public boolean isStampaDocButtonEnabled() {
        return !isStampaDocButtonHidden() && (isEditing() || isInserting());
    }

    private boolean hasAllegatoFirmato() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        String aspectFirmatoAtteso = DocTraspRientUtil.aspectFirmato(doc);
        if (aspectFirmatoAtteso == null) {
            return false;
        }

        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) obj;

                if (aspectFirmatoAtteso.equals(allegato.getAspectName())
                        && (OggettoBulk.NORMAL == allegato.getCrudStatus() || isEditing())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void initializePrintBP(ActionContext context, AbstractPrintBP abstractPrintBP) {
        OfflineReportPrintBP printbp = (OfflineReportPrintBP) abstractPrintBP;
        printbp.setReportName(DocTraspRientUtil.REPORT_DOC_TRASPORTO_RIENTRO);

        Doc_trasporto_rientroBulk doc = Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("Modello vuoto!"));

        addPrintParam(printbp, "esercizio", doc.getEsercizio(), Integer.class);
        addPrintParam(printbp, "pg_inventario", doc.getPgInventario(), Long.class);
        addPrintParam(printbp, "ti_documento", doc.getTiDocumento(), String.class);
        addPrintParam(printbp, "pg_doc_trasporto_rientro", doc.getPgDocTrasportoRientro(), Long.class);
    }

    private void addPrintParam(OfflineReportPrintBP printbp, String nome, Object valore, Class<?> type) {
        Print_spooler_paramBulk param = new Print_spooler_paramBulk();
        param.setNomeParam(nome);
        param.setValoreParam(valore == null ? null : valore.toString());
        param.setParamType(type.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    @Override
    protected Button[] createToolbar() {
        Properties props = it.cnr.jada.util.Config.getHandler().getProperties(getClass());

        return Stream.concat(
                Arrays.stream(super.createToolbar()),
                Stream.of(
                        new Button(props, "CRUDToolbar.inviaInFirma"),
                        new Button(props, "CRUDToolbar.predisponiFirma"),
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

    /** Visibile finché il documento non è predisposto alla firma. */
    public boolean isPredisponiFirmaButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        return !isFirmaManuale()
                || doc == null
                || doc.isTemporaneo()
                || !doc.isInserito()
                || doc.isPredispostoAllaFirma()
                || !hasBeniSalvati(doc);
    }

    /** Cliccabile appena tra gli allegati (anche non salvati) c'è un "Documento di Trasporto/Rientro". */
    public boolean isPredisponiFirmaButtonEnabled() {
        return !isPredisponiFirmaButtonHidden()
                && (isEditing() || isInserting())
                && countDocumentiDaFirmare() > 0;
    }

    private int countDocumentiDaFirmare() {
        Doc_trasporto_rientroBulk doc = getDoc();
        int count = 0;
        if (doc == null || doc.getArchivioAllegati() == null) {
            return count;
        }
        for (AllegatoGenericoBulk a : doc.getArchivioAllegati()) {
            if (a instanceof AllegatoDocTraspRientroBulk
                    && a.getCrudStatus() != OggettoBulk.TO_BE_DELETED) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) a;
                if (allegato.isAspectFirmato() || allegato.isAspectDaFirmare()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isInviaInFirmaButtonEnabled() {
        return !isInviaInFirmaButtonHidden() && (isEditing() || isInserting());
    }

    protected final RemoteDetailCRUDController dettBeniController =
            new RemoteDetailCRUDController("DettBeniController", InventarioDocTRBulk.class, "",
                    COMPONENT_SESSION, this) {

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
                        throw new ValidationException("Impossibile modificare i beni del documento in questo stato.");
                    }

                    eliminaTuttiBeniDaDettagli(context);
                    reset(context);
                }

                @Override
                public void removeDetails(ActionContext context, OggettoBulk[] details)
                        throws BusinessProcessException {

                    if (isTestataEDettagliBloccati()) {
                        throw new BusinessProcessException(new ApplicationException("Impossibile modificare i beni del documento in questo stato."));
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
            new RemoteDetailCRUDController("editDettController", getDocumentoClassDett(), "",
                    COMPONENT_SESSION, this) {

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

            List<InventarioDocTRBulk> accessori = getComp().cercaBeniAccessoriNeiDettagliSalvati(
                    context.getUserContext(), getDoc(), bene);

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
                    context.getUserContext(), getDoc(), getDocumentoClassDett(), filters);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    public RemoteIterator selectDettaglibyClause(ActionContext context)
            throws BusinessProcessException {
        try {
            return getComp().selectBeniAssociatiByClause(
                    context.getUserContext(), getDoc(), InventarioDocTRBulk.class);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /** Beni candidati all'inserimento. */
    public RemoteIterator selectBeniCandidatiByClause(ActionContext context, CompoundFindClause clauses)
            throws BusinessProcessException {
        try {
            return getComp().selectBeniPerInserimentoByClause(context.getUserContext(), getDoc(), clauses);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /** Columnset dei beni per la griglia del selezionatore. */
    public String getColumnSetSelezionatore() {
        return TRASPORTO.equals(getTipo()) ? "righeTrasporto" : "righeRientro";
    }

    public void inviaAllaFirma(ActionContext context) throws BusinessProcessException {
        try {
            fillModel(context);

            Doc_trasporto_rientroBulk doc = getDoc();
            if (doc == null) {
                throw new ApplicationException("Documento non presente.");
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
                    getComp().inviaDocumentoAllaFirma(context.getUserContext(), doc);

            // Ricarica il _DAFIRMARE dallo storage (lista svuotata per evitare duplicati)
            docFirmabile.setArchivioAllegati(new BulkList<>());
            docFirmabile = (Doc_trasporto_rientroBulk) initializeModelForEditAllegati(context, docFirmabile);

            setModel(context, docFirmabile);
            setStatus(VIEW);
            commitUserTransaction();

            setMessage("Documento inviato alla firma con successo. UUID: "
                    + docFirmabile.getUuidFlussoAutorizzativo());

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

    /**
     * Firma manuale: archivia tutti gli allegati e marca il documento come predisposto
     * (stato INS, flusso null, DATA_INVIO_FIRMA = now). Gli allegati spariscono dalla lista ma restano nel documentale.
     */
    public void predisponiAllaFirma(ActionContext context) throws BusinessProcessException, ApplicationException {
        Doc_trasporto_rientroBulk doc = getDoc();

        validaPredisposizione(doc);
        validaTipologieAllegatiFirmaManuale(doc);
        validaAllegatoDaFirmare();

        Timestamp dataInvioPrec = doc.getDataInvioFirma();
        Timestamp dataFirmaPrec = doc.getDataFirma();

        // 1) archivia tutti gli allegati (DATA_INVIO_FIRMA ancora null)
        archiviaAllegatiNelDocumentale = true;
        try {
            update(context);
        } finally {
            archiviaAllegatiNelDocumentale = false;
        }

        // 2) predisposizione: DATA_INVIO_FIRMA successiva agli allegati archiviati
        doc = getDoc();
        doc.inizializzaPerPredisposizioneFirma();
        try {
            update(context);
        } catch (BusinessProcessException e) {
            ripristinaDateFirma(dataInvioPrec, dataFirmaPrec);
            throw e;
        }

        // 3) gli allegati spariscono dalla lista, nel documentale restano
        getDoc().setArchivioAllegati(new BulkList<>());

        setMessage("Documento predisposto alla firma. Allegare il Documento firmato e premere Salva Definitivo.");
    }

    private void validaPredisposizione(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (doc == null) {
            throw new ApplicationException("Modello non disponibile.");
        }
        if (!isFirmaManuale()) {
            throw new ApplicationException("La predisposizione alla firma è prevista solo senza invio in firma automatico.");
        }
        if (doc.isTemporaneo()) {
            throw new ApplicationException("Salvare il documento prima di predisporlo alla firma.");
        }
        if (!doc.isInserito()) {
            throw new ApplicationException("Il documento deve essere in stato INSERITO per essere predisposto alla firma.");
        }
        if (doc.isPredispostoAllaFirma()) {
            throw new ApplicationException("Il documento è già predisposto alla firma.");
        }
        if (!hasBeniSalvati(doc)) {
            throw new ApplicationException("Il documento deve contenere almeno un bene salvato.");
        }
    }

    /** Deve esserci esattamente un allegato "Documento di T/R" da firmare. */
    private void validaAllegatoDaFirmare() throws ApplicationException {
        int count = countDocumentiDaFirmare();
        String tipoDoc = DocTraspRientUtil.descrizioneTipo(getTipo());
        if (count == 0) {
            throw new ApplicationException(
                    "Allegare il Documento di " + tipoDoc + " da firmare prima di predisporre il documento alla firma.");
        }
        if (count > 1) {
            throw new ApplicationException("È consentito allegare un solo Documento di " + tipoDoc + ".");
        }
    }

    private void validaStatoPerFirma() throws ApplicationException {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            throw new ApplicationException("Documento non presente");
        }
        if (doc.isAnnullato()) {
            throw new ApplicationException("Impossibile inviare in firma un documento annullato");
        }
        if (!doc.isInserito()) {
            throw new ApplicationException("Il documento deve essere in stato 'Inserito' per essere inviato in firma");
        }
        if (!doc.isInviabileAllaFirma()) {
            throw new ApplicationException("Documento non completo per l'invio in firma");
        }
    }

    public void annullaDoc(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = getComp().annullaDocumento(context.getUserContext(), getDoc());

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
    public void clearSelection(ActionContext context) throws BusinessProcessException {
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
    public BitSet getSelection(ActionContext context, OggettoBulk[] bulks, BitSet currentSelection)
            throws BusinessProcessException {
        return currentSelection;
    }

    @Override
    public void initializeSelection(ActionContext context) throws BusinessProcessException {
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
    public void selectAll(ActionContext context) throws BusinessProcessException {
        if (isTestataEDettagliBloccati()) {
            throw new BusinessProcessException(new ApplicationException("Impossibile modificare il documento"));
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();
            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

            List<InventarioDocTRBulk> beniFiltrati = getComp().caricaBeniPerInserimento(
                    context.getUserContext(), doc, getClauses(), isTrasporto);

            validaMassimoBeni(beniFiltrati);

            getComp().validaBeniNonInAltriDocumenti(context.getUserContext(), doc, beniFiltrati);
            getComp().selezionaTuttiBeni(context.getUserContext(), doc, getClauses());

            setClauses(null);
            getDettBeniController().resync(context);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public BitSet setSelection(ActionContext context, OggettoBulk[] bulks,
                               BitSet oldSelection, BitSet newSelection)
            throws BusinessProcessException {

        if (isTestataEDettagliBloccati()) {
            throw new BusinessProcessException(new ApplicationException("Impossibile modificare i beni del documento in questo stato."));
        }

        try {
            fillModel(context);

            PendingSelection ps = creaPendingAdd(context, bulks, oldSelection, newSelection);
            List<InventarioDocTRBulk> beniDaValidare = raccogliBeniDaValidare(ps);

            validaMassimoBeni(beniDaValidare);

            if (!beniDaValidare.isEmpty()) {
                getComp().validaBeniNonInAltriDocumenti(context.getUserContext(), getDoc(), beniDaValidare);
            }

            if (ps.getPrincipaliConAccessori().isEmpty()) {
                if (!ps.isEmpty()) {
                    modificaBeniConAccessoriComponente(context, bulks, oldSelection, newSelection);
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

    private PendingSelection creaPendingAdd(ActionContext context, OggettoBulk[] bulks,
                                            BitSet oldSelection, BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {

        PendingSelection ps = new PendingSelection();
        ps.bulks = bulks;
        ps.oldSel = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();
        ps.newSel = newSelection != null ? (BitSet) newSelection.clone() : new BitSet();
        ps.selectionAccumulata = oldSelection != null ? (BitSet) oldSelection.clone() : new BitSet();

        if (oldSelection == null || newSelection == null) {
            return ps;
        }

        for (int i = 0; i < bulks.length; i++) {
            if (oldSelection.get(i) == newSelection.get(i) || !newSelection.get(i)) {
                continue;
            }

            InventarioDocTRBulk bene = (InventarioDocTRBulk) bulks[i];

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

    private List<InventarioDocTRBulk> cercaAccessoriPerBene(ActionContext context, InventarioDocTRBulk bene)
            throws ComponentException, RemoteException, BusinessProcessException {

        if (getDoc() instanceof DocumentoRientroBulk) {
            return getComp().cercaBeniAccessoriPresentinelTrasportoOriginale(
                    context.getUserContext(), bene, getDoc());
        }
        return getComp().cercaBeniAccessoriAssociati(context.getUserContext(), bene);
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

    private void validaMassimoBeni(List<InventarioDocTRBulk> beni) throws ApplicationException {
        if (beni != null && beni.size() > MAX_BENI_SELEZIONABILI) {
            throw new ApplicationException(
                    "Impossibile selezionare più di " + MAX_BENI_SELEZIONABILI + " beni contemporaneamente");
        }
    }

    public InventarioDocTRBulk getBenePrincipaleCorrente(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        int index = isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta;

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

        List<InventarioDocTRBulk> accessori = ps.getPrincipaliConAccessori().get(bene);
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
            msg.append("Il bene principale con codice: ").append(bene.getNr_inventario());
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
            msg.append("La selezione include il bene principale con codice: ").append(bene.getNr_inventario());
            appendEtichetta(msg, bene);

            msg.append(" che ha ")
                    .append(numAccessori)
                    .append(numAccessori == 1 ? " bene accessorio associato" : " beni accessori associati")
                    .append(".\n\n");

            String articolo = numAccessori == 1 ? "questo accessorio" : "questi accessori";
            String azione = TRASPORTO.equals(getTipo()) ? "nell'aggiunta" : "nel rientro";

            msg.append("Vuoi includere anche ").append(articolo).append(" ").append(azione).append("?");
        }

        return msg.toString();
    }

    private void appendEtichetta(StringBuilder msg, InventarioDocTRBulk bene) {
        if (bene.getEtichetta() != null && !bene.getEtichetta().isEmpty()) {
            msg.append(" e etichetta ").append(bene.getEtichetta());
        }
    }

    public void elaboraBeneCorrente(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {

        if (!isEliminazione) {
            aggiungiBeneCorrente(context, includiAccessori);
        } else if (includiAccessori) {
            eliminaBeneCorrente(context);
        } else {
            eliminaBenePrincipaleSenzaAccessori(context);
        }
    }

    /** Wrapper per chiamate esterne. */
    public void elaboraBeneConAccessori(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {
        elaboraBeneCorrente(context, isEliminazione, includiAccessori);
    }

    private void aggiungiBeneCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        InventarioDocTRBulk benePrincipale = getBenePrincipaleCorrente(false);
        if (benePrincipale == null) {
            return;
        }

        try {
            aggiungiSingoloBene(context, benePrincipale);

            for (InventarioDocTRBulk accessorio : calcolaAccessoriDaAggiungere(benePrincipale, includiAccessori)) {
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

        BitSet selezione = new BitSet(1);
        selezione.set(0);
        modificaBeniConAccessoriComponente(context, new OggettoBulk[]{bene}, new BitSet(1), selezione);
    }

    private List<InventarioDocTRBulk> calcolaAccessoriDaAggiungere(
            InventarioDocTRBulk benePrincipale, boolean includiAccessori) {

        List<InventarioDocTRBulk> result = new ArrayList<>();
        PendingSelection pending = getPendingAdd();

        if (pending == null) {
            return result;
        }

        if (includiAccessori) {
            aggiungiSeNonPresente(result, pending.getPrincipaliConAccessori().get(benePrincipale));
            return result;
        }

        List<InventarioDocTRBulk> accessoriStrutturali = pending.getPrincipaliConAccessori().get(benePrincipale);

        for (InventarioDocTRBulk accessorioManuale : pending.getAccessori()) {
            if (isAccessorioDelPrincipale(accessorioManuale, benePrincipale)
                    && !contieneBene(accessoriStrutturali, accessorioManuale)) {
                aggiungiSeNonPresente(result, accessorioManuale);
            }
        }
        return result;
    }

    private void aggiungiSeNonPresente(List<InventarioDocTRBulk> destinazione, List<InventarioDocTRBulk> sorgente) {
        if (sorgente == null) {
            return;
        }
        for (InventarioDocTRBulk bene : sorgente) {
            aggiungiSeNonPresente(destinazione, bene);
        }
    }

    private void aggiungiSeNonPresente(List<InventarioDocTRBulk> destinazione, InventarioDocTRBulk bene) {
        if (bene == null || beneNelDettaglio(bene) || contieneBene(destinazione, bene)) {
            return;
        }
        destinazione.add(bene);
    }

    private boolean isAccessorioDelPrincipale(InventarioDocTRBulk accessorio, InventarioDocTRBulk principale) {
        return accessorio != null
                && principale != null
                && Objects.equals(accessorio.getNr_inventario(), principale.getNr_inventario())
                && !Objects.equals(accessorio.getProgressivo(), principale.getProgressivo());
    }

    private boolean contieneBene(List<InventarioDocTRBulk> lista, InventarioDocTRBulk candidato) {
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
        return (isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta) + 1;
    }

    public boolean hasBeniPrincipaliConAccessoriPerEliminazione() {
        return pendingDelete != null && !pendingDelete.getPrincipaliConAccessori().isEmpty();
    }

    public boolean hasBeniPrincipaliConAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.getPrincipaliConAccessori().isEmpty();
    }

    public boolean hasBeniAccessoriPendenti() {
        return pendingAdd != null && !pendingAdd.getAccessori().isEmpty();
    }

    public int getNumeroBeniPrincipaliConAccessori() {
        return pendingAdd == null ? 0 : pendingAdd.getPrincipaliConAccessori().size();
    }

    public int getNumeroBeniAccessoriTotaliPendenti() {
        if (pendingAdd == null) {
            return 0;
        }
        return pendingAdd.getPrincipaliConAccessori().values().stream().mapToInt(List::size).sum();
    }

    public int getNumeroBeniSemplici() {
        if (pendingAdd == null) {
            return 0;
        }
        return pendingAdd.getPrincipaliSenza().size() + pendingAdd.getAccessori().size();
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

    public void stampaDocTrasportoRientro(ActionContext context) throws Exception {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            throw new ApplicationException("Documento non valido");
        }

        try (InputStream input = getStreamDocTrasportoRientro(context.getUserContext(), doc)) {
            if (input == null) {
                return;
            }

            HttpServletResponse response = ((HttpActionContext) context).getResponse();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "inline; filename=\"doc_trasporto_rientro_" + doc.getPgDocTrasportoRientro() + ".pdf\"");
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

    private InputStream getStreamDocTrasportoRientro(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws Exception {

        Print_spoolerBulk print = DocTraspRientUtil.creaStampa(
                doc, "DocTrasportoRientro_" + doc.getPgDocTrasportoRientro(), userContext.getUser());

        Report report = SpringUtil.getBean("printService", PrintService.class)
                .executeReport(userContext, print);

        return report.getInputStream();
    }

    public void create(ActionContext context) throws BusinessProcessException {
        try {
            getModel().setToBeCreated();

            Doc_trasporto_rientroBulk docCreato = (Doc_trasporto_rientroBulk)
                    createComponentSession().creaConBulk(context.getUserContext(), getModel());

            setModel(context, docCreato);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        return (DocTrasportoRientroComponentSession) createComponentSession(
                COMPONENT_SESSION, DocTrasportoRientroComponentSession.class);
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
    protected void completeAllegato(T allegato, StorageObject storageObject) throws ApplicationException {
        Optional.ofNullable(storageObject.<String>getPropertyValue("sigla_commons_aspect:utente_applicativo"))
                .ifPresent(allegato::setUtenteSIGLA);

        List<String> secondaryTypes = storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");
        String aspect = DocTraspRientUtil.aspectDaStorage(allegato, secondaryTypes);

        if (aspect != null) {
            allegato.setAspectName(aspect);
        }

        super.completeAllegato(allegato, storageObject);
    }

    public void validaDatiPerDettagli() throws ApplicationException {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null) {
            return;
        }

        if (doc.getTipoMovimento() == null) {
            throw new ApplicationException("Selezionare il Tipo di Movimento prima di accedere al tab Dettagli.");
        }

        if (doc.isSmartworking()) {
            validaDatiSmartworking(doc);
            return;
        }

        validaDatiRitiro(doc);
    }

    private void validaDatiSmartworking(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (doc.getTerzoSmartworking() == null || doc.getTerzoSmartworking().getCd_terzo() == null) {
            throw new ApplicationException(
                    "Per il tipo di movimento Smartworking è necessario selezionare l'Assegnatario Smartworking.");
        }
    }

    private void validaDatiRitiro(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (!doc.hasTipoRitiroSelezionato()) {
            throw new ApplicationException("Selezionare il Tipo Ritiro (Incaricato o Vettore).");
        }

        if (doc.isRitiroIncaricato()
                && (doc.getTerzoIncRitiro() == null || doc.getTerzoIncRitiro().getCd_terzo() == null)) {
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

    protected void eliminaTuttiBeniDaDettagli(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            getComp().eliminaTuttiDettagliSalvati(context.getUserContext(), doc);

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

            getComp().eliminaDettagliSalvati(context.getUserContext(), doc, details);

            rimuoviBeniDaCollection(doc, details);
            setModel(context, doc);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaBeneCorrente(ActionContext context) throws BusinessProcessException {
        InventarioDocTRBulk bene = getBenePrincipaleCorrente(true);
        if (bene == null) {
            return;
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();
            List<InventarioDocTRBulk> accessori = getAccessoriCorrente(true);

            getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(context.getUserContext(), doc, bene, accessori);

            rimuoviBeneDaCollection(doc, bene);
            for (InventarioDocTRBulk accessorio : accessori) {
                rimuoviBeneDaCollection(doc, accessorio);
            }
            setModel(context, doc);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context) throws BusinessProcessException {
        InventarioDocTRBulk bene = getBenePrincipaleCorrente(true);
        if (bene == null) {
            return;
        }

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            getComp().eliminaDettagliSalvati(context.getUserContext(), doc, new OggettoBulk[]{bene});

            rimuoviBeneDaCollection(doc, bene);
            setModel(context, doc);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void rimuoviBeniDaCollection(Doc_trasporto_rientroBulk doc, OggettoBulk[] beni) {
        if (beni == null) {
            return;
        }
        for (OggettoBulk bene : beni) {
            if (bene instanceof InventarioDocTRBulk) {
                rimuoviBeneDaCollection(doc, (InventarioDocTRBulk) bene);
            }
        }
    }

    private void rimuoviBeneDaCollection(Doc_trasporto_rientroBulk doc, InventarioDocTRBulk bene) {
        if (doc == null || doc.getDoc_trasporto_rientro_dettColl() == null) {
            return;
        }
        doc.getDoc_trasporto_rientro_dettColl().removeIf(obj ->
                obj instanceof Doc_trasporto_rientro_dettBulk
                        && stessoBene((Doc_trasporto_rientro_dettBulk) obj, bene));
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

    private boolean stessoBene(Doc_trasporto_rientro_dettBulk dettaglio, InventarioDocTRBulk bene) {
        if (dettaglio == null || bene == null || bene.getProgressivo() == null) {
            return false;
        }
        return Objects.equals(dettaglio.getNr_inventario(), bene.getNr_inventario())
                && Objects.equals(dettaglio.getProgressivo(), bene.getProgressivo().intValue());
    }

    /** Assegna il file caricato all'allegato selezionato (sostituzione) o al primo nuovo senza file. */
    private void assegnaFileUploadato(ActionContext context, Doc_trasporto_rientroBulk doc) {
        if (!(context instanceof HttpActionContext)
                || doc == null
                || doc.getArchivioAllegati() == null) {
            return;
        }

        it.cnr.jada.util.upload.UploadedFile uploadedFile =
                ((HttpActionContext) context).getMultipartParameter("main.ArchivioAllegati.file");

        if (uploadedFile == null || uploadedFile.getName() == null || uploadedFile.getName().isEmpty()) {
            return;
        }

        AllegatoGenericoBulk selezionato = (AllegatoGenericoBulk) getCrudArchivioAllegati().getModel();
        Optional<AllegatoGenericoBulk> destinazione = Optional.empty();

        // Allegato esistente non bloccato: il nuovo file ne sostituisce il contenuto
        if (isFirmaManuale()
                && selezionato instanceof AllegatoDocTraspRientroBulk
                && !selezionato.isToBeCreated()
                && !((AllegatoDocTraspRientroBulk) selezionato).isAllegatoBloccato()
                && doc.getArchivioAllegati().contains(selezionato)) {
            destinazione = Optional.of(selezionato);
        }
        if (!destinazione.isPresent()) {
            destinazione = doc.getArchivioAllegati().stream()
                    .filter(a -> a.isToBeCreated() && a.getFile() == null)
                    .findFirst();
        }

        destinazione.ifPresent(a -> {
            a.setFile(uploadedFile.getFile());
            a.setContentType(uploadedFile.getContentType());
            a.setNome(a.parseFilename(uploadedFile.getName()));
            if (a.getCrudStatus() == OggettoBulk.NORMAL) {
                a.setToBeUpdated();
            }
        });
    }

    private void validaAllegati(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc.getArchivioAllegati() == null) {
            return;
        }
        for (AllegatoGenericoBulk allegato : doc.getArchivioAllegati()) {
            allegato.validate();
        }
    }

    @Override
    public void update(ActionContext context) throws BusinessProcessException {
        Doc_trasporto_rientroBulk docPrima = getDoc();
        Timestamp dataInvioPrec = docPrima != null ? docPrima.getDataInvioFirma() : null;
        Timestamp dataFirmaPrec = docPrima != null ? docPrima.getDataFirma() : null;

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            validaUpdate(doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            assegnaFileUploadato(context, doc);
            validaTipologieAllegatiFirmaManuale(doc);
            validaAllegati(doc);

            doc.setToBeUpdated();

            BulkList<AllegatoGenericoBulk> allegatiVisibili = doc.getArchivioAllegati();

            Doc_trasporto_rientroBulk docAggiornato = (Doc_trasporto_rientroBulk)
                    createComponentSession().modificaConBulk(context.getUserContext(), doc);

            // Firma manuale: il Salva in bozza non archivia e lascia visibili gli allegati
            boolean archivia = !isFirmaManuale() || archiviaAllegatiNelDocumentale;
            ultimoSalvataggioArchiviato = archivia;
            if (!archivia) {
                docAggiornato.setArchivioAllegati(allegatiVisibili);
            }

            setModel(context, docAggiornato);

            if (archivia) {
                setSkipAllegatiReload(false);
                getComp().archiviaAllegatiDocTR(context.getUserContext(), docAggiornato);
            }

            commitUserTransaction();
            setMessage("Documento salvato correttamente.");

        } catch (ApplicationException e) {
            rollbackUserTransaction();
            ripristinaDateFirma(dataInvioPrec, dataFirmaPrec);
            throw new BusinessProcessException(e);
        } catch (Exception e) {
            rollbackUserTransaction();
            ripristinaDateFirma(dataInvioPrec, dataFirmaPrec);
            throw handleException(e);
        }
    }

    private void validaUpdate(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (doc == null) {
            throw new ApplicationException("Modello non disponibile.");
        }
        if (doc.isAnnullato() || doc.isDefinitivo()) {
            throw new ApplicationException("Documento non modificabile.");
        }
        if (doc.isInAttesaDiFirma()) {
            throw new ApplicationException("Documento in attesa di firma: allegati non modificabili.");
        }
        if (doc.isFirmaCompletabile() && !isAllegatiModificabili()) {
            throw new ApplicationException("Allegati non modificabili.");
        }
        // L'obbligo del FIRMATO vale solo per la firma da workflow
        if (doc.isFirmatoDaCompletare()) {
            validaFirmatoPresente(doc);
        }
    }

    /** Il documento firmato deve mantenere un unico allegato FIRMATO attivo. */
    private void validaFirmatoPresente(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        String aspectFirmato = DocTraspRientUtil.aspectFirmato(doc);
        if (aspectFirmato == null || doc.getArchivioAllegati() == null) {
            throw new ApplicationException("Documento firmato privo del file FIRMATO: impossibile salvare.");
        }

        boolean firmatoAttivo = false;
        for (AllegatoGenericoBulk a : doc.getArchivioAllegati()) {
            if (!(a instanceof AllegatoDocTraspRientroBulk)) continue;
            AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) a;
            if (!aspectFirmato.equals(allegato.getAspectName())) continue;

            if (allegato.getCrudStatus() == OggettoBulk.TO_BE_DELETED) {
                if (allegato.getStorageKey() != null) {
                    throw new ApplicationException(
                            "Il file FIRMATO non può essere eliminato da un documento firmato.");
                }
            } else {
                firmatoAttivo = true;
            }
        }

        if (!firmatoAttivo) {
            throw new ApplicationException(
                    "Documento firmato privo del file FIRMATO: caricare la copia firmata prima di salvare.");
        }
    }

    public void salvaDefinitivo(ActionContext context) throws BusinessProcessException {
        Doc_trasporto_rientroBulk docPrima = getDoc();
        Timestamp dataInvioPrec = docPrima != null ? docPrima.getDataInvioFirma() : null;
        Timestamp dataFirmaPrec = docPrima != null ? docPrima.getDataFirma() : null;

        try {
            Doc_trasporto_rientroBulk doc = getDoc();

            validaSalvaDefinitivo(doc);

            if (doc.getArchivioAllegati() == null) {
                doc.setArchivioAllegati(new BulkList<>());
            }

            assegnaFileUploadato(context, doc);
            validaTipologieAllegatiFirmaManuale(doc);
            impostaDataFirmaManuale(doc);
            validaAllegati(doc);
            getComp().validaAggiuntaAllegatoFirmato(doc);

            doc.setToBeUpdated();

            doc = getComp().salvaDefinitivo(context.getUserContext(), doc);

            setModel(context, doc);
            setSkipAllegatiReload(false);

            getComp().archiviaAllegatiDocTR(context.getUserContext(), doc);

            doc.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            doc.setCrudStatus(OggettoBulk.NORMAL);

            setStatus(VIEW);
            commitUserTransaction();
            setMessage("Documento salvato in stato DEFINITIVO.");

        } catch (ComponentException | RemoteException | ValidationException e) {
            rollbackUserTransaction();
            ripristinaDateFirma(dataInvioPrec, dataFirmaPrec);
            throw handleException(e);
        }
    }

    private void validaSalvaDefinitivo(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (doc == null) {
            throw new ApplicationException("Modello non disponibile.");
        }
        if (doc.isAnnullato()) {
            throw new ApplicationException("Documento annullato: impossibile salvare definitivo.");
        }
        if (doc.isDefinitivo()) {
            throw new ApplicationException("Documento già definitivo.");
        }
        if (!isFirmaManuale() && doc.getDataFirma() == null) {
            throw new ApplicationException("Documento non ancora firmato: impossibile salvare definitivo.");
        }
        if (doc.isInAttesaDiFirma()) {
            throw new ApplicationException("Documento ancora in attesa di firma.");
        }
        if (!doc.isFirmaCompletabile()) {
            throw new ApplicationException(isFirmaManuale()
                    ? "Documento non ancora predisposto alla firma: impossibile salvare definitivo."
                    : "Documento non ancora firmato: impossibile salvare definitivo.");
        }
    }

    /** Firma manuale: un solo "Documento di T/R" per volta (prima il da firmare, poi il firmato). */
    private void validaTipologieAllegatiFirmaManuale(Doc_trasporto_rientroBulk doc) throws ApplicationException {
        if (!isFirmaManuale() || doc == null || doc.getArchivioAllegati() == null) {
            return;
        }

        int documenti = 0;
        for (AllegatoGenericoBulk a : doc.getArchivioAllegati()) {
            if (a instanceof AllegatoDocTraspRientroBulk && a.getCrudStatus() != OggettoBulk.TO_BE_DELETED) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) a;
                if (allegato.isAspectFirmato() || (!doc.isPredispostoAllaFirma() && allegato.isAspectDaFirmare())) {
                    documenti++;
                }
            }
        }

        if (documenti > 1) {
            throw new ApplicationException("È consentito allegare un solo Documento di "
                    + DocTraspRientUtil.descrizioneTipo(getTipo())
                    + (doc.isPredispostoAllaFirma() ? " firmato." : "."));
        }
    }

    /** Firma manuale: DATA_FIRMA impostata solo dal Salva definitivo con il firmato presente. */
    private void impostaDataFirmaManuale(Doc_trasporto_rientroBulk doc) {
        if (!isFirmaManuale() || doc == null || !hasAllegatoFirmato()) {
            return;
        }
        doc.setDataFirma(new Timestamp(System.currentTimeMillis()));
    }

    /** Dopo un rollback il modello in memoria torna alle date precedenti. */
    private void ripristinaDateFirma(Timestamp dataInvioFirma, Timestamp dataFirma) {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc != null) {
            doc.setDataInvioFirma(dataInvioFirma);
            doc.setDataFirma(dataFirma);
        }
    }

    @Override
    public boolean isEditable() {
        if (isViewing()) {
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

    @Override
    public boolean isInputReadonly() {
        if (isAllegatiModificabili()) {
            return super.isInputReadonly();
        }
        return super.isInputReadonly() || isTestataEDettagliBloccati() || isViewing();
    }

    public boolean isAnagraficiReadonly() {
        return isTestataEDettagliBloccati() || isViewing() || (!isInserting() && !isEditing());
    }

    /** Documento definitivo, annullato, in attesa di firma o inviato in firma: niente salvataggi. */
    private boolean isSalvataggioBloccato(Doc_trasporto_rientroBulk doc) {
        return doc.isDefinitivo() || doc.isAnnullato() || doc.isInAttesaDiFirma() || doc.isInviatoInFirma();
    }

    @Override
    public boolean isSaveButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || isViewing() || isSalvataggioBloccato(doc)) {
            return false;
        }

        return super.isSaveButtonEnabled()
                && (isEditing() || isInserting())
                && (!isDocumentoNonModificabile() || isAllegatiModificabili());
    }

    @Override
    public boolean isSaveButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null) {
            return false;
        }
        if (isSalvataggioBloccato(doc)) {
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
        return !doc.isFirmaCompletabile();
    }

    public boolean isSalvaDefinitivoButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || isViewing()) {
            return false;
        }
        if (doc.isAnnullato() || doc.isDefinitivo()) {
            return false;
        }
        return doc.isFirmaCompletabile()
                && doc.hasDettagli()
                && hasAllegatoFirmato()
                && (isEditing() || isInserting());
    }

    /** Tab Allegati accessibile dall'invio in firma in poi (firma manuale: dal primo salvataggio). */
    public boolean isAllegatiAccessibili() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return false;
        }
        return doc.isInviatoInFirma()
                || doc.isInAttesaDiFirma()
                || doc.isFirmaCompletabile()
                || doc.isDefinitivo()
                || doc.isAnnullato()
                || isFirmaManualeSalvata(doc);
    }

    public boolean isAllegatiModificabili() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || isViewing()) {
            return false;
        }
        if (doc.isAnnullato() || doc.isDefinitivo()) {
            return false;
        }
        return doc.isFirmaCompletabile() || isFirmaManualeSalvata(doc);
    }

    /**
     * Tipologia e Nome degli allegati salvati bloccati quando il documento è
     * - profilo ISS: inviato in firma, firmato, definitivo o annullato;
     * - firma manuale: predisposto alla firma, definitivo o annullato.
     */
    public boolean isTipologiaNomeAllegatiBloccati() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null) {
            return false;
        }
        if (doc.isDefinitivo() || doc.isAnnullato()) {
            return true;
        }
        if (isFirmaManuale()) {
            return doc.isPredispostoAllaFirma();
        }
        return doc.isInviatoInFirma() || doc.isFirmatoDaCompletare();
    }

    /** Propaga il blocco agli allegati prima del disegno del tab (chiamato dalla JSP a ogni render). */
    @Override
    public String getAllegatiFormName() {
        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc != null && doc.getArchivioAllegati() != null) {
            boolean bloccati = isTipologiaNomeAllegatiBloccati();
            for (AllegatoGenericoBulk allegato : doc.getArchivioAllegati()) {
                if (allegato instanceof AllegatoDocTraspRientroBulk) {
                    ((AllegatoDocTraspRientroBulk) allegato).setTipologiaNomeBloccati(bloccati);
                }
            }
        }
        return super.getAllegatiFormName();
    }

    /** Firma manuale, documento salvato con PG definitivo e ancora INSERITO. */
    private boolean isFirmaManualeSalvata(Doc_trasporto_rientroBulk doc) {
        return isFirmaManuale() && doc.isInserito() && !doc.isTemporaneo();
    }

    /** Firma manuale: mostra solo data invio firma e data firma. */
    public boolean isDateFirmaManualeVisible() {
        Doc_trasporto_rientroBulk doc = getDoc();
        return isFirmaManuale() && doc != null && !isInserting()
                && (doc.getDataInvioFirma() != null || doc.getDataFirma() != null);
    }

    @Override
    protected boolean isChildGrowable(boolean isGrowable) {
        return isAllegatiModificabili();
    }
}
