<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
            it.cnr.jada.action.*,
            it.cnr.jada.util.action.*,
            it.cnr.contab.pdg00.bp.*,
            it.cnr.contab.progettiric00.bp.*" %>
<!DOCTYPE html>
<html>
<head>
  <% JSPUtils.printBaseUrl(pageContext); %>
  <title>Caricamento Progetti da Riportare</title>
  <script src="scripts/util.js"></script>
  <script src="scripts/css.js"></script>
</head>
<body class="Form">
<%
  RiportaProgettiRicercaBP bp = (RiportaProgettiRicercaBP) BusinessProcess.getBusinessProcess(request);
  bp.openFormWindow(pageContext); // AllegatiCRUDBP imposta enctype multipart
%>

<!-- Form dettaglio: campo file + metadati allegato -->
<div class="Group card mt-3">
  <table>
    <% bp.getCrudArchivioAllegati().writeForm(out, bp.getAllegatiFormName()); %>
  </table>
</div>

<% bp.closeFormWindow(pageContext); %>
</body>
</html>
