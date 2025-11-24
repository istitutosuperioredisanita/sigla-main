package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoDettBulk;
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
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP<AllegatoDocumentoTrasportoBulk, DocumentoTrasportoBulk>{

    public CRUDTrasportoBeniInvBP() {
        super();
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

    @Override
    protected String getStorePath(DocumentoTrasportoBulk documentoTrasportoBulk, boolean create) throws BusinessProcessException {
        return documentoTrasportoBulk.getStorePath().get(0);
    }


    @Override
    protected Class getAllegatoClass() {
        return it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk.class;
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

    @Override
    Class getDocumentoClassDett() {
        return DocumentoTrasportoDettBulk.class;
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




}
