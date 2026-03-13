
package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bp.CRUDTrasportoBeniInvBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

/*
 * Action dedicata alla gestione del flusso di Trasporto Beni.
 *
 * Specifiche incluse:
 * - Cast al Business Process corretto (CRUDTrasportoBeniInvBP)
 * - Recupero beni trasportabili (qualsiasi bene inventariato)
 * - Callback specifici per la selezione/aggiunta beni
 * - Messaggi e label specifici del processo Trasporto
 */
public class CRUDTrasportoDocAction extends CRUDTraspRientDocAction {

    public CRUDTrasportoDocAction() {
        super();
    }

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

    public Forward doBringBackAddBeniTrasporto(ActionContext context) {
        return doBringBackGeneric(context);
    }

    public Forward doSelezionaBeniTrasporto(ActionContext context) {
        return doSelezionaBeniGeneric(context);
    }
}
