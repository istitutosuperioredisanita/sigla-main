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
    OrdineAcqEcoBulk model = (OrdineAcqEcoBulk)bp.getProposeRigheEcoTestata().getModel();
    boolean isVoceAnaliticaEnabled = model!=null && (model.getVoce_ep()==null ||
                   model.getVoce_ep().getCrudStatus() != it.cnr.jada.bulk.OggettoBulk.NORMAL ||
                   model.getVoce_ep().isAnaliticaEnabled());
%>
<div class="mt-1">
    <% bp.getProposeRigheEcoTestata().writeHTMLTable(pageContext, "propose", true, false, true,"100%","100px", true); %>
    <table class="Panel mt-1 p-2 card card-shadow" cellpadding="2">
        <tr>
            <td><% bp.getProposeRigheEcoTestata().writeFormLabel(out, "find_voce_eco_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getProposeRigheEcoTestata().writeFormInput(out, "find_voce_eco_searchtool"); %></td>
        </tr>
        <% if (isVoceAnaliticaEnabled) { %>
        <tr>
            <td><% bp.getProposeRigheEcoTestata().writeFormLabel(out, "find_voce_ana_searchtool"); %></td>
            <td colspan="7" class="w-100"><% bp.getProposeRigheEcoTestata().writeFormInput(out, "find_voce_ana_searchtool"); %></td>
        </tr>
        <% } %>
        <tr>
            <% bp.getProposeRigheEcoTestata().writeFormField(out, "find_linea_attivita");%>
            <% bp.getProposeRigheEcoTestata().writeFormField(out, "centro_responsabilita");%>
        </tr>
    </table>
</div>