<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.prevent01.bulk.*,
		it.cnr.contab.prevent01.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<%
   StampaMastroRendicontoFinanziarioBP bp = (StampaMastroRendicontoFinanziarioBP)BusinessProcess.getBusinessProcess(request);
   StampaMastroRendicontoFinanziarioBulk model = (StampaMastroRendicontoFinanziarioBulk)bp.getModel();
%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title><%=bp.getFormTitle()%></title>
</head>
<body class="Form">

<%	bp.openFormWindow(pageContext); %>

<table width=100%>
 <tr>
  <td>
	<div class="Group card p-2">
	<table>
	  <tr>
		<td><% bp.getController().writeFormLabel(out,"esercizio"); %></td>
		<td><% bp.getController().writeFormInput(out,"esercizio"); %></td>
	  </tr>	
	  <tr>
		<td><% bp.getController().writeFormLabel(out,"ti_gestione"); %></td>
        <td><% bp.getController().writeFormInput(out,null,"ti_gestione",false,null,"onClick=\"submitForm('doOnTipoChange')\""); %></td>
	  </tr>
	  <tr>
		<td><% bp.getController().writeFormLabel(out,"find_voce_da"); %></td>
		<td><% bp.getController().writeFormInput(out,"find_voce_da"); %></td>
	  </tr>
	  <tr>
		<td><% bp.getController().writeFormLabel(out,"find_voce_a"); %></td>
		<td><% bp.getController().writeFormInput(out,"find_voce_a"); %></td>
	  </tr>
	</table>
   </td>
  </tr>
</table>


<% bp.closeFormWindow(pageContext); %>

</body>
</html>