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

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistentCache;
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


		if(flussi.getTipoFlusso().equals(FlussiDiCassaDtoBulk.REVERSALI))
			this.setColumnMap("FLUSSO_REVERSALI");
		else
			this.setColumnMap("FLUSSO_MANDATI");

		sqlInterna=this.createSQLBuilder();
		sqlInterna.resetColumns();

		sqlInterna.setHeader("SELECT " +
"CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO1,'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO2),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO3),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO4),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO5),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO5) CLASSIFICAZIONE , " +
				      "CLASSIFICAZIONE_VOCI_EP.DS_CLASSIFICAZIONE , " +
					  " sum(CASE "+
						" WHEN TO_CHAR("+ sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE").concat(",'mm') <=3 THEN IMPORTO ")+
					    " ELSE 0 "+
		              " END ) IMP_PRIMO_TRIMESTRE ,"+
				      " sum(CASE "+
				      " WHEN TO_CHAR("+ sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE").concat(",'mm') <=6 THEN IMPORTO ")+
				      " ELSE 0 "+
				      " END) IMP_SECONDO_TRIMESTRE ,"+
					  " sum(CASE "+
					  " WHEN TO_CHAR("+ sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE").concat(",'mm') <=9 THEN IMPORTO ")+
					  " ELSE 0 "+
					  " END) IMP_TERZO_TRIMESTRE ,"+
					  " sum(CASE "+
				      " WHEN TO_CHAR("+ sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE").concat(",'mm') <=12 THEN IMPORTO ")+
				      " ELSE 0 "+
				      " END ) IMP_QUARTO_TRIMESTRE "
				);

		sqlInterna.addTableToHeader("CODICI_SIOPE");
		sqlInterna.addSQLJoin("CODICI_SIOPE.ESERCIZIO", sqlInterna.getColumnMap().getTableName().concat(".ESERCIZIO_SIOPE"));
		sqlInterna.addSQLJoin("CODICI_SIOPE.CD_SIOPE", sqlInterna.getColumnMap().getTableName().concat(".CD_SIOPE"));

		sqlInterna.addTableToHeader("CLASSIFICAZIONE_VOCI_EP");
		sqlInterna.addSQLJoin("CLASSIFICAZIONE_VOCI_EP.ID_CLASSIFICAZIONE", "CODICI_SIOPE.ID_CLASSIFICAZIONE_SIOPE");
		sqlInterna.addSQLClause(FindClause.AND, "CLASSIFICAZIONE_VOCI_EP.TIPO", SQLBuilder.EQUALS, "SP1");

		sqlInterna.addSQLClause(FindClause.AND, sqlInterna.getColumnMap().getTableName().concat(".ESERCIZIO"), SQLBuilder.EQUALS, flussi.getEsercizio());
		sqlInterna.addSQLClause(FindClause.AND,  sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE"), SQLBuilder.GREATER_EQUALS, flussi.getDtEmissioneDa());
		sqlInterna.addSQLClause(FindClause.AND,  sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE"), SQLBuilder.LESS_EQUALS, flussi.getDtEmissioneA());
		sqlInterna.addSQLGroupBy("CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO1,'.')," +
				"CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO2),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO3),'.')," +
				"CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO4),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO5),'.'),CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO5), CLASSIFICAZIONE_VOCI_EP.DS_CLASSIFICAZIONE ");
		return sqlInterna;
	}

}
