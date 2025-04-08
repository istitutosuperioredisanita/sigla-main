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
import it.cnr.contab.ordmag.ordini.bulk.FatturaOrdineBulk;
import it.cnr.contab.util.EuroFormat;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.Selection;
import it.cnr.jada.util.jsp.TableCustomizer;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert the type's description here.
 * Creation date: (10/09/2017 11:32:54 AM)
 *
 * @author: Marco Spasiano
 */
public class OrdiniCRUDController extends it.cnr.jada.util.action.CollapsableDetailCRUDController implements TableCustomizer {
    private boolean rettificheCollapse = true;
    private static BigDecimal range = new BigDecimal(2.02);
    public boolean isRettificheCollapse() {
        return rettificheCollapse;
    }

    public void setRettificheCollapse(boolean rettificheCollapse) {
        this.rettificheCollapse = rettificheCollapse;
    }
    /**
     * OrdiniCRUDController constructor comment.
     *
     * @param name             java.lang.String
     * @param modelClass       java.lang.Class
     * @param listPropertyName java.lang.String
     * @param parent           it.cnr.jada.util.action.FormController
     */
    public OrdiniCRUDController(String name, Class modelClass, String listPropertyName, it.cnr.jada.util.action.FormController parent) {
        super(name, modelClass, listPropertyName, parent);
        setCollapsed(false);
    }

    /**
     * Restituisce true se è possibile aggiungere nuovi elementi
     */
    public boolean isGrowable() {
        return false;
    }

    /**
     * Restituisce true se è possibile aggiungere nuovi elementi
     */
    public boolean isShrinkable() {
        return	super.isShrinkable() && !((it.cnr.jada.util.action.CRUDBP)getParentController()).isSearching();
    }

    @Override
    public String getRowStyle(Object obj) {
        return null;
    }

    @Override
    public String getRowCSSClass(Object obj, boolean even) {
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
    public Selection setSelection(ActionContext actioncontext) throws ValidationException, BusinessProcessException {
        Optional.ofNullable(getParentController())
                .filter(CRUDFatturaPassivaBP.class::isInstance)
                .map(CRUDFatturaPassivaBP.class::cast)
                .ifPresent(crudFatturaPassivaBP -> {
                    crudFatturaPassivaBP.getFatturaOrdiniController().setRettificheCollapse(false);
                });
        return super.setSelection(actioncontext);
    }

    private Map<String, BigDecimal> differenze() {
        final Optional<Fattura_passivaBulk> fattura_passiva = Optional.ofNullable(getParentController())
                .filter(CRUDFatturaPassivaBP.class::isInstance)
                .map(CRUDFatturaPassivaBP.class::cast)
                .map(crudFatturaPassivaBP -> crudFatturaPassivaBP.getModel()
                )
                .filter(Fattura_passivaBulk.class::isInstance)
                .map(Fattura_passivaBulk.class::cast);
        final List<FatturaOrdineBulk> fatturaOrdineBulks = getDetails();
        if (!fatturaOrdineBulks.isEmpty()) {
            final BigDecimal totaleImponibile = fatturaOrdineBulks
                    .stream()
                    .map(fatturaOrdineBulk -> Optional.ofNullable(fatturaOrdineBulk.getImImponibileRettificato()).orElse(fatturaOrdineBulk.getImImponibile()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            final BigDecimal totaleIva = fatturaOrdineBulks
                    .stream()
                    .map(fatturaOrdineBulk -> Optional.ofNullable(fatturaOrdineBulk.getImIvaRettificata()).orElse(fatturaOrdineBulk.getImIva()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal totaleImponibilePerNotaCredito = fatturaOrdineBulks
                    .stream()
                    .map(FatturaOrdineBulk::getImponibilePerRigaFattura)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            final BigDecimal totaleIvaPerNotaCredito = fatturaOrdineBulks
                    .stream()
                    .map(FatturaOrdineBulk::getIvaPerRigaFattura)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal differenzaImponibile = fattura_passiva.get()
                    .getTotaleImponibileFatturaElettronica().subtract(totaleImponibilePerNotaCredito);
            final BigDecimal differenzaIva = fattura_passiva.get()
                    .getTotaleIvaFatturaElettronica().subtract(totaleIvaPerNotaCredito);
            return Stream.of(
                    new AbstractMap.SimpleEntry<>("totaleImponibile", totaleImponibile),
                    new AbstractMap.SimpleEntry<>("totaleIva", totaleIva),
                    new AbstractMap.SimpleEntry<>("totaleImponibilePerNotaCredito", totaleImponibilePerNotaCredito),
                    new AbstractMap.SimpleEntry<>("totaleIvaPerNotaCredito", totaleIvaPerNotaCredito),
                    new AbstractMap.SimpleEntry<>("differenzaImponibile", differenzaImponibile),
                    new AbstractMap.SimpleEntry<>("differenzaIva", differenzaIva)
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Collections.emptyMap();
    }
    @Override
    public void writeTfoot(JspWriter jspWriter) throws IOException {
        final EuroFormat euroFormat = new EuroFormat();
        final long numberOfColspan = Collections.list(BulkInfo.getBulkInfo(this.getModelClass())
                .getColumnFieldProperties()).stream().count() - 2;
        final Optional<Fattura_passivaBulk> fattura_passiva = Optional.ofNullable(getParentController())
                .filter(CRUDFatturaPassivaBP.class::isInstance)
                .map(CRUDFatturaPassivaBP.class::cast)
                .map(crudFatturaPassivaBP -> crudFatturaPassivaBP.getModel()
                )
                .filter(Fattura_passivaBulk.class::isInstance)
                .map(Fattura_passivaBulk.class::cast);
        final Map<String, BigDecimal> differenze = differenze();
        if (!differenze.isEmpty()) {

            jspWriter.println("<tfoot class=\"bg-info\">");
            jspWriter.println("<tr>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\"  colspan=\"" + numberOfColspan + "\" align=\"right\">");
            jspWriter.println("<span>Totali Ordine:</span>");
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(differenze.get("totaleImponibile")));
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(differenze.get("totaleIva")));
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(differenze.get("totaleImponibile").add(differenze.get("totaleIva"))));
            jspWriter.println("</td>");
            jspWriter.println("</tr>");

            if (differenze.get("totaleImponibile").compareTo(differenze.get("totaleImponibilePerNotaCredito")) != 0 ||
                    differenze.get("totaleIva").compareTo(differenze.get("totaleIvaPerNotaCredito")) != 0){
                jspWriter.println("<tr>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\"  colspan=\"" + numberOfColspan + "\" align=\"right\">");
                jspWriter.println("<span>Totali Ordine Per Nota Credito:</span>");
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("totaleImponibilePerNotaCredito")));
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("totaleIvaPerNotaCredito")));
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("totaleImponibilePerNotaCredito").add(differenze.get("totaleIvaPerNotaCredito"))));
                jspWriter.println("</td>");
                jspWriter.println("</tr>");
            }

            jspWriter.println("<tr>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\"  colspan=\"" + numberOfColspan + "\" align=\"right\">");
            jspWriter.println("<span>Importi Fattura:</span>");
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(fattura_passiva.get()
                    .getTotaleImponibileFatturaElettronica()));
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(fattura_passiva.get()
                    .getTotaleIvaFatturaElettronica()));
            jspWriter.println("</td>");
            jspWriter.println("<td class=\"TableHeader text-white font-weight-bold\" align=\"right\">");
            jspWriter.print(euroFormat.format(fattura_passiva.get()
                    .getTotaleImponibileFatturaElettronica().add(fattura_passiva.get()
                            .getTotaleIvaFatturaElettronica())));
            jspWriter.println("</td>");
            jspWriter.println("</tr>");
            if (differenze.get("differenzaImponibile").compareTo(BigDecimal.ZERO) != 0 || differenze.get("differenzaIva").compareTo(BigDecimal.ZERO) != 0){
                jspWriter.println("<tr>");
                jspWriter.println("<td class=\"TableHeader text-white bg-danger font-weight-bold\" colspan=\"" + numberOfColspan +"\" align=\"right\">");
                jspWriter.println("<span>Differenze:</span>");
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white bg-danger font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("differenzaImponibile")));
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white bg-danger font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("differenzaIva")));
                jspWriter.println("</td>");
                jspWriter.println("<td class=\"TableHeader text-white bg-danger font-weight-bold\" align=\"right\">");
                jspWriter.print(euroFormat.format(differenze.get("differenzaImponibile").add(differenze.get("differenzaIva"))));
                jspWriter.println("</td>");
                jspWriter.println("</tr>");
            }
            jspWriter.println("</tfoot>");
        }
    }


    @Override
    public void writeHTMLToolbar(
            javax.servlet.jsp.PageContext context,
            boolean reset,
            boolean find,
            boolean delete, boolean closedToolbar) throws java.io.IOException, javax.servlet.ServletException {

        super.writeHTMLToolbar(context, reset, find, delete, false);
        boolean isFromBootstrap = HttpActionContext.isFromBootstrap(context);
        String command = null;
        if (getParentController() != null)
            command = "javascript:submitForm('doSelezionaOrdini')";
        it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                context,
                isFromBootstrap ? "fa fa-fw fa-bolt" : "img/history16.gif",
                !(isInputReadonly() ||  ((CRUDFatturaPassivaBP) getParentController()).isSearching()) ? command : null,
                true,
                "Seleziona Ordini",
                "btn-sm btn-outline-primary btn-title",
                isFromBootstrap);
        command = null;
        final Map<String, BigDecimal> differenze = differenze();
        if (getParentController() != null && !differenze.isEmpty()
                && ((differenze.get("differenzaImponibile").compareTo(range.negate()) >= 0
                && differenze.get("differenzaImponibile").compareTo(range) <= 0) || (
                     differenze.get("totaleImponibilePerNotaCredito").equals(differenze.get("totaleImponibile"))
                ))
            )
            command = "javascript:submitForm('doConfermaRiscontroAValore')";
        it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                context,
                isFromBootstrap ? "fa fa-fw fa-lock" : "img/history16.gif",
                !(isInputReadonly() || getDetails().isEmpty() || ((CRUDFatturaPassivaBP) getParentController()).isSearching()) ? command : null,
                true,
                "Fine riscontro a valore",
                "btn-sm btn-outline-primary btn-title",
                isFromBootstrap);

        super.closeButtonGROUPToolbar(context);
    }
}
