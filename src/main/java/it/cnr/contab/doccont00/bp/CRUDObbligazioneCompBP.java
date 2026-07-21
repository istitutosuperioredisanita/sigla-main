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

package it.cnr.contab.doccont00.bp;

import it.cnr.contab.doccont00.core.bulk.AllegatoObbligazioneBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.config.StoragePropertyNames;

import java.util.List;
import java.util.Optional;

/**
 * Business Process che gestisce le attività di CRUD per l'entita' Obbligazione
 */

public class CRUDObbligazioneCompBP extends CRUDObbligazioneBP {

    private boolean isEnabledAllegati = Boolean.FALSE;
    public CRUDObbligazioneCompBP() {
        super();

    }

    public CRUDObbligazioneCompBP(String function) {
        super(function);

    }



    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        try {
            super.init(config,actioncontext);

            isEnabledAllegati= Utility.createConfigurazioneCnrComponentSession().isEnabledAllegatiObbligazioni(actioncontext.getUserContext());

        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
        super.init(config, actioncontext);
    }

    @Override
    public String getAllegatiFormName() {
        if (this.getCrudArchivioAllegati().getModel()!=null && !this.getCrudArchivioAllegati().getModel().isNew())
            if (!isPossibileModifica((AllegatoGenericoBulk)this.getCrudArchivioAllegati().getModel()))
                return "readonly-comp";
        return "archivioAllegati-comp";
    }

    protected void completeAllegato(AllegatoObbligazioneBulk allegato, StorageObject storageObject) throws ApplicationException {

        Optional.ofNullable(storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()))
                .map(strings -> strings.stream())
                .ifPresent(stringStream -> {
                    stringStream
                            .filter(s -> AllegatoObbligazioneBulk.aspectNamesKeys.get(s) != null)
                            .findFirst()
                            .ifPresent(s -> (( AllegatoObbligazioneBulk) allegato).setAspectName(s));
                });
        super.completeAllegato(allegato, storageObject);
    }
}
