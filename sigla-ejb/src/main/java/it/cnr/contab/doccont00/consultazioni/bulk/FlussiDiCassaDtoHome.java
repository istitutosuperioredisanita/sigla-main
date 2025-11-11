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

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.contab.config00.sto.bulk.Ass_uo_areaBulk;
import it.cnr.contab.rest.bulk.RestServicesHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Connection;

public class FlussiDiCassaDtoHome extends BulkHome {


	public FlussiDiCassaDtoHome(Connection conn) {
		super(FlussiDiCassaDtoBulk.class, conn);
	}
	public FlussiDiCassaDtoHome(Connection conn, PersistentCache persistentCache) {
		super(FlussiDiCassaDtoBulk.class, conn, persistentCache);
	}

	public SQLBuilder findFlussiDiCassa(UserContext uc, FlussiDiCassaDtoBulk flussi){
		SQLBuilder sqlInterna = null;

		if(flussi.getTipoFlusso().equals(FlussiDiCassaDtoBulk.MANDATI)){
			sqlInterna = getHomeCache().getHome(V_cons_siope_mandatiBulk.class,"FLUSSI").createSQLBuilder();
		}else{
			sqlInterna = getHomeCache().getHome(V_cons_siope_reversaliBulk.class,"FLUSSI").createSQLBuilder();
		}

		//SQLBuilder sql = this.createSQLBuilder();
		sqlInterna.resetColumns();
		setColumnMap("FLUSSO_CASSA");

		sqlInterna.setHeader("SELECT " +
"CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CLASSIFICAZIONE.CD_LIVELLO1,'.'),CLASSIFICAZIONE.CD_LIVELLO2),'.'),CLASSIFICAZIONE.CD_LIVELLO3),'.'),CLASSIFICAZIONE.CD_LIVELLO4),'.'),CLASSIFICAZIONE.CD_LIVELLO5),'.'),CLASSIFICAZIONE.CD_LIVELLO5) CLASSIFICAZIONE , " +
				      "CLASSIFICAZIONE.DS_CLASSIFICAZIONE , " +
					  " CASE "+
						" WHEN TO_CHAR( FLUSSI.DT_EMISSIONE,'mm') <=3 THEN FLUSSI.IMPORTO "+
					    " ELSE 0 "+
		              " END PRIMO_TRIMESTRE ,"+
				      " CASE "+
				      " WHEN TO_CHAR( FLUSSI.DT_EMISSIONE,'mm') <=6 THEN FLUSSI.IMPORTO "+
				      " ELSE 0 "+
				      " END SECONDO_TRIMESTRE ,"+
					  " CASE "+
					  " WHEN TO_CHAR( FLUSSI.DT_EMISSIONE,'mm') <=9 THEN FLUSSI.IMPORTO "+
					  " ELSE 0 "+
					  " END TERZO_TRIMESTRE ,"+
					  " CASE "+
				      " WHEN TO_CHAR( FLUSSI.DT_EMISSIONE,'mm') <=12 THEN FLUSSI.IMPORTO "+
				      " ELSE 0 "+
				      " END TERZO_TRIMESTRE ,"
				      //"SUM( IMP_PRIMO_TRIMESTRE ) PRIMO_TRIMESTRE , " +
				      //"SUM( IMP_SECONDO_TRIMESTRE ) SECONDO_TRIMESTRE , " +
				      //"SUM( IMP_TERZO_TRIMESTRE ) TERZO_TRIMESTRE , " +
				      //"SUM( IMP_QUARTO_TRIMESTRE) QUARTO_TRIMESTRE "
				);

		sqlInterna.addTableToHeader("CODICI_SIOPE");
		sqlInterna.addJoin("CODICI_SIOPE.ESERCIZIO", "FLUSSI.ESERCIZIO_SIOPE");
		sqlInterna.addJoin("CODICI_SIOPE.CD_SIOPE", "FLUSSI.CD_SIOPE");

		sqlInterna.addTableToHeader("CODICI_SIOPE");
		sqlInterna.addJoin("CODICI_SIOPE.ESERCIZIO", "FLUSSI.ESERCIZIO_SIOPE");
		sqlInterna.addJoin("CODICI_SIOPE.CD_SIOPE", "FLUSSI.CD_SIOPE");

		sqlInterna.addTableToHeader("CLASSIFICAZIONE_VOCE_EP");
		sqlInterna.addJoin("CLASSIFICAZIONE_VOCE_EP.ID_CLASSIFICAZIONE", "FLUSSI.ID_CLASSIFICAZIONE_SIOPE");
		sqlInterna.addClause(FindClause.AND, "CLASSIFICAZIONE_VOCE_EP.TIPO", SQLBuilder.EQUALS, "SP1");

		sqlInterna.addClause(FindClause.AND, "FLUSSI.ESERCIZIO", SQLBuilder.EQUALS, flussi.getEsercizio());
		sqlInterna.addClause(FindClause.AND, "FLUSSI.DT_EMISSIONE", SQLBuilder.GREATER_EQUALS, flussi.getDtEmissioneDa());
		sqlInterna.addClause(FindClause.AND, "FLUSSI.DT_EMISSIONE", SQLBuilder.LESS_EQUALS, flussi.getDtEmissioneA());


		sqlInterna.addSQLGroupBy("CLASSIFICAZIONE,DS_CLASSIFICAZIONE");

		return sqlInterna;
	}

}
