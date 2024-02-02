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
</head>
<title>Chiusura Magazzino</title>

<body class="Form">

<%	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext); %>

<table>
  <tr>

    <table >
        <tr>
            <td><% bp.getController().writeFormLabel(out,"esercizio"); %></td>
            <td><% bp.getController().writeFormInput(out,"esercizio"); %></td>


         </tr>
         <tr>
             <td><% bp.getController().writeFormLabel(out,"dataInventario"); %></td>
             <td colspan="5"><% bp.getController().writeFormInput(out,"dataInventario"); %></td>
             <td><% bp.getController().writeFormLabel(out,"dataInventarioInizio"); %></td>
             <td colspan="5"><% bp.getController().writeFormInput(out,"dataInventarioInizio"); %></td>
         </tr>

       <tr>
            <td><% bp.getController().writeFormLabel(out,"findRaggrMagazzinoRim"); %></td>
            <td colspan="5"><% bp.getController().writeFormInput(out,"findRaggrMagazzinoRim"); %></td>
       </tr>
       <tr>
            <td><% bp.getController().writeFormLabel(out,"findCatGrp"); %></td>
            <td colspan="5"><% bp.getController().writeFormInput(out,"findCatGrp"); %></td>
        </tr>

        <tr>
            <td><% bp.getController().writeFormLabel(out,"flRaggCatGruppo"); %></td>
            <td><% bp.getController().writeFormInput(out,"flRaggCatGruppo"); %></td>
        </tr>

        <tr>
            <td><% bp.getController().writeFormLabel(out,"flDettaglioArticolo"); %></td>
            <td><% bp.getController().writeFormInput(out,"flDettaglioArticolo"); %></td>
        </tr>

         <tr>
            <td>	<% bp.getController().writeFormLabel( out, "ti_operazione"); %></td>
            <td>	<% bp.getController().writeFormInput( out, "ti_operazione"); %></td>
        </tr>

        <tr>
            <td>	<% bp.getController().writeFormLabel( out, "ti_valorizzazione"); %></td>
            <td>	<% bp.getController().writeFormInput( out, "ti_valorizzazione"); %></td>

        </tr>
        <tr>
           <td><% bp.getController().writeFormLabel(out,"ordinamento"); %></td>
           <td><% bp.getController().writeFormInput(out,"ordinamento"); %></td>
        </tr>
    </table>

	<td></td>
	<td></td>
  </tr>
  </table>


</body>