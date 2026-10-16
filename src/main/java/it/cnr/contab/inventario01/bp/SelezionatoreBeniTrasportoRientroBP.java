package it.cnr.contab.inventario01.bp;

import it.cnr.jada.util.Config;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.jsp.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Selezionatore dei beni per Trasporto/Rientro.
 * Il filtro (Filtra beni) è gestito da SelezionatoreBeniTrasportoRientroAction.
 */
public class SelezionatoreBeniTrasportoRientroBP extends SelezionatoreListaBP {

    public SelezionatoreBeniTrasportoRientroBP() {
        super();
    }

    public SelezionatoreBeniTrasportoRientroBP(String s) {
        super(s);
    }

    /** Il BP del documento è il parent di questo selezionatore. */
    public CRUDTraspRientInventarioBP getDocumentoBP() {
        return (CRUDTraspRientInventarioBP) getParent();
    }

    @Override
    public List<Button> createToolbarList() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.excel"));
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.filtraBeni"));
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.multiSelection"));
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.selectAll"));
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.deselectAll"));
        buttons.add(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.hiddenColumn"));
        setMostraHideColumns(true);
        return buttons;
    }
}