package it.cnr.contab.gestiva00.actions;

import it.cnr.contab.gestiva00.bp.ConsDettRegIvaBP;
import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.action.BulkAction;
import it.cnr.jada.util.action.ConsultazioniBP;

public class ConsDettRegIvaAction extends BulkAction {


    public Forward doCerca(ActionContext context) {
        ConsDettRegIvaBP bp = (ConsDettRegIvaBP) context.getBusinessProcess();
        try {
            bp.fillModel(context);
            V_cons_dett_ivaBulk bulk = (V_cons_dett_ivaBulk) bp.getModel();

            if (bulk.getTipo_sezionale() != null) {
                // Imposta il codice tipo sezionale direttamente
                bulk.setCd_tipo_sezionale(bulk.getTipo_sezionale().getCd_tipo_sezionale());

                ConsultazioniBP visDettBP = (ConsultazioniBP) context.createBusinessProcess("VisualTableDettRegIvaBP");
                CompoundFindClause clauses = new CompoundFindClause();
                clauses.addClause("AND", "tipo_sezionale", SQLBuilder.EQUALS, bulk.getCd_tipo_sezionale());

                if (visDettBP.getBaseclause() == null)
                    visDettBP.setBaseclause(clauses);

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
            throws BusinessProcessException {
        try {
            return doConfirmCloseForm(actioncontext, 4);
        } catch (Throwable throwable) {
            return handleException(actioncontext, throwable);
        }
    }


}

