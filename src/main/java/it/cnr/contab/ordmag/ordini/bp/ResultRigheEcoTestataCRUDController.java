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

package it.cnr.contab.ordmag.ordini.bp;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqEcoBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaEcoBulk;
import it.cnr.contab.util.EuroFormat;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.util.action.CollapsableDetailCRUDController;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.jsp.TableCustomizer;

import jakarta.servlet.jsp.JspWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResultRigheEcoTestataCRUDController extends CollapsableDetailCRUDController implements TableCustomizer {
    public ResultRigheEcoTestataCRUDController(String s, Class class1, String s1, FormController formcontroller) {
        super(s, class1, s1, formcontroller);
        this.setCollapsed(Boolean.FALSE);
    }

    public ResultRigheEcoTestataCRUDController(String s, Class class1, String s1, FormController formcontroller, boolean flag) {
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

    public void writeTfoot(JspWriter jspWriter) throws IOException {
        final EuroFormat euroFormat = new EuroFormat();
        final long numberOfColspan = Collections.list(BulkInfo.getBulkInfo(this.getModelClass())
                .getColumnFieldProperties("default")).stream().count();
        final List<OrdineAcqEcoBulk> detailAnaCogeBulks = getDetails();
        final BigDecimal totalEvasoForzatamanente = Optional.ofNullable(getParentModel())
                .filter(OrdineAcqBulk.class::isInstance)
                .map(OrdineAcqBulk.class::cast)
                .map(OrdineAcqBulk::getImEvasoForzatamente)
                .orElse(BigDecimal.ZERO);

        if (Optional.ofNullable(detailAnaCogeBulks).map(detailAnaCogeBulks1 -> !detailAnaCogeBulks1.isEmpty()).orElse(Boolean.FALSE) ||
                totalEvasoForzatamanente.compareTo(BigDecimal.ZERO)!=0) {
            final BigDecimal totalMovimento = detailAnaCogeBulks.stream()
                    .map(el->Optional.ofNullable(el.getImporto()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(totalEvasoForzatamanente);

            jspWriter.println("<tfoot class=\"bg-info\">");
            if (totalEvasoForzatamanente.compareTo(BigDecimal.ZERO)!=0) {
                jspWriter.println("<tr>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\"  colspan=\"" + numberOfColspan + "\" align=\"right\">");
                jspWriter.println("<span>Consegne Evase Forzatamente:</span>");
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(totalEvasoForzatamanente));
                jspWriter.println("</td>");
                jspWriter.println("</tr>");
            }
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
