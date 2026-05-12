<!-- 
 ?ResourceName "TemplateForm.jsp"
 ?ResourceTimestamp "08/11/00 16.43.22"
 ?ResourceEdition "1.0"
-->

<%@ page pageEncoding="UTF-8"
		import="it.cnr.jada.util.jsp.*,
	        it.cnr.jada.action.*,
	        java.util.*, 
	        it.cnr.contab.config00.bp.*,
	        it.cnr.jada.util.action.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
</head>
<title>Gestione Piano dei Conti Analitici Econ./Patr. - Conto</title>
<body class="Form">

<% SimpleCRUDBP bp = (SimpleCRUDBP)BusinessProcess.getBusinessProcess(request);
	 bp.openFormWindow(pageContext); %>
	<table class="Panel card p-2">
    <tr>
        <td><% bp.getController().writeFormLabel( out, "esercizio"); %></td>
        <td><% bp.getController().writeFormInputByStatus( out, "esercizio"); %></td>
    </tr>
    <tr>
        <td><% bp.getController().writeFormLabel( out, "cd_voce_ana"); %></td>
        <td><% bp.getController().writeFormInputByStatus( out, "cd_voce_ana"); %></td>
    </tr>
    <tr>
        <td><% bp.getController().writeFormLabel( out, "ds_voce_ana"); %></td>
        <td><% bp.getController().writeFormInputByStatus( out, "ds_voce_ana"); %></td>
    </tr>
    <tr>
        <td><% bp.getController().writeFormLabel( out, "find_voce_ep"); %></td>
        <td><% bp.getController().writeFormInputByStatus( out, "find_voce_ep"); %></td>
    </tr>
	<tr>
        <td><% bp.getController().writeFormInput( out, "fl_default"); %></td>
        <td><% bp.getController().writeFormLabel( out, "fl_default"); %></td>
	</tr>
	</table>
<%	bp.closeFormWindow(pageContext); %>
</body>