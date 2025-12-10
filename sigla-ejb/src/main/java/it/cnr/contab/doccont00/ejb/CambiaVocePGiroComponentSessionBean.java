package it.cnr.contab.doccont00.ejb;

import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.doccont00.comp.CambiaVocePGiroComponent;
import it.cnr.contab.doccont00.tabrif.bulk.CambiaVocePGiroBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSessionBean;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateless;
import java.rmi.RemoteException;

@Stateless(name="CNRDOCCONT00_EJB_CambiaVocePGiroComponentSession")
public class CambiaVocePGiroComponentSessionBean extends CRUDComponentSessionBean implements CambiaVocePGiroComponentSession {
    @PostConstruct
    public void ejbCreate() {
        componentObj = new it.cnr.contab.doccont00.comp.CambiaVocePGiroComponent();
    }
    @Remove
    public void ejbRemove() throws jakarta.ejb.EJBException {
        componentObj.release();
    }

    @Override
    public RemoteIterator cercaReversali(UserContext param0, CambiaVocePGiroBulk param1, V_voce_f_partita_giroBulk param2, CompoundFindClause param3) throws ComponentException, RemoteException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.util.RemoteIterator result = ((CambiaVocePGiroComponent)componentObj).cercaReversali(param0, param1, param2, param3);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

    @Override
    public RemoteIterator cercaMandati(UserContext param0, CambiaVocePGiroBulk param1, V_voce_f_partita_giroBulk param2, CompoundFindClause param3) throws ComponentException, RemoteException {
        pre_component_invocation(param0,componentObj);
        try {
            it.cnr.jada.util.RemoteIterator result = ((CambiaVocePGiroComponent)componentObj).cercaMandati(param0, param1, param2, param3);
            component_invocation_succes(param0,componentObj);
            return result;
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }
    @Override
    public void elabora(UserContext param0, CambiaVocePGiroBulk param1) throws ComponentException, RemoteException {
        pre_component_invocation(param0,componentObj);
        try {
            ((CambiaVocePGiroComponent)componentObj).elabora(param0, param1);
            component_invocation_succes(param0,componentObj);
        } catch(it.cnr.jada.comp.NoRollbackException e) {
            component_invocation_succes(param0,componentObj);
            throw e;
        } catch(it.cnr.jada.comp.ComponentException e) {
            component_invocation_failure(param0,componentObj);
            throw e;
        } catch(RuntimeException e) {
            throw uncaughtRuntimeException(param0,componentObj,e);
        } catch(Error e) {
            throw uncaughtError(param0,componentObj,e);
        }
    }

}
