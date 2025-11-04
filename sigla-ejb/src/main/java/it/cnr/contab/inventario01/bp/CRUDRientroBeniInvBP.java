package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario00.bp.RigheInvDaFatturaCRUDController;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.action.SelectionListener;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.STATO_INSERITO;

/**
 * Business Process per la gestione dei Documenti di Rientro.
 * <p>
 * CARATTERISTICHE SPECIFICHE PER RIENTRO:
 * - I beni devono provenire da documenti di TRASPORTO FIRMATI
 * - Ogni bene può rientrare una sola volta
 * - Collegamento obbligatorio con documento di trasporto di riferimento
 * - Gestione accessori identica al trasporto
 */
public class CRUDRientroBeniInvBP extends CRUDTraspRientInventarioBP implements SelectionListener {

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

    // Getter/Setter per indici
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

    public static final String DOC_RIENTRO = "Documenti Di Rientro";

    public CRUDRientroBeniInvBP() {
        super();
        setTab("tab", "tabRientroTestata");
    }

    public CRUDRientroBeniInvBP(String function) {
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
            "DettagliRientro",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            if (isInserting() || isEditing() && !isDocumentoAnnullato()) {
                try {
                    return selectDettagliRientrobyClause(context);
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
            eliminaDettagliRientroConBulk(context);
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
                    eliminaDettagliRientroConBulk(context, details);
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
            "EditDettagliRientro",
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
            this) {

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext context) {
            if (isInserting() || isDocumentoAnnullato()) {
                return new it.cnr.jada.util.EmptyRemoteIterator();
            }
            try {
                return selectEditDettagliRientrobyClause(context);
            } catch (BusinessProcessException e) {
                throw new RuntimeException("Errore caricamento dettagli modifica", e);
            }
        }
    };

    // ========================= GESTIONE DETTAGLI =========================

    private void eliminaDettagliRientroConBulk(ActionContext context, OggettoBulk[] details)
            throws BusinessProcessException {
        try {
            getComp().eliminaBeniAssociati(context.getUserContext(), getDoc(), details);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private void eliminaDettagliRientroConBulk(ActionContext context) throws BusinessProcessException {
        try {
            getComp().eliminaTuttiBeniAssociati(context.getUserContext(), getDoc());
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    private RemoteIterator selectEditDettagliRientrobyClause(ActionContext context)
            throws BusinessProcessException {
        try {
            it.cnr.jada.persistency.sql.CompoundFindClause filters =
                    ((RemoteDetailCRUDController) getEditDettController()).getFilter();
            return getComp().selectEditDettagliRientro(context.getUserContext(), getDoc(),
                    Doc_trasporto_rientro_dettBulk.class, filters);
        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    private RemoteIterator selectDettagliRientrobyClause(ActionContext context)
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

    /**
     * METODO SPECIFICO PER RIENTRO:
     * Carica solo i beni presenti in documenti di TRASPORTO FIRMATI
     */
    public RemoteIterator getListaBeniDaFarRientrare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaFarRientrare(userContext, getDoc(), beni_da_escludere, clauses);
    }

    // ========================= INIZIALIZZAZIONE MODELLI =========================

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk) bulk;
        testata.setTiDocumento(RIENTRO);
        return super.initializeModelForEdit(context, testata);
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocRientro(super.initializeModelForInsert(context, bulk));
    }

    @Override
    public OggettoBulk initializeModelForFreeSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocRientro(super.initializeModelForFreeSearch(context, bulk));
    }

    @Override
    public OggettoBulk initializeModelForSearch(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        return initializeDocRientro(super.initializeModelForSearch(context, bulk));
    }

    private OggettoBulk initializeDocRientro(OggettoBulk bulk) {
        ((Doc_trasporto_rientroBulk) bulk).setTiDocumento(RIENTRO);
        return bulk;
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    public void resetTabs() {
        setTab("tab", "tabRientroTestata");
    }

    // ========================= VISIBILITÀ UI =========================

    public boolean isDocumentoPredispostoAllaFirma() {
        return getDoc() != null && getDoc().isPredispostoAllaFirma();
    }

    public boolean isDocumentoNonModificabile() {
        return isDocumentoAnnullato() || isDocumentoPredispostoAllaFirma();
    }

    public boolean isPredispostoAllaFirma() {
        return getDoc() != null && getDoc().isPredispostoAllaFirma();
    }

    public boolean isBottoneAggiungiBeneEnabled() {
        return isInserting() && getDoc() != null && getDoc().isInserito() && !isDocumentoAnnullato();
    }

    public String getLabelData_registrazione() {
        return "Data Rientro";
    }

    // ========================= WORKFLOW E STATO =========================

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
            getComp().annullaModificaRientroBeni(context.getUserContext());
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
            getComp().inizializzaBeniDaFarRientrare(context.getUserContext());
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
            getComp().rientraTuttiBeni(context.getUserContext(), getDoc(), getClauses());
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
                    getComp().modificaBeniRientratiConAccessori(context.getUserContext(), getDoc(),
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
            msg.append("Vuoi includere anche ").append(articoloFinale).append(" nel rientro?");
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
            eliminaBeneCorrente(context);
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

    private void aggiungiBenesCorrente(ActionContext context, boolean includiAccessori)
            throws BusinessProcessException {

        Inventario_beniBulk bene = getBenePrincipaleCorrente(false);
        if (bene == null) return;

        try {
            BitSet tempSelection = new BitSet(pendingAdd.bulks.length);

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

            getComp().modificaBeniRientratiConAccessori(
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

    // ========================= HELPER & GETTERS =========================

    private DocTrasportoRientroComponentSession getComp() throws BusinessProcessException {
        return (DocTrasportoRientroComponentSession) createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
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