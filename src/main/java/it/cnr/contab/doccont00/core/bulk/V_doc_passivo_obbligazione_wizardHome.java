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

package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class V_doc_passivo_obbligazione_wizardHome extends V_doc_passivo_obbligazioneHome {
	public V_doc_passivo_obbligazione_wizardHome(java.sql.Connection conn) {
		super(V_doc_passivo_obbligazione_wizardBulk.class,conn);
	}
	public V_doc_passivo_obbligazione_wizardHome(java.sql.Connection conn,it.cnr.jada.persistency.PersistentCache persistentCache) {
		super(V_doc_passivo_obbligazione_wizardBulk.class,conn,persistentCache);
	}

	public java.util.List<V_doc_passivo_obbligazione_wizardBulk> findDocPassivi(Documento_genericoBulk docgen) throws IntrospectionException, PersistencyException {
		SQLBuilder sql = this.createSQLBuilder();
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docgen.getEsercizio());
		sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docgen.getCd_cds());
		sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docgen.getCd_unita_organizzativa());
		sql.addClause(FindClause.AND, "cd_tipo_documento_amm", SQLBuilder.EQUALS, docgen.getCd_tipo_documento_amm());
		sql.addClause(FindClause.AND, "pg_documento_amm", SQLBuilder.EQUALS, docgen.getPg_doc_amm());
		return this.fetchAll(sql);
	}

}
