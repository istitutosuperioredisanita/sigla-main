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

package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bulk.CNRUserInfo;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;

/**
 * Business Process che gestisce le attività di Ricerca per l'oggetto Movimento_cogeBulk
 */

public class RicercaMovContCogeBP extends it.cnr.jada.util.action.SimpleCRUDBP {

	private Unita_organizzativaBulk uoScrivania;

    public RicercaMovContCogeBP() {
        super();
    }

    public RicercaMovContCogeBP(String function) {
        super(function);
    }

    /**
     * Imposta la status del BP a Ricerca e inizializza il modello Movimento_cogeBulk per ricerca
     */

    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        super.init(config, context);
        setStatus(SEARCH);
        try {
			uoScrivania = CNRUserInfo.getUnita_organizzativa(context);
            resetForSearch(context);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }
	public boolean isUoEnte(){
		return (uoScrivania.getCd_tipo_unita().compareTo(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE)==0);
	}

    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk) throws BusinessProcessException {
        try {
            bulk = super.initializeModelForEdit(context, bulk);
            return bulk.initializeForEdit(this, context);
        } catch (Throwable e) {
            throw new it.cnr.jada.action.BusinessProcessException(e);
        }
    }

    /**
     * Nascondo il bottone Elimina
     */

    public boolean isDeleteButtonHidden() {
        return true;
    }

    /**
     * Nascondo il bottone Nuovo
     */

    public boolean isNewButtonHidden() {
        return true;
    }

    /**
     * Nascondo il bottone Salva
     */

    public boolean isSaveButtonHidden() {
        return true;
    }
}
