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

import it.cnr.contab.doccont00.core.bulk.AccertamentoBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_pluriennaleBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.comp.ComponentException;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

public class TransactionalAccertamentoPluriennaleComponentSession extends it.cnr.jada.ejb.TransactionalCRUDComponentSession implements AccertamentoPluriennaleComponentSession {
    public void aggiornaCogeCoanInDifferita(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1, java.util.Map param2) throws RemoteException, ComponentException {
        try {
            invoke("aggiornaCogeCoanInDifferita", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void aggiornaSaldiInDifferita(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1, java.util.Map param2, it.cnr.contab.doccont00.core.bulk.OptionRequestParameter param3) throws RemoteException, ComponentException {
        try {
            invoke("aggiornaSaldiInDifferita", new Object[]{
                    param0,
                    param1,
                    param2,
                    param3});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk aggiornaScadenzarioSuccessivoAccertamento(UserContext param0, it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk param1) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("aggiornaScadenzarioSuccessivoAccertamento", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk annullaAccertamento(UserContext param0, AccertamentoBulk param1) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("annullaAccertamento", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void callRiportaAvanti(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException, ComponentException {
        try {
            invoke("callRiportaAvanti", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void callRiportaIndietro(UserContext param0, it.cnr.contab.doccont00.core.bulk.IDocumentoContabileBulk param1) throws RemoteException, ComponentException {
        try {
            invoke("callRiportaIndietro", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk cancellaDettagliScadenze(UserContext param0, AccertamentoBulk param1, it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk param2) throws RemoteException, ComponentException {
        try {
            return (it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk) invoke("cancellaDettagliScadenze", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.util.RemoteIterator) invoke("cerca", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.util.RemoteIterator cerca(UserContext param0, it.cnr.jada.persistency.sql.CompoundFindClause param1, it.cnr.jada.bulk.OggettoBulk param2, it.cnr.jada.bulk.OggettoBulk param3, String param4) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.util.RemoteIterator) invoke("cerca", new Object[]{
                    param0,
                    param1,
                    param2,
                    param3,
                    param4});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk creaAccertamentoDiSistema(UserContext param0, it.cnr.contab.doccont00.core.bulk.MandatoAccreditamento_rigaBulk param1, it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk param2) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("creaAccertamentoDiSistema", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("creaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk[] creaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk[]) invoke("creaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            invoke("eliminaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void eliminaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
        try {
            invoke("eliminaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk generaDettagliScadenzaAccertamento(UserContext param0, AccertamentoBulk param1, it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk param2) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("generaDettagliScadenzaAccertamento", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerInserimento(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("inizializzaBulkPerInserimento", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("inizializzaBulkPerModifica", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk[] inizializzaBulkPerModifica(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk[]) invoke("inizializzaBulkPerModifica", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicerca(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("inizializzaBulkPerRicerca", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerRicercaLibera(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("inizializzaBulkPerRicercaLibera", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("inizializzaBulkPerStampa", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public Vector listaCodiciNaturaPerCapitolo(UserContext param0, AccertamentoBulk param1) throws RemoteException, ComponentException {
        try {
            return (Vector) invoke("listaCodiciNaturaPerCapitolo", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public Vector listaLineeAttivitaPerCapitolo(UserContext param0, AccertamentoBulk param1) throws RemoteException, ComponentException {
        try {
            return (Vector) invoke("listaLineeAttivitaPerCapitolo", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void lockScadenza(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1) throws RemoteException, ComponentException {
        try {
            invoke("lockScadenza", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("modificaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk[] modificaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk[] param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk[]) invoke("modificaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk modificaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, java.math.BigDecimal param2, boolean param3) throws RemoteException, ComponentException {
        try {
            return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk) invoke("modificaScadenzaInAutomatico", new Object[]{
                    param0,
                    param1,
                    param2,
					Boolean.valueOf(param3)});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk modificaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, java.math.BigDecimal param2, boolean param3, Boolean aggiornaCalcoloAutomatico) throws RemoteException, ComponentException {
        try {
            return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk) invoke("modificaScadenzaInAutomatico", new Object[]{
                    param0,
                    param1,
                    param2,
					Boolean.valueOf(param3),
                    aggiornaCalcoloAutomatico});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(UserContext param0, it.cnr.jada.bulk.OggettoBulk param1) throws RemoteException, ComponentException {
        try {
            return (it.cnr.jada.bulk.OggettoBulk) invoke("stampaConBulk", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void verificaAccertamento(UserContext param0, AccertamentoBulk param1) throws RemoteException, ComponentException {
        try {
            invoke("verificaAccertamento", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk verificaScadenzarioAccertamento(UserContext param0, it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk param1) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("verificaScadenzarioAccertamento", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public AccertamentoBulk verificaScadenzarioAccertamento(UserContext param0, it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk param1, boolean param2) throws RemoteException, ComponentException {
        try {
            return (AccertamentoBulk) invoke("verificaScadenzarioAccertamento", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public Vector listaCdrPerCapitoli(UserContext param0, AccertamentoBulk param1) throws ComponentException, RemoteException {
        try {
            return (Vector) invoke("listaCdrPerCapitoli", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public void validaContratto(UserContext param0, AccertamentoBulk param1, it.cnr.contab.config00.contratto.bulk.ContrattoBulk param2, it.cnr.jada.persistency.sql.CompoundFindClause param3) throws RemoteException, ComponentException {
        try {
            invoke("validaContratto", new Object[]{
                    param0,
                    param1,
                    param2,
                    param3});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk sdoppiaScadenzaInAutomatico(UserContext param0, it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk param1, java.math.BigDecimal param2) throws RemoteException, ComponentException {
        try {
            return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk) invoke("sdoppiaScadenzaInAutomatico", new Object[]{
                    param0,
                    param1,
                    param2});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }





    public PrimaryKeyHashtable getOldRipartizioneCdrVoceLinea(UserContext param0, AccertamentoBulk param1) throws RemoteException, ComponentException {
        try {
            return (PrimaryKeyHashtable) invoke("getOldRipartizioneCdrVoceLinea", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }

    @Override
    public AccertamentoBulk aggiornaAccertamentiTemporanei(UserContext param0, AccertamentoBulk param1) throws ComponentException, RemoteException {
        try {
            return (AccertamentoBulk) invoke("aggiornaAccertamentiTemporanei", new Object[]{
                    param0,
                    param1});
        } catch (RemoteException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (ComponentException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new RemoteException("Uncaugth exception", ex);
            }
        }
    }
	public AccertamentoBulk riportaSelezioneVoci(UserContext param0, AccertamentoBulk param1, java.util.List param2) throws RemoteException, ComponentException {
		try {
			return (AccertamentoBulk)invoke("riportaSelezioneVoci",new Object[] {
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

    @Override
    public List<Accertamento_pluriennaleBulk> findAccertamentiPluriennali(UserContext uc, int esercizio) throws ComponentException, RemoteException {
        try {
            return (List<Accertamento_pluriennaleBulk>)invoke("findAccertamentiPluriennali",new Object[] {
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
    public AccertamentoBulk createAccertamentoNew(UserContext uc, Integer esercizio,Accertamento_pluriennaleBulk pluriennaleBulk) throws ComponentException,RemoteException {
        try {
            return (AccertamentoBulk) invoke("createAccertamentoNew",new Object[] {
                    uc,
                    esercizio,
                    pluriennaleBulk });
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
