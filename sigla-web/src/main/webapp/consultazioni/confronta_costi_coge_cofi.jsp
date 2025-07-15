<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*,it.cnr.jada.util.action.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<!-- 
 ?ResourceName "risultato_ricerca.jsp"
 ?ResourceTimestamp "13/12/00 19.48.42"
 ?ResourceEdition "1.0"
-->

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<title>Risultato ricerca</title>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
</head>

<body class="Form">

<% SelezionatoreListaBP bp = (SelezionatoreListaBP)BusinessProcess.getBusinessProcess(request);
	 bp.openFormWindow(pageContext); %>

	 <table class="Panel" width="100%" height="100%">
     	<tr>
     		<td>
     			<fieldset>
     			<% bp.writeFormLabel(out,"tipo_visualizzazione"); %>
     			<% bp.writeFormInput(out,null,"tipo_visualizzazione",false,null,"onchange=\"javascript:submitForm('doCambiaVisualizzazione')\""); %>

     			</fieldset>
     		</td>
     	</tr>
     	<%	if (!bp.getParentRoot().isBootstrap()) { %>
             <tr height="100%">
                 <td colspan="4">
                     <% bp.writeHTMLTable(pageContext,"100%","100%"); %>
                 </td>
             </tr>
             <tr>
                 <td colspan="4">
                     <% bp.writeHTMLNavigator(out); %>
                 </td>
             </tr>
     	<% } %>
     </table>
     <%	if (bp.getParentRoot().isBootstrap()) {
             bp.writeHTMLTable(pageContext,"100%","100%");
             bp.writeHTMLNavigator(out);
         }
     %>

<%	bp.closeFormWindow(pageContext); %>
</body>

</html>
