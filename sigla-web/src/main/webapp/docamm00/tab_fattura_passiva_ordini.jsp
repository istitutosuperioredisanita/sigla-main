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
	boolean rigaEnabled = fatturaOrdine!=null && fatturaOrdine.getAttiva();
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
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "voceIva"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "voceIva",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "prezzoUnitarioRett"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "prezzoUnitarioRett",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "sconto1Rett"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "sconto1Rett",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "sconto2Rett"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "sconto2Rett",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-2">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "sconto3Rett"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "sconto3Rett",!rigaEnabled,null,null); %>
                    </div>
                </div>
                <div class="form-row pt-2">
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "imImponibile"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "imImponibile",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "imImponibileRettificato"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "imImponibileRettificato",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "imIva"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "imIva",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-3">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "imIvaRettificata"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "imIvaRettificata",!rigaEnabled,null,null); %>
                    </div>
                </div>
                <div class="form-row pt-2">
                    <div class="form-group col-md-6">
                        <% bp.getFatturaOrdiniController().writeFormLabel(out, "imponibileErrato"); %>
                        <% bp.getFatturaOrdiniController().writeFormInput(out, null, "imponibileErrato",!rigaEnabled,null,null); %>
                    </div>
                    <div class="form-group col-md-6 h-100">
                        <% if (fatturaOrdine != null && fatturaOrdine.isRigaAttesaNotaCredito()) { %>
                            <% bp.getFatturaOrdiniController().writeFormLabel(out, "operazioneImpegnoNotaCredito"); %>
                            <% bp.getFatturaOrdiniController().writeFormInput(out, null, "operazioneImpegnoNotaCredito",!rigaEnabled,null,null); %>
                        <%}%>
                    </div>
                </div>
                <% if (!rigaEnabled) { %>
                <div class="form-group col-md-6 h-100">
                    <label class="col-form-label text-danger">
                        RIGA ANNULLATA!
                    </label>
                </div>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
</span>