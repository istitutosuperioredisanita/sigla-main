package it.cnr.contab.consultazioni.bp;

import it.cnr.contab.incarichi00.tabrif.bulk.Tipo_norma_perlaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.action.ConsultazioniBP;

import java.util.Optional;

public class ConsPartitaGiroAssBP extends ConsultazioniBP {


    protected void init(Config config, ActionContext context) throws BusinessProcessException {

            CompoundFindClause clauses = new CompoundFindClause();
            clauses.addClause("AND","esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(context.getUserContext()).intValue());
            setBaseclause(clauses);
            super.init(config,context);
        }

}
