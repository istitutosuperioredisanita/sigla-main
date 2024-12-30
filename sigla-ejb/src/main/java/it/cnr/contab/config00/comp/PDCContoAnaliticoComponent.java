/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created on Sep 20, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.config00.comp;

import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class PDCContoAnaliticoComponent extends CRUDComponent {

    public SQLBuilder selectVoce_epByClause(UserContext userContext, Voce_analiticaBulk voceAnaliticaBulk, Voce_epBulk voceEpBulk, CompoundFindClause clauses) throws ComponentException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = getHome(userContext,Voce_epBulk.class).createSQLBuilder();
        if (clauses != null)
            sql.addClause(clauses);

        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.openParenthesis(FindClause.AND);
        sql.addClause(FindClause.OR, "natura_voce", SQLBuilder.EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_COSTO );
        sql.addClause(FindClause.OR, "natura_voce", SQLBuilder.EQUALS, Voce_epBulk.CONTO_ECONOMICO_ESERCIZIO_RICAVO );
        sql.closeParenthesis();

        return sql;
    }
}