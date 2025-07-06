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
%>
<div class="Panel card p-2 mb-2 card-shadow">
    <table cellpadding="2">
        <tr>
            <% bp.getControllerDetailEcoCoge().writeFormField(out,"find_voce_ep");%>
        </tr>
    </table>
    <table cellpadding="2">
        <tr>
            <% model.writeFormField(out, "imCostoEco", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoRipartito", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoDaRipartire", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
        </tr>
    </table>
</div>
<% bp.getChildrenAnaColl().writeHTMLTable(pageContext, "default", false, false, false,"100%","100px", true); %>
