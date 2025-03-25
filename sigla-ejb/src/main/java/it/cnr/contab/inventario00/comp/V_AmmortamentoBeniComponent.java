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

import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invHome;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniHome;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

public class V_AmmortamentoBeniComponent
		extends it.cnr.jada.comp.CRUDDetailComponent
			implements  Serializable, Cloneable {
/**
 * Inventario_beniComponent constructor comment.
 */
public V_AmmortamentoBeniComponent() {
	super();
}


	public List<V_ammortamento_beniBulk> findAllBeniDaAmmortizare(UserContext uc, Integer esercizio)  {
		try {
			V_ammortamento_beniHome v_ammortamentoBeneHome = (V_ammortamento_beniHome) getHome(uc, V_ammortamento_beniBulk.class);
			return v_ammortamentoBeneHome.findAllBeniDaAmmortizare(esercizio);
		}catch (ComponentException | PersistencyException ex){
			throw new RuntimeException("Error findAllBeniDaAmmortizare esercizio : "+esercizio);
		}

	}
}
