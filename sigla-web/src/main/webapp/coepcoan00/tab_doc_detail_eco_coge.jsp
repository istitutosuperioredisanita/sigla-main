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
    IDocAmmCogeCoanBP bp = (IDocAmmCogeCoanBP)BusinessProcess.getBusinessProcess(request);
    IDocumentoDetailEcoCogeBulk model =
                    Optional.ofNullable(bp.getControllerDetailEcoCoge().getModel())
                        .filter(IDocumentoDetailEcoCogeBulk.class::isInstance)
                        .map(IDocumentoDetailEcoCogeBulk.class::cast)
                        .orElse(null);
    boolean isVoceAnaliticaEnabled = model!=null && (model.getVoce_ep()==null ||
                   model.getVoce_ep().getCrudStatus() != it.cnr.jada.bulk.OggettoBulk.NORMAL ||
                   model.getVoce_ep().isAnaliticaEnabled());
%>
<div class="Panel card p-2 mb-2 card-shadow">
    <% if (bp.isAttivaEconomica()) { %>
    <table cellpadding="2">
        <tr>
			<td><% bp.getControllerDetailEcoCoge().writeFormLabel(out,"find_voce_ep");%> </td>
			<td><% bp.getControllerDetailEcoCoge().writeFormInput(out,null,"find_voce_ep",!model.getChildrenAna().isEmpty(),null,null);%></td>
        </tr>
    </table>
    <% } %>
    <% if (bp.isAttivaAnalitica() && model!=null) { %>
    <table cellpadding="2">
        <tr>
            <% model.writeFormField(out, "imCostoEco", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoRipartito", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoDaRipartire", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
        </tr>
    </table>
    <% } %>
</div>
<% if (bp.isAttivaAnalitica()) { %>
<div class="mt-1">
    <% bp.getChildrenAnaColl().writeHTMLTable(pageContext, isVoceAnaliticaEnabled?"default":"novoceanalitica", !bp.isAttivaFinanziaria(), false, !bp.isAttivaFinanziaria(),"100%","100px", true); %>
    <% if (!bp.getChildrenAnaColl().isCollapsed() && Optional.ofNullable(bp.getChildrenAnaColl().getModel()).isPresent() && !bp.isAttivaFinanziaria()) { %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <% if (isVoceAnaliticaEnabled) { %>
        <tr>
            <td><% bp.getChildrenAnaColl().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getChildrenAnaColl().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <% } %>
        <tr>
            <% bp.getChildrenAnaColl().writeFormField(out, "find_linea_attivita");%>
            <% bp.getChildrenAnaColl().writeFormField(out, "centro_responsabilita");%>
        </tr>
        <tr>
            <% bp.getChildrenAnaColl().writeFormField(out, "importo");%>
        </tr>
    </table>
    <% } %>
    <% bp.getChildrenAnaColl().closeHTMLTable(pageContext);%>
</div>
<% } %>
