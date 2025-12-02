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

package it.cnr.contab.utenze00.bp;

import it.cnr.contab.utente00.ejb.RuoloComponentSession;
import it.cnr.contab.utenze00.bulk.*;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.action.*;

import java.rmi.RemoteException;

public class CRUDTipoRuoloBP extends SimpleCRUDBP {
	private final SimpleDetailCRUDController crudPrivilegi_disponibili = new SimpleDetailCRUDController("Privilegi_disponibili",PrivilegioBulk.class,"privilegi_disponibili",this){
        public void setFilter(ActionContext actioncontext, CompoundFindClause compoundfindclause) {
            compoundfindclauseAccessiDisponibili = compoundfindclause;
            CRUDTipoRuoloBP bp = (CRUDTipoRuoloBP) actioncontext.getBusinessProcess();
            Tipo_ruoloBulk tipo_ruolo = (Tipo_ruoloBulk) bp.getModel();
            tipo_ruolo.resetPrivilegi();
            try {
                bp.setModel(actioncontext, ((RuoloComponentSession) createComponentSession()).
                        cercaAccessiDisponibili(actioncontext.getUserContext(), tipo_ruolo, compoundfindclause));

            } catch (BusinessProcessException e) {
                handleException(e);
            } catch (ComponentException e) {
                handleException(e);
            } catch (RemoteException e) {
                handleException(e);
            }
            super.setFilter(actioncontext, compoundfindclause);
        };

        public boolean isFiltered() {
            return compoundfindclauseAccessiDisponibili != null;
        };
    };
	private final SimpleDetailCRUDController crudPrivilegi = new SimpleDetailCRUDController("Privilegi",PrivilegioBulk.class,"privilegi",this);
    private CompoundFindClause compoundfindclauseAccessiDisponibili = null;
public CRUDTipoRuoloBP() throws BusinessProcessException {
	super();

}
public CRUDTipoRuoloBP( String function ) throws BusinessProcessException {
	super(function);

}

/**
 * Restituisce il Controller che gestisce il dettaglio dei Privilegi già assegnati ad un Tipo Ruolo
 * @return it.cnr.jada.util.action.SimpleDetailCRUDController controller
 */
public final it.cnr.jada.util.action.SimpleDetailCRUDController getCrudPrivilegi() {
	return crudPrivilegi;
}
/**
 * Restituisce il Controller che gestisce il dettaglio dei Privilegi ancora disponibili per un Tipo Ruolo
 * @return it.cnr.jada.util.action.SimpleDetailCRUDController controller
 */
public final it.cnr.jada.util.action.SimpleDetailCRUDController getCrudPrivilegi_disponibili() {
	return crudPrivilegi_disponibili;
}

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        compoundfindclauseAccessiDisponibili = null;
        return super.initializeModelForInsert(actioncontext, oggettobulk);
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        compoundfindclauseAccessiDisponibili = null;
        return super.initializeModelForEdit(actioncontext, oggettobulk);
    }

    @Override
    public OggettoBulk initializeModelForSearch(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        compoundfindclauseAccessiDisponibili = null;
        return super.initializeModelForSearch(actioncontext, oggettobulk);
    }
}
