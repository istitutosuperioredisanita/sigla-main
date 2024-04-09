<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*,it.cnr.contab.coepcoan00.bp.*,it.cnr.contab.coepcoan00.core.bulk.*"
%>
<%CRUDScritturaPDoppiaBP bp = (CRUDScritturaPDoppiaBP)BusinessProcess.getBusinessProcess(request);%>

<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-12">
            <% bp.getMovimentiDare().writeHTMLTable(pageContext,"scrittura", true, false, true,"100%","auto;max-height:40vh;", true); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-3 h-100"><% bp.getMovimentiDare().writeFormField(out, "ti_riga");%></div>
        <div class="col-md-9"><% bp.getMovimentiDare().writeFormField(out, "find_voce_ep_searchtool", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getMovimentiDare().writeFormField(out, "im_movimento");%></div>
        <div class="col-md-3"><% bp.getMovimentiDare().writeFormField(out, "dt_da_competenza_coge");%></div>
        <div class="col-md-3"><% bp.getMovimentiDare().writeFormField(out, "dt_a_competenza_coge");%></div>
    </div>
    <% if (bp.getMovimentiDare().isDebitoCredito()) { %>
    <div class="form-row">
        <div class="col-md-6"><% bp.getMovimentiDare().writeFormField(out, "terzo_movimento");%></div>
        <div class="col-md-6"><% bp.getMovimentiDare().writeFormField(out, "partitario");%></div>
    </div>
    <% } %>
</div>