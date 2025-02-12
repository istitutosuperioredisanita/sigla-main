package it.cnr.contab.config00.contratto.bulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class TipoContrattoPassivoRestHome extends Tipo_contrattoHome  {
    public TipoContrattoPassivoRestHome(java.sql.Connection conn) {
        super(TipoContrattoPassivoRestBulk.class, conn);
    }
    public TipoContrattoPassivoRestHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(TipoContrattoPassivoRestBulk.class, conn, persistentCache);
    }

    @Override
    public SQLBuilder restSelect(UserContext userContext, SQLBuilder sql, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws ComponentException, PersistencyException {
        sql.addClause(FindClause.AND, "fl_cancellato", SQLBuilder.EQUALS, Boolean.FALSE);
        sql.openParenthesis(FindClause.AND);
        sql.addClause(FindClause.OR, "natura_contabile", SQLBuilder.EQUALS, Tipo_contrattoBulk.NATURA_CONTABILE_PASSIVO);
        sql.addClause(FindClause.OR, "natura_contabile", SQLBuilder.EQUALS, Tipo_contrattoBulk.NATURA_CONTABILE_SENZA_FLUSSI_FINANZIARI);
        sql.closeParenthesis();
        return sql;
    }
}
