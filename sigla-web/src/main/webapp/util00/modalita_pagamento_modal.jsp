<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.action.*,
        it.cnr.jada.bulk.*,
        it.cnr.jada.util.action.*,
        it.cnr.jada.util.jsp.*,
        it.cnr.contab.anagraf00.tabrif.bulk.*,
        it.cnr.contab.anagraf00.core.bulk.*,
        it.cnr.contab.util00.bp.*,
        it.cnr.contab.util00.bulk.*"
%>
<%
    ModalBP bp = (ModalBP)BusinessProcess.getBusinessProcess(request);
    CambiaModalitaPagamentoBulk cambiaModalitaPagamento = (CambiaModalitaPagamentoBulk)bp.getModel();
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
                <div class="pb-2">
                    <div class="form-row">
                        <div class="form-group col-md-8 h-100">
                            <% bp.getController().writeFormLabel(out, "modalita_pagamento"); %>
                            <% bp.getController().writeFormInput(out,null,"modalita_pagamento",false,null,"onChange=\"submitForm('doOnModalitaPagamentoChange')\"");%>
                        </div>
                        <div class="form-group col-md-4 mt-4">
                            <% bp.getController().writeFormInput(out,null,"listabanche",false,null,"");%>
                        </div>
                    </div>
                </div>
                <%
                if (Rif_modalita_pagamentoBulk.BANCARIO.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento())) {
                    bp.getController().writeFormInput(out,"contoB");
                } else if (Rif_modalita_pagamentoBulk.POSTALE.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento())) {
                    bp.getController().writeFormInput(out,"contoP");
                } else if (Rif_modalita_pagamentoBulk.QUIETANZA.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento())) {
                    bp.getController().writeFormInput(out,"contoQ");
                } else if (Rif_modalita_pagamentoBulk.ALTRO.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento())) {
                    bp.getController().writeFormInput(out,"contoA");
                } else if (Rif_modalita_pagamentoBulk.IBAN.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento())) {
                    bp.getController().writeFormInput(out,"contoN");
                } else if (Rif_modalita_pagamentoBulk.BANCA_ITALIA.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento()) && cambiaModalitaPagamento.getBanca().isTABB()) {
                    bp.getController().writeFormInput(out,"contoB");
                } else if (Rif_modalita_pagamentoBulk.CONTO.equalsIgnoreCase(cambiaModalitaPagamento.getBanca().getTi_pagamento()) ) {
                     bp.getController().writeFormInput(out,"contoC");
                }

                %>
          </div>
          <div class="card-footer border bg-white">
            <input type="button" class="btn btn-outline-danger col-5 d-inline-block" name="comando.doAnnulla" value="Annulla" onclick="submitForm('doAnnulla')">
            <input type="button" class="btn btn-outline-primary col-5 d-inline-block pull-right" name="comando.doConferma" value="Conferma" onclick="submitForm('doConferma')">
          </div>
        </div>
    </div>
<% bp.closeForm(pageContext); %>
</body>