<%@ page pageEncoding="UTF-8"
	import = "it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.ordini.bp.CRUDOrdineAcqBP,
		it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk,
		it.cnr.contab.ordmag.anag00.*"
%>

<% CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
OrdineAcqBulk ordine = (OrdineAcqBulk)bp.getModel();
%>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-6"><% bp.getController().writeFormField(out, "findUnitaOperativaOrd", Boolean.FALSE);%></div>
        <div class="col-md-6"><% bp.getController().writeFormField(out, "findNumerazioneOrd", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-4"><% bp.getController().writeFormField(out, "findDivisa", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "cambio", Boolean.FALSE);%></div>
        <div class="col-md-5"><% bp.getController().writeFormField(out, "findTipoOrdine", Boolean.FALSE);%></div>
    </div>
</div>
<div class="h3 text-primary p-2 m-1">Dati del Contratto</div>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-12"><% bp.getController().writeFormField(out, "find_contratto", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-4"><% bp.getController().writeFormField(out, "figura_giuridica_interna", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "findFirmatario", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "findResponsabile", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-4"><% bp.getController().writeFormField(out, "findDirettore", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getController().writeFormField(out, "cig", Boolean.FALSE);%></div>
        <% if (bp.isVisibleMotivoAssenzaCig()) { %>
            <div class="col-md-4 h-100"><% bp.getController().writeFormField(out, "motivoAssenzaCig", Boolean.FALSE);%></div>
        <% } %>
    </div>
    <div class="form-row align-items-center">
        <div class="col-md-3"><% bp.getController().writeFormField(out, "procedura_amministrativa", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "cup", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "referenteEsterno", Boolean.FALSE);%></div>
        <div class="col-md-3 pt-4"><% bp.getController().writeFormField(out, "fl_mepa", Boolean.FALSE);%></div>
    </div>
</div>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-12"><% bp.getController().writeFormField(out, "nota", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-12"><% bp.getController().writeFormField(out, "findNotaPrecodificata", Boolean.FALSE);%></div>
    </div>
</div>
