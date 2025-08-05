<%@page import="it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk"%>
<%@ page pageEncoding="UTF-8"
	import = "it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.ordini.bp.CRUDOrdineAcqBP,
		it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk,
		it.cnr.contab.ordmag.anag00.*"
%>

<%  
    CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
	OrdineAcqConsegnaBulk cons = (OrdineAcqConsegnaBulk)bp.getConsegne().getModel();
	bp.getConsegne().writeHTMLTable(pageContext,"consegneSetOrdine",true,false,true,"100%","auto");
%>

<div class="Group card p-2 mb-2">
    <div class="form-row">
        <div class="col-md-4"><% bp.getConsegne().writeFormField(out, "quantita"); %></div>
        <div class="col-md-4 h-100"><% bp.getConsegne().writeFormField(out, "tipoConsegna"); %></div>
        <div class="col-md-4"><% bp.getConsegne().writeFormField(out, "dtPrevConsegna"); %></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getConsegne().writeFormField(out, "findMagazzino"); %></div>
        <div class="col-md-6"><% bp.getConsegne().writeFormField(out, "findLuogoConsegnaMag"); %></div>
    </div>
    <% if (cons != null && cons.getTipoConsegna() != null && !cons.getTipoConsegna().equals("MAG")) { %>
        <div class="form-row">
            <div class="col-md-12"><% bp.getConsegne().writeFormField(out, "findUnitaOperativaOrdDest"); %></div>
        </div>
    <% } %>
    <% if (cons==null || !cons.isImportoConsegnaModificato()) { %>
        <div class="form-row">
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imImponibile"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIva"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIvaD"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imTotaleConsegna"); %></div>
        </div>
    <% } else { %>
        <div class="form-row">
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imImponibileConsegnaModificato"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIvaConsegnaModificato"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIvaDConsegnaModificato"); %></div>
            <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imTotaleConsegnaModificato"); %></div>
        </div>
        <div class="h3 text-dark mb-1">Importi Rettificati</div>
        <div class="Group card border-dark p-2 mb-2">
            <div class="form-row">
                <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imImponibile"); %></div>
                <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIva"); %></div>
                <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imIvaD"); %></div>
                <div class="col-md-3"><% bp.getConsegne().writeFormField(out, "imTotaleConsegna"); %></div>
            </div>
        </div>
    <% } %>
</div>