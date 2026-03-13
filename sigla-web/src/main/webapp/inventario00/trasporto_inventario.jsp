<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Documento di Trasporto</title>
</head>
<body class="Form">

<% CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
    bp.openFormWindow(pageContext);
    	JSPUtils.tabbed(
    					pageContext,
    					"tab",
    					bp.getTabs(),
    					bp.getTab("tab"),
    					"center",
    					"100%",
    					"100%" );


    bp.closeFormWindow(pageContext); %>

</body>
</html>