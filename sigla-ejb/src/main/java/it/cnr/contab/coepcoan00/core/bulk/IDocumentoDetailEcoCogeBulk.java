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

public interface IDocumentoDetailEcoCogeBulk  {
    it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk getScadenzaDocumentoContabile();

    Integer getEsercizio();

    IDocumentoCogeBulk getFather();

    ContoBulk getVoce_ep();

    void setVoce_ep(ContoBulk voce_ep);

    java.util.List<IDocumentoDetailAnaCogeBulk> getChildrenAna();

    void clearChildrenAna();

    BigDecimal getImCostoEco();

    BigDecimal getImCostoEcoRipartito();

    BigDecimal getImCostoEcoDaRipartire();

    java.sql.Timestamp getDt_da_competenza_coge();

    java.sql.Timestamp getDt_a_competenza_coge();

    void writeFormField(JspWriter jspwriter, String s, int i, FieldValidationMap fieldvalidationmap, boolean isBootstrap) throws IOException;
}
