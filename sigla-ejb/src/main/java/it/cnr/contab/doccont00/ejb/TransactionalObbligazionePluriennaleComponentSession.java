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

package it.cnr.contab.doccont00.ejb;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.doccont00.core.DatiFinanziariScadenzeDTO;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.comp.ComponentException;

import java.rmi.RemoteException;
import java.util.List;

public class TransactionalObbligazionePluriennaleComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements ObbligazionePluriennaleComponentSession {
public void aggiornaCogeCoanInDifferita(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1, java.util.Map param2) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("aggiornaCogeCoanInDifferita",new Object[] {
			param0,
			param1,
			param2 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void aggiornaSaldiInDifferita(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1, java.util.Map param2, it.cnr.contab.doccont00.core.bulk.OptionRequestParameter param3) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("aggiornaSaldiInDifferita",new Object[] {
			param0,
			param1,
			param2,
			param3 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk aggiornaScadenzaSuccessivaObbligazione(UserContext param0, it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk)invoke("aggiornaScadenzaSuccessivaObbligazione",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk calcolaDispCassaAnniSuccessivi(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("calcolaDispCassaAnniSuccessivi",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void callRiportaAvanti(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("callRiportaAvanti",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void callRiportaIndietro(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("callRiportaIndietro",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void cancellaObbligazioneProvvisoria(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("cancellaObbligazioneProvvisoria",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2) throws RemoteException,it.cnr.jada.comp.ComponentException {
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
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2, it.cnr.jada.bulk.OggettoBulk param3, String param4) throws RemoteException,it.cnr.jada.comp.ComponentException {
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
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk confermaObbligazioneProvvisoria(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("confermaObbligazioneProvvisoria",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("creaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk[] creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk[])invoke("creaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("eliminaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("eliminaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.contab.doccont00.ordine.bulk.OrdineBulk findOrdineFor(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.doccont00.ordine.bulk.OrdineBulk)invoke("findOrdineFor",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk generaDettagliScadenzaObbligazione(UserContext param0, ObbligazioneBulk param1, it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk param2) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("generaDettagliScadenzaObbligazione",new Object[] {
			param0,
			param1,
			param2 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public java.util.List generaProspettoSpeseObbligazione(UserContext param0, java.util.List param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (java.util.List)invoke("generaProspettoSpeseObbligazione",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerInserimento(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerInserimento",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerModifica",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk[] inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk[])invoke("inizializzaBulkPerModifica",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicerca(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerRicerca",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicercaLibera(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerRicercaLibera",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("inizializzaBulkPerStampa",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk listaCapitoliPerCdsVoce(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("listaCapitoliPerCdsVoce",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public java.util.Vector listaCdrPerCapitoli(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (java.util.Vector)invoke("listaCdrPerCapitoli",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public java.util.Vector listaLineeAttivitaPerCapitoliCdr(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (java.util.Vector)invoke("listaLineeAttivitaPerCapitoliCdr",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void lockScadenza(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("lockScadenza",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("modificaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk[] modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk[])invoke("modificaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk modificaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, java.math.BigDecimal param2, boolean param3) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)invoke("modificaScadenzaInAutomatico",new Object[] {
			param0,
			param1,
			param2,
			new Boolean(param3) });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.jada.bulk.OggettoBulk stampaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.jada.bulk.OggettoBulk)invoke("stampaConBulk",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk stornaObbligazioneDefinitiva(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("stornaObbligazioneDefinitiva",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void verificaNuovaLineaAttivita(UserContext param0, it.cnr.contab.config00.latt.bulk.WorkpackageBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("verificaNuovaLineaAttivita",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void verificaObbligazione(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("verificaObbligazione",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk verificaScadenzarioObbligazione(UserContext param0, it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("verificaScadenzarioObbligazione",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public it.cnr.contab.config00.esercizio.bulk.EsercizioBulk verificaStatoEsercizio(UserContext param0, String param1, Integer param2) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.config00.esercizio.bulk.EsercizioBulk)invoke("verificaStatoEsercizio",new Object[] {
			param0,
			param1,
			param2 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void verificaTestataObbligazione(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("verificaTestataObbligazione",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void validaContratto(UserContext param0, ObbligazioneBulk param1, it.cnr.contab.config00.contratto.bulk.ContrattoBulk param2, it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("validaContratto",new Object[] {
			param0,
			param1,
			param2,
			param3 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk riportaSelezioneVoci(UserContext param0, ObbligazioneBulk param1, java.util.List param2) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("riportaSelezioneVoci",new Object[] {
			param0,
			param1,
			param2 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk creaScadenzaResiduale(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("creaScadenzaResiduale",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public ObbligazioneBulk validaImputazioneFinanziaria(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (ObbligazioneBulk)invoke("validaImputazioneFinanziaria",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}


public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk sdoppiaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, DatiFinanziariScadenzeDTO dati)  throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)invoke("sdoppiaScadenzaInAutomatico",new Object[] {
			param0,
			param1,
			dati});
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}

public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk sdoppiaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, java.math.BigDecimal param2)  throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)invoke("sdoppiaScadenzaInAutomatico",new Object[] {
			param0,
			param1,
			param2 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
	public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk sdoppiaScadenzaInAutomaticoLight(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, DatiFinanziariScadenzeDTO dati)  throws RemoteException,it.cnr.jada.comp.ComponentException {
		try {
			return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)invoke("sdoppiaScadenzaInAutomaticoLight",new Object[] {
					param0,
					param1,
					dati});
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(it.cnr.jada.comp.ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}


public PrimaryKeyHashtable getOldRipartizioneCdrVoceLinea(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (PrimaryKeyHashtable)invoke("getOldRipartizioneCdrVoceLinea",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void cancellaObbligazioneModTemporanea(UserContext param0, it.cnr.contab.doccont00.core.bulk.Obbligazione_modificaBulk param1) throws it.cnr.jada.comp.ComponentException,it.cnr.jada.persistency.PersistencyException,it.cnr.jada.persistency.IntrospectionException, RemoteException {
	try {
		invoke("cancellaObbligazioneModTemporanea",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void validaIncaricoRepertorio(UserContext param0, ObbligazioneBulk param1, it.cnr.contab.incarichi00.bulk.Incarichi_repertorioBulk param2, it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("validaIncaricoRepertorio",new Object[] {
			param0,
			param1,
			param2,
			param3 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public java.util.List<it.cnr.contab.prevent00.bulk.V_assestatoBulk> listaAssestatoSpese (UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException,it.cnr.jada.persistency.PersistencyException {
	try {
		return (java.util.List<it.cnr.contab.prevent00.bulk.V_assestatoBulk>)invoke("listaAssestatoSpese",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public boolean existAssElementoVoceNew(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		return (Boolean)invoke("existAssElementoVoceNew",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void callRiportaAvantiRequiresNew(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("callRiportaAvanti",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
public void callRiportaIndietroRequiresNew(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
	try {
		invoke("callRiportaIndietro",new Object[] {
			param0,
			param1 });
	} catch(RemoteException e) {
		throw e;
	} catch(java.lang.reflect.InvocationTargetException e) {
		try {
			throw e.getTargetException();
		} catch(it.cnr.jada.comp.ComponentException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new RemoteException("Uncaugth exception",ex);
		}
	}
}
	public ObbligazioneBulk findObbligazione(UserContext param0, ObbligazioneBulk param1) throws RemoteException,it.cnr.jada.comp.ComponentException {
		try {
			return (ObbligazioneBulk)invoke("findObbligazione",new Object[] {
					param0,
					param1 });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(it.cnr.jada.comp.ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}

	@Override
	public List<Obbligazione_pluriennaleBulk> findObbligazioniPluriennali(UserContext uc, int esercizio) throws ComponentException, RemoteException {
		try {
			return (List<Obbligazione_pluriennaleBulk>)invoke("findObbligazioniPluriennali",new Object[] {
					uc,
					esercizio });
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(it.cnr.jada.comp.ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}

	@Override
	public ObbligazioneBulk createObbligazioneNew(UserContext usercontext, Obbligazione_pluriennaleBulk pluriennaleBulk, Integer esercizio,WorkpackageBulk gaeIniziale) throws ComponentException, RemoteException {
		try {
			return (ObbligazioneBulk)invoke("createObbligazioneNew",new Object[] {
					usercontext,
					pluriennaleBulk,
					esercizio,
					gaeIniziale});
		} catch(RemoteException e) {
			throw e;
		} catch(java.lang.reflect.InvocationTargetException e) {
			try {
				throw e.getTargetException();
			} catch(it.cnr.jada.comp.ComponentException ex) {
				throw ex;
			} catch(Throwable ex) {
				throw new RemoteException("Uncaugth exception",ex);
			}
		}
	}
}
