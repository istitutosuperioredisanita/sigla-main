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

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        Doc_trasporto_rientroBulk testata = (Doc_trasporto_rientroBulk) bulk;
        testata.setTiDocumento(TRASPORTO);
        return super.initializeModelForEdit(context, testata);
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

    /**
     * Imposta tiDocumento SUBITO alla creazione del bulk
     */
    @Override
    public OggettoBulk createNewBulk(it.cnr.jada.action.ActionContext context)
            throws BusinessProcessException {
        Doc_trasporto_rientroBulk doc = new Doc_trasporto_rientroBulk();
        doc.setTiDocumento(TRASPORTO);
        return doc;
    }




}
