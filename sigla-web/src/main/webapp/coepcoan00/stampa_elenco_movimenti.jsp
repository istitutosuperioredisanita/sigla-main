<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
			it.cnr.jada.action.*,
			java.util.*,
			it.cnr.jada.util.action.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Stampa Elenco Movimenti per Conto</title>
</head>
<body class="Form">

<%	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext);  %>
<div class="Group card p-2" style="width:100%">
    <table>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"esercizio"); %></td>
        <td><% bp.getController().writeFormInput(out,"esercizio"); %></td>
      </tr>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"findCDSForPrint"); %></td>
        <td><% bp.getController().writeFormInput(out,"findCDSForPrint"); %></td>
      </tr>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"findUOForPrint"); %></td>
        <td><% bp.getController().writeFormInput(out,"findUOForPrint"); %></td>
      </tr>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"findTerzoForPrint"); %></td>
        <td><% bp.getController().writeFormInput(out,"findTerzoForPrint"); %></td>
      </tr>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"attiva"); %></td>
        <td><% bp.getController().writeFormInput(out,"attiva"); %></td>
      </tr>
      <tr><% bp.getController().writeFormField(out,"findContoForPrint"); %></tr>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"tipologia"); %></td>
        <td><% bp.getController().writeFormInput(out,"tipologia"); %></td>
      </tr>
    </table>
</div>
<div class="Group card mt-2 p-2" style="width:100%">
    <table>
          <tr>
            <td class="GroupLabel text-primary h3">Origine della Scrittura</td>
            <td><% bp.getController().writeFormInput(out,"seleziona"); %></td>
          </tr>
          <tr>
            <td class="w-20"><% bp.getController().writeFormLabel(out,"ragr_manuale"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_manuale"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_causale"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_causale"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_chiusura"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_chiusura"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_doc_amm"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_doc_amm"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_doc_cont"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_doc_cont"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_liquid_iva"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_liquid_iva"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_mig_beni"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_mig_beni"); %></td>
          </tr>
          <tr>
            <td><% bp.getController().writeFormLabel(out,"ragr_stipendi"); %></td>
            <td><% bp.getController().writeFormInput(out,"ragr_stipendi"); %></td>
          </tr>
    </table>
</div>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html>