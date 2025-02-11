package it.cnr.contab.consultazioni.bp;

import it.cnr.contab.config00.contratto.bulk.Procedure_amministrativeBulk;
import it.cnr.contab.incarichi00.tabrif.bulk.Tipo_norma_perlaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.action.ConsultazioniBP;

import java.util.Optional;

public class ConsProcedureAmministrativaBP extends ConsultazioniBP {
    private String tipoSelezione;
    public String getTipoSelezione() {
        return tipoSelezione;
    }

    public void setTipoSelezione(String tipoSelezione) {
        this.tipoSelezione = tipoSelezione;
    }

    protected void init(Config config, ActionContext context) throws BusinessProcessException {
        CompoundFindClause clauses = new CompoundFindClause();
        clauses.addClause("AND","fl_cancellato", SQLBuilder.EQUALS, Boolean.FALSE);
        this.setTipoSelezione(config.getInitParameter("tipoSelezione"));
        if(Optional.ofNullable(this.tipoSelezione).orElse("").equals(Tipo_norma_perlaBulk.ASS_CONTRATTI)){
            clauses.addClause("OR", "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_FORNITURA_SERVIZI);
            clauses.addClause("OR", "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_GENERICA);
            }
        setBaseclause(clauses);
        super.init(config,context);
        }


}
