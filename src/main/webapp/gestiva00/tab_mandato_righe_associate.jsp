<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.gestiva00.bp.*"
%>

<%
   LiquidazioneDefinitivaIvaBP bp = (LiquidazioneDefinitivaIvaBP)BusinessProcess.getBusinessProcess(request);
   SimpleDetailCRUDController controller = bp.getMandato_righe_associate();
%>
<div class="container-fluid">
    <div class="row">
        <div class="ml-auto">
        <% JSPUtils.button(out,
            bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-2x fa-long-arrow-right faa-passing" : "img/doublerightarrow24.gif",
            bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-2x fa-long-arrow-right" : "img/doublerightarrow24.gif",
            "Visualizza Mandato",
            "javascript:submitForm('doVisualizzaMandato')",
            "btn-outline-primary btn-title faa-parent animated-hover",
            controller.getModel() != null,
            bp.getParentRoot().isBootstrap());
        %>
        </div>
    </div>
    <div class="row mt-1"><% controller.writeHTMLTable(pageContext,"liquidazione_definitiva",false,false,false,"100%","200px"); %></div>
</div>
