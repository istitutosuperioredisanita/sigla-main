package it.cnr.contab.inventario01.bp;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/** Selezionatore comune beni di inventario. */
public class SelezionatoreBeniInventarioBP extends SelezionatoreListaBP {

    /** Nome del BP registrato in main.xml. */
    public static final String BP_NAME = "SelezionatoreBeniInventario";

    /** Parametro hook per la ricerca libera iniziale ("+"). */
    public static final String PARAM_RICERCA_INIZIALE = "ricercaBeniIniziale";

    /** Ricerca libera del filtro beni. */
    private RicercaLiberaBP ricercaFiltroBeni;

    /** Clausole del filtro applicato (null = nessun filtro). */
    private CompoundFindClause clausesFiltro;

    public SelezionatoreBeniInventarioBP() {
        super();
    }

    public SelezionatoreBeniInventarioBP(String s) {
        super(s);
    }

    /** Restituisce il provider padre del selezionatore. */
    public SelezionatoreBeniProvider getProvider() {
        return (SelezionatoreBeniProvider) getParent();
    }

    public RicercaLiberaBP getRicercaFiltroBeni() {
        return ricercaFiltroBeni;
    }

    public void setRicercaFiltroBeni(RicercaLiberaBP ricercaFiltroBeni) {
        this.ricercaFiltroBeni = ricercaFiltroBeni;
    }

    public CompoundFindClause getClausesFiltro() {
        return clausesFiltro;
    }

    public void setClausesFiltro(CompoundFindClause clausesFiltro) {
        this.clausesFiltro = clausesFiltro;
    }

    /** Collega la ricerca iniziale agli hook di ritorno. */
    public static void collegaRicercaIniziale(RicercaLiberaBP rlbp, HookForward... hooks) {
        for (HookForward hook : hooks) {
            if (hook != null) {
                hook.addParameter(PARAM_RICERCA_INIZIALE, rlbp);
            }
        }
    }

    /** Imposta il filtro iniziale basato sulla ricerca. */
    public void impostaFiltroIniziale(HookForward caller, CompoundFindClause clauses) {
        setClausesFiltro(clauses);
        Object ricerca = caller == null ? null : caller.getParameter(PARAM_RICERCA_INIZIALE);
        if (ricerca instanceof RicercaLiberaBP) {
            setCondizioneCorrente(((RicercaLiberaBP) ricerca).getCondizioneRadice());
        }
    }

    /** Crea e configura il selezionatore in multi-selezione. */
    public static SelezionatoreBeniInventarioBP crea(
            ActionContext context,
            SelezionatoreBeniProvider provider,
            RemoteIterator iterator)
            throws BusinessProcessException, RemoteException {

        SelezionatoreBeniInventarioBP slbp =
                (SelezionatoreBeniInventarioBP) context.createBusinessProcess(BP_NAME);

        BulkInfo bulkInfo = BulkInfo.getBulkInfo(provider.getClasseBeniSelezionatore());

        slbp.setSelectionListener(context, provider);
        slbp.setIterator(context, iterator);
        slbp.setBulkInfo(bulkInfo);

        String columnSet = provider.getColumnSetSelezionatore();
        slbp.setColumns(columnSet == null
                ? bulkInfo.getColumnFieldPropertyDictionary()
                : bulkInfo.getColumnFieldPropertyDictionary(columnSet));

        slbp.setMultiSelection(true);

        return slbp;
    }

    /** Configura la toolbar con esportazione, filtri e selezione beni. */
    @Override
    public List<Button> createToolbarList() {
        List<Button> buttons = new ArrayList<>();

        for (String name : new String[]{
                "excel", "filtraBeni", "multiSelection",
                "selectAll", "deselectAll", "hiddenColumn"}) {
            buttons.add(new Button(
                    Config.getHandler().getProperties(getClass()),
                    "Toolbar." + name));
        }

        setMostraHideColumns(true);
        return buttons;
    }
}