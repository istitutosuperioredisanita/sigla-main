package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario01.bp.CRUDTraspRientInventarioBP;
import it.cnr.contab.inventario01.bp.CRUDRientroBeniInvBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

/**
 * Action per la gestione del flusso di RIENTRO Beni.
 * <p>
 * Contiene SOLO le specifiche del Rientro:
 * - Cast al BP corretto
 * - Chiamata specifica per beni da far rientrare (solo da doc. trasporto FIRMATI)
 * - Nomi metodi callback specifici
 * - Messaggi specifici
 * <p>
 * CARATTERISTICHE SPECIFICHE RIENTRO:
 * - Validazione esistenza documento di trasporto firmato
 * - Controllo che i beni non siano già rientrati
 * - Collegamento automatico con documento di trasporto di riferimento
 */
public class CRUDRientroDocAction extends CRUDTraspRientDocAction {

    public CRUDRientroDocAction() {
        super();
    }

    // =======================================================
    // IMPLEMENTAZIONE METODI ASTRATTI
    // =======================================================

    @Override
    protected CRUDTraspRientInventarioBP getBP(ActionContext context) {
        return (CRUDRientroBeniInvBP) getBusinessProcess(context);
    }

    @Override
    protected RemoteIterator getListaBeni(
            ActionContext context,
            CRUDTraspRientInventarioBP bp,
            Doc_trasporto_rientroBulk doc,
            SimpleBulkList selezionati,
            CompoundFindClause clauses) throws Exception {

        // SPECIFICO RIENTRO: cerca solo beni da doc. trasporto FIRMATI
        return getComponentSession(bp).getListaBeniDaFarRientrare(
                context.getUserContext(),
                doc,
                selezionati,
                clauses);
    }

    @Override
    protected String getSelezionaMethod() {
        return "doSelezionaBeniRientro";
    }

    @Override
    protected String getBringBackMethod() {
        return "doBringBackAddBeniRientro";
    }

    @Override
    protected String getMessageNoResults() {
        return "Nessun Bene recuperato da documenti di trasporto firmati.";
    }

    @Override
    protected String getDataLabel() {
        return "data rientro";
    }

    @Override
    protected String getTabTestataName() {
        return "tabRientroTestata";
    }

    // =======================================================
    // CALLBACK SPECIFICI (richiamano metodi generici)
    // =======================================================

    public Forward doBringBackAddBeniRientro(ActionContext context) {
        return doBringBackGeneric(context);
    }

    public Forward doSelezionaBeniRientro(ActionContext context) {
        return doSelezionaBeniGeneric(context);
    }
}
