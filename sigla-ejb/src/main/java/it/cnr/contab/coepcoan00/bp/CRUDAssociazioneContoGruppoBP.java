package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.config00.pdcep.bulk.AssociazioneContoGruppoBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

public class CRUDAssociazioneContoGruppoBP extends SimpleCRUDBP {
    public CRUDAssociazioneContoGruppoBP() {
    }

    public CRUDAssociazioneContoGruppoBP(String s) {
        super(s);
    }

    @Override
    public Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.concat(Arrays.asList(super.createToolbar()).stream(),
                Arrays.asList(
                        new Button(properties, "CRUDToolbar.conti_da_associare"),
                        new Button(properties, "CRUDToolbar.conti_doppi")
                ).stream()).toArray(Button[]::new);
    }

    public RemoteIterator cercaContiDaAssociare(ActionContext actioncontext) throws BusinessProcessException {
        final CRUDComponentSession componentSession = createComponentSession();
        try {
            return componentSession.cerca(actioncontext.getUserContext(), null, new Voce_epBulk(), "selectContiNonAssociati");
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }
    public RemoteIterator cercaContiMultipli(ActionContext actioncontext) throws BusinessProcessException {
        final CRUDComponentSession componentSession = createComponentSession();
        try {
            return componentSession.cerca(actioncontext.getUserContext(), null, new AssociazioneContoGruppoBulk(), "selectAssociazioniMultiple");
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }
}
