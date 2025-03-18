package it.cnr.contab.gestiva00.bp;

import it.cnr.jada.util.action.ConsultazioniBP;

public class VisualTableDettRegIvaBP extends ConsultazioniBP {

    public VisualTableDettRegIvaBP() {
        super();
    }

    public VisualTableDettRegIvaBP(String function) {
        super(function);
    }

    public boolean isPrintButtonHidden() {
        return true;
    }

    public boolean isExcelButtonHidden() {
        return true;
    }


}