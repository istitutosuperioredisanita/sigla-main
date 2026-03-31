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

import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.io.Serializable;

/**
 * Insert the type's description here.
 * Creation date: (18/12/2001 13.49.33)
 * @author: Roberto Fantino
 */
public class TipoTrasportoRientroComponent
		extends it.cnr.jada.comp.CRUDComponent
		implements ICRUDMgr,ITipoTrasportoRientroMgr,Cloneable,Serializable {

	public void eliminaConBulk (UserContext userContext,OggettoBulk bulk) throws ComponentException{

		try{
			Tipo_trasporto_rientroBulk tipoTR= (Tipo_trasporto_rientroBulk)bulk;
			tipoTR.setDtCancellazione(getHome(userContext,Tipo_trasporto_rientroBulk.class).getServerTimestamp());
			updateBulk( userContext, tipoTR);
		}catch (it.cnr.jada.persistency.PersistencyException e){
			throw handleException(bulk,e);
		}
	}

	protected Query select(UserContext userContext,CompoundFindClause clauses,OggettoBulk bulk) throws ComponentException, it.cnr.jada.persistency.PersistencyException {

		Tipo_trasporto_rientroBulk tipoTR = (Tipo_trasporto_rientroBulk)bulk;
		SQLBuilder sql = (SQLBuilder)super.select(userContext,clauses, bulk);

		return sql;

	}
}