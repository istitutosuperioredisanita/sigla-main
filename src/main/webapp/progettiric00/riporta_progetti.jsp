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

<div class="alert alert-info w-100 mt-3 text-center">Il file Excel deve essere composto dalle seguenti colonne : ESERCIZIO_ESTRAZIONE, CD_PROGETTO, PG_PROGETTO,NUOVO CODICE PROGETTO</div>
<!-- Form dettaglio: campo file + metadati allegato -->

<div class="Group card mt-3">
  <table>
    <% bp.getCrudArchivioAllegati().writeForm(out, bp.getAllegatiFormName()); %>
  </table>
</div>

<% bp.closeFormWindow(pageContext); %>


</body>
</html>
