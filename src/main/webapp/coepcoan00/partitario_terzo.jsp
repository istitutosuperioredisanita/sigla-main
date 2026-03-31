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
<title>Partitario Terzo</title>

</head>
<body class="Form">
<%
    BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext);
%>
    <div class="Group card p-2">
        <div class="form-row">
            <div class="col-md-12"><% bp.getController().writeFormField(out,"terzo",Boolean.FALSE);%></div>
        </div>
        <div class="form-row">
            <div class="col-md-6"><% bp.getController().writeFormField(out,"cognome", Boolean.FALSE);%></div>
            <div class="col-md-6"><% bp.getController().writeFormField(out,"nome", Boolean.FALSE);%></div>
        </div>
        <div class="form-row">
            <div class="col-md-6"><% bp.getController().writeFormField(out,"codice_fiscale", Boolean.FALSE);%></div>
            <div class="col-md-6"><% bp.getController().writeFormField(out,"partita_iva", Boolean.FALSE);%></div>
        </div>
    </div>
    <div class="Group card p-2">
        <div class="form-inline">
            <div class="col-md-4">
                <% bp.getController().writeFormLabel(out,"dettaglioTributi");%>
                <span class="ml-2"><% bp.getController().writeFormInput(out,"dettaglioTributi");%></span>
            </div>
            <div class="col-md-4">
                <% bp.getController().writeFormLabel(out,"partite");%>
                <span class="ml-2 w-100"><% bp.getController().writeFormInput(out,"partite");%></span>
            </div>
            <div class="col-md-4">
                <% bp.getController().writeFormLabel(out,"toDataMovimento");%>
                <span class="ml-2"><% bp.getController().writeFormInput(out,"toDataMovimento");%></span>
            </div>
        </div>
    </div>
	<% bp.closeFormWindow(pageContext); %>
</body>