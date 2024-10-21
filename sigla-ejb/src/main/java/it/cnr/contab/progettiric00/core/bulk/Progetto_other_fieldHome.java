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

package it.cnr.contab.progettiric00.core.bulk;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;

public class Progetto_other_fieldHome extends BulkHome {

	public final static String TI_IMPORTO_FINANZIATO = "FIN" ;
	public final static String TI_IMPORTO_COFINANZIATO = "COF" ;

	public Progetto_other_fieldHome(java.sql.Connection conn) {
		super(Progetto_other_fieldBulk.class,conn);
	}
	
	public Progetto_other_fieldHome(java.sql.Connection conn,PersistentCache persistentCache) {
		super(Progetto_other_fieldBulk.class,conn,persistentCache);
	}


	//TODO da testare
	/*
	public java.util.List findObbligazioniFromProgetto(Integer pgProgetto) throws IntrospectionException, PersistencyException
	{
		PersistentHome obbligazioneHome = getHomeCache().getHome(ObbligazionePluriannaleVoceBulk.class, "OBBLIGAZIONE_PLURIENNALE_VOCE");
		SQLBuilder sql = obbligazioneHome.createSQLBuilder();

		sql.addTableToHeader("PROGETTO_OTHER_FIELD");
		sql.addTableToHeader("V_LINEA_ATTIVITA_VALIDA");

		sql.addSQLJoin("PROGETTO_OTHER_FIELD.PG_PROGETTO", "V_LINEA_ATTIVITA_VALIDA.PG_PROGETTO");
		sql.addSQLJoin("V_LINEA_ATTIVITA_VALIDA.CD_CENTRO_RESPONSABILITA", "OBBLIGAZIONE_PLURIENNALE_VOCE.CD_CENTRO_RESPONSABILITA");
		sql.addSQLJoin("V_LINEA_ATTIVITA_VALIDA.CD_LINEA_ATTIVITA", "OBBLIGAZIONE_PLURIENNALE_VOCE.CD_LINEA_ATTIVITA");

		sql.addSQLClause("AND", "OBBLIGAZIONE_PLURIENNALE_VOCE.ESERCIZIO", sql.EQUALS, 2024);
		sql.addSQLClause("AND", "PROGETTO_OTHER_FIELD.PG_PROGETTO", sql.EQUALS, pgProgetto);

		sql.addParameter(pgProgetto, java.sql.Types.INTEGER, 0);

		// Specifichiamo che vogliamo selezionare tutti i campi di OBBLIGAZIONE_PLURIENNALE_VOCE
		sql.setDistinctClause(false);

		return obbligazioneHome.fetchAll(sql);
	}
	*/
}
