<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.config00.pdcep.bulk.*,
		it.cnr.jada.util.jsp.*"
%>
<%
	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
%>
<html>
    <head>
        <% JSPUtils.printBaseUrl(pageContext); %>
        <title>Crea/Modica Ramo EP</title>
    </head>
<body class="Workspace">
<% bp.openForm(pageContext);%>
    <div class="col-md-8 mx-auto">
        <div class="card card-shadow">
          <h3 class="card-header">
            <i class="fa fa-edit fa-fw fa-2x text-info" aria-hidden="true"></i>
            <span>Inserire le informazioni del ramo</span>
            <% if (bp.getModel() != null && bp.getModel().isToBeCreated() && ((GruppoEPBulk)bp.getModel()).getCdGruppoPadre() != null ) {%>
                <span class="pl-1">figlio di </span><span class="text-primary h2"><%=GruppoEPBulk.ti_pianoGruppiKeys.get(((GruppoEPBulk)bp.getModel()).getCdPianoPadre())%> > <%=((GruppoEPBulk)bp.getModel()).getCdGruppoPadre()%>
            <% } %>
          </h3>
          <div class="card-block p-3">
            <% if (bp.getModel().isToBeCreated() && ((GruppoEPBulk)bp.getModel()).getCdGruppoPadre() == null ) {%>
                <div class="form-row">
                    <div class="col-md-12 h-100"><% bp.getController().writeFormField(out, "cdPianoGruppi", Boolean.FALSE);%></div>
                </div>
            <% } %>
            <div class="form-row">
                <div class="col-md-3"><% bp.getController().writeFormField(out, "cdGruppoEp", Boolean.FALSE);%></div>
                <div class="col-md-2"><% bp.getController().writeFormField(out, "sequenza", Boolean.FALSE);%></div>
                <div class="col-md-3"><% bp.getController().writeFormField(out, "nome", Boolean.FALSE);%></div>
                <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "segno", Boolean.FALSE);%></div>
                <div class="col-md-2 pt-4 mt-2"><% bp.getController().writeFormField(out, "flMastrino", Boolean.FALSE);%></div>
            </div>
            <div class="form-row">
                <div class="col-md-12"><% bp.getController().writeFormField(out, "dsGruppoEp", Boolean.FALSE);%></div>
            </div>
            <div class="form-row">
                <div class="col-md-12"><% bp.getController().writeFormField(out, "formula", Boolean.FALSE);%></div>
            </div>
          </div>
          <div class="card-footer bg-white">
            <input type="button" class="btn btn-outline-danger col-5 d-inline-block" name="comando.doAnnulla" value="Annulla" onclick="submitForm('doCloseForm')">
            <input type="button" class="btn btn-outline-primary col-5 d-inline-block pull-right" name="comando.doConferma" value="Conferma e Chiudi" onclick="submitForm('doSalvaAndChiudi')">
          </div>
        </div>
        <% bp.closeForm(pageContext); %>
    </div>
</body>