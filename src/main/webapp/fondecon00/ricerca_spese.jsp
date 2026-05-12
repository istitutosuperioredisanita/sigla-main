<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.fondecon00.bp.*,
		it.cnr.contab.fondecon00.core.bulk.*"
%>

<html>

<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title>Ricerca Spese del Fondo Economale</title>
</head>

<body class="Form">
<%	RicercaSpeseBP bp = (RicercaSpeseBP)BusinessProcess.getBusinessProcess(request);
	Filtro_ricerca_speseVBulk filtro = (Filtro_ricerca_speseVBulk)bp.getModel();

	bp.openFormWindow(pageContext); %>

	<div class="Group Panel card border-primary p-3 mb-2">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6"><% bp.getController().writeFormField(out,"pg_fondo_spesa"); %></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"fl_documentata"); %></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"im_ammontare_spesa"); %></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"dt_spesa"); %></div>
            </div>
    	</div>
    </div>

	<div class="Group Panel card border-primary p-3 mb-2">
    	<div class="card-body">
            <h5 class="card-title">Dati Fornitore</h5>
            <div class="form-row">
                <div class="col-md-12"><% bp.getController().writeFormField(out,"default","denominazione_fornitore"); %></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"default","codice_fiscale"); %></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"default","partita_iva"); %></div>
            </div>
    	</div>
	</div>

	<% bp.closeFormWindow(pageContext); %>
</body>