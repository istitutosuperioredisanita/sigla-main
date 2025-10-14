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

package it.cnr.contab.inventario01.ejb;

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Buono_carico_scaricoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import java.rmi.RemoteException;

public class TransactionalNumerazioneTempDocTRComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements NumerazioneTempDocTRComponentSession {
	public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.util.RemoteIterator)invoke("cerca",new Object[] {
				param0,
				param1,
				param2 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2, it.cnr.jada.bulk.OggettoBulk param3, String param4) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.util.RemoteIterator)invoke("cerca",new Object[] {
				param0,
				param1,
				param2,
				param3,
				param4 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("creaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk[] creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk[])invoke("creaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			invoke("eliminaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
		try {
			invoke("eliminaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerInserimento(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerInserimento",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerModifica",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk[] inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk[])invoke("inizializzaBulkPerModifica",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicerca(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerRicerca",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicercaLibera(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerRicercaLibera",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk)invoke("modificaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
	public it.cnr.jada.bulk.OggettoBulk[] modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
		try {
			return (it.cnr.jada.bulk.OggettoBulk[])invoke("modificaConBulk",new Object[] {
				param0,
				param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}

	@Override
	public Long getNextTempPG(UserContext param0, Doc_trasporto_rientroBulk param1) throws ComponentException, RemoteException {
		try {
			return (java.lang.Long)invoke("getNextTempPG",new Object[] {
					param0,
					param1 });
		} catch(java.rmi.RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(it.cnr.jada.comp.ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new java.rmi.RemoteException("Uncaugth exception",ex);
			}
		}
	}
}
