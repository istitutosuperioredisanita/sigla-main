<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.utenze00.bp.*"
%>
<!DOCTYPE html>
<html>
<head>
  <% JSPUtils.printBaseUrl(pageContext); %>
  <title></title>
  <script src="scripts/util.js"></script>
  <script src="scripts/css.js"></script>
</head>
<body class="Form">
<%
  AbilitazioniMassiveUtentiBP bp = (AbilitazioniMassiveUtentiBP) BusinessProcess.getBusinessProcess(request);
  bp.openFormWindow(pageContext); // AllegatiCRUDBP imposta enctype multipart
%>

<div class="Group card p-2">
  <table>
    <tr>
      <td nowrap="nowrap" valign="top"><% bp.getController().writeFormLabel(out, "tipoOperazione"); %></td>
      <td><% bp.getController().writeFormInput(out, null, "tipoOperazione", false, null, null); %></td>
    </tr>
  </table>
</div>

<div class="Group card mt-3">
  <table>
    <% bp.getCrudArchivioAllegati().writeForm(out, bp.getAllegatiFormName()); %>
  </table>
</div>

<div class="mt-3">
  <% bp.getController().writeFormInput(out, null, "nota", false, null, null); %>
</div>

<% bp.closeFormWindow(pageContext); %>
</body>
</html>
