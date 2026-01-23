package it.cnr.contab.doccont00.bp;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.ConsultazioniBP;

public class ConsScadAnnoCorrenteObbligazioniBP extends ConsultazioniBP {
    public ConsScadAnnoCorrenteObbligazioniBP(String s) {
        super(s);
    }

    public ConsScadAnnoCorrenteObbligazioniBP() {
    }

    @Override
    public RemoteIterator findFreeSearch(ActionContext context, CompoundFindClause clauses, OggettoBulk model) throws BusinessProcessException {
        try {
            clauses = CompoundFindClause.and(clauses, getBaseclause());
            it.cnr.jada.util.RemoteIterator ri =
                    it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator
                            (context, createComponentSession().cerca(context.getUserContext(), clauses, model, "cercaAnnoCorrente"));
            return ri;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void openIterator(ActionContext context) throws BusinessProcessException {
        try {
            OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();

            it.cnr.jada.util.RemoteIterator ri =
                    it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator
                            (context, createComponentSession().cerca(context.getUserContext(), CompoundFindClause.and(getBaseclause(), getFindclause()), model, "cercaAnnoCorrente"));
            this.setIterator(context, ri);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }
}
