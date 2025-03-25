<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
	it.cnr.jada.action.*,
	java.util.*,
	it.cnr.jada.util.action.*,
	it.cnr.contab.pdg00.bulk.Stampa_vpg_conto_econom_riclassVBulk"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Stampa conto economico riclassificato</title>
</head>
<body class="Form"> 

<%	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
	Stampa_vpg_conto_econom_riclassVBulk bulk = (Stampa_vpg_conto_econom_riclassVBulk)bp.getModel();
	bp.openFormWindow(pageContext); %>
<table class="card p-2">
  <tr><% bp.getController().writeFormField(out,"esercizio"); %></tr>
  <tr><% bp.getController().writeFormField(out,"tipoBilancio"); %></tr>
  <tr>
    <td><% bp.getController().writeFormLabel(out,"findCDSForPrint"); %></td>
    <td><% bp.getController().writeFormInput(out,null,"findCDSForPrint",(bulk!=null?!bulk.isCdsForPrintEnabled():false),null,null); %></td>
  </tr>
  <tr>
    <td><% bp.getController().writeFormLabel(out,"findUOForPrint"); %></td>
    <td><% bp.getController().writeFormInput(out,null,"findUOForPrint",(bulk!=null?!bulk.isUoForPrintEnabled():false),null,null); %></td>
  </tr>
  <tr><% bp.getController().writeFormField(out,"ti_ist_com"); %></tr>
  <tr><% bp.getController().writeFormField(out,"dettaglioConti"); %></tr>
</table>
<% bp.closeFormWindow(pageContext); %>

</body>
</html>