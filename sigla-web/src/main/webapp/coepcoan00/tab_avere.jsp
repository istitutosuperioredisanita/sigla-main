<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*,it.cnr.contab.coepcoan00.bp.*,it.cnr.contab.coepcoan00.core.bulk.*"
%>
<%CRUDScritturaPDoppiaBP bp = (CRUDScritturaPDoppiaBP)BusinessProcess.getBusinessProcess(request);%>

<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-12">
            <% bp.getMovimentiAvere().writeHTMLTable(pageContext,"scrittura", true, false, true,"100%","auto;max-height:40vh;", true); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-3 h-100"><% bp.getMovimentiAvere().writeFormField(out, "ti_riga");%></div>
        <div class="col-md-9"><% bp.getMovimentiAvere().writeFormField(out, "find_voce_ep_searchtool", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-3 h-100"><% bp.getMovimentiAvere().writeFormField(out, "ti_istituz_commerc");%></div>
        <div class="col-md-3"><% bp.getMovimentiAvere().writeFormField(out, "im_movimento");%></div>
        <div class="col-md-3"><% bp.getMovimentiAvere().writeFormField(out, "dt_da_competenza_coge");%></div>
        <div class="col-md-3"><% bp.getMovimentiAvere().writeFormField(out, "dt_a_competenza_coge");%></div>
    </div>
    <div class="form-row">
        <div class="col-md-4"><% bp.getMovimentiAvere().writeFormField(out, "partita");%></div>
        <div class="col-md-6"><% bp.getMovimentiAvere().writeFormField(out, "terzo_movimento");%></div>
        <div class="col-md-1"><% bp.getMovimentiAvere().writeFormField(out, "cd_contributo_ritenuta");%></div>
    </div>
</div>