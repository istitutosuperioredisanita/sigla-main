package it.cnr.contab.util.action;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.RicercaComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneComplessaBulk;
import it.cnr.jada.util.action.SearchProvider;
import it.cnr.jada.util.action.SelezionatoreListaBP;

import java.rmi.RemoteException;
import java.util.Optional;

public class ConsSelezionatoreListaBP extends SelezionatoreListaBP implements SearchProvider {

    private String componentSessionName;

    @Override
    protected void init(Config config, ActionContext context)
            throws BusinessProcessException {
        try {
            setMultiSelection(Optional.ofNullable(config.getInitParameter("multiSelezione")).map(s -> s.equalsIgnoreCase("Y")).orElse(Boolean.FALSE));
            setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(Class.forName(config.getInitParameter("bulkClassName"))));
            OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();
            setModel(context, model);
            this.componentSessionName = Optional.ofNullable(config.getInitParameter("componentSessionName")).orElse("JADAEJB_CRUDComponentSession");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw handleException(e);
        }
        setColumns(getBulkInfo().getColumnFieldPropertyDictionary());
        super.init(config, context);
        openIterator(context);
    }

    @Override
    public RemoteIterator search(
            ActionContext actioncontext,
            CompoundFindClause compoundfindclause,
            OggettoBulk oggettobulk)
            throws BusinessProcessException {
        try {
            return ((RicercaComponentSession)createComponentSession(this.componentSessionName))
                    .cerca(actioncontext.getUserContext(),
                    compoundfindclause,
                    (OggettoBulk) getBulkInfo().getBulkClass().newInstance());
        } catch (ComponentException | RemoteException | IllegalAccessException | InstantiationException e) {
            throw handleException(e);
        }
    }


    public void openIterator(ActionContext actioncontext)
            throws BusinessProcessException {
        try {
            setIterator(actioncontext, search(
                    actioncontext,
                    Optional.ofNullable(getCondizioneCorrente())
                            .map(CondizioneComplessaBulk::creaFindClause)
                            .filter(CompoundFindClause.class::isInstance)
                            .map(CompoundFindClause.class::cast)
                            .orElseGet(() -> new CompoundFindClause()),
                    getModel())
            );
        } catch (RemoteException e) {
            throw new BusinessProcessException(e);
        }
    }
}
