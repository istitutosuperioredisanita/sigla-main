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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.jada.bulk.FieldValidationMap;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public interface IDocumentoDetailEcoCogeBulk  {
    it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk getScadenzaDocumentoContabile();

    Integer getEsercizio();

    IDocumentoCogeBulk getFather();

    default ContoBulk getVoce_ep() {
        return null;
    }

    default void setVoce_ep(ContoBulk voce_ep) {}

    default java.util.List<IDocumentoDetailAnaCogeBulk> getChildrenAna() {
        return new ArrayList<>();
    }

    default void clearChildrenAna(){};

    BigDecimal getImCostoEco();

    BigDecimal getImCostoEcoRipartito();

    BigDecimal getImCostoEcoDaRipartire();

    void writeFormField(JspWriter jspwriter, String s, int i, FieldValidationMap fieldvalidationmap, boolean isBootstrap) throws IOException;
}
