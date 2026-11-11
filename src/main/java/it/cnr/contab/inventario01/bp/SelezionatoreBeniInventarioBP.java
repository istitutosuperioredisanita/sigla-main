
package it.cnr.contab.inventario01.bp;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Selezionatore comune dei beni di inventario, utilizzato dai diversi
 * documenti tramite l'interfaccia SelezionatoreBeniProvider.
 */
public class SelezionatoreBeniInventarioBP extends SelezionatoreListaBP {

    /** Nome del BP registrato in main.xml. */
    public static final String BP_NAME = "SelezionatoreBeniInventario";

    public SelezionatoreBeniInventarioBP() {
        super();
    }

    public SelezionatoreBeniInventarioBP(String s) {
        super(s);
    }

    /**
     * Restituisce il BP del documento che ha aperto il selezionatore.
     */
    public SelezionatoreBeniProvider getProvider() {
        return (SelezionatoreBeniProvider) getParent();
    }

    /**
     * Crea e configura il selezionatore in modalità multi-selezione,
     * utilizzando l'iteratore e le informazioni Bulk forniti dal documento.
     */
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

    /**
     * Configura la toolbar con le funzioni di esportazione,
     * filtraggio e gestione della selezione dei beni.
     */
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
