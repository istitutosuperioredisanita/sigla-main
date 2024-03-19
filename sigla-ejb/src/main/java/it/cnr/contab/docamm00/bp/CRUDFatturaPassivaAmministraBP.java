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

package it.cnr.contab.docamm00.bp;

import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Gestisce le catene di elementi correlate con la fattura passiva in uso.
 */
public class CRUDFatturaPassivaAmministraBP extends CRUDFatturaPassivaIBP {
    public CRUDFatturaPassivaAmministraBP() {
        super();
    }

    public CRUDFatturaPassivaAmministraBP(String function) throws it.cnr.jada.action.BusinessProcessException {
        super(function);
    }

    public void create(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        if (getModel() instanceof Fattura_passivaBulk)
            ((Fattura_passivaBulk)getModel()).setFromAmministra(Boolean.TRUE);
        super.create(context);
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk)throws BusinessProcessException {
        if (oggettobulk instanceof Fattura_passivaBulk)
            ((Fattura_passivaBulk)oggettobulk).setFromAmministra(Boolean.TRUE);
        return super.initializeModelForEdit( actioncontext,oggettobulk);
    }

    @Override
    public boolean isInputReadonlyFieldName(String fieldName) {
        final List<String> fieldNames = Arrays.asList("dt_da_competenza_coge", "dt_a_competenza_coge");
        if (Optional.ofNullable(fieldName).filter(s -> fieldNames.contains(s)).isPresent()) {
            return Boolean.FALSE;
        }
        return super.isInputReadonlyFieldName(fieldName);
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return Boolean.TRUE;
    }
}
