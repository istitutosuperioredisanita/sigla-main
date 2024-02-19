<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.doccont00.core.bulk.*,
		it.cnr.contab.ordmag.ordini.bulk.*,
		it.cnr.contab.docamm00.bp.*"
%>

<%	CRUDFatturaPassivaBP bp = (CRUDFatturaPassivaBP)BusinessProcess.getBusinessProcess(request);
	Fattura_passivaBulk fatturaPassiva = (Fattura_passivaBulk)bp.getModel();
 	FatturaOrdineBulk fatturaOrdine = (FatturaOrdineBulk)bp.getFatturaOrdiniController().getModel();
	String collapseIconClass = bp.getFatturaOrdiniController().isRettificheCollapse() ? "fa-angle-down" : "fa-angle-up";
%>
<% bp.getCrudDocEleAcquistoColl().writeHTMLTable(pageContext,"default",false,false,false,"100%","10vh"); %>
<% bp.getCrudDocEleAcquistoColl().closeHTMLTable(pageContext);%>
<span class="py-1">
    <% bp.getCrudDocEleIvaColl().writeHTMLTable(pageContext,"default",false,false,false,"100%","10vh"); %>
    <% bp.getCrudDocEleIvaColl().closeHTMLTable(pageContext);%>
</span>

<% bp.getFatturaOrdiniController().writeHTMLTable(pageContext,"default",false,false,true,"100%","auto;max-height:50vh"); %>
<% bp.getFatturaOrdiniController().closeHTMLTable(pageContext);%>

<span class="pt-1">
    <div class="card">
        <div class="card-header d-flex">
            <a onclick="submitForm('doToggle(ordiniRettifiche)')" class="text-primary d-flex w-100">
                <span class="h4 mb-0">Rettifiche</span>
                <i aria-hidden="true" class="ml-auto fa fa-2x <%=collapseIconClass%>"></i>
            </a>
        </div>
        <% if (!bp.getFatturaOrdiniController().isRettificheCollapse()) { %>
        <div class="card-block p-2">
            <div>
                <div class="form-row">
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "voceIva", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "prezzoUnitarioRett", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "sconto1Rett", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "sconto2Rett", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "sconto3Rett", Boolean.FALSE); %>
                    </div>
                </div>
                <div class="form-row pt-2">
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "imImponibile", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "imImponibileRettificato", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "imIva", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "imIvaRettificata", Boolean.FALSE); %>
                    </div>
                </div>
                <div class="form-row pt-2">
                    <div class="form-group col-md-6">
                        <% bp.getFatturaOrdiniController().writeFormField(out, "imponibileErrato", Boolean.FALSE); %>
                    </div>
                    <div class="form-group col-md-6 h-100">
                        <% if (fatturaOrdine != null && fatturaOrdine.isRigaAttesaNotaCredito()) { %>
                            <% bp.getFatturaOrdiniController().writeFormField(out, "operazioneImpegnoNotaCredito", Boolean.FALSE); %>
                        <%}%>
                    </div>
                </div>
            </div>
        </div>
        <% } %>
    </div>
</span>