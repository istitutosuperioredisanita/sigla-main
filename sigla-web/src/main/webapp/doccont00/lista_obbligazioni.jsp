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
		it.cnr.contab.doccont00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<script language="JavaScript" src="scripts/util.js"></script>
<% JSPUtils.printBaseUrl(pageContext);%>
</head>
<script language="javascript" src="scripts/css.js"></script>

<% ListaObbligazioniBP bp = (ListaObbligazioniBP)BusinessProcess.getBusinessProcess(request); %>

<% if (bp.isObbligazioniDefinitive()) { %>
	<title>Gestione Impegni Definitivi</title>
<% } else {%>
	<title>Gestione Impegni Provvisori</title>
<% } %>

<body class="Form">


<% bp.openFormWindow(pageContext); %>

	<table class="Panel">
		<tr><td>
		<% bp.writeHTMLTable(pageContext,"100%",null); %>
		</td></tr>
		<tr><td>
		<% bp.writeHTMLNavigator(out); %>
		</td></tr>
	</table>


<%bp.closeFormWindow(pageContext); %>
</body>