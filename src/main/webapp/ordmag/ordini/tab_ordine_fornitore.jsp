<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.anag00.*,
		it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk,
		it.cnr.contab.ordmag.ordini.bp.CRUDOrdineAcqBP,
		it.cnr.contab.ordmag.ordini.*,
		it.cnr.contab.anagraf00.tabrif.bulk.*"
%>

<% CRUDBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
	OrdineAcqBulk ordine = (OrdineAcqBulk)bp.getModel();
	it.cnr.contab.anagraf00.core.bulk.TerzoBulk fornitore = ordine.getFornitore();
	boolean roOnAutoGen = false;
%>

<div class="Group card p-2">
    <div class="form-row">
        <div class="col-md-9"><% bp.getController().writeFormField(out, "findFornitore", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "cd_precedente", Boolean.FALSE);%></div>
    </div>
    <%	if (fornitore != null && fornitore.getAnagrafico() != null) {
            if ((fornitore.getAnagrafico().isStrutturaCNR() ||
                fornitore.getAnagrafico().isPersonaGiuridica() ||
                fornitore.getAnagrafico().isDittaIndividuale()) &&
                ordine.getRagioneSociale() != null &&
                ordine.getRagioneSociale().length() > 0) { %>
                <div class="form-row">
                    <div class="col-md-12">
                        <%	if (fornitore.getAnagrafico().isStrutturaCNR()) { %>
                            <span class="FormLabel">Nome</span>
                        <%	} else { %>
                            <%bp.getController().writeFormLabel(out,"ragioneSociale");%>
                        <% } %>
                        <%bp.getController().writeFormInput(out,"ragioneSociale");%>
                    </div>
                </div>
        <%	}
            if (fornitore.getAnagrafico().isPersonaFisica()) { %>
                <div class="form-row">
                    <div class="col-md-6"><% bp.getController().writeFormField(out, "cognome", Boolean.FALSE);%></div>
                    <div class="col-md-6"><% bp.getController().writeFormField(out, "nome", Boolean.FALSE);%></div>
                </div>
        <%	} %>
            <div class="form-row">
                <div class="col-md-12"><% bp.getController().writeFormField(out, "denominazione_sede", Boolean.FALSE);%></div>
            </div>
        <%	if (!fornitore.getAnagrafico().isStrutturaCNR()) { %>
            <div class="form-row">
                <% 	if (fornitore.getAnagrafico().isPersonaGiuridica() ||
                        fornitore.getAnagrafico().isDittaIndividuale()) { %>
                    <div class="col-md-6"><% bp.getController().writeFormField(out, "partitaIva", Boolean.FALSE);%></div>
                <%	} %>
                <div class="col-md-6"><% bp.getController().writeFormField(out,"codiceFiscale", Boolean.FALSE); %></div>
                <% if (bp.isSearching() || fornitore.getCrudStatus() != it.cnr.jada.bulk.OggettoBulk.NORMAL) {%>
                    <div class="col-md-6"><% bp.getController().writeFormField(out, "partitaIva", Boolean.FALSE);%></div>
                <%	} %>
            </div>
    <%		}
        } else { %>
            <div class="form-row">
                <div class="col-md-12"><% bp.getController().writeFormField(out, "ragioneSociale", Boolean.FALSE);%></div>
            </div>
            <div class="form-row">
                <div class="col-md-6"><% bp.getController().writeFormField(out, "nome", Boolean.FALSE);%></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out, "cognome", Boolean.FALSE);%></div>
            </div>
            <div class="form-row">
                <div class="col-md-6"><% bp.getController().writeFormField(out, "codiceFiscale", Boolean.FALSE);%></div>
                <div class="col-md-6"><% bp.getController().writeFormField(out, "partitaIva", Boolean.FALSE);%></div>
            </div>
    <%	} %>
      <div class="form-row">
        <div class="col-md-9"><% bp.getController().writeFormField(out,"via_fiscale");%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out,"num_civico");%></div>
      </div>
      <div class="form-row">
        <div class="col-md-9"><% bp.getController().writeFormField(out,"ds_comune");%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out,"ds_provincia");%></div>
      </div>
</div>
<%	if (fornitore != null && fornitore.getCd_terzo() != null) { %>
    <span class="FormLabel text-info h4">Modalità di Pagamento</span>
    <div class="Group card p-2">
        <div class="form-row">
            <div class="col-md-3 h-100">
                <% bp.getController().writeFormLabel(out,"terminiPagamento");%>
                <% bp.getController().writeFormInput(out,null,"terminiPagamento",roOnAutoGen,null,"");%>
            </div>
            <div class="col-md-6 h-100">
                <% bp.getController().writeFormLabel(out,"modalitaPagamento");%>
                <% bp.getController().writeFormInput(out,null,"modalitaPagamento",false,null,"onChange=\"submitForm('doOnModalitaPagamentoChange')\"");%>
            </div>
            <div class="col-md-3 h-100 pt-4">
                  <% if (ordine.getBanca() != null) {
                    bp.getController().writeFormInput(out, null, "listabanche", false, null, "");
                  } %>
            </div>
        </div>
        <div class="form-row p-2">
            <%	if (ordine.getBanca() != null) {
                    if (Rif_modalita_pagamentoBulk.BANCARIO.equalsIgnoreCase(ordine.getBanca().getTi_pagamento())) {
                        bp.getController().writeFormInput(out,"contoB");
                    } else if (Rif_modalita_pagamentoBulk.POSTALE.equalsIgnoreCase(ordine.getBanca().getTi_pagamento())) {
                        bp.getController().writeFormInput(out,"contoP");
                    } else if (Rif_modalita_pagamentoBulk.QUIETANZA.equalsIgnoreCase(ordine.getBanca().getTi_pagamento())) {
                        bp.getController().writeFormInput(out,"contoQ");
                    } else if (Rif_modalita_pagamentoBulk.ALTRO.equalsIgnoreCase(ordine.getBanca().getTi_pagamento())) {
                        bp.getController().writeFormInput(out,"contoA");
                    } else if (Rif_modalita_pagamentoBulk.IBAN.equalsIgnoreCase(ordine.getBanca().getTi_pagamento())) {
                        bp.getController().writeFormInput(out,"contoN");
                    } else if (Rif_modalita_pagamentoBulk.BANCA_ITALIA.equalsIgnoreCase(ordine.getBanca().getTi_pagamento()) && ordine.getBanca().isTABB()) {
                        bp.getController().writeFormInput(out,"contoB");
                    }
                } else if (ordine.getModalitaPagamento() != null && (ordine.getFornitore() != null && ordine.getFornitore().getCrudStatus() != ordine.getFornitore().UNDEFINED)) { %>
                    <span class="FormLabel text-danger" style="color:red">
                        Nessun riferimento trovato per la modalità di pagamento selezionata!
                    </span>
            <%	} %>
        </div>

    </div>
<% } %>