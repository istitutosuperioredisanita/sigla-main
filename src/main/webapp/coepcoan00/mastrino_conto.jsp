<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.coepcoan00.filter.bulk.*,
		it.cnr.contab.coepcoan00.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title>Mastrino Conto</title>

</head>
<body class="Form">
<%
    MastrinoContoBP bp = (MastrinoContoBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext);
%>
	<div class="Group card p-2" style="width:100%">
        <table width="100%">
            <tr><% bp.getController().writeFormField(out,"conto");%></tr>
			<tr>
				<td><% bp.getController().writeFormLabel(out,"filtraUnitaOrganizzativa");%></td>
				<td><% bp.getController().writeFormInput(out,null,"filtraUnitaOrganizzativa",!bp.isUoScrivaniaEnte(),null,null);%></td>
			</tr>
			<tr>
				<td><% bp.getController().writeFormLabel(out,"findUoForPrint");%></td>
				<td><% bp.getController().writeFormInput(out,null,"findUoForPrint",!bp.isUoScrivaniaEnte(),null,null);%></td>
			</tr>
            <tr><% bp.getController().writeFormField(out,"fromDataMovimento");%></tr>
            <tr><% bp.getController().writeFormField(out,"toDataMovimento");%></tr>
        </table>
	</div>
	<% bp.closeFormWindow(pageContext); %>
</body>