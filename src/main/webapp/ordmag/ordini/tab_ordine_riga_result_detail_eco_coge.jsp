<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.ordini.bulk.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.intrastat.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.doccont00.core.bulk.*,
		it.cnr.contab.coepcoan00.core.bulk.*,
		it.cnr.contab.docamm00.bp.*,
        it.cnr.contab.ordmag.ordini.bp.*"
%>
<%
    CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
    OrdineAcqRigaBulk model = (OrdineAcqRigaBulk)bp.getRighe().getModel();
    boolean isVoceAnaliticaEnabled = model!=null && (model.getVoce_ep()==null ||
                   model.getVoce_ep().getCrudStatus() != it.cnr.jada.bulk.OggettoBulk.NORMAL ||
                   model.getVoce_ep().isAnaliticaEnabled());
%>
<div class="Panel card p-2 mb-2 card-shadow">
    <% if (bp.isAttivaEconomica()) { %>
    <table cellpadding="2">
        <tr>
			<td><% bp.getRighe().writeFormLabel(out,"find_voce_ep");%> </td>
			<td><% bp.getRighe().writeFormInput(out,null,"find_voce_ep");%></td>
        </tr>
    </table>
    <% } %>
    <% if (bp.isAttivaAnalitica() && model!=null) { %>
    <table cellpadding="2">
        <tr>
            <% model.writeFormField(out, "imCostoEcoConsegne", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoRipartitoConsegne", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
            <% model.writeFormField(out, "imCostoEcoDaRipartireConsegne", FormController.VIEW, bp.getFieldValidationMap(), bp.getParentRoot().isBootstrap()); %>
        </tr>
    </table>
    <% } %>
</div>
<% if (bp.isAttivaAnalitica()) { %>
<div class="mt-1">
    <% bp.getResultRigheEcoDettaglio().writeHTMLTable(pageContext, isVoceAnaliticaEnabled?"default":"novoceanalitica", false, false, false,"100%","100px", true); %>
    <% if (!bp.getResultRigheEcoDettaglio().isCollapsed() && Optional.ofNullable(bp.getResultRigheEcoDettaglio().getModel()).isPresent() && !bp.isAttivaFinanziaria()) { %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <% if (isVoceAnaliticaEnabled) { %>
        <tr>
            <td><% bp.getResultRigheEcoDettaglio().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getResultRigheEcoDettaglio().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <% } %>
        <tr>
            <% bp.getResultRigheEcoDettaglio().writeFormField(out, "find_linea_attivita");%>
            <% bp.getResultRigheEcoDettaglio().writeFormField(out, "centro_responsabilita");%>
        </tr>
        <tr>
            <% bp.getResultRigheEcoDettaglio().writeFormField(out, "importo");%>
        </tr>
    </table>
    <% } %>
    <% bp.getResultRigheEcoDettaglio().closeHTMLTable(pageContext);%>
</div>
<% } %>

