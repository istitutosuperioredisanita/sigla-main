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
<title>Ricerca movimenti contabili</title>
<body class="Form">

<%  
		RicercaMovContCogeBP bp = (RicercaMovContCogeBP)BusinessProcess.getBusinessProcess(request);
		bp.openFormWindow(pageContext); 
%>

<table class="Panel card p-2">
    <tr><% bp.getController().writeFormField(out,"esercizio"); %></tr>
    <tr><% bp.getController().writeFormField(out, bp.isUoEnte() ? "cdsEnte":"cds");%></tr>
    <tr><% bp.getController().writeFormField(out, bp.isUoEnte() ? "unita_organizzativaEnte":"unita_organizzativa");%></tr>
    <tr><% bp.getController().writeFormField(out,"pg_scrittura"); %></tr>
</table>
<table class="Panel card mt-2 p-2">
    <tr>
        <td><% bp.getController().writeFormLabel(out,"attiva"); %></td>
        <td>
            <% bp.getController().writeFormInput(out,"attiva"); %>
            <% bp.getController().writeFormLabel(out,"pg_scrittura_annullata"); %>
            <% bp.getController().writeFormInput(out,"pg_scrittura_annullata"); %>
        </td>
    </tr>
    <tr><% bp.getController().writeFormField(out,"sezione"); %></tr>
    <tr><% bp.getController().writeFormField(out,"ti_istituz_commerc"); %></tr>
    <tr><% bp.getController().writeFormField(out,"find_terzo_searchtool"); %></tr>
    <tr><% bp.getController().writeFormField(out,"find_voce_ep_searchtool"); %></tr>
    <tr>
        <td><% bp.getController().writeFormLabel(out,"dt_da_competenza_coge"); %></td>
        <td><% bp.getController().writeFormInput(out,"dt_da_competenza_coge"); %>
            <% bp.getController().writeFormLabel(out,"dt_a_competenza_coge"); %>
            <% bp.getController().writeFormInput(out,"dt_a_competenza_coge"); %></td>
    </tr>
    <tr><% bp.getController().writeFormField(out,"im_movimento"); %></tr>
</table>

<%	bp.closeFormWindow(pageContext); %>
</body>
</html>