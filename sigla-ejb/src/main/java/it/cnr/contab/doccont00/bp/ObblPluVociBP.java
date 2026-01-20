package it.cnr.contab.doccont00.bp;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennale_voceBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennale_voceHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.jsp.Button;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;

public class ObblPluVociBP extends ConsultazioniBP{

//    public ObblPluVociBP() {
//        super();
//    }
//
//    public ObblPluVociBP(String s) {
//        super(s);
//    }


    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws BusinessProcessException {
        try {
            CompoundFindClause clauses = new CompoundFindClause();
            int annoSelezionato = CNRUserContext.getEsercizio(context.getUserContext()).intValue();
            clauses.addClause("AND","esercizio", SQLBuilder.EQUALS, annoSelezionato);
            clauses.addClause("AND","esercizioOriginale", SQLBuilder.EQUALS, annoSelezionato);
            setBaseclause(clauses);

            super.init(config,context);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }
}


