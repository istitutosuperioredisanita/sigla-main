<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Chiusura Inventario</title>
</head>
<body class="Form">		

<%
    CRUDChiusuraInventarioBP bp = (CRUDChiusuraInventarioBP)BusinessProcess.getBusinessProcess(request);
   bp.openFormWindow(pageContext);


 %>

  <div class="Group">
	<table>

		<tr>
           <td>
                <% bp.getController().writeFormLabel(out,"anno"); %>
                <% bp.getController().writeFormInput(out,"anno"); %>
           </td>
        </tr>
	</table>
  </div>

</body>
</html>