package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.BitSet;

/**
 * Business Process per la gestione dei Documenti di TRASPORTO.
 * <p>
 * Contiene SOLO le specifiche del Trasporto:
 * - Tipo documento = "T"
 * - Nomi controller specifici
 * - Chiamate component session specifiche per trasporto
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP {

    public CRUDTrasportoBeniInvBP() {
        super();
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }


    // ==================== IMPLEMENTAZIONE METODI ASTRATTI ====================

    @Override
    protected String getDettagliControllerName() {
        return "DettagliTrasporto";
    }

    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliTrasporto";
    }

    @Override
    protected String getMainTabName() {
        return "tabTrasportoTestata";
    }

    @Override
    public String getLabelData_registrazione() {
        return "Data Trasporto";
    }

    @Override
    protected void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().inizializzaBeniDaTrasportare(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().annullaModificaTrasportoBeni(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void selezionaTuttiBeniComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().trasportaTuttiBeni(context.getUserContext(), getDoc(), getClauses());
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
            getComp().modificaBeniTrasportatiConAccessori(
                    context.getUserContext(),
                    getDoc(),
                    bulks,
                    oldSelection,
                    newSelection);
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== METODO SPECIFICO TRASPORTO ====================

    /**
     * Ottiene lista beni DA TRASPORTARE (qualsiasi bene inventariato)
     */
    public RemoteIterator getListaBeniDaTrasportare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(userContext, getDoc(), beni_da_escludere, clauses);
    }

    // ==================== INIZIALIZZAZIONE MODELLI ====================

    protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {

        super.init(config,context);
        resetTabs();
    }

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
    public OggettoBulk initializeModelForInsert(ActionContext context,OggettoBulk bulk) throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk)bulk;
        testata.setTiDocumento(TRASPORTO);
        bulk = super.initializeModelForInsert(context, testata);
        return bulk;
    }
    public OggettoBulk initializeModelForFreeSearch(
            ActionContext actioncontext,
            OggettoBulk oggettobulk)
            throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk)oggettobulk;
        testata.setTiDocumento(TRASPORTO);
        oggettobulk = super.initializeModelForFreeSearch(actioncontext, testata);
        return oggettobulk;
    }

    public OggettoBulk initializeModelForSearch(ActionContext context,OggettoBulk bulk) throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk)bulk;
        testata.setTiDocumento(TRASPORTO);
        bulk = super.initializeModelForSearch(context, testata);
        return bulk;
    }
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }


}
