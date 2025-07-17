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
		it.cnr.contab.ordmag.ordini.bp.*"
%>
<%
    CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
%>
<div class="mt-1">
    <% bp.getResultRigheEcoTestata().writeHTMLTable(pageContext, "default", false, false, false,"100%","100px", true); %>
    <% if (!bp.getResultRigheEcoTestata().isCollapsed()) { %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <tr>
            <td><% bp.getResultRigheEcoTestata().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getResultRigheEcoTestata().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <tr>
            <% bp.getResultRigheEcoTestata().writeFormField(out, "find_linea_attivita");%>
            <% bp.getResultRigheEcoTestata().writeFormField(out, "centro_responsabilita");%>
        </tr>
        <tr>
            <% bp.getResultRigheEcoTestata().writeFormField(out, "importo");%>
        </tr>
    </table>
    <% } %>
    <% bp.getResultRigheEcoTestata().closeHTMLTable(pageContext);%>
</div>