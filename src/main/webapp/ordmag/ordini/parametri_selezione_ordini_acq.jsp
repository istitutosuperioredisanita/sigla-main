<%@page import="it.cnr.contab.ordmag.ordini.bulk.ParametriSelezioneOrdiniAcqBulk"%>
<%@page import="it.cnr.contab.ordmag.ordini.bp.ParametriSelezioneOrdiniAcqBP"%>
<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>


<title>Parametri di Selezione Ordini</title>

</head>
<body class="Form">
<%	ParametriSelezioneOrdiniAcqBP bp = (ParametriSelezioneOrdiniAcqBP)BusinessProcess.getBusinessProcess(request);
	ParametriSelezioneOrdiniAcqBulk parametri = (ParametriSelezioneOrdiniAcqBulk)bp.getModel();
  bp.openFormWindow(pageContext);%>

	<div class="Group card p-2 mt-2" style="width:100%">
        <div class="form-row">
            <div class="col-md-6"><% bp.getController().writeFormField(out, "findUnitaOperativaOrd", Boolean.FALSE);%></div>
            <div class="col-md-6"><% bp.getController().writeFormField(out, "findMagazzino", Boolean.FALSE);%></div>
        </div>
	</div>

	<div class="Group card p-2">
        <div class="form-row form-group">
            <div class="col-md-2"><% bp.getController().writeFormField(out, "daDataOrdine", Boolean.FALSE);%></div>
            <div class="col-md-2"><% bp.getController().writeFormField(out, "aDataOrdine", Boolean.FALSE);%></div>
            <div class="col-md-8"><% bp.getController().writeFormField(out, "findUnitaOperativaRicevente", Boolean.FALSE);%></div>
        </div>
        <div class="form-row form-group">
            <div class="col-md-8"><% bp.getController().writeFormField(out, "findNumerazioneOrd", Boolean.FALSE);%></div>
            <div class="col-md-2"><% bp.getController().writeFormField(out, "daDataOrdineDef", Boolean.FALSE);%></div>
            <div class="col-md-2"><% bp.getController().writeFormField(out, "aDataOrdineDef", Boolean.FALSE);%></div>
        </div>
        <div class="form-row form-group">
            <div class="col-md-2"><% bp.getController().writeFormField(out, "daDataPrevConsegna", Boolean.FALSE);%></div>
            <div class="col-md-2"><% bp.getController().writeFormField(out, "aDataPrevConsegna", Boolean.FALSE);%></div>
            <div class="col-md-4"><% bp.getController().writeFormField(out, "findDaBeneServizio", Boolean.FALSE);%></div>
            <div class="col-md-4"><% bp.getController().writeFormField(out, "findABeneServizio", Boolean.FALSE);%></div>
        </div>
    </div>
	<div class="Group card p-2">
        <div class="form-row form-group">
            <div class="col-md-2"><% bp.getController().writeFormField(out, "daNumeroOrdine", Boolean.FALSE);%></div>
            <div class="col-md-2"><% bp.getController().writeFormField(out, "aNumeroOrdine", Boolean.FALSE);%></div>
            <div class="col-md-6"><% bp.getController().writeFormField(out, "findTerzo", Boolean.FALSE);%></div>
            <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "statoOrdine", Boolean.FALSE);%></div>
        </div>
        <div class="form-row form-group">
            <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "tipoConsegna", Boolean.FALSE);%></div>
            <div class="col-md-2 h-100"><% bp.getController().writeFormField(out, "statoConsegna", Boolean.FALSE);%></div>
            <div class="col-md-5"><% bp.getController().writeFormField(out, "findResponsabile", Boolean.FALSE);%></div>
            <div class="col-md-3"><% bp.getController().writeFormField(out, "findCup", Boolean.FALSE);%></div>
        </div>
        <div class="form-row form-group">
            <div class="col-md-4"><% bp.getController().writeFormField(out, "findCig", Boolean.FALSE);%></div>
            <div class="col-md-4"><% bp.getController().writeFormField(out, "findTipoOrdine", Boolean.FALSE);%></div>
            <div class="col-md-4"><% bp.getController().writeFormField(out, "findProceduraAmministrativa", Boolean.FALSE);%></div>
        </div>
    </div>
  	<div class="Group card p-2">
          <div class="form-row form-group">
            <div class="col-md-12"><% bp.getController().writeFormField(out, "findContratto", Boolean.FALSE);%></div>
        </div>
        <div class="form-row form-group">
            <div class="col-md-12"><% bp.getController().writeFormField(out, "findImpegno", Boolean.FALSE);%></div>
        </div>
	</div>
	<% bp.closeFormWindow(pageContext); %>
</body>