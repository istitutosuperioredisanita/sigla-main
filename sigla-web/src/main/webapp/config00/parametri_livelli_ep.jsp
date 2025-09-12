<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
  	    it.cnr.jada.UserContext,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.config00.bp.*,
		it.cnr.contab.config00.bulk.*,
		it.cnr.contab.config00.pdcep.cla.bulk.*"
%>

<%
	CRUDConfigParametriLivelliEPBP bp = (CRUDConfigParametriLivelliEPBP)BusinessProcess.getBusinessProcess(request);
    Parametri_livelli_epBulk bulk = ((Parametri_livelli_epBulk)bp.getModel());
%>

<html>

<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title>Parametri Livelli</title>
</head>
<%
	bp.openFormWindow(pageContext);
%>

<body class="Form">
<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-2"><% bp.getController().writeFormField(out, "esercizio");%></div>
        <div class="col-md-4 h-100"><% bp.getController().writeFormField(out, "tipo");%></div>
    </div>
    <% if (!bp.isSearching()) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "livelli");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=1) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello1");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello1");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=2) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello2");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello2");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=3) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello3");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello3");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=4) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello4");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello4");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=5) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello5");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello5");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=6) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello6");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello6");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()>=7) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello7");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello7");%></div>
    </div>
    <% } %>
    <% if (bulk!=null && bulk.getLivelli()!=null && bulk.getLivelli()==8) { %>
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "lung_livello8");%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "ds_livello8");%></div>
    </div>
    <% } %>
</div>

<%	bp.closeFormWindow(pageContext); %>
</body>