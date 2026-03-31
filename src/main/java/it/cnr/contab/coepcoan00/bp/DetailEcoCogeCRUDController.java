/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailEcoCogeBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.util.EuroFormat;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.CollapsableDetailCRUDController;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.jsp.TableCustomizer;

import jakarta.servlet.jsp.JspWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DetailEcoCogeCRUDController extends CollapsableDetailCRUDController implements TableCustomizer {
    public DetailEcoCogeCRUDController(Class class1, FormController formcontroller) {
        this(
                "Dati Coge Coan",
                class1,
                "righeEconomica",
                formcontroller
        );
    }
    public DetailEcoCogeCRUDController(String s, Class class1, String s1, FormController formcontroller) {
        super(s, class1, s1, formcontroller);
        this.setCollapsed(Boolean.FALSE);
    }

    public DetailEcoCogeCRUDController(String s, Class class1, String s1, FormController formcontroller, boolean flag) {
        super(s, class1, s1, formcontroller, flag);
    }

    @Override
    public String getRowStyle(Object obj) {
        return null;
    }

    @Override
    public boolean isRowEnabled(Object obj) {
        return true;
    }

    @Override
    public boolean isRowReadonly(Object obj) {
        return false;
    }

    @Override
    public String getTableClass() {
        return null;
    }
    @Override
    public String getRowCSSClass(Object obj, boolean even) {
        return null;
    };

    @Override
    protected void validate(ActionContext actioncontext, OggettoBulk oggettobulk) throws ValidationException {
        super.validate(actioncontext, oggettobulk);
        IDocumentoDetailAnaCogeBulk model = (IDocumentoDetailAnaCogeBulk)oggettobulk;
        if (model.getFather().getVoce_ep().isAnaliticaEnabled())
            if (model.getVoce_analitica()==null || model.getVoce_analitica().getCd_voce_ana()==null)
                throw new ValidationException("E' obbligatorio indicare la voce analitica!");
    }

    public void writeTfoot(JspWriter jspWriter) throws IOException {
        final EuroFormat euroFormat = new EuroFormat();
        String columnSetName = "default";
        if (Optional.ofNullable(this.getParentModel()).filter(IDocumentoDetailEcoCogeBulk.class::isInstance)
                .map(IDocumentoDetailEcoCogeBulk.class::cast)
                .map(IDocumentoDetailEcoCogeBulk::getVoce_ep)
                .map(el->!el.isAnaliticaEnabled())
                .orElse(Boolean.FALSE))
            columnSetName = "novoceanalitica";
        final long numberOfColspan = Collections.list(BulkInfo.getBulkInfo(this.getModelClass())
                .getColumnFieldProperties(columnSetName)).stream().count();
        final List<IDocumentoDetailAnaCogeBulk> detailAnaCogeBulks = getDetails();
        if (Optional.ofNullable(detailAnaCogeBulks).map(detailAnaCogeBulks1 -> !detailAnaCogeBulks1.isEmpty()).orElse(Boolean.FALSE) ) {
            final BigDecimal totalMovimento = detailAnaCogeBulks.stream()
                    .map(el->Optional.ofNullable(el.getImporto()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            jspWriter.println("<tfoot class=\"bg-info\">");
            jspWriter.println("<tr>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\"  colspan=\"" + numberOfColspan + "\" align=\"right\">");
            jspWriter.println("<span>Totale:</span>");
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(totalMovimento));
            jspWriter.println("</td>");
            jspWriter.println("</tr>");
            jspWriter.println("</tfoot>");
        }
    }
}
