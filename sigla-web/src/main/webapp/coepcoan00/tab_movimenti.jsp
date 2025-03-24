<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*,it.cnr.contab.coepcoan00.bp.*,it.cnr.contab.coepcoan00.core.bulk.*"
%>
<%CRUDScritturaAnaliticaBP bp = (CRUDScritturaAnaliticaBP)BusinessProcess.getBusinessProcess(request);%>
<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-12">
            <% bp.getMovimenti().writeHTMLTable(pageContext,"latt", true, false, true,"100%","auto;max-height:40vh;", true); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-3 h-100"><% bp.getMovimenti().writeFormField(out, "sezione");%></div>
        <div class="col-md-9"><% bp.getMovimenti().writeFormField(out, "find_voce_ana_searchtool", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getMovimenti().writeFormField(out, "find_linea_attivita");%></div>
        <div class="col-md-6"><% bp.getMovimenti().writeFormField(out, "centro_responsabilita");%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getMovimenti().writeFormField(out, "im_movimento");%></div>
    </div>
</div>
</table>