
package it.cnr.contab.inventario01.bp;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelectionListener;

/**
 * Contratto per i BP dei documenti di inventario che utilizzano
 * il selezionatore beni comune.
 */
public interface SelezionatoreBeniProvider extends SelectionListener {

    /**
     * Restituisce i beni candidati alla selezione, applicando
     * le eventuali clausole di ricerca.
     */
    RemoteIterator selectBeniCandidatiByClause(
            ActionContext context, CompoundFindClause clauses)
            throws BusinessProcessException;

    /**
     * Configura la ricerca libera utilizzata per filtrare i beni.
     */
    void configuraRicercaBeni(
            ActionContext context, RicercaLiberaBP rlbp)
            throws BusinessProcessException;

    /**
     * Restituisce la classe Bulk dei beni visualizzati nella griglia.
     */
    Class<? extends OggettoBulk> getClasseBeniSelezionatore();

    /**
     * Restituisce il ColumnSet della griglia.
     * Se null, vengono utilizzate le colonne predefinite.
     */
    default String getColumnSetSelezionatore() {
        return null;
    }

    /**
     * Memorizza le clausole di ricerca correnti.
     */
    void setClauses(CompoundFindClause clauses);
}
