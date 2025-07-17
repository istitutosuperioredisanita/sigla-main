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
		it.cnr.contab.ordmag.ordini.bp.*"
%>
<%
    CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
    OrdineAcqBulk ordine = Optional.ofNullable(bp.getModel())
                            .filter(OrdineAcqBulk.class::isInstance)
                            .map(OrdineAcqBulk.class::cast)
                            .orElse(null);
%>
<div class="Panel card p-2 mb-2 card-shadow">
    <% if (bp.isAttivaEconomica()) { %>
    <table cellpadding="2">
        <tr>
			<td><% bp.getController().writeFormLabel(out,"find_voce_ep");%> </td>
			<td><% bp.getController().writeFormInput(out,null,"find_voce_ep",!ordine.getRigheEconomica().isEmpty(),null,null);%></td>
        </tr>
    </table>
    <% } %>
</div>
<div class="mt-1">
    <% bp.getProposeRigheEcoTestata().writeHTMLTable(pageContext, "propose", true, false, true,"100%","100px", true); %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <tr>
            <td><% bp.getProposeRigheEcoTestata().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getProposeRigheEcoTestata().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <tr>
            <% bp.getProposeRigheEcoTestata().writeFormField(out, "find_linea_attivita");%>
            <% bp.getProposeRigheEcoTestata().writeFormField(out, "centro_responsabilita");%>
        </tr>
    </table>
</div>