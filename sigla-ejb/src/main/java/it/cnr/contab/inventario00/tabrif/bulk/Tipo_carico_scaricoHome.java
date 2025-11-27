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

package it.cnr.contab.inventario00.tabrif.bulk;

import it.cnr.contab.inventario01.bulk.Buono_carico_scaricoBulk;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;

import java.util.Optional;

public class Tipo_carico_scaricoHome extends BulkHome {
public Tipo_carico_scaricoHome(java.sql.Connection conn) {
	super(Tipo_carico_scaricoBulk.class,conn);
}
public Tipo_carico_scaricoHome(java.sql.Connection conn,PersistentCache persistentCache) {
	super(Tipo_carico_scaricoBulk.class,conn,persistentCache);
}
public java.util.Collection findTipoMovimenti(it.cnr.contab.inventario01.bulk.Buono_carico_scaricoBulk buonoCS) throws IntrospectionException, PersistencyException{

	SQLBuilder sql = createSQLBuilder();
	sql.addClause(FindClause.AND,"ti_documento",SQLBuilder.EQUALS, buonoCS.getTi_documento());
	sql.addClause(FindClause.AND,"dt_cancellazione", SQLBuilder.ISNULL,null);
	if(buonoCS.getTipoMovimento()!=null){
		sql.addClause(FindClause.AND,"fl_aumento_valore",SQLBuilder.EQUALS, buonoCS.getTipoMovimento().getFl_aumento_valore());
	}
	if(buonoCS.isPerVendita())
		sql.addClause(FindClause.AND,"fl_vendita",SQLBuilder.EQUALS, Boolean.TRUE);
	else
		if (buonoCS.isByFattura()||buonoCS.isByDocumento() || buonoCS.isByOrdini())
		  sql.addClause(FindClause.AND,"fl_vendita",SQLBuilder.EQUALS, Boolean.FALSE);
	if (buonoCS.isByFattura()||buonoCS.isByDocumento()){
		sql.addClause(FindClause.AND,"fl_fatturabile",SQLBuilder.EQUALS, Boolean.TRUE);
		// Se è un Buono di Carico proveniente da una fattura x aumento di valore
		if (buonoCS instanceof Buono_carico_scaricoBulk && ((Buono_carico_scaricoBulk)buonoCS).isByFatturaPerAumentoValore() ||
			(buonoCS instanceof Buono_carico_scaricoBulk && ((Buono_carico_scaricoBulk)buonoCS).isByDocumentoPerAumentoValore())){
				sql.addClause(FindClause.AND,"fl_aumento_valore",SQLBuilder.EQUALS, Boolean.TRUE);
		}
		else {
			sql.addClause(FindClause.AND,"fl_aumento_valore",SQLBuilder.EQUALS, Boolean.FALSE);
		}
	}
	//esclude o include il movimento di carico da ordini
	sql.addClause(FindClause.AND,"fl_da_ordini",SQLBuilder.EQUALS, buonoCS.isByOrdini() ||
            Optional.ofNullable(buonoCS.getTipoMovimento()).map(Tipo_carico_scaricoBulk::getFl_da_ordini).orElse(Boolean.FALSE));

	if (buonoCS instanceof Buono_carico_scaricoBulk &&
	   (buonoCS.getTipoMovimento() != null && !buonoCS.getTipoMovimento().getFl_buono_per_trasferimento())
		||((buonoCS.getCrudStatus()== OggettoBulk.UNDEFINED)||(buonoCS.getCrudStatus()== OggettoBulk.TO_BE_CREATED)))
	// Esclude i tipi di movimento "Per trasferimento"
		sql.addClause(FindClause.AND,"fl_buono_per_trasferimento",SQLBuilder.EQUALS, Boolean.FALSE);
	
	return fetchAll(sql);
}

public java.util.Collection findTipoMovimenti(String tipo) throws IntrospectionException, PersistencyException{

	SQLBuilder sql = createSQLBuilder();
	sql.addClause(FindClause.AND,"ti_documento",SQLBuilder.EQUALS, tipo);
	sql.addClause(FindClause.AND,"dt_cancellazione", SQLBuilder.ISNULL,null);
	
	return fetchAll(sql);
}
public java.util.Collection findTipoMovimentiPerTrasferimento(it.cnr.contab.inventario00.docs.bulk.Trasferimento_inventarioBulk bulk, String ti_documento) throws IntrospectionException, PersistencyException{

	SQLBuilder sql = createSQLBuilder();
	sql.addClause(FindClause.AND,"fl_buono_per_trasferimento",SQLBuilder.EQUALS, Boolean.TRUE);
	
	sql.addClause(FindClause.AND,"ti_documento",SQLBuilder.EQUALS, ti_documento);
	sql.addClause(FindClause.AND,"dt_cancellazione", SQLBuilder.ISNULL,null);
	
	
	return fetchAll(sql);
}
public boolean hasMovimentiFatturabili(String tipo) throws IntrospectionException, PersistencyException{

	SQLBuilder sql = createSQLBuilder();
	sql.addClause(FindClause.AND,"ti_documento",SQLBuilder.EQUALS, tipo);
	sql.addClause(FindClause.AND,"dt_cancellazione", SQLBuilder.ISNULL,null);
	sql.addClause(FindClause.AND, "fl_fatturabile", SQLBuilder.EQUALS, Boolean.TRUE);
	
	return (fetchAll(sql) ==null);
}
}
