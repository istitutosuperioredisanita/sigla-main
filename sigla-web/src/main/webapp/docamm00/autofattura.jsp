<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.docamm00.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<head>
    <% JSPUtils.printBaseUrl(pageContext); %>
    <script language="javascript" src="scripts/css.js"></script>
    <script language="JavaScript" src="scripts/util.js"></script>
    <% CRUDAutofatturaBP bp = (CRUDAutofatturaBP)BusinessProcess.getBusinessProcess(request); %>
    <title> Autofatture</title>
</head>
<html>
<body class="Form">
<%bp.openFormWindow(pageContext);%>

</body>
</html>