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

import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
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

		String tipo= Voce_epHome.SIOPE;
		String classificazioneTestata="CD_CLASSIFICAZIONE_5_LIV";

		if(flussi.isEstrazioneRendiconto()){
			tipo= Voce_epHome.SIOPE_RENDICONTO;
			classificazioneTestata="CD_CLASSIFICAZIONE";
		}


		sqlInterna=this.createSQLBuilder();
		sqlInterna.resetColumns();

		sqlInterna.setHeader("SELECT " +
					  "DETAIL."+classificazioneTestata+" CLASSIFICAZIONE , " +
				      "DETAIL.DS_CLASSIFICAZIONE , " +
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
				      " END ) IMP_QUARTO_TRIMESTRE ," +
						"SUM(IMPORTO) IMPORTO_TOTALE "
				);

		sqlInterna.addTableToHeader("CODICI_SIOPE");
		sqlInterna.addSQLJoin("CODICI_SIOPE.ESERCIZIO", sqlInterna.getColumnMap().getTableName().concat(".ESERCIZIO_SIOPE"));
		sqlInterna.addSQLJoin("CODICI_SIOPE.CD_SIOPE", sqlInterna.getColumnMap().getTableName().concat(".CD_SIOPE"));

		sqlInterna.addTableToHeader("V_CLASSIFICAZIONE_VOCI_EP");
		if(flussi.isEstrazioneRendiconto()) {
			sqlInterna.addSQLJoin("V_CLASSIFICAZIONE_VOCI_EP.ID_CLASSIFICAZIONE", "CODICI_SIOPE.ID_CLASSIFICAZIONE_SIOPE_REND");
		}else{
			sqlInterna.addSQLJoin("V_CLASSIFICAZIONE_VOCI_EP.ID_CLASSIFICAZIONE", "CODICI_SIOPE.ID_CLASSIFICAZIONE_SIOPE");
		}


		sqlInterna.addTableToHeader("V_CLASSIFICAZIONE_VOCI_EP","DETAIL");
		sqlInterna.addSQLJoin("V_CLASSIFICAZIONE_VOCI_EP.ESERCIZIO", "DETAIL.ESERCIZIO");
		sqlInterna.addSQLJoin("V_CLASSIFICAZIONE_VOCI_EP.TIPO", "DETAIL.TIPO");
		for(int i=1; i<=new Integer(flussi.getLivello());i++){
			sqlInterna.addSQLJoin(FindClause.AND, "V_CLASSIFICAZIONE_VOCI_EP.CD_LIVELLO"+i, SQLBuilder.EQUALS,"DETAIL.CD_LIVELLO"+i);
		}

		sqlInterna.addSQLClause(FindClause.AND, "V_CLASSIFICAZIONE_VOCI_EP.TIPO", SQLBuilder.EQUALS, tipo);
		sqlInterna.addSQLClause(FindClause.AND, "DETAIL.NR_LIVELLO", SQLBuilder.EQUALS,flussi.getLivello());

		sqlInterna.addSQLClause(FindClause.AND, sqlInterna.getColumnMap().getTableName().concat(".ESERCIZIO"), SQLBuilder.EQUALS, flussi.getEsercizio());
		if(flussi.getCds()!=null) {
			sqlInterna.addSQLClause(FindClause.AND, sqlInterna.getColumnMap().getTableName().concat(".CD_CDS"), SQLBuilder.EQUALS, flussi.getCdCds());
		}
		if(!flussi.isEstrazioneRendiconto()) {
			sqlInterna.addSQLClause(FindClause.AND, sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE"), SQLBuilder.GREATER_EQUALS, flussi.getDtEmissioneDa());
			sqlInterna.addSQLClause(FindClause.AND, sqlInterna.getColumnMap().getTableName().concat(".DT_EMISSIONE"), SQLBuilder.LESS_EQUALS, flussi.getDtEmissioneA());
		}

		sqlInterna.addSQLGroupBy("DETAIL."+classificazioneTestata+", DETAIL.DS_CLASSIFICAZIONE ");
		if(flussi.isEstrazioneRendiconto()) {
			sqlInterna.addOrderBy("DETAIL." + classificazioneTestata );
		}else{
			sqlInterna.addOrderBy("DETAIL." + classificazioneTestata + " DESC");
		}
		return sqlInterna;
	}



}
