package it.cnr.contab.gestiva00.bp;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.jsp.Button;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;

public class ConsRegIvaBP extends ConsultazioniBP {

    public ConsRegIvaBP() {
        super();
    }

    public ConsRegIvaBP(String s) {
        super(s);
    }


    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws BusinessProcessException {
        try {
            super.init(config, context);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
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