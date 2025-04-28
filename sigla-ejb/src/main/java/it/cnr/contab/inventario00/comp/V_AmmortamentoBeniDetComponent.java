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

package it.cnr.contab.inventario00.comp;

import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniHome;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beni_detBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beni_detHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.io.Serializable;
import java.util.List;

public class V_AmmortamentoBeniDetComponent
		extends it.cnr.jada.comp.CRUDDetailComponent
			implements  Serializable, Cloneable {
/**
 * Inventario_beniComponent constructor comment.
 */
public V_AmmortamentoBeniDetComponent() {
	super();
}


	public List<V_ammortamento_beni_detBulk> getDatiAmmortamentoBeni(UserContext uc, Integer esercizio)  {
		try {
			V_ammortamento_beni_detHome v_ammortamentoBeneDetHome = (V_ammortamento_beni_detHome)getHomeCache(uc).getHome( V_ammortamento_beni_detBulk.class,"PROCEDURA_AMMORTAMENTO");

		//	v_ammortamentoBeneDetHome.setColumnMap("PROCEDURA_AMMORTAMENTO");
			SQLBuilder sql = v_ammortamentoBeneDetHome.createSQLBuilder();

			sql.resetColumns();
			sql.addColumn("PG_INVENTARIO");
			sql.addColumn("NR_INVENTARIO");
			sql.addColumn("PROGRESSIVO");
			sql.addColumn("ETICHETTA");
			sql.addColumn("FL_TOTALMENTE_SCARICATO");
			sql.addColumn("TI_AMMORTAMENTO");
			sql.addColumn("CD_CATEGORIA_GRUPPO");
			sql.addColumn("PERC_PRIMO_ANNO");
			sql.addColumn("PERC_SUCCESSIVI");
			sql.addColumn("CD_TIPO_AMMORTAMENTO");
			sql.addColumn("ESERCIZIO_COMPETENZA");
			sql.addColumn("NVL(SUM(VALORE_INIZIALE),0)", "VALORE_INIZIALE");
			sql.addColumn("NVL(SUM(VALORE_AMMORTIZZATO),0)", "VALORE_AMMORTIZZATO_BENE");
			sql.addColumn("NVL(SUM(IMPONIBILE_AMMORTAMENTO),0)", "IMPONIBILE_AMMORTAMENTO_BENE");
			sql.addColumn("NVL(SUM(VARIAZIONE_PIU),0)", "VARIAZIONE_PIU");
			sql.addColumn("NVL(SUM(VARIAZIONE_MENO),0)", "VARIAZIONE_MENO");
			sql.addColumn("NVL(SUM(INCREMENTO_VALORE),0)", "INCREMENTO_VALORE");
			sql.addColumn("NVL(SUM(DECREMENTO_VALORE),0)", "DECREMENTO_VALORE");
			sql.addColumn("NVL(SUM(STORNO),0)", "STORNO");
			sql.addColumn("NVL(SUM(IMPONIBILE_AMMORTAMENTO),0) + NVL(SUM(INCREMENTO_VALORE),0) - NVL(SUM(DECREMENTO_VALORE),0)", "IMPONIBILE_AMMORTAMENTO_CALCOLATO");
			sql.addColumn("NVL(SUM(VALORE_AMMORTIZZATO),0) - NVL(SUM(STORNO),0)", "VALORE_AMMORTIZZATO_CALCOLATO");
			sql.addColumn("MAX(NUMERO_ANNO_AMMORTAMENTO) NUMERO_ANNO_AMMORTAMENTO");


			sql.addSQLClause( "AND", "fl_ammortamento", SQLBuilder.EQUALS, "Y");
			sql.openParenthesis("AND");
			sql.addSQLClause("AND", "esercizio_competenza", SQLBuilder.EQUALS, esercizio);
			sql.addSQLClause("OR", "esercizio_competenza", SQLBuilder.ISNULL,null);
			sql.closeParenthesis();


			sql.openParenthesis("AND");

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "TIPORECORD", SQLBuilder.EQUALS, "INCREMENTO");
			sql.addSQLClause( "AND", "ESERCIZIO_BUONO_CARICO", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "TIPORECORD", SQLBuilder.EQUALS, "DECREMENTO");
			sql.addSQLClause( "AND", "ESERCIZIO_BUONO_CARICO", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "STORNO");
			sql.addSQLClause( "AND", "esercizio_ammortanento", SQLBuilder.GREATER, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "VALORE");
			sql.addSQLClause( "AND", "esercizio_carico_bene", SQLBuilder.LESS_EQUALS, esercizio);
			sql.closeParenthesis();

			sql.openParenthesis("OR");
			sql.addSQLClause( "AND", "tiporecord", SQLBuilder.EQUALS, "AMMORTAMENTO");
			sql.addSQLClause( "AND", "esercizio_ammortanento", SQLBuilder.EQUALS, esercizio.intValue()-1);
			sql.closeParenthesis();


			sql.closeParenthesis();

		//	sql.addSQLClause( "AND", "ETICHETTA", SQLBuilder.EQUALS, "V-019698");

			sql.addSQLGroupBy("PG_INVENTARIO,NR_INVENTARIO,PROGRESSIVO,ETICHETTA,FL_TOTALMENTE_SCARICATO,TI_AMMORTAMENTO,CD_CATEGORIA_GRUPPO,PERC_PRIMO_ANNO,PERC_SUCCESSIVI,CD_TIPO_AMMORTAMENTO,ESERCIZIO_COMPETENZA");

			return v_ammortamentoBeneDetHome.fetchAll(sql);



			//return v_ammortamentoBeneDetHome.getDatiAmmortamentoBeni(esercizio);
		}catch (ComponentException | PersistencyException ex){
			throw new RuntimeException("Error getDatiAmmortamentoBeni esercizio : "+esercizio);
		}

	}
}
