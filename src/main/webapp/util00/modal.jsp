
<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.action.*,
        it.cnr.jada.bulk.*,
        it.cnr.jada.util.action.*,
        it.cnr.jada.util.jsp.*,
        it.cnr.contab.util00.bp.*,
        it.cnr.contab.util00.bulk.*"
%>

<%
    ModalBP bp = (ModalBP)BusinessProcess.getBusinessProcess(request);
%>
<html>
    <head>
        <% JSPUtils.printBaseUrl(pageContext); %>
        <title><%=bp.getBulkInfo().getShortDescription()%></title>
    </head>
<body class="Workspace">
<% bp.openForm(pageContext);%>
    <div class="<%=bp.getCssCard()%> mx-auto mt-3">
        <div class="card card-shadow">
          <h3 class="card-header">
            <i class="fa fa-question-circle fa-fw fa-2x text-info" aria-hidden="true"></i> <%=bp.getCardTitle()%>
          </h3>
          <div class="card-block p-3">
            <% bp.getController().writeForm(out,"default"); %>
          </div>
          <div class="card-footer border bg-white">
            <input type="button" class="btn btn-outline-danger col-5 d-inline-block" name="comando.doAnnulla" value="Annulla" onclick="submitForm('doAnnulla')">
            <input type="button" class="btn btn-outline-primary col-5 d-inline-block pull-right" name="comando.doConferma" value="Conferma" onclick="submitForm('doConferma')">
          </div>
        </div>
    </div>
<% bp.closeForm(pageContext); %>
</body>