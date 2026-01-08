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

package it.cnr.contab.config00.bp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class ConsConfCNRBP extends it.cnr.jada.util.action.ConsultazioniBP {

    public ConsConfCNRBP() {
	super();
}
protected void init(it.cnr.jada.action.Config config, ActionContext context) throws BusinessProcessException {
	try {
		super.init(config,context);

        setFreeSearchSet("CONS_CONF_TOTALE");
		setColumns(getBulkInfo().getColumnFieldPropertyDictionary("CONS_CONF_TOTALE"));

	}catch(Throwable e) {
		throw new BusinessProcessException(e);
	}
}

}
