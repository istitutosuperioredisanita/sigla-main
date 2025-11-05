package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.BitSet;

/**
 * Business Process per la gestione dei Documenti di RIENTRO.
 * <p>
 * Contiene SOLO le specifiche del Rientro:
 * - Tipo documento = "R"
 * - Nomi controller specifici
 * - Chiamate component session specifiche per rientro
 * <p>
 * CARATTERISTICHE SPECIFICHE PER RIENTRO:
 * - I beni devono provenire da documenti di TRASPORTO FIRMATI
 * - Ogni bene può rientrare una sola volta
 * - Collegamento obbligatorio con documento di trasporto di riferimento
 */
public class CRUDRientroBeniInvBP extends CRUDTraspRientInventarioBP {

    public CRUDRientroBeniInvBP() {
        super();
    }

    public CRUDRientroBeniInvBP(String function) {
        super(function);
    }

    // ==================== IMPLEMENTAZIONE METODI ASTRATTI ====================

    @Override
    protected String getDettagliControllerName() {
        return "DettagliRientro";
    }

    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliRientro";
    }

    @Override
    protected String getMainTabName() {
        return "tabRientroTestata";
    }

    @Override
    public String getLabelData_registrazione() {
        return "Data Rientro";
    }

    @Override
    protected void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().inizializzaBeniDaFarRientrare(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().annullaModificaRientroBeni(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void selezionaTuttiBeniComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().rientraTuttiBeni(context.getUserContext(), getDoc(), getClauses());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modificaBeniConAccessoriComponente(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection) throws ComponentException, RemoteException {
        try {
            getComp().modificaBeniRientratiConAccessori(
                    context.getUserContext(),
                    getDoc(),
                    bulks,
                    oldSelection,
                    newSelection);
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== METODO SPECIFICO RIENTRO ====================

    /**
     * Ottiene lista beni DA FAR RIENTRARE (solo da doc. trasporto FIRMATI)
     */
    public RemoteIterator getListaBeniDaFarRientrare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaFarRientrare(userContext, getDoc(), beni_da_escludere, clauses);
    }

    // ==================== INIZIALIZZAZIONE MODELLI ====================

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
}
