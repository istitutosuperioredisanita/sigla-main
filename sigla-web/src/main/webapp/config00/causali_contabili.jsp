<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.config00.bp.*"
%>

<%
	CRUDCausaliContabiliBP bp = (CRUDCausaliContabiliBP)BusinessProcess.getBusinessProcess(request);
%>

<html>

<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title><%=bp.getBulkInfo().getShortDescription()%></title>
</head>

<body class="Form">
<% bp.openFormWindow(pageContext);%>
    <div class="Group card p-2 m-1">
        <div class="form-row">
            <div class="col-md-3"><% bp.getController().writeFormField(out, "cdCausale", Boolean.FALSE);%></div>
            <div class="col-md-3 h-100"><% bp.getController().writeFormField(out, "cdTipoDocumentoAmm", Boolean.FALSE);%></div>
            <div class="col-md-3"><% bp.getController().writeFormField(out, "dtInizioValidita", Boolean.FALSE);%></div>
            <div class="col-md-3"><% bp.getController().writeFormField(out, "dtFineValidita", Boolean.FALSE);%></div>
        </div>
        <div class="form-row">
            <div class="col-md-12"><% bp.getController().writeFormField(out, "dsCausale", Boolean.FALSE);%></div>
        </div>
        <div class="form-row">
            <div class="col-md-2"><% bp.getController().writeFormField(out, "flStorno", Boolean.FALSE);%></div>
        </div>
    </div>
    <div class="card mt-2 w-100">
        <div class="card-header text-primary"><i class="fa fa-info-circle fa-fw fa-2x text-primary" aria-hidden="true"></i> <span class="h3">Conti associati</span></div>
        <div class="card-block p-1">
            <% bp.getAssCausaleVoceEPController().writeHTMLTable(pageContext,"causale",!bp.isSearching(),false,!bp.isSearching(),"100%","auto;max-height:50vh"); %>
            <div class="border rounded p-2 mt-1">
                <div class="form-row">
                  <div class="col-md-12"><% bp.getAssCausaleVoceEPController().writeFormField(out, "voce", Boolean.FALSE);%></div>
                </div>
                <div class="form-row">
                  <div class="col-md-12"><% bp.getAssCausaleVoceEPController().writeFormField(out, "tiSezione", Boolean.FALSE);%></div>
                </div>
            </div>
        </div>
    </div>
<% bp.closeFormWindow(pageContext); %>
</body>