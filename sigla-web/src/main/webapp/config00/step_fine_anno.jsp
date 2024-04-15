<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
	        it.cnr.jada.action.*,
	        java.util.*,
	        it.cnr.jada.util.action.*,
	        it.cnr.contab.config00.bp.*"
%>
<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<title>Step di Fine Anno</title>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
</head>

<body class="Form">
<% 
	SelezionatoreStepFineAnnoBP bp = (SelezionatoreStepFineAnnoBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext); 
%>
<% bp.getDetail().writeHTMLTable(pageContext,"STEP_FINE_ANNO",false,true,false,"100%","auto;max-height:50vh"); %>
<div class="card p-3 mt-2 w-100">
    <table class="Panel w-30">
      <% bp.getDetail().writeForm(out,"STEP_FINE_ANNO");%>
    </table>
</div>
<%bp.closeFormWindow(pageContext); %>
</body>
</html>