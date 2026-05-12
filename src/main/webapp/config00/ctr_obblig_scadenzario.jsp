<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.config00.bp.*"
%>

<%
	CRUDConfigCtrDispObblBP bp = (CRUDConfigCtrDispObblBP)BusinessProcess.getBusinessProcess(request);
%>
<html>

<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title><%=bp.getBulkInfo().getShortDescription()%></title>
</head>

<body class="Form">
    <% bp.openFormWindow(pageContext); %>
    <div class="Group card p-2">
	    <table class="Panel" width="100%">
		   <tr class="w-100">
           			    <td><% bp.getController().writeFormLabel(out, "findUnitaOrganizzativa"); %></td>
           			    <td><% bp.getController().writeFormInput(out, "findUnitaOrganizzativa"); %></td>
           			</tr>
           			<tr class="w-100">
                       	 <td><% bp.getController().writeFormLabel(out, "esercizioObbligazione"); %></td>
                       	 <td><% bp.getController().writeFormInput(out, "esercizioObbligazione"); %></td>
                       </tr>
           			<tr class="w-100">
           			     <td><% bp.getController().writeFormLabel(out, "esercizioOriObbligazione"); %></td>
                            <td><% bp.getController().writeFormInput(out, "esercizioOriObbligazione"); %></td>
                       </tr>
           			<tr class="w-100">
                            <td><% bp.getController().writeFormLabel(out, "cdCdsObbligazione"); %></td>
                            <td><% bp.getController().writeFormInput(out, "cdCdsObbligazione"); %></td>
                       </tr>
           			<tr class="w-100">
                            <td><% bp.getController().writeFormLabel(out, "totOrdineImpegnoStessaUo"); %></td>
                            <td><% bp.getController().writeFormInput(out, "totOrdineImpegnoStessaUo"); %></td>

           		    </tr>
           		    <tr class="w-100">
                               <td><% bp.getController().writeFormLabel(out, "totOrdineImpegnoAltraUo"); %></td>
                               <td><% bp.getController().writeFormInput(out, "totOrdineImpegnoAltraUo"); %></td>
                       </tr>
           			<tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totFattureOrdini"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totFattureOrdini"); %></td>
                       </tr>
           			<tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totFattureNoOrdini"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totFattureNoOrdini"); %></td>
                       </tr>
           			<tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totDocumentiGenerici"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totDocumentiGenerici"); %></td>
                       </tr>
                       <tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totImpegnoAssociatoCalcolato"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totImpegnoAssociatoCalcolato"); %></td>
                       </tr>
                        <tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totImpegnoAssociato"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totImpegnoAssociato"); %></td>
                       </tr>
                       <tr class="w-100">
                                <td><% bp.getController().writeFormLabel(out, "totImpegnoDaAssociare"); %></td>
                                <td><% bp.getController().writeFormInput(out, "totImpegnoDaAssociare"); %></td>
                       </tr>
		</table>
	</div>
</body>
</html>