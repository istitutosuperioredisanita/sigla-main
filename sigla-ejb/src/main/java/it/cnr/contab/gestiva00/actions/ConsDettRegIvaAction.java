package it.cnr.contab.gestiva00.actions;

import it.cnr.contab.gestiva00.bp.ConsDettRegIvaBP;
import it.cnr.contab.gestiva00.bp.VisualTableDettRegIvaBP;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.contab.gestiva00.ejb.ConsRegIvaComponentSession;
import it.cnr.contab.pagopa.bulk.PagamentoPagopaBulk;
import it.cnr.contab.pagopa.bulk.PendenzaPagopaBulk;
import it.cnr.contab.pagopa.ejb.PendenzaPagopaComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.ConsultazioniBP;

import java.util.Collection;

public class ConsDettRegIvaAction extends BulkAction {


    public Forward doCerca(ActionContext context) {
        ConsDettRegIvaBP bp = (ConsDettRegIvaBP) context.getBusinessProcess();
        try {
            bp.fillModel(context);
            V_cons_dett_ivaBulk bulk = (V_cons_dett_ivaBulk) bp.getModel();

            if (bulk.getTipo_sezionale() != null) {
                //CompoundFindClause clauses = new CompoundFindClause();
                bulk.setCd_tipo_sezionale(bulk.getTipo_sezionale().getCd_tipo_sezionale());

                //todo da eliminare
                //clauses.addClause("AND", "tipo_sezionale", SQLBuilder.EQUALS, bulk.getCd_tipo_sezionale());

                // Crea un BP specializzato con la configurazione appropriata per ConsultazioniBP
                ConsultazioniBP visDettBP = (ConsultazioniBP)context.createBusinessProcess("VisualTableDettRegIvaBP");

                //it.cnr.jada.util.RemoteIterator ri = bp.find(context, clauses, bulk, null,bulk.getCd_tipo_sezionale());

                it.cnr.jada.util.RemoteIterator ri = it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(context,((ConsRegIvaComponentSession)bp.createComponentSession()).cercaDettRegIva(context.getUserContext(),bulk.getCd_tipo_sezionale()));

                visDettBP.setIterator(context,ri);
                visDettBP.setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(V_cons_dett_ivaBulk.class));

//                if(visDettBP.getBaseclause()==null)
//                    visDettBP.setBaseclause(clauses);

                context.addBusinessProcess(visDettBP);

                visDettBP.openIterator(context);

                return context.findDefaultForward();
            } else {
                throw new it.cnr.jada.comp.ApplicationException("Attenzione bisogna indicare un tipo sezionale!");
            }
        } catch (Exception e) {
            return handleException(context, e);
        }
    }
    public Forward doCloseForm(ActionContext actioncontext)
            throws BusinessProcessException
    {
        try
        {
            return doConfirmCloseForm(actioncontext, 4);
        }
        catch(Throwable throwable)
        {
            return handleException(actioncontext, throwable);
        }
    }




}

