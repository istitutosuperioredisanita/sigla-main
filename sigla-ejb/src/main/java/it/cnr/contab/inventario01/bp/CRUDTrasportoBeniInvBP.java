package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario00.bp.RigheInvDaFatturaCRUDController;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.Trasferimento_inventarioBulk;
import it.cnr.contab.inventario01.bulk.Buono_carico_scaricoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.STATO_INSERITO;

/**
 * Business Process per la gestione dei Documenti di Trasporto/Rientro.
 * Gestisce le operazioni CRUD e il flusso di lavoro (workflow) con logica ricorsiva unificata
 * per eliminazione e aggiunta di beni principali con accessori.
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP implements SelectionListener {

    private it.cnr.jada.persistency.sql.CompoundFindClause clauses;

    /**
     * Contenitore helper per incapsulare tutti i dati di selezione "in sospeso" (pending).
     */
    private static class PendingSelection {
        Map<Inventario_beniBulk, List<Inventario_beniBulk>> principaliConAccessori = new LinkedHashMap<>();
        List<Inventario_beniBulk> accessori = new ArrayList<>();
        List<Inventario_beniBulk> principaliSenza = new ArrayList<>();
        OggettoBulk[] bulks = null;
        BitSet oldSel = null;
        BitSet newSel = null;

        void clear() {
            principaliConAccessori.clear();
            accessori.clear();
            principaliSenza.clear();
            bulks = null;
            oldSel = null;
            newSel = null;
        }

        boolean isEmpty() {
            return principaliConAccessori.isEmpty() && accessori.isEmpty() && principaliSenza.isEmpty();
        }
    }

    private PendingSelection pendingAdd = null;
    private PendingSelection pendingDelete = null;

    // ==================== INDICI PER FLUSSI RICORSIVI ====================
    private int indexBeneCurrentePerEliminazione = 0;
    private int indexBeneCurrentePerAggiunta = 0;
    private boolean ultimaOperazioneEliminazione = false;

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

    public static final String DOC_T_R = "Documenti Di";

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

    // ========================= CONTROLLERS =========================

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
                    eliminaDettagliTrasportoConBulk(context, details);
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

    // ========================= GESTIONE DETTAGLI =========================

    private void eliminaDettagliTrasportoConBulk(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaDettagliTrasportoConBulk(ActionContext context) throws BusinessProcessException {
        try {
            getComp().eliminaTuttiBeniAssociati(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    private RemoteIterator selectEditDettagliTrasportobyClause(ActionContext context)
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

    public RemoteIterator getListaBeniDaTrasportare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(userContext, getDoc(), beni_da_escludere, clauses);
    }

    // ========================= INIZIALIZZAZIONE MODELLI =========================

//    @Override
//    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
//            throws BusinessProcessException {
//        bulk = super.initializeModelForEdit(context, bulk);
//        bulk = initializeDocTrasporto(bulk);
//
//        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bulk;
//        if (doc.isAnnullato()) {
//            setStatus(VIEW);
//            setMessage("ATTENZIONE: Il DDT è stato ANNULLATO");
//        }
//
//        return bulk;
//    }

    public OggettoBulk initializeModelForEdit(ActionContext context,OggettoBulk bulk) throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk)bulk;
        testata.setTiDocumento(TRASPORTO);
        try {
            bulk = super.initializeModelForEdit(context, testata);
            return bulk;
        } catch(Throwable e) {
            throw new it.cnr.jada.action.BusinessProcessException(e);
        }

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

    // ========================= VISIBILITÀ UI =========================

    private boolean isDocumentoAnnullato() {
        return getDoc() != null && getDoc().isAnnullato();
    }

    public boolean isVisualizzazione() {
        return isDocumentoAnnullato();
    }

    public boolean isBottoneAggiungiBeneEnabled() {
        return isInserting() && getDoc() != null && getDoc().isInserito() && !isDocumentoAnnullato();
    }

    public boolean isDeleteButtonEnabled() {
        return !isVisualizzazione();
    }

    @Override
    public boolean isEditable() {
        if (isDocumentoAnnullato()) return false;
        return !isVisualizzazione() && super.isEditable();
    }

    public String getLabelData_registrazione() {
        return "Data Trasporto";
    }

    // ========================= WORKFLOW E STATO =========================

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
            getComp().annullaModificaTrasportoBeni(context.getUserContext());
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
            getComp().inizializzaBeniDaTrasportare(context.getUserContext());
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public void selectAll(ActionContext context) throws BusinessProcessException {
        if (isDocumentoAnnullato()) throw new BusinessProcessException("Impossibile modificare un documento annullato");
        try {
            getComp().trasportaTuttiBeni(context.getUserContext(), getDoc(), getClauses());
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
                    getComp().modificaBeniTrasportatiConAccessori(context.getUserContext(), getDoc(),
                            bulks, oldSelection, newSelection);
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

    // ========================= FLUSSO RICORSIVO UNIFICATO =========================

    /**
     * Restituisce il bene principale corrente per l'operazione specificata
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
     * Restituisce gli accessori del bene principale corrente
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
     * Genera il messaggio per il bene principale corrente
     */
    public String getMessaggioSingoloBene(boolean isEliminazione) {
        Inventario_beniBulk bene = getBenePrincipaleCorrente(isEliminazione);
        if (bene == null) {
            return "";
        }

        List<Inventario_beniBulk> accessori = getAccessoriCorrente(isEliminazione);
        int numAccessori = (accessori != null) ? accessori.size() : 0;

        StringBuilder msg = new StringBuilder();

        if (isEliminazione) {
            msg.append("Il bene principale con codice: ").append(bene.getNr_inventario());
            if (bene.getEtichetta() != null && !bene.getEtichetta().isEmpty()) {
                msg.append(" e etichetta ").append(bene.getEtichetta());
            }
            msg.append(" ha ").append(numAccessori);
            String pluraleAccessorio = (numAccessori == 1) ? " bene accessorio" : " beni accessori";
            msg.append(pluraleAccessorio).append(".\n\n");
            msg.append("Eliminare il bene principale con i relativi beni accessori?");
        } else {
            msg.append("La selezione include il bene principale con codice: ").append(bene.getNr_inventario());
            if (bene.getEtichetta() != null && !bene.getEtichetta().isEmpty()) {
                msg.append(" e etichetta ").append(bene.getEtichetta());
            }
            msg.append(" che ha ").append(numAccessori);
            String pluraleAccessorio = (numAccessori == 1) ? " bene accessorio" : " beni accessori";
            String verbo = (numAccessori == 1) ? "associato" : "associati";
            msg.append(pluraleAccessorio).append(" ").append(verbo).append(".\n\n");
            String articoloFinale = (numAccessori == 1) ? "questo accessorio" : "questi accessori";
            msg.append("Vuoi includere anche ").append(articoloFinale).append(" nell'aggiunta?");
        }

        return msg.toString();
    }

    /**
     * Restituisce il numero totale di beni principali con accessori
     */
    public int getTotaleBeniPrincipali(boolean isEliminazione) {
        PendingSelection ps = isEliminazione ? pendingDelete : pendingAdd;
        return (ps != null) ? ps.principaliConAccessori.size() : 0;
    }

    /**
     * Restituisce l'indice corrente (per info all'utente: "Bene 1 di 3")
     */
    public int getIndexBeneCorrente(boolean isEliminazione) {
        int index = isEliminazione ? indexBeneCurrentePerEliminazione : indexBeneCurrentePerAggiunta;
        return index + 1;
    }

    /**
     * Elabora il bene principale corrente
     */
    public void elaboraBeneCorrente(ActionContext context, boolean isEliminazione, boolean includiAccessori)
            throws BusinessProcessException {

        if (isEliminazione) {
            eliminaBeneCorrente(context);
        } else {
            aggiungiBenesCorrente(context, includiAccessori);
        }
    }

    /**
     * Elimina il bene principale corrente e i suoi accessori
     */
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
     * Aggiunge il bene principale corrente (con o senza accessori)
     */
    private void aggiungiBenesCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk bene = getBenePrincipaleCorrente(false);
        if (bene == null) return;

        try {
            BitSet tempSelection = new BitSet(pendingAdd.bulks.length);

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

            // Se richiesto, seleziona anche gli accessori
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

            // Applica la modifica
            getComp().modificaBeniTrasportatiConAccessori(
                    context.getUserContext(),
                    getDoc(),
                    pendingAdd.bulks,
                    pendingAdd.oldSel,
                    tempSelection);
            getDettBeniController().reset(context);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    /**
     * Avanza al prossimo bene principale
     */
    public boolean passaAlProssimoBene(boolean isEliminazione) {
        if (isEliminazione) {
            indexBeneCurrentePerEliminazione++;
            return indexBeneCurrentePerEliminazione < getTotaleBeniPrincipali(true);
        } else {
            indexBeneCurrentePerAggiunta++;
            return indexBeneCurrentePerAggiunta < getTotaleBeniPrincipali(false);
        }
    }

    /**
     * Reimposta l'indice e pulisce i dati pendenti
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

    public void annullaModificaBeniConAccessori() {
        if (pendingAdd != null) pendingAdd.clear();
        pendingAdd = null;
    }

    public void annullaEliminazioneBeniMultipli() {
        if (pendingDelete != null) pendingDelete.clear();
        pendingDelete = null;
    }

    // ========================= GETTER PER STATISTICHE =========================

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

    public String getNomeBenePrincipalePendente() {
        if (pendingAdd == null || pendingAdd.bulks == null) return "Bene";
        for (int i = 0; i < pendingAdd.bulks.length; i++) {
            if (pendingAdd.newSel != null && pendingAdd.oldSel != null &&
                    pendingAdd.newSel.get(i) && !pendingAdd.oldSel.get(i)) {
                Inventario_beniBulk b = (Inventario_beniBulk) pendingAdd.bulks[i];
                return b.getNumeroBeneCompleto();
            }
        }
        return "Bene";
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

    @Override
    public boolean isPrintButtonHidden() {
        return !Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .flatMap(d -> Optional.ofNullable(d.getPgDocTrasportoRientro()))
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

        addPrintParam(printbp, "esercizio", Optional.ofNullable(docT.getEsercizio()).map(String::valueOf).orElse(null), Integer.class);
        addPrintParam(printbp, "pgInventario", Optional.ofNullable(docT.getPgInventario()).map(String::valueOf).orElse(null), Integer.class);
        addPrintParam(printbp, "tiDocumento", docT.getTiDocumento(), String.class);
        addPrintParam(printbp, "pgDocTrasportoRientro", Optional.ofNullable(docT.getPgDocTrasportoRientro()).map(String::valueOf).orElse(null), Integer.class);
    }

    private void addPrintParam(OfflineReportPrintBP printbp, String name, String value, Class<?> type) {
        Print_spooler_paramBulk param = new Print_spooler_paramBulk();
        param.setNomeParam(name);
        param.setValoreParam(value);
        param.setParamType(type.getCanonicalName());
        printbp.addToPrintSpoolerParam(param);
    }

    // ========================= HELPER & GETTERS =========================

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
}