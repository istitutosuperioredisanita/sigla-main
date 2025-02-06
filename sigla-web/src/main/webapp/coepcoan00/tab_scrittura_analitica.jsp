<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*,it.cnr.contab.coepcoan00.bp.*,it.cnr.contab.coepcoan00.core.bulk.*"
%>
<%CRUDScritturaAnaliticaBP bp = (CRUDScritturaAnaliticaBP)BusinessProcess.getBusinessProcess(request);%>
<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "dt_contabilizzazione", Boolean.FALSE);%></div>
        <div class="col-md-4 h-100"><% bp.getController().writeFormField(out, "origine_scrittura", Boolean.FALSE);%></div>
        <div class="col-md-3 h-100"><% bp.getController().writeFormField(out, "ti_istituz_commerc", Boolean.FALSE);%></div>
        <div class="col-md-3 h-100"><% bp.getController().writeFormField(out, "attiva", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-12"><% bp.getController().writeFormField(out, "ds_scrittura_textarea", Boolean.FALSE);%></div>
    </div>
     <% if (bp.isScritturaAnnullata()) { %>
    <div class="form-row">
        <div class="col-md-3"><% bp.getController().writeFormField(out, "pg_scrittura_annullata", Boolean.FALSE);%></div>
    </div>
    <% } %>
</table>