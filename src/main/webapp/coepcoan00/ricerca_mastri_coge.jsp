<!-- 
 ?ResourceName ""
 ?ResourceTimestamp ""
 ?ResourceEdition ""
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,it.cnr.jada.bulk.*,it.cnr.jada.util.action.*,it.cnr.contab.coepcoan00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="JavaScript" src="scripts/disableRightClick.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Visualizzazione mastri</title>
</head>
<body class="Form">

<%  
		RicercaMastriCogeBP bp = (RicercaMastriCogeBP)BusinessProcess.getBusinessProcess(request);
		bp.openFormWindow(pageContext); 
%>

<table class="Panel card p-2">
    <tr><% bp.getController().writeFormField(out,"esercizio"); %></tr>
    <tr><% bp.getController().writeFormField(out, bp.isUoEnte() ? "cdsEnte":"cds");%></tr>
    <tr><% bp.getController().writeFormField(out, bp.isUoEnte() ? "unita_organizzativaEnte":"unita_organizzativa");%></tr>
</table>

<table class="Panel card mt-2 p-2">
    <tr>
        <td><% bp.getController().writeFormLabel(out,"ti_istituz_commerc"); %></td>
        <td><% bp.getController().writeFormInput(out,"ti_istituz_commerc"); %></td>
    </tr>
    <tr><% bp.getController().writeFormField(out,"find_terzo_searchtool"); %></tr>
    <tr><% bp.getController().writeFormField(out,"find_voce_ep_searchtool"); %></tr>
    <tr>
        <td><% bp.getController().writeFormLabel(out,"tot_avere"); %></td>
        <td><% bp.getController().writeFormInput(out,"tot_avere"); %></td>
    </tr>
    <tr>
        <td><% bp.getController().writeFormLabel(out,"tot_dare"); %></td>
        <td><% bp.getController().writeFormInput(out,"tot_dare"); %></td>
    </tr>
</table>

<%	bp.closeFormWindow(pageContext); %>
</body>
</html>