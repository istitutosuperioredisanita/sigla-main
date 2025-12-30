package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bp.CRUDTrasportoBeniInvBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

/**
 * Action per la gestione del flusso di TRASPORTO Beni.
 * <p>
 * Contiene SOLO le specifiche del Trasporto:
 * - Cast al BP corretto
 * - Chiamata specifica per beni trasportabili (qualsiasi bene inventariato)
 * - Nomi metodi callback specifici
 * - Messaggi specifici
 */
public class CRUDTrasportoDocAction extends CRUDTraspRientDocAction {

    public CRUDTrasportoDocAction() {
        super();
    }

    // =======================================================
    // IMPLEMENTAZIONE METODI ASTRATTI
    // =======================================================

    @Override
    protected CRUDTraspRientInventarioBP getBP(ActionContext context) {
        return (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
    }

    @Override
    protected RemoteIterator getListaBeni(
            ActionContext context,
            CRUDTraspRientInventarioBP bp,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList selezionati,
            CompoundFindClause clauses) throws Exception {

        // SPECIFICO TRASPORTO: cerca qualsiasi bene inventariato
        return getComponentSession(bp).cercaBeniTrasportabili(
                context.getUserContext(),
                doc,
                selezionati,
                clauses);
    }

    @Override
    protected String getSelezionaMethod() {
        return "doSelezionaBeniTrasporto";
    }

    @Override
    protected String getBringBackMethod() {
        return "doBringBackAddBeniTrasporto";
    }

    @Override
    protected String getMessageNoResults() {
        return "Nessun Bene recuperato.";
    }

    @Override
    protected String getDataLabel() {
        return "data trasporto";
    }

    @Override
    protected String getTabTestataName() {
        return "tabTrasportoTestata";
    }

    // =======================================================
    // CALLBACK SPECIFICI (richiamano metodi generici)
    // =======================================================

    public Forward doBringBackAddBeniTrasporto(ActionContext context) {
        return doBringBackGeneric(context);
    }

    public Forward doSelezionaBeniTrasporto(ActionContext context) {
        return doSelezionaBeniGeneric(context);
    }
}

