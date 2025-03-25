<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.util.jsp.*"
%>
<%
	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
%>
<html>
    <head>
        <% JSPUtils.printBaseUrl(pageContext); %>
        <title>Storna documenti</title>
    </head>
<body class="Workspace">
<% bp.openForm(pageContext);%>
    <div class="col-md-8 mx-auto">
        <div class="card card-shadow">
          <h3 class="card-header">
            <i class="fa fa-question-circle fa-fw fa-2x text-info" aria-hidden="true"></i> Inserire le informazioni necessarie allo storno
          </h3>
          <div class="card-block p-3">
            <div class="form-group row">
                <label class="col-sm-3 col-form-label"><% bp.getController().writeFormLabel(out,"causaleContabile"); %></label>
                <div class="col-sm-3 h-100">
                  <% bp.getController().writeFormInput(out,"causaleContabile"); %>
                </div>
                <label class="col-sm-3 col-form-label text-md-right pr-0"><% bp.getController().writeFormLabel(out,"dt_registrazione"); %></label>
                <div class="col-sm-3">
                  <% bp.getController().writeFormInput(out,"dt_registrazione");%>
                </div>
            </div>
            <div class="form-group row">
                <label class="col-sm-3 col-form-label"><% bp.getController().writeFormLabel(out,"tipoDocumentoGenerico"); %></label>
                <div class="col-sm-9">
                  <% bp.getController().writeFormInput(out,"tipoDocumentoGenerico");%>
                </div>
            </div>
            <div class="form-group row">
                <label class="col-sm-3 col-form-label"><% bp.getController().writeFormLabel(out,"descrizione"); %></label>
                <div class="col-sm-9">
                  <% bp.getController().writeFormInput(out,"descrizione");%>
                </div>
            </div>
            <div class="form-group row">
                <label class="col-sm-3 col-form-label"><% bp.getController().writeFormLabel(out,"dt_da_competenza_coge"); %></label>
                <div class="col-sm-3">
                  <% bp.getController().writeFormInput(out,"dt_da_competenza_coge");%>
                </div>
                <label class="col-sm-3 col-form-label text-md-right pr-0"><% bp.getController().writeFormLabel(out,"dt_a_competenza_coge"); %></label>
                <div class="col-sm-3">
                  <% bp.getController().writeFormInput(out,"dt_a_competenza_coge");%>
                </div>
            </div>
          </div>
          <div class="card-footer bg-white">
            <input type="button" class="btn btn-outline-danger col-5 d-inline-block" name="comando.doAnnulla" value="Annulla" onclick="submitForm('doAnnulla')">
            <input type="button" class="btn btn-outline-primary col-5 d-inline-block pull-right" name="comando.doConferma" value="Conferma" onclick="submitForm('doConferma')">
          </div>
        </div>
    </div>
<% bp.closeForm(pageContext); %>
</body>