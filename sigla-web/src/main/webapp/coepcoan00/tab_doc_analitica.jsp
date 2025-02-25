<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.intrastat.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.doccont00.core.bulk.*,
		it.cnr.contab.coepcoan00.core.bulk.*,
		it.cnr.contab.docamm00.bp.*"
%>
<%
    IDocAmmAnaliticaBP bp = (IDocAmmAnaliticaBP)BusinessProcess.getBusinessProcess(request);
    Scrittura_analiticaBulk scrittura =
                Optional.ofNullable(bp.getAnaliticaModel())
                    .filter(IDocumentoCogeBulk.class::isInstance)
                    .map(IDocumentoCogeBulk.class::cast)
                    .map(IDocumentoCogeBulk::getScrittura_analitica)
                    .orElse(new Scrittura_analiticaBulk());
    boolean scritturaNonAttiva =
                Optional.ofNullable(bp.getAnaliticaModel())
                    .filter(IDocumentoCogeBulk.class::isInstance)
                    .map(IDocumentoCogeBulk.class::cast)
                    .map(IDocumentoCogeBulk::getScrittura_analitica)
                    .map(spd->!spd.isScritturaAttiva())
                    .orElse(Boolean.FALSE);
%>
<% if (bp.isButtonGeneraScritturaAnaliticaVisible() || scritturaNonAttiva) { %>
<div class="btn-toolbar justify-content-between" role="toolbar" aria-label="Toolbar with button groups">
<% if (bp.isButtonGeneraScritturaAnaliticaVisible()) { %>
    <div class="btn-group mr-2" role="group">
		<% JSPUtils.button(out,
				bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-2x fa-bolt" : "img/bringback24.gif",
				bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-2x fa-bolt" : "img/bringback24.gif",
				"Genera scrittura",
				"javascript:submitForm('doGeneraScritturaAnalitica')",
				"btn-outline-primary btn-title",
				true,
				bp.getParentRoot().isBootstrap()); %>
    </div>
<% } %>
<% if (scritturaNonAttiva) { %>
    <div class="alert alert-danger" role="alert">
      Scrittura Analitica annullata!
    </div>
<% } %>
</div>
<% } %>
<div class="Panel card p-2 mb-2 card-shadow">
    <table cellpadding="2">
        <tr>
            <% scrittura.writeFormField(out, "dt_contabilizzazione", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% scrittura.writeFormField(out, "ti_istituz_commerc", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% scrittura.writeFormField(out, "ds_scrittura", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
        </tr>
        <% if (Optional.ofNullable(scrittura.getPg_scrittura_annullata()).isPresent()) { %>
            <tr><% scrittura.writeFormField(out, "pg_scrittura_annullata", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %></tr>
        <% } %>
    </table>
    <table cellpadding="2" style="width:30%">
        <tr>
            <% scrittura.writeFormField(out, "imTotaleMov", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
        </tr>
    </table>
</div>
<% bp.getMovimentiAnalitici().writeHTMLTable(pageContext, "scrittura", false, false, false,"100%","300px", true); %>
<% if (!bp.getMovimentiAnalitici().isCollapsed() && Optional.ofNullable(bp.getMovimentiAnalitici().getModel()).isPresent()) { %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <tr>
            <td><% bp.getMovimentiAnalitici().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getMovimentiAnalitici().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <tr>
            <td><% bp.getMovimentiAnalitici().writeFormLabel(out, "find_linea_attivita"); %></td>
            <td colspan="7" class="w-100"><% bp.getMovimentiAnalitici().writeFormInput(out, "find_linea_attivita"); %></td>
        </tr>
        <tr>
            <td><% bp.getMovimentiAnalitici().writeFormLabel(out, "centro_responsabilita"); %></td>
            <td colspan="7" class="w-100"><% bp.getMovimentiAnalitici().writeFormInput(out, "centro_responsabilita"); %></td>
        </tr>
        <tr>
            <% bp.getMovimentiAnalitici().writeFormField(out, "im_movimento");%>
        </tr>
    </table>
<% } %>
<% bp.getMovimentiAnalitici().closeHTMLTable(pageContext);%>