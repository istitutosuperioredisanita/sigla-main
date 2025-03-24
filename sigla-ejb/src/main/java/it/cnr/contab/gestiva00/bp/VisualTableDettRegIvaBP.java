package it.cnr.contab.gestiva00.bp;

import it.cnr.jada.util.Config;
import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.jsp.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VisualTableDettRegIvaBP extends ConsultazioniBP {

    public VisualTableDettRegIvaBP() {
        super();
    }

    public VisualTableDettRegIvaBP(String function) {
        super(function);
    }

    @Override
    public Button[] createToolbar() {
        Vector listButton = new Vector();
        listButton.addElement(new Button(Config.getHandler().getProperties(getClass()), "Toolbar.excel"));
        Button[] abutton = new Button[listButton.size()];
        for (int i = 0; i < listButton.size(); i++) {
            abutton[i] = (Button) listButton.get(i);
        }
        return abutton;
    }
}