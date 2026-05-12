<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*,it.cnr.jada.blobs.bp.*,it.cnr.contab.coepcoan00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
    <% JSPUtils.printBaseUrl(pageContext); %>
    <% 	SelezionatoreGruppoEpBP bp = (SelezionatoreGruppoEpBP)BusinessProcess.getBusinessProcess(request); %>
    <title></title>
    <script language="JavaScript" src="scripts/util.js"></script>
    <script language="javascript" src="scripts/css.js"></script>
</head>
<body class="Form">
<%	bp.openFormWindow(pageContext); %>
<table class="Panel mb-2" width="100%" height="100%">
    <tr><% bp.getSelezioneTipoBilancioController().writeFormField(out,"tipoBilancio"); %></tr>
    <tr>
        <td>
            <% bp.writeHistoryLabel(pageContext); %>
        </td>
        <td width="100%">
            <% bp.writeHistoryField(pageContext,"cdGruppoEp"); %>
        </td>
    </tr>
</table>
<%	bp.writeHTMLTable(pageContext,"100%","65vh");%>
<%	bp.writeHTMLNavigator(out);%>
<%	bp.closeFormWindow(pageContext); %>
</body>

</html>