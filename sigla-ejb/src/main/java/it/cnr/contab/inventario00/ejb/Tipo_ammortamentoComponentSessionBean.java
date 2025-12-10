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

package it.cnr.contab.inventario00.ejb;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;

import it.cnr.contab.inventario00.comp.Tipo_ammortamentoComponent;
import it.cnr.contab.inventario00.comp.V_AmmortamentoBeniComponent;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.dto.TipoAmmCatGruppoDto;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import java.rmi.RemoteException;
import java.util.List;

@Stateless(name="CNRINVENTARIO00_EJB_Tipo_ammortamentoComponentSession")
public class Tipo_ammortamentoComponentSessionBean extends it.cnr.jada.ejb.CRUDDetailComponentSessionBean implements Tipo_ammortamentoComponentSession {
@PostConstruct
	public void ejbCreate() {
	componentObj = new it.cnr.contab.inventario00.comp.Tipo_ammortamentoComponent();
}

public void annullaModificaGruppi(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).annullaModificaGruppi(param0);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void associaTuttiGruppi(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1,java.util.List param2) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).associaTuttiGruppi(param0,param1,param2);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public it.cnr.jada.util.RemoteIterator cercaGruppiAssociabili(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		it.cnr.jada.util.RemoteIterator result = ((Tipo_ammortamentoComponent)componentObj).cercaGruppiAssociabili(param0,param1);
		component_invocation_succes(param0,componentObj);
		return result;
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public it.cnr.jada.util.RemoteIterator cercaGruppiAssociabiliPerModifica(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		it.cnr.jada.util.RemoteIterator result = ((Tipo_ammortamentoComponent)componentObj).cercaGruppiAssociabiliPerModifica(param0,param1);
		component_invocation_succes(param0,componentObj);
		return result;
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void eliminaGruppiConBulk(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).eliminaGruppiConBulk(param0,param1);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void eliminaGruppiConBulk(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1,it.cnr.jada.bulk.OggettoBulk[] param2) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).eliminaGruppiConBulk(param0,param1,param2);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public it.cnr.jada.util.RemoteIterator getAmmortamentoRemoteIteratorPerRiassocia(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1,java.lang.Class param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		it.cnr.jada.util.RemoteIterator result = ((Tipo_ammortamentoComponent)componentObj).getAmmortamentoRemoteIteratorPerRiassocia(param0,param1,param2,param3);
		component_invocation_succes(param0,componentObj);
		return result;
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public java.lang.String getLocalTransactionID(it.cnr.jada.UserContext param0,boolean param1) throws it.cnr.jada.comp.ComponentException,it.cnr.jada.persistency.PersistencyException,it.cnr.jada.persistency.IntrospectionException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		java.lang.String result = ((Tipo_ammortamentoComponent)componentObj).getLocalTransactionID(param0,param1);
		component_invocation_succes(param0,componentObj);
		return result;
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.persistency.PersistencyException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.persistency.IntrospectionException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void inizializzaGruppi(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).inizializzaGruppi(param0);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void inizializzaGruppiPerModifica(it.cnr.jada.UserContext param0) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).inizializzaGruppiPerModifica(param0);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public void modificaGruppi(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1,it.cnr.jada.bulk.OggettoBulk[] param2,java.util.BitSet param3,java.util.BitSet param4) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		((Tipo_ammortamentoComponent)componentObj).modificaGruppi(param0,param1,param2,param3,param4);
		component_invocation_succes(param0,componentObj);
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}
public it.cnr.jada.util.RemoteIterator selectGruppiByClause(it.cnr.jada.UserContext param0,it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk param1,java.lang.Class param2,it.cnr.jada.persistency.sql.CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,jakarta.ejb.EJBException {
	pre_component_invocation(param0,componentObj);
	try {
		it.cnr.jada.util.RemoteIterator result = ((Tipo_ammortamentoComponent)componentObj).selectGruppiByClause(param0,param1,param2,param3);
		component_invocation_succes(param0,componentObj);
		return result;
	} catch(it.cnr.jada.comp.NoRollbackException e) {
		component_invocation_succes(param0,componentObj);
		throw e;
	} catch(it.cnr.jada.comp.ComponentException e) {
		component_invocation_failure(param0,componentObj);
		throw e;
	} catch(RuntimeException e) {
		throw uncaughtRuntimeException(param0,componentObj,e);
	} catch(Error e) {
		throw uncaughtError(param0,componentObj,e);
	}
}

	@Override
	public List<Tipo_ammortamentoBulk> findTipoAmmortamento(UserContext param0, String param1, String param2, Integer param3) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			List<Tipo_ammortamentoBulk> result = ((Tipo_ammortamentoComponent)componentObj).findTipoAmmortamento(param0,param1,param2,param3);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException  e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		}
	}

	@Override
	public List<TipoAmmCatGruppoDto> findTipoAmmortamento(UserContext param0, Integer param1) throws ComponentException, RemoteException {
		pre_component_invocation(param0,componentObj);
		try {
			List<TipoAmmCatGruppoDto> result = ((Tipo_ammortamentoComponent)componentObj).findTipoAmmortamento(param0,param1);
			component_invocation_succes(param0,componentObj);
			return result;

		} catch (RuntimeException  e) {
			throw uncaughtRuntimeException(param0,componentObj,e);
		}
	}
}
