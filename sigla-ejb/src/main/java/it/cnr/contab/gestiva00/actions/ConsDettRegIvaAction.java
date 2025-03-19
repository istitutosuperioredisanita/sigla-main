package it.cnr.contab.gestiva00.actions;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
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

            validateTipoSezionale(bulk.getCd_tipo_sezionale());

            ConsultazioniBP visDettBP = (ConsultazioniBP) context.createBusinessProcess("VisualTableDettRegIvaBP");
            CompoundFindClause clauses = createTipoSezionaleClause(bulk.getCd_tipo_sezionale());

            if (visDettBP.getBaseclause() == null) {
                visDettBP.setBaseclause(clauses);
            }

            context.addBusinessProcess(visDettBP);
            visDettBP.openIterator(context);

            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    private void validateTipoSezionale(String cdTipoSezionale) throws it.cnr.jada.comp.ApplicationException {
        if (cdTipoSezionale == null || cdTipoSezionale.isEmpty()) {
            throw new it.cnr.jada.comp.ApplicationException("Attenzione bisogna indicare un tipo sezionale!");
        }
    }

    private CompoundFindClause createTipoSezionaleClause(String cdTipoSezionale) {
        CompoundFindClause clauses = new CompoundFindClause();
        String[] values = cdTipoSezionale.split(" - ");

        // Aggiungo la prima clausola
        clauses.addClause("", "tipo_sezionale", SQLBuilder.EQUALS, values[0].trim());

        // Aggiungo la seconda clausola se esiste
        if (values.length > 1) {
            clauses.addClause("AND", "tipo_sezionale", SQLBuilder.EQUALS, values[1].trim());
        }

        return clauses;
    }

    public Forward doOnTipoSezionaleChange(ActionContext context) {
        try {
            ConsDettRegIvaBP bp = (ConsDettRegIvaBP) context.getBusinessProcess();
            bp.fillModel(context);

            bp.setModel(context, bp.getModel());

        } catch (Throwable t) {
            return handleException(context, t);
        }
        return context.findDefaultForward();
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

