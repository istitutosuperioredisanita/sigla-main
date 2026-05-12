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

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcess;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.FieldValidationMap;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.action.CollapsableDetailCRUDController;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.jsp.Button;

import java.util.Arrays;
import java.util.stream.Stream;

public interface IDocAmmEconomicaBP {
    String getTab(String tabName);

    FormController getController();

    int getStatus();

    boolean isDirty();

    FieldValidationMap getFieldValidationMap();

    BusinessProcess getParentRoot();

    void setDirty(boolean dirty);

    void setMessage(int status, String message);

    default OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        return oggettobulk;
    }

    void setModel(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException;

    void save(ActionContext actioncontext) throws ValidationException, BusinessProcessException;

    CollapsableDetailCRUDController getMovimentiDare();

    CollapsableDetailCRUDController getMovimentiAvere();

    OggettoBulk getEconomicaModel();

    boolean isButtonGeneraScritturaVisible();

    static Button[] addPartitario(Button[] buttons, boolean attivaEconomica, boolean isEditing, OggettoBulk model) {
        if (attivaEconomica) {
            return Stream.concat(Arrays.stream(buttons),
                    Stream.of(
                            new Button(Config.getHandler().getProperties(IDocAmmEconomicaBP.class), "CRUDToolbar.partitario")
                    )).toArray(Button[]::new);
        }
        return buttons;
    }

    boolean isAttivaEconomica();

    boolean isAttivaFinanziaria();
}
