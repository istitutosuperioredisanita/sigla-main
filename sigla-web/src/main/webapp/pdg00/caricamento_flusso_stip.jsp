<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
            it.cnr.jada.action.*,
            it.cnr.jada.util.action.*,
            it.cnr.contab.pdg00.bp.*" %>
<!DOCTYPE html>
<html>
<head>
  <% JSPUtils.printBaseUrl(pageContext); %>
  <title>Caricamento Flusso Stipendi</title>
  <script src="scripts/util.js"></script>
  <script src="scripts/css.js"></script>
</head>
<body class="Form">
<%
  CaricFlStipBP bp = (CaricFlStipBP) BusinessProcess.getBusinessProcess(request);
  bp.openFormWindow(pageContext); // AllegatiCRUDBP imposta enctype multipart
%>

<!-- Sezione selezione tipo rapporto -->
<div class="Group card">
  <table width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td nowrap="nowrap" valign="middle">
        <% bp.getController().writeFormLabel(out, "tipo_rapporto"); %>&nbsp;
        <% bp.getController().writeFormInput(out, null, "tipo_rapporto", false, null, null); %>
      </td>
    </tr>
  </table>
</div>


<!-- Form dettaglio: campo file + metadati allegato -->
<div class="Group card mt-3">
  <table>
    <% bp.getCrudArchivioAllegati().writeForm(out, bp.getAllegatiFormName()); %>
  </table>
</div>

<% bp.closeFormWindow(pageContext); %>
</body>
</html>
