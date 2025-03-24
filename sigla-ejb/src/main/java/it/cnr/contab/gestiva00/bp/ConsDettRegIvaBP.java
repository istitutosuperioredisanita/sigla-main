

package it.cnr.contab.gestiva00.bp;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.contab.gestiva00.core.bulk.V_cons_reg_ivaBulk;
import it.cnr.contab.gestiva00.ejb.ConsRegIvaComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.ConsultazioniBP;

import java.rmi.RemoteException;

public class ConsDettRegIvaBP extends BulkBP {

    public ConsDettRegIvaBP() {
        super();
    }

    public ConsDettRegIvaBP(String function) {
        super(function);
    }


    public it.cnr.jada.util.jsp.Button[] createToolbar() {
        it.cnr.jada.util.jsp.Button[] toolbar = new it.cnr.jada.util.jsp.Button[1];
        int i = 0;
        toolbar[i++] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.startSearch");
        return toolbar;
    }

    protected void init(Config config, ActionContext context) throws BusinessProcessException {
        // Inizializza il BulkInfo
        super.init(config, context);

        // Crea il bulk e imposta il modello
        V_cons_dett_ivaBulk bulk = new V_cons_dett_ivaBulk();
        setModel(context, bulk);
    }

    public boolean isRicercaButtonEnabled() {
        return true;
    }

    public ConsRegIvaComponentSession createComponentSession()
            throws javax.ejb.EJBException,
            java.rmi.RemoteException,
            BusinessProcessException {
        return (ConsRegIvaComponentSession) createComponentSession("CNRGESTIVA00_EJB_ConsRegIvaComponentSession", ConsRegIvaComponentSession.class);
    }


    /**
     * Effettua una operazione di ricerca per un attributo di un modello.
     *
     * @param actionContext contesto dell'azione in corso
     * @param clauses       Albero di clausole da utilizzare per la ricerca
     * @param bulk          prototipo del modello di cui si effettua la ricerca
     * @param context       modello che fa da contesto alla ricerca (il modello del FormController padre del
     *                      controller che ha scatenato la ricerca)
     * @return un RemoteIterator sul risultato della ricerca o null se la ricerca non ha ottenuto nessun risultato
     */
    public it.cnr.jada.util.RemoteIterator find(it.cnr.jada.action.ActionContext actionContext, it.cnr.jada.persistency.sql.CompoundFindClause clauses, it.cnr.jada.bulk.OggettoBulk bulk, it.cnr.jada.bulk.OggettoBulk context, String property) throws it.cnr.jada.action.BusinessProcessException {
        try {
            return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext, createComponentSession().cerca(actionContext.getUserContext(), clauses, bulk, context, property));
        } catch (Exception e) {
            throw new it.cnr.jada.action.BusinessProcessException(e);
        }
    }

}