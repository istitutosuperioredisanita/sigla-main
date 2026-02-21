package it.cnr.contab.doccont00.bp;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.action.ConsultazioniBP;

public class AccertPluVociBP extends ConsultazioniBP{

    protected void init(it.cnr.jada.action.Config config, ActionContext context)
            throws BusinessProcessException {
        try {
            CompoundFindClause clauses = new CompoundFindClause();
            int annoSelezionato = CNRUserContext.getEsercizio(context.getUserContext()).intValue();
            clauses.addClause("AND","esercizio", SQLBuilder.EQUALS, annoSelezionato);
            setBaseclause(clauses);

            super.init(config,context);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }
}


