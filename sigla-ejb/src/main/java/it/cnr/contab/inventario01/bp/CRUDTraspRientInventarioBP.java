package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.BuonoCaricoScaricoComponentSession;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.SimpleCRUDBP;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.STATO_INSERITO;

/**
 * Business Process OTTIMIZZATO per la gestione dei documenti di Trasporto/Rientro.
 * <p>
 * OTTIMIZZAZIONI:
 * -  Rimossa duplicazione logica inizializzazione
 * -  Semplificata gestione visibilità
 * -  Centralizzati null check
 * - Uniformata gestione bottoni
 * -  Migliorata gestione print
 */
public abstract class CRUDTraspRientInventarioBP extends SimpleCRUDBP {

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private String tipo;
    private boolean isAmministratore = false;
    private boolean isVisualizzazione = false;

    // ========================================
    // COSTRUTTORI
    // ========================================

    public CRUDTraspRientInventarioBP() {
        super();
    }

    public CRUDTraspRientInventarioBP(String function) {
        super(function);
    }


    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context.getUserContext());
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
        try {
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession) createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession", DocTrasportoRientroComponentSession.class);
            setVisualizzazione(session.isEsercizioCOEPChiuso(context.getUserContext()));
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e) {
            throw handleException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }
        super.init(config, context);

        initVariabili(context, getTipo());
    }

    public void initVariabili(it.cnr.jada.action.ActionContext context, String Tipo) {

        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk) throws BusinessProcessException {
        try {
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession) createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession", DocTrasportoRientroComponentSession.class);
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e1) {
            throw handleException(e1);
        } catch (RemoteException e1) {
            throw handleException(e1);
        }
        bulk = super.initializeModelForEdit(context, bulk);
        return bulk;
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
    // GESTIONE STATO CENTRALIZZATA
    // ========================================

    protected Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    /**
     * Verifica se il documento corrente è annullato
     */
    public boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    /**
     * Verifica se il documento è predisposto alla firma
     */
    public boolean isDocumentoPredispostoAllaFirma() {
        return getDoc() != null && getDoc().isPredispostoAllaFirma();
    }

    /**
     * Verifica se il documento è in uno stato non modificabile
     */
    public boolean isDocumentoNonModificabile() {
        return isDocumentoAnnullato() || isDocumentoPredispostoAllaFirma();
    }

    /**
     * Un documento NON è modificabile se:
     * - L'esercizio è chiuso (isVisualizzazione = true)
     * - Il documento è annullato
     * - Il documento è predisposto alla firma
     */
    @Override
    public boolean isEditable() {
        if (isDocumentoNonModificabile()) {  // ← Controlla anche "predisposto alla firma"
            return false;
        }
        return !isVisualizzazione() && super.isEditable();
    }

    /**
     * Verifica se i campi devono essere in sola lettura
     */
    public boolean isInputReadonly() {
        return isDocumentoNonModificabile() || isVisualizzazione();
    }

    // ========================================
    // VISIBILITÀ UI OTTIMIZZATA
    // ========================================

    /**
     * Helper per verificare se il modello è valido
     */
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

    // ========================================
    // READONLY FIELDS OTTIMIZZATI
    // ========================================

    /**
     * Metodo base per verificare se campi critici sono readonly
     */
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
    // VISIBILITÀ BOTTONI UNIFICATA
    // ========================================

    /**
     * Metodo base per tutti i bottoni di modifica
     */
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
    // PRINT OTTIMIZZATO
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

        // Usa helper generico per aggiungere parametri
        addPrintParam(printbp, "esercizio", docT.getEsercizio());
        addPrintParam(printbp, "pgInventario", docT.getPgInventario());
        addPrintParam(printbp, "tiDocumento", docT.getTiDocumento());
        addPrintParam(printbp, "pgDocTrasportoRientro", docT.getPgDocTrasportoRientro());
    }

    /**
     * Helper generico ottimizzato per aggiungere parametri di stampa
     */
    private void addPrintParam(OfflineReportPrintBP printbp, String name, Object value) {
        Print_spooler_paramBulk param = new Print_spooler_paramBulk();
        param.setNomeParam(name);
        param.setValoreParam(value != null ? String.valueOf(value) : null);
        param.setParamType(value != null ? value.getClass().getCanonicalName() : String.class.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    // ========================= TOOLBAR & PRINT =========================

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
}