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
	OrdineAcqRigaBulk riga = (OrdineAcqRigaBulk)bp.getRighe().getModel();
	String collapseIconClass = bp.isDettaglioContrattoCollapse() ? "fa-angle-down" : "fa-angle-up";
%>
<div class="Group p-2 mb-2">
    <div class="form-row">
        <div class="col-md-6"><% bp.getRighe().writeFormField(out, "findBeneServizio", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getRighe().writeFormField(out, "findUnitaMisura", Boolean.FALSE);%></div>
        <div class="col-md-2">
            <% bp.getRighe().writeFormLabel(out,"coefConv");%>
            <% bp.getRighe().writeFormInput(out,null,"coefConv",riga!=null&&riga.isROCoefConv(),null,""); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-12"><% bp.getRighe().writeFormField(out, "voce_iva", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-4"><% bp.getRighe().writeFormField(out, "prezzoUnitario", Boolean.FALSE);%></div>
        <div class="col-md-2"><% bp.getRighe().writeFormField(out, "dspQuantita", Boolean.FALSE);%></div>
        <div class="col-md-2"><% bp.getRighe().writeFormField(out, "sconto1", Boolean.FALSE);%></div>
        <div class="col-md-2"><% bp.getRighe().writeFormField(out, "sconto2", Boolean.FALSE);%></div>
        <div class="col-md-2"><% bp.getRighe().writeFormField(out, "sconto3", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-12"><% bp.getRighe().writeFormField(out, "notaRiga", Boolean.FALSE);%></div>
    </div>
    <% if (riga != null && riga.getDettaglioContratto() != null) { %>
        <div class="card border-info mt-2">
            <div class="card-header d-flex">
                <a onclick="submitForm('doToggle(dettaglioContratto)')" class="text-info d-flex w-100">
                    <span class="h4 mb-0">Dati Dettaglio Contratto</span>
                    <i aria-hidden="true" class="ml-auto fa fa-2x <%=collapseIconClass%>"></i>
                </a>
            </div>
            <% if (!bp.isDettaglioContrattoCollapse()) { %>
                <div class="card-block p-2">
                    <div class="form-row">
                        <div class="col-md-4"><% bp.getRighe().writeFormField(out, "quantitaMin", Boolean.FALSE); %></div>
                        <div class="col-md-4"><% bp.getRighe().writeFormField(out, "quantitaMax", Boolean.FALSE); %></div>
                        <div class="col-md-4"><% bp.getRighe().writeFormField(out, "quantitaOrdinata", Boolean.FALSE); %></div>
                    </div>
                </div>
            <% } %>
        </div>
    <% } %>
</div>
<div class="h3 text-primary mb-1">Consegna</div>
<div class="Group card border-primary p-2 mb-2">
    <div class="form-row">
        <div class="col-md-2 h-100"><% bp.getRighe().writeFormField(out, "tipoConsegna"); %></div>
        <div class="col-md-3"><% bp.getRighe().writeFormField(out, "dtPrevConsegna"); %></div>
        <div class="col-md-2 h-100"><% bp.getRighe().writeFormField(out, "statoConsegne"); %></div>
        <div class="col-md-5"><% bp.getRighe().writeFormField(out, "findMagazzino"); %></div>
    </div>
    <div class="form-row">
        <div class="col-md-6"><% bp.getRighe().writeFormField(out, "findLuogoConsegnaMag"); %></div>
        <div class="col-md-6"><% bp.getRighe().writeFormField(out, "findUnitaOperativaOrdDest"); %></div>
    </div>
    <div class="form-row">
        <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imImponibile"); %></div>
        <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imIva"); %></div>
        <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imIvaD"); %></div>
        <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imTotaleRiga"); %></div>
    </div>
    <% if (riga!=null && riga.isImportiConsegneModificati()) { %>
    <div class="h3 text-dark mb-1">Importi Rettificati</div>
    <div class="Group card border-dark p-2 mb-2">
        <div class="form-row">
            <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imImponibileConsegne"); %></div>
            <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imIvaConsegne"); %></div>
            <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imIvaDConsegne"); %></div>
            <div class="col-md-3"><% bp.getRighe().writeFormField(out, "imTotaleConsegne"); %></div>
        </div>
    </div>
    <% } %>
</div>