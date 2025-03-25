<!-- 
 ?ResourceName ""
 ?ResourceTimestamp ""
 ?ResourceEdition ""
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.coepcoan00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="JavaScript" src="scripts/disableRightClick.js"></script>
<% JSPUtils.printBaseUrl(pageContext);%>
</head>
<script language="javascript" src="scripts/css.js"></script>
<title>Ricerca movimenti contabili</title>
<body class="Form">

<%  
		RicercaMovContCoanBP bp = (RicercaMovContCoanBP)BusinessProcess.getBusinessProcess(request);
		bp.openFormWindow(pageContext); 
%>
<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "esercizio", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-8"><% bp.getController().writeFormField(out, "cds", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-8"><% bp.getController().writeFormField(out, "uo", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "pg_scrittura");%></div>
    </div>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "attiva", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "pg_scrittura_annullata", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "sezione");%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getController().writeFormField(out, "find_terzo", Boolean.FALSE);%></div>
        <div class="col-md-6 h-100"><% bp.getController().writeFormField(out, "find_voce_ana_searchtool", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getController().writeFormField(out, "find_linea_attivita");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "centro_responsabilita");%></div>
    </div>
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "im_movimento");%></div>
    </div>
</div>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html>