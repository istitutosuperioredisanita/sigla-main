package it.cnr.contab.inventario01.bp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
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
import it.cnr.jada.bulk.BulkList;
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
import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

/**
 * Business Process per la gestione dei documenti di Trasporto e Rientro di beni inventariali.
 * <p>
 * Gestisce il ciclo di vita completo dei documenti (CRUD + workflow), l'associazione di beni
 * inventariali, la gestione degli allegati su documentale Azure e il flusso di firma digitale.
 * <p>
 * Funzionalità principali:
 * - Creazione/modifica/eliminazione documenti trasporto/rientro
 * - Selezione beni principali e accessori con validazione preventiva
 * - Gestione allegati firmati/non firmati su CMIS
 * - Workflow: Inserito → Inviato in Firma → Definitivo / Annullato
 * - Stampa PDF documento con template Jasper
 * - Validazione presenza allegati firmati per stato Definitivo
 */
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
    private boolean skipAllegatiReload = false;


    /**
     * Classe interna per gestire la selezione ricorsiva di beni con accessori
     */
    public static class PendingSelection {
        Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori = new LinkedHashMap<>();
        List<Inventario_beniBulk> accessori = new ArrayList<>();
        List<Inventario_beniBulk> principaliSenza = new ArrayList<>();
        OggettoBulk[] bulks = null;
        BitSet oldSel = null;
        BitSet newSel = null;
        BitSet selectionAccumulata = null;

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

    public CRUDTraspRientInventarioBP() {
        super("Tn");
    }

    public CRUDTraspRientInventarioBP(String function) {
        super(function + "Tn");
    }

    /**
     * Restituisce il nome del controller per i dettagli beni
     */
    protected abstract String getDettagliControllerName();

    /**
     * Restituisce il nome del controller per la modifica dettagli
     */
    protected abstract String getEditDettagliControllerName();

    /**
     * Restituisce il nome del tab principale
     */
    public abstract String getMainTabName();

    /**
     * Restituisce la label per la data registrazione
     */
    public abstract String getLabelData_registrazione();

    /**
     * Inizializza la selezione beni nel component
     */
    public abstract void inizializzaSelezioneComponente(ActionContext context) throws ComponentException, RemoteException;

    /**
     * Annulla modifiche in corso nel component
     */
    public abstract void annullaModificaComponente(ActionContext context) throws ComponentException, RemoteException;

    /**
     * Modifica beni con gestione accessori nel component
     */
    public abstract void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks, BitSet oldSelection, BitSet newSelection) throws ComponentException, RemoteException, BusinessProcessException;

    /**
     * Restituisce la classe del documento dettaglio
     */
    public abstract Class getDocumentoClassDett();

    public PendingSelection getPendingDelete() {
        return pendingDelete;
    }

    private void setPendingDelete(PendingSelection pendingDelete) {
        this.pendingDelete = pendingDelete;
    }

    /**
     * Inizializzazione del BP con verifica permessi e configurazioni
     */
    protected void init(Config config, ActionContext context) throws BusinessProcessException {
        Integer esercizio = CNRUserContext.getEsercizio(context.getUserContext());
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);

        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());

        try {
            DocTrasportoRientroComponentSession docSession = (DocTrasportoRientroComponentSession)
                    createComponentSession(
                            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class
                    );

            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));

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

    public boolean isSkipAllegatiReload() {
        return skipAllegatiReload;
    }

    public void setSkipAllegatiReload(boolean skipAllegatiReload) {
        this.skipAllegatiReload = skipAllegatiReload;
    }

    /**
     * Inizializza variabili di stato in base al tipo documento
     */
    public void initVariabili(ActionContext context, String Tipo) {
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    /**
     * Inizializza modello per inserimento con collection allegati vuota
     */
    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        bulk = super.initializeModelForInsert(context, bulk);
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
        doc.setArchivioAllegati(new BulkList());
        return bulk;
    }

    /**
     * Inizializza modello per modifica con verifica stato documento
     */
    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        try {
            createComponentSession(
                    "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                    DocTrasportoRientroComponentSession.class
            );

            setAmministratore(
                    UtenteBulk.isAmministratoreInventario(context.getUserContext())
            );

            bulk = super.initializeModelForEdit(context, bulk);

            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;

            if (doc != null && Doc_trasporto_rientroBulk.STATO_ANNULLATO.equals(doc.getStato())) {
                setErrorMessage("Documento ANNULLATO - Nessuna modifica consentita");
            }

            return bulk;

        } catch (ComponentException | RemoteException e1) {
            throw handleException(e1);
        }
    }

    /**
     * Override per controllare se saltare il reload degli allegati durante il salvataggio.
     * Se il flag skipAllegatiReload è true, non ricarica gli allegati dallo storage CMIS.
     */
    @Override
    public OggettoBulk initializeModelForEditAllegati(ActionContext actioncontext, OggettoBulk oggettobulk)
            throws BusinessProcessException {
        if (isSkipAllegatiReload()) {
            return oggettobulk;
        }

        return super.initializeModelForEditAllegati(actioncontext, oggettobulk);
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    /**
     * Resetta i tab alla vista principale
     */
    public void resetTabs() {
        setTab("tab", getMainTabName());
    }

    /**
     * Gestisce cambio tab con validazione dati per dettagli/allegati
     */
    @Override
    public void setTab(String tabName, String tabValue) {
        if ("tab".equals(tabName) && tabValue != null) {
            Doc_trasporto_rientroBulk doc = getDoc();
            boolean isTabDettagli = tabValue.endsWith("Dettaglio") || tabValue.endsWith("Allegati");

            if (isTabDettagli) {
                if (doc != null && doc.getCrudStatus() == OggettoBulk.UNDEFINED) {
                    setErrorMessage("Non è possibile cambiare tab: in questa fase è consentita solo la ricerca.");
                    return;
                }

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

    /**
     * Restituisce il documento corrente
     */
    public Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    /**
     * Verifica se documento è annullato
     */
    public boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    /**
     * Verifica se documento è inviato in firma
     */
    public boolean isDocumentoInviatoInFirma() {
        return getDoc() != null && getDoc().isInviatoInFirma();
    }

    /**
     * Verifica se documento è definitivo
     */
    public boolean isDocumentoDefinitivo() {
        return getDoc() != null && getDoc().isDefinitivo();
    }

    /**
     * Verifica se documento è in stato non modificabile
     */
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

    /**
     * Determina se gli input devono essere readonly
     */
    public boolean isInputReadonly() {
        return isDocumentoNonModificabile() || isVisualizzazione();
    }

    private boolean hasValidModel() {
        return getModel() != null && getDoc() != null;
    }

    /**
     * Visibilità Tipo Ritiro (solo se NON Smartworking)
     */
    public boolean isTipoRitiroVisible() {
        return hasValidModel() && !isSmartworking();
    }

    /**
     * Visibilità Assegnatario Smartworking (solo se Smartworking)
     */
    public boolean isTerzoSmartworkingVisible() {
        return hasValidModel() && isSmartworking();
    }

    /**
     * Visibilità Destinazione (solo se NON Smartworking e tipo ritiro selezionato)
     */
    public boolean isDestinazioneVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().hasTipoRitiroSelezionato();
    }

    /**
     * Visibilità Assegnatario (solo se ritiro incaricato)
     */
    public boolean isAssegnatarioVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().isRitiroIncaricato();
    }

    /**
     * Visibilità Nominativo Vettore
     */
    public boolean isNominativoVettoreVisible() {
        return hasValidModel() && !isSmartworking() && getDoc().isNominativoVettoreVisible();
    }

    /**
     * Verifica se note sono abilitate dal tipo movimento
     */
    public boolean isNoteAbilitate() {
        return hasValidModel()
                && !isSmartworking()
                && getDoc().getTipoMovimento() != null
                && getDoc().getTipoMovimento().isAbilitaNote();
    }

    /**
     * Visibilità campo Stato (solo se documento salvato)
     */
    public boolean isStatoVisible() {
        return hasValidModel() && getDoc().getStato() != null && !isInserting();
    }

    /**
     * Visibilità campo Note
     */
    public boolean isNoteVisible() {
        return hasValidModel() && getDoc().isNoteRitiroEnabled();
    }

    /**
     * Readonly Tipo Movimento (se ci sono dettagli)
     */
    public boolean isTipoMovimentoReadOnly() {
        return isCampiCriticiReadOnly();
    }

    /**
     * Readonly campi critici (tipo movimento) se ci sono dettagli
     */
    private boolean isCampiCriticiReadOnly() {
        if (isDocumentoNonModificabile()) return true;
        return isEditing() && hasValidModel()
                && getDoc().getDoc_trasporto_rientro_dettColl() != null
                && !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    /**
     * Abilita campo Quantità
     */
    public boolean isQuantitaEnabled() {
        return !isDocumentoNonModificabile() && (isEditing() || isInserting());
    }

    /**
     * Nasconde pulsante ELIMINA se annullato/inviato/definitivo con rientro NON annullato
     */
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

    /**
     * Verifica se trasporto ha almeno un rientro associato NON annullato.
     * LOGICA:
     * - Se NON ha rientri → return false (pulsante visibile)
     * - Se ha rientri ANNULLATI → return false (pulsante visibile)
     * - Se ha rientri ATTIVI (non annullati) → return true (pulsante nascosto)
     */
    private boolean haRientroNonAnnullato() {
        if (!(getDoc() instanceof DocumentoTrasportoBulk)) {
            return false;
        }

        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null ||
                doc.getDoc_trasporto_rientro_dettColl() == null ||
                doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {
            return false;
        }

        for (Object obj : doc.getDoc_trasporto_rientro_dettColl()) {
            if (obj instanceof DocumentoTrasportoDettBulk) {
                DocumentoTrasportoDettBulk dettaglio = (DocumentoTrasportoDettBulk) obj;

                if (dettaglio.getDocRientroDettRif() != null) {
                    Doc_trasporto_rientro_dettBulk rientroDettaglio = dettaglio.getDocRientroDettRif();

                    if (rientroDettaglio.getDoc_trasporto_rientro() != null) {
                        Doc_trasporto_rientroBulk docRientro = rientroDettaglio.getDoc_trasporto_rientro();

                        if (!docRientro.isAnnullato()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Abilita pulsante SALVA (disabilitato se definitivo)
     */
    @Override
    public boolean isSaveButtonEnabled() {
        return !isDocumentoNonModificabile();
    }

    /**
     * Nasconde pulsante SALVA se definitivo/annullato
     */
    @Override
    public boolean isSaveButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();
        return doc != null && (doc.isDefinitivo() || doc.isAnnullato());
    }

    @Override
    public boolean isNewButtonEnabled() {
        return !isVisualizzazione();
    }

    /**
     * Nasconde pulsante STAMPA se documento non salvato o non nel tab testata
     */
    public boolean isStampaDocButtonHidden() {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();
        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return true;
        }
        String currentTab = getTab("tab");
        return !getMainTabName().equals(currentTab);
    }

    public boolean isStampaDocButtonEnabled() {
        return !isStampaDocButtonHidden() && !isVisualizzazione();
    }

    /**
     * Nasconde pulsante SALVA DEFINITIVO se non salvato/definitivo/senza allegati
     */
    public boolean isSalvaDefinitivoButtonHidden() {
        Doc_trasporto_rientroBulk doc = getDoc();

        if (doc == null || doc.getPgDocTrasportoRientro() == null) {
            return true;
        }

        if (isDocumentoNonModificabile()) {
            return true;
        }

        return !hasAllegati();
    }

    /**
     * Verifica presenza allegati non cancellati
     */
    private boolean hasAllegati() {
        if (getModel() == null) {
            return false;
        }

        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoGenericoBulk) {
                AllegatoGenericoBulk allegato = (AllegatoGenericoBulk) obj;

                if (allegato.getCrudStatus() != OggettoBulk.TO_BE_DELETED) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Abilita pulsante SALVA DEFINITIVO se inserito, con dettagli e allegato firmato
     */
    public boolean isSalvaDefinitivoButtonEnabled() {
        Doc_trasporto_rientroBulk doc = getDoc();

        return doc != null
                && doc.isInserito()
                && !doc.isAnnullato()
                && doc.hasDettagli()
                && hasAllegatoFirmato()
                && !isVisualizzazione();
    }

    /**
     * Verifica presenza allegato firmato (controllo UI-only)
     */
    private boolean hasAllegatoFirmato() {
        if (getModel() == null) {
            return false;
        }

        Doc_trasporto_rientroBulk doc = getDoc();
        if (doc == null || doc.getArchivioAllegati() == null) {
            return false;
        }

        String aspectFirmatoAtteso;
        if (doc instanceof DocumentoTrasportoBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else if (doc instanceof DocumentoRientroBulk) {
            aspectFirmatoAtteso = AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        } else {
            return false;
        }

        for (Object obj : doc.getArchivioAllegati()) {
            if (obj instanceof AllegatoDocTraspRientroBulk) {
                AllegatoDocTraspRientroBulk allegato = (AllegatoDocTraspRientroBulk) obj;
                String aspectName = allegato.getAspectName();

                if (aspectName != null
                        && aspectName.equals(aspectFirmatoAtteso)
                        && (OggettoBulk.NORMAL == allegato.getCrudStatus() || isEditing())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica se tipo movimento è Smartworking
     */
    public boolean isSmartworking() {
        return hasValidModel() && getDoc().isSmartworking();
    }

    /**
     * Inizializza parametri stampa per report Jasper
     */
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

    /**
     * Crea toolbar con pulsanti aggiuntivi (firma, definitivo, stampa)
     */
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
     * Nasconde pulsante INVIA IN FIRMA se disabilitato o stato non valido
     */
    public boolean isInviaInFirmaButtonHidden() {
        int status = getModel() != null ? getModel().getCrudStatus() : -1;
        Doc_trasporto_rientroBulk doc = getDoc();

        return !isGestioneInvioInFirmaAttiva()
                || getModel() == null
                || isDocumentoAnnullato() || isDocumentoInviatoInFirma()
                || (status != OggettoBulk.NORMAL && status != OggettoBulk.TO_BE_UPDATED)
                || doc == null
                || doc.getStato() == null
                || !STATO_INSERITO.equals(doc.getStato());
    }

    /**
     * Controller per gestione beni associati (visualizzazione tabella beni)
     */
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

            eliminaTuttiBeniDaDettagli(context);
            reset(context);
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

                for (OggettoBulk o : details) {
                    Inventario_beniBulk bene = (Inventario_beniBulk) o;

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
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

                if (ps.principaliConAccessori.isEmpty()) {
                    eliminaBeniDaDettagli(context, details);
                } else {
                    pendingDelete = ps;
                }

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    };

    /**
     * Controller per modifica dettagli documento
     */
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

    /**
     * Carica dettagli per modifica da database
     */
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

    /**
     * Carica beni associati al documento
     */
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

    /**
     * Invia documento in firma digitale
     */
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

    /**
     * Annulla documento (stato ANNULLATO)
     */
    public void annullaDoc(ActionContext context) throws BusinessProcessException {
        try {
            Doc_trasporto_rientroBulk doc = getComp().annullaDocumento(
                    context.getUserContext(), getDoc());

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

    /**
     * Valida stato documento prima dell'invio in firma
     */
    private void validaStatoPerFirma() throws BusinessProcessException {
        if (isDocumentoAnnullato()) {
            throw new BusinessProcessException("Impossibile inviare in firma un documento annullato");
        }
        if (!getDoc().isInserito()) {
            throw new BusinessProcessException("Il documento deve essere in stato 'Inserito' per essere inviato in firma");
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
        if (isDocumentoNonModificabile()) return;
        try {
            inizializzaSelezioneComponente(context);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Seleziona tutti i beni disponibili con validazione (max 100)
     */
    @Override
    public void selectAll(ActionContext context) throws BusinessProcessException {
        if (isDocumentoNonModificabile()) {
            throw new BusinessProcessException("Impossibile modificare il documento");
        }

        try {
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;
            List<Inventario_beniBulk> beniFiltrati = getComp().caricaBeniPerInserimento(
                    context.getUserContext(),
                    doc,
                    getClauses(),
                    isTrasporto
            );

            if (beniFiltrati != null && beniFiltrati.size() > 100) {
                throw new ApplicationException(
                        "Impossibile selezionare più di 100 beni contemporaneamente"
                );
            }

            getComp().validaBeniNonInAltriDocumenti(
                    context.getUserContext(),
                    doc,
                    beniFiltrati
            );

            getComp().selezionaTuttiBeni(context.getUserContext(), doc, getClauses());
            setClauses(null);
            getDettBeniController().resync(context);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Gestisce selezione beni con validazione e gestione accessori
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

            List<Inventario_beniBulk> beniDaAggiungere = new ArrayList<>();

            for (int i = 0; i < bulks.length; i++) {
                assert oldSelection != null;
                assert newSelection != null;
                if (oldSelection.get(i) == newSelection.get(i)) {
                    continue;
                }

                Inventario_beniBulk bene = (Inventario_beniBulk) bulks[i];

                if (ps.newSel.get(i)) {
                    beniDaAggiungere.add(bene);

                    if (bene.isBeneAccessorio()) {
                        ps.accessori.add(bene);
                    } else {
                        List found;
                        if (getDoc() instanceof DocumentoRientroBulk) {
                            found = getComp().cercaBeniAccessoriPresentinelTrasportoOriginale(
                                    context.getUserContext(),
                                    bene,
                                    getDoc()
                            );
                        } else {
                            found = getComp().cercaBeniAccessoriAssociati(
                                    context.getUserContext(),
                                    bene
                            );
                        }

                        if (found != null && !found.isEmpty()) {
                            ps.principaliConAccessori.put(bene, found);
                            beniDaAggiungere.addAll(found);
                        } else {
                            ps.principaliSenza.add(bene);
                        }
                    }
                }
            }

            if (!beniDaAggiungere.isEmpty()) {
                if (beniDaAggiungere.size() > 100) {
                    throw new ApplicationException(
                            "Impossibile selezionare più di 100 beni contemporaneamente"
                    );
                }

                getComp().validaBeniNonInAltriDocumenti(
                        context.getUserContext(),
                        getDoc(),
                        beniDaAggiungere
                );
            }

            if (ps.principaliConAccessori.isEmpty()) {
                if (!ps.isEmpty()) {
                    modificaBeniConAccessoriComponente(context, bulks, oldSelection, newSelection);
                    setModel(context, getDoc());
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

    /**
     * Restituisce bene principale corrente nel flusso ricorsivo
     */
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

    /**
     * Restituisce accessori del bene corrente
     */
    public List<Inventario_beniBulk> getAccessoriCorrente(boolean isEliminazione) {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(isEliminazione);
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;

        if (bene != null && ps != null) {
            return ps.principaliConAccessori.get(bene);
        }
        return Collections.emptyList();
    }

    /**
     * Genera messaggio utente per scelta inclusione accessori
     */
    public String getMessaggioSingoloBene(boolean isEliminazione) {
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

    public int getTotaleBeniPrincipali(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        return (ps != null) ? ps.principaliConAccessori.size() : 0;
    }

    public int getIndexBeneCorrente(boolean isEliminazione) {
        int index = isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta;
        return index + 1;
    }

    /**
     * Elabora bene corrente nel flusso ricorsivo
     */
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



    /**
     * Aggiunge il bene principale e gli eventuali accessori
     * secondo la scelta dell’utente, evitando duplicazioni.
     */
    private void aggiungiBenesCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk benePrincipale = getBenePrincipaleCorrente(false);
        if (benePrincipale == null) return;

        try {
            OggettoBulk[] soloBenePrincipale = new OggettoBulk[]{benePrincipale};
            BitSet vuoto = new BitSet(1);
            BitSet selezionato = new BitSet(1);
            selezionato.set(0);

            modificaBeniConAccessoriComponente(context, soloBenePrincipale, vuoto, selezionato);

            PendingSelection pending = getPendingAdd();
            List<Inventario_beniBulk> accessoriDaAggiungere = new ArrayList<>();

            if (includiAccessori) {
                if (pending != null && pending.principaliConAccessori != null) {
                    List<Inventario_beniBulk> accessoriStrutturali =
                            pending.principaliConAccessori.get(benePrincipale);

                    if (accessoriStrutturali != null) {
                        for (Inventario_beniBulk accStrutturale : accessoriStrutturali) {
                            if (!beneAccNelDettaglio(accStrutturale) &&
                                    !contieneAccessorio(accessoriDaAggiungere, accStrutturale)) {
                                accessoriDaAggiungere.add(accStrutturale);
                            }
                        }
                    }
                }
            } else {
                if (pending != null && pending.accessori != null) {

                    List<Inventario_beniBulk> accessoriStrutturali = null;
                    if (pending.principaliConAccessori != null) {
                        accessoriStrutturali = pending.principaliConAccessori.get(benePrincipale);
                    }

                    for (Inventario_beniBulk accManuale : pending.accessori) {
                        if (isAccessorioDelPrincipale(accManuale, benePrincipale) &&
                                !beneAccNelDettaglio(accManuale) &&
                                !contieneAccessorio(accessoriDaAggiungere, accManuale)) {

                            accessoriDaAggiungere.add(accManuale);
                        }
                    }
                }
            }

            for (Inventario_beniBulk accessorio : accessoriDaAggiungere) {
                if (beneAccNelDettaglio(accessorio)) {
                    continue;
                }

                OggettoBulk[] soloAccessorio = new OggettoBulk[]{accessorio};
                BitSet vuotoAcc = new BitSet(1);
                BitSet selezionatoAcc = new BitSet(1);
                selezionatoAcc.set(0);

                modificaBeniConAccessoriComponente(context, soloAccessorio, vuotoAcc, selezionatoAcc);
            }

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }


    private boolean contieneAccessorio(List<Inventario_beniBulk> lista, Inventario_beniBulk candidato) {
        for (Inventario_beniBulk presente : lista) {
            if (presente.equalsByPrimaryKey(candidato)) {
                return true;
            }
        }
        return false;
    }




    private boolean isAccessorioDelPrincipale(Inventario_beniBulk accessorio,
                                              Inventario_beniBulk principale) {

        return accessorio.getNr_inventario().equals(principale.getNr_inventario())
                && accessorio.getProgressivo().equals(principale.getProgressivo());
    }




    /**
     * Aggiunge accessori non presenti in bulks[] (pagine successive)
     */
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

    /**
     * Passa al prossimo bene nel flusso ricorsivo
     */
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

    /**
     * Resetta operazione ricorsiva in corso
     */
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

    /**
     * Stampa documento PDF via HTTP response
     */
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

    /**
     * Genera stream PDF documento tramite Jasper
     */
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

    /**
     * Stampa documento su file system temporaneo
     */
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

    /**
     * Genera nome file PDF con timestamp e ID documento
     */
    private String getOutputFileNameOrdine(String reportName, Doc_trasporto_rientroBulk doc) {
        String fileName = preparaFileNamePerStampa(reportName);
        fileName = PDF_DATE_FORMAT.format(new Date()) + '_' + doc.recuperoIdDocAsString() + '_' + fileName;
        return fileName;
    }

    /**
     * Crea documento e inizializza PG per allegati
     */
    public void create(ActionContext context) throws BusinessProcessException {
        try {
            getModel().setToBeCreated();
            Doc_trasporto_rientroBulk docCreato = (Doc_trasporto_rientroBulk)
                    createComponentSession().creaConBulk(
                            context.getUserContext(),
                            getModel()
                    );
            setModel(context, docCreato);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Aggiorna documento con validazione allegati e prevenzione duplicati
     */
    @Override
    public void update(ActionContext context) throws BusinessProcessException {
        try {
            fillModel(context);
            setSkipAllegatiReload(false);

            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

            if (doc.getArchivioAllegati() != null) {
                List<AllegatoGenericoBulk> allegatiValidi = new ArrayList<>();

                for (AllegatoGenericoBulk allegato : doc.getArchivioAllegati()) {
                    if (allegato.getFile() != null ||
                            allegato.getStorageKey() != null ||
                            allegato.getCrudStatus() == OggettoBulk.NORMAL) {
                        allegatiValidi.add(allegato);
                    }
                }

                doc.getArchivioAllegati().clear();
                doc.getArchivioAllegati().addAll(allegatiValidi);
            }

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

            archiviaAllegati(context);

            getModel().setToBeUpdated();
            Doc_trasporto_rientroBulk docAggiornato = (Doc_trasporto_rientroBulk)
                    createComponentSession().modificaConBulk(
                            context.getUserContext(),
                            getModel()
                    );

            setModel(context, docAggiornato);

        } catch (ApplicationException e) {
            throw new BusinessProcessException(e);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Pulisce nome file per stampa (rimuove slash e estensione)
     */
    private String preparaFileNamePerStampa(String reportName) {
        String fileName = reportName;
        fileName = fileName.replace('/', '_').replace('\\', '_');
        if (fileName.startsWith("_"))
            fileName = fileName.substring(1);
        if (fileName.endsWith(".jasper"))
            fileName = fileName.substring(0, fileName.length() - 7);
        return fileName + ".pdf";
    }

    /**
     * Restituisce session component per operazioni su DB
     */
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

    /**
     * Elabora singolo bene con accessori nel flusso ricorsivo
     */
    public void elaboraBeneConAccessori(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {

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
     * Elabora eliminazione bene con/senza accessori
     */
    private void elaboraBenePerEliminazione(ActionContext context, Inventario_beniBulk beneCorrente,
                                            boolean includiAccessori) throws BusinessProcessException {
        try {
            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = pendingDelete.principaliConAccessori.get(beneCorrente);
                getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(
                        context.getUserContext(), getDoc(), beneCorrente, accessori);
            } else {
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{beneCorrente};
                getComp().eliminaDettagliSalvati(
                        context.getUserContext(), getDoc(), soloIlPrincipale);
            }
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Elabora aggiunta bene con validazione preventiva e gestione accessori
     */
    private void elaboraBenePerAggiunta(ActionContext context, Inventario_beniBulk beneCorrente,
                                        boolean includiAccessori) throws BusinessProcessException {
        try {
            List<Inventario_beniBulk> accessoriDaValidare = null;
            if (includiAccessori && pendingAdd.principaliConAccessori.containsKey(beneCorrente)) {
                accessoriDaValidare = pendingAdd.principaliConAccessori.get(beneCorrente);
            }

            validaBeneConAccessoriPerAggiunta(context, beneCorrente, accessoriDaValidare);

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

            BitSet oldSelection = new BitSet(pendingAdd.bulks.length);
            BitSet newSelection = new BitSet(pendingAdd.bulks.length);

            newSelection.set(indiceBenePrincipale);

            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = pendingAdd.principaliConAccessori.get(beneCorrente);

                if (accessori != null && !accessori.isEmpty()) {
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

            modificaBeniConAccessoriComponente(context, pendingAdd.bulks, oldSelection, newSelection);

            if (includiAccessori) {
                List<Inventario_beniBulk> accessori = pendingAdd.principaliConAccessori.get(beneCorrente);
                aggiungiAccessoriMancanti(context, accessori);
            }

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Valida bene e accessori prima dell'aggiunta ricorsiva
     */
    private void validaBeneConAccessoriPerAggiunta(
            ActionContext context,
            Inventario_beniBulk benePrincipale,
            List<Inventario_beniBulk> accessori)
            throws BusinessProcessException {

        try {
            List<Inventario_beniBulk> beniDaValidare = new ArrayList<>();
            beniDaValidare.add(benePrincipale);

            if (accessori != null && !accessori.isEmpty()) {
                beniDaValidare.addAll(accessori);
            }

            getComp().validaBeniNonInAltriDocumenti(
                    context.getUserContext(),
                    getDoc(),
                    beniDaValidare
            );

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Completa metadati allegato da CMIS (utente SIGLA e aspect firmato/altro)
     */
    @Override
    protected void completeAllegato(T allegato, StorageObject storageObject)
            throws ApplicationException {

        Optional.ofNullable(storageObject.<String>getPropertyValue(
                        "sigla_commons_aspect:utente_applicativo"))
                .ifPresent(allegato::setUtenteSIGLA);

        List<String> secondaryTypes = storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");

        if (secondaryTypes != null && !secondaryTypes.isEmpty()) {
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
                for (String secondaryType : secondaryTypes) {
                    if (secondaryType.equals("P:cm:titled") ||
                            secondaryType.contains("sigla_commons_aspect")) {
                        continue;
                    }

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

        super.completeAllegato(allegato, storageObject);
    }

    /**
     * Salva documento in stato DEFINITIVO con validazione
     */
    public void salvaDefinitivo(ActionContext context) throws BusinessProcessException {
        try {
            fillModel(context);
            archiviaAllegati(context);

            getModel().setToBeUpdated();
            Doc_trasporto_rientroBulk doc = getComp().salvaDefinitivo(
                    context.getUserContext(), getDoc());

            setModel(context, doc);
            commitUserTransaction();

            doc.setStato(Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
            doc.setCrudStatus(OggettoBulk.NORMAL);

            setStatus(VIEW);
            setMessage("Documento salvato in stato DEFINITIVO");
        } catch (ComponentException | RemoteException ex) {
            rollbackUserTransaction();
            throw handleException(ex);
        } catch (FillException e) {
            rollbackUserTransaction();
            throw new RuntimeException(e);
        }
    }

    /**
     * Valida campi obbligatori prima di accedere ai dettagli
     */
    public void validaDatiPerDettagli() throws ApplicationException {
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
                if (isNullOrEmpty(doc.getNominativoVettore())) {
                    throw new ApplicationException(
                            "Specificare il Nominativo del Vettore."
                    );
                }
            }

            if (doc.isRitiroIncaricato() || doc.isRitiroVettore()) {
                if (isNullOrEmpty(doc.getIndirizzo())) {
                    throw new ApplicationException(
                            "Specificare l'Indirizzo."
                    );
                }
                if (isNullOrEmpty(doc.getDestinazione())) {
                    throw new ApplicationException(
                            "Specificare la Destinazione."
                    );
                }
            }
        }
    }

    /**
     * Readonly campi anagrafici se documento non modificabile o non in edit
     */
    public boolean isAnagraficiReadonly() {
        return isDocumentoNonModificabile()
                || isVisualizzazione()
                || (!isInserting() && !isEditing());
    }

    /**
     * Elimina tutti i beni dai dettagli salvati
     */
    protected void eliminaTuttiBeniDaDettagli(ActionContext context)
            throws BusinessProcessException {
        try {
            getComp().eliminaTuttiDettagliSalvati(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    /**
     * Elimina beni specifici dai dettagli salvati
     */
    public void eliminaBeniDaDettagli(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaDettagliSalvati(context.getUserContext(), getDoc(), details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Elimina bene principale corrente con tutti gli accessori
     */
    private void eliminaBeneCorrente(ActionContext context) throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);
        List<Inventario_beniBulk> accessori = getAccessoriCorrente(true);

        if (bene != null && accessori != null) {
            try {
                getComp().eliminaBeniPrincipaleConAccessoriDaDettagli(
                        context.getUserContext(), getDoc(), bene, accessori);

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    /**
     * Elimina solo bene principale senza accessori
     */
    private void eliminaBenePrincipaleSenzaAccessori(ActionContext context)
            throws BusinessProcessException {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(true);

        if (bene != null) {
            try {
                OggettoBulk[] soloIlPrincipale = new OggettoBulk[]{bene};

                getComp().eliminaDettagliSalvati(
                        context.getUserContext(), getDoc(), soloIlPrincipale);

            } catch (ComponentException | RemoteException e) {
                throw handleException(e);
            }
        }
    }

    /**
     * Carica TerzoBulk completo da AnagraficoBulk tramite component
     */
    public TerzoBulk caricaTerzoDaAnagrafico(ActionContext context,
                                             AnagraficoBulk anagrafico)
            throws BusinessProcessException {

        if (anagrafico == null || anagrafico.getCd_anag() == null) {
            return null;
        }

        try {
            return getComp().caricaTerzoDaAnagrafico(
                    context.getUserContext(),
                    anagrafico.getCd_anag()
            );

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private boolean beneAccNelDettaglio(Inventario_beniBulk bene) {
        if (getDoc().getDoc_trasporto_rientro_dettColl() == null) return false;

        for (Object o : getDoc().getDoc_trasporto_rientro_dettColl()) {
            Doc_trasporto_rientro_dettBulk dett = (Doc_trasporto_rientro_dettBulk) o;

            if (dett.getNr_inventario().equals(bene.getNr_inventario()) &&
                    dett.getProgressivo().equals(bene.getProgressivo().intValue())) {
                return true;
            }
        }
        return false;
    }

}