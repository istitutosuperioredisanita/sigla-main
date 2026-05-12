<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.config00.bp.*"
%>

<%
	GestConfCNRBP bp = (GestConfCNRBP)BusinessProcess.getBusinessProcess(request);
%>

<html>

<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title><%=bp.getBulkInfo().getShortDescription()%></title>
</head>

<body class="Form">
<% bp.openFormWindow(pageContext);%>
   <div class="card p-2 mt-1 w-100">
       <div class="row">
           <div class="col-6">
               <table class="Panel w-100">
                 <% bp.getController().writeForm(out, "SEARCH_FORM");%>
               </table>
           </div>
       </div>
   </div>
<% bp.closeFormWindow(pageContext); %>
</body>