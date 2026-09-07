package it.cnr.contab.progettiric00.consultazioni.bp;

import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.jsp.Button;

public class EstrazioneCompletaProgettiBP extends ConsultazioniBP {

    public EstrazioneCompletaProgettiBP() {
        super();
    }

    public EstrazioneCompletaProgettiBP(String s) {
        super(s);
    }

    @Override
    public Button[] createToolbar() {

        Button[] toolbar = new Button[1];
        int i = 0;

        toolbar[i++] = new Button(
                it.cnr.jada.util.Config.getHandler().getProperties(getClass()),
                "Toolbar.excel");

        return toolbar;
    }
}