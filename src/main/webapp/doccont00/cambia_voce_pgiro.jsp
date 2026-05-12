<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.doccont00.bp.*"
%>
<%
	CambiaVocePGiroBP bp = (CambiaVocePGiroBP)BusinessProcess.getBusinessProcess(request);
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
    <div class="card p-2 mb-1 w-100">
        <div class="form-row">
            <div class="p-2"><% bp.getController().writeFormLabel( out, "tiGestione"); %></div>
            <div class="p-2">
                <% bp.getController().writeFormInput(out,"default","tiGestione",false,null,"onchange=\"submitForm('doOnChangeTiGestione')\""); %>
            </div>
        </div>
   	</div>
    <div class="card p-2 w-100">
        <table class="Panel w-100">
            <tr><% bp.getController().writeFormField( out, "voceIniziale"); %></tr>
            <tr>
                <td colspan = "4">
                    <% bp.getDettagliCRUDController().writeHTMLTable(
                        pageContext,
                        "default",
                        true,
                        false,
                        true,
                        null,
                        "40vh",
                        true); %>
                </td>
            </tr>
            <tr><% bp.getController().writeFormField( out, "voceFinale"); %></tr>
        </table>
   	</div>
<% bp.closeFormWindow(pageContext); %>
</body>