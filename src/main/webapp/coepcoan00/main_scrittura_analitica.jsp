<!-- 
 ?ResourceName ""
 ?ResourceTimestamp ""
 ?ResourceEdition ""
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.coepcoan00.core.bulk.*,
		it.cnr.contab.coepcoan00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="JavaScript" src="scripts/disableRightClick.js"></script>
<% JSPUtils.printBaseUrl(pageContext);%>
</head>
<script language="javascript" src="scripts/css.js"></script>
<title>Scrittura Analitica</title>
<body class="Form">

<%  
		CRUDScritturaAnaliticaBP bp = (CRUDScritturaAnaliticaBP)BusinessProcess.getBusinessProcess(request);
		Scrittura_analiticaBulk model = (Scrittura_analiticaBulk)bp.getModel();
		bp.openFormWindow(pageContext);
%>
<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "esercizio", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "cds",  Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "unita_organizzativa", Boolean.FALSE);%></div>
        <div class="col-md-2"><% bp.getController().writeFormField(out, "pg_scrittura", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getController().writeFormField(out, "imTotaleMov", Boolean.FALSE);%></div>
    </div>
</div>
<% if (!bp.isInserting() && bp.isFromDocumentoOrigine()) { %>
    <% if (model.isScritturaFromConsegnaOrdineAcquisto()) { %>
        <div class="card my-2">
          <h3 class="card-header text-info"><i class="fa fa-question-circle fa-fw" aria-hidden="true"></i> Documento origine</h3>
          <div class="card-block p-2">
            <div class="form-row">
                <div class="col-md-4"><% bp.getController().writeFormField(out, "cd_tipo_documento", Boolean.FALSE);%></div>
                <div class="col-md-8"><% bp.getController().writeFormField(out, "chiaveDocumentoOrigine", Boolean.FALSE);%></div>
            </div>
          </div>
        </div>
    <% } else { %>
        <div class="card my-2">
          <h3 class="card-header text-info"><i class="fa fa-question-circle fa-fw" aria-hidden="true"></i> Documento origine</h3>
          <div class="card-block p-2">
            <div class="form-row">
                <div class="col-md-4"><% bp.getController().writeFormField(out, "cd_tipo_documento", Boolean.FALSE);%></div>
                <div class="col-md-4"><% bp.getController().writeFormField(out, "pg_numero_documento", Boolean.FALSE);%></div>
                <div class="col-md-4"><% bp.getController().writeFormField(out, "cd_comp_documento", Boolean.FALSE);%></div>
            </div>
            <div class="form-row">
                <div class="col-md-2"><% bp.getController().writeFormField(out, "esercizio_documento_amm", Boolean.FALSE);%></div>
                <div class="col-md-4"><% bp.getController().writeFormField(out, "cds_documento", Boolean.FALSE);%></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out, "uo_documento", Boolean.FALSE);%></div>
            </div>
          </div>
        </div>
    <% } %>
<% } %>
<%
	JSPUtils.tabbed(
    				pageContext,
					"tab",
					new String[][] {
							{ "tabScritturaAnalitica","Scrittura Analitica","/coepcoan00/tab_scrittura_analitica.jsp" },
							{ "tabMovimenti","Movimenti","/coepcoan00/tab_movimenti.jsp" }},
					bp.getTab("tab"),
					"center",
					null,null);
%>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html>